package com.aalamstudio.compiler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    private Paint backgroundPaint, progressPaint;
    private RectF rectF;
    private int progress = 0;
    private int strokeWidth = 18;

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#2A2A2A"));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(Color.parseColor("#F5A623"));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setAntiAlias(true);

        rectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int size = Math.min(w, h);
        int padding = strokeWidth;
        rectF.set(padding, padding, size - padding, size - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);
        float sweepAngle = 360 * progress / 100f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }
}
