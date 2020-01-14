package com.educarea.ServerApp;

import DBUtils.DBWorker;
import DBUtils.EducareaDBWorker;

import java.sql.SQLException;

public class AppContext {

    public static EducareaDB educareaDB = null;
    public static AppWebSocket appWebSocket = null;

    public static void appInit(){
        DBWorker.init("jdbc:mysql://localhost:3306/educarea","testuser","testuser1234");
        try {
            DBWorker.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        educareaDB = EducareaDBWorker.getInstance();
        appWebSocket = new AppWebSocket();
        appWebSocket.start();
    }
}
