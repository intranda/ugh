package ugh.dl;

/*******************************************************************************
 * ugh.dl / MD.java
 * 
 * Copyright 2012 intranda GmbH, Göttingen
 * 
 * http://www.intranda.com
 * http://www.digiverso.com
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
import java.io.Serializable;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.extern.log4j.Log4j2;
import ugh.fileformats.mets.MetsMods;

@Log4j2
public class Md implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6784880447020540980L;
    private transient Node content;
    private String id;
    private MdType type;

    public Md(Node content, MdType type) {
        this.content = content;
        this.type = type;
        checkWrapperElement();
    }

    public Md(org.jdom2.Element content, MdType type) throws JDOMException {
        // convert jdom element into Node
        DOMOutputter outputter = new DOMOutputter();
        this.content = outputter.output(content);
        this.type = type;
        checkWrapperElement();
    }

    public Node getContent() {
        return content;
    }

    @JsonIgnore
    public void setContent(Node content) {
        this.content = content;
        checkWrapperElement();
    }

    public void setContent(org.jdom2.Element content) throws JDOMException {
        DOMOutputter outputter = new DOMOutputter();
        this.content = outputter.output(content);
        checkWrapperElement();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MdType getType() {
        return type;
    }

    /**
     * check, if node starts with wrapper elements <mets:techMD><mets:mdWrap><mets:xmlData> or with the content itself
     *
     * generate wrapper, if needed
     */
    private void checkWrapperElement() {
        if (content != null && !"techMD".equals(content.getLocalName())) {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.newDocument();
                // create wrapper elements
                Element techMd = doc.createElementNS(MetsMods.DEFAULT_METS_URI, "techMD");
                techMd.setPrefix(MetsMods.DEFAULT_METS_PREFIX);
                Element mdWrap = doc.createElementNS(MetsMods.DEFAULT_METS_URI, "mdWrap");
                mdWrap.setPrefix(MetsMods.DEFAULT_METS_PREFIX);
                mdWrap.setAttribute("MDTYPE", "OTHER");
                mdWrap.setAttribute("MIMETYPE", "text/xml");
                techMd.appendChild(mdWrap);
                Element xmlData = doc.createElementNS(MetsMods.DEFAULT_METS_URI, "xmlData");
                xmlData.setPrefix(MetsMods.DEFAULT_METS_PREFIX);
                mdWrap.appendChild(xmlData);

                // add content to wrapper elements
                xmlData.appendChild(doc.importNode(content, true));
                content = techMd;
            } catch (ParserConfigurationException e) {
                log.error(e);
            }
        }
    }

    public void generateId() {
        if (StringUtils.isBlank(id)) {
            id = "AMD_" + UUID.randomUUID().toString();
        }
    }

    public enum MdType {
        TECH_MD("techMD"),
        RIGHTS_MD("rightsMD"),
        DIGIPROV_MD("digiprovMD"),
        SOURCE_MD("sourceMD");

        private String type;

        private MdType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static MdType getType(String type) {
            for (MdType v : values()) {
                if (v.toString().equals(type)) {
                    return v;
                }
            }
            return null;
        }

    }
}
