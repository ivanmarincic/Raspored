package com.idiotnation.raspored.Helpers;


public abstract class BackgroundTask<Type> {

    public onFinishListener listener;
    Thread thread;

    protected abstract void onCreate();

    protected abstract Type onExecute();

    public void onFinish(onFinishListener onFinishListener) {
        this.listener = onFinishListener;
    }

    public void run() {
        onCreate();
        thread = new Thread() {
            public void run() {
                Type t = onExecute();
                if (t != null && t.getClass() != Void.class) {
                    listener.onFinish(t);
                }
            }
        };
        thread.start();
    }

    public void kill() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            thread.stop();
            e.printStackTrace();
        }
    }

    public interface onFinishListener {
        <Type> void onFinish(Type t);
    }


}
