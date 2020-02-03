package com.educarea.ServerApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import transfers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.sql.Savepoint;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;


public class MessageWorker implements Runnable, TypeRequestAnswer {

    private WebSocket webSocket;
    private String recivedMessage;
    private static final Object lock = new Object();

    public static final int MIN_TOKEN_LENGTH = 128;
    public static final int MAX_TOKEN_LENGTH = 200;
    public static final int MAX_TRY_CONNECT_WRONG_TOKEN = 5;

    private AppLogger log;

    public MessageWorker(WebSocket webSocket, String recivedMessage) {
        this.webSocket = webSocket;
        this.recivedMessage = recivedMessage;
        log = AppContext.log;
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
        }else if (message instanceof Timetable){
            try {
                timetableEdit((Timetable) message);
            } catch (Exception e) {
                log.warn("Timetable transfer error",e);
                sendError();
            }
        }
        else if (message instanceof TransferRequestAnswer){
            if (((TransferRequestAnswer) message).request==null){
                sendError();
                return;
            }
            if (((TransferRequestAnswer) message).request.equals(CREATE_GROUP)){
                createGroup((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(GET_MY_GROUPS)){
                getMyGroup();
            }else if (((TransferRequestAnswer) message).request.equals(LEAVE_GROUP)){
                leaveGroup((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(GET_GROUP_PERSONS)){
                getGroupPersons((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(CREATE_PERSON)){
                Transfers in = TransfersFactory.createFromJSON(((TransferRequestAnswer) message).extra);
                if (in instanceof GroupPerson){
                    createGroupPerson((GroupPerson) in);
                }else sendError();
            }else if (((TransferRequestAnswer) message).request.equals(UPDATE_PERSON)){
                Transfers in = TransfersFactory.createFromJSON(((TransferRequestAnswer) message).extra);
                if (in instanceof GroupPerson){
                    updateGroupPerson((GroupPerson) in);
                }else sendError();
            }else if (((TransferRequestAnswer) message).request.equals(INVITE_USER_TO_PERSON)){
                inviteUser((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(ACCEPT_INVITE)){
                acceptInvite((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(REJECT_INVITE)){
                rejectInvite((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(UNTIE_USER)){
                untieUser((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(GET_INVITES)){
                getInvites();
            }else if (((TransferRequestAnswer) message).request.equals(GET_PERSON_INVITES)){
                getPersonInvites((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(GET_TIMETABLE)){
                try {
                    getTimetable((TransferRequestAnswer) message);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.warn("getTimetable error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_TIMETABLE)){
                try {
                    deleteTimetable((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.warn("deleteTimetable error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_PERSON)){
                try {
                    deletePerson((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.warn("deletePerson",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_GROUP)){
                try {
                    deleteGroup((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.warn("deleteGroup",e);
                    sendError();
                }
            }
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
            log.warn("err",e);
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
                    log.warn("err",e);
                    sendError();
                    return;
                }
                try {
                    AppContext.educareaDB.insertNewUser(new User(registration.login,registration.password));
                    AppContext.educareaDB.commit();
                    sendAnswer(REGISTRATION_DONE);
                    log.info("new registration "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                } catch (Exception e) {
                    log.warn("insertNewUser error",e);
                    try {
                        AppContext.educareaDB.rollback(savepoint);
                    } catch (Exception ex) {
                        log.warn("err",ex);
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
                log.warn("err",e);
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
                    log.warn("err",e);
                    sendError();
                    return;
                }
                synchronized (lock){
                    Savepoint savepoint = null;
                    try{
                        savepoint = AppContext.educareaDB.setSavepoint("insert_token");
                    } catch (Exception e) {
                        log.warn("err",e);
                        sendError();
                        return;
                    }
                    try {
                        AppContext.educareaDB.insertAuthToken(userId, token);
                        AppContext.educareaDB.commit();
                        TransferRequestAnswer out = new TransferRequestAnswer(AUTHENTICATION_DONE,token);
                        sendTransfers(out);
                        log.info("new AUTHENTICATION "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                    } catch (Exception e) {
                        log.warn("err",e);
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
            log.warn("err",e);
            sendError();
            return;
        }

        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(this.webSocket);
        if (userId==0){
            sendAnswer(LOGOUT);
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
                TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_DONE,String.valueOf(userId),clientInfo.getLogin());
                sendTransfers(out);
                log.info("AUTHORIZATION_DONE "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
            } catch (Exception e) {
                log.warn("err",e);
                sendError();
            }
        }

    }

    private void createGroup(TransferRequestAnswer transferRequestAnswer){
        String groupName = transferRequestAnswer.extra;
        if (groupName==null){
            sendError();
            return;
        }
        if (groupName.equals("")){
            sendError();
            return;
        }

        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            int alreadyGroupCreate = 0;
            try {
                alreadyGroupCreate = AppContext.educareaDB.getGroupIdByName(groupName);
            } catch (Exception e) {
                e.printStackTrace();
                sendError();
                return;
            }
            if (alreadyGroupCreate != 0) {
                sendAnswer(GROUP_ALREADY_EXIST);
            }else {
                synchronized (lock){
                    Savepoint savepoint = null;
                    try {
                        savepoint = AppContext.educareaDB.setSavepoint("create_group");
                    }catch (Exception e){
                        log.warn("err",e);
                        sendError();
                        return;
                    }
                    try {
                        AppContext.educareaDB.insertNewGroup(groupName);
                        int groupId = AppContext.educareaDB.getGroupIdByName(groupName);
                        GroupPerson groupPerson = new GroupPerson(groupId, userId, 1);
                        AppContext.educareaDB.insertGroupPerson(groupPerson);
                        AppContext.educareaDB.commit();
                        sendAnswer(GROUP_ADDED);
                        log.info("GROUP_ADDED "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                    }catch (Exception e){
                        log.warn("err",e);
                        try {
                            AppContext.educareaDB.rollback(savepoint);
                        } catch (Exception ex) {
                            log.warn("err",ex);
                        }
                        sendError();
                    }
                }
            }
        }
    }

    private void getMyGroup(){
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            try {
                ArrayList<Integer> groupsId = AppContext.educareaDB.getGroupsIdByUserId(userId);
                ArrayList<Group> groups = new ArrayList<>();
                for (int i = 0; i < groupsId.size(); i++) {
                    groups.add(AppContext.educareaDB.getGroupById(groupsId.get(i)));
                }
                ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByUserId(userId);
                UserGroups userGroups = new UserGroups();
                for (int i = 0; i < groups.size(); i++) {
                    for (int j = 0; j < groupPeople.size(); j++) {
                        if (groups.get(i).groupId == groupPeople.get(j).groupId) {
                            userGroups.add(groups.get(i), groupPeople.get(j));
                        }
                    }
                }
                log.info("getMyGroup "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                sendTransfers(userGroups);
            }catch (Exception e){
                log.warn("err",e);
                sendError();
            }
        }else {
            sendError();
        }
    }

    private void leaveGroup(TransferRequestAnswer transferRequestAnswer){
        int groupId = 0;
        try {
            groupId = Integer.valueOf(transferRequestAnswer.extra);
        }catch (Exception e){
            log.warn("err",e);
            sendError();
            return;
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            ArrayList<GroupPerson> groupPeople = null;
            try {
                groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            boolean userIsModerator = false;
            int moderatorCount = 0;
            int groupPersonId = 0;
            for (int i = 0; i < groupPeople.size(); i++) {
                if (groupPeople.get(i).moderator==1 && groupPeople.get(i).userId!=0){
                    moderatorCount++;
                    if (userId==groupPeople.get(i).userId){
                        userIsModerator=true;
                    }
                }
                if (userId==groupPeople.get(i).userId){
                    groupPersonId = groupPeople.get(i).groupPersonId;
                }
            }
            if (userIsModerator && moderatorCount<=1){
                sendAnswer(YOU_ONLY_MODERATOR);
            }else {
                synchronized (lock){
                    Savepoint savepoint = null;
                    try {
                        savepoint = AppContext.educareaDB.setSavepoint("leave_group");
                    }catch (Exception e){
                        log.warn("err",e);
                        sendError();
                        return;
                    }
                    try {
                        AppContext.educareaDB.updateGroupPersonUserId(groupPersonId, 0);
                        AppContext.educareaDB.commit();
                        sendAnswer(UPDATE_INFO);
                        log.info("leave group "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                    }catch (Exception e){
                        log.warn("err",e);
                        sendError();
                        try {
                            AppContext.educareaDB.rollback(savepoint);
                        }catch (Exception e1){
                        }
                        return;
                    }

                }
            }
        }
    }

    private void getGroupPersons(TransferRequestAnswer transferRequestAnswer){
        int groupId = 0;
        try {
            groupId = Integer.parseInt(transferRequestAnswer.extra);
        }catch (Exception e){
            log.warn("err",e);
            sendError();
            return;
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId !=0){
            ArrayList<GroupPerson> groupPeople = null;
            try {
                groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
            }catch (Exception e){
                log.warn("err",e);
                return;
            }
            if (userInGroup(userId, groupPeople)){
                if (!userIsModerator(userId, groupPeople)){
                    deleteUserIdInfo(userId,groupPeople);
                }
                GroupPersons out = new GroupPersons(groupPeople);
                sendTransfers(out);
                log.info("getGroupPerson "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
            }else {
                sendAnswer(NO_PERMISSION);
            }
        }
    }

    private void createGroupPerson(GroupPerson groupPerson){
        int groupId = groupPerson.groupId;
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId != 0){
            synchronized (lock) {
                ArrayList<GroupPerson> groupPeople = null;
                try {
                    groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
                } catch (Exception e) {
                    log.warn("err",e);
                    sendError();
                    return;
                }
                if (userInGroup(userId, groupPeople)) {
                    if (userIsModerator(userId, groupPeople)) {
                        if (checkNewGroupPesron(groupPerson)) {
                            Savepoint savepoint = null;
                            try {
                                savepoint = AppContext.educareaDB.setSavepoint("new_groupPerson");
                            }catch (Exception e){
                                log.warn("err",e);
                                sendError();
                                return;
                            }
                            try {
                                groupPerson.userId=0;
                                AppContext.educareaDB.insertGroupPerson(groupPerson);
                                AppContext.educareaDB.commit();
                                sendAnswer(UPDATE_INFO);
                                log.info("create group person "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                            }catch (Exception e){
                                log.warn("err",e);
                                try {
                                    AppContext.educareaDB.rollback(savepoint);
                                }catch (Exception ex){
                                    log.warn("err",ex);
                                }
                                sendError();
                                return;
                            }
                        } else {
                            sendError();
                        }
                    } else {
                        sendAnswer(NO_PERMISSION);
                    }
                } else {
                    sendAnswer(NO_PERMISSION);
                }
            }
        }
    }

    private void updateGroupPerson(GroupPerson groupPerson){
        int groupId = groupPerson.groupId;
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock){
                ArrayList<GroupPerson> groupPeople = null;
                try {
                    groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
                    GroupPerson oldGroupPerson = AppContext.educareaDB.getGroupPersonById(groupPerson.groupPersonId);
                    groupPerson.userId = oldGroupPerson.userId;
                } catch (Exception e) {
                    log.warn("err",e);
                    sendError();
                    return;
                }
                if (userInGroup(userId, groupPeople) && personInGroup(groupPerson.groupPersonId, groupPeople)){
                    if (userIsModerator(userId, groupPeople)){
                        if (checkNewGroupPesron(groupPerson)) {
                            if (lifeModeratorCount(groupPeople)==1 && groupPerson.moderator==0 && groupPerson.userId==userId){
                                sendAnswer(NO_PERMISSION);
                                return;
                            }
                            Savepoint savepoint = null;
                            try {
                                savepoint = AppContext.educareaDB.setSavepoint("update_groupPerson");
                            } catch (Exception e) {
                                log.warn("err",e);
                                sendError();
                                return;
                            }
                            try {
                                AppContext.educareaDB.updateGroupPerson(groupPerson.groupPersonId, groupPerson);
                                AppContext.educareaDB.commit();
                                sendAnswer(UPDATE_INFO);
                                log.info("update group person "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                            }catch (Exception e){
                                log.warn("err",e);
                                try {
                                    AppContext.educareaDB.rollback(savepoint);
                                }catch (Exception ex){
                                    log.warn("err",ex);
                                }
                                sendError();
                                return;
                            }
                        } else sendError();
                    } else sendAnswer(NO_PERMISSION);
                } else sendAnswer(NO_PERMISSION);
            }
        }
    }

    private void getInvites(){
        log.info("get invites "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        try {
            if (userId != 0) {
                ArrayList<MyInvite> myInvites = new ArrayList<>();
                ArrayList<GroupPersonInvite> invites;
                invites = AppContext.educareaDB.getPersonInviteByUserId(userId);
                for (int i = 0; i < invites.size(); i++) {
                    GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(invites.get(i).groupPersonId);
                    Group group = AppContext.educareaDB.getGroupById(groupPerson.groupId);
                    MyInvite myInvite = new MyInvite(invites.get(i).groupPersonInviteId, group.name, groupPerson);
                    myInvites.add(myInvite);
                }
                MyInvites out = new MyInvites(myInvites);
                sendTransfers(out);
            }
        }catch (Exception e){
            log.warn("err",e);
            sendError();
        }
    }

    private void acceptInvite(TransferRequestAnswer transferRequestAnswer){
        log.info("accept invites "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int inviteId = 0;
        try {
            inviteId = Integer.parseInt(transferRequestAnswer.extra);
        }catch (Exception e){
            log.warn("err",e);
            sendError();
            return;
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                Savepoint savepoint = null;
                try {
                    savepoint = AppContext.educareaDB.setSavepoint("acceptInvite");
                }catch (Exception e){
                    log.warn("err",e);
                    sendError();
                    return;
                }
                try {
                    GroupPersonInvite invite = AppContext.educareaDB.getGroupPersonInviteById(inviteId);
                    if (userId == invite.userId) {
                        int groupPersonId = invite.groupPersonId;
                        GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                        AppContext.educareaDB.removeGroupPersonInvite(inviteId);
                        groupPerson.userId = userId;
                        AppContext.educareaDB.updateGroupPerson(groupPersonId, groupPerson);
                        AppContext.educareaDB.commit();
                        sendAnswer(UPDATE_INFO);
                    } else {
                        sendError();
                    }
                }catch (Exception e){
                    log.warn("err",e);
                    try {
                        AppContext.educareaDB.rollback(savepoint);
                    } catch (Exception ex) {
                        log.warn("err",ex);
                    }
                    sendError();
                }
            }
        }
    }

    private void rejectInvite(TransferRequestAnswer transferRequestAnswer){
        log.info("reject invites"+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int inviteId = 0;
        try {
            inviteId = Integer.parseInt(transferRequestAnswer.extra);
        }catch (Exception e){
            log.warn("err",e);
            sendError();
            return;
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                Savepoint savepoint = null;
                try {
                    savepoint = AppContext.educareaDB.setSavepoint("rejectInvite");
                }catch (Exception e){
                    log.warn("err",e);
                    sendError();
                    return;
                }
                try {
                    GroupPersonInvite invite = AppContext.educareaDB.getGroupPersonInviteById(inviteId);
                    if (userId == invite.userId) {
                        AppContext.educareaDB.removeGroupPersonInvite(inviteId);
                        AppContext.educareaDB.commit();
                        sendAnswer(UPDATE_INFO);
                    } else {
                        sendError();
                    }
                }catch (Exception e){
                    log.warn("err",e);
                    try {
                        AppContext.educareaDB.rollback(savepoint);
                    } catch (Exception ex) {
                        log.warn("err",ex);
                    }
                    sendError();
                }
            }
        }
    }

    private void inviteUser(TransferRequestAnswer transferRequestAnswer){
        log.info("invite user "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int groupPersonId = Integer.parseInt(transferRequestAnswer.extra);;
        String login = transferRequestAnswer.extraArr[0];
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                try {
                    GroupPerson invitePerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                    if (invitePerson.userId!=0){
                        sendError();
                        return;
                    }
                    ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(invitePerson.groupId);
                    if (userInGroup(userId, groupPeople)) {
                        if (userIsModerator(userId, groupPeople)) {
                            int inviteUserId = AppContext.educareaDB.getUserIdByLogin(login);
                            for (int i = 0; i < groupPeople.size(); i++) {
                                if (groupPeople.get(i).userId == inviteUserId){
                                    sendError();
                                    return;
                                }
                            }
                            if (inviteUserId == 0) {
                                sendAnswer(USER_NOT_EXIST);
                            } else {
                                GroupPersonInvite invite = new GroupPersonInvite(groupPersonId, inviteUserId);
                                Savepoint savepoint = null;
                                try {
                                    savepoint = AppContext.educareaDB.setSavepoint("inviteUser");
                                } catch (Exception e) {
                                    log.warn("err",e);
                                    sendError();
                                    return;
                                }
                                try {
                                    AppContext.educareaDB.insertPersonInvite(invite);
                                    AppContext.educareaDB.commit();
                                    sendAnswer(UPDATE_INFO);
                                } catch (Exception e) {
                                    log.warn("err",e);
                                    try {
                                        AppContext.educareaDB.rollback(savepoint);
                                    } catch (Exception ex) {
                                        log.warn("err",ex);
                                    }
                                    sendError();
                                }
                            }
                        } else sendAnswer(NO_PERMISSION);
                    } else sendAnswer(NO_PERMISSION);
                }catch (Exception e){
                    log.warn("err",e);
                    sendError();
                }
            }
        }
    }

    private void untieUser(TransferRequestAnswer transferRequestAnswer){
        log.info("untie user "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int groupPersonId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                try {
                    GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                    ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupPerson.groupId);
                    if (userInGroup(userId, groupPeople)) {
                        if (userIsModerator(userId, groupPeople)) {
                            if (userId==groupPerson.userId){
                                if (lifeModeratorCount(groupPeople)==1){
                                    sendError();
                                    return;
                                }
                            }
                            groupPerson.userId = 0;
                            Savepoint savepoint = null;
                            try {
                                savepoint = AppContext.educareaDB.setSavepoint("inviteUser");
                            } catch (Exception e) {
                                log.warn("err",e);
                                sendError();
                                return;
                            }
                            try {
                                AppContext.educareaDB.updateGroupPerson(groupPerson.groupPersonId, groupPerson);
                                AppContext.educareaDB.removeGroupPersonInviteByPersonId(groupPerson.groupPersonId);
                                AppContext.educareaDB.commit();
                                sendAnswer(UPDATE_INFO);
                            } catch (Exception e) {
                                log.warn("err",e);
                                try {
                                    AppContext.educareaDB.rollback(savepoint);
                                } catch (Exception ex) {
                                    log.warn("err",ex);
                                }
                                sendError();
                            }
                        } else sendAnswer(NO_PERMISSION);
                    } else sendAnswer(NO_PERMISSION);
                }catch (Exception e){
                    log.warn("err",e);
                    sendError();
                }
            }
        }
    }

    private void getPersonInvites(TransferRequestAnswer transferRequestAnswer){
        log.info("get person invites "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int groupPersonId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            try {
                GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupPerson.groupId);
                if (userInGroup(userId, groupPeople)) {
                    if (userIsModerator(userId, groupPeople)) {
                        GroupPersonInvite groupPersonInvite = AppContext.educareaDB.getPersonInviteByPersonId(groupPersonId);
                        sendTransfers(groupPersonInvite);
                    } else sendAnswer(NO_PERMISSION);
                } else sendAnswer(NO_PERMISSION);
            }catch (Exception e){
                log.warn("err",e);
                sendError();
                return;
            }
        }
    }

    private void timetableEdit(Timetable timetable) throws Exception{
        log.info("timetable edit"+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                if (checkTimetable(timetable)) {
                    ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(timetable.groupId);
                    if (userInGroup(userId, groupPeople)) {
                        if (userIsModerator(userId, groupPeople)) {
                            if (timetable.groupPersonId != 0) {
                                GroupPerson teacher = AppContext.educareaDB.getGroupPersonById(timetable.groupPersonId);
                                if ((teacher.groupId != timetable.groupId) || teacher.personType == 0) {
                                    sendAnswer(NO_PERMISSION);
                                    return;
                                }
                            }
                                if (timetable.timetableId != 0) {
                                    Timetable oldTimeTable = AppContext.educareaDB.getTimetableById(timetable.timetableId);
                                    if (oldTimeTable.groupId != timetable.groupId) {
                                        sendAnswer(NO_PERMISSION);
                                        return;
                                    }
                                }
                                Savepoint savepoint = null;
                                try {
                                    savepoint = AppContext.educareaDB.setSavepoint("timetableEdit");
                                } catch (Exception e) {
                                    log.warn("err",e);
                                    sendError();
                                    return;
                                }
                                try {
                                    if (timetable.timetableId != 0) {
                                        AppContext.educareaDB.updateTimetable(timetable.timetableId, timetable);
                                    } else {
                                        AppContext.educareaDB.insertTimetable(timetable);
                                    }
                                    AppContext.educareaDB.commit();
                                    sendAnswer(UPDATE_INFO);
                                } catch (Exception e) {
                                    log.warn("err",e);
                                    try {
                                        AppContext.educareaDB.rollback(savepoint);
                                    } catch (Exception ex) {
                                        log.warn("err",ex);
                                    }
                                    sendError();
                                }
                        } else sendAnswer(NO_PERMISSION);
                    } else sendAnswer(NO_PERMISSION);
                } else sendError();
            }
        }
    }

    private void deleteTimetable(TransferRequestAnswer transferRequestAnswer) throws Exception{
        log.info("delete timetable "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int timetableId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                Timetable timetable = AppContext.educareaDB.getTimetableById(timetableId);
                ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(timetable.groupId);
                if (userInGroup(userId, groupPeople)) {
                    if (userIsModerator(userId, groupPeople)) {
                        Savepoint savepoint = null;
                        try{
                            savepoint = AppContext.educareaDB.setSavepoint("deleteTimetable");
                        }catch (Exception e){
                            log.warn("err",e);
                            sendError();
                            return;
                        }
                        try {
                            AppContext.educareaDB.deleteTimetable(timetableId);
                            AppContext.educareaDB.commit();
                            sendAnswer(UPDATE_INFO);
                        }catch (Exception e){
                            log.warn("err",e);
                            try {
                                AppContext.educareaDB.rollback(savepoint);
                            } catch (Exception ex) {
                                log.warn("err",ex);
                            }
                            sendError();
                        }
                    } else sendAnswer(NO_PERMISSION);
                } else sendAnswer(NO_PERMISSION);
            }
        }
    }

    private void deletePerson(TransferRequestAnswer transferRequestAnswer) throws Exception{
        log.info("delete person"+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int groupPersonId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                GroupPerson deletePerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                if (deletePerson.userId == userId) {
                    sendAnswer(NO_PERMISSION);
                    return;
                }
                ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(deletePerson.groupId);
                if (userInGroup(userId, groupPeople)) {
                    if (userIsModerator(userId, groupPeople)) {
                        ArrayList<Timetable> timetables = AppContext.educareaDB.getTimetableByPersonId(groupPersonId);
                        Savepoint savepoint = null;
                        try {
                            savepoint = AppContext.educareaDB.setSavepoint("deletePerson");
                        } catch (Exception e) {
                            log.warn("err",e);
                            sendError();
                            return;
                        }
                        try {
                            for (int i = 0; i < timetables.size(); i++) {
                                Timetable timetable = timetables.get(i);
                                if (timetable.groupPersonId == groupPersonId) {
                                    timetable.groupPersonId = 0;
                                    AppContext.educareaDB.updateTimetable(timetable.timetableId, timetable);
                                }
                            }
                            AppContext.educareaDB.deletePersonInviteByPersonId(groupPersonId);
                            AppContext.educareaDB.deleteGroupPersonById(groupPersonId);
                            AppContext.educareaDB.commit();
                            sendAnswer(UPDATE_INFO);
                        } catch (Exception e) {
                            log.warn("err",e);
                            try {
                                AppContext.educareaDB.rollback(savepoint);
                            } catch (Exception ex) {
                                log.warn("err",ex);
                            }
                            sendError();
                        }
                    } else sendAnswer(NO_PERMISSION);
                } else sendAnswer(NO_PERMISSION);
            }
        }
    }

    private void deleteGroup(TransferRequestAnswer transferRequestAnswer) throws Exception{
        log.info("delete group "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int groupId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock){
                ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
                if (userInGroup(userId,groupPeople)){
                    if (userIsModerator(userId,groupPeople)){
                        Savepoint savepoint = null;
                        try {
                            savepoint = AppContext.educareaDB.setSavepoint("deleteGroup");
                        } catch (Exception e) {
                            log.warn("err",e);
                            sendError();
                            return;
                        }
                        try {
                            for (int i = 0; i < groupPeople.size(); i++) {
                                AppContext.educareaDB.deletePersonInviteByPersonId(groupPeople.get(i).groupPersonId);
                            }
                            AppContext.educareaDB.deleteGroupPersonByGroupId(groupId);
                            AppContext.educareaDB.deleteGroupById(groupId);
                            AppContext.educareaDB.commit();
                            sendAnswer(DELETE_GROUP);
                        } catch (Exception e) {
                            log.warn("err",e);
                            try {
                                AppContext.educareaDB.rollback(savepoint);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            sendError();
                        }
                    }else sendAnswer(NO_PERMISSION);
                }else sendAnswer(NO_PERMISSION);
            }
        }
    }

    private void getTimetable(TransferRequestAnswer transferRequestAnswer) throws Exception{
        log.info("get timetable "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        int groupId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
            if (userInGroup(userId,groupPeople)){
                ArrayList<Timetable> timetables = AppContext.educareaDB.getTimetableByGroupId(groupId);
                Collections.sort(timetables);
                sendTransfers(new Timetables(timetables));
            }else sendAnswer(NO_PERMISSION);
        }
    }

    private boolean checkTimetable(Timetable timetable){
        if (timetable.day<1 || timetable.day>7) return false;
        if (timetable.parityweek<0 || timetable.parityweek>2) return false;
        if (timetable.time!=null){
            try {
                LocalTime time = LocalTime.parse(timetable.time);
            }catch (Exception e){
                return false;
            }
        }
        return true;
    }

    private boolean checkNewGroupPesron(GroupPerson groupPerson){
        if (groupPerson.personType<0 || groupPerson.personType>1) return false;
        if (groupPerson.moderator<0 || groupPerson.moderator>1) return false;
        if (groupPerson.surname!=null){
            if (groupPerson.surname.equals("")){
                groupPerson.surname = null;
            }
        }
        if (groupPerson.name!=null){
            if (groupPerson.name.equals("")){
                groupPerson.name = null;
            }
        }
        if (groupPerson.patronymic!=null){
            if (groupPerson.patronymic.equals("")){
                groupPerson.patronymic = null;
            }
        }
        return true;
    }

    private boolean userInGroup(int userId, ArrayList<GroupPerson> groupPeople){
        for (int i = 0; i < groupPeople.size(); i++) {
            if (userId == groupPeople.get(i).userId){
                return true;
            }
        }
        return false;
    }

    private boolean personInGroup(int groupPersonId, ArrayList<GroupPerson> groupPeople){
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPersonId == groupPeople.get(i).groupPersonId){
                return true;
            }
        }
        return false;
    }

    private boolean userIsModerator(int userId, ArrayList<GroupPerson> groupPeople){
        for (int i = 0; i < groupPeople.size(); i++) {
            if (userId == groupPeople.get(i).userId){
                if (groupPeople.get(i).moderator == 1){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean personIsTeacher(int groupPersonId, ArrayList<GroupPerson> groupPeople){
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPersonId == groupPeople.get(i).groupPersonId){
                if (groupPeople.get(i).personType==1){
                    return true;
                }else return false;
            }
        }
        return false;
    }

    private int lifeModeratorCount(ArrayList<GroupPerson> groupPeople){
        int count = 0;
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPeople.get(i).userId!=0 && groupPeople.get(i).moderator==1){
                count++;
            }
        }
        return count;
    }

    private void deleteUserIdInfo(int userId, ArrayList<GroupPerson> groupPeople){
        for (int i = 0; i < groupPeople.size(); i++) {
            if (userId!=groupPeople.get(i).userId){
                groupPeople.get(i).userId = 0;
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

    private boolean connectBrutforseCheck(ClientInfo clientInfo){
        sendAnswer(LOGOUT);
        if (clientInfo == null){
            sendError();
            return false;
        }else {
            if (clientInfo.getAuthTryCount()<MAX_TRY_CONNECT_WRONG_TOKEN){
                clientInfo.setAuthTryCount(clientInfo.getAuthTryCount()+1);
                return false;
            }else {
                webSocket.close();
                return true;
            }
        }
    }

    private int checkAuthorizationGetUserId(ClientInfo clientInfo){
        if (clientInfo.getToken()==null){
            sendAnswer(AUTHORIZATION_FAILURE);
            return 0;
        }
        int userId = 0;
        try {
            userId = AppContext.educareaDB.getUserIdByAuthToken(clientInfo.getToken());
        } catch (Exception e) {
            e.printStackTrace();
            sendError();
            return 0;
        }
        if (userId==0){
            connectBrutforseCheck(clientInfo);
            return 0;
        }else return userId;
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

    public static String generateSafeToken(int size) {
        SecureRandom random = new SecureRandom();
        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        char []simbol = alphabet.toCharArray();
        String key = "";
        for (int i = 0; i < size; i++) {
            int current = random.nextInt(alphabet.length());
            key += simbol[current];
        }
        return key;
    }
}
