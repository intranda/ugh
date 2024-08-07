package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFileArea.java
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

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.ContentFileAreaTypeUnknownException;

/*******************************************************************************
 * <p>
 * A ContentFileArea object defines an area inside a ContentFile. Depending on the type of ContentFile, there may be several options to define such an
 * area.
 * <ul>
 * <li>coordinates, e.g. when using images the from field contains the coordinates of the upper left corner of an area, the to field contains the
 * lower right corner of the area.
 * <li>xmlid, if pointing into an XML file, id values (stored in xml-identifier in the Contentfile) can be used to point to a part of a content file.
 * <li>smtpe-codes; time codes pointing to the beginning and end of a streaming media part (as video or audio)
 * <li>
 * </ul>
 * byte-offset: If an area just contains the lowest entity, which can be adressed in the content file (e.g. a single pixel, a single second, a single
 * xml-element), the from and to fields must contain the same values.
 * </p>
 * 
 * @author Markus Enders
 * @version 2010-02-13
 * 
 *          CHANGELOG
 * 
 *          13.02.2010 --- Funk --- Minor changes.
 * 
 *          17.11.2009 --- Funk --- Refactored some things for Sonar improvement. --- Removed modifier "transient" from LOGGER.
 * 
 ******************************************************************************/

@Log4j2
public class ContentFileArea implements Serializable {

    private static final long serialVersionUID = 3957147069912977429L;

    // Type of area (coordinates, xml id, byteoffset, ...).
    @Getter
    private String type;
    // From attribute; e.g. can be xml id, SMTPE code etc.
    @Getter
    @Setter
    private String from;
    // To attribute; same as from.
    @Getter
    @Setter
    private String to;

    /***************************************************************************
     * <p>
     * Sets the type of reference. The following types are known:
     * <ul>
     * <li>byteoffset</li>
     * <li>coordinates</li>
     * <li>smtpe</li>
     * <li>xmlid Types are case sensitive. If an unknown type is set, an exception is thrown.</li>
     * </ul>
     * </p>
     * 
     * @param type As a String.
     * @throws ContentFileAreaTypeUnknownException
     **************************************************************************/
    public void setType(String type) throws ContentFileAreaTypeUnknownException {

        if ("coordinates".equals(type) || "byteoffset".equals(type) || "xmlid".equals(type) || "smtpe".equals(type)) {
            this.type = type;
        } else {
            String message = "'" + type + "' is unknown for ContentFileArea type";
            log.error(message);
            throw new ContentFileAreaTypeUnknownException(message);
        }
    }
}
