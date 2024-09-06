package ugh.dl;

/*******************************************************************************
 * ugh.dl / Prefs.java
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lombok.extern.log4j.Log4j2;
import ugh.exceptions.PreferencesException;

/*******************************************************************************
 * <b>Title:</b> Preferences
 * 
 * <b>Description:</b> Reads global preferences (ruleset files) and provides methods to access information and retrieve information about
 * <code>MetadataType</code> and <code>DocStructType</code> objects.
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2013-05-08
 * @since 2004-05-21
 * 
 *        TODOLOG
 * 
 * 
 *        CHANGELOG
 * 
 *        13.02.2010 --- Funk --- Refactored some conditionals and loops.
 * 
 *        14.12.2009 --- Funk --- Added the getAllAnchorDocStructTypes() method.
 * 
 *        17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *        30.10.2009 --- Funk --- Added generated serialVersionUID.
 * 
 *        24.10.2008 --- Funk --- Commented out the setting of: "current_DocStrctType.setMyPrefs(this);". Do we need that? I think not!
 * 
 *        29.09.2008 --- Funk --- Added log4j logging, removed the debug level methods.
 * 
 *        29.04.2008 --- Funk --- Added public setDebug() method.
 * 
 ******************************************************************************/

@Log4j2
public class Prefs implements Serializable {

    private static final String VERSION = "1.1-20091117";

    private static final long serialVersionUID = 6162006030440683152L;
    private static final String HIDDEN_METADATA_CHAR = "_";

    private List<DocStructType> allDocStrctTypes;
    private List<MetadataType> allMetadataTypes;
    private List<MetadataGroupType> allMetadataGroupTypes;
    private transient Map<String, Node> allFormats;

    public static final short ELEMENT_NODE = 1;

    /***************************************************************************
     * <p>
     * Constructor.
     * </p>
     **************************************************************************/
    public Prefs() {
        this.allDocStrctTypes = new LinkedList<>();
        this.allMetadataTypes = new LinkedList<>();
        this.allMetadataGroupTypes = new LinkedList<>();
        this.allFormats = new HashMap<>();
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getVersion() {
        return VERSION;
    }

    /***************************************************************************
     * <p>
     * Loads all known DocStruct types from the prefs XML file.
     * </p>
     * 
     * @param filename
     * @return
     * @throws PreferencesException
     **************************************************************************/
    public boolean loadPrefs(String filename) throws PreferencesException {

        Document document;
        NodeList childlist;
        NodeList upperChildlist;
        Node upperchild;
        // Single node of the childlist.
        Node currentNode;
        // New document builder instance
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Do not validate xml file.
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(false);

        DocStructType parsedDocStrctType;
        MetadataType parsedMetadataType;

        MetadataGroupType parsedMetadataGroup;

        // Read file and parse it.
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(filename));
        } catch (SAXParseException e) {
            // Error generated by the parser.
            String message = "Parse error at line " + e.getLineNumber() + ", uri " + e.getSystemId() + "!";
            log.error(message);
            throw new PreferencesException(message, e);
        } catch (SAXException | ParserConfigurationException e) {
            log.error(e);
            throw new PreferencesException(e);
        } catch (IOException e) {
            String message = "Unable to load preferences file '" + filename + "'!";
            log.error(message);
            throw new PreferencesException(message, e);
        }

        // File was parsed; DOM was created.
        //
        // Parse the DOM.
        upperChildlist = document.getElementsByTagName("Preferences");
        if (upperChildlist == null || upperChildlist.getLength() == 0) {
            // No preference element -> wrong XML file.
            String message = "Preference file does not begin with <Preferences> element!";
            log.error(message);
            throw new PreferencesException(message);
        }

        // Get first preferences element.
        upperchild = upperChildlist.item(0);
        if (upperchild == null) {
            String message = "No upper child in preference file";
            log.error(message);
            throw new PreferencesException(message);
        }

