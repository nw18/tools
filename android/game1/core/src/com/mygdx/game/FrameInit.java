package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.events.CommandBus;

/**
 * Created by newind on 17-7-21.
 */

public class FrameInit extends Frame{
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 900;
    public FrameInit() {
        makeInit();
    }

    private void makeInit() {
        Stage stage = new Stage(new FitViewport(WIDTH,HEIGHT));
        for (String name : new String[] {"self_config.png", "join_room.png" , "make_room.png"}) {
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
                    switch (s){
                        case "self_config":
                        case "join_room":
                        case "make_room":
                            CommandBus.getInstance().dispatchAny(s);
                            break;
                    }
                }
            }
            return false;
        }
    };
}
