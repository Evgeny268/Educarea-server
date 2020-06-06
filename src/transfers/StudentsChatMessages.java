package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class StudentsChatMessages implements Serializable, Transfers{
    public ArrayList<StudentsChatMessage> studentsChatMessages;

    public StudentsChatMessages() {
        studentsChatMessages = new ArrayList<>();
    }

    public StudentsChatMessages(ArrayList<StudentsChatMessage> studentsChatMessages) {
        this.studentsChatMessages = studentsChatMessages;
    }
}
