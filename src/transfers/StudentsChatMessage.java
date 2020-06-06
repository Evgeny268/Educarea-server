package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class StudentsChatMessage extends Message implements Serializable,Transfers, Comparable<StudentsChatMessage> {

    public Integer studentsChatId = null;
    public Integer groupId = null;
    public Integer groupPersonId = null;
    public Date readIn = null;

    public StudentsChatMessage() {
    }

    public StudentsChatMessage(Integer groupId, String text) {
        this.groupId = groupId;
        this.text = text;
    }

    @Override
    public int compareTo(StudentsChatMessage o) {
        return Integer.compare(this.studentsChatId, o.studentsChatId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentsChatMessage)) return false;
        StudentsChatMessage that = (StudentsChatMessage) o;
        return Objects.equals(studentsChatId, that.studentsChatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentsChatId);
    }
}
