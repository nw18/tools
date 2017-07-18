package com.newind;


import com.newind.gser.Server;
import com.newind.util.TextUtil;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            Server server = new Server("127.0.0.1",19999);
            server.setupServer();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String cmd = scanner.next();
                if (TextUtil.equal(cmd, "quit")) {
                    break;
                }
            }
            server.shutServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
