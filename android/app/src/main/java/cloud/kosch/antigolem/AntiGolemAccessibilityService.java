package cloud.kosch.antigolem;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Toast;

import java.util.LinkedHashSet;
import java.util.Set;

public class AntiGolemAccessibilityService extends AccessibilityService {
    private WindowManager windowManager;
    private Button overlayButton;
    private WindowManager.LayoutParams overlayParams;
    private float downRawX;
    private float downRawY;
    private int downX;
    private int downY;
    private boolean moved;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        showOverlay();
        Toast.makeText(this,
                "AntiGolem overlay active. Tap AG above an app to analyze accessibility-visible text locally.",
                Toast.LENGTH_LONG).show();
    }

    private void showOverlay() {
        if (overlayButton != null) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayButton = new Button(this);
        overlayButton.setText("AG");
        overlayButton.setTextColor(Color.WHITE);
        overlayButton.setTextSize(13f);
        overlayButton.setAllCaps(false);
        overlayButton.setMinWidth(dp(54));
        overlayButton.setMinHeight(dp(54));
        overlayButton.setPadding(dp(10), dp(8), dp(10), dp(8));
        overlayButton.setElevation(dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(18));
        bg.setColor(Color.rgb(92, 75, 224));
        bg.setStroke(dp(1), Color.rgb(0, 216, 179));
        overlayButton.setBackground(bg);

        overlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        overlayParams.gravity = Gravity.TOP | Gravity.END;
        overlayParams.x = dp(12);
        overlayParams.y = dp(180);

        overlayButton.setOnTouchListener(this::handleOverlayTouch);
        windowManager.addView(overlayButton, overlayParams);
    }

    private boolean handleOverlayTouch(View v, MotionEvent event) {
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
                if (windowManager != null && overlayButton != null) {
                    windowManager.updateViewLayout(overlayButton, overlayParams);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!moved) captureCurrentWindow();
                return true;
            default:
                return false;
        }
    }

    private void captureCurrentWindow() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            Toast.makeText(this, "No accessibility text is available in the active window.", Toast.LENGTH_LONG).show();
            return;
        }

        LinkedHashSet<String> parts = new LinkedHashSet<>();
        collectText(root, parts, 0);
        StringBuilder full = new StringBuilder();
        for (String part : parts) {
            String clean = part == null ? "" : part.trim();
            if (clean.isEmpty() || clean.equals("AG")) continue;
            if (full.length() > 0) full.append('\n');
            full.append(clean);
            if (full.length() >= 30000) break;
        }

        String captured = full.length() > 30000 ? full.substring(0, 30000) : full.toString();
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

        Intent launch = new Intent(this, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launch);
    }

    private void collectText(AccessibilityNodeInfo node, Set<String> out, int depth) {
        if (node == null || depth > 80 || out.size() > 1500) return;
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
        // Intentionally no background harvesting. Capture occurs only on explicit overlay tap.
    }

    @Override
    public void onInterrupt() {
        // No persistent processing to interrupt.
    }

    @Override
    public void onDestroy() {
        if (windowManager != null && overlayButton != null) {
            try {
                windowManager.removeView(overlayButton);
            } catch (Exception ignored) {
            }
        }
        overlayButton = null;
        super.onDestroy();
    }
}
