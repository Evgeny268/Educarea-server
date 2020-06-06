package com.educarea.ServerApp;

import com.educarea.ServerApp.firebase.CloudMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import transfers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MessageWorker implements Runnable, TypeRequestAnswer {

    private WebSocket webSocket;
    private String recivedMessage;
    private static final Object lock = new Object();

    public static final int MIN_TOKEN_LENGTH = 128;
    public static final int MAX_TOKEN_LENGTH = 200;
    public static final int MAX_TRY_CONNECT_WRONG_TOKEN = 5;
    public static final int PERSON_CODE_START_SIZE = 8;
    public static final int LIVE_TOKEN_COUNT = 5;

    private Logger log;

    public MessageWorker(WebSocket webSocket, String recivedMessage) {
        this.webSocket = webSocket;
        this.recivedMessage = recivedMessage;
        log = Logger.getLogger(EducLogger.class.getName());
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
                log.log(Level.WARNING, "Timetable transfer error",e);
                sendError();
            }
        }else if (message instanceof StudentsChatMessage){
            try{
                sendStudentChatMessage((StudentsChatMessage) message);
            }catch (Exception e){
                log.log(Level.WARNING, "error in function sendStudentChatMessage",e);
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
                    try {
                        updateGroupPerson((GroupPerson) in);
                    }catch (Exception e){
                        e.printStackTrace();
                        log.log(Level.WARNING, "updateGroupPerson error",e);
                        sendError();
                    }
                }else sendError();
            }else if (((TransferRequestAnswer) message).request.equals(GET_TIMETABLE)){
                try {
                    getTimetable((TransferRequestAnswer) message);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.log(Level.WARNING, "getTimetable error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_TIMETABLE)){
                try {
                    deleteTimetable((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "deleteTimetable error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_PERSON)){
                try {
                    deletePerson((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "deletePerson error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_GROUP)){
                try {
                    deleteGroup((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "deleteGroup error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(SEND_CHANNEL_MESSAGE)){
                try {
                    sendChannelMessage((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "send channel message error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(GET_CHANNEL_MESSAGE)){
                try {
                    getChannelMessage((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "getChannelMessage error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(GET_PLATFORM_VERSION)){
                getPlatfrormVersion((TransferRequestAnswer) message);
            }else if (((TransferRequestAnswer) message).request.equals(CREATE_PERSON_CODE)){
                try {
                    generatePersonCode((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "send channel message error",e);
                    sendError();
                }
            }else if (((TransferRequestAnswer) message).request.equals(DELETE_PERSON_CODE)){
                try {
                    deletePersonCode((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "send channel message error",e);
                    sendError();
                }
            }
            else if (((TransferRequestAnswer) message).request.equals(INVITE_BY_PERSON_CODE)){
                try {
                    inviteByPersonCode((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "send channel message error",e);
                    sendError();
                }
            }
            else if (((TransferRequestAnswer) message).request.equals(GET_PERSON_CODE)){
                try {
                    getPersonCode((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "send channel message error",e);
                    sendError();
                }
            }
            else if (((TransferRequestAnswer) message).request.equals(UNBIND_USER)){
                try {
                    untieUser((TransferRequestAnswer) message);
                } catch (Exception e) {
                    log.log(Level.WARNING, "send channel message error",e);
                    sendError();
                }
            }
            else if (((TransferRequestAnswer) message).request.equals(LOGOUT)){
                logout();
            }
            else if (((TransferRequestAnswer) message).request.equals(LOGOUT_OTHER_SESSION)){
                logoutOtherSession();
            }else if (((TransferRequestAnswer) message).request.equals(GET_STUDENT_MESSAGE)){
                try {
                    getStudentChatMessage((TransferRequestAnswer) message);
                }catch (Exception e){
                    log.log(Level.WARNING, "can't get students chat message",e);
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
            log.log(Level.WARNING, "error",e);
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
                    log.log(Level.WARNING, "error",e);
                    sendError();
                    return;
                }
                try {
                    AppContext.educareaDB.insertNewUser(new User(registration.login,registration.password));
                    AppContext.educareaDB.commit();
                    sendAnswer(REGISTRATION_DONE);
                    log.info("new registration "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                } catch (Exception e) {
                    log.log(Level.WARNING, "error",e);
                    try {
                        AppContext.educareaDB.rollback(savepoint);
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "error",e);
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
                log.log(Level.WARNING, "error",e);
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
                    log.log(Level.WARNING, "error",e);
                    sendError();
                    return;
                }
                synchronized (lock){
                    Savepoint savepoint = null;
                    try{
                        savepoint = AppContext.educareaDB.setSavepoint("insert_token");
                    } catch (Exception e) {
                        log.log(Level.WARNING, "error",e);
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
                        log.log(Level.WARNING, "error",e);
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
            log.log(Level.WARNING, "error",e);
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
                log.log(Level.WARNING, "error",e);
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
                        log.log(Level.WARNING, "error",e);
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
                        log.log(Level.WARNING, "error",e);
                        try {
                            AppContext.educareaDB.rollback(savepoint);
                        } catch (Exception ex) {
                            log.log(Level.WARNING, "error",e);
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
                log.log(Level.WARNING, "error",e);
                sendError();
            }
        }else {
            sendError();
        }
    }

    private void leaveGroup(TransferRequestAnswer transferRequestAnswer){
        int groupId = 0;
        try {
            groupId = Integer.parseInt(transferRequestAnswer.extra);
        }catch (Exception e){
            log.log(Level.WARNING, "error",e);
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
                        log.log(Level.WARNING, "error",e);
                        sendError();
                        return;
                    }
                    try {
                        AppContext.educareaDB.updateGroupPersonUserId(groupPersonId, 0);
                        AppContext.educareaDB.commit();
                        sendAnswer(UPDATE_INFO);
                        log.info("leave group "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                    }catch (Exception e){
                        log.log(Level.WARNING, "error",e);
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
            log.log(Level.WARNING, "error",e);
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
                log.log(Level.WARNING, "error",e);
                return;
            }
            if (userInGroup(userId, groupPeople)){
                deleteUserIdInfo(userId,groupPeople);
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
                    log.log(Level.WARNING, "error",e);
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
                                log.log(Level.WARNING, "error",e);
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
                                log.log(Level.WARNING, "error",e);
                                try {
                                    AppContext.educareaDB.rollback(savepoint);
                                }catch (Exception ex){
                                    log.log(Level.WARNING, "error",ex);
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

    private void updateGroupPerson(GroupPerson groupPerson) throws Exception{
        int groupId = groupPerson.groupId;
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock){
                ArrayList<GroupPerson> groupPeople = null;
                boolean emptyName = false;
                groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
                GroupPerson oldGroupPerson = AppContext.educareaDB.getGroupPersonById(groupPerson.groupPersonId);
                int groupPersonSenderId = LogicUtils.getGroupPersonIdByUserId(userId, oldGroupPerson.groupId);
                GroupPerson groupPersonSender = AppContext.educareaDB.getGroupPersonById(groupPersonSenderId);
                groupPerson.userId = oldGroupPerson.userId;
                if (oldGroupPerson.name==null && oldGroupPerson.surname==null && oldGroupPerson.patronymic==null) {
                    emptyName = true;
                }
                if (userInGroup(userId, groupPeople) && personInGroup(groupPerson.groupPersonId, groupPeople)){
                    if (userIsModerator(userId, groupPeople) || emptyName){
                        if (checkNewGroupPesron(groupPerson)) {
                            if (lifeModeratorCount(groupPeople)==1 && groupPerson.moderator==0 && groupPerson.userId==userId && oldGroupPerson.moderator==1){
                                sendAnswer(NO_PERMISSION);
                                return;
                            }
                            if (groupPersonSender.moderator==0){
                                groupPerson.personType=oldGroupPerson.personType;
                                groupPerson.moderator=0;
                            }
                            Savepoint savepoint = null;
                            try {
                                savepoint = AppContext.educareaDB.setSavepoint("update_groupPerson");
                            } catch (Exception e) {
                                log.log(Level.WARNING, "error",e);
                                sendError();
                                return;
                            }
                            try {
                                if (oldGroupPerson.personType==1 && groupPerson.personType==0){
                                    ArrayList<Timetable> timetables = AppContext.educareaDB.getTimetableByPersonId(groupPerson.groupPersonId);
                                    for (int i = 0; i < timetables.size(); i++) {
                                        Timetable timetable = timetables.get(i);
                                        if (timetable.groupPersonId == groupPerson.groupPersonId) {
                                            timetable.groupPersonId = 0;
                                            AppContext.educareaDB.updateTimetable(timetable.timetableId, timetable);
                                        }
                                    }
                                }
                                AppContext.educareaDB.updateGroupPerson(groupPerson.groupPersonId, groupPerson);
                                AppContext.educareaDB.commit();
                                sendAnswer(UPDATE_INFO);
                                log.info("update group person "+webSocket.getRemoteSocketAddress().getAddress().getHostName());
                            }catch (Exception e){
                                log.log(Level.WARNING, "error",e);
                                try {
                                    AppContext.educareaDB.rollback(savepoint);
                                }catch (Exception ex){
                                    log.log(Level.WARNING, "error",ex);
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
                                log.log(Level.WARNING, "error",e);
                                sendError();
                                return;
                            }
                            try {
                                AppContext.educareaDB.updateGroupPerson(groupPerson.groupPersonId, groupPerson);
                                AppContext.educareaDB.deleteGroupPersonCodeByPersonId(groupPerson.groupPersonId);
                                AppContext.educareaDB.commit();
                                sendAnswer(UPDATE_INFO);
                            } catch (Exception e) {
                                log.log(Level.WARNING, "error",e);
                                try {
                                    AppContext.educareaDB.rollback(savepoint);
                                } catch (Exception ex) {
                                    log.log(Level.WARNING, "error",ex);
                                }
                                sendError();
                            }
                        } else sendAnswer(NO_PERMISSION);
                    } else sendAnswer(NO_PERMISSION);
                }catch (Exception e){
                    log.log(Level.WARNING, "error",e);
                    sendError();
                }
            }
        }
    }

    private void timetableEdit(Timetable timetable) throws Exception{
        log.info("timetable edit"+webSocket.getRemoteSocketAddress().getAddress().getHostName());
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock){
                if (checkTimetable(timetable)){
                    int userPersonId = LogicUtils.getGroupPersonIdByUserId(userId, timetable.groupId);
                    if (userPersonId!=0){ //if user have account in group
                        GroupPerson person = AppContext.educareaDB.getGroupPersonById(userPersonId);
                        if (timetable.groupPersonId!=0){ //if timetable has a teacher
                            GroupPerson teacher = AppContext.educareaDB.getGroupPersonById(timetable.groupPersonId);
                            if (timetable.groupId!=teacher.groupId || teacher.personType==0){ //if teacher is in a different group or not teacher
                                sendAnswer(NO_PERMISSION);
                                return;
                            }
                            if (person.moderator!=1 && person.groupPersonId!=timetable.groupPersonId){//if not moderator and timetable has different teacher (not user person id)
                                sendAnswer(NO_PERMISSION);
                                return;
                            }
                        }else {
                            if (person.moderator!=1){
                                sendAnswer(NO_PERMISSION);
                                return;
                            }
                        }
                        Savepoint savepoint = null;
                        try {
                            savepoint = AppContext.educareaDB.setSavepoint("timetableEdit");
                        } catch (Exception e) {
                            log.log(Level.WARNING, "error",e);
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
                            log.log(Level.WARNING, "error",e);
                            try {
                                AppContext.educareaDB.rollback(savepoint);
                            } catch (Exception ex) {
                                log.log(Level.WARNING, "error",ex);
                            }
                            sendError();
                        }
                    }else sendAnswer(NO_PERMISSION);
                }else sendError();
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
                int userPersonId = LogicUtils.getGroupPersonIdByUserId(userId, timetable.groupId);
                GroupPerson person = AppContext.educareaDB.getGroupPersonById(userPersonId);
                if (userPersonId!=0) {
                    if (person.moderator==1 || person.groupPersonId == timetable.groupPersonId) {
                        Savepoint savepoint = null;
                        try{
                            savepoint = AppContext.educareaDB.setSavepoint("deleteTimetable");
                        }catch (Exception e){
                            log.log(Level.WARNING, "error",e);
                            sendError();
                            return;
                        }
                        try {
                            AppContext.educareaDB.deleteTimetable(timetableId);
                            AppContext.educareaDB.commit();
                            sendAnswer(UPDATE_INFO);
                        }catch (Exception e){
                            log.log(Level.WARNING, "error",e);
                            try {
                                AppContext.educareaDB.rollback(savepoint);
                            } catch (Exception ex) {
                                log.log(Level.WARNING, "error",ex);
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
                            log.log(Level.WARNING, "error",e);
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
                            AppContext.educareaDB.deleteGroupPersonCodeByPersonId(groupPersonId);
                            AppContext.educareaDB.deleteChannelMessageByPersonId(groupPersonId);
                            AppContext.educareaDB.deleteGroupPersonById(groupPersonId);
                            AppContext.educareaDB.commit();
                            sendAnswer(UPDATE_INFO);
                        } catch (Exception e) {
                            log.log(Level.WARNING, "error",e);
                            try {
                                AppContext.educareaDB.rollback(savepoint);
                            } catch (Exception ex) {
                                log.log(Level.WARNING, "error",ex);
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
                        LogicUtils utils = new LogicUtils();
                        if (utils.deleteGroup(groupId)){
                            sendAnswer(DELETE_GROUP);
                        }else {
                            sendAnswer(ERROR);
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

    private void sendChannelMessage(TransferRequestAnswer transferRequestAnswer) throws Exception{
        int groupId = Integer.parseInt(transferRequestAnswer.extra);
        String message = transferRequestAnswer.extraArr[0];
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
            if (userInGroup(userId, groupPeople)){
                GroupPerson groupPerson = getPersonByUserId(userId,groupPeople);
                if (groupPerson.personType==0 && groupPerson.moderator==0){
                    sendAnswer(NO_PERMISSION);
                    return;
                }
                ChannelMessage channelMessage = new ChannelMessage(groupPerson.groupPersonId,message);
                synchronized (lock) {
                    Savepoint savepoint = null;
                    savepoint = AppContext.educareaDB.setSavepoint("sendChannelMessage");
                    try {
                        AppContext.educareaDB.insertChannelMessage(channelMessage);
                        AppContext.educareaDB.commit();
                    } catch (Exception e) {
                        log.log(Level.WARNING, "error", e);
                        AppContext.educareaDB.rollback(savepoint);
                        sendError();
                        return;
                    }
                }
                sendToAllGroupUsers(new TransferRequestAnswer(NEW_CHANNEL_MESSAGE),groupId);
                CloudMessageSender.channelMessage(groupId,groupPerson.groupPersonId,message);
            }
        }
    }

    private void getChannelMessage(TransferRequestAnswer transferRequestAnswer) throws Exception{
        int groupId = Integer.parseInt(transferRequestAnswer.extra);
        int count = Integer.parseInt(transferRequestAnswer.extraArr[0]);
        Date date = null;
        if (transferRequestAnswer.extraArr.length>1) {
            if (transferRequestAnswer.extraArr[1] != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = format.parse(transferRequestAnswer.extraArr[1]);
            }
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
            if (userInGroup(userId, groupPeople)){
                GroupPerson groupPerson = getPersonByUserId(userId,groupPeople);
                ArrayList<ChannelMessage> messages = null;
                if (groupPerson.personType==1 && groupPerson.moderator==0){
                    if (date==null){
                        messages = AppContext.educareaDB.selectChannelMessageByPersonId(groupPerson.groupPersonId,count);
                    }else {
                        messages = AppContext.educareaDB.selectChannelMessageByPersonId(groupPerson.groupPersonId,date,count);
                    }
                }else {
                    messages = new ArrayList<>();
                    for (int i = 0; i < groupPeople.size(); i++) {
                        if (date==null) {
                            ArrayList<ChannelMessage> personMessages = AppContext.educareaDB.selectChannelMessageByPersonId(groupPeople.get(i).groupPersonId,count);
                            messages.addAll(personMessages);
                        }else {
                            ArrayList<ChannelMessage> personMessages = AppContext.educareaDB.selectChannelMessageByPersonId(groupPeople.get(i).groupPersonId,date,count);
                            messages.addAll(personMessages);
                        }
                    }
                }
                Collections.sort(messages);
                if (messages.size()>count){
                    ArrayList<ChannelMessage> temp = new ArrayList<>(messages);
                    messages = new ArrayList<>();
                    for (int i = count; i < temp.size(); i++) {
                        messages.add(temp.get(i));
                    }
                }
                sendTransfers(new ChannelMessages(messages));
            }
        }
    }

    private void generatePersonCode(TransferRequestAnswer  transferRequestAnswer) throws Exception{
        int personId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock) {
                GroupPerson person = AppContext.educareaDB.getGroupPersonById(personId);
                int userPersonId = LogicUtils.getGroupPersonIdByUserId(userId, person.groupId);
                if (userPersonId!=0) {
                    if (LogicUtils.groupPersonIsModerator(userPersonId)) {
                        if (person.userId!=0){ //user already bind to this person
                            sendError();
                            return;
                        }
                        GroupPersonCode oldCode = AppContext.educareaDB.getGroupPersonCodeByPersonId(personId);
                        if (oldCode==null){
                            int startCodeSize = PERSON_CODE_START_SIZE;
                            String newCode = null;
                            while (true){
                                newCode = generateSafeToken(startCodeSize);
                                GroupPersonCode newPCode = AppContext.educareaDB.getGroupPersonCodeByCode(newCode);
                                if (newPCode==null) break;
                                startCodeSize++;
                            }
                            GroupPersonCode insertGroupCode = new GroupPersonCode(personId, newCode);
                            AppContext.educareaDB.insertGroupPersonCode(insertGroupCode);
                            AppContext.educareaDB.commit();
                            sendTransfers(insertGroupCode);
                        }else sendError();
                    }else sendAnswer(NO_PERMISSION);
                }else sendAnswer(NO_PERMISSION);
            }
        }
    }

    private void sendStudentChatMessage(StudentsChatMessage message) throws Exception{
        if (message.text == null){
            sendError();
            return;
        }
        if (message.text.equals("")){
            sendError();
            return;
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            Integer groupPersonId = LogicUtils.getGroupPersonIdByUserId(userId, message.groupId);
            if (groupPersonId == null){
                sendAnswer(NO_PERMISSION);
                return;
            }
            GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
            if (groupPerson.personType==0){
                StudentsChatMessage chatMessage = new StudentsChatMessage();
                chatMessage.groupId = groupPerson.groupId;
                chatMessage.groupPersonId = groupPersonId;
                chatMessage.text = message.text;
                synchronized (lock){
                    Savepoint savepoint = null;
                    savepoint = AppContext.educareaDB.setSavepoint("sendStudentChatMessage");
                    try{
                        AppContext.educareaDB.insertStudentChatMessage(chatMessage);
                        AppContext.educareaDB.commit();
                    }catch (Exception e){
                        log.log(Level.WARNING, "can't insert new studentMessage in db", e);
                        AppContext.educareaDB.rollback(savepoint);
                        sendError();
                        return;
                    }
                }
                LogicUtils.sendToAllStudentGroupUser(new TransferRequestAnswer(NEW_STUDENT_MESSAGE), groupPerson.groupId);
                CloudMessageSender.studentMessage(chatMessage);

            }else {
                sendAnswer(NO_PERMISSION);
                return;
            }
        }
    }

    private void getStudentChatMessage(TransferRequestAnswer transferRequestAnswer) throws Exception{
        int groupId = Integer.parseInt(transferRequestAnswer.extra);
        int count = Integer.parseInt(transferRequestAnswer.extraArr[0]);
        Integer lastId = null;
        if (transferRequestAnswer.extraArr.length>1) {
            if (transferRequestAnswer.extraArr[1] != null) {
                lastId = Integer.parseInt(transferRequestAnswer.extraArr[1]);
            }
        }
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            Integer groupPersonId = LogicUtils.getGroupPersonIdByUserId(userId,groupId);
            if (groupPersonId==null){
                sendAnswer(NO_PERMISSION);
                return;
            }
            GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(groupPersonId);
            if (groupPerson.personType==0){
                ArrayList<StudentsChatMessage> messages = null;
                if (lastId==null){
                    messages = AppContext.educareaDB.selectStudentsChatMessage(groupId, count);
                }else {
                    messages = AppContext.educareaDB.selectStudentsChatMessage(groupId, count, lastId);
                }
                Collections.sort(messages);
                sendTransfers(new StudentsChatMessages(messages));
            }else {
                sendAnswer(NO_PERMISSION);
                return;
            }
        }
    }

    private void deletePersonCode(TransferRequestAnswer transferRequestAnswer) throws Exception{
        int personId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            GroupPerson person = AppContext.educareaDB.getGroupPersonById(personId);
            int userPersonId = LogicUtils.getGroupPersonIdByUserId(userId, person.groupId);
            if (userPersonId!=0){
                if (LogicUtils.groupPersonIsModerator(userPersonId)){
                    AppContext.educareaDB.deleteGroupPersonCodeByPersonId(personId);
                    AppContext.educareaDB.commit();
                    sendAnswer(UPDATE_INFO);
                }else sendAnswer(NO_PERMISSION);
            }else sendAnswer(NO_PERMISSION);
        }
    }

    private void getPersonCode(TransferRequestAnswer transferRequestAnswer) throws Exception{
        int personId = Integer.parseInt(transferRequestAnswer.extra);
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            GroupPerson person = AppContext.educareaDB.getGroupPersonById(personId);
            int userPersonId = LogicUtils.getGroupPersonIdByUserId(userId, person.groupId);
            if (userPersonId!=0){
                if (LogicUtils.groupPersonIsModerator(userPersonId)){
                    if (person.userId!=0){
                        sendAnswer(USER_ALREADY_BIND);
                    }else {
                        GroupPersonCode code = AppContext.educareaDB.getGroupPersonCodeByPersonId(personId);
                        if (code==null){
                            sendAnswer(NO_PERSON_CODE);
                        }else {
                            sendTransfers(code);
                        }
                    }
                }else sendAnswer(NO_PERMISSION);
            }else sendAnswer(NO_PERMISSION);
        }
    }

    private void inviteByPersonCode(TransferRequestAnswer transferRequestAnswer) throws Exception{
        String textCode = transferRequestAnswer.extra;
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            synchronized (lock){
                GroupPersonCode personCode = AppContext.educareaDB.getGroupPersonCodeByCode(textCode);
                if (personCode==null){
                    sendAnswer(PERSON_CODE_NOT_FOUND);
                }else {
                    GroupPerson groupPerson = AppContext.educareaDB.getGroupPersonById(personCode.groupPersonId);
                    groupPerson.userId = userId;
                    Savepoint savepoint = null;
                    try {
                        savepoint = AppContext.educareaDB.setSavepoint("inviteByPersonCode");
                    }catch (Exception e){
                        log.log(Level.WARNING, "can't create savepoint", e);
                        sendError();
                        return;
                    }
                    try {
                        AppContext.educareaDB.updateGroupPerson(groupPerson.groupPersonId, groupPerson);
                        AppContext.educareaDB.deleteGroupPersonCodeByPersonId(groupPerson.groupPersonId);
                        AppContext.educareaDB.commit();
                        sendAnswer(INVITE_SUCCESS);
                    }catch (Exception e){
                        try {
                            AppContext.educareaDB.rollback(savepoint);
                        } catch (Exception ex) {
                            log.log(Level.WARNING, "can't rollback in database");
                        }
                        sendError();
                    }
                }
            }
        }
    }

    private void logout(){
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            Savepoint savepoint = null;
            try {
                savepoint = AppContext.educareaDB.setSavepoint("logout");
            }catch (Exception e){
                log.log(Level.WARNING, "can't create savepoint", e);
                sendError();
                return;
            }
            try{
                AppContext.educareaDB.deleteTokenByToken(clientInfo.getToken());
                AppContext.educareaDB.commit();
                clientInfo.setId(0);
                clientInfo.setLogin(null);
                clientInfo.setToken(null);
                clientInfo.setCloudToken(null);
                try {
                    sendAnswer(LOGOUT);
                }catch (Exception ex){}
            }catch (Exception e){
                log.log(Level.WARNING, "can't logout",e);
                try {
                    AppContext.educareaDB.rollback(savepoint);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "can't rollback in database", ex);
                }
                sendError();
            }
        }else sendError();
    }

    private void logoutOtherSession(){
        ClientInfo clientInfo = AppContext.appWebSocket.getClientInfo(webSocket);
        int userId = checkAuthorizationGetUserId(clientInfo);
        if (userId!=0){
            Savepoint savepoint = null;
            try {
                savepoint = AppContext.educareaDB.setSavepoint("logout");
            }catch (Exception e){
                log.log(Level.WARNING, "can't create savepoint", e);
                sendError();
                return;
            }
            try{
                AppContext.educareaDB.deleteTokenExceptOne(userId, clientInfo.getToken());
                AppContext.educareaDB.commit();
                sendAnswer(UPDATE_INFO);
            }catch (Exception e){
                log.log(Level.WARNING, "can't logout other session",e);
                try {
                    AppContext.educareaDB.rollback(savepoint);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "can't rollback in database", ex);
                }
                sendError();
            }
        }else sendError();
    }

    private void sendToAllGroupUsers(Transfers transfers, int groupId){
        ArrayList<GroupPerson> groupPeople = null;
        try {
            groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
        }catch (Exception e){
            log.log(Level.WARNING, "error",e);
            return;
        }
        ArrayList<Integer> usersId = new ArrayList<>();
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPeople.get(i).userId!=0){
                usersId.add(groupPeople.get(i).userId);
            }
        }
        HashMap<WebSocket, ClientInfo> map = new HashMap<>(AppContext.appWebSocket.getClients());
        for(Map.Entry<WebSocket, ClientInfo> entry: map.entrySet()){
            try {
                WebSocket webSocket = entry.getKey();
                ClientInfo clientInfo = entry.getValue();
                if (usersId.contains(clientInfo.getId())) {
                    String out = objToJson(transfers);
                    webSocket.send(out);
                }
            }catch (Exception e){
                log.log(Level.WARNING, "error",e);
            }
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

    private GroupPerson getPersonByUserId(int userId, ArrayList<GroupPerson> groupPeople){
        for (int i = 0; i < groupPeople.size(); i++) {
            if (userId == groupPeople.get(i).userId){
                return groupPeople.get(i);
            }
        }
        return null;
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
                AppContext.educareaDB.deleteOldTokens(userId,LIVE_TOKEN_COUNT);
                if (cloudToken != null) {
                    AppContext.educareaDB.updateCloudToken(token, cloudToken);
                }
                if (user == null) {
                    throw new Exception("user is empty");
                } else {
                    clientInfo.setId(user.iduser);
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

    private void getPlatfrormVersion(TransferRequestAnswer transferRequestAnswer){
        String platformName = transferRequestAnswer.extra;
        VersionList list = AppContext.getVersionList();
        VersionInfo info = null;
        for (int i = 0; i < list.versionInfos.size(); i++) {
            if (list.versionInfos.get(i).platformName.equals(platformName)){
                info = list.versionInfos.get(i);
            }
        }
        if (info!=null){
            sendTransfers(info);
        }else sendError();
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
