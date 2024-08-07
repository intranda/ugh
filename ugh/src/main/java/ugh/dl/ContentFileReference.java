package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFileReference.java
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

/*******************************************************************************
 * <p>
 * A ContentFileReference stores a single reference from a DocStruct to a ContentFile. This reference can contain additional information as the an
 * area information An Area defines a special part of the ContentFile to which it is linked to. An Area is defined in a <code>ContentFileArea</code>
 * object.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-12-09
 * @see ContentFileArea
 ******************************************************************************/

public class ContentFileReference implements Serializable {

    private static final long serialVersionUID = 3878365395668660681L;

    // Contentfile Area.
    @Getter
    @Setter
    private ContentFileArea cfa = null;
    // ContentFile object.
    @Getter
    @Setter
    private ContentFile cf = null;

}
