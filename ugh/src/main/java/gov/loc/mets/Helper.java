package gov.loc.mets;

/*******************************************************************************
 * gov.loc.mets / Helper.java
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
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlAnyURI;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.MdSecType.MdWrap;
import gov.loc.mets.MdSecType.MdWrap.XmlData;
import gov.loc.mets.MetsType.FileSec;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mets.MetsType.StructLink;
import gov.loc.mets.StructLinkType.SmLink;

/*******************************************************************************
 * <p>
 * Helper class to parse and create METS files more easily.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-10-21
 * @since Jan 2007
 * 
 *        CHANGELOG
 * 
 *        21.10.2009 --- Funk --- Refactored some conditionals.
 * 
 *        20.10.2009 --- Funk --- Added method for retrieving DivTypes by DivType ID --- Corrected some typos.
 * 
 ******************************************************************************/
public class Helper {
    /***************************************************************************
     * STATIC FINALS
     **************************************************************************/
    private static final String VERSION = "0.2-20091021";
    // Define.
    private static final int DIGIPROVMD = 1;
    private static final int RIGHTSMD = 2;
    private static final int TECHMD = 3;
    private static final int SOURCEMD = 4;
    /***************************************************************************
     * INSTANCE VARIABLES
     **************************************************************************/
    // The Mets wrapper element <mets>.
    private MetsType mets;

    /***************************************************************************
     * CONSTRUCTORS
     **************************************************************************/
    /***************************************************************************
     * <p>
     * Default Constructor.
     * </p>
     * 
     * @param inMets
     **************************************************************************/
    public Helper(MetsType inMets) {
        this.mets = inMets;
    }

    /***************************************************************************
     * WHAT THE OBJECT DOES
     **************************************************************************/
    /***************************************************************************
     * @param inDiv
     * @param inContent
     * @return
     **************************************************************************/
    public MdSecType addDmdSecType(DivType inDiv, String inContent) {
        LinkedList<String> l1 = null;
        MdSecType newDmdSecType = createDescriptiveMetadata(inContent);

        // Get ID of newDmdSecType.
        String newID = newDmdSecType.getID();
        // Add this id to the inDiv.
        @SuppressWarnings("unchecked")
        List<String> l = inDiv.getDMDID();
        if (l == null) {
            l1 = new LinkedList<>();
        } else {
            l1 = new LinkedList<>(l);
        }
        l1.add(newID);
        inDiv.setDMDID(l1);
        return newDmdSecType;
    }

    /***************************************************************************
     * <p>
     * Creates a new DmdSec object for a given DivType. Cause the content is transmitted as a byte-array, it will be regarded as binary content.
     * Therefore an &lt;binData&gt; element will be created.<br/>
     * 
     * The method will construct an internal unique identifier for the DmdSec automatically and will add this identifier to the &lt;div&gt;.
     * </p>
     * 
     * @param inDiv the &lt;div&gt; element, which will conntect
     * @param inContent a byte-array containing the binary content.
     * @return
     **************************************************************************/
    public MdSecType addDmdSecType(DivType inDiv, byte[] inContent) {
        LinkedList<String> l1 = null;
        MdSecType newDmdSecType = createDescriptiveMetadata(inContent);
        // Get ID of newDmdSecType.
        String newID = newDmdSecType.getID();
        // Add this id to the inDiv.
        @SuppressWarnings("unchecked")
        List<String> l = inDiv.getDMDID();
        if (l == null) {
            l1 = new LinkedList<>();
        } else {
            l1 = new LinkedList<>(l);
        }
        l1.add(newID);
        inDiv.setDMDID(l1);
        return newDmdSecType;
    }

    /***************************************************************************
     * <p>
     * Creates a new DMDSec with xml compliant content. Identifier for the DMDSec is created automatically.
     * </p>
     * 
     * @param mets
     * @param content content must be xml compliant
     * @return
     **************************************************************************/
    private MdSecType createDescriptiveMetadata(String content) {
        String dmdidString = createNewXMLID("dmdsec");
        // Create descriptive metadata section.
        MdSecType dmdSec = this.mets.addNewDmdSec();
        dmdSec.setID(dmdidString);
        // Check, if inContent is XML compliant or not create an xmlData
        // section, if it is xml compliant, otherwise a binData section.
        // Create a new <MdWrap> element.
        MdSecType.MdWrap mdwrap = dmdSec.addNewMdWrap();
        // Create new <xmlData> element. We have to create DOM node, to avoid
        // having a schema for the xml content; for xmlbeans a schema and the
        // derived classes are a must.
        MdSecType.MdWrap.XmlData xml = mdwrap.addNewXmlData();
        Node xmlnode = xml.getDomNode();
        Document domdocument = xmlnode.getOwnerDocument();
        Node contentnode = domdocument.createTextNode(content);
        xmlnode.appendChild(contentnode);

        return dmdSec;
    }

