package com.educarea.ServerApp.Console;

import com.educarea.ServerApp.AppContext;
import com.educarea.ServerApp.EducLogger;
import com.educarea.ServerApp.PlatformVerLoader;
import com.educarea.ServerApp.firebase.CloudMessageSender;
import com.educarea.ServerApp.firebase.CloudMessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        }else if (command.startsWith("delete ")){
            command = command.replace("delete ", "");
            deleteParse(command);
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

    private static void deleteParse(String command){
        if (command.startsWith("group ")){
            command = command.replace("group ","");
            deleteGroupParse(command);
        }
    }

    private static void deleteGroupParse(String command){
        if (command.startsWith("-name ")){
            command = command.replace("-name ","");
            deleteGroupByName(command);
        }else if (command.startsWith("-id ")){
            command = command.replace("-id ","");
            int id = Integer.valueOf(command);
            deleteGroupById(id);
        }
    }

    private static void deleteGroupByName(String command){
        Logger log = Logger.getLogger(EducLogger.class.getName());
        try {
            Actions.deleteGroupByName(command);
            System.out.println("group was delete if exist");
        }catch (Exception e){
            e.printStackTrace();
            log.log(Level.WARNING, "can't manual delete group",e);
        }
    }

    private static void deleteGroupById(int groupId){
        Logger log = Logger.getLogger(EducLogger.class.getName());
        try {
            Actions.deleteGroupById(groupId);
            System.out.println("group was delete if exist");
        }catch (Exception e){
            e.printStackTrace();
            log.log(Level.WARNING, "can't manual delete group",e);
        }
    }
}
