package com.brendigo.brojko;

import android.app.Activity;
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
    static final int INK=0xff192b4d, CREAM=0xfffff8ee, CORAL=0xffff7054,
        TEAL=0xff27c7bb, MUTED=0xff77849a, WHITE=Color.WHITE;
    static final int[] TILES={0xffeee0ce,0xffff805f,0xff27c7bb,0xff3d98f0,
        0xffffbb4a,0xffff8751,0xffff6f96,0xffbd60e9,0xff835bea,
        0xff409ff0,0xff36c7a1,0xfff3b547,0xffe16bca,0xff587be3};
    SharedPreferences prefs;
    TileGame game;
    BoardScreen screen;
    String page="home", pendingMode="classic";
    int best, plays, total, wins, recordTile, totalMerges, streak, theme, tileStyle, animation=1;
    int todayMerges, todayPoints, playDayStreak;
    String lastDaily="", statsDay="", lastPlayDay="";
    boolean sound=true, vibration=true;
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
        wins=prefs.getInt("wins",0);recordTile=prefs.getInt("tile",0);
        totalMerges=prefs.getInt("totalMerges",0);streak=prefs.getInt("streak",0);
        lastDaily=prefs.getString("lastDaily","");
        statsDay=prefs.getString("statsDay","");
        todayMerges=prefs.getInt("todayMerges",0);todayPoints=prefs.getInt("todayPoints",0);
        playDayStreak=prefs.getInt("playDayStreak",0);
        lastPlayDay=prefs.getString("lastPlayDay","");ensureDay();
        theme=prefs.getInt("theme",0);
        tileStyle=prefs.getInt("style",0);animation=prefs.getInt("animation",1);
        sound=prefs.getBoolean("sound",true);vibration=prefs.getBoolean("vibrate",true);
        try {
            String saved=prefs.getString("session","");
            if(!saved.isEmpty())game=TileGame.fromJson(new JSONObject(saved));
            else migrateLegacy();
        }catch(Exception ignored){game=null;}
        screen=new BoardScreen(this);setContentView(screen);
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
    void ensureDay(){if(!today().equals(statsDay)){statsDay=today();todayMerges=0;todayPoints=0;}}
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
            .putInt("wins",wins).putInt("tile",recordTile).putInt("totalMerges",totalMerges)
            .putInt("streak",streak).putString("lastDaily",lastDaily)
            .putInt("todayMerges",todayMerges).putInt("todayPoints",todayPoints)
            .putString("statsDay",statsDay).putInt("playDayStreak",playDayStreak)
            .putString("lastPlayDay",lastPlayDay)
            .putInt("theme",theme).putInt("style",tileStyle).putInt("animation",animation)
            .putBoolean("sound",sound).putBoolean("vibrate",vibration);
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
        page="game";persist();screen.invalidate();
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
        TileGame.Result result=game.move(direction);
        if(!result.changed){
            if(result.lost){page="over";persist();screen.invalidate();}
            return;
        }
        ensureDay();total+=result.points;totalMerges+=result.merges;
        todayPoints+=result.points;todayMerges+=result.merges;
        best=Math.max(best,game.score);recordTile=Math.max(recordTile,game.peak);
        if(result.first2048)wins++;
        if("daily".equals(game.mode)&&today().equals(game.day)
           &&game.peak>=256 && game.score>=3000 && game.moves<=80
           &&!today().equals(lastDaily)){
            streak=yesterday().equals(lastDaily)?streak+1:1;
            lastDaily=today();
        }
        soundEffect();
        screen.setContentDescription("Brojko, rezultat "+fmt(game.score)+
            ", najveća pločica "+fmt(game.peak)+", potez "+game.moves);
        if(result.first2048)page="win";
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
        total=Math.max(0,total-reverted);todayPoints=Math.max(0,todayPoints-reverted);
        int revertedMerges=mergeCount-game.merges;
        totalMerges=Math.max(0,totalMerges-revertedMerges);
        todayMerges=Math.max(0,todayMerges-revertedMerges);
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
        "lastDaily","statsDay","todayMerges","todayPoints","playDayStreak",
        "lastPlayDay","theme","style","animation","sound","vibrate",
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
            if((key.equals("sound")||key.equals("vibrate"))&&!(value instanceof Boolean))
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
        else if(page.equals("privacy")||page.equals("about")||
            page.equals("language")||page.equals("backup"))navigate("settings");
        else if(page.equals("confirm")||page.equals("confirmEnd"))navigate(game==null?"home":"game");
        else if(page.equals("win")||page.equals("over"))navigate("home");
        else if(page.equals("home"))super.onBackPressed();
        else navigate("home");
    }
    @Override protected void onPause(){persist();super.onPause();}
    @Override protected void onDestroy(){if(tones!=null){tones.release();tones=null;}super.onDestroy();}

    final class BoardScreen extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        final ArrayList<Tap> taps=new ArrayList<>();
        Canvas c;float scale,offX,offY,touchX,touchY;
        boolean pointerBoard;
        int ink,muted,bg,card,shadow;
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
        BoardScreen(Context context){super(context);}
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
            shape(x,y+3,w,h,radius,shadow);shape(x,y,w,h,radius,color);
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
        void txt(String content,float x,float baseline,float size,int color,boolean bold) {
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);
            p.setColor(color);p.setTypeface(Typeface.create("sans-serif-rounded",
                bold?Typeface.BOLD:Typeface.NORMAL));
            p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);
            c.drawText(content,x,baseline,p);
        }
        void fitted(String content,float x,float baseline,float size,float maxWidth,int color,boolean bold) {
            p.setTypeface(Typeface.create("sans-serif-rounded",bold?Typeface.BOLD:Typeface.NORMAL));
            p.setTextSize(size);
            while(p.measureText(content)>maxWidth && size>10){size-=1;p.setTextSize(size);}
            txt(content,x,baseline,size,color,bold);
        }
        void icon(String type,float cx,float cy,float width,int color){
            c.save();c.translate(cx,cy);c.scale(width/24f,width/24f);
            p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.7f);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            p.setShader(null);
            Path z=new Path();
            switch(type) {
                case "back":z.moveTo(16,4);z.lineTo(8,12);z.lineTo(16,20);break;
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
            card(x,y,w,h,18,color);
            if(symbol!=null)icon(symbol,x+25,y+h/2,22,prominent?WHITE:ink);
            fitted(label,x+(symbol!=null?(w+38)/2:w/2),y+h/2+6,
                h>=66?19:16,symbol!=null?w-56:w-22,prominent?WHITE:ink,true);
            taps.add(new Tap(x,y,x+w,y+h,label==null||label.isEmpty()?
                (symbol==null?"Akcija":symbol):label,action));
        }
        void title(String name) {
            back();
            fitted(name,225,95,34,300,ink,true);
            txt("by Brendigo",259,121,14,muted,true);
        }
        void back(){button("","back",22,49,52,52,card,false,()->navigate("home"));}
        void brand(float y,float size) {
            p.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));
            p.setTextSize(size);
            float left=p.measureText("Br"),right=p.measureText("jko");
            float gap=size*.09f,tile=size*.74f;
            float total=left+right+tile+2*gap;
            float start=200-total/2;
            txt("Br",start+left/2,y,size,ink,true);
            float bx=start+left+gap;
            card(bx,y-size*.78f,tile,tile,Math.max(7,size*.16f),CORAL);
            txt("2",bx+tile/2,y-size*.20f,size*.52f,WHITE,true);
            txt("jko",bx+tile+gap+right/2,y,size,ink,true);
            txt("by Brendigo",265,y+23,15,muted,true);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);c=canvas;palette();
            float usableWidth=Math.max(1,getWidth()-getPaddingLeft()-getPaddingRight());
            float usableHeight=Math.max(1,getHeight()-getPaddingTop()-getPaddingBottom());
            scale=Math.min(usableWidth/400f,usableHeight/880f);
            offX=getPaddingLeft()+(usableWidth-400*scale)/2f;
            offY=getPaddingTop()+(usableHeight-880*scale)/2f;
            c.drawColor(bg);c.save();c.translate(offX,offY);c.scale(scale,scale);
            taps.clear();
            switch(page) {
                case "home":home();break;
                case "game":gameScreen();break;
                case "pause":gameScreen();taps.clear();pauseOverlay();break;
                case "win":gameScreen();taps.clear();resultOverlay(true);break;
                case "over":gameScreen();taps.clear();resultOverlay(false);break;
                case "confirm":gameScreen();taps.clear();confirmOverlay();break;
                case "confirmEnd":gameScreen();taps.clear();confirmEndOverlay();break;
                case "daily":daily();break;
                case "missions":missions();break;
                case "stats":statistics();break;
                case "themes":themes();break;
                case "settings":settings();break;
                case "language":languagePage();break;
                case "backup":backupPage();break;
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
        void home(){
            // Supplied reference: wide playful wordmark, two score cards, one primary
            // action, three mode tiles and four equally sized shortcut cards.
            brand(130,66);
            fitted("Spajaj brojeve. Ruši rekorde.",200,187,21,371,CORAL,true);
            card(23,217,171,93,22,card);card(206,217,171,93,22,card);
            icon("crown",58,250,27,0xffffb228);
            txt("NAJBOLJI",122,252,14,muted,true);
            fitted(fmt(best),113,288,29,150,ink,true);
            icon("stats",241,250,28,0xff468df0);
            txt("POSLJEDNJI",312,252,13,muted,true);
            fitted(fmt(game==null?0:game.score),296,288,29,150,ink,true);
            button(game==null?"Nova igra":"Nastavi igru","play",27,338,346,82,CORAL,true,
                ()->{if(game==null)requestNew("classic");else navigate(game.gameOver?"over":"game");});
            modeCard("Klasično","4 × 4",22,448,111,171,0xffffe9de,false,()->requestNew("classic"));
            modeCard("Opušteno","5 × 5",144,448,111,171,0xffdbfaf3,false,()->requestNew("relaxed"));
            modeCard("Dnevni izazov","Igraj danas",266,448,111,171,0xfffff4d4,true,()->navigate("daily"));
            shortcut("Misije","target",21,650,85,156,0xffffecdd,()->navigate("missions"));
            shortcut("Statistika","stats",112,650,85,156,0xffe2efff,()->navigate("stats"));
            shortcut("Teme","palette",203,650,85,156,0xfff3e6fa,()->navigate("themes"));
            shortcut("Postavke","settings",294,650,85,156,0xffe8f8ee,()->navigate("settings"));
            bottomBrand();
        }
        void shortcut(String label,String glyph,float x,float y,float w,float h,int fill,Runnable action){
            card(x,y,w,h,18,theme==1?card:fill);
            icon(glyph,x+w/2,y+48,37,glyph.equals("target")?CORAL:
                glyph.equals("stats")?0xff488ce9:glyph.equals("palette")?0xffb05de9:ink);
            fitted(label,x+w/2,y+112,15,w-7,ink,true);
            taps.add(new Tap(x,y,x+w,y+h,label,action));
        }
        void modeCard(String name,String sub,float x,float y,float w,float h,int fill,
                      boolean daily,Runnable tap){
            card(x,y,w,h,23,theme==1?card:fill);
            if(daily) {
                icon("calendar",x+w/2,y+60,49,CORAL);
                icon("crown",x+w/2,y+62,16,0xffffaf24);
            } else {
                for(int i=0;i<4;i++){
                    float tx=x+14+(i%2)*43,ty=y+21+(i/2)*39;
                    shape(tx,ty,39,35,8,TILES[i+1]);
                    txt(""+(2<<i),tx+19.5f,ty+24,16,WHITE,true);
                }
            }
            fitted(name,x+w/2,y+118,17,w-8,ink,true);
            fitted(sub,x+w/2,y+146,14,w-8,muted,false);
            taps.add(new Tap(x,y,x+w,y+h,name+" "+sub,tap));
        }
        void scoreCard(String caption,String value,float x,float y,float width){
            card(x,y,width,72,17,card);
            txt(caption,x+width/2,y+25,15,muted,true);
            fitted(value,x+width/2,y+59,28,width-16,ink,true);
        }
        float gridX=24,gridY=246,gridSize=352;
        void gameScreen(){
            brand(84,48);
            fitted(labelMode(game.mode),167,152,32,288,ink,true);
            button("","pause",326,115,51,51,card,false,()->navigate("pause"));
            scoreCard("NAJBOLJI",fmt(best),24,173,168);
            scoreCard("REZULTAT",fmt(game.score),208,173,168);
            drawGrid();
            button("Novo","undo",22,653,113,79,card,false,()->requestNew(game.mode));
            if(game.canUndo())button("Poništi","undo",144,653,112,79,card,false,()->undo());
            else {card(144,653,112,79,18,theme==1?0xff29364a:0xffeae5de);
                  icon("undo",167,691,21,muted);txt("Poništi",210,696,15,muted,true);}
            button("Završi","check",265,653,113,79,CORAL,true,()->navigate("confirmEnd"));
            button("Podijeli rezultat","share",22,759,173,60,card,false,()->share());
            button("Izbornik","home",205,759,173,60,card,false,()->navigate("home"));
            bottomBrand();
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
                shape(rx+inset,ry+inset,tile-2*inset,tile-2*inset,round,
                    value==0?color:lighten(color,.08f));
                if(value>0) {
                    if(tileStyle==2)gradient(rx+inset,ry+inset,tile-2*inset,tile-2*inset,
                        round,lighten(color,.27f),color);
                    else shape(rx+inset,ry+inset+3,tile-2*inset,tile-2*inset-3,
                        round,color);
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
        void dim(){shape(0,0,400,880,0,0xb71b2940);}
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
            if(winning) {
                // Celebratory particles are part of the UI, not image assets.
                int[] confetti={CORAL,TEAL,0xff428fea,0xffffbd4e};
                for(int i=0;i<21;i++) {
                    float x=(i*79+37)%380+9, y=(i*73+187)%515+120;
                    c.save();c.rotate((i*37)%180,x,y);
                    shape(x,y,8,20,5,confetti[i%4]);c.restore();
                }
            }
            overlayBox(winning?177:208,winning?528:484);
            icon(winning?"crown":"target",200,244,52,winning?0xffffb52a:CORAL);
            txt(winning?"Bravo!":"Kraj igre",200,316,40,ink,true);
            txt(winning?"Dosegnuta pločica 2048!":"Nema više mogućih poteza.",
                200,356,18,muted,true);
            card(65,381,270,101,20,card);
            txt("TVOJ REZULTAT",200,417,15,muted,true);
            txt(fmt(game.score),200,464,35,ink,true);
            if(winning) {
                txt("★  ★  ★",200,513,30,0xffffac22,true);
                button("Nastavi igrati","play",54,536,292,65,CORAL,true,()->{
                    game.winAcknowledged=true;persist();navigate(game.gameOver?"over":"game");});
                button("Podijeli rezultat","share",54,615,292,58,card,false,()->share());
            }else{
                button("Nova igra","undo",54,504,292,62,CORAL,true,()->requestNew(game.mode));
                if(game.canUndo())
                    button("Poništi zadnji potez","undo",54,582,292,61,card,false,()->undo());
                else
                    button("Podijeli rezultat","share",54,582,292,61,card,false,()->share());
            }
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
            title("Dnevni izazov");
            txt(compactDate(),200,154,19,CORAL,true);
            card(286,163,94,47,16,theme==1?card:0xffffecd1);
            icon("crown",307,187,22,0xffffac22);
            txt(streak+" dana",347,193,16,ink,true);
            fitted("Riješi današnji izazov i osvoji nagradu!",200,198,17,271,muted,false);
            // Visual preview, not the actual randomized opening board.
            card(77,231,246,246,20,theme==1?0xff36445d:0xffefe1ce);
            int[] sample={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
            for(int i=0;i<16;i++){
                int col=i%4,row=i/4;
                float px=84+col*59,py=238+row*59;
                int v=sample[i];
                int color=tileColor(v);
                shape(px,py,52,52,10,color);
                fitted(""+v,px+26,py+32,v>999?16:23,49,WHITE,true);
            }
            txt("PRIMJER PLOČE",200,502,13,muted,true);
            boolean dailyActive=game!=null&&"daily".equals(game.mode)&&today().equals(game.day);
            boolean finished=today().equals(lastDaily);
            dailyGoalRow("Dosegni 256","Spoji do pločice 256",530,
                finished||(dailyActive&&game.peak>=256));
            dailyGoalRow("Osvoji 3000 bodova","Igraj i skupljaj bodove",608,
                finished||(dailyActive&&game.score>=3000));
            dailyGoalRow("Završi u 80 poteza","Ispuni oba cilja u 80 poteza",686,finished);
            button(dailyActive&&!game.gameOver?"Nastavi izazov":"Započni izazov",
                "play",27,781,346,64,CORAL,true,
                ()->{if(dailyActive&&!game.gameOver)navigate("game");
                     else requestNew("daily");});
        }
        void dailyGoalRow(String name,String description,int y,boolean done){
            card(28,y,344,70,17,card);
            shape(42,y+14,44,44,11,done?TEAL:0xffffdbc6);
            icon(done?"check":"target",64,y+36,25,done?WHITE:CORAL);
            fitted(name,231,y+30,19,274,ink,true);
            fitted(description,231,y+53,13,274,muted,false);
        }
        void missions() {
            title("Misije i postignuća");
            txt("DNEVNE",81,181,17,CORAL,true);
            missionRow("Spoji 20 pločica","Danas spojene pločice",todayMerges,20,206);
            missionRow("Skupi 1000 bodova","Danas osvojeni bodovi",todayPoints,1000,322);
            txt("TJEDNE",77,465,17,CORAL,true);
            missionRow("Igraj 7 dana zaredom","Različiti dani igranja",playDayStreak,7,484);
            txt("POSTIGNUĆA",107,629,17,CORAL,true);
            missionRow("Majstor spajanja","Dosegni pločicu 512",recordTile,512,654);
            missionRow("Kralj brojeva","Dosegni pločicu 2048",recordTile,2048,760);
        }
        void missionRow(String name,String sub,int value,int target,int y){
            card(25,y,350,97,18,card);
            fitted(name,200,y+27,19,320,ink,true);
            fitted(sub,200,y+50,14,320,muted,false);
            shape(58,y+67,284,8,4,theme==1?0xff43516c:0xffe9dfd4);
            float fraction=Math.min(1,(float)value/target);
            if(fraction>0)shape(58,y+67,284*fraction,8,4,
                value>=target?TEAL:CORAL);
            txt(Math.min(value,target)+"/"+target,200,y+91,12,muted,true);
        }
        JSONArray history() {
            try{return new JSONArray(prefs.getString("history","[]"));}
            catch(Exception ignored){return new JSONArray();}
        }
        void statistics(){
            title("Statistika");
            statCell("Najbolji rezultat",fmt(best),20,151,114,113,CORAL);
            statCell("Ukupno partija",fmt(plays),143,151,114,113,TEAL);
            statCell("Najveća pločica",fmt(recordTile),266,151,114,113,0xffe79b35);
            int average=plays==0?0:total/plays;
            statCell("Prosjek bodova",fmt(average),22,279,173,100,0xff4a90ef);
            statCell("Niz dana igranja",fmt(playDayStreak),205,279,173,100,0xff9b53dd);
            card(22,403,356,211,22,card);
            txt("ZADNJIH 10 PARTIJA",137,437,17,ink,true);
            JSONArray array=history();
            int count=Math.min(10,array.length());
            if(count==0){
                txt("Grafikon će se popuniti nakon",200,511,16,muted,false);
                txt("završenih partija.",200,539,16,muted,false);
            } else {
                int ceiling=1;
                for(int i=0;i<count;i++){
                    JSONObject row=array.optJSONObject(i);
                    if(row!=null)ceiling=Math.max(ceiling,row.optInt("score",0));
                }
                float chartLeft=39,chartWidth=322;
                for(int i=0;i<count;i++){
                    JSONObject row=array.optJSONObject(count-1-i);
                    int score=row==null?0:row.optInt("score",0);
                    float bw=chartWidth/count, height=Math.max(4,119f*score/ceiling);
                    float x=chartLeft+i*bw+bw*.13f;
                    float y=581-height;
                    shape(x,y,bw*.73f,height,5,
                        score==ceiling?CORAL:(i%2==0?TEAL:0xff4b9def));
                    txt(""+(i+1),x+bw*.37f,600,10,muted,false);
                }
            }
            txt("POVIJEST PARTIJA",111,648,17,ink,true);
            int displayed=Math.min(2,array.length());
            if(displayed==0)txt("Još nema dovršenih partija.",200,702,16,muted,false);
            for(int i=0;i<displayed;i++){
                JSONObject row=array.optJSONObject(i);if(row==null)continue;
                int y=668+i*75;
                card(24,y,352,65,17,card);
                txt(row.optString("day",""),102,y+26,14,muted,false);
                fitted(labelMode(row.optString("mode")),102,y+50,13,150,muted,true);
                txt(fmt(row.optInt("score")),296,y+42,23,ink,true);
            }
            bottomBrand();
        }
        void statCell(String title,String value,int x,int y,int width,int h,int accent){
            card(x,y,width,h,20,card);
            shape(x+11,y+12,8,8,4,accent);
            fitted(title,x+width/2,y+39,14,width-10,muted,true);
            fitted(value,x+width/2,y+h-20,29,width-10,accent,true);
        }
        void themes(){
            title("Teme i izgled");
            txt("Odaberi omiljeni izgled igre",200,174,19,muted,false);
            String[] names={"Svijetla","Tamna","Živopisna"};
            int[] fills={0xffffe7d7,0xff24354e,0xffdcd5ff};
            for(int i=0;i<3;i++){
                final int selected=i;
                int x=24+i*126;
                card(x,217,116,158,20,theme==i?CORAL:card);
                shape(x+9,227,98,96,17,fills[i]);
                int[] cs=i==2?new int[]{0xffff6e99,0xff36d178,0xff00b9ed,0xffad5bee}:
                    new int[]{TILES[1],TILES[2],TILES[3],TILES[4]};
                for(int j=0;j<4;j++){
                    int a=j%2,b=j/2;
                    shape(x+19+a*43,239+b*39,38,35,8,cs[j]);
                    txt(""+(2<<j),x+38+a*43,263+b*39,17,WHITE,true);
                }
                fitted(names[i],x+58,350,15,112,theme==i?WHITE:ink,true);
                taps.add(new Tap(x,217,x+116,375,names[i],()->{
                    theme=selected;persist();updateBars();invalidate();
                }));
            }
            txt("STIL PLOČICA",109,429,18,ink,true);
            String[] styles={"Zaobljene","Ravne","Staklaste"};
            for(int i=0;i<3;i++){
                final int chosen=i;float x=24+i*126;
                button(styles[i],null,x,452,116,57,
                    tileStyle==i?CORAL:card,tileStyle==i,()->{
                        tileStyle=chosen;persist();invalidate();
                    });
            }
            txt("JAČINA ANIMACIJA",136,561,18,ink,true);
            String[] options={"Niska","Srednja","Visoka"};
            for(int i=0;i<3;i++){
                final int value=i;
                button(options[i],null,24+i*126,584,116,57,
                    animation==i?CORAL:card,animation==i,()->{
                        animation=value;persist();invalidate();
                    });
            }
            button("Postavke zvuka","settings",25,716,350,68,card,false,()->navigate("settings"));
            bottomBrand();
        }
        void settings(){
            title("Postavke");
            switchRow("Zvuk","sound",178,sound,()->{sound=!sound;persist();invalidate();});
            switchRow("Vibracija","phone",281,vibration,()->{
                vibration=!vibration;persist();invalidate();});
            button("Jezik: Hrvatski",null,25,384,350,69,card,false,()->navigate("language"));
            button("Teme i izgled","palette",25,473,350,69,card,false,()->navigate("themes"));
            button("Sigurnosna kopija","shield",25,562,350,69,card,false,()->navigate("backup"));
            button("Pravila privatnosti","shield",25,651,169,67,card,false,()->navigate("privacy"));
            button("O aplikaciji","settings",206,651,169,67,card,false,()->navigate("about"));
            fitted("Brojko "+appVersion()+"  •  by Brendigo",200,812,17,350,muted,true);
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
                pointerBoard=x>=gridX && x<=gridX+gridSize &&
                    y>=gridY&&y<=gridY+gridSize && "game".equals(page);
                return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_UP){
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
