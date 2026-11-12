package com.brendigo.brojko;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * A process-scoped, single-writer archive barrier. No disk commit is made on
 * the Android main thread. The same transaction atomically updates "history"
 * and the archived flag of the previous persisted "session".
 *
 * During rotation the new Activity attaches to the pending operation; if the
 * process is killed, the atomic on-disk session remains recoverable.
 */
final class GameArchiveCoordinator {
    private static final ExecutorService IO=Executors.newSingleThreadExecutor(r->{
        Thread thread=new Thread(r,"brojko-archive");
        thread.setDaemon(false);
        return thread;
    });
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static Pending pending;

    static final class Pending {
        final String session;
        final String day;
        final String time;
        final String nextMode;
        final boolean finishSession;
        final int score;
        WeakReference<MainActivity> receiver;

        Pending(String session,String day,String time,String nextMode,
                boolean finishSession,int score,MainActivity activity){
            this.session=session;
            this.day=day;
            this.time=time;
            this.nextMode=nextMode;
            this.finishSession=finishSession;
            this.score=score;
            this.receiver=new WeakReference<>(activity);
        }
    }

    private GameArchiveCoordinator(){}

    /** Called on the main thread. Only one archive can be pending at a time. */
    static synchronized boolean start(MainActivity activity,SharedPreferences prefs,
            TileGame game,String day,String time,String nextMode,boolean finishSession){
        if(pending!=null||game==null||game.archived||game.moves<=0)return false;
        // Snapshot on the UI thread: workers must NEVER read a mutable board.
        Pending request=new Pending(game.toJson().toString(),day,time,
            nextMode,finishSession,game.score,activity);
        pending=request;
        try{
            IO.execute(()->{
                boolean successful=LocalHistoryRepository.archiveSnapshot(
                    prefs,request.session,request.day,request.time);
                MAIN.post(()->finish(request,successful));
            });
            return true;
        }catch(RejectedExecutionException rejected){
            pending=null;
            return false;
        }
    }

    /** Rebind the in-flight transaction after Android rotates the Activity. */
    static synchronized boolean attach(MainActivity activity){
        if(pending==null)return false;
        pending.receiver=new WeakReference<>(activity);
        return true;
    }

    static synchronized void detach(MainActivity activity){
        if(pending!=null && pending.receiver.get()==activity)
            pending.receiver.clear();
    }

    private static void finish(Pending completed,boolean success){
        MainActivity target;
        synchronized(GameArchiveCoordinator.class){
            if(pending!=completed)return;
            target=completed.receiver.get();
            pending=null;
        }
        if(target!=null&&!target.isDestroyed()&&!target.isFinishing())
            target.onArchiveTransactionFinished(completed,success);
    }
}
