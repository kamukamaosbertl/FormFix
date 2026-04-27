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
    // DRAWING PAINTS
    // =========================================================
    private final Paint pointPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint clearPaint = new Paint();

    // =========================================================
    // DATA TO DRAW
    // =========================================================
    private final Object drawLock = new Object();
    private List<PointF> points = new ArrayList<>();
    private List<int[]> connections = new ArrayList<>();

    // =========================================================
    // RENDER THREAD STATE
    // =========================================================
    private SurfaceHolder surfaceHolder;
    private Thread renderThread;
    private boolean surfaceAvailable = false;
    private boolean renderRequested = false;
    private boolean running = false;
    private boolean dataDirty = false; // true only when new pose data has arrived


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

    private void init() {
        // -----------------------------------------------------
        // Transparent SurfaceView so camera preview remains visible below
        // -----------------------------------------------------
        setZOrderOnTop(true);
        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(this);

        // -----------------------------------------------------
        // Point style
        // -----------------------------------------------------
        pointPaint.setColor(Color.GREEN);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        // -----------------------------------------------------
        // Line style
        // -----------------------------------------------------
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setAntiAlias(true);

        // -----------------------------------------------------
        // Clear paint for transparent redraw
        // -----------------------------------------------------
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setStyle(Paint.Style.FILL);
    }

    // =========================================================
    // PUBLIC API
    // =========================================================
    public void setPoseData(@Nullable List<PointF> points, @Nullable List<int[]> connections) {
        synchronized (drawLock) {
            this.points = points != null ? new ArrayList<>(points) : new ArrayList<>();
            this.connections = connections != null ? new ArrayList<>(connections) : new ArrayList<>();
            dataDirty = true;         // mark that there is something new to draw
            renderRequested = true;
            drawLock.notifyAll();
        }
    }

    public void clearPose() {
        synchronized (drawLock) {
            this.points.clear();
            this.connections.clear();
            renderRequested = true;
            drawLock.notifyAll();
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
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
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
        if (running) {
            return;
        }

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

            if (!surfaceAvailable) {
                continue;
            }

            drawFrame();
        }
    }

    // =========================================================
    // DRAWING
    // =========================================================
    private void drawFrame() {
        // Bail early if nothing changed since last draw.
        // This prevents redundant canvas locks during heavy inference on low-end devices.
        synchronized (drawLock) {
            if (!dataDirty) {
                return;
            }
            dataDirty = false;
        }
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }

            // -------------------------------------------------
            // Clear old frame
            // -------------------------------------------------
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);

            List<PointF> localPoints;
            List<int[]> localConnections;

            synchronized (drawLock) {
                localPoints = new ArrayList<>(points);
                localConnections = new ArrayList<>(connections);
            }

            // -------------------------------------------------
            // Draw skeleton connections
            // -------------------------------------------------
            for (int[] pair : localConnections) {
                int start = pair[0];
                int end = pair[1];

                if (start >= 0 && start < localPoints.size()
                        && end >= 0 && end < localPoints.size()) {

                    PointF p1 = localPoints.get(start);
                    PointF p2 = localPoints.get(end);

                    if (p1 != null && p2 != null) {
                        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint);
                    }
                }
            }

            // -------------------------------------------------
            // Draw landmark points
            // -------------------------------------------------
            for (PointF point : localPoints) {
                if (point != null) {
                    canvas.drawCircle(point.x, point.y, 10f, pointPaint);
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