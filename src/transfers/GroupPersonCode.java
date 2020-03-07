package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class GroupPersonCode implements Serializable, Transfers {
    public int groupPersonCodeId = 0;
    public int groupPersonId = 0;
    public String code = null;

    public GroupPersonCode() {
    }

    public GroupPersonCode(int groupPersonId, String code) {
        this.groupPersonId = groupPersonId;
        this.code = code;
    }

    public GroupPersonCode(int groupPersonCodeId, int groupPersonId, String code) {
        this.groupPersonCodeId = groupPersonCodeId;
        this.groupPersonId = groupPersonId;
        this.code = code;
    }
}
