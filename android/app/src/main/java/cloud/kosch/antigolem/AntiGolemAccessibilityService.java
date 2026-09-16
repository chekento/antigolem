package cloud.kosch.antigolem;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
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
                "AntiGolem Toolkit is active. Tap the floating icon to open the toolkit.",
                Toast.LENGTH_LONG).show();
    }

    private void showToolkit() {
        if (overlayRoot != null) return;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayRoot = new LinearLayout(this);
        overlayRoot.setOrientation(LinearLayout.HORIZONTAL);
        overlayRoot.setGravity(Gravity.CENTER_VERTICAL);
        overlayRoot.setPadding(dp(2), dp(2), dp(2), dp(2));

        menuPanel = buildMenuPanel();
        menuPanel.setVisibility(View.GONE);
        menuPanel.setAlpha(0f);
        overlayRoot.addView(menuPanel, new LinearLayout.LayoutParams(dp(248), LinearLayout.LayoutParams.WRAP_CONTENT));

        View spacer = new View(this);
        overlayRoot.addView(spacer, new LinearLayout.LayoutParams(dp(8), dp(1)));

        bubbleButton = new ImageButton(this);
        bubbleButton.setImageResource(R.drawable.ic_antigolem);
        bubbleButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        bubbleButton.setPadding(dp(3), dp(3), dp(3), dp(3));
        bubbleButton.setContentDescription("Open AntiGolem Toolkit");
        bubbleButton.setElevation(dp(14));
        bubbleButton.setBackground(circleBackground(Color.rgb(7, 17, 31), Color.rgb(33, 217, 198), 28));
        overlayRoot.addView(bubbleButton, new LinearLayout.LayoutParams(dp(62), dp(62)));

        overlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        overlayParams.gravity = Gravity.TOP | Gravity.END;
        overlayParams.x = dp(10);
        overlayParams.y = dp(180);

        bubbleButton.setOnTouchListener(this::handleBubbleTouch);
        windowManager.addView(overlayRoot, overlayParams);
    }

    private LinearLayout buildMenuPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(12), dp(12), dp(12));
        panel.setElevation(dp(16));

        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        card.setCornerRadius(dp(22));
        card.setColor(Color.rgb(8, 17, 31));
        card.setStroke(dp(1), Color.rgb(51, 87, 132));
        panel.setBackground(card);

        TextView title = new TextView(this);
        title.setText("AntiGolem Toolkit");
        title.setTextColor(Color.WHITE);
        title.setTextSize(17f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        title.setPadding(dp(8), dp(2), dp(8), 0);
        panel.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText("Analyze · import · write · local AI");
        subtitle.setTextColor(Color.rgb(153, 175, 204));
        subtitle.setTextSize(11.5f);
        subtitle.setPadding(dp(8), 0, dp(8), dp(8));
        panel.addView(subtitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

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
        hint.setText("Drag the AntiGolem icon to move it. Tap it to minimize.");
        hint.setTextColor(Color.rgb(110, 132, 160));
        hint.setTextSize(10.5f);
        hint.setPadding(dp(8), dp(7), dp(8), dp(2));
        panel.addView(hint);

        return panel;
    }

    private Button menuButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setTextColor(Color.rgb(230, 241, 255));
        button.setTextSize(13.5f);
        button.setPadding(dp(12), 0, dp(10), 0);
        button.setMinHeight(dp(46));
        button.setMinimumHeight(dp(46));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(13));
        bg.setColor(Color.rgb(14, 31, 53));
        bg.setStroke(dp(1), Color.rgb(30, 61, 93));
        button.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        lp.setMargins(0, dp(3), 0, dp(3));
        button.setLayoutParams(lp);
        button.setOnClickListener(listener);
        return button;
    }

    private GradientDrawable circleBackground(int fill, int stroke, int radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(radiusDp));
        bg.setColor(fill);
        bg.setStroke(dp(2), stroke);
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
                if (!moved) toggleMenu();
                return true;
            default:
                return false;
        }
    }

    private void toggleMenu() {
        if (menuOpen) collapseMenu(); else expandMenu();
    }

    private void expandMenu() {
        if (menuPanel == null) return;
        menuOpen = true;
        menuPanel.setVisibility(View.VISIBLE);
        menuPanel.animate().alpha(1f).setDuration(140).start();
        bubbleButton.setContentDescription("Minimize AntiGolem Toolkit");
    }

    private void collapseMenu() {
        if (menuPanel == null) return;
        menuOpen = false;
        menuPanel.animate().alpha(0f).setDuration(100).withEndAction(() -> {
            if (!menuOpen && menuPanel != null) menuPanel.setVisibility(View.GONE);
        }).start();
        if (bubbleButton != null) bubbleButton.setContentDescription("Open AntiGolem Toolkit");
    }

    private void captureCurrentWindow() {
        AccessibilityNodeInfo root = findTargetRoot();
        if (root == null) {
            Toast.makeText(this, "No accessibility text is available in the active app.", Toast.LENGTH_LONG).show();
            return;
        }

        LinkedHashSet<String> parts = new LinkedHashSet<>();
        collectText(root, parts, 0);
        StringBuilder full = new StringBuilder();
        for (String part : parts) {
            String clean = part == null ? "" : part.trim();
            if (clean.isEmpty()) continue;
            if (clean.equals("AntiGolem Toolkit") || clean.startsWith("Analyze · import") ||
                    clean.contains("Open AntiGolem Toolkit")) continue;
            if (full.length() > 0) full.append('\n');
            full.append(clean);
            if (full.length() >= 60000) break;
        }

        String captured = full.length() > 60000 ? full.substring(0, 60000) : full.toString();
        if (captured.trim().isEmpty()) {
            Toast.makeText(this,
                    "This screen exposes no readable accessibility text. Image/canvas/PDF content may require OCR.",
                    Toast.LENGTH_LONG).show();
            return;
        }

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
        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        CharSequence hint = node.getHintText();
        if (text != null) out.add(text.toString());
        if (description != null) out.add(description.toString());
        if (hint != null) out.add(hint.toString());
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) collectText(child, out, depth + 1);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No background harvesting. Analysis/capture starts only from an explicit toolkit action.
    }

    @Override
    public void onInterrupt() {
        // No persistent analysis task to interrupt.
    }

    @Override
    public void onDestroy() {
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
}
