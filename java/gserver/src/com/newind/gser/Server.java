package com.newind.gser;

import com.newind.net.UDPServer;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by newind on 17-7-18.
 */
public class Server extends UDPServer {
    public Server(String ip, int port) throws IOException {
        super(ip, port);
    }

    @Override
    protected DatagramPacket newPacket() {
        return super.newPacket();
    }

    @Override
    public void handleData(DatagramPacket packet) {
        CommandCenter.getInstance().putTask(packet);
    }

    public void setupServer() {
        CommandCenter.getInstance().setup();
        start();
    }

    public void shutServer() {
        close();
        CommandCenter.getInstance().release();
    }
}
