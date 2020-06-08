package cz.gattserver.stargate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class StargateView extends View {

    private ToneGenerator toneGenerator;

    private boolean symbolMenuVisible = false;
    private int choosenSlotId = -1;
    private String chars = "abcdefghijklmnABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private int bevel = 20;
    private int strokeWidth = 3;
    private int spacing = 15;

    private int segment1Height = 390;
    private int segment1Width = 180;
    private int segment3Width = 400;
    private int segment4Width = 200;
    private int segment5Width = 180;

    private char choosenGlyph[] = new char[7];

    private UIButton slotButtons[] = new UIButton[7];
    private UIButton glyphButtons[] = new UIButton[40];
    private UIButton menuButton = new UIButton();

    private int screenW, screenH;

    private long randomRefreshTime = 0;
    private long randomRefreshDelay = 100;

    private int angleShift = 0;

    private int textSpeed = 3;
    private int textSize = 16;
    private int randomStringsOffset = 0;
    private int randomStringsPointer = 0;
    private int randomStringsChars = 14;
    private int randomStringsLines = 25;
    private String randomStrings[] = new String[randomStringsLines];

    private int randomBits[] = new int[4];

    private int randomBlocks;

    int bars = 10;
    int cels = 5;
    private int randomCels[] = new int[bars];

    // Optimalizace
    private Paint strokeBluePaint;
    private Paint fillBluePaint;
    private Paint fillBlueTextPaint;
    private Paint fillWhiteTextPaint;
    private Paint fillBlackPaint;
    private Paint fillLightBluePaint;
    private Paint fillWhitePaint;
    private Paint strokeWhiteAliasedThinPaint;
    private Paint strokeRedAliasedPaint;
    private Paint fillWhiteGlyphPaint;
    private Paint fillWhiteRexliaPaint;

    public StargateView(Context context) {
        super(context);

        for (int l = 0; l < randomStringsLines; l++)
            randomStrings[l] = randomString();

        strokeBluePaint = preparePaint(Paint.Style.STROKE, Color.argb(0xff, 0x00, 0xa0, 0xff));

        fillBluePaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0x00, 0xa0, 0xff));

        fillLightBluePaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0x00, 0xf0, 0xff));

        Typeface rexliaFont = Typeface.createFromAsset(getContext().getAssets(), "rexlia.ttf");
        fillBlueTextPaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0x00, 0xa0, 0xff));
        fillBlueTextPaint.setStyle(Paint.Style.FILL);
