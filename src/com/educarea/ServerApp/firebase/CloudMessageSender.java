package com.educarea.ServerApp.firebase;

import com.educarea.ServerApp.AppContext;
import com.educarea.ServerApp.EducLogger;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import transfers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudMessageSender implements CloudMessageType{

    public static final int MAX_MESSAGE_SIZE = 500;

    public static void channelMessage(int groupId, int groupPersonId, String message){
        new Thread(() -> {
            Logger log = Logger.getLogger(EducLogger.class.getName());
            try {
                String who = "";
                Group group = AppContext.educareaDB.getGroupById(groupId);
                GroupPerson authorMessage = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                if (authorMessage!=null){
                    if (authorMessage.surname!=null){
                        who+=authorMessage.surname+" ";
                    }
                    if (authorMessage.name!=null){
                        who+=authorMessage.name+" ";
                    }
                    if (authorMessage.patronymic!=null){
                        who+=authorMessage.patronymic;
                    }
                }
                GroupPersons groupPersons = new GroupPersons(AppContext.educareaDB.getGroupPersonsByGroupId(groupId));
                groupPersons.persons.removeIf(current -> current.personType == 1 || current.groupPersonId == groupPersonId);
                List<String> userCloudTokens = new ArrayList<>();
                for (int j = 0; j < groupPersons.persons.size(); j++) {
                    ArrayList<UserTokens> tokens = AppContext.educareaDB.getUserTokensByUserId(groupPersons.persons.get(j).userId);
                    for (int k = 0; k < tokens.size(); k++) {
                        if (tokens.get(k).cloudToken != null) {
                            userCloudTokens.add(tokens.get(k).cloudToken);
                        }
                    }
                }
                if (userCloudTokens.size()==0) return;
                String sendMessage = message;
                if (sendMessage.length() > MAX_MESSAGE_SIZE) {
                    sendMessage = sendMessage.substring(0, MAX_MESSAGE_SIZE);
                }

                MulticastMessage firebaseMessage = MulticastMessage.builder()
                        .putData("type", channel_message)
                        .putData("group_id",String.valueOf(group.groupId))
                        .putData("group", group.name)
                        .putData("who",who)
                        .putData("message",sendMessage)
                        .addAllTokens(userCloudTokens)
                        .build();
                FirebaseMessaging.getInstance().sendMulticast(firebaseMessage);
            } catch (Exception e) {
                log.log(Level.WARNING, "can't send firebase message", e);
            }
        }).start();
    }

    public static void studentMessage(StudentsChatMessage message){
        new Thread(() ->{
            Logger log = Logger.getLogger(EducLogger.class.getName());
            try{
                String who = "";
                Group group = AppContext.educareaDB.getGroupById(message.groupId);
                GroupPerson authorMessage = AppContext.educareaDB.getGroupPersonById(message.groupPersonId);
                if (authorMessage!=null){
                    if (authorMessage.surname!=null){
                        who+=authorMessage.surname+" ";
                    }
                    if (authorMessage.name!=null){
                        who+=authorMessage.name+" ";
                    }
                    if (authorMessage.patronymic!=null){
                        who+=authorMessage.patronymic;
                    }
                }
                GroupPersons groupPersons = new GroupPersons(AppContext.educareaDB.getGroupPersonsByGroupId(message.groupId));
                groupPersons.persons.removeIf(current -> current.personType == 1 || current.groupPersonId == message.groupPersonId);
                List<String> userCloudTokens = new ArrayList<>();
                for (int j = 0; j < groupPersons.persons.size(); j++) {
                    ArrayList<UserTokens> tokens = AppContext.educareaDB.getUserTokensByUserId(groupPersons.persons.get(j).userId);
                    for (int k = 0; k < tokens.size(); k++) {
                        if (tokens.get(k).cloudToken != null) {
                            userCloudTokens.add(tokens.get(k).cloudToken);
                        }
                    }
                }
                if (userCloudTokens.size()==0) return;
                String sendMessage = message.text;
                if (sendMessage.length() > MAX_MESSAGE_SIZE) {
                    sendMessage = sendMessage.substring(0, MAX_MESSAGE_SIZE);
                }

                MulticastMessage firebaseMessage = MulticastMessage.builder()
                        .putData("type", student_message)
                        .putData("group_id",String.valueOf(group.groupId))
                        .putData("group", group.name)
                        .putData("who",who)
                        .putData("message",sendMessage)
                        .addAllTokens(userCloudTokens)
                        .build();
                FirebaseMessaging.getInstance().sendMulticast(firebaseMessage);
            }catch (Exception e){
                log.log(Level.WARNING, "can't send firebase message (student message)", e);
            }
        }).start();
    }

    public static void appNews(final String message){
        new Thread(() -> {
            Logger log = Logger.getLogger(EducLogger.class.getName());
            try {
                String topic = app_news;
                Message firebaseMessage = Message.builder()
                        .putData("type", app_news)
                        .putData("message", message)
                        .setTopic(topic)
                        .build();
                FirebaseMessaging.getInstance().send(firebaseMessage);
            } catch (Exception e) {
                log.log(Level.WARNING, "can't send firebase app news message", e);
            }
        }).start();
    }

    public static void event(final Event event, int groupPersonId){
        new Thread(() -> {
            Logger log = Logger.getLogger(EducLogger.class.getName());
            try{
                GroupPerson person = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                Group group = AppContext.educareaDB.getGroupById(person.groupId);
                List<GroupPerson> personList = AppContext.educareaDB.getGroupPersonsByGroupId(group.groupId);
                personList.removeIf(current -> current.personType == 1 || current.groupPersonId == groupPersonId);
                List<String> userCloudTokens = new ArrayList<>();
                for (int j = 0; j < personList.size(); j++) {
                    ArrayList<UserTokens> tokens = AppContext.educareaDB.getUserTokensByUserId(personList.get(j).userId);
                    for (int k = 0; k < tokens.size(); k++) {
                        if (tokens.get(k).cloudToken != null) {
                            userCloudTokens.add(tokens.get(k).cloudToken);
                        }
                    }
                }
                if (userCloudTokens.size()==0) return;
                String eventName = event.title;
                if (eventName.length() > MAX_MESSAGE_SIZE){
                    eventName = eventName.substring(0, MAX_MESSAGE_SIZE);
                }
                String evendDate = String.valueOf(event.date.getTime());
                MulticastMessage firebaseMessage = MulticastMessage.builder()
                        .putData("type", CloudMessageType.event)
                        .putData("name", eventName)
                        .putData("date", evendDate)
                        .addAllTokens(userCloudTokens)
                        .build();
                FirebaseMessaging.getInstance().sendMulticast(firebaseMessage);
            }catch (Exception e){
                log.log(Level.WARNING, "can't send firebase event notify", e);
            }
        }).start();
    }

    public static void personalMessage(final PersonalMessage personalMessage){
        new Thread(() -> {
            Logger log = Logger.getLogger(EducLogger.class.getName());
            try{
                GroupPerson person = AppContext.educareaDB.getGroupPersonById(personalMessage.personFrom);
                GroupPerson personTo = AppContext.educareaDB.getGroupPersonById(personalMessage.personTo);
                Group group = AppContext.educareaDB.getGroupById(person.groupId);
                List<String> userCloudTokens = new ArrayList<>();
                List<UserTokens> tokens = AppContext.educareaDB.getUserTokensByUserId(personTo.userId);
                for (UserTokens userTokens: tokens){
                    if (userTokens.cloudToken != null){
                        userCloudTokens.add(userTokens.cloudToken);
                    }
                }
                if (userCloudTokens.size()==0) return;
                String sendMessage = personalMessage.text;
                if (sendMessage.length() > MAX_MESSAGE_SIZE) {
                    sendMessage = sendMessage.substring(0, MAX_MESSAGE_SIZE);
                }
                MulticastMessage message = MulticastMessage.builder()
                        .putData("type", CloudMessageType.channel_personal_message)
                        .putData("group_id",String.valueOf(group.groupId))
                        .putData("groupPersonId",String.valueOf(person.groupPersonId))
                        .putData("message",sendMessage)
                        .addAllTokens(userCloudTokens)
                        .build();
                FirebaseMessaging.getInstance().sendMulticast(message);
            }catch (Exception e){
                log.log(Level.WARNING, "can't send firebase personalMessage notify", e);
            }
        }).start();
    }
}
