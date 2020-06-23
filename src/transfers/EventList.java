package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class EventList implements Serializable, Transfers {
    public List<Event> events = null;

    public EventList() {
        events = new ArrayList<>();
    }

    public EventList(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
