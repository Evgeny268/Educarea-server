package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Timetables implements Serializable, Transfers {
    public ArrayList<Timetable> timetables;

    public Timetables() {
        timetables = new ArrayList<>();
    }

    public Timetables(ArrayList<Timetable> timetables) {
        this.timetables = timetables;
    }
}
