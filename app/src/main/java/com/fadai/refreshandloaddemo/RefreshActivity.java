package com.fadai.refreshandloaddemo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.CheckBox;
import android.widget.Toast;

import com.fadai.refreshandloaddemo.adapter.RvLoadMoreAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : FaDai
 *     e-mail : i_fadai@163.com
 *     time   : 2017/04/27
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */

public class RefreshActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private Context mContext;

    private RecyclerView mRv;

    private SwipeRefreshLayout mSrl;

    private CheckBox mCbError,mCbLoadAll;

    private RvLoadMoreAdapter mAdapter;

    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_load);
        initView();
        mSrl.setRefreshing(true);
        onRefresh();
    }

    private boolean isUp;

    private void initView(){
        mCbError=(CheckBox)findViewById(R.id.cb_error);
        mCbLoadAll=(CheckBox)findViewById(R.id.cb_loaded_all);

        mRv=(RecyclerView)findViewById(R.id.rv_load_more);
        mAdapter=new RvLoadMoreAdapter(mContext);
        mLayoutManager=new LinearLayoutManager(mContext);
        mRv.setLayoutManager(mLayoutManager);
        mRv.setAdapter(mAdapter);

        mSrl=(SwipeRefreshLayout)findViewById(R.id.sr_load_more);
        mSrl.setOnRefreshListener(this);


        // recyclerView的item点击事件，和网络错误时重新加载的点击事件
        mAdapter.setListener(new RvLoadMoreAdapter.OnListener() {
            @Override
            public void onItemClick() { // item点击事件

            }

            @Override
            public void onReLoad() {  // 重新加载
                mAdapter.setmLoadState(false,false,null);
                mAdapter.setmIsLoading(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<String> data=initData();
                        Log.d("44444",state+"");
                        if(state==RvLoadMoreAdapter.STATE_ERROR_NET){
                            mAdapter.setmLoadState(true,false,null);
                        } else  if(state==RvLoadMoreAdapter.STATE_ALL_LOADED){
                            mAdapter.setmLoadState(false,true,null);
                        } else
                            mAdapter.setmLoadState(false,false,data);
                        mAdapter.setmIsLoading(false);
                    }
                },2000);
            }
        });

        // 监听滑动事件，判断是否是手指向上滑动，避免item不满一页时，下拉刷新触发上拉加载事件。
        mRv.setOnTouchListener(new View.OnTouchListener() {
            float oldy;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        oldy=event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //
                        if(oldy-event.getY()> ViewConfiguration.get(mContext).getScaledTouchSlop()){
                            isUp=true;
                        } else {
                            isUp=false;
                        }
                }
                return false; // 不拦截事件
            }
        });


        mRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItemPosition;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //正在滚动
                if(isUp && !mAdapter.ismIsLoading() && newState==RecyclerView.SCROLL_STATE_IDLE  &&
                        lastVisibleItemPosition+1==mAdapter.getItemCount()){  // 判断条件：手指上滑、当前不是正在加载的状态、停止滑动、末尾的item为最后一项
                    // 初始状态时，footer为隐藏，这里将其设为可见
                    mAdapter.showFooter();
                    if(mAdapter.getLoadState()==RvLoadMoreAdapter.STATE_FIRST){ //初始状态
                        mAdapter.setmLoadState(false,false,null);
                    }
                    // 只有显示状态时，才能响应滑动事件（不包括错误状态和加载完成状态）
                    if(mAdapter.getLoadState()==RvLoadMoreAdapter.STATE_SHOW){ // 显示状态
                        mAdapter.setmIsLoading(true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                List<String> data=initData();
                                if(state==RvLoadMoreAdapter.STATE_ERROR_NET){
                                    mAdapter.setmLoadState(true,false,null);
                                } else  if(state==RvLoadMoreAdapter.STATE_ALL_LOADED){
                                    mAdapter.setmLoadState(false,true,null);
                                } else
                                    mAdapter.setmLoadState(false,false,data);
                                mAdapter.setmIsLoading(false);
                            }
                        },2000);
                    }

                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItemPosition=mLayoutManager.findLastVisibleItemPosition();
            }
        });

    }

    private int num=0;
    private int state=1;
    private List<String> initData(){
        state=1;
        if(mCbLoadAll.isChecked()){
            state=RvLoadMoreAdapter.STATE_ALL_LOADED;
        }
        if(mCbError.isChecked()){
            state=RvLoadMoreAdapter.STATE_ERROR_NET;
        }
        if(state==RvLoadMoreAdapter.STATE_ERROR_NET){
            return null;
        }
        if(state==RvLoadMoreAdapter.STATE_ALL_LOADED){
            return null;
        }


        int n=num;
        num=n+7;
        List<String> data=new ArrayList<>();
        for(int i=n;i<num;i++){
            String str="测试数据"+i;
            data.add(str);
        }
        return data;

    }

    @Override
    public void onRefresh() {
        mCbError.setEnabled(false);
        mCbLoadAll.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                num=0;
                List<String> data=initData();
                if(state==RvLoadMoreAdapter.STATE_ERROR_NET){
                    Toast.makeText(RefreshActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                } else  if(state==RvLoadMoreAdapter.STATE_ALL_LOADED){
                    Toast.makeText(RefreshActivity.this,"服务器上没有数据了",Toast.LENGTH_SHORT).show();
                } else{
                    mAdapter.setData(data);
                }

                mAdapter.setmIsLoading(false);
                mSrl.setRefreshing(false);

                mCbError.setEnabled(true);
                mCbLoadAll.setEnabled(true);

            }
        },2000);

    }

}
