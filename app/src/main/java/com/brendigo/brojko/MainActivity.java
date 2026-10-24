package com.brendigo.brojko;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.animation.ValueAnimator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/** Brojko: offline, native number-merging game. Every visible action is functional. */
public final class MainActivity extends Activity {
    static final int INK=0xff1e2a44, CREAM=0xfffff8ee, CORAL=0xffff7a5b,
        TEAL=0xff2ec4b6, MUTED=0xff77849a, WHITE=Color.WHITE;
    static final int[] TILES={0xffeee0ce,0xffff7a5b,0xff2ec4b6,0xff3d98f0,
        0xffffbb4a,0xffff8751,0xffff6f96,0xffbd60e9,0xff835bea,
        0xff409ff0,0xff36c7a1,0xfff3b547,0xffe16bca,0xff587be3};
    SharedPreferences prefs;
    TileGame game;
    BoardScreen screen;
    String page="splash", pendingMode="classic";
    int selectedHistoryIndex=0;
    int chartMetric=0;
    int best, plays, total, wins, recordTile, totalMerges, streak, theme, tileStyle, animation=1;
    int todayMerges, todayPoints, playDayStreak, lastScore, stars, rewardFlags;
    String lastDaily="", statsDay="", lastPlayDay="", dailyRewardDate="";
    boolean sound=true, vibration=true, notifications=false;
    static final int NOTIFICATION_PERMISSION_REQUEST=704;
    float pulse=1f;
    ToneGenerator tones;
    final NumberFormat number=NumberFormat.getIntegerInstance(new Locale("hr","HR"));

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(CREAM);
        getWindow().setNavigationBarColor(CREAM);
        if(Build.VERSION.SDK_INT>=26)getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs=getSharedPreferences("brojko",MODE_PRIVATE);
        best=prefs.getInt("best",0);plays=prefs.getInt("games",0);total=prefs.getInt("total",0);
        lastScore=prefs.getInt("lastScore",0);
        stars=prefs.getInt("stars",0);rewardFlags=prefs.getInt("rewardFlags",0);
        wins=prefs.getInt("wins",0);recordTile=prefs.getInt("tile",0);
        totalMerges=prefs.getInt("totalMerges",0);streak=prefs.getInt("streak",0);
        lastDaily=prefs.getString("lastDaily","");
        dailyRewardDate=prefs.getString("dailyRewardDate","");
        statsDay=prefs.getString("statsDay","");
        todayMerges=prefs.getInt("todayMerges",0);todayPoints=prefs.getInt("todayPoints",0);
        playDayStreak=prefs.getInt("playDayStreak",0);
        lastPlayDay=prefs.getString("lastPlayDay","");ensureDay();
        theme=prefs.getInt("theme",0);
        tileStyle=prefs.getInt("style",0);animation=prefs.getInt("animation",1);
        sound=prefs.getBoolean("sound",true);vibration=prefs.getBoolean("vibrate",true);
        notifications=prefs.getBoolean("notifications",false);
        if(notifications)AppReminder.configure(this,true);
        try {
            String saved=prefs.getString("session","");
            if(!saved.isEmpty())game=TileGame.fromJson(new JSONObject(saved));
            else migrateLegacy();
        }catch(Exception ignored){game=null;}
        screen=new BoardScreen(this);setContentView(screen);
        screen.postDelayed(()->{
            if("splash".equals(page))navigate("home");
        },1350);
        screen.setFocusable(true);
        screen.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        screen.setContentDescription("Brojko, igra spajanja brojeva. Povucite prstom po ploči ili koristite strelice na tipkovnici.");
        if(Build.VERSION.SDK_INT>=35){
            screen.setOnApplyWindowInsetsListener((v,insets)->{
                android.graphics.Insets safe=insets.getInsets(
                    android.view.WindowInsets.Type.systemBars()|
                    android.view.WindowInsets.Type.displayCutout());
                v.setPadding(safe.left,safe.top,safe.right,safe.bottom);
                return insets;
            });
            screen.requestApplyInsets();
        }
        updateBars();
    }
    void migrateLegacy(){
        try{
            String old=prefs.getString("board","");
            if(old.isEmpty())return;
            JSONObject s=new JSONObject(old);
            JSONArray arr=s.getJSONArray("cells");
            int n=s.getInt("n");
            if(n!=4&&n!=5||arr.length()!=n*n)return;
            game=new TileGame(n==5?"relaxed":"classic","",new java.util.Random().nextLong());
            for(int i=0;i<arr.length();i++)game.cells[i]=arr.getInt(i);
            game.score=s.optInt("score");game.peak=game.maxTile();
            prefs.edit().remove("board").apply();persist();
        }catch(Exception ignored){game=null;}
    }
    String today(){return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());}
    void ensureDay(){
        if(!today().equals(statsDay)){
            statsDay=today();todayMerges=0;todayPoints=0;
            // Daily rewards may be earned once per local calendar day.
            rewardFlags &= ~3;
        }
    }
    void reward(int bit,int amount,boolean reached){
        if(reached&&(rewardFlags&bit)==0){
            rewardFlags |= bit;stars=Math.max(0,stars+amount);
        }
    }
    void updateRewards(){
        reward(1,50,todayMerges>=20);
        reward(2,100,todayPoints>=1000);
        reward(4,150,playDayStreak>=7);
        reward(8,500,recordTile>=1024);
        reward(16,1000,best>=50000);
    }
    String yesterday(){
        Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,-1);
        return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(c.getTime());
    }
    String compactDate(){return new SimpleDateFormat("dd.MM.yyyy.",new Locale("hr","HR")).format(new Date());}
    String fmt(int x){return number.format(x);}
    String labelMode(String m) {
        return "relaxed".equals(m)?"Opušteno 5×5":"daily".equals(m)?"Dnevni izazov":"Klasično 4×4";
    }
    void updateBars() {
        boolean dark=theme==1;
        int base=dark?0xff14233a:CREAM;
        getWindow().setStatusBarColor(base);getWindow().setNavigationBarColor(base);
        int flags=dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    void persist() {
        SharedPreferences.Editor e=prefs.edit()
            .putInt("best",best).putInt("games",plays).putInt("total",total)
            .putInt("lastScore",lastScore)
            .putInt("stars",stars).putInt("rewardFlags",rewardFlags)
            .putInt("wins",wins).putInt("tile",recordTile).putInt("totalMerges",totalMerges)
            .putInt("streak",streak).putString("lastDaily",lastDaily)
            .putString("dailyRewardDate",dailyRewardDate)
            .putInt("todayMerges",todayMerges).putInt("todayPoints",todayPoints)
            .putString("statsDay",statsDay).putInt("playDayStreak",playDayStreak)
            .putString("lastPlayDay",lastPlayDay)
            .putInt("theme",theme).putInt("style",tileStyle).putInt("animation",animation)
            .putBoolean("sound",sound).putBoolean("vibrate",vibration)
            .putBoolean("notifications",notifications);
        if(game!=null)e.putString("session",game.toJson().toString());
        else e.remove("session");
        e.apply();
    }
    void navigate(String target) {
        if(!today().equals(statsDay)){ensureDay();persist();}
        if("game".equals(target)&&game==null){newGame("classic");return;}
        page=target;updateBars();
        screen.setContentDescription("Brojko — "+target+
            (game==null?"":". Rezultat "+fmt(game.score)));
        screen.invalidate();
        screen.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
    void requestNew(String mode){
        if(game!=null&&!game.gameOver&&!game.archived&&game.moves>0){
            pendingMode=mode;navigate("confirm");
        }else newGame(mode);
    }
    void archive(){
        if(game==null||game.archived||game.moves==0)return;
        game.archived=true;
        lastScore=game.score;
        try {
            JSONObject row=new JSONObject();
            row.put("score",game.score).put("moves",game.moves).put("peak",game.maxTile())
                .put("mode",game.mode).put("day",compactDate()).put("won",game.reached2048);
            JSONArray old=new JSONArray(prefs.getString("history","[]"));
            JSONArray recent=new JSONArray();recent.put(row);
            for(int i=0;i<Math.min(19,old.length());i++)recent.put(old.getJSONObject(i));
            prefs.edit().putString("history",recent.toString()).apply();
        }catch(Exception ignored){}
    }
    void newGame(String mode) {
        archive();
        long seed="daily".equals(mode)?
            (0x3BAF31248EA76C1DL ^ (long)today().hashCode()*0x9E3779B97F4A7C15L):
            new java.util.Random().nextLong();
        game=new TileGame(mode,"daily".equals(mode)?today():"",seed);
        ensureDay();
        if(!today().equals(lastPlayDay)){
            playDayStreak=yesterday().equals(lastPlayDay)?playDayStreak+1:1;
            lastPlayDay=today();
        }
        plays++;recordTile=Math.max(recordTile,game.maxTile());
        updateRewards();
        page="game";persist();screen.invalidate();
    }
    void toggleNotifications(){
        if(notifications){
            notifications=false;
            AppReminder.configure(this,false);
            persist();screen.invalidate();
            return;
        }
        if(Build.VERSION.SDK_INT>=33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=
                PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST);
            return;
        }
        notifications=true;
        AppReminder.configure(this,true);
        persist();screen.invalidate();
    }
    @Override public void onRequestPermissionsResult(int code,
             String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(code,permissions,grantResults);
        if(code==NOTIFICATION_PERMISSION_REQUEST){
            notifications=grantResults.length>0 &&
                grantResults[0]==PackageManager.PERMISSION_GRANTED;
            AppReminder.configure(this,notifications);
            persist();screen.invalidate();
            if(!notifications)Toast.makeText(this,
                "Obavijesti nisu dopuštene u postavkama Androida.",
                Toast.LENGTH_LONG).show();
        }
    }
    void soundEffect(){
        if(sound)try{
            if(tones==null)tones=new ToneGenerator(AudioManager.STREAM_MUSIC,35);
            tones.startTone(ToneGenerator.TONE_PROP_BEEP2,55);
        }catch(Exception ignored){}
        if(vibration)try{
            Vibrator v=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            if(v!=null&&v.hasVibrator()) {
                if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(18,55));
                else v.vibrate(18);
            }
        }catch(Exception ignored){}
    }
    void move(int direction) {
        if(!page.equals("game")||game==null)return;
        boolean dailyWasComplete=game.isDailyGoalComplete() && today().equals(game.day);
        TileGame.Result result=game.move(direction);
        if(!result.changed){
            if(result.lost){page="over";persist();screen.invalidate();}
            return;
        }
        ensureDay();total+=result.points;totalMerges+=result.merges;
        todayPoints+=result.points;todayMerges+=result.merges;
        best=Math.max(best,game.score);recordTile=Math.max(recordTile,game.peak);
        if(result.first2048)wins++;
        updateRewards();
        boolean daily="daily".equals(game.mode)&&today().equals(game.day);
        boolean dailyComplete=daily && game.isDailyGoalComplete();
        if(dailyComplete&&!today().equals(lastDaily)){
            streak=yesterday().equals(lastDaily)?streak+1:1;
            lastDaily=today();
        }
        if(dailyComplete&&!today().equals(dailyRewardDate)){
            dailyRewardDate=today();
            stars+=250;
        }
        soundEffect();
        screen.setContentDescription("Brojko, rezultat "+fmt(game.score)+
            ", najveća pločica "+fmt(game.peak)+", potez "+game.moves);
        if(dailyComplete && !dailyWasComplete) {
            page="dailyWin";
        }else if(daily && game.isDailyOutOfMoves()){
            game.gameOver=true;
            page="dailyFail";
        }else if(result.first2048)page="win";
        else if(result.lost){page="over";}
        persist();animate();
    }
    void endSession(){
        if(game!=null)archive();
        game=null;page="home";persist();screen.invalidate();
    }
    void undo() {
        if(game==null||!game.canUndo())return;
        ensureDay();
        int mergeCount=game.merges;
        boolean hadWon=game.reached2048;
        int reverted=game.undo();
        total=Math.max(0,total-reverted);
        if(today().equals(statsDay))todayPoints=Math.max(0,todayPoints-reverted);
        int revertedMerges=mergeCount-game.merges;
        totalMerges=Math.max(0,totalMerges-revertedMerges);
        if(today().equals(statsDay))todayMerges=Math.max(0,todayMerges-revertedMerges);
        if(hadWon&&!game.reached2048)wins=Math.max(0,wins-1);
        page="game";persist();screen.invalidate();
    }
    void animate(){
        if(animation==0){screen.invalidate();return;}
        ValueAnimator a=ValueAnimator.ofFloat(0.90f,1f);
        a.setDuration(animation==2?185:115);
        a.addUpdateListener(v->{pulse=(float)v.getAnimatedValue();screen.invalidate();});
        a.start();
    }
    void share() {
        String value=game==null?fmt(best):fmt(game.score);
        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,"U igri Brojko osvojio/la sam "+value
            +" bodova! Spajaj brojeve. Ruši rekorde. — Brojko by Brendigo");
        startActivity(Intent.createChooser(i,"Podijeli rezultat"));
    }

    private static final int EXPORT_BACKUP=601, IMPORT_BACKUP=602;
    private static final String BACKUP_FORMAT="brendigo.brojko.backup.v1";
    private static final String[] BACKUP_KEYS={
        "best","games","total","wins","tile","totalMerges","streak",
        "stars","rewardFlags","lastScore",
        "lastDaily","dailyRewardDate","statsDay","todayMerges","todayPoints","playDayStreak",
        "lastPlayDay","theme","style","animation","sound","vibrate","notifications",
        "session","history"
    };
    String appVersion(){
        try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}
        catch(Exception ignored){return "1.1";}
    }
    void chooseBackupFile(boolean exporting){
        Intent i=new Intent(exporting?Intent.ACTION_CREATE_DOCUMENT:Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/json");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if(exporting)i.putExtra(Intent.EXTRA_TITLE,"Brojko-backup-"+today()+".json");
        try{startActivityForResult(i,exporting?EXPORT_BACKUP:IMPORT_BACKUP);}
        catch(Exception ex){Toast.makeText(this,"Nije moguće otvoriti datoteke.",Toast.LENGTH_LONG).show();}
    }
    byte[] getBackupBytes() throws Exception{
        JSONObject root=new JSONObject();
        root.put("format",BACKUP_FORMAT);
        root.put("created",today());
        root.put("package","com.brendigo.brojko");
        JSONObject data=new JSONObject();
        java.util.Map<String,?> stored=prefs.getAll();
        for(String key:BACKUP_KEYS){
            Object value=stored.get(key);
            if(value!=null)data.put(key,value);
        }
        root.put("data",data);
        return root.toString(2).getBytes(StandardCharsets.UTF_8);
    }
    void readBackupBytes(byte[] bytes) throws Exception{
        if(bytes.length>512000)throw new IllegalArgumentException("Datoteka je prevelika.");
        JSONObject root=new JSONObject(new String(bytes,StandardCharsets.UTF_8));
        if(!BACKUP_FORMAT.equals(root.optString("format")))
            throw new IllegalArgumentException("Nepodržani oblik sigurnosne kopije.");
        JSONObject data=root.getJSONObject("data");
        if(data.has("session")){
            TileGame restored=TileGame.fromJson(new JSONObject(data.getString("session")));
            if(restored==null)throw new IllegalArgumentException("Oštećena spremljena partija.");
        }
        if(data.has("history")){
            JSONArray history=new JSONArray(data.getString("history"));
            if(history.length()>200)throw new IllegalArgumentException("Oštećena povijest partija.");
        }
        for(String key:BACKUP_KEYS){
            if(!data.has(key))continue;
            Object value=data.get(key);
            Object expected=prefs.getAll().get(key);
            if(value instanceof Integer){
                if(((Integer)value)<0&&!key.equals("theme"))throw new IllegalArgumentException("Nevaljani podatci.");
            }else if(!(value instanceof String) && !(value instanceof Boolean))
                throw new IllegalArgumentException("Nevaljani podatci.");
            if(key.equals("theme")||key.equals("style")||key.equals("animation")){
                if(!(value instanceof Integer)||((Integer)value)>2||((Integer)value)<0)
                    throw new IllegalArgumentException("Nevaljane postavke.");
            }
            if((key.equals("sound")||key.equals("vibrate")||
                key.equals("notifications"))&&!(value instanceof Boolean))
                throw new IllegalArgumentException("Nevaljane postavke.");
        }
        SharedPreferences.Editor editor=prefs.edit().clear();
        for(String key:BACKUP_KEYS){
            if(!data.has(key))continue;
            Object value=data.get(key);
            if(value instanceof Integer)editor.putInt(key,(Integer)value);
            else if(value instanceof Boolean)editor.putBoolean(key,(Boolean)value);
            else editor.putString(key,(String)value);
        }
        if(!editor.commit())throw new IllegalStateException("Neuspješno spremanje.");
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=Activity.RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{
            if(requestCode==EXPORT_BACKUP){
                byte[] bytes=getBackupBytes();
                try(OutputStream stream=getContentResolver().openOutputStream(uri)){
                    if(stream==null)throw new IllegalStateException("Datoteka nije dostupna.");
                    stream.write(bytes);
                }
                Toast.makeText(this,"Sigurnosna kopija spremljena.",Toast.LENGTH_LONG).show();
            }else if(requestCode==IMPORT_BACKUP){
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                try(InputStream input=getContentResolver().openInputStream(uri)){
                    if(input==null)throw new IllegalStateException("Datoteka nije dostupna.");
                    byte[] buffer=new byte[8192];int count;
                    while((count=input.read(buffer))!=-1){
                        bytes.write(buffer,0,count);
                        if(bytes.size()>512000)throw new IllegalArgumentException("Datoteka je prevelika.");
                    }
                }
                readBackupBytes(bytes.toByteArray());
                Toast.makeText(this,"Sigurnosna kopija vraćena.",Toast.LENGTH_LONG).show();
                recreate();
            }
        }catch(Exception err){
            Toast.makeText(this,"Pogreška: "+err.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    @Override public boolean onKeyDown(int keyCode,android.view.KeyEvent event){
        if("game".equals(page)){
            switch(keyCode){
                case android.view.KeyEvent.KEYCODE_DPAD_LEFT:move(TileGame.LEFT);return true;
                case android.view.KeyEvent.KEYCODE_DPAD_RIGHT:move(TileGame.RIGHT);return true;
                case android.view.KeyEvent.KEYCODE_DPAD_UP:move(TileGame.UP);return true;
                case android.view.KeyEvent.KEYCODE_DPAD_DOWN:move(TileGame.DOWN);return true;
                default:break;
            }
        }
        return super.onKeyDown(keyCode,event);
    }
    @Override public void onBackPressed(){
        if(page.equals("game"))navigate("pause");
        else if(page.equals("pause"))navigate("game");
        else if(page.equals("historyDetail"))navigate("stats");
        else if(page.equals("privacy")||page.equals("about")||
            page.equals("language")||page.equals("backup")||page.equals("help"))
            navigate("settings");
        else if(page.equals("confirm")||page.equals("confirmEnd"))navigate(game==null?"home":"game");
        else if(page.equals("win")||page.equals("over")||page.equals("dailyWin")||page.equals("dailyFail"))navigate("home");
        else if(page.equals("splash"))navigate("home");
        else if(page.equals("home"))super.onBackPressed();
        else navigate("home");
    }
    @Override protected void onPause(){persist();super.onPause();}
    @Override protected void onDestroy(){if(tones!=null){tones.release();tones=null;}super.onDestroy();}

    final class BoardScreen extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        final ArrayList<Tap> taps=new ArrayList<>();
        Canvas c;float scale,offX,offY,touchX,touchY;
        float scrollOffset,maxScroll,lastFingerY,initialFingerY;
        String renderedPage="";
        boolean pointerBoard,draggingScroll;
        int ink,muted,bg,card,shadow;
        final Typeface regularFont, boldFont, displayFont;
        Typeface makeTypeface(Typeface base,int weight){
            if(Build.VERSION.SDK_INT>=28)return Typeface.create(base,weight,false);
            return Typeface.create(base,weight>=600?Typeface.BOLD:Typeface.NORMAL);
        }
        class Tap {
            float left,top,right,bottom;Runnable action;String label;
            Tap(float l,float t,float r,float b,Runnable a) {
                this(l,t,r,b,"Aktiviraj",a);
            }
            Tap(float l,float t,float r,float b,String caption,Runnable a){
                left=l;top=t;right=r;bottom=b;action=a;
                label=caption==null||caption.isEmpty()?"Akcija":caption;
            }
        }
        BoardScreen(Context context){
            super(context);
            Typeface base;
            try{base=Typeface.createFromAsset(getContext().getAssets(),"Nunito.ttf");}
            catch(RuntimeException error){base=Typeface.create("sans-serif-rounded",Typeface.NORMAL);}
            if(Build.VERSION.SDK_INT>=26){
                Typeface normal=null,heavy=null,display=null;
                try{
                    normal=new Typeface.Builder(getContext().getAssets(),"Nunito.ttf")
                        .setFontVariationSettings("'wght' 500").setWeight(500).build();
                    heavy=new Typeface.Builder(getContext().getAssets(),"Nunito.ttf")
                        .setFontVariationSettings("'wght' 750").setWeight(750).build();
                    display=new Typeface.Builder(getContext().getAssets(),"Nunito.ttf")
                        .setFontVariationSettings("'wght' 900").setWeight(900).build();
                }catch(RuntimeException ignored){}
                regularFont=normal!=null?normal:makeTypeface(base,500);
                boldFont=heavy!=null?heavy:makeTypeface(base,750);
                displayFont=display!=null?display:makeTypeface(base,900);
            }else{
                regularFont=makeTypeface(base,500);
                boldFont=makeTypeface(base,750);
                displayFont=makeTypeface(base,900);
            }
        }
        void palette() {
            boolean dark=theme==1;
            bg=dark?0xff14233a:CREAM;
            card=dark?0xff263653:0xfffffdfa;
            ink=dark?0xffeff6ff:INK;
            muted=dark?0xffaabbce:MUTED;
            shadow=dark?0xff0b1829:0xffe8dace;
        }
        void shape(float x,float y,float w,float h,float radius,int color) {
            p.clearShadowLayer();p.setStyle(Paint.Style.FILL);p.setShader(null);
            p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,radius,radius,p);
        }
        void card(float x,float y,float w,float h,float radius,int color) {
            // One elevation/outline treatment for ALL selectable cards.
            shape(x,y+6,w,h,radius,theme==1?0xff0d192b:0xffe9d9c7);
            shape(x+1,y+3,w-2,h,radius,shadow);
            gradient(x,y,w,h,radius,lighten(color,theme==1?.10f:.07f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1f);p.setColor(theme==1?0x30ffffff:0x85ffffff);
            c.drawRoundRect(x+1,y+1,x+w-1,y+h-1,Math.max(1,radius-1),
                Math.max(1,radius-1),p);
            p.setStyle(Paint.Style.FILL);
        }
        void gradient(float x,float y,float w,float h,float radius,int top,int bottom) {
            p.clearShadowLayer();p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(x,y,x,y+h,top,bottom,Shader.TileMode.CLAMP));
            c.drawRoundRect(x,y,x+w,y+h,radius,radius,p);p.setShader(null);
        }
        int lighten(int color,float amount) {
            int r=Color.red(color),g=Color.green(color),b=Color.blue(color);
            return Color.rgb(Math.min(255,(int)(r+(255-r)*amount)),
                Math.min(255,(int)(g+(255-g)*amount)),Math.min(255,(int)(b+(255-b)*amount)));
        }
        int darken(int color,float amount){
            return Color.rgb(Math.max(0,(int)(Color.red(color)*(1-amount))),
                Math.max(0,(int)(Color.green(color)*(1-amount))),
                Math.max(0,(int)(Color.blue(color)*(1-amount))));
        }
        void glossyTile(float x,float y,float width,float height,float radius,int color){
            if(width<=0||height<=0)return;
            if(tileStyle==1){shape(x,y,width,height,radius,color);return;}
            float depth=Math.min(5,Math.max(2,height*.075f));
            shape(x,y+depth,width,Math.max(1,height-depth),radius,
                darken(color,.22f));
            gradient(x,y,width,Math.max(1,height-depth),radius,
                lighten(color,tileStyle==2?.32f:.24f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(.6f,Math.min(1.7f,width*.025f)));
            p.setColor(0xb0ffffff);
            c.drawRoundRect(x+1.5f,y+1.5f,x+width-1.5f,
                y+height-depth-1.3f,Math.max(2,radius-1.5f),
                Math.max(2,radius-1.5f),p);
            p.setStyle(Paint.Style.FILL);
            if(width>=24&&height>=24){
                shape(x+6,y+3,Math.max(1,width-12),
                    Math.max(1,height*.055f),radius,0x38ffffff);
            }
            if(tileStyle==2 && width>=26 && height>=26){
                shape(x+7,y+8,Math.max(1,width-14),
                    Math.max(1,height*.12f),radius,0x25ffffff);
            }
        }
        void rays(float cx,float cy,float size,int color){
            for(int i=0;i<3;i++){
                c.save();
                c.rotate(-37+i*37,cx,cy);
                shape(cx-2,cy-size*.85f,4,size*.24f,3,color);
                c.restore();
            }
        }
        void txt(String content,float x,float baseline,float size,int color,boolean bold) {
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTypeface(bold?(size>=26?displayFont:boldFont):regularFont);
            // Nunito's own wght axis supplies the stroke thickness.
            // FakeBold/FILL_AND_STROKE distorted Croatian glyphs in v1.7.25.
            p.setFakeBoldText(false);
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(0);
            p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);
            if(bold&&size>=25&&color==ink) p.setShadowLayer(.75f,0,1,
                theme==1?0x44000000:0x150c1d3e);
            c.drawText(content,x,baseline,p);
            p.clearShadowLayer();p.setFakeBoldText(false);
            p.setStrokeWidth(0);p.setStyle(Paint.Style.FILL);
        }
        void fitted(String content,float x,float baseline,float size,float maxWidth,int color,boolean bold) {
            p.setTypeface(bold?(size>=26?displayFont:boldFont):regularFont);
            p.setFakeBoldText(false);
            p.setTextSize(size);
            while(p.measureText(content)>maxWidth && size>10){size-=1;p.setTextSize(size);}
            txt(content,x,baseline,size,color,bold);
        }
        void polishedIcon(String type,float cx,float cy,float width,int color){
            c.save();c.translate(cx-width/2f,cy-width/2f);
            c.scale(width/24f,width/24f);
            p.setStyle(Paint.Style.FILL);p.setShader(null);
            if("crown".equals(type)){
                Path crown=new Path();
                crown.moveTo(1,8);crown.lineTo(7,13);crown.lineTo(12,3);
                crown.lineTo(17,13);crown.lineTo(23,8);crown.lineTo(20,20);
                crown.lineTo(4,20);crown.close();
                c.save();c.translate(0,1.6f);p.setColor(0xffd77e08);c.drawPath(crown,p);
                c.restore();
                p.setShader(new LinearGradient(0,3,0,20,0xffffe36a,
                    color==WHITE?0xfff5c036:color,Shader.TileMode.CLAMP));
                c.drawPath(crown,p);p.setShader(null);
                p.setColor(0xffffe584);
                c.drawCircle(1.5f,7.7f,2.1f,p);
                c.drawCircle(12,3.3f,2.1f,p);
                c.drawCircle(22.5f,7.7f,2.1f,p);
                p.setColor(0xffffa91f);c.drawCircle(12,15.5f,2,p);
            }else if("gamepad".equals(type)){
                Path pad=new Path();
                pad.moveTo(6,6);pad.cubicTo(8,4,16,4,18,6);
                pad.cubicTo(21,8,24,17,22,20);
                pad.cubicTo(20,23,17,17,15,17);
                pad.lineTo(9,17);pad.cubicTo(7,17,4,23,2,20);
                pad.cubicTo(0,17,3,8,6,6);pad.close();
                p.setColor(color);c.drawPath(pad,p);
                p.setColor(WHITE);c.drawRoundRect(6,9,8,15,1,1,p);
                c.drawRoundRect(4,11,10,13,1,1,p);
                c.drawCircle(16,10,1.5f,p);c.drawCircle(19,13,1.5f,p);
            }else if("grid".equals(type)){
                p.setColor(color);
                for(int row=0;row<2;row++)
                    for(int col=0;col<2;col++)
                        c.drawRoundRect(2+col*11,2+row*11,11+col*11,
                            11+row*11,2.5f,2.5f,p);
            }else if("star".equals(type)){
                Path star=new Path();
                for(int i=0;i<10;i++){
                    double angle=-Math.PI/2+i*Math.PI/5;
                    float rr=i%2==0?11f:5.3f;
                    float px=12+(float)Math.cos(angle)*rr;
                    float py=12+(float)Math.sin(angle)*rr;
                    if(i==0)star.moveTo(px,py);else star.lineTo(px,py);
                }
                star.close();p.setColor(color);c.drawPath(star,p);
            }else if("flame".equals(type)){
                Path flame=new Path();
                flame.moveTo(12,1);
                flame.cubicTo(15,6,15,8,17,10);
                flame.cubicTo(18,7,19,6,19,5);
                flame.cubicTo(25,12,23,17,20,21);
                flame.cubicTo(16,25,8,25,4,20);
                flame.cubicTo(0,15,5,8,12,1);
                flame.close();p.setColor(color);c.drawPath(flame,p);
                Path core=new Path();core.moveTo(12,12);
                core.cubicTo(17,17,17,19,14,22);
                core.cubicTo(11,24,8,21,9,18);
                core.close();p.setColor(0x70ffffff);c.drawPath(core,p);
            }else if("stats".equals(type)){
                int[] fills={0xff8196b8,0xff647d9f,0xff243c66};
                float[] hs={8,13,19};
                for(int i=0;i<3;i++){
                    float x=1+i*8, h=hs[i];
                    p.setColor(0x38000000);
                    c.drawRoundRect(x,23-h,x+6,24,2,2,p);
                    p.setColor(color==ink?fills[i]:lighten(color,(2-i)*.17f));
                    c.drawRoundRect(x,21-h,x+6,22,2,2,p);
                }
            }else if("target".equals(type)){
                p.setColor(0xffee6650);c.drawCircle(11.5f,12.8f,11,p);
                p.setColor(WHITE);c.drawCircle(11.5f,12.2f,7.6f,p);
                p.setColor(0xffff8a71);c.drawCircle(11.5f,12.2f,4.6f,p);
                p.setColor(WHITE);c.drawCircle(11.5f,12.2f,2.2f,p);
                Path arrow=new Path();arrow.moveTo(11.3f,12.3f);
                arrow.lineTo(22,1.2f);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);
                p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xff233b64);
                c.drawPath(arrow,p);p.setStyle(Paint.Style.FILL);
                p.setColor(0xff2c80df);
                Path head=new Path();head.moveTo(18.5f,2);
                head.lineTo(22.7f,0);head.lineTo(24,5);head.lineTo(20.3f,5.5f);
                head.close();c.drawPath(head,p);
            }else if("palette".equals(type)){
                p.setColor(0xfff3d9d1);c.drawCircle(12,12.8f,11,p);
                p.setColor(0xffffa339);c.drawCircle(12,4.7f,2.5f,p);
                p.setColor(0xffff6752);c.drawCircle(5.5f,10.5f,2.7f,p);
                p.setColor(0xff489ef4);c.drawCircle(17.9f,11,2.7f,p);
                p.setColor(0xff37caa0);c.drawCircle(9.6f,18.3f,2.7f,p);
                p.setColor(0xfffef9f1);c.drawCircle(17.2f,18.9f,2.6f,p);
            }else if("settings".equals(type)){
                Path gear=new Path();
                for(int i=0;i<24;i++){
                    double angle=-Math.PI/2+i*Math.PI/12;
                    float radius=i%3==0||i%3==1?11:8.4f;
                    float x=12+(float)Math.cos(angle)*radius;
                    float y=12+(float)Math.sin(angle)*radius;
                    if(i==0)gear.moveTo(x,y);else gear.lineTo(x,y);
                }
                gear.close();
                p.setColor(color==ink?0xff172b4f:color);
                c.drawPath(gear,p);
                p.setColor(theme==1?0xff263653:CREAM);
                c.drawCircle(12,12,4.1f,p);
            }else if("calendar".equals(type)){
                p.setColor(0xffeddaca);c.drawRoundRect(3,4,22,24,4,4,p);
                p.setColor(WHITE);c.drawRoundRect(2,3,21,22,4,4,p);
                p.setColor(0xffff7156);c.drawRoundRect(2,3,21,10,4,4,p);
                p.setColor(0xff243653);
                c.drawRoundRect(6,0,9,7,2,2,p);
                c.drawRoundRect(15,0,18,7,2,2,p);
                Path star=new Path();
                for(int i=0;i<10;i++){
                    double angle=-Math.PI/2+i*Math.PI/5;
                    float rr=i%2==0?6.4f:3.1f;
                    float x=11.5f+(float)Math.cos(angle)*rr;
                    float y=15.8f+(float)Math.sin(angle)*rr;
                    if(i==0)star.moveTo(x,y);else star.lineTo(x,y);
                }
                star.close();p.setColor(0xffffaf1f);c.drawPath(star,p);
            }else if("bell".equals(type)){
                p.setColor(color);
                Path bell=new Path();bell.moveTo(5,17);bell.lineTo(7,15);
                bell.lineTo(7,10);bell.cubicTo(7,3,17,3,17,10);
                bell.lineTo(17,15);bell.lineTo(19,17);bell.close();
                c.drawPath(bell,p);
                c.drawCircle(12,20,2,p);
            }else if("globe".equals(type)){
                p.setColor(color);p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2.0f);
                c.drawCircle(12,12,9,p);
                c.drawOval(8,3,16,21,p);
                c.drawLine(3,12,21,12,p);
                c.drawLine(5,7,19,7,p);
                c.drawLine(5,17,19,17,p);
            }else if("info".equals(type)){
                p.setColor(color);
                c.drawCircle(12,12,10,p);
                p.setColor(0xff478de5);
                c.drawCircle(12,6.7f,1.5f,p);
                c.drawRoundRect(10.9f,10,13.1f,18,1,1,p);
            }else if("play".equals(type)){
                Path tri=new Path();tri.moveTo(5,1.5f);tri.quadTo(4,0,7,2);
                tri.lineTo(21,10.5f);tri.quadTo(23,12,21,13.5f);
                tri.lineTo(7,22);tri.quadTo(4,24,5,21);tri.close();
                c.save();c.translate(0,1.4f);p.setColor(0x30000000);c.drawPath(tri,p);c.restore();
                p.setColor(color);c.drawPath(tri,p);
            }else if("pause".equals(type)){
                p.setColor(color);
                c.drawRoundRect(4,2,10,22,3,3,p);
                c.drawRoundRect(14,2,20,22,3,3,p);
            }else if("check".equals(type)){
                Path check=new Path();check.moveTo(2,12);check.lineTo(9,20);check.lineTo(22,3);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);
                p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(color);c.drawPath(check,p);
            }
            p.setShader(null);p.setStyle(Paint.Style.FILL);c.restore();
        }
        void icon(String type,float cx,float cy,float width,int color){
            if("crown".equals(type)||"stats".equals(type)||
               "gamepad".equals(type)||"grid".equals(type)||
               "star".equals(type)||"flame".equals(type)||
               "target".equals(type)||"palette".equals(type)||
               "settings".equals(type)||"calendar".equals(type)||
               "play".equals(type)||"pause".equals(type)||
               "check".equals(type)||"bell".equals(type)||
               "globe".equals(type)||"info".equals(type)){
                polishedIcon(type,cx,cy,width,color);return;
            }
            c.save();c.translate(cx-width/2f,cy-width/2f);
            c.scale(width/24f,width/24f);
            p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.7f);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            p.setShader(null);
            Path z=new Path();
            switch(type) {
                case "chevron-down":
                    z.moveTo(5,9);z.lineTo(12,16);z.lineTo(19,9);break;
                case "back":z.moveTo(16,4);z.lineTo(8,12);z.lineTo(16,20);break;
                case "refresh":
                    z.arcTo(new RectF(4,4,21,21),-40,300,false);
                    z.moveTo(20,5);z.lineTo(22,11);z.lineTo(16,10);break;
                case "undo":z.arcTo(new RectF(5,4,22,22),-100,305,false);
                    z.moveTo(6,5);z.lineTo(5,12);z.lineTo(12,11);break;
                case "play":z.moveTo(7,3);z.lineTo(21,12);z.lineTo(7,21);z.close();break;
                case "pause":z.moveTo(8,4);z.lineTo(8,20);z.moveTo(16,4);z.lineTo(16,20);break;
                case "check":z.moveTo(3,13);z.lineTo(9,19);z.lineTo(21,5);break;
                case "crown":z.moveTo(2,8);z.lineTo(7,13);z.lineTo(12,4);z.lineTo(17,13);
                    z.lineTo(22,8);z.lineTo(20,20);z.lineTo(4,20);z.close();break;
                case "stats":z.moveTo(4,20);z.lineTo(4,13);z.moveTo(11,20);
                    z.lineTo(11,8);z.moveTo(18,20);z.lineTo(18,3);break;
                case "target": c.drawCircle(12,12,9,p);c.drawCircle(12,12,4,p);returnIcon();return;
                case "settings":c.drawCircle(12,12,9,p);c.drawCircle(12,12,3,p);returnIcon();return;
                case "star":
                    for(int i=0;i<10;i++){
                        double angle=-Math.PI/2+i*Math.PI/5;
                        float radius=(i%2==0)?10:4.6f;
                        float px=12+(float)Math.cos(angle)*radius;
                        float py=12+(float)Math.sin(angle)*radius;
                        if(i==0)z.moveTo(px,py);else z.lineTo(px,py);
                    }
                    z.close();p.setStyle(Paint.Style.FILL);break;
                case "calendar":p.setStrokeWidth(2.3f);c.drawRoundRect(3,5,21,21,3,3,p);
                    z.moveTo(3,10);z.lineTo(21,10);z.moveTo(8,2);z.lineTo(8,8);
                    z.moveTo(16,2);z.lineTo(16,8);break;
                case "home":z.moveTo(2,11);z.lineTo(12,3);z.lineTo(22,11);
                    z.moveTo(5,10);z.lineTo(5,21);z.lineTo(19,21);z.lineTo(19,10);break;
                case "palette":c.drawCircle(12,12,9,p);c.drawCircle(8,9,1,p);
                    c.drawCircle(14,6,1,p);c.drawCircle(17,12,1,p);returnIcon();return;
                case "share":c.drawCircle(5,12,2,p);c.drawCircle(19,5,2,p);
                    c.drawCircle(19,19,2,p);z.moveTo(7,11);z.lineTo(17,6);
                    z.moveTo(7,13);z.lineTo(17,18);break;
                case "sound":z.moveTo(3,9);z.lineTo(8,9);z.lineTo(13,5);
                    z.lineTo(13,19);z.lineTo(8,15);z.lineTo(3,15);z.close();
                    z.moveTo(17,8);z.quadTo(23,12,17,16);break;
                case "phone":c.drawRoundRect(6,2,18,22,2,2,p);z.moveTo(10,19);
                    z.lineTo(14,19);break;
                case "shield":z.moveTo(12,2);z.lineTo(21,6);z.lineTo(20,14);
                    z.quadTo(18,19,12,22);z.quadTo(6,19,4,14);
                    z.lineTo(3,6);z.close();break;
                default: z.moveTo(5,5);z.lineTo(19,19);z.moveTo(19,5);z.lineTo(5,19);
            }
            c.drawPath(z,p);returnIcon();
        }
        void returnIcon(){p.setStyle(Paint.Style.FILL);c.restore();}
        void button(String label,String symbol,float x,float y,float w,float h,int color,
                boolean prominent,Runnable action) {
            float radius=prominent?Math.min(38,h*.43f):Math.min(23,h*.27f);
            if(prominent){
                // Elevated, pill-shaped coral CTA with warm specular highlight.
                shape(x,y+5,w,h,radius,darken(color,.23f));
                gradient(x,y,w,h-4,radius,lighten(color,.18f),color);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);
                p.setColor(0x86ffffff);
                c.drawRoundRect(x+3,y+2,x+w-3,y+h-8,radius-3,radius-3,p);
                p.setStyle(Paint.Style.FILL);
            }else card(x,y,w,h,radius,color);
            boolean hasSymbol=symbol!=null;
            int iconColor=prominent?WHITE:ink;
            if(hasSymbol&&w<150&&h>=77){
                icon(symbol,x+w/2,y+h*.30f,29,iconColor);
                fitted(label,x+w/2,y+h*.78f,21,w-14,prominent?WHITE:ink,true);
            }else{
                if(hasSymbol)icon(symbol,x+(prominent?Math.min(58,w*.21f):25),
                    y+h/2,Math.min(prominent?33:23,h*.52f),iconColor);
                float textCenter=x+(hasSymbol?(prominent?w*.58f:(w+38)/2):w/2);
                int labelColor=prominent?WHITE:ink;
                float font=prominent?Math.min(27,h*.34f):(h>=66?18:16);
                fitted(label,textCenter,y+h/2+font*.35f,font,
                    hasSymbol?(prominent?w*.76f:w-52):w-20,labelColor,true);
            }
            taps.add(new Tap(x,y,x+w,y+h,label==null||label.isEmpty()?
                (symbol==null?"Akcija":symbol):label,action));
        }
        void title(String name) {
            back();
            brand(64,38);
            fitted(name,200,127,name.length()>18?28:34,356,ink,true);
        }
        void back(){button("","back",19,43,48,48,card,false,
                ()->navigate("historyDetail".equals(page)?"stats":
                   ("privacy".equals(page)||"about".equals(page)||
                    "language".equals(page)||"backup".equals(page)||
                    "help".equals(page))?"settings":"home"));}
        void brand(float y,float size) {
            p.setTypeface(displayFont);
            p.setTextSize(size);
            float left=p.measureText("Br"),right=p.measureText("jko");
            float gap=size*.09f,tile=size*.74f;
            float total=left+right+tile+2*gap;
            float start=200-total/2;
            txt("Br",start+left/2,y,size,ink,true);
            float bx=start+left+gap;
            glossyTile(bx,y-size*.78f,tile,tile,Math.max(7,size*.16f),CORAL);
            rays(bx+tile*.5f,y-size*.79f,size*.34f,CORAL);
            txt("2",bx+tile/2,y-size*.20f,size*.52f,WHITE,true);
            txt("jko",bx+tile+gap+right/2,y,size,ink,true);
            txt("by Brendigo",265,y+23,15,muted,true);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);c=canvas;palette();
            float usableWidth=Math.max(1,getWidth()-getPaddingLeft()-getPaddingRight());
            float usableHeight=Math.max(1,getHeight()-getPaddingTop()-getPaddingBottom());
            boolean inGame=page.equals("game")||page.equals("pause")||
                page.equals("win")||page.equals("over")||page.equals("confirm")||
                page.equals("confirmEnd")||page.equals("dailyWin")||page.equals("dailyFail");
            // Themes has a compact bottom navigation within the 880-unit design.
            // Fit it to the available window INCLUDING system bar insets; only
            // genuinely taller content (statistics/history) must always scroll.
            referencePortraitLayout=usableHeight/usableWidth<1.85f;
            compactBoardLayout=inGame&&referencePortraitLayout;
            gridY=compactBoardLayout?171:222;
            boolean extended=page.equals("stats");
            // Keep buttons touch-sized on 16:9 screens; permit scrolling
            // outside the board rather than scaling the whole game to tiny size.
            boolean compact=extended || usableHeight/usableWidth<1.85f;
            scale=compact?usableWidth/400f:Math.min(usableWidth/400f,usableHeight/880f);
            float contentHeight=page.equals("stats")?1120f:880f;
            maxScroll=compact?Math.max(0,contentHeight-usableHeight/scale):0;
            if(!page.equals(renderedPage)){scrollOffset=0;renderedPage=page;}
            scrollOffset=Math.max(0,Math.min(maxScroll,scrollOffset));
            offX=getPaddingLeft()+(usableWidth-400*scale)/2f;
            offY=compact?getPaddingTop()-scrollOffset*scale:
                getPaddingTop()+(usableHeight-880*scale)/2f;
            c.drawColor(bg);c.save();c.translate(offX,offY);c.scale(scale,scale);
            taps.clear();
            switch(page) {
                case "splash":splashPage();break;
                case "home":home();break;
                case "game":gameScreen();break;
                case "pause":gameScreen();taps.clear();pauseOverlay();break;
                case "win":gameScreen();taps.clear();resultOverlay(true);break;
                case "over":gameScreen();taps.clear();resultOverlay(false);break;
                case "confirm":gameScreen();taps.clear();confirmOverlay();break;
                case "confirmEnd":gameScreen();taps.clear();confirmEndOverlay();break;
                case "dailyWin":gameScreen();taps.clear();dailyResultOverlay(true);break;
                case "dailyFail":gameScreen();taps.clear();dailyResultOverlay(false);break;
                case "daily":daily();break;
                case "missions":missions();break;
                case "stats":statistics();break;
                case "historyDetail":historyDetail();break;
                case "themes":themes();break;
                case "settings":settings();break;
                case "language":languagePage();break;
                case "backup":backupPage();break;
                case "help":helpPage();break;
                case "privacy":textPage("Privatnost",new String[]{"Brojko čuva rezultate i postavke",
                    "samo na vašem uređaju.","Igra radi bez interneta i računa.",
                    "Ne prikupljamo osobne podatke,","ne prikazujemo oglase i ne šaljemo",
                    "analitiku na poslužitelje."});break;
                case "about":textPage("O igri",new String[]{"Brojko "+appVersion()+" — by Brendigo",
                    "Spajaj jednake pločice i osvoji 2048.",
                    "Povuci gore, dolje, lijevo ili desno.",
                    "Dostupni su način 4×4, 5×5",
                    "te poseban dnevni izazov.","brendigo.com"});break;
                default:home();
            }
            c.restore();
        }
        void bottomBrand(){txt("BROJKO  •  BY BRENDIGO",200,852,12,muted,true);}
        void splashPage(){
            // Programmatic branded splash based on the provided portrait reference.
            c.save();c.rotate(-19,59,140);
            glossyTile(-18,74,133,124,25,TILES[1]);
            txt("2",49,157,64,WHITE,true);c.restore();
            c.save();c.rotate(21,353,142);
            glossyTile(288,79,129,121,24,TILES[2]);
            txt("4",353,155,64,WHITE,true);c.restore();
            c.save();c.rotate(-22,28,283);
            glossyTile(-34,246,107,102,23,TILES[3]);
            txt("8",20,316,57,WHITE,true);c.restore();
            c.save();c.rotate(18,379,297);
            glossyTile(329,247,111,109,22,TILES[4]);
            txt("16",383,324,49,WHITE,true);c.restore();
            c.save();c.rotate(17,23,661);
            glossyTile(-34,610,112,111,25,TILES[7]);
            txt("64",21,679,46,WHITE,true);c.restore();
            c.save();c.rotate(-15,374,713);
            glossyTile(327,666,118,111,23,TILES[8]);
            txt("256",386,734,38,WHITE,true);c.restore();
            brand(487,77);
            card(26,747,348,77,32,theme==1?card:0xfffff0db);
            fitted("Spajaj brojeve.",128,793,22,192,CORAL,true);
            fitted("Ruši rekorde.",290,793,22,182,TEAL,true);
        }
        void home(){
            // Geometry follows the user's original nine-by-sixteen design.
            c.save();c.rotate(-16,35,76);
            glossyTile(-3,4,81,77,16,TILES[1]);
            txt("2",38,57,42,WHITE,true);c.restore();
            c.save();c.rotate(15,365,56);
            glossyTile(327,6,77,75,16,TILES[2]);
            txt("4",366,56,40,WHITE,true);c.restore();
            c.save();c.rotate(-12,11,176);
            glossyTile(-37,144,69,66,14,TILES[3]);
            txt("8",0,185,35,WHITE,true);c.restore();
            c.save();c.rotate(9,390,179);
            glossyTile(360,149,76,69,14,TILES[4]);
            txt("16",392,191,31,WHITE,true);c.restore();
            brand(197,72);
            fitted("Spajaj brojeve. Ruši rekorde.",200,245,22,367,CORAL,true);
            card(21,269,171,80,19,card);
            card(207,269,171,80,19,card);
            icon("crown",53,300,28,0xffffb228);
            txt("Najbolji rezultat",132,298,13,muted,true);
            fitted(fmt(best),128,329,27,123,ink,true);
            icon("stats",236,300,27,0xff63799d);
            txt("Zadnji rezultat",319,298,13,muted,true);
            fitted(fmt(lastScore),311,329,27,125,ink,true);
            boolean resumable=game!=null&&!game.archived&&!game.gameOver;
            // Reference uses 'Nova igra'; preserve resume through existing session.
            button(resumable?"Nastavi igru":"Nova igra","play",
                43,372,314,86,CORAL,true,
                ()->{if(resumable)navigate("game");else requestNew("classic");});
            // Decorative coral rays around the primary button.
            rays(24,408,21,CORAL);rays(376,408,21,CORAL);
            modeCard("Klasično","4 × 4",15,483,117,187,0xffffe9de,false,()->requestNew("classic"));
            modeCard("Opušteno","5 × 5",141,483,117,187,0xffddf9f5,false,()->requestNew("relaxed"));
            modeCard("Dnevni izazov","Novi izazov svaki dan!",267,483,117,187,
                0xfffff4d7,true,()->navigate("daily"));
            shortcut("Misije","target","Ispuni ciljeve","i osvoji nagrade!",
                16,687,88,144,card,()->navigate("missions"));
            shortcut("Statistika","stats","Prati svoj","napredak.",
                109,687,88,144,card,()->navigate("stats"));
            shortcut("Teme","palette","Otkrij nove","izglede.",
                202,687,88,144,card,()->navigate("themes"));
            shortcut("Postavke","settings","Prilagodi","igru sebi.",
                295,687,88,144,card,()->navigate("settings"));
        }
        void shortcut(String label,String glyph,String sub1,String sub2,
                      float x,float y,float w,float h,int fill,Runnable action){
            card(x,y,w,h,19,theme==1?card:fill);
            icon(glyph,x+w/2,y+39,43,glyph.equals("target")?CORAL:
                glyph.equals("stats")?0xff344868:glyph.equals("palette")?0xffb05de9:ink);
            fitted(label,x+w/2,y+91,16,w-5,ink,true);
            fitted(sub1,x+w/2,y+115,12,w-6,muted,false);
            fitted(sub2,x+w/2,y+132,12,w-6,muted,false);
            taps.add(new Tap(x,y,x+w,y+h,label,action));
        }
        void modeCard(String name,String sub,float x,float y,float w,float h,int fill,
                      boolean daily,Runnable tap){
            card(x,y,w,h,22,theme==1?card:fill);
            if(daily){
                icon("calendar",x+w/2,y+77,69,CORAL);
            }else{
                int[] values=daily?new int[]{}:new int[]{2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
                int cols=daily?0:("Opušteno".equals(name)?5:4);
                int rows=cols;
                float space=cols==5?2.1f:2.2f;
                float side=cols==5?17.0f:21.5f;
                float left=x+(w-(side+space)*cols+space)/2;
                float top=y+(cols==5?20:19);
                for(int i=0;i<cols*rows;i++){
                    float px=left+(i%cols)*(side+space);
                    float py=top+(i/cols)*(side+space);
                    int v=values[Math.min(values.length-1,i)];
                    int col=cols==5?TILES[1+(i+((i/5)*2))%Math.min(10,TILES.length-1)]:tileColor(v);
                    glossyTile(px,py,side,side,4,col);
                    if(cols==4)fitted(""+v,px+side/2,py+side*.67f,
                        v>99?8.4f:12,side-2,WHITE,true);
                }
            }
            fitted(name,x+w/2,y+150,17,w-5,ink,true);
            if(daily){
                fitted("Novi izazov",x+w/2,y+169,12,w-5,muted,false);
                fitted("svaki dan!",x+w/2,y+182,12,w-5,muted,false);
            }else fitted(sub,x+w/2,y+177,17,w-6,muted,false);
            taps.add(new Tap(x,y,x+w,y+h,name+" "+sub,tap));
        }
        void scoreCard(String caption,String value,float x,float y,float width){
            card(x,y,width,72,17,card);
            txt(caption,x+width/2,y+25,15,muted,true);
            fitted(value,x+width/2,y+59,28,width-16,ink,true);
        }
        float gridX=16,gridY=222,gridSize=368;
        boolean compactBoardLayout=false;
        boolean referencePortraitLayout=false;
        void gameScreen(){
            boolean relaxed="relaxed".equals(game.mode);
            boolean daily="daily".equals(game.mode);
            boolean narrow=compactBoardLayout;
            fitted(daily?"Dnevni":relaxed?"Opušteno":"Klasično",
                105,89,daily?32:43,201,ink,true);
            if(!daily) {
                card(39,108,141,50,19,CORAL);
                txt(relaxed?"5×5":"4×4",110,144,35,WHITE,true);
                rays(34,134,16,CORAL);rays(190,134,16,CORAL);
            }else{
                fitted("izazov",103,135,36,195,CORAL,true);
            }
            icon("crown",narrow?216:224,70,24,0xffffb43d);
            fitted("NAJBOLJI",narrow?224:257,100,12,narrow?72:105,muted,true);
            fitted(fmt(best),narrow?224:258,129,25,narrow?72:105,ink,true);
            icon("stats",narrow?292:313,69,23,muted);
            fitted("REZULTAT",narrow?292:331,101,12,narrow?68:98,muted,true);
            fitted(fmt(game.score),narrow?292:330,129,24,narrow?70:113,ink,true);
            if(narrow){
                // Reference 941x1672: action buttons live beside the heading,
                // while the large numbered board and primary controls remain visible.
                button("","pause",335,32,43,49,card,false,()->navigate("pause"));
                button("","undo",335,98,43,49,card,false,()->{
                    if(game.canUndo())undo();
                });
            }else{
                button("","pause",327,153,48,49,card,false,()->navigate("pause"));
                button("","undo",265,153,50,49,card,false,()->{
                    if(game.canUndo())undo();
                });
            }
            drawGrid();
            if(daily){
                int remaining=Math.max(0,80-game.moves);
                fitted("DNEVNI IZAZOV • "+remaining+" POTEZA PREOSTALO",
                    200,narrow?561:624,15,351,remaining<=10?CORAL:muted,true);
            }
            float actionsY=narrow?(daily?578:555):647;
            float actionsH=narrow?82:91;
            button("Novo","refresh",22,actionsY,113,actionsH,card,false,
                ()->requestNew(game.mode));
            if(game.canUndo())
                button("Poništi","undo",144,actionsY,112,actionsH,card,false,()->undo());
            else {
                card(144,actionsY,112,actionsH,20,
                    theme==1?0xff29364a:0xffeae5de);
                icon("undo",200,actionsY+27,25,muted);
                txt("Poništi",200,actionsY+actionsH-15,17,muted,true);
            }
            button("Završi","check",265,actionsY,113,actionsH,CORAL,true,
                ()->navigate("confirmEnd"));
            if(!narrow){
                button("Izbornik","home",22,757,171,64,card,false,
                    ()->navigate("home"));
                button("Podijeli","share",207,757,171,64,card,false,()->share());
                bottomBrand();
            }
        }
        void drawGrid(){
            if(game==null)return;
            int n=game.size;
            card(gridX-4,gridY-4,gridSize+8,gridSize+8,23,
                theme==1?0xff354661:0xfff2e5d3);
            float gap=n==4?9:6;
            float tile=(gridSize-gap*(n+1))/n;
            for(int y=0;y<n;y++)for(int x=0;x<n;x++){
                int value=game.cells[y*n+x];
                float rx=gridX+gap+(tile+gap)*x;
                float ry=gridY+gap+(tile+gap)*y;
                int color=tileColor(value);
                float delta=animation==0?1:pulse;
                float inset=value>0?(1-delta)*tile/2:0;
                float round=tileStyle==1?7:tileStyle==2?24:15;
                if(value==0)shape(rx,ry,tile,tile,round,color);
                else {
                    glossyTile(rx+inset,ry+inset,tile-2*inset,tile-2*inset,round,color);
                    String str=Integer.toString(value);
                    int digits=str.length();
                    float fontsize=(n==5?33:44)-(digits>3?(digits-3)*7:0);
                    fitted(str,rx+tile/2,ry+tile*.62f,fontsize,tile-7,
                        WHITE,true);
                }
            }
        }
        int tileColor(int value) {
            if(value==0)return theme==1?0xff475672:TILES[0];
            int log=31-Integer.numberOfLeadingZeros(value);
            int index=Math.min(log,TILES.length-1);
            if(theme==2&&index>=2){
                int[] vivid={0xffff805f,0xff36d178,0xff00b9ed,0xffad5bee,
                    0xffff6e99,0xffecc647};
                return vivid[(index-1)%vivid.length];
            }
            return TILES[index];
        }
        void dim(){c.drawColor(0xb71b2940);}
        void overlayBox(float y,float height){
            card(27,y,346,height,30,theme==1?0xff293956:CREAM);
        }
        void pauseOverlay(){
            dim();overlayBox(255,370);
            icon("pause",200,302,44,CORAL);
            txt("Igra je pauzirana",200,361,31,ink,true);
            txt("Rezultat: "+fmt(game.score),200,404,20,muted,false);
            button("Nastavi","play",52,443,296,65,CORAL,true,()->navigate("game"));
            button("Izbornik","home",52,532,296,61,card,false,()->navigate("home"));
        }
        void resultOverlay(boolean winning){
            dim();
            boolean narrow=referencePortraitLayout;
            if(winning){
                int[] confetti={CORAL,TEAL,0xff428fea,0xffffbd4e};
                for(int i=0;i<29;i++){
                    float x=(i*83+31)%376+12,y=(i*73+197)%625+95;
                    c.save();c.rotate((i*43)%170,x,y);
                    shape(x,y,7,18,4,confetti[i%4]);c.restore();
                }
            }
            // At the reference phone's aspect ratio, the victory card and
            // all three real buttons fit in one frame without vertical cropping.
            float panelTop=narrow?181:159;
            overlayBox(panelTop,narrow?503:(winning?577:528));
            if(winning){
                icon("crown",200,narrow?177:160,91,0xffffba38);
                txt("Bravo!",200,narrow?267:279,44,ink,true);
                card(71,narrow?282:300,258,43,21,
                    theme==1?card:0xffffeddb);
                fitted("Dosegnuli ste 2048!",200,narrow?310:329,
                    23,245,CORAL,true);
            }else{
                icon("target",200,narrow?211:205,68,CORAL);
                txt("Kraj igre",200,narrow?276:291,42,ink,true);
                fitted("Nema više mogućih poteza.",200,narrow?316:335,
                    18,310,muted,false);
            }
            float summary=narrow?336:365;
            card(52,summary,145,99,21,card);
            card(208,summary,145,99,21,card);
            txt("Tvoj rezultat",124,summary+28,15,muted,false);
            fitted(fmt(game.score),124,summary+81,35,132,ink,true);
            txt(winning?"Zvjezdice":"Najveća pločica",
                280,summary+28,15,muted,false);
            if(winning)fitted("★ ★ ★",280,summary+79,28,125,
                0xffffaf25,true);
            else fitted(fmt(game.maxTile()),280,summary+81,31,125,TEAL,true);
            button(winning?"Nastavi igrati":"Nova igra","play",
                54,narrow?454:491,292,67,CORAL,true,()->{
                    if(winning){game.winAcknowledged=true;persist();navigate("game");}
                    else newGame(game.mode);
                });
            button(winning?"Nova igra":"Povratak",winning?"undo":"home",
                54,narrow?532:576,292,64,card,false,
                ()->{if(winning)newGame(game.mode);else navigate("home");});
            button("Podijeli rezultat","share",54,narrow?606:657,292,57,
                theme==1?0xff3269b5:0xff3b95ee,true,()->share());
        }
        void dailyResultOverlay(boolean success){
            dim();
            overlayBox(193,501);
            icon(success?"crown":"target",200,256,52,success?0xffffb829:CORAL);
            txt(success?"Dnevni izazov riješen!":"Izazov nije uspio",200,329,
                success?28:31,ink,true);
            fitted(success?"Sva 3 zadatka su ostvarena!":
                "Potrošeno je 80 dopuštenih poteza.",200,369,17,316,muted,false);
            card(65,401,270,98,20,card);
            txt("TVOJ REZULTAT",200,432,15,muted,true);
            txt(fmt(game.score),200,472,36,ink,true);
            txt(success?"+250 zvjezdica (jednom dnevno)":
                "Pokušaj ponovno i dosegni 256.",200,534,17,
                success?0xff259d85:muted,true);
            button("Igraj ponovno","undo",53,558,294,62,CORAL,true,
                ()->newGame("daily"));
            button("Izbornik","home",53,639,294,55,card,false,
                ()->navigate("home"));
        }
        void confirmEndOverlay(){
            dim();overlayBox(266,356);
            icon("check",200,312,43,TEAL);
            txt("Završiti partiju?",200,371,29,ink,true);
            txt("Rezultat se sprema u statistiku.",200,414,16,muted,false);
            button("Završi i spremi","check",50,448,300,64,CORAL,true,()->endSession());
            button("Nastavi igrati","play",50,532,300,60,card,false,()->navigate("game"));
        }
        void confirmOverlay() {
            dim();overlayBox(282,320);
            txt("Nova partija?",200,341,30,ink,true);
            fitted("Trenutačni napredak bit će spremljen",200,380,17,300,muted,false);
            txt("u povijest partija.",200,408,17,muted,false);
            button("Započni novo","play",54,452,292,57,CORAL,true,()->newGame(pendingMode));
            button("Odustani","back",54,526,292,54,card,false,()->navigate("game"));
        }
        void daily(){
            back();brand(63,35);
            fitted("Dnevni",158,119,36,173,ink,true);
            fitted("izazov",268,119,36,160,CORAL,true);
            card(112,142,177,39,19,theme==1?card:0xffffecdc);
            icon("calendar",143,163,22,muted);
            fitted(compactDate(),208,169,16,147,ink,true);
            card(291,143,87,41,17,theme==1?card:0xffffebd4);
            icon("crown",308,163,20,0xffffab23);
            fitted(streak+" dana",343,170,15,66,CORAL,true);
            fitted("Riješi današnji izazov i osvoji nagradu!",200,218,17,
                337,muted,false);
            card(76,250,248,248,24,theme==1?0xff35455f:0xfff3e8da);
            int[] sample={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
            for(int i=0;i<16;i++){
                int col=i%4,row=i/4;
                float x=83+col*59,y=257+row*59;
                int value=sample[i];
                glossyTile(x,y,52,52,10,tileColor(value));
                fitted(""+value,x+26,y+34,value>999?17:25,49,WHITE,true);
            }
            boolean active=game!=null&&"daily".equals(game.mode)&&today().equals(game.day);
            boolean finished=today().equals(lastDaily);
            dailyGoalRow("Dosegni 256","Spoji pločice do vrijednosti 256",511,
                finished||(active&&game.peak>=256));
            dailyGoalRow("Osvoji 3000 bodova","Prikupi najmanje 3000 bodova",589,
                finished||(active&&game.score>=3000));
            dailyGoalRow("Završi u 80 poteza","Ostvari oba cilja u 80 poteza",667,finished);
            card(28,744,344,48,19,theme==1?0xff3a3459:0xfff0e9ff);
            icon("star",65,769,31,0xffffb62f);
            fitted(finished?"Nagrada osvojena!":
                "DANAŠNJA NAGRADA",194,767,16,190,ink,true);
            txt("+250",318,775,21,0xffb069dc,true);
            button(active&&!game.gameOver?"Nastavi izazov":
                finished?"Igraj ponovno":"Započni izazov","play",
                25,802,350,58,CORAL,true,()->{
                    if(active&&!game.gameOver)navigate("game");
                    else requestNew("daily");
                });
        }
        void dailyGoalRow(String name,String description,int y,boolean done){
            card(28,y,344,72,18,card);
            glossyTile(40,y+13,46,46,13,done?TEAL:0xffffb754);
            icon(done?"check":"target",63,y+36,26,WHITE);
            fitted(name,231,y+29,19,274,ink,true);
            fitted(description,231,y+55,13,274,muted,false);
        }
        void missions() {
            back();brand(63,32);
            fitted("Misije i",112,128,30,174,ink,true);
            fitted("postignuća",273,128,30,209,CORAL,true);
            card(260,154,117,44,18,theme==1?card:0xffffefcc);
            icon("star",283,177,26,0xffffae1d);
            fitted(fmt(stars),331,185,20,70,ink,true);
            txt("Dnevne",84,205,23,ink,true);
            missionRow("Spoji 20 pločica","Spoji pločice tijekom dana",
                todayMerges,20,50,214,TEAL);
            missionRow("Skupi 1000 bodova","Dnevni rezultat",
                todayPoints,1000,100,322,0xff468ff0);
            txt("Tjedne",79,463,23,ink,true);
            missionRow("Igraj 7 dana zaredom","Sedam različitih dana",
                playDayStreak,7,150,481,0xff3ecb8c);
            txt("Postignuća",107,619,23,ink,true);
            missionRow("Majstor spajanja","Dosegni pločicu 1024",
                recordTile,1024,500,644,0xffa663e4);
            missionRow("Kralj rezultata","Osvoji 50.000 bodova",
                best,50000,1000,752,0xffff934b);
        }
        void missionRow(String name,String sub,int value,int target,int reward,int y,int accent){
            card(26,y,348,96,21,card);
            shape(39,y+14,58,62,15,theme==1?0xff35435d:lighten(accent,.80f));
            icon(value>=target?"check":"target",68,y+45,32,accent);
            fitted(name,198,y+27,18,188,ink,true);
            fitted(sub,198,y+48,13,188,muted,false);
            shape(106,y+65,174,8,4,theme==1?0xff40516c:0xffdce3e8);
            float fraction=Math.max(0,Math.min(1,(float)value/target));
            if(fraction>0)shape(106,y+65,174*fraction,8,4,accent);
            fitted(fmt(Math.min(value,target))+"/"+fmt(target),196,y+90,12,175,muted,true);
            card(289,y+12,71,73,15,theme==1?0xff3b3b4c:0xfffff0d5);
            icon("star",323,y+43,27,0xffffad1a);
            fitted("+"+fmt(reward),323,y+70,13,63,ink,true);
        }
        JSONArray history() {
            try{return new JSONArray(prefs.getString("history","[]"));}
            catch(Exception ignored){return new JSONArray();}
        }
        void statistics(){
            title("Statistika");
            rays(52,122,18,CORAL);rays(352,122,18,CORAL);
            statCell("Najbolji rezultat",fmt(best),20,151,114,112,CORAL);
            statCell("Ukupno partija",fmt(plays),143,151,114,112,TEAL);
            statCell("Najveća pločica",fmt(recordTile),266,151,114,112,0xffffb431);
            int average=plays==0?0:total/plays;
            statCell("Prosjek bodova",fmt(average),21,276,174,102,0xff478df4);
            statCell("Niz dana igranja",fmt(playDayStreak),205,276,174,102,0xffa657de);
            card(22,399,356,219,24,card);
            txt("Zadnjih 10 partija",114,432,19,ink,true);
            card(213,411,157,33,17,theme==1?0xff34445f:0xfff4ebdf);
            shape(215+(chartMetric==0?0:77),413,77,29,15,CORAL);
            fitted("Bodovi",253,434,13,71,chartMetric==0?WHITE:ink,true);
            fitted("Pločica",330,434,13,71,chartMetric==1?WHITE:ink,true);
            taps.add(new Tap(214,410,291,445,"Grafikon bodova",()->{
                chartMetric=0;invalidate();
            }));
            taps.add(new Tap(292,410,371,445,"Grafikon pločica",()->{
                chartMetric=1;invalidate();
            }));
            JSONArray data=history();
            int count=Math.min(10,data.length());
            if(count==0){
                txt("Grafikon će se prikazati nakon",200,519,16,muted,false);
                txt("prve završene partije.",200,545,16,muted,false);
            }else{
                int ceiling=1, topScoreIndex=-1;
                for(int i=0;i<count;i++){
                    JSONObject row=data.optJSONObject(i);
                    if(row==null)continue;
                    int value=chartMetric==0?row.optInt("score"):row.optInt("peak");
                    if(value>ceiling){ceiling=value;topScoreIndex=i;}
                }
                float chartLeft=42,barSlot=315f/Math.max(10,count);
                for(int i=0;i<count;i++){
                    JSONObject row=data.optJSONObject(count-1-i);
                    int val=row==null?0:(chartMetric==0?
                        row.optInt("score"):row.optInt("peak"));
                    float height=Math.max(4,112f*Math.max(0,val)/ceiling);
                    float x=chartLeft+i*barSlot+2,y=581-height;
                    shape(x,y,barSlot-6,height,Math.min(6,height/2),
                        val==ceiling?CORAL:(i%2==0?TEAL:0xff4699e8));
                    fitted(Integer.toString(i+1),x+(barSlot-6)/2,606,11,barSlot-4,
                        muted,false);
                }
            }
            txt("Povijest partija",109,652,22,ink,true);
            int visible=Math.min(5,data.length());
            if(visible==0){
                card(22,673,356,75,17,card);
                txt("Još nema dovršenih partija.",200,719,16,muted,false);
            }
            for(int i=0;i<visible;i++){
                JSONObject row=data.optJSONObject(i);
                if(row==null)continue;
                int y=673+i*78,idx=i;
                card(22,y,356,70,17,card);
                txt(row.optString("day",""),88,y+27,14,muted,false);
                fitted(labelMode(row.optString("mode","classic")),97,y+51,12,136,muted,false);
                fitted(fmt(row.optInt("score")),242,y+43,22,128,
                    i==0?CORAL:TEAL,true);
                int peak=row.optInt("peak",0);
                if(peak>0){
                    shape(296,y+17,57,35,12,
                        theme==1?0xff46416b:0xfff4e0ff);
                    fitted(fmt(peak),324,y+40,15,52,0xff8a41bc,true);
                }
                txt("›",367,y+48,25,muted,true);
                taps.add(new Tap(22,y,378,y+70,"Detalji partije "+(i+1),()->{
                    selectedHistoryIndex=idx;
                    navigate("historyDetail");
                }));
            }
        }
        void statCell(String caption,String value,int x,int y,int width,int h,int accent){
            int bg=theme==1?card:lighten(accent,.84f);
            card(x,y,width,h,21,bg);
            String symbol=caption.contains("Najbolji")?"crown":
                caption.contains("partija")?"gamepad":
                caption.contains("pločica")?"grid":
                caption.contains("Prosjek")?"star":"flame";
            String top,bottom;
            switch(caption){
                case "Najbolji rezultat":top="Najbolji";bottom="rezultat";break;
                case "Ukupno partija":top="Ukupno";bottom="partija";break;
                case "Najveća pločica":top="Najveća";bottom="pločica";break;
                case "Prosjek bodova":top="Prosjek";bottom="bodova";break;
                default:top="Niz dana";bottom="igranja";
            }
            icon(symbol,x+25,y+29,width<=118?24:29,accent);
            float labelX=x+(width<=118?77:98);
            float labelWidth=width<=118?68:123;
            fitted(top,labelX,y+31,width<=118?13:16,labelWidth,ink,true);
            fitted(bottom,labelX,y+48,width<=118?13:16,labelWidth,ink,true);
            fitted(value,x+width/2,y+h-17,29,width-12,accent,true);
        }
        void historyDetail(){
            title("Detalji partije");
            JSONArray rows=history();
            JSONObject row=rows.optJSONObject(selectedHistoryIndex);
            if(row==null){
                txt("Partija više nije dostupna.",200,273,19,muted,false);
                button("Statistika","back",32,729,336,68,CORAL,true,
                    ()->navigate("stats"));
                return;
            }
            card(26,189,348,313,25,card);
            icon("crown",200,246,57,0xffffad27);
            txt("Rezultat",200,293,23,ink,true);
            fitted(fmt(row.optInt("score")),200,357,49,310,CORAL,true);
            fitted(labelMode(row.optString("mode","classic")),200,405,22,318,ink,true);
            fitted(row.optString("day",""),200,448,17,318,muted,false);
            card(26,523,165,121,20,card);
            card(209,523,165,121,20,card);
            txt("Najveća pločica",108,565,17,muted,true);
            fitted(fmt(row.optInt("peak")),108,615,29,145,TEAL,true);
            txt("Potezi",291,565,17,muted,true);
            fitted(fmt(row.optInt("moves")),291,615,29,145,ink,true);
            button("Natrag na statistiku","back",28,729,344,69,CORAL,true,
                ()->navigate("stats"));
        }
        void themes(){
            back();
            brand(61,38);
            fitted("Teme i",158,126,35,165,ink,true);
            fitted("izgled",276,126,35,166,CORAL,true);
            fitted("Prilagodi igru svojem stilu.",200,169,18,350,muted,false);
            String[] names={"Svijetla","Tamna","Živopisna"};
            int[] fills={0xffffece1,0xff1e2943,0xffdcd4ff};
            for(int i=0;i<3;i++){
                final int chosen=i;
                int x=19+i*126;
                card(x,197,116,174,22,card);
                if(theme==i){
                    p.setColor(CORAL);p.setShader(null);p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(3.2f);
                    c.drawRoundRect(x+1.5f,198.5f,x+114.5f,369.5f,21,21,p);
                    p.setStyle(Paint.Style.FILL);
                }
                shape(x+7,207,102,109,18,fills[i]);
                int[] cs=i==2?new int[]{0xffff5a92,0xff58d649,0xff19b7f0,0xffb35ceb}:
                    new int[]{TILES[1],TILES[2],TILES[3],TILES[4]};
                for(int j=0;j<4;j++){
                    int a=j%2,b=j/2;
                    glossyTile(x+15+a*45,218+b*43,40,39,9,cs[j]);
                    txt(Integer.toString(2<<j),x+35+a*45,246+b*43,21,WHITE,true);
                }
                fitted(names[i],x+58,353,17,108,ink,true);
                if(theme==i){
                    shape(x+48,374,20,20,11,CORAL);
                    icon("check",x+58,384,12,WHITE);
                }
                taps.add(new Tap(x,197,x+116,371,names[i],()->{
                    theme=chosen;persist();updateBars();invalidate();
                }));
            }
            card(20,395,360,405,28,card);
            txt("Pregled",99,434,24,ink,true);
            txt("Stil pločica",267,433,19,ink,true);
            // Real visual tile preview for all 16 values, independent of the game board.
            shape(29,465,167,172,17,theme==1?0xff344157:0xfff3e9da);
            int[] sample={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
            for(int i=0;i<16;i++){
                int val=sample[i],col=i%4,row=i/4;
                float x=34+col*39.8f,y=470+row*40f;
                glossyTile(x,y,36,36,tileStyle==1?5:tileStyle==2?13:9,tileColor(val));
                fitted(Integer.toString(val),x+18,y+24,val>=1000?12:16,33,WHITE,true);
            }
            txt("Ovako će izgledati",113,671,13,muted,false);
            txt("tvoja ploča.",113,690,13,muted,false);
            String[] styles={"Zaobljene","Ravne","Staklaste"};
            for(int i=0;i<3;i++){
                final int value=i;float x=210+i*56;
                card(x,456,51,93,15,card);
                if(tileStyle==i){
                    p.setColor(CORAL);p.setShader(null);p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(2.6f);
                    c.drawRoundRect(x+1.5f,457.5f,x+49.5f,547.5f,14,14,p);
                    p.setStyle(Paint.Style.FILL);
                }
                glossyTile(x+6,465,39,44,i==1?5:i==2?15:10,TILES[1]);
                if(i==0){
                    fitted("Zaob-",x+25,525,11,49,ink,true);
                    fitted("ljene",x+25,541,11,49,ink,true);
                }else if(i==2){
                    fitted("Stak-",x+25,525,11,49,ink,true);
                    fitted("laste",x+25,541,11,49,ink,true);
                }else fitted("Ravne",x+25,534,11,49,ink,true);
                taps.add(new Tap(x,456,x+51,549,styles[i],()->{
                    tileStyle=value;persist();invalidate();
                }));
            }
            // Native controls integrated into the same reference-style preview card.
            icon("phone",220,593,30,TEAL);
            txt("Vibracija",284,596,18,ink,true);
            shape(324,577,43,27,14,vibration?TEAL:0xffb7c1cf);
            shape(vibration?342:326,579,23,23,12,WHITE);
            taps.add(new Tap(205,564,378,615,"Vibracija",()->{
                vibration=!vibration;persist();invalidate();
            }));
            icon("sound",219,658,30,0xff4c9af3);
            txt("Zvukovi",280,661,18,ink,true);
            shape(324,642,43,27,14,sound?TEAL:0xffb7c1cf);
            shape(sound?342:326,644,23,23,12,WHITE);
            taps.add(new Tap(205,628,378,683,"Zvukovi",()->{
                sound=!sound;persist();invalidate();
            }));
            txt("Jačina animacija",286,730,19,ink,true);
            shape(211,756,159,9,5,theme==1?0xff536078:0xffe8ded1);
            shape(211,756,animation*79+4,9,5,CORAL);
            shape(210+animation*79,748,23,23,12,CORAL);
            String[] intensities={"Niska","Srednja","Visoka"};
            for(int i=0;i<3;i++){
                int pos=i;
                fitted(intensities[i],223+i*74,791,12,67,muted,false);
                taps.add(new Tap(208+i*55,739,265+i*53,801,intensities[i],()->{
                    animation=pos;persist();invalidate();
                }));
            }
            card(20,805,360,72,29,card);
            String[] tabs={"Novo","Rekordi","Teme","Postavke"};
            String[] glyphs={"home","crown","palette","settings"};
            String[] destinations={"home","stats","themes","settings"};
            for(int i=0;i<4;i++){
                int index=i;float cx=65+i*90;
                if(i==2)shape(205,809,90,65,27,theme==1?0xff453a48:0xffffecdf);
                icon(glyphs[i],cx,830,27,i==2?CORAL:muted);
                fitted(tabs[i],cx,863,13,84,i==2?CORAL:muted,true);
                taps.add(new Tap(22+i*89,805,110+i*89,877,tabs[i],
                    ()->navigate(destinations[index])));
            }
        }

        void settings(){
            // The supplied reference is 941x1672. Keep every setting and
            // the branded footer in one frame at this aspect ratio.
            boolean shortView=referencePortraitLayout;
            back();
            txt("Postavke",200,shortView?84:108,44,ink,true);
            rays(74,shortView?75:97,24,CORAL);
            rays(331,shortView?75:97,24,CORAL);
            int start=shortView?105:148, step=shortView?63:81;
            settingsSwitch("Zvuk","sound",CORAL,start,sound,()->{
                sound=!sound;persist();invalidate();
            });
            settingsSwitch("Vibracija","phone",TEAL,start+step,vibration,()->{
                vibration=!vibration;persist();invalidate();
            });
            settingsAction("Jezik","Hrvatski","globe",0xff378ef6,start+2*step,
                ()->navigate("language"));
            settingsSwitch("Obavijesti","bell",0xffffb630,start+3*step,
                notifications,()->toggleNotifications());
            settingsAction("Sinkronizacija napretka","Lokalna kopija","shield",
                0xffa55ee5,start+4*step,()->navigate("backup"));
            settingsAction("Pravila privatnosti","›","shield",0xffff7969,
                start+5*step,()->navigate("privacy"));
            settingsAction("O aplikaciji","›","info",0xff378ef6,
                start+6*step,()->navigate("about"));
            float footer=shortView?556:729;
            card(22,footer,356,shortView?138:127,27,
                theme==1?card:0xfffff3e6);
            brand(shortView?615:792,shortView?50:54);
            fitted("v"+appVersion(),200,shortView?675:840,16,320,muted,false);
        }
        void settingsSwitch(String caption,String glyph,int accent,int y,boolean on,
                            Runnable action){
            boolean shortView=referencePortraitLayout;
            int height=shortView?57:71;
            card(23,y,354,height,21,card);
            float iconSize=shortView?43:51;
            float iconY=y+(height-iconSize)/2f;
            glossyTile(37,iconY,iconSize,iconSize,12,accent);
            icon(glyph,37+iconSize/2,iconY+iconSize/2,shortView?25:29,WHITE);
            fitted(caption,shortView?180:201,y+height*.61f,
                shortView?20:21,shortView?183:203,ink,true);
            float toggleY=y+(height-32)/2f;
            shape(309,toggleY,55,32,17,on?TEAL:0xffc6ced6);
            shape(on?333:313,toggleY+3,26,26,15,WHITE);
            taps.add(new Tap(23,y,377,y+height,
                caption+(on?" uključeno":" isključeno"),action));
        }
        void settingsAction(String caption,String value,String glyph,
                            int accent,int y,Runnable action){
            boolean shortView=referencePortraitLayout;
            int height=shortView?57:71;
            card(23,y,354,height,21,card);
            float iconSize=shortView?43:51;
            float iconY=y+(height-iconSize)/2f;
            glossyTile(37,iconY,iconSize,iconSize,12,accent);
            icon(glyph,37+iconSize/2,iconY+iconSize/2,shortView?25:29,WHITE);
            boolean hasValue=!"›".equals(value);
            if(hasValue&&caption.length()>16){
                fitted(caption,shortView?216:224,y+(shortView?24:31),
                    shortView?15:16,shortView?236:240,ink,true);
                fitted(value,shortView?216:224,y+(shortView?44:55),
                    shortView?12:13,shortView?230:225,muted,false);
            }else{
                fitted(caption,caption.length()>13?206:189,
                    y+(shortView?36:43),shortView?19:20,
                    caption.length()>13?189:150,ink,true);
                if("Jezik".equals(caption)){
                    float pillY=y+(shortView?9:14),pillH=shortView?39:44;
                    shape(247,pillY,116,pillH,19,
                        theme==1?0xff394a63:0xfffff2e2);
                    fitted(value,293,y+(shortView?36:43),17,83,ink,true);
                    icon("chevron-down",348,y+height/2f,17,muted);
                }else if(hasValue){
                    fitted(value,319,y+(shortView?35:44),14,90,muted,true);
                }else txt("›",355,y+(shortView?39:49),35,muted,false);
            }
            taps.add(new Tap(23,y,377,y+height,caption,action));
        }
        void helpPage(){
            title("Kako igrati");
            txt("Spajaj jednake pločice",200,170,24,ink,true);
            card(42,202,316,139,22,card);
            glossyTile(78,229,81,81,16,TILES[1]);
            glossyTile(167,229,81,81,16,TILES[1]);
            glossyTile(261,229,81,81,16,TILES[2]);
            txt("2",119,283,37,WHITE,true);
            txt("2",208,283,37,WHITE,true);
            txt("4",301,283,37,WHITE,true);
            txt("→",253,322,20,CORAL,true);
            card(29,381,342,196,20,card);
            txt("1. Povuci prstom",200,424,23,ink,true);
            fitted("Sve pločice pomiču se u istom smjeru.",200,461,17,314,muted,false);
            fitted("Dvije jednake spajaju se u jednu veću.",200,495,17,314,muted,false);
            fitted("Pokušaj dosegnuti 2048 i oboriti rekord!",200,529,16,314,muted,false);
            card(29,594,342,89,20,theme==1?card:0xffe5f7f3);
            fitted("2. Igraj svojim tempom",200,630,21,310,ink,true);
            fitted("4×4, 5×5 ili dnevni izazov od 80 poteza.",200,658,15,312,muted,false);
            button("Započni klasičnu igru","play",29,727,342,73,CORAL,true,
                ()->requestNew("classic"));
            bottomBrand();
        }
        void backupPage(){
            title("Sigurnosna kopija");
            card(24,161,352,206,22,card);
            icon("shield",200,214,45,TEAL);
            txt("Spremi svoj napredak",200,270,23,ink,true);
            txt("Izvezi rezultate i postavke u JSON.",200,305,16,muted,false);
            txt("Datoteka ostaje pod tvojom kontrolom.",200,335,15,muted,false);
            button("Izvezi kopiju","share",24,404,352,79,CORAL,true,
                ()->chooseBackupFile(true));
            button("Vrati kopiju","undo",24,507,352,79,card,false,
                ()->chooseBackupFile(false));
            txt("Vraćanje zamjenjuje lokalni napredak.",200,649,16,muted,false);
            txt("Cloud sinkronizacija nije uključena.",200,684,16,muted,false);
            button("Postavke","back",24,755,352,63,card,false,()->navigate("settings"));
            bottomBrand();
        }
        void languagePage(){
            title("Jezik");
            txt("Jezik sučelja",200,181,21,muted,true);
            card(25,223,350,95,20,card);
            icon("check",67,269,28,TEAL);
            txt("Hrvatski",208,278,25,ink,true);
            txt("Ostali jezici nisu dostupni u ovoj verziji.",200,369,15,muted,false);
            button("Natrag na postavke","back",25,720,350,70,CORAL,true,()->navigate("settings"));
        }
        void switchRow(String name,String symbol,int y,boolean active,Runnable click){
            card(25,y,350,85,20,card);
            shape(40,y+14,58,57,15,active?TEAL:0xffa3abb6);
            icon(symbol,69,y+43,30,WHITE);
            fitted(name,174,y+50,22,130,ink,true);
            shape(291,y+23,65,38,21,active?TEAL:0xffb6bfce);
            shape(active?325:296,y+27,30,30,15,WHITE);
            taps.add(new Tap(25,y,375,y+85,name+(active?" uključeno":" isključeno"),click));
        }
        void textPage(String heading,String[] lines){
            title(heading);
            card(24,170,352,Math.max(290,lines.length*50),25,card);
            for(int i=0;i<lines.length;i++)
                fitted(lines[i],200,219+i*48,17,330,ink,false);
            button("Nazad","back",25,722,350,72,CORAL,true,()->navigate("settings"));
        }
        // Screen readers expose the buttons and all four board directions as
        // explicit custom actions; directional keyboard keys also remain supported.
        static final int ACTION_TAP_BASE=0x01001000;
        static final int ACTION_DIRECTION_BASE=0x01002000;
        @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName("android.view.View");
            info.setFocusable(true);
            int count=Math.min(taps.size(),200);
            for(int i=0;i<count;i++){
                Tap tap=taps.get(i);
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    ACTION_TAP_BASE+i,tap.label));
            }
            if("game".equals(page)){
                String[] directions={"Pomiči lijevo","Pomiči desno","Pomiči gore","Pomiči dolje"};
                for(int i=0;i<4;i++)
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                        ACTION_DIRECTION_BASE+i,directions[i]));
            }
        }
        @Override public boolean performAccessibilityAction(int action,Bundle args){
            if(action>=ACTION_TAP_BASE&&action<ACTION_TAP_BASE+taps.size()){
                taps.get(action-ACTION_TAP_BASE).action.run();invalidate();return true;
            }
            if(action>=ACTION_DIRECTION_BASE&&action<ACTION_DIRECTION_BASE+4&&
                "game".equals(page)){
                move(action-ACTION_DIRECTION_BASE);return true;
            }
            return super.performAccessibilityAction(action,args);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            float x=(event.getX()-offX)/Math.max(.1f,scale);
            float y=(event.getY()-offY)/Math.max(.1f,scale);
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
                touchX=x;touchY=y;
                lastFingerY=event.getY();initialFingerY=event.getY();
                draggingScroll=false;
                pointerBoard=x>=gridX && x<=gridX+gridSize &&
                    y>=gridY&&y<=gridY+gridSize && "game".equals(page);
                return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_MOVE &&
                maxScroll>0&&!pointerBoard){
                if(Math.abs(event.getY()-initialFingerY)>13f*getResources()
                    .getDisplayMetrics().density)draggingScroll=true;
                if(draggingScroll){
                    scrollOffset=Math.max(0,Math.min(maxScroll,
                        scrollOffset+(lastFingerY-event.getY())/Math.max(.1f,scale)));
                    invalidate();
                }
                lastFingerY=event.getY();
                return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_CANCEL){
                pointerBoard=false;draggingScroll=false;return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_UP){
                if(draggingScroll){draggingScroll=false;return true;}
                float dx=x-touchX,dy=y-touchY;
                if(pointerBoard&&Math.max(Math.abs(dx),Math.abs(dy))>=25) {
                    move(Math.abs(dx)>Math.abs(dy)?
                        (dx<0?TileGame.LEFT:TileGame.RIGHT):
                        (dy<0?TileGame.UP:TileGame.DOWN));
                    pointerBoard=false;return true;
                }
                // A release after scrolling should never activate a button.
                if(Math.max(Math.abs(dx),Math.abs(dy))>18)return true;
                for(Tap t:new ArrayList<>(taps))if(x>=t.left&&x<=t.right&&
                    y>=t.top&&y<=t.bottom){t.action.run();return true;}
                performClick();return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
