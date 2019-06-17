package com.zl.ncnn;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class FaceRectView extends View {
    private Paint paint;
    private Rect rect;
    private int lineWidth = 24;
    private int lineHeight = 24;
    private int strokeWidth = 8;
    private int strokeColor = Color.WHITE;

    public FaceRectView(Context context) {
        super(context);
        init();
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(attrs);
        init();
    }

    private void initAttr(AttributeSet attrs) {
        if (null == attrs) return;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FaceRectView);
        strokeColor = typedArray.getColor(R.styleable.FaceRectView_strokeColor, Color.WHITE);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.FaceRectView_strokeWidth, 6);
    }

    public void init() {
        paint = new Paint();
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        rect = new Rect(0, 0, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == rect) return;
        float[] lines = {
                rect.left, rect.top, rect.left + lineWidth, rect.top,
                rect.left, rect.top, rect.left, rect.top + lineHeight,
                rect.right - lineWidth, rect.top, rect.right, rect.top,
                rect.right, rect.top, rect.right, rect.top + lineWidth,
                rect.left, rect.bottom - lineHeight, rect.left, rect.bottom,
                rect.left, rect.bottom, rect.left + lineWidth, rect.bottom,
                rect.right, rect.bottom - lineHeight, rect.right, rect.bottom,
                rect.right - lineWidth, rect.bottom, rect.right, rect.bottom,
        };
        canvas.drawLines(lines, paint);
        float[] points = {
                rect.left, rect.top,
                rect.right, rect.top,
                rect.left, rect.bottom,
                rect.right, rect.bottom,
        };
        canvas.drawPoints(points, paint);
    }

    public void setRect(Rect rect) {
        this.rect = rect;
        lineWidth = rect.width() / 4;
        lineHeight = rect.height() / 4;
        invalidate();
    }

    public void setTip(String tip) {

    }

    public void setRect(int left, int top, int right, int bottom) {
        this.rect.left = left;
        this.rect.top = top;
        this.rect.right = right;
        this.rect.bottom = bottom;
        lineWidth = rect.width() / 5;
        lineHeight = rect.height() / 5;
        invalidate();
    }


    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        this.paint.setStrokeWidth(strokeWidth);
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        this.paint.setColor(strokeColor);
    }

    public Paint getPaint() {
        return paint;
    }

    public Rect getRect() {
        return rect;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }
}
