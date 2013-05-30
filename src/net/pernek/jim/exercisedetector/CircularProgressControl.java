package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.util.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author Igor
 * 
 */
public class CircularProgressControl extends Button {
	/** Contains possible states for the {@link CircularProgressControl}
	 * @author Igor
	 *
	 */
	public enum CircularProgressState {
		START, EXERCISE, STOP, OVERVIEW
	}

	private static final String TAG = Utils.getApplicationTag();

	/**
	 * Training progress bar background color.
	 */
	private static final int TRAIN_PROG_BG_COLOR = 0xFFD1D2D4;

	/**
	 * Training progress bar foreground color.
	 */
	private static final int TRAIN_PROG_FG_COLOR = 0xFFEAB130;

	/**
	 * Exercise progress bar background color.
	 */
	private static final int EXER_PROG_BG_COLOR = 0xFFBDBEC2;

	/**
	 * Exercise progress bar foreground color.
	 */
	private static final int EXER_PROG_FG_COLOR = 0xFF55A0C0;

	/**
	 * Rest progress bar background color.
	 */
	private static final int REST_PROG_BG_COLOR = 0xFFFBC911;

	/**
	 * Rest progress bar foreground color.
	 */
	private static final int REST_PROG_FG_COLOR = 0xFFE1563E;

	/**
	 * Rest progress bar click background color.
	 */
	private static final int REST_PROG_BG_CLICK_COLOR = 0x99FBC911;

	/**
	 * Rest progress bar click foreground color.
	 */
	private static final int REST_PROG_FG_CLICK_COLOR = 0x99E1563E;

	/**
	 * Start button outer border color.
	 */
	private static final int START_PROG_OUT_COLOR = 0xFFE5E8E8;

	/**
	 * Start button middle border color.
	 */
	private static final int START_PROG_MID_COLOR = 0xFFFFFFFF;

	/**
	 * Start button inner border color.
	 */
	private static final int START_PROG_IN_COLOR = 0xFFF2F2F2;

	/**
	 * Start and stop button strong text color.
	 */
	private static final int START_STRONG_TEXT_COLOR = 0xFF424242;

	/**
	 * Start and stop button thin text color.
	 */
	private static final int START_THIN_TEXT_COLOR = 0xFFFFFFFF;
	
	/**
	 * Thickness of training and exercise progress bars in dp.
	 */
	private static final int PROG_THICK_IN_DIP = 10;

	/**
	 * Thickness of start button's outer border in dp.
	 */
	private static final int START_PROG_OUT_THICK_IN_DIP = 3;

	/**
	 * Thickness of start button's middle border in dp.
	 */
	private static final int START_PROG_MID_THICK_IN_DIP = 11;

	/**
	 * Thickness of start buttons's inner border in dp.
	 */
	private static final int START_PROG_IN_THICK_IN_DIP = 6;

	/**
	 * Start and stop button strong text size in dp.
	 */
	private static final int START_STRONG_TEXT_SIZE_IN_DIP = 60;

	/**
	 * Start and stop button thin text size in dp.
	 */
	private static final int START_THIN_TEXT_SIZE_IN_DIP = 30;
	
	/**
	 * Overview state "min active/total" text size in dp.
	 */
	private static final int OVERVIEW_TEXT_SIZE_IN_DIP = 20;
	
	/**
	 * Overview state big total and active number text size in dp.
	 */
	private static final int OVERVIEW_TEXT_NUMBER_SIZE_IN_DIP = 65;
	
	/**
	 * Overview state dashed line stroke thickness in dp.
	 */
	private static final int OVERVIEW_DASH_THICK_IN_DIP = 3;
	
	/**
	 * Overview state dashed line part length in dp.
	 */
	private static final int OVERVIEW_DASH_PART_LEN_IN_DIP = 5; 

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
	 * Paint for the "total" and "active" text in the
	 * overview state.
	 */
	private Paint mTextOverviewTotalPaint;
	
	/**
	 * Paint for the big total and active number in the
	 * overview state.
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
	 * Max value for the training progress bar.
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

	/*
	 * END PRECALCULATED VALUES FOR FAST DRAWING.
	 */

	/**
	 * Current state of the control.
	 */
	private CircularProgressState mCurrentState;

	private Typeface mCooperBlackTypeface;

	private Typeface mHaginCapsThinTypeface;

	private String mStartButtonStrongText = "PUSH";

	private String mStartButtonThinText = "TO START";

	private String mStopButtonStrongText = "DONE";

	private String mStopButtonThinText = "I'M";
	
	private String mOverviewMinText = "min ";
	
	private String mOverviewActiveText = "active";
	
	private String mOverviewTotalText = "total";
	
	private int mNumberActive = 70;
	
	private int mNumberTotal = 120;

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
	
	/** Sets the active minutes number and invalidates the screen.
	 * @param value
	 */
	public void setNumberActive(int value){
		mNumberActive = value;
		
		invalidate();
	}
	
	
	/** Gets the active minutes number.
	 * @return
	 */
	public int getNumberActive(){
		return mNumberActive;
	}
	
