package ugh.fileformats.opac;

/*******************************************************************************
 * ugh.fileformats.opac / PicaPlus.java
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
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

/*******************************************************************************
 * <p>
 * The PicaPLus import class.
 * </p>
 * 
 * <p>
 * PicaPlus is described here: <a href="http://www.gbv.de/wikis/cls/PICA_XML_Version_1.0" >http://www.gbv.de/wikis/cls/PICA_XML_Version_1.0</a>
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-09-19
 * 
 *          TODOLOG
 *
 *          CHANGELOG
 * 
 *          19.09.2014 --- Ronge --- Add import into metadata groups
 * 
 *          15.02.2010 --- Funk --- Logging version information now.
 * 
 *          11.12.2009 --- Funk --- Re-added a continue in parsePicaPlusRecord().
 * 
 *          04.12.2009 --- Funk --- Added trim() to all PicaPlus prefs values' getTextNodeValue() calls.
 * 
 *          20.11.2009 --- Funk --- Added RegExp support for PicaPlus catalog entry value changes, and conditions for mapping PicaPlus fields to
 *          internal Metadata.
 * 
 *          17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *          13.11.2009 --- Funk --- Minor changes to the log and Exception messages -- Removed the field of type FileSet: myImageset.
 * 
 ******************************************************************************/

@Log4j2
public class PicaPlus implements ugh.dl.Fileformat {

    /***************************************************************************
     * VERSION STRING
     **************************************************************************/

    private static final String VERSION = "1.3-20100215";

    /***************************************************************************
     * STATIC FINALS
     **************************************************************************/

    protected static final String PICAPLUS_PREFS_NODE_NAME_STRING = "PicaPlus";

    private static final String PREFS_METADATA_STRING = "Metadata";
    private static final String PREFS_VALUECONDITION_STRING = "ValueCondition";
    private static final String PREFS_VALUEREGEXP_STRING = "ValueRegExp";
    private static final String PREFS_VALUEREPLACEMENT_STRING = "ValueReplacement";
    private static final String PREFS_DOCSTRUCT_STRING = "DocStruct";
    private static final String PREFS_PERSON_STRING = "Person";
    private static final String PREFS_PICAPLUSGROUP_STRING = "PicaPlusGroup";
    private static final String PREFS_METADATAGROUP_STRING = "MetadataGroup";
    private static final String PREFS_GROUPNAME_STRING = "Groupname";
    private static final String PREFS_DELIMETER_STRING = "Delimiter";
    private static final String PREFS_PICAMAINTAG_STRING = "PicaMainTag";
    private static final String PREFS_PICASUBTAG_STRING = "PicaSubTag";
    private static final String PREFS_PICACONTENT_STRING = "PicaContent";
    private static final String PREFS_NAME_STRING = "Name";

    private static final String PREFS_PERSONFIRSTNAME_STRING = "firstname";
    private static final String PREFS_PERSONLASTNAME_STRING = "lastname";
    private static final String PREFS_PERSONIDENTIFIER_STRING = "identifier";
    private static final String PREFS_PERSONEXPANSION_STRING = "expansion";
    private static final String PREFS_PERSONFUNCTION_STRING = "function";

    private static final String PREFS_PPPICAPLUSRESULTS_STRING = "picaplusresults";
    private static final String PREFS_PPCOLLECTION_STRING = "collection";
    private static final String PREFS_PPPICAPLUSRECORD_STRING = "picaplusrecord";
    private static final String PREFS_PPRECORD_STRING = "record";
    private static final String PREFS_PPPICAPLUS_STRING = "picaplus";
    private static final String PREFS_PPFIELD_STRING = "field";
    private static final String PREFS_PPTAG_STRING = "tag";
    private static final String PREFS_PPVALUE_STRING = "value";
    private static final String PREFS_PPSUBFIELD_STRING = "subfield";
    private static final String PREFS_PPCODE_STRING = "code";

    public static final short ELEMENT_NODE = Node.ELEMENT_NODE;

    // UGH document.
    private ugh.dl.DigitalDocument mydoc = new DigitalDocument();

    // General preferences.
    private ugh.dl.Prefs myPreferences;

    // Contains all PicaPlusGroups.
    private Set<MatchingMetadataGroup> allGroups = new HashSet<>();

    // Contains all rules for metadata matching.
    private Set<MatchingMetadataObject> mmoList = new HashSet<>();

    private Map<String, String> metadataGroups = new HashMap<>();