        childlist = upperchild.getChildNodes();
        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            currentNode = childlist.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if ("DocStrctType".equals(currentNode.getNodeName())) {
                    NamedNodeMap nnm = currentNode.getAttributes();
                    Node topmost = nnm.getNamedItem("topStruct");
                    Node fileset = nnm.getNamedItem("fileset");
                    String topmostvalue = null;
                    String filesetvalue = null;
                    parsedDocStrctType = null;
                    if (topmost != null) {
                        topmostvalue = topmost.getNodeValue();
                    }
                    if (fileset != null) {
                        filesetvalue = fileset.getNodeValue();
                    }
                    parsedDocStrctType = parseDocStrctType(currentNode);
                    if (parsedDocStrctType != null) {
                        if (topmostvalue != null && ("yes".equals(topmostvalue) || "true".equals(topmostvalue))) {
                            parsedDocStrctType.setTopmost(true);
                        }
                        if (filesetvalue != null && ("no".equals(filesetvalue) || "false".equals(filesetvalue))) {
                            parsedDocStrctType.setHasFileSet(false);
                        }

                        this.allDocStrctTypes.add(parsedDocStrctType);
                    }
                }

                if ("MetadataType".equals(currentNode.getNodeName())) {
                    parsedMetadataType = parseMetadataType(currentNode);
                    if (parsedMetadataType != null) {
                        this.allMetadataTypes.add(parsedMetadataType);
                    }
                }

                if ("Group".equals(currentNode.getNodeName())) {
                    parsedMetadataGroup = parseMetadataGroup(currentNode);
                    if (parsedMetadataGroup != null) {
                        this.allMetadataGroupTypes.add(parsedMetadataGroup);
                    }
                }

