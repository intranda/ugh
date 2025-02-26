package ugh.fileformats.mets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptionCharEscapeMap;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/*******************************************************************************
 * ugh.fileformats.mets / MetsMods.java
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

import gov.loc.mets.AmdSecType;
import gov.loc.mets.AreaType;
import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.FileType;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.Helper;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MdSecType.MdWrap;
import gov.loc.mets.MdSecType.MdWrap.XmlData;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsDocument.Mets;
import gov.loc.mets.MetsType.FileSec;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mets.MetsType.StructLink;
import gov.loc.mets.SeqType;
import gov.loc.mets.StructLinkType.SmLink;
import gov.loc.mets.StructMapType;
import gov.loc.mods.v3.ModsDocument;
import lombok.extern.log4j.Log4j2;
import ugh.dl.AmdSec;
import ugh.dl.ContentFile;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.FileSet;
import ugh.dl.Md;
import ugh.dl.Md.MdType;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.PrefsType;
import ugh.dl.Reference;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.ImportException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.MissingModsMappingException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

/*******************************************************************************
 * @author Stefan Funk
 * @author Robert Sehr
 * @version 2013-05-08
 * @since 2008-05-09
 * 
 *        TODOLOG
 * 
 *        REFACTOR ALL THE XPATH PARSING STUFF!!
 * 
 *        Separate the VirtualFileGroup usage, put it into MetsModsImportExport!
 * 
 *        Get the anchor files from the METS' mptrs, and not via filename! Don't we do that already?
 * 
 *        Check if there is a metadata with type="identifier" is existing in those DocStructs with anchor="true"! Already checked?
 * 
 *        Maybe read the content files while reading the DocStructs and then use the MetsHelper to retrieve things!
 * 
 *        CHANGELOG
 * 
 *        05.05.2010 --- Funk --- Commented out some DPD-407 debigging checks. --- Added check for empty displayName at displayName creation.
 * 
 *        11.03.2010 --- Funk --- Closing ">"s now are escaped with &gt; at XML document storing.
 * 
 *        10.03.2010 --- Funk --- Added ValueRegExps to AnchorIdentifier.
 * 
 *        03.03.2010 --- Funk --- Fixed the vanishing-of-XPath-element-values-if-attributes-are-existing-bug. Using Java RegExps instead of single
 *        string char processing!
 * 
 *        25.02.2010 --- Funk --- If no MODS section is existing for a child of an anchor DocStruct, fail due to missing backlink. --- Changed some
 *        WARNINGs into DEBUG loglevel. --- Changed a "&" into a "&&", was actually a typo! --- Throw a WriteException in writeLogDmd(), if the child
 *        of anchor DocStruct has no MODS metadata! We have then no identifier!
 * 
 *        24.02.2010 --- Funk --- " "s are now also beeing ignored inside of "'"s in the prefs XPath configuration.
 * 
 *        15.02.2010 --- Funk --- Logging version information now.
 * 
 *        14.02.2010 --- Funk --- Commented the whitespace things.
 * 
 *        12.02.2010 --- Funk --- "/"s are now beeing ignored inside of "'"s in the prefs XPath configuration.
 * 
 *        26.01.2010 --- Funk --- Fixed a bug not writing all the SMLINKS into the METS file in writeSMLinks.
 * 
 *        18.01.2010 --- Funk --- Adapted class to changed DocStruct.getAllMetadataByType().
 * 
 *        23.12.2009 --- Funk --- Slightly improved the grouping functionality.
 * 
 *        22.12.2009 --- Funk --- Added some grouping functionality.
 * 
 *        21.12.2009 --- Funk --- Added some "? extends " to metadata things.
 * 
 *        14.12.2009 --- Funk --- Fixed bug forgetting to write the CatalogIDDigital in!
 * 
 *        09.12.2009 --- Funk --- Refactored the whole addAllContentFile() thing: Removed unnecesarry FPTR and addFile()s. --- Deleted unused stuff
 *        concerning FPTRs. --- Creating ContentFiles now, if no FileGroup LOCAL is existing.
 * 
 *        08.12.2009 --- Funk --- Fixed bug with FPTRs.
 * 
 *        03.12.2009 --- Funk --- Write a person, if a role is existing and a firstname OR a lastname. --- Generalized writing persons to the MODS,
 *        created writeDmdPersons().
 * 
 *        16.11.2009 --- Funk --- Always check for new content files in non-anchor documents if storing the METS file.
 * 
 *        13.11.2009 --- Funk --- Added check for non-existing anchorIdentifierMetadata Type.
 * 
 *        30.10.2009 --- Funk --- Improved XML date and RDFFile version comment.
 * 
 *        29.10.2009 --- Funk --- Fixed a NPE accessing DocStructs without children. --- WE NEED THOSE NULL RETURN VALUES TO BE REMOVED!!!!!!
 * 
 *        26.10.2009 --- Funk --- Removed the constructor without Prefs object. We really need that Prefs thing! --- Added finals for namespace
 *        prefixes, uris, and schema locations.
 * 
 *        20.10.2009 --- Funk --- smlink section is only read for non-anchor structs now, fixes bug DPD-352 --- Added modifiers to all class
 *        atributes.
 * 
 *        19.10.2009 --- Funk --- Persons with last name OR first name == "" are now be written, too!
 * 
 *        13.10.2009 --- Funk --- Fixed bug with smlink attributes, now the have an xlink namespace prefix! --- Fixed bug with NullPointerExceptions
 *        in parseMetadataForPhysicalDocStruct() at setting content files.
 * 
 *        06.10.2009 --- Funk --- Corrected some not-conform-to-rules variable names.
 * 
 *        05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *        24.09.2009 --- Funk --- Refactored all the Exception things.
 * 
 *        22.09.2009 --- Funk --- Removed checking of all not needed tags in the METS formats section.
 * 
 *        21.09.2009 --- Funk --- Removed returning FALSE in readMetadataPrefs(), if a tag is set in the METS section but has no values. That is
 *        ignored now. Checked are only if <internalName> and <writeXPath> are existing --- Removed the class readPrefs from MetsMods, and put it into
 *        MetsModeImportExport. It is not needed here.
 * 
 *        11.09.2009 --- Funk --- Created a final static for the anchor filename suffix.
 * 
 *        27.08.2009 --- Funk --- Added version string comment to METS file.
 * 
 *        18.08.2009 --- Funk --- Changed Goobi namespace from "http://meta.goobi.org/v1.5.1" to "http://meta.goobi.org/v1.5.1/". --- Changed
 *        exception handling in checkForAnchorReference(), now an Exception is thrown, if an anchor file is not existing and if an anchor reference is
 *        not found.
 * 
 *        22.07.2009 --- Funk --- Fixed the non-read-internal-periodicals-bug. --- Added HTML tags to JavaDOC.
 * 
 *        17.07.2009 --- Funk --- Removed the excalibur XML parser kwatsch!
 * 
 *        16.07.2009 --- Funk --- Namespaces are handled correctly now. METS is serialised using the METS XMLBeans.
 * 
 *        08.07.2009 --- Funk --- Namespaces now are first defined with default values, then definitions from the prefs are considered and default
 *        values are beeing changed.
 * 
 *        26.06.2009 --- Funk --- ADMSEC is written no more for internal METS.
 * 
 *        18.06.2009 --- Funk --- Generalised the WriteLogDMD() method, using WriteMODS() now.
 * 
 *        08.06.2009 --- Funk --- Added sorting the metadata and persons according to prefs when storing internally --> Put into
 *        DocStruct.sortMetadataRecursively!
 * 
 *        03.06.2009 --- Funk --- Added setContentIDs setter. --- Added SUB-internal PURL handling. --- Added SUB-internal METS Reference
 *        "PPN"-dimisher.
 * 
 *        02.06.2009 --- Funk --- CHECK if the whitespaces in the XML tags can be avoided anyhow, or do we need them? TESTIT! It can: just avoid
 *        storing the XML in pretty-print OUTSIDE of UGH :-) --- CHECK why the label values have got so many special chars in it after reading! See
 *        above!
 * 
 *        29.05.2009 --- Funk --- Now metadata of the physical DocStructs are written in goobi:goobi, too. Persons are not implemented yet! --- If no
 *        files are existing in the fileSec:filegroup LOCAL, the fileset will be set from the "pathimagefiles" metadata!
 * 
 *        28.05.2009 --- Funk --- Added digiprovReferenceAnchor and digiprovPresentationAnchor.
 * 
 *        28.04.2009 --- Funk --- changed "dv:digiprov" to "dv:links" in the AMD sec.
 * 
 *        27.04.2009 --- Funk --- Re-Engaged the removal of the internal Sun JRE classes.
 * 
 *        24.04.2009 --- Funk --- Labels are not written for internal METS.
 * 
 *        22.04.2009 --- Funk --- Fixed a NullPointerException at reading the physSequence and the pages without if no files given. --- Changed
 *        writePhysDivs and writeLogDivs, for internal writing the METS DocStructTypes must not be mapped.
 * 
 *        06.04.2009 --- Funk --- Fixed problems with the empty smLink element, fixed reading of persons. Now the METS internal storing is working
 *        just fine.
 * 
 *        03.04.2009 --- Mahnke --- Got rid of internal Sun JRE classes.
 * 
 *        03.04.2009 --- Funk --- Finished METS writing. --- added some things that valid METS is written without smLinks. --- refactored some oooold
 *        for loop constructs.
 * 
 *        30.03.2009 --- Funk --- Change some DEBUG log messages to TRACE. --- Separated internal storing from exporting.
 * 
 *        27.03.2009 --- Funk --- Added some null pointer checks.
 * 
 *        24.03.2009 --- Funk --- Namespace SchemaLocations now are set for METS and MODS, if not contained in prefs.
 * 
 *        23.03.2009 --- Funk --- Finished putting all unmapped metadata to mods:extension.goobi:metadata and mods:extension:goobi:person.
 * 
 *        19.03.2009 --- Funk --- Exceptions thrown by public classes now are logged from the public classes only --- improved METS reading ---
 *        organised class documentation structure --- All "ruleset"s changed to "prefs".
 * 
 *        18.03.2009 --- Funk --- Added metsPtrAnchorUrl, fixed FileGroupID bug.
 * 
 *        13.03.2009 --- Funk --- Tested METS reading with Monographs and MultivolumeWorks. It works! --- More stringifying done.
 * 
 *        12.03.2009 --- Funk --- Nearly completed METS import --- "stringyfied" some strings.
 * 
 *        09.03.2009 --- Funk --- Persons are checked for existing type and not for value at METS export now.
 * 
 *        24.02.2009 --- Funk --- Added/Swiched on/Improved internalName 2 MetyType mapping in METS formats section.
 * 
 *        16.02.2009 --- Funk --- Empty metadata fields are NOT taken as empty tags anymore.
 * 
 *        13.02.2009 --- Funk --- ADMID is set for Monographs again, too --- DisplayName is generated from first and last name, if existing.
 * 
 *        11.02.2009 --- Funk --- MODS tags are ordered by their appearance in the prefs' METS metadata section now --- Multiple usage of the same
 *        metadataType is possible now, too.
 * 
 *        23.12.2008 --- Funk --- Commented out all the special GDZ things --- Merry Christmas!
 * 
 *        22.12.2008 --- Funk --- Error is logged, if a MODS mapping is missing for an existing metadata from the prefs.
 * 
 *        11.12.2008 --- Funk --- Added some changes for the filegroups. A LOCAL filegroup is ALWAYS created now.
 * 
 *        09.12.2008 --- Funk --- Added MPTRs.
 * 
 *        19.11.2008 --- Funk --- Added VirtualFileGroup support.
 * 
 *        18.11.2008 --- Funk --- Added digiprovMD section.
 * 
 *        21.10.2008 --- Funk --- IDs starting with "0" now instead of "1".
 * 
 *        14.10.2008 --- Funk-- - Moved the Java object storing methods into the DigitalDocument class.
 * 
 *        07.10.2008 --- Funk --- Added Java Object storing and reading.
 * 
 *        29.09.2008 --- Funk --- Logging added.
 * 
 *        26.09.2008 --- Funk --- Prefixes and default namespace can be configured in the regelsatz now --- File group data (pathes, etc.) can be
 *        configured via setters now.
 * 
 *        19.08.2008 --- Funk --- Merging of METSMODS and METSMODSGDZ
 * 
 *        30.08.2008 --- Funk --- Changed class name from ZvddMets to MetsMods, to stay compatible to the existing Goobi calls --- Added ADM and
 *        several file groups.
 * 
 *        29.07.2008 --- Funk --- Added some methods from MetsModsGdz and adapted them to the ZVDD METS profile.
 * 
 *        09.05.2008 --- Funk --- First version.
 * 
 *        OLD CHANGELOG METSMODSGDZ
 * 
 *        08.08.2008 --- Funk --- Changed class name from MetsMods to MetsModsGdz.
 * 
 *        04.08.2008 --- Funk --- Changed some namespace issues.
 * 
 *        29.07.2008 --- Funk --- Changed some visibilities to "protected" for objects to be used from MetsMods class.
 * 
 *        13.05.2008 --- Funk --- Added default constructor.
 * 
 *        29.04.2008 --- Funk --- Tried to change the whitespace handling at the XML factory level (getMDValueOfNode()), but my solution is NOT
 *        working here. Please have a look at the class RDFFile.java --- Added security checks for given person metadata in the RDF:GDZ MODS, but
 *        missing xpath queries in the Regelsatz file concerning that metadata.
 * 
 *        25.04.2008 --- Funk ---Trimming added to avoid empty xquery values caused by newlines.
 * 
 ******************************************************************************/
@Log4j2
public class MetsMods implements ugh.dl.Fileformat {

    /***************************************************************************
     * VERSION STRING
     **************************************************************************/

    private static String version = "1.9-20100505";

    /***************************************************************************
     * STATIC FINALS
     **************************************************************************/

    // The line.
    protected static final String LINE = "--------------------" + "--------------------" + "--------------------" + "--------------------";

    // Default namespace things.
    public static final String DEFAULT_METS_PREFIX = "mets";
    public static final String DEFAULT_METS_URI = "http://www.loc.gov/METS/";
    private static final String DEFAULT_METS_SCHEMA_LOCATION = "http://www.loc.gov/standards/mets/mets.xsd";
    private static final String DEFAULT_MODS_PREFIX = "mods";
    private static final String DEFAULT_MODS_URI = "http://www.loc.gov/mods/v3";
    private static final String DEFAULT_SCHEMA_LOCATION = "http://www.loc.gov/standards/mods/v3/mods-3-7.xsd";
    private static final String DEFAULT_GOOBI_PREFIX = "goobi";
    private static final String DEFAULT_GOOBI_URI = "http://meta.goobi.org/v1.5.1/";
    private static final String DEFAULT_GOOBI_SCHEMA_LOCATION = "";
    private static final String DEFAULT_DV_PREFIX = "dv";
    private static final String DEFAULT_DV_URI = "http://dfg-viewer.de/";
    private static final String DEFAULT_DV_SCHEMA_LOCATION = "";
    private static final String DEFAULT_XLINK_PREFIX = "xlink";
    private static final String DEFAULT_XLINK_URI = "http://www.w3.org/1999/xlink";
    private static final String DEFAULT_XLINK_SCHEMA_LOCATION = "";
    private static final String DEFAULT_XSI_PREFIX = "xsi";
    private static final String DEFAULT_XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String DEFAULT_XSI_SCHEMA_LOCATION = "";

    private static final String DEFAULT_MIX_PREFIX = "mix";
    private static final String DEFAULT_MIX_URI = "http://www.loc.gov/standards/mix/";
    private static final String DEFAULT_MIX_SCHEMA_LOCATION = "http://www.loc.gov/standards/mix/mix.xsd";

    private static final String DEFAULT_PREMIS_PREFIX = "premis";
    private static final String DEFAULT_PREMIS_URI = "http://www.loc.gov/standards/premis/";
    private static final String DEFAULT_PREMIS_SCHEMA_LOCATION = "http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd";

    // Validation and anchor finals.
    protected static final boolean DO_VALIDATE = true;
    protected static final boolean DO_NOT_VALIDATE = false;
    protected static final boolean IS_ANCHOR = true;
    protected static final boolean IS_NOT_ANCHOR = false;

    // Type names for METS generation (from the prefs).
    protected static final String METS_PREFS_NODE_NAME_STRING = "METS";
    protected static final String METS_PREFS_INTERNALNAME_STRING = "InternalName";
    protected static final String METS_PREFS_METSTYPE_STRING = "MetsType";
    protected static final String METS_PREFS_WRITEXPATH_SEPARATOR_STRING = "#";

    // Store persons in mods:extension.goobi:person.
    protected static final String GOOBI_PERSON_LASTNAME_STRING = "lastName";
    protected static final String GOOBI_PERSON_FIRSTNAME_STRING = "firstName";
    protected static final String GOOBI_PERSON_IDENTIFIER_STRING = "identifier";
    protected static final String GOOBI_PERSON_IDENTIFIERTYPE_STRING = "identifierType";
    protected static final String GOOBI_PERSON_AFFILIATION_STRING = "affiliation";
    protected static final String GOOBI_PERSON_AUTHORITYID_STRING = "authorityID";
    protected static final String GOOBI_PERSON_AUTHORITYURI_STRING = "authorityURI";
    protected static final String GOOBI_PERSON_AUTHORITYVALUE_STRING = "authorityValue";
    protected static final String GOOBI_PERSON_DISPLAYNAME_STRING = "displayName";
    protected static final String GOOBI_PERSON_PERSONTYPE_STRING = "personType";

    protected static final String GOOBI_PERSON_DATEVALUE_STRING = "date";
    protected static final String GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING = "termsOfAddress";

    protected static final String GOOBI_PERSON_NAMEIDENTIFIER_STRING = "nameIdentifier";

    protected static final String GOOBI_CORPORATE_MAINNAME_STRING = "mainName";
    protected static final String GOOBI_CORPORATE_SUBNAME_STRING = "subName";
    protected static final String GOOBI_CORPORATE_PARTNAME_STRING = "partName";

    // The Goobi internal metadata XPath.
    protected static final String GOOBI_INTERNAL_METADATA_XPATH = "/mods:mods/mods:extension/goobi:goobi/goobi:metadata"; //NOSONAR: false positive, this is not an URI

    // This is the metadata the StructMap LOGICAL labels are creared from.
    protected static final String METS_PREFS_LABEL_METADATA_STRING = "TitleDocMain";

    // Some METS string finals.
    protected static final String METS_METS_STRING = "mets";
    protected static final String METS_STRUCTMAP_TYPE_LOGICAL_STRING = "LOGICAL";
    protected static final String METS_STRUCTMAP_TYPE_PHYSICAL_STRING = "PHYSICAL";
    protected static final String METS_MPTR_URL_STRING = "mptrUrl";
    protected static final String METS_MPTR_URL_ANCHOR_STRING = "mptrUrlAnchor";
    protected static final String METS_FILESEC_STRING = "fileSec";
    protected static final String METS_STRUCTMAP_STRING = "structMap";
    protected static final String METS_STRUCTMAPTYPE_STRING = "TYPE";
    protected static final String METS_FILEGROUP_LOCAL_STRING = "LOCAL";
    protected static final String METS_AMDSEC_STRING = "amdSec";
    protected static final String METS_DMDSEC_STRING = "dmdSec";
    protected static final String METS_RIGHTSMD_STRING = "rightsMD";
    protected static final String METS_MDWRAP_STRING = "mdWrap";
    protected static final String METS_MIMETYPE_STRING = "MIMETYPE";
    protected static final String METS_MDTYPE_STRING = "MDTYPE";
    protected static final String METS_OTHERMDTYPE_STRING = "OTHERMDTYPE";
    protected static final String METS_ID_STRING = "ID";
    protected static final String METS_XMLDATA_STRING = "xmlData";
    protected static final String METS_FILEGRP_STRING = "fileGrp";
    protected static final String METS_FILEGROUPUSE_STRING = "USE";
    protected static final String METS_LOCTYPE_STRING = "LOCTYPE";
    protected static final String METS_DIV_STRING = "div";
    protected static final String METS_FPTR_STRING = "fptr";
    protected static final String METS_MPTR_STRING = "mptr";
    protected static final String METS_SMLINK_STRING = "smLink";
    protected static final String METS_STRUCTLINK_STRING = "structLink";
    protected static final String METS_DIVTYPE_STRING = "TYPE";
    protected static final String METS_CONTENTIDS_STRING = "CONTENTIDS";
    protected static final String METS_FILEID_STRING = "FILEID";
    protected static final String METS_LABEL_STRING = "LABEL";
    protected static final String METS_DMDID_STRING = "DMDID";
    protected static final String METS_ADMID_STRING = "ADMID";
    protected static final String METS_ORDER_STRING = "ORDER";
    protected static final String METS_ORDERLABEL_STRING = "ORDERLABEL";
    protected static final String METS_HREF_STRING = "href";
    protected static final String METS_TO_STRING = "to";
    protected static final String METS_FROM_STRING = "from";
    protected static final String METS_XMLNS_STRING = "xmlns";
    protected static final String METS_SCHEMALOCATION_STRING = "schemaLocation";
    protected static final String METS_URN_NAME = "_urn";
    protected static final String RULESET_ORDER_NAME = "CurrentNoSorting";

    // Type names for preferences parsing.
    protected static final String PREFS_METADATA_STRING = "Metadata";
    protected static final String PREFS_GROUP_STRING = "Group";
    protected static final String PREFS_DOCSTRUCT_STRING = "DocStruct";
    protected static final String PREFS_NAMESPACEDEFINITION_STRING = "NamespaceDefinition";
    protected static final String PREFS_XPATHANCHORQUERY_STRING = "XPathAnchorQuery";
    protected static final String PREFS_ANCHORIDENTIFIERMETADATATYPE_STRING = "AnchorIdentifierMetadataType";
    protected static final String PREFS_ANCHORIDENTIFIERVALUEREGEXP_STRING = "ValueRegExp";
    protected static final String PREFS_ANCHORIDENTIFIERVALUEREPLACEMENT_STRING = "ValueReplacement";
    protected static final String PREFS_NAMESPACE_URI_STRING = "URI";
    protected static final String PREFS_NAMESPACE_PREFIX_STRING = "prefix";
    protected static final String PREFS_NAMESPACE_SCHEMALOCATION = "schemaLocation";

    // Type names for metadata handling.
    protected static final String METADATA_LOGICAL_PAGE_NUMBER = "logicalPageNumber";
    protected static final String METADATA_PHYSICAL_PAGE_NUMBER = "physPageNumber";
    protected static final String METADATA_PAGE_UNCOUNTED_VALUE = "uncounted";
    protected static final String METADATA_PHYSICAL_BOUNDBOOK_STRING = "BoundBook";
    protected static final String METADATA_PHYSICAL_PAGE_STRING1 = "page";

    // Reference type name for logical <> physical references.
    protected static final String LOGICAL_PHYSICAL_MAPPING_TYPE_STRING = "logical_physical";

    // Some XPath processor finals.
    public static final short ELEMENT_NODE = 1;
    public static final short ATTRIBUTE_NODE = 2;
    public static final short TEXT_NODE = 3;

    // Some general pre- and suffixes.
    protected static final String DECIMAL_FORMAT = "0000";
    protected static final String AMD_PREFIX = "AMD";
    protected static final String TECHMD_PREFIX = "techMD";
    protected static final String FILE_PREFIX = "FILE_";
    protected static final String LOG_PREFIX = "LOG_";
    protected static final String DMDLOG_PREFIX = "DMDLOG_";
    protected static final String PHYS_PREFIX = "PHYS_";
    protected static final String DMDPHYS_PREFIX = "DMDPHYS_";
    protected static final String ANCHOR_XML_FILE_SUFFIX_STRING = "_anchor";
    private boolean writeLocalFilegroup = true;

