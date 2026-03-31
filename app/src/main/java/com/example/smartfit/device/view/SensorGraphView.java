package com.example.smartfit.device.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SensorGraphView extends View {

    private final List<Float> values = new ArrayList<>();

    private final Paint linePaint = new Paint();
    private final Paint axisPaint = new Paint();

    private static final int MAX_POINTS = 100;

    public SensorGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);

        axisPaint.setColor(Color.GRAY);
        axisPaint.setStrokeWidth(2f);
    }

    public void addValue(float value) {

        values.add(value);

        if (values.size() > MAX_POINTS) {
            values.remove(0);
        }

        invalidate(); // redraw view
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        // Draw axis
        canvas.drawLine(0, height / 2, width, height / 2, axisPaint);

        if (values.size() < 2) return;

        float stepX = width / (float) MAX_POINTS;

        float prevX = 0;
        float prevY = height / 2 - values.get(0);

        for (int i = 1; i < values.size(); i++) {

            float x = i * stepX;
            float y = height / 2 - values.get(i);

            canvas.drawLine(prevX, prevY, x, y, linePaint);

            prevX = x;
            prevY = y;
        }
    }
}