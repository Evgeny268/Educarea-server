package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class ChannelMessage extends Message implements Serializable, Transfers, Comparable<ChannelMessage>{
    public int channelMessageId = 0;
    public int personFrom = 0;
    public Date readIn = null;

    public ChannelMessage() {
    }

    public ChannelMessage(int personFrom, String text) {
        this.personFrom = personFrom;
        this.text = text;
    }

    public ChannelMessage(int channelMessageId, int personFrom, String text, Date date, Date readIn) {
        this.channelMessageId = channelMessageId;
        this.personFrom = personFrom;
        this.text = text;
        this.date = date;
        this.readIn = readIn;
    }

    @Override
    public int compareTo(ChannelMessage o) {
        return Integer.compare(this.channelMessageId, o.channelMessageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelMessage)) return false;
        ChannelMessage that = (ChannelMessage) o;
        return channelMessageId == that.channelMessageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelMessageId);
    }
}
