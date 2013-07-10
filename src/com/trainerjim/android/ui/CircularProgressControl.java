package com.trainerjim.android.ui;

import net.pernek.jim.exercisedetector.R;

import com.trainerjim.android.util.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.net.NetworkInfo.State;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * A circular progress control. NOTE: At this point this button ignores the
 * padding property (this should be added in the future) and the height property
 * (height gets set to the same values as width).
 * 
 * @author Igor
 * 
 */
public class CircularProgressControl extends View {
	/**
	 * Contains possible states for the {@link CircularProgressControl}
	 * 
	 * @author Igor
	 * 
	 */
	public enum CircularProgressState {
		START, REST, EXERCISE, STOP, OVERVIEW
	}

	private static final String TAG = Utils.getApplicationTag();

	/**
	 * Info button background paint.
	 */
	private Paint mInfoButtonBackgroundPaint;

	/**
	 * Info button large text paint.
	 */
	private Paint mInfoButtonLargeTextPaint;

	/**
	 * Info button small text paint.
	 */
	private Paint mInfoButtonSmallTextPaint;

	/**
	 * Training progress bar background paint.
	 */
	private Paint mTrainingProgressBackgroundPaint;

	/**
	 * Training progress bar foreground paint.
	 */
	private Paint mTrainingProgressForegroundPaint;

	/**
	 * Exercise progress bar background paint.
	 */
	private Paint mExerciseProgressBackgroundPaint;

	/**
	 * Exercise progress bar foreground paint.
	 */
	private Paint mExerciseProgressForegroundPaint;

	/**
	 * Rest progress bar background paint.
	 */
	private Paint mRestProgressBackgroundPaint;

	/**
	 * Rest progress bar foreground paint.
	 */
	private Paint mRestProgressForegroundPaint;

	/**
	 * Rest progress bar click background paint.
	 */
	private Paint mRestProgressClickBackgroundPaint;

	/**
	 * Rest progress bar click foreground paint.
	 */
	private Paint mRestProgressClickForegroundPaint;

	/**
	 * Start button outer border paint.
	 */
	private Paint mStartProgressOuterPaint;

	/**
	 * Start button middle border paint.
	 */
	private Paint mStartProgressMiddlePaint;

	/**
	 * Start button inner border paint.
	 */
	private Paint mStartProgressInnerPaint;

	/**
	 * Start and stop button strong text paint.
	 */
	private Paint mStartButtonStrongTextPaint;

	/**
	 * Start and stop button thin text paint.
	 */
	private Paint mStartButtonThinTextPaint;

	/**
	 * Paint for the overview dashed line.
	 */
	private Paint mOverviewDashPaint;

	/**
	 * Paint for the "min" text in overview state.
	 */
	private Paint mTextOverviewMinPaint;

	/**
	 * Paint for the "total" and "active" text in the overview state.
	 */
	private Paint mTextOverviewTotalPaint;

	/**
	 * Paint for the timer text in rest and countdown state.
	 */
	private Paint mTextTimerPaint;

	/**
	 * Paint for the timer unit text in rest and countdown state.
	 */
	private Paint mTextTimerUnitPaint;

	/**
	 * Paint for the timer message text in rest and countdown state.
	 */
	private Paint mTextTimerMessagePaint;

	/**
	 * Paint for the current repetition text in the exercise state.
	 */
	private Paint mTextCurrentRepetitionPaint;

	/**
	 * Paint for the total repetitions text in the exercise state.
	 */
	private Paint mTextTotalRepetitionsPaint;

	/**
	 * Paint for the current series text in the exercise state.
	 */
	private Paint mTextCurrentSeriesPaint;

	/**
	 * Paint for the total series text in the exercise state.
	 */
	private Paint mTextTotalSeriesPaint;

	/**
	 * Paint for the big total and active number in the overview state.
	 */
	private Paint mTextOverviewNumberPaint;

	/*
	 * START PROGRESS BARS VALUES
	 */

	/**
	 * Value indicating the length of the training progress bar.
	 */
	private int mTrainingProgressValue;

	/**
	 * Value indicating the length of the exercise progress bar.
	 */
	private int mExerciseProgressValue;

	/**
	 * Value indicating the length of the rest progress bar.
	 */
	private int mRestProgressValue;

	/**
	 * Max value for the training progress bar (default 100).
	 */
	private int mTrainingMaxProgress = 100;

	/**
	 * Max value for the exercise progress bar (default 100)
	 */
	private int mExerciseMaxProgress = 100;

	/**
	 * Max value for the rest progress bar (default 100).
	 */
	private int mRestMaxProgress = 100;

	/**
	 * Min value for the training progress bar (default 0).
	 */
	private int mTrainingMinProgress = 0;

	/**
	 * Min value for the exercise progress bar (default 0).
	 */
	private int mExerciseMinProgress = 0;

	/**
	 * Min value for the rest progress bar (default 0).
	 */
	private int mRestMinProgress = 0;

	/*
	 * END PROGRESS BARS VALUES
	 */

