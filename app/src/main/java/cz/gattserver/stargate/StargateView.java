package cz.gattserver.stargate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.View;

public class StargateView extends View {

    private int bevel = 10;
    private int strokeWidth = 3;

    private int screenW, screenH;
    private int angle;

    private long now;
    private long delay;

    private long randomStringTime = 0;
    private long randomStringDelay = 100;
    private String randomString;

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

    private void generateString(int chars) {
        String s = "";
        for (int c = 0; c < chars; c++)
            s += (char) (65 + Math.random() * 25);
        randomString = s;
        randomStringTime = now;
    }

    private void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
        canvas.drawRect(0, 0, screenW, screenH, paint);
    }

    private void drawSegment1(Canvas canvas) {
        float x = bevel;
        float y = bevel;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(0xff, 0x00, 0xa0, 0xff));
        paint.setStrokeWidth(strokeWidth);
        canvas.drawRect(x, y, 200, 290, paint);

        int wSpacing = 10;
        int hSpacing = 15;
        int size = 10;

        x += 10;
        y += 22;

        int rows = 11;
        int cols = 9;
        if (randomString == null || (now - randomStringTime > randomStringDelay))
            generateString(rows * cols);

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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.save();

        now = System.currentTimeMillis();
        delay = now - lastFrame;
        lastFrame = now;

        drawBackground(canvas);
        drawSegment1(canvas);

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