                if ("Formats".equals(currentNode.getNodeName())) {
                    // Get all formats.
                    NodeList formatlist = currentNode.getChildNodes();
                    for (int x = 0; x < formatlist.getLength(); x++) {
                        Node currentnode = formatlist.item(x);
                        if (currentnode.getNodeType() == ELEMENT_NODE) {
                            this.allFormats.put(currentnode.getNodeName(), currentnode);
                        }
                    }
                }
            }
        }

        // Add internal metadata types; all internal metadata types are
        // beginning with HIDDEN_METADATA_CHAR.
        MetadataType mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "pagephysstart");
        this.allMetadataTypes.add(mdt);

        mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "overlapping");
        this.allMetadataTypes.add(mdt);

        mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "pagephysend");
        this.allMetadataTypes.add(mdt);

        mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "PaginationNo");
        this.allMetadataTypes.add(mdt);

        return true;
    }

    /***************************************************************************
     * <p>
     * Parses just the part of the XML-file which contains information about a single DocStrctType (everything inside the DocStrctType element).
     * </p>
     * 
     * @param theDocStrctTypeNode
     * @return DocStructType instance
     * @throws PreferencesException
     **************************************************************************/
    public DocStructType parseDocStrctType(Node theDocStrctTypeNode) {

        NodeList allchildren;
        // NamedNodeMap containing all attributes.
        NamedNodeMap attributeNodelist;
        // Node containing a single Attribute.
        Node attribNode;
        // Attribute containing information, if metadata type should be
        // displayed, even if it has no content.
        Node defaultNode;
        // Attribute containing information, if metadata type should be
        // displayed, if it has content.
        Node invisibleNode;
        // Single node from allchildren nodeList.
        Node currentNode;

        String languageName;
        String languageValue;
        String mdtypeName;
        String mdtypeNum;
        String allowedChild;
        DocStructType currentDocStrctType = new DocStructType();

        // Get all children.
        allchildren = theDocStrctTypeNode.getChildNodes();
        HashMap<String, String> allLanguages = new HashMap<>();

        // Get attributes for docstructtype first.
        //
        // Get all attributes.
        NamedNodeMap attrnodes = theDocStrctTypeNode.getAttributes();
        if (attrnodes != null) {
            // Check if it's an anchor.
            Node typenode = attrnodes.item(0);
            if (typenode != null && "anchor".equals(typenode.getNodeName()) && "true".equalsIgnoreCase(typenode.getNodeValue())) {
                currentDocStrctType.isAnchor(true);
            }
        }

        for (int i = 0; i < allchildren.getLength(); i++) {
            currentNode = allchildren.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if ("Name".equals(currentNode.getNodeName())) {
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Error reading config for DocStrctType unknown (error code p004a)");
                            // No text node available; maybe is's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        currentDocStrctType.setName(textnode.getNodeValue());
                    } else {
                        // Error; Name-element is empty element.
                        log.error("Error reading config for DocStrctType unknown (error code p004)");
                        return null;
                    }
                }
                if ("language".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    attribNode = attributeNodelist.getNamedItem("name");
                    if (attribNode == null) {
                        log.error("No name definition for language (" + currentDocStrctType.getName() + "); " + currentDocStrctType.getName()
                                + "; Error Code: p005");
                        return null;
                    }
                    languageName = attribNode.getNodeValue().trim();
                    // Get value; value is always a text node.
                    languageValue = "";
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Error reading config for DocStrctType " + currentDocStrctType.getName() + "; Error Code: p006");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        languageValue = textnode.getNodeValue().trim();
                    }

                    if (languageName == null || languageValue == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    allLanguages.put(languageName, languageValue);
                }
                // Read all types which are allowed for this documentstructure
                // type.
                if ("metadata".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    attribNode = attributeNodelist.getNamedItem("num");
                    defaultNode = attributeNodelist.getNamedItem("DefaultDisplay");
                    invisibleNode = attributeNodelist.getNamedItem("Invisible");

                    if (attribNode == null) {
                        mdtypeNum = "1";
                        log.warn("Num attribute not set for <metadata> element!");
                    } else {
                        // Get max. number: 1,+,*
                        mdtypeNum = attribNode.getNodeValue();
                    }
                    // Get value for DefaultDisplay attribute.
                    String defaultValue = null;
                    if (defaultNode != null) {
                        defaultValue = defaultNode.getNodeValue();
                    }
                    mdtypeName = "";

                    String invisibleValue = null;
                    if (invisibleNode != null) {
                        invisibleValue = invisibleNode.getNodeValue();
                    }

                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Node is not of type text");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        mdtypeName = textnode.getNodeValue().trim();
                    }

                    if (mdtypeName == null || mdtypeNum == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }

                    MetadataType newMdType = getMetadataTypeByName(mdtypeName);
                    if (newMdType == null) {
                        log.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! MetadataType '" + mdtypeName
                                + "' is unknown");
                        return null;
                    }
                    // Set max. number.
                    newMdType.setNum(mdtypeNum);
                    PrefsType result = null;

                    // Handle Invisible attribute.
                    boolean invisible = false;
                    if (invisibleValue != null && ("true".equalsIgnoreCase(invisibleValue) || "yes".equalsIgnoreCase(invisibleValue))) {
                        invisible = true;
                    }

                    // Handle DefaultDisplay attribute.
                    if (defaultValue != null) {
                        if ("true".equalsIgnoreCase(defaultValue) || "yes".equalsIgnoreCase(defaultValue)) {
                            result = currentDocStrctType.addMetadataType(newMdType, mdtypeNum, true, invisible);
                        } else {
                            result = currentDocStrctType.addMetadataType(newMdType, mdtypeNum, false, invisible);
                        }
                    } else {
                        result = currentDocStrctType.addMetadataType(newMdType, mdtypeNum);
                    }

                    if (result == null) {
                        // Error occured; so exit this method; no new
                        // DocStrctType.
                        log.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Can't add metadatatype '"
                                + newMdType.getName() + "'");
                        return null;
                    }
                }

                if ("group".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    attribNode = attributeNodelist.getNamedItem("num");
                    defaultNode = attributeNodelist.getNamedItem("DefaultDisplay");
                    invisibleNode = attributeNodelist.getNamedItem("Invisible");

                    if (attribNode == null) {
                        mdtypeNum = "1";
                        log.warn("Num attribute not set for <group> element!");
                    } else {
                        // Get max. number: 1,+,*
                        mdtypeNum = attribNode.getNodeValue();
                    }
                    // Get value for DefaultDisplay attribute.
                    String defaultValue = null;
                    if (defaultNode != null) {
                        defaultValue = defaultNode.getNodeValue();
                    }
                    mdtypeName = "";

                    String invisibleValue = null;
                    if (invisibleNode != null) {
                        invisibleValue = invisibleNode.getNodeValue();
                    }

                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Node is not of type text");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        mdtypeName = textnode.getNodeValue().trim();
                    }

                    if (mdtypeName == null || mdtypeNum == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }

                    MetadataGroupType newMdGroup = getMetadataGroupTypeByName(mdtypeName);
                    if (newMdGroup == null) {
                        log.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! MetadataType '" + mdtypeName
                                + "' is unknown");
                        return null;
                    }
                    // Set max. number.
                    newMdGroup.setNum(mdtypeNum);
                    MetadataGroupType result = null;

                    // Handle Invisible attribute.
                    boolean invisible = false;
                    if (invisibleValue != null && ("true".equalsIgnoreCase(invisibleValue) || "yes".equalsIgnoreCase(invisibleValue))) {
                        invisible = true;
                    }

                    // Handle DefaultDisplay attribute.
                    if (defaultValue != null) {
                        if ("true".equalsIgnoreCase(defaultValue) || "yes".equalsIgnoreCase(defaultValue)) {
                            result = currentDocStrctType.addMetadataGroup(newMdGroup, mdtypeNum, true, invisible);
                        } else {
                            result = currentDocStrctType.addMetadataGroup(newMdGroup, mdtypeNum, false, invisible);
                        }
                    } else {
                        result = currentDocStrctType.addMetadataGroup(newMdGroup, mdtypeNum);
                    }

                    if (result == null) {
                        // Error occured; so exit this method; no new
                        // DocStrctType.
                        log.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Can't add metadatatype '"
                                + newMdGroup.getName() + "'");
                        return null;
                    }
                }

                // Read type of DocStruct, which is allowed as children.
                if ("allowedchildtype".equals(currentNode.getNodeName())) {
                    allowedChild = "";
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Syntax Error reading config for DocStrctType '" + currentDocStrctType.getName()
                                    + "'! Expected a text node under <allowedchildtype> element containing the DocStructType's name");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        allowedChild = textnode.getNodeValue().trim();
                    }

                    // Check, if an appropriate DocStruct Type is defined.
                    boolean bResult = currentDocStrctType.addDocStructTypeAsChild(allowedChild);
                    if (!bResult) {
                        // Error occured; so exit this method; no new
                        // DocStrctType.
                        log.error("Error reading config for DocStructType '" + currentDocStrctType.getName() + "'! Can't addDocStructType as child '"
                                + allowedChild + "'");
                        return null;
                    }
                }
            }

            // Add allLanguages and all Metadata to DocStrctType.
            currentDocStrctType.setAllLanguages(allLanguages);
        }

        return currentDocStrctType;
    }

    /***************************************************************************
     * @param theMetadataTypeNode
     * @return
     **************************************************************************/
    public MetadataType parseMetadataType(Node theMetadataTypeNode) {
        NodeList allchildren;
        // NamedNodeMap containing all attributes.
        NamedNodeMap attributeNodelist;
        // Node containing a single Attribute.
        Node attributeNode;
        // Single node from allchildren nodeList.
        Node currentNode;

        String languageName;
        String languageValue;
        String validationExpression = "";
        HashMap<String, String> allLanguages = new HashMap<>();
        Map<String, String> validationErrorMessages = new HashMap<>();
        MetadataType currenMdType = new MetadataType();

        NamedNodeMap nnm = theMetadataTypeNode.getAttributes();
        // Get type attribute.
        Node node = nnm.getNamedItem("type");
        if (node != null) {
            String nodevalue = node.getNodeValue();
            if (nodevalue != null) {
                switch (nodevalue) {
                    case "person":
                        currenMdType.setIsPerson(true);
                        break;
                    case "identifier":
                        currenMdType.setIdentifier(true);
                        break;
                    case "corporate":
                        currenMdType.setCorporate(true);
                        break;
                    default:
                }
            }

        }

        Node normdata = nnm.getNamedItem("normdata");
        if (normdata != null) {
            String nodevalue = normdata.getNodeValue();
            if (nodevalue != null && "true".equals(nodevalue)) {
                currenMdType.setAllowNormdata(true);
            }
        }

        Node restriction = nnm.getNamedItem("allowAccessRestriction");
        if (restriction != null) {
            String nodevalue = restriction.getNodeValue();
            if (nodevalue != null && "true".equals(nodevalue)) {
                currenMdType.setAllowAccessRestriction(true);
            }
        }

        Node additional = nnm.getNamedItem("namepart");
        if (additional != null) {
            String nodevalue = additional.getNodeValue();
            if (nodevalue != null && "true".equals(nodevalue)) {
                currenMdType.setAllowNameParts(true);
            }
        }

        allchildren = theMetadataTypeNode.getChildNodes(); // get allchildren
        for (int i = 0; i < allchildren.getLength(); i++) {
            currentNode = allchildren.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if ("Name".equals(currentNode.getNodeName())) {
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            log.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p002b! Expected a text node under <Name> attribute at '" + theMetadataTypeNode.getNodeName()
                                    + "'. <Name> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error(
                                    "Error reading config for MetadataType unknown; Error Code: p002! Expected a text node under <Name> element at '"
                                            + theMetadataTypeNode.getNodeName() + "'");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        currenMdType.setName(textnode.getNodeValue().trim());
                    }
                } else if ("language".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    attributeNode = attributeNodelist.getNamedItem("name");
                    languageName = attributeNode.getNodeValue().trim();
                    // Get value; value is always a text node.
                    languageValue = "";
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            log.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p001b! Expected a text node under <language> attribute at '" + theMetadataTypeNode.getNodeName()
                                    + "'. <language> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p001! Wrong node type under <language> attribute - a text node was expected at "
                                    + theMetadataTypeNode.getNodeName());
                            return null;
                        }
                        languageValue = textnode.getNodeValue().trim();
                    }
                    if (languageName == null || languageValue == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    allLanguages.put(languageName, languageValue);
                } else if ("validationExpression".equals(currentNode.getNodeName())) {
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        validationExpression = textnode.getNodeValue();
                    }
                } else if ("validationErrorMessage".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    attributeNode = attributeNodelist.getNamedItem("name");
                    String lang = attributeNode.getNodeValue();

                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            log.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p001b! Expected a text node under <language> attribute at '" + theMetadataTypeNode.getNodeName()
                                    + "'. <language> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p001! Wrong node type under <language> attribute - a text node was expected at "
                                    + theMetadataTypeNode.getNodeName());
                            return null;
                        }
                        String value = textnode.getNodeValue();
                        validationErrorMessages.put(lang, value);
                    }
                }
            }
        }

        // Add allLanguages and all Metadata to DocStrctType.
        currenMdType.setAllLanguages(allLanguages);
        currenMdType.setValidationExpression(validationExpression);
        currenMdType.setValidationErrorMessages(validationErrorMessages);
        return currenMdType;
    }

    /***************************************************************************
     * @param theMetadataTypeNode
     * @return
     **************************************************************************/
    public MetadataGroupType parseMetadataGroup(Node theMetadataGroupNode) {

        NodeList allchildren;
        // NamedNodeMap containing all attributes.
        NamedNodeMap attributeNodelist;
        // Node containing a single Attribute.
        Node attributeNode;
        // Single node from allchildren nodeList.
        Node currentNode;

        String languageName;
        String languageValue;
        HashMap<String, String> allLanguages = new HashMap<>();

        MetadataGroupType currenGroup = new MetadataGroupType();

        String mdtypeName;
        allchildren = theMetadataGroupNode.getChildNodes(); // get allchildren
        for (int i = 0; i < allchildren.getLength(); i++) {
            currentNode = allchildren.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if ("Name".equals(currentNode.getNodeName())) {
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            log.error("Syntax Error reading config for MetadataGroup! Expected a text node under <Name> attribute at '"
                                    + theMetadataGroupNode.getNodeName() + "'. <Name> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error(
                                    "Error reading config for MetadataType unknown; Error Code: p002! Expected a text node under <Name> element at '"
                                            + theMetadataGroupNode.getNodeName() + "'");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        currenGroup.setName(textnode.getNodeValue());
                    }
                }

                String mdtypeNum = "*";
                boolean defaultDisplay = false;
                if ("metadata".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    if (attributeNodelist != null) {
                        Node attribNode = attributeNodelist.getNamedItem("num");
                        if (attribNode != null) {
                            mdtypeNum = attribNode.getNodeValue();
                        }
                        Node defaultNode = attributeNodelist.getNamedItem("DefaultDisplay");
                        if (defaultNode != null && "true".equals(defaultNode.getNodeValue())) {
                            defaultDisplay = true;
                        }
                    }
                    mdtypeName = "";

                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();

                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Error reading config for DocStrctType '" + currenGroup.getName() + "'! Node is not of type text");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        mdtypeName = textnode.getNodeValue().trim();
                    }

                    if (mdtypeName == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    // MetadataType newMDType=new MetadataType();
                    // newMDType.setName(mdtype_name);
                    MetadataType newMdType = getMetadataTypeByName(mdtypeName);
                    if (newMdType == null) {
                        log.error("Error reading config for DocStrctType '" + currenGroup.getName() + "'! MetadataType '" + mdtypeName
                                + "' is unknown");
                        return null;
                    }
                    newMdType.setNum(mdtypeNum);
                    currenGroup.addMetadataType(newMdType, mdtypeNum, defaultDisplay, false);
                } else if ("group".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    Node attribNode = attributeNodelist.getNamedItem("num");
                    Node defaultNode = attributeNodelist.getNamedItem("DefaultDisplay");

                    if (attribNode == null) {
                        mdtypeNum = "1";
                        log.warn("Num attribute not set for <group> element!");
                    } else {
                        // Get max. number: 1,+,*
                        mdtypeNum = attribNode.getNodeValue();
                    }
                    boolean defaultValue = false;
                    if (defaultNode != null && "true".equals(defaultNode.getNodeValue())) {
                        defaultValue = true;
                    }

                    String groupName = null;
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Syntax Error reading config for group '" + currenGroup.getName()
                                    + "'! Expected a text node element containing the group's name");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        groupName = textnode.getNodeValue().trim();
                    }
                    boolean invisibleValue = groupName.startsWith("_");
                    currenGroup.addGroupTypeAsChild(groupName, mdtypeNum, defaultValue, invisibleValue);

                } else if ("language".equals(currentNode.getNodeName())) {
                    attributeNodelist = currentNode.getAttributes();
                    attributeNode = attributeNodelist.getNamedItem("name");
                    languageName = attributeNode.getNodeValue();
                    // Get value; value is always a text node.
                    languageValue = "";
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            log.error("Syntax Error reading config for MetadataType " + currenGroup.getName()
                                    + "; Error Code: p001b! Expected a text node under <language> attribute at '" + theMetadataGroupNode.getNodeName()
                                    + "'. <language> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            log.error("Syntax Error reading config for MetadataType " + currenGroup.getName()
                                    + "; Error Code: p001! Wrong node type under <language> attribute - a text node was expected at "
                                    + theMetadataGroupNode.getNodeName());
                            return null;
                        }
                        languageValue = textnode.getNodeValue().trim();
                    }
                    if (languageName == null || languageValue == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    allLanguages.put(languageName, languageValue);
                }
            }
        }

        // Add allLanguages and all Metadata to DocStrctType.
        currenGroup.setAllLanguages(allLanguages);

        return currenGroup;
    }

    /***************************************************************************
     * <p>
     * Checks, if MetadataType is allowed for given DocStrctType returns the DocStructType, otherwise null.
     * </p>
     * 
     * @param theName
     * @return
     **************************************************************************/
    public DocStructType getDocStrctTypeByName(String theName) {

        for (DocStructType currentDocStructType : this.allDocStrctTypes) {
            if (currentDocStructType.getName().equals(theName)) {
                return currentDocStructType;
            }
        }

        return null;
    }

    /**************************************************************************
     * <p>
     * Gets all anchor DocStrctTypes defined in the Prefs.
     * </p>
     * 
     * @return A List of all anchor DocStructTypes defined in the Prefs if some are existing, an empty list otherwise.
     **************************************************************************/
    public List<DocStructType> getAllAnchorDocStructTypes() {

        List<DocStructType> result = new LinkedList<>();

        // Get all DocStructTypes.
        List<DocStructType> allTypes = this.getAllDocStructTypes();
        if (allTypes != null) {
            // Iterate...
            for (DocStructType dst : allTypes) {
                // ...and add to result list if anchor DocStruct.
                if (dst.isAnchor()) {
                    result.add(dst);
                }
            }
        }

        return result;
    }

    /***************************************************************************
     * @param name
     * @param inLanguage
     * @return
     **************************************************************************/
    public DocStructType getDocStrctTypeByName(String name, String inLanguage) {

        DocStructType currentDocStrctType;
        Map<String, String> allLanguages;
        String checklanguage;
        String checklanguagevalue = "";

        for (DocStructType element : this.allDocStrctTypes) {
            currentDocStrctType = element;
            // Get all languages.
            allLanguages = currentDocStrctType.getAllLanguages();

            // Find language "inLanguage".
            for (Entry<String, String> entry : allLanguages.entrySet()) {
                checklanguage = entry.getKey();
                checklanguagevalue = entry.getValue();
                if (checklanguage.equals(inLanguage)) {
                    break;
                }
            }

            if (!"".equals(checklanguagevalue) && checklanguagevalue.equals(name)) {
                // Found DocStrctType.
                return currentDocStrctType;
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Provides access for FileFormat implementations to read their preferences. The preferences of FileFormats are included in the global preference
     * file (in section formats). This method just retrieves the Node element from the DOM tree, which contains the whole configuration.
     * </p>
     * <p>
     * It is up to the FileFormat implementation to parse this configuration.
     * </p>
     * 
     * @param in name of fileformat (Excel, RDF, METS....), which is the name of the node.
     * @return a DOM Node objects, which contains the whole configuration for this requested FileFormat.
     **************************************************************************/
    public Node getPreferenceNode(String in) {

        if (!this.allFormats.containsKey(in)) {
            // Format not available.
            return null;
        }
        return this.allFormats.get(in);

    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public List<MetadataType> getAllMetadataTypes() {
        return this.allMetadataTypes;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public List<DocStructType> getAllDocStructTypes() {
        return this.allDocStrctTypes;
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean addMetadataType(MetadataType inType) {

        PrefsType tempType;

        if (inType == null || inType.getName() == null) {
            return false;
        }
        if (getMetadataTypeByName(inType.getName()) == null) {
            // still not available, so add ist
            this.allMetadataTypes.add(inType);
            return true;
        }

        tempType = getMetadataTypeByName(inType.getName());
        // Remove old.
        this.allMetadataTypes.remove(tempType);
        // Add new.
        this.allMetadataTypes.add(inType);

        return true;
    }

    /***************************************************************************
     * <p>
     * Returns all metadataType instances which represents a person. These metadata types have a "type"-attribute with the value "person".
     * </p>
     * 
     * @return List containing MetadataType attributes
     **************************************************************************/
    public List<MetadataType> getAllPersonTypes() {

        MetadataType currentMdType;
        List<MetadataType> allPersons = new LinkedList<>();

        for (MetadataType element : this.allMetadataTypes) {
            currentMdType = element;
            if (currentMdType.getIsPerson()) {
                allPersons.add(currentMdType);
            }
        }

        return allPersons;
    }

    /***************************************************************************
     * <p>
     * Needs string as parameter and returns MetadataType object with this name.
     * </p>
     * 
     * @param name
     * @return
     **************************************************************************/
    public MetadataType getMetadataTypeByName(String name) {

        MetadataType currentMdType;
        String checkname;

        for (MetadataType element : this.allMetadataTypes) {
            currentMdType = element;
            checkname = currentMdType.getName();

            if (checkname.equals(name)) {
                // Found MetadataType.
                return currentMdType;
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Needs string as parameter and returns MetadataGroup object with this name.
     * </p>
     * 
     * @param name
     * @return
     **************************************************************************/
    public MetadataGroupType getMetadataGroupTypeByName(String name) {

        MetadataGroupType currentMdGroup;
        String checkname;

        for (MetadataGroupType element : this.allMetadataGroupTypes) {
            currentMdGroup = element;
            checkname = currentMdGroup.getName();

            if (checkname.equals(name)) {
                // Found MetadataType.
                return currentMdGroup;
            }
        }

        return null;
    }

    /***************************************************************************
     * @param name
     * @param inLanguage
     * @return
     **************************************************************************/
    public PrefsType getMetadataTypeByName(String name, String inLanguage) {

        PrefsType currentMdType;
        Map<String, String> allLanguages;
        String checklanguage;
        String checklanguagevalue = "";

        for (MetadataType element : this.allMetadataTypes) {
            currentMdType = element;

            // Get all languages.
            allLanguages = currentMdType.getAllLanguages();
            if (allLanguages == null) {
                if (!(HIDDEN_METADATA_CHAR.equals(currentMdType.getName().substring(0, 1)))) {
                    log.debug("MetadataType without language definition:" + currentMdType.getName());
                }

                // No languages available for this MetadataType.
                continue;
            }

            // Find language "inLanguage".
            for (Entry<String, String> entry : allLanguages.entrySet()) {
                checklanguage = entry.getKey();
                checklanguagevalue = entry.getValue();
                if (checklanguage.equals(inLanguage)) {
                    break;
                }
            }

            if (!"".equals(checklanguagevalue) && checklanguagevalue.equals(name)) {
                // Found MetadataType.
                return currentMdType;
            }
        }

        return null;
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean addMetadataGroup(MetadataGroupType inGroup) {

        MetadataGroupType tempType;

        if (inGroup == null || inGroup.getName() == null) {
            return false;
        }
        if (getMetadataGroupTypeByName(inGroup.getName()) == null) {
            // still not available, so add ist
            this.allMetadataGroupTypes.add(inGroup);
            return true;
        }

        tempType = getMetadataGroupTypeByName(inGroup.getName());
        // Remove old.
        this.allMetadataGroupTypes.remove(tempType);
        // Add new.
        this.allMetadataGroupTypes.add(inGroup);

        return true;
    }
}
