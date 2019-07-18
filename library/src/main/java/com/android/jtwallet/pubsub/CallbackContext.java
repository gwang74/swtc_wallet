package com.android.jtwallet.pubsub;

public abstract class CallbackContext {
    public void execute(Runnable runnable) {
        runnable.run();
    }
    public boolean shouldExecute() {
        return true;
    }
    public boolean shouldRemove() {
        return false;
    }
}
