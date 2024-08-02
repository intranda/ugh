package ugh.fileformats.slimjson;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Md;
import ugh.dl.Md.MdType;

@Data
@Log4j2
public class SlimMd {
    private static Transformer transformer = createTransformer();

    private String content;
    private String id;
    private String type;

    public static SlimMd fromMd(Md md) {
        SlimMd smd = new SlimMd();
        if (md.getId() != null) {
            smd.setId(md.getId());
        } else {
            smd.setId(UUID.randomUUID().toString());
            md.setId(smd.id);
        }
        smd.setType(md.getType().toString());
        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(md.getContent()), new StreamResult(writer));
            smd.content = writer.toString();
        } catch (TransformerException e) {
            log.error(e);
        }
        return smd;
    }

    private static Transformer createTransformer() {
        try {
            return TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
            log.error(e);
        }
        return null;
    }

    public Md toMd() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(content)));
            return new Md(doc.getFirstChild(), MdType.getType(type));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.error(e);
        }
        return null;
    }

}
