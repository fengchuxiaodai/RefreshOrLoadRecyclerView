package com.djy.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.djy.refreshorloadrecyclerview.R;


/**
 * Created by JYcainiao on 2017/2/20.
 */

public class RefreshOrLoadRecyclerView extends LinearLayout {
    /**
     * 当前下拉的状态
     */
    private int mCurrFootViewDownLoadMoreState = DEFAULT_STATE;
    /**
     * 当前上拉的状态
     */
    private int mCurrHeadViewUpRefreshState = DEFAULT_STATE;
    /**
     * 默认状态
     */
    public static final int DEFAULT_STATE = -1;
    /**
     * 下拉刷新
     */
    public static final int HEAD_TO_REFRESH = 0;
    /**
     * 松开刷新
     */
    public static final int HEAD_RELESE_REFRESH = 1;
    /**
     * 正在刷新
     */
    public static final int HEAD_REFRESHING = 2;

    /**
     * 上拉加载
     */
    public static final int FOOT_TO_LOAD = 0;
    /**
     * 松开加载
     */
    public static final int FOOT_RELESE_LOAD = 1;
    /**
     * 加载中
     */
    public static final int FOOT_LOADING = 2;

    /**
     * 箭头动画
     */
    private RotateAnimation arrowRotate;

    /**
     * 加载中动画
     */
    private RotateAnimation progressBarRotate;


    private Context mContext;
    int height;
    private RelativeLayout layout;//最外层布局

    //头布局相关  xml文件中可以修改
    private RelativeLayout headView;//头布局 - 下拉刷新
    private int headerHeigh;//头布局的高度
    private ImageView headLoadRefreshImage;//刷新中
    private ImageView headToRefreshImage;//下拉中
    private TextView headToRefreshText;//下拉时的文本框

    //尾布局相关  xml文件中可以修改
    private RelativeLayout footView;//脚布局 - 上拉加载
    private int footerHeigh;//脚布局的高度
    private ImageView footLoadRefreshImage;//加载中
    private ImageView footToLoadImage;//上拉的时候
    private TextView footToRefreshText;//文本框

    private long toRefreshTime;
    private long refreshedTime;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private boolean loadMore = false;//加载更多
    /**
     * 是否为加载更多
     */
    private boolean isLoadMore;

    public RefreshOrLoadRecyclerView(Context context) {
        super(context);
        mCurrHeadViewUpRefreshState = DEFAULT_STATE;
    }

