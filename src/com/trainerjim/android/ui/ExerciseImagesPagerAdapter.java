package com.trainerjim.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.trainerjim.android.R;

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

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        Picasso.with(mContext)
                .load(String.format("%s%s",
                        mContext.getResources().getString(R.string.server_url),
                        mExerciseImages.get(position)))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .transform(new Transformation() {

                    @Override
                    public Bitmap transform(Bitmap source) {
                        /**
                         * This code rotates the image if it's height is bigger than width
                         * (as the app is used in the portrait orientation only)
                         */
                        int targetWidth = source.getWidth();
                        int targetHeight = source.getHeight();

                        if (targetHeight < targetWidth) {
                            Matrix matrix = new Matrix();

                            matrix.postRotate(90);


                            Bitmap rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

                            if (rotatedBitmap != source) {
                                source.recycle();
                            }

                            return rotatedBitmap;
                        }

                        return source;
                    }

                    @Override
                    public String key() {
                        return "transformation" + " desiredWidth";
                    }
                })
                .into(imageView);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}