    /***************************************************************************
     * @param inPrefs
     * @throws PreferencesException
     **************************************************************************/
    public PicaPlus(ugh.dl.Prefs inPrefs) {

        this.myPreferences = inPrefs;

        // Read preferences.
        Node picaplusNode = inPrefs.getPreferenceNode(PICAPLUS_PREFS_NODE_NAME_STRING);
        if (picaplusNode == null) {
            log.error("Can't read preferences for picaplus fileformat! Node 'PicaPlus' in XML-file not found!");
        } else {
            this.readPrefs(picaplusNode);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public static String getVersion() {
        return VERSION;
    }

    /***************************************************************************
     * @param picaplusnode
     **************************************************************************/
    public void readPrefs(Node picaplusnode) {

        // Children should be "metadata" or "docstruct" nodes.
        NodeList children = picaplusnode.getChildNodes();

        log.info("Reading " + PICAPLUS_PREFS_NODE_NAME_STRING + " prefs");

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            // It's an element.
            if (n.getNodeType() == ELEMENT_NODE) {
                // Check the name of the node.
                MatchingMetadataObject mmo = null;
                if (PREFS_METADATA_STRING.equalsIgnoreCase(n.getNodeName())) {
                    mmo = readMetadata(n);
                } else if (PREFS_DOCSTRUCT_STRING.equalsIgnoreCase(n.getNodeName())) {
                    mmo = readDocStruct(n);
                } else if (PREFS_PERSON_STRING.equalsIgnoreCase(n.getNodeName())) {
                    Set<MatchingMetadataObject> hs = readPerson(n);
                    this.mmoList.addAll(hs);
                } else if (PREFS_PICAPLUSGROUP_STRING.equalsIgnoreCase(n.getNodeName())) {
                    readPicaGroup(n);
                } else if (PREFS_METADATAGROUP_STRING.equalsIgnoreCase(n.getNodeName())) {
                    readMetadataGroup(n);
                } else if ("Corporate".equalsIgnoreCase(n.getNodeName())) {
                    mmoList.addAll(readPicaCorporateNode(n));
                }

                if (mmo != null) {
                    // Add the metadatamatchingobject to a string add it to list
                    // of objects.
                    this.mmoList.add(mmo);
                }
            }
        }

        log.info("Reading picaplus prefs complete");
    }

    /**
     * Reads a MetadataGroup mapping and writes it in the global variable metadataGroups. A metadata group mapping is defined in a
     * {@code <MetadataGroup>} tag which must contain the two tags {@code <Name>} and {@code <picaMainTag>}. In this case, all contents found in the
     * given pica main tag will be grouped as a metadata group with the given name. The metadata and person elements that shall go into the group must
     * independently have been defined.
     * 
     * @param node rule set node to parse
     */
    private void readMetadataGroup(Node node) {
        String resultPicaMainTag = null;
        String resultMetadataGroupType = null;

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == ELEMENT_NODE) {
                if (PREFS_NAME_STRING.equalsIgnoreCase(child.getNodeName())) {
                    resultMetadataGroupType = readTextNode(child);
                } else if (PREFS_PICAMAINTAG_STRING.equalsIgnoreCase(child.getNodeName())) {
                    resultPicaMainTag = readTextNode(child);
                }
            }
        }

