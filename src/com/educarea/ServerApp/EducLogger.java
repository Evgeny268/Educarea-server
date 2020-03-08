package com.educarea.ServerApp;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class EducLogger{
    private Logger logger;
    private String pathToLoggerSetting;

    public EducLogger(String pathToLoggerSetting) {
        this.pathToLoggerSetting = pathToLoggerSetting;
    }

    public boolean init(){
        try(FileInputStream ins = new FileInputStream(pathToLoggerSetting)){
            LogManager.getLogManager().readConfiguration(ins);
            logger = Logger.getLogger(EducLogger.class.getName());
            logger.log(Level.INFO, "################### LOGGER START ######################");
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Logger getLogger() {
        return logger;
    }
}
