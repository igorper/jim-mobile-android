package com.trainerjim.android.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trainerjim.android.R;
import com.trainerjim.android.entities.Training;
import com.trainerjim.android.events.EndExerciseEvent;
import com.trainerjim.android.events.EndRateTraining;
import com.trainerjim.android.events.StartRateTraining;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class RateTrainingFragment extends Fragment implements View.OnClickListener {


    private TextView mTrainingCommentText;

    private Training mCurrentTraining;

    private Context mContext;

    /**
     * Holds the ID of the currently selected training rating (or -1 if no
     * rating was yet selected).
     */
    private int mTrainingRatingSelectedID = -1;

    /**
     * Contains IDs of training rating images in non-selected (non-clicked)
     * state.
     */
    private static int[] TRAINING_RATING_IMAGES = { R.drawable.sm_1_ns,
            R.drawable.sm_2_ns, R.drawable.sm_3_ns };

    /**
     * Contains IDs of training rating images in selected (touched) state.
     */
    private static int[] TRAINING_RATING_SELECTED_IMAGES = { R.drawable.sm_1_s,
            R.drawable.sm_2_s, R.drawable.sm_3_s };

    /**
     * References to the ImageViews hosting training rating images.
     */
    private ImageView[] mTrainingRatingImages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.rate_training_fragment, container, false);

        mTrainingCommentText = (TextView) fragmentView.findViewById(R.id.textTrainingComment);

        // register click listeners
        fragmentView.findViewById(R.id.rate_training_button).setOnClickListener(this);
        fragmentView.findViewById(R.id.trainingRating1).setOnClickListener(this);
        fragmentView.findViewById(R.id.trainingRating2).setOnClickListener(this);
        fragmentView.findViewById(R.id.trainingRating3).setOnClickListener(this);

        EventBus.getDefault().register(this);

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);

        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rate_training_button:{
                onFinishClick();
                return;
            }
            case R.id.trainingRating1:
            case R.id.trainingRating2:
            case R.id.trainingRating3:
            {
                onTrainingRatingSelected(view);
                return;
            }
        }
    }


    /**
     * Triggered on finish button click in the training rating screen.
     *
     */
    public void onFinishClick() {
        // TODO: think about how fragments will communicate with each other
        if (mTrainingRatingSelectedID == -1) {
            Toast.makeText(mContext,
                    "You should rate the training.", Toast.LENGTH_SHORT).show();
        } else {
            mCurrentTraining.setTrainingRating(mTrainingRatingSelectedID);
            mCurrentTraining.setTrainingComment(mTrainingCommentText.getText()
                    .toString());

            EventBus.getDefault().post(new EndRateTraining());

            getView().setVisibility(View.GONE);
        }
    }

    /**
     * Triggered when clicked on a specific training rating icon.
     *
     * @param v
     *            The view hosting the clicked icon (of type ImageView).
     */
    public void onTrainingRatingSelected(View v) {
        ImageView trainingRatingSelected = (ImageView) v;

        int imageSelectedPadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_selected_padding);
        int imagePadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_padding);

        // loop through training ratings image views and set the appropriate
        // image
        for (int i = 0; i < mTrainingRatingImages.length; i++) {
            if (mTrainingRatingImages[i] == trainingRatingSelected) {
                mTrainingRatingImages[i]
                        .setImageResource(TRAINING_RATING_SELECTED_IMAGES[i]);
                mTrainingRatingImages[i].setPadding(imageSelectedPadding,
                        imageSelectedPadding, imageSelectedPadding,
                        imageSelectedPadding);
                mTrainingRatingSelectedID = i;
            } else {
                mTrainingRatingImages[i]
                        .setImageResource(TRAINING_RATING_IMAGES[i]);
                mTrainingRatingImages[i].setPadding(imagePadding, imagePadding,
                        imagePadding, imagePadding);
            }
        }
    }


    /**
     * Initializes a list of training rating ImageViews references.
     * Additionally, sets the non-selected images.
     */
    private void initializeTrainingRatings(View fragmentView) {
        mTrainingRatingSelectedID = -1;
        mTrainingCommentText.setText("");

        mTrainingRatingImages = new ImageView[TRAINING_RATING_IMAGES.length];
        mTrainingRatingImages[0] = (ImageView) fragmentView.findViewById(R.id.trainingRating1);
        mTrainingRatingImages[1] = (ImageView) fragmentView.findViewById(R.id.trainingRating2);
        mTrainingRatingImages[2] = (ImageView) fragmentView.findViewById(R.id.trainingRating3);

        int imagePadding = getResources().getDimensionPixelSize(
                R.dimen.training_rating_smile_padding);

        for (int i = 0; i < mTrainingRatingImages.length; i++) {
            mTrainingRatingImages[i]
                    .setImageResource(TRAINING_RATING_IMAGES[i]);
            mTrainingRatingImages[i].setPadding(imagePadding, imagePadding,
                    imagePadding, imagePadding);
        }
    }

    public void onEvent(StartRateTraining event){
        this.mCurrentTraining = event.getCurrentTraining();
        this.mContext = event.getApplicationContext();

        // initialize training rating icons and deselect them
        mTrainingRatingSelectedID = -1;
        initializeTrainingRatings(getView());

        getView().setVisibility(View.VISIBLE);
    }

}
