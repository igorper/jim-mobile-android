package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.util.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
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
public class CircularProgressControl extends Button{
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
	 * Thickness of training and exercise progress bars in dp. 
	 */
	private static final int PROG_THICK_IN_DIP = 10;
		
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
     *  Max value for the exercise progress bar (default 100)
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
	 * Calculated training and exercise progress bar
	 * thickness in px.
	 */
	private float mProgThicknessInPx;
	
	/**
	 * Precalculated value of the training
	 * arc length.
	 */
	private float mCalculatedTrainingArc;
	
	/**
	 * Precalculated value of the exercise
	 * arc length.
	 */
	private float mCalculatedExerciseArc;
	
	/**
	 * Precalculate value of the rest
	 * arc length.
	 */
	private float mCalculatedRestArc;
	
	/*
     * END PRECALCULATED VALUES FOR FAST DRAWING.
     */
        
	/** Calculates the arc length based on the input values. 
	 * @param value
	 * @param max
	 * @param min
	 * @return a number between [0, 360] if the value is between [min, max], 
	 * otherwise the number can be out of bounds.
	 */
	public static float calculateArc(float value, float max, float min){
		return (value - min) / (max - min)  * 360;
	}
	 
    /** Sets the active value of the training progress bar and invalidates the screen.
     * The training progress bar visualizes the training completion.
     */
    public void setTrainingProgressValue(int value){
    	mTrainingProgressValue = value;
    	
    	mCalculatedTrainingArc = calculateArc(mTrainingProgressValue, mTrainingMaxProgress, mTrainingMinProgress);
    	
    	invalidate();
    	
    	Log.d(TAG, Integer.toString(mTrainingProgressValue));
    }
    
    /** Sets the active value of the exercise progress bar and invalidates the screen.
     * The exercise progress bar visualizes the exercise completion.
     */
    public void setExerciseProgressValue(int value){
    	mExerciseProgressValue = value;
    	
    	mCalculatedExerciseArc  = calculateArc(mExerciseProgressValue, mExerciseMaxProgress, mExerciseMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the active value of the rest progress bar and invalidates the screen.
     * The rest progress bar visualizes the rest interval.
     */
    public void setRestProgressValue(int value){
    	mRestProgressValue = value;
    	
    	mCalculatedRestArc = calculateArc(mRestProgressValue, mRestMaxProgress, mRestMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the max value of the training progress bar and invalidates the screen.
     * This value indicates when the training progress bar is full.
     */
    public void setTrainingMaxProgress(int value){
    	mTrainingMaxProgress = value;
    	
    	mCalculatedTrainingArc = calculateArc(mTrainingProgressValue, mTrainingMaxProgress, mTrainingMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the min value of the training progress bar and invalidates the screen.
     * This value indicates when the training progress bar is empty.
     */
    public void setTrainingMinProgress(int value){
    	mTrainingMinProgress  = value;
    	
    	mCalculatedTrainingArc = calculateArc(mTrainingProgressValue, mTrainingMaxProgress, mTrainingMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the max value of the rest progress bar and invalidates the screen.
     * This value indicates when the rest progress bar is full.
     */
    public void setRestMaxProgress(int value){
    	mRestMaxProgress = value;
    	
    	mCalculatedRestArc = calculateArc(mRestProgressValue, mRestMaxProgress, mRestMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the min value of the rest progress bar and invalidates the screen.
     * This value indicates when the rest progress bar is empty.
     */
    public void setRestMinProgress(int value){
    	mRestMinProgress = value;
    	
    	mCalculatedRestArc = calculateArc(mRestProgressValue, mRestMaxProgress, mRestMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the max value of the exercise progress bar and invalidates the screen.
     * This value indicates when the exercise progress bar is full.
     */
    public void setExerciseMaxProgress(int value){
    	mExerciseMaxProgress = value;
    	
    	mCalculatedExerciseArc  = calculateArc(mExerciseProgressValue, mExerciseMaxProgress, mExerciseMinProgress);
    	
    	invalidate();
    }
    
    /** Sets the min value of the rest progress bar and invalidates the screen.
     * This value indicates when the rest progress bar is empty.
     */
    public void setExerciseMinProgress(int value){
    	mExerciseMinProgress = value;
    	
    	mCalculatedExerciseArc  = calculateArc(mExerciseProgressValue, mExerciseMaxProgress, mExerciseMinProgress);
    	
    	invalidate();
    }
    
    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
    public CircularProgressControl(Context context) {
        super(context);
        initLabelView();
    }

    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file. 
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public CircularProgressControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLabelView();
    }

    /**
     * Initializes all drawing objects. 
     */
    private final void initLabelView() {
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
    	        
    	mProgThicknessInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PROG_THICK_IN_DIP, getResources().getDisplayMetrics());
    	
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
    	mTrainingCircleRadius = width / 2;
    	mCenterX = mCenterY = width / 2;
    	mExerciseCircleRadius = mTrainingCircleRadius - mProgThicknessInPx;
    	mRestCircleRadius = mExerciseCircleRadius - mProgThicknessInPx;
    	
    	mTrainingCircleOval = new RectF(0, 0, width, width);
    	mExerciseCircleOval = new RectF(mProgThicknessInPx, mProgThicknessInPx, 
    			width - mProgThicknessInPx, width - mProgThicknessInPx);
    	mRestCircleOval = new RectF(2*mProgThicknessInPx, 2*mProgThicknessInPx, 
    			width - 2*mProgThicknessInPx, width - 2*mProgThicknessInPx);
    }
      
    /**
     * Render the circular progress control.
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // draw training, exercise and rest progress bars
        canvas.drawCircle(mCenterX, mCenterY, 
        		mTrainingCircleRadius, mTrainingProgressBackgroundPaint);
        canvas.drawArc(mTrainingCircleOval, 270, 
        		mCalculatedTrainingArc, 
        		true, mTrainingProgressForegroundPaint);
        canvas.drawCircle(mCenterX, mCenterY, 
        		mExerciseCircleRadius, mExerciseProgressBackgroundPaint);
        canvas.drawArc(mExerciseCircleOval, 270, 
        		mCalculatedExerciseArc, true, 
        		mExerciseProgressForegroundPaint);
        if(!isPressed()){
        canvas.drawCircle(mTrainingCircleRadius, mTrainingCircleRadius, 
        		mRestCircleRadius, mRestProgressBackgroundPaint);
        }
        canvas.drawArc(mRestCircleOval, 270, 
        		mCalculatedRestArc, true, 
        		mRestProgressForegroundPaint);
    }
}

