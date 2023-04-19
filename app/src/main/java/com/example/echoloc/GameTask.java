package com.example.echoloc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class GameTask {
    private RequestTask rt;
    private boolean canRetry;
    private final Context context;
    private final GameSettings gs;

    public GameTask(Context context, GameSettings gameSettings) {
        this.context = context;
        this.gs = gameSettings;
        this.rt = null;
        this.canRetry = true;

        setNewRequestTask();
    }

    @SuppressLint("StaticFieldLeak")
    private void setNewRequestTask(){
        rt = new RequestTask();
        rt.create(this.context, this.gs);
        rt.setOnFail(e -> {
            Log.e("echoloc", e.getMessage());
            boolean retry = true;
            switch (e.getExceptionType()){ // TODO Traitement des erreurs
                case GameException.OUT_OF_CITY_RANGE:
                    gs.setIntoMostPopulate((int)e.getData());
                    retry = true;
                    break;
                case GameException.URL_ERROR:
                    retry = false;
                    break;
            }
            onTaskFailed(e);
            if (retry && canRetry) {
                retry();
            } else {
                cancel();
            }
        });
        rt.setOnSuccessed(()->{
            this.canRetry = false;
            onTaskFinished();
        });
    }

    public void execute(){
        if(canRetry){
            try {
                rt.execute();
            } catch (Exception e){
                retry();
            }
        }
    }

    public void cancel(){
        rt.cancel(true);
        this.canRetry = false;
        this.onTaskCancelled();
    }

    public void retry(){
        if (canRetry) {
            setNewRequestTask();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (canRetry) {
                        rt.execute();
                    }
                }
            }, 3000);
            onTaskRetried();
        }
    }

    private GameExceptionRunnable onTaskFailedRunnable;
    public void setOnTaskFailed(GameExceptionRunnable r){
        onTaskFailedRunnable = r;
    }
    private void onTaskFailed(GameException e){
        if (onTaskFailedRunnable != null){
            onTaskFailedRunnable.fail(e);
        }
    }

    private Runnable onTaskFinishedRunnable;
    public void setOnTaskFinished(Runnable r){
        onTaskFinishedRunnable = r;
    }
    private void onTaskFinished(){
        if (onTaskFinishedRunnable != null){
            onTaskFinishedRunnable.run();
        }
    }

    private Runnable onTaskRetriedRunnable;
    public void setOnTaskRetried(Runnable r){
        onTaskRetriedRunnable = r;
    }
    private void onTaskRetried(){
        if (onTaskRetriedRunnable != null){
            onTaskRetriedRunnable.run();
        }
    }

    private Runnable onTaskCancelledRunnable;
    public void setOnTaskCancelled(Runnable r){
        onTaskCancelledRunnable = r;
    }
    private void onTaskCancelled(){
        if (onTaskCancelledRunnable != null){
            onTaskCancelledRunnable.run();
        }
    }
}
