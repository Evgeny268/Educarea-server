package com.educarea.ServerApp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppWebSocket extends WebSocketServer {
    private static final Object lock = new Object();
    private static final int TCP_PORT = 4444;
    private HashMap<WebSocket, ClientInfo> clients;// может лучше ConcurrentHashMap и убрать все synchronized?
    private Logger log;

    public AppWebSocket() {
        super(new InetSocketAddress(TCP_PORT));
        clients = new HashMap<>();
        log = Logger.getLogger(EducLogger.class.getName());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (lock){
            clients.put(conn,new ClientInfo());
            log.log(Level.INFO,"new connection from "+ conn.getRemoteSocketAddress().getAddress().getHostName());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (lock){
            clients.remove(conn);
        }
        log.log(Level.INFO,"close connection "+conn.getRemoteSocketAddress().getAddress().getHostName()+" Clients count = "+clients.size());
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
