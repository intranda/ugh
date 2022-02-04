package ugh.dl;

import java.util.ArrayList;
import java.util.List;

/*******************************************************************************
 * ugh.dl / Corporate.java
 * 
 * Copyright 2020 intranda GmbH, Göttingen
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ugh.exceptions.MetadataTypeNotAllowedException;

/*******************************************************************************
 * <p>
 * A corporate is a special metadata that is different from both normal metadata and persons. In contrast to metadata, it consists of several pieces
 * of information such as name, role, address, identifier.
 * </p>
 * 
 * @author Robert Sehr
 * @see Metadata
 * 
 ******************************************************************************/

@JsonIgnoreProperties(ignoreUnknown = true)

public class Corporate extends Metadata {

    private static final long serialVersionUID = -7415336079178700058L;

    /**
     * Corporate name or jurisdiction name, the main entry
     * <p>
     * MARC: 110, 710 $a
     * </p>
     * <p>
     * Pica: 029A, 029F $a, $8
     * </p>
     */
    @Getter
    @Setter
    private String mainName;

    /**
     * Subordinate unit, additional name parts
     * <p>
     * MARC: 110, 710 $b
     * </p>
     * <p>
     * Pica: 029A, 029F $b
     * </p>
     */
    @Getter
    @Setter
    private List<NamePart> subNames = new ArrayList<>();

    /**
     * Location, dates, numeric entries
     * <p>
     * MARC: 110, 710 $c$d$n
     * </p>
     * <p>
     * Pica: 029A, 029F $c$d$n
     * </p>
     */
    @Getter
    @Setter
    private String partName;

    /**
     * Role of the corporate within the work
     * <p>
     * MARC: 110, 710 $e, $4
     * </p>
     * <p>
     * Pica: 029A, 029F $B, $4
     * </p>
     */
    @Getter
    @Setter
    private String role;

    public Corporate(MetadataType type) throws MetadataTypeNotAllowedException {
        super(type);
        role = type.getName();
    }

    public void addSubName(NamePart name) {
        if (!subNames.contains(name)) {
            subNames.add(name);
        }
    }

    public void removeSubName(NamePart name) {
        if (subNames.contains(name)) {
            subNames.remove(name);
        }
    }
}
