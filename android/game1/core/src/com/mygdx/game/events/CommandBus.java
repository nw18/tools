package com.mygdx.game.events;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by newind on 17-8-11.
 */

public abstract class CommandBus {
    public static final int DISP_ANY = 100;
    public static final int DISP_ALL = 200;
    protected static CommandBus theBus = null;
    private HashMap<Object,ICommander> commanderHashMap = new HashMap<>();
    private List<ICommander> tempList = new LinkedList<>();
    private long tempTick = -1, hashTick = 0;

    protected CommandBus() {

    }

    public void register(ICommander commander) {
        register(commander,commander);
    }

    public synchronized void register(Object key,ICommander commander) {
        commanderHashMap.put(key,commander);
        hashTick++;
    }

    public synchronized void unregister(Object key) {
        commanderHashMap.remove(key);
        hashTick++;
    }

    protected void doDispatch(int type,String cmd,Object ...args) {
        synchronized (this) {
            if (tempTick != hashTick) {
                tempList.clear();
                tempList.addAll(commanderHashMap.values());
                tempTick = hashTick;
            }
        }
        for (ICommander commander : tempList) {
            if (type != DISP_ALL && commander.onCommand(cmd,args)) {
                break;
            }
        }
    }

    public abstract void dispatchAny(String cmd,Object ...args);

    public abstract void dispatchAll(String cmd,Object ...args);

    public static CommandBus getInstance() {
        return theBus;
    }
}
