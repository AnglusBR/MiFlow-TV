package com.miflow.tv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;

final class ActionPillView extends TextView {
    ActionPillView(Context context, String text) {
        super(context);
        setText(text);
        setTextColor(Color.argb(220, 255, 255, 255));
        setTextSize(15);
        setGravity(Gravity.CENTER);
        setPadding(dp(22), dp(11), dp(22), dp(11));
        setFocusable(true);
        setClickable(true);
        setBackground(background(false));
        setOnFocusChangeListener((v, focused) -> {
            animate().scaleX(focused ? 1.06f : 1f).scaleY(focused ? 1.06f : 1f).setDuration(120).start();
            setTextColor(Color.WHITE);
            setBackground(background(focused));
        });
    }

    private GradientDrawable background(boolean focused) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(focused ? Color.rgb(255, 105, 0) : Color.argb(30, 255, 255, 255));
        shape.setCornerRadius(dp(24));
        shape.setStroke(dp(1), focused ? Color.rgb(255, 153, 72) : Color.argb(50, 255, 255, 255));
        return shape;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
