package cz.gattserver.stargate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.View;

public class StargateView extends View {

    private int bevel = 20;
    private int strokeWidth = 3;

    // Text random
    private int rows = 11;
    private int cols = 9;

    private int screenW, screenH;
    private int angle;

    private long randomRefreshTime = 0;
    private long randomRefreshDelay = 100;
    private String randomString;
    private int randomBits[] = new int[4];

    private long lastFrame = 0;

    public StargateView(Context context) {
        super(context);
        angle = 0;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenW = w;
        screenH = h;
    }

    private Paint createBasePaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(0xff, 0x00, 0xa0, 0xff));
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(false);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeJoin(Paint.Join.MITER);
        return paint;
    }

    private void generateRandom() {
        String s = "";
        for (int c = 0; c < rows * cols; c++)
            s += (char) (65 + Math.random() * 25);
        randomString = s;

        for (int i = 0; i < 4; i++)
            randomBits[i] = (int) (Math.random() * (1 << 3 * 3));
    }

    private void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
        canvas.drawRect(0, 0, screenW, screenH, paint);
    }

    private void drawSegment1(Canvas canvas) {
        float x = bevel;
        float y = bevel;

        Paint paint = createBasePaint();
        paint.setStrokeWidth(strokeWidth);
        canvas.drawRect(x, y, x + 190, y + 280, paint);

        int wSpacing = 10;
        int hSpacing = 15;
        int size = 10;

        x += 10;
        y += 22;

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setStrokeWidth(2);
        paint.setTextSize(20);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                canvas.drawText("" + randomString.charAt(i * cols + j), x, y, paint);
                x += size + wSpacing;
            }
            x = bevel + 10;
            y += size + hSpacing;
        }
    }

    private void drawSegment2(Canvas canvas) {
        Paint paint = createBasePaint();

        int baseSize = 190;
        int spacing = 20;

        Path path = new Path();
        float x = bevel;
        float y = bevel + 280 + bevel;
        path.moveTo(x, y);
        path.lineTo(x, y + baseSize);
        path.moveTo(x + baseSize, y);
        path.lineTo(x + baseSize, y + baseSize);
        canvas.drawPath(path, paint);

        float cellWidth = (baseSize - spacing * 3) / 2;
        float cellHeight = (baseSize - spacing) / 2;
        float cellHeight4 = cellHeight / 4;
        float cellWidth3 = cellWidth / 3;
        float radius = cellWidth3 / 4;
        float spacingRadius = radius + 2;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                float cx = x + spacing + (cellWidth + spacing) * i;
                float cy = y + (cellHeight + spacing) * j;
                paint = createBasePaint();

                path = new Path();
                path.moveTo(cx, cy);
                path.lineTo(cx + cellWidth, cy);
                path.moveTo(cx + cellWidth / 2, cy);
                path.lineTo(cx + cellWidth / 2, cy + cellHeight4);
                path.moveTo(cx + cellWidth3, cy + cellHeight4);
                path.lineTo(cx + cellWidth3 * 2, cy + cellHeight4);
                path.lineTo(cx + cellWidth3 * 3, cy + cellHeight4 * 2);
                path.lineTo(cx + cellWidth3 * 3, cy + cellHeight4 * 3);
                path.lineTo(cx + cellWidth3 * 2, cy + cellHeight4 * 4);
                path.lineTo(cx + cellWidth3, cy + cellHeight4 * 4);
                path.lineTo(cx, cy + cellHeight4 * 3);
                path.lineTo(cx, cy + cellHeight4 * 2);
                path.lineTo(cx + cellWidth3, cy + cellHeight4);
                path.lineTo(cx + cellWidth3 * 2, cy + cellHeight4);
                canvas.drawPath(path, paint);

                paint = createBasePaint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));
                for (int ci = 0; ci < 3; ci++) {
                    for (int cj = 0; cj < 3; cj++) {
                        if ((randomBits[i * 2 + j] & 1 << (ci * 3 + cj)) > 0)
                            canvas.drawCircle((float) (strokeWidth / 2 + cx + cellWidth3 - radius + ci * spacingRadius * 2), (float) (strokeWidth / 2 + cy + cellHeight4 * 2 - radius + cj * spacingRadius * 2), radius, paint);
                        //canvas.drawCircle((float) (cx + cellWidth3 * 1.5 + radius * 2 * ci), (float) (cy + radius + cellHeight4 * (1 + cj + 0.5)), radius, paint);
                    }
                }
            }
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.save();

        long now = System.currentTimeMillis();
        if (now - randomRefreshTime > randomRefreshDelay) {
            generateRandom();
            randomRefreshTime = now;
        }

        drawBackground(canvas);
        drawSegment1(canvas);
        drawSegment2(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.argb(0xff, 0x99, 0x00, 0x00));
        paint.setStrokeWidth(2);
        paint.setAntiAlias(false);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStyle(Paint.Style.STROKE);

        Path path = new Path();

        float x = screenW / 2;
        float y = screenH / 2;
        float r = 50;

        double rad = angle / 180f * Math.PI;
        float x1 = (float) (x + Math.cos(rad) * r);
        float y1 = (float) (y + Math.sin(rad) * r);

        angle = (angle + 1) % 360;
        rad = angle / 180f * Math.PI;
        float x2 = (float) (x + Math.cos(rad) * r);
        float y2 = (float) (y + Math.sin(rad) * r);

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        canvas.drawPath(path, paint);

        //canvas.restore();
        invalidate();
    }
}