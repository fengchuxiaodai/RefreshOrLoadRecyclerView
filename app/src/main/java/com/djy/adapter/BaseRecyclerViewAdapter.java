package com.djy.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JYcainiao on 2017/2/6.
 * RecyclerView的通用适配器
 */

public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter {

    /**
     * 数据集合  子类自己实现
     */
    protected final List<T> dataList;
    protected final Context mContext;//上下文
    private OnItemClickListener mClickListener;//item的点击监听
    private OnItemLongClickListener mLongClickListener;//item的长按监听
    LayoutInflater inflater;

    public BaseRecyclerViewAdapter(Context mContext,List<T> dataList){
        inflater = LayoutInflater.from(mContext);
        this.dataList = (dataList != null) ? dataList : new ArrayList<T>();
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerViewHolder holder = new RecyclerViewHolder(mContext,
                inflater.inflate(getItemLayoutId(viewType), parent, false));
        if (mClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            });
        }
        if (mLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mLongClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                    return true;
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindData((RecyclerViewHolder) holder, position, dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public T getItemData(int position){
        return dataList.get(position);
    }

    /**
     * 在指定位置添加item
     * @param pos   要添加的item位置
     * @param item  要添加的item数据
     */
    public void add(int pos, T item) {
        dataList.add(pos, item);
    }

    /**
     * 添加一个集合在指定位置
     * @param pos
     * @param list
     */
    public void addList(int pos,List<T> list){
        dataList.addAll(pos,list);
    }

    public  List<T> getListData(){
        return dataList;
    }

    /**
     * 删除指定位置的item
     * @param pos   要删除的item位置
     */
    public void delete(int pos) {
        dataList.remove(pos);
    }

    /**
     * 清空数据
     */
    public void deleteAll(){
        dataList.clear();
    }

    /**
     * 点击事件
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    /**
     * 长按事件
     * @param listener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    /**
     * 获取item的布局文件
     * @param viewType  item的布局的ID
     * @return  item的布局
     */
    abstract public int getItemLayoutId(int viewType);

    /**
     * 给item设置数据
     * @param holder    item的布局的viewholder
     * @param position  当前item的位置
     * @param itemData  当前item的数据
     */
    abstract public void bindData(RecyclerViewHolder holder, int position, T itemData);

    /**
     * 点击监听
     */
    public interface OnItemClickListener {
        public void onItemClick(View itemView, int pos);
    }

    /**
     * 长按监听
     */
    public interface OnItemLongClickListener {
        public void onItemLongClick(View itemView, int pos);
    }

    /**
     * viewholder 封装类
     */
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;//集合类，layout里包含的View,以view的id作为key，value是view对象
        private Context mContext;//上下文对象

        public RecyclerViewHolder(Context ctx, View itemView) {
            super(itemView);
            mContext = ctx;
            mViews = new SparseArray<View>();
        }

        private <T extends View> T findViewById(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (T) view;
        }

        public View getView(int viewId) {
            return findViewById(viewId);
        }

        public TextView getTextView(int viewId) {
            return (TextView) getView(viewId);
        }

        public Button getButton(int viewId) {
            return (Button) getView(viewId);
        }

        public ImageView getImageView(int viewId) {
            return (ImageView) getView(viewId);
        }

        public ImageButton getImageButton(int viewId) {
            return (ImageButton) getView(viewId);
        }

        public EditText getEditText(int viewId) {
            return (EditText) getView(viewId);
        }

        public RecyclerViewHolder setBackground(int viewId, int resId) {
            View view = findViewById(viewId);
            view.setBackgroundResource(resId);
            return this;
        }

        public RecyclerViewHolder setClickListener(int viewId, View.OnClickListener listener) {
            View view = findViewById(viewId);
            view.setOnClickListener(listener);
            return this;
        }
    }
}
