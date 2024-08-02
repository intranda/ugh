package ugh.dl;

/*******************************************************************************
 * ugh.dl / AmdSec.java
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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import lombok.Getter;
import lombok.Setter;

public class AmdSec implements Serializable {

    /**
     * @author Robert Sehr
     */
    private static final long serialVersionUID = -2651069769792564435L;
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private List<Md> techMdList;

    public AmdSec(List<Md> techMdList) {
        super();
        this.techMdList = techMdList;
    }

    public void addTechMd(Md techMd) {
        if (techMdList == null) {
            techMdList = new ArrayList<>();
        }
        this.techMdList.add(techMd);
    }

    public List<Node> getTechMdsAsNodes() {
        List<Node> nodeList = new ArrayList<>();
        if (this.techMdList != null) {
            for (Md techMd : this.techMdList) {
                nodeList.add(techMd.getContent());
            }
        }
        return nodeList;
    }

}
