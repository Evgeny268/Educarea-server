package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class VersionList implements Serializable, Transfers {
    public ArrayList<VersionInfo> versionInfos = null;

    public VersionList() {
        versionInfos = new ArrayList<>();
    }

    public VersionList(ArrayList<VersionInfo> versionInfos) {
        this.versionInfos = versionInfos;
    }

    public VersionInfo getInfoByPlatformName(String name){
        for (int i = 0; i < versionInfos.size(); i++) {
            if (versionInfos.get(i).platformName.equals(name)){
                return versionInfos.get(i);
            }
        }
        return null;
    }
}
