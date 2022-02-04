package ugh.dl;

import java.io.Serializable;

/***************************************************************************
 * <p>
 * Just a small class to store the MetadataType together with number (which depends on the DocStructType).
 * </p>
 **************************************************************************/

public class MetadataTypeForDocStructType implements Serializable {

    private static final long serialVersionUID = -5908952924188415337L;

    private MetadataType mdt = null;
    // Number of metadatatypes for this docStruct.
    private String num = null;
    // Just a filter to display only default metadata types.
    private boolean defaultdisplay = false;
    // Just a filter to avoid displaying invisible fields.
    private boolean invisible = false;

    /***********************************************************************
     * @param inType
     **********************************************************************/
    public MetadataTypeForDocStructType(MetadataType inType) {
        this.mdt = inType;
    }

    /***********************************************************************
     * @param in
     **********************************************************************/
    public void setNumber(String in) {
        this.num = in;
    }

    /***********************************************************************
     * @return
     **********************************************************************/
    public String getNumber() {
        return this.num;
    }

    /***********************************************************************
     * @return
     **********************************************************************/
    public MetadataType getMetadataType() {
        return this.mdt;
    }

    /***********************************************************************
     * @return the defaultdisplay
     **********************************************************************/
    public boolean isDefaultdisplay() {
        return this.defaultdisplay;
    }

    /***********************************************************************
     * Sets the DefaultDisplay variable for this DocStructType. Dosn't make any sense at all!
     * 
     * @param inDefaultdisplay the defaultdisplay to set
     **********************************************************************/
    public void setDefaultdisplay(boolean inDefaultdisplay) {
        this.defaultdisplay = inDefaultdisplay;
    }

    /***********************************************************************
     * @return the invisible
     **********************************************************************/
    public boolean isInvisible() {
        return this.invisible;
    }

    /***********************************************************************
     * @param invisible the invisible to set
     **********************************************************************/
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

}
