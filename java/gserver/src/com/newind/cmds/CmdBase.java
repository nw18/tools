package com.newind.cmds;

import com.alibaba.fastjson.JSON;

/**
 * Created by newind on 17-7-18.
 */
class CmdBase {
    private String Name;
    public CmdBase(){
        this.Name = getClass().getSimpleName();
    }

    public String getName() {
        return Name;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