	/*
	 * START PRECALCULATED VALUES FOR FAST DRAWING.
	 */

	/**
	 * Rectangle for drawing training progress arc.
	 */
	private RectF mTrainingCircleOval;

	/**
	 * Rectangle for drawing exercise progress arc.
	 */
	private RectF mExerciseCircleOval;

	/**
	 * Rectangle for drawing rest progress arc.
	 */
	private RectF mRestCircleOval;

	/**
	 * X value of circle center.
	 */
	private float mCenterX;

	/**
	 * Y value of circle center.
	 */
	private float mCenterY;

	/**
	 * Y value of repetition counter text.
	 */
	private float mRepetitionCounterTextY;

	/**
	 * Y value of message visible in screens with timer (rest and countdown).
	 */
	private float mTimerMessageTextY;

	/**
	 * Training circle radius.
	 */
	private int mTrainingCircleRadius;

	/**
	 * Exercise circle radius.
	 */
	private float mExerciseCircleRadius;

	/**
	 * Rest circle radius.
	 */
	private float mRestCircleRadius;

	/**
	 * Calculated training and exercise progress bar thickness in px.
	 */
	private float mProgThicknessInPx;

	/**
	 * Precalculated value of the training arc length.
	 */
	private float mCalculatedTrainingArc;

	/**
	 * Precalculated value of the exercise arc length.
	 */
	private float mCalculatedExerciseArc;

	/**
	 * Precalculated value of the rest arc length.
	 */
	private float mCalculatedRestArc;

	/**
	 * Start button outer border radius.
	 */
	private float mStartOuterCircleRadius;

	/**
	 * Start button middle border radius.
	 */
	private float mStartMiddleCircleRadius;

	/**
	 * Start button inner border radius.
	 */
	private float mStartInnerCircleRadius;

	/**
	 * Start button center circle radius.
	 */
	private float mStartCenterCircleRadius;

	/**
	 * Precalculated start button outer border thickness in px.
	 */
	private float mStartOuterThicknessInPx;

	/**
	 * Precalculated start button middle border thickness in px.
	 */
	private float mStartMiddleThicknessInPx;

	/**
	 * Precalculated start button inner border thickness in px.
	 */
	private float mStartInnerThicknessInPx;

	private float mChairTextY;
	private float mChairTapY;

	/*
	 * END PRECALCULATED VALUES FOR FAST DRAWING.
	 */

	/**
	 * Current state of the control.
	 */
	private CircularProgressState mCurrentState;

	/**
	 * Holds the information if the button is currently pressed.
	 */
	private boolean mIsPressedState;

	private Typeface mCooperBlackTypeface;

	private Typeface mHaginCapsMediumTypeface;

	private Typeface mFinenessRegularTypeface;

	private Typeface mOpenSansRegular;

	private String mStartButtonStrongText = "PUSH";

	private String mStartButtonThinText = "TO START";

	private String mStopButtonStrongText = "DONE";

	private String mStopButtonThinText = "I'M";

	private String mOverviewActiveText = "active";

	private String mOverviewTotalText = "total";

	private String mChairText = "Chair";

	private String mChairLevelText = "level: ";

	private String mTapText = "tap to close";

	private String mNoText = "No";

	private String mSettingText = "setting.";

	private int mNumberActive = 70;

	private int mNumberTotal = 120;

	private String mInfoChairLevel = "";

	public void setInfoChairLevel(String value) {
		if (!mInfoChairLevel.equals(value)) {
			mInfoChairLevel = value == null ? "" : value;
			invalidate();
		}
	}

	public String getInfoChairLevel() {
		return mInfoChairLevel;
	}

	private boolean mIsInfoVisible = false;

	public boolean isInfoVisible() {
		return mIsInfoVisible;
	}

	public void setInfoVisible(boolean value) {
		if (mIsInfoVisible != value) {
			mIsInfoVisible = value;
			invalidate();
		}
	}

	/**
	 * Contains the numerical timer value.
	 */
	private int mTimer;

	/**
	 * Sets the timer value and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setTimer(int value) {
		if (mTimer != value) {
			mTimer = value;
			invalidate();
		}
	}

	/**
	 * Gets the timer value and invalidates the screen.
	 * 
	 * @return
	 */
	public int getTimer() {
		return mTimer;
	}

	/**
	 * Contains the current repetition number.
	 */
	private int mCurrentRepetition = 9;

	/**
	 * Sets the current repetition number and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setCurrentRepetition(int value) {
		if (mCurrentRepetition != value) {
			mCurrentRepetition = value;
			invalidate();
		}
	}

	/**
	 * Gets the current repetition number.
	 * 
	 * @return
	 */
	public int getCurrentRepetition() {
		return mCurrentRepetition;
	}

	/**
	 * Contains the total repetitions number.
	 */
	private int mTotalRepetitions = 10;

	/**
	 * Sets the total repetitions number and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setTotalRepetitions(int value) {
		if (mTotalRepetitions != value) {
			mTotalRepetitions = value;
			invalidate();
		}
	}

	/**
	 * Gets the total repetitions number.
	 * 
	 * @return
	 */
	public int getTotalRepetitions() {
		return mTotalRepetitions;
	}

