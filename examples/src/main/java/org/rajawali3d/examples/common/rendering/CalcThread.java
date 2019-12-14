package org.rajawali3d.examples.common.rendering;

import android.os.HandlerThread;

public class CalcThread extends HandlerThread {

    public static final int MSG_GET_BITMAP = 1;

    protected LooperState looperState;

    public CalcThread(String name) {
        super(name);
    }

    public void setLooperState(LooperState looperState) {
        this.looperState = looperState;
    }

    @Override
    protected void onLooperPrepared() {
        if (looperState !=  null) {
            looperState.onPrepared();
        }

    }

    public interface LooperState {
        void onPrepared();
    }
}
