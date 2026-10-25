package com.miflow.tv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class AppCatalog {
    private AppCatalog() {}

    static List<AppEntry> load(Context context) {
        PackageManager pm = context.getPackageManager();
        Map<String, ResolveInfo> unique = new LinkedHashMap<>();
        collect(pm, Intent.CATEGORY_LEANBACK_LAUNCHER, unique);
        collect(pm, Intent.CATEGORY_LAUNCHER, unique);

        List<AppEntry> apps = new ArrayList<>();
        for (ResolveInfo info : unique.values()) {
            if (info.activityInfo == null || context.getPackageName().equals(info.activityInfo.packageName)) continue;
            String label = String.valueOf(info.loadLabel(pm));
            String packageName = info.activityInfo.packageName;
            apps.add(new AppEntry(
                    label,
                    packageName,
                    new ComponentName(packageName, info.activityInfo.name),
                    info.loadIcon(pm),
                    CategoryClassifier.classify(label, packageName)
            ));
        }

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);
        Collections.sort(apps, (a, b) -> collator.compare(a.label, b.label));
        return apps;
    }

    private static void collect(PackageManager pm, String category, Map<String, ResolveInfo> target) {
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo info : pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)) {
            if (info.activityInfo != null) {
                String key = info.activityInfo.packageName + "/" + info.activityInfo.name;
                if (!target.containsKey(key)) target.put(key, info);
            }
        }
    }
}