    /***************************************************************************
     * <p>
     * Creates a new DMDSec with binary content.
     * </p>
     * 
     * @param mets
     * @param content
     * @return
     **************************************************************************/
    private MdSecType createDescriptiveMetadata(byte[] content) {
        String dmdidString = createNewXMLID("dmdsec");
        // Create descriptive metadata section.
        MdSecType dmdSec = this.mets.addNewDmdSec();
        dmdSec.setID(dmdidString);
        // Check, if inContent is XML compliant or not create an xmlData
        // section, if it is xml compliant otherwise a binData section.
        // Create a new <MdWrap> element.
        MdSecType.MdWrap mdwrap = dmdSec.addNewMdWrap();
        mdwrap.setBinData(content);
        return dmdSec;
    }

    /***************************************************************************
     * @param content
     * @param mdTypeValue
     * @param mdPart
     * @return
     **************************************************************************/
    public MdSecType addAmdSecType(XmlObject content, String mdTypeValue, int mdPart) {
        MdSecType result = null;
        AmdSecType amdsec = this.mets.addNewAmdSec();
        // Create identifier for amdsec.
        String amdid = createNewXMLID("amdsec");
        amdsec.setID(amdid);
        // Create subsection and create id for the subsection.
        String id = null;
        if (mdPart == DIGIPROVMD) {
            result = amdsec.addNewDigiprovMD();
            id = createNewXMLID("digiprov");
        } else if (mdPart == RIGHTSMD) {
            result = amdsec.addNewRightsMD();
            id = createNewXMLID("rights");
        } else if (mdPart == TECHMD) {
            result = amdsec.addNewTechMD();
            id = createNewXMLID("tech");
        } else if (mdPart == SOURCEMD) {
            result = amdsec.addNewSourceMD();
            id = createNewXMLID("source");
        } else {
            // Unknown type, should really throw an exception here.
            return null;
        }
        // Create identifier for result.
        result.setID(id);
        MdWrap mdwrap = createMDWrap(result, mdTypeValue);
        // Add content.
        XmlData xmldata = mdwrap.addNewXmlData();
        xmldata.set(content);
        return result;
    }

    /***************************************************************************
     * @param content
     * @param mdTypeValue
     * @param mdPart
     * @return
     * @throws XmlException
     **************************************************************************/
    public MdSecType addAmdSecType(String content, String mdTypeValue, int mdPart) {
        MdSecType result = null;
        AmdSecType amdsec = this.mets.addNewAmdSec();
        // Create identifier for amdsec.
        String amdid = createNewXMLID("amdsec");
        amdsec.setID(amdid);
        // Create subsection and create id for the subsection.
        String id = null;
        if (mdPart == DIGIPROVMD) {
            result = amdsec.addNewDigiprovMD();
            id = createNewXMLID("digiprov");
        } else if (mdPart == RIGHTSMD) {
            result = amdsec.addNewRightsMD();
            id = createNewXMLID("rights");
        } else if (mdPart == TECHMD) {
            result = amdsec.addNewTechMD();
            id = createNewXMLID("tech");
        } else if (mdPart == SOURCEMD) {
            result = amdsec.addNewSourceMD();
            id = createNewXMLID("source");
        } else {
            // Unknown type, should really throw an exception here.
            return null;
        }
        // Create identifier for result.
        result.setID(id);
        MdWrap mdwrap = createMDWrap(result, mdTypeValue);
        // Add content.
        XmlData xmldata = mdwrap.addNewXmlData();
        // We have to create DOM node, to avoid having a schema for the xml
        // content; for xmlbeans a schema and the derived classes are a must.
        // Get DOM node.
        Node xmlnode = xmldata.getDomNode();
        Document domdocument = xmlnode.getOwnerDocument();
        Node contentnode = domdocument.createTextNode(content);
        xmlnode.appendChild(contentnode);
        return result;
    }

