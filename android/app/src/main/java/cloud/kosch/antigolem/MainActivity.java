package cloud.kosch.antigolem;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class MainActivity extends Activity {
    public static final String EXTRA_TOOLKIT_ACTION = "cloud.kosch.antigolem.TOOLKIT_ACTION";
    public static final String ACTION_ANALYZE_CAPTURE = "analyze_capture";
    public static final String ACTION_OPEN_FILE = "open_file";
    public static final String ACTION_COMPOSE = "compose";
    public static final String ACTION_OPEN_APP = "open_app";
    public static final String ACTION_LOCAL_AI = "local_ai";
    public static final String ACTION_OPEN_IMPROVE = "open_improve";
    public static final String ACTION_OPEN_REPLY = "open_reply";

    private static final int REQUEST_WEB_FILE = 7101;
    private WebView webView;
    private Button toolkitButton;
    private boolean pageReady;
    private boolean pendingCompose;
    private String pendingWorkbenchTab;
    private boolean pendingDocumentPicker;
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
            @Override public void onPageFinished(WebView view, String url) {
                pageReady = true;
                if (isMainPage(url)) {
                    injectPendingCapture();
                    if (pendingCompose) focusComposer();
                    dispatchPendingWorkbench();
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (webFileCallback != null) webFileCallback.onReceiveValue(null);
                webFileCallback = callback;
                try {
                    Intent chooser = params.createIntent();
                    chooser.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(chooser, REQUEST_WEB_FILE);
                    return true;
                } catch (Exception e) {
                    webFileCallback = null;
                    Toast.makeText(MainActivity.this, "No compatible file picker is available.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
        root.addView(webView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        toolkitButton = new Button(this);
        toolkitButton.setAllCaps(false);toolkitButton.setTextColor(Color.WHITE);toolkitButton.setTextSize(12.5f);toolkitButton.setPadding(dp(14),0,dp(14),0);toolkitButton.setElevation(dp(10));
        toolkitButton.setOnClickListener(v -> openAccessibilitySettings());
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, dp(46));bp.gravity=Gravity.BOTTOM|Gravity.END;bp.setMargins(dp(16),dp(16),dp(16),dp(24));root.addView(toolkitButton,bp);
        setContentView(root);

        String action = getIntent().getStringExtra(EXTRA_TOOLKIT_ACTION);
        if (ACTION_LOCAL_AI.equals(action)) loadPage("local-ai.html"); else loadMainPage();
        handleToolkitAction(getIntent());
        updateToolkitButton();
    }

    private void loadPage(String asset){pageReady=false;webView.loadUrl("file:///android_asset/"+asset);}
    private void loadMainPage(){String url=webView==null?null:webView.getUrl();if(isMainPage(url))return;loadPage("index.html");}
    private boolean isMainPage(String url){return url!=null&&(url.endsWith("/index.html")||url.equals("file:///android_asset/"));}

    private void handleToolkitAction(Intent intent){
        if(intent==null)return;String action=intent.getStringExtra(EXTRA_TOOLKIT_ACTION);if(action==null)return;intent.removeExtra(EXTRA_TOOLKIT_ACTION);
        switch(action){
            case ACTION_OPEN_FILE: pendingWorkbenchTab="docs";pendingDocumentPicker=true;loadMainPage();dispatchPendingWorkbench();break;
            case ACTION_COMPOSE: pendingCompose=true;loadMainPage();if(pageReady&&isMainPage(webView.getUrl()))focusComposer();break;
            case ACTION_LOCAL_AI: loadPage("local-ai.html");break;
            case ACTION_OPEN_IMPROVE: pendingWorkbenchTab="diff";loadMainPage();if(pageReady)injectPendingCapture();dispatchPendingWorkbench();break;
            case ACTION_OPEN_REPLY: pendingWorkbenchTab="reply";loadMainPage();if(pageReady)injectPendingCapture();dispatchPendingWorkbench();break;
            case ACTION_ANALYZE_CAPTURE: loadMainPage();if(pageReady&&isMainPage(webView.getUrl()))injectPendingCapture();break;
            case ACTION_OPEN_APP: default: loadMainPage();break;
        }
    }

    private void dispatchPendingWorkbench(){
        if(!pageReady||webView==null||!isMainPage(webView.getUrl())||pendingWorkbenchTab==null)return;
        final String tab=pendingWorkbenchTab;final boolean openPicker=pendingDocumentPicker;pendingWorkbenchTab=null;pendingDocumentPicker=false;
        new Handler(Looper.getMainLooper()).postDelayed(()->webView.evaluateJavascript(
                "(function(){if(window.AntiGolemWorkbench){window.AntiGolemWorkbench.open("+JSONObject.quote(tab)+");"+(openPicker?"setTimeout(function(){var f=document.getElementById('agDocInput');if(f)f.click();},180);":"")+"return true;}return false;})()",null),220);
    }

    private void focusComposer(){if(!pageReady||webView==null||!isMainPage(webView.getUrl()))return;pendingCompose=false;webView.evaluateJavascript("(function(){var e=document.getElementById('sourceText');if(e){e.focus();e.scrollIntoView({behavior:'smooth',block:'center'});return true;}return false;})()",null);}

    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);handleToolkitAction(intent);}
    @Override protected void onResume(){super.onResume();updateToolkitButton();if(pageReady&&isMainPage(webView==null?null:webView.getUrl())){injectPendingCapture();if(pendingCompose)focusComposer();dispatchPendingWorkbench();}}

    private void injectPendingCapture(){
        if(!pageReady||webView==null||!isMainPage(webView.getUrl()))return;
        String text=getSharedPreferences("antigolem",MODE_PRIVATE).getString("captured_text","");long stamp=getSharedPreferences("antigolem",MODE_PRIVATE).getLong("captured_at",0L),consumed=getSharedPreferences("antigolem",MODE_PRIVATE).getLong("consumed_at",0L);
        if(text==null||text.trim().isEmpty()||stamp<=consumed)return;String quoted=JSONObject.quote(text);
        webView.evaluateJavascript("(function(){if(window.AntiGolem&&window.AntiGolem.setCapturedText){window.AntiGolem.setCapturedText("+quoted+");return true;}return false;})()",value->{if("true".equals(value))getSharedPreferences("antigolem",MODE_PRIVATE).edit().putLong("consumed_at",stamp).apply();});
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==REQUEST_WEB_FILE){if(webFileCallback!=null){Uri[] result=WebChromeClient.FileChooserParams.parseResult(resultCode,data);webFileCallback.onReceiveValue(result);webFileCallback=null;}}}

    private void openAccessibilitySettings(){Toast.makeText(this,"Enable AntiGolem Screen Text Analyzer once. The floating toolkit then stays available over launcher and apps.",Toast.LENGTH_LONG).show();startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}
    private boolean isToolkitEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(enabled==null||enabled.trim().isEmpty())return false;String target=new ComponentName(this,AntiGolemAccessibilityService.class).flattenToString();for(String item:enabled.split(":"))if(target.equalsIgnoreCase(item))return true;return false;}
    private void updateToolkitButton(){if(toolkitButton==null)return;boolean active=isToolkitEnabled();toolkitButton.setText(active?"Toolkit active ✓":"Enable floating toolkit");GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(22));bg.setColor(active?Color.rgb(10,84,78):Color.rgb(67,55,166));bg.setStroke(dp(1),active?Color.rgb(33,217,198):Color.rgb(115,96,255));toolkitButton.setBackground(bg);}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(webFileCallback!=null){webFileCallback.onReceiveValue(null);webFileCallback=null;}if(webView!=null){webView.stopLoading();webView.destroy();}super.onDestroy();}
}
