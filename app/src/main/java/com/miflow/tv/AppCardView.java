package com.miflow.tv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class AppCardView extends LinearLayout {
    private final TextView label;
    private final int normalColor = Color.argb(88, 255, 255, 255);
    private final int focusColor = Color.argb(235, 255, 255, 255);
    private OnFocusChangeListener externalFocusListener;

    AppCardView(Context context, AppEntry app, int widthDp, int heightDp) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setPadding(dp(12), dp(14), dp(12), dp(10));
        setFocusable(true);
        setClickable(true);
        setClipToOutline(false);
        setLayoutParams(new ViewGroup.LayoutParams(dp(widthDp), dp(heightDp)));
        setBackground(cardBackground(false));

        ImageView icon = new ImageView(context);
        icon.setImageDrawable(app.icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(icon, new LayoutParams(dp(78), dp(78)));

        label = new TextView(context);
        label.setText(app.label);
        label.setTextColor(normalColor);
        label.setTextSize(14);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(1);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LayoutParams textParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textParams.topMargin = dp(10);
        addView(label, textParams);

        setOnFocusChangeListener((view, focused) -> {
            animate().scaleX(focused ? 1.09f : 1f).scaleY(focused ? 1.09f : 1f)
                    .translationY(focused ? -dp(5) : 0).setDuration(135).start();
            setElevation(focused ? dp(18) : dp(2));
            setBackground(cardBackground(focused));
            label.setTextColor(focused ? Color.WHITE : normalColor);
            if (externalFocusListener != null) externalFocusListener.onFocusChange(view, focused);
        });
    }

    void setExternalFocusListener(OnFocusChangeListener listener) {
        externalFocusListener = listener;
    }

    private GradientDrawable cardBackground(boolean focused) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(focused ? Color.argb(75, 255, 255, 255) : Color.argb(28, 255, 255, 255));
        shape.setCornerRadius(dp(22));
        shape.setStroke(dp(focused ? 3 : 1), focused ? Color.rgb(255, 105, 0) : Color.argb(42, 255, 255, 255));
        return shape;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