	/**
	 * Contains the current series number.
	 */
	private int mCurrentSeries = 1;

	/**
	 * Sets the current series number and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setCurrentSeries(int value) {
		if (mCurrentSeries != value) {
			mCurrentSeries = value;
			invalidate();
		}
	}

	/**
	 * Gets the current series number.
	 * 
	 * @return
	 */
	public int getCurrentSeries() {
		return mCurrentSeries;
	}

	/**
	 * Contains the total series number.
	 */
	private int mTotalSeries = 3;

	/**
	 * Sets the total series number and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setTotalSeries(int value) {
		if (mTotalSeries != value) {
			mTotalSeries = value;
			invalidate();
		}
	}

	/**
	 * Gets the total series number.
	 * 
	 * @return
	 */
	public int getTotalSeries() {
		return mTotalSeries;
	}

	/**
	 * Contains the timer message.
	 */
	private String mTimerMessage = "Get ready!";

	/**
	 * Sets the timer message and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setTimerMessage(String value) {
		if (!mTimerMessage.equals(value)) {
			mTimerMessage = value;
			invalidate();
		}
	}

	/**
	 * Gets the current timer message and invalidates the screen.
	 * 
	 * @return
	 */
	public String getTimerMessage() {
		return mTimerMessage;
	}

	/**
	 * Calculates the arc length based on the input values.
	 * 
	 * @param value
	 * @param max
	 * @param min
	 * @return a number between [0, 360] if the value is between [min, max],
	 *         otherwise the number can be out of bounds.
	 */
	public static float calculateArc(float value, float max, float min) {
		return (value - min) / (max - min) * 360;
	}

	/**
	 * Checks if the input parameters are in legal order (value has to be in
	 * [min, max] interval)
	 * 
	 * @param value
	 * @param max
	 * @param min
	 * @return
	 */
	public static boolean isValueCombinationLegal(int value, int max, int min) {
		return value >= min && value <= max;
	}

	/**
	 * Returns if the circular button is currently pressed.
	 * 
	 * @return
	 */
	public boolean isButtonPressed() {
		return mIsPressedState;
	}

	/**
	 * Sets the active minutes number and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setNumberActive(int value) {
		if (mNumberActive != value) {
			mNumberActive = value;

			invalidate();
		}
	}

	/**
	 * Gets the active minutes number.
	 * 
	 * @return
	 */
	public int getNumberActive() {
		return mNumberActive;
	}

	/**
	 * Sets the total minutes number and invalidates the screen.
	 * 
	 * @param value
	 */
	public void setNumberTotal(int value) {
		if (mNumberTotal != value) {
			mNumberTotal = value;

			invalidate();
		}
	}

	/**
	 * Gets the total minutes number.
	 * 
	 * @return
	 */
	public int getNumberTotal() {
		return mNumberTotal;
	}

	/**
	 * Sets the active value of the training progress bar and invalidates the
	 * screen. The training progress bar visualizes the training completion.
	 * Note: Nothing happens if the min, max and progress values are not legal
	 * (progress value has to be included in the [min,max] interval)
	 */
	public void setTrainingProgressValue(int value) {
		if (isValueCombinationLegal(value, mTrainingMaxProgress,
				mTrainingMinProgress)) {
			mTrainingProgressValue = value;

			mCalculatedTrainingArc = calculateArc(mTrainingProgressValue,
					mTrainingMaxProgress, mTrainingMinProgress);

			invalidate();

			Log.d(TAG, Integer.toString(mTrainingProgressValue));
		}
	}

	/**
	 * Sets the active value of the exercise progress bar and invalidates the
	 * screen. The exercise progress bar visualizes the exercise completion.
	 * Note: Nothing happens if the min, max and progress values are not legal
	 * (progress value has to be included in the [min,max] interval)
	 */
	public void setExerciseProgressValue(int value) {
		if (isValueCombinationLegal(value, mExerciseMaxProgress,
				mExerciseMinProgress)) {
			mExerciseProgressValue = value;

			mCalculatedExerciseArc = calculateArc(mExerciseProgressValue,
					mExerciseMaxProgress, mExerciseMinProgress);

			invalidate();
		}
	}

	/**
	 * Sets the active value of the rest progress bar and invalidates the
	 * screen. The rest progress bar visualizes the rest interval. Note: Nothing
	 * happens if the min, max and progress values are not legal (progress value
	 * has to be included in the [min,max] interval)
	 */
	public void setRestProgressValue(int value) {
		if (isValueCombinationLegal(value, mRestMaxProgress, mRestMinProgress)) {
			mRestProgressValue = value;

			mCalculatedRestArc = calculateArc(mRestProgressValue,
					mRestMaxProgress, mRestMinProgress);

			invalidate();
		}
	}

