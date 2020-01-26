package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class GroupPerson implements Serializable, Transfers {
    public int groupPersonId = 0;
    public int groupId = 0;
    public int userId = 0;
    public int personType = 0;
    public int moderator = 0;
    public String surname = null;
    public String name = null;
    public String patronymic = null;

    public GroupPerson() {
    }

    public GroupPerson(int groupId, int moderator) {
        this.groupId = groupId;
        this.moderator = moderator;
    }

    public GroupPerson(int groupId, int userId, int moderator) {
        this.groupId = groupId;
        this.userId = userId;
        this.moderator = moderator;
    }

    public GroupPerson(int groupPersonId, int groupId, int userId, int personType, int moderator) {
        this.groupPersonId = groupPersonId;
        this.groupId = groupId;
        this.userId = userId;
        this.personType = personType;
        this.moderator = moderator;
    }


}