    /***************************************************************************
     * @param content
     * @param mdTypeValue
     * @param mdPart
     * @return
     **************************************************************************/
    public MdSecType addAmdSecType(byte[] content, String mdTypeValue, int mdPart) {
        MdSecType result = null;
        AmdSecType amdsec = this.mets.addNewAmdSec();
        // Ccreate identifier for amdsec.
        String amdid = createNewXMLID("amdsec");
        amdsec.setID(amdid);
        // Create subsection and create id for the subsection.
        String id = null;
        if (mdPart == DIGIPROVMD) {
            result = amdsec.addNewDigiprovMD();
            id = createNewXMLID("digiprov");
        } else if (mdPart == RIGHTSMD) {
            result = amdsec.addNewRightsMD();
            id = createNewXMLID("rights");
        } else if (mdPart == TECHMD) {
            result = amdsec.addNewTechMD();
            id = createNewXMLID("tech");
        } else if (mdPart == SOURCEMD) {
            result = amdsec.addNewSourceMD();
            id = createNewXMLID("source");
        } else {
            // Unknown type, should really throw an exception here.
            return null;
        }
        // Create identifier for result.
        result.setID(id);
        MdWrap mdwrap = createMDWrap(result, mdTypeValue);
        // Add content.
        mdwrap.setBinData(content);
        return result;
    }

    /***************************************************************************
     * <p>
     * Retrieves the appropriate descriptive metadata section of a given type. The type is stored in the TYPE-attribute. The comparison is case
     * sensitive.
     * </p>
     * 
     * @param metsDoc the MetsDocument object
     * @param inDiv the DivType object
     * @param type the type as a string; type is case sensitive.
     * @return the MdSecType object, which matches . If several objects with the same type are available, the first one is returned.
     **************************************************************************/
    public MdSecType getMetadataSectionByType(DivType inDiv, String type) {
        // Get all IDs for administrative metadata sections.
        @SuppressWarnings("unchecked")
        List<String> allmdids = inDiv.getADMID();
        if (allmdids == null) {
            return null;
        }
        for (String admid : allmdids) {
            // Get section by ID verify the type of metadata.
            MdSecType dmdSec = getDmdSecByID(admid);
            // Get <mdWrap> element.
            MdSecType.MdWrap mdwrap = dmdSec.getMdWrap();
            if (mdwrap != null) {
                MdSecType.MdWrap.MDTYPE.Enum e = mdwrap.getMDTYPE();
                // Compare value of MDTYPE here.
                if (e.equals(MdSecType.MdWrap.MDTYPE.OTHER)) {
                    String othermdtype = mdwrap.getOTHERMDTYPE();
                    // If it's PREMIS metadata.
                    if (othermdtype.equals(type)) {
                        // We found the LMER metadata type; create a PREMIS
                        // metadata for it.
                        return dmdSec;
                    }
                }
            }
        }
        return null;
    }

    /***************************************************************************
     * <p>
     * Retrieves a &lt;amdSec&gt; with a given ID; this ID is used to link from the appropriate <div> element to the attached metadata.
     * </p>
     * 
     * @param id xml-id
     * @return
     **************************************************************************/
    public AmdSecType getAmdSecByID(String id) {
        Object o = getAmdSecByID(id, false);
        if (o != null) {
            return (AmdSecType) o;
        }
        return null;
    }