	/**
	 * Sets the max value of the training progress bar and invalidates the
	 * screen. This value indicates when the training progress bar is full.
	 */
	public void setTrainingMaxProgress(int value) {
		mTrainingMaxProgress = value;

		mCalculatedTrainingArc = calculateArc(mTrainingProgressValue,
				mTrainingMaxProgress, mTrainingMinProgress);

		invalidate();
	}

	/**
	 * Sets the min value of the training progress bar and invalidates the
	 * screen. This value indicates when the training progress bar is empty.
	 */
	public void setTrainingMinProgress(int value) {
		mTrainingMinProgress = value;

		mCalculatedTrainingArc = calculateArc(mTrainingProgressValue,
				mTrainingMaxProgress, mTrainingMinProgress);

		invalidate();
	}

	/**
	 * Sets the max value of the rest progress bar and invalidates the screen.
	 * This value indicates when the rest progress bar is full.
	 */
	public void setRestMaxProgress(int value) {
		mRestMaxProgress = value;

		mCalculatedRestArc = calculateArc(mRestProgressValue, mRestMaxProgress,
				mRestMinProgress);

		invalidate();
	}

	/**
	 * Sets the min value of the rest progress bar and invalidates the screen.
	 * This value indicates when the rest progress bar is empty.
	 */
	public void setRestMinProgress(int value) {
		mRestMinProgress = value;

		mCalculatedRestArc = calculateArc(mRestProgressValue, mRestMaxProgress,
				mRestMinProgress);

		invalidate();
	}

	/**
	 * Sets the max value of the exercise progress bar and invalidates the
	 * screen. This value indicates when the exercise progress bar is full.
	 */
	public void setExerciseMaxProgress(int value) {
		mExerciseMaxProgress = value;

		mCalculatedExerciseArc = calculateArc(mExerciseProgressValue,
				mExerciseMaxProgress, mExerciseMinProgress);

		invalidate();
	}

	/**
	 * Sets the min value of the rest progress bar and invalidates the screen.
	 * This value indicates when the rest progress bar is empty.
	 */
	public void setExerciseMinProgress(int value) {
		mExerciseMinProgress = value;

		mCalculatedExerciseArc = calculateArc(mExerciseProgressValue,
				mExerciseMaxProgress, mExerciseMinProgress);

		invalidate();
	}

	public void setCurrentState(CircularProgressState state) {
		mCurrentState = state;

		invalidate();
	}

	public CircularProgressState getCurrentState() {
		return mCurrentState;
	}

	/**
	 * Constructor. This version is only needed if you will be instantiating the
	 * object manually (not from a layout XML file).
	 * 
	 * @param context
	 */
	public CircularProgressControl(Context context) {
		super(context);
		init();
	}

