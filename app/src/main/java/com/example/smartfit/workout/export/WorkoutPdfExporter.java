package com.example.smartfit.workout.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.example.smartfit.workout.db.WorkoutSessionEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WorkoutPdfExporter {

    private static final int PAGE_WIDTH  = 595;   // A4 at 72dpi
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN      = 48;
    private static final int LINE_HEIGHT = 22;

    private final Paint titlePaint;
    private final Paint headerPaint;
    private final Paint bodyPaint;
    private final Paint mutedPaint;
    private final Paint dividerPaint;

    public WorkoutPdfExporter() {
        titlePaint = new Paint();
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.BLACK);

        headerPaint = new Paint();
        headerPaint.setTextSize(13f);
        headerPaint.setFakeBoldText(true);
        headerPaint.setColor(Color.BLACK);

        bodyPaint = new Paint();
        bodyPaint.setTextSize(11f);
        bodyPaint.setColor(Color.BLACK);

        mutedPaint = new Paint();
        mutedPaint.setTextSize(10f);
        mutedPaint.setColor(Color.GRAY);

        dividerPaint = new Paint();
        dividerPaint.setColor(Color.LTGRAY);
        dividerPaint.setStrokeWidth(0.5f);
    }

    /**
     * Writes a PDF of all sessions to the app's external files directory.
     * Returns the File on success, throws IOException on failure.
     * Must be called off the main thread.
     */
    @WorkerThread
    @NonNull
    public File export(@NonNull Context context,
                       @NonNull List<WorkoutSessionEntity> sessions) throws IOException {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = MARGIN;

        // Title
        canvas.drawText("SmartFit — workout history", MARGIN, y, titlePaint);
        y += 10;

        // Export date
        String exportDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date());
        canvas.drawText("Exported " + exportDate, MARGIN, y + LINE_HEIGHT, mutedPaint);
        y += LINE_HEIGHT * 2 + 8;

        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint);
        y += 16;

        // Column headers
        canvas.drawText("Type",      MARGIN,       y, headerPaint);
        canvas.drawText("Date",      MARGIN + 120, y, headerPaint);
        canvas.drawText("Duration",  MARGIN + 260, y, headerPaint);
        canvas.drawText("Reps/Hold", MARGIN + 360, y, headerPaint);
        canvas.drawText("Result",    MARGIN + 450, y, headerPaint);
        y += 6;
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint);
        y += LINE_HEIGHT;

        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());

        for (WorkoutSessionEntity session : sessions) {
            // Start a new page if we're near the bottom
            if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 2) {
                document.finishPage(page);
                PdfDocument.PageInfo nextPage = new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH, PAGE_HEIGHT,
                        document.getPages().size() + 1).create();
                page = document.startPage(nextPage);
                canvas = page.getCanvas();
                y = MARGIN;
            }

            String type     = formatType(session.workoutType);
            String date     = dateFmt.format(new Date(session.startTimeMillis));
            String duration = formatDuration(session.activeDurationMillis);
            String metric   = formatMetric(session);
            String state    = formatState(session.state);

            canvas.drawText(type,     MARGIN,       y, bodyPaint);
            canvas.drawText(date,     MARGIN + 120, y, bodyPaint);
            canvas.drawText(duration, MARGIN + 260, y, bodyPaint);
            canvas.drawText(metric,   MARGIN + 360, y, bodyPaint);
            canvas.drawText(state,    MARGIN + 450, y, bodyPaint);

            y += LINE_HEIGHT;
        }

        document.finishPage(page);

        // Write to Downloads/SmartFit/ — scoped storage safe for API 29+
        File dir = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), "SmartFit");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "smartfit_history_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".pdf";
        File outputFile = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            document.writeTo(fos);
        } finally {
            document.close();
        }

        return outputFile;
    }

    private String formatType(String rawType) {
        if (rawType == null) return "—";
        return rawType.replace("_", " ").toLowerCase(Locale.getDefault());
    }

    private String formatDuration(long millis) {
        long mins = TimeUnit.MILLISECONDS.toMinutes(millis);
        long secs = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format(Locale.getDefault(), "%dm %02ds", mins, secs);
    }

    private String formatMetric(WorkoutSessionEntity session) {
        if (session.repCount > 0) {
            return session.repCount + " reps";
        }
        if (session.holdDurationMillis > 0) {
            return formatDuration(session.holdDurationMillis) + " hold";
        }
        return "—";
    }

    private String formatState(String state) {
        if ("COMPLETED".equals(state)) return "done";
        if ("CANCELLED".equals(state)) return "cancelled";
        return state != null ? state.toLowerCase(Locale.getDefault()) : "—";
    }
}