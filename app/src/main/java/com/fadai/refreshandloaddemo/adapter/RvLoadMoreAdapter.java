package com.fadai.refreshandloaddemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fadai.refreshandloaddemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : FaDai
 *     e-mail : i_fadai@163.com
 *     time   : 2017/05/09
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */

public class RvLoadMoreAdapter extends RecyclerView.Adapter<RvLoadMoreAdapter.MyViewHolder> {

    /**
     * 普通项和底部
     */
    private final int TYPE_ITEM=1;
    private final int TYPE_FOOTER=2;

    /**
     * 底部上拉加载的view有四种状态：
     * 初始时：隐藏（避免recyclerView的item不满一页时，上拉加载的view一直显示）
     * 第一次滑动后：显示（显示信息为正在加载）
     * 网络错误时：显示（显示信息为网络错误，点击重新加载）
     * 全部都已加载完时：显示（显示信息为已全部加载完毕）
     */
    public static final int STATE_FIRST=1;
    public static final int STATE_SHOW=2;
    public static final int STATE_ERROR_NET=3;
    public static final int STATE_ALL_LOADED=4;

    /**
     * 当前加载状态
     */
    private int mLoadState=1;

    /**
     *是否在加载中，加载中的话，屏蔽上拉加载事件
     */
    private boolean mIsLoading;

    /**
     * 底部上拉加载显示的View
     */
    private View mFootView;

    private OnListener mOnListener;

    private List<String>  mData;

    private Context mContext;



    /**
     * 是否加载中
     * */
    public boolean ismIsLoading() {
        return mIsLoading;
    }
    public void setmIsLoading(boolean mIsLoading) {
        this.mIsLoading = mIsLoading;
    }

    /**
     * 修改加载View的状态
     */
    public int getLoadState(){
        return mLoadState;
    }
    public void setmLoadState(boolean isError,boolean isAllLoaded,List<String> data){
        if(isError){
            mLoadState=STATE_ERROR_NET;
            notifyItemChanged(mData.size());
            return;
        }
        if(isAllLoaded){
            mLoadState=STATE_ALL_LOADED;
            notifyItemChanged(mData.size());
            return;
        }
        mLoadState=STATE_SHOW;
        if(data==null){
            notifyItemChanged(mData.size());
        } else {
            addData(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 控制显示 上拉加载View
     */
    public void showFooter(){
        mFootView.setVisibility(View.VISIBLE);
    }

    public void initState(){
        mLoadState=STATE_FIRST;
        if(mFootView!=null){
            mFootView.setVisibility(View.GONE);
        }
        notifyDataSetChanged();
    }

    /**
     * 事件*/
    public void setListener(OnListener onListener){
        mOnListener=onListener;
    }

    public RvLoadMoreAdapter(Context context){
        mContext=context;
        mData=new ArrayList<>();
    }



    public void setData(List<String> data){
        mData.clear();
        addData(data);
        // 初始化底部view的状态
        initState();
    }

    public void addData(List<String> data){
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position+1==getItemCount()){ // 到了底部
            return TYPE_FOOTER;
        }else {
            return TYPE_ITEM;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_ITEM){
            // 普通item
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_rv_test,parent,false);
            return new MyViewHolder(view);
        } else{
            // 底部
            mFootView=LayoutInflater.from(mContext).inflate(R.layout.item_first_footer,parent,false);
            return new MyViewHolder(mFootView);
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if(getItemViewType(position)==TYPE_ITEM){  // 普通项
            holder.tvTest.setText(mData.get(position));
        }else {      // 底部
            switch (mLoadState){  // 判断底部上拉加载的view的状态
                case STATE_FIRST:  // 初始状态 view为隐藏
                    mFootView.setVisibility(View.GONE);
                    holder.llLoading.setVisibility(View.VISIBLE);
                    holder.llLoadError.setVisibility(View.GONE);
                    holder.llLoadedAll.setVisibility(View.GONE);
                    break;
                case STATE_SHOW:
                    mFootView.setVisibility(View.VISIBLE);
                    holder.llLoading.setVisibility(View.VISIBLE);
                    holder.llLoadError.setVisibility(View.GONE);
                    holder.llLoadedAll.setVisibility(View.GONE);
                    break;
                case STATE_ERROR_NET:
                    mFootView.setVisibility(View.VISIBLE);
                    holder.llLoading.setVisibility(View.GONE);
                    holder.llLoadError.setVisibility(View.VISIBLE);
                    holder.llLoadedAll.setVisibility(View.GONE);
                    // 重新加载
                    if(mOnListener!=null){
                        holder.llLoadError.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOnListener.onReLoad();
                            }
                        });
                    }
                    break;
                case STATE_ALL_LOADED:
                    mFootView.setVisibility(View.VISIBLE);
                    holder.llLoading.setVisibility(View.GONE);
                    holder.llLoadError.setVisibility(View.GONE);
                    holder.llLoadedAll.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size()!=0?mData.size()+1:0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView tvTest;

        public LinearLayout llLoading;  // 正在加载
        public LinearLayout llLoadError; // 错误
        public LinearLayout llLoadedAll; // 全部加载完

        public MyViewHolder(View itemView) {
            super(itemView);
            // 判断是否是底部加载的view
            if(mFootView==null || itemView!=mFootView){
                tvTest=(TextView)itemView.findViewById(R.id.tv_item_test);
            }
            if(mFootView!=null && mFootView==itemView){
                llLoading=(LinearLayout)itemView.findViewById(R.id.ll_footer_loading);
                llLoadError=(LinearLayout)itemView.findViewById(R.id.ll_footer_error);
                llLoadedAll=(LinearLayout)itemView.findViewById(R.id.ll_footer_all_loaded);
            }

        }
    }

    // item点击事件和底部重新加载点击事件
    public interface OnListener{
        void onItemClick();
        void onReLoad();
    }
}
