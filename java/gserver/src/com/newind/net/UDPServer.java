package com.newind.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by newind on 17-7-18.
 */
public abstract class UDPServer extends ServerBase{
    protected DatagramSocket socket;
    protected DatagramPacket newPacket () {
        return new DatagramPacket(new byte[1024*4],1024*4);
    }

    public UDPServer(String ip, int port) throws IOException {
        socket = new DatagramSocket(new InetSocketAddress(ip, port));
    }

    @Override
    public void run() {
        logger.info("start start @" + socket.getLocalSocketAddress().toString());
        while (running) {
            DatagramPacket packet = newPacket();
            try {
                socket.receive(packet);
                callHandleData(packet);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void callHandleData(DatagramPacket packet) {
        if (packet.getSocketAddress().toString().equals(socket.getLocalSocketAddress().toString())) {
            running = false;
        }else {
            handleData(packet);
        }
    }

    public abstract void handleData(DatagramPacket packet);

    @Override
    public void close() {
        DatagramPacket exitPack = new DatagramPacket(new byte[1], 0, 1,socket.getLocalSocketAddress());
        try {
            socket.send(exitPack);
            join(200);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAddress() {
        return socket.getLocalSocketAddress().toString();
    }
}
