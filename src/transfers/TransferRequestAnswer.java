package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class TransferRequestAnswer implements Transfers {
    public String request;
    public String extra;
    public String []extraArr;

    public TransferRequestAnswer() {
    }

    public TransferRequestAnswer(String request) {
        this.request = request;
    }

    public TransferRequestAnswer(String request, String extra) {
        this.request = request;
        this.extra = extra;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String[] getExtraArr() {
        return extraArr;
    }

    public void setExtraArr(String... extraArr) {
        this.extraArr = extraArr;
    }
}
