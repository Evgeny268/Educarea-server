package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Event extends DateObject implements Serializable, Transfers{
    public Integer eventId = null;
    public Integer groupId = null;
    public Integer groupPersonId = null;
    public String title = null;
    public String text = null;

    public Event() {
    }

    public Event(Integer groupId, Integer groupPersonId, String title, Date date, String text) {
        super(date);
        this.groupId = groupId;
        this.groupPersonId = groupPersonId;
        this.title = title;
        this.text = text;
    }
}
