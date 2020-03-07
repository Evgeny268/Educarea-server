package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class VersionInfo implements Serializable, Transfers {

    public static final String PLATFORM_ANDROID = "ANDROID";
    public static final String PLATFORM_IOS = "IOS";
    public static final String PLATFORM_WINDOWS = "WINDOWS";
    public static final String PLATFORM_MAC = "MAC";
    public static final String PLATFORM_LINUX = "LINUX";

    public String platformName = null;
    public int lastVersionId = 0;
    public String lastVersionName = null;
    public int supportedVersionId = 0;
    public String updateLink = null;

    public VersionInfo() {
    }

    public VersionInfo(String platformName, int lastVersionId, String lastVersionName, int supportedVersionId, String updateLink) {
        this.platformName = platformName;
        this.lastVersionId = lastVersionId;
        this.lastVersionName = lastVersionName;
        this.supportedVersionId = supportedVersionId;
        this.updateLink = updateLink;
    }

}
