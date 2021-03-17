package ugh.dl;

import java.util.Map;

public interface PrefsType {

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getName();

    /***************************************************************************
     * @return
     **************************************************************************/
    public Map<String, String> getAllLanguages();

    /***************************************************************************
     * <p>
     * Adds a name (in the given language) for this instance of MetadataType.
     * </p>
     * 
     * @param lang language code
     * @param value name of the metadata type in the given language
     * @return true, if successful
     **************************************************************************/
    public   void addLanguage(String theLanguage, String theValue);

    /***************************************************************************
     * <p>
     * Retrieves the name for a certain language.
     * </p>
     * 
     * @param lang language code
     * @return the translation of this MetadataType; or null, if it has no translation for this language.
     **************************************************************************/
    public  String getNameByLanguage(String lang);

}