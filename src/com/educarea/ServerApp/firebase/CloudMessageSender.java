package com.educarea.ServerApp.firebase;

import com.educarea.ServerApp.AppContext;
import com.educarea.ServerApp.EducLogger;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import transfers.Group;
import transfers.GroupPerson;
import transfers.GroupPersons;
import transfers.UserTokens;

import java.util.ArrayList;
import java.util.Iterator;
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
                GroupPersons groupPersons = new GroupPersons(AppContext.educareaDB.getGroupPersonsByGroupId(groupId));
                GroupPerson authorMessage = AppContext.educareaDB.getGroupPersonById(groupPersonId);
                if (authorMessage!=null){
                    if (authorMessage.surname!=null){
                        who+=authorMessage.surname+" ";
                    }
                    if (authorMessage.name!=null){
                        who+=authorMessage.name=" ";
                    }
                    if (authorMessage.patronymic!=null){
                        who+=authorMessage.patronymic;
                    }
                }
                groupPersons.persons.removeIf(current -> current.personType == 1 || current.moderator == 1 || current.groupPersonId == groupPersonId);
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
}
