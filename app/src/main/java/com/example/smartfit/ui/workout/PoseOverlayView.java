package com.example.smartfit.ui.workout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PoseOverlayView extends SurfaceView implements SurfaceHolder.Callback {

    // =========================================================
    // SMOOTHING CONFIGURATION
    // =========================================================

    /**
     * Exponential smoothing factor.
     * 0.0 = no movement
     * 1.0 = no smoothing
     *
     * 0.35–0.55 works best for pose landmarks.
     */
    private static final float SMOOTHING_ALPHA = 0.45f;

    // =========================================================
    // PAINTS
    // =========================================================

    private final Paint pointPaint = new Paint();
    private final Paint linePaint = new Paint();

    // =========================================================
    // DATA
    // =========================================================

    private final Object drawLock = new Object();

    private final List<PointF> points = new ArrayList<>();
    private final List<int[]> connections = new ArrayList<>();

    private final List<PointF> renderPoints = new ArrayList<>();
    private final List<int[]> renderConnections = new ArrayList<>();

    // Smoothed landmark buffer
    private final List<PointF> smoothedPoints = new ArrayList<>();

    // =========================================================
    // RENDER THREAD STATE
    // =========================================================

    private SurfaceHolder surfaceHolder;
    private Thread renderThread;

    private boolean surfaceAvailable = false;
    private boolean renderRequested = false;
    private boolean running = false;
    private boolean dataDirty = false;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public PoseOverlayView(Context context) {
        super(context);
        init();
    }

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // =========================================================
    // INITIALIZATION
    // =========================================================

    private void init() {

        setZOrderOnTop(true);

        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(this);

        // Point style
        pointPaint.setColor(Color.GREEN);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        // Line style
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setAntiAlias(true);
    }

    // =========================================================
    // PUBLIC API
    // =========================================================

    public void setPoseData(@Nullable List<PointF> newPoints,
                            @Nullable List<int[]> newConnections) {

        synchronized (drawLock) {

            // Update raw points
            points.clear();
            if (newPoints != null) {
                points.addAll(newPoints);
            }

            // Update connections
            connections.clear();
            if (newConnections != null) {
                connections.addAll(newConnections);
            }

            applySmoothing();

            dataDirty = true;
            renderRequested = true;
            drawLock.notifyAll();
        }
    }

    public void clearPose() {
        synchronized (drawLock) {

            points.clear();
            connections.clear();
            smoothedPoints.clear();

            dataDirty = true;
            renderRequested = true;

            drawLock.notifyAll();
        }
    }

    // =========================================================
    // POSE SMOOTHING FILTER
    // =========================================================

    /**
     * Applies exponential smoothing to reduce jitter
     * between frames.
     */
    private void applySmoothing() {

        if (points.isEmpty()) {
            smoothedPoints.clear();
            return;
        }

        if (smoothedPoints.size() != points.size()) {

            smoothedPoints.clear();

            for (PointF p : points) {
                smoothedPoints.add(new PointF(p.x, p.y));
            }

            return;
        }

        for (int i = 0; i < points.size(); i++) {

            PointF current = points.get(i);
            PointF previous = smoothedPoints.get(i);

            if (current == null || previous == null) {
                continue;
            }

            previous.x = previous.x + SMOOTHING_ALPHA * (current.x - previous.x);
            previous.y = previous.y + SMOOTHING_ALPHA * (current.y - previous.y);
        }
    }

    // =========================================================
    // SURFACE CALLBACKS
    // =========================================================

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceAvailable = true;
        startRenderThread();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder,
                               int format,
                               int width,
                               int height) {
        surfaceAvailable = true;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceAvailable = false;
        stopRenderThread();
    }

    // =========================================================
    // RENDER THREAD
    // =========================================================

    private void startRenderThread() {

        if (running) return;

        running = true;

        renderThread = new Thread(this::renderLoop, "PoseOverlayRenderThread");
        renderThread.start();
    }

    private void stopRenderThread() {

        running = false;

        synchronized (drawLock) {
            drawLock.notifyAll();
        }

        if (renderThread != null) {
            try {
                renderThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            renderThread = null;
        }
    }

    private void renderLoop() {

        while (running) {

            synchronized (drawLock) {

                while (running && !renderRequested) {

                    try {
                        drawLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                renderRequested = false;
            }

            if (!surfaceAvailable) continue;

            drawFrame();
        }
    }

    // =========================================================
    // DRAWING
    // =========================================================

    private void drawFrame() {

        synchronized (drawLock) {
            if (!dataDirty) return;
            dataDirty = false;
        }

        Canvas canvas = null;

        try {

            canvas = surfaceHolder.lockCanvas();

            if (canvas == null) return;

            canvas.drawColor(Color.TRANSPARENT);

            synchronized (drawLock) {

                renderPoints.clear();
                renderPoints.addAll(smoothedPoints);

                renderConnections.clear();
                renderConnections.addAll(connections);
            }

            // Draw skeleton lines
            for (int[] pair : renderConnections) {

                int start = pair[0];
                int end = pair[1];

                if (start >= renderPoints.size() || end >= renderPoints.size())
                    continue;

                PointF p1 = renderPoints.get(start);
                PointF p2 = renderPoints.get(end);

                if (p1 != null && p2 != null) {

                    canvas.drawLine(
                            p1.x,
                            p1.y,
                            p2.x,
                            p2.y,
                            linePaint
                    );
                }
            }

            // Draw landmark points
            for (PointF point : renderPoints) {

                if (point != null) {

                    canvas.drawCircle(
                            point.x,
                            point.y,
                            10f,
                            pointPaint
                    );
                }
            }

        } finally {

            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    public void release() {
        stopRenderThread();
    }
}