    public RefreshOrLoadRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.myrefreshrecyclerviewtest,this,true);
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();

        layout = (RelativeLayout) view.findViewById(R.id.layout);
        headView = (RelativeLayout) view.findViewById(R.id.rl_head);
        headLoadRefreshImage = (ImageView) view.findViewById(R.id.head_load_reflash);
        headToRefreshImage = (ImageView) view.findViewById(R.id.head_to_refresh_image);
        headToRefreshText = (TextView) view.findViewById(R.id.head_to_refresh_text);

        footView  = (RelativeLayout) view.findViewById(R.id.rl_foot);
        footLoadRefreshImage = (ImageView) view.findViewById(R.id.foot_load_reflash_image);
        footToLoadImage = (ImageView) view.findViewById(R.id.foot_to_load_image);
        footToRefreshText = (TextView) view.findViewById(R.id.foot_to_load_text);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setOnTouchListener(recyclerViewOnTouchListener);


        headView.measure(0, 0);
        headerHeigh = headView.getMeasuredHeight();
        footView.measure(0, 0);
        footerHeigh = footView.getMeasuredHeight();

        initAnima();
        mCurrHeadViewUpRefreshState = DEFAULT_STATE;
    }

    /**
     * 开始时Y轴位置
     */
    private int startY;

    /**
     * 临时解决下拉加载的bug   表现为  脚布局被拉出来后  会被判断为可以想上滑动 recyclerView.canScrollVertically(1) 值为true，先加个变量控制一下
     */
    private boolean isLast;
    private boolean isFirst;

    private OnTouchListener recyclerViewOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                //按下
                case MotionEvent.ACTION_DOWN:
                    startY = (int) motionEvent.getRawY();
                    break;
                //移动
                case MotionEvent.ACTION_MOVE:
                    if (startY == 0 || startY == -1) {
                        startY = (int) motionEvent.getRawY();
                    }
                    int endY = (int) motionEvent.getRawY();
                    int move = Math.abs(endY-startY);
                    if (mCurrFootViewDownLoadMoreState == FOOT_LOADING ){

                        return true;
                    } else if ( mCurrHeadViewUpRefreshState == HEAD_REFRESHING) {

                        return true;
                    } else if (mCurrFootViewDownLoadMoreState != FOOT_LOADING && mCurrHeadViewUpRefreshState != HEAD_REFRESHING) {
                        if (endY - startY > 0 && (!recyclerView.canScrollVertically(-1) || isFirst) && !isLast) {
                            isFirst = true;
                            //下拉刷新
                            isLoadMore = false;
                            headView.setVisibility(VISIBLE);
                            int padding = move - headerHeigh;
                            if (padding > 0) {
                                padding = padding / 3;
                            }
                            layout.setPadding(0, padding, 0, 0);
                            Log.i("tag", "moving");
                            recyclerView.smoothScrollToPosition(0);
                            if (padding < 0) {
                                if (mCurrHeadViewUpRefreshState != HEAD_TO_REFRESH) {
                                    mCurrHeadViewUpRefreshState = HEAD_TO_REFRESH;
                                    headViewRefreshState();
                                }
                            } else if (padding >= 0) {
                                if (mCurrHeadViewUpRefreshState == HEAD_TO_REFRESH) {
                                    mCurrHeadViewUpRefreshState = HEAD_RELESE_REFRESH;
                                    headViewRefreshState();
                                }
                            }
                        } else if (endY - startY < 0 && (!recyclerView.canScrollVertically(1) || isLast) && !isFirst) {
                            if (loadMore) {
                                isLast = true;
                                //上拉加载
                                isLoadMore = true;
                                footView.setVisibility(VISIBLE);
                                int padding = move - footerHeigh;
                                if (padding > 0) {
                                    padding = padding / 3;
                                }
                                layout.setPadding(0, 0, 0, padding);
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() > 0 ? adapter.getItemCount() - 1 : 0);
                                if (padding < 0) {
                                    if (mCurrFootViewDownLoadMoreState != FOOT_TO_LOAD) {
                                        mCurrFootViewDownLoadMoreState = FOOT_TO_LOAD;
                                        footViewLoadMoreState();
                                    }
                                } else if (padding >= 0) {
                                    if (mCurrFootViewDownLoadMoreState == FOOT_TO_LOAD) {
                                        mCurrFootViewDownLoadMoreState = FOOT_RELESE_LOAD;
                                        footViewLoadMoreState();
                                    }
                                }
                            }
                        }
                    }
                    break;
                //抬起
                case MotionEvent.ACTION_UP:
                    startY = 0;
                    if (isLoadMore) {
                        if (mCurrFootViewDownLoadMoreState == FOOT_RELESE_LOAD) {
                            mCurrFootViewDownLoadMoreState = FOOT_LOADING;
                            footViewLoadMoreState();
                            layout.setPadding(0, 0, 0, 0);
                        } else if (mCurrFootViewDownLoadMoreState == FOOT_TO_LOAD) {
                            layout.setPadding(0,0,0,-footerHeigh);
                            mCurrFootViewDownLoadMoreState = DEFAULT_STATE;
                        } else{
                            //没有上面两种情况  默认恢复原始布局
                            if (mCurrFootViewDownLoadMoreState != FOOT_LOADING)
                            footViewGone();
                        }
                    } else {
                        if (mCurrHeadViewUpRefreshState == HEAD_RELESE_REFRESH){
                            mCurrHeadViewUpRefreshState = HEAD_REFRESHING;
                            headViewRefreshState();
                            layout.setPadding(0, 0, 0, 0);
                        } else if (mCurrHeadViewUpRefreshState == HEAD_TO_REFRESH) {
                            layout.setPadding(0,-headerHeigh,0,0);
                            mCurrHeadViewUpRefreshState = DEFAULT_STATE;
                        }else{
                            //没有上面两种情况  默认恢复原始布局
                            if (mCurrHeadViewUpRefreshState!=HEAD_REFRESHING)
                            headViewGone();
                        }
                    }
                    break;
            }
            return false;
        }
    };

    private void headViewGone() {
        isFirst = false;
        headLoadRefreshImage.setVisibility(GONE);
        headToRefreshImage.setVisibility(GONE);
        headToRefreshText.setVisibility(GONE);
        headView.setVisibility(GONE);
        mCurrHeadViewUpRefreshState = DEFAULT_STATE;
        headLoadRefreshImage.clearAnimation();
        headToRefreshText.setText("下拉刷新");
        layout.setPadding(0, 0, 0, 0);//设置布局padding
    }

    private void footViewGone() {
        isLast = false;
        footToLoadImage.setVisibility(GONE);
        footLoadRefreshImage.setVisibility(GONE);
        footToRefreshText.setVisibility(GONE);
        footView.setVisibility(GONE);
        mCurrFootViewDownLoadMoreState = DEFAULT_STATE;
        footToRefreshText.setText("上拉加载更多");
        footLoadRefreshImage.clearAnimation();
        layout.setPadding(0, 0, 0, 0);//设置布局padding
    }

    public RefreshOrLoadRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public void setAdapter(RecyclerView.Adapter adpater) {
        this.adapter = adpater;
        recyclerView.setAdapter(adpater);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layout) {
        layoutManager = layout;
        recyclerView.setLayoutManager(layout);
    }

    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        recyclerView.setItemAnimator(animator);
    }

    /**
     * 头布局刷新状态
     */
    private void headViewRefreshState(){
        switch (mCurrHeadViewUpRefreshState) {
            //下拉中
            case HEAD_TO_REFRESH:
                headToRefreshText.setVisibility(VISIBLE);
                headToRefreshText.setText("下拉刷新");
                headToRefreshImage.setVisibility(VISIBLE);
                headToRefreshImage.clearAnimation();
                break;
            case HEAD_RELESE_REFRESH:
                headToRefreshText.setText("松开刷新");
                headToRefreshImage.startAnimation(arrowRotate);
                break;
            //刷新中
            case HEAD_REFRESHING:
                toRefreshTime = System.currentTimeMillis();
                long time = toRefreshTime - refreshedTime;
                if (refreshedTime > 0){
                    headToRefreshText.setText(long2Time(time) == null ? "正在刷新" : "上次刷新时间:\r\n"+long2Time(time));
                }else{
                    headToRefreshText.setText("正在刷新");
                }
                headToRefreshImage.setVisibility(GONE);
                headLoadRefreshImage.setVisibility(VISIBLE);
                //清除箭头动画
                headToRefreshImage.clearAnimation();
                //开始加载动画
                headLoadRefreshImage.startAnimation(progressBarRotate);
                if (onRefreshListener!=null){
                    onRefreshListener.refresh();
                }
                break;
        }
    }

    /**
     * 计算时差
     * @param time
     * @return
     */
    private String long2Time(long time) {
        String str= "";
        long second = time / 1000;
        long minute = second / 60;
        long hour = minute / 60;
        if (hour > 0){
            return hour+"小时前";
        }else if (minute < 60 && second >=60){
            return minute+"分钟前";
        } else if (second > 0) {
            return second+"秒前";
        }
        return null;
    }

    /**
     * 记录加载之前的位置，加载完成后定位
     */
    private int preAdapterItemCount;

    private void footViewLoadMoreState(){
        switch (mCurrFootViewDownLoadMoreState) {
            //上拉中
            case FOOT_TO_LOAD:
                footToRefreshText.setVisibility(VISIBLE);
                footToRefreshText.setText("上拉加载更多");
                footToLoadImage.setVisibility(VISIBLE);
                footToLoadImage.clearAnimation();
                break;
            //上拉距离过大
            case FOOT_RELESE_LOAD:
                footToRefreshText.setText("松开加载");
                footToLoadImage.startAnimation(arrowRotate);
                break;
            //加载中
            case FOOT_LOADING:
                footToRefreshText.setText("正在加载");
                footToLoadImage.setVisibility(GONE);
                footLoadRefreshImage.setVisibility(VISIBLE);
                //关闭箭头动画
                footToLoadImage.clearAnimation();
                //开启加载动画
                footLoadRefreshImage.startAnimation(progressBarRotate);
                if (onRefreshListener!=null){
                    preAdapterItemCount = adapter.getItemCount();
                    onRefreshListener.loadMore();
                }
                break;
        }
    }

    /**
     * 上拉刷新、下拉加载完成状态
     */
    public void onRefreshFinish(){
        if (isLoadMore){
            //上拉加载
            footToLoadImage.setVisibility(GONE);
            footLoadRefreshImage.setVisibility(GONE);
            footToRefreshText.setVisibility(GONE);
            footView.setVisibility(GONE);
            mCurrFootViewDownLoadMoreState = DEFAULT_STATE;
            footToRefreshText.setText("上拉加载更多");
            footLoadRefreshImage.clearAnimation();
            isLoadMore = false;
            layout.setPadding(0, 0, 0, 0);//设置布局padding
            recyclerView.smoothScrollToPosition(preAdapterItemCount > 0 ? preAdapterItemCount - 1 : 0);
            if (preAdapterItemCount == recyclerView.getAdapter().getItemCount()){
                Toast.makeText(mContext, "没有数据了", Toast.LENGTH_SHORT).show();
            }
        }else{
            //下拉刷新
            refreshedTime = System.currentTimeMillis();
            headLoadRefreshImage.setVisibility(GONE);
            headToRefreshImage.setVisibility(GONE);
            headToRefreshText.setVisibility(GONE);
            headView.setVisibility(GONE);
            mCurrHeadViewUpRefreshState = DEFAULT_STATE;
            headLoadRefreshImage.clearAnimation();
            headToRefreshText.setText("下拉刷新");
            layout.setPadding(0, 0, 0, 0);//设置布局padding
            Log.i("tag","onRefreshFinish");
            recyclerView.smoothScrollToPosition(0);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        isLast = false;
        isFirst = false;
    }



    /**
     * 初始化动画
     */
    private void initAnima(){

        //加载中动画
        progressBarRotate = new RotateAnimation(0, 359, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        progressBarRotate.setDuration(1000);
        progressBarRotate.setRepeatCount(-1);

        //箭头动画
        arrowRotate =  new RotateAnimation(0, 180, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        arrowRotate.setDuration(500);
        arrowRotate.setFillAfter(true);
    }

    /**
     * 创建箭头旋转动画
     * @param i
     */
    private void createArrowAnima(int i){
        arrowRotate  = new RotateAnimation(0, i, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        arrowRotate.setFillAfter(!arrowRotate.getFillAfter());
        arrowRotate.setDuration(1);
    }

    /**
     * 是否开启上啦加载更多  默认关闭
     *
     * @param isopen
     */
    public void openLoadMore(boolean isopen) {
        loadMore = isopen;
    }

    /**
     * 刷新监听
     */
    private MyRecyclerViewOnRefreshListener onRefreshListener;

    /**
     * 设置监听
     * @param listener
     */
    public void setOnRefreshListener(MyRecyclerViewOnRefreshListener listener){
        this.onRefreshListener = listener;
    }

    /**
     * 刷新监听接口
     */
    public interface MyRecyclerViewOnRefreshListener{
        void refresh();
        void loadMore();
    }

}
