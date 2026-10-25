package com.miflow.tv;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

final class AppEntry {
    final String label;
    final String packageName;
    final ComponentName component;
    final Drawable icon;
    final AppCategory category;

    AppEntry(String label, String packageName, ComponentName component, Drawable icon, AppCategory category) {
        this.label = label;
        this.packageName = packageName;
        this.component = component;
        this.icon = icon;
        this.category = category;
    }
}
