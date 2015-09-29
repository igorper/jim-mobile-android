package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.EndOverviewEvent;
import com.trainerjim.mobile.android.events.EndTrainingEvent;
import com.trainerjim.mobile.android.ui.CircularProgressControl;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class OverviewTrainingFragment extends Fragment implements View.OnClickListener {

    private Training mCurrentTraining;

    private TextView mTextRectOneLine;
    private CircularProgressControl mCircularProgress;
    private TextView mSeriesInfoText;


    public OverviewTrainingFragment(){}

    // TODO: this is not the best pattern as if the fragment is killed and recreated by android
    // this constructor might not be called. In the future think about passing those args through
    // a bundle, but for now this should not be a problem.
    public OverviewTrainingFragment(Training training){
        this.mCurrentTraining = training;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.overview_view_fragment, container, false);

        mTextRectOneLine = (TextView)fragmentView.findViewById(R.id.text_no_trainings);
        mCircularProgress = (CircularProgressControl) fragmentView.findViewById(R.id.circularProgress);
        mSeriesInfoText = (TextView)fragmentView.findViewById(R.id.nextSeriesText);

        mCircularProgress.setOnClickListener(this);

        EventBus.getDefault().register(this);

        mCircularProgress.setNumberTotal(mCurrentTraining
                .getTotalTrainingDuration());
        mCircularProgress.setNumberActive(mCurrentTraining
                .getActiveTrainingDuration());

        mCircularProgress
                .setCurrentState(CircularProgressControl.CircularProgressState.OVERVIEW);

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
            case R.id.circularProgress: {
                EventBus.getDefault().post(new EndOverviewEvent());
                return;
            }
        }
    }

    public void onEvent(EndTrainingEvent event) {}
}
