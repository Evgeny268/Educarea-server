package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class PersonalMessage extends Message implements Serializable, Transfers, Comparable<PersonalMessage>{
    public Integer personalMessageId = null;
    public Integer personFrom = null;
    public Integer personTo = null;
    public Date readIn = null;

    public PersonalMessage() {
    }

    public PersonalMessage(Integer personTo, String text) {
        this.personTo = personTo;
        this.text = text;
    }

    @Override
    public int compareTo(PersonalMessage o) {
        return Integer.compare(this.personalMessageId, o.personalMessageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonalMessage)) return false;
        PersonalMessage that = (PersonalMessage) o;
        return personalMessageId.equals(that.personalMessageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalMessageId);
    }
}
