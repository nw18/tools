package com.newind.gser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newind.base.LogManager;
import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;
import com.newind.cmds.CmdBase;

import java.net.DatagramPacket;

/**
 * Created by newind on 17-7-18.
 */
public class CommandCenter extends Pooling<DatagramPacket,PoolingWorker<DatagramPacket>> {
    private class CommandHandler implements PoolingWorker<DatagramPacket>{
        @Override
        public void handle(DatagramPacket param) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(new String(param.getData()));
                CmdBase cmd = CmdBase.createFromJSON(jsonObject);
                CommandDefine.Execute(cmd);
            } catch (ClassNotFoundException e) {
                LogManager.getLogger().info("parse command error: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public CommandCenter() {
        super(1024,32);
    }

    @Override
    protected PoolingWorker<DatagramPacket> makeWorker() {
        return new CommandHandler();
    }

    private static CommandCenter _inst_ = null;
    public static CommandCenter getInstance() {
        if (_inst_ == null) {
            _inst_ = new CommandCenter();
        }
        return _inst_;
    }
}
