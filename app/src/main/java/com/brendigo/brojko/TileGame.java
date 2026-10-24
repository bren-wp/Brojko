package com.brendigo.brojko;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/** Pure, deterministic 2048 engine. No Android UI or network dependencies. */
public final class TileGame {
    public static final int LEFT=0, RIGHT=1, UP=2, DOWN=3;
    public final int size;
    public final String mode;
    public final String day;
    public int[] cells;
    public int score, moves, merges, peak;
    public boolean reached2048, gameOver, archived, winAcknowledged;
    private long randomState;
    private Snapshot undo;

    private static final class Snapshot {
        final int[] cells;
        final int score, moves, merges, peak;
        final long randomState;
        final boolean reached2048, gameOver, winAcknowledged;
        Snapshot(TileGame g) {
            cells=g.cells.clone(); score=g.score; moves=g.moves; merges=g.merges;peak=g.peak;
            randomState=g.randomState; reached2048=g.reached2048;gameOver=g.gameOver;
            winAcknowledged=g.winAcknowledged;
        }
    }
    public static final class Result {
        public final boolean changed, first2048, lost;
        public final int points, merges;
        Result(boolean changed, boolean first2048, boolean lost, int points, int merges) {
            this.changed=changed;this.first2048=first2048;this.lost=lost;
            this.points=points;this.merges=merges;
        }
    }
    public TileGame(String mode, String day, long seed) {
        this.mode=mode;this.day=day==null?"":day;
        this.size="relaxed".equals(mode)?5:4;
        this.cells=new int[size*size];
        this.randomState=seed==0?0x9E3779B97F4A7C15L:seed;
        spawn();spawn();peak=maxTile();
    }
    private TileGame(int size, String mode, String day) {
        this.size=size;this.mode=mode;this.day=day;cells=new int[size*size];
    }
    private long nextRandom() {
        long x=randomState;
        x^=x>>>12; x^=x<<25; x^=x>>>27;
        randomState=x;
        return x*2685821657736338717L;
    }
    private int nextInt(int max) { return (int)Long.remainderUnsigned(nextRandom(), max); }
    private void spawn() {
        ArrayList<Integer> empty=new ArrayList<>();
        for(int i=0;i<cells.length;i++)if(cells[i]==0)empty.add(i);
        if(!empty.isEmpty())cells[empty.get(nextInt(empty.size()))]=nextInt(10)==0?4:2;
    }
    public int maxTile() {int max=0;for(int x:cells)max=Math.max(max,x);return max;}
    public boolean canMove() {
        for(int y=0;y<size;y++)for(int x=0;x<size;x++) {
            int n=cells[y*size+x];
            if(n==0 || (x<size-1 && n==cells[y*size+x+1])
                 || (y<size-1 && n==cells[(y+1)*size+x]))return true;
        }
        return false;
    }
    public Result move(int direction) {
        if(direction<0||direction>3||gameOver)return new Result(false,false,gameOver,0,0);
        int[] original=cells.clone();
        Snapshot previous=new Snapshot(this);
        int oldScore=score, oldMerges=merges;
        for(int line=0;line<size;line++) {
            int[] indexes=new int[size],values=new int[size];
            int occupied=0;
            for(int step=0;step<size;step++) {
                int x=(direction==LEFT)?step:(direction==RIGHT)?size-1-step:line;
                int y=(direction==UP)?step:(direction==DOWN)?size-1-step:line;
                int index=y*size+x;
                indexes[step]=index;
                if(cells[index]!=0)values[occupied++]=cells[index];
            }
            int[] output=new int[size];
            int next=0;
            for(int i=0;i<occupied;i++) {
                if(i+1<occupied && values[i]==values[i+1]) {
                    output[next]=values[i]*2;
                    score+=output[next];merges++;i++;
                }else output[next]=values[i];
                next++;
            }
            for(int i=0;i<size;i++)cells[indexes[i]]=output[i];
        }
        if(Arrays.equals(original,cells)) {
            // A fully blocked board must finish even if the final attempted swipe
            // cannot move any tile (e.g. after restoring a stale session).
            gameOver=!canMove();
            return new Result(false,false,gameOver,0,0);
        }
        undo=previous;
        moves++;
        spawn();
        peak=Math.max(peak,maxTile());
        boolean first2048=!reached2048 && peak>=2048;
        if(first2048)reached2048=true;
        gameOver=!canMove();
        return new Result(true,first2048,gameOver,score-oldScore,merges-oldMerges);
    }
    public boolean canUndo() {return undo!=null;}
    public int undo() {
        if(undo==null)return 0;
        int scoreChange=score-undo.score;
        cells=undo.cells.clone();score=undo.score;moves=undo.moves;
        merges=undo.merges;peak=undo.peak;randomState=undo.randomState;
        reached2048=undo.reached2048;gameOver=undo.gameOver;
        winAcknowledged=undo.winAcknowledged;
        undo=null;
        return scoreChange;
    }
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("size",size);obj.put("mode",mode);obj.put("day",day);
            obj.put("score",score);obj.put("moves",moves);obj.put("merges",merges);
            obj.put("peak",peak);obj.put("rng",Long.toString(randomState));
            obj.put("reached",reached2048);obj.put("gameOver",gameOver);
            obj.put("archived",archived);obj.put("ack",winAcknowledged);
            JSONArray arr=new JSONArray();for(int x:cells)arr.put(x);obj.put("cells",arr);
            if(undo!=null) {
                JSONObject u=new JSONObject();JSONArray a=new JSONArray();
                for(int x:undo.cells)a.put(x);
                u.put("cells",a);u.put("score",undo.score);u.put("moves",undo.moves);
                u.put("merges",undo.merges);u.put("peak",undo.peak);
                u.put("rng",Long.toString(undo.randomState));
                u.put("reached",undo.reached2048);u.put("over",undo.gameOver);
                u.put("ack",undo.winAcknowledged);obj.put("undo",u);
            }
        }catch(Exception ex){throw new IllegalStateException("Session JSON",ex);}
        return obj;
    }
    public static TileGame fromJson(JSONObject obj) {
        try {
            int n=obj.getInt("size");
            if(n!=4&&n!=5)throw new IllegalArgumentException("Invalid size");
            String mode=obj.getString("mode"),day=obj.optString("day","");
            TileGame game=new TileGame(n,mode,day);
            JSONArray a=obj.getJSONArray("cells");
            if(a.length()!=n*n)throw new IllegalArgumentException("Invalid board");
            for(int i=0;i<a.length();i++){
                int x=a.getInt(i);
                if(x!=0&&(x<2||(x&(x-1))!=0))throw new IllegalArgumentException("Invalid tile");
                game.cells[i]=x;
            }
            game.score=obj.getInt("score");game.moves=obj.optInt("moves");
            game.merges=obj.optInt("merges");game.peak=obj.optInt("peak",game.maxTile());
            game.randomState=Long.parseLong(obj.getString("rng"));
            if(game.randomState==0)throw new IllegalArgumentException("Invalid RNG");
            game.reached2048=obj.optBoolean("reached",false);
            game.gameOver=obj.optBoolean("gameOver",false);
            game.archived=obj.optBoolean("archived",false);
            game.winAcknowledged=obj.optBoolean("ack",false);
            JSONObject u=obj.optJSONObject("undo");
            if(u!=null) {
                TileGame older=new TileGame(n,mode,day);
                JSONArray b=u.getJSONArray("cells");
                if(b.length()==n*n){
                    for(int i=0;i<b.length();i++)older.cells[i]=b.getInt(i);
                    older.score=u.getInt("score");older.moves=u.getInt("moves");
                    older.merges=u.getInt("merges");older.peak=u.getInt("peak");
                    older.randomState=Long.parseLong(u.getString("rng"));
                    older.reached2048=u.optBoolean("reached");
                    older.gameOver=u.optBoolean("over");
                    older.winAcknowledged=u.optBoolean("ack");
                    game.undo=new Snapshot(older);
                }
            }
            return game;
        } catch(Exception ex){return null;}
    }
}
