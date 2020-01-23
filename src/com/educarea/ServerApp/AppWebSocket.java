package com.educarea.ServerApp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class AppWebSocket extends WebSocketServer {
    private static final Object lock = new Object();
    private static final int TCP_PORT = 4444;
    private HashMap<WebSocket, ClientInfo> clients;// может лучше ConcurrentHashMap и убрать все synchronized?

    public AppWebSocket() {
        super(new InetSocketAddress(TCP_PORT));
        clients = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (lock){
            clients.put(conn,new ClientInfo());
            System.out.println("new client");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (lock){
            clients.remove(conn);
        }
        System.out.println("close connection. Clients count = "+clients.size());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(message);
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
        return clients;
    }

    public ClientInfo getClientInfo(WebSocket webSocket){
        ClientInfo clientInfo = null;
        synchronized (lock) {
            clientInfo = clients.get(webSocket);
        }
        return clientInfo;
    }
}
