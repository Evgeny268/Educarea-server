package com.educarea.ServerApp;


public class Main {

    public static void main(String[] args) {
	    AppContext.appInit();

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //TODO добавить сообщение в логер о завершении работы программы (когда появится логер)
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
