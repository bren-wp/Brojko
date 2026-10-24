package com.brendigo.brojko;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
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
        if("game".equals(target)&&game==null){newGame("classic");return;}
        page=target;updateBars();screen.invalidate();
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
        if(!result.changed)return;
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
        if(result.first2048)page="win";
        else if(result.lost){archive();page="over";}
        persist();animate();
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
    @Override public void onBackPressed(){
        if(page.equals("game"))navigate("pause");
        else if(page.equals("pause"))navigate("game");
        else if(page.equals("privacy")||page.equals("about")||page.equals("language"))navigate("settings");
        else if(page.equals("confirm"))navigate(game==null?"home":"game");
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
            float left,top,right,bottom;Runnable action;
            Tap(float l,float t,float r,float b,Runnable a) {
                left=l;top=t;right=r;bottom=b;action=a;
            }
        }
        BoardScreen(Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
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
            taps.add(new Tap(x,y,x+w,y+h,action));
        }
        void title(String name) {
            back();
            fitted(name,225,95,34,300,ink,true);
            txt("by Brendigo",259,121,14,muted,true);
        }
        void back(){button("","back",22,49,52,52,card,false,()->navigate("home"));}
        void brand(float y,float size) {
            fitted("Brojko",200,y,size,350,ink,true);
            float tw=size*.20f;
            card(174,y-size*.79f,tw+4,tw+4,6,CORAL);
            txt("2",176+tw/2,y-size*.26f,size*.22f,WHITE,true);
            txt("by Brendigo",269,y+23,15,muted,true);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);c=canvas;palette();
            scale=Math.min(getWidth()/400f,getHeight()/880f);
            offX=(getWidth()-400*scale)/2f;offY=(getHeight()-880*scale)/2f;
            c.drawColor(bg);c.save();c.translate(offX,offY);c.scale(scale,scale);
            taps.clear();
            switch(page) {
                case "home":home();break;
                case "game":gameScreen();break;
                case "pause":gameScreen();taps.clear();pauseOverlay();break;
                case "win":gameScreen();taps.clear();resultOverlay(true);break;
                case "over":gameScreen();taps.clear();resultOverlay(false);break;
                case "confirm":gameScreen();taps.clear();confirmOverlay();break;
                case "daily":daily();break;
                case "missions":missions();break;
                case "stats":statistics();break;
                case "themes":themes();break;
                case "settings":settings();break;
                case "language":languagePage();break;
                case "privacy":textPage("Privatnost",new String[]{"Brojko čuva rezultate i postavke",
                    "samo na vašem uređaju.","Igra radi bez interneta i računa.",
                    "Ne prikupljamo osobne podatke,","ne prikazujemo oglase i ne šaljemo",
                    "analitiku na poslužitelje."});break;
                case "about":textPage("O igri",new String[]{"Brojko 1.1.0 — by Brendigo",
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
            // Rounded tile confetti recalls the supplied visual identity.
            for(int i=0;i<4;i++){
                float x=30+i*91;
                card(x,38,70,69,16,TILES[i+1]);
                txt(String.valueOf(2<<i),x+35,82,31,WHITE,true);
            }
            brand(182,66);
            fitted("Spajaj brojeve. Ruši rekorde.",200,238,22,370,
                theme==1?TEAL:CORAL,true);
            card(22,264,171,88,22,card);card(207,264,171,88,22,card);
            icon("crown",54,300,26,0xffffae16);txt("NAJBOLJI",116,303,15,muted,true);
            txt(fmt(best),111,335,29,ink,true);
            icon("stats",238,300,26,0xff3d98f0);txt("PARTIJA",310,303,15,muted,true);
            txt(fmt(plays),297,335,29,ink,true);
            button(game==null?"Nova igra":"Nastavi igru","play",27,382,346,79,CORAL,true,
                ()->{if(game==null)requestNew("classic");else navigate(game.gameOver?"over":"game");});
            modeCard("Klasično","4×4",23,481,170,110,0xffffe3d5,()->requestNew("classic"));
            modeCard("Opušteno","5×5",207,481,170,110,0xffd2f6ef,()->requestNew("relaxed"));
            button("Dnevni izazov","calendar",23,611,354,64,0xfffff0cb,false,()->navigate("daily"));
            button("Misije","target",23,698,170,60,card,false,()->navigate("missions"));
            button("Statistika","stats",207,698,170,60,card,false,()->navigate("stats"));
            button("Teme","palette",23,780,170,54,card,false,()->navigate("themes"));
            button("Postavke","settings",207,780,170,54,card,false,()->navigate("settings"));
        }
        void modeCard(String name,String sub,float x,float y,float w,float h,int fill,Runnable tap){
            card(x,y,w,h,23,theme==1?card:fill);
            fitted(name,x+w/2,y+48,22,w-20,ink,true);
            txt(sub,x+w/2,y+82,23,muted,true);
            taps.add(new Tap(x,y,x+w,y+h,tap));
        }
        void scoreCard(String caption,String value,float x,float y,float width){
            card(x,y,width,72,17,card);
            txt(caption,x+width/2,y+25,15,muted,true);
            fitted(value,x+width/2,y+59,28,width-16,ink,true);
        }
        float gridX=24,gridY=258,gridSize=352;
        void gameScreen(){
            brand(91,49);
            txt(labelMode(game.mode),123,161,22,ink,true);
            scoreCard("NAJBOLJI",fmt(best),24,176,168);
            scoreCard("REZULTAT",fmt(game.score),208,176,168);
            button("","pause",328,54,48,49,card,false,()->navigate("pause"));
            drawGrid();
            if(game.canUndo())button("Poništi","undo",24,656,168,72,card,false,()->undo());
            else {card(24,656,168,72,18,theme==1?0xff29364a:0xffeae5de);
                icon("undo",49,692,22,muted);txt("Poništi",112,698,19,muted,true);}
            button("Novo","undo",208,656,168,72,card,false,()->requestNew(game.mode));
            button("Izbornik","home",24,747,168,65,card,false,()->navigate("home"));
            button("Podijeli","share",208,747,168,65,CORAL,true,()->share());
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
            dim();overlayBox(198,494);
            icon(winning?"crown":"target",200,248,47,winning?0xffffb52a:CORAL);
            txt(winning?"Bravo!":"Kraj igre",200,320,40,ink,true);
            txt(winning?"Dosegnuta pločica 2048!":"Nema više mogućih poteza.",
                200,363,18,muted,true);
            card(65,388,270,93,20,card);
            txt("TVOJ REZULTAT",200,420,15,muted,true);
            txt(fmt(game.score),200,461,35,ink,true);
            button(winning?"Nastavi igrati":"Nova igra",
                winning?"play":"undo",54,508,292,65,CORAL,true,
                ()->{if(winning){game.winAcknowledged=true;persist();navigate("game");}
                else requestNew(game.mode);});
            button("Podijeli rezultat","share",54,594,292,64,card,false,()->share());
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
            txt(compactDate(),200,176,21,CORAL,true);
            card(31,202,338,71,20,card);
            txt("Isti početak za sve pokušaje danas",200,233,17,ink,true);
            txt("Niz dovršenih dana: "+streak,200,258,17,muted,false);
            taskRow("Dosegni pločicu 256","Spoji pločice do 256.",304,
                game!=null&&"daily".equals(game.mode)&&today().equals(game.day)?
                (game.peak>=256||today().equals(lastDaily)):today().equals(lastDaily));
            taskRow("Osvoji 3000 bodova","Skupi najmanje 3000 bodova.",416,
                game!=null&&"daily".equals(game.mode)&&today().equals(game.day)?
                (game.score>=3000||today().equals(lastDaily)):today().equals(lastDaily));
            taskRow("U najviše 80 poteza","Ispuni oba cilja do 80. poteza.",528,
                today().equals(lastDaily));
            card(29,641,342,70,20,theme==1?0xff284b48:0xffe5f9ee);
            txt(today().equals(lastDaily)?"Današnji izazov dovršen!":
                "Ispuni sve ciljeve za niz pobjeda",200,685,18,ink,true);
            button("Započni izazov","play",33,757,334,67,CORAL,true,
                ()->requestNew("daily"));
        }
        void taskRow(String text,String caption,int y,boolean checked){
            card(30,y,340,99,19,card);
            shape(45,y+21,57,57,14,checked?TEAL:0xffffe1ce);
            icon(checked?"check":"target",73,y+49,30,checked?WHITE:CORAL);
            fitted(text,242,y+40,20,246,ink,true);
            fitted(caption,242,y+70,14,246,muted,false);
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
            statCell("Najbolji rezultat",fmt(best),24,157,169,109,CORAL);
            statCell("Ukupno partija",fmt(plays),207,157,169,109,TEAL);
            statCell("Najveća pločica",fmt(recordTile),24,279,169,109,0xffe79b35);
            statCell("Ukupno bodova",fmt(total),207,279,169,109,0xff5e9fee);
            statCell("Dosegnuto 2048",fmt(wins),24,401,169,109,0xffba68dd);
            statCell("Ukupno spajanja",fmt(totalMerges),207,401,169,109,0xff35ba8f);
            txt("POVIJEST PARTIJA",116,560,17,ink,true);
            JSONArray array=history();
            int count=Math.min(3,array.length());
            if(count==0)txt("Odigrane partije prikazat će se ovdje.",200,620,16,muted,false);
            for(int i=0;i<count;i++){
                JSONObject r=array.optJSONObject(i);if(r==null)continue;
                int y=583+i*78;
                card(27,y,346,67,15,card);
                txt(r.optString("day",""),105,y+26,14,muted,false);
                txt(labelMode(r.optString("mode")),107,y+50,13,muted,true);
                txt(fmt(r.optInt("score")),299,y+40,22,ink,true);
            }
        }
        void statCell(String title,String value,int x,int y,int width,int h,int accent){
            card(x,y,width,h,20,card);
            shape(x+14,y+15,7,7,3,accent);
            fitted(title,x+width/2,y+42,15,width-20,muted,true);
            fitted(value,x+width/2,y+85,31,width-20,accent,true);
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
                taps.add(new Tap(x,217,x+116,375,()->{
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
            button("Pravila privatnosti","shield",25,562,350,69,card,false,()->navigate("privacy"));
            button("O aplikaciji","settings",25,651,350,69,card,false,()->navigate("about"));
            txt("Brojko v1.1.0  •  by Brendigo",200,812,17,muted,true);
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
            taps.add(new Tap(25,y,375,y+85,click));
        }
        void textPage(String heading,String[] lines){
            title(heading);
            card(24,170,352,Math.max(290,lines.length*50),25,card);
            for(int i=0;i<lines.length;i++)
                fitted(lines[i],200,219+i*48,17,330,ink,false);
            button("Nazad","back",25,722,350,72,CORAL,true,()->navigate("settings"));
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
                for(Tap t:new ArrayList<>(taps))if(x>=t.left&&x<=t.right&&
                    y>=t.top&&y<=t.bottom){t.action.run();return true;}
                performClick();return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
