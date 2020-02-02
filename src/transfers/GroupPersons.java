package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class GroupPersons implements Serializable, Transfers{
    public ArrayList<GroupPerson> persons;

    public GroupPersons() {
        persons = new ArrayList<>();
    }

    public GroupPersons(ArrayList<GroupPerson> persons) {
        this.persons = persons;
    }
}
