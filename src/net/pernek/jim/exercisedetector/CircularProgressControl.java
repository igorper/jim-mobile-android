package net.pernek.jim.exercisedetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class CircularProgressControl extends View {
	private static final int TRAIN_PROG_BG_COLOR = 0xFFD1D2D4;
	private static final int TRAIN_PROG_FG_COLOR = 0xFFEAB130;
	private static final int EXER_PROG_BG_COLOR = 0xFFBDBEC2;
	private static final int EXER_PROG_FG_COLOR = 0xFF55A0C0;
	private static final int REST_PROG_BG_COLOR = 0xFFFBC911;
	private static final int REST_PROG_FG_COLOR = 0xFFE1563E;
	
	private static final int PROG_THICK_IN_DIP = 10;
	
    private Paint mTrainingProgressBackgroundPaint;
    private Paint mTrainingProgressForegroundPaint;
    private Paint mExerciseProgressBackgroundPaint;
    private Paint mExerciseProgressForegroundPaint;
    private Paint mRestProgressBackgroundPaint;
    private Paint mRestProgressForegroundPaint;
	
	private float mProgThicknessInPx;
    
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
     * layout file. These attributes are defined in
     * SDK/assets/res/any/classes.xml.
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public CircularProgressControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLabelView();
    }

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
    	width = MeasureSpec.getSize(widthMeasureSpec);
    	
    	setMeasuredDimension(width, width);
    	
    	circleRadius = width / 2;
    	mCenterX = mCenterY = width / 2;
    	mExerciseCircleRadius = circleRadius - mProgThicknessInPx;
    	mRestCircleRadius = mExerciseCircleRadius - mProgThicknessInPx;
    	
    	mTrainingCircleOval = new RectF(0, 0, width, width);
    	mExerciseCircleOval = new RectF(mProgThicknessInPx, mProgThicknessInPx, width - mProgThicknessInPx, width - mProgThicknessInPx);
    	mRestCircleOval = new RectF(2*mProgThicknessInPx, 2*mProgThicknessInPx, width - 2*mProgThicknessInPx, width - 2*mProgThicknessInPx);
    }
    
    private RectF mTrainingCircleOval;
    private RectF mExerciseCircleOval;
    private RectF mRestCircleOval;

    private int width;
    int circleRadius;
    private float mCenterX;
    private float mCenterY;
    private float mExerciseCircleRadius;
    private float mRestCircleRadius; 
   
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
        canvas.drawCircle(mCenterX, mCenterY, circleRadius, mTrainingProgressBackgroundPaint);
        canvas.drawArc(mTrainingCircleOval, 270, 90, true, mTrainingProgressForegroundPaint);
        canvas.drawCircle(mCenterX, mCenterY, mExerciseCircleRadius, mExerciseProgressBackgroundPaint);
        canvas.drawArc(mExerciseCircleOval, 270, 180, true, mExerciseProgressForegroundPaint);
        canvas.drawCircle(circleRadius, circleRadius, mRestCircleRadius, mRestProgressBackgroundPaint);
        canvas.drawArc(mRestCircleOval, 270, 270, true, mRestProgressForegroundPaint);
    }
}