	/** Sets the total minutes number and invalidates the screen.
	 * @param value
	 */
	public void setNumberTotal(int value){
		mNumberTotal = value;
		
		invalidate();
	}
	
	/** Gets the total minutes number.
	 * @return
	 */
	public int getNumberTotal(){
		return mNumberTotal;
	}

	/**
	 * Sets the active value of the training progress bar and invalidates the
	 * screen. The training progress bar visualizes the training completion.
	 */
	public void setTrainingProgressValue(int value) {
		mTrainingProgressValue = value;

		mCalculatedTrainingArc = calculateArc(mTrainingProgressValue,
				mTrainingMaxProgress, mTrainingMinProgress);

		invalidate();

		Log.d(TAG, Integer.toString(mTrainingProgressValue));
	}

	/**
	 * Sets the active value of the exercise progress bar and invalidates the
	 * screen. The exercise progress bar visualizes the exercise completion.
	 */
	public void setExerciseProgressValue(int value) {
		mExerciseProgressValue = value;

		mCalculatedExerciseArc = calculateArc(mExerciseProgressValue,
				mExerciseMaxProgress, mExerciseMinProgress);

		invalidate();
	}

	/**
	 * Sets the active value of the rest progress bar and invalidates the
	 * screen. The rest progress bar visualizes the rest interval.
	 */
	public void setRestProgressValue(int value) {
		mRestProgressValue = value;

		mCalculatedRestArc = calculateArc(mRestProgressValue, mRestMaxProgress,
				mRestMinProgress);

		invalidate();
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
		mHaginCapsThinTypeface = Typeface.createFromAsset(getContext()
				.getAssets(), "fonts/Hagin Caps Thin.ttf");

		// exercise state variables
		mTrainingProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTrainingProgressBackgroundPaint.setColor(TRAIN_PROG_BG_COLOR);

		mExerciseProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mExerciseProgressBackgroundPaint.setColor(EXER_PROG_BG_COLOR);

		mRestProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressBackgroundPaint.setColor(REST_PROG_BG_COLOR);

		mTrainingProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTrainingProgressForegroundPaint.setColor(TRAIN_PROG_FG_COLOR);

		mExerciseProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mExerciseProgressForegroundPaint.setColor(EXER_PROG_FG_COLOR);

		mRestProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressForegroundPaint.setColor(REST_PROG_FG_COLOR);

		mRestProgressClickBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressClickBackgroundPaint.setColor(REST_PROG_BG_CLICK_COLOR);

		mRestProgressClickForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRestProgressClickForegroundPaint.setColor(REST_PROG_FG_CLICK_COLOR);

		mProgThicknessInPx = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, PROG_THICK_IN_DIP, getResources()
						.getDisplayMetrics());

		// start button state variables
		mStartProgressOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartProgressOuterPaint.setColor(START_PROG_OUT_COLOR);

		mStartProgressMiddlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartProgressMiddlePaint.setColor(START_PROG_MID_COLOR);

		mStartProgressInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartProgressInnerPaint.setColor(START_PROG_IN_COLOR);

		mStartButtonStrongTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartButtonStrongTextPaint.setColor(START_STRONG_TEXT_COLOR);
		mStartButtonStrongTextPaint.setTextSize(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, START_STRONG_TEXT_SIZE_IN_DIP,
				getResources().getDisplayMetrics()));
		mStartButtonStrongTextPaint.setTypeface(mCooperBlackTypeface);

		mStartButtonThinTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStartButtonThinTextPaint.setColor(START_THIN_TEXT_COLOR);
		mStartButtonThinTextPaint.setTextSize(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, START_THIN_TEXT_SIZE_IN_DIP,
				getResources().getDisplayMetrics()));
		mStartButtonThinTextPaint.setTypeface(mHaginCapsThinTypeface);

		mStartOuterThicknessInPx = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, START_PROG_OUT_THICK_IN_DIP,
				getResources().getDisplayMetrics());

		mStartMiddleThicknessInPx = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, START_PROG_MID_THICK_IN_DIP,
				getResources().getDisplayMetrics());

		mStartInnerThicknessInPx = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, START_PROG_IN_THICK_IN_DIP,
				getResources().getDisplayMetrics());
		
		// overview state variables
		float dashPartLenInPx = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, OVERVIEW_DASH_PART_LEN_IN_DIP, getResources()
				.getDisplayMetrics());
		
		mOverviewDashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mOverviewDashPaint.setColor(REST_PROG_FG_COLOR);
		mOverviewDashPaint.setStyle(Style.STROKE);
		mOverviewDashPaint.setPathEffect(new DashPathEffect(new float[] { dashPartLenInPx,
				dashPartLenInPx }, 0));
		mOverviewDashPaint.setStrokeWidth(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, OVERVIEW_DASH_THICK_IN_DIP, getResources()
				.getDisplayMetrics()));
		
		mTextOverviewMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOverviewMinPaint.setColor(REST_PROG_FG_COLOR);
		mTextOverviewMinPaint.setTextSize(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, OVERVIEW_TEXT_SIZE_IN_DIP,
				getResources().getDisplayMetrics()));
		
		mTextOverviewTotalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOverviewTotalPaint.setColor(START_STRONG_TEXT_COLOR);
		mTextOverviewTotalPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextOverviewTotalPaint.setTextSize(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, OVERVIEW_TEXT_SIZE_IN_DIP,
				getResources().getDisplayMetrics()));
		
		mTextOverviewNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOverviewNumberPaint.setColor(START_STRONG_TEXT_COLOR);
		mTextOverviewNumberPaint.setTextSize(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, OVERVIEW_TEXT_NUMBER_SIZE_IN_DIP,
				getResources().getDisplayMetrics()));

		// hide the default button background
		setBackgroundDrawable(null);
		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				Toast.makeText(getContext(), "test", Toast.LENGTH_LONG).show();
				return false;
			}
		});
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension(width, width);

		// precalculate all variables used for drawing
		mCenterX = mCenterY = width / 2;
		mTrainingCircleRadius = width / 2;
		mExerciseCircleRadius = mTrainingCircleRadius - mProgThicknessInPx;
		mRestCircleRadius = mExerciseCircleRadius - mProgThicknessInPx;
		mStartOuterCircleRadius = width / 2;
		mStartMiddleCircleRadius = mStartOuterCircleRadius
				- mStartOuterThicknessInPx;
		mStartInnerCircleRadius = mStartMiddleCircleRadius
				- mStartMiddleThicknessInPx;
		mStartCenterCircleRadius = mStartInnerCircleRadius
				- mStartInnerThicknessInPx;

		mTrainingCircleOval = new RectF(0, 0, width, width);
		mExerciseCircleOval = new RectF(mProgThicknessInPx, mProgThicknessInPx,
				width - mProgThicknessInPx, width - mProgThicknessInPx);
		mRestCircleOval = new RectF(2 * mProgThicknessInPx,
				2 * mProgThicknessInPx, width - 2 * mProgThicknessInPx, width
						- 2 * mProgThicknessInPx);
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

		boolean isPressedState = isPressed();

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
					isPressedState ? mRestProgressClickBackgroundPaint
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
					isPressedState ? mRestProgressClickBackgroundPaint
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
						3 * dashedLineLength, mCenterX,
						mOverviewDashPaint);
				
				// position the ("min active") text in the center of the button 
				float minLength = mTextOverviewMinPaint.measureText(mOverviewMinText);
				float activeLength = mTextOverviewTotalPaint.measureText(mOverviewActiveText);
				float minActiveStartX  = mCenterX - (minLength + activeLength) / 2;
								
				// measure the height of the text for vertical placement
				float textDescent = mTextOverviewTotalPaint.descent();
				float textAscent = mTextOverviewTotalPaint.ascent();
				
				
				// draw the "min active"
				canvas.drawText(mOverviewMinText, 
						minActiveStartX, 
						mCenterY - 2*textDescent, mTextOverviewMinPaint);
				
				canvas.drawText(mOverviewActiveText, 
						minActiveStartX + minLength, 
						mCenterY - 2*textDescent, mTextOverviewTotalPaint);
				
				// center and draw the big active number
				float activeNumberLength = mTextOverviewNumberPaint.measureText(
						Integer.toString(mNumberActive));
				
				canvas.drawText(Integer.toString(mNumberActive), 
						mCenterX - activeNumberLength / 2,
						mCenterY - 3*textDescent + textAscent, 
						mTextOverviewNumberPaint);
				
				
				// center and draw the big total number
				float totalNumberLength = mTextOverviewNumberPaint.measureText(
						Integer.toString(mNumberTotal));
				
				float numberTextAscent = mTextOverviewNumberPaint.ascent();
				canvas.drawText(Integer.toString(mNumberTotal), 
						mCenterX - totalNumberLength / 2,
						mCenterY + textDescent - numberTextAscent, 
						mTextOverviewNumberPaint);
				
				// center and draw the "min total" text
				float totalLength = mTextOverviewTotalPaint.measureText(mOverviewTotalText);
				float minTotalStartX = mCenterX - (minLength + totalLength) / 2;
				
				canvas.drawText(mOverviewMinText, 
						minTotalStartX, 
						mCenterY + 2*textDescent - numberTextAscent - textAscent, mTextOverviewMinPaint);
				
				canvas.drawText(mOverviewTotalText, 
						minTotalStartX + minLength, 
						mCenterY + 2*textDescent - numberTextAscent - textAscent, mTextOverviewTotalPaint);
			}
			break;
		}
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
					isPressedState ? mRestProgressClickBackgroundPaint
							: mRestProgressBackgroundPaint);
			canvas.drawArc(mRestCircleOval, 270, mCalculatedRestArc, true,
					isPressedState ? mRestProgressClickForegroundPaint
							: mRestProgressForegroundPaint);
			break;
		}
		default:
			canvas.drawText("State not set!", mCenterX, mCenterY,
					mRestProgressForegroundPaint);
		}

	}
}
