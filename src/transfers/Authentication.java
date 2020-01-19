package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Authentication implements Serializable,Transfers {

    public String login = null;
    public String password = null;

    public Authentication() {
    }

    public Authentication(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
