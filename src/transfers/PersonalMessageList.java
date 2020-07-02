package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class PersonalMessageList implements Serializable, Transfers{
    public List<PersonalMessage> messages;

    public PersonalMessageList() {
        messages = new ArrayList<>();
    }

    public PersonalMessageList(List<PersonalMessage> messages) {
        this.messages = messages;
    }
}
