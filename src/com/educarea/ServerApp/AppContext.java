package com.educarea.ServerApp;

import DBUtils.DBWorker;
import DBUtils.EducareaDBWorker;
import transfers.VersionList;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppContext {

    public static final String propPath = "config.properties";

    public static EducareaDB educareaDB = null;
    public static AppWebSocket appWebSocket = null;
    private static VersionList versionList = null;
    private static SelfControl selfControl;

    public static final Object appConLock = new Object();

    public static void appInit(){
        PropLoader propLoader = new PropLoader(propPath);
        propLoader.load();
        EducLogger logger = new EducLogger(propLoader.getLogSetting());
        if(!((EducLogger) logger).init()){
            System.err.println("can't init logger");
            System.exit(-1);
        }
        Logger log = Logger.getLogger(EducLogger.class.getName());
        try {
            versionList = PlatformVerLoader.loadFromFile(propLoader.getPlatformsVersionFilePath());
        } catch (IOException e) {
            log.log(Level.SEVERE,"versionList can't be set",e);
            System.exit(-1);
        }
        DBWorker.init(propLoader.getHost(),propLoader.getLogin(),propLoader.getPassword());
        try {
            DBWorker.connect();
        } catch (SQLException e) {
            log.log(Level.SEVERE,"can't connect to database",e);
            System.exit(-1);
        }
        educareaDB = EducareaDBWorker.getInstance();
        appWebSocket = new AppWebSocket();
        selfControl = new SelfControl();
        selfControl.start();
        appWebSocket.start();
    }

    public static VersionList getVersionList() {
        synchronized (appConLock) {
            return versionList;
        }
    }
}
