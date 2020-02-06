package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class ChannelMessage implements Serializable, Transfers, Comparable<ChannelMessage>{
    public int channelMessageId = 0;
    public int personFrom = 0;
    public String text = null;
    public Date date = null;
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
        if (this.date.equals(o.date)){
            if (this.channelMessageId<o.channelMessageId){
                return -1;
            }else return 1;
        }
        if (this.date.before(o.date)){
            return -1;
        }else return 1;
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
