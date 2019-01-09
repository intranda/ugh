package ugh;

import com.jcabi.manifests.Manifests;

/*
DO NOT CHANGE THIS FILE, IT'S GENERATED BY ANT!
 */
public class UghVersion {
    private static String BUILDDATE = null;

    private static String BUILDVERSION = null;
    public static String PROGRAMNAME = "Goobi";

    public static String getBUILDDATE() {
        if (BUILDDATE == null) {
            readConfigurationFile();
        }
        return BUILDDATE;
    }

    public static String getBUILDVERSION() {
        if (BUILDVERSION == null) {
            readConfigurationFile();
        }

        return BUILDVERSION;
    }

    private static void readConfigurationFile() {
        BUILDDATE= Manifests.read("Implementation-Build-Date");
        BUILDVERSION = Manifests.read("Implementation-Revision");

        //        @SuppressWarnings("rawtypes")
        //        Enumeration resEnum;
        //        InputStream is = null;
        //        BufferedReader bufferedReader = null;

        //        try {
        //            is = DocStruct.class.getResourceAsStream("/META-INF/MANIFEST.MF");
        //
        //            //            resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
        //            //            while (resEnum.hasMoreElements()) {
        //            //                try {
        //            //                    URL url = (URL) resEnum.nextElement();
        //            //                    InputStream is = url.openStream();
        //            //                    if (is != null) {
        //            bufferedReader = new BufferedReader(new InputStreamReader(is));
        //            String line = null;
        //            while ((line = bufferedReader.readLine()) != null) {
        //                if (line.startsWith("Implementation-Build-Date")) {
        //                    BUILDDATE = line.replace("Implementation-Build-Date: ", "");
        //                } else if (line.startsWith("Implementation-Version")) {
        //                    BUILDVERSION = line.replace("Implementation-Version: ", "ugh-3.0-");
        //                }
        //            }
        //        } catch (IOException e1) {
        //            // Silently ignore wrong manifests on classpath?
        //        }
        //        if (is != null) {
        //            try {
        //                is.close();
        //            } catch (IOException e) {
        //            }
        //        }
        //        if (bufferedReader != null) {
        //            try {
        //                bufferedReader.close();
        //            } catch (IOException e) {
        //            }
        //        }

    }
}