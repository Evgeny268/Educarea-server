package com.educarea.ServerApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import transfers.Event;
import transfers.GroupPerson;
import transfers.Transfers;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogicUtils {
    public static Integer getGroupPersonIdByUserId(int userId, int groupId) throws Exception {
        Integer personId = null;
        ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPeople.get(i).userId==userId){
                personId = groupPeople.get(i).groupPersonId;
            }
        }
        return personId;
    }

    public static boolean groupPersonIsModerator(int personId) throws Exception{
        GroupPerson person = AppContext.educareaDB.getGroupPersonById(personId);
        if (person.moderator==1){
            return true;
        }else return false;
    }

    public boolean deleteGroup(int groupId){
        Logger log = Logger.getLogger(EducLogger.class.getName());
        ArrayList<GroupPerson> groupPeople = null;
        try {
            groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "can't get group persons",e);
            return false;
        }
        Savepoint savepoint = null;
        try {
            savepoint = AppContext.educareaDB.setSavepoint("deleteGroup");
        } catch (Exception e) {
            log.log(Level.WARNING, "error",e);
            return false;
        }
        try {
            for (int i = 0; i < groupPeople.size(); i++) {
                AppContext.educareaDB.deleteGroupPersonCodeByPersonId(groupPeople.get(i).groupPersonId);
                AppContext.educareaDB.deleteChannelMessageByPersonId(groupPeople.get(i).groupPersonId);
            }
            AppContext.educareaDB.deleteEventByGroupId(groupId);
            AppContext.educareaDB.deleteStudentsChatMessageByGroupId(groupId);
            AppContext.educareaDB.deleteTimetableByGroupId(groupId);
            AppContext.educareaDB.deleteGroupPersonByGroupId(groupId);
            AppContext.educareaDB.deleteGroupById(groupId);
            AppContext.educareaDB.commit();
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "error",e);
            try {
                AppContext.educareaDB.rollback(savepoint);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public static void sendToAllStudentGroupUser(Transfers transfers, int groupId){
        Logger log = Logger.getLogger(EducLogger.class.getName());
        ArrayList<GroupPerson> groupPeople = null;
        try {
            groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
        }catch (Exception e){
            log.log(Level.WARNING, "error",e);
            return;
        }
        ArrayList<Integer> usersId = new ArrayList<>();
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPeople.get(i).userId!=0 && groupPeople.get(i).personType==0){
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

    public static boolean addEvent(Event event, boolean update){
        Logger log = Logger.getLogger(EducLogger.class.getName());
        Savepoint savepoint = null;
        try {
            savepoint = AppContext.educareaDB.setSavepoint("addEvent");
        } catch (Exception e) {
            log.log(Level.WARNING, "error",e);
            return false;
        }
        try{
            if (update){
                AppContext.educareaDB.updateEvent(event);
            }else {
                AppContext.educareaDB.insertEvent(event);
            }
            AppContext.educareaDB.commit();
            return true;
        }catch (Exception e){
            log.log(Level.WARNING, "error",e);
            try {
                AppContext.educareaDB.rollback(savepoint);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public static boolean deleteEvent(int eventId){
        Logger log = Logger.getLogger(EducLogger.class.getName());
        Savepoint savepoint = null;
        try {
            savepoint = AppContext.educareaDB.setSavepoint("deleteEvent");
        } catch (Exception e) {
            log.log(Level.WARNING, "error",e);
            return false;
        }
        try{
            AppContext.educareaDB.deleteEventById(eventId);
            AppContext.educareaDB.commit();
            return true;
        } catch (Exception e){
            log.log(Level.WARNING, "error",e);
            try {
                AppContext.educareaDB.rollback(savepoint);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public static String objToJson(Object c) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter,c);
        return stringWriter.toString();
    }
}
