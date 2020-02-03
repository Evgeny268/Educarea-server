package com.educarea.ServerApp;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class EducLogger implements AppLogger{
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

    @Override
    public void info(String text) {
        logger.log(Level.INFO,text);
        System.err.println(text);
    }

    @Override
    public void info(String text, Throwable thrown) {
        logger.log(Level.INFO,text, thrown);
        System.err.println(text);
        thrown.printStackTrace();
    }

    @Override
    public void warn(String text) {
        logger.log(Level.WARNING,text);
        System.err.println(text);
    }

    @Override
    public void warn(String text, Throwable thrown) {
        logger.log(Level.WARNING, text, thrown);
        System.err.println(text);
        thrown.printStackTrace();
    }

    @Override
    public void severe(String text) {
        logger.log(Level.SEVERE,text);
        System.err.println(text);
    }

    @Override
    public void severe(String text, Throwable thrown) {
        logger.log(Level.SEVERE, text, thrown);
        System.err.println(text);
        thrown.printStackTrace();
    }
}
