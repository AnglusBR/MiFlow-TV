package com.miflow.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Iterator;

public final class MainActivity extends Activity {
    private static final String PREFS = "miflow_preferences";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_PALETTE = "palette";

    private final Handler clockHandler = new Handler();
    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            updateClock();
            clockHandler.postDelayed(this, 15_000);
        }
    };

    private SharedPreferences preferences;
    private final Set<String> favorites = new LinkedHashSet<>();
    private List<AppEntry> apps = new ArrayList<>();
    private BackdropView backdrop;
    private FrameLayout root;
    private LinearLayout favoriteRow;
    private TextView clockView;
    private TextView dateView;
    private ImageView heroIcon;
    private TextView heroTitle;
    private TextView heroMeta;
    private FrameLayout drawer;
    private GridLayout drawerGrid;
    private TextView drawerTitle;
    private AppCategory activeCategory = AppCategory.ALL;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        readFavorites();
        buildScreen();
    }

    @Override protected void onResume() {
        super.onResume();
        hideSystemUi();
        apps = AppCatalog.load(this);
        seedFavoritesIfNeeded();
        renderFavorites();
        if (drawer != null && drawer.getVisibility() == View.VISIBLE) renderDrawer(activeCategory);
        clockHandler.removeCallbacks(clockTick);
        clockHandler.post(clockTick);
    }

    @Override protected void onPause() {
        clockHandler.removeCallbacks(clockTick);
        super.onPause();
    }

    @Override public void onBackPressed() {
        if (drawer != null && drawer.getVisibility() == View.VISIBLE) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            openDrawer(AppCategory.ALL);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void buildScreen() {
        root = new FrameLayout(this);
        backdrop = new BackdropView(this);
        backdrop.setPalette(preferences.getInt(KEY_PALETTE, 0));
        root.addView(backdrop, match());

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(58), dp(28), dp(58), dp(32));
        root.addView(page, match());

        page.addView(buildTopBar(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(74)));
        page.addView(buildHero(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView heading = heading(getString(R.string.favorites));
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headingParams.bottomMargin = dp(10);
        page.addView(heading, headingParams);

        HorizontalScrollView favoriteScroll = new HorizontalScrollView(this);
        favoriteScroll.setHorizontalScrollBarEnabled(false);
        favoriteScroll.setClipToPadding(false);
        favoriteScroll.setPadding(dp(8), dp(8), dp(8), dp(8));
        favoriteRow = new LinearLayout(this);
        favoriteRow.setOrientation(LinearLayout.HORIZONTAL);
        favoriteScroll.addView(favoriteRow);
        page.addView(favoriteScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(182)));

        page.addView(buildQuickActions(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));

        drawer = buildDrawer();
        drawer.setVisibility(View.GONE);
        root.addView(drawer, match());
        setContentView(root);
    }

    private View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        TextView mark = new TextView(this);
        mark.setText("mi");
        mark.setTextColor(Color.WHITE);
        mark.setTextSize(28);
        mark.setGravity(Gravity.CENTER);
        mark.setTypeface(null, android.graphics.Typeface.BOLD);
        mark.setBackground(roundRect(Color.rgb(255, 105, 0), 17));
        bar.addView(mark, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(16), 0, 0, 0);
        TextView title = text("MiFlow TV", 23, Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        brand.addView(title);
        brand.addView(text(getString(R.string.tagline), 13, Color.argb(150, 255, 255, 255)));
        bar.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout dateBlock = new LinearLayout(this);
        dateBlock.setOrientation(LinearLayout.VERTICAL);
        dateBlock.setGravity(Gravity.END);
        clockView = text("--:--", 28, Color.WHITE);
        clockView.setGravity(Gravity.END);
        clockView.setTypeface(null, android.graphics.Typeface.BOLD);
        dateView = text("", 13, Color.argb(170, 255, 255, 255));
        dateView.setGravity(Gravity.END);
        dateBlock.addView(clockView);
        dateBlock.addView(dateView);
        bar.addView(dateBlock, new LinearLayout.LayoutParams(dp(260), ViewGroup.LayoutParams.WRAP_CONTENT));
        return bar;
    }

    private View buildHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(2), dp(14), dp(2), dp(10));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);
        TextView eyebrow = text("PRONTO PARA ASSISTIR", 12, Color.rgb(255, 151, 76));
        eyebrow.setLetterSpacing(.16f);
        heroTitle = text("Escolha um favorito", 40, Color.WHITE);
        heroTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        heroMeta = text("Use o controle para navegar. Segure OK para organizar.", 16, Color.argb(175, 255, 255, 255));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(6);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaParams.topMargin = dp(8);
        copy.addView(eyebrow);
        copy.addView(heroTitle, titleParams);
        copy.addView(heroMeta, metaParams);
        hero.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        FrameLayout iconGlass = new FrameLayout(this);
        iconGlass.setBackground(roundRect(Color.argb(35, 255, 255, 255), 34));
        heroIcon = new ImageView(this);
        heroIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(105), dp(105), Gravity.CENTER);
        iconGlass.addView(heroIcon, iconParams);
        hero.addView(iconGlass, new LinearLayout.LayoutParams(dp(190), dp(145)));
        return hero;
    }

    private View buildQuickActions() {
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(dp(2), dp(8), 0, 0);

        ActionPillView all = action("▦  Todos os apps", v -> openDrawer(AppCategory.ALL));
        ActionPillView wifi = action("Wi‑Fi", v -> startSafe(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        ActionPillView bluetooth = action("Bluetooth", v -> startSafe(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));
        ActionPillView theme = action("Tema", v -> {
            int palette = backdrop.nextPalette();
            preferences.edit().putInt(KEY_PALETTE, palette).apply();
        });
        ActionPillView settings = action("Configurações", v -> startSafe(new Intent(Settings.ACTION_SETTINGS)));

        addAction(actions, all);
        addAction(actions, wifi);
        addAction(actions, bluetooth);
        addAction(actions, theme);
        addAction(actions, settings);
        TextView notice = text(getString(R.string.unofficial_notice), 11, Color.argb(100, 255, 255, 255));
        notice.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actions.addView(notice, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        return actions;
    }

    private FrameLayout buildDrawer() {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.argb(244, 6, 7, 10));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(55), dp(35), dp(55), dp(35));
        overlay.addView(content, match());

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        drawerTitle = text("Todos os apps", 34, Color.WHITE);
        drawerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(drawerTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ActionPillView close = action("Voltar", v -> closeDrawer());
        header.addView(close);
        content.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));

        LinearLayout categories = new LinearLayout(this);
        categories.setGravity(Gravity.CENTER_VERTICAL);
        for (AppCategory category : AppCategory.values()) {
            ActionPillView chip = action(category.title, v -> renderDrawer(category));
            addAction(categories, chip);
        }
        content.addView(categories, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        drawerGrid = new GridLayout(this);
        drawerGrid.setColumnCount(6);
        drawerGrid.setUseDefaultMargins(false);
        drawerGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        scroll.addView(drawerGrid, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return overlay;
    }

    private void renderFavorites() {
        favoriteRow.removeAllViews();
        int shown = 0;
        for (String packageName : favorites) {
            AppEntry app = findApp(packageName);
            if (app != null) {
                addFavoriteCard(app);
                shown++;
                if (shown == 8) break;
            }
        }
        if (shown == 0) {
            TextView empty = text("Abra Todos os apps e segure OK para adicionar favoritos.", 16, Color.argb(150, 255, 255, 255));
            empty.setGravity(Gravity.CENTER_VERTICAL);
            favoriteRow.addView(empty, new LinearLayout.LayoutParams(dp(650), dp(155)));
        }
    }

    private void addFavoriteCard(AppEntry app) {
        AppCardView card = makeAppCard(app, 154, 154);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(154), dp(154));
        params.rightMargin = dp(16);
        favoriteRow.addView(card, params);
    }

    private AppCardView makeAppCard(AppEntry app, int width, int height) {
        AppCardView card = new AppCardView(this, app, width, height);
        card.setOnClickListener(v -> launch(app));
        card.setOnLongClickListener(v -> {
            showAppMenu(app);
            return true;
        });
        card.setExternalFocusListener((view, focused) -> {
            if (focused) showHero(app);
        });
        return card;
    }

    private void renderDrawer(AppCategory category) {
        activeCategory = category;
        drawerTitle.setText(category == AppCategory.ALL ? getString(R.string.all_apps) : category.title);
        drawerGrid.removeAllViews();
        int count = 0;
        for (AppEntry app : apps) {
            boolean matches = category == AppCategory.ALL
                    || (category == AppCategory.FAVORITES && favorites.contains(app.packageName))
                    || app.category == category;
            if (!matches) continue;
            AppCardView card = makeAppCard(app, 160, 150);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(160);
            params.height = dp(150);
            params.setMargins(dp(8), dp(8), dp(8), dp(8));
            drawerGrid.addView(card, params);
            count++;
        }
        if (count == 0) {
            TextView empty = text(getString(R.string.empty_apps), 18, Color.argb(160, 255, 255, 255));
            empty.setPadding(dp(8), dp(35), 0, 0);
            drawerGrid.addView(empty);
        }
    }

    private void openDrawer(AppCategory category) {
        renderDrawer(category);
        drawer.setAlpha(0f);
        drawer.setVisibility(View.VISIBLE);
        drawer.animate().alpha(1f).setDuration(160).start();
        drawer.post(() -> {
            if (drawerGrid.getChildCount() > 0) drawerGrid.getChildAt(0).requestFocus();
        });
    }

    private void closeDrawer() {
        drawer.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            drawer.setVisibility(View.GONE);
            renderFavorites();
        }).start();
    }

    private void showHero(AppEntry app) {
        heroIcon.setImageDrawable(app.icon);
        heroTitle.setText(app.label);
        heroMeta.setText(app.category.title + "  •  OK para abrir  •  Segure OK para organizar");
    }

    private void showAppMenu(AppEntry app) {
        boolean favorite = favorites.contains(app.packageName);
        String favoriteAction = favorite ? "Remover dos favoritos" : "Adicionar aos favoritos";
        String[] choices = {favoriteAction, "Informações do app", "Desinstalar"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(app.label)
                .setItems(choices, (d, which) -> {
                    if (which == 0) toggleFavorite(app);
                    if (which == 1) openAppDetails(app);
                    if (which == 2) uninstall(app);
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.rgb(28, 29, 34)));
                window.getDecorView().setSystemUiVisibility(immersiveFlags());
            }
        });
        dialog.show();
    }

    private void toggleFavorite(AppEntry app) {
        if (!favorites.remove(app.packageName)) favorites.add(app.packageName);
        saveFavorites();
        renderFavorites();
        if (drawer.getVisibility() == View.VISIBLE) renderDrawer(activeCategory);
        Toast.makeText(this, favorites.contains(app.packageName) ? "Adicionado aos favoritos" : "Removido dos favoritos", Toast.LENGTH_SHORT).show();
    }

    private void launch(AppEntry app) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(app.component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startSafe(intent);
    }

    private void openAppDetails(AppEntry app) {
        startSafe(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + app.packageName)));
    }

    private void uninstall(AppEntry app) {
        startSafe(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + app.packageName)));
    }

    private void startSafe(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception error) {
            Toast.makeText(this, "Esta opção não está disponível neste aparelho.", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFavorites() {
        favorites.clear();
        String saved = preferences.getString(KEY_FAVORITES, "");
        if (!saved.isEmpty()) {
            for (String packageName : saved.split("\\n")) {
                if (!packageName.isEmpty()) favorites.add(packageName);
            }
        }
    }

    private void seedFavoritesIfNeeded() {
        Iterator<String> iterator = favorites.iterator();
        while (iterator.hasNext()) {
            if (findApp(iterator.next()) == null) iterator.remove();
        }
        if (favorites.isEmpty()) {
            String[] preferred = {"com.netflix.ninja", "com.google.android.youtube.tv", "com.amazon.amazonvideo.livingroom", "com.disney.disneyplus"};
            for (String packageName : preferred) if (findApp(packageName) != null) favorites.add(packageName);
            for (AppEntry app : apps) {
                if (favorites.size() >= 6) break;
                favorites.add(app.packageName);
            }
            saveFavorites();
        }
    }

    private void saveFavorites() {
        StringBuilder saved = new StringBuilder();
        for (String packageName : favorites) {
            if (saved.length() > 0) saved.append('\n');
            saved.append(packageName);
        }
        preferences.edit().putString(KEY_FAVORITES, saved.toString()).apply();
    }

    private AppEntry findApp(String packageName) {
        for (AppEntry app : apps) if (app.packageName.equals(packageName)) return app;
        return null;
    }

    private void updateClock() {
        Date now = new Date();
        clockView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        DateFormat formatter = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR"));
        String date = formatter.format(now);
        dateView.setText(Character.toUpperCase(date.charAt(0)) + date.substring(1));
    }

    private ActionPillView action(String title, View.OnClickListener listener) {
        ActionPillView view = new ActionPillView(this, title);
        view.setOnClickListener(listener);
        return view;
    }

    private void addAction(LinearLayout parent, View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(46));
        params.rightMargin = dp(10);
        parent.addView(view, params);
    }

    private TextView heading(String value) {
        TextView view = text(value, 18, Color.WHITE);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private TextView text(String value, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        return view;
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radiusDp));
        return shape;
    }

    private FrameLayout.LayoutParams match() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(immersiveFlags());
    }

    private int immersiveFlags() {
        return View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    }
}
