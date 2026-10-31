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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
public final class MainActivity extends ComponentActivity {
    static final int INK=0xff1e2a44, CREAM=0xfffff8ee,
        LIGHT_CANVAS=0xfffefcf6, CORAL=0xffff7a5b,
        TEAL=0xff2ec4b6, MUTED=0xff77849a, WHITE=Color.WHITE;
    static final int[] TILES={0xffeee0ce,0xffff7a5b,0xff2ec4b6,0xff3d98f0,
        0xffffbb4a,0xffff8751,0xffff6f96,0xffbd60e9,0xff835bea,
        0xff409ff0,0xff36c7a1,0xfff3b547,0xffe16bca,0xff587be3};
    // Reuse immutable victory lettering data; no array allocations in onDraw.
    private static final String[] VICTORY_GLYPHS={"B","r","a","v","o","!"};
    private static final float[] VICTORY_TILT={-8f,4f,-6f,5f,-4f,7f};
    private static final float[] VICTORY_RISE={2f,-2f,1f,-1f,0f,-3f};
    // B and exclamation are deliberately oversized in the source logo.
    private static final float[] VICTORY_WIDTH={1.24f,1f,1f,1f,1f,1.35f};
    private static final float[] VICTORY_HEIGHT={1.2f,1f,1f,1f,1f,1.08f};
    // Immutable mission pictogram data: avoid new arrays on every redraw.
    private static final int[] MISSION_TILE_VALUES={2,4,8,16};
    private static final float[][] MISSION_CORAL_RAYS={
        {-10f,-10f,-33f},{11f,-7f,38f},{-12f,12f,36f}
    };
    SharedPreferences prefs;
    TileGame game;
    BoardScreen screen;
    // Allow the full branded opening illustration to be read before Home.
    // Previously 1350 ms was too brief, especially on faster devices.
    static final long SPLASH_DURATION_MS=3200L;
    String page="splash", pendingMode="classic";
    int selectedHistoryIndex=0;
    int chartMetric=0;
    int best, plays, total, wins, recordTile, totalMerges, streak, theme, tileStyle, animation=1;
    int todayMerges, todayPoints, weekPoints, playDayStreak, lastScore, stars, rewardFlags;
    String lastDaily="", statsDay="", weekStart="", lastPlayDay="", dailyRewardDate="";
    boolean undoUsedThisGame=false;
    boolean sound=true, vibration=true, notifications=false;
    // UI-only state: distinguishes a newly awarded daily bonus from a replay.
    boolean dailyBonusGrantedThisAttempt=false;
    // After a successful restore the old Activity must not overwrite imported preferences.
    boolean skipPersistOnPauseAfterImport=false;
    static final int NOTIFICATION_PERMISSION_REQUEST=704;
    float pulse=1f;
    ToneGenerator tones;
    final NumberFormat number=NumberFormat.getIntegerInstance(new Locale("hr","HR"));

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // Draw behind both system bars on API 24-36. Use the manual
        // edge-to-edge contract instead of WindowCompat.enableEdgeToEdge():
        // AndroidX's setup helper can invoke deprecated Window bar-color
        // setters on API 35+, which Google Play reports against onCreate.
        // Transparent bar colors for API <=34 are provided by qualified
        // themes; API 35+ supplies transparent bars automatically.
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        // Android 16 predictive back gestures do not invoke Activity.onBackPressed.
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){
            @Override public void handleOnBackPressed(){handleBackNavigation();}
        });
        prefs=getSharedPreferences("brojko",MODE_PRIVATE);
        best=prefs.getInt("best",0);plays=prefs.getInt("games",0);total=prefs.getInt("total",0);
        lastScore=prefs.getInt("lastScore",0);
        stars=prefs.getInt("stars",0);rewardFlags=prefs.getInt("rewardFlags",0);
        wins=prefs.getInt("wins",0);recordTile=prefs.getInt("tile",0);
        totalMerges=prefs.getInt("totalMerges",0);streak=prefs.getInt("streak",0);
        lastDaily=prefs.getString("lastDaily","");
        dailyRewardDate=prefs.getString("dailyRewardDate","");
        statsDay=prefs.getString("statsDay","");
        weekStart=prefs.getString("weekStart","");
        weekPoints=prefs.getInt("weekPoints",0);
        undoUsedThisGame=prefs.getBoolean("undoUsedThisGame",false);
        todayMerges=prefs.getInt("todayMerges",0);todayPoints=prefs.getInt("todayPoints",0);
        playDayStreak=prefs.getInt("playDayStreak",0);
        lastPlayDay=prefs.getString("lastPlayDay","");ensureDay();
        theme=prefs.getInt("theme",0);
        tileStyle=prefs.getInt("style",0);animation=prefs.getInt("animation",1);
        sound=prefs.getBoolean("sound",true);vibration=prefs.getBoolean("vibrate",true);
        notifications=prefs.getBoolean("notifications",false);
        // Reconcile alarm state after importing a backup that disables notifications.
        AppReminder.configure(this,notifications);
        try {
            String saved=prefs.getString("session","");
            if(!saved.isEmpty())game=TileGame.fromJson(new JSONObject(saved));
            else migrateLegacy();
        }catch(Exception ignored){game=null;}
        float restoredScroll=0f;
        if(bundle!=null){
            String restored=bundle.getString("brojko_game", "");
            if(!restored.isEmpty()){
                try{game=TileGame.fromJson(new JSONObject(restored));}
                catch(Exception ignored){/* Keep the persisted session. */}
            }
            page=bundle.getString("brojko_page","home");
            pendingMode=bundle.getString("brojko_mode","classic");
            selectedHistoryIndex=bundle.getInt("brojko_history_index",0);
            chartMetric=bundle.getInt("brojko_chart_metric",0);
            restoredScroll=bundle.getFloat("brojko_scroll",0f);
            if(game==null && (page.equals("game")||page.equals("pause")||
                page.equals("win")||page.equals("over")||
                page.equals("dailyWin")||page.equals("dailyFail")))page="home";
        }
        screen=new BoardScreen(this);setContentView(screen);
        if(bundle!=null){screen.scrollOffset=restoredScroll;screen.renderedPage=page;}
        screen.postDelayed(()->{
            if("splash".equals(page))navigate("home");
        },SPLASH_DURATION_MS);
        screen.setFocusable(true);
        screen.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        screen.setContentDescription("Brojko, igra spajanja brojeva. Povucite prstom po ploči ili koristite strelice na tipkovnici.");
        ViewCompat.setOnApplyWindowInsetsListener(screen,(v,insets)->{
            androidx.core.graphics.Insets safe=insets.getInsets(
                WindowInsetsCompat.Type.systemBars()|
                WindowInsetsCompat.Type.displayCutout());
            if(v.getPaddingLeft()!=safe.left||v.getPaddingTop()!=safe.top||
               v.getPaddingRight()!=safe.right||v.getPaddingBottom()!=safe.bottom)
                v.setPadding(safe.left,safe.top,safe.right,safe.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(screen);
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
    String currentWeekStart(){
        Calendar c=Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR,-((c.get(Calendar.DAY_OF_WEEK)+5)%7));
        return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(c.getTime());
    }
    void ensureDay(){
        if(!today().equals(statsDay)){
            statsDay=today();todayMerges=0;todayPoints=0;
            rewardFlags &= ~3;
        }
        String currentWeek=currentWeekStart();
        if(weekStart.isEmpty())weekStart=currentWeek;
        else if(!weekStart.equals(currentWeek)){
            weekStart=currentWeek;weekPoints=0;rewardFlags &= ~(4|32);
        }
    }
    void reward(int bit,int amount,boolean reached){
        if(reached&&(rewardFlags&bit)==0){
            rewardFlags |= bit;stars=Math.max(0,stars+amount);
        }
    }
    void updateRewards(){
        reward(1,50,todayMerges>=20);
        reward(2,100,game!=null&&game.peak>=512&&!undoUsedThisGame);
        reward(4,150,playDayStreak>=7);
        reward(8,500,recordTile>=1024);
        reward(16,1000,best>=50000);
        reward(32,200,weekPoints>=10000);
    }
    String timeRemainingInPeriod(boolean week){
        Calendar now=Calendar.getInstance();
        Calendar end=(Calendar)now.clone();
        if(week){
            int daysUntilMonday=(Calendar.MONDAY-end.get(Calendar.DAY_OF_WEEK)+7)%7;
            end.add(Calendar.DAY_OF_YEAR,daysUntilMonday==0?7:daysUntilMonday);
        }else end.add(Calendar.DAY_OF_YEAR,1);
        end.set(Calendar.HOUR_OF_DAY,0);end.set(Calendar.MINUTE,0);
        end.set(Calendar.SECOND,0);end.set(Calendar.MILLISECOND,0);
        long remaining=Math.max(0,
            (end.getTimeInMillis()-now.getTimeInMillis())/60000);
        return week?(remaining<1440?(remaining/60)+"h":
            (remaining/1440)+"d "+((remaining%1440)/60)+"h"):
            (remaining/60)+"h "+(remaining%60)+"min";
    }
    String yesterday(){
        Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,-1);
        return new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(c.getTime());
    }
    String compactDate(){return new SimpleDateFormat("dd.MM.yyyy.",new Locale("hr","HR")).format(new Date());}
    String compactTime(){return new SimpleDateFormat("HH:mm",new Locale("hr","HR")).format(new Date());}
    String historyDate(){return new SimpleDateFormat("d. MMM yyyy.",new Locale("hr","HR")).format(new Date());}
    String dailyLongDate(){
        return new SimpleDateFormat("d. MMMM yyyy.",new Locale("hr","HR")).format(new Date());
    }
    String fmt(int x){return number.format(x);}
    String labelMode(String m) {
        return "relaxed".equals(m)?"Opušteno 5×5":"daily".equals(m)?"Dnevni izazov":"Klasično 4×4";
    }
    void updateBars() {
        // Never call deprecated Window.setStatusBarColor/NavigationBarColor.
        // Android 15 forces transparent bars: choose icon contrast, while the
        // BoardScreen itself draws the correct light/dark background behind.
        WindowInsetsControllerCompat controller=WindowCompat.getInsetsController(
            getWindow(),getWindow().getDecorView());
        boolean lightBackground=theme!=1;
        controller.setAppearanceLightStatusBars(lightBackground);
        controller.setAppearanceLightNavigationBars(lightBackground);
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
            .putString("statsDay",statsDay).putString("weekStart",weekStart)
            .putInt("weekPoints",weekPoints).putBoolean("undoUsedThisGame",undoUsedThisGame)
            .putInt("playDayStreak",playDayStreak)
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
    boolean archive(){
        if(game==null||game.archived||game.moves==0)return true;
        try {
            JSONObject row=new JSONObject();
            row.put("score",game.score).put("moves",game.moves).put("peak",game.maxTile())
                .put("mode",game.mode).put("day",historyDate())
                .put("time",compactTime()).put("won",game.reached2048);
            // Preserve a finished game even if legacy history is malformed.
            JSONArray old;
            try{old=new JSONArray(prefs.getString("history","[]"));}
            catch(Exception invalidHistory){old=new JSONArray();}
            JSONArray recent=new JSONArray();recent.put(row);
            for(int i=0;i<Math.min(200,old.length())&&recent.length()<20;i++){
                JSONObject prior=old.optJSONObject(i);
                if(prior!=null)recent.put(prior);
            }
            // A completed game must reach disk before replacing its session.
            if(!prefs.edit().putString("history",recent.toString()).commit())
                return false;
            game.archived=true;
            lastScore=game.score;
            return true;
        }catch(Exception error){return false;}
    }
    void newGame(String mode) {
        if(!archive()){
            Toast.makeText(this,"Partija nije spremljena. Pokušajte ponovno.",
                Toast.LENGTH_LONG).show();
            return;
        }
        long seed="daily".equals(mode)?
            (0x3BAF31248EA76C1DL ^ (long)today().hashCode()*0x9E3779B97F4A7C15L):
            new java.util.Random().nextLong();
        game=new TileGame(mode,"daily".equals(mode)?today():"",seed);
        undoUsedThisGame=false;
        dailyBonusGrantedThisAttempt=false;
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
            if(result.lost){
                page="daily".equals(game.mode)?"dailyFail":"over";
                persist();screen.invalidate();
            }
            return;
        }
        ensureDay();total+=result.points;totalMerges+=result.merges;
        todayPoints+=result.points;todayMerges+=result.merges;
        weekPoints+=result.points;
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
            dailyBonusGrantedThisAttempt=true;
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
        else if(result.lost){page="daily".equals(game.mode)?"dailyFail":"over";}
        if("win".equals(page)){
            String announcement="Bravo! Dosegnuli ste pločicu 2048. "
                +"Tvoj rezultat je "+fmt(game.score)+".";
            screen.setContentDescription(announcement+
                " Nastavi igrati, Nova igra ili Podijeli rezultat.");
            screen.announceForAccessibility(announcement);
        }
        persist();animate();
    }
    void endSession(){
        if(!archive()){
            Toast.makeText(this,"Partija nije spremljena. Pokušajte ponovno.",
                Toast.LENGTH_LONG).show();
            return;
        }
        game=null;page="home";persist();screen.invalidate();
    }
    void undo() {
        if(game==null||!game.canUndo())return;
        ensureDay();undoUsedThisGame=true;
        int mergeCount=game.merges;
        boolean hadWon=game.reached2048;
        int reverted=game.undo();
        total=Math.max(0,total-reverted);
        if(today().equals(statsDay))todayPoints=Math.max(0,todayPoints-reverted);
        weekPoints=Math.max(0,weekPoints-reverted);
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
        "lastDaily","dailyRewardDate","statsDay","todayMerges","todayPoints",
        "weekStart","weekPoints","undoUsedThisGame","playDayStreak",
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
        // Validate each key against its actual SharedPreferences storage
        // type before clearing any existing settings. A backup with e.g.
        // "best":"100" previously committed successfully, then getInt()
        // crashed on the next launch. Never coerce untrusted JSON values.
        for(String key:BACKUP_KEYS){
            if(!data.has(key))continue;
            Object value=data.get(key);
            boolean booleanKey=key.equals("sound")||key.equals("vibrate")||
                key.equals("notifications")||key.equals("undoUsedThisGame");
            boolean stringKey=key.equals("session")||key.equals("history")||
                key.equals("lastDaily")||key.equals("dailyRewardDate")||
                key.equals("statsDay")||key.equals("weekStart")||
                key.equals("lastPlayDay");
            if(booleanKey){
                if(!(value instanceof Boolean))
                    throw new IllegalArgumentException("Nevaljana logička postavka.");
            }else if(stringKey){
                if(!(value instanceof String))
                    throw new IllegalArgumentException("Nevaljani tekstualni podatci.");
                if(!key.equals("session")&&!key.equals("history")&&
                    ((String)value).length()>20)
                    throw new IllegalArgumentException("Nevaljani datum.");
            }else{
                if(!(value instanceof Integer)||((Integer)value)<0)
                    throw new IllegalArgumentException("Nevaljani brojčani podatci.");
                if((key.equals("theme")||key.equals("style")||
                    key.equals("animation"))&&((Integer)value)>2)
                    throw new IllegalArgumentException("Nevaljane postavke.");
            }
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
        skipPersistOnPauseAfterImport=true;
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
    void handleBackNavigation(){
        if(page.equals("game"))navigate("pause");
        else if(page.equals("pause"))navigate("game");
        else if(page.equals("historyDetail"))navigate("stats");
        else if(page.equals("privacy")||page.equals("about")||
            page.equals("language")||page.equals("backup")||page.equals("help"))
            navigate("settings");
        else if(page.equals("confirm")||page.equals("confirmEnd"))navigate(game==null?"home":"game");
        else if(page.equals("win")||page.equals("over")||page.equals("dailyWin")||page.equals("dailyFail"))navigate("home");
        else if(page.equals("splash"))navigate("home");
        else if(page.equals("home"))finish();
        else navigate("home");
    }
    @Override protected void onSaveInstanceState(Bundle out){
        // Unlocked portrait/landscape and foldable resizing recreate Activities.
        // Keep the current game, page and scroll offset rather than returning
        // to the splash screen or losing the active move after rotation.
        out.putString("brojko_page",page);
        out.putString("brojko_mode",pendingMode);
        out.putInt("brojko_history_index",selectedHistoryIndex);
        out.putInt("brojko_chart_metric",chartMetric);
        if(screen!=null)out.putFloat("brojko_scroll",screen.scrollOffset);
        if(game!=null)out.putString("brojko_game",game.toJson().toString());
        super.onSaveInstanceState(out);
    }
    @Override protected void onPause(){
        if(!skipPersistOnPauseAfterImport)persist();
        super.onPause();
    }
    @Override protected void onDestroy(){if(tones!=null){tones.release();tones=null;}super.onDestroy();}

    final class BoardScreen extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        final ArrayList<Tap> taps=new ArrayList<>();
        Canvas c;float scale,offX,offY,touchX,touchY;
        float scrollOffset,maxScroll,lastFingerY,initialFingerY;
        String renderedPage="";
        boolean pointerBoard,draggingScroll;
        Tap pressedTap;
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
            // The very light canvas matches the user-provided nine portrait
            // reference screens; retain CREAM on warm cards and buttons.
            bg=dark?0xff14233a:LIGHT_CANVAS;
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
            // Softer cream elevation matches the supplied portrait cards;
            // keep the existing dark-mode elevation and all hit rectangles.
            boolean dark=theme==1;
            shape(x,y+(dark?6f:4f),w,h,radius,
                dark?0xff0d192b:0xfff2e7d9);
            shape(x+1,y+(dark?3f:2f),w-2,h,radius,
                dark?shadow:0xfff1e8dd);
            gradient(x,y,w,h,radius,lighten(color,dark?.10f:.07f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1f);p.setColor(dark?0x30ffffff:0x72ffffff);
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
            styledTile(x,y,width,height,radius,color,tileStyle);
        }
        // Theme thumbnails must preview their own style, not the currently selected one.
        void styledTile(float x,float y,float width,float height,float radius,
                        int color,int previewStyle){
            if(width<=0||height<=0)return;
            if(previewStyle==1){shape(x,y,width,height,radius,color);return;}
            float depth=Math.min(3.2f,Math.max(1.7f,height*.047f));
            // Warm extrusion and restrained shadow used across all themes.
            shape(x+1.2f,y+depth+1.4f,Math.max(1,width-2.4f),
                Math.max(1,height-depth),radius,
                theme==1?0x60000000:0x1fa35e46);
            shape(x,y+depth,width,Math.max(1,height-depth),radius,
                darken(color,.17f));
            gradient(x,y,width,Math.max(1,height-depth),radius,
                lighten(color,previewStyle==2?.30f:.20f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(.5f,Math.min(1.1f,width*.014f)));
            p.setColor(0x49ffffff);
            c.drawRoundRect(x+1.5f,y+1.5f,x+width-1.5f,
                y+height-depth-1.3f,Math.max(2,radius-1.5f),
                Math.max(2,radius-1.5f),p);
            p.setStyle(Paint.Style.FILL);
            if(width>=24&&height>=24){
                shape(x+6,y+3,Math.max(1,width-12),
                    Math.max(1,height*.043f),radius,0x24ffffff);
                // Small upper-left reflective sweep, matching the glossy
                // highlights of the supplied colored reference tiles.
                p.setColor(0x24ffffff);p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.min(2f,width*.027f));
                Path glint=new Path();
                glint.moveTo(x+radius*.62f,y+6);
                glint.quadTo(x+6,y+6,x+5,y+radius*.78f);
                c.drawPath(glint,p);p.setStyle(Paint.Style.FILL);
            }
            if(previewStyle==2 && width>=26 && height>=26){
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
        // The home hero uses three substantial coral accent strokes on each
        // side, not the tiny radial dots used by small headings and badges.
        // Keep them completely outside the play button and its touch zone.
        void homeAccentRays(float playY,boolean onRight){
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setColor(CORAL);p.setStrokeWidth(6f);
            p.setStrokeCap(Paint.Cap.ROUND);
            // The reference has two broad marks at each side: one tilted
            // above and one horizontal beside the CTA (not a third below).
            float[][] strokes={{33,11,48,25},{22,40,43,40}};
            for(float[] endpoints:strokes){
                float x1=onRight?400-endpoints[0]:endpoints[0];
                float x2=onRight?400-endpoints[2]:endpoints[2];
                c.drawLine(x1,playY+endpoints[1],
                    x2,playY+endpoints[3],p);
            }
            p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
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
            if(content==null||content.isEmpty()||maxWidth<=0)return;
            p.setTextScaleX(1f);
            p.setTypeface(bold?(size>=26?displayFont:boldFont):regularFont);
            p.setFakeBoldText(false);
            p.setTextSize(size);
            while(p.measureText(content)>maxWidth && size>10){
                size=Math.max(10,size-1);
                p.setTypeface(bold?(size>=26?displayFont:boldFont):regularFont);
                p.setTextSize(size);
            }
            // Extremely long localized values can still exceed their card at
            // 10 units. Scale ONLY the text's x axis instead of drawing over
            // adjacent buttons or changing responsive hit areas.
            float width=p.measureText(content);
            if(width>maxWidth){
                c.save();c.translate(x,baseline);
                c.scale(maxWidth/width,1f);
                txt(content,0,0,size,color,bold);
                c.restore();
            }else txt(content,x,baseline,size,color,bold);
        }
        // Home shortcut captions must never run into neighboring cards.
        // Preserve their intended font size: only compress the horizontal
        // glyph advance when a long localized caption exceeds its card.
        void strictCaption(String content,float x,float baseline,
                           float size,float maxWidth,int color){
            p.setTypeface(regularFont);p.setFakeBoldText(false);
            p.setTextScaleX(1f);p.setTextSize(size);
            float actualWidth=p.measureText(content);
            if(actualWidth>maxWidth){
                c.save();c.translate(x,baseline);
                c.scale(maxWidth/actualWidth,1f);
                txt(content,0,0,size,color,false);
                c.restore();
            }else txt(content,x,baseline,size,color,false);
        }
        // Reference screenshot typography is left-aligned in settings rows.
        // Shrink only when the actual Nunito glyphs would overlap controls.
        void leftFitted(String content,float left,float baseline,float size,
                        float maxWidth,int color,boolean bold){
            if(content==null||content.isEmpty()||maxWidth<=0)return;
            p.setTypeface(bold?(size>=26?displayFont:boldFont):regularFont);
            p.setTextScaleX(1f);
            p.setFakeBoldText(false);p.setTextSize(size);
            while(p.measureText(content)>maxWidth&&size>10){
                size=Math.max(10,size-.5f);
                p.setTypeface(bold?(size>=26?displayFont:boldFont):regularFont);
                p.setTextSize(size);
            }
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);
            p.setColor(color);p.setTextAlign(Paint.Align.LEFT);
            p.setStrokeWidth(0);
            float drawnWidth=p.measureText(content);
            if(drawnWidth>maxWidth){
                c.save();c.translate(left,baseline);c.scale(maxWidth/drawnWidth,1f);
                c.drawText(content,0,0,p);c.restore();
            }else c.drawText(content,left,baseline,p);
            p.setTextAlign(Paint.Align.CENTER);
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
            }else if("gift".equals(type)){
                // Original-style purple reward box with gold bow.
                p.setShader(new LinearGradient(3,9,21,24,
                    0xffb27cff,0xff7840e1,Shader.TileMode.CLAMP));
                c.drawRoundRect(2,10,22,22,3,3,p);p.setShader(null);
                p.setColor(0xff6937ce);c.drawRoundRect(2,19,22,23,2,2,p);
                p.setShader(new LinearGradient(0,8,0,14,
                    0xffbf93ff,0xff9253ee,Shader.TileMode.CLAMP));
                c.drawRoundRect(1,9,23,14,2,2,p);p.setShader(null);
                p.setColor(0xffffc238);c.drawRoundRect(10.4f,9,13.6f,22,1,1,p);
                c.drawRoundRect(1,10,23,12,1,1,p);
                Path leftBow=new Path();
                leftBow.moveTo(12,10);leftBow.cubicTo(3,-1,0,7,8,10);
                leftBow.cubicTo(9,10.5f,11,11,12,10);
                Path rightBow=new Path();
                rightBow.moveTo(12,10);rightBow.cubicTo(21,-1,24,7,16,10);
                rightBow.cubicTo(15,10.5f,13,11,12,10);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.8f);
                p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xffffdb55);
                c.drawPath(leftBow,p);c.drawPath(rightBow,p);
                p.setStyle(Paint.Style.FILL);p.setColor(0xffffe379);
                c.drawCircle(12,10,2.1f,p);
            }else if("gamepad".equals(type)){
                Path pad=new Path();
                pad.moveTo(6,6);pad.cubicTo(8,4,16,4,18,6);
                pad.cubicTo(21,8,24,17,22,20);
                pad.cubicTo(20,23,17,17,15,17);
                pad.lineTo(9,17);pad.cubicTo(7,17,4,23,2,20);
                pad.cubicTo(0,17,3,8,6,6);pad.close();
                // Small raised navy controller, as on the reference KPI.
                c.save();c.translate(0,1.15f);
                p.setColor(0x35081327);c.drawPath(pad,p);c.restore();
                p.setShader(new LinearGradient(0,4,0,22,
                    lighten(color,.20f),darken(color,.11f),
                    Shader.TileMode.CLAMP));
                c.drawPath(pad,p);p.setShader(null);
                p.setColor(WHITE);c.drawRoundRect(6,9,8,15,1,1,p);
                c.drawRoundRect(4,11,10,13,1,1,p);
                c.drawCircle(16,10,1.5f,p);c.drawCircle(19,13,1.5f,p);
            }else if("grid".equals(type)){
                for(int row=0;row<2;row++){
                    for(int col=0;col<2;col++){
                        float gx=2+col*11,gy=2+row*11;
                        p.setColor(0x26a05d18);
                        c.drawRoundRect(gx,gy+1.15f,gx+9,gy+10.15f,
                            2.5f,2.5f,p);
                        p.setShader(new LinearGradient(gx,gy,gx,gy+9,
                            lighten(color,.25f),color,Shader.TileMode.CLAMP));
                        c.drawRoundRect(gx,gy,gx+9,gy+9,2.5f,2.5f,p);
                        p.setShader(null);
                    }
                }
            }else if("star".equals(type)){
                // Rounded, raised golden star used for *earned* rewards.
                // Its geometry remains vector-drawn at every Android density.
                float[] xs=new float[10],ys=new float[10];
                for(int i=0;i<10;i++){
                    double angle=-Math.PI/2+i*Math.PI/5;
                    // Only the victory card uses the soft, pillow-like points
                // of the owner's 941x1672 reward illustration.
                float radius=i%2==0?(page.equals("win")?10.2f:10.6f):
                    (page.equals("win")?5.15f:5.35f);
                    xs[i]=12+(float)Math.cos(angle)*radius;
                    ys[i]=12+(float)Math.sin(angle)*radius;
                }
                Path star=new Path();
                for(int i=0;i<10;i++){
                    int prev=(i+9)%10,next=(i+1)%10;
                    float corner=page.equals("win")?.73f:.82f;
                    float startX=xs[i]*corner+xs[prev]*(1f-corner);
                    float startY=ys[i]*corner+ys[prev]*(1f-corner);
                    float endX=xs[i]*corner+xs[next]*(1f-corner);
                    float endY=ys[i]*corner+ys[next]*(1f-corner);
                    if(i==0)star.moveTo(startX,startY);
                    else star.lineTo(startX,startY);
                    star.quadTo(xs[i],ys[i],endX,endY);
                }
                star.close();
                // Statistics uses a BLUE star; mission rewards are gold.
                // Respect the supplied accent instead of recoloring every star.
                boolean golden=Color.red(color)>200 &&
                    Color.green(color)>100&&Color.blue(color)<140;
                boolean victoryGold=golden&&page.equals("win");
                int rim=victoryGold?0xffe88b10:
                    golden?0xffe48408:darken(color,.28f);
                int top=victoryGold?0xffffdc65:
                    golden?0xffffed87:lighten(color,.43f);
                // The victory stars are filled golden-orange with soft tips,
                // rather than the lighter outline-like mission style.
                int edge=victoryGold?0xffffa722:
                    golden?0xffff9f19:lighten(color,.69f);
                c.save();c.translate(0,1.6f);
                p.setColor(rim);c.drawPath(star,p);
                c.restore();
                p.setShader(new LinearGradient(0,2,0,23,
                    top,color,Shader.TileMode.CLAMP));
                c.drawPath(star,p);p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setStrokeWidth(victoryGold?.72f:1.05f);
                p.setColor(edge);c.drawPath(star,p);
                p.setStyle(Paint.Style.FILL);
                // Keep the original broad glint outside the victory modal;
                // its 3D stars instead have a subtle warm highlight.
                p.setShader(new LinearGradient(6,4,17,12,
                    victoryGold?0x40ffffff:0x7fffffff,
                    0x00ffffff,Shader.TileMode.CLAMP));
                c.drawOval(6,5,17,10,p);
                p.setShader(null);
            }else if("flame".equals(type)){
                Path flame=new Path();
                flame.moveTo(12,1);
                flame.cubicTo(15,6,15,8,17,10);
                flame.cubicTo(18,7,19,6,19,5);
                flame.cubicTo(25,12,23,17,20,21);
                flame.cubicTo(16,25,8,25,4,20);
                flame.cubicTo(0,15,5,8,12,1);
                flame.close();
                c.save();c.translate(0,1f);p.setColor(0x270b1740);
                c.drawPath(flame,p);c.restore();
                // setColor(0x27...) above also sets Paint alpha to 15%.
                // Restore opaque paint before the Daily streak gradient;
                // otherwise the orange flame looks washed-out grey.
                if("daily".equals(page))p.setColor(WHITE);
                p.setShader(new LinearGradient(4,2,18,23,
                    lighten(color,.26f),darken(color,.07f),
                    Shader.TileMode.CLAMP));
                c.drawPath(flame,p);p.setShader(null);
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
               "gift".equals(type)||"target".equals(type)||"palette".equals(type)||
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
                case "chevron-right":
                    z.moveTo(9,5);z.lineTo(16,12);z.lineTo(9,19);break;
                case "swap":
                    z.moveTo(3,7);z.lineTo(21,7);z.lineTo(17,3);
                    z.moveTo(21,7);z.lineTo(17,11);
                    z.moveTo(21,17);z.lineTo(3,17);z.lineTo(7,13);
                    z.moveTo(3,17);z.lineTo(7,21);break;
                case "clock":
                    c.drawCircle(12,12,9,p);
                    z.moveTo(12,6);z.lineTo(12,12);z.lineTo(17,15);break;
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
                case "calendar-outline":
                    // Daily-date capsule uses the simple navy line-calendar
                    // from the source, not the decorated calendar/star emblem.
                    p.setStrokeWidth(2.3f);
                    c.drawRoundRect(3,5,21,21,3,3,p);
                    z.moveTo(3,10);z.lineTo(21,10);
                    z.moveTo(8,2);z.lineTo(8,8);
                    z.moveTo(16,2);z.lineTo(16,8);
                    break;
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
                case "phone-vibrate":
                    c.drawRoundRect(8,3,16,21,2,2,p);
                    z.moveTo(5,7);z.quadTo(2,12,5,17);
                    z.moveTo(19,7);z.quadTo(22,12,19,17);
                    break;
                case "cloud-backup":
                    z.moveTo(7,18);z.cubicTo(2,18,2,11,7,10);
                    z.cubicTo(9,4,16,5,18,9);
                    z.cubicTo(24,10,24,18,18,18);
                    z.lineTo(7,18);
                    z.moveTo(10,14);z.cubicTo(11,11,15,11,16,13);
                    z.moveTo(16,11);z.lineTo(16,14);z.lineTo(13,14);
                    break;
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
                // The original daily-start pill has a shallow lip, unlike the
                // higher raised game buttons. Change ONLY this one CTA.
                boolean flatDaily=page.equals("daily");
                boolean homeHero=page.equals("home")&&w>=280;
                boolean victoryPrimary=page.equals("win")&&
                    "Nastavi igrati".equals(label);
                boolean victoryShare=page.equals("win")&&
                    "Podijeli rezultat".equals(label);
                float lip=flatDaily?2.5f:homeHero?6f:
                    (victoryPrimary||victoryShare?2.7f:5f);
                // Home source has a warm coral top and a deeper red-orange
                // bottom. The original HERO button used an almost flat
                // salmon gradient despite matching geometry.
                shape(x,y+lip,w,h,radius,
                    flatDaily?0xffd94a33:
                    victoryPrimary?0xffd8432f:
                    victoryShare?0xff2467c9:
                    homeHero?0xffda4932:darken(color,flatDaily?.13f:.23f));
                gradient(x,y,w,h-(flatDaily?2:homeHero?3:4),radius,
                    flatDaily?0xffff8a60:
                    victoryPrimary?0xffff8054:
                    victoryShare?0xff66b0fc:
                    homeHero?0xffff8554:lighten(color,flatDaily?.10f:.18f),
                    flatDaily?0xffff593a:
                    victoryPrimary?0xffff5134:
                    victoryShare?0xff2f83ec:
                    homeHero?0xffff5837:color);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(flatDaily?.9f:
                    (victoryPrimary||victoryShare?.75f:1.2f));
                p.setColor(flatDaily||victoryPrimary||victoryShare?
                    0x48ffffff:0x86ffffff);
                c.drawRoundRect(x+3,y+2,x+w-3,
                    y+h-(flatDaily?5:8),radius-3,radius-3,p);
                p.setStyle(Paint.Style.FILL);
            }else {
                // Game action cards in the supplied 4x4 screenshot have a
                // warm, raised cream surface; the previous near-white fill
                // blended into the canvas. Leave small header controls and
                // every other screen unchanged.
                boolean raisedGameAction=page.equals("game")&&w>=100&&h>=77;
                boolean victorySecondary=page.equals("win")&&
                    "Nova igra".equals(label)&&theme!=1;
                if(victorySecondary){
                    // A warm, raised but borderless reference pill. Do not
                    // alter the same shared component outside the win modal.
                    shape(x,y+1.7f,w,h-1,radius,0xffe6d4c0);
                    gradient(x,y,w,h-2.2f,radius,
                        0xfffff7ea,0xfff0e3d1);
                }else{
                    card(x,y,w,h,radius,
                        raisedGameAction&&theme!=1?0xfffff3e5:color);
                }
            }
            boolean hasSymbol=symbol!=null;
            int iconColor=prominent?WHITE:ink;
            if(hasSymbol&&w<150&&h>=77){
                icon(symbol,x+w/2,y+h*.30f,29,iconColor);
                fitted(label,x+w/2,y+h*.78f,21,w-14,prominent?WHITE:ink,true);
            }else{
                boolean homeHero=page.equals("home")&&prominent&&w>=280;
                if(hasSymbol){
                    boolean winSecondary=page.equals("win")&&
                        "Nova igra".equals(label);
                    boolean winShare=page.equals("win")&&
                        "Podijeli rezultat".equals(label);
                    float iconX=x+(winSecondary?78f:winShare?69f:
                        prominent?(page.equals("daily")&&
                        referencePortraitLayout&&w>=300?80f:
                        Math.min(58f,w*.21f)):25f);
                    if(winSecondary)
                        victoryRestartIcon(iconX,y+h/2f,29f,iconColor);
                    else if(page.equals("daily")&&prominent&&
                        "play".equals(symbol))
                        dailyPlayGlyph(iconX,y+h/2f,32f,iconColor);
                    else icon(symbol,iconX,y+h/2,
                        Math.min(prominent?
                            (page.equals("daily")?34:homeHero?43:33):23,
                            h*(page.equals("daily")?.65f:
                                homeHero?.57f:.52f)),iconColor);
                }
                float textCenter=x+(hasSymbol?(prominent?
                    (homeHero&&label.length()>10?w*.62f:w*.58f):
                    (page.equals("win")&&"Nova igra".equals(label)?
                        w*.59f:(w+38f)/2f)):w/2f);
                int labelColor=prominent?WHITE:ink;
                float font=prominent?
                    (page.equals("daily")?Math.min(24,h*.44f):
                        homeHero?(label.length()>10?
                            Math.min(30,h*.37f):Math.min(36,h*.44f)):
                            Math.min(27,h*.34f)):
                    (h>=66?18:16);
                fitted(label,textCenter,y+h/2+font*.35f,font,
                    hasSymbol?(prominent?w*.76f:w-52):w-20,labelColor,true);
            }
            // Icon-only actions need localized TalkBack labels and a full touch target.
            boolean iconOnly=label==null||label.isEmpty();
            String accessibleLabel=label;
            if(iconOnly) {
                if("back".equals(symbol))accessibleLabel="Natrag";
                else if("pause".equals(symbol))accessibleLabel="Pauziraj igru";
                else if("undo".equals(symbol))accessibleLabel="Poništi potez";
                else accessibleLabel=symbol==null?"Akcija":symbol;
            }
            float extraX=iconOnly?Math.max(0,(54f-w)/2):0;
            float extraY=iconOnly?Math.max(0,(54f-h)/2):0;
            taps.add(new Tap(Math.max(0,x-extraX),Math.max(0,y-extraY),
                Math.min(400,x+w+extraX),y+h+extraY,accessibleLabel,action));
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
            brand(y,size,true);
        }
        void brand(float y,float size,boolean includeByline) {
            p.setTypeface(displayFont);
            p.setTextSize(size);
            float left=p.measureText("Br"),right=p.measureText("jko");
            float gap=size*.09f,tile=size*.74f;
            float total=left+right+tile+2*gap;
            float start=200-total/2;
            txt("Br",start+left/2,y,size,ink,true);
            float bx=start+left+gap;
            // Keep brand emblem independent of gameplay tile style.
            styledTile(bx,y-size*.78f,tile,tile,Math.max(7,size*.16f),CORAL,0);
            rays(bx+tile*.5f,y-size*.79f,size*.34f,CORAL);
            txt("2",bx+tile/2,y-size*.20f,size*.52f,WHITE,true);
            txt("jko",bx+tile+gap+right/2,y,size,ink,true);
            if(includeByline)
                txt("by Brendigo",265,y+Math.max(23,size*.43f),15,muted,true);
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
            gridY=compactBoardLayout&&game!=null&&game.size==4?159:(compactBoardLayout?171:222);
            boolean extended=page.equals("stats");
            // Keep buttons touch-sized on 16:9 screens; permit scrolling
            // outside the board rather than scaling the whole game to tiny size.
            // Splash uses its own width-fitted short-portrait arrangement.
            // Using the 880-unit tall layout shrank the reference by ~19%.
            // Decorative edge tiles may be cropped, but logo/slogan must fit.
            boolean compact=referencePortraitLayout||
                (!page.equals("splash")&&extended);
            // Landscape phones and resizable tablets must keep the whole
            // interaction region reachable; portrait stays pixel-identical.
            boolean landscape=usableWidth>usableHeight;
            scale=landscape?Math.min(usableWidth/400f,usableHeight/720f):
                (compact?usableWidth/400f:
                    Math.min(usableWidth/400f,usableHeight/880f));
            float contentHeight=page.equals("stats")?
                (referencePortraitLayout?
                    (statsTouchExploration()?
                        Math.max(710f,545f+Math.min(5,history().length())*48f):
                        710f)
                    :1120f):
                // All Home shortcuts end at y=690 plus their card shadows.
                // The old 880-unit height caused ~170 units of empty scrolling
                // at the original 941x1672 reference viewport. Preserve real
                // scrolling on shorter devices without cropping any shortcut.
                ((page.equals("pause")||page.equals("confirm")||
                    page.equals("confirmEnd"))&&referencePortraitLayout?654f:
                (page.equals("over")&&referencePortraitLayout?705f:
                ((page.equals("dailyWin")||page.equals("dailyFail"))&&
                    referencePortraitLayout?700f:
                (page.equals("home")&&referencePortraitLayout?710f:
                ((page.equals("settings")||page.equals("themes")||
                    page.equals("missions")||page.equals("daily"))&&
                    referencePortraitLayout?710f:880f)))));
            // Splash must never scroll; its compact lettering fits a 360x600
            // viewport while longer non-splash pages still scroll as needed.
            maxScroll=page.equals("splash")?0:
                (compact?Math.max(0,contentHeight-usableHeight/scale):0);
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
                case "win":
                    if(referencePortraitLayout)victoryBackdropPortrait();
                    else gameScreen();
                    taps.clear();resultOverlay(true);break;
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
            // Shared press highlight for all visible interactive UI controls.
            if(pressedTap!=null&&!pointerBoard&&!draggingScroll){
                float w=pressedTap.right-pressedTap.left;
                float h=pressedTap.bottom-pressedTap.top;
                if(w>5&&h>5){
                    shape(pressedTap.left+1,pressedTap.top+1,
                        w-2,h-2,Math.min(20,Math.min(w,h)*.25f),
                        theme==1?0x2dffffff:0x20051933);
                }
            }
            c.restore();
        }
        void bottomBrand(){txt("BROJKO  •  BY BRENDIGO",200,852,12,muted,true);}
        void splashAmbient(float x,float y,float width,float height,
                           float rotation,int color){
            // Decorative distant tiles: soft-focus silhouettes only.
            // Keep all blur drawing local to Splash, not active game cells.
            c.save();c.rotate(rotation,x+width/2,y+height/2);
            p.clearShadowLayer();p.setStyle(Paint.Style.FILL);p.setShader(null);
            p.setColor(color);p.setShadowLayer(24f,0,0,color);
            c.drawRoundRect(x,y,x+width,y+height,17,17,p);
            p.clearShadowLayer();c.restore();
        }
        void splashFloatingTile(float x,float y,float w,float h,
                                float radius,int color){
            // Native illustration-like extrusion and highlight. Other
            // screens continue using their established styledTile renderer.
            p.clearShadowLayer();p.setStyle(Paint.Style.FILL);
            p.setShader(null);p.setColor(0xfff8eee2);
            p.setShadowLayer(13f,0f,7f,0x3ca9835a);
            c.drawRoundRect(x+1.7f,y+2.4f,x+w+1.2f,y+h+1.8f,
                radius,radius,p);
            p.clearShadowLayer();
            shape(x+1f,y+4.5f,w-1f,h-3f,radius,darken(color,.13f));
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,w,h-4f,radius,
                lighten(color,.20f),darken(color,.035f));
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.5f);p.setColor(0x77ffffff);
            c.drawRoundRect(x+2,y+1.6f,x+w-2,y+h-6.7f,
                radius-2,radius-2,p);
            p.setStrokeWidth(1.8f);p.setColor(0x42ffffff);
            Path gleam=new Path();
            gleam.moveTo(x+radius*.64f,y+5f);
            gleam.quadTo(x+5,y+5,x+5,y+radius*.75f);
            c.drawPath(gleam,p);
            p.setStyle(Paint.Style.FILL);p.setStrokeWidth(0);
        }
        void splashAccentStroke(float x1,float y1,float x2,float y2,
                                int color){
            p.clearShadowLayer();p.setShader(null);p.setColor(color);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7.4f);
            p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(x1,y1,x2,y2,p);
            p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
        }
        void splashEdgeGlowTile(){
            // The reference's lower-right 256 is intentionally out of focus.
            // Draw a softly lit BLUE distant tile rather than a sharp,
            // foreground purple game tile. It is never a playable tile.
            c.save();c.rotate(-15f,375f,714f);
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.FILL);p.setColor(0xff539aff);
            p.setShadowLayer(26f,0,0,0x995295fa);
            c.drawRoundRect(298,648,435,775,25,25,p);
            p.clearShadowLayer();
            p.setColor(0xa3449af5);p.setShadowLayer(12f,0,0,0x774498ef);
            c.drawRoundRect(302,652,435,771,24,24,p);
            p.clearShadowLayer();
            p.setColor(0xd6ffffff);p.setTypeface(displayFont);
            p.setTextAlign(Paint.Align.CENTER);p.setTextSize(45);
            p.setShadowLayer(7f,0,0,0xaaffffff);
            c.drawText("256",364,726,p);
            p.clearShadowLayer();c.restore();
        }
        // Splash-only wordmark: the source uses wider rounded lettering,
        // a shorter coral number tile and three pronounced accent strokes.
        // Never substitute a reference bitmap: draw each part natively so
        // the screen remains responsive and the brand is theme-aware.
        void splashWordmark(){
            final float y=399f,size=116f;
            p.setTypeface(displayFont);
            p.setTextSize(size);
            p.setTextScaleX(1f);
            float left=p.measureText("Br"),right=p.measureText("jko");
            float gap=size*.09f,tile=size*.74f;
            float start=200f-(left+right+tile+2f*gap)/2f;
            float bx=start+left+gap;

            c.save();
            c.scale(.80f,1.19f,200f,405f);
            // The navy B/r in the reference have a broader silhouette.
            c.save();
            c.translate(-10.6f,0);
            c.scale(1.185f,1f,3.6f,y);
            txt("Br",start+left/2f,y,size,ink,true);
            c.restore();

            // Reference: coral 2 tile about 146 x 158 native pixels.
            // Preserve its native shaded bevel, but remove the old extra
            // vertical stretch and align its bottom with the letterforms.
            c.save();
            c.translate(-4.25f,2.2f);
            c.scale(.90f,.66f,137.6f,394.36f);
            styledTile(bx,y-size*.78f,tile,tile,
                Math.max(7,size*.16f),CORAL,0);
            txt("2",bx+tile/2f,y-size*.20f,size*.52f,WHITE,true);
            c.restore();

            // The source's jko starts immediately after the number tile.
            c.save();
            c.translate(-36.2f,0);
            c.scale(1.25f,.92f,228f,316f);
            txt("jko",bx+tile+gap+right/2f,y,size,ink,true);
            c.restore();
            c.restore();

            // Accent strokes must sit *above* the number tile, not behind it.
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setColor(CORAL);
            p.setStrokeWidth(8.5f);p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(133,310,148,322,p);
            c.drawLine(155,283,166,307,p);
            c.drawLine(195,282,184,307,p);
            p.setStrokeCap(Paint.Cap.BUTT);
            p.setStyle(Paint.Style.FILL);
        }
        void splashPage(){
            if(referencePortraitLayout){
                // Source 941x1672 uses full width; only edge decorations crop.
                // All information stays inside the 400x~667 compact viewport.
                splashAmbient(161,24,40,43,-18,0x1fffbd94);
                splashAmbient(167,198,35,38,14,0x12eabb93);
                splashAmbient(84,475,71,67,-17,0x252ec4b6);
                splashAmbient(321,444,79,78,20,0x21ff7a5b);
                splashAmbient(111,633,66,67,13,0x25ffba30);
                splashAmbient(173,636,65,65,29,0x282ec4b6);
                // Reference accents sit OUTSIDE raised tiles.
                splashAccentStroke(51,65,60,82,CORAL);
                splashAccentStroke(36,88,49,94,CORAL);
                splashAccentStroke(341,77,335,92,TEAL);
                splashAccentStroke(349,100,336,104,TEAL);
                c.save();c.rotate(-18,115,111);
                splashFloatingTile(63,62,104,101,24,TILES[1]);
                txt("2",115,134,57,WHITE,true);c.restore();
                c.save();c.rotate(17,289,123);
                splashFloatingTile(239,76,101,101,24,TILES[2]);
                txt("4",289,145,57,WHITE,true);c.restore();
                c.save();c.rotate(-17,42,236);
                splashFloatingTile(-12,187,106,100,22,TILES[3]);
                txt("8",42,256,56,WHITE,true);c.restore();
                c.save();c.rotate(18,374,234);
                splashFloatingTile(323,182,104,104,24,TILES[4]);
                txt("16",375,255,46,WHITE,true);c.restore();
                splashAccentStroke(92,499,102,507,CORAL);
                splashAccentStroke(89,525,100,531,CORAL);
                c.save();c.rotate(16,21,509);
                splashFloatingTile(-23,459,97,100,23,TILES[7]);
                txt("64",25,523,46,WHITE,true);c.restore();
                splashEdgeGlowTile();
                splashWordmark();
                txt("by Brendigo",284,417,19,muted,true);
                // Native 941x1672 reference: cream slogan pill and both
                // phrases sit about 10 design units higher than before.
                // Keep the source's compact gap between coral and teal.
                card(17,552,366,58,30,theme==1?card:0xfffff0db);
                fitted("Spajaj brojeve.",127,594,24.2f,187,CORAL,true);
                fitted("Ruši rekorde.",287,594,23.8f,179,TEAL,true);
                return;
            }
            // Taller devices retain the full-height composition.
            // Programmatic branded splash based on the provided portrait reference.
            c.save();c.rotate(-19,59,140);
            splashFloatingTile(-18,74,133,124,25,TILES[1]);
            txt("2",49,157,64,WHITE,true);c.restore();
            c.save();c.rotate(21,353,142);
            splashFloatingTile(288,79,129,121,24,TILES[2]);
            txt("4",353,155,64,WHITE,true);c.restore();
            c.save();c.rotate(-22,28,283);
            splashFloatingTile(-34,246,107,102,23,TILES[3]);
            txt("8",20,316,57,WHITE,true);c.restore();
            c.save();c.rotate(18,379,297);
            splashFloatingTile(329,247,111,109,22,TILES[4]);
            txt("16",383,324,49,WHITE,true);c.restore();
            c.save();c.rotate(17,23,661);
            splashFloatingTile(-34,610,112,111,25,TILES[7]);
            txt("64",21,679,46,WHITE,true);c.restore();
            c.save();c.rotate(-15,374,713);
            styledTile(327,666,118,111,23,TILES[8],0);
            txt("256",386,734,38,WHITE,true);c.restore();
            brand(487,108);
            card(26,704,348,77,32,theme==1?card:0xfffff0db);
            fitted("Spajaj brojeve.",128,751,22,192,CORAL,true);
            fitted("Ruši rekorde.",290,751,22,182,TEAL,true);
        }
        // Home is a dedicated illustrated surface: softer cream cards and
        // ambient shadows belong here, not on shared in-game controls.
        void homeSoftCard(float x,float y,float w,float h,float radius,int color){
            if(theme==1||!referencePortraitLayout){
                card(x,y,w,h,radius,color);return;
            }
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xfffdf7ec);
            p.setShadowLayer(9f,0f,5.5f,0x28a78353);
            c.drawRoundRect(x,y,x+w,y+h,radius,radius,p);
            p.clearShadowLayer();
            gradient(x,y,w,h,radius,lighten(color,.40f),color);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.85f);p.setColor(0xb4ffffff);
            c.drawRoundRect(x+1,y+1,x+w-1,y+h-1,radius-1,radius-1,p);
            p.setStyle(Paint.Style.FILL);
        }
        void homeFloatingTile(float x,float y,float w,float h,float radius,
                              int color,int value){
            // The source artwork floats over a warm, defocused shadow.
            // Decorative only; no fake values are added to the game state.
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.FILL);p.setColor(0x349e826f);
            p.setShadowLayer(15f,0f,9f,0x3abf9b73);
            c.drawRoundRect(x+3f,y+7f,x+w+3f,y+h+7f,radius,radius,p);
            p.clearShadowLayer();
            styledTile(x,y,w,h,radius,color,0);
            fitted(Integer.toString(value),x+w/2f,y+h*.68f,
                value>=16?40:47,w-12,WHITE,true);
        }
        void homeBestMiniChart(float x,float baseline){
            // The source's best-score card has three pale bars at its end.
            for(int i=0;i<3;i++){
                float height=10+i*5.5f;
                shape(x+i*7.1f,baseline-height,5.6f,height,2.1f,
                    theme==1?0xff65758e:0xffe4e0d9);
            }
        }
        void home(){
            boolean portrait=referencePortraitLayout;
            c.save();c.rotate(-16,35,76);
            homeFloatingTile(-4,3,85,80,18,TILES[1],2);c.restore();
            c.save();c.rotate(15,365,56);
            homeFloatingTile(324,4,83,81,18,TILES[2],4);c.restore();
            c.save();c.rotate(-12,11,176);
            homeFloatingTile(-37,143,71,70,15,TILES[3],8);c.restore();
            c.save();c.rotate(9,390,179);
            homeFloatingTile(359,147,77,73,16,TILES[4],16);c.restore();
            // Use the geometrically verified Splash wordmark on Home,
            // scaled to the Home source artwork's native bounds. This keeps
            // the navy rounded silhouette, raised "2", and coral rays in sync
            // with screen 1 without modifying any other page.
            if(portrait){
                c.save();
                c.translate(0f,-129f);
                c.scale(.775f,.74f,200f,0f);
                splashWordmark();
                c.restore();
                txt("by Brendigo",265,184,15,muted,true);
            }else brand(197,72);
            if(!portrait)
                fitted("Spajaj brojeve. Ruši rekorde.",200,245,22,367,CORAL,true);
            float scoresY=portrait?205:269;
            float scoresH=portrait?74:80;
            homeSoftCard(21,scoresY,171,scoresH,19,
                theme==1?card:0xfffcf5e9);
            homeSoftCard(207,scoresY,171,scoresH,19,
                theme==1?card:0xfffcf5e9);
            icon("crown",53,scoresY+29,28,0xffffb228);
            fitted("Najbolji rezultat",132,scoresY+29,13,117,muted,true);
            // Source-matched 12.560 remains clear of the three right-hand
            // mini bars even for larger real records.
            fitted(fmt(best),112,scoresY+60,27,84,ink,true);
            if(portrait)homeBestMiniChart(159,scoresY+58);
            icon("stats",236,scoresY+30,27,0xff63799d);
            fitted("Zadnji rezultat",portrait?301:319,scoresY+29,
                13,portrait?104:116,muted,true);
            fitted(fmt(lastScore),portrait?296:311,scoresY+60,
                27,portrait?113:125,ink,true);
            if(portrait)
                icon("chevron-right",365,scoresY+39,17,muted);
            // Source score cards are actual shortcuts, not dead display art.
            taps.add(new Tap(21,scoresY,192,scoresY+scoresH,
                "Najbolji rezultat",()->navigate("stats")));
            taps.add(new Tap(207,scoresY,378,scoresY+scoresH,
                "Zadnji rezultat",()->navigate("stats")));
            boolean resumable=game!=null&&!game.archived&&!game.gameOver;
            float playY=portrait?293:372;
            float playH=portrait?82:86;
            // Reference orange hero pill is about 285 design units wide,
            // leaving room for the large bursts on both sides.
            button(resumable?"Nastavi igru":"Nova igra","play",
                portrait?58:43,playY,portrait?284:314,playH,CORAL,true,
                ()->{if(resumable)navigate("game");else requestNew("classic");});
            if(portrait){
                homeAccentRays(playY,false);
                homeAccentRays(playY,true);
            }else{
                rays(24,playY+playH*.45f,21,CORAL);
                rays(376,playY+playH*.45f,21,CORAL);
            }
            float modeY=portrait?386:483;
            float modeH=portrait?171:187;
            modeCard("Klasično","4 × 4",15,modeY,117,modeH,0xfffff4ed,false,
                ()->requestNew("classic"));
            modeCard("Opušteno","5 × 5",141,modeY,117,modeH,0xffe9fcfa,false,
                ()->requestNew("relaxed"));
            modeCard("Dnevni izazov","Novi izazov svaki dan!",
                267,modeY,117,modeH,0xfffff4d7,true,()->navigate("daily"));
            float shortcutY=portrait?570:687;
            float shortcutH=portrait?120:144;
            shortcut("Misije","target","Ispuni ciljeve","i osvoji nagrade!",
                16,shortcutY,88,shortcutH,card,()->navigate("missions"));
            shortcut("Statistika","stats","Prati svoj","napredak.",
                109,shortcutY,88,shortcutH,card,()->navigate("stats"));
            shortcut("Teme","palette","Otkrij nove","izglede.",
                202,shortcutY,88,shortcutH,card,()->navigate("themes"));
            shortcut("Postavke","settings","Prilagodi","igru sebi.",
                295,shortcutY,88,shortcutH,card,()->navigate("settings"));
        }
        void shortcut(String label,String glyph,String sub1,String sub2,
                      float x,float y,float w,float h,int fill,Runnable action){
            boolean portrait=referencePortraitLayout;
            homeSoftCard(x,y,w,h,19,theme==1?card:0xfffaf3e8);
            icon(glyph,x+w/2,y+(portrait?31:39),portrait?38:43,
                glyph.equals("target")?CORAL:
                glyph.equals("stats")?0xff344868:
                glyph.equals("palette")?0xffb05de9:ink);
            fitted(label,x+w/2,y+(portrait?70:91),portrait?15:16,w-5,ink,true);
            strictCaption(sub1,x+w/2,y+(portrait?87:115),
                portrait?11:12,w-6,muted);
            strictCaption(sub2,x+w/2,y+(portrait?101:132),
                portrait?11:12,w-6,muted);
            taps.add(new Tap(x,y,x+w,y+h,label,action));
        }
        void modeCard(String name,String sub,float x,float y,float w,float h,int fill,
                      boolean daily,Runnable tap){
            boolean portrait=referencePortraitLayout;
            homeSoftCard(x,y,w,h,22,theme==1?card:fill);
            if(daily){
                // Actual emulator comparison: the existing vector calendar
                // tracks the owner's source better than the bulkier trial.
                icon("calendar",x+w/2,y+(portrait?68:77),69,CORAL);
            }else{
                int[] values={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
                int cols="Opušteno".equals(name)?5:4;
                float space=cols==5?2.1f:2.2f;
                float side=cols==5?17.0f:21.5f;
                float left=x+(w-(side+space)*cols+space)/2;
                float top=y+(portrait?14:(cols==5?20:19));
                // Decorative Home preview only; the actual 5x5 engine is
                // unchanged. Allocate the palette once per thumbnail.
                // The relaxed thumbnail is a fixed, *decorative*
                // sample, not the active game board. The old index
                // formula produced purple in row 2 and coral in the
                // fifth slot of row 1, unlike the supplied 5x5 artwork.
                // Keep runtime game spawn and tileColor unchanged.
                int[] relaxedPreview={
                    TILES[1],TILES[2],TILES[3],TILES[4],0xfff1e3b1,
                    TILES[2],TILES[3],TILES[4],TILES[1],TILES[6],
                    TILES[3],TILES[4],TILES[1],TILES[6],TILES[7],
                    TILES[6],0xffff6193,TILES[7],TILES[3],TILES[10],
                    TILES[7],TILES[8],TILES[3],TILES[10],TILES[10]
                };
                for(int i=0;i<cols*cols;i++){
                    float px=left+(i%cols)*(side+space);
                    float py=top+(i/cols)*(side+space);
                    int v=values[Math.min(values.length-1,i)];
                    int col=cols==5?relaxedPreview[i]:tileColor(v);
                    glossyTile(px,py,side,side,4,col);
                    if(cols==4)fitted(""+v,px+side/2,py+side*.67f,
                        v>99?8.4f:12,side-2,WHITE,true);
                }
            }
            // Native pixel audit: 4x4/5x5 headings were ~27 px too high,
            // whereas the daily heading was ~6 px high at 941x1672.
            // Only the Home thumbnail typography is repositioned.
            float captionX=x+w/2f+(portrait&&!daily?5f:0f);
            float captionY=y+(portrait?(daily?128f:137f):150f);
            if(portrait&&daily){
                c.save();
                c.scale(.90f,1f,captionX,captionY);
                fitted(name,captionX,captionY,16,w-5,ink,true);
                c.restore();
            }else fitted(name,captionX,captionY,portrait?16:17,w-5,ink,true);
            if(daily){
                fitted("Novi izazov",x+w/2,y+(portrait?143:169),12,w-5,muted,false);
                fitted("svaki dan!",x+w/2,y+(portrait?157:182),12,w-5,muted,false);
            }else fitted(sub,x+w/2,y+(portrait?155:177),17,w-6,muted,false);
            taps.add(new Tap(x,y,x+w,y+h,name+" "+sub,tap));
        }
        float gridX=16,gridY=222,gridSize=368;
        boolean compactBoardLayout=false;
        boolean referencePortraitLayout=false;
        void gameScreen(){
            boolean relaxed="relaxed".equals(game.mode);
            boolean daily="daily".equals(game.mode);
            boolean narrow=compactBoardLayout;
            if(narrow&&!daily&&!relaxed){
                // The source's classic portrait heading is deliberately tall:
                // stretch only its drawn glyphs, not the text hit regions or
                // neighboring score columns. Keep the 4x4 tag independent.
                c.save();
                c.scale(1f,1.40f,105f,60f);
                fitted("Klasično",105,89,43,201,ink,true);
                c.restore();
                // Thick coral rays in the owner reference are distinct
                // strokes, not the tiny dots of the generic rays() helper.
                if(theme==0)classicGameAccentRays(false);
                else rays(110,67,35,CORAL);
            }else{
                fitted(daily?"Dnevni":relaxed?"Opušteno":"Klasično",
                    105,89,daily?32:43,201,ink,true);
            }
            if(!daily) {
                if(narrow&&!relaxed){
                    // Reference badge: narrower than the old 93-unit pill.
                    classicBadge(60,100,84,43);
                    txt("4×4",102,133,34,WHITE,true);
                    if(theme==0)classicGameAccentRays(true);
                    else {rays(49,120,16,CORAL);rays(165,120,16,CORAL);}
                }else{
                    card(39,108,141,50,19,CORAL);
                    txt(relaxed?"5×5":"4×4",110,144,35,WHITE,true);
                    rays(34,134,16,CORAL);rays(190,134,16,CORAL);
                }
            }else{
                fitted("izazov",103,135,36,195,CORAL,true);
            }
            // Both score labels have independent, non-overlapping bounds even
            // for 5+ digit values shown in the original populated 4x4 artwork.
            icon("crown",narrow?231:224,70,24,0xffffb43d);
            fitted("Najbolji",narrow?230:255,100,12,narrow?66:70,muted,true);
            fitted(fmt(best),narrow?230:255,129,24,narrow?64:70,ink,true);
            icon("stats",narrow?299:313,69,23,muted);
            fitted("Rezultat",narrow?301:334,101,12,narrow?65:70,muted,true);
            fitted(fmt(game.score),narrow?301:334,129,23,narrow?64:70,ink,true);
            if(narrow&&!relaxed)
                shape(262,58,1,65,0,theme==1?0xff526078:0xffe1d8cc);
            if(narrow){
                // Reference 941x1672: action buttons live beside the heading,
                // while the large numbered board and primary controls remain visible.
                if(!relaxed&&!daily&&theme==0){
                    classicHeaderControl("pause",335,32,43,49,
                        ()->navigate("pause"));
                    classicHeaderControl("undo",335,98,43,49,
                        ()->undo());
                }else{
                    button("","pause",335,32,43,49,card,false,()->navigate("pause"));
                    headerUndo(335,98,43,49);
                }
            }else{
                button("","pause",327,153,48,49,card,false,()->navigate("pause"));
                headerUndo(265,153,50,49);
            }
            drawGrid();
            if(daily){
                int remaining=Math.max(0,80-game.moves);
                fitted("DNEVNI IZAZOV • "+remaining+" POTEZA PREOSTALO",
                    200,narrow?561:624,15,351,remaining<=10?CORAL:muted,true);
            }
            boolean portraitFour=compactBoardLayout&&game.size==4;
            float actionsY=narrow?(portraitFour?(daily?578:577):555):647;
            float actionsH=portraitFour?94:(narrow?82:91);
            float firstX=portraitFour?17:22,undoX=portraitFour?139:144;
            float finishX=portraitFour?258:265;
            boolean classicArtwork=portraitFour&&theme==0&&
                "classic".equals(game.mode);
            // Keep standard (already measured) Novo/Poništi rendering.
            // Apply special artwork only to the coral confirmation button,
            // avoiding a measurable regression in the two cream controls.
            if(classicArtwork)
                classicBottomControl("Novo","refresh",firstX,actionsY,
                    113,actionsH,()->requestNew(game.mode));
            else
                button("Novo","refresh",firstX,actionsY,113,actionsH,card,false,
                    ()->requestNew(game.mode));
            if(game.canUndo()){
                if(classicArtwork)
                    classicBottomControl("Poništi","undo",undoX,actionsY,
                        112,actionsH,()->undo());
                else
                    button("Poništi","undo",undoX,actionsY,112,actionsH,
                        card,false,()->undo());
            }else{
                card(undoX,actionsY,112,actionsH,20,
                    theme==1?0xff29364a:0xffeae5de);
                icon("undo",undoX+56,actionsY+27,25,muted);
                txt("Poništi",undoX+56,actionsY+actionsH-15,17,muted,true);
            }
            if(portraitFour&&theme==0)
                portraitClassicFinish(finishX,actionsY,120,actionsH,
                    ()->navigate("confirmEnd"));
            else
                button("Završi","check",finishX,actionsY,portraitFour?120:113,
                    actionsH,CORAL,true,()->navigate("confirmEnd"));
            if(!narrow){
                button("Izbornik","home",22,757,171,64,card,false,
                    ()->navigate("home"));
                button("Podijeli","share",207,757,171,64,card,false,()->share());
                bottomBrand();
            }
        }
        // All following art is scoped to the light-theme 4x4 gameplay
        // view. Do not change achievements, tile values, scoring, gameplay
        // physics, other themes, 5x5, or any modal/backdrop rendering.
        void classicBadge(float x,float y,float width,float height){
            shape(x,y+2.2f,width,height,17.5f,0xffdf5740);
            gradient(x,y,width,height-2f,17.5f,0xffff9270,0xffff6346);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.9f);p.setColor(0x8bffffff);
            c.drawRoundRect(x+1.6f,y+1.2f,x+width-1.6f,y+height-4,
                16f,16f,p);
            p.setStyle(Paint.Style.FILL);
        }
        void classicSoftCard(float x,float y,float w,float h,float r,int fill){
            if(theme!=0){card(x,y,w,h,r,fill);return;}
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xfff0e4d5);
            p.setShadowLayer(6.5f,0f,3.5f,0x27977a52);
            c.drawRoundRect(x,y+1.4f,x+w,y+h+1.4f,r,r,p);
            p.clearShadowLayer();
            gradient(x,y,w,h,r,0xfffffdf7,0xfff8efdf);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.7f);p.setColor(0x93ffffff);
            c.drawRoundRect(x+1.2f,y+1.2f,x+w-1.2f,y+h-1.2f,
                r-1f,r-1f,p);
            p.setStyle(Paint.Style.FILL);
        }
        void classicBottomControl(String label,String glyph,float x,float y,
                float w,float h,Runnable action){
            classicSoftCard(x,y,w,h,20f,card);
            float center=x+w/2f;
            icon(glyph,center,y+h*.36f,39f,ink);
            fitted(label,center,y+h*.81f,19f,w-10f,ink,true);
            taps.add(new Tap(x,y,x+w,y+h,label,action));
        }
        void classicHeaderControl(String glyph,float x,float y,float w,
                float h,Runnable action){
            classicSoftCard(x,y,w,h,15f,card);
            boolean enabled=!glyph.equals("undo")||(game!=null&&game.canUndo());
            icon(glyph,x+w/2f,y+h/2f,enabled?29f:26f,
                enabled?ink:muted);
            if(enabled){
                float extraX=Math.max(0,(54f-w)/2f);
                float extraY=Math.max(0,(54f-h)/2f);
                taps.add(new Tap(Math.max(0,x-extraX),Math.max(0,y-extraY),
                    Math.min(400,x+w+extraX),y+h+extraY,
                    glyph.equals("pause")?"Pauziraj igru":"Poništi potez",
                    action));
            }
        }
        void classicRaisedTile(float x,float y,float w,float h,float radius,int color){
            // Owner's rounded, softly elevated 4×4 tiles have broad gradient
            // lighting. Avoid the hard double-outline of generic glossyTile.
            p.clearShadowLayer();p.setShader(null);
            p.setColor(0xffe4cbb8);p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(3.1f,0f,2.4f,0x3e8b755d);
            c.drawRoundRect(x,y+1.1f,x+w,y+h+1.1f,radius,radius,p);
            p.clearShadowLayer();
            shape(x,y+2f,w,h-1f,radius,darken(color,.16f));
            gradient(x,y,w,h-3f,radius,
                lighten(color,.23f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.85f);p.setColor(0x78ffffff);
            c.drawRoundRect(x+1.6f,y+1.5f,x+w-1.6f,
                y+h-4.5f,radius-1.4f,radius-1.4f,p);
            p.setStyle(Paint.Style.FILL);
        }
        int classicReferenceTileColor(int value){
            switch(value){
                case 2: return 0xffff6948;
                case 4: return 0xff19d6bf;
                case 8: return 0xff248fff;
                case 16: return 0xffffb638;
                case 32: return 0xffff6482;
                case 64: return 0xffc23cf8;
                case 128: return 0xffa846ed;
                case 256: return 0xff874cf5;
                default: return tileColor(value);
            }
        }
        void classicGameAccentRays(boolean aroundBadge){
            p.clearShadowLayer();p.setShader(null);
            p.setColor(CORAL);p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(aroundBadge?5f:6.9f);
            if(aroundBadge){
                float[][] marks={
                    {53,117,44,112},{50,126,43,126},{53,130,46,132},
                    {156,117,164,114},{158,126,166,126},{155,131,163,134}
                };
                for(float[] m:marks)c.drawLine(m[0],m[1],m[2],m[3],p);
            }else{
                float[][] marks={{102,58,96,52},{110,53,109,40.5f},
                                  {121,58,127,50}};
                for(float[] m:marks)c.drawLine(m[0],m[1],m[2],m[3],p);
            }
            p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
        }
        void portraitClassicFinish(float x,float y,float width,
                float height,Runnable action){
            float radius=22f;
            shape(x,y+3,width,height,radius,0xffdf5941);
            gradient(x,y,width,height-3,radius,0xffff9a71,0xffff6341);
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.85f);
            p.setColor(0x70ffffff);
            c.drawRoundRect(x+2,y+2,x+width-2,y+height-4,
                radius-2,radius-2,p);
            p.setStyle(Paint.Style.FILL);
            float center=x+width/2f;
            icon("check",center,y+height*.37f,39f,WHITE);
            fitted("Završi",center,y+height*.81f,18.5f,width-10,WHITE,true);
            taps.add(new Tap(x,y,x+width,y+height,"Završi",action));
        }
        void headerUndo(float x,float y,float w,float h){
            if(game!=null && game.canUndo()){
                button("","undo",x,y,w,h,card,false,()->undo());
            }else{
                // Match the disabled bottom Undo button: no inert hit target.
                card(x,y,w,h,Math.min(23,h*.27f),
                    theme==1?0xff29364a:0xffeae5de);
                icon("undo",x+w/2,y+h/2,Math.min(23,h*.52f),muted);
            }
        }
        void drawGrid(){
            if(game==null)return;
            int n=game.size;
            boolean tallFour=compactBoardLayout&&n==4;
            boolean victoryBoard=tallFour&&referencePortraitLayout&&
                "win".equals(page);
            // The win mockup uses 84-unit square-like tiles with a tighter
            // vertical cadence. Active gameplay keeps its existing geometry.
            // Source 4×4 artwork has taller tiles and a lower cream frame.
            // Leave the existing 5×5 cell geometry unchanged.
            float left=gridX-(tallFour?2.5f:0f);
            card(left-4,gridY-4,gridSize+8,
                gridSize+(victoryBoard?14:(tallFour?34:8)),23,
                theme==1?0xff354661:0xfff2e5d3);
            float gap=tallFour?5:(n==4?9:6);
            float tile=(gridSize-gap*(n+1))/n;
            float tileHeight=tallFour?(victoryBoard?84f:89f):tile;
            for(int y=0;y<n;y++)for(int x=0;x<n;x++){
                int value=game.cells[y*n+x];
                float rx=left+gap+(tile+gap)*x;
                float ry=gridY+(tallFour?10:gap)+
                    (victoryBoard?90.5f:(tallFour?94f:tile+gap))*y;
                // Victory/pause backdrops also call drawGrid(). Only the
                // *active* light-theme 4x4 gameplay may use this palette.
                int color="game".equals(page)&&tallFour&&theme==0&&tileStyle==0?
                    classicReferenceTileColor(value):tileColor(value);
                float delta=animation==0?1:pulse;
                float inset=value>0?(1-delta)*tile/2:0;
                float round=tileStyle==1?7:tileStyle==2?24:15;
                if(value==0)shape(rx,ry,tile,tileHeight,round,color);
                else {
                    boolean classicSource="game".equals(page)&&tallFour&&
                        "classic".equals(game.mode)&&theme==0&&
                        tileStyle==0&&referencePortraitLayout;
                    if(classicSource)
                        classicRaisedTile(rx+inset,ry+inset,tile-2*inset,
                            tileHeight-2*inset,round,color);
                    else
                        glossyTile(rx+inset,ry+inset,tile-2*inset,
                            tileHeight-2*inset,round,color);
                    String str=Integer.toString(value);
                    int digits=str.length();
                    float fontsize=classicSource?
                        (digits>=3?37.5f:40.5f)-(digits>3?(digits-3)*5f:0f):
                        (n==5?33:44)-(digits>3?(digits-3)*7:0);
                    fitted(str,rx+tile/2,ry+tileHeight*.62f,
                        fontsize,tile-7,WHITE,true);
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
        // Post-main-screen QA: Pause shares the warm, raised Brojko
        // material system without affecting the board or the saved session.
        // There is no owner-supplied Pause PNG; never claim exact 1:1 here.
        void pauseOverlay(){
            dim();
            overlayBox(229,414);
            card(159,247,82,76,23,
                theme==1?0xff354660:0xfffff1e4);
            p.setShader(null);p.setColor(WHITE);
            gradient(175,262,50,49,16,lighten(CORAL,.13f),CORAL);
            icon("pause",200,286,29,WHITE);
            fitted("Igra je pauzirana",200,364,30,321,ink,true);
            fitted("Nastavi kad budeš spreman.",200,391,14,308,muted,false);
            card(62,406,276,55,17,
                theme==1?card:0xfffff8ef);
            icon("stats",91,433,22,muted);
            leftFitted("Rezultat",114,429,14,110,muted,false);
            fitted(fmt(game.score),271,443,25,119,ink,true);
            button("Nastavi","play",52,483,296,66,CORAL,true,
                ()->navigate("game"));
            button("Izbornik","home",52,561,296,61,card,false,
                ()->navigate("home"));
        }
        // Source artwork uses a quiet post-game summary behind the modal:
        // the real frozen board, compact brand / score HUD and bottom shortcuts.
        // No game state is reset; all background taps are cleared before the modal.
        void victoryBackdropPortrait(){
            c.save();c.translate(-96,0);
            brand(66,43,true);
            c.restore();
            card(205,18,81,78,15,card);
            card(297,18,81,78,15,card);
            icon("crown",245,37,24,0xffffb52b);
            fitted("Najbolji",245,56,12,78,muted,true);
            fitted(fmt(best),245,79,20,75,ink,true);
            icon("stats",335,37,23,muted);
            fitted("Rezultat",335,56,12,77,muted,true);
            fitted(fmt(game.score),335,79,20,75,ink,true);
            float prevGridY=gridY;
            // The illustrated post-win board begins 10 units above the
            // gameplay grid; its tiles are slightly shorter as well.
            gridY=102;
            drawGrid();
            gridY=prevGridY;
            card(18,620,114,77,17,card);
            card(143,620,114,77,17,card);
            card(268,620,114,77,17,card);
            icon("refresh",75,645,33,ink);
            icon("crown",200,645,31,ink);
            icon("settings",325,645,30,ink);
            fitted("Novo",75,685,15,105,muted,true);
            fitted("Rekordi",200,685,15,105,muted,true);
            fitted("Postavke",325,685,15,105,muted,true);
        }
        void victoryCrown(float cx,float cy,float size){
            // Soft three-peak silhouette with an ivory outline, in real Canvas
            // geometry: no non-interactive bitmap of the reference screen.
            c.save();
            c.translate(cx-size/2f,cy-size/2f);
            c.scale(size/24f,size/24f);
            Path crown=new Path();
            crown.moveTo(2.8f,9.4f);
            crown.cubicTo(1.0f,6.6f,3.3f,6.1f,5.1f,8.8f);
            crown.cubicTo(7.5f,12.5f,8.0f,10.8f,9.1f,8.4f);
            crown.cubicTo(10.2f,5.9f,10.3f,2.6f,12f,2.6f);
            crown.cubicTo(13.7f,2.6f,14.1f,7.6f,15.1f,9.7f);
            crown.cubicTo(16.2f,12.1f,17.9f,7.5f,19.7f,7.8f);
            crown.cubicTo(22.8f,6.8f,23.2f,9.5f,21.3f,11.8f);
            crown.lineTo(19.8f,18.2f);
            crown.quadTo(19.7f,19.2f,18.4f,19.2f);
            crown.lineTo(5.8f,19.2f);
            crown.quadTo(4.5f,19.2f,4.2f,18.1f);
            crown.close();
            p.setShader(null);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.4f);
            p.setColor(0x55ffffff);
            c.drawPath(crown,p);
            p.setStrokeWidth(1.35f);
            p.setColor(WHITE);
            c.drawPath(crown,p);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0,3,0,19,
                0xffffeb7c,0xffffb324,Shader.TileMode.CLAMP));
            c.drawPath(crown,p);
            p.setShader(null);
            p.setColor(0xffff8f16);
            c.drawRoundRect(5.1f,17.3f,19.5f,19.9f,1.2f,1.2f,p);
            p.setColor(0xffffce46);
            c.drawRoundRect(5.5f,16.8f,19.1f,18.4f,.8f,.8f,p);
            c.restore();
        }
        void victoryHeadingRays(){
            // The hand-lettered reference has coral strokes framing the
            // raised navy wordmark. Keep rays off the three touch targets.
            for(int side=0;side<2;side++){
                float x=side==0?91f:309f;
                for(int i=0;i<3;i++){
                    float y=247f+i*24f;
                    int color=i==1?TEAL:CORAL;
                    c.save();
                    c.rotate((side==0?-1f:1f)*(i==0?-36f:i==2?39f:69f),x,y);
                    shape(x-3f,y-9f,6f,i==1?14f:17f,3f,color);
                    c.restore();
                }
            }
        }
        void victoryHeading(){
            // The reference logo is a bouncing, hand-lettered Nunito word,
            // not a single rigid baseline. Build it as individual glyphs
            // so the weight, overlap and subtle rotations work at any DPI.
            // Golden screenshot: previous heading was visibly ~20% taller
            // than the painted wordmark, especially the capital B.
            c.save();c.scale(.95f,1.10f,200f,286f);
            p.clearShadowLayer();p.setShader(null);
            p.setTypeface(displayFont);p.setTextSize(63f);
            p.setTextScaleX(1f);p.setFakeBoldText(false);
            p.setTextAlign(Paint.Align.CENTER);
            float full=0f;
            for(int i=0;i<VICTORY_GLYPHS.length;i++){
                full+=p.measureText(VICTORY_GLYPHS[i])*VICTORY_WIDTH[i]-
                    (i<VICTORY_GLYPHS.length-1?3.1f:0f);
            }
            float left=202f-full/2f;
            for(int i=0;i<VICTORY_GLYPHS.length;i++){
                float glyphWidth=p.measureText(VICTORY_GLYPHS[i])*VICTORY_WIDTH[i];
                float middle=left+glyphWidth/2f;
                float baseline=286f+VICTORY_RISE[i];
                c.save();
                c.rotate(VICTORY_TILT[i],middle,baseline-20f);
                c.scale(VICTORY_WIDTH[i],VICTORY_HEIGHT[i],
                    middle,baseline-27f);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setStrokeWidth(2.6f);p.setColor(WHITE);
                c.drawText(VICTORY_GLYPHS[i],middle,baseline,p);
                p.setStyle(Paint.Style.FILL);p.setColor(ink);
                c.drawText(VICTORY_GLYPHS[i],middle,baseline,p);
                c.restore();
                left+=glyphWidth-3.1f;
            }
            p.setStyle(Paint.Style.FILL);p.setStrokeWidth(0);
            p.setTextAlign(Paint.Align.CENTER);
            c.restore();
        }
        void victoryRestartIcon(float cx,float cy,float width,int color){
            // The original new-game pictogram is a two-arrow refresh.
            c.save();c.translate(cx-width/2f,cy-width/2f);
            c.scale(width/24f,width/24f);
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.7f);p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(color);
            c.drawArc(new RectF(3.4f,3.4f,20.6f,20.6f),
                201f,148f,false,p);
            c.drawArc(new RectF(3.4f,3.4f,20.6f,20.6f),
                20f,148f,false,p);
            p.setStyle(Paint.Style.FILL);
            Path arrows=new Path();
            arrows.moveTo(17.8f,2.0f);arrows.lineTo(21.5f,7.3f);
            arrows.lineTo(15.3f,7.4f);arrows.close();
            c.drawPath(arrows,p);
            arrows.reset();arrows.moveTo(6.2f,22.0f);
            arrows.lineTo(2.5f,16.7f);arrows.lineTo(8.7f,16.6f);
            arrows.close();c.drawPath(arrows,p);
            c.restore();
        }
        void victorySummaryCard(float x,float y,float width,float height){
            if(theme==1){
                card(x,y,width,height,19,card);return;
            }
            // Reference interior is ~RGB(249,241,228), unlike the former
            // #F8EAD9 with a thick white bevel.
            p.clearShadowLayer();p.setShader(null);
            shape(x+.5f,y+1.6f,width-1f,height,19,0x30c9a98c);
            gradient(x,y,width,height-1.8f,19,
                0xfffdf3e6,0xfff9f1e4);
        }
        void compactVictoryCard(){
            // Match the 941x1672 portrait illustration while retaining all
            // three working actions and readable real scores.
            card(59,200,282,397,29,theme==1?0xff293956:CREAM);
            // Pixel audit: original golden silhouette was ~21 px too wide
            // and ~18 px too low at the 941x1672 reference viewport.
            victoryCrown(200,187,98);
            victoryHeadingRays();
            victoryHeading();
            if(theme==1)card(95,299,210,30,17,card);
            else gradient(95,299,210,30,17,
                0xfffff5e7,0xffffeedb);
            fitted("Dosegnuli ste",161,323,20,133,0xffff5537,true);
            fitted("2048!",258,323,21,88,0xff008d83,true);
            // Measured reference card interior averages RGB(249,237,221);
            // the generic nearly-white card was RGB(255,253,250).
            victorySummaryCard(76,337,120,69);
            victorySummaryCard(204,337,120,69);
            txt("Tvoj rezultat",136,356,12,muted,false);
            fitted(fmt(game.score),136,392,35,113,ink,true);
            txt("Osvojene zvjezdice",264,356,11,muted,false);
            for(int i=0;i<3;i++)icon("star",229+i*35,382,32,0xffffb526);
            button("Nastavi igrati","play",74,417,252,53,CORAL,true,()->{
                game.winAcknowledged=true;persist();navigate("game");
            });
            button("Nova igra","refresh",74,478,252,49,card,false,
                ()->newGame(game.mode));
            button("Podijeli rezultat","share",74,532,252,49,
                theme==1?0xff3269b5:0xff3b95ee,true,()->share());
        }
        // There is no separately supplied owner's Game Over PNG. Keep the
        // same tactile Brojko language as Victory while faithfully rendering
        // the frozen real game, score and peak tile (not screenshot fixtures).
        void compactGameOverCard(){
            card(42,168,316,525,29,theme==1?0xff293956:CREAM);
            card(155,182,90,85,24,
                theme==1?0xff3b4b66:0xfffff0e0);
            p.setShader(null);p.setColor(WHITE);
            gradient(174,199,52,50,17,lighten(CORAL,.16f),CORAL);
            icon("target",200,224,31,WHITE);
            fitted("Kraj igre",200,308,39,294,ink,true);
            fitted("Nema više mogućih poteza.",200,340,17,287,muted,false);
            // Warm, raised status panels are not fake reward counters.
            victorySummaryCard(59,358,135,107);
            victorySummaryCard(206,358,135,107);
            fitted("Tvoj rezultat",126.5f,383,14,123,muted,false);
            fitted(fmt(game.score),126.5f,440,31,123,ink,true);
            fitted("Najveća pločica",273.5f,383,13.5f,128,muted,false);
            fitted(fmt(game.maxTile()),273.5f,440,30,119,TEAL,true);
            button("Nova igra","refresh",54,483,292,64,CORAL,true,
                ()->newGame(game.mode));
            button("Povratak","home",54,559,292,56,card,false,
                ()->navigate("home"));
            button("Podijeli rezultat","share",54,624,292,52,
                theme==1?0xff3269b5:0xff3b95ee,true,()->share());
        }
        void resultOverlay(boolean winning){
            // Reference victory backdrop is charcoal-neutral rather than blue.
            // Keep the original overlay for loss and wide-device modals.
            if(winning&&referencePortraitLayout)c.drawColor(0x88000000);
            else dim();
            boolean narrow=referencePortraitLayout;
            if(winning){
                int[] confetti={CORAL,TEAL,0xff428fea,0xffffbd4e};
                for(int i=0;i<29;i++){
                    float x=(i*83+31)%376+12,y=(i*73+197)%625+95;
                    c.save();c.rotate((i*43)%170,x,y);
                    shape(x,y,7,18,4,confetti[i%4]);c.restore();
                }
            }
            if(winning&&narrow){
                compactVictoryCard();
                return;
            }
            if(!winning&&narrow){
                compactGameOverCard();
                return;
            }
            // Non-portrait and game-over states retain their established
            // responsive layout and navigation behavior.
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
        // Daily result is a stateful gameplay overlay, not a static reward
        // illustration. No dedicated owner PNG was supplied for these modals.
        // Both compact variants share measured geometry, real score/peak,
        // and a local-only bonus that must never be awarded twice.
        void compactDailyResultOverlay(boolean success){
            dim();
            overlayBox(177,511);
            card(155,191,90,85,24,theme==1?0xff394963:0xfffff0df);
            p.setShader(null);p.setColor(WHITE);
            int accent=success?TEAL:CORAL;
            gradient(173,208,54,52,17,lighten(accent,.16f),accent);
            icon(success?"star":"target",200,234,31,WHITE);
            fitted(success?"Dnevni izazov riješen!":"Izazov nije uspio",
                200,315,success?28:30,321,ink,true);
            fitted(success?"Sva 3 zadatka su ostvarena!":
                (game.moves>=80?"Dosegnuto je ograničenje od 80 poteza.":
                "Nema više mogućih poteza."),200,345,16,319,muted,false);

            victorySummaryCard(59,366,135,99);
            victorySummaryCard(206,366,135,99);
            fitted("Tvoj rezultat",126.5f,390,13.6f,126,muted,false);
            fitted(fmt(game.score),126.5f,444,29,124,ink,true);
            fitted("Najveća pločica",273.5f,390,13.1f,127,muted,false);
            fitted(fmt(game.maxTile()),273.5f,444,28,118,TEAL,true);

            card(59,476,282,54,18,theme==1?0xff32445c:
                success?0xffe3fbf2:0xfffff0e5);
            icon(success?"star":"target",84,503,23,
                success?0xffffaf26:CORAL);
            // The date stored in preferences is the source of truth; the
            // transient UI flag is only needed to distinguish first win.
            String message=success?
                (dailyBonusGrantedThisAttempt?
                    "+250 zvjezdica osvojeno!":
                    (today().equals(dailyRewardDate)?
                        "Današnja nagrada je preuzeta.":
                        "Dnevni izazov dovršen.")):
                "Pokušaj ponovno ostvariti ciljeve.";
            leftFitted(message,108,508,15,216,
                success?0xff078e77:ink,true);
            button("Igraj ponovno","refresh",53,543,294,61,CORAL,true,
                ()->newGame("daily"));
            button("Izbornik","home",53,615,294,55,card,false,
                ()->navigate("home"));
        }
        void dailyResultOverlay(boolean success){
            if(referencePortraitLayout){
                compactDailyResultOverlay(success);
                return;
            }
            // Keep the taller established layout for long-aspect devices.
            dim();
            overlayBox(193,501);
            icon(success?"crown":"target",200,256,52,success?0xffffb829:CORAL);
            txt(success?"Dnevni izazov riješen!":"Izazov nije uspio",200,329,
                success?28:31,ink,true);
            fitted(success?"Sva 3 zadatka su ostvarena!":
                (game.moves>=80?"Potrošeno je 80 dopuštenih poteza.":
                "Nema više mogućih poteza."),200,369,17,316,muted,false);
            card(65,401,270,98,20,card);
            txt("TVOJ REZULTAT",200,432,15,muted,true);
            txt(fmt(game.score),200,472,36,ink,true);
            fitted(success?(dailyBonusGrantedThisAttempt?
                "+250 zvjezdica osvojeno!":
                today().equals(dailyRewardDate)?
                    "Današnja nagrada je preuzeta.":
                    "Dnevni izazov dovršen."):
                "Pokušaj ponovno ostvariti sve ciljeve.",200,534,17,310,
                success?0xff259d85:muted,true);
            button("Igraj ponovno","refresh",53,546,294,62,CORAL,true,
                ()->newGame("daily"));
            button("Izbornik","home",53,621,294,55,card,false,
                ()->navigate("home"));
        }
        // These confirmation screens are functional modals, not independent
        // games. Both use actual session state, including local archive status.
        // Owner supplied no separate PNG for the two dialogs.
        void confirmModalBadge(String glyph,int accent){
            card(156,245,88,76,22,theme==1?0xff364560:0xfffff0e1);
            p.setShader(null);p.setColor(WHITE);
            gradient(173,258,54,52,17,lighten(accent,.18f),accent);
            icon(glyph,200,284,30,WHITE);
        }
        void confirmSessionSummary(float y){
            card(60,y,280,57,18,theme==1?card:0xfffff7ed);
            icon("stats",91,y+27,22,muted);
            leftFitted("Rezultat",115,y+25,14,98,muted,false);
            fitted(game==null?"0":fmt(game.score),273,y+38,24,118,ink,true);
        }
        void confirmEndOverlay(){
            dim();
            overlayBox(226,421);
            confirmModalBadge("check",TEAL);
            fitted("Završiti partiju?",200,362,29,316,ink,true);
            fitted("Rezultat se sprema u povijest partija.",
                200,393,15.5f,317,muted,false);
            confirmSessionSummary(411);
            button("Završi i spremi","check",52,488,296,65,CORAL,true,
                ()->endSession());
            button("Nastavi igrati","play",52,570,296,59,card,false,
                ()->navigate("game"));
        }
        void confirmOverlay() {
            dim();
            overlayBox(226,421);
            confirmModalBadge("refresh",CORAL);
            fitted("Nova partija?",200,362,30,318,ink,true);
            fitted("Trenutačni napredak sprema se u povijest.",
                200,393,15.1f,316,muted,false);
            confirmSessionSummary(411);
            String modeLabel="relaxed".equals(pendingMode)?"Opušteno 5×5":
                "daily".equals(pendingMode)?"Dnevni izazov":"Klasično 4×4";
            fitted("Novi način: "+modeLabel,200,482,14,310,muted,true);
            button("Započni novo","play",52,500,296,60,CORAL,true,
                ()->newGame(pendingMode));
            button("Odustani","back",52,578,296,54,card,false,
                ()->navigate("game"));
        }
        void dailyPlayGlyph(float cx,float cy,float size,int color){
            c.save();c.translate(cx-size/2f,cy-size/2f);
            c.scale(size/24f,size/24f);
            p.setStyle(Paint.Style.FILL);p.setShader(null);
            p.setColor(0x2dc04f27);p.setShadowLayer(1.3f,0,.7f,0x4e953a18);
            Path play=new Path();
            play.moveTo(6.1f,2.0f);
            play.quadTo(5.3f,1.6f,5.3f,4.5f);
            play.lineTo(5.3f,19.5f);
            play.quadTo(5.3f,22.6f,8.0f,21.3f);
            play.lineTo(20.6f,13.8f);
            play.quadTo(23.5f,12f,20.6f,10.2f);
            play.close();
            p.setColor(color);c.drawPath(play,p);p.clearShadowLayer();
            c.restore();
        }
        void dailyBoardFrame(float x,float y,float width,float height){
            // Warm ivory raised frame around the *preview*, not game state.
            p.clearShadowLayer();p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xfff8efe4);
            p.setShadowLayer(6f,0,3.5f,0x2ca78052);
            c.drawRoundRect(x,y,x+width,y+height,24,24,p);
            p.clearShadowLayer();
            gradient(x,y,width,height,24,0xfffffcf5,0xfff8efe5);
            shape(x+10,y+9,width-20,height-18,18,0xfff4eadd);
        }
        void dailySampleTile(float x,float y,float size,int color){
            // Subtle 3D volume without the harsh double outline of
            // the shared game tile. Restrict to the illustration preview.
            p.clearShadowLayer();p.setShader(null);
            p.setColor(0x36ad7e56);
            p.setShadowLayer(2.4f,0f,2.4f,0x36a57d58);
            c.drawRoundRect(x,y+1.4f,x+size,y+size+1.4f,10,10,p);
            p.clearShadowLayer();
            shape(x,y+2.1f,size,size-1.7f,10,darken(color,.13f));
            gradient(x,y,size,size-3.1f,10,
                lighten(color,.13f),color);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.65f);p.setColor(0x60ffffff);
            c.drawRoundRect(x+1.1f,y+1.0f,x+size-1.1f,
                y+size-3.9f,9,9,p);
            p.setStyle(Paint.Style.FILL);
        }
        void dailySwapArrows(float cx,float cy,float size){
            // Reference's opposing blue arrowheads are chunky and bevelled.
            c.save();c.translate(cx-size/2f,cy-size/2f);
            c.scale(size/24f,size/24f);
            p.clearShadowLayer();p.setStyle(Paint.Style.FILL);
            Path upper=new Path();
            upper.moveTo(2,8);upper.lineTo(8,2);
            upper.quadTo(9,1,10,2.5f);
            upper.lineTo(10,5);upper.lineTo(20,5);
            upper.quadTo(22,5,22,7.2f);
            upper.quadTo(22,9,20,9);
            upper.lineTo(10,9);upper.lineTo(10,12);
            upper.quadTo(9,13,8,12);upper.close();
            Path lower=new Path();
            lower.moveTo(22,16);lower.lineTo(16,22);
            lower.quadTo(15,23,14,21.5f);
            lower.lineTo(14,19);lower.lineTo(4,19);
            lower.quadTo(2,19,2,16.8f);
            lower.quadTo(2,15,4,15);
            lower.lineTo(14,15);lower.lineTo(14,12);
            lower.quadTo(15,11,16,12);lower.close();
            c.save();c.translate(0,1.0f);
            p.setColor(0xff125bb4);
            c.drawPath(upper,p);c.drawPath(lower,p);c.restore();
            p.setShader(new LinearGradient(0,0,24,24,
                0xff60baff,0xff177fe8,Shader.TileMode.CLAMP));
            c.drawPath(upper,p);c.drawPath(lower,p);
            p.setShader(null);c.restore();
        }
        // Three broad, slanted rays at either side of the daily illustration,
        // matching the provided PNG instead of the shared tiny ray dots.
        void dailyBoardBursts(float cx,float cy,boolean right,int color){
            // Source: separated rounded orange/yellow petals, not three
            // identical rays forming a single angular mark.
            final float[] yOff={-17f,13f,46f};
            final float[] xOff={right?3f:-3f,right?7f:-7f,0f};
            final float[] tilt={right?-36f:36f,90f,right?37f:-37f};
            final int[] palette={0xffffb33b,0xffff6547,0xffffae3b};
            for(int i=0;i<3;i++){
                float x=cx+xOff[i],y=cy+yOff[i];
                c.save();c.rotate(tilt[i],x,y);
                shape(x-3.4f,y-12f,6.8f,24f,3.4f,
                    theme==1?color:palette[i]);
                c.restore();
            }
        }
        // The portrait daily CTA has three large side bursts, but only
        // 49 logical units of height near the screen's safe bottom edge.
        // The decorative lines stay outside x=25..375 (the real button).
        void dailyStartRays(float startY,boolean right){
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5f);p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(CORAL);
            float[][] strokes={{23,3,34,12},{18,26,33,26}};
            for(float[] stroke:strokes){
                float x1=right?400-stroke[0]:stroke[0];
                float x2=right?400-stroke[2]:stroke[2];
                c.drawLine(x1,startY+stroke[1],x2,startY+stroke[3],p);
            }
            p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
        }
        void dailyHeaderAccents(float cx,float cy){
            // Source heading uses three solid coral confetti strokes rather
            // than the tiny evenly spaced dots of the shared rays() utility.
            c.save();c.rotate(-32,cx-13,cy+3);
            shape(cx-16,cy-2,6,15,4,CORAL);c.restore();
            shape(cx-3,cy-10,7,18,4,CORAL);
            c.save();c.rotate(32,cx+15,cy+3);
            shape(cx+12,cy-2,6,15,4,CORAL);c.restore();
        }
        void daily(){
            boolean portrait=referencePortraitLayout;
            // The reference back control sits above the first title line.
            // Keep its action and touch padding identical to shared back().
            if(portrait)
                button("","back",14,16,48,48,card,false,()->navigate("home"));
            else back();
            // Reference: dominant two-line title with compact line spacing.
            fitted("Dnevni",portrait?196:184,portrait?54:90,
                portrait?51:48,portrait?240:223,ink,true);
            fitted("izazov",portrait?205:199,portrait?99:141,
                portrait?49:48,234,CORAL,true);
            dailyHeaderAccents(154,portrait?16:39);
            float dateY=portrait?99:164;
            // Keep the calendar glyph and locally formatted long date apart.
            // Reference date is a shallow capsule; the previous 32-unit
            // pill touched the first description line on compact phones.
            if(portrait&&theme!=1)
                gradient(132,dateY,136,25,16,
                    0xfffff6ec,0xfff9eede);
            else card(portrait?132:98,dateY,portrait?136:191,
                portrait?25:34,17,theme==1?card:0xffffecdc);
            icon(portrait?"calendar-outline":"calendar",portrait?153:119,
                dateY+(portrait?12:18),portrait?16:19,muted);
            // Source pill is ~320px wide; long real dates shrink inside
            // it without touching the calendar or changing today's date.
            leftFitted(dailyLongDate(),portrait?174:139,
                dateY+(portrait?18:24),portrait?11.5f:14,
                portrait?89:142,ink,true);
            float streakTop=portrait?38:66;
            if(portrait&&theme!=1)
                gradient(302,streakTop,80,72,18,
                    0xfffffcf5,0xffffead2);
            else card(portrait?302:291,streakTop,portrait?80:87,
                portrait?72:83,18,theme==1?card:0xffffebd4);
            icon("flame",portrait?325:315,streakTop+29,30,0xffff8b2c);
            fitted(Integer.toString(streak),portrait?358:351,streakTop+40,
                25,46,CORAL,true);
            fitted("dana zaredom",portrait?344:335,streakTop+
                (portrait?60:65),12,83,muted,true);
            if(portrait){
                // The original portrait artwork has two subtitle lines,
                // spaced between the date pill and the illustrative board.
                fitted("Riješi današnji izazov i osvoji posebnu nagradu.",
                    200,145,13,350,muted,false);
                fitted("Pokaži koliko daleko možeš stići!",
                    200,159,13,338,muted,false);
            }else{
                fitted("Riješi današnji izazov i osvoji nagradu!",
                    200,218,17,337,muted,false);
                fitted("Pokaži koliko daleko možeš stići!",
                    200,237,16,337,muted,false);
            }
            float boardY=portrait?167:250;
            // The second subtitle line ends well above the sample board.
            // Do not move the board upward without leaving its text margin.
            // Decorative bursts live outside the board and stay below
            // the description, as in the supplied 941x1672 mockup.
            dailyBoardBursts(51,boardY+(portrait?105:118),
                false,0xffffb23f);
            dailyBoardBursts(349,boardY+(portrait?105:118),
                true,CORAL);
            // The source card extends beyond the edge tiles; the previous
            // 229-unit square made this cream frame visibly too narrow.
            if(portrait&&theme!=1)
                dailyBoardFrame(75,boardY+2,247,235);
            else missionSurface(portrait?75:76,boardY+(portrait?2:0),
                portrait?247:248,portrait?235:248,24,
                theme==1?0xff35455f:0xfff3e8da);
            int[] sample={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
            for(int i=0;i<16;i++){
                int col=i%4,row=i/4;
                float x=(portrait?93:83)+col*(portrait?54.5f:59);
                float y=boardY+(portrait?16:7)+row*(portrait?53:59);
                int value=sample[i];
                float cellSize=portrait?51:52;
                if(portrait&&theme!=1&&tileStyle==0)
                    dailySampleTile(x,y,cellSize,tileColor(value));
                else glossyTile(x,y,cellSize,cellSize,10,tileColor(value));
                float numeral=portrait?
                    (value>=1000?15.3f:value>=100?19.0f:
                        value>=10?21.1f:22.3f):
                    (value>999?16f:24f);
                fitted(""+value,x+cellSize*.5f,
                    y+(portrait?33:34),numeral,
                    portrait?48:49,WHITE,true);
            }
            // Source art groups the three daily objectives inside one
            // quiet cream panel, with each individual white row above it.
            if(portrait){
                shape(18,412,364,151,22,
                    theme==1?0xff273851:0xfffff2df);
            }else{
                shape(18,506,364,232,22,
                    theme==1?0xff273851:0xfffff2df);
            }
            boolean active=game!=null&&"daily".equals(game.mode)&&
                today().equals(game.day);
            boolean finished=today().equals(lastDaily);
            dailyGoalRow("Dosegni 256","Spoji brojeve i dosegni pločicu 256.",
                portrait?417:511,finished||(active&&game.peak>=256));
            dailyGoalRow("Ostvari 3000 bodova","Skupi najmanje 3000 bodova.",
                portrait?465:589,finished||(active&&game.score>=3000));
            dailyGoalRow("Završi u 80 poteza","Riješi izazov u najviše 80 poteza.",
                portrait?513:667,finished);
            float rewardY=portrait?569:744;
            if(portrait&&theme!=1)
                gradient(18,rewardY,364,80,19,
                    0xffefebff,0xffeee8ff);
            else missionSurface(portrait?18:28,rewardY,portrait?364:344,
                portrait?80:48,19,theme==1?0xff3a3459:0xfff0e9ff);
            // The illustration fills the left third of the source card.
            // Keep all copy to its right, including the real earned amount.
            dailyRewardGift(portrait?91:65,rewardY+(portrait?43:24),
                portrait?80:37);
            if(portrait&&theme!=1)dailyGiftBursts(91,rewardY+40f);
            if(portrait){
                leftFitted(finished?"NAGRADA OSVOJENA":"DANAŠNJA NAGRADA",
                    158,rewardY+17,12,190,muted,true);
                leftFitted(finished?"Bravo, osvojeno!":"Posebna nagrada",
                    158,rewardY+40,18,157,ink,true);
                // Preserve the actual earned currency while retaining the
                // reference's explanatory reward copy.
                fitted("+250",346,rewardY+41,14,52,0xffa65be2,true);
                leftFitted(finished?"Današnja nagrada već je preuzeta.":
                    "Dovrši sve ciljeve i osvoji",158,rewardY+57,10.5f,
                    198,muted,false);
                if(!finished)
                    leftFitted("jedinstvenu nagradu!",158,rewardY+69,
                        10.5f,194,muted,false);
            }else{
                leftFitted(finished?"NAGRADA OSVOJENA":"DANAŠNJA NAGRADA",
                    100,rewardY+17,12,197,muted,true);
                leftFitted(finished?"Bravo, osvojeno!":"Posebna nagrada",
                    100,rewardY+34,16,185,ink,true);
                fitted("+250",335,rewardY+30,18,69,0xffa65be2,true);
            }
            button(active&&!game.gameOver?"Nastavi izazov":
                finished?"Igraj ponovno":"Započni izazov","play",
                portrait?43:25,portrait?655:802,portrait?314:350,
                portrait?49:58,0xffff684f,true,()->{
                    if(active&&!game.gameOver)navigate("game");
                    else requestNew("daily");
                });
            if(portrait){
                dailyStartRays(655,false);
                dailyStartRays(655,true);
            }else{
                rays(16,831,16,CORAL);
                rays(384,831,16,CORAL);
            }
        }
        void dailyGoalRow(String name,String description,int y,boolean done){
            boolean portrait=referencePortraitLayout;
            int height=portrait?45:72;
            missionSurface(28,y,344,height,18,card);
            float iconSize=portrait?41:46;
            float tileY=y+(height-iconSize)/2f;
            int goalColor=name.startsWith("Dosegni")?0xffff6f96:
                name.startsWith("Ostvari")?0xffffb541:0xff3d98f0;
            float glyphCenter=40+iconSize/2f;
            if(done){
                // Completed challenges communicate state, not a decorative star.
                shape(40,tileY,iconSize,iconSize,12,TEAL);
                icon("check",glyphCenter,y+height/2f,portrait?25:28,WHITE);
            }else if(name.startsWith("Dosegni")){
                // Only the numeric 256 pictogram is a numbered game tile.
                styledTile(40,tileY,iconSize,iconSize,12,goalColor,0);
                fitted("256",glyphCenter,y+height/2f+7,
                    portrait?20:22,iconSize-3,WHITE,true);
            }else if(name.startsWith("Ostvari")){
                // The reference draws the earned-star symbol on the white card,
                // not a separate orange square tile.
                icon("star",glyphCenter,y+height/2f,
                    portrait?35:39,0xffffb51a);
            }else{
                // Blue opposite-facing arrows directly on the row background.
                if(portrait&&theme!=1)
                    dailySwapArrows(glyphCenter,y+height/2f,35f);
                else icon("swap",glyphCenter,y+height/2f,
                    portrait?34:38,0xff268fec);
            }
            // Reference goal cards use left-aligned text and a right-side help
            // affordance; each target opens the actual goal explanation.
            leftFitted(name,105,y+(portrait?22:29),
                portrait?17:18,225,ink,true);
            leftFitted(description,105,y+(portrait?40:55),
                portrait?11:12,225,muted,false);
            float helpCenterY=y+height/2f;
            shape(340,helpCenterY-13,26,26,13,
                theme==1?0xff445471:0xffede9e2);
            fitted("?",353,helpCenterY+7,17,23,muted,true);
            // The tighter visual card must not shrink the 48-unit help target.
            taps.add(new Tap(322,y,376,y+(portrait?48:height),
                "Pomoć za cilj: "+name,
                ()->Toast.makeText(MainActivity.this,description,Toast.LENGTH_LONG).show()));
        }
        void dailyRewardGift(float cx,float cy,float size){
            // All-vector, soft 3D present. The reference has a plump bow,
            // purple side facets, and a wide gold ribbon, not a flat square.
            // Measured: first bow rose into the final objective row.
            // Raise the box lid, extend the body, but lower its bow tips.
            float top=cy-size*.22f;
            float left=cx-size*.40f;
            float width=size*.80f;
            float height=size*.62f;
            p.clearShadowLayer();p.setShader(null);
            p.setColor(0xff885fc0);p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(7f,0f,4.5f,0x559875a9);
            c.drawRoundRect(left,top+3f,left+width,top+height+3f,
                9.0f,9.0f,p);
            p.clearShadowLayer();
            gradient(left,top,width,height,9,
                0xffba82ff,0xff7034d3);
            // Side wall and inset highlight give the box a rounded body.
            gradient(left+width*.74f,top+3.0f,width*.26f,height-4,
                8,0xff995ae3,0xff6330bf);
            p.setColor(0x59ffffff);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.8f);
            c.drawRoundRect(left+1.3f,top+1.3f,
                left+width-1.3f,top+height-2.0f,8,8,p);
            p.setStyle(Paint.Style.FILL);
            gradient(cx-size*.09f,top,size*.18f,height,2.8f,
                0xffffeb75,0xffffaa1d);
            // Raised purple lid, with golden horizontal band.
            gradient(left-size*.025f,top-size*.075f,
                width+size*.05f,size*.23f,7,
                0xffcf9bff,0xff7a3bdb);
            gradient(cx-size*.1f,top-size*.075f,
                size*.20f,size*.23f,2.0f,
                0xffffed78,0xffffb21e);
            Path bowL=new Path();
            bowL.moveTo(cx,top-size*.10f);
            bowL.cubicTo(cx-size*.19f,top-size*.44f,
                cx-size*.64f,top-size*.46f,
                cx-size*.51f,top-size*.34f);
            bowL.cubicTo(cx-size*.46f,top-size*.10f,
                cx-size*.15f,top-size*.04f,cx,top-size*.10f);
            bowL.close();
            Path bowR=new Path();
            bowR.moveTo(cx,top-size*.10f);
            bowR.cubicTo(cx+size*.19f,top-size*.44f,
                cx+size*.64f,top-size*.46f,
                cx+size*.51f,top-size*.34f);
            bowR.cubicTo(cx+size*.46f,top-size*.10f,
                cx+size*.15f,top-size*.04f,cx,top-size*.10f);
            bowR.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xffe88a0b);
            c.save();c.translate(0,1f);c.drawPath(bowL,p);
            c.drawPath(bowR,p);c.restore();
            p.setShader(new LinearGradient(cx-size*.5f,top-size*.55f,
                cx+size*.5f,top-size*.13f,
                0xffffed76,0xffffaf15,Shader.TileMode.CLAMP));
            c.drawPath(bowL,p);c.drawPath(bowR,p);
            p.setShader(null);
            p.setColor(0xfffff2a7);
            c.drawOval(cx-size*.44f,top-size*.35f,
                cx-size*.16f,top-size*.19f,p);
            c.drawOval(cx+size*.16f,top-size*.35f,
                cx+size*.44f,top-size*.19f,p);
            gradient(cx-size*.145f,top-size*.22f,
                size*.29f,size*.21f,6,
                0xffffeb77,0xffffa51c);
        }
        void dailyGiftBursts(float cx,float cy){
            // Gold petals sit outside the illustration and never cover copy.
            final float[][] rays={{-65,-20,-30},{-68,2,79},
                {63,-25,31},{67,3,-75}};
            for(float[] spec:rays){
                float x=cx+spec[0],y=cy+spec[1];
                c.save();c.rotate(spec[2],x,y);
                shape(x-2.9f,y-8.2f,5.8f,16.4f,3f,
                    0xffffbc30);
                c.restore();
            }
        }
        void missionSurface(float x,float y,float width,float height,
                            float radius,int color){
            // The illustrated mission rows have a restrained 2-unit soft
            // shadow; applying raised game-tile bevels makes every row look
            // twice as tall and puts shadows over neighboring content.
            shape(x,y+2,width,height,radius,
                theme==1?0xff162239:0x1cb0916d);
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,width,height,radius,
                lighten(color,theme==1?.07f:.27f),color);
        }
        void missionSoftIconBackground(float x,float y,float size){
            // Reference uses warm ivory mini-cards behind the illustrated
            // mission pictograms, with barely raised corners.
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);
            p.setColor(0x1b947a60);
            p.setShadowLayer(2f,0,2f,0x269f805f);
            c.drawRoundRect(x,y+1,x+size,y+size+1,11,11,p);
            p.clearShadowLayer();
            // gradient() preserves the Paint alpha; reset it after the
            // translucent shadow so the ivory body is not see-through.
            p.setColor(WHITE);
            gradient(x,y,size,size,11,0xfffffcf6,0xfff8ecdd);
        }
        void missionRaisedNumber(float x,float y,float size,int color,
                                 String number,float rotation){
            c.save();c.rotate(rotation,x+size/2f,y+size/2f);
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(2.1f,0f,2.5f,0x349c704c);
            p.setColor(darken(color,.14f));
            c.drawRoundRect(x,y+1.2f,x+size,y+size+1.2f,8,8,p);
            p.clearShadowLayer();
            gradient(x,y,size,size-2f,8,lighten(color,.18f),color);
            p.setColor(0x7dffffff);p.setStrokeWidth(.8f);
            p.setStyle(Paint.Style.STROKE);
            c.drawRoundRect(x+1.3f,y+1.3f,x+size-1.3f,
                y+size-3.5f,7.2f,7.2f,p);
            p.setStyle(Paint.Style.FILL);
            fitted(number,x+size/2f,y+size*.68f,
                number.length()>3?11:number.length()>2?15:
                    number.length()>1?18:21,size-4,WHITE,true);
            c.restore();
        }
        void missionOrangeRays(float x,float y){
            for(float[] line:MISSION_CORAL_RAYS){
                float cx=x+line[0],cy=y+line[1];
                c.save();c.rotate(line[2],cx,cy);
                shape(cx-2f,cy-5.5f,4f,11f,2f,0xffffb429);
                c.restore();
            }
        }
        void missionHand(float x,float y){
            // Explicitly hand-drawn pointer on the orange 2 + teal 4
            // icon, as in the supplied source (no emoji or network asset).
            c.save();c.translate(x,y);
            p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);
            Path finger=new Path();
            finger.moveTo(5,16);finger.lineTo(-1,7);
            finger.quadTo(-3,2,0,1);
            finger.quadTo(3,-1,5,3);
            finger.lineTo(9,11);finger.lineTo(10,7);
            finger.quadTo(12,4,15,7);
            finger.lineTo(20,17);finger.quadTo(24,24,18,27);
            finger.lineTo(12,29);finger.lineTo(5,26);
            finger.close();
            p.setColor(0xff304b73);p.setStrokeWidth(2.0f);
            p.setStrokeJoin(Paint.Join.ROUND);
            c.drawPath(finger,p);
            p.setColor(0xfffffff8);
            c.save();c.translate(0,-1f);c.scale(.93f,.92f,9,16);
            c.drawPath(finger,p);c.restore();
            c.restore();
        }
        void missionCalendarSeven(float centerX,float centerY){
            float x=centerX-22f,y=centerY-21f;
            gradient(x,y+1,44,45,10,0xfff8fff9,0xffe8e1d8);
            shape(x+1.8f,y+3,40.4f,13,8,0xffff694d);
            shape(x+8,y-1,5,13,2.5f,0xffe74e39);
            shape(x+29,y-1,5,13,2.5f,0xffe74e39);
            fitted("7",centerX,centerY+17,25,34,ink,true);
        }
        void missionTarget(float cx,float cy,float size){
            // Large coral bullseye and diagonal blue dart.
            icon("target",cx,cy,size,0xffff795b);
        }
        void missionIllustration(String name,float y,boolean portrait,
                                int accent){
            if(!portrait){
                return;
            }
            final float cx=62f, cy=y+34f;
            missionSoftIconBackground(38,y+7,51f);
            if(name.startsWith("Spoji 20")){
                missionRaisedNumber(41,y+13,28f,CORAL,"2",-11f);
                missionRaisedNumber(62,y+14,28f,TEAL,"4",9f);
                missionHand(54,y+29);
            }else if(name.startsWith("Dosegni 512")){
                missionRaisedNumber(42,y+11,45f,0xffb45bf6,"512",-9f);
                missionOrangeRays(81,y+17);
            }else if(name.startsWith("Igraj 7")){
                missionCalendarSeven(cx,cy);
            }else if(name.startsWith("Ostvari 10.000")){
                missionTarget(cx,cy,44f);
            }else if(name.startsWith("Majstor")){
                for(int i=0;i<MISSION_TILE_VALUES.length;i++)
                    missionRaisedNumber(40+(i%2)*23,y+10+(i/2)*23,
                        22,TILES[i+1],Integer.toString(MISSION_TILE_VALUES[i]),0);
            }else if(name.startsWith("Kralj")){
                icon("crown",cx,cy+2,48,0xffffaf25);
            }else{
                icon("star",cx,cy,38,accent);
            }
        }
        void missionWeeklyHeaderIcon(float cx,float cy){
            // Blue outlined weekly planner, rather than a red daily calendar.
            p.clearShadowLayer();p.setShader(null);
            shape(cx-10,cy-10,20,21,4,0xff137ecb);
            shape(cx-7,cy-5,14,12,2,0xffebf7ff);
            shape(cx-6,cy-12,4,7,2,0xff2a99ed);
            shape(cx+2,cy-12,4,7,2,0xff2a99ed);
            p.setColor(0xff3d91e4);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.4f);
            c.drawLine(cx-4,cy+1,cx+4,cy+1,p);
            c.drawLine(cx-4,cy+5,cx+3,cy+5,p);
            p.setStyle(Paint.Style.FILL);
        }
        int completedAchievements(){
            int result=0;
            if((rewardFlags&8)!=0)result++;
            if((rewardFlags&16)!=0)result++;
            return result;
        }
        void missions() {
            boolean portrait=referencePortraitLayout;
            back();brand(portrait?45:67,39);
            // Keep both header words clear of the upper-left back button.
            fitted("Misije i",125,portrait?99:128,
                portrait?36:33,142,ink,true);
            fitted("postignuća",274,portrait?99:128,
                portrait?36:33,203,CORAL,true);
            // Three section colors follow the Dnevne / Tjedne / Postignuca cards.
            int dailyTint=theme==1?0xff293b4c:0xfffff0df;
            int weeklyTint=theme==1?0xff253b4f:0xffe9f5ff;
            int achievementTint=theme==1?0xff24483f:0xffe5fbef;
            missionSurface(17,portrait?134:179,366,portrait?185:209,24,dailyTint);
            missionSurface(17,portrait?328:398,366,portrait?185:207,24,weeklyTint);
            missionSurface(17,portrait?522:614,366,portrait?187:249,24,achievementTint);
            float sunY=portrait?148:204;
            rays(43,sunY,19,0xffffb431);
            p.setShader(null);p.setColor(0xffffb431);
            p.setStyle(Paint.Style.FILL);c.drawCircle(43,sunY,6,p);
            txt("Dnevne",110,portrait?150:204,23,ink,true);
            missionPeriodPill("day",portrait?134:182);
            if(portrait&&theme!=1)
                missionWeeklyHeaderIcon(43,portrait?346:420);
            else icon("calendar",43,portrait?346:420,23,0xff3d98f0);
            icon("crown",43,portrait?540:636,23,0xffffb431);
            missionPeriodPill("week",portrait?330:402);
            // The source layout counts achievements, not star currency.
            // Only two achievements currently exist; don't display a fictive
            // 4/20 or incorrectly label the total stars as achievements.
            float countTop=portrait?522:619;
            missionSurface(299,countTop,76,25,13,
                theme==1?card:0xfffff5e5);
            fitted(completedAchievements()+"/2",337,countTop+18,
                13,66,muted,true);
            missionRow("Spoji 20 pločica","Spoji pločice tijekom dana",
                todayMerges,20,50,portrait?160:212,TEAL);
            boolean noUndo512=(rewardFlags&2)!=0 ||
                (game!=null&&game.peak>=512&&!undoUsedThisGame);
            missionRow("Dosegni 512 bez poništavanja","Ostvari pločicu 512 bez poništavanja",
                noUndo512?1:0,1,100,portrait?238:308,0xffa663e4);
            txt("Tjedne",105,portrait?348:422,23,ink,true);
            missionRow("Igraj 7 dana zaredom","Sedam različitih dana",
                playDayStreak,7,150,portrait?356:433,0xff3ecb8c);
            missionRow("Ostvari 10.000 bodova ukupno","Skupljaj bodove tijekom tjedna",
                weekPoints,10000,200,portrait?434:516,0xffff934b);
            txt("Postignuća",132,portrait?542:638,23,ink,true);
            missionRow("Majstor spajanja","Dosegni pločicu 1024",
                recordTile,1024,500,portrait?550:648,0xffa663e4);
            missionRow("Kralj rezultata","Osvoji 50.000 bodova",
                best,50000,1000,portrait?627:741,0xffff934b);
        }
        void missionPeriodPill(String period,int y){
            boolean week="week".equals(period);
            float x=week?290:283;
            int width=week?87:94;
            missionSurface(x,y,width,25,13,theme==1?card:0xfffffcf6);
            icon("clock",x+15,y+12,15,muted);
            fitted(timeRemainingInPeriod(week),x+width/2+6,y+18,
                11,width-32,muted,true);
        }
        void missionRow(String name,String sub,int value,int target,int reward,int y,int accent){
            boolean portrait=referencePortraitLayout;
            int height=portrait?70:80;
            missionSurface(26,y,348,height,19,card);
            if(!portrait||theme==1)
                shape(39,y+(portrait?9:10),portrait?46:52,
                    portrait?50:58,15,
                    theme==1?0xff35435d:lighten(accent,.80f));
            String taskGlyph=value>=target?"check":
                name.contains("Kralj")?"crown":
                name.contains("Igraj")?"calendar":
                name.contains("Majstor")?"grid":
                name.contains("Spoji")?"grid":"target";
            if(portrait&&theme!=1){
                missionIllustration(name,y,true,accent);
            }else if(name.startsWith("Dosegni 512")){
                styledTile(42,y+12,40,40,10,0xffa663e4,0);
                fitted("512",62,y+40,18,39,WHITE,true);
            }else if(name.startsWith("Spoji 20")){
                styledTile(42,y+15,21,24,6,CORAL,0);
                styledTile(62,y+15,21,24,6,TEAL,0);
                fitted("2",52,y+33,16,19,WHITE,true);
                fitted("4",72,y+33,16,19,WHITE,true);
            }else if(name.startsWith("Majstor")){
                int[] mini={2,4,8,16};
                for(int index=0;index<4;index++){
                    float mx=41+(index%2)*22,my=y+10+(index/2)*22;
                    styledTile(mx,my,20,20,5,TILES[index+1],0);
                    fitted(Integer.toString(mini[index]),mx+10,my+14,
                        mini[index]>9?9:11,18,WHITE,true);
                }
            }else if(name.startsWith("Igraj 7")){
                // Original calendar pictogram shows a clearly readable 7.
                shape(42,y+13,40,39,8,WHITE);
                shape(42,y+13,40,13,7,CORAL);
                shape(48,y+9,6,11,3,INK);
                shape(69,y+9,6,11,3,INK);
                fitted("7",62,y+48,23,34,INK,true);
            }else{
                icon(taskGlyph,portrait?62:65,y+(portrait?33:38),
                    portrait?26:31,accent);
            }
            // Original: titles and descriptions begin after the pictogram,
            // rather than floating in the center of their entire row.
            float titleBaseline=y+(portrait?21:26);
            leftFitted(name,103,titleBaseline,portrait?15.5f:17.5f,
                179,ink,true);
            if(name.startsWith("Dosegni 512")){
                leftFitted("Ostvari pločicu 512",103,
                    y+(portrait?36:43),portrait?11:12,178,muted,false);
                leftFitted("bez korištenja poništavanja",103,
                    y+(portrait?46:55),portrait?10:11,177,muted,false);
            }else{
                leftFitted(sub,103,y+(portrait?38:48),
                    portrait?11.5f:12.5f,177,muted,false);
            }
            // Thicker progress track and a separate adjacent counter: the
            // reference places progress and 12 / 20 on a common baseline.
            float progressY=y+(portrait?50:59);
            shape(104,progressY,151,7,4,
                theme==1?0xff40516c:0xffdce3e8);
            // "Majstor" is an unlock, not an incremental reward. Keep
            // the filled bar consistent with its 0/1 or 1/1 counter:
            // a previous 512 tile must not display a fake 50% unlock.
            boolean binaryMilestone=name.startsWith("Majstor");
            float fraction=binaryMilestone?(value>=target?1f:0f):
                Math.max(0,Math.min(1,(float)value/target));
            if(fraction>0)shape(104,progressY,
                151*fraction,7,4,accent);
            int displayValue=binaryMilestone?(value>=1024?1:0):Math.min(value,target);
            int displayTarget=binaryMilestone?1:target;
            fitted(fmt(displayValue)+"/"+fmt(displayTarget),
                270,y+(portrait?59:68),11,45,muted,true);
            // Narrower, right-aligned reward bay matches the source card
            // and gives the adjacent real-progress counter enough space.
            float rewardCenter=portrait?345:323;
            if(portrait&&theme!=1)
                gradient(315,y+4,54,61,15,0xfffff9e7,0xfffff0d3);
            else missionSurface(portrait?315:289,y+(portrait?4:6),
                portrait?54:71,portrait?61:68,15,
                theme==1?0xff3b3b4c:0xfffff0d5);
            icon("star",rewardCenter,y+(portrait?23:29),
                portrait?31:29,0xffffad1a);
            fitted("+"+fmt(reward),rewardCenter,y+(portrait?55:62),
                portrait?14:13,portrait?49:63,ink,true);
        }
        JSONArray history() {
            try{return new JSONArray(prefs.getString("history","[]"));}
            catch(Exception ignored){return new JSONArray();}
        }
        String historyTimestamp(JSONObject row){
            if(row==null)return "";
            String day=row.optString("day","");
            String time=row.optString("time","");
            return time.isEmpty()?day:day+" · "+time;
        }
        int consecutiveWins(){
            // Unlike playDayStreak (calendar days used by missions), this is
            // the actual number of consecutive victories in saved game order.
            // Missing legacy "won" flags are never assumed to be victories.
            JSONArray recent=history();
            int wins=0;
            for(int i=0;i<recent.length();i++){
                JSONObject row=recent.optJSONObject(i);
                if(row==null||!row.optBoolean("won",false))break;
                wins++;
            }
            return wins;
        }
        void statistics(){
            if(referencePortraitLayout){statisticsCompact();return;}
            title("Statistika");
            rays(52,122,18,CORAL);rays(352,122,18,CORAL);
            statCell("Najbolji rezultat",fmt(best),20,151,114,112,CORAL);
            statCell("Ukupno partija",fmt(plays),143,151,114,112,TEAL);
            statCell("Najveća pločica",Integer.toString(recordTile),266,151,114,112,0xffffb431);
            int average=plays==0?0:total/plays;
            statCell("Prosjek bodova",fmt(average),21,276,174,102,0xff478df4);
            statCell("Niz pobjeda",fmt(consecutiveWins()),205,276,174,102,0xffa657de);
            statsSoftCard(22,399,356,219,24,card);
            txt("Zadnjih 10 partija",114,432,19,ink,true);
            card(213,411,157,33,17,theme==1?0xff34445f:0xfff4ebdf);
            shape(215+(chartMetric==0?0:77),413,77,29,15,CORAL);
            fitted("Bodovi",253,434,13,71,chartMetric==0?WHITE:ink,true);
            fitted("Pločica",330,434,13,71,chartMetric==1?WHITE:ink,true);
            taps.add(new Tap(214,405,291,453,"Grafikon bodova",()->{
                chartMetric=0;invalidate();
            }));
            taps.add(new Tap(292,405,371,453,"Grafikon pločica",()->{
                chartMetric=1;invalidate();
            }));
            JSONArray data=history();
            int count=Math.min(10,data.length());
            if(count==0){
                txt("Grafikon će se prikazati nakon",200,519,16,muted,false);
                txt("prve završene partije.",200,545,16,muted,false);
            }else{
                int ceiling=1;
                for(int i=0;i<count;i++){
                    JSONObject row=data.optJSONObject(i);
                    if(row==null)continue;
                    int value=chartMetric==0?row.optInt("score"):row.optInt("peak");
                    ceiling=Math.max(ceiling,value);
                }
                float chartLeft=42,barSlot=315f/Math.max(10,count);
                for(int i=0;i<count;i++){
                    JSONObject row=data.optJSONObject(count-1-i);
                    int val=row==null?0:(chartMetric==0?
                        row.optInt("score"):row.optInt("peak"));
                    float x=chartLeft+i*barSlot+2;
                    if(val>0){
                        float height=Math.max(4f,112f*val/ceiling);
                        float y=581-height;
                        shape(x,y,barSlot-6,height,Math.min(6f,height/2f),
                            val==ceiling?CORAL:(i%2==0?CORAL:TEAL));
                        fitted(fmt(val),x+(barSlot-6)/2,y-9,9,barSlot+3,ink,true);
                    }
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
                fitted(historyTimestamp(row),102,y+27,13,158,muted,false);
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
        void statsSoftCard(float x,float y,float width,float height,float radius,int color){
            // Reference KPI cards use pastel surfaces and a subtle soft
            // shadow, not the raised 6-unit extruded game-tile treatment.
            shape(x,y+2.4f,width,height,radius,
                theme==1?0xff142138:0x17b68c6a);
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(WHITE);
            gradient(x,y,width,height,radius,lighten(color,theme==1?.08f:.11f),color);
        }
        void statCell(String caption,String value,int x,int y,int width,int h,int accent){
            int bg=theme==1?card:lighten(accent,.84f);
            statsSoftCard(x,y,width,h,21,bg);
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
                case "Niz pobjeda":top="Niz";bottom="pobjeda";break;
                default:top="Niz dana";bottom="igranja";
            }
            // Pixel comparison showed that the earlier native KPI scale
            // matches the owner reference better than enlarging everything.
            boolean referenceStats=referencePortraitLayout&&
                "stats".equals(page)&&theme!=1;
            icon(symbol,x+(width<=118?27:38),
                y+(referenceStats?(width<=118?38:43):29),
                referenceStats?(width<=118?34:43):
                    (width<=118?24:29),
                "gamepad".equals(symbol)?ink:accent);
            float labelX=x+(width<=118?77:98);
            float labelWidth=width<=118?68:123;
            boolean dense=h<90;
            fitted(top,labelX,y+(dense?24:31),width<=118?13:16,labelWidth,ink,true);
            fitted(bottom,labelX,y+(dense?41:48),width<=118?13:16,labelWidth,ink,true);
            fitted(value,x+width/2,y+h-(dense?11:14),
                referenceStats?(dense?30:35):(dense?21:29),
                width-10,accent,true);
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
            fitted(historyTimestamp(row),200,448,17,318,muted,false);
            card(26,523,165,121,20,card);
            card(209,523,165,121,20,card);
            txt("Najveća pločica",108,565,17,muted,true);
            fitted(fmt(row.optInt("peak")),108,615,29,145,TEAL,true);
            txt("Potezi",291,565,17,muted,true);
            fitted(fmt(row.optInt("moves")),291,615,29,145,ink,true);
            button("Natrag na statistiku","back",28,729,344,69,CORAL,true,
                ()->navigate("stats"));
        }
        void themesTabIcon(String type,float cx,float cy,float width,int color){
            if(!"crown".equals(type)&&!"palette".equals(type)){
                icon(type,cx,cy,width,color);return;
            }
            c.save();c.translate(cx-width/2,cy-width/2);
            c.scale(width/24,width/24);
            p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);
            if("crown".equals(type)){
                Path crown=new Path();
                crown.moveTo(2,8);crown.lineTo(7,12.5f);
                crown.lineTo(12,3);crown.lineTo(17,12.5f);
                crown.lineTo(22,8);crown.lineTo(20,20);
                crown.lineTo(4,20);crown.close();
                c.drawPath(crown,p);
            }else{
                c.drawCircle(12,12,10,p);
                p.setColor(theme==1?0xff263653:CREAM);
                c.drawCircle(7,9,1.4f,p);c.drawCircle(13,6.5f,1.4f,p);
                c.drawCircle(18,12,1.4f,p);c.drawCircle(10,17,1.4f,p);
                c.drawCircle(18,19,2.5f,p);
            }
            c.restore();
        }
        void themesTitlePill(float y,float h){
            p.setShader(null);p.setColor(WHITE);
            gradient(53,y,294,h,h/2,theme==1?0xff30394b:0xfffff1dc,
                theme==1?0xff253047:0xfffff7ed);
        }
        // Theme-only preview bevels: don't change the real game tiles.
        void themePreviewTile(float x,float y,float w,float h,int color,int style){
            float radius=style==1?5:style==2?12:9;
            if(style==1){shape(x,y,w,h,radius,color);return;}
            shape(x+1,y+2.5f,w,h-2,radius,darken(color,.15f));
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,w,h-3,radius,
                lighten(color,style==2?.23f:.14f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.65f);p.setColor(0x4dffffff);
            c.drawRoundRect(x+1,y+1,x+w-1,y+h-5,
                Math.max(2,radius-1),Math.max(2,radius-1),p);
            p.setStyle(Paint.Style.FILL);
            if(style==2){
                // Glass tile is visibly different from the rounded tile,
                // with a diagonal white corner reflection as in the design.
                p.setShader(null);p.setStrokeWidth(Math.max(1.4f,w*.075f));
                p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(0xd5ffffff);p.setStyle(Paint.Style.STROKE);
                Path shine=new Path();
                shine.moveTo(x+w*.17f,y+h*.32f);
                shine.quadTo(x+w*.17f,y+h*.13f,x+w*.37f,y+h*.13f);
                c.drawPath(shine,p);
                p.setStrokeCap(Paint.Cap.BUTT);
                p.setStyle(Paint.Style.FILL);
            }
        }

        // Scope Screen 07's preview materials to the picker only.
        // Actual game tile colors and the persisted game are untouched.
        void themeChoiceSurface(float x,float y,float w,float h,int preset){
            int top=preset==2?0xff9f54f9:preset==1?0xff2d405e:0xfffff7eb;
            int bottom=preset==2?0xff177be9:preset==1?0xff101c32:0xfff1e5d6;
            shape(x+1.1f,y+3,w-2.2f,h-1,17,
                theme==1?0x55000000:0x239d7957);
            // Shadow uses translucent Paint; reset it before an opaque shader.
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,w,h-2,17,top,bottom);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setColor(preset==1?0x45ffffff:0x90ffffff);
            p.setStrokeWidth(1f);
            c.drawRoundRect(x+1,y+1,x+w-1,y+h-3,16,16,p);
            p.setStyle(Paint.Style.FILL);
        }
        void themeBoardTray(float x,float y,float w,float h){
            shape(x+1,y+3,w-2,h-1,18,
                theme==1?0x44000000:0x18a17b59);
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,w,h-2,17,
                theme==1?0xff3b4b62:0xfffff7ec,
                theme==1?0xff28364c:0xfff1e7da);
        }
        void themes(){
            if(referencePortraitLayout){themesCompact();return;}
            brand(61,38);
            themesTitlePill(99,53);
            fitted("Teme i",158,126,35,165,ink,true);
            fitted("izgled",276,126,35,166,CORAL,true);
            fitted("Prilagodi igru svojem stilu.",200,169,18,350,muted,false);
            String[] names={"Svijetla","Tamna","Živopisna"};
            for(int i=0;i<3;i++){
                final int chosen=i;
                int x=19+i*126;
                if(theme==i){
                    card(x,197,116,174,22,card);
                    p.setColor(CORAL);p.setShader(null);p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(3.2f);
                    c.drawRoundRect(x+1.5f,198.5f,x+114.5f,369.5f,21,21,p);
                    p.setStyle(Paint.Style.FILL);
                }
                themeChoiceSurface(x+7,207,102,109,i);
                int[] cs=i==2?new int[]{0xffff5a92,0xff58d649,0xff19b7f0,0xffb35ceb}:
                    new int[]{TILES[1],TILES[2],TILES[3],TILES[4]};
                for(int j=0;j<4;j++){
                    int a=j%2,b=j/2;
                    themePreviewTile(x+15+a*45,218+b*43,40,39,cs[j],0);
                    txt(Integer.toString(2<<j),x+35+a*45,246+b*43,21,WHITE,true);
                }
                fitted(names[i],x+58,362,17,108,ink,true);
                // Selection state is a centered radio-like badge below preview.
                shape(x+47,320,22,22,11,theme==i?CORAL:0xffe4e3e0);
                if(theme==i)icon("check",x+58,331,13,WHITE);
                taps.add(new Tap(x,197,x+116,371,names[i],()->{
                    theme=chosen;persist();updateBars();invalidate();
                }));
            }
            card(20,395,360,405,28,card);
            leftFitted("Pregled",30,434,24,167,ink,true);
            leftFitted("Stil pločica",210,433,19,162,ink,true);
            shape(201,416,1,360,0,theme==1?0xff47536a:0xffebdfd0);
            // Real visual tile preview for all 16 values, independent of the game board.
            themeBoardTray(29,465,167,172);
            int[] sample={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
            for(int i=0;i<16;i++){
                int val=sample[i],col=i%4,row=i/4;
                float x=34+col*39.8f,y=470+row*40f;
                themePreviewTile(x,y,36,36,tileColor(val),tileStyle);
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
                // Use the warm peach sample from the supplied swatch cards,
                // not the game's much redder numbered "2" tile.
                int swatchColor=i==2?0xffffc8ad:
                    (i==1?0xffffa87d:0xffffad6a);
                themePreviewTile(x+6,465,39,44,swatchColor,i);
                fitted(styles[i],x+25,537,9.5f,49,ink,true);
                // Original source shows a radio badge beneath each swatch.
                shape(x+19,546,13,13,7,tileStyle==i?CORAL:0xffdedfdf);
                if(tileStyle==i)icon("check",x+25.5f,552.5f,9,WHITE);
                taps.add(new Tap(x,456,x+51,563,styles[i],()->{
                    tileStyle=value;persist();invalidate();
                }));
            }
            // Source shows labels, explanations and dividers, not bare toggles.
            shape(208,565,161,1,0,theme==1?0xff4a5871:0xffe9dfd4);
            icon("phone-vibrate",220,587,29,ink);
            leftFitted("Vibracija",245,590,17,86,ink,true);
            leftFitted("Kada spojiš brojeve.",245,610,10,86,muted,false);
            shape(337,570,43,27,14,vibration?CORAL:0xffb7c1cf);
            shape(vibration?355:339,572,23,23,12,WHITE);
            taps.add(new Tap(205,564,378,615,"Vibracija",()->{
                vibration=!vibration;persist();invalidate();
            }));
            icon("sound",219,655,29,ink);
            leftFitted("Zvukovi",245,658,17,86,ink,true);
            leftFitted("Zvučni efekti u igri.",245,678,10,86,muted,false);
            shape(337,636,43,27,14,sound?CORAL:0xffb7c1cf);
            shape(sound?355:339,638,23,23,12,WHITE);
            shape(208,696,161,1,0,theme==1?0xff4a5871:0xffe9dfd4);
            taps.add(new Tap(205,628,378,683,"Zvukovi",()->{
                sound=!sound;persist();invalidate();
            }));
            leftFitted("Jačina animacija",208,720,18,162,ink,true);
            leftFitted("Koliko su izražene animacije spajanja.",208,738,
                10,166,muted,false);
            shape(211,756,159,9,5,theme==1?0xff536078:0xffe8ded1);
            shape(211,756,11+animation*68,9,5,CORAL);
            shape(211+animation*68,748,23,23,12,CORAL);
            String[] intensities={"Niska","Srednja","Visoka"};
            for(int i=0;i<3;i++){
                int pos=i;
                fitted(intensities[i],236+i*54,791,12,62,muted,false);
                float left=i==0?208:i==1?262:316;
                float right=i==0?261:i==1?315:378;
                taps.add(new Tap(left,739,right,801,intensities[i],()->{
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
                themesTabIcon(glyphs[i],cx,830,27,i==2?CORAL:muted);
                fitted(i==2?"Teme i izgled":tabs[i],cx,863,
                    i==2?10:13,86,i==2?CORAL:muted,true);
                taps.add(new Tap(22+i*89,805,110+i*89,877,tabs[i],
                    ()->navigate(destinations[index])));
            }
        }


        boolean statsTouchExploration(){
            // Keep the owner's dense five-row layout normally, but expand
            // targets and permit scrolling for TalkBack/touch exploration.
            android.view.accessibility.AccessibilityManager manager=
                (android.view.accessibility.AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
            return manager!=null&&manager.isTouchExplorationEnabled();
        }
        void statsHeaderBurst(float cx,float cy,boolean right){
            // Three source-style coral rays at either side of the rounded title.
            final float sign=right?1f:-1f;
            float[] yOffsets={-13f,0f,13f};
            float[] turns={right?-32f:32f,90f,right?32f:-32f};
            for(int i=0;i<3;i++){
                float x=cx+sign*(i==1?3f:0f),y=cy+yOffsets[i];
                c.save();c.rotate(turns[i],x,y);
                shape(x-2.8f,y-9.0f,5.6f,18f,2.8f,CORAL);
                c.restore();
            }
        }
        void statisticsCompact(){
            back();
            brand(40,35);
            fitted("Statistika",200,116,43,325,ink,true);
            statsHeaderBurst(79,104,false);
            statsHeaderBurst(354,104,true);
            statCell("Najbolji rezultat",fmt(best),20,132,114,94,CORAL);
            statCell("Ukupno partija",fmt(plays),143,132,114,94,TEAL);
            statCell("Najveća pločica",Integer.toString(recordTile),266,132,114,94,0xffffb431);
            int average=plays==0?0:total/plays;
            statCell("Prosjek bodova",fmt(average),21,235,174,84,0xff478df4);
            statCell("Niz pobjeda",fmt(consecutiveWins()),205,235,174,84,0xffa657de);
            statsSoftCard(16,329,368,158,23,card);
            leftFitted("Zadnjih 10 partija",30,353,20,199,ink,true);
            shape(223,337,149,25,14,theme==1?0xff34445f:0xfff4ebdf);
            shape(225+(chartMetric==0?0:72),339,72,21,12,CORAL);
            fitted("Bodovi",261,354,11,67,chartMetric==0?WHITE:ink,true);
            fitted("Najveća pločica",334,354,9.5f,73,
                chartMetric==1?WHITE:ink,true);
            taps.add(new Tap(223,324,297,372,"Grafikon bodova",()->{
                chartMetric=0;invalidate();
            }));
            taps.add(new Tap(297,324,374,372,"Grafikon pločica",()->{
                chartMetric=1;invalidate();
            }));
            JSONArray data=history();
            int count=Math.min(10,data.length());
            if(count==0){
                txt("Grafikon će se prikazati nakon",200,412,15,muted,false);
                txt("prve završene partije.",200,436,15,muted,false);
            }else{
                int ceiling=1;
                for(int i=0;i<count;i++){
                    JSONObject row=data.optJSONObject(i);
                    if(row==null)continue;
                    int value=chartMetric==0?row.optInt("score"):row.optInt("peak");
                    ceiling=Math.max(ceiling,value);
                }
                float barSlot=315f/Math.max(10,count);
                for(int i=0;i<count;i++){
                    JSONObject row=data.optJSONObject(count-1-i);
                    int val=row==null?0:(chartMetric==0?
                        row.optInt("score"):row.optInt("peak"));
                    float x=40+i*barSlot+2;
                    // A zero-value history row is not an earned bar. The
                    // former minimum 4-unit pillar falsely visualized 0.
                    if(val>0){
                        float barHeight=Math.max(4f,78f*val/ceiling);
                        float y=463-barHeight;
                        int barColor=val==ceiling?CORAL:(i%2==0?CORAL:TEAL);
                        p.setShader(null);p.setColor(WHITE);
                        gradient(x,y,barSlot-6,barHeight,Math.min(5f,barHeight/2),
                            lighten(barColor,.17f),barColor);
                        fitted(fmt(val),x+(barSlot-6)/2,y-7,9,
                            barSlot+4,ink,true);
                    }
                    fitted(Integer.toString(i+1),x+(barSlot-6)/2,478,
                        10,barSlot-4,muted,false);
                }
            }
            // One visual history panel as in the reference; row cards are flat,
            // separated by a 4-unit gap, never five heavy elevated panels.
            final boolean accessibleRows=statsTouchExploration();
            statsSoftCard(16,497,368,accessibleRows?273:204,23,card);
            leftFitted("Povijest partija",30,522,19,250,ink,true);
            int visible=Math.min(5,data.length());
            if(visible==0){
                shape(25,529,350,32,13,theme==1?0xff34445f:0xfffaf5ee);
                txt("Još nema dovršenih partija.",200,552,15,muted,false);
            }
            for(int i=0;i<visible;i++){
                JSONObject row=data.optJSONObject(i);
                if(row==null)continue;
                int pitch=accessibleRows?48:34;
                int y=530+i*pitch,idx=i;
                int score=row.optInt("score");
                boolean record=best>0&&score>=best;
                shape(25,y,350,accessibleRows?44:30,12,
                    theme==1?0xff34445f:0xfffaf5ee);
                leftFitted(row.optString("day",""),33,y+21,11.3f,83,muted,false);
                String completedAt=row.optString("time","");
                // Legacy archives have no time: keep the field empty, never
                // fabricate a timestamp for past users.
                if(!completedAt.isEmpty())
                    leftFitted(completedAt,117,y+21,11.3f,46,muted,false);
                fitted(fmt(score),205,y+22,17,87,record?CORAL:TEAL,true);
                if(record)icon("crown",265,y+15,17,0xffff9e23);
                int peak=row.optInt("peak",0);
                if(peak>0){
                    int tagBg=peak>=1024?0xffc7f7e6:
                        peak>=512?0xffc7dfff:peak>=128?0xffefdbff:0xffffeacc;
                    int tagFg=peak>=1024?0xff078e77:
                        peak>=512?0xff2167c8:peak>=128?0xff9443bd:0xffc67914;
                    shape(282,y+4,59,23,10,theme==1?0xff46416b:tagBg);
                    fitted(Integer.toString(peak),311.5f,y+21,14,54,
                        theme==1?WHITE:tagFg,true);
                }
                icon("chevron-right",366,y+15,15,muted);
                taps.add(new Tap(22,y-2,378,y+(accessibleRows?46:32),
                    "Detalji partije "+(i+1),
                    ()->{selectedHistoryIndex=idx;navigate("historyDetail");}));
            }
        }
        void themesCompact(){
            // Measured against original supplied 941x1672 screen.
            brand(52,42);
            themesTitlePill(85,53);
            fitted("Teme i",158,125,36,165,ink,true);
            fitted("izgled",276,125,36,166,CORAL,true);
            fitted("Prilagodi igru svojem stilu.",200,154,16,344,muted,false);
            String[] names={"Svijetla","Tamna","Živopisna"};
            for(int i=0;i<3;i++){
                final int chosen=i;float x=19+i*126,y=165;
                if(theme==i){
                    card(x,y,116,143,20,card);
                    p.setColor(CORAL);p.setShader(null);p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(3f);
                    c.drawRoundRect(x+1.5f,y+1.5f,x+114.5f,y+141.5f,19,19,p);
                    p.setStyle(Paint.Style.FILL);
                }
                themeChoiceSurface(x+7,y+18,102,96,i);
                int[] colors=i==2?
                    new int[]{0xffff5a92,0xff58d649,0xff19b7f0,0xffb35ceb}:
                    new int[]{TILES[1],TILES[2],TILES[3],TILES[4]};
                for(int j=0;j<4;j++){
                    int xx=j%2,yy=j/2;
                    themePreviewTile(x+15+xx*45,y+29+yy*38,40,35,colors[j],0);
                    txt(Integer.toString(2<<j),
                        x+35+xx*45,y+55+yy*38,19,WHITE,true);
                }
                fitted(names[i],x+58,y+157,16,108,ink,true);
                // Original design: selector beneath the four numbered tiles.
                shape(x+47,y+114,22,22,11,theme==i?CORAL:0xffe4e3e0);
                if(theme==i)icon("check",x+58,y+125,13,WHITE);
                taps.add(new Tap(x,y,x+116,y+143,names[i],()->{
                    theme=chosen;persist();updateBars();invalidate();
                }));
            }
            card(20,335,360,293,26,card);
            leftFitted("Pregled",30,370,22,167,ink,true);
            leftFitted("Stil pločica",201,369,18,172,ink,true);
            shape(193,356,1,257,0,theme==1?0xff47536a:0xffebdfd0);
            themeBoardTray(29,411,163,169);
            int[] sample={2,4,8,16,4,8,16,32,8,16,32,64,128,256,512,1024};
            for(int i=0;i<16;i++){
                int val=sample[i],col=i%4,row=i/4;
                float x=34+col*39.8f,y=416+row*39.6f;
                themePreviewTile(x,y,36,36,tileColor(val),tileStyle);
                fitted(Integer.toString(val),x+18,y+25,
                    val>=1000?11:15,33,WHITE,true);
            }
            leftFitted("Ovako će izgledati",30,384,12,159,muted,false);
            leftFitted("tvoja ploča u odabranoj temi.",30,399,11,161,muted,false);
            String[] styles={"Zaobljene","Ravne","Staklaste"};
            for(int i=0;i<3;i++){
                final int value=i;float x=210+i*56;
                card(x,371,51,69,13,card);
                if(tileStyle==i){
                    p.setColor(CORAL);p.setShader(null);p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(2.5f);
                    c.drawRoundRect(x+1.5f,372.5f,x+49.5f,438.5f,12,12,p);
                    p.setStyle(Paint.Style.FILL);
                }
                int swatchColor=i==2?0xffffc8ad:
                    (i==1?0xffffa87d:0xffffad6a);
                themePreviewTile(x+6,383,39,39,swatchColor,i);
                fitted(styles[i],x+25,434,9,48,ink,true);
                shape(x+19,440,13,13,7,tileStyle==i?CORAL:0xffdedfdf);
                if(tileStyle==i)icon("check",x+25.5f,446.5f,9,WHITE);
                taps.add(new Tap(x,371,x+51,461,styles[i],()->{
                    tileStyle=value;persist();invalidate();
                }));
            }
            shape(208,461,161,1,0,theme==1?0xff4a5871:0xffe9dfd4);
            icon("phone-vibrate",219,476,26,ink);
            leftFitted("Vibracija",245,480,15,86,ink,true);
            leftFitted("Kada spojiš brojeve.",245,495,10,86,muted,false);
            shape(337,466,43,27,14,vibration?CORAL:0xffb7c1cf);
            shape(vibration?355:339,468,23,23,12,WHITE);
            taps.add(new Tap(205,461,378,509,"Vibracija",()->{
                vibration=!vibration;persist();invalidate();
            }));
            icon("sound",219,520,26,ink);
            leftFitted("Zvukovi",245,523,15,86,ink,true);
            leftFitted("Zvučni efekti u igri.",245,538,10,86,muted,false);
            shape(337,511,43,27,14,sound?CORAL:0xffb7c1cf);
            shape(sound?355:339,513,23,23,12,WHITE);
            shape(208,550,161,1,0,theme==1?0xff4a5871:0xffe9dfd4);
            taps.add(new Tap(205,509,378,557,"Zvukovi",()->{
                sound=!sound;persist();invalidate();
            }));
            leftFitted("Jačina animacija",208,570,17,162,ink,true);
            leftFitted("Koliko su izražene animacije spajanja.",
                208,588,9.5f,166,muted,false);
            shape(211,599,159,8,5,theme==1?0xff536078:0xffe8ded1);
            shape(211,599,11+animation*68,8,5,CORAL);
            shape(211+animation*68,592,22,22,11,CORAL);
            String[] intensities={"Niska","Srednja","Visoka"};
            for(int i=0;i<3;i++){
                int pos=i;
                fitted(intensities[i],236+i*54,619,11,62,muted,false);
                float left=i==0?208:i==1?262:316;
                float right=i==0?261:i==1?315:378;
                taps.add(new Tap(left,582,right,627,
                    intensities[i],()->{
                        animation=pos;persist();invalidate();
                    }));
            }
            card(20,635,360,69,28,card);
            String[] tabs={"Novo","Rekordi","Teme","Postavke"};
            String[] glyphs={"home","crown","palette","settings"};
            String[] destinations={"home","stats","themes","settings"};
            for(int i=0;i<4;i++){
                final int index=i;float cx=65+i*90;
                if(i==2)shape(205,639,90,61,25,
                    theme==1?0xff453a48:0xffffecdf);
                themesTabIcon(glyphs[i],cx,658,26,i==2?CORAL:muted);
                fitted(i==2?"Teme i izgled":tabs[i],cx,690,
                    i==2?10:13,86,i==2?CORAL:muted,true);
                taps.add(new Tap(22+i*89,635,110+i*89,704,tabs[i],
                    ()->navigate(destinations[index])));
            }
        }

        // The reference settings screen has its own restrained cards/icons.
        // Do not globally alter the raised tiles used in the actual game.
        void settingsPanel(float x,float y,float width,float height,int radius,int color){
            shape(x,y+2.5f,width,height,radius,
                theme==1?0xff132038:0x22c5ad91);
            // A translucent shadow must not leak its Paint alpha into
            // the subsequent opaque gradient fill.
            p.setShader(null);p.setColor(WHITE);
            // Distinguish the footer's cream surface from seven white
            // setting rows, as in the supplied 941x1672 Settings reference.
            boolean creamFooter=theme!=1&&color==0xfffff3e6;
            gradient(x,y,width,height,radius,
                theme==1?lighten(color,.09f):
                    (creamFooter?0xfffffbf3:WHITE),
                theme==1?color:
                    (creamFooter?0xfffff3e6:0xfffffdfa));
        }
        // Screen 08 uses its own glossy material. This does not alter
        // gameplay tiles, the launcher, or icons on other screens.
        void settingsIconTile(float x,float y,float size,int color,String glyph){
            shape(x+.7f,y+3.1f,size-1.4f,size-.5f,12,
                theme==1?0x66000000:0x26966c51);
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,size,size-1.5f,12,lighten(color,.17f),color);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(.8f);p.setColor(0x7bffffff);
            c.drawRoundRect(x+1.1f,y+1.1f,x+size-1.1f,y+size-3.3f,11,11,p);
            p.setStyle(Paint.Style.FILL);
            float cx=x+size/2f,cy=y+size/2f;
            if("sound".equals(glyph)){
                settingsSpeakerGlyph(cx,cy,Math.min(31,size*.68f));
            }else if("cloud-backup".equals(glyph)){
                settingsBackupGlyph(cx,cy,Math.min(31,size*.68f));
            }else if("shield".equals(glyph)){
                settingsShieldGlyph(cx,cy,Math.min(32,size*.70f));
            }else{
                icon(glyph,cx,cy,Math.min(29,size*.63f),WHITE);
            }
        }
        void settingsSpeakerGlyph(float cx,float cy,float size){
            c.save();c.translate(cx-size/2f,cy-size/2f);
            c.scale(size/24f,size/24f);
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(WHITE);
            Path horn=new Path();
            horn.moveTo(2,9);horn.lineTo(7.5f,9);horn.lineTo(12.8f,4);
            horn.quadTo(15,2,15,5.6f);horn.lineTo(15,18.5f);
            horn.quadTo(15,22,12.8f,20);horn.lineTo(7.5f,15);
            horn.lineTo(2,15);horn.close();c.drawPath(horn,p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(2.7f);
            Path waves=new Path();
            waves.arcTo(new RectF(12.3f,6f,22f,18f),-58f,116f,false);
            c.drawPath(waves,p);
            p.setStrokeWidth(2.1f);
            c.drawArc(new RectF(9.8f,2.1f,26.7f,21.9f),
                -54f,108f,false,p);
            p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
            c.restore();
        }
        void settingsBackupGlyph(float cx,float cy,float size){
            c.save();c.translate(cx-size/2f,cy-size/2f);
            c.scale(size/24f,size/24f);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(3.2f);p.setColor(WHITE);
            Path cloud=new Path();
            cloud.moveTo(6.3f,18.2f);
            cloud.cubicTo(-.5f,18.2f,1.8f,9.5f,6.8f,10.7f);
            cloud.cubicTo(8.1f,3.1f,18.8f,4.3f,19f,11f);
            cloud.cubicTo(25f,11.5f,23.7f,19f,17.8f,19f);
            cloud.lineTo(6.3f,19f);c.drawPath(cloud,p);
            p.setStrokeWidth(2.4f);
            c.drawArc(new RectF(8,9.4f,17.4f,18.5f),210,200,false,p);
            Path arrow=new Path();
            arrow.moveTo(15.4f,11.2f);arrow.lineTo(17.4f,12.1f);
            arrow.lineTo(16.1f,14.2f);c.drawPath(arrow,p);
            p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
            c.restore();
        }
        void settingsShieldGlyph(float cx,float cy,float size){
            c.save();c.translate(cx-size/2f,cy-size/2f);
            c.scale(size/24f,size/24f);
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(WHITE);
            Path shield=new Path();
            shield.moveTo(12,2);shield.cubicTo(16,5,19,5,22,6.1f);
            shield.lineTo(21.5f,13.2f);
            shield.cubicTo(21,18.8f,16.4f,21.6f,12,23);
            shield.cubicTo(7.6f,21.6f,3,18.8f,2.5f,13.2f);
            shield.lineTo(2,6.1f);shield.cubicTo(5,5,8,5,12,2);
            shield.close();c.drawPath(shield,p);
            p.setColor(0xfffa6153);
            c.drawRoundRect(10.3f,8.2f,13.7f,16.6f,1.7f,1.7f,p);
            c.restore();
        }
        void settingsHeaderAccent(){
            // The source has two broad coral rays per side, not
            // three thin marks. Keep them outside the title glyphs.
            p.clearShadowLayer();p.setShader(null);p.setColor(CORAL);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8f);
            p.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(80,32,90,45,p);
            // Keep the coral accent outside the new Back touch surface.
            c.drawLine(81,58,94,58,p);
            c.drawLine(320,32,310,45,p);
            c.drawLine(331,58,318,58,p);
            p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
        }
        void settingsToggle(float x,float y,boolean enabled){
            int from=enabled?0xff2ddcca:0xffdce1e8;
            int to=enabled?0xff17c4b0:0xffbac4d1;
            p.setShader(null);p.setColor(WHITE);
            gradient(x,y,55,32,17,from,to);
            shape(x+(enabled?25:3)+.3f,y+4,25,25,13,
                theme==1?0x34000000:0x1394684a);
            p.setShader(null);p.setColor(WHITE);
            gradient(x+(enabled?24:2),y+2,27,27,14,WHITE,0xfff9f9f8);
        }
        void settings(){
            boolean compact=referencePortraitLayout;
            // Match all other detail screens: visible back arrow with an
            // accessible 54x54 logical touch target, returning to Home.
            back();
            // Source PNG spacing: four close rows, a slightly larger break,
            // local backup, privacy and app info, and the footer byline.
            int[] rowY=compact?
                new int[]{97,161,223,286,360,433,498}:
                new int[]{148,229,310,391,478,559,640};
            txt("Postavke",200,compact?75:108,compact?45:47,ink,true);
            if(compact)settingsHeaderAccent();
            else {
                rays(74,97,29,CORAL);
                rays(331,97,29,CORAL);
            }
            settingsSwitch("Zvuk","sound",CORAL,rowY[0],sound,()->{
                sound=!sound;persist();invalidate();
            });
            settingsSwitch("Vibracija","phone-vibrate",TEAL,rowY[1],vibration,()->{
                vibration=!vibration;persist();invalidate();
            });
            settingsAction("Jezik","Hrvatski","globe",0xff378ef6,rowY[2],
                ()->navigate("language"));
            settingsSwitch("Obavijesti","bell",0xffffb630,rowY[3],
                notifications,()->toggleNotifications());
            // No cloud service is connected: show honest local backup navigation,
            // not the fictional enabled sync switch in the supplied illustration.
            settingsAction("Sinkronizacija napretka","Lokalna kopija",
                "cloud-backup",0xffa55ee5,rowY[4],()->navigate("backup"));
            settingsAction("Pravila privatnosti","›","shield",0xffff7969,
                rowY[5],()->navigate("privacy"));
            settingsAction("O aplikaciji","›","info",0xff378ef6,
                rowY[6],()->navigate("about"));
            float footerY=compact?566:729;
            settingsPanel(18,footerY,364,compact?132:127,25,
                theme==1?card:0xfffff3e6);
            if(compact){
                // Owner's footer wordmark is taller and slightly wider than
                // the current native font glyphs. Scale only the settings
                // wordmark, keeping the real version and byline legible.
                c.save();
                c.scale(1.05f,1.32f,200,630);
                brand(639,54,false);
                c.restore();
            }else brand(795,53,false);
            fitted("v"+appVersion(),200,compact?668:829,15,320,muted,false);
            fitted("by Brendigo",200,compact?689:849,15,300,muted,true);
        }
        void settingsSwitch(String caption,String glyph,int accent,int y,boolean on,
                            Runnable action){
            boolean compact=referencePortraitLayout;
            int height=compact?57:71;
            settingsPanel(18,y,364,height,20,card);
            float iconSize=compact?47:51;
            float iconX=compact?29:31;
            settingsIconTile(iconX,y+(height-iconSize)/2f,iconSize,accent,glyph);
            leftFitted(caption,compact?91:97,y+height*.62f,
                compact?18.8f:21,compact?210:202,ink,true);
            float switchY=y+(height-32)/2f;
            settingsToggle(313,switchY,on);
            taps.add(new Tap(18,y,382,y+height,
                caption+(on?" uključeno":" isključeno"),action));
        }
        void settingsAction(String caption,String value,String glyph,
                            int accent,int y,Runnable action){
            boolean compact=referencePortraitLayout;
            int height=compact?57:71;
            settingsPanel(18,y,364,height,20,card);
            float iconSize=compact?47:51;
            float iconX=compact?29:31;
            settingsIconTile(iconX,y+(height-iconSize)/2f,iconSize,accent,glyph);
            boolean hasValue=!"›".equals(value);
            if(hasValue&&caption.length()>16){
                leftFitted(caption,compact?91:97,y+(compact?24:31),
                    compact?17.6f:19,compact?218:212,ink,true);
                leftFitted(value,compact?91:97,y+(compact?45:55),
                    compact?12:13,compact?210:205,muted,false);
                icon("chevron-right",355,y+height/2f,19,muted);
            }else{
                leftFitted(caption,compact?91:97,y+(compact?36:43),
                    compact?18.8f:20,
                    "Jezik".equals(caption)?145:215,ink,true);
                if("Jezik".equals(caption)){
                    float pillY=y+(compact?9:14),pillH=compact?39:44;
                    gradient(245,pillY,124,pillH,19,
                        theme==1?0xff4b5971:0xfffffaf1,
                        theme==1?0xff394a63:0xfff7ecdd);
                    fitted(value,289,y+(compact?36:43),17,91,ink,true);
                    icon("chevron-down",351,y+height/2f,17,muted);
                }else if(hasValue){
                    fitted(value,319,y+(compact?35:44),14,90,muted,true);
                }else icon("chevron-right",355,y+height/2f,22,muted);
            }
            taps.add(new Tap(18,y,382,y+height,caption,action));
        }
        void helpPage(){
            title("Kako igrati");
            txt("Spajaj jednake pločice",200,170,24,ink,true);
            card(42,202,316,139,22,card);
            // Readable visual equation; neither operator overlaps its tiles.
            glossyTile(58,235,70,70,15,TILES[1]);
            glossyTile(156,235,70,70,15,TILES[1]);
            glossyTile(274,235,70,70,15,TILES[2]);
            txt("2",93,283,36,WHITE,true);
            txt("+",142,279,27,ink,true);
            txt("2",191,283,36,WHITE,true);
            txt("→",250,280,29,CORAL,true);
            txt("4",309,283,36,WHITE,true);
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
                // Pointer area follows the enlarged last row of 4×4 tiles.
                float hitHeight=compactBoardLayout&&game!=null&&game.size==4?
                    402:gridSize;
                pointerBoard=x>=gridX-3 && x<=gridX+gridSize &&
                    y>=gridY&&y<=gridY+hitHeight && "game".equals(page);
                pressedTap=null;
                if(!pointerBoard){
                    for(Tap tap:new ArrayList<>(taps)){
                        if(x>=tap.left&&x<=tap.right&&y>=tap.top&&y<=tap.bottom){
                            pressedTap=tap;break;
                        }
                    }
                }
                invalidate();return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_MOVE &&
                maxScroll>0&&!pointerBoard){
                if(Math.abs(event.getY()-initialFingerY)>13f*getResources()
                    .getDisplayMetrics().density)draggingScroll=true;
                if(draggingScroll){
                    scrollOffset=Math.max(0,Math.min(maxScroll,
                        scrollOffset+(lastFingerY-event.getY())/Math.max(.1f,scale)));
                    pressedTap=null;invalidate();
                }
                if(pressedTap!=null&&
                    (x<pressedTap.left||x>pressedTap.right||
                     y<pressedTap.top||y>pressedTap.bottom)){
                    pressedTap=null;invalidate();
                }
                lastFingerY=event.getY();
                return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_CANCEL){
                pointerBoard=false;draggingScroll=false;pressedTap=null;
                invalidate();return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_UP){
                Tap target=pressedTap;
                pressedTap=null;invalidate();
                if(draggingScroll){draggingScroll=false;return true;}
                float dx=x-touchX,dy=y-touchY;
                if(pointerBoard&&Math.max(Math.abs(dx),Math.abs(dy))>=25) {
                    move(Math.abs(dx)>Math.abs(dy)?
                        (dx<0?TileGame.LEFT:TileGame.RIGHT):
                        (dy<0?TileGame.UP:TileGame.DOWN));
                    pointerBoard=false;return true;
                }
                pointerBoard=false;
                // A release never activates a different or unpressed button.
                if(Math.max(Math.abs(dx),Math.abs(dy))>18)return true;
                if(target!=null&&x>=target.left&&x<=target.right&&
                    y>=target.top&&y<=target.bottom){
                    performClick();target.action.run();
                }
                return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
