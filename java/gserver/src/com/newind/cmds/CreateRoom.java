package com.newind.cmds;

import com.newind.cmds.beans.RoomInfo;

/**
 * Created by newind on 17-7-18.
 */
public class CreateRoom extends CmdBase {
    private RoomInfo roomInfo = new RoomInfo();
    public CreateRoom() {

    }

    public RoomInfo getRoomInfo() {
        return roomInfo;
    }
}
