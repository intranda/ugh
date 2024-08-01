package ugh.fileformats.mets;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;

import org.junit.Test;

import ugh.dl.DigitalDocument;

public class GetPathFromFileLinkTest {

    String[] links = {
            "file://opt/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "file:/opz/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "file:///opt/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "/opt/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "00000001.jpg",
            "file://C:/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "file:/C:/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "file:///C:/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg",
            "C:/Users/steffen/Desktop/g2g/goobi/../viewer/media/212/00000001.jpg"
    };

    @Test
    public void test() {
        for (String link : links) {
            Path path = MetsMods.getFilePath(link);
            assertEquals("image/jpeg", DigitalDocument.detectMimeType(path));
            assertEquals("00000001.jpg", path.getFileName().toString());
        }
    }

}
