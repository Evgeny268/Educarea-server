package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class MyInvites implements Serializable, Transfers{
    public ArrayList<MyInvite> myInvites;

    public MyInvites() {
        myInvites = new ArrayList<>();
    }

    public MyInvites(ArrayList<MyInvite> myInvites) {
        this.myInvites = myInvites;
    }
}
