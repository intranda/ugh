package ugh.fileformats.mets;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/*******************************************************************************
 * ugh.fileformats.mets / MetsModsImportExport.java
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

import gov.loc.mods.v3.ModsDocument;
import lombok.extern.log4j.Log4j2;
import ugh.dl.AmdSec;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.ExportFileformat;
import ugh.dl.Md;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.PrefsType;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.ImportException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

/*******************************************************************************
 * @author Stefan Funk
 * @author Robert Sehr
 * @version 2013-05-08
 * @since 2009-05-09
 * 
 * 
 *        CHANGELOG
 * 
 *        05.05.2010 --- Funk --- Added displayName check at displayName creation time. --- Added some DPD-407 debugging outputs (commented out).
 * 
 *        10.03.2010 --- Funk --- Added ValueRegExps to AnchorIdentifier. --- References to anchor is written only once now, and not for every
 *        identifier mapping in the prefs.
 * 
 *        03.03.2010 --- Funk --- ORDERLABEL uncounted things corrected. No additional tags are written anymore.
 * 
 *        24.02.2010 --- Funk --- ORDERLABEL only is filled with data, if a metadata value is existing. "uncounted" pages are not written as
 *        ORDERLABEL anymore. --- Empty nodes (nodes without value) in RegExp conditions are NOT created anymore.
 * 
 *        15.02.2010 --- Funk --- Logging version information now. --- Added the RegExp things for the AMD section to the documentation.
 * 
 * 
 *        12.02.2010 --- Funk --- Added RegExp support for amdsec setters.
 * 
 *        22.01.2010 --- Funk --- findbugs improvements.
 * 
 *        18.01.2010 --- Funk --- Adapted class to changed DocStruct.getAllMetadataByType().
 * 
 *        22.12.2009 --- Funk --- Grouping of MODS elements added.
 * 
 *        08.12.2009 --- Funk --- Moved the trim() calls some lines down to avoid NPEs.
 * 
 *        04.12.2009 --- Funk --- Added trim() to all METS prefs values' getTextNodeValue() calls.
 * 
 *        21.11.2009 --- Funk --- Added ValueCondition and ValueRegExp to MODS mapping.
 * 
 *        06.11.2009 --- Funk --- Beautified code for Sonar ranking improvement!
 * 
 *        26.10.2009 --- Funk --- Removed the constructor without Prefs object. We really need that Prefs thing!
 * 
 *        09.10.2009 --- Funk --- Changed the authority for the PND ID to "gbv".
 * 
 *        06.10.2009 --- Funk --- Corrected some not-conform-to-rules variable names.
 * 
 *        05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *        24.09.2009 --- Funk --- Refactored all the Exception things.
 * 
 *        21.09.2009 --- Funk --- Moved the class readPrefs from MetsMods, and put it into MetsModeImportExport. It is only needed here.
 * 
 *        11.09.2009 --- Funk --- Using more String finals now.
 * 
 *        04.09.2009 --- Funk --- Catch all the NULLs in the METS setters. If a value is "", do not create a METS tag, or throw an exception if
 *        necesarry.
 * 
 *        20.08.2009 --- Funk --- Removed the xPathAnchorReference attribute from this class. We must use the one from MetsMods!
 * 
 *        28.07.2009 --- Funk --- Added PURLs as CONTENTIDS to logical structmap div.
 * 
 *        24.07.2009 --- Funk --- Added version string.
 * 
 *        22.07.2009 --- Funk --- Added HTML tags to JavaDOC.
 * 
 *        26.06.2009 --- Funk --- ADMSEC is written only here now.
 * 
 *        22.06.2009 --- Funk --- <physicalDescription><digitalOrigin> added if a _digitalOrigin metadata is existing in the prefs and a
 *        CatalogIDDigital is set.
 * 
 *        19.06.2009 -- Funk --- writePhysDMD integriert, nun wird auch MODS in der PhysDmd geschrieben.
 * 
 *        18.06.2009 --- Funk --- Generalised the WriteLogDMD() method, using WriteMODS() now.
 * 
 *        09.06.2009 --- Funk --- Added authority and ID attributes to the person name MODS part (yet still hard coded).
 * 
 *        30.04.2009 --- Funk --- Added setPurlFromCatalogIDDigital() to put in Purls from PPNs.
 * 
 *        22.04.2009 --- Funk --- Corrected some logging things. --- Added methods writePhysDivs and writeLogDivs, here the METS DocStructTypes must
 *        be mapped from the prefs.
 * 
 *        31.03.2009 --- Funk --- Added the method checkForAnchorReference, because we need another one here.
 * 
 *        27.03.2009 --- Funk --- Class created.
 * 
 ******************************************************************************/
@Log4j2
public class MetsModsImportExport extends ugh.fileformats.mets.MetsMods implements ExportFileformat {

    /***************************************************************************
     * VERSION STRING
     **************************************************************************/

    private static final String VERSION = "1.9-20100505";

    /***************************************************************************
     * STATIC FINALS
     **************************************************************************/

    protected static final String METS_PREFS_XPATH_STRING = "XPath";
    protected static final String METS_PREFS_WRITEXPATH_STRING = "WriteXPath";
    protected static final String METS_PREFS_FIRSTNAMEXPATH_STRING = "FirstnameXPath";
    protected static final String METS_PREFS_LASTNAMEXPATH_STRING = "LastnameXPath";
    protected static final String METS_PREFS_AFFILIATIONXPATH_STRING = "AffiliationXPath";
    protected static final String METS_PREFS_DISPLAYNAMEXPATH_STRING = "DisplayNameXPath";
    protected static final String METS_PREFS_PERSONTYPEXPATH_STRING = "PersonTypeXPath";
    protected static final String METS_PREFS_AUTHORITYFILEIDXPATH_STRING = "AuthorityFileIDXPath";
    protected static final String METS_PREFS_IDENTIFIERXPATH_STRING = "IdentifierXPath";
    protected static final String METS_PREFS_IDENTIFIERTYPEXPATH_STRING = "IdentifierTypeXPath";
    protected static final String METS_PREFS_READMODSNAME_STRING = "ReadModsName";
    protected static final String METS_PREFS_WRITEMODSNAME_STRING = "WriteModsName";
    protected static final String METS_PREFS_MODSAUTHORITY_STRING = "MODSAuthority";
    protected static final String METS_PREFS_MODSENCODING_STRING = "MODSEncoding";
    protected static final String METS_PREFS_MODSID_STRING = "MODSID";
    protected static final String METS_PREFS_MODSLANG_STRING = "MODSLang";
    protected static final String METS_PREFS_MODSSCRIPT_STRING = "MODSScript";
    protected static final String METS_PREFS_MODSTRANSLITERATION_STRING = "MODSTransliteration";
    protected static final String METS_PREFS_MODSTYPE_STRING = "MODSType";
    protected static final String METS_PREFS_MODSXMLLANG_STRING = "MODSXMLLang";
    protected static final String METS_PREFS_VALUECONDITION_STRING = "ValueCondition";
    protected static final String METS_PREFS_VALUEREGEXP_STRING = "ValueRegExp";
    protected static final String METS_PREFS_VALUEREPLACEMENT_STRING = "ValueReplacement";

    protected static final String METS_PREFS_DATABASE_SOURCE = "DatabaseXpath";
    protected static final String METS_PREFS_DATABASE_IDENTIFIER = "IdentifierXpath";

    protected static final String METS_PREFS_MAINNAMEXPATH_STRING = "MainNameXPath";
    protected static final String METS_PREFS_SUBNAMEXPATH_STRING = "SubNameXPath";
    protected static final String METS_PREFS_PARTNAMEXPATH_STRING = "PartNameXPath";

    protected static final String METS_RIGHTS_OWNER_STRING = "rightsOwner";
    protected static final String METS_RIGHTS_OWNER_LOGO_STRING = "rightsOwnerLogo";
    protected static final String METS_RIGHTS_OWNER_SITE_STRING = "rightsOwnerSiteUrl";
    protected static final String METS_RIGHTS_OWNER_CONTACT_STRING = "rightsOwnerContact";
    protected static final String METS_DIGIPROV_REFERENCE_STRING = "digiprovReference";
    protected static final String METS_DIGIPROV_PRESENTATION_STRING = "digiprovPresentation";

    /***************************************************************************
     * INSTANCE VARIABLES
     **************************************************************************/

    // Set METS Rights, Digiprov, PURLs, and CONTENTIDS.
    private String rightsOwner = "";
    private String rightsOwnerLogo = "";
    private String rightsOwnerSiteURL = "";
    private String rightsOwnerContact = "";
    private String digiprovReference = "";
    private String digiprovPresentation = "";
    private String digiprovReferenceAnchor = "";
    private String digiprovPresentationAnchor = "";
    private String purlUrl = "";
    private String contentIDs = "";

    private String metsRightsSponsor = "";
    private String metsRightsSponsorLogo = "";
    private String metsRightsSponsorSiteURL = "";
    private String metsRightsLicense = "";

    private String iiifUrl = "";
    private String sruUrl = "";

    /***************************************************************************
     * CONSTRUCTORS
     **************************************************************************/
    public MetsModsImportExport() {
    }

    /***************************************************************************
     * @param inPrefs
     * @throws PreferencesException
     **************************************************************************/
    public MetsModsImportExport(Prefs inPrefs) throws PreferencesException {
        super(inPrefs);

        log.info(this.getClass().getName() + " " + getVersion());
    }

    /***************************************************************************
     * WHAT THE OBJECT DOES
     **************************************************************************/

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#Update(java.lang.String)
     */
    @Override
    public boolean update(String filename) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#SetDigitalDocument(ugh.dl.DigitalDocument)
     */
    @Override
    public void setDigitalDocument(DigitalDocument inDoc) {
        this.digdoc = inDoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#WriteMODS(ugh.dl.DocStruct, org.w3c.dom.Node, org.w3c.dom.Document)
     */
    @Override
    protected void writeLogModsSection(DocStruct inStruct, Node dommodsnode, Document domDoc)
            throws PreferencesException, DOMException, WriteException {

        // Prepare lists of all metadata and all persons, that will monitor if
        // some metadata are NOT mapped to MODS.
        List<Object> notMappedMetadataAndPersons = new LinkedList<>();
        if (inStruct.getAllMetadata() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadata());
        }
        // Add persons to list.
        if (inStruct.getAllPersons() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllPersons());
        }

