package com.newind.cmds;

/**
 * Created by newind on 17-7-18.
 * 这里心跳同时起校准时间戳的作用
 * 核算伤害以服务器时间为准
 */
public class CmdHeartBeat extends  CmdBase{
    public long LocalTick = 0;
    public long RemoteTick = 0;
    public CmdHeartBeat() {

    }
}
