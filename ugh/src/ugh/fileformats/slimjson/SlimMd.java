package ugh.fileformats.slimjson;

import java.io.StringWriter;
import java.util.UUID;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import ugh.dl.Md;

@Data
@Log4j
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
        smd.setType(md.getType());
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

}
