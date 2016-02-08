package org.random_access.newsreader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 27.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MsgHierarchyView extends View {

    private static final String TAG = MsgHierarchyView.class.getSimpleName();

    private Paint mPaint;
    private int level;

    private final float strokeWidth = 10.0F;
    private final float paddingBetween = 5.0F;


    public MsgHierarchyView(Context context) {
        super(context, null, 0);
        init(context);
    }

    public MsgHierarchyView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet, 0);
        init(context);
    }

    public MsgHierarchyView(Context context, AttributeSet attributeSet, int defStyleAttr ) {
        super(context, attributeSet, defStyleAttr);
        init(context);
    }


    public void setLevel(int level) {
        this.level = level;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int preferred =  (level * (int)(strokeWidth + paddingBetween) + (int)paddingBetween);
        return getMeasurement(measureSpec,preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = super.getMeasuredHeight();
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement;

        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }



    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAlpha(255);
        mPaint.setColor(ContextCompat.getColor(context, R.color.blue));
        mPaint.setStrokeWidth(strokeWidth);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < level; i++) {
            canvas.drawLine(getPaddingLeft() + paddingBetween + (strokeWidth+paddingBetween) * i, 0,
                    getPaddingLeft() + paddingBetween + (strokeWidth+paddingBetween) * i,getMeasuredHeight(), mPaint);

        }
    }
}
