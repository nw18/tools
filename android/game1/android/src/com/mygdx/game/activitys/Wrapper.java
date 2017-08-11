package com.mygdx.game.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.mygdx.game.R;

/**
 * Created by Administrator on 2017/8/11.
 */

public abstract class Wrapper extends Activity{
    protected int layoutID;
    protected View rootView;
    public Wrapper(int layoutID) {
        this.layoutID = layoutID;
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        preCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        rootView = makeRootView();
        setContentView(rootView);
        onCreate(savedInstanceState);
    }

    protected View makeRootView() {
        return getLayoutInflater().inflate(layoutID,null);
    }

    public <T extends View> T find(int id) {
        return (T) findViewById(id);
    }

    protected abstract void onCreateFinish(Bundle savedInstanceState);

    protected void preCreate(Bundle savedInstanceState) {

    }
}
