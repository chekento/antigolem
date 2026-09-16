package cloud.kosch.antigolem;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    public static final String EXTRA_TOOLKIT_ACTION = "cloud.kosch.antigolem.TOOLKIT_ACTION";
    public static final String ACTION_ANALYZE_CAPTURE = "analyze_capture";
    public static final String ACTION_OPEN_FILE = "open_file";
    public static final String ACTION_COMPOSE = "compose";
    public static final String ACTION_OPEN_APP = "open_app";
    public static final String ACTION_LOCAL_AI = "local_ai";

    private static final int REQUEST_WEB_FILE = 7101;
    private static final int REQUEST_TOOLKIT_FILE = 7102;
    private static final int MAX_IMPORTED_CHARS = 1_500_000;

    private WebView webView;
    private Button toolkitButton;
    private boolean pageReady = false;
    private boolean pendingCompose = false;
    private ValueCallback<Uri[]> webFileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                if (isMainPage(url)) {
                    injectPendingCapture();
                    if (pendingCompose) focusComposer();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (webFileCallback != null) webFileCallback.onReceiveValue(null);
                webFileCallback = filePathCallback;
                try {
                    Intent chooser = fileChooserParams.createIntent();
                    startActivityForResult(chooser, REQUEST_WEB_FILE);
                    return true;
                } catch (Exception e) {
                    webFileCallback = null;
                    Toast.makeText(MainActivity.this,
                            "No compatible file picker is available.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        toolkitButton = new Button(this);
        toolkitButton.setAllCaps(false);
        toolkitButton.setTextColor(Color.WHITE);
        toolkitButton.setTextSize(12.5f);
        toolkitButton.setPadding(dp(14), 0, dp(14), 0);
        toolkitButton.setElevation(dp(10));
        toolkitButton.setOnClickListener(v -> openAccessibilitySettings());

        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                dp(46));
        buttonParams.gravity = Gravity.BOTTOM | Gravity.END;
        buttonParams.setMargins(dp(16), dp(16), dp(16), dp(24));
        root.addView(toolkitButton, buttonParams);

        setContentView(root);

        String launchAction = getIntent().getStringExtra(EXTRA_TOOLKIT_ACTION);
        if (ACTION_LOCAL_AI.equals(launchAction)) {
            loadPage("local-ai.html");
        } else {
            loadMainPage();
        }
        handleToolkitAction(getIntent());
        updateToolkitButton();
    }

    private void loadPage(String assetPage) {
        pageReady = false;
        webView.loadUrl("file:///android_asset/" + assetPage);
    }

    private void loadMainPage() {
        String url = webView == null ? null : webView.getUrl();
        if (isMainPage(url)) return;
        loadPage("index.html");
    }

    private boolean isMainPage(String url) {
        return url != null && (url.endsWith("/index.html") || url.equals("file:///android_asset/"));
    }

    private void handleToolkitAction(Intent intent) {
        if (intent == null) return;
        String action = intent.getStringExtra(EXTRA_TOOLKIT_ACTION);
        if (action == null) return;
        intent.removeExtra(EXTRA_TOOLKIT_ACTION);

        switch (action) {
            case ACTION_OPEN_FILE:
                loadMainPage();
                openToolkitFilePicker();
                break;
            case ACTION_COMPOSE:
                pendingCompose = true;
                loadMainPage();
                if (pageReady && isMainPage(webView.getUrl())) focusComposer();
                break;
            case ACTION_LOCAL_AI:
                loadPage("local-ai.html");
                break;
            case ACTION_OPEN_APP:
                loadMainPage();
                break;
            case ACTION_ANALYZE_CAPTURE:
                loadMainPage();
                if (pageReady && isMainPage(webView.getUrl())) injectPendingCapture();
                break;
            default:
                loadMainPage();
                break;
        }
    }

    private void openToolkitFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "text/plain", "text/markdown", "text/csv", "text/html",
                "application/json", "application/xml", "text/xml"
        });
        try {
            startActivityForResult(intent, REQUEST_TOOLKIT_FILE);
        } catch (Exception e) {
            Toast.makeText(this, "No compatible document picker is available.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_WEB_FILE) {
            if (webFileCallback != null) {
                Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                webFileCallback.onReceiveValue(result);
                webFileCallback = null;
            }
            return;
        }

        if (requestCode == REQUEST_TOOLKIT_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try {
                String text = readTextFromUri(uri);
                if (text.trim().isEmpty()) {
                    Toast.makeText(this, "The selected file contains no readable text.", Toast.LENGTH_LONG).show();
                    return;
                }
                queueTextForAnalysis(text);
                Toast.makeText(this, "Text file loaded into AntiGolem.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this,
                        "Could not read this file as text.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String readTextFromUri(Uri uri) throws Exception {
        StringBuilder out = new StringBuilder();
        try (InputStream stream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1 && out.length() < MAX_IMPORTED_CHARS) {
                int remaining = MAX_IMPORTED_CHARS - out.length();
                out.append(buffer, 0, Math.min(read, remaining));
            }
        }
        if (out.length() >= MAX_IMPORTED_CHARS) {
            Toast.makeText(this,
                    "Large file: imported the first 1.5 million characters.", Toast.LENGTH_LONG).show();
        }
        return out.toString();
    }

    private void queueTextForAnalysis(String text) {
        long now = System.currentTimeMillis();
        getSharedPreferences("antigolem", MODE_PRIVATE).edit()
                .putString("captured_text", text)
                .putLong("captured_at", now)
                .apply();
        loadMainPage();
        if (pageReady && isMainPage(webView.getUrl())) injectPendingCapture();
    }

    private void focusComposer() {
        if (!pageReady || webView == null || !isMainPage(webView.getUrl())) return;
        pendingCompose = false;
        webView.evaluateJavascript(
                "(function(){var e=document.getElementById('sourceText');if(e){e.focus();e.scrollIntoView({behavior:'smooth',block:'center'});return true;}return false;})()",
                null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleToolkitAction(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToolkitButton();
        if (pageReady && isMainPage(webView == null ? null : webView.getUrl())) {
            injectPendingCapture();
            if (pendingCompose) focusComposer();
        }
    }

    private void injectPendingCapture() {
        if (!pageReady || webView == null || !isMainPage(webView.getUrl())) return;

        String text = getSharedPreferences("antigolem", MODE_PRIVATE)
                .getString("captured_text", "");
        long stamp = getSharedPreferences("antigolem", MODE_PRIVATE)
                .getLong("captured_at", 0L);
        long consumed = getSharedPreferences("antigolem", MODE_PRIVATE)
                .getLong("consumed_at", 0L);

        if (text == null || text.trim().isEmpty() || stamp <= consumed) return;

        String quoted = JSONObject.quote(text);
        webView.evaluateJavascript(
                "(function(){if(window.AntiGolem&&window.AntiGolem.setCapturedText){window.AntiGolem.setCapturedText(" + quoted + ");return true;}return false;})()",
                value -> {
                    if ("true".equals(value)) {
                        getSharedPreferences("antigolem", MODE_PRIVATE)
                                .edit()
                                .putLong("consumed_at", stamp)
                                .apply();
                    }
                });
    }

    private void openAccessibilitySettings() {
        Toast.makeText(this,
                "Enable AntiGolem Screen Text Analyzer once. Its floating toolkit then stays available over the launcher and apps.",
                Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private boolean isToolkitEnabled() {
        String enabled = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null || enabled.trim().isEmpty()) return false;
        String target = new ComponentName(this, AntiGolemAccessibilityService.class).flattenToString();
        for (String item : enabled.split(":")) {
            if (target.equalsIgnoreCase(item)) return true;
        }
        return false;
    }

    private void updateToolkitButton() {
        if (toolkitButton == null) return;
        boolean active = isToolkitEnabled();
        toolkitButton.setText(active ? "Toolkit active ✓" : "Enable floating toolkit");
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(22));
        bg.setColor(active ? Color.rgb(10, 84, 78) : Color.rgb(67, 55, 166));
        bg.setStroke(dp(1), active ? Color.rgb(33, 217, 198) : Color.rgb(115, 96, 255));
        toolkitButton.setBackground(bg);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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
        if (webFileCallback != null) {
            webFileCallback.onReceiveValue(null);
            webFileCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
