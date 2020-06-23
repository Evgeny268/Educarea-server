package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class DateObject implements Serializable, Transfers, Comparable<DateObject>{
    public Date date = null;

    public DateObject() {
    }

    public DateObject(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(DateObject o) {
        return this.date.compareTo(o.date);
    }
}
