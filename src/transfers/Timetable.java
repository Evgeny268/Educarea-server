package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Timetable  implements Serializable, Transfers, Comparable<Timetable> {
    public int timetableId = 0; //NOT NULL
    public int groupId = 0; //NOT NULL
    public int groupPersonId = 0;
    public String objectName = null; //NOT NULL
    public String cabinet = null;
    public int parityweek; //NOT NULL
    public int day; //NOT NULL
    public String time = null;

    public Timetable() {
    }

    public Timetable(int groupId, String objectName, int parityweek, int day) {
        this.groupId = groupId;
        this.objectName = objectName;
        this.parityweek = parityweek;
        this.day = day;
    }

    public Timetable(int timetableId, int groupId, String objectName, int parityweek, int day) {
        this.timetableId = timetableId;
        this.groupId = groupId;
        this.objectName = objectName;
        this.parityweek = parityweek;
        this.day = day;
    }

    @Override
    public int compareTo(Timetable o) {
        if (this.time == null && o.time==null){
            return 0;
        }else if (this.time == null){
            return -1;
        }else if (o.time == null){
            return 1;
        }else {
            LocalTime thisLocalTime = LocalTime.parse(this.time);
            LocalTime otherLocalTime = LocalTime.parse(o.time);
            if (thisLocalTime.equals(otherLocalTime)){
                return 0;
            }else if (thisLocalTime.isBefore(otherLocalTime)){
                return -1;
            }else return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Timetable)) return false;
        Timetable timetable = (Timetable) o;
        return timetableId == timetable.timetableId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timetableId);
    }
}