	/**
	 * Construct object, initializing with any attributes we understand from a
	 * layout file.
	 * 
	 * @see android.view.View#View(android.content.Context,
	 *      android.util.AttributeSet)
	 */
	public CircularProgressControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Initializes all drawing objects.
	 */
	private final void init() {
		// load typefaces
		mCooperBlackTypeface = Typeface.createFromAsset(getContext()
				.getAssets(), "fonts/Cooper Black.ttf");
		mHaginCapsMediumTypeface = Typeface.createFromAsset(getContext()
				.getAssets(), "fonts/Hagin Caps Medium.otf");
		mFinenessRegularTypeface = Typeface.createFromAsset(getContext()
				.getAssets(), "fonts/FinenessRegular.ttf");
		mOpenSansRegular = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/OpenSans-Regular.ttf");

		// exercise state variables
		mInfoButtonBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mInfoButtonBackgroundPaint.setColor(getResources().getColor(
				R.color.cpc_info_button_background));

		mTrainingProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTrainingProgressBackgroundPaint.setColor(getResources().getColor(
				R.color.cpc_training_progress_background));

		mExerciseProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mExerciseProgressBackgroundPaint.setColor(getResources().getColor(
				R.color.cpc_exercise_progress_background));

		mRestProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressBackgroundPaint.setColor(getResources().getColor(
				R.color.cpc_rest_progress_background));

		mTrainingProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTrainingProgressForegroundPaint.setColor(getResources().getColor(
				R.color.cpc_training_progress_foreground));

		mExerciseProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mExerciseProgressForegroundPaint.setColor(getResources().getColor(
				R.color.cpc_exercise_progress_foreground));

		mRestProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressForegroundPaint.setColor(getResources().getColor(
				R.color.cpc_rest_progress_foreground));

		mRestProgressClickBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressClickBackgroundPaint.setColor(getResources().getColor(
				R.color.cpc_rest_progress_pressed_background));

		mRestProgressClickForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressClickForegroundPaint.setColor(getResources().getColor(
				R.color.cpc_rest_progress_pressed_foreground));

		mProgThicknessInPx = getResources().getDimensionPixelSize(
				R.dimen.cpc_outer_progress_thickness);

		// start button state variables
		mStartProgressOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartProgressOuterPaint.setColor(getResources().getColor(
				R.color.cpc_start_button_outer_border));

		mStartProgressMiddlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartProgressMiddlePaint.setColor(getResources().getColor(
				R.color.cpc_start_button_middle_border));

		mStartProgressInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartProgressInnerPaint.setColor(getResources().getColor(
				R.color.cpc_start_button_inner_border));

		mInfoButtonLargeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mInfoButtonLargeTextPaint.setColor(getResources().getColor(
				R.color.cpc_info_button_text));
		mInfoButtonLargeTextPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_info_button_large_text));
		mInfoButtonLargeTextPaint.setTypeface(mOpenSansRegular);

		mInfoButtonSmallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mInfoButtonSmallTextPaint.setColor(getResources().getColor(
				R.color.cpc_info_button_text));
		mInfoButtonSmallTextPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_info_button_small_text));
		mInfoButtonSmallTextPaint.setTypeface(mOpenSansRegular);

		mStartButtonStrongTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartButtonStrongTextPaint.setColor(getResources().getColor(
				R.color.cpc_start_strong_text));
		mStartButtonStrongTextPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_start_strong_text));
		mStartButtonStrongTextPaint.setTypeface(mCooperBlackTypeface);

		mStartButtonThinTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartButtonThinTextPaint.setColor(getResources().getColor(
				R.color.cpc_start_thin_text));
		mStartButtonThinTextPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_start_thin_text));
		mStartButtonThinTextPaint.setTypeface(mHaginCapsMediumTypeface);

		mStartOuterThicknessInPx = getResources().getDimensionPixelSize(
				R.dimen.cpc_start_button_outer_border_thickness);

		mStartMiddleThicknessInPx = getResources().getDimensionPixelSize(
				R.dimen.cpc_start_button_middle_border_thickness);

		mStartInnerThicknessInPx = getResources().getDimensionPixelSize(
				R.dimen.cpc_start_button_inner_border_thickness);

		// overview state variables
		float dashPartLenInPx = getResources().getDimensionPixelSize(
				R.dimen.cpc_overview_dash_part_length);

		mOverviewDashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mOverviewDashPaint.setColor(getResources().getColor(
				R.color.cpc_overview_dash));
		mOverviewDashPaint.setStyle(Style.STROKE);
		mOverviewDashPaint.setPathEffect(new DashPathEffect(new float[] {
				dashPartLenInPx, dashPartLenInPx }, 0));
		mOverviewDashPaint.setStrokeWidth(getResources().getDimensionPixelSize(
				R.dimen.cpc_overview_dash_thickness));

		mTextOverviewMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOverviewMinPaint.setColor(getResources().getColor(
				R.color.cpc_overiview_units_text));
		mTextOverviewMinPaint.setTextSize(getResources().getDimensionPixelSize(
				R.dimen.cpc_overview_text));

		mTextOverviewTotalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOverviewTotalPaint.setColor(getResources().getColor(
				R.color.cpc_start_strong_text));
		mTextOverviewTotalPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextOverviewTotalPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_overview_text));

		mTextOverviewNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOverviewNumberPaint.setColor(getResources().getColor(
				R.color.cpc_start_strong_text));
		mTextOverviewNumberPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_overview_number_text));

		mTextTimerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextTimerPaint.setColor(getResources().getColor(
				R.color.cpc_timer_counter_text));
		mTextTimerPaint.setTextSize(getResources().getDimensionPixelSize(
				R.dimen.cpc_timer_counter_text));

		mTextTimerUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextTimerUnitPaint.setColor(getResources().getColor(
				R.color.cpc_timer_counter_text));
		mTextTimerUnitPaint.setTextSize(getResources().getDimensionPixelSize(
				R.dimen.cpc_timer_unit_text));

		mTextTimerMessagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextTimerMessagePaint.setColor(getResources().getColor(
				R.color.cpc_timer_message_text));
		mTextTimerMessagePaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_timer_message_text));
		mTextTimerMessagePaint.setTypeface(mFinenessRegularTypeface);

		mTextCurrentRepetitionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextCurrentRepetitionPaint.setColor(getResources().getColor(
				R.color.cpc_current_repetition_text));
		mTextCurrentRepetitionPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextCurrentRepetitionPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_current_repetition_text));
		mTextCurrentRepetitionPaint.setTypeface(mFinenessRegularTypeface);

		mTextTotalRepetitionsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextTotalRepetitionsPaint.setColor(getResources().getColor(
				R.color.cpc_total_repetitions_text));
		mTextTotalRepetitionsPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_total_repetitions_text));
		mTextTotalRepetitionsPaint.setTypeface(mFinenessRegularTypeface);

		mTextCurrentSeriesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextCurrentSeriesPaint.setColor(getResources().getColor(
				R.color.cpc_current_series_text));
		mTextCurrentSeriesPaint.setTextSize(getResources()
				.getDimensionPixelSize(R.dimen.cpc_current_series_text));
		mTextCurrentSeriesPaint.setTypeface(mFinenessRegularTypeface);

		mTextTotalSeriesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextTotalSeriesPaint.setColor(getResources().getColor(
				R.color.cpc_total_series_text));
		mTextTotalSeriesPaint.setTextSize(getResources().getDimensionPixelSize(
				R.dimen.cpc_total_series_text));
		mTextTotalSeriesPaint.setTypeface(mFinenessRegularTypeface);

		// hide the default button background
		setBackgroundDrawable(null);
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int radius = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
				- getPaddingRight();

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = radius + getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(width, height);

		// precalculate all variables used for drawing
		mCenterX = getPaddingLeft() + radius / 2;
		mCenterY = getPaddingTop() + radius / 2;
		mRepetitionCounterTextY = getPaddingTop() + radius / 12 * 7;
		mTimerMessageTextY = getPaddingTop() + radius / 4 * 3;
		mChairTextY = getPaddingTop() + radius / 4;
		mChairTapY = getPaddingTop() + radius / 4 * 3;

		mTrainingCircleRadius = radius / 2;
		mExerciseCircleRadius = mTrainingCircleRadius - mProgThicknessInPx;
		mRestCircleRadius = mExerciseCircleRadius - mProgThicknessInPx;
		mStartOuterCircleRadius = radius / 2;
		mStartMiddleCircleRadius = mStartOuterCircleRadius
				- mStartOuterThicknessInPx;
		mStartInnerCircleRadius = mStartMiddleCircleRadius
				- mStartMiddleThicknessInPx;
		mStartCenterCircleRadius = mStartInnerCircleRadius
				- mStartInnerThicknessInPx;

		mTrainingCircleOval = new RectF(getPaddingLeft(), getPaddingTop(),
				getPaddingLeft() + radius, getPaddingTop() + radius);
		mExerciseCircleOval = new RectF(getPaddingLeft() + mProgThicknessInPx,
				getPaddingTop() + mProgThicknessInPx, getPaddingLeft() + radius
						- mProgThicknessInPx, getPaddingTop() + radius
						- mProgThicknessInPx);
		mRestCircleOval = new RectF(getPaddingLeft() + 2 * mProgThicknessInPx,
				getPaddingTop() + 2 * mProgThicknessInPx, getPaddingLeft()
						+ radius - 2 * mProgThicknessInPx, getPaddingTop()
						+ radius - 2 * mProgThicknessInPx);
	}

	/**
	 * Gets the formated time string for more convenient drawing. If the time
	 * number is low enough it's simply output, otherwise, we convert it to the
	 * appropriate unit. The unit is returned through the input parameter unit.
	 * 
	 * @param timeInSec
	 *            input time in seconds
	 * @param unit
	 *            output unit for the calculated time
	 * @param shortForm
	 *            <code>true</code> if unit should be ouput in short form,
	 *            otherwise <code>false</code>.
	 * @return time in unit specified in @param unit
	 */

	private static String getFormatedTime(int timeInSec, StringBuilder unit,
			boolean shortForm) {
		if (timeInSec < 120) {
			// seconds
			unit.append(shortForm ? "s" : "sec");
			return String.format("%d", timeInSec);
		} else {
			float timeInMin = timeInSec / 60f;
			String textTimeInMin = String.format("%.1f", timeInMin);

			if (timeInMin < 60) {
				// minutes
				unit.append(shortForm ? "m" : "min");
				return textTimeInMin;
			} else {
				// hours
				unit.append(shortForm ? "h" : "hr");
				float timeInHour = timeInMin / 60f;
				return String.format("%.1f", timeInHour);
			}
		}
	}

	/**
	 * Render the circular progress control.
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mCurrentState == null) {
			Log.d(TAG, "Current state not set yet.");
			return;
		}

		Log.d(TAG, Boolean.toString(mIsPressedState));

		switch (mCurrentState) {
		case START: {
			// draw start button circle with borders
			canvas.drawCircle(mCenterX, mCenterY, mStartOuterCircleRadius,
					mStartProgressOuterPaint);
			canvas.drawCircle(mCenterX, mCenterY, mStartMiddleCircleRadius,
					mStartProgressMiddlePaint);
			canvas.drawCircle(mCenterX, mCenterY, mStartInnerCircleRadius,
					mStartProgressInnerPaint);
			canvas.drawCircle(mCenterX, mCenterY, mStartCenterCircleRadius,
					mIsPressedState ? mRestProgressClickBackgroundPaint
							: mRestProgressBackgroundPaint);

			// draw start button text
			float startStrongTextWidth = mStartButtonStrongTextPaint
					.measureText(mStartButtonStrongText);
			float startStrongTextDescent = mStartButtonStrongTextPaint
					.descent();
			float startThinTextWidth = mStartButtonThinTextPaint
					.measureText(mStartButtonThinText);
			float startThinTextAscent = mStartButtonThinTextPaint.ascent();
			canvas.drawText(mStartButtonStrongText, mCenterX
					- startStrongTextWidth / 2, mCenterY,
					mStartButtonStrongTextPaint);
			canvas.drawText(mStartButtonThinText, mCenterX - startThinTextWidth
					/ 2, mCenterY + startStrongTextDescent
					- startThinTextAscent, mStartButtonThinTextPaint);
			break;
		}
		case STOP:
		case OVERVIEW: {
			// the circle with borders
			canvas.drawCircle(mCenterX, mCenterY, mTrainingCircleRadius,
					mTrainingProgressForegroundPaint);
			canvas.drawCircle(mCenterX, mCenterY, mExerciseCircleRadius,
					mExerciseProgressForegroundPaint);
			canvas.drawCircle(mCenterX, mCenterY, mRestCircleRadius,
					mIsPressedState ? mRestProgressClickBackgroundPaint
							: mRestProgressBackgroundPaint);

			if (mCurrentState == CircularProgressState.STOP) {
				// draw stop button text
				float stopThinTextWidth = mStartButtonThinTextPaint
						.measureText(mStopButtonThinText);
				float stopStrongTextWidth = mStartButtonStrongTextPaint
						.measureText(mStopButtonStrongText);
				float stopThinTextAscent = mStartButtonThinTextPaint.ascent();
				float stopStrongTextAscent = mStartButtonStrongTextPaint
						.ascent();

				canvas.drawText(mStopButtonThinText, mCenterX
						- stopThinTextWidth / 2, mCenterY + stopThinTextAscent
						* 1.5f, mStartButtonThinTextPaint);
				canvas.drawText(mStopButtonStrongText, mCenterX
						- stopStrongTextWidth / 2, mCenterY
						- stopStrongTextAscent / 2, mStartButtonStrongTextPaint);
			} else {

				// draw the dashed line
				float dashedLineLength = mCenterX / 2f;
				canvas.drawLine(dashedLineLength, mCenterX,
						3 * dashedLineLength, mCenterX, mOverviewDashPaint);

				StringBuilder unit = new StringBuilder();
				String activeTimerText = getFormatedTime(mNumberActive, unit,
						false);
				String activeUnit = String.format("%s ", unit.toString());

				// position the ("min active") text in the center of the button
				float unitActiveLength = mTextOverviewMinPaint
						.measureText(activeUnit);
				float activeLength = mTextOverviewTotalPaint
						.measureText(mOverviewActiveText);
				float minActiveStartX = mCenterX
						- (unitActiveLength + activeLength) / 2;

				// measure the height of the text for vertical placement
				float textDescent = mTextOverviewTotalPaint.descent();
				float textAscent = mTextOverviewTotalPaint.ascent();

				// draw the "min active"
				canvas.drawText(activeUnit, minActiveStartX, mCenterY - 2
						* textDescent, mTextOverviewMinPaint);

				canvas.drawText(mOverviewActiveText, minActiveStartX
						+ unitActiveLength, mCenterY - 2 * textDescent,
						mTextOverviewTotalPaint);

				// center and draw the big active number
				float activeNumberLength = mTextOverviewNumberPaint
						.measureText(activeTimerText);

				canvas.drawText(activeTimerText, mCenterX - activeNumberLength
						/ 2, mCenterY - 3 * textDescent + textAscent,
						mTextOverviewNumberPaint);

				unit = new StringBuilder();
				activeTimerText = getFormatedTime(mNumberTotal, unit, false);
				String totalUnit = String.format("%s ", unit.toString());

				// center and draw the big total number
				float unitTotalLength = mTextOverviewMinPaint
						.measureText(totalUnit);
				float totalNumberLength = mTextOverviewNumberPaint
						.measureText(activeTimerText);

				float numberTextAscent = mTextOverviewNumberPaint.ascent();
				canvas.drawText(activeTimerText, mCenterX - totalNumberLength
						/ 2, mCenterY + textDescent - numberTextAscent,
						mTextOverviewNumberPaint);

				// center and draw the "min total" text
				float totalLength = mTextOverviewTotalPaint
						.measureText(mOverviewTotalText);
				float minTotalStartX = mCenterX
						- (unitTotalLength + totalLength) / 2;

				canvas.drawText(totalUnit, minTotalStartX, mCenterY + 2
						* textDescent - numberTextAscent - textAscent,
						mTextOverviewMinPaint);

				canvas.drawText(mOverviewTotalText, minTotalStartX
						+ unitTotalLength, mCenterY + 2 * textDescent
						- numberTextAscent - textAscent,
						mTextOverviewTotalPaint);
			}
			break;
		}
		case REST:
		case EXERCISE: {
			// draw training, exercise and rest progress bars
			canvas.drawCircle(mCenterX, mCenterY, mTrainingCircleRadius,
					mTrainingProgressBackgroundPaint);
			canvas.drawArc(mTrainingCircleOval, 270, mCalculatedTrainingArc,
					true, mTrainingProgressForegroundPaint);
			canvas.drawCircle(mCenterX, mCenterY, mExerciseCircleRadius,
					mExerciseProgressBackgroundPaint);
			canvas.drawArc(mExerciseCircleOval, 270, mCalculatedExerciseArc,
					true, mExerciseProgressForegroundPaint);
			canvas.drawCircle(mCenterX, mCenterY, mRestCircleRadius,
					mIsPressedState ? mRestProgressClickBackgroundPaint
							: mRestProgressBackgroundPaint);

			if (mCurrentState == CircularProgressState.REST) {
				canvas.drawArc(mRestCircleOval, 270, mCalculatedRestArc, true,
						mIsPressedState ? mRestProgressClickForegroundPaint
								: mRestProgressForegroundPaint);
				float timerMessageLength = mTextTimerMessagePaint
						.measureText(mTimerMessage);

				canvas.drawText(mTimerMessage, mCenterX - timerMessageLength
						/ 2, mTimerMessageTextY, mTextTimerMessagePaint);

				StringBuilder unit = new StringBuilder();
				String timerText = getFormatedTime(mTimer, unit, true);
				float timerLength = mTextTimerPaint.measureText(timerText);
				float unitLength = mTextTimerUnitPaint.measureText(unit
						.toString());

				float textStart = mCenterX - (timerLength + unitLength) / 2;

				float timerTextDescent = mTextTimerPaint.descent();
				canvas.drawText(timerText, textStart, mCenterY
						+ timerTextDescent, mTextTimerPaint);
				canvas.drawText(unit.toString(), textStart + timerLength,
						mCenterY + timerTextDescent, mTextTimerUnitPaint);

			} else if (mCurrentState == CircularProgressState.EXERCISE) {

				float currentRepetitionLength = mTextCurrentRepetitionPaint
						.measureText(Integer.toString(mCurrentRepetition));

				float totalRepetitionsLength = mTextTotalRepetitionsPaint
						.measureText(String.format("/%d", mTotalRepetitions));

				float startCurrentRepetitionX = mCenterX
						- (currentRepetitionLength + totalRepetitionsLength)
						/ 2;

				canvas.drawText(Integer.toString(mCurrentRepetition),
						startCurrentRepetitionX, mRepetitionCounterTextY,
						mTextCurrentRepetitionPaint);
				canvas.drawText(String.format("/%d", mTotalRepetitions),
						startCurrentRepetitionX + currentRepetitionLength,
						mRepetitionCounterTextY, mTextTotalRepetitionsPaint);

				float currentSeriesLength = mTextCurrentSeriesPaint
						.measureText(Integer.toString(mCurrentSeries));

				float totalSeriesLength = mTextTotalSeriesPaint
						.measureText(String.format("/%d", mTotalSeries));

				float startCurrentSeriesX = mCenterX
						- (currentSeriesLength + totalSeriesLength) / 2;

				float seriesTextY = mRepetitionCounterTextY
						- mTextCurrentSeriesPaint.ascent() * 4 / 3;

				canvas.drawText(Integer.toString(mCurrentSeries),
						startCurrentSeriesX, seriesTextY,
						mTextCurrentSeriesPaint);
				canvas.drawText(String.format("/%d", mTotalSeries),
						startCurrentSeriesX + currentSeriesLength, seriesTextY,
						mTextTotalSeriesPaint);
			}

			break;
		}
		default:
			canvas.drawText("State not set!", mCenterX, mCenterY,
					mRestProgressForegroundPaint);
		}

		// overlay the info button
		if (mIsInfoVisible) {
			canvas.drawCircle(mCenterX, mCenterY, mTrainingCircleRadius,
					mInfoButtonBackgroundPaint);

			float line1Length = mInfoButtonLargeTextPaint
					.measureText(mInfoChairLevel.equals("") ? mNoText
							: mChairText);

			canvas.drawText(mInfoChairLevel.equals("") ? mNoText : mChairText,
					mCenterX - line1Length / 2, mChairTextY,
					mInfoButtonLargeTextPaint);

			float chairLevelY = mChairTextY
					- mInfoButtonLargeTextPaint.ascent()
					+ mInfoButtonLargeTextPaint.descent();

			float chairLevelX = mCenterX
					- (mInfoChairLevel.equals("") ? mInfoButtonLargeTextPaint
							.measureText(mSettingText) / 2
							: (mInfoButtonLargeTextPaint
									.measureText(mChairLevelText) + mInfoButtonLargeTextPaint
									.measureText(mInfoChairLevel)) / 2);

			canvas.drawText(mInfoChairLevel.equals("") ? mSettingText
					: mChairLevelText + mInfoChairLevel, chairLevelX,
					chairLevelY, mInfoButtonLargeTextPaint);

			float tapLength = mInfoButtonSmallTextPaint.measureText(mTapText);
			canvas.drawText(mTapText, mCenterX - tapLength / 2, mChairTapY,
					mInfoButtonSmallTextPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// calculate if the touch was inside the button
		// (currently the whole button is active,
		// including outer borders as well)
		boolean isInCircle = Math.pow(event.getX() - mCenterX, 2)
				+ Math.pow(event.getY() - mCenterY, 2) < Math.pow(
				mTrainingCircleRadius, 2);

		if (isInCircle) {
			// propagate event further (for longclick and other events
			// detection)
			super.onTouchEvent(event);
		}

		mIsPressedState = (event.getAction() == MotionEvent.ACTION_DOWN || event
				.getAction() == MotionEvent.ACTION_MOVE) && isInCircle;

		invalidate();

		return mIsPressedState;
	}
}
