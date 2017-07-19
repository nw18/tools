package com.newind.cmds;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.newind.util.TextUtil;

/**
 * Created by newind on 17-7-18.
 */
public class CmdBase {
    private String name;
    private String clientID;
    public CmdBase(){
        this.name = getClass().getName();
    }

    public String getName() {
        return name;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static <T extends CmdBase> T createFromJSON(JSONObject jsonObject) throws ClassNotFoundException {
        String cmdName = jsonObject.getString("name");
        if (TextUtil.isEmpty(cmdName)){
            return null;
        }
        return TypeUtils.castToJavaBean(jsonObject, (Class<T>) Class.forName(cmdName));
    }
}
