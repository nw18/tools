package com.newind.gser;

import com.newind.base.Pooling;

import java.net.DatagramPacket;

/**
 * Created by newind on 17-7-18.
 */
public class CommandCenter extends Pooling<DatagramPacket,CommandHandler> {
    public CommandCenter() {
        super(1024,32);
    }

    @Override
    protected CommandHandler makeWorker() {
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
