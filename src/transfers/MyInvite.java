package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class MyInvite implements Serializable, Transfers{
    public int inviteId;
    public String groupName;
    public GroupPerson groupPerson;

    public MyInvite() {
    }

    public MyInvite(String groupName, GroupPerson groupPerson) {
        this.groupName = groupName;
        this.groupPerson = groupPerson;
    }

    public MyInvite(int inviteId, String groupName, GroupPerson groupPerson) {
        this.inviteId = inviteId;
        this.groupName = groupName;
        this.groupPerson = groupPerson;
    }
}
