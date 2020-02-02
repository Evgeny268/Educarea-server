package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class GroupPersonInvite  implements Serializable, Transfers {
    public int groupPersonInviteId = 0;
    public int groupPersonId = 0;
    public int userId = 0;

    public GroupPersonInvite() {
    }

    public GroupPersonInvite(int groupPersonId, int userId) {
        this.groupPersonId = groupPersonId;
        this.userId = userId;
    }

    public GroupPersonInvite(int groupPersonInviteId, int groupPersonId, int userId) {
        this.groupPersonInviteId = groupPersonInviteId;
        this.groupPersonId = groupPersonId;
        this.userId = userId;
    }
}