//fillBlueTextPaint.setTypeface(Typeface.MONOSPACE);
        fillBlueTextPaint.setTypeface(rexliaFont);
        fillBlueTextPaint.setStrokeWidth(2);
        fillBlueTextPaint.setTextSize(textSize);

        fillWhiteTextPaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0xff, 0xff, 0xff));
        fillWhiteTextPaint.setStyle(Paint.Style.FILL);
        //fillWhiteTextPaint.setTypeface(Typeface.MONOSPACE);
        fillWhiteTextPaint.setTypeface(rexliaFont);
        fillWhiteTextPaint.setStrokeWidth(2);
        fillWhiteTextPaint.setTextSize(40);

        fillBlackPaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0x00, 0x00, 0x00));

        fillWhitePaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0xff, 0xff, 0xff));

        strokeWhiteAliasedThinPaint = preparePaint(Paint.Style.STROKE, Color.argb(0xff, 0xff, 0xff, 0xff));
        strokeWhiteAliasedThinPaint.setAntiAlias(true);
        strokeWhiteAliasedThinPaint.setStrokeWidth(1);

        strokeRedAliasedPaint = preparePaint(Paint.Style.STROKE, Color.argb(0xff, 0xff, 0x30, 0x30));
        strokeWhiteAliasedThinPaint.setAntiAlias(true);
        strokeRedAliasedPaint.setStrokeWidth(3);

        // https://webfonts0.com/font-stargatesg-1addressglyphs-free/126f117375.htm
        Typeface glyphFont = Typeface.createFromAsset(getContext().getAssets(), "stargatesg1addressglyphs.ttf");
        fillWhiteGlyphPaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0xff, 0xff, 0xff));
        fillWhiteGlyphPaint.setStyle(Paint.Style.FILL);
        fillWhiteGlyphPaint.setTypeface(glyphFont);
        fillWhiteGlyphPaint.setStrokeWidth(2);
        fillWhiteGlyphPaint.setTextSize(80);

        fillWhiteRexliaPaint = preparePaint(Paint.Style.FILL, Color.argb(0xff, 0xff, 0xff, 0xff));
        fillWhiteRexliaPaint.setStyle(Paint.Style.FILL);
        fillWhiteRexliaPaint.setTypeface(rexliaFont);
        fillWhiteRexliaPaint.setStrokeWidth(2);
        fillWhiteRexliaPaint.setTextSize(50);

        for (int i = 0; i < 7; i++)
            slotButtons[i] = new UIButton().setId(i);
        for (int i = 0; i < 40; i++)
            glyphButtons[i] = new UIButton().setId(i);
        menuButton = new UIButton();
    }

    private Paint preparePaint(Paint.Style style, int color) {
        Paint paint = new Paint();
        paint.setStyle(style);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(false);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeJoin(Paint.Join.MITER);
        return paint;
    }

    private String randomString() {
        String s = "";
        int len = (int) (Math.random() * randomStringsChars);
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

    private void generateRandom() {
        for (int i = 0; i < 4; i++)
            randomBits[i] = (int) (Math.random() * (1 << 3 * 3));

        for (int i = 0; i < bars; i++)
            randomCels[i] = (int) (Math.random() * cels);

        randomBlocks = (int) (Math.random() * (1 << 15));
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(0, 0, screenW, screenH, fillBlackPaint);
    }

    private void drawSegment1(Canvas canvas) {
        float x = bevel;
        float y = bevel;

        randomStringsOffset += textSpeed;
        if (randomStringsOffset >= textSize) {
            randomStringsOffset = randomStringsOffset % textSize;
            randomStrings[randomStringsPointer] = randomString();
            randomStringsPointer = (randomStringsPointer + 1) % randomStringsLines;
        }

        for (int i = 0; i < randomStringsLines; i++)
            canvas.drawText(randomStrings[(randomStringsPointer + i) % randomStringsLines], x + 10, y + 25 - randomStringsOffset + textSize * i, fillBlueTextPaint);

        canvas.drawRect(x, y - textSize, x + segment1Width, y, fillBlackPaint);
        canvas.drawRect(x, y + segment1Height, x + segment1Width, y + segment1Height + textSize * 2, fillBlackPaint);

        canvas.drawRect(x, y, x + segment1Width, y + segment1Height, strokeBluePaint);
    }

    private void drawSegment2(Canvas canvas) {
        Path path = new Path();
        float x = bevel;
        float y = bevel + segment1Height + bevel;
        path.moveTo(x, y);
        path.lineTo(x, y + segment1Width);
        path.moveTo(x + segment1Width, y);
        path.lineTo(x + segment1Width, y + segment1Width);

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

                for (int ci = 0; ci < 3; ci++)
                    for (int cj = 0; cj < 3; cj++)
                        if ((randomBits[i * 2 + j] & 1 << (ci * 3 + cj)) > 0)
                            canvas.drawCircle((float) (strokeWidth / 2 + cx + cellWidth3 - radius + ci * spacingRadius * 2), (float) (strokeWidth / 2 + cy + cellHeight4 * 2 - radius + cj * spacingRadius * 2), radius, fillWhitePaint);
            }
        }

        canvas.drawPath(path, strokeBluePaint);
    }

    private void drawSegment3(Canvas canvas) {
        float x = bevel;
        float y = bevel + segment1Height + bevel + segment1Width + bevel;
        float toY = screenH - bevel;

        canvas.drawRect(x, y, x + segment3Width, toY, strokeBluePaint);

        int barSpacing = 5;
        float barWidth = (segment3Width + barSpacing - spacing * 2) * 1f / bars - barSpacing;
        int cellSpacing = 5;
        float cellHeight = (toY - y - spacing * 2) * 1f / cels - cellSpacing;
        for (int i = 0; i < bars; i++) {
            float fromX = x + spacing + (barWidth + barSpacing) * i;
            for (int c = 0; c < randomCels[i]; c++) {
                float fromY = toY - spacing - (cellHeight + cellSpacing) * c;
                boolean last = c == randomCels[i] - 1;
                canvas.drawRect(fromX, fromY - cellHeight, fromX + barWidth, fromY, last ? fillLightBluePaint : fillBluePaint);
            }
        }
    }

    private void drawSegment4(Canvas canvas) {
        float x = bevel + segment3Width + bevel;
        float y = bevel + segment1Height + bevel + segment1Width + bevel;
        float toY = screenH - bevel;

        canvas.drawRect(x, y, x + segment4Width, toY, strokeBluePaint);

        float barWidth = segment4Width * 1f / 5;
        float barHeight = (toY - y) * 1f / 3;
        for (int i = 0; i < 5; i++) {
            float fromX = x + barWidth * i;
            for (int j = 0; j < 3; j++) {
                float fromY = toY - barHeight * j;
                canvas.drawRect(fromX, fromY - barHeight, fromX + barWidth, fromY, strokeBluePaint);
                if ((randomBlocks & 1 << (i * 3 + j)) > 0)
                    canvas.drawRect(fromX + 2, fromY - barHeight + 2, fromX + barWidth - 1, fromY - 1, fillWhitePaint);
            }
        }
    }

    private void drawSegment5(Canvas canvas) {
        float x = screenW - bevel - segment5Width;
        float y = bevel;

        float slotHeight = (screenH + spacing - 2 * bevel) * 1f / 7 - spacing;
        float slotWidth = 60;
        for (int i = 0; i < 7; i++) {
            float sx = x + 30;
            float sy = y + i * (slotHeight + spacing);
            canvas.drawText("" + (i + 1), x - 10, sy + slotWidth, fillWhiteTextPaint);
            canvas.drawRect(sx, sy, x + segment5Width, sy + slotHeight, strokeBluePaint);
            slotButtons[i].setX(sx).setY(sy).setW(slotWidth).setH(slotHeight);

            if (choosenGlyph[i] != 0)
                canvas.drawText("" + choosenGlyph[i], sx + 20, sy + 70, fillWhiteGlyphPaint);
        }
    }

    private void prepareNotchDraw(float start, float cx, float cy, Path path, float[] offset, float[] radius, int nodes) {
        int coef = 1;
        for (int s = 0; s < 3; s++) {
            coef *= -1;
            for (int n = 0; n < offset.length; n++) {
                int index = coef < 0 ? n : (offset.length - 1) - n;
                float nx = cx + (float) Math.cos(start + coef * offset[index]) * radius[index];
                float ny = cy + (float) Math.sin(start + coef * offset[index]) * radius[index];
                if (s == 0 && n == 0) {
                    path.moveTo(nx, ny);
                } else {
                    path.lineTo(nx, ny);
                }
                // dokončovací uzavření smyčky
                if (s == 2)
                    break;
            }
        }
    }

    private void drawGate(Canvas canvas) {
        float w = (screenW - bevel * 4 - segment1Width - segment5Width);
        float h = (segment1Height + bevel + segment1Width);

        float x = bevel + segment1Width + bevel;
        float y = bevel;

        canvas.drawRect(x, y, x + w, y + h, strokeBluePaint);

        Path path = new Path();
        path.moveTo(x, y + 20);
        path.lineTo(x + 200, y + 20);
        path.moveTo(x + w, y + 20);
        path.lineTo(x + w - 200, y + 20);

        path.moveTo(x, y + 60);
        path.lineTo(x + 85, y + 60);
        path.lineTo(x + 100, y + 75);
        path.moveTo(x + w, y + 60);
        path.lineTo(x + w - 85, y + 60);
        path.lineTo(x + w - 100, y + 75);

        path.moveTo(x, y + 245);
        path.lineTo(x + 5, y + 245);
        path.moveTo(x + w, y + 245);
        path.lineTo(x + w - 5, y + 245);

        path.moveTo(x, y + h - 130);
        path.lineTo(x + 20, y + h - 130);
        path.lineTo(x + 40, y + h - 150);
        path.moveTo(x + w, y + h - 130);
        path.lineTo(x + w - 20, y + h - 130);
        path.lineTo(x + w - 40, y + h - 150);

        path.moveTo(x, y + h - 20);
        path.lineTo(x + 180, y + h - 20);
        path.moveTo(x + w, y + h - 20);
        path.lineTo(x + w - 180, y + h - 20);
        canvas.drawPath(path, strokeBluePaint);

        float r = Math.min(w, h) / 2 - 2;
        float cx = bevel * 2 + segment1Width + w / 2;
        float cy = bevel + h / 2;
        float r1 = r - 20;
        canvas.drawCircle(cx, cy, r1, strokeWhiteAliasedThinPaint);
        float r2 = r - 40;
        canvas.drawCircle(cx, cy, r2, strokeWhiteAliasedThinPaint);
        float r3 = r - 50;
        canvas.drawCircle(cx, cy, r3, strokeWhiteAliasedThinPaint);
        float r4 = r - 80;
        canvas.drawCircle(cx, cy, r4, strokeWhiteAliasedThinPaint);

        angleShift = (angleShift + 1) % 360;

        path = new Path();

        int segments = 39;
        float increment = 360f / segments;
        for (int i = 0; i < segments; i++) {
            float rad = (float) ((increment * i + angleShift) * Math.PI / 180);
            path.moveTo(cx + (float) Math.cos(rad) * r3, cy + (float) Math.sin(rad) * r3);
            path.lineTo(cx + (float) Math.cos(rad) * r4, cy + (float) Math.sin(rad) * r4);
        }

        canvas.drawPath(path, strokeWhiteAliasedThinPaint);

        path = new Path();

        float offset[] = new float[]{0.03f, 0.07f, 0.15f, 0.2f, 0.25f, 0.26f, 0.11f, 0.05f};
        float radius[] = new float[]{r1 - 24, r1 + 11, r1 + 11, r1 + 9, r1 + 5, r1, r1 - 2, r1 - 40};
        for (int i = 0; i < 9; i++) {
            float start = (float) (Math.PI * 1.5 + Math.PI * 2 / 9 * i);
            prepareNotchDraw(start, cx, cy, path, offset, radius, offset.length);
        }
        canvas.drawPath(path, fillBlackPaint);
        canvas.drawPath(path, strokeWhiteAliasedThinPaint);

        Path freeNotchPath = new Path();
        Path selectedNotchPath = new Path();
        offset = new float[]{0.03f, 0.07f, 0.03f};
        radius = new float[]{r1 - 24, r1 + 10, r1 + 11};
        for (int i = 0; i < 9; i++) {
            float start = (float) (Math.PI * 1.5 + Math.PI * 2 / 9 * i);
            Path p = freeNotchPath;
            // chevron 4 a 5 se nepoužívá
            if (i < 4 && choosenGlyph[i] != 0 || i > 5 && choosenGlyph[i - 2] != 0)
                p = selectedNotchPath;
            prepareNotchDraw(start, cx, cy, p, offset, radius, offset.length);
        }
        canvas.drawPath(freeNotchPath, fillBlackPaint);
        canvas.drawPath(selectedNotchPath, fillBlackPaint);
        canvas.drawPath(freeNotchPath, strokeWhiteAliasedThinPaint);
        canvas.drawPath(selectedNotchPath, strokeRedAliasedPaint);

        freeNotchPath = new Path();
        selectedNotchPath = new Path();
        offset = new float[]{0.005f, 0.04f, 0.005f};
        radius = new float[]{r1 - 16, r1 + 9, r1 + 10};
        for (int i = 0; i < 9; i++) {
            float start = (float) (Math.PI * 1.5 + Math.PI * 2 / 9 * i);
            Path p = freeNotchPath;
            // chevron 4 a 5 se nepoužívá
            if (i < 4 && choosenGlyph[i] != 0 || i > 5 && choosenGlyph[i - 2] != 0)
                p = selectedNotchPath;
            prepareNotchDraw(start, cx, cy, p, offset, radius, offset.length);
        }
        canvas.drawPath(freeNotchPath, fillBlackPaint);
        canvas.drawPath(selectedNotchPath, fillBlackPaint);
        canvas.drawPath(freeNotchPath, strokeWhiteAliasedThinPaint);
        canvas.drawPath(selectedNotchPath, strokeRedAliasedPaint);
    }

    private void drawSymbolMenu(Canvas canvas) {
        canvas.drawRect(bevel, bevel, screenW - bevel, screenH - bevel, strokeBluePaint);

        int cols = 7;
        int rows = 6;

        float w = (screenW - spacing - 2f * bevel) / cols - spacing;
        float h = (screenH - spacing - 2f * bevel) / rows - spacing;

        for (char i = 0; i < rows; i++) {
            for (char j = 0; j < cols; j++) {
                int index = i * cols + j;
                float gx = bevel + spacing + j * (spacing + w);
                float gy = bevel + spacing + i * (spacing + h);
                if (index < chars.length()) {
                    canvas.drawText("" + chars.charAt(index), gx + 10, gy + 80, fillWhiteGlyphPaint);
                    canvas.drawRect(gx, gy, gx + w, gy + h, strokeBluePaint);
                    glyphButtons[index].setX(gx).setY(gy).setW(w).setH(h).setId(index);
                }
                if (index == chars.length()) {
                    canvas.drawText("Menu", gx + 40, gy + 70, fillWhiteRexliaPaint);
                    canvas.drawRect(gx, gy, gx + w * 2, gy + h, strokeBluePaint);
                    menuButton.setX(gx).setY(gy).setW(w).setH(h);
                }
            }
        }
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

        if (symbolMenuVisible) {
            drawSymbolMenu(canvas);
        } else {
            drawSegment1(canvas);
            drawSegment2(canvas);
            drawSegment3(canvas);
            drawSegment4(canvas);
            drawSegment5(canvas);
            drawGate(canvas);
        }

        //canvas.restore();
        invalidate();
    }

    private void beep1() {
        // ok funguje
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void beep2() {
        new cz.gattserver.stargate.ToneGenerator().play();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (!symbolMenuVisible) {
            for (int i = 0; i < slotButtons.length; i++) {
                UIButton slotButton = slotButtons[i];
                if (slotButton.isHit(x, y)) {
                    choosenSlotId = slotButton.getId();
                    symbolMenuVisible = true;
                    break;
                }
            }
        } else {
            for (int i = 0; i < glyphButtons.length; i++) {
                UIButton glyphButton = glyphButtons[i];
                if (glyphButton.isHit(x, y)) {
                    choosenGlyph[choosenSlotId] = chars.charAt(i);
                    choosenSlotId = -1;
                    symbolMenuVisible = false;
                    beep2();
                    break;
                }
            }
            if (menuButton.isHit(x, y))
                symbolMenuVisible = false;
        }

        return super.onTouchEvent(event);
    }
}