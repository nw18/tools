package com.mygdx.game.events;

/**
 * Created by newind on 17-8-11.
 */

public interface ICommander {
    boolean onCommand(String cmd,Object ...args);
}