    /***************************************************************************
     * INSTANCE VARIABLES
     **************************************************************************/

    // Contains key/value pairs of namespace prefix/namespace and namespace
    // prefix/namespaceDeclaration.
    protected Map<String, Namespace> namespaces = new HashMap<>();
    protected Map<String, String> namespaceDeclarations = new HashMap<>();

    // SortedMap for mapping file IDs to content files (default sorting order is
    // the key (String).
    protected Map<String, ContentFile> sortedFileMap = new TreeMap<>();

    // Set mptr things.
    protected String mptrUrl = "";
    protected String mptrUrlAnchor = "";
    protected String goobiID = "";

    protected Prefs myPreferences;

    protected DigitalDocument digdoc = null;
    protected int dmdidMax = 0;
    protected int dmdidPhysMax = 0;
    protected int amdidMax = 0;
    protected int divlogidMax = 0;
    protected int techidMax = 0;
    protected int divphysidMax = 0;
    protected int fileidMax = 0;

    // Contains MetadataMatchingObjects for mapping MODS to internal
    // MetadataType elements and vice versa.
    protected List<MatchingMetadataObject> modsNamesMD = new LinkedList<>();
    protected List<MatchingDocStructObject> modsNamesDS = new LinkedList<>();

    protected Element metsNode = null;
    protected Node firstDivNode = null;

    // A METS Helper.
    private Helper metsHelper;

    // List to store all identifiers of the anchor (Metadata objects are
    // contained in here).
    @SuppressWarnings("unused")
    private List<Metadata> anchorIdentifiers = null;
    // Contains the xpath to reference the anchor.
    protected String xPathAnchorReference = null;
    // Contains the valueRegExp for the reference the anchor.
    protected String valueRegExpAnchorReference = null;
    protected String valueReplacementAnchorReference = null;

    // Default namespace URIs for some namespaces and namespace declarations.
    protected String metsNamespacePrefix;
    protected String modsNamespacePrefix;
    protected String goobiNamespacePrefix;
    protected String mixNamespacePrefix;
    protected String premisNamespacePrefix;
    protected String dvNamespacePrefix;
    protected String xsiNamespacePrefix;
    protected String xlinkNamespacePrefix;
    protected String xmlNamespacePrefix;

    // Stores the xpath expression to extract the identifier of the anchor.
    protected String xpathForLinkToAnchor = null;
    protected String anchorIdentifierMetadataType = null;

    // A hash to store some tag grouping things.
    // This is a really dirty hack, I will fix it tomorrow! (hihi)
    protected HashMap<String, String> replaceGroupTags = new HashMap<>();

    // use UUID or incremental numbers as file identifier
    protected boolean createUUIDs = false;

    private String softwareName;
    private String softwareVersion;
    private String instanceName;
    private String clientName;

    /***************************************************************************
     * CONSTRUCTORS
     **************************************************************************/
    public MetsMods() {
    }

    /***************************************************************************
     * @param inPrefs
     * @throws PreferencesException
     **************************************************************************/
    public MetsMods(Prefs inPrefs) throws PreferencesException {

        setNamespaces();
        this.myPreferences = inPrefs;

        log.info(this.getClass().getName() + " " + getVersion());

        // Read preferences.
        Node prefsMetsNode = inPrefs.getPreferenceNode(METS_PREFS_NODE_NAME_STRING);
        if (prefsMetsNode == null) {
            String message = "Can't read preferences for METS fileformat!";
            PreferencesException pe = new PreferencesException("Node '" + METS_PREFS_NODE_NAME_STRING + "' in preferences file not found!");
            log.error(message, pe);
            throw pe;
        }

        readPrefs(prefsMetsNode);
    }

    /***************************************************************************
     * WHAT THE OBJECT DOES
     **************************************************************************/

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#GetDigitalDocument()
     */
    @Override
    public DigitalDocument getDigitalDocument() {
        return this.digdoc;
    }

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
     * @see ugh.dl.Fileformat#read(java.lang.String)
     */
    @Override
    public boolean read(String theFilename) throws ReadException {

        log.info("Reading METS file...");

        MetsDocument mets = null;
        Mets metsElement = null;

        File f = new File(theFilename);
        try {
            XmlOptions opts = new XmlOptions();
            opts.setLoadStripWhitespace();
            mets = MetsDocument.Factory.parse(f, opts);
        } catch (XmlException e) {
            String message = "Error parsing METS file '" + f.getAbsolutePath() + "'!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (IOException e) {
            String message = "Error accessing METS file '" + f.getAbsolutePath() + "'!";
            log.error(message, e);
            throw new ReadException(message, e);
        }

        metsElement = mets.getMets();
        this.metsHelper = new Helper(metsElement);

        // No digital document available yet, create one.
        if (this.getDigitalDocument() == null) {
            log.info("No DigitalDocument existing yet, creating new one");
            this.setDigitalDocument(new DigitalDocument());
        }

        // readAmdSec now to provide references for fileSec and LogDocStruct
        readAmdSec(metsElement);

        // Get FileSec to read all files.
        readFileSec(metsElement);

        // Get PhysicalStructMap and create the appropriate links to the file.
        readPhysDocStruct();

        // Get logical StructMap.
        try {
            readLogDocStruct(theFilename);
        } catch (ClassNotFoundException e) {
            String message = "Class could not be found!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (InstantiationException e) {
            String message = "Class could not be instanciated!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (IllegalAccessException e) {
            String message = "Class was illegal accessed!";
            log.error(message, e);
            throw new ReadException(message, e);
        } catch (XPathExpressionException e) {
            String message = "Wrong XPath expression!";
            log.error(message, e);
            throw new ReadException(message, e);
        }

        // Map logical and physical Document Structures.
        mapLogAndPhysDocStruct(metsElement);

        this.digdoc.sortMetadataRecursively(this.myPreferences);

        return true;
    }

