package com.educarea.ServerApp;


public class Main {

    public static void main(String[] args) {
	    AppContext.appInit();

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                AppContext.log.severe("-----------CLOSE PROGRAM!--------");
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
