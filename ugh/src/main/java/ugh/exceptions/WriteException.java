package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / WriteException.java
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

/*******************************************************************************
 * @author Markus Enders
 * @version 2008-09-29
 ******************************************************************************/

public class WriteException extends UGHException {

    private static final long serialVersionUID = -2295413163869354489L;

    /***************************************************************************
     * Constructor.
     **************************************************************************/
    public WriteException() {
        super();
    }

    /***************************************************************************
     * @param inReason
     **************************************************************************/
    public WriteException(String inReason) {
        super(inReason);
    }

    /***************************************************************************
     * @param e
     **************************************************************************/
    public WriteException(Exception e) {
        super(e);
    }

    /***************************************************************************
     * @param inReason
     * @param e
     **************************************************************************/
    public WriteException(String inReason, Exception e) {
        super(inReason, e);
    }

}
