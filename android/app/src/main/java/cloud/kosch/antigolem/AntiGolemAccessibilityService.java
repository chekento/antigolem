package cloud.kosch.antigolem;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AntiGolemAccessibilityService extends AccessibilityService {
    private WindowManager windowManager;
    private LinearLayout overlayRoot;
    private LinearLayout menuPanel;
    private ImageButton bubbleButton;
    private WindowManager.LayoutParams overlayParams;
    private View selectionOverlay;
    private float downRawX;
    private float downRawY;
    private int downX;
    private int downY;
    private boolean moved;
    private boolean menuOpen;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        showToolkit();
        Toast.makeText(this,
                "AntiGolem Toolkit active. Tap the small floating icon to open it.",
                Toast.LENGTH_LONG).show();
    }

    private void showToolkit() {
        if (overlayRoot != null) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayRoot = new LinearLayout(this);
        overlayRoot.setOrientation(LinearLayout.HORIZONTAL);
        overlayRoot.setGravity(Gravity.CENTER_VERTICAL);
        overlayRoot.setPadding(dp(1), dp(1), dp(1), dp(1));

        menuPanel = buildMenuPanel();
        menuPanel.setVisibility(View.GONE);
        menuPanel.setAlpha(0f);
        menuPanel.setScaleX(0.94f);
        menuPanel.setScaleY(0.94f);
        menuPanel.setTranslationX(dp(16));
        overlayRoot.addView(menuPanel,
                new LinearLayout.LayoutParams(dp(232), LinearLayout.LayoutParams.WRAP_CONTENT));

        View spacer = new View(this);
        overlayRoot.addView(spacer, new LinearLayout.LayoutParams(dp(6), dp(1)));

        bubbleButton = new ImageButton(this);
        bubbleButton.setImageResource(R.drawable.ic_antigolem);
        bubbleButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        bubbleButton.setPadding(dp(4), dp(4), dp(4), dp(4));
        bubbleButton.setContentDescription("Open AntiGolem Toolkit");
        bubbleButton.setElevation(dp(12));
        bubbleButton.setAlpha(0.92f);
        bubbleButton.setBackground(bubbleBackground());
        overlayRoot.addView(bubbleButton, new LinearLayout.LayoutParams(dp(46), dp(46)));

        overlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        overlayParams.gravity = Gravity.TOP | Gravity.END;
        overlayParams.x = dp(8);
        overlayParams.y = dp(190);

        bubbleButton.setOnTouchListener(this::handleBubbleTouch);
        windowManager.addView(overlayRoot, overlayParams);
    }

    private LinearLayout buildMenuPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(9), dp(10), dp(9));
        panel.setElevation(dp(16));

        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        card.setCornerRadius(dp(20));
        card.setColor(Color.rgb(8, 17, 31));
        card.setStroke(dp(1), Color.rgb(54, 91, 134));
        panel.setBackground(card);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(5), 0, 0, dp(5));

        LinearLayout titleColumn = new LinearLayout(this);
        titleColumn.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText("AntiGolem");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15.5f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        titleColumn.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Floating language toolkit");
        subtitle.setTextColor(Color.rgb(132, 158, 190));
        subtitle.setTextSize(10.5f);
        titleColumn.addView(subtitle);

        header.addView(titleColumn, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView close = new TextView(this);
        close.setText("×");
        close.setTextColor(Color.rgb(220, 234, 250));
        close.setTextSize(23f);
        close.setGravity(Gravity.CENTER);
        close.setContentDescription("Close AntiGolem Toolkit");
        close.setClickable(true);
        close.setFocusable(true);
        close.setBackground(compactRippleBackground(Color.rgb(14, 31, 53), 18));
        close.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            collapseMenu();
        });
        header.addView(close, new LinearLayout.LayoutParams(dp(34), dp(34)));
        panel.addView(header);

        panel.addView(menuButton("◯  Circle select & analyze", v -> startCircleSelection()));
        panel.addView(menuButton("◎  Analyze visible text", v -> {
            collapseMenu();
            captureCurrentWindow();
        }));
        panel.addView(menuButton("▣  Open text file", v -> {
            collapseMenu();
            launchMain(MainActivity.ACTION_OPEN_FILE);
        }));
        panel.addView(menuButton("✎  Write / paste text", v -> {
            collapseMenu();
            launchMain(MainActivity.ACTION_COMPOSE);
        }));
        panel.addView(menuButton("↗  Open AntiGolem", v -> {
            collapseMenu();
            launchMain(MainActivity.ACTION_OPEN_APP);
        }));
        panel.addView(menuButton("AI  Local AI", v -> {
            collapseMenu();
            launchMain(MainActivity.ACTION_LOCAL_AI);
        }));
        panel.addView(menuButton("⚙  Toolkit settings", v -> {
            collapseMenu();
            Intent settings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settings);
        }));

        TextView hint = new TextView(this);
        hint.setText("Tap × or the bubble to close · drag bubble to move");
        hint.setTextColor(Color.rgb(103, 128, 157));
        hint.setTextSize(9.5f);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(5), dp(5), dp(5), 0);
        panel.addView(hint);

        return panel;
    }

    private Button menuButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setTextColor(Color.rgb(232, 242, 255));
        button.setTextSize(12.5f);
        button.setPadding(dp(11), 0, dp(8), 0);
        button.setMinHeight(dp(41));
        button.setMinimumHeight(dp(41));
        button.setStateListAnimator(null);
        button.setBackground(compactRippleBackground(Color.rgb(13, 29, 49), 12));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(41));
        lp.setMargins(0, dp(2), 0, dp(2));
        button.setLayoutParams(lp);
        button.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(55)
                    .withEndAction(() -> {
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        listener.onClick(v);
                    }).start();
        });
        return button;
    }

    private RippleDrawable compactRippleBackground(int fill, int radiusDp) {
        GradientDrawable content = new GradientDrawable();
        content.setShape(GradientDrawable.RECTANGLE);
        content.setCornerRadius(dp(radiusDp));
        content.setColor(fill);
        content.setStroke(dp(1), Color.rgb(29, 57, 87));
        return new RippleDrawable(
                ColorStateList.valueOf(Color.argb(75, 33, 217, 198)), content, null);
    }

    private GradientDrawable bubbleBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.rgb(7, 17, 31));
        bg.setStroke(dp(2), Color.rgb(33, 217, 198));
        return bg;
    }

    private boolean handleBubbleTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                downX = overlayParams.x;
                downY = overlayParams.y;
                moved = false;
                v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(60).start();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - downRawX;
                float dy = event.getRawY() - downRawY;
                if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved = true;
                overlayParams.x = Math.max(0, downX - Math.round(dx));
                overlayParams.y = Math.max(0, downY + Math.round(dy));
                if (windowManager != null && overlayRoot != null) {
                    windowManager.updateViewLayout(overlayRoot, overlayParams);
                }
                return true;
            case MotionEvent.ACTION_UP:
                v.animate().scaleX(menuOpen ? 1.06f : 1f)
                        .scaleY(menuOpen ? 1.06f : 1f).setDuration(80).start();
                if (!moved) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    toggleMenu();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                return true;
            default:
                return false;
        }
    }

    private void toggleMenu() {
        if (menuOpen) collapseMenu(); else expandMenu();
    }

    private void expandMenu() {
        if (menuPanel == null || menuOpen) return;
        menuOpen = true;
        menuPanel.animate().cancel();
        menuPanel.setVisibility(View.VISIBLE);
        menuPanel.setAlpha(0f);
        menuPanel.setScaleX(0.94f);
        menuPanel.setScaleY(0.94f);
        menuPanel.setTranslationX(dp(16));
        menuPanel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .setInterpolator(new OvershootInterpolator(0.85f))
                .setDuration(185)
                .start();
        if (bubbleButton != null) {
            bubbleButton.animate().alpha(1f).rotation(-8f).scaleX(1.06f).scaleY(1.06f)
                    .setDuration(150).start();
            bubbleButton.setContentDescription("Close AntiGolem Toolkit");
        }
    }

    private void collapseMenu() {
        if (menuPanel == null) return;
        if (!menuOpen && menuPanel.getVisibility() != View.VISIBLE) return;
        menuOpen = false;
        menuPanel.animate().cancel();
        menuPanel.animate()
                .alpha(0f)
                .scaleX(0.94f)
                .scaleY(0.94f)
                .translationX(dp(16))
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(115)
                .withEndAction(() -> {
                    if (!menuOpen && menuPanel != null) menuPanel.setVisibility(View.GONE);
                }).start();
        if (bubbleButton != null) {
            bubbleButton.animate().alpha(0.92f).rotation(0f).scaleX(1f).scaleY(1f)
                    .setDuration(120).start();
            bubbleButton.setContentDescription("Open AntiGolem Toolkit");
        }
    }

    private void startCircleSelection() {
        collapseMenu();
        if (windowManager == null || selectionOverlay != null) return;
        if (overlayRoot != null) overlayRoot.setVisibility(View.GONE);

        CircleSelectionView selector = new CircleSelectionView();
        selectionOverlay = selector;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        windowManager.addView(selector, params);
        Toast.makeText(this,
                "Draw an oval around the text to analyze. A tiny tap cancels.",
                Toast.LENGTH_SHORT).show();
    }

    private void finishCircleSelection(RectF region) {
        RectF selected = new RectF(region);
        removeSelectionOverlay();
        if (overlayRoot != null) overlayRoot.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> captureSelectedRegion(selected), 140);
    }

    private void cancelCircleSelection() {
        removeSelectionOverlay();
        if (overlayRoot != null) overlayRoot.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Circle Select cancelled.", Toast.LENGTH_SHORT).show();
    }

    private void removeSelectionOverlay() {
        if (windowManager != null && selectionOverlay != null) {
            try {
                windowManager.removeView(selectionOverlay);
            } catch (Exception ignored) {
            }
        }
        selectionOverlay = null;
    }

    private void captureSelectedRegion(RectF region) {
        AccessibilityNodeInfo root = findTargetRoot();
        if (root == null) {
            Toast.makeText(this, "No accessibility text is available in the selected app.", Toast.LENGTH_LONG).show();
            return;
        }

        LinkedHashSet<String> parts = new LinkedHashSet<>();
        collectTextInRegion(root, parts, 0, region);
        String captured = joinCaptured(parts, 60000);
        if (captured.trim().isEmpty()) {
            Toast.makeText(this,
                    "No accessibility-visible text was found inside the circle. Image/canvas/PDF text may need OCR.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        storeCapturedAndAnalyze(captured);
    }

    private void captureCurrentWindow() {
        AccessibilityNodeInfo root = findTargetRoot();
        if (root == null) {
            Toast.makeText(this, "No accessibility text is available in the active app.", Toast.LENGTH_LONG).show();
            return;
        }

        LinkedHashSet<String> parts = new LinkedHashSet<>();
        collectText(root, parts, 0);
        String captured = joinCaptured(parts, 60000);
        if (captured.trim().isEmpty()) {
            Toast.makeText(this,
                    "This screen exposes no readable accessibility text. Image/canvas/PDF content may require OCR.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        storeCapturedAndAnalyze(captured);
    }

    private String joinCaptured(Set<String> parts, int maxChars) {
        StringBuilder full = new StringBuilder();
        for (String part : parts) {
            String clean = part == null ? "" : part.trim();
            if (clean.isEmpty()) continue;
            if (clean.equals("AntiGolem") || clean.equals("Floating language toolkit") ||
                    clean.contains("Open AntiGolem Toolkit")) continue;
            if (full.length() > 0) full.append('\n');
            full.append(clean);
            if (full.length() >= maxChars) break;
        }
        return full.length() > maxChars ? full.substring(0, maxChars) : full.toString();
    }

    private void storeCapturedAndAnalyze(String captured) {
        long now = System.currentTimeMillis();
        getSharedPreferences("antigolem", MODE_PRIVATE).edit()
                .putString("captured_text", captured)
                .putLong("captured_at", now)
                .apply();
        launchMain(MainActivity.ACTION_ANALYZE_CAPTURE);
    }

    private AccessibilityNodeInfo findTargetRoot() {
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            AccessibilityNodeInfo fallback = null;
            if (windows != null) {
                for (AccessibilityWindowInfo window : windows) {
                    if (window == null || window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION) continue;
                    AccessibilityNodeInfo root = window.getRoot();
                    if (root == null) continue;
                    if (window.isActive() || window.isFocused()) return root;
                    if (fallback == null) fallback = root;
                }
            }
            if (fallback != null) return fallback;
        } catch (Exception ignored) {
        }
        return getRootInActiveWindow();
    }

    private void launchMain(String action) {
        Intent launch = new Intent(this, MainActivity.class);
        launch.putExtra(MainActivity.EXTRA_TOOLKIT_ACTION, action);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launch);
    }

    private void collectText(AccessibilityNodeInfo node, Set<String> out, int depth) {
        if (node == null || depth > 90 || out.size() > 2500) return;
        addNodeText(node, out);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) collectText(child, out, depth + 1);
        }
    }

    private void collectTextInRegion(AccessibilityNodeInfo node, Set<String> out, int depth, RectF ellipse) {
        if (node == null || depth > 90 || out.size() > 2500) return;
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        if (!bounds.isEmpty() && ellipseIntersectsRect(ellipse, bounds)) addNodeText(node, out);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) collectTextInRegion(child, out, depth + 1, ellipse);
        }
    }

    private void addNodeText(AccessibilityNodeInfo node, Set<String> out) {
        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        CharSequence hint = node.getHintText();
        if (text != null) out.add(text.toString());
        if (description != null) out.add(description.toString());
        if (hint != null) out.add(hint.toString());
    }

    private boolean ellipseIntersectsRect(RectF ellipse, Rect rect) {
        float rx = ellipse.width() / 2f;
        float ry = ellipse.height() / 2f;
        if (rx <= 1f || ry <= 1f) return false;
        float cx = ellipse.centerX();
        float cy = ellipse.centerY();
        float nearestX = Math.max(rect.left, Math.min(cx, rect.right));
        float nearestY = Math.max(rect.top, Math.min(cy, rect.bottom));
        float dx = (nearestX - cx) / rx;
        float dy = (nearestY - cy) / ry;
        return dx * dx + dy * dy <= 1f;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No background harvesting. Capture starts only from an explicit toolkit action.
    }

    @Override
    public void onInterrupt() {
        // No persistent analysis task to interrupt.
    }

    @Override
    public void onDestroy() {
        removeSelectionOverlay();
        if (windowManager != null && overlayRoot != null) {
            try {
                windowManager.removeView(overlayRoot);
            } catch (Exception ignored) {
            }
        }
        overlayRoot = null;
        menuPanel = null;
        bubbleButton = null;
        super.onDestroy();
    }

    private class CircleSelectionView extends View {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float startX;
        private float startY;
        private float currentX;
        private float currentY;
        private boolean drawing;

        CircleSelectionView() {
            super(AntiGolemAccessibilityService.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            fillPaint.setColor(Color.argb(34, 33, 217, 198));
            fillPaint.setStyle(Paint.Style.FILL);
            strokePaint.setColor(Color.rgb(33, 217, 198));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dp(3));
            handlePaint.setColor(Color.WHITE);
            handlePaint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!drawing) return;
            RectF region = normalizedRegion();
            canvas.drawOval(region, fillPaint);
            canvas.drawOval(region, strokePaint);
            canvas.drawCircle(region.left, region.centerY(), dp(4), handlePaint);
            canvas.drawCircle(region.right, region.centerY(), dp(4), handlePaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = currentX = event.getX();
                    startY = currentY = event.getY();
                    drawing = true;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    currentX = event.getX();
                    currentY = event.getY();
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    currentX = event.getX();
                    currentY = event.getY();
                    RectF region = normalizedRegion();
                    drawing = false;
                    invalidate();
                    if (region.width() < dp(28) || region.height() < dp(28)) {
                        cancelCircleSelection();
                    } else {
                        finishCircleSelection(region);
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    drawing = false;
                    invalidate();
                    cancelCircleSelection();
                    return true;
                default:
                    return false;
            }
        }

        private RectF normalizedRegion() {
            return new RectF(
                    Math.min(startX, currentX),
                    Math.min(startY, currentY),
                    Math.max(startX, currentX),
                    Math.max(startY, currentY));
        }
    }
}
