package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Authorization implements Serializable, Transfers {

    public String token = null;
    public String cloudToken = null;

    public Authorization() {
    }

    public Authorization(String token) {
        this.token = token;
    }

    public Authorization(String token, String cloudToken) {
        this.token = token;
        this.cloudToken = cloudToken;
    }
}
