package com.skynet.adplayer.activities.mainactvity;

public abstract class BasicTask extends Thread {
    protected boolean running = false;

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public void stopAllAndQuit() {
        setRunning(false);
        this.interrupt();
        try {
            this.join();
        } catch (InterruptedException e) {
        }
    }

    public void startToRun() {
        setRunning(true);
        this.start();
    }
}