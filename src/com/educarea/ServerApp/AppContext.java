package com.educarea.ServerApp;

import DBUtils.DBWorker;
import DBUtils.EducareaDBWorker;

import java.sql.SQLException;

public class AppContext {

    public static final String propPath = "config.properties";

    public static EducareaDB educareaDB = null;
    public static AppWebSocket appWebSocket = null;
    public static AppLogger log;

    public static void appInit(){
        PropLoader propLoader = new PropLoader(propPath);
        propLoader.load();
        log = new EducLogger(propLoader.getLogSetting());
        if(!((EducLogger) log).init()){
            System.err.println("can't init logger");
        }
        DBWorker.init(propLoader.getHost(),propLoader.getLogin(),propLoader.getPassword());
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
