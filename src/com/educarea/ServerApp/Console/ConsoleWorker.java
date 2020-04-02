package com.educarea.ServerApp.Console;

import com.educarea.ServerApp.AppContext;
import com.educarea.ServerApp.PlatformVerLoader;
import com.educarea.ServerApp.firebase.CloudMessageSender;
import com.educarea.ServerApp.firebase.CloudMessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleWorker {
    private static BufferedReader reader;

    public static void init(){
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public static void start(){
        while (true){
            try {
                String command = reader.readLine();
                parserCommand(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void parserCommand(String command){
        if (command.equals("stop")){
            appStop();
        }else if (command.equals("send update-info")){
            sendUpdateInfo();
            System.out.println("done");
        }else if (command.equals("update version")){
            updateVersion();
        }
        else{
            System.out.println("unknown command");
        }
    }

    private static void appStop(){
        System.exit(0);
    }

    private static void sendUpdateInfo(){
        CloudMessageSender.appNews(CloudMessageType.update_available);
    }

    private static void updateVersion(){
        try {
            AppContext.setVersionList(PlatformVerLoader.loadFromFile(AppContext.getPropLoader().getPlatformsVersionFilePath()));
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
