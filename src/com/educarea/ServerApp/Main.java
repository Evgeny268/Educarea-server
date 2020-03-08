package com.educarea.ServerApp;


import java.util.logging.Logger;

public class Main {

    private static Logger log;

    public static void main(String[] args) {
	    AppContext.appInit();
        log = Logger.getLogger(EducLogger.class.getName());
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.severe("-----------CLOSE PROGRAM!--------");
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
