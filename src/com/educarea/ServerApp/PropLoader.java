package com.educarea.ServerApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropLoader {
    private String propPath;

    private String host;
    private String login;
    private String password;
    private String logSetting;
    private String platformsVersionFilePath;
    private String firebaseAccountKeyJSONPath;
    private String firebaseDatabaseUrl;

    public PropLoader(String propPath) {
        this.propPath = propPath;
    }

    public void load(){
        try(FileInputStream fis = new FileInputStream(propPath)) {
            Properties property = new Properties();
            property.load(fis);
            host = property.getProperty("db.host");
            login = property.getProperty("db.login");
            password = property.getProperty("db.password");
            logSetting = property.getProperty("log.file");
            platformsVersionFilePath = property.getProperty("platforms.version");
            firebaseAccountKeyJSONPath = property.getProperty("firebaseAccountKeyJSONPath");
            firebaseDatabaseUrl = property.getProperty("firebaseDatabaseUrl");
        }catch (Exception e){
            System.err.println("Can't load property file");
            restoreProperty();
            System.exit(-1);
        }
    }

    private void restoreProperty(){
        Properties props = new Properties();
        props.setProperty("db.host","");
        props.setProperty("db.login","");
        props.setProperty("db.password","");
        props.setProperty("log.file","");
        props.setProperty("platforms.version","");
        props.setProperty("firebaseAccountKeyJSONPath","");
        props.setProperty("firebaseDatabaseUrl","");
        try {
            props.store(new FileOutputStream(new File(propPath)), "create new prop file");
        }catch (Exception e){
            e.printStackTrace();
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

    public String getPlatformsVersionFilePath() {
        return platformsVersionFilePath;
    }

    public String getFirebaseAccountKeyJSONPath() {
        return firebaseAccountKeyJSONPath;
    }

    public String getFirebaseDatabaseUrl() {
        return firebaseDatabaseUrl;
    }
}
