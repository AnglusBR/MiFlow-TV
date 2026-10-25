package com.miflow.tv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

final class BackdropView extends View {
    private static final int[][] PALETTES = {
            {Color.rgb(8, 9, 13), Color.rgb(31, 16, 9), Color.rgb(255, 105, 0)},
            {Color.rgb(5, 10, 18), Color.rgb(9, 32, 48), Color.rgb(0, 163, 255)},
            {Color.rgb(11, 7, 18), Color.rgb(40, 13, 48), Color.rgb(186, 74, 255)}
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int palette;
    private LinearGradient baseGradient;
    private RadialGradient heroGlow;
    private RadialGradient cornerGlow;

    BackdropView(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setPalette(int palette) {
        this.palette = Math.floorMod(palette, PALETTES.length);
        rebuildShaders(getWidth(), getHeight());
        invalidate();
    }

    int nextPalette() {
        setPalette(palette + 1);
        return palette;
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        rebuildShaders(w, h);
    }

    private void rebuildShaders(int w, int h) {
        if (w <= 0 || h <= 0) return;
        int[] colors = PALETTES[palette];
        baseGradient = new LinearGradient(0, 0, w, h, colors[1], colors[0], Shader.TileMode.CLAMP);
        heroGlow = new RadialGradient(w * .79f, h * .18f, w * .48f,
                withAlpha(colors[2], 115), Color.TRANSPARENT, Shader.TileMode.CLAMP);
        cornerGlow = new RadialGradient(w * .12f, h * .92f, w * .38f,
                withAlpha(colors[2], 42), Color.TRANSPARENT, Shader.TileMode.CLAMP);
    }

    @Override protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        paint.setShader(baseGradient);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setShader(heroGlow);
        canvas.drawCircle(w * .79f, h * .18f, w * .48f, paint);

        paint.setShader(cornerGlow);
        canvas.drawCircle(w * .12f, h * .92f, w * .38f, paint);
        paint.setShader(null);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
