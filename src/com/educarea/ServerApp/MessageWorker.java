package com.educarea.ServerApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import transfers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.sql.Savepoint;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;


public class MessageWorker implements Runnable, TypeRequestAnswer {

    private WebSocket webSocket;
    private String recivedMessage;
    private static final Object lock = new Object();

    public static final int MIN_TOKEN_LENGTH = 128;
    public static final int MAX_TOKEN_LENGTH = 200;
    public static final int MAX_TRY_CONNECT_WRONG_TOKEN = 5;

    public MessageWorker(WebSocket webSocket, String recivedMessage) {
        this.webSocket = webSocket;
        this.recivedMessage = recivedMessage;
    }

    @Override
    public void run() {
        if (recivedMessage == null) return;
        Transfers message = TransfersFactory.createFromJSON(recivedMessage);
        if (message == null) return;
        if (message instanceof Registration){
            registration((Registration) message);
        }else if (message instanceof Authentication){
            authentication((Authentication) message);
        }else if (message instanceof Authorization){
            authorization((Authorization) message);
        }
        else {
            sendError();
        }
    }

    private void registration(Registration registration){

        if (registration.login == null || registration.password == null){
            sendError();
            return;
        }

        if (!checkLogin(registration.login)){
            sendAnswer(BAD_LOGIN);
            return;
        }
        if (!checkPassword(registration.password)){
            sendAnswer(BAD_PASSWORD);
            return;
        }
        int alreadyCreate = 0;
        try {
            alreadyCreate = AppContext.educareaDB.getUserIdByLogin(registration.login);
        } catch (Exception e) {
            e.printStackTrace();
            sendError();
            return;
        }
        if (alreadyCreate!=0){
            sendAnswer(USER_ALREADY_EXIST);
        }else {
            synchronized (lock){
                Savepoint savepoint = null;
                try {
                    savepoint = AppContext.educareaDB.setSavepoint("insert_user");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError();
                    return;
                }
                try {
                    AppContext.educareaDB.insertNewUser(new User(registration.login,registration.password));
                    AppContext.educareaDB.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        AppContext.educareaDB.rollback(savepoint);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    sendError();
                }
            }
        }
    }

    private void authentication(Authentication authentication){
        if (authentication.login == null || authentication.password == null){
            sendError();
            return;
        }

        int alreadyCreate = 0;
        try {
            alreadyCreate = AppContext.educareaDB.getUserIdByLogin(authentication.login);
        } catch (Exception e) {
            e.printStackTrace();
            sendError();
            return;
        }
        if (alreadyCreate == 0){
            sendAnswer(USER_NOT_EXIST);
        }else {
            int userId = 0;
            try {
                userId = AppContext.educareaDB.getUserIdByLogAndPass(authentication.login, authentication.password);
            } catch (Exception e) {
                e.printStackTrace();
                sendError();
                return;
            }
            if (userId==0){
                sendAnswer(WRONG_PASSWORD);
            }else {
                String token = null;
                try {
                    do {
                        int tokenSize = ThreadLocalRandom.current().nextInt(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH + 1);
                        token = generateSafeToken(tokenSize);
                    } while (AppContext.educareaDB.getUserIdByAuthToken(token) != 0);
                }catch (Exception e) {
                    e.printStackTrace();
                    sendError();
                    return;
                }
                synchronized (lock){
                    Savepoint savepoint = null;
                    try{
                        savepoint = AppContext.educareaDB.setSavepoint("insert_token");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendError();
                        return;
                    }
                    try {
                        AppContext.educareaDB.insertAuthToken(userId, token);
                        AppContext.educareaDB.commit();
                        TransferRequestAnswer out = new TransferRequestAnswer(AUTHENTICATION_DONE,token);
                        sendTransfers(out);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            AppContext.educareaDB.rollback(savepoint);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        sendError();
                    }
                }
            }
        }
    }

    private void authorization(Authorization authorization){
        if (authorization.token == null){
            sendError();
            return;
        }

        int userId;
        try {
            userId = AppContext.educareaDB.getUserIdByAuthToken(authorization.token);
        } catch (Exception e) {
            e.printStackTrace();
            sendError();
            return;
        }

        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(this.webSocket);
        if (userId==0){
            sendAnswer(USER_NOT_EXIST);
            if (clientInfo == null){
                sendError();
                return;
            }else {
                if (clientInfo.getAuthTryCount()<MAX_TRY_CONNECT_WRONG_TOKEN){
                    clientInfo.setAuthTryCount(clientInfo.getAuthTryCount()+1);
                }else {
                    webSocket.close();
                }
            }
        }else {
            try {
                updateClientInfo(clientInfo, userId, authorization.token, authorization.cloudToken);
                sendAnswer(AUTHORIZATION_DONE);
            } catch (Exception e) {
                e.printStackTrace();
                sendError();
            }
        }

    }

    private String objToJson(Object c) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter,c);
        return stringWriter.toString();
    }

    private void sendTransfers(Transfers transfers){
        try {
            String out = objToJson(transfers);
            webSocket.send(out);
        } catch (IOException e) {
            e.printStackTrace();
            sendError();
        }
    }

    private void sendAnswer(String answer){
        TransferRequestAnswer transferRequestAnswer = new TransferRequestAnswer(answer);
        String out;
        try {
            out = objToJson(transferRequestAnswer);
        } catch (IOException e) {
            e.printStackTrace();
            sendError();
            return;
        }
        webSocket.send(out);
    }

    private void updateClientInfo(ClientInfo clientInfo, int userId, String token, String cloudToken) throws Exception{
        synchronized (lock) {
            Savepoint savepoint = null;
            try {
                savepoint = AppContext.educareaDB.setSavepoint("update_tokens");
            } catch (Exception e) {
                throw e;
            }
            try {
                User user = AppContext.educareaDB.getUserById(userId);
                AppContext.educareaDB.updateTokenTime(token);
                AppContext.educareaDB.updateTokenAddress(token, webSocket.getRemoteSocketAddress().getAddress().getHostName());
                if (cloudToken != null) {
                    AppContext.educareaDB.updateCloudToken(token, cloudToken);
                }
                if (user == null) {
                    throw new Exception("user is empty");
                } else {
                    clientInfo.setLogin(user.login);
                    clientInfo.setToken(token);
                    clientInfo.setCloudToken(cloudToken);
                    AppContext.educareaDB.commit();
                }
            } catch (Exception e) {
                AppContext.educareaDB.rollback(savepoint);
            }
        }
    }

    public static boolean checkLogin(String login){
        if (login.length()==0) return false;
        if (login.length()>30) return false;
        return login.matches("\\w+");
    }

    public static boolean checkPassword(String password){
        if (password.length()==0) return false;
        if (password.length()<6 || password.length()>255){
            return false;
        }else return true;
    }

    private void sendError(){
        TransferRequestAnswer out = new TransferRequestAnswer(ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
            webSocket.send(stringWriter.toString());
        } catch (IOException e) {
            webSocket.send(ERROR);
        }
    }

    private static String generateSafeToken(int size) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[size];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String token = encoder.encodeToString(bytes);
        return token;
    }
}
