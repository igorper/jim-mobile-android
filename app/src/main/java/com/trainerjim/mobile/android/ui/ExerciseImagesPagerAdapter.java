package com.trainerjim.mobile.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.trainerjim.mobile.android.R;

import java.util.List;

public class ExerciseImagesPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    private List<String> mExerciseImages;

    public ExerciseImagesPagerAdapter(Context context, List<String> exerciseImages) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mExerciseImages = exerciseImages;
    }

    @Override
    public int getCount() {
        return mExerciseImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        DraweeController animatedGifController = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(
                        ImageRequestBuilder.newBuilderWithSource(Uri.parse(
                                String.format("%s%s",
                                        mContext.getResources().getString(R.string.server_url),
                                        mExerciseImages.get(position))))
                                //.setLowestPermittedRequestLevel(ImageRequest.RequestLevel.DISK_CACHE)
                                .build()
                )
                .build();
        SimpleDraweeView draweeView = (SimpleDraweeView) itemView.findViewById(R.id.imageView);
        draweeView.setController(animatedGifController);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}