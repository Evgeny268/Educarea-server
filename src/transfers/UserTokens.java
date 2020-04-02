package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class UserTokens implements Serializable, Transfers {
    public int userTokensId = 0;
    public int userId = 0;
    public String authToken = null;
    public String cloudToken = null;
    public Date lastDate = null;
    public String ipAddress = null;

    public UserTokens() {
    }
}
