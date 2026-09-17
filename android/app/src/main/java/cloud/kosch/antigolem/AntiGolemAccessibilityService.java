package cloud.kosch.antigolem;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Display;
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

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AntiGolemAccessibilityService extends AccessibilityService {
    private WindowManager windowManager;
    private LinearLayout overlayRoot;
    private LinearLayout menuPanel;
    private ImageButton bubbleButton;
    private WindowManager.LayoutParams overlayParams;
    private View selectionOverlay;
    private View quickOverlay;
    private float downRawX;
    private float downRawY;
    private int downX;
    private int downY;
    private boolean moved;
    private boolean menuOpen;
    private boolean selectionUsesOcr;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        showToolkit();
        Toast.makeText(this, "AntiGolem Toolkit active. Tap the small floating icon to open it.", Toast.LENGTH_LONG).show();
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
        menuPanel.setScaleX(.94f);
        menuPanel.setScaleY(.94f);
        menuPanel.setTranslationX(dp(16));
        overlayRoot.addView(menuPanel, new LinearLayout.LayoutParams(dp(236), LinearLayout.LayoutParams.WRAP_CONTENT));
        View spacer = new View(this);
        overlayRoot.addView(spacer, new LinearLayout.LayoutParams(dp(6), dp(1)));

        bubbleButton = new ImageButton(this);
        bubbleButton.setImageResource(R.drawable.ic_antigolem);
        bubbleButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        bubbleButton.setPadding(dp(4), dp(4), dp(4), dp(4));
        bubbleButton.setContentDescription("Open AntiGolem Toolkit");
        bubbleButton.setElevation(dp(12));
        bubbleButton.setAlpha(.92f);
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
        card.setCornerRadius(dp(20));
        card.setColor(Color.rgb(8,17,31));
        card.setStroke(dp(1), Color.rgb(54,91,134));
        panel.setBackground(card);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(5),0,0,dp(5));
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("AntiGolem",15.5f,Color.WHITE,true);
        TextView subtitle = text("Floating forensic toolkit",10.5f,Color.rgb(132,158,190),false);
        titles.addView(title);titles.addView(subtitle);
        header.addView(titles,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        TextView close = text("×",23f,Color.rgb(220,234,250),false);
        close.setGravity(Gravity.CENTER);
        close.setBackground(compactRippleBackground(Color.rgb(14,31,53),18));
        close.setOnClickListener(v->{v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);collapseMenu();});
        header.addView(close,new LinearLayout.LayoutParams(dp(34),dp(34)));
        panel.addView(header);

        panel.addView(menuButton("◯  Circle text select", v -> startCircleSelection(false)));
        panel.addView(menuButton("◉  Circle OCR select", v -> startCircleSelection(true)));
        panel.addView(menuButton("◎  Analyze visible text", v -> { collapseMenu(); captureCurrentWindow(); }));
        panel.addView(menuButton("▣  Open document", v -> { collapseMenu(); launchMain(MainActivity.ACTION_OPEN_FILE); }));
        panel.addView(menuButton("✎  Write / paste text", v -> { collapseMenu(); launchMain(MainActivity.ACTION_COMPOSE); }));
        panel.addView(menuButton("↗  Open AntiGolem", v -> { collapseMenu(); launchMain(MainActivity.ACTION_OPEN_APP); }));
        panel.addView(menuButton("AI  Local AI", v -> { collapseMenu(); launchMain(MainActivity.ACTION_LOCAL_AI); }));
        panel.addView(menuButton("⚙  Toolkit settings", v -> {
            collapseMenu();
            Intent settings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settings);
        }));
        TextView hint = text("Tap × or bubble to close · drag bubble to move",9.3f,Color.rgb(103,128,157),false);
        hint.setGravity(Gravity.CENTER);hint.setPadding(dp(4),dp(5),dp(4),0);panel.addView(hint);
        return panel;
    }

    private TextView text(String value,float size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(t.getTypeface(),android.graphics.Typeface.BOLD);return t;
    }

    private Button menuButton(String label, View.OnClickListener listener) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        b.setTextColor(Color.rgb(232,242,255));b.setTextSize(12.3f);b.setPadding(dp(11),0,dp(8),0);b.setStateListAnimator(null);
        b.setBackground(compactRippleBackground(Color.rgb(13,29,49),12));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(40));lp.setMargins(0,dp(2),0,dp(2));b.setLayoutParams(lp);
        b.setOnClickListener(v->{v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);v.animate().scaleX(.97f).scaleY(.97f).setDuration(55).withEndAction(()->{v.setScaleX(1f);v.setScaleY(1f);listener.onClick(v);}).start();});
        return b;
    }

    private RippleDrawable compactRippleBackground(int fill,int radius){
        GradientDrawable content=new GradientDrawable();content.setCornerRadius(dp(radius));content.setColor(fill);content.setStroke(dp(1),Color.rgb(29,57,87));
        return new RippleDrawable(ColorStateList.valueOf(Color.argb(75,33,217,198)),content,null);
    }
    private GradientDrawable bubbleBackground(){GradientDrawable bg=new GradientDrawable();bg.setShape(GradientDrawable.OVAL);bg.setColor(Color.rgb(7,17,31));bg.setStroke(dp(2),Color.rgb(33,217,198));return bg;}

    private boolean handleBubbleTouch(View v, MotionEvent e){
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:downRawX=e.getRawX();downRawY=e.getRawY();downX=overlayParams.x;downY=overlayParams.y;moved=false;v.animate().scaleX(.94f).scaleY(.94f).setDuration(60).start();return true;
            case MotionEvent.ACTION_MOVE:float dx=e.getRawX()-downRawX,dy=e.getRawY()-downRawY;if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved=true;overlayParams.x=Math.max(0,downX-Math.round(dx));overlayParams.y=Math.max(0,downY+Math.round(dy));if(windowManager!=null&&overlayRoot!=null)windowManager.updateViewLayout(overlayRoot,overlayParams);return true;
            case MotionEvent.ACTION_UP:v.animate().scaleX(menuOpen?1.06f:1f).scaleY(menuOpen?1.06f:1f).setDuration(80).start();if(!moved){v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);toggleMenu();}return true;
            case MotionEvent.ACTION_CANCEL:v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();return true;
            default:return false;
        }
    }
    private void toggleMenu(){if(menuOpen)collapseMenu();else expandMenu();}
    private void expandMenu(){
        clearQuickResult();if(menuPanel==null||menuOpen)return;menuOpen=true;menuPanel.animate().cancel();menuPanel.setVisibility(View.VISIBLE);menuPanel.setAlpha(0f);menuPanel.setScaleX(.94f);menuPanel.setScaleY(.94f);menuPanel.setTranslationX(dp(16));menuPanel.animate().alpha(1f).scaleX(1f).scaleY(1f).translationX(0f).setInterpolator(new OvershootInterpolator(.85f)).setDuration(185).start();if(bubbleButton!=null){bubbleButton.animate().alpha(1f).rotation(-8f).scaleX(1.06f).scaleY(1.06f).setDuration(150).start();bubbleButton.setContentDescription("Close AntiGolem Toolkit");}}
    private void collapseMenu(){
        if(menuPanel==null)return;if(!menuOpen&&menuPanel.getVisibility()!=View.VISIBLE)return;menuOpen=false;menuPanel.animate().cancel();menuPanel.animate().alpha(0f).scaleX(.94f).scaleY(.94f).translationX(dp(16)).setInterpolator(new AccelerateInterpolator()).setDuration(115).withEndAction(()->{if(!menuOpen&&menuPanel!=null)menuPanel.setVisibility(View.GONE);}).start();if(bubbleButton!=null){bubbleButton.animate().alpha(.92f).rotation(0f).scaleX(1f).scaleY(1f).setDuration(120).start();bubbleButton.setContentDescription("Open AntiGolem Toolkit");}}

    private void startCircleSelection(boolean useOcr){
        collapseMenu();clearQuickResult();if(windowManager==null||selectionOverlay!=null)return;
        selectionUsesOcr=useOcr;if(overlayRoot!=null)overlayRoot.setVisibility(View.GONE);
        CircleSelectionView selector=new CircleSelectionView();selectionOverlay=selector;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.START;windowManager.addView(selector,p);
        Toast.makeText(this,useOcr?"Draw an oval around image/PDF text for local OCR.":"Draw an oval around accessibility-visible text.",Toast.LENGTH_SHORT).show();
    }
    private void finishCircleSelection(RectF region){
        RectF selected=new RectF(region);removeSelectionOverlay();
        if(selectionUsesOcr){new Handler(Looper.getMainLooper()).postDelayed(()->captureSelectedRegionOcr(selected),130);}else{if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);new Handler(Looper.getMainLooper()).postDelayed(()->captureSelectedRegion(selected),100);}
    }
    private void cancelCircleSelection(){removeSelectionOverlay();if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);Toast.makeText(this,"Circle Select cancelled.",Toast.LENGTH_SHORT).show();}
    private void removeSelectionOverlay(){if(windowManager!=null&&selectionOverlay!=null){try{windowManager.removeView(selectionOverlay);}catch(Exception ignored){}}selectionOverlay=null;}

    private void captureSelectedRegion(RectF region){
        AccessibilityNodeInfo root=findTargetRoot();if(root==null){Toast.makeText(this,"No accessibility text is available in the selected app.",Toast.LENGTH_LONG).show();return;}
        LinkedHashSet<String> parts=new LinkedHashSet<>();collectTextInRegion(root,parts,0,region);String captured=joinCaptured(parts,60000);
        if(captured.trim().isEmpty()){Toast.makeText(this,"No accessibility-visible text found. Try Circle OCR.",Toast.LENGTH_LONG).show();return;}storeCaptured(captured);showQuickResult(captured,"Circle text");
    }

    private void captureSelectedRegionOcr(RectF region){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.R){if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);Toast.makeText(this,"Circle OCR requires Android 11 or newer.",Toast.LENGTH_LONG).show();return;}
        takeScreenshot(Display.DEFAULT_DISPLAY,getMainExecutor(),new TakeScreenshotCallback(){
            @Override public void onSuccess(ScreenshotResult screenshot){
                HardwareBuffer hb=screenshot.getHardwareBuffer();Bitmap wrapped=null,copy=null,crop=null;
                try{
                    wrapped=Bitmap.wrapHardwareBuffer(hb,screenshot.getColorSpace());if(wrapped==null)throw new IllegalStateException("Could not create screenshot bitmap");copy=wrapped.copy(Bitmap.Config.ARGB_8888,false);
                    int l=Math.max(0,Math.min(copy.getWidth()-1,Math.round(region.left))),t=Math.max(0,Math.min(copy.getHeight()-1,Math.round(region.top))),r=Math.max(l+1,Math.min(copy.getWidth(),Math.round(region.right))),b=Math.max(t+1,Math.min(copy.getHeight(),Math.round(region.bottom)));
                    crop=Bitmap.createBitmap(copy,l,t,r-l,b-t);InputImage img=InputImage.fromBitmap(crop,0);TextRecognizer rec=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);Bitmap finalCopy=copy,finalCrop=crop;
                    rec.process(img).addOnSuccessListener(txt->{String captured=txt.getText()==null?"":txt.getText().trim();if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);if(captured.isEmpty())Toast.makeText(AntiGolemAccessibilityService.this,"OCR found no readable text in the selected area.",Toast.LENGTH_LONG).show();else{storeCaptured(captured);showQuickResult(captured,"Circle OCR");}rec.close();if(finalCrop!=null&&!finalCrop.isRecycled())finalCrop.recycle();if(finalCopy!=null&&!finalCopy.isRecycled())finalCopy.recycle();}).addOnFailureListener(err->{if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);Toast.makeText(AntiGolemAccessibilityService.this,"OCR failed: "+err.getMessage(),Toast.LENGTH_LONG).show();rec.close();if(finalCrop!=null&&!finalCrop.isRecycled())finalCrop.recycle();if(finalCopy!=null&&!finalCopy.isRecycled())finalCopy.recycle();});
                }catch(Exception ex){if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);Toast.makeText(AntiGolemAccessibilityService.this,"Circle OCR failed: "+ex.getMessage(),Toast.LENGTH_LONG).show();if(crop!=null&&!crop.isRecycled())crop.recycle();if(copy!=null&&!copy.isRecycled())copy.recycle();}finally{hb.close();}
            }
            @Override public void onFailure(int errorCode){if(overlayRoot!=null)overlayRoot.setVisibility(View.VISIBLE);Toast.makeText(AntiGolemAccessibilityService.this,"Screenshot unavailable (code "+errorCode+"). Protected windows cannot be OCR-scanned.",Toast.LENGTH_LONG).show();}
        });
    }

    private void captureCurrentWindow(){AccessibilityNodeInfo root=findTargetRoot();if(root==null){Toast.makeText(this,"No accessibility text is available in the active app.",Toast.LENGTH_LONG).show();return;}LinkedHashSet<String> parts=new LinkedHashSet<>();collectText(root,parts,0);String captured=joinCaptured(parts,60000);if(captured.trim().isEmpty()){Toast.makeText(this,"No readable accessibility text. Try Circle OCR.",Toast.LENGTH_LONG).show();return;}storeCaptured(captured);showQuickResult(captured,"Visible text");}

    private void storeCaptured(String captured){getSharedPreferences("antigolem",MODE_PRIVATE).edit().putString("captured_text",captured).putLong("captured_at",System.currentTimeMillis()).apply();}

    private void showQuickResult(String captured,String source){
        clearQuickResult();QuickStats q=quickStats(captured);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(13),dp(12),dp(13),dp(12));card.setElevation(dp(18));GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(18));bg.setColor(Color.rgb(7,18,32));bg.setStroke(dp(1),Color.rgb(45,93,125));card.setBackground(bg);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView ttl=text("AntiGolem · Quick scan",15f,Color.WHITE,true);head.addView(ttl,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));TextView close=text("×",22f,Color.WHITE,false);close.setGravity(Gravity.CENTER);close.setBackground(compactRippleBackground(Color.rgb(15,34,55),18));close.setOnClickListener(v->clearQuickResult());head.addView(close,new LinearLayout.LayoutParams(dp(34),dp(34)));card.addView(head);
        TextView meta=text(source+" · "+q.sentences+" sentences",10.5f,Color.rgb(132,158,190),false);card.addView(meta);
        TextView score=text(q.flagged+" flagged · "+q.percent+"% quick issue rate",18f,q.percent>=50?Color.rgb(255,105,118):q.percent>=25?Color.rgb(244,184,90):Color.rgb(74,218,166),true);score.setPadding(0,dp(7),0,dp(5));card.addView(score);
        TextView note=text("Preliminary overlay estimate. Open Details for the full strict sentence audit and grounded percentages.",10.2f,Color.rgb(154,178,207),false);card.addView(note);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);actions.setPadding(0,dp(8),0,0);
        actions.addView(quickButton("Details",v->{clearQuickResult();launchMain(MainActivity.ACTION_ANALYZE_CAPTURE);}),new LinearLayout.LayoutParams(0,dp(40),1f));
        actions.addView(quickButton("Improve",v->{clearQuickResult();launchMain(MainActivity.ACTION_OPEN_IMPROVE);}),new LinearLayout.LayoutParams(0,dp(40),1f));
        actions.addView(quickButton("Reply",v->{clearQuickResult();launchMain(MainActivity.ACTION_OPEN_REPLY);}),new LinearLayout.LayoutParams(0,dp(40),1f));
        actions.addView(quickButton("Copy",v->{ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("AntiGolem capture",captured));Toast.makeText(this,"Captured text copied.",Toast.LENGTH_SHORT).show();}),new LinearLayout.LayoutParams(0,dp(40),1f));card.addView(actions);
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(330),WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.y=dp(90);quickOverlay=card;windowManager.addView(card,p);card.setAlpha(0f);card.setScaleX(.95f);card.setScaleY(.95f);card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(160).start();
    }
    private Button quickButton(String label,View.OnClickListener l){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(10.3f);b.setTextColor(Color.WHITE);b.setPadding(dp(3),0,dp(3),0);b.setBackground(compactRippleBackground(Color.rgb(15,35,58),10));b.setOnClickListener(l);return b;}
    private void clearQuickResult(){if(windowManager!=null&&quickOverlay!=null){try{windowManager.removeView(quickOverlay);}catch(Exception ignored){}}quickOverlay=null;}

    private QuickStats quickStats(String text){String[] ss=text.trim().split("(?<=[.!?…])\\s+|\\n+");int flagged=0,total=0;String[] cues={"fail","failure","scheiter","problem","crisis","krise","threat","bedroh","danger","gefähr","never","niemals","always","immer","must","muss","should","soll","impossible","unmöglich","powerless","machtlos"};for(String s:ss){if(s.trim().isEmpty())continue;total++;String lower=s.toLowerCase(Locale.ROOT);boolean hit=false;for(String c:cues)if(lower.contains(c)){hit=true;break;}if(hit)flagged++;}return new QuickStats(total,flagged,total==0?0:Math.round(flagged*100f/total));}
    private static class QuickStats{final int sentences,flagged,percent;QuickStats(int s,int f,int p){sentences=s;flagged=f;percent=p;}}

    private AccessibilityNodeInfo findTargetRoot(){try{List<AccessibilityWindowInfo>w=getWindows();AccessibilityNodeInfo fallback=null;if(w!=null)for(AccessibilityWindowInfo x:w){if(x==null||x.getType()!=AccessibilityWindowInfo.TYPE_APPLICATION)continue;AccessibilityNodeInfo r=x.getRoot();if(r==null)continue;if(x.isActive()||x.isFocused())return r;if(fallback==null)fallback=r;}if(fallback!=null)return fallback;}catch(Exception ignored){}return getRootInActiveWindow();}
    private void launchMain(String action){Intent i=new Intent(this,MainActivity.class);i.putExtra(MainActivity.EXTRA_TOOLKIT_ACTION,action);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(i);}
    private void collectText(AccessibilityNodeInfo n,Set<String> out,int depth){if(n==null||depth>90||out.size()>2500)return;addNodeText(n,out);for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)collectText(c,out,depth+1);}}
    private void collectTextInRegion(AccessibilityNodeInfo n,Set<String> out,int depth,RectF ellipse){if(n==null||depth>90||out.size()>2500)return;Rect b=new Rect();n.getBoundsInScreen(b);if(!b.isEmpty()&&ellipseIntersectsRect(ellipse,b))addNodeText(n,out);for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null)collectTextInRegion(c,out,depth+1,ellipse);}}
    private void addNodeText(AccessibilityNodeInfo n,Set<String> out){CharSequence t=n.getText(),d=n.getContentDescription(),h=n.getHintText();if(t!=null)out.add(t.toString());if(d!=null)out.add(d.toString());if(h!=null)out.add(h.toString());}
    private String joinCaptured(Set<String> parts,int max){StringBuilder full=new StringBuilder();for(String part:parts){String clean=part==null?"":part.trim();if(clean.isEmpty()||clean.equals("AntiGolem")||clean.equals("Floating forensic toolkit")||clean.contains("Open AntiGolem Toolkit"))continue;if(full.length()>0)full.append('\n');full.append(clean);if(full.length()>=max)break;}return full.length()>max?full.substring(0,max):full.toString();}
    private boolean ellipseIntersectsRect(RectF e,Rect r){float rx=e.width()/2f,ry=e.height()/2f;if(rx<=1||ry<=1)return false;float cx=e.centerX(),cy=e.centerY(),nx=Math.max(r.left,Math.min(cx,r.right)),ny=Math.max(r.top,Math.min(cy,r.bottom)),dx=(nx-cx)/rx,dy=(ny-cy)/ry;return dx*dx+dy*dy<=1f||e.contains(r.centerX(),r.centerY());}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    @Override public void onAccessibilityEvent(AccessibilityEvent event){ }
    @Override public void onInterrupt(){ }
    @Override public void onDestroy(){removeSelectionOverlay();clearQuickResult();if(windowManager!=null&&overlayRoot!=null){try{windowManager.removeView(overlayRoot);}catch(Exception ignored){}}overlayRoot=null;menuPanel=null;bubbleButton=null;super.onDestroy();}

    private class CircleSelectionView extends View{
        private final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG),shade=new Paint();private float sx,sy,cx,cy;private boolean drawing;
        CircleSelectionView(){super(AntiGolemAccessibilityService.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(dp(4));stroke.setColor(Color.rgb(33,217,198));stroke.setShadowLayer(dp(6),0,0,Color.argb(180,33,217,198));shade.setColor(Color.argb(70,0,0,0));}
        @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawRect(0,0,getWidth(),getHeight(),shade);if(drawing){RectF r=norm(sx,sy,cx,cy);c.drawOval(r,stroke);}}
        @Override public boolean onTouchEvent(MotionEvent e){switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:sx=cx=e.getX();sy=cy=e.getY();drawing=true;invalidate();return true;case MotionEvent.ACTION_MOVE:cx=e.getX();cy=e.getY();invalidate();return true;case MotionEvent.ACTION_UP:cx=e.getX();cy=e.getY();drawing=false;RectF r=norm(sx,sy,cx,cy);if(r.width()<dp(28)||r.height()<dp(28))cancelCircleSelection();else finishCircleSelection(r);return true;case MotionEvent.ACTION_CANCEL:cancelCircleSelection();return true;default:return false;}}
        private RectF norm(float x1,float y1,float x2,float y2){return new RectF(Math.min(x1,x2),Math.min(y1,y2),Math.max(x1,x2),Math.max(y1,y2));}
    }
}
