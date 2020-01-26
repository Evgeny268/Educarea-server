package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Group implements Serializable, Transfers{
    public int groupId;
    public String name;
    public Date createDate;

    public Group() {
    }

    public Group(int groupId, String name, Date createDate) {
        this.groupId = groupId;
        this.name = name;
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return groupId == group.groupId &&
                name.equals(group.name) &&
                Objects.equals(createDate, group.createDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }
}
