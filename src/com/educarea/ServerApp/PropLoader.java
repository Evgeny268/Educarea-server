package com.educarea.ServerApp;

import java.io.FileInputStream;
import java.util.Properties;

public class PropLoader {
    private String propPath;

    private String host;
    private String login;
    private String password;
    private String logSetting;

    public PropLoader(String propPath) {
        this.propPath = propPath;
    }

    public void load(){
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(propPath);
            Properties property = new Properties();
            property.load(fis);
            host = property.getProperty("db.host");
            login = property.getProperty("db.login");
            password = property.getProperty("db.password");
            logSetting = property.getProperty("log.file");
        }catch (Exception e){
            System.err.println("Can't load property file");
            System.exit(-1);
        }
    }

    public String getHost() {
        return host;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getLogSetting() {
        return logSetting;
    }
}
