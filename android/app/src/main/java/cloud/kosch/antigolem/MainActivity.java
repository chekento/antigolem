package cloud.kosch.antigolem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView webView;
    private boolean pageReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                injectPendingCapture();
            }
        });

        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        Button accessibilityButton = new Button(this);
        accessibilityButton.setText("AG Overlay");
        accessibilityButton.setAllCaps(false);
        accessibilityButton.setAlpha(0.90f);
        accessibilityButton.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Enable AntiGolem Screen Text Analyzer, then tap the floating AG button over another app.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.BOTTOM | Gravity.END;
        buttonParams.setMargins(18, 18, 18, 28);
        root.addView(accessibilityButton, buttonParams);

        setContentView(root);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        injectPendingCapture();
    }

    @Override
    protected void onResume() {
        super.onResume();
        injectPendingCapture();
    }

    private void injectPendingCapture() {
        if (!pageReady || webView == null) return;

        String text = getSharedPreferences("antigolem", MODE_PRIVATE)
                .getString("captured_text", "");
        long stamp = getSharedPreferences("antigolem", MODE_PRIVATE)
                .getLong("captured_at", 0L);
        long consumed = getSharedPreferences("antigolem", MODE_PRIVATE)
                .getLong("consumed_at", 0L);

        if (text == null || text.trim().isEmpty() || stamp <= consumed) return;

        String quoted = JSONObject.quote(text);
        webView.evaluateJavascript(
                "if(window.AntiGolem){window.AntiGolem.setCapturedText(" + quoted + ");}",
                value -> getSharedPreferences("antigolem", MODE_PRIVATE)
                        .edit()
                        .putLong("consumed_at", stamp)
                        .apply());
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
