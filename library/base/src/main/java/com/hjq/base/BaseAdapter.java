package com.hjq.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.action.ResourcesAction;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/10/18
 *    desc   : RecyclerView 适配器技术基类
 */
public abstract class BaseAdapter<VH extends BaseAdapter<?>.ViewHolder>
        extends RecyclerView.Adapter<VH> implements ResourcesAction {

    /** 上下文对象 */
    private final Context mContext;

    /** RecyclerView 对象 */
    private RecyclerView mRecyclerView;

    /** 条目点击监听器 */
    @Nullable
    private OnItemClickListener mItemClickListener;
    /** 条目长按监听器 */
    @Nullable
    private OnItemLongClickListener mItemLongClickListener;

    /** 条目子 View 点击监听器 */
    @Nullable
    private SparseArray<OnChildClickListener> mChildClickListeners;
    /** 条目子 View 长按监听器 */
    @Nullable
    private SparseArray<OnChildLongClickListener> mChildLongClickListeners;

    /** ViewHolder 位置偏移值 */
    private int mPositionOffset = 0;

    public BaseAdapter(Context context) {
        mContext = context;
        if (mContext == null) {
            throw new IllegalArgumentException("are you ok?");
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position) {
        // 根据 ViewHolder 绑定的位置和传入的位置进行对比
        // 一般情况下这两个位置值是相等的，但是有一种特殊的情况
        // 在外层添加头部 View 的情况下，这两个位置值是不对等的
        mPositionOffset = position - holder.getAdapterPosition();
        holder.onBindView(position);
    }

    /**
     * 获取 RecyclerView 对象
     */
    @Nullable
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    /**
     * 条目 ViewHolder，需要子类 ViewHolder 继承
     */
    public abstract class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public ViewHolder(@LayoutRes int id) {
            this(LayoutInflater.from(getContext()).inflate(id, mRecyclerView, false));
        }

        public ViewHolder(View itemView) {
            super(itemView);

            // 设置条目的点击和长按事件
            if (mItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
            if (mItemLongClickListener != null) {
                itemView.setOnLongClickListener(this);
            }

            // 设置条目子 View 点击事件
            if (mChildClickListeners != null) {
                for (int i = 0; i < mChildClickListeners.size(); i++) {
                    View childView = findViewById(mChildClickListeners.keyAt(i));
                    if (childView != null) {
                        childView.setOnClickListener(this);
                    }
                }
            }

            // 设置条目子 View 长按事件
            if (mChildLongClickListeners != null) {
                for (int i = 0; i < mChildLongClickListeners.size(); i++) {
                    View childView = findViewById(mChildLongClickListeners.keyAt(i));
                    if (childView != null) {
                        childView.setOnLongClickListener(this);
                    }
                }
            }
        }

        /**
         * 数据绑定回调
         */
        public abstract void onBindView(int position);

        /**
         * 获取 ViewHolder 位置
         */
        protected final int getViewHolderPosition() {
            // 这里解释一下为什么用 getLayoutPosition 而不用 getAdapterPosition
            // 如果是使用 getAdapterPosition 会导致一个问题，那就是快速点击删除条目的时候会出现 -1 的情况，因为这个 ViewHolder 已经解绑了
            // 而使用 getLayoutPosition 则不会出现位置为 -1 的情况，因为解绑之后在布局中不会立马消失，所以不用担心在动画执行中获取位置有异常的情况
            return getLayoutPosition() + mPositionOffset;
        }

        /**
         * {@link View.OnClickListener}
         */

        @Override
        public void onClick(View view) {
            int position = getViewHolderPosition();
            if (position < 0 || position >= getItemCount()) {
                return;
            }

            if (view == getItemView()) {
                if(mItemClickListener != null) {
                    mItemClickListener.onItemClick(mRecyclerView, view, position);
                }
                return;
            }

            if (mChildClickListeners != null) {
                OnChildClickListener listener = mChildClickListeners.get(view.getId());
                if (listener != null) {
                    listener.onChildClick(mRecyclerView, view, position);
                }
            }
        }

        /**
         * {@link View.OnLongClickListener}
         */

        @Override
        public boolean onLongClick(View view) {
            int position = getViewHolderPosition();
            if (position < 0 || position >= getItemCount()) {
                return false;
            }

            if (view == getItemView()) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(mRecyclerView, view, position);
                }
                return false;
            }

            if (mChildLongClickListeners != null) {
                OnChildLongClickListener listener = mChildLongClickListeners.get(view.getId());
                if (listener != null) {
                    return listener.onChildLongClick(mRecyclerView, view, position);
                }
            }
            return false;
        }

        public final View getItemView() {
            return itemView;
        }

        public final <V extends View> V findViewById(@IdRes int id) {
            return getItemView().findViewById(id);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        // 判断当前的布局管理器是否为空，如果为空则设置默认的布局管理器
        if (mRecyclerView.getLayoutManager() == null) {
            RecyclerView.LayoutManager layoutManager = generateDefaultLayoutManager(mContext);
            if (layoutManager != null) {
                mRecyclerView.setLayoutManager(layoutManager);
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = null;
    }

    /**
     * 生成默认的布局摆放器
     */
    protected RecyclerView.LayoutManager generateDefaultLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }

    /**
     * 设置 RecyclerView 条目点击监听
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        checkRecyclerViewState();
        mItemClickListener = listener;
    }

    /**
     * 设置 RecyclerView 条目子 View 点击监听
     */
    public void setOnChildClickListener(@IdRes int id, @Nullable OnChildClickListener listener) {
        checkRecyclerViewState();
        if (mChildClickListeners == null) {
            mChildClickListeners = new SparseArray<>();
        }
        mChildClickListeners.put(id, listener);
    }

    /**
     * 设置 RecyclerView 条目长按监听
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener listener) {
        checkRecyclerViewState();
        mItemLongClickListener = listener;
    }

    /**
     * 设置 RecyclerView 条目子 View 长按监听
     */
    public void setOnChildLongClickListener(@IdRes int id, @Nullable OnChildLongClickListener listener) {
        checkRecyclerViewState();
        if (mChildLongClickListeners == null) {
            mChildLongClickListeners = new SparseArray<>();
        }
        mChildLongClickListeners.put(id, listener);
    }

    /**
     * 检查 RecyclerView 状态
     */
    private void checkRecyclerViewState() {
        if (mRecyclerView != null) {
            // 必须在 RecyclerView.setAdapter() 之前设置监听
            throw new IllegalStateException("are you ok?");
        }
    }

    /**
     * RecyclerView 条目点击监听类
     */
    public interface OnItemClickListener{

        /**
         * 当 RecyclerView 某个条目被点击时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param itemView          被点击的条目对象
         * @param position          被点击的条目位置
         */
        void onItemClick(RecyclerView recyclerView, View itemView, int position);
    }

    /**
     * RecyclerView 条目长按监听类
     */
    public interface OnItemLongClickListener {

        /**
         * 当 RecyclerView 某个条目被长按时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param itemView          被点击的条目对象
         * @param position          被点击的条目位置
         * @return                  是否拦截事件
         */
        boolean onItemLongClick(RecyclerView recyclerView, View itemView, int position);
    }

    /**
     * RecyclerView 条目子 View 点击监听类
     */
    public interface OnChildClickListener {

        /**
         * 当 RecyclerView 某个条目 子 View 被点击时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param childView         被点击的条目子 View
         * @param position          被点击的条目位置
         */
        void onChildClick(RecyclerView recyclerView, View childView, int position);
    }

    /**
     * RecyclerView 条目子 View 长按监听类
     */
    public interface OnChildLongClickListener {

        /**
         * 当 RecyclerView 某个条目子 View 被长按时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param childView         被点击的条目子 View
         * @param position          被点击的条目位置
         */
        boolean onChildLongClick(RecyclerView recyclerView, View childView, int position);
    }
}