package com.brendigo.brojko;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
 Board game; SharedPreferences prefs; String page="menu", mode="Klasično"; int theme=0, best=0, total=0, games=0, wins=0, bestTile=0, undoCount=0; boolean sound=true, vibrate=true, wonShown=false;
 final ArrayList<Integer> history=new ArrayList<>(); GameView view;
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("brojko",MODE_PRIVATE);best=prefs.getInt("best",0); total=prefs.getInt("total",0); games=prefs.getInt("games",0);wins=prefs.getInt("wins",0);bestTile=prefs.getInt("tile",0);theme=prefs.getInt("theme",0);sound=prefs.getBoolean("sound",true);vibrate=prefs.getBoolean("vibrate",true); restore();view=new GameView(this);setContentView(view);}
 void restore(){try{String saved=prefs.getString("board","");if(!saved.isEmpty()){JSONObject j=new JSONObject(saved);game=new Board(j.getInt("n"),new Random());JSONArray a=j.getJSONArray("cells");for(int i=0;i<game.cells.length;i++)game.cells[i]=a.getInt(i);game.score=j.getInt("score");mode=j.optString("mode","Klasično");}}catch(Exception ignored){}}
 void save(){SharedPreferences.Editor e=prefs.edit().putInt("best",best).putInt("total",total).putInt("games",games).putInt("wins",wins).putInt("tile",bestTile).putInt("theme",theme).putBoolean("sound",sound).putBoolean("vibrate",vibrate);if(game!=null){try{JSONObject j=new JSONObject();j.put("n",game.n);j.put("score",game.score);j.put("mode",mode);JSONArray a=new JSONArray();for(int v:game.cells)a.put(v);j.put("cells",a);e.putString("board",j.toString());}catch(Exception ignored){}}e.apply();}
 void start(String which){mode=which; Random r=which.equals("Dnevni izazov")?new Random(today().hashCode()):new Random();game=new Board(which.equals("Opušteno")?5:4,r);game.spawn();game.spawn();games++;wonShown=false;undoCount=0;page="game";save();view.invalidate();}
 String today(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());}
 void move(int dir){if(game==null)return;int[] before=game.cells.clone();int score=game.score;boolean changed=game.slide(dir);if(changed){game.undo=before;game.undoScore=score;game.spawn();int add=game.score-score;total+=add;best=Math.max(best,game.score);for(int v:game.cells)bestTile=Math.max(v,bestTile); if(game.max()>=2048&&!wonShown){wonShown=true;wins++;page="win";}else if(!game.canMove())page="over";save();view.invalidate();}}
 void undo(){if(game!=null&&game.undo!=null){game.cells=game.undo;game.score=game.undoScore;game.undo=null;undoCount++;save();view.invalidate();}}
 @Override public void onBackPressed(){if(!page.equals("menu")){page="menu";save();view.invalidate();}else super.onBackPressed();}
 static class Board{
  final int n; int[] cells;int score=0,undoScore;int[] undo;Random random;
  Board(int n,Random r){this.n=n;cells=new int[n*n];random=r;}
  void spawn(){ArrayList<Integer> empty=new ArrayList<>();for(int i=0;i<cells.length;i++)if(cells[i]==0)empty.add(i);if(!empty.isEmpty())cells[empty.get(random.nextInt(empty.size()))]=random.nextInt(10)==0?4:2;}
  int max(){int m=0;for(int x:cells)m=Math.max(m,x);return m;}
  boolean canMove(){for(int y=0;y<n;y++)for(int x=0;x<n;x++){int v=cells[y*n+x];if(v==0||x+1<n&&v==cells[y*n+x+1]||y+1<n&&v==cells[(y+1)*n+x])return true;}return false;}
  boolean slide(int dir){boolean changed=false;for(int line=0;line<n;line++){int[] pos=new int[n], vals=new int[n];int k=0;for(int step=0;step<n;step++){int x=dir==0?step:dir==1?n-1-step:line;int y=dir==2?step:dir==3?n-1-step:line;pos[step]=y*n+x;if(cells[pos[step]]!=0)vals[k++]=cells[pos[step]];}int[] out=new int[n];int j=0;for(int i=0;i<k;i++){if(i+1<k&&vals[i]==vals[i+1]){out[j]=vals[i]*2;score+=out[j];i++;}else out[j]=vals[i];j++;}for(int i=0;i<n;i++){if(cells[pos[i]]!=out[i])changed=true;cells[pos[i]]=out[i];}}return changed;}
 }
 class GameView extends View {
  Canvas c; Paint p=new Paint(3); float w,h,sx,sy,downX,downY; ArrayList<Hit> hits=new ArrayList<>();int ink=Color.rgb(25,42,74),coral=Color.rgb(255,111,80),cream=Color.rgb(255,250,242); final int[] colors={0xffeee2d4,0xffff815f,0xff2ec4b6,0xff4599ef,0xffffbd4e,0xffff8055,0xffff6e99,0xffb052e9,0xff8659e9,0xff4aa8f3,0xff40cba7};
  GameView(Context cx){super(cx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  class Hit{float l,t,r,b;Runnable action;Hit(float a,float d,float e,float f,Runnable rr){l=a;t=d;r=e;b=f;action=rr;}}
  void rect(float x,float y,float ww,float hh,float rad,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,y,x+ww,y+hh,rad,rad,p);}
  void text(String a,float x,float y,float size,int color,boolean bold){p.setTypeface(bold?Typeface.create("sans-serif-rounded",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));p.setColor(color);p.setTextSize(size);p.setTextAlign(Paint.Align.CENTER);c.drawText(a,x,y,p);}
  void button(String name,float x,float y,float ww,float hh,int color,Runnable action){rect(x,y,ww,hh,20,color);text(name,x+ww/2,y+hh*.64f,19,color==coral?Color.WHITE:ink,true);hits.add(new Hit(x,y,x+ww,y+hh,action));}
  @Override protected void onDraw(Canvas canvas){c=canvas; sx=getWidth()/400f;sy=getHeight()/820f;c.save();c.scale(sx,sy);c.drawColor(theme==1?0xff18263e:cream);hits.clear();if(page.equals("menu"))menu();else if(page.equals("game"))gameScreen();else if(page.equals("win")||page.equals("over"))result();else if(page.equals("stats"))stats();else if(page.equals("daily"))daily();else if(page.equals("missions"))missions();else if(page.equals("themes"))themes();else settings();c.restore();}
  void header(String name){text(name,200,100,40,ink,true);button("‹",16,35,52,52,0xfff1e5d8,()->navigate("menu"));}
  void navigate(String next){page=next;invalidate();}
  void menu(){text("Brojko",200,133,60,ink,true);text("by Brendigo",260,162,15,0xff738094,true);text("Spajaj brojeve. Ruši rekorde.",200,206,19,coral,true);rect(25,231,168,90,20,0xfff9ecdf);rect(207,231,168,90,20,0xffe1f8f4);text("NAJBOLJI",109,265,16,ink,true);text(String.valueOf(best),109,301,26,ink,true);text("PARTIJA",290,265,16,ink,true);text(String.valueOf(games),290,301,26,ink,true);button(game==null?"▶  Nova igra":"▶  Nastavi",32,346,336,76,coral,()->{if(game==null)start("Klasično");else navigate("game");});button("Klasično 4×4",25,446,168,95,0xffffe5d7,()->start("Klasično"));button("Opušteno 5×5",207,446,168,95,0xffd9f7f1,()->start("Opušteno"));button("Dnevni izazov",25,551,350,64,0xfffff0cb,()->navigate("daily"));button("Misije",25,641,168,65,0xfff5e7f9,()->navigate("missions"));button("Statistika",207,641,168,65,0xffdff0ff,()->navigate("stats"));button("Teme",25,725,168,60,0xffe4f8e9,()->navigate("themes"));button("Postavke",207,725,168,60,0xffefe7de,()->navigate("settings"));}
  int colorFor(int v){if(v==0)return colors[0];int index=31-Integer.numberOfLeadingZeros(v);return colors[1+Math.min(index-1,colors.length-2)];}
  void board(float x,float y,float size){int n=game.n;rect(x-9,y-9,size+18,size+18,19,0xffe9dbc8);float gap=8,tile=(size-gap*(n-1))/n;for(int yy=0;yy<n;yy++)for(int xx=0;xx<n;xx++){int v=game.cells[yy*n+xx];float tx=x+xx*(tile+gap),ty=y+yy*(tile+gap);rect(tx,ty,tile,tile,12,colorFor(v));if(v>0)text(""+v,tx+tile/2,ty+tile*.62f,v>=10000?19:v>=1000?23:30,Color.WHITE,true);}}
  void gameScreen(){text(mode,142,95,33,ink,true);text("Najbolji: "+best,265,150,18,ink,true);text("Rezultat: "+game.score,116,150,18,ink,true);board(34,212,332);button("Novo",26,604,105,70,0xfff1e5d8,()->start(mode));button("Poništi",148,604,105,70,0xfff1e5d8,()->undo());button("Izbornik",270,604,105,70,coral,()->navigate("menu"));text("Povuci u bilo kojem smjeru",200,718,17,0xff738094,false);}
  void result(){gameScreen();rect(20,240,360,330,28,0xfffffaf2);text(page.equals("win")?"Bravo! 2048!":"Nema više poteza",200,316,30,coral,true);text("Tvoj rezultat: "+game.score,200,374,25,ink,true);button("Nastavi igrati",52,420,296,57,coral,()->navigate("game"));button("Nova igra",52,491,296,55,0xfff1e5d8,()->start(mode));}
  void stats(){header("Statistika");label("Najbolji rezultat",best,175);label("Ukupno partija",games,270);label("Ukupno bodova",total,365);label("Najveća pločica",bestTile,460);label("Dosegnuto 2048",wins,555);}
  void label(String s,int v,int y){rect(30,y-45,340,80,18,0xfff5eadc);text(s,200,y-17,17,0xff708098,false);text(String.valueOf(v),200,y+17,29,ink,true);}
  void daily(){header("Dnevni izazov");text(today(),200,163,21,coral,true);text("Isti izazov za isti datum",200,222,18,ink,false);rect(30,260,340,245,24,0xfffff0db);text("Dnevni ciljevi",200,307,26,ink,true);text("• Dosegni pločicu 256",200,364,19,ink,false);text("• Osvoji 3000 bodova",200,413,19,ink,false);text("• Igraj do 80 poteza",200,462,19,ink,false);button("Započni izazov",32,552,336,78,coral,()->start("Dnevni izazov"));}
  void missions(){header("Misije i postignuća");mission("Prva partija",games>0,205);mission("1000 ukupnih bodova",total>=1000,315);mission("Pločica 128",bestTile>=128,425);mission("Pločica 512",bestTile>=512,535);mission("Pločica 2048",bestTile>=2048,645);}
  void mission(String s,boolean yes,int y){rect(26,y-42,348,85,17,yes?0xffe0f9ef:0xfff4e7da);text(s,187,y+7,21,ink,true);text(yes?"✓":"○",345,y+7,27,yes?0xff09a985:coral,true);}
  void themes(){header("Teme i izgled");button("Svijetla "+(theme==0?"✓":""),32,178,336,90,0xffffdbc7,()->{theme=0;save();invalidate();});button("Tamna "+(theme==1?"✓":""),32,296,336,90,0xffb5c6dd,()->{theme=1;save();invalidate();});button("Živopisna "+(theme==2?"✓":""),32,414,336,90,0xffdef3e7,()->{theme=2;save();invalidate();});}
  void settings(){header("Postavke");button("Zvuk: "+(sound?"Uključeno":"Isključeno"),25,190,350,80,0xfff6e9da,()->{sound=!sound;save();invalidate();});button("Vibracija: "+(vibrate?"Da":"Ne"),25,294,350,80,0xffdff5ed,()->{vibrate=!vibrate;save();invalidate();});button("Teme i izgled",25,398,350,80,0xffe4edff,()->navigate("themes"));text("Brojko v1.0.0",200,612,22,ink,true);text("by Brendigo",200,648,18,0xff738094,false);text("Napredak se čuva na ovom uređaju.",200,702,15,0xff738094,false);}
  @Override public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX()/sx,y=e.getY()/sy;if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;return true;}if(e.getAction()==MotionEvent.ACTION_UP){float dx=x-downX,dy=y-downY;if(page.equals("game")&&Math.max(Math.abs(dx),Math.abs(dy))>34){move(Math.abs(dx)>Math.abs(dy)?(dx<0?0:1):(dy<0?2:3));return true;}for(Hit a:new ArrayList<>(hits))if(x>=a.l&&x<=a.r&&y>=a.t&&y<=a.b){a.action.run();invalidate();return true;}return true;}return true;}
 }
}
