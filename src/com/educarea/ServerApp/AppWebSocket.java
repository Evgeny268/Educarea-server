package com.educarea.ServerApp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class AppWebSocket extends WebSocketServer {
    private static final Object lock = new Object();
    private static final int TCP_PORT = 4444;
    private static HashMap<WebSocket, ClientInfo> clients;

    public AppWebSocket() {
        super(new InetSocketAddress(TCP_PORT));
        clients = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (lock){
            clients.put(conn,new ClientInfo());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (lock){
            clients.remove(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        MessageWorker messageWorker = new MessageWorker(conn,message);
        Thread thread = new Thread(messageWorker);
        thread.start();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    public HashMap<WebSocket, ClientInfo> getClients(){
        HashMap returnMap = null;
        synchronized (lock){
            returnMap = new HashMap(clients);
        }
        return returnMap;
    }
}
