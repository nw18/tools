package com.newind.cmds;

import com.newind.beans.RoomInfo;

/**
 * Created by newind on 17-7-18.
 */
public class CmdCreateRoom extends CmdBase {
    private RoomInfo roomInfo = new RoomInfo();
    public CmdCreateRoom() {

    }

    public RoomInfo getRoomInfo() {
        return roomInfo;
    }
}
