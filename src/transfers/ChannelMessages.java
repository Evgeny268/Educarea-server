package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class ChannelMessages implements Serializable, Transfers {
    public ArrayList<ChannelMessage> channelMessages;

    public ChannelMessages() {
        channelMessages = new ArrayList<>();
    }

    public ChannelMessages(ArrayList<ChannelMessage> channelMessages) {
        this.channelMessages = channelMessages;
    }
}
