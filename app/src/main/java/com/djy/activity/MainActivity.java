package com.djy.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Button;

import com.djy.adapter.BaseRecyclerViewAdapter;
import com.djy.refreshorloadrecyclerview.R;
import com.djy.recyclerview.RefreshOrLoadRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MainActivity extends FragmentActivity {
    RefreshOrLoadRecyclerView refreshRecyclerView;
    List<String> list;
    BaseRecyclerViewAdapter<String> adapter;
    Button bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshRecyclerView = (RefreshOrLoadRecyclerView) findViewById(R.id.refreshrecyclerview);
        refreshRecyclerView.openLoadMore(true);
        refreshRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        refreshRecyclerView.setOnRefreshListener(new RefreshOrLoadRecyclerView.MyRecyclerViewOnRefreshListener() {
            @Override
            public void refresh() {
               new Handler(){
                   @Override
                   public void handleMessage(Message msg) {
                       adapter.add(0,"我是添加的条目"+refresh++);
                       refreshRecyclerView.onRefreshFinish();
                   }
               }.sendEmptyMessageDelayed(1,1000);
            }

            @Override
            public void loadMore() {
                new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        adapter.add(adapter.getItemCount(),"我是加载的条目"+load++);
                        refreshRecyclerView.onRefreshFinish();
                    }
                }.sendEmptyMessageDelayed(1,1000);
            }
        });
        bt = (Button) findViewById(R.id.bt);
        list = new ArrayList<>();
        for (int i = 0; i<30;i++) {
            list.add("我是第"+i+"条数据");
        }
        Log.i("tag",list.size()+"");
        adapter = new BaseRecyclerViewAdapter<String>(MainActivity.this,list) {
            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item;
            }

            @Override
            public void bindData(RecyclerViewHolder holder, int position, String itemData) {
                holder.getTextView(R.id.tv).setText(itemData);
            }
        };
        refreshRecyclerView.setAdapter(adapter);
    }
    int load = 1;
    int refresh = 1;
}
