package com.aalamstudio.compiler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Circular ring progress indicator used on the Build APK screen (matches the
 * 78% amber ring in the mockup). Track = dark grey, progress arc = amber,
 * rounded caps, starts from top (-90deg) and sweeps clockwise.
 */
public class CircularProgressView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private RectF arcRect = new RectF();

    private float progress = 0f; // 0..100
    private float strokeWidth = 18f;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(Color.parseColor("#26262A"));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#F2A93B")); // amber
    }

    public void setStrokeWidthDp(float widthPx) {
        this.strokeWidth = widthPx;
        trackPaint.setStrokeWidth(widthPx);
        progressPaint.setStrokeWidth(widthPx);
        invalidate();
    }

    /** Animate/set progress 0-100 */
    public void setProgress(float percent) {
        this.progress = Math.max(0f, Math.min(100f, percent));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pad = strokeWidth / 2f + 4f;
        arcRect.set(pad, pad, getWidth() - pad, getHeight() - pad);

        // background track (full circle)
        canvas.drawArc(arcRect, 0, 360, false, trackPaint);

        // progress arc, starts at top (-90deg), clockwise
        float sweep = 360f * (progress / 100f);
        canvas.drawArc(arcRect, -90f, sweep, false, progressPaint);
    }
}
