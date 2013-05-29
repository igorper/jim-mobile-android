package net.pernek.jim.exercisedetector;

import net.pernek.jim.exercisedetector.util.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * @author Igor
 *
 */
public class CircularProgressControl extends View {
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
	 * A modifier for converting from input values to progress circle values
	 * (e.g. 3.6 if input values are between [0,100]) 
	 */
	private static final float VALUE_CIRCLE_MODIFIER = 3.6f;
	
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
        
	 
    /** Set value of training progress bar.
     * @param value should be between [0,100], if larger
     * the progress bar will be full.
     */
    public void setTrainingProgressValue(int value){
    	mTrainingProgressValue = value;
    	invalidate();
    	
    	Log.d(TAG, Integer.toString(mTrainingProgressValue));
    }
    
    /** Set value of exercise progress bar.
     * @param value should be between [0,100], if larger
     * the progress bar will be full.
     */
    public void setExerciseProgressValue(int value){
    	mExerciseProgressValue = value;
    	invalidate();
    }
    
    /** Set value of rest progress bar.
     * @param value should be between [0,100], if larger
     * the progress bar will be full.
     */
    public void setRestProgressValue(int value){
    	mRestProgressValue = value;
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
    }
    
    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	int width = MeasureSpec.getSize(widthMeasureSpec);
    	
    	setMeasuredDimension(width, width);
    	
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
        		mTrainingProgressValue *  VALUE_CIRCLE_MODIFIER, 
        		true, mTrainingProgressForegroundPaint);
        canvas.drawCircle(mCenterX, mCenterY, 
        		mExerciseCircleRadius, mExerciseProgressBackgroundPaint);
        canvas.drawArc(mExerciseCircleOval, 270, 
        		mExerciseProgressValue *  VALUE_CIRCLE_MODIFIER, true, 
        		mExerciseProgressForegroundPaint);
        canvas.drawCircle(mTrainingCircleRadius, mTrainingCircleRadius, 
        		mRestCircleRadius, mRestProgressBackgroundPaint);
        canvas.drawArc(mRestCircleOval, 270, 
        		mRestProgressValue *  VALUE_CIRCLE_MODIFIER, true, 
        		mRestProgressForegroundPaint);
    }
}