        // Add groups to list
        if (inStruct.getAllMetadataGroups() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadataGroups());
        }

        if (inStruct.getAllCorporates() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllCorporates());
        }

        // Check if we already have written the anchor reference.
        boolean referenceWritten = false;

        // Iterate over all metadata (and person) objects, ordered by the
        // appearance of the metadata in the METS section of the prefs - the
        // MatchingMetadataObjects.
        for (MatchingMetadataObject mmo : this.modsNamesMD) {
            if (!METS_URN_NAME.equalsIgnoreCase(mmo.getInternalName())) {

                //
                // Check if we need a reference to an anchor.
                //

                // Get parent first.
                DocStruct parentStruct = inStruct.getParent();

                // Create a reference only, if parentStruct exists, the
                // MMO's internal name is mentioned in the prefs, and we have not
                // yet written the reference.
                if (parentStruct != null && mmo.getInternalName().equalsIgnoreCase(this.anchorIdentifierMetadataType) && !referenceWritten) {
                    DocStructType parentDst = parentStruct.getType();

                    // Check, if the parent is an anchor.
                    if (parentDst.isAnchor() && this.xPathAnchorReference != null) {

                        // Get identifier(s) of parent.
                        PrefsType identifierType = this.myPreferences.getMetadataTypeByName(this.anchorIdentifierMetadataType);
                        if (identifierType == null) {
                            String message =
                                    "No Metadata of type '" + this.anchorIdentifierMetadataType + "' found to create the anchor in MODS record";
                            log.error(message);
                            throw new PreferencesException(message);
                        }

                        // Go throught all the identifier metadata of the
                        // parent struct and look for the XPath anchor
                        // reference.
                        for (Metadata md : parentStruct.getAllMetadataByType(identifierType)) {
                            // Create the node according to the prefs' METS/MODS
                            // section's XQuery.
                            Node createdNode = createNode(this.xPathAnchorReference, dommodsnode, domDoc, true);

                            if (createdNode != null) {
                                // Get the value of the node.
                                String metadataValue = md.getValue();
                                // If existing, process the valueRegExp.
                                if (this.valueRegExpAnchorReference != null && !"".equals(this.valueRegExpAnchorReference)) {
                                    String oldMetadataValue = metadataValue;

                                    metadataValue = metadataValue.replaceAll(valueRegExpAnchorReference, valueReplacementAnchorReference);
                                    log.info("Regular expression '" + this.valueRegExpAnchorReference + "' changed value of Anchor Identifier '"
                                            + md.getType().getName() + "' from '" + oldMetadataValue + "' to '" + metadataValue + "'");
                                }
                                // Node was created successfully, now add
                                // value to it.
                                Node valueNode = domDoc.createTextNode(metadataValue);
                                createdNode.appendChild(valueNode);

                                // The node was sucessfully written, remove the
                                // metadata object from the notMappedMetadata
                                // list.
                                notMappedMetadataAndPersons.remove(md);

                                referenceWritten = true;
                            }
                        }
                    }
                }

                //
                // Handle Metadata.
                //

                if (inStruct.getAllMetadata() != null) {
                    // Only if the metadata type does exist in the current
                    // DocStruct...
                    PrefsType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());

                    // ... go throught all the available metadata of that type.
                    if (inStruct.hasMetadataType(mdt)) {
                        for (Metadata m : inStruct.getAllMetadataByType(mdt)) {
                            // Only if the metadata has a value, add it to the MODS!
                            if (m.getValue() != null && !"".equals(m.getValue())) {

                                // Create the node according to the prefs' METS/MODS
                                // section's WriteXPath. The Query contains the path
                                // which is used for creating new elements.
                                if (mmo.getWriteXPath() != null) {
                                    writeSingleModsMetadata(mmo.getWriteXPath(), mmo, m, dommodsnode, domDoc);

                                    // The node was sucessfully written! Remove the
                                    // metadata object from the checklist.
                                    notMappedMetadataAndPersons.remove(m);
                                }
                            }
                        }
                    }
                }

                // handle groups

                if (inStruct.getAllMetadataGroups() != null) {
                    // Only if the metadata type does exist in the current DocStruct...
                    MetadataGroupType mdt = this.myPreferences.getMetadataGroupTypeByName(mmo.getInternalName());

                    // ... go throught all the available metadata of that type.

                    if (inStruct.hasMetadataGroupType(mdt)) {
                        for (MetadataGroup group : inStruct.getAllMetadataGroupsByType(mdt)) {
                            boolean isEmpty = true;
                            for (Metadata md : group.getMetadataList()) {
                                if (md.getValue() != null && md.getValue().length() > 0) {
                                    isEmpty = false;
                                    break;
                                }
                            }
                            for (Person p : group.getPersonList()) {
                                if (StringUtils.isNotEmpty(p.getLastname()) || StringUtils.isNotEmpty(p.getFirstname())) {
                                    isEmpty = false;
                                    break;
                                }
                            }

                            for (Corporate c : group.getCorporateList()) {
                                if (StringUtils.isNotBlank(c.getMainName())) {
                                    isEmpty = false;
                                    break;
                                }
                            }
                            // only write groups with values

                            if (!isEmpty) {
                                writeSingleModsGroup(mmo, group, dommodsnode, domDoc, null);

                                // The node was sucessfully written! Remove the
                                // metadata object from the checklist.
                                notMappedMetadataAndPersons.remove(group);

                            }

                        }
                    }
                }

                //
                // Handle Persons.
                //

                if (inStruct.getAllPersons() != null) {
                    // Only if the person type does exist in the current
                    // DocStruct...
                    PrefsType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());

                    // ... go throught all the available metadata of that type.
                    if (inStruct.hasMetadataType(mdt) && inStruct.getAllPersonsByType(mdt) != null) {

                        for (Person p : inStruct.getAllPersonsByType(mdt)) {
                            // Only if the person has a firstname or a lastname, add
                            // it to the MODS!
                            if (((p.getFirstname() != null && !"".equals(p.getFirstname()))
                                    || (p.getLastname() != null && !"".equals(p.getLastname())))) {

                                // Create the node according to the prefs' METS/MODS
                                // section's WriteXPath. The Query contains the path
                                // which is used for creating new elements.
                                if (mmo.getWriteXPath() != null) {
                                    writeSingleModsPerson(mmo.getWriteXPath(), mmo, p, dommodsnode, domDoc);

                                    // The node was sucessfully written! Remove the
                                    // person object from the checklist.
                                    notMappedMetadataAndPersons.remove(p);
                                }
                            }
                        }
                    }
                }

                // export corporates
                if (inStruct.getAllCorporates() != null) {
                    PrefsType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());
                    if (inStruct.hasMetadataType(mdt) && inStruct.getAllCorporatesByType(mdt) != null) {
                        for (Corporate corp : inStruct.getAllCorporatesByType(mdt)) {
                            if (StringUtils.isNotBlank(corp.getMainName()) && mmo.getWriteXPath() != null) {
                                writeSingleModsCorporate(mmo.getWriteXPath(), mmo, corp, dommodsnode, domDoc);
                                notMappedMetadataAndPersons.remove(corp);
                            }
                        }

                    }
                }
            }

            // Check for not mapped metadata and persons.
            if (!notMappedMetadataAndPersons.isEmpty()) {
                log.warn(getMappingWarning(inStruct.getType(), notMappedMetadataAndPersons));
            }
        }
        dirtyReplaceGroupingTagNameHack(dommodsnode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#writePhysModsSection(ugh.dl.DocStruct, org.w3c.dom.Node, org.w3c.dom.Document, org.w3c.dom.Element)
     */
    @Override
    protected void writePhysModsSection(DocStruct inStruct, Node dommodsnode, Document domDoc, Element divElement) throws PreferencesException {

        // Prepare lists of all metadata and all persons, that will monitor if
        // some metadata are NOT mapped to MODS.
        List<Object> notMappedMetadataAndPersons = new LinkedList<>();
        inStruct.getAllMetadata();
        if (inStruct.getAllMetadata() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadata());
        }
        // Add persons to list.
        if (inStruct.getAllPersons() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllPersons());
        }

        // Add groups to list
        if (inStruct.getAllMetadataGroups() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadataGroups());
        }

        // Iterate over all metadata, ordered by the appearance of the
        // metadata in the METS section of the prefs.
        for (MatchingMetadataObject mmo : this.modsNamesMD) {

            // Get current metdata object list.
            List<Metadata> currentMdList = new LinkedList<>();
            if (inStruct.getAllMetadata() != null) {
                for (Metadata m : inStruct.getAllMetadata()) {
                    // Only if the metadata has a value AND its type is
                    // equal to the given metadata object, take it as
                    // current metadata.
                    if (m.getValue() != null && !"".equals(m.getValue())
                            && (m.getType().getName().equalsIgnoreCase(mmo.getInternalName())
                                    || METADATA_PHYSICAL_PAGE_NUMBER.equals(m.getType().getName())
                                    || METADATA_LOGICAL_PAGE_NUMBER.equals(m.getType().getName()) || METS_URN_NAME.equals(m.getType().getName()))) {
                        currentMdList.add(m);
                    }
                }
            }

            // Handle Metadata.
            for (Metadata currentMd : currentMdList) {
                // Query contains the path which is used for creating new
                // elements. If writeXQuery is not existing, try to get
                // readXQuery.
                String xquery = mmo.getWriteXPath();
                if (xquery == null) {
                    xquery = mmo.getReadXQuery();
                }

                // Write physical page number into div.
                if (METADATA_PHYSICAL_PAGE_NUMBER.equals(currentMd.getType().getName())) {
                    divElement.setAttribute(METS_ORDER_STRING, currentMd.getValue());

                    notMappedMetadataAndPersons.remove(currentMd);
                } else if (METS_URN_NAME.equals(currentMd.getType().getName())) {
                    divElement.setAttribute("CONTENTIDS", currentMd.getValue());
                    notMappedMetadataAndPersons.remove(currentMd);
                }

                // Write logical page number into div, if current metadata value
                // is not METADATA_PAGE_UNCOUNTED_VALUE.
                else if (METADATA_LOGICAL_PAGE_NUMBER.equals(currentMd.getType().getName())) {
                    if (!METADATA_PAGE_UNCOUNTED_VALUE.equals(currentMd.getValue())) {
                        divElement.setAttribute(METS_ORDERLABEL_STRING, currentMd.getValue());
                    } else {
                        divElement.setAttribute(METS_ORDERLABEL_STRING, " - ");
                    }

                    notMappedMetadataAndPersons.remove(currentMd);
                } else // Create the node according to the prefs' METS/MODS
                       // section's XQuery.
                if (xquery != null) {
                    // Write other metadata into MODS the section.
                    writeSingleModsMetadata(xquery, mmo, currentMd, dommodsnode, domDoc);

                    // The node was sucessfully written! Remove the
                    // metadata object from the notMappedMetadata list.
                    notMappedMetadataAndPersons.remove(currentMd);
                }
            }
            // handle groups

            if (inStruct.getAllMetadataGroups() != null) {
                // Only if the metadata type does exist in the current DocStruct...
                MetadataGroupType mdt = this.myPreferences.getMetadataGroupTypeByName(mmo.getInternalName());

                // ... go throught all the available metadata of that type.

                if (inStruct.hasMetadataGroupType(mdt)) {
                    for (MetadataGroup group : inStruct.getAllMetadataGroupsByType(mdt)) {
                        boolean isEmpty = true;
                        for (Metadata md : group.getMetadataList()) {
                            if (md.getValue() != null && md.getValue().length() > 0) {
                                isEmpty = false;
                                break;
                            }
                        }
                        // only write groups with values

                        if (!isEmpty) {
                            writeSingleModsGroup(mmo, group, dommodsnode, domDoc, null);

                            // The node was sucessfully written! Remove the
                            // metadata object from the checklist.
                            notMappedMetadataAndPersons.remove(group);

                        }

                    }
                }
            }
            // Get the current person object list.
            List<Person> currentPerList = new LinkedList<>();
            if (inStruct.getAllPersons() != null) {
                for (Person p : inStruct.getAllPersons()) {
                    // Only if the person has a first or last name AND its
                    // type is equal to the given metadata object, take it
                    // as current person.
                    if (((p.getFirstname() != null && !"".equals(p.getFirstname())) || (p.getLastname() != null && !"".equals(p.getLastname())))
                            && p.getType().getName().equalsIgnoreCase(mmo.getInternalName())) {
                        currentPerList.add(p);
                    }
                }
            }

            // Handle Persons.
            for (Person currentPer : currentPerList) {
                // Query contains the path which is used for
                // creating new elements.
                String xquery = mmo.getWriteXPath();
                if (xquery == null) {
                    xquery = mmo.getReadXQuery();
                }

                // Create the node according to the prefs' METS/MODS
                // section's XQuery.
                if (xquery != null) {
                    writeSingleModsPerson(xquery, mmo, currentPer, dommodsnode, domDoc);

                    // The node was sucessfully written, remove the
                    // person object from the notMappedPersons list.
                    notMappedMetadataAndPersons.remove(currentPer);
                }
            }
            // TODO export corporates
        }

        // Check for not mapped metadata and persons.
        if (!notMappedMetadataAndPersons.isEmpty()) {
            log.warn(getMappingWarning(inStruct.getType(), notMappedMetadataAndPersons));
        }
        dirtyReplaceGroupingTagNameHack(dommodsnode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#parseMODS(org.w3c.dom.Node, ugh.dl.DocStruct)
     */
    @Override
    protected void parseMODS(Node inMods, DocStruct inStruct)
            throws ReadException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        // Document in DOM tree which represents the MODS.
        Document modsdocument = null;

        DOMImplementationRegistry registry = null;
        registry = DOMImplementationRegistry.newInstance();

        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");

        // Test, if the needed DOMImplementation (DOM 3!) is available, else
        // throw Exception. We are using Xerxes here!
        if (impl == null) {
            String message =
                    "There is NO implementation of DOM3 in your ClassPath! We are using Xerxes here, I have no idea why that's not available!";
            log.error(message);
            throw new UnsupportedOperationException(message);
        }
        LSSerializer writer = impl.createLSSerializer();

        // Get string for MODS.
        String modsstr = writer.writeToString(inMods);

        // Parse MODS section; create a DOM tree just for the MODS from the
        // string new document builder instance.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Do not validate xml file (for we want to store unfinished files, too)
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(true);

        Reader r = new StringReader(modsstr);

        // Read file and parse it.
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(r);

            modsdocument = builder.parse(is);
        } catch (SAXParseException e) {
            // Error generated by the parser.
            String message = "Parse error on line: " + e.getLineNumber() + ", uri: " + e.getSystemId();
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (ParserConfigurationException e) {
            // Parser with specified options can't be built.
            String message = "XML parser not configured correctly!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (SAXException | IOException e) {
            String message = "Exception while parsing METS file; can't create DOM tree!";
            log.error(message, e);
            throw new ReadException(message, e);
        }

        log.trace("\n" + LINE + "\nMODS\n" + LINE + "\n" + modsstr + "\n" + LINE);

        // Result of XQuery.
        Object xqueryresult = null;

        // Create XQuery.
        XPathFactory xpathfactory = XPathFactory.newInstance();

        // New namespace context.
        PersonalNamespaceContext pnc = new PersonalNamespaceContext();
        pnc.setNamespaceHash(this.namespaces);
        XPath xpath = xpathfactory.newXPath();
        xpath.setNamespaceContext(pnc);

        // Get the first element; this is where we start with out XPATH.
        Node startingNode = null;
        NodeList nl = modsdocument.getChildNodes();
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == ELEMENT_NODE) {
                    startingNode = n;
                }
            }
        }

        //
        // Iterate over all MatchingMetadataObjects and try to find the required
        // MODS information for external METS import.
        //
        for (MatchingMetadataObject mmo : this.modsNamesMD) {

            // No internal name available: next one.
            if (mmo.getInternalName() == null) {
                continue;
            }

            String queryExpression = mmo.getReadXQuery();

            // No query expression in: next one.
            if (queryExpression == null) {
                continue;
            }

            // Delete the leading "." if there is one available.
            if (queryExpression.startsWith(".")) {
                queryExpression = queryExpression.substring(1, queryExpression.length());
            }

            // Carry out the XPATH query.
            try {
                XPathExpression expr = xpath.compile(queryExpression);
                xqueryresult = expr.evaluate(startingNode, XPathConstants.NODESET);
                log.debug("Query expression: " + queryExpression);
            } catch (XPathExpressionException e) {
                String message =
                        "Error while parsing MODS metadata: " + mmo.getInternalName() + "! Please check XPath '" + mmo.getReadXQuery() + "'!";
                log.error(message, e);
                throw new ReadException(message, e);
            }

            NodeList nodes = (NodeList) xqueryresult;

            // Get Nodes.
            //
            // Iterate over all nodes and get the text node which contains the
            // value of the element.
            Node node = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                // Get DOM Node.
                node = nodes.item(i);

                // Get child Nodes to find the TextNode.
                NodeList nodelist = node.getChildNodes();
                for (int x = 0; x < nodelist.getLength(); x++) {
                    Node subnode = nodelist.item(x);
                    String value = null;
                    if (subnode.getNodeType() == TEXT_NODE) {
                        value = subnode.getNodeValue();
                    }

                    // Create Metadata.
                    MetadataType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());
                    if (mdt == null) {
                        // No valid metadata type found.
                        String message = "Can't find internal Metadata with name '" + mmo.getInternalName() + "' for MODS element '"
                                + mmo.getReadModsName() + "'";
                        log.error(message);
                        throw new ImportException(message);
                    }

                    //
                    // Add Metadata to DocStruct.
                    //

                    // Handle Persons.
                    if (mdt.getIsPerson()) {
                        Person ps = null;
                        try {
                            ps = new Person(mdt);
                        } catch (MetadataTypeNotAllowedException e) {
                            // mdt is NOT null, we ensure this above!
                            e.printStackTrace();
                        }

                        ps.setRole(mdt.getName());

                        // It is supposed that the <name> element is selected,
                        // the following queries are just subqueries to the name
                        // element.

                        String[] firstnamevalue = null;
                        String[] lastnamevalue = null;
                        String[] affiliationvalue = null;
                        String[] authorityfileidvalue = null;
                        String[] authorityuri = null;
                        String[] authorityvalue = null;
                        String[] displaynamevalue = null;
                        String[] persontypevalue = null;

                        if (mmo.getFirstnameXQuery() != null) {
                            firstnamevalue = getValueForUnambigiousXQuery(node, mmo.getFirstnameXQuery());
                        }
                        if (mmo.getLastnameXQuery() != null) {
                            lastnamevalue = getValueForUnambigiousXQuery(node, mmo.getLastnameXQuery());
                        }
                        if (mmo.getAffiliationXQuery() != null) {
                            affiliationvalue = getValueForUnambigiousXQuery(node, mmo.getAffiliationXQuery());
                        }
                        if (mmo.getAuthorityIDXquery() != null) {
                            authorityfileidvalue = getValueForUnambigiousXQuery(node, mmo.getAuthorityIDXquery());
                        }
                        if (mmo.getAuthorityURIXquery() != null) {
                            authorityuri = getValueForUnambigiousXQuery(node, mmo.getAuthorityURIXquery());
                        }
                        if (mmo.getAuthorityValueXquery() != null) {
                            authorityvalue = getValueForUnambigiousXQuery(node, mmo.getAuthorityValueXquery());
                        }
                        if (mmo.getDisplayNameXQuery() != null) {
                            displaynamevalue = getValueForUnambigiousXQuery(node, mmo.getDisplayNameXQuery());
                        }
                        if (mmo.getPersontypeXQuery() != null) {
                            persontypevalue = getValueForUnambigiousXQuery(node, mmo.getDisplayNameXQuery());
                        }

                        if (lastnamevalue != null) {
                            ps.setLastname(lastnamevalue[0]);
                        }
                        if (firstnamevalue != null) {
                            ps.setFirstname(firstnamevalue[0]);
                        }
                        if (affiliationvalue != null) {
                            ps.setAffiliation(affiliationvalue[0]);
                        }
                        if (authorityfileidvalue != null && authorityuri != null && authorityvalue != null) {
                            ps.setAuthorityFile(authorityfileidvalue[0], authorityuri[0], authorityvalue[0]);
                        }
                        if (displaynamevalue != null) {
                            ps.setDisplayname(displaynamevalue[0]);
                        }
                        if (persontypevalue != null) {
                            ps.setPersontype(persontypevalue[0]);
                        }

                        try {
                            inStruct.addPerson(ps);
                        } catch (DocStructHasNoTypeException e) {
                            String message = "DocumentStructure for which metadata should be added has no type!";
                            log.error(message, e);
                            throw new ImportException(message, e);
                        } catch (MetadataTypeNotAllowedException e) {
                            String message = "Person '" + mdt.getName() + "' (" + ps.getDisplayname() + ") is not allowed as a child for '"
                                    + inStruct.getType().getName() + "' during MODS import!";
                            log.error(message, e);
                        }

                        // Get out of for loop; we don't need to iterate over
                        // all nodes.
                        break;
                    }

                    if (value == null) {
                        // Value not found, as the subnode is not a TEXT node
                        // continue iterating over subnodes.
                        continue;
                    }

                    // Handle metadata.
                    Metadata md = null;
                    try {
                        md = new Metadata(mdt);
                    } catch (MetadataTypeNotAllowedException e) {
                        // mdt is NOT null, we ensure this above!
                        e.printStackTrace();
                    }
                    if (node.getAttributes().getNamedItem("authority") != null && node.getAttributes().getNamedItem("authorityURI") != null
                            && node.getAttributes().getNamedItem("valueURI") != null) {
                        String authority = node.getAttributes().getNamedItem("authority").getNodeValue();
                        String authorityURI = node.getAttributes().getNamedItem("authorityURI").getNodeValue();
                        String valueURI = node.getAttributes().getNamedItem("valueURI").getNodeValue();
                        md.setAuthorityFile(authority, authorityURI, valueURI);
                    }
                    md.setValue(value);

                    // Add the metadata.
                    try {
                        inStruct.addMetadata(md);

                        log.debug("Added metadata '" + mdt.getName() + "' to DocStruct '" + inStruct.getType().getName() + "' with value '" + value
                                + "'");
                    } catch (DocStructHasNoTypeException e) {
                        String message = "DocumentStructure for which metadata should be added, has no type!";
                        log.error(message, e);
                        throw new ImportException(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        String message = "Metadata '" + mdt.getName() + "' (" + value + ") is not allowed as child for '"
                                + inStruct.getType().getName() + "' during MODS import!";
                        log.error(message, e);
                        throw new ImportException(message, e);
                    }

                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#checkForAnchorReference(java.lang.String, java.lang.String)
     */
    @Override
    protected DocStruct checkForAnchorReference(String inMods, String filename) throws ReadException {

        ModsDocument modsDocument;
        DocStruct anchorDocStruct = null;

        // Check for MODS validity.
        try {
            modsDocument = ModsDocument.Factory.parse(inMods);
        } catch (XmlException e) {
            String message = "MODS section doesn't seem to contain valid MODS";
            log.error(message, e);
            throw new ImportException("Doesn't seem to contain valid MODS", e);
        }

        // Do query. Query syntax is like in the following example:
        // String queryExpression = "declare namespace

        String path = this.namespaceDeclarations.get(this.modsNamespacePrefix) + " $this/" + this.xPathAnchorReference;

        XmlOptions xo = new XmlOptions();
        xo.setUseDefaultNamespace();
        XmlObject[] objects = modsDocument.selectPath(path, xo);

        // Iterate over all objects; objects can be available more than once.
        for (XmlObject object : objects) {
            // Get DOM Node.
            Node node = object.getDomNode();

            // Get child nodes to find the text node.
            NodeList nodelist = node.getChildNodes();

            for (int x = 0; x < nodelist.getLength(); x++) {
                Node subnode = nodelist.item(x);
                if (subnode.getNodeType() == TEXT_NODE) {
                    String identifierOfAnchor = subnode.getNodeValue();
                    // Found the reference to the anchor.
                    log.debug("Anchor's identifier: " + identifierOfAnchor);

                    // Try to read anchor from separate file.
                    String anchorfilename = buildAnchorFilename(filename);
                    if (!new File(anchorfilename).exists()) {
                        // File does not exists: no anchor available.
                        return null;
                    }
                    MetsMods anchorMets = null;

                    try {
                        anchorMets = new MetsMods(this.myPreferences);
                    } catch (PreferencesException e) {
                        String message = "Can't read Preferences for METS while reading the Anchor file";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }

                    try {
                        anchorMets.read(anchorfilename);
                    } catch (ReadException e) {
                        String message = "Can't read Anchor file, which must be in METS format as well";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }

                    // Get Digital Document and first logical DocStruct (which
                    // should be the only one).
                    DigitalDocument anchordd = anchorMets.getDigitalDocument();
                    DocStruct anchorStruct = anchordd.getLogicalDocStruct();
                    List<Metadata> allMetadata = anchorStruct.getAllMetadata();

                    // Iterate over all metadata and find an identifier with the
                    // value of identifierOfAnchor.
                    if (allMetadata != null) {
                        for (Metadata md : allMetadata) {
                            if (md.getValue() != null && md.getValue().equals(identifierOfAnchor)) {
                                if (md.getType().isIdentifier()) {
                                    // That's the anchor!
                                    anchorDocStruct = anchorStruct;
                                } else {
                                    // Log an error, maybe only the metadata is
                                    // not set as identifier.
                                    log.warn("Identifier '" + md.getType().getName()
                                            + "' found, but its type is NOT set to 'identifier' in the prefs!");
                                }
                            }
                        }
                    }
                    if (anchorDocStruct == null) {
                        log.error("CheckForAnchorReference: Referenced identifier for anchor '" + identifierOfAnchor
                                + "' not found in anchor DocStruct '" + anchorfilename + "'");
                        return null;
                    }
                }
            }
        }

        // Copy the anchor DocStruct, so it can be added as a parent: copy all
        // metadata, but not it's children.
        return anchorDocStruct.copy(true, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#getAnchorIdentifierFromMODSDOM(org.w3c. dom.Node, ugh.dl.DocStruct)
     */
    @Override
    protected String getAnchorIdentifierFromMODSDOM(Node inMods) {

        String anchoridentifier = null;
        // Result from XPath expression.
        NodeList resultlist = null;

        // Create an XPath Query to get the anchor identifier. Check, if
        // currentPath is already available.
        XPathFactory factory = XPathFactory.newInstance();

        // New namespace context.
        PersonalNamespaceContext pnc = new PersonalNamespaceContext();
        pnc.setNamespaceHash(this.namespaces);
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(pnc);

        try {
            XPathExpression expr = xpath.compile(this.xPathAnchorReference);

            // Carry out the query.
            Object list = expr.evaluate(inMods, XPathConstants.NODESET);
            resultlist = (NodeList) list;

            // Iterate over results.
            if (resultlist.getLength() > 1) {
                log.error("XPath expression for reference to the anchor is ambigious!");
                return null;
            }
            for (int i = 0; i < resultlist.getLength(); i++) {
                Node node = resultlist.item(i);
                // Get child Nodes to find the TextNode.
                NodeList nodelist = node.getChildNodes();
                for (int x = 0; x < nodelist.getLength(); x++) {
                    Node subnode = nodelist.item(x);
                    if (subnode.getNodeType() == TEXT_NODE) {
                        anchoridentifier = subnode.getNodeValue();
                        break;
                    }
                }
            }
        } catch (XPathExpressionException e) {
            log.error("Something is wrong with the XPATH: " + e.getMessage());
            e.printStackTrace();
        }

        return anchoridentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsModsGdz#writePhysDivs(org.w3c.dom.Node, ugh.dl.DocStruct)
     */
    @Override
    protected Element writePhysDivs(Node parentNode, DocStruct inStruct) throws PreferencesException {

        // Write div element.
        Document domDoc = parentNode.getOwnerDocument();
        if ("div".equals(inStruct.getDocstructType())) {
            Element div = domDoc.createElementNS(this.namespaces.get("mets").getUri(), METS_DIV_STRING);

            String idphys = PHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(this.divphysidMax);
            this.divphysidMax++;

            inStruct.setIdentifier(idphys);
            div.setAttribute(METS_ID_STRING, idphys);

            if (StringUtils.isNotBlank(inStruct.getAdmId())) {
                div.setAttribute(METS_ADMID_STRING, inStruct.getAdmId());
            }

            // Write METS type given in preferences, if existing.
            String type = getMetsType(inStruct.getType());
            if (type == null) {
                // If no METS type was configured, use internal type.
                type = inStruct.getType().getName();
            }
            div.setAttribute(METS_DIVTYPE_STRING, type);
            if (StringUtils.isNotBlank(inStruct.getAdditionalValue())) {
                createDomAttributeNS(div, "xlink", "label", inStruct.getAdditionalValue());
            }
            // Add physical CONTENTIDS attribute, if existing.
            if (!"".equals(this.contentIDs)) {
                div.setAttribute(METS_CONTENTIDS_STRING, this.contentIDs);
            }

            // Add div element as child to parentNode.
            parentNode.appendChild(div);

            // Write metdata.
            if (this.metsNode == null) {
                log.error("METS node is null... can't write anything");
                return null;
            }

            int dmdid = writePhysDmd(this.metsNode, div, inStruct);

            // If dmdid is != -1 then the appropriate metadata section has been
            // written, if dmdid == -1, the inStruct has no metadata.
            String dmdidString = "";
            if (dmdid != -1) {
                dmdidString = DMDPHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(dmdid);
                div.setAttribute("DMDID", dmdidString);
            }

            // Write links to ContentFiles (FPTRs)
            writeFptrs(inStruct, domDoc, div);

            // Get all children and write their divs recursive.
            List<DocStruct> allChildren = inStruct.getAllChildren();
            if (allChildren != null) {
                for (DocStruct child : allChildren) {
                    if (writePhysDivs(div, child) == null) {
                        // Error occured while writing div for child.
                        return null;
                    }
                }
            }

            return div;
        } else {
            Element area = null;
            // get fptr from parent
            NodeList fptrList = parentNode.getChildNodes();

            String mainGroupName = null;
            for (VirtualFileGroup vFileGroup : this.digdoc.getFileSet().getVirtualFileGroups()) {
                if (vFileGroup.isMainGroup()) {
                    mainGroupName = vFileGroup.getName();
                    break;
                }
            }

            for (int x = 0; x < fptrList.getLength(); x++) {
                Element fptr = (Element) fptrList.item(x);
                // check if it is the main file group
                if (mainGroupName == null || fptr.getAttribute("FILEID").endsWith(mainGroupName)) {

                    // check for seq element
                    Node seq = null;
                    if (fptr.getChildNodes().getLength() > 0) {
                        seq = fptr.getChildNodes().item(0);
                    } else {
                        seq = createDomElementNS(domDoc, this.metsNamespacePrefix, "seq");
                        fptr.appendChild(seq);
                    }
                    // create area element
                    area = createDomElementNS(domDoc, this.metsNamespacePrefix, "area");
                    seq.appendChild(area);
                    String idphys = PHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(this.divphysidMax);
                    this.divphysidMax++;

                    inStruct.setIdentifier(idphys);
                    area.setAttribute(METS_ID_STRING, idphys);

                    for (Metadata md : inStruct.getAllMetadata()) {
                        if ("_urn".equals(md.getType().getName())) {
                            area.setAttribute("CONTENTIDS", md.getValue());
                        } else if ("_COORDS".equals(md.getType().getName())) {
                            area.setAttribute("COORDS", md.getValue());
                        } else if ("_SHAPE".equals(md.getType().getName())) {
                            area.setAttribute("SHAPE", md.getValue());
                        }
                    }
                    area.setAttribute(METS_FILEID_STRING, fptr.getAttribute(METS_FILEID_STRING));
                }
            }
            return area;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#writeLogDivs(org.w3c.dom.Node, ugh.dl.DocStruct, boolean)
     */
    @Override
    protected Element writeLogDivs(Node parentNode, DocStruct inStruct, boolean isAnchorFile) throws WriteException, PreferencesException {

        // Write div element.
        Document domDoc = parentNode.getOwnerDocument();
        Element div = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_DIV_STRING);

        // Add div element as child to parentNode.
        parentNode.appendChild(div);
        if (this.firstDivNode == null) {
            this.firstDivNode = div;
        }

        String idlog = LOG_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(this.divlogidMax);
        div.setAttribute(METS_ID_STRING, idlog);
        this.divlogidMax++;

        // Write METS type given in preferences, if existing.
        String type = getMetsType(inStruct.getType());
        if (type == null) {
            // If no METS type was configured, use internal type.
            type = inStruct.getType().getName();
        }
        div.setAttribute(METS_DIVTYPE_STRING, type);

        // Set logical CONTENTIDS attribute if existing, and if current
        // docstruct is topstruct.
        //
        if (inStruct.getParent() == null && !"".equals(this.purlUrl)) {
            div.setAttribute(METS_CONTENTIDS_STRING, this.purlUrl);
        }

        String label = "";

        if (inStruct.getAllMetadata() != null) {
            for (Metadata md : inStruct.getAllMetadata()) {
                if (METS_PREFS_LABEL_METADATA_STRING.equals(md.getType().getName())) {
                    label = md.getValue();
                } else if (METS_URN_NAME.equals(md.getType().getName())) {
                    div.setAttribute(METS_CONTENTIDS_STRING, md.getValue());
                }
            }
        }
        if (label != null && !"".equals(label)) {
            div.setAttribute(METS_LABEL_STRING, label);
        }

        // Set identifier for this docStruct.
        inStruct.setIdentifier(idlog);

        // Write metadata.
        if (this.metsNode == null) {
            log.error("METS node is null... can't write anything");
            return null;
        }

        // Set the DMDIDs.
        int dmdid = writeLogDmd(this.metsNode, inStruct, isAnchorFile);
        if (dmdid >= 0) {
            // Just set DMDID attribute, if there is a metadata set.
            String dmdidString = DMDLOG_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(dmdid);
            div.setAttribute(METS_DMDID_STRING, dmdidString);
        }

        //Set the AMDIDs if necessary
        if (inStruct != null && inStruct.getAmdSec() != null) {
            String amdid = inStruct.getAmdSec().getId();
            if (amdid != null && !amdid.isEmpty()) {
                div.setAttribute(METS_ADMID_STRING, amdid);
            }
        } else // Set the ADMID, depends if the current element is an anchor or not.
        if ((isAnchorFile && inStruct.getType().isAnchor())
                || (!isAnchorFile && inStruct.getParent() != null && inStruct.getParent().getType().isAnchor())
                || (!isAnchorFile && !inStruct.getType().isAnchor() && inStruct.getParent() == null)) {
            div.setAttribute(METS_ADMID_STRING, AMD_PREFIX);
        }

        // Create MPTR element.
        Element mptr = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MPTR_STRING);
        mptr.setAttribute(METS_LOCTYPE_STRING, "URL");

        String ordernumber = "";
        // current element is anchor file
        if (inStruct.getType().isAnchor()) {
            if (inStruct.getAllChildren() != null && !inStruct.getAllChildren().isEmpty()) {
                DocStruct child = inStruct.getAllChildren().get(0);
                if (child.getAllMetadata() != null) {
                    for (Metadata md : child.getAllMetadata()) {
                        if (RULESET_ORDER_NAME.equals(md.getType().getName())) {
                            ordernumber = md.getValue();
                        }
                    }
                }
            }
        }
        // current element is first child of an anchor file
        else if (!inStruct.getType().isAnchor() && inStruct.getParent() != null && inStruct.getParent().getType().isAnchor()) {
            if (inStruct.getAllMetadata() != null) {
                for (Metadata md : inStruct.getAllMetadata()) {
                    if (RULESET_ORDER_NAME.equals(md.getType().getName())) {
                        ordernumber = md.getValue();
                    }
                }
            }
        }

        if (!isAnchorFile && !inStruct.getType().isAnchor() && inStruct.getParent() != null && inStruct.getParent().getType().isAnchor()) {
            if (ordernumber != null && ordernumber.length() > 0) {
                div.setAttribute(METS_ORDER_STRING, ordernumber);
            }
        }

        if (StringUtils.isNotBlank(inStruct.getOrderLabel())) {
            div.setAttribute(METS_ORDERLABEL_STRING, inStruct.getOrderLabel());
        }

        // Write the MPTR element if a non-anchor file is written AND element is
        // defined as an anchor in the prefs --> METS pointer in e.g.
        // "periodical volume".
        if (!isAnchorFile && inStruct.getType().isAnchor()) {
            if ("".equals(this.mptrUrl)) {
                log.warn("No METS pointer URL (mptr) to the parent/anchor DocStruct is defined! Referencing will NOT work!");
            }
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, this.mptrUrl);
            // Write mptr element.
            div.appendChild(mptr);
        }

        // Write the MPTR element if an anchor file is written AND parent
        // element is an anchor --> METS pointer in e.g. "periodical".
        else if (isAnchorFile && !inStruct.getType().isAnchor()) {
            if ("".equals(this.mptrUrlAnchor)) {
                log.warn("No METS pointer URL (mptr) to the child DocStructs is defined! Referencing will NOT work!");
            }
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, this.mptrUrlAnchor);
            if (ordernumber != null && ordernumber.length() > 0) {
                div.setAttribute(METS_ORDER_STRING, ordernumber);
            }
            // Write mptr element.
            div.appendChild(mptr);
        } else if (StringUtils.isNotBlank(inStruct.getLink())) {
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, inStruct.getLink());
            div.appendChild(mptr);
        }

        // Get all children and write their divs.
        List<DocStruct> allChildren = inStruct.getAllChildren();
        if (allChildren != null) {
            for (DocStruct child : allChildren) {
                if (writeLogDivs(div, child, isAnchorFile) == null) {
                    // Error occured while writing div for child.
                    return null;
                }
            }
        }

        return div;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#writeAmdSec(org.w3c.dom.Document, boolean)
     */
    @Override
    protected void writeAmdSec(Document domDoc, boolean isAnchorFile) {

        boolean rightsMDExists = false;
        boolean digiprovMDExists = false;

        // Creates the METS' AMDSEC, uses only *ONE* AMDID for ZVDD/DFG-Viewer.
        Element amdSec = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_AMDSEC_STRING);
        AmdSec amd = this.digdoc.getAmdSec();
        if (amd != null) {
            if (StringUtils.isBlank(amd.getId())) {
                amd.setId(AMD_PREFIX);
            }
            amdSec.setAttribute(METS_ID_STRING, amd.getId());
        } else {
            amdSec.setAttribute(METS_ID_STRING, AMD_PREFIX);
        }

        // create techMD
        List<Md> techMdList = this.digdoc.getTechMds();
        if (techMdList != null && !techMdList.isEmpty()) {
            for (Md md : techMdList) {
                this.techidMax++;
                Node theNode = domDoc.importNode(md.getContent(), true);
                Node child = theNode.getFirstChild();
                Element techMd = createDomElementNS(domDoc, this.metsNamespacePrefix, md.getType().toString());
                if (md.getType().toString().contentEquals(METS_RIGHTSMD_STRING)) {
                    Node mdWrap = md.getContent();
                    if (mdWrap != null) {
                        Node mdType = mdWrap.getAttributes().getNamedItem("OTHERMDTYPE");
                        if (mdType != null && mdType.getNodeValue().contentEquals("DVRIGHTS")) {
                            rightsMDExists = true;
                        }
                    }
                } else if (md.getType().toString().contentEquals("digiprovMD")) {
                    Node mdWrap = md.getContent();
                    if (mdWrap != null) {
                        Node mdType = mdWrap.getAttributes().getNamedItem("OTHERMDTYPE");
                        if (mdType != null && mdType.getNodeValue().contentEquals("DVLINKS")) {
                            digiprovMDExists = true;
                        }
                    }
                }
                techMd.setAttribute(METS_ID_STRING, md.getId());

                techMd.appendChild(child);
                amdSec.appendChild(techMd);
            }
        }

        if (!rightsMDExists) {
            // Create rightsMD.
            //
            Element rightsMd = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_RIGHTSMD_STRING);
            rightsMd.setAttribute(METS_ID_STRING, "RIGHTS");
            amdSec.appendChild(rightsMd);

            // Create mdWrap.
            Element mdWrap = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MDWRAP_STRING);
            mdWrap.setAttribute(METS_MIMETYPE_STRING, "text/xml");
            mdWrap.setAttribute(METS_MDTYPE_STRING, "OTHER");
            mdWrap.setAttribute(METS_OTHERMDTYPE_STRING, "DVRIGHTS");
            rightsMd.appendChild(mdWrap);

            // Create xmlData.
            Element xmlData = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_XMLDATA_STRING);
            mdWrap.appendChild(xmlData);

            // Create dv tags.
            Element dv = createDomElementNS(domDoc, this.dvNamespacePrefix, "rights");
            xmlData.appendChild(dv);
            Element dvOwner = createDomElementNS(domDoc, this.dvNamespacePrefix, "owner");
            Element dvOwnerLogo = createDomElementNS(domDoc, this.dvNamespacePrefix, "ownerLogo");
            Element dvOwnerSiteURL = createDomElementNS(domDoc, this.dvNamespacePrefix, "ownerSiteURL");
            Element dvOwnerContact = createDomElementNS(domDoc, this.dvNamespacePrefix, "ownerContact");
            dvOwner.setTextContent(this.rightsOwner);
            dvOwnerLogo.setTextContent(this.rightsOwnerLogo);
            dvOwnerSiteURL.setTextContent(this.rightsOwnerSiteURL);
            dvOwnerContact.setTextContent(this.rightsOwnerContact);
            dv.appendChild(dvOwner);
            dv.appendChild(dvOwnerLogo);
            dv.appendChild(dvOwnerSiteURL);
            dv.appendChild(dvOwnerContact);

            if (metsRightsSponsor != null && !metsRightsSponsor.isEmpty()) {
                Element dvSponsor = createDomElementNS(domDoc, this.dvNamespacePrefix, "sponsor");
                dvSponsor.setTextContent(metsRightsSponsor);
                dv.appendChild(dvSponsor);
            }

            if (metsRightsSponsorLogo != null && !metsRightsSponsorLogo.isEmpty()) {
                Element dvSponsor = createDomElementNS(domDoc, this.dvNamespacePrefix, "sponsorLogo");
                dvSponsor.setTextContent(metsRightsSponsorLogo);
                dv.appendChild(dvSponsor);
            }

            if (metsRightsSponsorSiteURL != null && !metsRightsSponsorSiteURL.isEmpty()) {
                Element dvSponsor = createDomElementNS(domDoc, this.dvNamespacePrefix, "sponsorSiteURL");
                dvSponsor.setTextContent(metsRightsSponsorSiteURL);
                dv.appendChild(dvSponsor);
            }
            if (metsRightsLicense != null && !metsRightsLicense.isEmpty()) {
                Element dvSponsor = createDomElementNS(domDoc, this.dvNamespacePrefix, "license");
                dvSponsor.setTextContent(metsRightsLicense);
                dv.appendChild(dvSponsor);
            }

        }

        if (!digiprovMDExists) {
            // Create digiprovMD.
            //
            Element digiprovMd = createDomElementNS(domDoc, this.metsNamespacePrefix, "digiprovMD");
            digiprovMd.setAttribute(METS_ID_STRING, "DIGIPROV");
            amdSec.appendChild(digiprovMd);

            // Create mdWrap.
            Element mdWrapDigiprov = createDomElementNS(domDoc, this.metsNamespacePrefix, "mdWrap");
            mdWrapDigiprov.setAttribute(METS_MIMETYPE_STRING, "text/xml");
            mdWrapDigiprov.setAttribute(METS_MDTYPE_STRING, "OTHER");
            mdWrapDigiprov.setAttribute(METS_OTHERMDTYPE_STRING, "DVLINKS");
            digiprovMd.appendChild(mdWrapDigiprov);

            // Create xmlData.
            Element xmlDataDigiprov = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_XMLDATA_STRING);
            mdWrapDigiprov.appendChild(xmlDataDigiprov);

            // Create dv tags.
            Element dvDigiprov = createDomElementNS(domDoc, this.dvNamespacePrefix, "links");
            xmlDataDigiprov.appendChild(dvDigiprov);
            Element dvReference = createDomElementNS(domDoc, this.dvNamespacePrefix, "reference");
            Element dvPresentation = createDomElementNS(domDoc, this.dvNamespacePrefix, "presentation");
            // Set values according to anchor flag.
            if (isAnchorFile) {
                dvReference.setTextContent(this.digiprovReferenceAnchor);
                dvPresentation.setTextContent(this.digiprovPresentationAnchor);
            } else {
                dvReference.setTextContent(this.digiprovReference);
                dvPresentation.setTextContent(this.digiprovPresentation);
            }

            dvDigiprov.appendChild(dvReference);
            dvDigiprov.appendChild(dvPresentation);

            if (StringUtils.isNotBlank(iiifUrl)) {
                Element dvIiif = createDomElementNS(domDoc, dvNamespacePrefix, "iiif");
                dvIiif.setTextContent(iiifUrl);
                dvDigiprov.appendChild(dvIiif);
            }

            if (StringUtils.isNotBlank(sruUrl)) {
                Element dvIiif = createDomElementNS(domDoc, dvNamespacePrefix, "sru");
                dvIiif.setTextContent(sruUrl);
                dvDigiprov.appendChild(dvIiif);
            }
        }

        // Append to our metsNode, before the fileSec (or before the structMap
        // if anchor file).
        //
        String element;
        if (isAnchorFile) {
            element = this.metsNamespacePrefix + ":" + METS_STRUCTMAP_STRING;
        } else {
            element = this.metsNamespacePrefix + ":" + METS_FILESEC_STRING;
        }

        NodeList dmdList = this.metsNode.getElementsByTagName(element);
        Node refChild = dmdList.item(0);
        if (refChild != null) {
            this.metsNode.insertBefore(amdSec, refChild);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#checkMissingSettings()
     */
    @Override
    protected List<String> checkMissingSettings() {
        List<String> result = new LinkedList<>();

        if ("".equals(this.rightsOwner)) {
            result.add(METS_RIGHTS_OWNER_STRING);
        }
        if ("".equals(this.rightsOwnerLogo)) {
            result.add(METS_RIGHTS_OWNER_LOGO_STRING);
        }
        if ("".equals(this.rightsOwnerSiteURL)) {
            result.add(METS_RIGHTS_OWNER_SITE_STRING);
        }
        if ("".equals(this.rightsOwnerContact)) {
            result.add(METS_RIGHTS_OWNER_CONTACT_STRING);
        }
        if ("".equals(this.digiprovReference)) {
            result.add(METS_DIGIPROV_REFERENCE_STRING);
        }
        if ("".equals(this.digiprovPresentation) || "".equals(this.digiprovPresentationAnchor)) {
            result.add(METS_DIGIPROV_PRESENTATION_STRING);
        }
        if ("".equals(this.mptrUrl)) {
            result.add(METS_MPTR_URL_STRING);
        }
        if ("".equals(this.mptrUrlAnchor)) {
            result.add(METS_MPTR_URL_ANCHOR_STRING);
        }

        return result;
    }

    /***************************************************************************
     * PRIVATE (AND PROTECTED) METHODS
     **************************************************************************/

    /***************************************************************************
     * <p>
     * Creates a single Goobi internal metadata element.
     * </p>
     * 
     * @param theXQuery
     * @param theMetadata
     * @param theStartingNode
     * @param theDocument
     * @throws PreferencesException
     **************************************************************************/
    protected void writeSingleModsMetadata(String theXQuery, MatchingMetadataObject theMMO, Metadata theMetadata, Node theStartingNode,
            Document theDocument) throws PreferencesException {

        // Get metadata to set.
        String newMetadataValue = theMetadata.getValue();

        // Check conditions from the prefs. If they exist and do NOT
        // match, continue with the next mmo.

        if (theMMO != null && theMMO.getValueCondition() != null && !"".equals(theMMO.getValueCondition())) {
            Pattern pattern = Pattern.compile(splitRegularExpression(theMMO.getValueCondition()).get(0));
            Matcher matcher = pattern.matcher(theMetadata.getValue());
            if (!matcher.find()) {
                log.info("Condition '" + theMMO.getValueCondition() + "' for Metadata '" + theMMO.getInternalName() + " (" + theMetadata.getValue()
                        + ")" + "' does not match, no node was created...");
                return;

            }

        }

        // Check and process regular expression from the prefs.

        if (theMMO != null && StringUtils.isNotBlank(theMMO.getValueRegExp())) {
            List<String> params = splitRegularExpression(theMMO.getValueRegExp());
            newMetadataValue = newMetadataValue.replaceAll(params.get(0), params.get(1));
            log.info("Regular expression '" + theMMO.getValueRegExp() + "' changed value of Metadata '" + theMMO.getInternalName() + "' from '"
                    + theMetadata.getValue() + "' to '" + newMetadataValue + "'");
        }

        // Only create node, if a value is existing.
        if (!"".equals(newMetadataValue)) {
            Node createdNode = createNode(theXQuery, theStartingNode, theDocument, true);

            if (createdNode == null) {
                String message =
                        "DOM Node could not be created for metadata '" + theMetadata.getType().getName() + "'! XQuery was '" + theXQuery + "'";
                log.error(message);
                throw new PreferencesException(message);
            }

            // Add value to node.
            Node valueNode = theDocument.createTextNode(newMetadataValue);
            if (StringUtils.isNotBlank(theMetadata.getAuthorityValue())) {
                if (theMetadata.getAuthorityValue().startsWith("http")) {
                    ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityValue());
                } else {
                    ((Element) createdNode).setAttribute("authority", theMetadata.getAuthorityID());
                    ((Element) createdNode).setAttribute("authorityURI", theMetadata.getAuthorityURI());
                    ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityURI() + theMetadata.getAuthorityValue());
                }
            }

            if (theMetadata.getType().isAllowAccessRestriction() && theMetadata.isAccessRestrict()) {
                ((Element) createdNode).setAttribute("shareable", "no");
            }

            createdNode.appendChild(valueNode);
            log.trace(
                    "Value '" + newMetadataValue + "' (" + theMetadata.getType().getName() + ") added to node >>" + createdNode.getNodeName() + "<<");
        }
    }

    protected void writeSingleModsGroup(MatchingMetadataObject mmo, MetadataGroup theGroup, Node theStartingNode, Document theDocument,
            String parentId) throws PreferencesException {
        Node createdNode = null;
        String xpath = mmo.getWriteXPath();
        if ("./mods:mods".equals(xpath) || "./".equals(xpath) || ".".equals(xpath) || "/".equals(xpath)) {
            createdNode = theStartingNode;
        } else if (xpath.startsWith("./@") || xpath.startsWith("@")) {
            createdNode = createNode(mmo.getWriteXPath(), theStartingNode, theDocument, false);
        } else {
            createdNode = createNode(mmo.getWriteXPath(), theStartingNode, theDocument, true);
        }
        String groupId = null;
        Map<String, Map<String, String>> xpathMap = mmo.getMetadataGroupXQueries();
        for (String metadataName : xpathMap.keySet()) {
            if ("GENERATED_ID".equals(metadataName)) {
                Map<String, String> xqueryMap = xpathMap.get(metadataName);
                String xquery = xqueryMap.get(metadataName);
                Node node;
                if (xquery.startsWith("./@") || xquery.startsWith("@")) {
                    node = createNode(xquery, createdNode, theDocument, false);
                } else {
                    node = createNode(xquery, createdNode, theDocument, true);
                }
                groupId = "id" + UUID.randomUUID().toString();
                Node valueNode = theDocument.createTextNode(groupId);
                node.appendChild(valueNode);
            } else if ("PARENT_ID".equals(metadataName) && StringUtils.isNotBlank(parentId)) {
                Map<String, String> xqueryMap = xpathMap.get(metadataName);
                String xquery = xqueryMap.get(metadataName);
                Node node;
                if (xquery.startsWith("./@") || xquery.startsWith("@")) {
                    node = createNode(xquery, createdNode, theDocument, false);
                } else {
                    node = createNode(xquery, createdNode, theDocument, true);
                }
                Node valueNode = theDocument.createTextNode(parentId);
                node.appendChild(valueNode);
            } else {
                for (Metadata md : theGroup.getMetadataList()) {
                    if (md.getType().getName().equals(metadataName) && md.getValue() != null && !md.getValue().isEmpty()) {
                        Map<String, String> xqueryMap = xpathMap.get(metadataName);
                        String xquery = xqueryMap.get(metadataName);
                        writeSingleModsMetadata(xquery, md, createdNode, theDocument);
                    }
                }
                for (Person p : theGroup.getPersonList()) {
                    if (p.getType().getName().equals(metadataName)) {
                        Map<String, String> xqueryMap = xpathMap.get(metadataName);
                        writeSingleGroupPerson(p, xqueryMap, createdNode, theDocument);
                    }
                }
                for (Corporate c : theGroup.getCorporateList()) {
                    if (c.getType().getName().equals(metadataName)) {
                        Map<String, String> xqueryMap = xpathMap.get(metadataName);
                        writeSingleGroupCorporate(c, xqueryMap, createdNode, theDocument);
                    }
                }
            }
        }
        for (MetadataGroup mg : theGroup.getAllMetadataGroups()) {
            // find correct mmo
            for (MatchingMetadataObject mm : this.modsNamesMD) {
                if (mm.getInternalName().equals(mg.getType().getName())) {
                    String groupPath = mm.getWriteXPath();
                    if (groupPath.startsWith("./mods:mods") || groupPath.startsWith("/mods:mods") || groupPath.startsWith("mods:mods")) {
                        Node n = createdNode;
                        while (!"mods:mods".equals(n.getNodeName())) {
                            n = n.getParentNode();
                        }
                        writeSingleModsGroup(mm, mg, n, theDocument, groupId);
                    } else if (groupPath.isEmpty()) {
                        writeSingleModsGroup(mm, mg, createdNode, theDocument, groupId);
                    } else if (groupPath.startsWith(".")) {
                        writeSingleModsGroup(mm, mg, createdNode, theDocument, groupId);
                    } else if (groupPath.startsWith("/")) {
                        writeSingleModsGroup(mm, mg, theStartingNode, theDocument, groupId);
                    }

                }
            }

        }
    }

    @Override
    public void writeSingleModsMetadata(String theXQuery, Metadata theMetadata, Node theStartingNode, Document theDocument)
            throws PreferencesException {
        Node createdNode = null;
        if (".".equals(theXQuery)) {
            createdNode = theStartingNode;
        } else if (theXQuery.startsWith("@") || theXQuery.startsWith("./@")) {
            createdNode = createNode(theXQuery, theStartingNode, theDocument, false);
        } else if (theXQuery.startsWith("./")) {
            createdNode = createNode(theXQuery, theStartingNode, theDocument, true);
        } else {
            if (theXQuery.startsWith("/")) {
                theXQuery = "." + theXQuery;
            } else {
                theXQuery = "./" + theXQuery;
            }
            createdNode = createNode(theXQuery, theStartingNode, theDocument, false);
        }
        if (createdNode == null) {
            String message = "DOM Node could not be created for metadata '" + theMetadata.getType().getName() + "'! XQuery was '" + theXQuery + "'";
            log.error(message);
            throw new PreferencesException(message);
        }

        // Add value to node.
        Node valueNode = theDocument.createTextNode(theMetadata.getValue());
        createdNode.appendChild(valueNode);
        if (StringUtils.isNotBlank(theMetadata.getAuthorityID()) && StringUtils.isNotBlank(theMetadata.getAuthorityURI())
                && StringUtils.isNotBlank(theMetadata.getAuthorityValue())) {
            if (createdNode instanceof Element) {
                if (theMetadata.getAuthorityValue().startsWith("http")) {
                    ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityValue());
                } else {
                    ((Element) createdNode).setAttribute("authority", theMetadata.getAuthorityID());
                    ((Element) createdNode).setAttribute("authorityURI", theMetadata.getAuthorityURI());
                    ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityURI() + theMetadata.getAuthorityValue());
                }
            }
        }

        log.trace("Value '" + theMetadata.getValue() + "' (" + theMetadata.getType().getName() + ") added to node >>" + createdNode.getNodeName()
                + "<<");
    }

    private void writeSingleGroupPerson(Person thePerson, Map<String, String> xpathMap, Node theDomModsNode, Document theDomDoc)
            throws PreferencesException {

        if ((thePerson.getLastname() != null && !"".equals(thePerson.getLastname()))
                || (thePerson.getFirstname() != null && !"".equals(thePerson.getFirstname()))) {
            if (thePerson.getLastname() != null && !"".equals(thePerson.getLastname()) && thePerson.getFirstname() != null
                    && !"".equals(thePerson.getFirstname())) {
                thePerson.setDisplayname(thePerson.getLastname() + ", " + thePerson.getFirstname());
            } else if (thePerson.getFirstname() == null || "".equals(thePerson.getFirstname())) {
                thePerson.setDisplayname(thePerson.getLastname());
            } else {
                thePerson.setDisplayname(thePerson.getFirstname());
            }
        }

        String xquery = xpathMap.get(METS_PREFS_WRITEXPATH_STRING);
        Node createdNode = createNode(xquery, theDomModsNode, theDomDoc, true);

        if (StringUtils.isNotBlank(thePerson.getFirstname())) {
            xquery = xpathMap.get(METS_PREFS_FIRSTNAMEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s firstname '" + thePerson.getFirstname() + "'");
            } else {
                Node firstnameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node firstnamevalueNode = theDomDoc.createTextNode(thePerson.getFirstname());
                firstnameNode.appendChild(firstnamevalueNode);
                createdNode.appendChild(firstnameNode);
            }
        }
        if (StringUtils.isNotBlank(thePerson.getLastname())) {
            xquery = xpathMap.get(METS_PREFS_LASTNAMEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s lastname '" + thePerson.getLastname() + "'");
            } else {
                Node lastnameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node lastnamevalueNode = theDomDoc.createTextNode(thePerson.getLastname());
                lastnameNode.appendChild(lastnamevalueNode);
                createdNode.appendChild(lastnameNode);
            }
        }

        if (StringUtils.isNotBlank(thePerson.getAffiliation())) {
            xquery = xpathMap.get(METS_PREFS_AFFILIATIONXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s affiliation '" + thePerson.getAffiliation() + "'");
            } else {
                Node affiliationNode = createNode(xquery, createdNode, theDomDoc, true);
                Node affiliationvalueNode = theDomDoc.createTextNode(thePerson.getAffiliation());
                affiliationNode.appendChild(affiliationvalueNode);
                createdNode.appendChild(affiliationNode);
            }
        }

        if (StringUtils.isNotBlank(thePerson.getDisplayname())) {
            xquery = xpathMap.get(METS_PREFS_DISPLAYNAMEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s displayName '" + thePerson.getDisplayname() + "'");
            } else {
                Node displaynameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node displaynamevalueNode = theDomDoc.createTextNode(thePerson.getDisplayname());
                displaynameNode.appendChild(displaynamevalueNode);
                createdNode.appendChild(displaynameNode);
            }
        }

        if (StringUtils.isNotBlank(thePerson.getPersontype())) {
            xquery = xpathMap.get(METS_PREFS_PERSONTYPEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s personType '" + thePerson.getPersontype() + "'");
            } else {
                Node persontypeNode = createNode(xquery, createdNode, theDomDoc, true);
                Node persontypevalueNode = theDomDoc.createTextNode(thePerson.getPersontype());
                persontypeNode.appendChild(persontypevalueNode);
                createdNode.appendChild(persontypeNode);
            }
        }

        if (thePerson.getAdditionalNameParts() != null && !thePerson.getAdditionalNameParts().isEmpty()) {
            for (NamePart namePart : thePerson.getAdditionalNameParts()) {
                if (namePart.getValue() != null && !namePart.getValue().isEmpty()) {
                    if (GOOBI_PERSON_DATEVALUE_STRING.equals(namePart.getType())) {
                        xquery = "./mods:namePart[@type='date']";
                        Node displaynameNode = createNode(xquery, createdNode, theDomDoc, true);
                        Node displaynamevalueNode = theDomDoc.createTextNode(namePart.getValue());
                        displaynameNode.appendChild(displaynamevalueNode);
                        createdNode.appendChild(displaynameNode);
                    } else if (GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING.equals(namePart.getType())) {
                        xquery = "./mods:namePart[@type='termsOfAddress']";
                        Node displaynameNode = createNode(xquery, createdNode, theDomDoc, true);
                        Node displaynamevalueNode = theDomDoc.createTextNode(namePart.getValue());
                        displaynameNode.appendChild(displaynamevalueNode);
                        createdNode.appendChild(displaynameNode);
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(thePerson.getAuthorityID()) && StringUtils.isNotBlank(thePerson.getAuthorityURI())
                && StringUtils.isNotBlank(thePerson.getAuthorityValue())) {
            if (thePerson.getAuthorityValue().startsWith("http")) {
                ((Element) createdNode).setAttribute("valueURI", thePerson.getAuthorityValue());
            } else {
                ((Element) createdNode).setAttribute("authority", thePerson.getAuthorityID());
                ((Element) createdNode).setAttribute("authorityURI", thePerson.getAuthorityURI());
                ((Element) createdNode).setAttribute("valueURI", thePerson.getAuthorityURI() + thePerson.getAuthorityValue());
            }
        }

        if (!thePerson.getAuthorityUriMap().isEmpty()) {
            for (Entry<String, String> entry : thePerson.getAuthorityUriMap().entrySet()) {
                xquery = "./mods:nameIdentifier[@name=" + entry.getKey() + "]";
                Node identifierNode = createNode(xquery, createdNode, theDomDoc, true);
                Node persontypevalueNode = theDomDoc.createTextNode(entry.getValue());
                identifierNode.appendChild(persontypevalueNode);
                createdNode.appendChild(identifierNode);
            }
        }

        if (thePerson.getType().isAllowAccessRestriction() && thePerson.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }
    }

    private void writeSingleGroupCorporate(Corporate corporate, Map<String, String> xpathMap, Node theDomModsNode, Document theDomDoc)
            throws PreferencesException {

        String xquery = xpathMap.get(METS_PREFS_WRITEXPATH_STRING);
        Node createdNode = createNode(xquery, theDomModsNode, theDomDoc, true);

        if (StringUtils.isNotBlank(corporate.getMainName())) {
            xquery = xpathMap.get(METS_PREFS_MAINNAMEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + corporate.getType().getName() + "'s main name '" + corporate.getMainName() + "'");
            } else {
                Node lastnameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node lastnamevalueNode = theDomDoc.createTextNode(corporate.getMainName());
                lastnameNode.appendChild(lastnamevalueNode);
                createdNode.appendChild(lastnameNode);
            }
        }

        if (corporate.getSubNames() != null) {
            xquery = xpathMap.get(METS_PREFS_SUBNAMEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + corporate.getType().getName() + "'s sub names '");
            } else {
                for (NamePart subName : corporate.getSubNames()) {
                    if (StringUtils.isNotBlank(subName.getValue())) {
                        Node firstnameNode = createNode(xquery, createdNode, theDomDoc, true);
                        Node firstnamevalueNode = theDomDoc.createTextNode(subName.getValue());
                        firstnameNode.appendChild(firstnamevalueNode);
                        createdNode.appendChild(firstnameNode);
                    }
                }
            }

        }
        if (StringUtils.isNotBlank(corporate.getPartName())) {
            xquery = xpathMap.get(METS_PREFS_PARTNAMEXPATH_STRING);
            if (xquery == null) {
                log.warn("No XQuery given for " + corporate.getType().getName() + "'s part name '" + corporate.getPartName() + "'");
            } else {
                Node affiliationNode = createNode(xquery, createdNode, theDomDoc, true);
                Node affiliationvalueNode = theDomDoc.createTextNode(corporate.getPartName());
                affiliationNode.appendChild(affiliationvalueNode);
                createdNode.appendChild(affiliationNode);
            }
        }

        if (StringUtils.isNotBlank(corporate.getAuthorityID()) && StringUtils.isNotBlank(corporate.getAuthorityURI())
                && StringUtils.isNotBlank(corporate.getAuthorityValue())) {
            if (corporate.getAuthorityValue().startsWith("http")) {
                ((Element) createdNode).setAttribute("valueURI", corporate.getAuthorityValue());
            } else {
                ((Element) createdNode).setAttribute("authority", corporate.getAuthorityID());
                ((Element) createdNode).setAttribute("authorityURI", corporate.getAuthorityURI());
                ((Element) createdNode).setAttribute("valueURI", corporate.getAuthorityURI() + corporate.getAuthorityValue());
            }
        }

        if (corporate.getType().isAllowAccessRestriction() && corporate.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }

    }

    /***************************************************************************
     * <p>
     * Creates a single Goobi MODS person element.
     * </p>
     * 
     * @param xquery
     * @param theMMO
     * @param thePerson
     * @param theDomModsNode
     * @param theDomDoc
     * @throws PreferencesException
     **************************************************************************/
    private void writeSingleModsPerson(String xquery, MatchingMetadataObject theMMO, Person thePerson, Node theDomModsNode, Document theDomDoc)
            throws PreferencesException {

        Node createdNode = createNode(xquery, theDomModsNode, theDomDoc, true);

        if (createdNode == null) {
            String message = "DOM Node could not be created for person '" + thePerson + "'! XQuery was '" + xquery + "'";
            log.error(message);
            throw new PreferencesException(message);
        }

        // Set the displayname of the current person, if NOT already set! Use
        // "lastname, name" as we were told in the MODS profile.

        if ((thePerson.getLastname() != null && !"".equals(thePerson.getLastname()))
                || (thePerson.getFirstname() != null && !"".equals(thePerson.getFirstname()))) {
            if (thePerson.getLastname() != null && !"".equals(thePerson.getLastname()) && thePerson.getFirstname() != null
                    && !"".equals(thePerson.getFirstname())) {
                thePerson.setDisplayname(thePerson.getLastname() + ", " + thePerson.getFirstname());
            } else if (thePerson.getFirstname() == null || "".equals(thePerson.getFirstname())) {
                thePerson.setDisplayname(thePerson.getLastname());
            } else {
                thePerson.setDisplayname(thePerson.getFirstname());
            }
        }

        // Create the subnodes.
        if (thePerson.getLastname() != null) {
            xquery = theMMO.getLastnameXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s lastname '" + thePerson.getLastname() + "'");
            } else {
                Node lastnameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node lastnamevalueNode = theDomDoc.createTextNode(thePerson.getLastname());
                lastnameNode.appendChild(lastnamevalueNode);
                createdNode.appendChild(lastnameNode);
            }
        }
        if (thePerson.getFirstname() != null) {
            xquery = theMMO.getFirstnameXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s firstname '" + thePerson.getFirstname() + "'");
            } else {
                Node firstnameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node firstnamevalueNode = theDomDoc.createTextNode(thePerson.getFirstname());
                firstnameNode.appendChild(firstnamevalueNode);
                createdNode.appendChild(firstnameNode);
            }
        }
        if (thePerson.getAffiliation() != null) {
            xquery = theMMO.getAffiliationXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s affiliation '" + thePerson.getAffiliation() + "'");
            } else {
                Node affiliationNode = createNode(xquery, createdNode, theDomDoc, true);
                Node affiliationvalueNode = theDomDoc.createTextNode(thePerson.getAffiliation());
                affiliationNode.appendChild(affiliationvalueNode);
                createdNode.appendChild(affiliationNode);
            }
        }

        if (StringUtils.isNotBlank(thePerson.getAuthorityID()) && StringUtils.isNotBlank(thePerson.getAuthorityURI())
                && StringUtils.isNotBlank(thePerson.getAuthorityValue())) {
            if (thePerson.getAuthorityValue().startsWith("http")) {
                ((Element) createdNode).setAttribute("valueURI", thePerson.getAuthorityValue());
            } else {
                ((Element) createdNode).setAttribute("authority", thePerson.getAuthorityID());
                ((Element) createdNode).setAttribute("authorityURI", thePerson.getAuthorityURI());
                ((Element) createdNode).setAttribute("valueURI", thePerson.getAuthorityURI() + thePerson.getAuthorityValue());
            }
        }
        if (thePerson.getDisplayname() != null) {
            xquery = theMMO.getDisplayNameXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s displayName '" + thePerson.getDisplayname() + "'");
            } else {
                Node displaynameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node displaynamevalueNode = theDomDoc.createTextNode(thePerson.getDisplayname());
                displaynameNode.appendChild(displaynamevalueNode);
                createdNode.appendChild(displaynameNode);
            }
        }
        if (thePerson.getPersontype() != null) {
            xquery = theMMO.getPersontypeXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + thePerson.getType().getName() + "'s personType '" + thePerson.getPersontype() + "'");
            } else {
                Node persontypeNode = createNode(xquery, createdNode, theDomDoc, true);
                Node persontypevalueNode = theDomDoc.createTextNode(thePerson.getPersontype());
                persontypeNode.appendChild(persontypevalueNode);
                createdNode.appendChild(persontypeNode);
            }
        }

        if (thePerson.getAdditionalNameParts() != null && !thePerson.getAdditionalNameParts().isEmpty()) {
            for (NamePart namePart : thePerson.getAdditionalNameParts()) {
                if (namePart.getValue() != null && !namePart.getValue().isEmpty()) {
                    if (GOOBI_PERSON_DATEVALUE_STRING.equals(namePart.getType())) {
                        xquery = "./mods:namePart[@type='date']";
                        Node displaynameNode = createNode(xquery, createdNode, theDomDoc, true);
                        Node displaynamevalueNode = theDomDoc.createTextNode(namePart.getValue());
                        displaynameNode.appendChild(displaynamevalueNode);
                        createdNode.appendChild(displaynameNode);
                    } else if (GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING.equals(namePart.getType())) {
                        xquery = "./mods:namePart[@type='termsOfAddress']";
                        Node displaynameNode = createNode(xquery, createdNode, theDomDoc, true);
                        Node displaynamevalueNode = theDomDoc.createTextNode(namePart.getValue());
                        displaynameNode.appendChild(displaynamevalueNode);
                        createdNode.appendChild(displaynameNode);
                    }
                }
            }
        }
        if (!thePerson.getAuthorityUriMap().isEmpty()) {
            for (Entry<String, String> entry : thePerson.getAuthorityUriMap().entrySet()) {
                xquery = "./mods:nameIdentifier[@type=" + entry.getKey() + "]";
                Node identifierNode = createNode(xquery, createdNode, theDomDoc, true);
                Node persontypevalueNode = theDomDoc.createTextNode(entry.getValue());
                identifierNode.appendChild(persontypevalueNode);
                createdNode.appendChild(identifierNode);
            }
        }

        if (thePerson.getType().isAllowAccessRestriction() && thePerson.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }
    }

    private void writeSingleModsCorporate(String xquery, MatchingMetadataObject theMMO, Corporate corporate, Node theDomModsNode, Document theDomDoc)
            throws PreferencesException {

        Node createdNode = createNode(xquery, theDomModsNode, theDomDoc, true);

        if (createdNode == null) {
            String message = "DOM Node could not be created for corporate '" + corporate + "'! XQuery was '" + xquery + "'";
            log.error(message);
            throw new PreferencesException(message);
        }

        // Create the subnodes.
        if (StringUtils.isNotBlank(corporate.getMainName())) {
            xquery = theMMO.getMainNameXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + corporate.getType().getName() + "'s main name '" + corporate.getMainName() + "'");
            } else {
                Node lastnameNode = createNode(xquery, createdNode, theDomDoc, true);
                Node lastnamevalueNode = theDomDoc.createTextNode(corporate.getMainName());
                lastnameNode.appendChild(lastnamevalueNode);
                createdNode.appendChild(lastnameNode);
            }
        }

        if (corporate.getSubNames() != null) {
            xquery = theMMO.getSubNameXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + corporate.getType().getName() + "'s sub names '");
            } else {
                for (NamePart subName : corporate.getSubNames()) {
                    if (StringUtils.isNotBlank(subName.getValue())) {
                        Node firstnameNode = createNode(xquery, createdNode, theDomDoc, true);
                        Node firstnamevalueNode = theDomDoc.createTextNode(subName.getValue());
                        firstnameNode.appendChild(firstnamevalueNode);
                        createdNode.appendChild(firstnameNode);
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(corporate.getPartName())) {
            xquery = theMMO.getPartNameXQuery();
            if (xquery == null) {
                log.warn("No XQuery given for " + corporate.getType().getName() + "'s part name '" + corporate.getPartName() + "'");
            } else {
                Node affiliationNode = createNode(xquery, createdNode, theDomDoc, true);
                Node affiliationvalueNode = theDomDoc.createTextNode(corporate.getPartName());
                affiliationNode.appendChild(affiliationvalueNode);
                createdNode.appendChild(affiliationNode);
            }
        }

        if (StringUtils.isNotBlank(corporate.getAuthorityID()) && StringUtils.isNotBlank(corporate.getAuthorityURI())
                && StringUtils.isNotBlank(corporate.getAuthorityValue())) {
            if (corporate.getAuthorityValue().startsWith("http")) {
                ((Element) createdNode).setAttribute("valueURI", corporate.getAuthorityValue());
            } else {
                ((Element) createdNode).setAttribute("authority", corporate.getAuthorityID());
                ((Element) createdNode).setAttribute("authorityURI", corporate.getAuthorityURI());
                ((Element) createdNode).setAttribute("valueURI", corporate.getAuthorityURI() + corporate.getAuthorityValue());
            }
        }

        if (corporate.getType().isAllowAccessRestriction() && corporate.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }
    }

    /***************************************************************************
     * <p>
     * Finds the METS type element for a given DocStructType; iterates over the modNamesDS list.
     * </p>
     * 
     * @param metstype
     * @return METS type string for the given internal DocStructType.
     **************************************************************************/
    private String getMetsType(DocStructType docStructType) {

        for (MatchingDocStructObject mds : this.modsNamesDS) {
            if (mds.getInternaltype() != null && mds.getInternaltype().getName().equals(docStructType.getName())) {
                return mds.getMetstype();
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Gives a warning string including all missing MODS mappings.
     * </p>
     * 
     * @param theStruct
     * @param theList
     * @return
     **************************************************************************/
    private String getMappingWarning(DocStructType theStruct, List<Object> theList) {

        String result = "";

        if (theStruct != null && !theList.isEmpty()) {
            StringBuilder listEntries = new StringBuilder();

            for (Object o : theList) {
                if (o instanceof Metadata || o instanceof Person) {
                    Metadata m = (Metadata) o;
                    if (m.getValue() != null && !"".equals(m.getValue())) {
                        listEntries.append("[" + m.getType().getName() + ":'" + m.getValue() + "'] ");
                    } else {
                        listEntries.append("[" + m.getType().getName() + "] ");
                    }
                }
            }

            result = "The following metadata types for DocStruct '" + theStruct.getName() + "' are NOT YET mapped to the MODS: "
                    + listEntries.toString().trim();
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Parses a single &lt;Metadata> element in the METS section of the preference file. For the element an appropriate MatchingMetadataObject is
     * created and added to the list of all MatchingMetadataObjects - modsNamesMD.
     * </p>
     * 
     * @param inNode the DOM node of the opening tag of the &lt;metadata> element
     * @throws PreferencesException
     **************************************************************************/
    @Override
    protected void readMetadataPrefs(Node inNode) throws PreferencesException {

        String internalName = null;
        String personName = null;
        String modsName = null;
        NodeList childlist = inNode.getChildNodes();
        MatchingMetadataObject mmo = new MatchingMetadataObject();

        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);

            if (currentNode.getNodeName() == null) {
                continue;
            }

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                // Get internal name.
                if (METS_PREFS_INTERNALNAME_STRING.equals(currentNode.getNodeName())) {
                    internalName = getTextNodeValue(currentNode);

                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_INTERNALNAME_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        log.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setInternalName(internalName.trim());
                }

                // Get valueCondition.
                if (METS_PREFS_VALUECONDITION_STRING.equals(currentNode.getNodeName())) {
                    internalName = getTextNodeValue(currentNode);
                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_VALUECONDITION_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        log.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setValueCondition(internalName.trim());
                }

                // Get valueRegExp.
                if (METS_PREFS_VALUEREGEXP_STRING.equals(currentNode.getNodeName())) {
                    internalName = getTextNodeValue(currentNode);
                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_VALUEREGEXP_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        log.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setValueRegExp(internalName.trim());
                }

                // Get MODS XPATH settings.
                if (METS_PREFS_XPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    String xpathName = getTextNodeValue(currentNode);
                    if (xpathName != null) {
                        mmo.setReadXQuery(xpathName.trim());
                    }
                }
                if (METS_PREFS_WRITEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    String xpathName = getTextNodeValue(currentNode);
                    if (xpathName == null) {
                        throw new PreferencesException("<" + METS_PREFS_WRITEXPATH_STRING + "> is existing, but has no value!");
                    }
                    mmo.setWriteXQuery(xpathName.trim());
                }
                // Get MODS Person settings.
                if (METS_PREFS_FIRSTNAMEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setFirstnameXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_LASTNAMEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setLastnameXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_AFFILIATIONXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setAffiliationXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_DISPLAYNAMEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setDisplayNameXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_PERSONTYPEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setPersontypeXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_AUTHORITYFILEIDXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setAuthorityIDXquery(personName.trim());
                    }
                }

                if (METS_PREFS_MAINNAMEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setMainNameXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_SUBNAMEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setSubNameXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_PARTNAMEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setPartNameXQuery(personName.trim());
                    }
                }

                if (METS_PREFS_IDENTIFIERXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setIdentifierXQuery(personName.trim());
                    }
                }
                if (METS_PREFS_IDENTIFIERTYPEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setIdentifierTypeXQuery(personName.trim());
                    }
                }

                // Get other MODS settings (used for reading only?).
                if (METS_PREFS_READMODSNAME_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setReadModsName(modsName.trim());
                    }
                }
                if (METS_PREFS_WRITEMODSNAME_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setReadModsName(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSTYPE_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSType(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSENCODING_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSEncoding(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSAUTHORITY_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSAuthority(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSLANG_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSLang(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSXMLLANG_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSXMLLang(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSID_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSID(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSSCRIPT_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSScript(modsName.trim());
                    }
                }
                if (METS_PREFS_MODSTRANSLITERATION_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSTransliteration(modsName.trim());
                    }
                }
            }
        }

        // The internal name is needed for every MMO!
        if (mmo.getInternalName() != null) {
            this.modsNamesMD.add(mmo);
        }
    }

    @Override
    protected void readMetadataGroupPrefs(Node inNode) throws PreferencesException {
        String internalName = null;

        NodeList childlist = inNode.getChildNodes();
        MatchingMetadataObject mmo = new MatchingMetadataObject();

        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);

            if (currentNode.getNodeName() == null) {
                continue;
            }

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                // Get internal name.
                if (METS_PREFS_INTERNALNAME_STRING.equals(currentNode.getNodeName())) {
                    internalName = getTextNodeValue(currentNode);

                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_INTERNALNAME_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        log.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setInternalName(internalName.trim());
                }

                // Get MODS XPATH settings.

                if (METS_PREFS_WRITEXPATH_STRING.equalsIgnoreCase(currentNode.getNodeName())) {
                    String xpathName = getTextNodeValue(currentNode);
                    if (xpathName == null) {
                        throw new PreferencesException("<" + METS_PREFS_WRITEXPATH_STRING + "> is existing, but has no value!");
                    }
                    mmo.setWriteXQuery(xpathName.trim());
                }

                if ("Metadata".equalsIgnoreCase(currentNode.getNodeName())) {

                    NodeList metadataChildlist = currentNode.getChildNodes();

                    String elementName = "";
                    String xpath = "";
                    for (int k = 0; k < metadataChildlist.getLength(); k++) {
                        // Get single node.

                        Node metadataSubElement = metadataChildlist.item(k);

                        if (metadataSubElement.getNodeType() == ELEMENT_NODE) {
                            // Get internal name.
                            if (METS_PREFS_INTERNALNAME_STRING.equals(metadataSubElement.getNodeName())) {
                                elementName = getTextNodeValue(metadataSubElement);
                            } else if (METS_PREFS_WRITEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                xpath = getTextNodeValue(metadataSubElement);

                            }
                        }

                    }
                    if (!elementName.isEmpty() && !xpath.isEmpty()) {
                        Map<String, String> map = new LinkedHashMap<>();
                        map.put(elementName, xpath);
                        mmo.addToMap(elementName, map);
                    }
                } else if ("Person".equalsIgnoreCase(currentNode.getNodeName())) {

                    NodeList metadataChildlist = currentNode.getChildNodes();

                    String elementName = "";
                    Map<String, String> map = new HashMap<>();
                    for (int k = 0; k < metadataChildlist.getLength(); k++) {
                        // Get single node.

                        Node metadataSubElement = metadataChildlist.item(k);
                        if (metadataSubElement.getNodeType() == ELEMENT_NODE) {
                            // Get internal name.
                            if (METS_PREFS_INTERNALNAME_STRING.equals(metadataSubElement.getNodeName())) {
                                elementName = getTextNodeValue(metadataSubElement);
                            } else if (METS_PREFS_WRITEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_WRITEXPATH_STRING, value.trim());
                                }
                            } else if (METS_PREFS_FIRSTNAMEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_FIRSTNAMEXPATH_STRING, value.trim());
                                }
                            } else if (METS_PREFS_LASTNAMEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_LASTNAMEXPATH_STRING, value.trim());
                                }
                            } else if (METS_PREFS_AFFILIATIONXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_AFFILIATIONXPATH_STRING, value.trim());
                                }

                            } else if (METS_PREFS_DISPLAYNAMEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_DISPLAYNAMEXPATH_STRING, value.trim());
                                }

                            } else if (METS_PREFS_PERSONTYPEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_PERSONTYPEXPATH_STRING, value.trim());
                                }

                            } else if (METS_PREFS_AUTHORITYFILEIDXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_AUTHORITYFILEIDXPATH_STRING, value.trim());
                                }

                            } else if (METS_PREFS_IDENTIFIERXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_IDENTIFIERXPATH_STRING, value.trim());
                                }

                            } else if (METS_PREFS_IDENTIFIERTYPEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_IDENTIFIERTYPEXPATH_STRING, value.trim());
                                }

                                // Get other MODS settings (used for reading only?).
                            }
                        }
                    }
                    mmo.addToMap(elementName, map);
                } else if ("Corporate".equalsIgnoreCase(currentNode.getNodeName())) {

                    NodeList metadataChildlist = currentNode.getChildNodes();

                    String elementName = "";
                    Map<String, String> map = new HashMap<>();
                    for (int k = 0; k < metadataChildlist.getLength(); k++) {
                        // Get single node.

                        Node metadataSubElement = metadataChildlist.item(k);
                        if (metadataSubElement.getNodeType() == ELEMENT_NODE) {
                            // Get internal name.
                            if (METS_PREFS_INTERNALNAME_STRING.equals(metadataSubElement.getNodeName())) {
                                elementName = getTextNodeValue(metadataSubElement);
                            } else if (METS_PREFS_WRITEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_WRITEXPATH_STRING, value.trim());
                                }
                            } else if (METS_PREFS_MAINNAMEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_MAINNAMEXPATH_STRING, value.trim());
                                }
                            } else if (METS_PREFS_SUBNAMEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_SUBNAMEXPATH_STRING, value.trim());
                                }
                            } else if (METS_PREFS_PARTNAMEXPATH_STRING.equalsIgnoreCase(metadataSubElement.getNodeName())) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_PARTNAMEXPATH_STRING, value.trim());
                                }

                            }
                        }
                    }
                    mmo.addToMap(elementName, map);
                }
            }
        }

        // The internal name is needed for every MMO!
        if (mmo.getInternalName() != null) {
            this.modsNamesMD.add(mmo);
        }
    }

    /***************************************************************************
     * <p>
     * Reads the DocStruct settings from the preferences fie.
     * </p>
     * 
     * @param inNode
     **************************************************************************/
    @Override
    protected void readDocStructPrefs(Node inNode) throws PreferencesException {

        NodeList childlist = inNode.getChildNodes();
        MatchingDocStructObject mds = new MatchingDocStructObject();

        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);
            String nodename = currentNode.getNodeName();
            if (nodename == null) {
                continue;
            }
            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if (METS_PREFS_INTERNALNAME_STRING.equalsIgnoreCase(nodename)) {
                    String internalName = getTextNodeValue(currentNode);
                    if (internalName != null) {
                        DocStructType internalType = this.myPreferences.getDocStrctTypeByName(internalName.trim());
                        mds.setInternaltype(internalType);
                    } else {
                        String message =
                                "<" + METS_PREFS_INTERNALNAME_STRING + "> is existing in " + PREFS_DOCSTRUCT_STRING + " mapping, but has no value!";
                        log.error(message);
                        throw new PreferencesException(message);
                    }
                }

                if (METS_PREFS_METSTYPE_STRING.equalsIgnoreCase(nodename)) {
                    String metstypename = getTextNodeValue(currentNode);
                    if (metstypename != null) {
                        mds.setMetstype(metstypename.trim());
                    }
                }
            }
        }

        // The internal type is needed for every MDS!
        if (mds.getInternaltype() != null) {
            this.modsNamesDS.add(mds);
        }
    }

    /**************************************************************************
     * <p>
     * PLEASE DO NOT TELL ANYONE! This is just a small grouping hack, until the MODS creation is implemented maybe using MODS XML Beans!
     * </p>
     * 
     * TODO This is a really dirty hack, I will fix it tomorrow! (hihi)
     * 
     * @param theModsNode
     * 
     **************************************************************************/

    @Deprecated
    private void dirtyReplaceGroupingTagNameHack(Node theNode) {

        // Replace things.
        if (this.replaceGroupTags.containsKey(theNode.getLocalName())) {
            // Get replacement name.
            String replacementName = this.replaceGroupTags.get(theNode.getLocalName());
            // Create replacement node.
            Node replacementNode = createDomElementNS(theNode.getOwnerDocument(), theNode.getPrefix(), replacementName);
            // Copy all children from the old node to the new node.
            if (theNode.hasChildNodes()) {
                for (int i = 0; i < theNode.getChildNodes().getLength(); i++) {
                    replacementNode.appendChild(theNode.getChildNodes().item(i).cloneNode(true));
                }
            }
            // Copy all attributes of the old node to the new one.
            if (theNode.hasAttributes()) {
                for (int i = 0; i < theNode.getAttributes().getLength(); i++) {
                    replacementNode.appendChild(theNode.getAttributes().item(i).cloneNode(true));
                }
            }
            // Finally replace the node.
            theNode.getParentNode().replaceChild(replacementNode, theNode);

            log.trace("Tag '" + theNode.getLocalName() + "' replaced with '" + replacementName + "'! DO NOT TELL ANYONE!");
        }

        // Get all child nodes and iterate, if some do exist.
        if (theNode.hasChildNodes()) {
            for (int i = 0; i < theNode.getChildNodes().getLength(); i++) {
                dirtyReplaceGroupingTagNameHack(theNode.getChildNodes().item(i));
            }
        }
    }

    /**************************************************************************
     * <p>
     * Substitutes an existing $REGEXP().
     * </p>
     * 
     * 
     * @param theString
     * @return
     **************************************************************************/
    private String checkForRegExp(String theString) {

        // Look, if things shall be substituted.

        Pattern pattern = Pattern.compile("\\$REGEXP(.*)");
        Matcher matcher = pattern.matcher(theString);
        if (matcher.find()) {
            // Get the index of the "(" and the index of the ")".
            int bracketStartIndex = matcher.start();
            int bracketEndIndex = matcher.end();

            // Get the RegExp out of the string.
            String regExp = theString.substring(bracketStartIndex + 10, bracketEndIndex - 2);

            String[] parts = regExp.split("(?<!\\\\)\\/"); // slash that is not preceded by a backslash
            // Remove the RegExp from the string.
            theString = theString.substring(0, bracketStartIndex);
            String searchValue = parts[0];
            String replacement = "";
            // replace with empty string, if no replacement string exists
            if (parts.length > 1) {
                replacement = parts[1];
            }
            // Substitute things, if any $REGEXP() is existing.
            theString = theString.replaceAll(searchValue, replacement);
        }

        return theString;
    }

    /***************************************************************************
     * GETTERS AND SETTERS
     **************************************************************************/

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwner() {
        return this.rightsOwner;
    }

    /***************************************************************************
     * @param rightsOwner
     **************************************************************************/
    @Override
    public void setRightsOwner(String rightsOwner) {

        if (rightsOwner == null) {
            this.rightsOwner = "";
        } else {
            this.rightsOwner = checkForRegExp(rightsOwner);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwnerLogo() {
        return this.rightsOwnerLogo;
    }

    /***************************************************************************
     * @param rightsOwnerLogo
     **************************************************************************/
    @Override
    public void setRightsOwnerLogo(String rightsOwnerLogo) {

        if (rightsOwnerLogo == null) {
            this.rightsOwnerLogo = "";
        } else {
            this.rightsOwnerLogo = checkForRegExp(rightsOwnerLogo);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwnerSiteURL() {
        return this.rightsOwnerSiteURL;
    }

    /***************************************************************************
     * @param rightsOwnerSiteURL
     **************************************************************************/
    @Override
    public void setRightsOwnerSiteURL(String rightsOwnerSiteURL) {

        if (rightsOwnerSiteURL == null) {
            this.rightsOwnerSiteURL = "";
        } else {
            this.rightsOwnerSiteURL = checkForRegExp(rightsOwnerSiteURL);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwnerContact() {
        return this.rightsOwnerContact;
    }

    /***************************************************************************
     * @param rightsOwnerContact
     **************************************************************************/
    @Override
    public void setRightsOwnerContact(String rightsOwnerContact) {

        if (rightsOwnerContact == null) {
            this.rightsOwnerContact = "";
        } else {
            this.rightsOwnerContact = checkForRegExp(rightsOwnerContact);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovReference() {
        return this.digiprovReference;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv reference and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovReference
     **************************************************************************/
    @Override
    public void setDigiprovReference(String digiprovReference) {

        if (digiprovReference == null) {
            this.digiprovReference = "";
        } else {
            this.digiprovReference = checkForRegExp(digiprovReference);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovPresentation() {
        return this.digiprovPresentation;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv presentation and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovPresentation
     **************************************************************************/
    @Override
    public void setDigiprovPresentation(String digiprovPresentation) {

        if (digiprovPresentation == null) {
            this.digiprovPresentation = "";
        } else {
            this.digiprovPresentation = checkForRegExp(digiprovPresentation);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovReferenceAnchor() {
        return this.digiprovReferenceAnchor;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv anchor reference and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovReference
     **************************************************************************/
    @Override
    public void setDigiprovReferenceAnchor(String digiprovReferenceAnchor) {

        if (digiprovReferenceAnchor == null) {
            this.digiprovReferenceAnchor = "";
        } else {
            this.digiprovReferenceAnchor = checkForRegExp(digiprovReferenceAnchor);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovPresentationAnchor() {
        return this.digiprovPresentationAnchor;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv anchor presentation and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovPresentation
     **************************************************************************/
    @Override
    public void setDigiprovPresentationAnchor(String digiprovPresentationAnchor) {

        if (digiprovPresentationAnchor == null) {
            this.digiprovPresentationAnchor = "";
        } else {
            this.digiprovPresentationAnchor = checkForRegExp(digiprovPresentationAnchor);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getPurlUrl() {
        return this.purlUrl;
    }

    /***************************************************************************
     * <p>
     * Set the PURL URL and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param purlUrl
     **************************************************************************/
    @Override
    public void setPurlUrl(String purlUrl) {

        if (purlUrl == null) {
            this.purlUrl = "";
        } else {
            this.purlUrl = checkForRegExp(purlUrl);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getContentIDs() {
        return this.contentIDs;
    }

    /***************************************************************************
     * <p>
     * Set the content IDs and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param contentIDs
     **************************************************************************/
    @Override
    public void setContentIDs(String contentIDs) {

        if (contentIDs == null) {
            this.contentIDs = "";
        } else {
            this.contentIDs = checkForRegExp(contentIDs);
        }
    }

    @Override
    public void setMetsRightsLicense(String metsRightsLicense) {
        this.metsRightsLicense = metsRightsLicense;

    }

    @Override
    public void setIIIFUrl(String iifApiUrl) {
        iiifUrl = iifApiUrl;
    }

    @Override
    public void setSruUrl(String sruUrl) {
        this.sruUrl = sruUrl;
    }

    @Override
    public void setMetsRightsSponsor(String metsRightsSponsor) {
        this.metsRightsSponsor = metsRightsSponsor;
    }

    @Override
    public void setMetsRightsSponsorLogo(String metsRightsSponsorLogo) {
        this.metsRightsSponsorLogo = metsRightsSponsorLogo;
    }

    @Override
    public void setMetsRightsSponsorSiteURL(String metsRightsSponsorSiteURL) {
        this.metsRightsSponsorSiteURL = metsRightsSponsorSiteURL;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public static String getVersion() {
        return VERSION;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isExportable() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Mets";
    }

    @Override
    public void setCreateUUIDs(boolean createUUIDs) {
        this.createUUIDs = createUUIDs;
    }

    public static List<String> splitRegularExpression(final String regex) {
        List<String> values = new ArrayList<>();
        if (StringUtils.isBlank(regex)) {
            return values;
        }
        if (!regex.contains("/")) {
            values.add(regex);
            return values;
        }

        // remove optional s/../../ and /../../g
        String expression = regex.substring(regex.indexOf("/"), regex.lastIndexOf("/") + 1);
        // split regex at the "/" symbol, if it was not escaped by "\/"
        String[] parts = expression.split("(?<!\\\\)\\/"); // slash that is not preceded by a backslash
        String searchValue = parts[1];
        values.add(searchValue);
        if (parts.length > 2) {
            String replacement = parts[2];
            values.add(replacement);
        }
        // handle empty replacement value
        else if (expression.endsWith("//")) {
            values.add("");
        }

        return values;
    }
}
