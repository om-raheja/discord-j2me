package com.gtrxac.discord;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import javax.microedition.io.*;

import cc.nnproject.json.JSONObject;

public class SocketThing extends Thread {
    State s;

    String gateway;
    String token;

    private SocketConnection sc;
    private InputStream is;
    private OutputStream os;

    public SocketThing(State s, String gateway, String token) {
        this.s = s;
        this.gateway = gateway;
        this.token = token;
    }

    public void run() {
        // Build initial connection message
        JSONObject connProps = new JSONObject();
        connProps.put("os", "Linux");
        connProps.put("browser", "Firefox");
        connProps.put("device", "");

        JSONObject connInfo = new JSONObject();
        connInfo.put("token", token);
        // GUILD_MESSAGES, DIRECT_MESSAGES, MESSAGE_CONTENT
        connInfo.put("intents", 1 << 9 | 1 << 12 | 1 << 15);
        connInfo.put("properties", connProps);

        JSONObject connMsg = new JSONObject();
        connMsg.put("op", 2);
        connMsg.put("d", connInfo);

        try {
            sc = (SocketConnection) Connector.open(gateway);
            sc.setSocketOption(SocketConnection.LINGER, 45);
            sc.setSocketOption(SocketConnection.KEEPALIVE, 1);

            is = sc.openInputStream();
            os = sc.openOutputStream();

            os.write(connMsg.build().getBytes());
        }
        catch (Exception e) {
            e.printStackTrace();
            s.error("Gateway connection error: " + e.toString());
        }

        StringBuffer sb = new StringBuffer();
        String message;

        while (true) {
            try {
                // Get message
                while (true) {
                    int ch = is.read();
                    if (ch == '\n' || ch == -1) {
                        if (sb.length() > 0) {
                            message = sb.toString();
                            sb = new StringBuffer();
                            break;
                        }
                    } else {
                        sb.append((char) ch);
                    }
                }

                // Process message
                s.error("Got gateway msg: " + message);
            }
            catch (Exception e) {
                e.printStackTrace();
                s.error("Gateway error: " + e.toString());
                break;
            }
        }
    }
}
