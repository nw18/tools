package com.newind.gser;

import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;

import java.net.DatagramPacket;

/**
 * Created by newind on 17-7-18.
 */
public class CommandCenter extends Pooling<DatagramPacket,PoolingWorker<DatagramPacket>> {
    private class CommandHandler implements PoolingWorker<DatagramPacket>{
        @Override
        public void handle(DatagramPacket param) {

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
