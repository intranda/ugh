package ugh;

import com.jcabi.manifests.Manifests;

/*
DO NOT CHANGE THIS FILE, IT'S GENERATED BY ANT!
 */
public class UghVersion {
    public static String buildDate = null;

    public static String buildVersion = null;
    public static String PROGRAMNAME = "Goobi";

    public static String getBUILDDATE() {
        if (buildDate == null) {
            readConfigurationFile();
        }
        return buildDate;
    }

    public static String getBUILDVERSION() {
        if (buildVersion == null) {
            readConfigurationFile();
        }

        return buildVersion;
    }

    private static void readConfigurationFile() {
        buildDate = Manifests.read("Implementation-Build-Date");
        buildVersion = Manifests.read("Implementation-Revision");
    }
}
