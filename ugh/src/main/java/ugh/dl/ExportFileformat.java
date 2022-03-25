package ugh.dl;

/*******************************************************************************
 * ugh.dl / FileFormat.java
 * 
 * Copyright 2010 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

/*******************************************************************************
 * <p>
 * A Fileformat is an abstract description of a serialization of a complete <code>DigitalDocument</code>. A Fileformat may store or read a
 * <code>DigitalDocument</code> object to/from a file.
 * </p>
 * 
 * <p>
 * Depending on the implementation a Fileformat may store all or only part of the information.
 * </p>
 * 
 * <p>
 * Every fileformat may have methods to load, save or update a file. In an implementation not all methods need to be available. Certain Fileformats
 * are just readable; other may not be updateable.
 * </p>
 * 
 * <p>
 * <b>Differences between readable, updateable, writeable:</b><br>
 * <ul>
 * <li>readable: the fileformat can be read from a file
 * <li>updateable: after reading a fileformat, some information can be updated. The result can be written back (to the same file).
 * <li>writeable: a <code>DigitalDocument</code> can be written to a completly new file.
 * </ul>
 * </p>
 * 
 * <p>
 * Internally every fileformat has a DigitalDocument instance, which will be created while reading a file successfully. This instance can be obtained
 * by calling the GetDigitalDocument instance. Before writing a file, a DigitalDocument instance must be available.
 * </p>
 * 
 * @author Markus Enders
 * @author Robert Sehr
 * @version 2014-07-04
 * @see DigitalDocument
 ******************************************************************************/

public interface ExportFileformat extends Fileformat {

    public void setWriteLocal(boolean writeLocalFilegroup);

    public void setRightsOwner(String rightsOwner);

    public void setRightsOwnerLogo(String rightsOwnerLogo);

    public void setRightsOwnerSiteURL(String rightsOwnerSiteURL);

    public void setRightsOwnerContact(String rightsOwnerContact);

    public void setDigiprovPresentation(String digiprovPresentation);

    public void setDigiprovReference(String digiprovReference);

    public void setDigiprovPresentationAnchor(String digiprovPresentationAnchor);

    public void setDigiprovReferenceAnchor(String digiprovReferenceAnchor);

    public void setMptrUrl(String pointer);

    public void setMptrAnchorUrl(String pointer);

    public void setPurlUrl(String purlUrl);

    public void setContentIDs(String contentIDs);

    public void setMetsRightsSponsor(String metsRightsSponsor);

    public void setMetsRightsSponsorLogo(String metsRightsSponsorLogo);

    public void setMetsRightsSponsorSiteURL(String metsRightsSponsorSiteURL);

    public void setMetsRightsLicense(String metsRightsLicense);

    default void setCreateUUIDs(boolean createUUIDS) {
        // default implementation: ignore value
    }

    default void setIIIFUrl(String iifApiUrl) {
        // default implementation: ignore value, its only relevant in mets
    }

    default void setSruUrl(String sruUrl) {
        // default implementation: ignore value, its only relevant in mets
    }
}
