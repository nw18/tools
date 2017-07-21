package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Created by newind on 17-7-21.
 */

public class FrameInit extends Frame{
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 900;
    public FrameInit() {
        Stage stage;
        //make room stage.
        //stage = new Stage(new FitViewport(WIDTH,HEIGHT));

        //init stage
        stage = new Stage(new FitViewport(WIDTH,HEIGHT));
        for (String name : new String[] {"join_room.png" , "make_room.png"}) {
            Button button = makeButton(name);
            button.setName(name.split(".png")[0]);
            stage.addActor(button);
        }
        Layout.InVertical(stage,400,0,900,800,160);
        stage.addCaptureListener(initListener);
        pushStage("init",stage);
    }

    private EventListener initListener = new EventListener() {
        @Override
        public boolean handle(Event event) {
            if(InputEvent.class.isInstance(event)) {
                InputEvent inputEvent = (InputEvent) event;
                if (inputEvent.getType() == InputEvent.Type.enter) {
                    String s = event.getTarget().getName();
                    if (s.equals("make_room")) {
                        
                    }else if (s.equals("enter_room")) {

                    }
                    return true;
                }
            }
            return false;
        }
    };
}
