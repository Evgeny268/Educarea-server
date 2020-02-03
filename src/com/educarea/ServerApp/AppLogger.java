package com.educarea.ServerApp;

public interface AppLogger {

    void info(String text);

    void info(String text, Throwable thrown);

    void warn(String text);

    void warn(String text, Throwable thrown);

    void severe(String text);

    void severe(String text, Throwable thrown);

}