    /***************************************************************************
     * <p>
     * Gets the descriptive metadata section defined by the ID.
     * </p>
     * 
     * @param inDoc the MetsDocument which should contain the DmdSec element
     * @param id unique ID of the descriptive metadata section
     * @return
     **************************************************************************/
    public MdSecType getDmdSecByID(String id) {
        // Get all descriptive Metadata sections as an array.
        MdSecType[] mdsections = this.mets.getDmdSecArray();

        if (mdsections != null) {
            for (MdSecType mdsec : mdsections) {
                String sectionid = mdsec.getID();
                if (sectionid.equals(id)) { // compare the id and the given id
                    return mdsec;
                }
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Retrieves the content of a descriptive/administrative metadata section; if the metadata is referenced by a <mdRef> element, the data is loaded
     * via http (other protocols are currently unsupported.
     * </p>
     * 
     * @param the appropriate MdSecType element to the <dmdSec>
     * @return the content of the section as a string
     * @throws IOException
     **************************************************************************/
    public String getMdSecContents(MdSecType mdsec) throws IOException {
        String result = null;
        // Get mdWrap and mdRef elements.
        MdSecType.MdRef mdref = mdsec.getMdRef();
        MdSecType.MdWrap mdwrap = mdsec.getMdWrap();
        if (mdref != null) {
            // Get the http URL/URI.
            XmlAnyURI uri = mdref.xgetHref();
            // Convert the URI to String.
            String uriString = uri.toString();
            // Make an HTTP request, the result of this request is returned as a
            // string.
            result = makeHTTPRequest(uriString);
        }
        if (mdwrap != null) {
            // Get xml-data and convert it to String.
            MdSecType.MdWrap.XmlData xmldata = mdwrap.getXmlData();
            result = xmldata.toString();
        }
        return result;
    }

    /***************************************************************************
     * <p>
     * Retrieves all
     * 
     * <pre>
     * StructMapType
     * </pre>
     * 
     * objects of a given type.
     * </p>
     * 
     * @param type
     * @return List containing StructMapType objects, if none was found null is returned.
     **************************************************************************/
    public List<StructMapType> getStructMapByType(String type) {
        List<StructMapType> resultList = new LinkedList<>();
        StructMapType[] structmap = this.mets.getStructMapArray();

        if (structmap != null) {
            for (StructMapType smt : structmap) {
                if ((smt.getTYPE() != null) && (smt.getTYPE().equals(type))) {
                    resultList.add(smt);
                }
            }
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Retrieves all
     * 
     * <pre>
     * StructMapType
     * </pre>
     * 
     * objects with a given label.
     * </p>
     * 
     * @param type
     * @return List containing StructMapType objects, if none was found null is returned
     **************************************************************************/
    public List<StructMapType> getStructMapByLabel(String label) {
        List<StructMapType> resultList = new LinkedList<>();
        StructMapType[] structmap = this.mets.getStructMapArray();
        if (structmap != null) {
            for (StructMapType smt : structmap) {
                if ((smt.getLABEL() != null) && (smt.getLABEL().equals(label))) {
                    resultList.add(smt);
                }
            }
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Retrieves all
     * 
     * <pre>
     * StructMapType
     * </pre>
     * 
     * objects with a given ID.
     * </p>
     * 
     * @param type
     * @return the StructMapType or null
     **************************************************************************/
    public StructMapType getStructMapByID(String id) {
        StructMapType[] structmap = this.mets.getStructMapArray();
        if (structmap != null) {
            for (StructMapType smt : structmap) {
                if ((smt.getID() != null) && (smt.getID().equals(id))) {
                    return smt;
                }
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Retrieves a FileType object with the given ID.
     * </p>
     * 
     * @param id
     * @return FileType object or null (if none available)
     **************************************************************************/
    public FileType getFileByID(String id) {
        FileSec filesec = this.mets.getFileSec();

        FileGrp[] filegroup = filesec.getFileGrpArray();
        if (filegroup != null) {
            for (FileGrp grp : filegroup) {
                FileType file = getFileByID(id, grp);
                if (file != null) {
                    return file;
                }
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Retrieves all &lt;div&gt; elements, which are linked to a given &lt;div&gt; element in the structLink-section, no matter if the relationship
     * points to or from the given &lt;div&gt; element.
     * </p>
     * 
     * @param inDiv
     * @return
     **************************************************************************/
    public List<SmLink> getAllLinkedDivs(DivType inDiv) {
        return getAllLinkedDivs(inDiv, true, true);
    }

    /***************************************************************************
     * <p>
     * Retrieves all &lt;div&gt; elements, which point to the given &lt;div&gt; element in the structLink section.
     * </p>
     * 
     * @param inDiv
     * @return
     **************************************************************************/
    public List<SmLink> getAllLinkedFromDivs(DivType inDiv) {
        return getAllLinkedDivs(inDiv, false, true);
    }

    /***************************************************************************
     * <p>
     * Retrieves all &lt;div&gt; elements, which point from the given &lt;div&gt; element in the structLink section.
     * </p>
     * 
     * @param inDiv
     * @return
     **************************************************************************/
    public List<SmLink> getAllLinkedToDivs(DivType inDiv) {
        return getAllLinkedDivs(inDiv, true, false);
    }

    /**************************************************************************
     * <p>
     * Gets a StructMap div type by the given div ID.
     * </p>
     * 
     * @param theId
     * @param theDiv
     * @return The appropriate StructMap if existing, NULL otherwise.
     **************************************************************************/
    public DivType getStructMapDiv(String theId) {
        StructMapType[] structMapArray = this.mets.getStructMapArray();
        if (structMapArray != null) {
            return getStructMapDiv(theId, structMapArray[0].getDiv());
        }
        return null;
    }

    /***************************************************************************
     * <p>
     * Creates a <file> object as the last object in a given <FileGrp>. The <file> object will have a new, unique ID attribute and a single <FLocat>
     * sub element. The LOCTYPE attribute is filled automatically, depending on the value of the file's location.
     * </p>
     * 
     * @param inDiv
     * @param filegroup
     * @param url
     * @return FileType object, which represents the new <file> element.
     **************************************************************************/
    public FileType createFileType(DivType inDiv, FileGrp filegroup, String url) {
        // Ccreate new FileType.
        FileType ft = filegroup.addNewFile();
        // Add <mets:FLocat> element.
        FLocat filelocation = ft.addNewFLocat();
        if (url.startsWith("http")) {
            filelocation.setLOCTYPE(FileType.FLocat.LOCTYPE.URL);
        } else if (url.startsWith("doi")) {
            filelocation.setLOCTYPE(FileType.FLocat.LOCTYPE.DOI);
        } else if (url.startsWith("handle")) {
            filelocation.setLOCTYPE(FileType.FLocat.LOCTYPE.HANDLE);
        } else if (url.startsWith("urn")) {
            filelocation.setLOCTYPE(FileType.FLocat.LOCTYPE.URN);
        } else {
            filelocation.setLOCTYPE(FileType.FLocat.LOCTYPE.OTHER);
        }
        // Set URL; create identifier for file and add one.
        filelocation.setHref(url);
        String fileidentifier = "";
        // Set file pointer for the new <file> element.
        ft.setID(fileidentifier);
        Fptr filepointer = inDiv.addNewFptr();
        // Set fileidentifier.
        filepointer.setFILEID(fileidentifier);
        return ft;
    }

    /***************************************************************************
     * PRIVATE (AND PROTECTED) METHODS
     **************************************************************************/
    /***************************************************************************
     * <p>
     * Private class, which is used by
     * 
     * <pre>
     * getAMDSecByID
     * </pre>
     * 
     * method. From an array of MdSecType objects it finds the one with the given ID.<br/>
     * 
     * Usually.
     * </p>
     * 
     * @param inMDsec
     * @param inID
     * @return an MdSecType object
     **************************************************************************/
    private MdSecType getMdSecTypeByID(MdSecType[] inMDsec, String inID) {
        if (inMDsec != null) {
            for (MdSecType current : inMDsec) {
                String id = current.getID();
                if ((id != null) && (id.equals(inID))) {
                    return current;
                }
            }
        }
        return null;
    }

    /***************************************************************************
     * @param type
     * @return
     **************************************************************************/
    private String createNewXMLID(String type) {
        String xmlid = null;
        UUID uuid = UUID.randomUUID();
        String uuidstring = uuid.toString();
        if ("amdsec".equals(type)) {
            xmlid = "amd" + uuidstring;
        } else if ("dmdsec".equals(type)) {
            xmlid = "dmd" + uuidstring;
        } else if ("tech".equals(type)) {
            xmlid = "tech" + uuidstring;
        } else if ("digiprov".equals(type)) {
            xmlid = "dipr" + uuidstring;
        } else if ("rights".equals(type)) {
            xmlid = "rgt" + uuidstring;
        } else if ("source".equals(type)) {
            xmlid = "src" + uuidstring;
        } else if ("file".equals(type)) {
            xmlid = "fl" + uuidstring;
        }
        return xmlid;
    }

    /**************************************************************************
     * <p>
     * Gets a StructMap div type by the given ID and a given div.
     * </p>
     * 
     * @param theId
     * @param theDiv
     * @return The appropriate StructMap if existing, NULL otherwise.
     **************************************************************************/
    private DivType getStructMapDiv(String theId, DivType theDiv) {
        // Firt check the given div type.
        if (theDiv.getID().equals(theId)) {
            return theDiv;
        }
        DivType[] divArray = theDiv.getDivArray();
        if (divArray != null) {
            // Then check all divs from the the div list, if existing.
            for (DivType dt : divArray) {
                DivType result = getStructMapDiv(theId, dt);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /***************************************************************************
     * <p>
     * Makes a simple HTTP call to a URI; this method is used for internal purposes only. The content of the request is returned as a string.
     * </p>
     * 
     * @param uri
     * @return
     * @throws IOException
     **************************************************************************/
    @SuppressWarnings("deprecation")
    private String makeHTTPRequest(String uri) throws IOException {
        URL url;
        URLConnection urlConn = null;
        String str;
        url = new URL(uri);
        urlConn = url.openConnection();
        // Get response data.
        DataInputStream input = new DataInputStream(urlConn.getInputStream());
        while (null != (str = input.readLine())) {
            // Do nothing!
        }
        return str;
    }

    /***************************************************************************
     * <p>
     * Retrieves all &lt;div&gt; elements which are linked to a given &lt;div&gt; (inDiv) using the &lt;structLink&gt; section. Depending on the
     * checkto and checkfrom switches, the linked &lt;div&gt; elements are only searched in the "to" or in the "from" attribute of the &lt;smLink&gt;
     * element.
     * </p>
     * 
     * @param inDiv the &lt;div&gt; element for which all linked &lt;div&gt; elements should be found.
     * @param checkto
     * @param checkfrom
     * @return
     **************************************************************************/
    private List<SmLink> getAllLinkedDivs(DivType inDiv, boolean checkto, boolean checkfrom) {
        LinkedList<SmLink> result = new LinkedList<>();
        // Get ID for inDiv, this ID is used as a target.
        String divid = inDiv.getID();
        if (divid == null) {
            // Has no ID attribute; can't neither source nor target of an smlink
            // element.
            return result;
        }
        // Get structLink section.
        StructLink structlink = this.mets.getStructLink();
        SmLink[] links = structlink.getSmLinkArray();

        for (SmLink link : links) {

            boolean from = false;
            boolean to = false;
            if (checkfrom && link.getFrom() != null && link.getFrom().equals(divid)) {
                from = true;
            } else if (checkfrom && (link.getFrom() == null) || !link.getFrom().equals(divid)) {
                // Checkfrom should be checked, but was not equal.
                from = false;
            } else {
                // Checkfrom ist false, from field is not beeing tested.
                from = true;
            }
            if (checkto && link.getTo() != null && link.getTo().equals(divid)) {
                to = true;
            } else if (checkto && (link.getTo() == null) || !link.getTo().equals(divid)) {
                // Checkfrom should be checked, but was not equal.
                to = false;
            } else {
                // checkto ist false, to field is not beeing tested.
                to = true;
            }
            if (from && to) {
                // In either the from or the two link.
                result.add(link);
            }
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Retrieves a file by ID from a filegroup, including all sub filegroups.
     * </p>
     * 
     * @param id
     * @param filegroup
     * @return a FileType or null
     **************************************************************************/
    private FileType getFileByID(String id, FileGrpType filegroup) {
        // Iterate over all files.

        FileType[] file = filegroup.getFileArray();
        if (file != null) {
            for (FileType ft : file) {

                if ((ft.getID() != null) && (ft.getID().equals(id))) {
                    // Found the type.
                    return ft;
                }
            }
        }
        // Nothing found, so get the list of all sub groups and iterate over
        // those subgroups.
        FileGrpType[] subfilegroup = filegroup.getFileGrpArray();

        if (subfilegroup != null) {
            for (FileGrpType fgt : subfilegroup) {
                FileType singlefile = getFileByID(id, fgt);
                if (singlefile != null) {
                    // File found in sub filegroup.
                    return singlefile;
                }
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Parses an &lt;AmdSec&gt; section and retrieves the appropriate subsection with the requested id.
     * </p>
     * 
     * @param inDoc MetsDocument, which contains the requested section
     * @param id
     * @param checkSubSections if set to true, all sub-section &lt;techMD&gt;, &lt;rightsMD&gt;,&lt;sourceMD&gt; and &lt;digiprovMD&gt; are checked,
     *            if one of those contains the required element. If set to false, only the appropriate &lt;AmdSec&gt; is retrieved.
     * @return
     **************************************************************************/
    private Object getAmdSecByID(String id, boolean checkSubSections) {
        MdSecType result = null;
        // Get all descriptive Metadata sections as an array.
        AmdSecType[] mdsections = this.mets.getAmdSecArray();

        // Iterate over all sections.
        if (mdsections != null) {
            for (AmdSecType mdsec : mdsections) {

                String sectionid = mdsec.getID();
                // Compare the id and the given ID.
                if (sectionid.equals(id)) {
                    return mdsec;
                }
                if (checkSubSections) {
                    MdSecType[] mdsecRights = mdsec.getRightsMDArray();
                    MdSecType[] mdsecSource = mdsec.getSourceMDArray();
                    MdSecType[] mdsecTech = mdsec.getTechMDArray();
                    MdSecType[] mdsecDigiprov = mdsec.getDigiprovMDArray();
                    result = getMdSecTypeByID(mdsecRights, id);
                    if (result != null) {
                        return result;
                    }
                    result = getMdSecTypeByID(mdsecSource, id);
                    if (result != null) {
                        return result;
                    }
                    result = getMdSecTypeByID(mdsecTech, id);
                    if (result != null) {
                        return result;
                    }
                    result = getMdSecTypeByID(mdsecDigiprov, id);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /***************************************************************************
     * <p>
     * Creates an MdWrap element under the given MdSecType object of a certain type. The following values.
     * </p>
     * 
     * <p>
     * For mdTypeValue are supported:
     * 
     * <ul>
     * <li>dc - creates DC value for DublinCore</li>
     * <li>dc - creates DC value for DublinCore</li>
     * <li>ddi - creates DDI value</li>
     * <li>ead - creates EAD value for Encoded Archival Description</li>
     * <li>fgdc - creates FGDC value</li>
     * <li>lc_av - creates LCAV value for Library of Congress audio video project</li>
     * <li>lom - creates LOM value for learning objects metadata</li>
     * <li>marc - creates MARC value</li>
     * <li>mods - creates MODS value</li>
     * <li>nisoimg - creates NISOIMG value NISO technical metadata for still images (MIX)</li>
     * <li>premis - creates PREMIS value</li>
     * <li>teihdr - creates TEIHDR value for metadata from the TEI header</li>
     * <li>vra - creates VRA value</li>
     * </ul>
     * 
     * In case mdTypeValue has none of the above values, the value is stored in the OTHERMDTYPE attribute and MDTYPE is set to "other".
     * </p>
     * 
     * @param inMDSec
     * @param mdTypeValue
     * @return MdWrap element
     **************************************************************************/
    private MdWrap createMDWrap(MdSecType inMDSec, String mdTypeValue) {
        MdWrap mdwrap = inMDSec.addNewMdWrap();
        MdSecType.MdWrap.MDTYPE.Enum mdtype = null;
        if ("dc".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.DC;
        }
        if ("ddi".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.DDI;
        }
        if ("ead".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.EAD;
        }
        if ("fgdc".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.FGDC;
        }
        if ("lc_av".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.LC_AV;
        }
        if ("lom".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.LOM;
        }
        if ("marc".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.MARC;
        }
        if ("mods".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.MODS;
        }
        if ("nisoimg".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.NISOIMG;
        }
        if ("premis".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.PREMIS;
        }
        if ("teihdr".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.TEIHDR;
        }
        if ("vra".equalsIgnoreCase(mdTypeValue)) {
            mdtype = MdSecType.MdWrap.MDTYPE.VRA;
        }
        if (mdtype == null) {
            mdtype = MdSecType.MdWrap.MDTYPE.OTHER;
        }
        mdwrap.setMDTYPE(mdtype);
        // If the type is none of the above, we create an OTHERMDTYPE attribute.
        if (mdtype == MdSecType.MdWrap.MDTYPE.OTHER) {
            mdwrap.setOTHERMDTYPE(mdTypeValue);
        }
        return mdwrap;
    }

    /***************************************************************************
     * GETTERS AND SETTERS
     **************************************************************************/
    /***************************************************************************
     * @return
     **************************************************************************/
    public String getVersion() {
        return VERSION;
    }
}
