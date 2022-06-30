package com.hjq.demo.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.demo.R;
import com.hjq.demo.app.AppAdapter;
import com.hjq.demo.http.glide.GlideApp;

import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/07/24
 *    desc   : 图片选择适配器
 */
public final class ImageSelectAdapter extends AppAdapter<String> {

    private final List<String> mSelectImages;

    public ImageSelectAdapter(Context context, List<String> images) {
        super(context);
        mSelectImages = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder();
    }

    private final class ViewHolder extends AppAdapter<?>.ViewHolder {

        private final ImageView mImageView;
        private final CheckBox mCheckBox;

        private ViewHolder() {
            super(R.layout.image_select_item);
            mImageView = findViewById(R.id.iv_image_select_image);
            mCheckBox = findViewById(R.id.iv_image_select_check);
        }

        @Override
        public void onBindView(int position) {
            String imagePath = getItem(position);
            GlideApp.with(getContext())
                    .asBitmap()
                    .load(imagePath)
                    .into(mImageView);

            mCheckBox.setChecked(mSelectImages.contains(imagePath));
        }
    }

    @Override
    protected RecyclerView.LayoutManager generateDefaultLayoutManager(Context context) {
        return new GridLayoutManager(context, 3);
    }
}