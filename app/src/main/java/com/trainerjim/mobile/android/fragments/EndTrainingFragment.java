package com.trainerjim.mobile.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trainerjim.mobile.android.R;
import com.trainerjim.mobile.android.entities.Training;
import com.trainerjim.mobile.android.events.EndTrainingEvent;
import com.trainerjim.mobile.android.ui.CircularProgressControl;

import de.greenrobot.event.EventBus;

/**
 * Created by igor on 23.05.15.
 */
public class EndTrainingFragment extends Fragment implements View.OnClickListener {
    private Training mCurrentTraining;

    private CircularProgressControl mCircularProgress;

    private Handler mUiHandler = new Handler();
    public EndTrainingFragment(){}

    // TODO: this is not the best pattern as if the fragment is killed and recreated by android
    // this constructor might not be called. In the future think about passing those args through
    // a bundle, but for now this should not be a problem.
    public EndTrainingFragment(Training training){
        this.mCurrentTraining = training;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.end_view_fragment, container, false);

        mCircularProgress = (CircularProgressControl)fragmentView.findViewById(R.id.circularProgress);
        mCircularProgress.setCurrentState(CircularProgressControl.CircularProgressState.STOP);

        mCircularProgress.setOnClickListener(this);

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
            case R.id.circularProgress: {
                mCurrentTraining.endTraining();
                EventBus.getDefault().post(new EndTrainingEvent(mCurrentTraining));

                return;
            }
        }
    }

    public void onEvent(EndTrainingEvent event){
    }
}
