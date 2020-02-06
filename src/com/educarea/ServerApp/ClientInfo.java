package com.educarea.ServerApp;


public class ClientInfo {
    private int id = 0;
    private String login = null;
    private String token = null;
    private String cloudToken = null;
    private int authTryCount = 0;//TODO протестировать этот момент

    public ClientInfo() {
    }

    public ClientInfo(String login) {
        this.login = login;
    }

    public ClientInfo(String login, String token, String cloudToken) {
        this.login = login;
        this.token = token;
        this.cloudToken = cloudToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCloudToken() {
        return cloudToken;
    }

    public void setCloudToken(String cloudToken) {
        this.cloudToken = cloudToken;
    }

    public int getAuthTryCount() {
        return authTryCount;
    }

    public void setAuthTryCount(int authTryCount) {
        this.authTryCount = authTryCount;
    }
}