    private void readAmdSec(Mets metsElement) {
        AmdSecType[] list = metsElement.getAmdSecArray();
        if (list != null && list.length > 0) {
            AmdSecType ast = list[0]; // allow only one amdSec
            this.digdoc.setAmdSec(ast.getID());

            MdSecType[] mst = ast.getTechMDArray();
            if (mst != null) {
                for (MdSecType tech : mst) {
                    Node premis = tech.getDomNode();
                    Md techMd = new Md(premis, MdType.TECH_MD);
                    techMd.setId(tech.getID());
                    this.digdoc.addTechMd(techMd);
                }
            }
            mst = ast.getRightsMDArray();
            if (mst != null) {
                for (MdSecType tech : mst) {
                    Node premis = tech.getDomNode();
                    Md techMd = new Md(premis, MdType.RIGHTS_MD);
                    techMd.setId(tech.getID());
                    this.digdoc.addTechMd(techMd);
                }
            }
            mst = ast.getDigiprovMDArray();
            if (mst != null) {
                for (MdSecType tech : mst) {
                    Node premis = tech.getDomNode();
                    Md techMd = new Md(premis, MdType.DIGIPROV_MD);
                    techMd.setId(tech.getID());
                    this.digdoc.addTechMd(techMd);
                }
            }
            mst = ast.getSourceMDArray();
            if (mst != null) {
                for (MdSecType tech : mst) {
                    Node premis = tech.getDomNode();
                    Md techMd = new Md(premis, MdType.SOURCE_MD);
                    techMd.setId(tech.getID());
                    this.digdoc.addTechMd(techMd);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsModsGdz#write(java.lang.String)
     */
    @Override
    public boolean write(String filename) throws WriteException, PreferencesException {

        log.info("Writing METS ....");

        // Digital Document for the anchor.
        DigitalDocument anchorDocument = null;
        DigitalDocument topDocument = null;
        DigitalDocument myDigDoc = this.digdoc;

        // Get the uppermost logical DocStruct and check if it's an
        // anchor.
        if (this.getDigitalDocument() == null) {
            String message = "Can't obtain DigitalDocument! Maybe wrong preferences file?";
            log.error(message);
            throw new PreferencesException(message);
        }
        DocStruct uppermostStruct = this.getDigitalDocument().getLogicalDocStruct();
        DocStructType uppermostType = uppermostStruct.getType();

        if (uppermostType.isAnchor()) {
            // The uppermost structure is an anchor; so we need to split the
            // document and create two different METS/MODS files: One for
            // the anchor and one for the rest.
            anchorDocument = new DigitalDocument();

            // Copy metadata, but not the children.
            DocStruct newStruct = uppermostStruct.copy(true, false);

            // Copy here the children from the next level only for MPTRs
            // from zvdd-dfgviewer-v2 without metadata.
            if (uppermostStruct.getAllChildren() == null) {
                String message = "Top DocStruct '" + uppermostType.getName() + "' is anchor struct, bus has no children!";
                log.error(message);
                throw new PreferencesException(message);
            }

            for (DocStruct d : uppermostStruct.getAllChildren()) {
                DocStruct c = null;
                try {
                    c = d.copy(true, false);
                    // Add the child to the new DocStruct.
                    newStruct.addChild(c);
                } catch (TypeNotAllowedAsChildException e) {
                    String message = "DocStruct '" + c.getType().getName() + "' is not allowed as child of DocStruct '" + d.getType().getName() + "'";
                    log.error(message, e);
                    throw new PreferencesException(message, e);
                }
            }

            // New struct is top document.
            anchorDocument.setLogicalDocStruct(newStruct);

            // Get child of top document; there should only be a single child.
            List<DocStruct> children = uppermostStruct.getAllChildren();

            if (children != null && children.size() > 1) {
                // Error; there must only be a single top-document under the
                // anchor.
                this.digdoc = myDigDoc;
                throw new WriteException("More than one structure entity available; only one expected as child of an anchor!");
            }

            // There is a child, so delete only the anchor's metadata from
            // the Digital Document.
            if (children != null) {

                topDocument = this.digdoc;

            }
        } else {
            // Simply write the normal DigitalDocument.
            topDocument = this.digdoc;
        }

        boolean success = true;
        if (anchorDocument != null) {
            // First write the anchor.
            this.digdoc = anchorDocument;
            String anchorfilename = buildAnchorFilename(filename);

            log.info("Writing anchor file '" + anchorfilename + "' from DocStruct '" + this.digdoc.getLogicalDocStruct().getType().getName() + "'");

            success = writeMetsMods(anchorfilename, DO_NOT_VALIDATE, IS_ANCHOR);

            log.info("Anchor file written");
        }

        if (topDocument != null) {
            this.digdoc = topDocument;

            log.info("Writing regular file '" + filename + "' from DocStruct '" + this.digdoc.getLogicalDocStruct().getType().getName() + "'");

            success = writeMetsMods(filename, DO_NOT_VALIDATE, IS_NOT_ANCHOR);
        }

        this.digdoc = myDigDoc;

        log.info("Writing METS complete");

        return success;
    }

    /***************************************************************************
     * <p>
     * All methods to read METS file specific preferences are read here.
     * </p>
     * 
     * @param inNode
     * @return true, if preferences were read succesfully, false otherwise.
     **************************************************************************/
    public void readPrefs(Node inNode) throws PreferencesException {

        String nn = inNode.getNodeName();

        if (inNode.getNodeType() == ELEMENT_NODE && METS_PREFS_NODE_NAME_STRING.equals(nn)) {

            // Read information about a single metadata matching.
            NodeList childnodes = inNode.getChildNodes();

            for (int x = 0; x < childnodes.getLength(); x++) {
                Node childnode = childnodes.item(x);

                if (childnode.getNodeType() == ELEMENT_NODE) {
                    // Read Metadata prefs from deferred method.
                    if (PREFS_METADATA_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        try {
                            readMetadataPrefs(childnode);
                        } catch (PreferencesException pe) {
                            String message = "Could not parse the prefs' metadata section!";
                            log.error(message, pe);
                            throw pe;
                        }
                    }
                    // Read Group prefs from deferred method.
                    if (PREFS_GROUP_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        try {
                            readMetadataGroupPrefs(childnode);
                        } catch (PreferencesException pe) {
                            String message = "Could not parse the prefs' metadata section!";
                            log.error(message, pe);
                            throw pe;
                        }
                    }

                    // Read DocStruct prefs from deferred method.
                    if (PREFS_DOCSTRUCT_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        try {
                            readDocStructPrefs(childnode);
                        } catch (PreferencesException pe) {
                            String message = "Could not parse the prefs' DocStruct section!";
                            log.error(message, pe);
                            throw pe;
                        }
                    }
                    // Read namespace information
                    if (PREFS_NAMESPACEDEFINITION_STRING.equalsIgnoreCase(childnode.getNodeName()) && !readNamespacePrefs(childnode)) {
                        String message =
                                "Can't read prefs for METS module: Namespace declaration not complete; the namespace URI and its prefix must be declared!";
                        log.error(message);
                        throw new PreferencesException(message);

                    }
                    // Read some anchor identifier information.
                    if (PREFS_ANCHORIDENTIFIERMETADATATYPE_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        String anchorIdentifierTypeName = getTextNodeValue(childnode).trim();
                        if (anchorIdentifierTypeName == null) {
                            String message = "<" + PREFS_ANCHORIDENTIFIERMETADATATYPE_STRING + "> is existing in " + METS_PREFS_NODE_NAME_STRING
                                    + " mapping, but has no value!";
                            log.error(message);
                            throw new PreferencesException(message);
                        }
                        this.anchorIdentifierMetadataType = anchorIdentifierTypeName;
                    }
                    // Read XPath information.
                    if (PREFS_XPATHANCHORQUERY_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        this.xPathAnchorReference = getTextNodeValue(childnode).trim();
                    }
                    // Read ValueRegExp information.
                    if (PREFS_ANCHORIDENTIFIERVALUEREGEXP_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        this.valueRegExpAnchorReference = getTextNodeValue(childnode).trim();
                    }
                    if (PREFS_ANCHORIDENTIFIERVALUEREPLACEMENT_STRING.equalsIgnoreCase(childnode.getNodeName())) {
                        this.valueReplacementAnchorReference = getTextNodeValue(childnode).trim();
                    }

                }

                // Check some values, e.g. if metadata types are available etc.
                if (this.anchorIdentifierMetadataType != null) {
                    PrefsType identifierType = this.myPreferences.getMetadataTypeByName(this.anchorIdentifierMetadataType);
                    if (identifierType == null) {
                        String message = "MetadataType for anchor (identifier) not found: " + this.anchorIdentifierMetadataType;
                        log.error(message);
                        throw new PreferencesException(message);
                    }
                }
            }
        }

        // Log namespaces.
        for (Entry<String, Namespace> e : this.namespaces.entrySet()) {
            log.debug("Namespace prefix: " + e.getKey() + ", URI: " + e.getValue().getUri());
        }
    }

    /***************************************************************************
     * PRIVATE (AND PROTECTED) METHODS
     **************************************************************************/

    /***************************************************************************
     * <p>
     * Gets a DocStruct by div ID.
     * </p>
     * 
     * @param id
     * @param inStruct
     * @return
     **************************************************************************/
    private DocStruct getDocStructByDivID(String id, DocStruct inStruct) {

        if (inStruct == null) {
            return null;
        }

        // Get the related METS div object.
        Object o = inStruct.getOrigObject();
        if (o != null) {
            if (o instanceof DivType div) {
                if (div.getID() != null && div.getID().equals(id)) {
                    return inStruct;

                }
            } else if (o instanceof AreaType area) {
                // Convert object to area.
                if (area.getID() != null && area.getID().equals(id)) {
                    return inStruct;

                }
            }
        }

        // Iterate over all children.
        List<DocStruct> children = inStruct.getAllChildren();
        if (children != null) {
            for (DocStruct child : children) {
                DocStruct foundStruct = getDocStructByDivID(id, child);
                if (foundStruct != null) {
                    return foundStruct;
                }
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Maps logical and physical DocStructs.
     * </p>
     * 
     * @param inMetsElement
     * @throws ReadException
     **************************************************************************/
    private void mapLogAndPhysDocStruct(Mets inMetsElement) throws ReadException {

        log.info("Mapping Physical and Logical DocStruct...");

        // Get the only StructLink section and iterate over all smLink elements.
        StructLink sl = inMetsElement.getStructLink();

        // If no structLink element available (could happen, if file is an
        // anchor) OR if we have only one smLink element and to and from
        // attribute are empty, just return (We do that at METS writing to
        // always get a valid METS file).

        if (sl == null || sl.getSmLinkArray() == null || sl.getSmLinkArray().length == 0) {
            return;
        }
        SmLink link = sl.getSmLinkArray()[0];
        boolean getFromNotExisting = link.getFrom() == null || "".equals(link.getFrom());
        boolean getToNotExisting = link.getTo() == null || "".equals(link.getTo());
        if (sl.getSmLinkArray().length == 1 && getFromNotExisting && getToNotExisting) {
            return;
        }

        // Iterate over all smLinks.
        for (SmLink singleLink : sl.getSmLinkArray()) {
            String linkFrom = singleLink.getFrom();
            String linkTo = singleLink.getTo();

            // Throw exception if smLink elements are incomplete.
            if ((linkFrom == null) || (linkTo == null)) {
                String message = "smLink section contains incomplete smLink elements, 'to' or 'from' attribute is missing!";
                log.error(message);
                throw new ReadException(message);
            }

            log.trace("Processing smLink FROM = " + linkFrom + " and TO = " + linkTo);

            // Get the StructMap type name from the div ID.
            DivType linkFromDivType = this.metsHelper.getStructMapDiv(linkFrom);

            // If the type name is not existing, throw an exception.
            if (linkFromDivType == null) {
                String message = "No logical DocStruct available with div ID = '" + linkFrom + "'!";
                log.error(message);
                throw new ReadException(message);
            }

            // Only if the given DocStruct type is NOT an anchor, set references
            // from the current smLink.
            if (!this.myPreferences.getDocStrctTypeByName(linkFromDivType.getTYPE()).isAnchor()) {

                // Get the appropriate logical DocStruct 'from' reference.
                DocStruct foundLogicalStruct = getDocStructByDivID(linkFrom, this.digdoc.getLogicalDocStruct());
                if (foundLogicalStruct == null) {
                    String message = "Linked div in logical structMap with ID '" + linkFrom + "' not available";
                    log.error(message);
                    throw new ReadException(message);
                }

                // Get the appropriate physical DocStruct 'to' reference.
                DocStruct foundPhysicalStruct = getDocStructByDivID(linkTo, this.digdoc.getPhysicalDocStruct());
                if (foundPhysicalStruct == null) {
                    String message = "Linked div in physical structMap with ID '" + linkTo + "' not available";
                    log.error(message);
                    throw new ReadException(message);
                }

                // Create relationship between logical and physical DocStruct.
                foundLogicalStruct.addReferenceTo(foundPhysicalStruct, LOGICAL_PHYSICAL_MAPPING_TYPE_STRING);

                log.trace("Added reference: " + foundLogicalStruct.getType().getName() + " (" + linkFrom + ") > "
                        + foundPhysicalStruct.getType().getName() + " (" + linkTo + ")");

            }
        }
    }

    /***************************************************************************
     * <p>
     * Adds something to a list.
     * </p>
     * 
     * @param result
     * @param inStruct
     **************************************************************************/
    private void addToList(LinkedList<DocStruct> result, DocStruct inStruct) {
        DivType currentDiv = (DivType) inStruct.getOrigObject();
        BigInteger currentOrder = currentDiv.getORDER();

        if (currentOrder == null) {
            // Add it as the last one.
            result.addLast(inStruct);
            return;
        }

        // Iterate over all DocStruct elements in the list.
        int position = 0;
        for (DocStruct ds : result) {
            DivType div = (DivType) ds.getOrigObject();
            BigInteger order = div.getORDER();
            if (order == null) {
                // Next from list as this one has no order label.
                continue;
            }

            // Order is bigger than currentOrder add it at the current position.
            if (order.compareTo(currentOrder) > 0) {
                // Get out of loop.
                break;
            }
            position++;
        }

        // Position contains the position where to add shift all entry to the
        // back (to the right).
        result.add(position, inStruct);
    }

    /***************************************************************************
     * <p>
     * Read all sub <div> elements of the current one.
     * </p>
     * 
     * @param inDiv
     * @return LinkedList containing DocStruct instances
     * @throws ReadException
     **************************************************************************/
    private LinkedList<DocStruct> readDivChildren(DivType inDiv) throws ReadException {

        MetadataType logpagetype = this.myPreferences.getMetadataTypeByName(METADATA_LOGICAL_PAGE_NUMBER);
        MetadataType physpagetype = this.myPreferences.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER);

        // List containing DocStruct objects of the children.
        LinkedList<DocStruct> result = new LinkedList<>();

        // Get all sub <div> elements.
        DivType[] children = inDiv.getDivArray();

        // TODO check for <area>
        Fptr[] fptrArray = inDiv.getFptrArray();
        if (fptrArray != null) {

        }

        if (children == null | children.length == 0) {
            // No children available, so there is nothing to read.
            return null;
        }

        for (DivType dt : children) {
            String type = dt.getTYPE();
            String id = dt.getID();
            BigInteger order = dt.getORDER();
            String orderlabel = dt.getORDERLABEL();

            // Get the DocStructType from the prefs.
            DocStructType myType = this.myPreferences.getDocStrctTypeByName(type);

            // Can't find the appropriate type.
            if (myType == null) {
                String message = "No internal DocStructType with the name '" + type + "'";
                log.error(message);
                throw new ReadException(message);
            }

            // Create DocStruct.
            DocStruct newDocStruct = null;
            try {
                newDocStruct = this.getDigitalDocument().createDocStruct(myType);
            } catch (TypeNotAllowedForParentException e) {
                String message = "Can't create this DocStruct of type '" + type + "' at the current position in tree";
                log.error(message, e);
                throw new ReadException(message, e);
            }
            newDocStruct.setAdditionalValue(dt.getLabel());
            // get the corresponding amdSec
            @SuppressWarnings("rawtypes")
            List admList = dt.getADMID();
            if (admList != null) {
                for (Object object : admList) {
                    String admid = (String) object;
                    newDocStruct.setAdmId(admid);
                    AmdSec amdSec = digdoc.getAmdSec(admid);
                    if (amdSec != null) {
                        newDocStruct.setAmdSec(amdSec);
                    }
                }
            }

            // If order and orderlabel are stored here; than we should create
            // the appropriate metadata.
            try {
                if (order != null) {
                    Metadata md = new Metadata(physpagetype);
                    md.setValue(order.toString());
                }
            } catch (MetadataTypeNotAllowedException e) {
                String message = "Can't create metadata with expected type '" + METADATA_PHYSICAL_PAGE_NUMBER + "'! Type must not be null!";
                log.error(message, e);
                throw new ReadException(message, e);
            }

            try {
                if (orderlabel != null) {
                    if ("uncounted".equals(orderlabel)) {
                        orderlabel = " - ";
                    }
                    Metadata md = new Metadata(logpagetype);
                    md.setValue(orderlabel);

                }
            } catch (MetadataTypeNotAllowedException e) {
                String message = "Can't create metadata with expected type '" + METADATA_LOGICAL_PAGE_NUMBER + "'! Type must not be null!";
                log.error(message, e);
                throw new ReadException(message, e);
            }

            // Add the <div> element as the original object.
            newDocStruct.setOrigObject(dt);
            newDocStruct.setIdentifier(id);

            // Add the digdoc to the list according to its ORDER attribute
            // value.
            addToList(result, newDocStruct);
        }

        // No children available, return.
        if (result.isEmpty()) {
            return null;
        }

        // Iterate over all DocStructs and see, if the appropriate DivType
        // object has child objects.
        for (DocStruct ds : result) {
            // Get the original div.
            DivType div = (DivType) ds.getOrigObject();
            // No div element.
            if (div == null) {
                continue;
            }

            // Get all children.
            LinkedList<DocStruct> childlist = readDivChildren(div);

            // We got a list with all children (DocStruct objects), add children
            // from list to docstruct.
            if (childlist != null) {
                for (DocStruct child : childlist) {
                    try {
                        ds.addChild(child);
                    } catch (TypeNotAllowedAsChildException e) {
                        String message = "Child '" + child.getType().getName() + "' nod allowed for DocStructType '" + ds.getType().getName() + "'";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }
                }
            }
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Reads the logical doc struct.
     * </p>
     * 
     * @param inMetsElement
     * @param theFilename
     * @throws ReadException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws XPathExpressionException
     **************************************************************************/
    private void readLogDocStruct(String theFilename)
            throws ReadException, ClassNotFoundException, InstantiationException, IllegalAccessException, XPathExpressionException {

        log.info("Reading Logical DocStruct...");

        List<?> logmaplist = this.metsHelper.getStructMapByType(METS_STRUCTMAP_TYPE_LOGICAL_STRING);
        if (logmaplist.isEmpty()) {
            String message = "No <structMap> element of type LOGICAL available!";
            log.error(message);
            throw new ReadException(message);
        }
        if (logmaplist.size() > 1) {
            String message = "Too many <structMap> elements of type LOGICAL!";
            log.error(message);
            throw new ReadException(message);
        }

        log.info("Parsing the one and only StructMap logical");

        // Get topmost <div>.
        if (logmaplist.size() == 1) {
            // Get the first one.
            StructMapType logstructmap = (StructMapType) logmaplist.get(0);
            // There can only be a single topmost div.
            DivType topmostdiv = logstructmap.getDiv();

            // Create DocStruct instance for the topmost <div>
            //
            // (A) Create the DocStructType.
            String type = topmostdiv.getTYPE();
            String id = topmostdiv.getID();

            if (type == null) {
                String message = "No type attribute set for topmost <div> in physical structMap";
                log.error(message);
                throw new ReadException(message);
            }

            // Get the DocStructType from the prefs.
            DocStructType myType = this.myPreferences.getDocStrctTypeByName(type);
            // Can't find the appropriate DocStructType object.
            if (myType == null) {
                String message = "No internal DocStructType with name '" + type + "'";
                log.error(message);
                throw new ReadException(message);
            }

            // (B) Create DocStruct for the topmost <div>.
            DocStruct newDocStruct = null;
            try {
                newDocStruct = this.getDigitalDocument().createDocStruct(myType);
                newDocStruct.setIdentifier(id);
                newDocStruct.setOrigObject(topmostdiv);

                // get the corresponding amdSec
                @SuppressWarnings("rawtypes")
                List admList = topmostdiv.getADMID();
                if (admList != null) {
                    for (Object object : admList) {
                        String admid = (String) object;
                        newDocStruct.setAdmId(admid);
                        AmdSec amdSec = digdoc.getAmdSec(admid);
                        if (amdSec != null) {
                            newDocStruct.setAmdSec(amdSec);
                        }
                    }
                }

            } catch (TypeNotAllowedForParentException e) {
                String message = "Can't create this DocStruct of type '" + type + "' at the current position in tree (logical tree)";
                log.error(message, e);
                throw new ReadException(message, e);
            }

            log.info("DocStruct of type '" + type + "' created");

            // Handle children for the topmost <div>
            //
            // Parse the child divs and create appropriate DocStruct elements
            // for them. Do this also if the topStruct is an anchor, because we
            // included the anchor structs because of the <mptr> tag.
            //
            // Get all children.
            LinkedList<DocStruct> toplist = readDivChildren(topmostdiv);
            if (toplist != null) {
                for (DocStruct child : toplist) {
                    try {
                        newDocStruct.addChild(child);
                    } catch (TypeNotAllowedAsChildException e) {
                        String message = "Can't add DocStruct of type '" + child.getType().getName() + "', it is not allowed for parent DocStruct '"
                                + newDocStruct.getType().getName() + "'";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }
                }
            }

            log.info("Parsed and created all child DocStructs");

            // Set the topmost div's DocStruct object as the topmost physical
            // DocStruct.
            this.getDigitalDocument().setLogicalDocStruct(newDocStruct);

            // Get metadata for this digdoc (and all its child docs).
            parseMetadataForLogicalDocStruct(newDocStruct, true);

            // If the top DocStruct is an anchor, get its child's MODS section.
            if (newDocStruct.getType().isAnchor()) {
                String modsdata = getMODSSection(newDocStruct.getAllChildren().get(0));

                // If a MODS section is existing, look for anchor references. A
                // reference to an anchor is done via the <XPathAnchorQuery>
                // element from the prefs, get the DocStruct for the anchor from
                // another XML-file.
                if (modsdata != null) {
                    DocStruct newanchor = checkForAnchorReference(modsdata, theFilename);

                    // If an anchor reference is existing, take the newly
                    // created DocStruct and change it with the current
                    // TopStruct.
                    if (newanchor != null) {
                        // Check if the two DocStructs are from the same type!
                        if (!newanchor.getType().equals(newDocStruct.getType())) {
                            String message = "Top DocStructs from METS file '" + newanchor.getType().getName() + "' and METS anchor file '"
                                    + newDocStruct.getType().getName() + "' are not from the same type!";
                            log.error(message);
                            throw new ReadException(message);
                        }
                        try {
                            newDocStruct = newDocStruct.getAllChildren().get(0);
                            newanchor.addChild(newDocStruct);
                        } catch (TypeNotAllowedAsChildException e) {
                            String message = "Can't add anchor as parent of type '" + newanchor.getType().getName() + "' to the current DocStruct '"
                                    + newDocStruct.getType().getName() + "'";
                            log.error(message, e);
                            throw new ReadException(message, e);
                        }

                        this.getDigitalDocument().setLogicalDocStruct(newanchor);
                    }
                } else if (!theFilename.contains(ANCHOR_XML_FILE_SUFFIX_STRING)) {
                    String message = "DocStruct '" + newDocStruct.getType().getName()
                            + "' is an anchor DocStruct, but NO anchor identifier is existing for child DocStruct '"
                            + newDocStruct.getAllChildren().get(0).getType().getName() + "' in file '" + theFilename + "'!";
                    log.error(message);
                    throw new ReadException(message);
                }
            }
        } else {
            // No logical structMap available. Error - there must be at least a
            // single uppermost div containing basic bibliographic metadata e.g.
            // a persistent Identifier.
            String message = "There is no StructMap 'LOGICAL' in the METS file";
            log.error(message);
            throw new ReadException(message);
        }
    }

    /***************************************************************************
     * <p>
     * Builds the anchor filename.
     * </p>
     * 
     * @param xmlfileName
     * @return
     **************************************************************************/
    protected String buildAnchorFilename(String xmlFilename) {

        if (!xmlFilename.endsWith(".xml")) {
            xmlFilename += ".xml";
        }

        return xmlFilename.substring(0, xmlFilename.lastIndexOf('.')) + ANCHOR_XML_FILE_SUFFIX_STRING
                + xmlFilename.substring(xmlFilename.lastIndexOf('.'), xmlFilename.length());
    }

    /***************************************************************************
     * <p>
     * Checks, if there is a reference to another METS file using the <code>&lt;XPathAnchorQuery></code> element from the prefs.
     * </p>
     * 
     * 
     * @param inMods
     * @param filename
     * @return
     * @throws ReadException
     **************************************************************************/
    protected DocStruct checkForAnchorReference(String inMods, String filename) throws ReadException {

        ModsDocument modsDocument;
        DocStruct anchorDocStruct = null;
        String anchorFilename = "";
        String identifierOfAnchor = "";

        // Check for MODS validity.
        try {
            modsDocument = ModsDocument.Factory.parse(inMods);
        } catch (XmlException e) {
            String message = "MODS section doesn't seem to contain valid MODS";
            log.error(message, e);
            throw new ImportException("Doesn't seem to contain valid MODS", e);
        }

        this.xPathAnchorReference =
                this.namespaceDeclarations.get(this.modsNamespacePrefix) + this.namespaceDeclarations.get(this.goobiNamespacePrefix) + " $this/" + "."
                        + GOOBI_INTERNAL_METADATA_XPATH + "[@anchorId='true'][@name='" + this.anchorIdentifierMetadataType + "']";

        log.debug("XQuery path for anchor ID: " + this.xPathAnchorReference);

        XmlOptions xo = new XmlOptions();
        xo.setUseDefaultNamespace();
        XmlObject[] objects = modsDocument.selectPath(this.xPathAnchorReference, xo);

        // Iterate over all objects; objects can be available more than once.
        for (XmlObject xmlobject : objects) {
            // Get DOM Node.
            Node node = xmlobject.getDomNode();

            // Get child nodes to find the text node.
            NodeList nodelist = node.getChildNodes();

            // Read anchor from separate file, if existing.
            anchorFilename = buildAnchorFilename(filename);
            if (!new File(anchorFilename).exists()) {
                String message = "Anchor file '" + anchorFilename + "' expected due to existing anchor referenece, none found";
                log.error(message);
                throw new ReadException(message);
            }

            // Iterate over all the given results.
            for (int x = 0; x < nodelist.getLength(); x++) {
                Node subnode = nodelist.item(x);
                if (subnode.getNodeType() == TEXT_NODE) {
                    identifierOfAnchor = subnode.getNodeValue();

                    log.debug("Anchor's identifier: " + identifierOfAnchor + " (" + subnode.getNodeName() + ")");

                    // Found the reference to the anchor.
                    MetsMods anchorMets = null;
                    try {
                        anchorMets = new MetsMods(this.myPreferences);
                    } catch (PreferencesException e) {
                        String message = "Can't read Preferences for METS while reading the anchor file";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }

                    try {
                        anchorMets.read(anchorFilename);
                    } catch (ReadException e) {
                        String message = "Can't read anchor file, which must be in METS format as well";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }

                    // Get Digital Document and first logical DocStruct (which
                    // should be the only one).
                    DigitalDocument anchorDocument = anchorMets.getDigitalDocument();
                    DocStruct anchorStruct = anchorDocument.getLogicalDocStruct();
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
                }
            }

            // Check if anchor exists.
            if (anchorDocStruct == null) {
                String message = "Referenced identifier for anchor '" + identifierOfAnchor + "' not found in anchor struct '" + anchorFilename + "'";
                log.error(message);
                throw new ReadException(message);
            }
        }

        // If the anchorDocStruct is null, the anchor identifier type is not
        // existing.
        if (anchorDocStruct == null) {
            String message = "The anchor identifier metadata type '" + this.anchorIdentifierMetadataType
                    + "' is not existing in the MODS metadata section of the METS file";
            log.error(message);
            throw new ReadException(message);
        }

        // Copy the anchor DocStruct, so it can be added as a parent: copy all
        // metadata, but not it's children.
        return anchorDocStruct.copy(true, false);
    }

    /***************************************************************************
     * <p>
     * Gets the descriptive metadata section of the type "MODS" for the given DocStruct. The appropriavte DivType element must be stored in the
     * DocStructs.getOrigObject() method. After reading the DocStruct it is stored there.
     * </p>
     * 
     * @param inStruct
     * @return String which contains the mods data
     **************************************************************************/
    @SuppressWarnings("unchecked")
    private String getMODSSection(DocStruct inStruct) {

        // Contains the whole MODS metadata section as a string.
        String modsstring = null;
        Object o = inStruct.getOrigObject();
        DivType div = null;
        if (o instanceof DivType d) {
            div = d;
        }
        if (div == null) {
            log.warn("Can't get div object for DocStruct to find appropriate metadata sections!");
            return null;
        }

        // Get all referenced Descriptive Metadata Sections.
        List<String> ids = div.getDMDID();
        if (ids == null) {
            // No IDs found in DMDID section; probably these <div> don't have
            // any metadata attached.
            log.debug("DMDID attribute for div TYPE '" + div.getTYPE() + "' does not contain any IDs");
            return null;
        }

        for (String xid : ids) {
            // Get descriptive metadata section with the given ID xid.
            MdSecType mdsection = this.metsHelper.getDmdSecByID(xid);
            // Get wrap, we don't support referenced metadata sections.
            MdWrap mdw = mdsection.getMdWrap();
            if (mdw.getMDTYPE() == MdSecType.MdWrap.MDTYPE.MODS) {
                // It's MODS.
                XmlData xmldata = mdw.getXmlData();
                modsstring = xmldata.xmlText();
                // Get out of loop as we have found the MODS section.
                break;
            }
            // No MODS, check next one in list.
        }

        return modsstring;
    }

    /***************************************************************************
     * <p>
     * Returns the first Element NODE within the &lt;xmlData> element.
     * </p>
     * 
     * @param inStruct
     * @return
     **************************************************************************/
    @SuppressWarnings("unchecked")
    private Node getDOMforMODSSection(DocStruct inStruct) {

        // Contains the whole MODS metadata section as a string.
        Node modsnode = null;
        Object o = inStruct.getOrigObject();
        DivType div = null;
        if (o instanceof DivType d) {
            div = d;
        }
        if (div == null) {
            log.warn("Can't get DIV object for DocStruct to find appropriate metadata sections");
            return null;
        }

        // Get all referenced Descriptive Metadata Sections.
        List<String> ids = div.getDMDID();
        if (ids == null) {
            // No IDs found in DMDID section; probably these <div> don't have
            // any metadata attached.
            log.info("DMDID attribute for div TYPE '" + div.getTYPE() + "' does not contain any IDs");
            return null;
        }

        for (String xid : ids) {
            // Get descriptive metadata section with the given ID xid.
            MdSecType mdsection = this.metsHelper.getDmdSecByID(xid);

            // Get wrap, we don't support referenced metadata sections.
            MdWrap mdw = mdsection.getMdWrap();
            if (mdw.getMDTYPE() == MdSecType.MdWrap.MDTYPE.MODS) {
                // It's MODS.
                XmlData xmldata = mdw.getXmlData();
                Node xmldatanode = xmldata.getDomNode();
                NodeList nl = xmldatanode.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    modsnode = nl.item(i);
                    if (modsnode.getNodeType() == ELEMENT_NODE) {
                        // This is the mods node.
                        return modsnode;
                    }
                }
                // Get out of loop as we have found the MODS section.
                break;
            }
            // No MODS, check next one in list.
        }

        return modsnode;
    }

    /***************************************************************************
     * <p>
     * Parses the MODS metadata section for the given DocStruct Element. The DocStruct element must have an appropriate DivType object (&lt;div>
     * element) attached to it. The metadata is stored and added to the DocStruct.
     * </p>
     * 
     * @param inStruct
     * @param recursive
     * @throws ReadException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws XPathExpressionException
     **************************************************************************/
    private void parseMetadataForLogicalDocStruct(DocStruct inStruct, boolean recursive)
            throws ReadException, ClassNotFoundException, InstantiationException, IllegalAccessException, XPathExpressionException {

        // Get the appropriate MODS-section for inStruct.
        Node modsnode = getDOMforMODSSection(inStruct);

        // Parse the MODS section, if not NULL; metadata are added to inStruct.
        if (modsnode != null) {
            parseMODS(modsnode, inStruct);

            // DocStruct has no parent, so this might have an anchor reference.
            if (inStruct.getParent() == null && this.xPathAnchorReference != null) {
                String anchorreference = getAnchorIdentifierFromMODSDOM(modsnode);
                inStruct.setReferenceToAnchor(anchorreference);
            }
        }

        // If recursive is set to TRUE, parse the child's metadata, too.
        List<DocStruct> children = inStruct.getAllChildren();
        if (recursive && children != null) {
            for (DocStruct child : children) {
                parseMetadataForLogicalDocStruct(child, recursive);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Gets the anchor identifier from the MODS DOM.
     * </p>
     * 
     * @param inMods
     * @param inStruct
     * @return
     * @throws ReadException
     **************************************************************************/
    protected String getAnchorIdentifierFromMODSDOM(Node inMods) throws ReadException {

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

        String queryExpression = "." + GOOBI_INTERNAL_METADATA_XPATH + "[@name]";
        try {
            XPathExpression expr = xpath.compile(queryExpression);

            // No anchor reference found in file.
            if (inMods == null) {
                String message = "No anchor identifier '" + this.anchorIdentifierMetadataType + "' is existing as defined in the prefs!";
                log.error(message);
                throw new ReadException(message);
            }

            // Carry out the query.
            Object list = expr.evaluate(inMods, XPathConstants.NODESET);
            resultlist = (NodeList) list;

            // Iterate over results.
            if (resultlist.getLength() > 1) {
                String message = "XPath expression '" + queryExpression + "' for reference to the anchor is ambigious!";
                log.error(message);
                throw new ReadException(message);
            }

            for (int i = 0; i < resultlist.getLength(); i++) {
                Node node = resultlist.item(i);
                // Get child Nodes to find the TextNode.
                NodeList nodelist = node.getChildNodes();
                for (int j = 0; j < nodelist.getLength(); j++) {
                    Node subnode = nodelist.item(j);
                    if (subnode.getNodeType() == TEXT_NODE) {
                        anchoridentifier = subnode.getNodeValue();
                        break;
                    }
                }
            }
        } catch (XPathExpressionException e) {
            String message = "XPath expression '" + queryExpression + "' seems not to be correct!";
            log.error(message, e);
            throw new ReadException(message, e);
        }

        return anchoridentifier;
    }

    /***************************************************************************
     * <p>
     * Retrieves the subnodes of inNode using the queryExpression and gets the element's value. The element's value is actually the value of the text
     * node found under the element node selected by the XPath As more than one node can be returned by the xquery expression the result of this
     * method is an array containing the values of all textnodes.
     * </p>
     * 
     * @param inNode
     * @param queryExpression
     * @return
     **************************************************************************/
    protected String[] getValueForUnambigiousXQuery(Node inNode, String queryExpression) throws ReadException {

        List<String> resultList = new LinkedList<>();

        // Check, if currentPath is already available.
        XPathFactory factory = XPathFactory.newInstance();

        // New namespace context.
        PersonalNamespaceContext pnc = new PersonalNamespaceContext();
        pnc.setNamespaceHash(this.namespaces);
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(pnc);

        try {
            XPathExpression expr = xpath.compile(queryExpression);
            // Carry out the query.
            Object objectresult = null;
            objectresult = expr.evaluate(inNode, XPathConstants.NODESET);

            // Iterate over the result nodes - though there should just be a
            // single one.
            //
            // If there are several Nodes, iterate over nodes.
            if (objectresult == null) {
                // No nodes had been selected.
                return null;
            }
            NodeList nodes = (NodeList) objectresult;
            for (int i = 0; i < nodes.getLength(); i++) {
                // Iterate over all found nodes.
                Node node = nodes.item(i);
                // Get text node for this element node.
                //
                // Get child Nodes to find the TextNode.
                NodeList nodelist = node.getChildNodes();
                for (int x = 0; x < nodelist.getLength(); x++) {
                    Node subnode = nodelist.item(x);
                    if (subnode.getNodeType() == TEXT_NODE) {
                        String value = subnode.getNodeValue();
                        // Add the value to the result.
                        resultList.add(value);
                        // Get out of for loop - we just get the first text
                        // node.
                        break;
                    }
                }
            }
        } catch (XPathExpressionException e) {
            String message = "XPath expression '" + queryExpression + "' seems not to be correct!";
            log.error(message, e);
            throw new ReadException(message, e);
        }
        if (resultList.isEmpty()) {
            return null;
        }

        // Convert List content's to array.
        return resultList.toArray(new String[resultList.size()]);

    }

    /***************************************************************************
     * <p>
     * DOMImplementationLS was possibly used by Mr Enders to be able to use XPath on the MODS XML fragments, that is configurable in the prefs.
     * </p>
     * 
     * 
     * @param inMods
     * @param inStruct
     * @throws ReadException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws XPathExpressionException
     **************************************************************************/
    protected void parseMODS(Node inMods, DocStruct inStruct)
            throws ReadException, ClassNotFoundException, InstantiationException, IllegalAccessException, XPathExpressionException {

        // Document in DOM tree which represents the MODS.
        Document modsDocument = null;

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

            modsDocument = builder.parse(is);
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
            String message = "Exception while parsing METS file! Can't create DOM tree!";
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
        NodeList nl = modsDocument.getChildNodes();
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == ELEMENT_NODE) {
                    startingNode = n;
                }
            }
        }

        //
        // Only look for Goobi internal MODS metadata extensions in the MODS
        // data here, used for internal METS file reading.
        //
        XPathExpression expr = xpath.compile(GOOBI_INTERNAL_METADATA_XPATH);
        xqueryresult = expr.evaluate(startingNode, XPathConstants.NODESET);
        log.debug("Query expression: " + GOOBI_INTERNAL_METADATA_XPATH);

        // Get metadata node and handle Goobi extension metadata (and persons).
        NodeList metadataAndPersonNodes = (NodeList) xqueryresult;
        if (metadataAndPersonNodes != null) {
            for (int i = 0; i < metadataAndPersonNodes.getLength(); i++) {

                Node metabagu = metadataAndPersonNodes.item(i);
                if (metabagu.getNodeType() == ELEMENT_NODE && metabagu.getAttributes().getNamedItem("anchorId") == null
                        && metabagu.getAttributes().getNamedItem("type") == null) {
                    String name = metabagu.getAttributes().getNamedItem("name").getNodeValue();
                    String value = metabagu.getTextContent();

                    log.debug("Metadata '" + name + "' with value '" + value + "' found in Goobi's MODS extension");

                    // Check if metadata exists in prefs.
                    MetadataType mdt = this.myPreferences.getMetadataTypeByName(name);
                    if (mdt == null) {
                        // No valid metadata type found.
                        String message =
                                "Can't find internal Metadata with name '" + name + "' for DocStruct '" + inStruct.getType().getName() + "' in prefs";
                        log.error(message);
                        throw new ImportException(message);
                    }

                    // Create and add metadata.
                    try {
                        Metadata md = new Metadata(mdt);
                        md.setValue(value);
                        if (metabagu.getAttributes().getNamedItem("authority") != null
                                && metabagu.getAttributes().getNamedItem("authorityURI") != null
                                && metabagu.getAttributes().getNamedItem("valueURI") != null) {
                            String authority = metabagu.getAttributes().getNamedItem("authority").getNodeValue();
                            String authorityURI = metabagu.getAttributes().getNamedItem("authorityURI").getNodeValue();
                            String valueURI = metabagu.getAttributes().getNamedItem("valueURI").getNodeValue();
                            md.setAuthorityFile(authority, authorityURI, valueURI);
                        }
                        if (mdt.isAllowAccessRestriction() && metabagu.getAttributes().getNamedItem("accessRestrict") != null
                                && "true".equals(metabagu.getAttributes().getNamedItem("accessRestrict").getNodeValue())) {
                            md.setAccessRestrict(true);
                        }

                        inStruct.addMetadata(md);

                        log.debug("Added metadata '" + mdt.getName() + "' to DocStruct '" + inStruct.getType().getName() + "' with value '" + value
                                + "'");
                    } catch (DocStructHasNoTypeException e) {
                        String message = "DocumentStructure for which metadata should be added, has no type!";
                        log.error(message, e);
                        throw new ImportException(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        String message = "Metadata '" + mdt.getName() + "' (" + value + ") is not allowed as a child for '"
                                + inStruct.getType().getName() + "' during MODS import!";
                        log.error(message, e);
                        throw new ImportException(message, e);
                    }
                }

                if (metabagu.getNodeType() == ELEMENT_NODE && metabagu.getAttributes().getNamedItem("anchorId") == null
                        && metabagu.getAttributes().getNamedItem("type") != null
                        && "group".equals(metabagu.getAttributes().getNamedItem("type").getTextContent())) {
                    MetadataGroup metadataGroup = readMetadataGroup(metabagu);
                    if (metadataGroup != null) {
                        try {

                            log.debug("Added metadataGroup '" + metadataGroup.getType().getName() + "' to DocStruct '" + inStruct.getType().getName()
                                    + "'");

                            inStruct.addMetadataGroup(metadataGroup);

                        } catch (DocStructHasNoTypeException e) {
                            String message = "DocumentStructure for which metadata should be added, has no type!";
                            log.error(message, e);
                            throw new ImportException(message, e);

                        } catch (MetadataTypeNotAllowedException e) {
                            String message = "MetadataGroup '" + metadataGroup.getType().getName() + "' is not allowed as a child for '"
                                    + inStruct.getType().getName() + "' during MODS import!";
                            log.error(message, e);
                            throw new ImportException(message, e);
                        }
                    }
                }

                if (metabagu.getNodeType() == ELEMENT_NODE && metabagu.getAttributes().getNamedItem("anchorId") == null
                        && metabagu.getAttributes().getNamedItem("type") != null
                        && "corporate".equals(metabagu.getAttributes().getNamedItem("type").getTextContent())) {
                    Corporate corp = parseModsCorporate(metabagu);
                    if (corp != null) {
                        try {
                            inStruct.addCorporate(corp);
                        } catch (MetadataTypeNotAllowedException e) {
                            String message = "Corporate '" + corp.getType().getName() + "' " + corp.getMainName()
                                    + ") is not allowed as a child for '" + inStruct.getType().getName() + "' during MODS import!";
                            log.error(message, e);
                            throw new ImportException(message);
                        }
                    }
                }

                // We have a person node here!
                if (metabagu.getNodeType() == ELEMENT_NODE && metabagu.getAttributes().getNamedItem("anchorId") == null
                        && metabagu.getAttributes().getNamedItem("type") != null
                        && "person".equals(metabagu.getAttributes().getNamedItem("type").getTextContent())) {
                    Person ps = parseModsPerson(metabagu);
                    if (ps != null) {
                        try {
                            inStruct.addPerson(ps);
                            log.debug("Added person '" + ps.getType().getName() + "' to DocStruct '" + inStruct.getType().getName() + "'");
                        } catch (DocStructHasNoTypeException e) {
                            String message = "DocumentStructure for which metadata should be added has no type!";
                            log.error(message, e);
                            throw new ImportException(message, e);
                        } catch (MetadataTypeNotAllowedException e) {
                            String message = "Person '" + ps.getType().getName() + "' " + ps.getDisplayname() + ") is not allowed as a child for '"
                                    + inStruct.getType().getName() + "' during MODS import!";
                            log.error(message, e);
                            throw new ImportException(message);
                        }
                    }
                }
            }
        }

    }

    private MetadataGroup readMetadataGroup(Node metabagu) throws ReadException {
        String groupName = metabagu.getAttributes().getNamedItem("name").getTextContent();
        // Check if group exists in prefs.
        MetadataGroupType mgt = this.myPreferences.getMetadataGroupTypeByName(groupName);

        if (mgt == null) {
            // No valid metadata type found.
            String message = "Can't find internal Metadata with name '" + groupName + " in prefs";
            log.error(message);
            throw new ImportException(message);
        }
        // Create and add group.
        try {
            MetadataGroup metadataGroup = new MetadataGroup(mgt);

            NodeList metadataNodelist = metabagu.getChildNodes();
            for (int j = 0; j < metadataNodelist.getLength(); j++) {
                Node metadata = metadataNodelist.item(j);

                // metadata
                if (metadata.getNodeType() == ELEMENT_NODE && metadata.getAttributes().getNamedItem("type") == null) {

                    String metadataName = metadata.getAttributes().getNamedItem("name").getTextContent();
                    String value = metadata.getTextContent();
                    String authority = null;
                    String authorityURI = null;
                    String valueURI = null;
                    if (metadata.getAttributes().getNamedItem("authority") != null && metadata.getAttributes().getNamedItem("authorityURI") != null
                            && metadata.getAttributes().getNamedItem("valueURI") != null) {
                        authority = metadata.getAttributes().getNamedItem("authority").getNodeValue();
                        authorityURI = metadata.getAttributes().getNamedItem("authorityURI").getNodeValue();
                        valueURI = metadata.getAttributes().getNamedItem("valueURI").getNodeValue();
                    }

                    Metadata md = new Metadata(myPreferences.getMetadataTypeByName(metadataName));
                    md.setValue(value);
                    metadataGroup.addMetadata(md);
                    if (valueURI != null) {
                        md.setAuthorityFile(authority, authorityURI, valueURI);
                    }
                }

                // person
                else if (metadata.getNodeType() == ELEMENT_NODE && metadata.getAttributes().getNamedItem("type") != null
                        && "person".equals(metadata.getAttributes().getNamedItem("type").getTextContent())) {

                    String role = metadata.getAttributes().item(0).getTextContent();
                    MetadataType mdt = this.myPreferences.getMetadataTypeByName(role);
                    if (mdt == null) {
                        // No valid metadata type found.
                        String message = "Can't find person with name '" + role + "' in prefs";
                        log.error(message);
                        throw new ImportException(message);
                    }

                    // Create and add person.
                    if (mdt.getIsPerson()) {

                        Person ps = new Person(mdt);
                        ps.setRole(mdt.getName());
                        metadataGroup.addPerson(ps);

                        // Iterate over every person's data.
                        NodeList personNodelist = metadata.getChildNodes();
                        String authorityID = null;
                        String authorityURI = null;
                        String authortityValue = null;
                        for (int k = 0; k < personNodelist.getLength(); k++) {

                            Node personbagu = personNodelist.item(k);
                            if (personbagu.getNodeType() == ELEMENT_NODE) {
                                String name = personbagu.getLocalName();
                                String value = personbagu.getTextContent();

                                // Get and set values.
                                if (GOOBI_PERSON_FIRSTNAME_STRING.equals(name)) {
                                    ps.setFirstname(value);
                                } else if (GOOBI_PERSON_LASTNAME_STRING.equals(name)) {
                                    ps.setLastname(value);
                                } else if (GOOBI_PERSON_AFFILIATION_STRING.equals(name)) {
                                    ps.setAffiliation(value);
                                } else if (GOOBI_PERSON_AUTHORITYID_STRING.equals(name)) {
                                    authorityID = value;
                                } else if (GOOBI_PERSON_AUTHORITYURI_STRING.equals(name)) {
                                    authorityURI = value;
                                } else if (GOOBI_PERSON_AUTHORITYVALUE_STRING.equals(name)) {
                                    authortityValue = value;
                                }

                                else if (GOOBI_PERSON_PERSONTYPE_STRING.equals(name)) {
                                    ps.setPersontype(value);
                                } else if (GOOBI_PERSON_DISPLAYNAME_STRING.equals(name)) {
                                    ps.setDisplayname(value);
                                } else if (GOOBI_PERSON_DATEVALUE_STRING.equals(name) && StringUtils.isNotBlank(value)) {
                                    ps.addNamePart(new NamePart(GOOBI_PERSON_DATEVALUE_STRING, value));
                                } else if (GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING.equals(name) && StringUtils.isNotBlank(value)) {
                                    ps.addNamePart(new NamePart(GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING, value));
                                } else if (GOOBI_PERSON_NAMEIDENTIFIER_STRING.equals(name)) {
                                    ps.addAuthorityUriToMap(personbagu.getAttributes().item(0).getTextContent(), value);
                                }

                            }

                        }
                        if (authorityID != null && authorityURI != null && authortityValue != null) {
                            ps.setAuthorityFile(authorityID, authorityURI, authortityValue);
                        }
                    }
                }

                // corporate in group
                else if (metadata.getNodeType() == ELEMENT_NODE && metadata.getAttributes().getNamedItem("type") != null
                        && "corporate".equals(metadata.getAttributes().getNamedItem("type").getTextContent())) {
                    Corporate corp = parseModsCorporate(metadata);
                    if (corp != null) {
                        metadataGroup.addCorporate(corp);
                    }
                } else if (metadata.getNodeType() == ELEMENT_NODE && metadata.getAttributes().getNamedItem("type") != null
                        && "group".equals(metadata.getAttributes().getNamedItem("type").getTextContent())) {
                    // sub group
                    MetadataGroup subGrp = readMetadataGroup(metadata);
                    if (subGrp != null) {
                        metadataGroup.addMetadataGroup(subGrp);
                    }
                }

            }
            return metadataGroup;
        } catch (MetadataTypeNotAllowedException e) {
            log.error(e);
        }
        return null;
    }

    private Corporate parseModsCorporate(Node metabagu) throws ReadException {

        Corporate corporate = null;

        String role = metabagu.getAttributes().getNamedItem("name").getTextContent();

        log.debug("Corporate metadata '" + role + "' found in Goobi's MODS extension");

        // Ccheck if corporate does exist in prefs.
        MetadataType mdt = this.myPreferences.getMetadataTypeByName(role);
        if (mdt == null) {
            // No valid metadata type found.
            String message = "Can't find corporate with name '" + role + "' in prefs";
            log.error(message);
            throw new ImportException(message);
        }

        // Create and add corporate.
        if (mdt.isCorporate()) {

            try {
                corporate = new Corporate(mdt);
            } catch (MetadataTypeNotAllowedException e) {
                String message = "Can't add corporate! MetadataType must not be null!";
                log.error(message, e);
                throw new ReadException(message, e);
            }
            corporate.setRole(mdt.getName());

            // Iterate over every person's data.
            NodeList corporateNodelist = metabagu.getChildNodes();
            String authorityFileID = null;
            String authorityURI = null;
            String authortityValue = null;
            for (int j = 0; j < corporateNodelist.getLength(); j++) {

                Node personbagu = corporateNodelist.item(j);
                if (personbagu.getNodeType() == ELEMENT_NODE) {
                    String name = personbagu.getLocalName();
                    String value = personbagu.getTextContent();

                    // Get and set values.
                    if (GOOBI_CORPORATE_MAINNAME_STRING.equals(name)) {
                        corporate.setMainName(value);
                    } else if (GOOBI_CORPORATE_SUBNAME_STRING.equals(name)) {
                        corporate.addSubName(new NamePart("subname", value));
                    } else if (GOOBI_CORPORATE_PARTNAME_STRING.equals(name)) {
                        corporate.setPartName(value);
                    } else if (GOOBI_PERSON_AUTHORITYID_STRING.equals(name)) {
                        authorityFileID = value;
                    } else if (GOOBI_PERSON_AUTHORITYURI_STRING.equals(name)) {
                        authorityURI = value;
                    } else if (GOOBI_PERSON_AUTHORITYVALUE_STRING.equals(name)) {
                        authortityValue = value;
                    }
                }
            }
            if (authorityFileID != null && authorityURI != null && authortityValue != null) {
                corporate.setAuthorityFile(authorityFileID, authorityURI, authortityValue);
            }
            if (mdt.isAllowAccessRestriction() && metabagu.getAttributes().getNamedItem("accessRestrict") != null
                    && "true".equals(metabagu.getAttributes().getNamedItem("accessRestrict").getNodeValue())) {
                corporate.setAccessRestrict(true);
            }
        }
        return corporate;
    }

    private Person parseModsPerson(Node metabagu) throws ReadException {

        Person ps = null;

        String role = metabagu.getAttributes().getNamedItem("name").getTextContent();

        log.debug("Person metadata '" + role + "' found in Goobi's MODS extension");

        // Ccheck if person does exist in prefs.
        MetadataType mdt = this.myPreferences.getMetadataTypeByName(role);
        if (mdt == null) {
            // No valid metadata type found.
            String message = "Can't find person with name '" + role + "' in prefs";
            log.error(message);
            throw new ImportException(message);
        }

        // Create and add person.
        if (mdt.getIsPerson()) {

            try {
                ps = new Person(mdt);
            } catch (MetadataTypeNotAllowedException e) {
                String message = "Can't add person! MetadataType must not be null!";
                log.error(message, e);
                throw new ReadException(message, e);
            }
            ps.setRole(mdt.getName());

            // Iterate over every person's data.
            NodeList personNodelist = metabagu.getChildNodes();
            String authorityFileID = null;
            String authorityURI = null;
            String authortityValue = null;
            for (int j = 0; j < personNodelist.getLength(); j++) {

                Node personbagu = personNodelist.item(j);
                if (personbagu.getNodeType() == ELEMENT_NODE) {
                    String name = personbagu.getLocalName();
                    String value = personbagu.getTextContent();

                    // Get and set values.
                    if (GOOBI_PERSON_FIRSTNAME_STRING.equals(name)) {
                        ps.setFirstname(value);
                    } else if (GOOBI_PERSON_LASTNAME_STRING.equals(name)) {
                        ps.setLastname(value);
                    } else if (GOOBI_PERSON_AFFILIATION_STRING.equals(name)) {
                        ps.setAffiliation(value);
                    } else if (GOOBI_PERSON_AUTHORITYID_STRING.equals(name)) {
                        authorityFileID = value;
                    } else if (GOOBI_PERSON_AUTHORITYURI_STRING.equals(name)) {
                        authorityURI = value;
                    } else if (GOOBI_PERSON_AUTHORITYVALUE_STRING.equals(name)) {
                        authortityValue = value;
                    }

                    else if (GOOBI_PERSON_PERSONTYPE_STRING.equals(name)) {
                        ps.setPersontype(value);
                    } else if (GOOBI_PERSON_DISPLAYNAME_STRING.equals(name)) {
                        ps.setDisplayname(value);
                    }

                    else if (GOOBI_PERSON_DATEVALUE_STRING.equals(name)) {
                        ps.addNamePart(new NamePart(GOOBI_PERSON_DATEVALUE_STRING, value));
                    } else if (GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING.equals(name)) {
                        ps.addNamePart(new NamePart(GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING, value));
                    } else if (GOOBI_PERSON_NAMEIDENTIFIER_STRING.equals(name)) {
                        ps.addAuthorityUriToMap(personbagu.getAttributes().item(0).getTextContent(), value);
                    }
                }
            }
            if (authorityFileID != null && authorityURI != null && authortityValue != null) {
                ps.setAuthorityFile(authorityFileID, authorityURI, authortityValue);
            }
            if (mdt.isAllowAccessRestriction() && metabagu.getAttributes().getNamedItem("accessRestrict") != null
                    && "true".equals(metabagu.getAttributes().getNamedItem("accessRestrict").getNodeValue())) {
                ps.setAccessRestrict(true);
            }
        }
        return ps;
    }

    /***************************************************************************
     * <p>
     * Checks for missing, but needed settings.
     * </p>
     * 
     * @return
     **************************************************************************/
    protected List<String> checkMissingSettings() {
        return new LinkedList<>();
    }

    /***************************************************************************
     * <p>
     * Reads the physical structMap (&lt;structMap type="PHYSICAL">) and creates the appropriate physical DocStruct objects. The topmost physical
     * structure entity for the DigitalDocument is set as well.
     * </p>
     * 
     * <p>
     * We expext here only to be an amount of children of the top DocStruct, no grandchildren or greatgreatgrandchildren or something else!
     * </p>
     * 
     * @param inMetsElement
     * @throws ReadException
     **************************************************************************/
    private void readPhysDocStruct() throws ReadException {

        log.info("Reading Physical DocStruct...");

        List<StructMapType> physmaplist = this.metsHelper.getStructMapByType(METS_STRUCTMAP_TYPE_PHYSICAL_STRING);

        // No physical map available.
        if (physmaplist.isEmpty()) {
            log.info("No Physical StructMap available");
            return;
        }

        // That's what we need: only ONE structmap of type PHYSICAL!
        if (physmaplist.size() == 1) {
            // Get topmost <div>, we take the first one in the list.
            StructMapType physstructmap = physmaplist.get(0);
            // There can only be a single topmost div.
            DivType topmostdiv = physstructmap.getDiv();

            // Create DocStruct instance for the topmost <div>.
            String type = topmostdiv.getTYPE();
            String id = topmostdiv.getID();
            if (type == null) {
                String message = "No type attribute set for topmost div in physical structMap!";
                log.error(message);
                throw new ReadException(message);
            }

            // Get the DocStructType from the prefs.
            DocStructType myType = this.myPreferences.getDocStrctTypeByName(type);
            if (myType == null) {
                // Can't find the appropriate type, return without creating a
                // physical structmap.
                String message = "No internal DocStructType with the name '" + type + "' available in prefs!";
                log.error(message);
                throw new ReadException(message);
            }

            // Create DocStruct for the topmost <div>.
            DocStruct newDocStruct = null;
            try {
                newDocStruct = this.getDigitalDocument().createDocStruct(myType);
                newDocStruct.setIdentifier(id);
                newDocStruct.setOrigObject(topmostdiv);
            } catch (TypeNotAllowedForParentException e) {
                String message = "Can't create this DocStruct of type '" + type + "' at the current tree position (physical structMap)!";
                log.error(message);
                throw new ReadException(message);
            }

            // Handle children for the topmost <div>. Parse the child divs and
            // create appropriate DocStruct elements for them.
            List<DocStruct> toplist = readDivChildren(topmostdiv);
            if (toplist != null) {
                // Create an iterator over the sorted fileMap keyset.
                // sortedFileMap should be not null, because we check this when
                // reading the FileGroup LOCAL.
                Iterator<String> fileMapIterator = this.sortedFileMap.keySet().iterator();

                for (DocStruct child : toplist) {
                    try {
                        // Get the fileList from the DivType object.
                        Fptr[] fileList = ((DivType) child.getOrigObject()).getFptrArray();

                        // Get file pointer list from current div and add all
                        // content files to the current DocStruct, if any FPTRs
                        // are existing.
                        if (fileList != null && fileList.length > 0) {
                            for (Fptr fptr : fileList) {
                                if (fptr != null) {
                                    ContentFile cf = this.sortedFileMap.get(fptr.getFILEID());

                                    if (cf != null) {
                                        child.addContentFile(cf);
                                        child.setTechMds(cf.getTechMds());

                                        log.trace("Added content file with ID '" + cf.getIdentifier() + "' to DocStruct '" + child.getType().getName()
                                                + "'");
                                    } else {
                                        log.warn("No content file added to DocStruct '" + child.getType().getName() + "'");
                                    }
                                    // check if fptr contains an area sequence
                                    SeqType seq = fptr.getSeq();
                                    if (seq != null) {
                                        AreaType[] areas = seq.getAreaArray();
                                        if (areas != null && areas.length > 0) {
                                            for (AreaType area : areas) {
                                                try {
                                                    DocStruct areaDocStruct =
                                                            getDigitalDocument().createDocStruct(myPreferences.getDocStrctTypeByName("area"));
                                                    areaDocStruct.setDocstructType("area");
                                                    String coords = area.getCOORDS(); // COORDS: xsd:string   optional
                                                    Metadata coordinates = new Metadata(myPreferences.getMetadataTypeByName("_COORDS"));
                                                    coordinates.setValue(coords);
                                                    areaDocStruct.addMetadata(coordinates);

                                                    // String ID = area.getID(); // ID: xsd:ID   optional
                                                    areaDocStruct.setOrigObject(area);
                                                    // String fileId = area.getFILEID(); // FILEID: xsd:IDREF   required

                                                    AreaType.SHAPE.Enum currentShape = area.getSHAPE(); //  SHAPE:   optional   | RECT | CIRCLE | POLY
                                                    Metadata shape = new Metadata(myPreferences.getMetadataTypeByName("_SHAPE"));
                                                    shape.setValue(currentShape == null ? AreaType.SHAPE.RECT.toString() : currentShape.toString());
                                                    areaDocStruct.addMetadata(shape);
                                                    // List<String> admid = area.getADMID(); // ADMID: xsd:IDREFS   optional
                                                    child.addChild(areaDocStruct);
                                                } catch (TypeNotAllowedForParentException | MetadataTypeNotAllowedException e) {
                                                    log.error("area is unsupported in the given ruleset. ");
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        } else if (fileMapIterator.hasNext()) {
                            ContentFile cf = this.sortedFileMap.get(fileMapIterator.next());
                            child.addContentFile(cf);

                            log.warn("File pointer list for DocStruct '" + child.getType().getName() + "' is empty! Using file '"
                                    + cf.getIdentifier() + "' from FileGroup " + METS_FILEGROUP_LOCAL_STRING);
                        }

                        // Add the child.
                        newDocStruct.addChild(child);

                        log.trace("Added DocStruct '" + child.getType().getName() + "' to DocStruct '" + newDocStruct.getType().getName() + "'");

                    } catch (TypeNotAllowedAsChildException e) {
                        String message = "Can't create this DocStruct of type '" + type + "' at the current position in tree (physical tree)!";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }
                }
            }

            // Set the topmost div's DocStruct object as the topmost physical
            // DocStruct.
            this.getDigitalDocument().setPhysicalDocStruct(newDocStruct);

            // Get metadata for this digdoc (and all its child docs, in this
            // case pages only!).
            try {
                parseMetadataForPhysicalDocStruct(newDocStruct, true);
            } catch (MetadataTypeNotAllowedException e) {
                String message = "Can't create DocStruct! MetadataType must not be null!";
                log.error(message, e);
                throw new ReadException(message, e);
            }

            // If the fileSet is still empty, iterate over all DocStructs "page"
            // with metadata "physPageNumber" and add a content file each.
            List<DocStruct> pages = newDocStruct.getAllChildren();
            if (this.digdoc.getFileSet().getAllFiles().isEmpty() && pages != null) {
                for (DocStruct ds : pages) {
                    // Get the content file and add it to the DocStruct.
                    this.digdoc.addContentFileFromPhysicalPage(ds);
                }
            }
        }
        // More than one physical map.
        else if (physmaplist.size() > 1) {
            String message = "Too many <structMap> elements of type PHYSICAL!";
            log.error(message);
            throw new ReadException(message);
        }
        // No physical structMap available.
        else {
            log.debug("No physical structMap available");
        }
    }

    /***************************************************************************
     * <p>
     * Parses the Metadata for the physical DocStruct.
     * </p>
     * 
     * @param inStruct
     * @param recursive
     * @throws ReadException
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    private void parseMetadataForPhysicalDocStruct(DocStruct inStruct, boolean recursive) throws ReadException, MetadataTypeNotAllowedException {

        Node modsnode = getDOMforMODSSection(inStruct);

        // Parse the MODS section, if not NULL; metadata are added to inStruct.
        if (modsnode != null) {
            try {
                parseMODS(modsnode, inStruct);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | XPathExpressionException e) {
                String message = "Unable to get MODS Section for DocStruct '" + inStruct.getType().getName() + "'!";
                log.error(message, e);
                throw new ReadException(message, e);
            }
        }

        Object o = inStruct.getOrigObject();

        if (o == null) {
            log.warn("Can't get div object for DocStruct to find appropriate metadata sections!");
            return;
        }

        // Check, if ORDER and ORDERLABEL are set; if so, they contain metadata
        // for logical and physical page number.
        // Get metadataTypes for logical and physical page numbers.
        MetadataType logpageType = this.myPreferences.getMetadataTypeByName(METADATA_LOGICAL_PAGE_NUMBER);
        MetadataType physpageType = this.myPreferences.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER);

        // If type="page", check if logical and physical page numbers are set.
        // If not, add them and set them both to "1".
        if (DigitalDocument.PhysicalElement.checkPhysicalType(inStruct.getType().getName())) {
            DivType pagediv = null;
            if (o instanceof DivType d) {
                pagediv = d;
            }
            if (pagediv != null) {
                // DivType object available, get and set ORDER and ORDERLABEL.
                //
                // Logical page number.
                String logpageString = pagediv.getORDERLABEL();
                if (logpageString == null) {
                    logpageString = METADATA_PAGE_UNCOUNTED_VALUE;
                }
                if ("uncounted".equals(logpageString)) {
                    logpageString = METADATA_PAGE_UNCOUNTED_VALUE;
                }

                // If no value is given, it is an uncounted page.
                if (logpageString == null || "".equals(logpageString)) {
                    logpageString = METADATA_PAGE_UNCOUNTED_VALUE;
                }

                // Add logical page metadata.
                if (inStruct.getAllMetadataByType(logpageType).isEmpty()) {
                    Metadata logmd = new Metadata(logpageType);
                    logmd.setValue(logpageString);
                    try {
                        inStruct.addMetadata(logmd);
                    } catch (DocStructHasNoTypeException e) {
                        String message = "Error while adding logical page number!";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        String message = "Metadata '" + METADATA_LOGICAL_PAGE_NUMBER + "' is not allowed for DocStruct '"
                                + inStruct.getType().getName() + "'!";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }
                }

                // Physical page number.
                String physpageString = null;
                if (pagediv.getORDER() != null) {
                    physpageString = pagediv.getORDER().toString();
                } else {
                    String message = "Div with type '" + pagediv.getTYPE() + "' contains no ORDER element!";
                    log.error(message);
                    throw new ReadException(message);
                }

                if (inStruct.getAllMetadataByType(physpageType).isEmpty()) {
                    Metadata physmd = new Metadata(physpageType);
                    physmd.setValue(physpageString);

                    // Add the metadata to the DocStruct.
                    try {
                        inStruct.addMetadata(physmd);
                    } catch (DocStructHasNoTypeException e) {
                        String message = "'DocStruct has no type' while adding physical page number!";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        String message = "Metadata '" + METADATA_PHYSICAL_PAGE_NUMBER + "' is not allowed for DocStruct!";
                        log.error(message, e);
                        throw new ReadException(message, e);
                    }
                }
                if (inStruct.getAllChildren() != null) {
                    for (DocStruct ds : inStruct.getAllChildren()) {
                        Metadata logmd = new Metadata(logpageType);
                        logmd.setValue(logpageString);
                        Metadata physmd = new Metadata(physpageType);
                        physmd.setValue(physpageString);
                        try {
                            ds.addMetadata(physmd);
                            ds.addMetadata(logmd);
                        } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                            // metadata is not allowed
                        }
                    }
                }

            }
        }

        List<DocStruct> children = inStruct.getAllChildren();

        if (recursive && children != null) {
            for (DocStruct child : children) {
                parseMetadataForPhysicalDocStruct(child, recursive);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Gets a DOM text node value.
     * </p>
     * 
     * @param inNode
     * @return
     **************************************************************************/
    protected String getTextNodeValue(Node inNode) {

        String result = null;
        NodeList textnodes = inNode.getChildNodes();
        if (textnodes != null) {
            Node textnode = textnodes.item(0);
            if (textnode == null || textnode.getNodeType() != Node.TEXT_NODE) {
                // No text node available.
                return null;
            }
            result = textnode.getNodeValue();
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Write the METS/MODS object.
     * </p>
     * 
     * @param filename
     * @param validate
     * @param isAnchorFile
     * @return
     * @throws WriteException
     * @throws PreferencesException
     * @throws MissingModsMappingException
     **************************************************************************/
    private boolean writeMetsMods(String filename, boolean validate, boolean isAnchorFile) throws WriteException, PreferencesException {

        // Check if all necesarry things are set from outside.
        List<String> missingSettings = checkMissingSettings();
        if (!missingSettings.isEmpty()) {
            log.warn("The following settings have not been initialised: " + missingSettings);
        }

        // Get output stream.
        FileOutputStream xmlFile = null;
        try {
            xmlFile = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            String message = "Can't write file '" + filename + "'!";
            log.error(message, e);
            throw new WriteException(message, e);
        }

        try {
            // Find the implementation.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document domDoc = builder.newDocument();

            // Create the document, set METS and xlink namespaces.
            this.metsNode = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_METS_STRING);

            // Iterate over all namespace prefixes, and collect the namespaces'
            // URIs and schema locations, if a schema location is existing.
            StringBuilder schemaLocations = new StringBuilder();
            for (Entry<String, Namespace> e : this.namespaces.entrySet()) {
                if (e.getValue().getSchemalocation() != null) {
                    schemaLocations.append(e.getValue().getUri() + " " + e.getValue().getSchemalocation() + " ");
                }
            }

            // Write schema locations.
            if (schemaLocations.length() > 0) {
                createDomAttributeNS(this.metsNode, this.xsiNamespacePrefix, METS_SCHEMALOCATION_STRING, schemaLocations.toString().trim());
            }

            // add OBJID
            metsNode.setAttribute("OBJID", goobiID);
            // Append the METS node.
            domDoc.appendChild(this.metsNode);

            Element metsHdr = createDomElementNS(domDoc, this.metsNamespacePrefix, "metsHdr");

            metsHdr.setAttribute("CREATEDATE", generateDate());
            Element agent = createDomElementNS(domDoc, this.metsNamespacePrefix, "agent");
            agent.setAttribute("ROLE", "CREATOR");

            agent.setAttribute("TYPE", "OTHER");

            agent.setAttribute("OTHERTYPE", "SOFTWARE");

            try {
                Element name = createDomElementNS(domDoc, this.metsNamespacePrefix, "name");
                if (StringUtils.isBlank(softwareName)) {
                    name.setTextContent(
                            ugh.UghVersion.PROGRAMNAME + " - " + ugh.UghVersion.getBUILDVERSION() + " - " + ugh.UghVersion.getBUILDDATE());
                    agent.appendChild(name);
                } else {
                    name.setTextContent(softwareName);
                    agent.appendChild(name);

                    Element hash = createDomElementNS(domDoc, this.metsNamespacePrefix, "note");
                    hash.setAttributeNS(namespaces.get("intranda").getUri(), "type", "hash");
                    hash.setTextContent(ugh.UghVersion.getBUILDVERSION());
                    agent.appendChild(hash);

                    Element buildDate = createDomElementNS(domDoc, this.metsNamespacePrefix, "note");
                    buildDate.setAttributeNS(namespaces.get("intranda").getUri(), "type", "builddate");
                    buildDate.setTextContent(ugh.UghVersion.getBUILDDATE());
                    agent.appendChild(buildDate);

                }

            } catch (Exception e1) {
                // nothing
            }

            if (StringUtils.isNotBlank(softwareVersion)) {
                Element note = createDomElementNS(domDoc, this.metsNamespacePrefix, "note");
                note.setAttributeNS(namespaces.get("intranda").getUri(), "type", "version");
                note.setTextContent(softwareVersion);
                agent.appendChild(note);
            }

            if (StringUtils.isNotBlank(instanceName)) {
                Element note = createDomElementNS(domDoc, this.metsNamespacePrefix, "note");
                note.setAttributeNS(namespaces.get("intranda").getUri(), "type", "instance");
                note.setTextContent(instanceName);
                agent.appendChild(note);
            }

            if (StringUtils.isNotBlank(clientName)) {
                Element note = createDomElementNS(domDoc, this.metsNamespacePrefix, "note");
                note.setAttributeNS(namespaces.get("intranda").getUri(), "type", "client");
                note.setTextContent(clientName);
                agent.appendChild(note);
            }

            metsHdr.appendChild(agent);

            this.metsNode.appendChild(metsHdr);

            // Get topmost divs.
            DocStruct toplogdiv = this.digdoc.getLogicalDocStruct();
            if (toplogdiv == null && validate) {
                log.error("DigitalDocument has no logical structure");
                return false;
            }

            // Check, if content files and physical DocStruct is available.
            if ((this.digdoc.getFileSet() != null && validate) && (this.digdoc.getPhysicalDocStruct() == null)) {
                String message = "FileSet is available, but no physical Structure!";
                log.error(message);
                throw new WriteException(message);
            }

            if ((this.digdoc.getFileSet() == null && validate) && (this.digdoc.getPhysicalDocStruct() != null)) {
                String message = "ContentFiles (FileSec) must be available for physical structure!";
                log.error(message);
                throw new WriteException(message);
            }

            // Write logical divs. They must be available in any case (even if
            // the DocStruct is an anchor).
            log.info("Writing logical divs");
            Element logdiv = writeLogDivs(this.metsNode, toplogdiv, isAnchorFile);

            // Write fileSec.
            log.info("Writing fileSec");
            Element fileSecElement = null;
            if (this.digdoc.getFileSet() != null) {
                fileSecElement = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_FILESEC_STRING);

                // Write all fileGroupPathes.
                boolean localFilegroupInGoobi = false;

                if (!this.digdoc.getFileSet().getVirtualFileGroups().isEmpty()) {
                    for (VirtualFileGroup vFileGroup : this.digdoc.getFileSet().getVirtualFileGroups()) {
                        if (METS_FILEGROUP_LOCAL_STRING.equals(vFileGroup.getName())) {
                            localFilegroupInGoobi = true;
                            if (this.writeLocalFilegroup) {
                                fileSecElement.appendChild(createFileGroup(domDoc, vFileGroup));
                            }
                        } else {
                            fileSecElement.appendChild(createFileGroup(domDoc, vFileGroup));
                        }
                    }
                }

                // Only write local file group, if no file group "LOCAL" is
                // defined.
                if (!localFilegroupInGoobi && this.writeLocalFilegroup) {
                    VirtualFileGroup vFileGroup = new VirtualFileGroup();
                    vFileGroup.setName(METS_FILEGROUP_LOCAL_STRING);
                    this.digdoc.getFileSet().addVirtualFileGroup(vFileGroup);
                    fileSecElement.appendChild(createFileGroup(domDoc, vFileGroup));
                }
            }

            // Create structMap type logical.
            log.info("Creating structMap logical");
            Element structMapLog = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_STRUCTMAP_STRING);
            this.metsNode.appendChild(structMapLog);
            structMapLog.setAttribute(METS_STRUCTMAPTYPE_STRING, METS_STRUCTMAP_TYPE_LOGICAL_STRING);
            this.firstDivNode = structMapLog;
            structMapLog.appendChild(logdiv);

            // Create structMap type physical.
            DocStruct topphysdiv = this.digdoc.getPhysicalDocStruct();
            if (topphysdiv != null) {
                log.info("Creating structMap physical");

                Element structMapPhys = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_STRUCTMAP_STRING);
                this.metsNode.appendChild(structMapPhys);
                structMapPhys.setAttribute(METS_STRUCTMAPTYPE_STRING, METS_STRUCTMAP_TYPE_PHYSICAL_STRING);
                Element physdiv = writePhysDivs(this.metsNode, topphysdiv);
                if (physdiv != null) {
                    structMapPhys.appendChild(physdiv);
                }
                if (topphysdiv.getAllChildren() != null && !topphysdiv.getAllChildren().isEmpty()) {
                    // Write smLinks.
                    log.info("Creating structLink element");
                    Element structLinkElement = writeSMLinks(this.metsNode);

                    // Order all XML-Elements according to METS schema.
                    log.info("Writing structMaps and structLink element");
                    // NOTE Changed the "&" into an "&&", most possibly a typo.
                    if (fileSecElement != null && structMapLog != null) {
                        this.metsNode.insertBefore(fileSecElement, structMapLog);
                    } else {
                        log.debug("No FileSec or StructMap LOGICAL existing yet");
                    }
                    if (structMapPhys != null) {
                        this.metsNode.appendChild(structMapPhys);
                    }
                    this.metsNode.appendChild(structLinkElement);
                }
            }

            // Write amdSec, if needed.
            log.info("Writing amdSec");

            writeAmdSec(domDoc, isAnchorFile);

            // Serialize the document.
            log.info("Serializing METS document to file");
            serializeMets(domDoc, xmlFile);

        } catch (

        FactoryConfigurationError e) {
            String message = "JAXP can't be found!";
            log.error(message, e.getException());
            throw new WriteException(message, e.getException());
        } catch (ParserConfigurationException e) {
            String message = "XML parser couldn't be loaded!";
            log.error(message, e);
            throw new WriteException(message, e);
        } catch (IOException e) {
            String message = "File '" + filename + "' could not be written!";
            log.error(message, e);
            throw new WriteException(message, e);
        } catch (DOMException e) {
            String message = "Exception building DOM tree!";
            log.error(message, e);
            throw new WriteException(message, e);
        } finally {
            try {
                xmlFile.close();
            } catch (IOException e) {
                log.error(e);
            }
        }

        return true;
    }

    private String generateDate() {
        Date d = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");// ;YYYY-MM-DDThh:mm:ssZ
        SimpleDateFormat hours = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        hours.setTimeZone(TimeZone.getTimeZone("GMT"));
        String yearMonthDay = format.format(d);
        String hourMinuteSecond = hours.format(d);

        return yearMonthDay + "T" + hourMinuteSecond + "Z";
    }

    /***************************************************************************
     * <p>
     * Reads namespace information from the configuration file, and change namespace prefixes.
     * </p>
     * 
     * @param inNode
     * @return
     **************************************************************************/
    private boolean readNamespacePrefs(Node inNode) {

        Namespace nSpace = new Namespace();

        // Check child nodes.
        NodeList childlist = inNode.getChildNodes();
        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);
            String nodename = currentNode.getNodeName();

            if (nodename != null) {
                if ((currentNode.getNodeType() == ELEMENT_NODE) && (PREFS_NAMESPACE_URI_STRING.equalsIgnoreCase(nodename))) {
                    nSpace.setUri(getTextNodeValue(currentNode));
                }
                if ((currentNode.getNodeType() == ELEMENT_NODE) && (PREFS_NAMESPACE_PREFIX_STRING.equalsIgnoreCase(nodename))) {
                    nSpace.setPrefix(getTextNodeValue(currentNode));
                }
                if ((currentNode.getNodeType() == ELEMENT_NODE) && (PREFS_NAMESPACE_SCHEMALOCATION.equalsIgnoreCase(nodename))) {
                    nSpace.setSchemalocation(getTextNodeValue(currentNode));
                }
            }
        }

        // Check values.
        if ((nSpace.getUri() == null) || (nSpace.getPrefix() == null)) {
            return false;
        }

        // Iterate over the namespace entryset and check if the namespace URI
        // already is existing.
        boolean newNamespace = true;
        for (Entry<String, Namespace> e : this.namespaces.entrySet()) {
            if (e.getValue().getUri().equals(nSpace.getUri())) {

                log.warn("Namespace URI '" + nSpace.getUri() + "' maps existing (pre-definded) namespace! New values assigned!");

                // Change the existing namespace content with the one from the
                // prefs, if not null.
                if (!"".equals(nSpace.getPrefix())) {
                    e.getValue().setPrefix(nSpace.getPrefix());
                }
                if (nSpace.getSchemalocation() != null && !"".equals(nSpace.getSchemalocation())) {
                    e.getValue().setSchemalocation(nSpace.getSchemalocation());
                }
                newNamespace = false;
            }
        }

        // Otherwise, put the new namespace into the hashmap.
        if (newNamespace) {
            Namespace n = this.namespaces.put(nSpace.prefix, nSpace);
            if (n == null) {
                log.info("New namespace added with prefix '" + nSpace.getPrefix() + "' and URI '" + nSpace.getUri() + "' added");
            }
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Creates a METS file group.
     * </p>
     * 
     * @param domDoc
     * @param theFilegroup
     * @return
     **************************************************************************/
    private Element createFileGroup(Document domDoc, VirtualFileGroup theFilegroup) {

        Element result = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_FILEGRP_STRING);
        result.setAttribute(METS_FILEGROUPUSE_STRING, theFilegroup.getName());

        // Check fileset availibility.
        FileSet fs = this.digdoc.getFileSet();
        if (fs == null) {
            log.warn("No fileset available... unable to create FileGroups!");
            return result;
        }

        if (fs.getAllFiles() != null) {
            for (ContentFile file : fs.getAllFiles()) {
                if (file.getReferencedDocStructs() != null) {
                    for (DocStruct ds : file.getReferencedDocStructs()) {
                        if (ds.getTechMds() != null) {
                            file.setTechMds(ds.getTechMds());
                        }
                    }
                }
            }
        }

        // Check file group pathes, suffixes, and mimetypes, except for
        // filegroup LOCAL.
        if (!METS_FILEGROUP_LOCAL_STRING.equals(theFilegroup.getName())) {
            if ("".equals(theFilegroup.getPathToFiles())) {
                log.warn("The path for file group " + theFilegroup.getName() + " is not configured yet! Using local path '"
                        + theFilegroup.getPathToFiles() + "'.");
            }
            if ("".equals(theFilegroup.getMimetype())) {
                log.warn("The mimetype for file group " + theFilegroup.getName() + " is not configured yet! Using local mimetype '"
                        + theFilegroup.getMimetype() + "'.");
            }
            if ("".equals(theFilegroup.getFileSuffix())) {
                log.warn("The file suffix for file group " + theFilegroup.getName() + " is not configured yet! Using local suffix '"
                        + theFilegroup.getFileSuffix() + "'.");
            }
        }
        // Iterate over all the content files.
        List<ContentFile> contentFiles = fs.getAllFiles();
        for (ContentFile cf : contentFiles) {
            //only add content file to filegroup if it may be contained in the filegroup.
            //Per default all files may be contained in all filegroups
            if (theFilegroup.contains(cf)) {

                if (checkIfFiletypeIsSkipped(theFilegroup.getFileExtensionsToIgnore(), cf.getLocation())) {
                    // remove current content file from theFilegroup
                    theFilegroup.removeContentFile(cf);
                    continue;
                }

                Element file = createDomElementNS(domDoc, this.metsNamespacePrefix, "file");
                String mt = cf.getMimetype();
                // We use the mimetype from Goobi if configured, the local one if not.
                if ("".equals(theFilegroup.getMimetype())) {
                    file.setAttribute(METS_MIMETYPE_STRING, mt);
                } else {
                    file.setAttribute(METS_MIMETYPE_STRING, theFilegroup.getMimetype());
                }

                // We use the ID suffix from Goobi if configured, the filegroup's
                // name if not.
                String idSuffix = theFilegroup.getIdSuffix();
                if (idSuffix == null || "".equals(idSuffix)) {
                    idSuffix = "_" + theFilegroup.getName();
                    theFilegroup.setIdSuffix(idSuffix);
                }

                // Set content file's identifier (if not existing yet).
                String id = null;
                if (createUUIDs) {
                    UUID uuid = UUID.randomUUID();
                    id = uuid.toString();
                    cf.addUUID(theFilegroup.getName(), id);
                } else {

                    id = cf.getIdentifier();
                    if (id == null || "".equals(id)) {
                        id = FILE_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(++fileidMax);
                        cf.setIdentifier(id);
                    } else if (id.contains(FILE_PREFIX)) {
                        String numberPart = id.replace(FILE_PREFIX, "");
                        try {
                            int number = Integer.parseInt(numberPart);
                            fileidMax = number;
                        } catch (NumberFormatException e) {
                            // do nothing
                        }

                    }
                }

                // Use the content file's ID if local filegroup is written, append
                // the filegroup's name if not.
                if (!createUUIDs && !METS_FILEGROUP_LOCAL_STRING.equals(theFilegroup.getName())) {
                    id += "_" + theFilegroup.getName();
                }
                file.setAttribute(METS_ID_STRING, id);

                if (cf.isRepresentative()) {
                    file.setAttribute("USE", "banner");
                }

                // write admid attribute is necessary
                List<Md> mdList = cf.getTechMds();
                if (mdList != null) {
                    StringBuilder admid = new StringBuilder();
                    for (Md md : mdList) {
                        if (admid.length() > 0) {
                            admid.append(" ");
                        }
                        admid.append(md.getId());
                    }
                    if (StringUtils.isNotBlank(admid)) {
                        file.setAttribute(METS_ADMID_STRING, admid.toString());
                    }
                }

                // Write location (as URL).
                Element flocat = createDomElementNS(domDoc, this.metsNamespacePrefix, "FLocat");
                flocat.setAttribute(METS_LOCTYPE_STRING, "URL");

                // We use the path from Goobi if configured, the local one if not.
                String lc = cf.getLocation();
                if (!"".equals(theFilegroup.getPathToFiles())) {
                    // Get the filename and replace the filename suffix, if
                    // necessary.
                    if (theFilegroup.isIgnoreConfiguredMimetypeAndSuffix()) {
                        Path path = getFilePath(lc);

                        String mimeType = DigitalDocument.detectMimeType(path);
                        if (StringUtils.isBlank(mimeType) && StringUtils.isNotBlank(cf.getMimetype())) {
                            mimeType = cf.getMimetype();
                        }
                        file.setAttribute(METS_MIMETYPE_STRING, mimeType);

                        lc = theFilegroup.getPathToFiles() + path.getFileName().toString();
                    } else {
                        String n = new File(lc).getName();
                        n = n.substring(0, n.lastIndexOf('.') + 1) + theFilegroup.getFileSuffix();
                        lc = theFilegroup.getPathToFiles() + n;
                    }
                }
                createDomAttributeNS(flocat, this.xlinkNamespacePrefix, METS_HREF_STRING, lc);

                file.appendChild(flocat);
                result.appendChild(file);
            }
        }

        return result;
    }

    /**
     * Try to get the actual Path from a String that may be a path, URI or mixture of both
     * 
     * @param lc
     * @return
     */
    public static Path getFilePath(String lc) {
        Path path;
        try {
            path = Paths.get(lc);
        } catch (InvalidPathException e) {
            try {
                path = getPath(new URI(lc));
            } catch (URISyntaxException e1) {
                path = Paths.get(URI.create(lc));
            }
        }
        return path;
    }

    /**
     * returns the path part of an {@link URI} as {@link Path}. Any characters encoded for the URI are decoded in the Path
     * 
     * 
     * @param uri
     * @return The path of the uri
     */
    private static Path getPath(URI uri) {

        if (uri.isAbsolute()) {
            try {
                return Paths.get(new File(uri).getAbsolutePath());
            } catch (FileSystemNotFoundException | IllegalArgumentException e) {
                return Paths.get(uri.getPath());
            }
        } else {
            try {
                return Paths.get(URLDecoder.decode(uri.toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                return Paths.get(uri.toString());

            }
        }
    }

    /**
     * Check if filetype of is mentioned in extension list. Return true if this is the case
     * 
     * @param extensionsToSkip list of file extensions to skip. Can be comma separated or semicolon separated
     * @param filename filename to test
     * @return
     */
    private boolean checkIfFiletypeIsSkipped(String extensionsToSkip, String filename) {
        if (StringUtils.isNotBlank(extensionsToSkip)) {
            String[] extensions;
            if (extensionsToSkip.contains(",")) {
                extensions = extensionsToSkip.split(",");
            } else {
                extensions = extensionsToSkip.split(";");
            }
            for (String ext : extensions) {
                if (StringUtils.isNotBlank(ext) && filename.endsWith(ext.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    /***************************************************************************
     * <p>
     * Reads the METS FileSec.
     * </p>
     * 
     * @param inMetsElement
     * @throws ReadException
     **************************************************************************/
    private void readFileSec(Mets inMetsElement) throws ReadException {

        log.info("Reading FileSec...");

        FileSet fileset = this.getDigitalDocument().getFileSet();

        // No fileset given yet, create a new one.
        if (fileset == null) {
            log.info("No FileSet existing, creating new one");
            fileset = new FileSet();
            this.digdoc.setFileSet(fileset);
        }

        // Get the file section and all filegroups (on uppermost level).
        FileSec fileSection = inMetsElement.getFileSec();

        // No FileSec existing, e.g. because the METS file only contains an
        // anchor.
        if (fileSection == null) {
            return;
        }

        // Iterate over local filegroup only (that's where the REAL files are
        // stored).
        FileGrp[] filegroups = fileSection.getFileGrpArray();
        if (filegroups != null) {
            for (FileGrp filegroup : filegroups) {
                if (METS_FILEGROUP_LOCAL_STRING.equals(filegroup.getUSE())) {

                    // Read all files from the METS; we are not having subgroups
                    // here.
                    FileType[] fileTypeList = filegroup.getFileArray();
                    if (fileTypeList != null) {
                        for (FileType file : fileTypeList) {
                            // Get location array.
                            FLocat[] location = file.getFLocatArray();

                            // We are just supporting a single location Element.
                            if (location.length != 1) {
                                String message = "None or too many FLocat elements for <file> element!";
                                log.error(message);
                                throw new ReadException(message);
                            }
                            String href = location[0].getHref();
                            if (href == null) {
                                String message = "FLocat element for <file> element has no href attribute specifying the location of the file!";
                                log.error(message);
                                throw new ReadException(message);
                            }

                            // Create a new content file.
                            ContentFile cf = new ContentFile();
                            cf.setLocation(href);

                            // Set the content file's ID.
                            if (file.getID() != null) {
                                cf.setIdentifier(file.getID());
                            }
                            // Set the content file's mimetype.
                            if (file.getMIMETYPE() != null) {
                                cf.setMimetype(file.getMIMETYPE());
                            }

                            // set the admIDs
                            List<?> admIds = file.getADMID();
                            if (admIds != null) {
                                for (Object object : admIds) {
                                    if (object instanceof String id) {
                                        cf.addTechMd(this.digdoc.getTechMd(id));
                                    }
                                }
                            }

                            // Add the file to the fileset.
                            fileset.addFile(cf);

                            log.trace("Added file '" + cf.getLocation() + "' (" + cf.getIdentifier() + ") from FileGrp " + METS_FILEGROUP_LOCAL_STRING
                                    + " to FileSet");

                            // Add the file and the file ID to the content file hashmap
                            // to retrieve the file by using the ID lateron.
                            if (cf.getIdentifier() != null) {
                                this.sortedFileMap.put(cf.getIdentifier(), cf);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsModsGdz#writePhysDivs(org.w3c.dom.Node, ugh.dl.DocStruct)
     */
    protected Element writePhysDivs(Node parentNode, DocStruct inStruct) throws PreferencesException {
        Document domDoc = parentNode.getOwnerDocument();
        if ("div".equals(inStruct.getDocstructType())) {
            // Write div element.
            Element div = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_DIV_STRING);

            String idphys = PHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(this.divphysidMax);
            this.divphysidMax++;

            inStruct.setIdentifier(idphys);
            div.setAttribute(METS_ID_STRING, idphys);

            // Always write internal DocStruct type.
            String type = inStruct.getType().getName();
            div.setAttribute(METS_DIVTYPE_STRING, type);

            if (StringUtils.isNotBlank(inStruct.getAdmId())) {
                div.setAttribute(METS_ADMID_STRING, inStruct.getAdmId());
            }
            if (StringUtils.isNotBlank(inStruct.getAdditionalValue())) {
                createDomAttributeNS(div, "xlink", "label", inStruct.getAdditionalValue());
            }

            // Add div element as child to parentNode.
            parentNode.appendChild(div);

            // Write metadata.
            if (this.metsNode == null) {
                log.error("METS node is null... can't write anything!");
                return null;
            }

            int dmdid = writePhysDmd(this.metsNode, div, inStruct);

            // If dmdid is != -1 then the appropriate metadata section has been
            // written, if dmdid == -1, the inStruct has no metadata.
            String dmdidString = "";
            if (dmdid != -1) {
                dmdidString = DMDPHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(dmdid);
                div.setAttribute(METS_DMDID_STRING, dmdidString);
            }

            // Write links to ContentFiles (FPTRs).
            writeFptrs(inStruct, domDoc, div);
            // Get all children and write their divs recursive.
            List<DocStruct> allChildren = inStruct.getAllChildren();
            if (allChildren != null) {
                for (DocStruct child : allChildren) {
                    writePhysDivs(div, child);
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

    /**************************************************************************
     * <p>
     * Write links to ContentFiles (for this physical docstruct), currently only linking to complete files are supported. The METS <area> element is
     * NOT supported.
     * </p>
     * 
     * @param theStruct
     * @param theDocument
     * @param theDiv
     **************************************************************************/
    protected void writeFptrs(DocStruct theStruct, Document theDocument, Element theDiv) {

        // Get a list of referenced ContentFiles.
        List<ContentFile> contentFiles = theStruct.getAllContentFiles();

        if (contentFiles == null) {
            // No content files for this physical structure.
            log.debug("No content files for DocStruct '" + theStruct.getType().getName() + "'");
            return;
        }

        for (ContentFile cf : contentFiles) {
            // Pass each file group.
            for (VirtualFileGroup vFileGroup : this.digdoc.getFileSet().getVirtualFileGroups()) {
                // Write XML elements (METS:fptr).
                if (vFileGroup.contains(cf)) {
                    if (checkIfFiletypeIsSkipped(vFileGroup.getFileExtensionsToIgnore(), cf.getLocation())) {
                        // remove current content file from theFilegroup
                        vFileGroup.removeContentFile(cf);
                        continue;
                    }
                    Element fptr = createDomElementNS(theDocument, this.metsNamespacePrefix, METS_FPTR_STRING);
                    String id = null;
                    if (createUUIDs) {
                        id = cf.getUUID(vFileGroup.getName());

                    } else {
                        id = cf.getIdentifier();
                        if (!METS_FILEGROUP_LOCAL_STRING.equals(vFileGroup.getName())) {
                            id += "_" + vFileGroup.getName();
                        }
                    }

                    fptr.setAttribute(METS_FILEID_STRING, id);
                    theDiv.appendChild(fptr);

                    log.trace("File '" + cf.getLocation() + "' written in file group " + vFileGroup.getName() + " for DocStruct '"
                            + theStruct.getType().getName() + "'!");
                }
            }
        }
    }

    /***************************************************************************
     * <p>
     * Write single logical div METS sections.
     * </p>
     * 
     * @param parentNode
     * @param inStruct
     * @param isAnchorFile
     * @return
     * @throws WriteException
     * @throws PreferencesException
     **************************************************************************/
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

        // Always write internal DocStruct type.
        String type = inStruct.getType().getName();
        div.setAttribute(METS_DIVTYPE_STRING, type);

        // PLEASE NOTE: We do not set labels for internal storing!

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

        // Set the AMDIDs if necessary
        if (inStruct != null && inStruct.getAmdSec() != null) {
            String amdid = inStruct.getAmdSec().getId();
            if (amdid != null && !amdid.isEmpty()) {
                div.setAttribute(METS_ADMID_STRING, amdid);
            }
        }

        // Create mptr element.
        Element mptr = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MPTR_STRING);
        mptr.setAttribute(METS_LOCTYPE_STRING, "URL");

        // Write the MPTR element if a non-anchor file is written AND element is
        // defined as an anchor in the prefs --> METS pointer in e.g.
        // "periodical volume".
        if (!isAnchorFile && inStruct.getType().isAnchor()) {
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, this.mptrUrl);
            div.appendChild(mptr);
        }

        // Write the MPTR element if an anchor file is written AND parent
        // element is an anchor --> METS pointer in e.g. "periodical".
        if (isAnchorFile && !inStruct.getType().isAnchor()) {
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, this.mptrUrlAnchor);
            // Write mptr element.
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

    /***************************************************************************
     * <p>
     * Retrieves all Metadata for the current DocStruct which are identifiers and can be used to link to inStruct from its child. This is only used,
     * if inStruct is an anchor and therefore stored in a separate XML file.
     * </p>
     * 
     * @param inStruct
     **************************************************************************/
    private void getIdentifiersForAnchorLink(DocStruct inStruct) {

        // If parent is no anchor; so don't write anything.
        if (!inStruct.getType().isAnchor()) {
            return;
        }

        // Get identifier for parent.
        this.anchorIdentifiers = inStruct.getAllIdentifierMetadata();
    }

    /***************************************************************************
     * <p>
     * Parses a string and checks, if it contains attribute definitions. These are starting with "@". An attribute definition may contain a value as
     * well. In this the appropriate attribute is created for inNode and an appropriate value is set. If the attribute definitions has no value, the
     * attribute is created without a value and returned.
     * </p>
     * 
     * <p>
     * PLEASE NOTE: There can only be ONE attribute inside this input string! The " " splitting was taken out because (a) no spaces were possible in
     * the attribute values and (b) nobody seems to have used that!
     * </p>
     * 
     * @param in
     * @param inNode Tthe parent Node, which needs to be an element.
     * @param inDoc
     * @return
     **************************************************************************/
    private Node parseAttributeWithoutValue(String in, Node inNode, Document inDoc) {

        Node resultNode = null;

        if (in.startsWith("@")) {
            // It's an attribute; find its name and value.
            String[] attributeFields = in.split("=");

            // Delete the leading "@" if available.
            if (attributeFields[0].startsWith("@")) {
                attributeFields[0] = attributeFields[0].substring(1, attributeFields[0].length());
            }

            // Check, if we have a name.
            Element element = (Element) inNode;
            // We have a value; the second element is the value delete " and '.
            if (attributeFields.length > 1) {
                if ((attributeFields[1].startsWith("'")) || (attributeFields[1].startsWith("\""))) {
                    attributeFields[1] = attributeFields[1].substring(1, attributeFields[1].length());
                }
                if ((attributeFields[1].endsWith("'")) || (attributeFields[1].endsWith("\""))) {
                    attributeFields[1] = attributeFields[1].substring(0, attributeFields[1].length() - 1);
                }
                // add attribute namespace
                if (attributeFields[0].contains(":")) {
                    String[] parts = attributeFields[0].split(":");
                    String namespace = parts[0];
                    String attributeName = parts[1];

                    createDomAttributeNS(element, namespace, attributeName, attributeFields[1]);

                } else {
                    // Add the attribNode to inNode.
                    element.setAttribute(attributeFields[0], attributeFields[1]);
                }
            }
            // No value available.
            else {
                Attr attr = inDoc.createAttribute(attributeFields[0]);
                element.setAttributeNode(attr);
                resultNode = attr;
            }
        }

        return resultNode;
    }

    /***************************************************************************
     * <p>
     * Creates a node according to XPath.
     * </p>
     * 
     * @param query The xpath.
     * @param startingNode The base node for the xquery.
     * @param modsDocument
     * @throws PreferencesException
     * @return
     **************************************************************************/
    protected Node createNode(String query, Node startingNode, Document modsDocument, boolean checkParent) throws PreferencesException {

        Node newNode = null;
        Node parentNode = startingNode;
        Node latestNode = null;
        String availablePath = "";
        String currentPath = "";
        String currentPathNS = "";
        boolean getout = false;
        String group = "";
        String tag = "";

        // Trim the query string.
        query = query.trim();

        // Check if element contains the "[0-9+]" to enable grouping.
        //
        // This is a really dirty hack, I will fix it tomorrow! (hihi)
        // Check element for MODS grouping brackets.
        Pattern pattern = Pattern.compile("\\[(\\d)+\\]");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            // Get the index of the "[" and the index of the "]".

            int bracketStartIndex = matcher.start();
            int bracketEndIndex = matcher.end() - 1;
            int colonIndex = (query.substring(0, bracketStartIndex)).lastIndexOf(":");
            // Get the group number and the group tag name.
            group = query.substring(bracketStartIndex + 1, bracketEndIndex);
            tag = query.substring(colonIndex + 1, bracketStartIndex);
            // Store the group and tag in a replacement hash.
            this.replaceGroupTags.put(tag + group, tag);
            // Remove the "[]" from the query string.
            query = query.substring(0, bracketStartIndex) + group + query.substring(bracketEndIndex + 1);
        }
        // This is a really dirty hack, I will fix it tomorrow! (hihi)

        log.debug("XPath expression  >>" + query + "<<");

        // Split query into single elements and check, if some of these elements
        // are already available.
        String[] elementPath = splitPath(query);

        // Iterate over all elements from path and check, which part of the path
        // is already available in the DOM tree.
        for (String element : elementPath) {

            // Set to true, if we are requesting an element.
            boolean requestingElement = false;

            // No content in elementPath.
            if ("".equals(element)) {
                continue;
            }

            if (".".equals(element)) {
                currentPath += element;
                currentPathNS += element;
                continue;
            }

            // Check if the path starts (a) with one of the defined namespace
            // prefixes, (b) with a leading "#" and a following defined
            // namespace prefix, or (c) with a leading "@".
            boolean prefixCheck = false;
            for (Namespace iSpace : this.namespaces.values()) {
                if (element.startsWith(iSpace.getPrefix() + ":")
                        || element.startsWith(METS_PREFS_WRITEXPATH_SEPARATOR_STRING + iSpace.getPrefix() + ":")) {
                    prefixCheck = true;
                    break;
                }
            }
            if (!element.startsWith("@") && !prefixCheck) {
                String message = "Prefix missing in METS XPath  >>" + query + "<<  path element  >>" + element + "<<. One of "
                        + this.namespaces.keySet() + " or '@' is expected!";
                log.error(message);
                throw new PreferencesException(message);
            }

            log.trace("Path  >>" + element + "<<");

            // Check, what we are requesting: a NodeList or a boolean.
            availablePath = currentPathNS;

            // Check, if the element name starts with a "#", if so, this element
            // needs to be created anyhow; even if it's new.
            if (element.startsWith(METS_PREFS_WRITEXPATH_SEPARATOR_STRING)) {
                // Start with a hash, get out of loop.
                element = element.substring(1, element.length());
                break;
            }

            // We are not requesting a Node or a NodeList, instead we just
            // check, if something is available with a certain value.
            if (element.contains("=")) {
                requestingElement = false;
                if (StringUtils.isBlank(currentPath)) {
                    // It is the first attribute, so add a "./".
                    currentPath = "./" + element;
                } else {
                    currentPath += "/" + element;
                }
            }
            // We are requesting an element.
            else {
                requestingElement = true;
                // Check, if the element already has a namespace prefix.
                if (element.contains(":")) {
                    // It has, so we don't have to add the namespace.
                    currentPath += "/" + element;
                } else if (element.startsWith("@")) {
                    currentPath += "/" + element;
                } else {
                    // Add the mods namespace.
                    currentPath += "/" + this.modsNamespacePrefix + element;
                }
            }
            currentPathNS += "/" + element;

            // Check, if currentPath is already available.
            XPathFactory factory = XPathFactory.newInstance();

            // Set namespace context.
            XPath xpath = factory.newXPath();
            PersonalNamespaceContext pnc = new PersonalNamespaceContext();
            pnc.setNamespaceHash(this.namespaces);
            xpath.setNamespaceContext(pnc);

            try {
                XPathExpression expr = xpath.compile(currentPath);

                log.trace("Single part - XPath expression  >>" + currentPath + "<<");
                log.trace("Starting node  >>" + startingNode.getNodeName() + "<<");

                // Carry out the query.
                Object result = null;
                if (requestingElement) {
                    if (checkParent) {
                        result = expr.evaluate(startingNode.getParentNode(), XPathConstants.NODESET);
                    } else {
                        result = expr.evaluate(startingNode, XPathConstants.NODESET);
                    }
                } else // We are requesting an attribute.
                if (checkParent) {
                    result = expr.evaluate(startingNode.getParentNode(), XPathConstants.BOOLEAN);
                } else {
                    result = expr.evaluate(startingNode, XPathConstants.BOOLEAN);
                }

                // We were requesting an element, now we should have a
                // nodeset.
                if (requestingElement) {
                    if (result != null) {
                        NodeList nodes = (NodeList) result;
                        log.trace(nodes.getLength() + " nodes found");
                        if (nodes.getLength() == 0) {
                            // No nodes found.
                            getout = true;
                        } else {
                            // Get the first node.
                            latestNode = nodes.item(0);
                        }
                    } else {
                        // No nodes found, so this path (currentpath) is not
                        // available anymore.
                        getout = true;
                    }
                    if (getout) {
                        // Get out of for loop.
                        break;
                    }
                } else {
                    // We were requesting an attribute, so check, if it is
                    // available.
                    Boolean available = (Boolean) result;
                    if (available.booleanValue()) {
                        // After requesting an attribute, we cannot request
                        // anything else, the attribute cannot have any
                        // children.
                        availablePath = availablePath + "/" + element;

                        //get the existing Node with the right attributes
                        String elementName = getSubPathElementName(availablePath);
                        HashMap<String, String> attributeMap = getAttributesFromNode(element);
                        XPathExpression expr2 = xpath.compile(elementName);
                        result = expr2.evaluate(startingNode.getParentNode(), XPathConstants.NODESET);
                        if (result != null) {
                            NodeList nodes = (NodeList) result;
                            if (nodes.getLength() > 0) {
                                for (int i = 0; i < nodes.getLength(); i++) {
                                    Node node = nodes.item(i);
                                    NamedNodeMap attributes = node.getAttributes();
                                    int matchingattributes = 0;
                                    if (attributes != null && attributes.getLength() > 0) {
                                        for (int j = 0; j < attributes.getLength(); j++) {
                                            Node attribute = attributes.item(j);
                                            String testValue = attributeMap.get(attribute.getNodeName());
                                            if (testValue != null && testValue.contentEquals(attribute.getNodeValue())) {
                                                matchingattributes++;
                                            }
                                        }
                                    }
                                    if (matchingattributes > 0 && matchingattributes == attributeMap.size()) {
                                        latestNode = node;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // Attribute is not available, so we need to add it.
                        break;
                    }
                }
            } catch (XPathExpressionException e) {
                String message = "Error due to querying XPath expression '" + query + "'!";
                log.error(message, e);
                throw new PreferencesException(message, e);
            }
        }

        // Now the availablePath variable should contain the path which is
        // already available; the latestNode contains the appropriate Node.
        //
        // Find the path we need to create.
        String pathtocreate = substractStrings(query, availablePath);

        // Split the whole path into subpathes.
        String[] elementsToCreate = splitPath(pathtocreate);

        // Define the node to which the new path should be added.
        if (latestNode != null) {
            // The latest node which was found becomes the parentNode for the
            // creation of a new element or attribute.
            parentNode = latestNode;
        } else {
            // If no node was detected (e.g. in case of a single attribute, the
            // parentNode is the startingNode.
            parentNode = startingNode;
        }

        // Iterate over all elements, which should be added; the element can be
        // an attribute, if it starts with an "@".
        for (String elementName : elementsToCreate) {

            if ("".equals(elementName) || ".".equals(elementName)) {
                // Get next one; this element name does not contain anything.
                continue;
            }

            log.trace("Subpath to create: " + elementName);

            // Create the appropriate element.
            //
            // Check, if there is a leading NOT, e.g. a not(myElement) or
            // not(myAttribut) must not be created.
            if (elementName.startsWith("not") || elementName.startsWith("NOT")) {
                // Ignore this element.
                continue;
            }

            // The element is not an element but an attribute.
            //
            if (elementName.startsWith("@")) {
                newNode = parseAttributeWithoutValue(elementName, parentNode, modsDocument);
                // Get out of loop, an attribute can only be the end of an Xpath
                // expression, as we cannot connect subelements to the
                // attribute.
                break;
            }

            // It's an element
            //
            // Separate the element name from the square brackets.
            //
            String elementNameWOAttributes = getSubPathElementName(elementName);

            // No parent available, so create a new element.
            if (parentNode == null) {
                // Check, if the new element name contains a "=" in this case,
                // we need to get the value (everything which is behing the "=")
                // and set this value as well.
                newNode = createElementWithOrWithoutValue(elementNameWOAttributes, startingNode);

                log.trace("Creating node  >>" + elementNameWOAttributes + "<<");
            }

            // A parent Node is available
            else {
                // Check, if the new element name contains a "=" in this case,
                // we need to get the value (everything which is behind the "=")
                // and set this value as well.
                newNode = createElementWithOrWithoutValue(elementNameWOAttributes, parentNode);

                log.trace("Creating node  >>" + elementNameWOAttributes + "<<");
            }

            // Get information within the brackets.
            String[] bracketcontents = getBracketContents(elementName);
            if (bracketcontents != null) {
                // Create the appropriate nodes for the bracket contents; notice
                // that the current parentNode is the new starting Node.
                for (String content : bracketcontents) {

                    log.trace("Bracket content  >>" + content + "<<");

                    if (!content.startsWith("not")) {
                        // Only check (and create) the bracket contents, if it
                        // does not start with a "not".
                        createNode("./" + content, newNode, modsDocument, checkParent);
                    }
                }
            }

            // The new node becomes the parent node.
            parentNode = newNode;
        }

        return newNode;
    }

    /***************************************************************************
     * <p>
     * Gets the content of some brackets.
     * </p>
     * 
     * @param in
     * @return
     **************************************************************************/
    private String[] getBracketContents(String in) {

        List<String> resultList = new LinkedList<>();

        int firstbracketpos = 0;
        int lastbracketpos = 0;

        // In cases, brackets are nested it gives the depth.
        int nestingdepth = 0;

        // Iterate over all.
        for (int i = 0; i < in.length(); i++) {
            String c = in.substring(i, i + 1);
            if ("[".equals(c)) {
                if (nestingdepth == 0) {
                    // Start of first bracket.
                    firstbracketpos = i;
                }
                nestingdepth++;
            } else if ("]".equals(c)) {
                nestingdepth--;
                if (nestingdepth == 0) {
                    // It the last bracket.
                    lastbracketpos = i;
                }
            }

            if ((firstbracketpos != 0) && (lastbracketpos != 0)) {
                // Add this bracket.
                resultList.add(in.substring(firstbracketpos + 1, lastbracketpos));
                firstbracketpos = 0;
                lastbracketpos = 0;
            }
        }
        return resultList.toArray(new String[resultList.size()]);

    }

    /***************************************************************************
     * <p>
     * Gets the element name of a subpath, and its value if existing. Just takes out all brackets.
     * </p>
     * 
     * @param in
     * @return
     **************************************************************************/
    private String getSubPathElementName(String in) {
        return in.replaceAll("\\[(.*)\\]", "");
    }

    /***************************************************************************
     * <p>
     * Delete String in2 from String in1, if in2 is not included in in1, then the comlete String n1 is returned.
     * </p>
     * 
     * @param in1
     * @param in2
     * @return the result string
     **************************************************************************/
    private String substractStrings(String in1, String in2) {

        if ("".equals(in2)) {
            // There is nothing to substract.
            return in1;
        }

        for (int i = 0; i < in1.length(); i++) {
            String char1 = in1.substring(i);
            // Is there still something to check length of in1-substring is
            // still longer than in2.
            if (char1.length() > in2.length()) {
                String char2 = in1.substring(i, in2.length());
                if (char2.equals(in2)) {
                    return in1.substring(i + in2.length(), in1.length());
                }

                // Get out of loop.
                break;
            }
        }

        return in1;
    }

    /***************************************************************************
     * <p>
     * Splits the Xpath into subpathes. The "'"s also are now beeing ignored.
     * </p>
     * 
     * @param in
     * @return
     **************************************************************************/
    private String[] splitPath(String in) {

        List<String> resultList = new LinkedList<>();

        int oldsplitpos = -1;
        // "insubpath" is set to true, if we are within a subpath (in square
        // brackets).
        boolean insubpath = false;
        // "inquotes" is set to true, if we are within a single quote quotation
        // ('').
        boolean inquotes = false;

        // Iterate over all characters of the given string.
        for (int i = 0; i < in.length(); i++) {
            String c = in.substring(i, i + 1);
            if ("[".equals(c)) {
                insubpath = true;
            } else if ("]".equals(c)) {
                insubpath = false;
            }
            if ("'".equals(c) && !inquotes) {
                inquotes = true;
            } else if ("'".equals(c) && inquotes) {
                inquotes = false;
            }
            if ("/".equals(c) && !insubpath && !inquotes) {
                String pathpart = in.substring(oldsplitpos + 1, i);
                resultList.add(pathpart);
                oldsplitpos = i;
            }
        }

        // Add the last part, if the last char wasn't a "/".
        if (!in.endsWith("/")) {
            String pathpart = in.substring(oldsplitpos + 1, in.length());
            resultList.add(pathpart);
        }

        return resultList.toArray(new String[resultList.size()]);
    }

    /***************************************************************************
     * <p>
     * Creates an element, if the node name contains a "=", the part behind the "=" is regarded as the value of a textNode, which is created. The
     * value between the "''" is added to the TextNode.
     * </p>
     * 
     * @param in the name of the node
     * @param inNode the parentNode
     * @return
     * @throws PreferencesException
     **************************************************************************/
    private Node createElementWithOrWithoutValue(String in, Node inNode) throws PreferencesException {

        Document inDoc = inNode.getOwnerDocument();
        Node resultNode = null;

        // It's an Element: Check, if "in" contains a "=", whih would mean, that
        // we have a value as well.
        String[] attributeFields = in.split("=");
        if (attributeFields[0].startsWith("@")) {
            // Delete the leading "@" if available.
            attributeFields[0] = attributeFields[0].substring(1, attributeFields[0].length());
        }

        // Create the element first; element name is in attributeFields[0].
        //
        //
        // Create Element and text Node.
        Node newNode = null;
        if (attributeFields[0].contains(":")) {
            // It contains a colon; so the element name has namespace
            // information.
            //
            // First part is namespace prefix.
            String[] ns = attributeFields[0].split(":");
            // If the namespace name starts with an "#", delete the first char.
            if (ns[0].startsWith(METS_PREFS_WRITEXPATH_SEPARATOR_STRING)) {
                ns[0] = ns[0].substring(1, ns[0].length());
            }
            // Get the Namespace instance for the given prefix.
            Namespace myNamespace = this.namespaces.get(ns[0]);

            if (ns[0] == null || myNamespace == null || myNamespace.getPrefix() == null || "".equals(myNamespace.getPrefix())) {
                String message = "Namespace '" + ns[0] + "' not defined in prefs or empty! One of " + this.namespaces.keySet() + " is expected!";
                log.error(message);
                throw new PreferencesException(message);
            }

            newNode = createDomElementNS(inDoc, myNamespace.getPrefix(), ns[1]);
            newNode.setPrefix(myNamespace.getPrefix());
        } else {
            // It does not have namespace information, use the current
            // namespace.
            if (attributeFields[0].startsWith(METS_PREFS_WRITEXPATH_SEPARATOR_STRING)) {
                // If the element name starts with an "#", delete the first
                // char.
                attributeFields[0] = attributeFields[0].substring(1, attributeFields[0].length());
            }
            newNode = inDoc.createElement(attributeFields[0]);
        }

        // Check, if we have a value as well; in this case, the value is stored
        // in attributeFields[1]; this is the value of the text node.
        if (attributeFields.length > 1) {
            // We have a value; the second element is the value delete " and '.
            if (attributeFields[1].startsWith("'") || attributeFields[1].startsWith("\"")) {
                attributeFields[1] = attributeFields[1].substring(1, attributeFields[1].length());
            }
            if (attributeFields[1].endsWith("'") || attributeFields[1].endsWith("\"")) {
                attributeFields[1] = attributeFields[1].substring(0, attributeFields[1].length() - 1);
            }

            Node newValueNode = inDoc.createTextNode(attributeFields[1]);
            newNode.appendChild(newValueNode);
        }

        // Add the newly created node.
        inNode.appendChild(newNode);
        resultNode = newNode;

        return resultNode;
    }

    /***************************************************************************
     * <p>
     * Uses XPATH expressions to create new elements.
     * </p>
     * 
     * 
     * @param parentNode
     * @param inStruct
     * @return
     * @throws PreferencesException
     * @throws MissingModsMappingException
     * @throws WriteException
     **************************************************************************/
    protected int writeLogDmd(org.w3c.dom.Node parentNode, DocStruct inStruct, boolean isAnchorFile) throws PreferencesException, WriteException {

        Document domDoc = parentNode.getOwnerDocument();

        // Do throw a WriteException, if the child of anchor DocStruct has no
        // MODS metadata! We have then no identifier!
        if (!isAnchorFile && !inStruct.getType().isAnchor() && inStruct.getParent() != null && inStruct.getParent().getType().isAnchor()
                && inStruct.getAllMetadata() == null) {
            String message = "DocStruct '" + inStruct.getParent().getType().getName()
                    + "' is an anchor DocStruct, but NO anchor identifier is existing for child DocStruct '" + inStruct.getType().getName() + "'!";
            log.error(message);
            throw new WriteException(message);
        }

        // Do NOT create a DIV element, if a non-anchor file is written AND
        // element is defined as an anchor in the prefs OR if an anchor
        // file is written AND parent element is an anchor.
        if ((!isAnchorFile && inStruct.getType().isAnchor()) || (isAnchorFile && !inStruct.getType().isAnchor())) {
            return -1;
        }

        // Check, if the inStruct has metadata or persons; otherwise we can
        // return.
        if ((inStruct.getAllMetadata() == null || inStruct.getAllMetadata().isEmpty())
                && (inStruct.getAllPersons() == null || inStruct.getAllPersons().isEmpty())) {
            // No metadata or persons available.
            return -1;
        }

        // If inStruct is an anchor DocStruct, get the identifier for the
        // anchor.
        if (inStruct.getType().isAnchor()) {
            this.getIdentifiersForAnchorLink(inStruct);
        }

        // Get new ID for metadata.
        int dmdid = this.dmdidMax;
        this.dmdidMax++;

        // Write metadata header.
        Element dmdsec = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_DMDSEC_STRING);
        Element dommodsnode = createModsMetadataHeader(DMDLOG_PREFIX, dmdid, dmdsec, domDoc);

        // Write metadata MODS section.
        writeLogModsSection(inStruct, dommodsnode, domDoc);

        // Only add the dmdsec to the node, if the MODS section is not empty.
        if (dommodsnode.getChildNodes().getLength() > 0) {
            parentNode.appendChild(dmdsec);
            return dmdid;
        }

        return -1;
    }

    /***************************************************************************
     * <p>
     * Creates the MODS metadata header.
     * </p>
     * 
     * @param thePrefix
     * @param theDmdid
     * @param theDmdsec
     * @param theDocument
     * @return
     **************************************************************************/
    private Element createModsMetadataHeader(String thePrefix, int theDmdid, Element theDmdsec, Document theDocument) {

        log.trace("DMDID: " + thePrefix + new DecimalFormat(DECIMAL_FORMAT).format(theDmdid));

        theDmdsec.setAttribute(METS_ID_STRING, thePrefix + new DecimalFormat(DECIMAL_FORMAT).format(theDmdid));

        Element mdWrap = createDomElementNS(theDocument, this.metsNamespacePrefix, METS_MDWRAP_STRING);
        theDmdsec.appendChild(mdWrap);
        mdWrap.setAttribute(METS_MDTYPE_STRING, "MODS");
        Element xmlData = createDomElementNS(theDocument, this.metsNamespacePrefix, METS_XMLDATA_STRING);
        mdWrap.appendChild(xmlData);

        // Create MODS element.
        Element dommodsnode = createDomElementNS(theDocument, this.modsNamespacePrefix, "mods");

        // Append the MODS document.
        xmlData.appendChild(dommodsnode);

        return dommodsnode;
    }

    /***************************************************************************
     * <p>
     * The logical MODS section for internal METS storing.
     * </p>
     * 
     * @param inStruct
     * @param dommodsnode
     * @param domDoc
     * @throws PreferencesException
     **************************************************************************/
    protected void writeLogModsSection(DocStruct inStruct, Node dommodsnode, Document domDoc) throws PreferencesException, WriteException {

        boolean gotAnchorIdentifierType = false;

        // Get parent DocStruct.
        DocStruct parentStruct = inStruct.getParent();

        // Go through all the current metadata and write them to
        // <mods:extension><goobi:goobi><goobi:metadata>.

        if (inStruct.getAllMetadata() != null) {
            for (Metadata m : inStruct.getAllMetadata()) {

                // Always write non-anchorIdentifier metadata!
                if (StringUtils.isNotBlank(m.getValue())) {
                    String xquery = "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                            + ":goobi/#" + this.goobiNamespacePrefix + ":metadata[@name='" + m.getType().getName() + "']";
                    writeSingleModsMetadata(xquery, m, dommodsnode, domDoc);
                }

                // Create a reference only, if parentStruct exists, and
                // parentStruct is an anchor DocStruct, and the MMO's internal
                // name is mentioned in the prefs.
                if (parentStruct != null && parentStruct.getType().isAnchor()
                        && m.getType().getName().equalsIgnoreCase(this.anchorIdentifierMetadataType)) {

                    // Check if anchor identifier type is existing in the prefs.
                    PrefsType identifierType = this.myPreferences.getMetadataTypeByName(this.anchorIdentifierMetadataType);
                    if (identifierType == null) {
                        throw new PreferencesException("Unable to write MODS section! No metadata of type '"
                                + this.anchorIdentifierMetadataType + "' found in prefs to create anchor MODS record");
                    }

                    // Go throught all the identifier metadata of the
                    // parent struct and look for the XPath anchor
                    // reference.
                    List<? extends Metadata> identifierMetadataList = parentStruct.getAllMetadataByType(identifierType);

                    // Can only be one!
                    if (identifierMetadataList.isEmpty()) {
                        throw new WriteException("Unable to write MODS section! No metadata of type '"
                                + this.anchorIdentifierMetadataType + "' existing for parent DocStruct '" + parentStruct.getType().getName() + "'");
                    }

                    Metadata identifierMetadata = identifierMetadataList.get(0);

                    // Add identifier metadata.
                    String xquery = "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                            + ":goobi/" + this.goobiNamespacePrefix + ":metadata[@name='" + this.anchorIdentifierMetadataType
                            + "'][@anchorId='true']";
                    Node createdNode = createNode(xquery, dommodsnode, domDoc, true);

                    if (createdNode != null) {
                        // Node was created successfully, now add
                        // value to it.
                        Node valueNode = domDoc.createTextNode(identifierMetadata.getValue());
                        createdNode.appendChild(valueNode);

                        // Set check flag TRUE.
                        gotAnchorIdentifierType = true;
                    }
                }
            }

            // Check if an anchorIdentifier metadata type was written, if
            // inStruct was a anchor.
            if (parentStruct != null && parentStruct.getType().isAnchor() && !gotAnchorIdentifierType) {
                throw new WriteException("Unable to write MODS section! No metadata of type '" + this.anchorIdentifierMetadataType
                        + "' existing for parent DocStruct '" + parentStruct.getType().getName() + "'");
            }
        }

        if (inStruct.getAllMetadataGroups() != null) {
            for (MetadataGroup m : inStruct.getAllMetadataGroups()) {
                // check if group has values
                boolean isEmpty = true;
                for (Metadata md : m.getMetadataList()) {
                    if (StringUtils.isNotBlank(md.getValue())) {
                        isEmpty = false;
                        break;
                    }
                }
                for (Person p : m.getPersonList()) {
                    if (StringUtils.isNotEmpty(p.getLastname()) || StringUtils.isNotEmpty(p.getFirstname())) {
                        isEmpty = false;
                        break;
                    }
                }
                // only write groups with values

                if (!isEmpty) {
                    String xquery = "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                            + ":goobi/#" + this.goobiNamespacePrefix + ":metadata[@type='group'][@name='" + m.getType().getName() + "']";
                    writeSingleModsGroup(xquery, m, dommodsnode, domDoc, true);
                }

            }

        }
        // write all corporates
        writeDmdCorporates(inStruct, dommodsnode, domDoc);
        // Write all persons.
        writeDmdPersons(inStruct, dommodsnode, domDoc);
    }

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
    protected void writeSingleModsMetadata(String theXQuery, Metadata theMetadata, Node theStartingNode, Document theDocument)
            throws PreferencesException {

        Node createdNode = createNode(theXQuery, theStartingNode, theDocument, true);

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
            ((Element) createdNode).setAttribute("authority", theMetadata.getAuthorityID());
            ((Element) createdNode).setAttribute("authorityURI", theMetadata.getAuthorityURI());
            if (theMetadata.getAuthorityValue().startsWith("http")) {
                ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityValue());
            } else {
                ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityURI() + theMetadata.getAuthorityValue());
            }
        } else if (StringUtils.isNotBlank(theMetadata.getAuthorityValue())) {
            ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityURI() + theMetadata.getAuthorityValue());
        }
        if (theMetadata.getType().isAllowAccessRestriction() && theMetadata.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }

        log.trace("Value '" + theMetadata.getValue() + "' (" + theMetadata.getType().getName() + ") added to node >>" + createdNode.getNodeName()
                + "<<");
    }

    /***************************************************************************
     * <p>
     * Creates a single Goobi internal person element.
     * </p>
     * >
     * 
     * @param theXQuery
     * @param thePerson
     * @param theStartingNode
     * @param theDocument
     * @throws PreferencesException
     **************************************************************************/
    protected void writeSingleModsPerson(String theXQuery, Person thePerson, Node theStartingNode, Document theDocument) throws PreferencesException {

        if (StringUtils.isBlank(thePerson.getLastname()) && StringUtils.isBlank(thePerson.getFirstname())) {
            // person is empty, abort
            return;
        }

        Node createdNode = createNode(theXQuery, theStartingNode, theDocument, true);

        // Set the displayname of the current person, use
        // "lastname, name" as we were told in the MODS
        // profile, only if the displayName is not yet set.
        if (StringUtils.isNotBlank(thePerson.getLastname()) && StringUtils.isNotBlank(thePerson.getFirstname())) {
            thePerson.setDisplayname(thePerson.getLastname() + ", " + thePerson.getFirstname());
        } else if (StringUtils.isBlank(thePerson.getFirstname())) {
            thePerson.setDisplayname(thePerson.getLastname());
        } else {
            thePerson.setDisplayname(thePerson.getFirstname());
        }

        // Create the subnodes.
        if (StringUtils.isNotBlank(thePerson.getLastname())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_LASTNAME_STRING;
            Node lastnameNode = createNode(theXQuery, createdNode, theDocument, true);
            Node lastnamevalueNode = theDocument.createTextNode(thePerson.getLastname());
            lastnameNode.appendChild(lastnamevalueNode);
            createdNode.appendChild(lastnameNode);
        }
        if (StringUtils.isNotBlank(thePerson.getFirstname())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_FIRSTNAME_STRING;
            Node firstnameNode = createNode(theXQuery, createdNode, theDocument, true);
            Node firstnamevalueNode = theDocument.createTextNode(thePerson.getFirstname());
            firstnameNode.appendChild(firstnamevalueNode);
            createdNode.appendChild(firstnameNode);
        }
        if (StringUtils.isNotBlank(thePerson.getAffiliation())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AFFILIATION_STRING;
            Node affiliationNode = createNode(theXQuery, createdNode, theDocument, true);
            Node affiliationvalueNode = theDocument.createTextNode(thePerson.getAffiliation());
            affiliationNode.appendChild(affiliationvalueNode);
            createdNode.appendChild(affiliationNode);
        }

        if (StringUtils.isNotBlank(thePerson.getAuthorityID())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AUTHORITYID_STRING;
            Node authorityfileidNode = createNode(theXQuery, createdNode, theDocument, true);
            Node authorityfileidvalueNode = theDocument.createTextNode(thePerson.getAuthorityID());
            authorityfileidNode.appendChild(authorityfileidvalueNode);
            createdNode.appendChild(authorityfileidNode);
        }
        if (StringUtils.isNotBlank(thePerson.getAuthorityURI())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AUTHORITYURI_STRING;
            Node authorityfileidNode = createNode(theXQuery, createdNode, theDocument, true);
            Node authorityfileidvalueNode = theDocument.createTextNode(thePerson.getAuthorityURI());
            authorityfileidNode.appendChild(authorityfileidvalueNode);
            createdNode.appendChild(authorityfileidNode);
        }
        if (StringUtils.isNotBlank(thePerson.getAuthorityValue())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AUTHORITYVALUE_STRING;
            Node authorityfileidNode = createNode(theXQuery, createdNode, theDocument, true);
            Node authorityfileidvalueNode = theDocument.createTextNode(thePerson.getAuthorityValue());
            authorityfileidNode.appendChild(authorityfileidvalueNode);
            createdNode.appendChild(authorityfileidNode);
        }

        if (StringUtils.isNotBlank(thePerson.getDisplayname())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_DISPLAYNAME_STRING;
            Node displaynameNode = createNode(theXQuery, createdNode, theDocument, true);
            Node displaynamevalueNode = theDocument.createTextNode(thePerson.getDisplayname());
            displaynameNode.appendChild(displaynamevalueNode);
            createdNode.appendChild(displaynameNode);
        }
        if (StringUtils.isNotBlank(thePerson.getPersontype())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_PERSONTYPE_STRING;
            Node persontypeNode = createNode(theXQuery, createdNode, theDocument, true);
            Node persontypevalueNode = theDocument.createTextNode(thePerson.getPersontype());
            persontypeNode.appendChild(persontypevalueNode);
            createdNode.appendChild(persontypeNode);
        }

        // write additional nameParts
        if (thePerson.getAdditionalNameParts() != null) {
            for (NamePart part : thePerson.getAdditionalNameParts()) {
                if (GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING.equals(part.getType()) && StringUtils.isNotBlank(part.getValue())) {
                    theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_TERMSOFADDRESSVALUE_STRING;
                    Node persontypeNode = createNode(theXQuery, createdNode, theDocument, true);
                    Node persontypevalueNode = theDocument.createTextNode(part.getValue());
                    persontypeNode.appendChild(persontypevalueNode);
                    createdNode.appendChild(persontypeNode);
                } else if (GOOBI_PERSON_DATEVALUE_STRING.equals(part.getType()) && StringUtils.isNotBlank(part.getValue())) {
                    theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_DATEVALUE_STRING;
                    Node persontypeNode = createNode(theXQuery, createdNode, theDocument, true);
                    Node persontypevalueNode = theDocument.createTextNode(part.getValue());
                    persontypeNode.appendChild(persontypevalueNode);
                    createdNode.appendChild(persontypeNode);
                }
            }
        }

        if (!thePerson.getAuthorityUriMap().isEmpty()) {
            for (Entry<String, String> entry : thePerson.getAuthorityUriMap().entrySet()) {
                theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_NAMEIDENTIFIER_STRING + "[@name=" + entry.getKey() + "]";
                Node identifierNode = createNode(theXQuery, createdNode, theDocument, true);
                Node persontypevalueNode = theDocument.createTextNode(entry.getValue());
                identifierNode.appendChild(persontypevalueNode);
                createdNode.appendChild(identifierNode);
            }
        }

        if (thePerson.getType().isAllowAccessRestriction() && thePerson.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }
    }

    protected void writeSingleModsCorporate(String theXQuery, Corporate corp, Node theStartingNode, Document theDocument)
            throws PreferencesException {

        Node createdNode = createNode(theXQuery, theStartingNode, theDocument, true);

        if (StringUtils.isNotBlank(corp.getMainName())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_CORPORATE_MAINNAME_STRING;
            Node node = createNode(theXQuery, createdNode, theDocument, true);
            Node lastnamevalueNode = theDocument.createTextNode(corp.getMainName());
            node.appendChild(lastnamevalueNode);
            createdNode.appendChild(node);
        }
        if (corp.getSubNames() != null) {
            for (NamePart subName : corp.getSubNames()) {
                theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_CORPORATE_SUBNAME_STRING;
                Node node = createNode(theXQuery, createdNode, theDocument, true);
                Node lastnamevalueNode = theDocument.createTextNode(subName.getValue());
                node.appendChild(lastnamevalueNode);
                createdNode.appendChild(node);
            }
        }

        if (StringUtils.isNotBlank(corp.getPartName())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_CORPORATE_PARTNAME_STRING;
            Node node = createNode(theXQuery, createdNode, theDocument, true);
            Node lastnamevalueNode = theDocument.createTextNode(corp.getPartName());
            node.appendChild(lastnamevalueNode);
            createdNode.appendChild(node);
        }

        if (StringUtils.isNotBlank(corp.getAuthorityID())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AUTHORITYID_STRING;
            Node authorityfileidNode = createNode(theXQuery, createdNode, theDocument, true);
            Node authorityfileidvalueNode = theDocument.createTextNode(corp.getAuthorityID());
            authorityfileidNode.appendChild(authorityfileidvalueNode);
            createdNode.appendChild(authorityfileidNode);
        }
        if (StringUtils.isNotBlank(corp.getAuthorityURI())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AUTHORITYURI_STRING;
            Node authorityfileidNode = createNode(theXQuery, createdNode, theDocument, true);
            Node authorityfileidvalueNode = theDocument.createTextNode(corp.getAuthorityURI());
            authorityfileidNode.appendChild(authorityfileidvalueNode);
            createdNode.appendChild(authorityfileidNode);
        }
        if (StringUtils.isNotBlank(corp.getAuthorityValue())) {
            theXQuery = "./" + this.goobiNamespacePrefix + ":" + GOOBI_PERSON_AUTHORITYVALUE_STRING;
            Node authorityfileidNode = createNode(theXQuery, createdNode, theDocument, true);
            Node authorityfileidvalueNode = theDocument.createTextNode(corp.getAuthorityValue());
            authorityfileidNode.appendChild(authorityfileidvalueNode);
            createdNode.appendChild(authorityfileidNode);
        }

        if (corp.getType().isAllowAccessRestriction() && corp.isAccessRestrict()) {
            ((Element) createdNode).setAttribute("accessRestrict", "true");
        }
    }

    protected void writeSingleModsGroup(String theXQuery, MetadataGroup theGroup, Node theStartingNode, Document theDocument, boolean checkParent)
            throws PreferencesException {

        Node createdNode = createNode(theXQuery, theStartingNode, theDocument, checkParent);

        for (Metadata md : theGroup.getMetadataList()) {
            if (!md.getType().getIsPerson()) {
                String xquery = "./#" + this.goobiNamespacePrefix + ":metadata[@name='" + md.getType().getName() + "']";
                writeSingleModsMetadata(xquery, md, createdNode, theDocument);

            }
        }
        for (Person p : theGroup.getPersonList()) {
            if (p != null && p.getRole() != null && !"".equals(p.getRole())
                    && (p.getFirstname() != null || p.getLastname() != null || p.getDisplayname() != null)) {
                String xquery = "./#" + this.goobiNamespacePrefix + ":metadata[@type='person'][@name='" + p.getRole() + "']";
                writeSingleModsPerson(xquery, p, createdNode, theDocument);
            }
        }

        for (Corporate corp : theGroup.getCorporateList()) {
            if (corp != null && StringUtils.isNotBlank(corp.getRole()) && StringUtils.isNotBlank(corp.getMainName())) {
                String xquery = "./#" + this.goobiNamespacePrefix + ":metadata[@type='corporate'][@name='" + corp.getRole() + "']";
                writeSingleModsCorporate(xquery, corp, createdNode, theDocument);
            }
        }
        for (MetadataGroup m : theGroup.getAllMetadataGroups()) {
            String xquery = "./#" + this.goobiNamespacePrefix + ":metadata[@type='group'][@name='" + m.getType().getName() + "']";
            writeSingleModsGroup(xquery, m, createdNode, theDocument, false);
        }
    }

    /***************************************************************************
     * <p>
     * Write a single <code>smLink</code> element in DOM tree; If logical and/or physical DocStruct instances don't have an identifier, a new one is
     * created.
     * </p>
     * 
     * @param parentNode Node in DOM tree
     * @param currentLogStruct DocStruct element of current logical structure entity
     * @return true, if write statement was successful
     **************************************************************************/
    private boolean writeSingleSMLink(org.w3c.dom.Node parentNode, DocStruct currentLogStruct) {

        // Identifier of physical struct.
        String idphys = null;

        Document domDoc = parentNode.getOwnerDocument();

        // Get all references from currentStruct to other Structs.
        List<Reference> refs = currentLogStruct.getAllReferences(METS_TO_STRING);

        // Get all children and write their divs.
        List<DocStruct> allChildren = currentLogStruct.getAllChildren();

        // Iterate over all references and set to- and from- links.
        for (Reference ref : refs) {
            Element smlink = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_SMLINK_STRING);
            parentNode.appendChild(smlink);

            String idlog = currentLogStruct.getIdentifier();
            if (idlog == null) {
                // No identifier available for the logical struct.
                idlog = LOG_PREFIX + Integer.toString(this.divlogidMax);
                this.divlogidMax++;
                currentLogStruct.setIdentifier(idlog);
            }
            createDomAttributeNS(smlink, this.xlinkNamespacePrefix, METS_FROM_STRING, idlog);
            String refType = ref.getType();

            if (LOGICAL_PHYSICAL_MAPPING_TYPE_STRING.equals(refType)) {
                // It's a reference to a physical structure entity.
                DocStruct dsphys = ref.getTarget();
                if (dsphys.getIdentifier() == null) {
                    // No identifier available, we have to create one.
                    idphys = "phys" + Integer.toString(this.divphysidMax);
                    dsphys.setIdentifier(idphys);
                    this.divphysidMax++;
                } else {
                    idphys = dsphys.getIdentifier();
                }
                createDomAttributeNS(smlink, this.xlinkNamespacePrefix, METS_TO_STRING, idphys);
            } else {
                log.warn("Unknown reference type '" + refType + "'");
                return false;
            }
        }

        // Handle all children of this logical docStruct.
        //
        if (allChildren != null) {
            for (DocStruct child : allChildren) {
                if (!writeSingleSMLink(parentNode, child)) {
                    // Error occured while writing div for child.
                    return false;
                }
            }
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Writes the <code>structLink</code> section.
     * </p>
     * 
     * @param parentNode Node in DOM tree
     * @return DOM-Element representing &lt;structLink>
     **************************************************************************/
    private Element writeSMLinks(org.w3c.dom.Node parentNode) {

        Document domDoc = parentNode.getOwnerDocument();
        Element structLink = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_STRUCTLINK_STRING);

        // Get digitalDocument and the logical and physical structure.
        DocStruct logStruct = this.digdoc.getLogicalDocStruct();

        if (logStruct == null) {
            log.warn("DigitalDocument has no logical document structure");
            return null;
        }

        // PLEASE NOTE: It would suffice here, to only link from the overall
        // logical structure to the overall physical structure, IF all
        // references were checked! For simplicity we are referencing each
        // single link!

        // Write a single smLink element, if no links and no children are
        // existing, just to get a valid METS document.
        List<Reference> refs = logStruct.getAllReferences(METS_TO_STRING);
        List<DocStruct> allChildren = logStruct.getAllChildren();
        if ((refs == null || refs.isEmpty()) && (allChildren == null || allChildren.isEmpty())) {
            Element smlink = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_SMLINK_STRING);
            structLink.appendChild(smlink);

            createDomAttributeNS(smlink, this.xlinkNamespacePrefix, METS_FROM_STRING, "");
            createDomAttributeNS(smlink, this.xlinkNamespacePrefix, METS_TO_STRING, "");

            log.debug("No refs existing in DigitalDocument, added empty smLink to get a valid METS document");
        } else {
            writeSingleSMLink(structLink, logStruct);
        }

        return structLink;
    }

    /***************************************************************************
     * <p>
     * Write the descriptive metadata for physical structure entities.
     * </p>
     * 
     * @param parentNode
     * @param divElement
     * @param inStruct
     * @return The internal number of the descriptive metadata section.
     * @throws PreferencesException
     **************************************************************************/
    protected int writePhysDmd(org.w3c.dom.Node parentNode, Element divElement, DocStruct inStruct) throws PreferencesException {

        Document domDoc = parentNode.getOwnerDocument();

        // Check, if there is something to write.
        List<Metadata> allMDs = inStruct.getAllMetadata();
        if ((allMDs == null) || (allMDs.isEmpty())) {
            // Nothing to write.
            return -1;
        }

        // Get new ID for metadata.
        int dmdid = this.dmdidPhysMax;
        this.dmdidPhysMax++;

        // Write metadata header.
        Element dmdsec = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_DMDSEC_STRING);
        Element dommodsnode = createModsMetadataHeader(DMDPHYS_PREFIX, dmdid, dmdsec, domDoc);

        // Write metadata MODS section.
        writePhysModsSection(inStruct, dommodsnode, domDoc, divElement);

        // Add dmdsec node before the parentNode, but only if metadata had
        // been written.
        if (dommodsnode.getChildNodes().getLength() > 0) {
            parentNode.insertBefore(dmdsec, this.firstDivNode);
            return dmdid;
        }

        return -1;
    }

    /***************************************************************************
     * <p>
     * The physical MODS section for internal METS storing.
     * </p>
     * 
     * @param inStruct
     * @param dommodsnode
     * @param domDoc
     * @param divElement
     * @throws PreferencesException
     **************************************************************************/
    protected void writePhysModsSection(DocStruct inStruct, Node dommodsnode, Document domDoc, Element divElement) throws PreferencesException {

        // Go through all the current metadata and write them to
        // <mods:extension><goobi:goobi><goobi:metadata>.
        if (inStruct.getAllMetadata() != null) {
            for (Metadata m : inStruct.getAllMetadata()) {
                if (m.getValue() != null && !"".equals(m.getValue())) {

                    // Write physical page number into div.
                    if (METADATA_PHYSICAL_PAGE_NUMBER.equals(m.getType().getName())) {
                        divElement.setAttribute(METS_ORDER_STRING, m.getValue());
                    }
                    // Write logical page number into div.
                    else if (METADATA_LOGICAL_PAGE_NUMBER.equals(m.getType().getName())) {
                        divElement.setAttribute(METS_ORDERLABEL_STRING, m.getValue());
                    }
                    // Write other metadata into MODS the section.
                    else {
                        String xquery =
                                "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                                        + ":goobi/#" + this.goobiNamespacePrefix + ":metadata[@name='" + m.getType().getName() + "']";
                        writeSingleModsMetadata(xquery, m, dommodsnode, domDoc);
                    }
                }
            }
        }

        if (inStruct.getAllMetadataGroups() != null) {
            for (MetadataGroup m : inStruct.getAllMetadataGroups()) {
                // check if group has values
                boolean isEmpty = true;
                for (Metadata md : m.getMetadataList()) {
                    if (StringUtils.isNotBlank(md.getValue())) {
                        isEmpty = false;
                        break;
                    }
                }
                // only write groups with values

                if (!isEmpty) {
                    String xquery = "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                            + ":goobi/#" + this.goobiNamespacePrefix + ":metadata[@type='group'][@name='" + m.getType().getName() + "']";
                    writeSingleModsGroup(xquery, m, dommodsnode, domDoc, true);
                }

            }

        }
        // Write all persons.
        writeDmdPersons(inStruct, dommodsnode, domDoc);
    }

    /**************************************************************************
     * <p>
     * Go through all the current persons and write them to <mods:extension><goobi:goobi><goobi:persons>.
     * 
     * NOTE Write a person only if a role is existing and firstname OR lastname is not null.
     * </p>
     * 
     * @param inStruct
     * @param domModsNode
     * @param domDoc
     * @throws PreferencesException
     **************************************************************************/
    private void writeDmdPersons(DocStruct inStruct, Node domModsNode, Document domDoc) throws PreferencesException {

        if (inStruct.getAllPersons() != null) {
            for (Person p : inStruct.getAllPersons()) {
                if (p != null && StringUtils.isNotBlank(p.getRole())
                        && (StringUtils.isNotBlank(p.getFirstname()) || StringUtils.isNotBlank(p.getLastname()))) {
                    String xquery = "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                            + ":goobi/#" + this.goobiNamespacePrefix + ":metadata[@type='person'][@name='" + p.getRole() + "']";
                    writeSingleModsPerson(xquery, p, domModsNode, domDoc);
                }
            }
        }
    }

    private void writeDmdCorporates(DocStruct inStruct, Node domModsNode, Document domDoc) throws PreferencesException {
        if (inStruct.getAllCorporates() != null) {
            for (Corporate corp : inStruct.getAllCorporates()) {
                if (corp != null && StringUtils.isNotBlank(corp.getRole()) && StringUtils.isNotBlank(corp.getMainName())) {
                    String xquery = "./" + this.modsNamespacePrefix + ":mods/" + this.modsNamespacePrefix + ":extension/" + this.goobiNamespacePrefix
                            + ":goobi/#" + this.goobiNamespacePrefix + ":metadata[@type='corporate'][@name='" + corp.getRole() + "']";
                    writeSingleModsCorporate(xquery, corp, domModsNode, domDoc);

                }
            }
        }
    }

    /***************************************************************************
     * <p>
     * Writes the AMD section.
     * </p>
     * 
     * @param theDomDoc
     * @param isAnchorFile
     **************************************************************************/
    protected void writeAmdSec(Document theDomDoc, boolean isAnchorFile) {
        List<Md> techMdList = this.digdoc.getTechMds();
        Element amdSec = createDomElementNS(theDomDoc, this.metsNamespacePrefix, METS_AMDSEC_STRING);
        AmdSec amd = this.digdoc.getAmdSec();
        if (amd != null) {
            if (StringUtils.isBlank(amd.getId())) {
                amd.setId(AMD_PREFIX);
            }
            amdSec.setAttribute(METS_ID_STRING, amd.getId());
        } else {
            amdSec.setAttribute(METS_ID_STRING, AMD_PREFIX);
        }
        if (techMdList != null && !techMdList.isEmpty()) {
            for (Md md : techMdList) {
                this.techidMax++;
                Node theNode = theDomDoc.importNode(md.getContent(), true);

                if (MdType.getType(theNode.getLocalName()) != null) {
                    // wrapper element exists, add it directly
                    amdSec.appendChild(theNode);
                    ((Element) theNode).setAttribute(METS_ID_STRING, md.getId());
                } else {
                    // create wrapper element
                    Node child = theNode.getFirstChild();
                    Element techMd = createDomElementNS(theDomDoc, this.metsNamespacePrefix, md.getType().toString());
                    techMd.setAttribute(METS_ID_STRING, md.getId());
                    Element techNode = createDomElementNS(theDomDoc, this.metsNamespacePrefix, METS_MDWRAP_STRING);
                    for (int i = 0; i < theNode.getAttributes().getLength(); i++) {
                        Node attribute = theNode.getAttributes().item(i);
                        techNode.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
                    }

                    techNode.appendChild(child);
                    techMd.appendChild(techNode);
                    amdSec.appendChild(techMd);
                }
            }
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
            } else {
                this.metsNode.appendChild(amdSec);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Reads the internal METS metadata prefs.
     * </p>
     * 
     * @param childnode
     * @throws PreferencesException
     **************************************************************************/
    protected void readMetadataPrefs(Node childnode) throws PreferencesException {
        // No metadataPrefs to read here in the internal METS file.
    }

    protected void readMetadataGroupPrefs(Node inNode) throws PreferencesException {
        // No metadataPrefs to read here in the internal METS file.
    }

    /***************************************************************************
     * <p>
     * Reads the DocStruct settings from the preferences fie.
     * </p>
     * 
     * @param inNode
     * @throws PreferencesException
     **************************************************************************/
    protected void readDocStructPrefs(Node inNode) throws PreferencesException {
        // No docStructPrefs to read here in the internal METS file.
    }

    /***************************************************************************
     * <p>
     * Serializes the METS DOM document.
     * </p>
     * 
     * @param domDoc
     * @param xmlFile
     * @throws IOException
     **************************************************************************/
    protected synchronized void serializeMets(Document domDoc, FileOutputStream xmlFile) throws IOException {

        MetsDocument metsBean = null;
        XmlOptions opts = new XmlOptions();

        // Escape all closing ">"s to &gt;.
        XmlOptionCharEscapeMap charEsc = new XmlOptionCharEscapeMap();
        try {
            charEsc.addMapping('>', XmlOptionCharEscapeMap.PREDEF_ENTITY);
            charEsc.addMapping('"', XmlOptionCharEscapeMap.PREDEF_ENTITY);
        } catch (XmlException e) {
            // No error should occur, unless the above code is correct!
            e.printStackTrace();
        }
        opts.setSaveSubstituteCharacters(charEsc);
        opts.setSavePrettyPrint().setSavePrettyPrintIndent(3);
        opts.setSaveAggressiveNamespaces();
        Map<String, String> suggestedPrefixes = new HashMap<>();
        for (Entry<String, Namespace> abbrev : namespaces.entrySet()) {
            suggestedPrefixes.put(abbrev.getValue().getUri(), abbrev.getKey());
        }

        opts.setSaveSuggestedPrefixes(suggestedPrefixes);
        try {
            metsBean = MetsDocument.Factory.parse(domDoc, opts);
        } catch (XmlException e) {
            String message = "METS file could not be parsed for storing!";
            log.error(message, e);
            throw new IOException(message + "! System message: " + e.getMessage());
        }

        metsBean.save(xmlFile, opts);

        // Close METS file.
        xmlFile.close();
    }

    /***************************************************************************
     * <p>
     * Creates a DOM element and sets its prefix.
     * </p>
     * 
     * @param theDocument
     * @param thePrefix
     * @param theElement
     * @return
     **************************************************************************/
    protected Element createDomElementNS(Document theDocument, String thePrefix, String theElement) {

        Element result;

        // Create the needed element.
        result = theDocument.createElementNS(this.namespaces.get(thePrefix).getUri(), theElement);
        // Set the element's namespace prefix.
        result.setPrefix(thePrefix);

        return result;
    }

    /***************************************************************************
     * <p>
     * Creates a DOM attribute and sets its prefix.
     * </p>
     * 
     * @param theElement
     * @param thePrefix
     * @param theName
     * @param theValue
     **************************************************************************/
    protected void createDomAttributeNS(Element theElement, String thePrefix, String theName, String theValue) {

        // Create the needed attribute.
        theElement.setAttributeNS(this.namespaces.get(thePrefix).getUri(), theName, theValue);
        // Set the attribute's namespace prefix.
        theElement.getAttributeNode(theName).setPrefix(thePrefix);
    }

    /***************************************************************************
     * <p>
     * Set namespaces: METS, MODS, XLINK, XSI, GOOBI and DFG-VIEWER namespaces set by default here. Only change the prefixes in the prefs.
     * </p>
     **************************************************************************/
    protected void setNamespaces() {
        // METS namespace.
        Namespace mets = new Namespace();
        mets.setPrefix(DEFAULT_METS_PREFIX);
        mets.setUri(DEFAULT_METS_URI);
        mets.setSchemalocation(DEFAULT_METS_SCHEMA_LOCATION);
        this.namespaces.put(mets.getPrefix(), mets);
        this.metsNamespacePrefix = mets.getPrefix();

        // MODS namespcae.
        Namespace mods = new Namespace();
        mods.setPrefix(DEFAULT_MODS_PREFIX);
        mods.setUri(DEFAULT_MODS_URI);
        mods.setSchemalocation(DEFAULT_SCHEMA_LOCATION);
        this.namespaces.put(mods.getPrefix(), mods);
        this.modsNamespacePrefix = mods.getPrefix();
        // Handle namespace declarations.
        this.namespaceDeclarations.put(mods.getPrefix(), "declare namespace " + mods.getPrefix() + "='" + mods.getUri() + "';");

        // MIX namespace
        Namespace mix = new Namespace();
        mix.setPrefix(DEFAULT_MIX_PREFIX);
        mix.setUri(DEFAULT_MIX_URI);
        mix.setSchemalocation(DEFAULT_MIX_SCHEMA_LOCATION);
        this.namespaces.put(mix.getPrefix(), mix);
        this.mixNamespacePrefix = mix.getPrefix();
        this.namespaceDeclarations.put(mix.getPrefix(), "declare namespace " + mix.getPrefix() + "='" + mix.getUri() + "';");

        // premis namespace
        Namespace premis = new Namespace();
        premis.setPrefix(DEFAULT_PREMIS_PREFIX);
        premis.setUri(DEFAULT_PREMIS_URI);
        premis.setSchemalocation(DEFAULT_PREMIS_SCHEMA_LOCATION);
        this.namespaces.put(premis.getPrefix(), premis);
        this.mixNamespacePrefix = premis.getPrefix();
        this.namespaceDeclarations.put(premis.getPrefix(), "declare namespace " + premis.getPrefix() + "='" + premis.getUri() + "';");

        // Goobi namespace.
        Namespace goobi = new Namespace();
        goobi.setPrefix(DEFAULT_GOOBI_PREFIX);
        goobi.setUri(DEFAULT_GOOBI_URI);
        if (!DEFAULT_GOOBI_SCHEMA_LOCATION.equals("")) {
            goobi.setSchemalocation(DEFAULT_GOOBI_SCHEMA_LOCATION);
        }
        this.namespaces.put(goobi.getPrefix(), goobi);
        this.goobiNamespacePrefix = goobi.getPrefix();
        // Handle namespace declarations.
        this.namespaceDeclarations.put(goobi.getPrefix(), "declare namespace " + goobi.getPrefix() + "='" + goobi.getUri() + "';");

        // DFG-Viewer namespace.
        Namespace dv = new Namespace();
        dv.setPrefix(DEFAULT_DV_PREFIX);
        dv.setUri(DEFAULT_DV_URI);
        if (!DEFAULT_DV_SCHEMA_LOCATION.equals("")) {
            dv.setSchemalocation(DEFAULT_DV_SCHEMA_LOCATION);
        }
        this.namespaces.put(dv.getPrefix(), dv);
        this.dvNamespacePrefix = dv.getPrefix();

        // XLink namespace.
        Namespace xlink = new Namespace();
        xlink.setPrefix(DEFAULT_XLINK_PREFIX);
        xlink.setUri(DEFAULT_XLINK_URI);
        if (!DEFAULT_XLINK_SCHEMA_LOCATION.equals("")) {
            xlink.setSchemalocation(DEFAULT_XLINK_SCHEMA_LOCATION);
        }
        this.namespaces.put(xlink.getPrefix(), xlink);
        this.xlinkNamespacePrefix = xlink.getPrefix();

        Namespace xml = new Namespace();
        xml.setPrefix("xml");
        xml.setUri("http://www.w3.org/XML/1998/namespace");

        this.namespaces.put(xml.getPrefix(), xml);
        this.xmlNamespacePrefix = xml.getPrefix();

        // XSI namespace.
        Namespace xsi = new Namespace();
        xsi.setPrefix(DEFAULT_XSI_PREFIX);
        xsi.setUri(DEFAULT_XSI_URI);
        if (!DEFAULT_XSI_SCHEMA_LOCATION.equals("")) {
            xsi.setSchemalocation(DEFAULT_XSI_SCHEMA_LOCATION);
        }
        this.namespaces.put(xsi.getPrefix(), xsi);
        this.xsiNamespacePrefix = xsi.getPrefix();

        // extension
        Namespace metsExtension = new Namespace();
        metsExtension.setPrefix("intranda");
        metsExtension.setUri("http://intranda.com/MODS/");

        this.namespaces.put(metsExtension.getPrefix(), metsExtension);

    }

    /**
     * Gets a map of all attributes of this node, with their respective values. attributes are expected to follow the pattern [@attribute='value']
     * 
     * @param nodeName
     * @return
     */
    HashMap<String, String> getAttributesFromNode(String nodeName) {

        HashMap<String, String> attributes = new HashMap<>();

        Pattern p = Pattern.compile("\\[[^\\]]+\\]");
        Matcher m = p.matcher(nodeName);
        while (m.find()) {
            String group = m.group();
            int indexAt = group.indexOf("@");
            int indexEq = group.indexOf("=");
            if (indexAt == -1 || indexEq == -1 || indexAt > indexEq) {
                //pattern does not match an attribute pattern
                continue;
            }
            String atrName = group.substring(indexAt + 1, indexEq);
            String atrValue = group.substring(indexEq + 1, group.length() - 1).replace("'", "");
            attributes.put(atrName, atrValue);
        }
        return attributes;
    }

    /***************************************************************************
     * GETTERS AND SETTERS
     **************************************************************************/

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMptrUrl() {
        return this.mptrUrl;
    }

    /***************************************************************************
     * @param mptrUrl
     **************************************************************************/
    public void setMptrUrl(String mptrUrl) {
        this.mptrUrl = mptrUrl;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMptrAnchorUrl() {
        return this.mptrUrlAnchor;
    }

    /***************************************************************************
     * @param mptrUrl
     **************************************************************************/
    public void setMptrAnchorUrl(String mptrAnchorUrl) {
        this.mptrUrlAnchor = mptrAnchorUrl;
    }

    public String getGoobiID() {
        return goobiID;
    }

    @Override
    public void setGoobiID(String goobiID) {
        this.goobiID = goobiID;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public static String getVersion() {
        return version;
    }

    public void setWriteLocal(boolean writeLocal) {
        this.writeLocalFilegroup = writeLocal;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isExportable() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "Mets";
    }

    @Override
    public void setPrefs(Prefs prefs) throws PreferencesException {
        setNamespaces();
        this.myPreferences = prefs;

        log.info(this.getClass().getName() + " " + getVersion());

        // Read preferences.
        Node prefsMetsNode = prefs.getPreferenceNode(METS_PREFS_NODE_NAME_STRING);
        if (prefsMetsNode == null) {
            String message = "Can't read preferences for METS fileformat!";
            PreferencesException pe = new PreferencesException("Node '" + METS_PREFS_NODE_NAME_STRING + "' in preferences file not found!");
            log.error(message, pe);
            throw pe;
        }

        readPrefs(prefsMetsNode);
    }

    public List<MatchingMetadataObject> getModsNamesMD() {
        return modsNamesMD;
    }

    public List<MatchingDocStructObject> getModsNamesDS() {
        return modsNamesDS;
    }

    @Override
    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    @Override
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    @Override
    public void setInstanceName(String instance) {
        instanceName = instance;
    }

    @Override
    public void setClientName(String client) {
        clientName = client;
    }
}
