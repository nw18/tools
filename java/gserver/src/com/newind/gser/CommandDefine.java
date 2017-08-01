package com.newind.gser;

import com.newind.base.LogManager;
import com.newind.cmds.CmdBase;
import com.newind.cmds.CmdCreateRoom;

import java.util.HashMap;

/**
 * Created by newind on 17-7-19.
 */
public class CommandDefine {

    public abstract static class Commander<T>{
        public abstract void execute(T param);
    }

    private static HashMap<String,Commander> cmdMap = new HashMap<>();
    public static void Init() {
        //create room command.
        cmdMap.put(CmdCreateRoom.class.getSimpleName(),new Commander<CmdCreateRoom>(){

            @Override
            public void execute(CmdCreateRoom param) {

            }
        });


    }

    public static <T extends CmdBase> void Execute(T command) {
        Commander commander = cmdMap.getOrDefault(command.getName(),null);
        if (null == commander) {
            LogManager.getLogger().info("dismiss command executor:" + command.getName());
        }else {
            commander.execute(command);
        }
    }
}