        if (resultPicaMainTag != null && resultMetadataGroupType != null) {
            metadataGroups.put(resultPicaMainTag, resultMetadataGroupType);
        }
    }

    /***************************************************************************
     * @param inNode
     **************************************************************************/
    private void readPicaGroup(Node inNode) {

        MatchingMetadataGroup mmg = new MatchingMetadataGroup();

        NodeList nl = inNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == ELEMENT_NODE && PREFS_GROUPNAME_STRING.equalsIgnoreCase(n.getNodeName())) {
                String groupname = readTextNode(n);
                mmg.setGroupname(groupname);
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_METADATA_STRING.equalsIgnoreCase(n.getNodeName())) {
                String internalname = readTextNode(n);
                mmg.setMetadatatypename(internalname);
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_DELIMETER_STRING.equalsIgnoreCase(n.getNodeName())) {
                String delimiter = readTextNode(n);
                mmg.setDelimiter(delimiter);
            }
        }

        // Add group to list of all groups.
        this.allGroups.add(mmg);
    }

    /***************************************************************************
     * @param inNode
     * @return
     **************************************************************************/
    private MatchingMetadataObject readMetadata(Node inNode) {

        MatchingMetadataObject mmo = new MatchingMetadataObject();
        mmo.setType("Metadata");

        NodeList nl = inNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICAMAINTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                mmo.setPicaplusField(readTextNode(n));
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICASUBTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                mmo.setPicaplusSubfield(readTextNode(n));
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICAPLUSGROUP_STRING.equalsIgnoreCase(n.getNodeName())) {
                mmo.setPicaplusGroupname(readTextNode(n));
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_NAME_STRING.equalsIgnoreCase(n.getNodeName())) {
                mmo.setInternalName(readTextNode(n));
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_VALUECONDITION_STRING.equalsIgnoreCase(n.getNodeName())) {
                mmo.setValueCondition(readTextNode(n));
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_VALUEREGEXP_STRING.equalsIgnoreCase(n.getNodeName())) {
                String val = readTextNode(n);
                if (val == null) {
                    val = "";
                }
                mmo.setValueRegExp(val);
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_VALUEREPLACEMENT_STRING.equalsIgnoreCase(n.getNodeName())) {
                mmo.setValueRegExReplacement(readTextNode(n));
            }
        }

        // Check if all required data is set.
        if (mmo.getPicaplusField() == null || mmo.getType() == null) {
            return null;
        }
        if (mmo.getPicaplusField() == null && mmo.getPicaplusGroupname() == null) {
            return null;
        }

        return mmo;
    }

    private Set<MatchingMetadataObject> readPicaCorporateNode(Node inNode) {

        HashSet<MatchingMetadataObject> result = new HashSet<>();

        String maintag = null;
        String internal = null;

        MatchingMetadataObject mmo = new MatchingMetadataObject();
        mmo.setType("Corporate");

        NodeList nl = inNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == ELEMENT_NODE) {
                if (PREFS_PICAMAINTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                    maintag = readTextNode(n);
                    mmo.setPicaplusField(maintag);
                }
                if (PREFS_PICASUBTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                    String subtag = readTextNode(n);
                    mmo.setPicaplusSubfield(subtag);

                    NamedNodeMap nnm = n.getAttributes();
                    Node tn = nnm.getNamedItem("type");
                    String attributeValue = tn.getNodeValue();

                    if ("mainName".equalsIgnoreCase(attributeValue)) {
                        mmo.setMainField(true);
                    } else if ("subName".equalsIgnoreCase(attributeValue)) {
                        mmo.setSubField(true);
                    } else if ("partName".equalsIgnoreCase(attributeValue)) {
                        mmo.setPartField(true);
                    } else if ("identifier".equalsIgnoreCase(attributeValue)) {
                        mmo.setIdentifier(true);
                    }
                } else if (PREFS_NAME_STRING.equalsIgnoreCase(n.getNodeName())) {
                    mmo.setInternalName(readTextNode(n));
                } else if (PREFS_VALUECONDITION_STRING.equalsIgnoreCase(n.getNodeName())) {
                    mmo.setValueCondition(readTextNode(n));
                } else if (PREFS_VALUEREGEXP_STRING.equalsIgnoreCase(n.getNodeName())) {
                    mmo.setValueRegExp(readTextNode(n));
                }

                // Create new MMO object, this is necessary, because in this
                // case we have several subtags, each one will be a new MMO.
                if (StringUtils.isNotBlank(mmo.getPicaplusSubfield())) {
                    result.add(mmo);
                }
                mmo = new MatchingMetadataObject();
                mmo.setType("Corporate");

                if (maintag != null) {
                    mmo.setPicaplusField(maintag);
                }
                if (internal != null) {
                    mmo.setInternalName(internal);
                }

                if (PREFS_NAME_STRING.equalsIgnoreCase(n.getNodeName())) {
                    internal = readTextNode(n);
                    mmo.setInternalName(internal);

                    for (MatchingMetadataObject mmo2 : result) {
                        mmo2.setInternalName(internal);
                    }
                }
            }
        }

        return result;
    }

    /***************************************************************************
     * @param inNode
     * @return
     **************************************************************************/
    private Set<MatchingMetadataObject> readPerson(Node inNode) {

        HashSet<MatchingMetadataObject> result = new HashSet<>();

        String maintag = null;
        String internal = null;

        MatchingMetadataObject mmo = new MatchingMetadataObject();
        mmo.setType("Person");

        NodeList nl = inNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICAMAINTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                maintag = readTextNode(n);
                mmo.setPicaplusField(maintag);

                // Add it also to MMOs we already created.
                for (MatchingMetadataObject mmo2 : result) {
                    mmo2.setPicaplusField(maintag);
                }
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICASUBTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                String subtag = readTextNode(n);
                mmo.setPicaplusSubfield(subtag);

                NamedNodeMap nnm = n.getAttributes();
                Node tn = nnm.getNamedItem("type");
                // Get value of type attribute.
                String attributeValue = tn.getNodeValue();

                if (PREFS_PERSONLASTNAME_STRING.equalsIgnoreCase(attributeValue)) {
                    mmo.setLastname(true);
                }
                if (PREFS_PERSONFIRSTNAME_STRING.equalsIgnoreCase(attributeValue)) {
                    mmo.setFirstname(true);
                }
                if (PREFS_PERSONIDENTIFIER_STRING.equalsIgnoreCase(attributeValue)) {
                    mmo.setIdentifier(true);
                }
                if (PREFS_PERSONEXPANSION_STRING.equalsIgnoreCase(attributeValue)) {
                    mmo.setExpansion(true);
                }
                if (PREFS_PERSONFUNCTION_STRING.equalsIgnoreCase(attributeValue)) {
                    mmo.setFunction(true);
                }

                // Create new MMO object, this is necessary, because in this
                // case we have several subtags, each one will be a new MMO.
                result.add(mmo);
                mmo = new MatchingMetadataObject();
                mmo.setType("Person");

                if (maintag != null) {
                    mmo.setPicaplusField(maintag);
                }
                if (internal != null) {
                    mmo.setInternalName(internal);
                }
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_NAME_STRING.equalsIgnoreCase(n.getNodeName())) {
                internal = readTextNode(n);
                mmo.setInternalName(internal);

                for (MatchingMetadataObject mmo2 : result) {
                    mmo2.setInternalName(internal);
                }
            }
        }

        // Check if all required data is set.
        if (mmo.getInternalName() == null || mmo.getPicaplusField() == null || mmo.getType() == null) {
            return null;
        }

        return result;
    }

    /***************************************************************************
     * @param inNode
     * @return
     **************************************************************************/
    private MatchingMetadataObject readDocStruct(Node inNode) {

        MatchingMetadataObject mmo = new MatchingMetadataObject();
        mmo.setType("DocStruct");

        NodeList nl = inNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICAMAINTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                String maintag = readTextNode(n);
                mmo.setPicaplusField(maintag);
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICASUBTAG_STRING.equalsIgnoreCase(n.getNodeName())) {
                String subtag = readTextNode(n);
                mmo.setPicaplusSubfield(subtag);
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_PICACONTENT_STRING.equalsIgnoreCase(n.getNodeName())) {
                String content = readTextNode(n);
                mmo.setContent(content);
            }
            if (n.getNodeType() == ELEMENT_NODE && PREFS_NAME_STRING.equalsIgnoreCase(n.getNodeName())) {
                String internal = readTextNode(n);
                mmo.setInternalName(internal);
            }
        }

        // Check if all required data is set.
        if (mmo.getInternalName() == null || mmo.getPicaplusField() == null || mmo.getType() == null) {
            return null;
        }

        return mmo;
    }

    /***************************************************************************
     * @param inNode
     * @return
     **************************************************************************/
    private String readTextNode(Node inNode) {

        NodeList nl = inNode.getChildNodes();
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return n.getNodeValue().trim();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#getDigitalDocument()
     */
    @Override
    public DigitalDocument getDigitalDocument() throws PreferencesException {
        return this.mydoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#setDigitalDocument(ugh.dl.DigitalDocument)
     */
    @Override
    public void setDigitalDocument(DigitalDocument inDoc) {
        this.mydoc = inDoc;
    }

    /***************************************************************************
     * <p>
     * Read from Node of the DOM tree.
     * </p>
     **************************************************************************/
    public boolean read(Node inNode) throws ReadException {

        DocStruct ds = null;
        DocStruct dsOld = null;
        DocStruct dsTop = null;

        log.info("Parsing picaplus record");

        // DOM tree is created already - parse the the tree and find
        // picaplusresults and picaplusrecord elements.
        try {
            // There should only be <picaplusresults> element nodes.
            Node ppr = inNode;
            if (ppr.getNodeType() == ELEMENT_NODE) {
                String nodename = ppr.getNodeName();
                if (PREFS_PPPICAPLUSRESULTS_STRING.equals(nodename) || PREFS_PPCOLLECTION_STRING.equals(nodename)) {

                    // Iterate over all results.
                    NodeList picaplusrecords = ppr.getChildNodes();
                    for (int x = 0; x < picaplusrecords.getLength(); x++) {
                        Node n = picaplusrecords.item(x);

                        if (n.getNodeType() == ELEMENT_NODE) {
                            nodename = n.getNodeName();
                            if (PREFS_PPPICAPLUSRECORD_STRING.equals(nodename) || PREFS_PPRECORD_STRING.equals(nodename)) {
                                // Parse a single picaplus record.
                                ds = parsePicaPlusRecord(n);
                                // It's the first one, so this becomes the
                                // toplogical structural entity.
                                if (dsOld == null) {
                                    this.mydoc.setLogicalDocStruct(ds);
                                    dsTop = ds;
                                } else {
                                    dsOld.addChild(ds);
                                }
                                dsOld = ds;
                                ds = null;
                            }
                        }
                    }
                }
            }

            // No DocumentStructure never read!!!
            if (dsTop == null) {
                throw new ReadException("No DocStruct created");
            }

            log.info("DocumentStructure created:" + dsTop.getType().getName());

            this.mydoc.setLogicalDocStruct(dsTop);
        } catch (TypeNotAllowedAsChildException e) {
            // Child DocStruct could not be added to father, because of ruleset.
            String message = "Can't add child to parent DocStruct! Child type '" + ds.getType().getName() + "' not allowed for parent type";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (MetadataTypeNotAllowedException e) {
            String message = "Can't add child to parent DocStruct! Child type must not be null";
            log.error(message, e);
            throw new ReadException(message, e);
        }

        log.info("Parsing picaplus record complete");

        return true;
    }

    /***************************************************************************
     * <p>
     * Read XMLfile.
     * </p>
     **************************************************************************/
    @Override
    public boolean read(String filename) throws ReadException {

        // DOM Document.
        Document document;

        DocStruct ds = null;
        DocStruct dsOld = null;
        DocStruct dsTop = null;

        // New document builder instance.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Do not validate XML file.
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(false);

        this.mydoc = new DigitalDocument();

        // Read file and parse it.
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(filename));

            // Old version.
            NodeList upperChildlist = document.getElementsByTagName(PREFS_PPPICAPLUSRESULTS_STRING);

            // Picaplusresults elements found.
            if (upperChildlist.getLength() < 1) {
                // New version.
                upperChildlist = document.getElementsByTagName(PREFS_PPCOLLECTION_STRING);
            }

            // Iterate over node list (over all picaplusresults).
            for (int i = 0; i < upperChildlist.getLength(); i++) {
                Node ppr = upperChildlist.item(i);
                if (ppr.getNodeType() == ELEMENT_NODE) {
                    String nodename = ppr.getNodeName();
                    if (PREFS_PPPICAPLUSRESULTS_STRING.equals(nodename) || PREFS_PPCOLLECTION_STRING.equals(nodename)) {

                        // Iterate over all results.
                        NodeList picaplusrecords = ppr.getChildNodes();
                        for (int x = 0; x < picaplusrecords.getLength(); x++) {
                            Node n = picaplusrecords.item(x);

                            if (n.getNodeType() == ELEMENT_NODE) {
                                nodename = n.getNodeName();
                                if (PREFS_PPPICAPLUSRECORD_STRING.equals(nodename) || PREFS_PPRECORD_STRING.equals(nodename)) {
                                    // Parse a single picaplus record.
                                    ds = parsePicaPlusRecord(n);
                                    // It's the first one, so this becomes the
                                    // toplogical structural entity.
                                    if (dsOld == null) {
                                        this.mydoc.setLogicalDocStruct(ds);
                                        dsTop = ds;
                                    } else {
                                        dsOld.addChild(ds);
                                    }
                                    dsOld = ds;
                                    ds = null;
                                }
                            }
                        }
                    }
                }
            }

            // No DocumentStructure never read!!!
            if (dsTop == null) {
                String message = "No DocStruct created!";
                log.error(message);
                throw new ReadException(message);
            }

            this.mydoc.setLogicalDocStruct(dsTop);
        } catch (SAXParseException e) {
            String message = "Not a valid XML file!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (SAXException sxe) {
            Exception e = sxe;
            if (sxe.getException() != null) {
                e = sxe.getException();
            }
            String message = "Not a valid XML file!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (ParserConfigurationException e) {
            String message = "Parser configuration exception!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (IOException e) {
            String message = "IOException while reading file!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (TypeNotAllowedAsChildException e) {
            // Child DocStruct could not be added to parent, because of ruleset.
            String message = "Can't add child to parent DocStruct; Child type not allowed for parent type!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (MetadataTypeNotAllowedException e) {
            String message = "Can't add child to parent DocStruct; Child type must not be null!";
            log.error(message, e);
            throw new ReadException(message, e);
        }

        return true;
    }

    /***************************************************************************
     * @param inNode
     * @return
     * @throws MetadataTypeNotAllowedException
     * @throws ReadException
     **************************************************************************/
    private DocStruct parsePicaPlusRecord(Node inNode) throws MetadataTypeNotAllowedException, ReadException {

        // Contains all metadata.
        LinkedList<Metadata> allMDs = new LinkedList<>();
        // Contains all persons.
        LinkedList<Person> allPer = new LinkedList<>();
        // Contains all corporations
        LinkedList<Corporate> allCorp = new LinkedList<>();
        // Contains all metadata groups.
        LinkedList<MetadataGroup> allGroups = new LinkedList<>();

        DocStruct ds = null;
        Metadata md = null;
        Person per = null;
        if (!PREFS_PPPICAPLUSRECORD_STRING.equals(inNode.getNodeName()) && !PREFS_PPRECORD_STRING.equals(inNode.getNodeName())) {
            throw new ReadException("Unable to parse record node, not a pica plus record. Node name is " + inNode.getNodeName());
        }

        // Get all subfields.
        NodeList nl = inNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == ELEMENT_NODE) {
                HashSet<Serializable> hs = parsePicaPlusField(n);
                if (hs != null) {
                    for (Serializable pprObject : hs) {
                        if (pprObject != null && pprObject.getClass() == ugh.dl.DocStruct.class) {
                            if (ds != null) {
                                log.warn("Additional DocStruct found, replacing the old one!");
                            }
                            ds = (DocStruct) pprObject;
                            log.info("DocStruct '" + ds.getType().getName() + "' found");
                        } else if (pprObject != null && pprObject.getClass() == ugh.dl.Metadata.class) {
                            md = (Metadata) pprObject;
                            allMDs.add(md);
                            log.debug("Metadata (" + md.getType().getName() + "): '" + md.getValue() + "'");
                        } else if (pprObject != null && pprObject.getClass() == ugh.dl.Person.class) {
                            per = (Person) pprObject;
                            allPer.add(per);
                            log.debug("Person '" + per.getType().getName() + "' found");
                        } else if (pprObject != null && pprObject.getClass() == ugh.dl.MetadataGroup.class) {
                            MetadataGroup group = (MetadataGroup) pprObject;
                            allGroups.add(group);
                            log.debug("MetadataGroup '" + group.getType().getName() + "' found");
                        } else if (pprObject != null && pprObject.getClass() == ugh.dl.Corporate.class) {
                            allCorp.add((Corporate) pprObject);
                        }
                    }
                }
            }
        }

        if (ds == null) {
            // No DocStruct found, this is a serious problem; as I do not know
            // to where I should attach the metadata.
            try {
                StringWriter writer = new StringWriter();
                Transformer transformer;
                transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(inNode), new StreamResult(writer));
                throw new ReadException("Picaplus record read, but no DocStruct found!\nRecord node content:\n" + writer.toString());
            } catch (TransformerException | TransformerFactoryConfigurationError e) {
                log.error("Error parsing xml node: " + e.toString());
                throw new ReadException("Picaplus record read, but no DocStruct found!");
            }
        }

        // Add metadata to DocStruct.
        if (allMDs != null) {
            for (Metadata md2 : allMDs) {
                try {
                    ds.addMetadata(md2);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    log.warn(message, e);
                    continue;
                } catch (DocStructHasNoTypeException e) {
                    String message = "Ignoring DocStructHasNoTypeException at OPAC import!";
                    log.warn(message, e);
                    continue;
                }
            }
        }

        // Add metadata from groups to DocStruct only, if the group has content.
        if (this.allGroups != null) {
            for (MatchingMetadataGroup mmg : this.allGroups) {
                if (mmg.getContent() == null) {
                    // Has no content.
                    continue;
                }
                if (mmg.getMetadatatypename() == null) {
                    // Has no typename.
                    continue;
                }
                MetadataType mdt = this.myPreferences.getMetadataTypeByName(mmg.getMetadatatypename());
                if (mdt == null) {
                    // Unknown metadata type.
                    String message = "No appropriate MetadataType with name '" + mmg.getMetadatatypename() + "' for group found!";
                    log.error(message);
                    break;
                }
                Metadata md3 = new Metadata(mdt);
                md3.setValue(mmg.getContent());
                try {
                    // Add metadata to docstruct.
                    ds.addMetadata(md3);
                    log.info("Added metadata '" + md3.getType().getName() + "' to DocStruct '" + ds.getType().getName() + "'");
                } catch (DocStructHasNoTypeException e) {
                    String message = "Ignoring DocStructHasNoTypeException at OPAC import!";
                    log.warn(message, e);
                    break;
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    log.warn(message, e);
                    break;
                }
            }
        }

        // Add persons to DocStruct.
        if (allPer != null) {
            for (Person per2 : allPer) {
                try {
                    ds.addPerson(per2);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    log.warn(message, e);
                } catch (IncompletePersonObjectException e) {
                    String message = "Ignoring IncompletePersonObjectException at OPAC import!";
                    log.warn(message, e);
                }
            }
        }

        if (allCorp != null) {
            for (Corporate corp : allCorp) {
                try {
                    ds.addCorporate(corp);
                } catch (MetadataTypeNotAllowedException e) {
                    String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                    log.warn(message, e);
                }
            }
        }

        // Add metadata groups to DocStruct.
        for (MetadataGroup group : allGroups) {
            try {
                ds.addMetadataGroup(group);
            } catch (MetadataTypeNotAllowedException e) {
                String message = "Ignoring MetadataTypeNotAllowedException at OPAC import!";
                log.warn(message, e);
            }
        }

        return ds;
    }

    /***************************************************************************
     * <p>
     * Parse a singe PicaPlusField - this method may parse the old and new version of a PicePlus field:
     * </p>
     * 
     * <p>
     * Old: <picaplus field="001A"> <value subfield="0">Wert</value> </picaplus>
     * </p>
     * 
     * <p>
     * New: <field tag="001A"> <subfield code="0">Wert</subfield> </field>
     * </p>
     * 
     * @param inNode
     * @return a HashSet containing, DocStruct, a Person or a Metadata instance, depending on the field which has been parsed.
     * @throws MetadataTypeNotAllowedException
     * @throws ReadException
     **************************************************************************/
    private HashSet<Serializable> parsePicaPlusField(Node inNode) throws MetadataTypeNotAllowedException, ReadException {

        // Contains the result objects.
        HashSet<Serializable> result = new HashSet<>();

        // Content of "field" attribute in <picaplus> element.
        String fieldAttribute = null;
        DocStruct ds;
        Person per = null;
        Corporate corp = null;
        // Set Metadata to null for every new picaplus node.
        Metadata md = null;
        NamedNodeMap nnm = null;
        Node attributeNode = null;

        // Determines, if all subfields should be parsed individually or if
        // content should be aggragated.
        boolean hasNoSubfields = false;

        if (!PREFS_PPPICAPLUS_STRING.equals(inNode.getNodeName()) && !PREFS_PPFIELD_STRING.equals(inNode.getNodeName())) {
            return null;
        }

        // Old version of XML file.
        if (PREFS_PPPICAPLUS_STRING.equals(inNode.getNodeName())) {
            nnm = inNode.getAttributes();
            attributeNode = nnm.getNamedItem(PREFS_PPFIELD_STRING);
            fieldAttribute = attributeNode.getNodeValue();

        }
        // Picaplus compliant version of XML file.
        else if (PREFS_PPFIELD_STRING.equals(inNode.getNodeName())) {
            nnm = inNode.getAttributes();
            attributeNode = nnm.getNamedItem(PREFS_PPTAG_STRING);
            fieldAttribute = attributeNode.getNodeValue();
        }

        if (fieldAttribute == null) {
            return null;
        }

        // Get all subfields.
        NodeList nl = inNode.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);

            if (n.getNodeType() == ELEMENT_NODE && PREFS_PPVALUE_STRING.equals(n.getNodeName()) || PREFS_PPSUBFIELD_STRING.equals(n.getNodeName())) {
                // Get value of "field" attribute.
                nnm = n.getAttributes();
                if (PREFS_PPVALUE_STRING.equals(n.getNodeName())) {
                    // Old version of xml file.
                    attributeNode = nnm.getNamedItem(PREFS_PPSUBFIELD_STRING);
                } else if (PREFS_PPSUBFIELD_STRING.equals(n.getNodeName())) {
                    // Picaplus compliant version of XML file.
                    attributeNode = nnm.getNamedItem(PREFS_PPCODE_STRING);
                }
                String subfieldAttribute = attributeNode.getNodeValue();

                // Get text of element.
                String content = this.readTextNode(n);

                // Create the MMO.
                MatchingMetadataObject mmo = findMMO(fieldAttribute, subfieldAttribute);

                // Check conditions from the prefs. If they exist and do NOT
                // match, continue with the next mmo.

                if (mmo != null && mmo.getValueCondition() != null && !"".equals(mmo.getValueCondition())) {
                    Pattern pattern = Pattern.compile(mmo.getValueCondition());
                    Matcher matcher = pattern.matcher(content);
                    if (!matcher.find()) {
                        log.info("Condition '" + mmo.getValueCondition() + "' for " + mmo.getType() + " '" + mmo.getInternalName() + " (" + content
                                + ")" + "' does not match, skipping...");
                        continue;
                    }
                }

                // Check regular expression from the prefs. If it exist, do
                // process.

                if (content != null && mmo != null && mmo.getValueRegExp() != null && !"".equals(mmo.getValueRegExp())) {
                    content = content.replaceAll(mmo.getValueRegExp(), mmo.getValueRegExReplacement());
                }

                // Now we have the MMO; check, if mmo has a subfield or not; if
                // not, all information from all subfields are added to the
                // appropriate field.
                if (mmo != null && "DocStruct".equals(mmo.getType())) {
                    // It's a docstruct type; they must contain a ??.
                    mmo = findMMO(fieldAttribute, subfieldAttribute, content);
                    DocStructType dst;

                    if (mmo == null) {
                        continue;
                    }

                    dst = this.myPreferences.getDocStrctTypeByName(mmo.getInternalName());

                    if (dst == null) {
                        log.warn("Can't create unknown DocStruct '" + mmo.getInternalName() + "'");
                    }

                    try {
                        ds = this.mydoc.createDocStruct(dst);
                    } catch (TypeNotAllowedForParentException e) {
                        log.warn("DocStructType '" + dst.getName() + "' is not allowed for parent DocStruct", e);
                        return null;
                    }
                    this.mydoc.setLogicalDocStruct(ds);
                    result.add(ds);

                } else if (mmo != null && "Metadata".equals(mmo.getType())) {
                    // It's a metadata type if it belongs to a group, add it.
                    log.debug("Picafield (" + mmo.getPicaplusField() + "): " + content);

                    if (mmo.getPicaplusGroupname() != null) {
                        for (MatchingMetadataGroup mmg : this.allGroups) {
                            if (mmg.getGroupname().equals(mmo.getPicaplusGroupname())) {
                                mmg.addContent(content);
                            }
                        }
                    } else // Now check, if the mmo has a subfield.
                    if (mmo.getPicaplusSubfield() == null) {
                        // It has no subfield, so iterate over all subfields
                        // and add content from all subfields to the one
                        // metadata field.

                        String value;

                        if (md == null) {
                            String internalName = mmo.getInternalName();
                            MetadataType mdt = this.myPreferences.getMetadataTypeByName(internalName);
                            if (mdt == null) {
                                log.warn("Can't create unknown Metadata object '" + internalName + "'");
                            } else {
                                md = new Metadata(mdt);
                                value = content;
                            }
                        } else {
                            value = md.getValue();
                            value = value + "; " + content;
                            md.setValue(value);
                            hasNoSubfields = true;
                        }
                    } else {
                        // It has a subfield, so create a Metadata
                        // object, add content and return it.
                        String internalname = mmo.getInternalName();
                        MetadataType mdt = this.myPreferences.getMetadataTypeByName(internalname);
                        if (mdt == null) {
                            log.warn("Can't create unknown Metadata object '" + internalname + "'");
                        } else {
                            md = new Metadata(mdt);
                            md.setValue(content);

                            result.add(md);
                        }
                    }
                } else if (mmo != null && "Corporate".equals(mmo.getType())) {
                    // TODO
                    String internalname = mmo.getInternalName();
                    if (corp == null) {

                        MetadataType mdt = this.myPreferences.getMetadataTypeByName(internalname);
                        if (mdt == null) {
                            log.warn("Can't find MetadataType with internal name '" + internalname + "'");
                            return null;
                        }
                        corp = new Corporate(mdt);
                        corp.setRole(internalname);
                    }
                    if (mmo.getInternalName().equals(corp.getRole())) {
                        if (mmo.isIdentifier()) {
                            // gnd/123456 or gnd123456 or 123456 or (DE-588)123456
                            if (content.contains("/")) {
                                String catalogue = content.substring(0, content.indexOf("/"));
                                String identifier = content.substring(content.indexOf("/") + 1);
                                if ("gnd".equals(catalogue)) {
                                    corp.setAutorityFile(catalogue, "http://d-nb.info/gnd/", identifier);
                                }
                            } else if (content.matches("gnd\\.+")) {
                                corp.setAutorityFile("gnd", "http://d-nb.info/gnd/", content.replace("gnd", ""));
                            } else if (content.matches("\\(.*\\).+")) {
                                corp.setAutorityFile("gnd", "http://d-nb.info/gnd/", content.replaceAll("\\(.*\\)", ""));
                            } else {
                                corp.setAutorityFile("gnd", "http://d-nb.info/gnd/", content);
                            }
                        }
                    }
                    if (mmo.isMainField()) {
                        if (StringUtils.isBlank(corp.getMainName())) {
                            corp.setMainName(content);
                        }
                    }
                    if (mmo.isSubField()) {
                        corp.addSubName(new NamePart("subname", content));
                    }
                    if (mmo.isPartField()) {
                        if (StringUtils.isNotBlank(corp.getPartName())) {
                            corp.setPartName(corp.getPartName() + "; " + content);
                        } else {
                            corp.setPartName(content);
                        }
                    }

                }

                else if (mmo != null && "Person".equals(mmo.getType())) {
                    // It's a person; we can get to this point several times, as
                    // person's metadata information is split over several
                    // subfields.
                    String internalname = mmo.getInternalName();

                    if (per == null) {
                        // Person is not yet instantiated.
                        MetadataType mdt = this.myPreferences.getMetadataTypeByName(internalname);
                        if (mdt == null) {
                            log.warn("Can't find MetadataType with internal name '" + internalname + "'");
                            return null;
                        }
                        per = new Person(mdt);
                        per.setRole(mdt.getName());
                    }
                    if (mmo.getInternalName().equals(per.getRole())) {
                        // Check if mmo is lastname, firstname.
                        if (mmo.isFirstname()) {
                            per.setFirstname(content);
                        }
                        if (mmo.isLastname()) {
                            per.setLastname(content);
                        }
                        if (mmo.isIdentifier()) {
                            // gnd/123456 or gnd123456 or 123456 or (DE-588)123456
                            if (content.contains("/")) {
                                String catalogue = content.substring(0, content.indexOf("/"));
                                String identifier = content.substring(content.indexOf("/") + 1);
                                if ("gnd".equals(catalogue)) {
                                    per.setAutorityFile(catalogue, "http://d-nb.info/gnd/", identifier);
                                }
                            } else if (content.matches("gnd\\.+")) {
                                per.setAutorityFile("gnd", "http://d-nb.info/gnd/", content.replace("gnd", ""));
                            } else if (content.matches("\\(.*\\).+")) {
                                per.setAutorityFile("gnd", "http://d-nb.info/gnd/", content.replaceAll("\\(.*\\)", ""));
                            } else {
                                per.setAutorityFile("gnd", "http://d-nb.info/gnd/", content);
                            }

                        }

                        // Some OPAC (e.g. SWB) don't carry separate subfields for
                        // firstname and lastname. Therefore we try to extract the
                        // name parts from the expansion subfield
                        // ("Expansion der Ansetzungsform").
                        //
                        // <cite>Vorbehaltlich anderslautender Aussagen aus Konstanz
                        // wäre das also unser Weg, an Personenansetzungen
                        // herauzukommen: 028A $8 ..., Name 028A $8 , ... Vorname
                        // 028A $8 , ... / von Vorname mit angehängtem Präfix (nur
                        // "/" fällt weg bei Übernahme nach Goobi) 028A $8 ..., ...
                        // *Jahr-Jahr* Lebensdaten (weglassen oder in eigenes
                        // Metadatenfeld schieben, nicht immer vorhanden).
                        //
                        // 028A $8 @... <...> Name (@ kennzeichnet persönlichen
                        // Namen, kein "," als Steuerzeichen vorhanden, vollständig
                        // mit Sonderzeichen <> in Feld Name übernehmen)</cite>.
                        if (mmo.isExpansion() && content != null) {
                            // Ignore life dates.
                            String heading = content.split("\\*")[0].trim();

                            if (heading.length() > 0) {
                                String lastname = null;
                                String firstname = null;

                                // Personal does not really start with '@', so check
                                // for '<' (i.e. "&lt;") or missing comma.
                                if (heading.contains("&lt;") || heading.contains("<") || !heading.contains(",")) {
                                    // Take personal name as lastname.
                                    heading = heading.replace("&lt;", "<");
                                    heading = heading.replace("&gt;", ">");
                                    lastname = heading.trim();
                                    firstname = "";
                                } else {
                                    // Take the comma as separator of lastname and
                                    // firstname.
                                    lastname = heading.split(",")[0].trim();
                                    firstname = heading.split(",")[1].trim();
                                    firstname = firstname.replace("/", "");
                                    firstname = firstname.replaceAll("\\s+", " ");
                                }
                                if (per.getLastname() == null) {
                                    per.setLastname(lastname);
                                }
                                if (per.getFirstname() == null) {
                                    per.setFirstname(firstname);
                                }
                            }
                        }

                        // Map the function of other involved persons to the
                        // corresponding metadata type (i.e. person). This type is
                        // found by PicaPlus_<field>_<function>, where <field> is
                        // the picafield (e.g. 028C) and <function> is this content
                        // without whitespaces and dots.
                        if (mmo.isFunction() && content != null) {
                            content = content.replaceAll("\\s+", "");
                            content = content.replace(".", "");

                            MetadataType mdt = null;
                            if (content.length() > 0) {
                                internalname = "PicaPlus_" + mmo.getPicaplusField() + "_" + content;
                                mdt = this.myPreferences.getMetadataTypeByName(internalname);
                                per.setType(mdt);
                                per.setRole(internalname);
                            }
                        }

                        // Don't return anything; if it's a person we have to
                        // get all subfields each subfield contains different
                        // information: lastname, firstname etc.
                    }
                }
            }
        }

        // Add Metadata or Person to the result HashSet.
        if (per != null) {
            result.add(per);
        }
        if (corp != null) {
            result.add(corp);
        }

        if (hasNoSubfields) {
            result.add(md);
        }

        // Return NULL if set is empty.
        if (result.isEmpty()) {
            return null;
        }

        // Combine result elements to a metadata group, if requested
        if (metadataGroups.containsKey(fieldAttribute)) {
            MetadataGroupType type = myPreferences.getMetadataGroupTypeByName(metadataGroups.get(fieldAttribute));
            MetadataGroup createdGroup = new MetadataGroup(type);
            for (Serializable content : result) {
                if (content instanceof Person) {
                    createdGroup.addPerson((Person) content);
                } else if (content instanceof Metadata) {
                    createdGroup.addMetadata((Metadata) content);
                } else {
                    log.warn("Can't add a " + content.getClass().getSimpleName() + " to a MetadataGroup.");
                }
            }
            result = new HashSet<>();
            result.add(createdGroup);
        }
        return result;
    }

    /***************************************************************************
     * <p>
     * Finds an appropriate MatchingMetadataObject.
     * </p>
     * 
     * @param theField
     * @param theSubField
     * @return The MatchingMetadataObject or null, if none was found.
     **************************************************************************/
    private MatchingMetadataObject findMMO(String theField, String theSubField) {

        for (MatchingMetadataObject mmo : this.mmoList) {
            if (mmo.getPicaplusField() != null && mmo.getPicaplusField().equals(theField) && mmo.getPicaplusSubfield() != null
                    && mmo.getPicaplusSubfield().equals(theSubField)) {
                // Field and subfield are the same.
                return mmo;
            }

            if (mmo.getPicaplusField() != null && mmo.getPicaplusField().equals(theField) && mmo.getPicaplusSubfield() == null) {
                // Field is the same; no subfield needed as none is defined in
                // the ruleset.
                return mmo;
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Finds an appropriate MatchingMetadataObject.
     * </p>
     * 
     * @param theField
     * @param theSubField
     * @param content
     * @return
     **************************************************************************/
    private MatchingMetadataObject findMMO(String theField, String theSubField, String theContent) {

        for (MatchingMetadataObject mmo : this.mmoList) {
            if (mmo.getPicaplusField() != null && mmo.getPicaplusField().equals(theField) && mmo.getPicaplusSubfield() != null
                    && mmo.getPicaplusSubfield().equals(theSubField) && mmo.getContent() != null
                    && mmo.getContent().equals(theContent.substring(0, mmo.getContent().length()))) {
                return mmo;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#update(java.lang.String)
     */
    @Override
    public boolean update(String filename) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#write(java.lang.String)
     */
    @Override
    public boolean write(String theFilename) throws WriteException {
        return false;
    }

    /***************************************************************************
     * 
     **************************************************************************/
    private class MatchingMetadataGroup {

        private String groupname;
        private String metadatatypename;
        // Delimits the values.
        private String delimiter = "";
        // Is used to store content during processing.
        private StringBuffer content;

        /***********************************************************************
         * Constructor.
         **********************************************************************/
        protected MatchingMetadataGroup() {
            // Nothing to construct here.
        }

        /***********************************************************************
         * @return the groupname
         **********************************************************************/
        public String getGroupname() {
            return this.groupname;
        }

        /***********************************************************************
         * @param groupname the groupname to set
         **********************************************************************/
        public void setGroupname(String groupname) {
            this.groupname = groupname;
        }

        /***********************************************************************
         * @return the metadatatypename
         **********************************************************************/
        public String getMetadatatypename() {
            return this.metadatatypename;
        }

        /***********************************************************************
         * @param metadatatypename the metadatatypename to set
         **********************************************************************/
        public void setMetadatatypename(String metadatatypename) {
            this.metadatatypename = metadatatypename;
        }

        //		/***********************************************************************
        //		 * @return the delimiter
        //		 **********************************************************************/
        //		public String getDelimiter() {
        //			return this.delimiter;
        //		}

        /***********************************************************************
         * @param delimiter the delimiter to set
         **********************************************************************/
        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        /***********************************************************************
         * @param in
         **********************************************************************/
        public void addContent(String in) {

            if (this.content == null) {
                this.content = new StringBuffer();
                this.content.append(in);
            } else {
                this.content.append(this.delimiter);
                this.content.append(in);
            }
        }

        /***********************************************************************
         * @return
         **********************************************************************/
        public String getContent() {

            if (this.content == null) {
                return null;
            }
            return this.content.toString();
        }

    }

    /***************************************************************************
     * <p>
     * Implements an object which translates a PicaPlus record (with a picaplus number and subnumber) to a MetadataType object.
     * </p>
     **************************************************************************/
    @Getter
    @Setter
    private class MatchingMetadataObject {

        private String picaplusField = null;
        private String picaplusSubfield = null;
        private String internalName = null;
        private String type = null;
        private String content = null;
        private String picaplusGroupname = null;
        private String valueCondition = null;
        private String valueRegExp = null;
        private String valueRegExReplacement = null;

        // These are only important, if MMO matches a person.
        private boolean isFirstname = false;
        private boolean isLastname = false;
        private boolean isIdentifier = false;
        private boolean isExpansion = false;
        // Role of other involved.
        private boolean isFunction = false;

        // Fields for corporations
        private boolean isMainField = false;
        private boolean isSubField = false;
        private boolean isPartField = false;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isExportable() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "Pica+";
    }

    @Override
    public void setPrefs(Prefs prefs) throws PreferencesException {
        myPreferences = prefs;
    }

    @Override
    public void setGoobiID(String goobiId) {
        // TODO Auto-generated method stub

    }
}
