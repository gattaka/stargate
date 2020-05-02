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
    private int spacing = 20;

    private int segment1Height = 340;
    private int segment1Width = 210;

    private int screenW, screenH;

    private long randomRefreshTime = 0;
    private long randomRefreshDelay = 100;

    private int textSize = 16;
    private int randomStringsOffset = 0;
    private int randomStringsPointer = 0;
    private int randomStringsLines = 21;
    private String randomStrings[] = new String[randomStringsLines];

    private int randomBits[] = new int[4];

    public StargateView(Context context) {
        super(context);

        for (int l = 0; l < randomStringsLines; l++)
            randomStrings[l] = randomString();
    }

    private String randomString() {
        String s = "";
        int len = (int) (Math.random() * 20);
        for (int c = 0; c < len; c++)
            s += (char) (Math.random() * 255);
        return s;
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
        for (int i = 0; i < 4; i++)
            randomBits[i] = (int) (Math.random() * (1 << 3 * 3));
    }

    private void drawBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
        canvas.drawRect(0, 0, screenW, screenH, paint);

        paint = createBasePaint();
        canvas.drawRect(0, 0, 1024, 768, paint);
    }

    private void drawSegment1(Canvas canvas) {
        float x = bevel;
        float y = bevel;

        Paint paint = createBasePaint();

        randomStringsOffset += 3;
        if (randomStringsOffset >= textSize) {
            randomStringsOffset = randomStringsOffset % textSize;
            randomStrings[randomStringsPointer] = randomString();
            randomStringsPointer = (randomStringsPointer + 1) % randomStringsLines;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setStrokeWidth(2);
        paint.setTextSize(textSize);
        for (int i = 0; i < randomStringsLines; i++)
            canvas.drawText(randomStrings[(randomStringsPointer + i) % randomStringsLines], x + 10, y + 25 - randomStringsOffset + textSize * i, paint);

        paint = new Paint();
        paint.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
        canvas.drawRect(x, y - textSize, x + segment1Width, y, paint);
        canvas.drawRect(x, y + segment1Height, x + segment1Width, y + segment1Height + textSize, paint);

        paint = createBasePaint();
        canvas.drawRect(x, y, x + segment1Width, y + segment1Height, paint);
    }

    private void drawSegment2(Canvas canvas) {
        Paint paint = createBasePaint();

        Path path = new Path();
        float x = bevel;
        float y = bevel + segment1Height + bevel;
        path.moveTo(x, y);
        path.lineTo(x, y + segment1Width);
        path.moveTo(x + segment1Width, y);
        path.lineTo(x + segment1Width, y + segment1Width);
        canvas.drawPath(path, paint);

        float cellWidth = (segment1Width - spacing * 3) / 2;
        float cellHeight = (segment1Width - spacing) / 2;
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

    private void drawSegment3(Canvas canvas) {
        Paint paint = createBasePaint();

        float x = bevel;
        float y = bevel + segment1Height + bevel + segment1Width + bevel;

        int baseSize = 190;
        int spacing = 20;

        paint = createBasePaint();
        canvas.drawRect(x, y, x + 400, 768 - bevel, paint);


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
        drawSegment3(canvas);

        //canvas.restore();
        invalidate();
    }
}