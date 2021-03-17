package ugh.dl;

import java.io.Serializable;

public class MetadataGroupForDocStructType implements Serializable {

    private static final long serialVersionUID = -4571877810721395422L;

    private MetadataGroupType mdg = null;
    // Number of metadatatypes for this docStruct.
    private String num = null;
    // Just a filter to display only default metadata types.
    private boolean defaultdisplay = false;
    // Just a filter to avoid displaying invisible fields.
    private boolean invisible = false;

    /***********************************************************************
     * @param inType
     **********************************************************************/
    public MetadataGroupForDocStructType(MetadataGroupType group) {
        this.mdg = group;
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
    public MetadataGroupType getMetadataGroup() {
        return this.mdg;
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