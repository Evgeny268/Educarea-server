package com.educarea.ServerApp;

import DBUtils.DBWorker;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelfControl extends Thread {

    private Logger log;

    public SelfControl() {
        super();
        log = Logger.getLogger(EducLogger.class.getName());
    }

    @Override
    public void run() {
        log.info("start self control");
        while (!isInterrupted()) {
            checkDB();
            try {
                TimeUnit.MINUTES.sleep(5);
            } catch (InterruptedException e) {

            }
        }
    }

    private void checkDB(){
        try(DBWorker.Builder builder = new DBWorker.Builder(false)
        .setSql("SELECT ? FROM educarea.user")
        .setParameters(String.valueOf(1))
        .setTypes("int")){

        }catch (Exception e){
            log.log(Level.SEVERE,"problem with database, try to reconnect",e);
            DBWorker.disconnect();
            try {
                DBWorker.connect();
            } catch (SQLException ex) {
                log.log(Level.SEVERE,"can't reconnect to database",ex);
            }
        }
    }
}
