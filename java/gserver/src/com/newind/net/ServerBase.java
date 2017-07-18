package com.newind.net;

import com.newind.base.LogManager;

import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * Created by newind on 17-7-18.
 */
public abstract class ServerBase extends Thread {
    protected boolean running = true;
    protected Logger logger = LogManager.getLogger();
    public abstract void close();
    public abstract String getAddress();
}
