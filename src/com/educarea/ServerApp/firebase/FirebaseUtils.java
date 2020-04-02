package com.educarea.ServerApp.firebase;

import com.educarea.ServerApp.EducLogger;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirebaseUtils {
    private static Logger log;
    public static void init(String filePathToJSONFirebase, String firebaseDatabaseUser){
        log = Logger.getLogger(EducLogger.class.getName());
        try {
            FileInputStream serviceAccount =
                    new FileInputStream(filePathToJSONFirebase);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(firebaseDatabaseUser)
                    .build();

            FirebaseApp.initializeApp(options);
        }catch (IOException e){
            log.log(Level.WARNING,"can't init firebase",e);
        }
    }

    public static void sendMessage(Message message){

        class MSender implements Runnable {

            Message message;

            public MSender(Message message) {
                this.message = message;
            }

            @Override
            public void run() {
                try {
                    FirebaseMessaging.getInstance().send(message);
                } catch (FirebaseMessagingException e) {
                    log.log(Level.WARNING,"can't send firebase message",e);
                }
            }
        }
        new Thread(new MSender(message)).start();
    }
}
