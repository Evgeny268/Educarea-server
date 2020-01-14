package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Registration implements Serializable, Transfers{
    public String login;
    public String password;

    public Registration() {
    }

    public Registration(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
