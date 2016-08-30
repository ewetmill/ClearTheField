package com.wetsts.ctf;

import android.widget.Button;

/**
 * Created by ewetmill on 8/26/16.
 */
public class LongPressHandler implements Runnable {
    private Button button;
    public LongPressHandler(Button button) {
        this.button = button;
    }

    @Override
    public void run() {
        button.setBackgroundResource(R.drawable.check_icon_orig);
    }
}
