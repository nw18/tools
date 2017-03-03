package com.newind.android.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.newind.android.ApplicationMain;
import com.newind.android.R;

public class ActivityStart extends Activity {
    private static final long MIN_START_TIME = 2 * 1000L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);
        if (ApplicationMain.getServer() == null){
            startMainDelay();
        }else {
            startMain();
        }
    }

    private void startMainDelay() {
        ImageView iv_logo = (ImageView) findViewById(R.id.iv_logo);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.routing);
        animation.setRepeatCount(Animation.INFINITE);
        iv_logo.startAnimation(animation);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long beginTime = System.currentTimeMillis();
                ApplicationMain.makeServer();
                while (System.currentTimeMillis() - beginTime < MIN_START_TIME){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                startMain();
            }
        }).start();
    }

    private void startMain(){
        Intent it = new Intent(this,ActivityMain.class);
        startActivity(it);
        finish();
    }
}
