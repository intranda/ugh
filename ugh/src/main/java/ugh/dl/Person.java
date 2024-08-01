package ugh.dl;

/*******************************************************************************
 * ugh.dl / Person.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ugh.exceptions.MetadataTypeNotAllowedException;

/*******************************************************************************
 * <p>
 * A person is a very special kind of metadata. For this reason it is inheirited from <code>Metadata</code> class.
 * </p>
 * 
 * <p>
 * A person has several metadata and not only a single value. The person's name can be splitted into:
 * </p>
 * <ul>
 * <li>firstname
 * <li>lastname
 * <li>affilication (company they are working at...}
 * <li>a role, when creating or producing a book, article etc.; usually this is a MetadataType.
 * </ul>
 * <p>
 * This class provides methods to store and retrieve these additional information.
 * </p>
 * <p>
 * Most file formats are not able to serialize / store all information from a Person object.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-14
 * @see Metadata
 * 
 *      CHANGELOG
 * 
 *      14.02.2010 --- Funk --- Added method toString().
 * 
 *      30.11.2009 --- Funk --- Again removed deprecated Person() constructor.
 * 
 *      17.11.2009 --- Funk --- Changed method person() to constructor Person(). --- Refactored some things for Sonar improvement.
 * 
 *      10.11.2009 --- Funk --- Removed deprecated Person() constructor.
 * 
 *      06.10.2009 --- Funk --- Adapted metadata and person constructors. Left the old constructors where they were, to not confuse the intranda
 *      peoples :-)
 * 
 *      06.05.2009 --- Wulf Riebensahm --- Method equals() overloaded .
 * 
 ******************************************************************************/

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person extends Metadata {

    private static final long serialVersionUID = -3667880952431707982L;

    private String firstname = null;
    private String lastname = null;
    private String displayname = null;
    private String affiliation = null;
    private String institution = null;
    private String role = null;
    private String persontype = null;

    private List<NamePart> additionalNameParts = null;

    @Deprecated
    public Person() {
        super();
    }

    /***************************************************************************
     * <p>
     * Constructor with a MetadataType is needed.
     * </p>
     * 
     * @param theType
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public Person(MetadataType theType) throws MetadataTypeNotAllowedException {
        super(theType);
        if (!theType.getIsPerson()) {
            throw new MetadataTypeNotAllowedException("To create a Person one needs a MetadataType with isPerson set to be true.");
        }
    }

    /***************************************************************************
     * <p>
     * Creates a person; each person has usually a first and a lastname. For this reason, both must be given in this constructor. If one is not
     * available; simply set one or both parameters to null.
     * </p>
     * 
     * @param in1 firstname of the person
     * @param in2 lastname of the person
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public Person(MetadataType theType, String in1, String in2)
            throws MetadataTypeNotAllowedException {

        super(theType);
        this.firstname = in1;
        this.lastname = in2;
    }

    /***************************************************************************
     * <p>
     * Store the firstname of this person.
     * </p>
     * 
     * @param in
     **************************************************************************/
    public void setFirstname(String in) {
        this.firstname = in;
    }

    /***************************************************************************
     * <p>
     * Retrieves the firstname of this person.
     * </p>
     * 
     * @return firstname of this person
     **************************************************************************/
    public String getFirstname() {
        return this.firstname;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setLastname(String in) {
        this.lastname = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getLastname() {
        return this.lastname;
    }

    /***************************************************************************
     * @param in
     * @return
     **************************************************************************/
    public void setInstitution(String in) {
        this.institution = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getInstitution() {
        return this.institution;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setAffiliation(String in) {
        this.affiliation = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getAffiliation() {
        return this.affiliation;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setRole(String in) {
        this.role = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRole() {
        if (role == null) {
            role = MDType.getName();
        }
        return this.role;
    }

    /***************************************************************************
     * <p>
     * Sets the type of a person. The type of a person, may distinguish between normal/natural persons, juristic persons (as companies) or virtual
     * persons (e.g. as conferences etc...).
     * </p>
     * 
     * @param in
     **************************************************************************/
    public void setPersontype(String in) {
        this.persontype = in;
    }

    /***************************************************************************
     * <p>
     * Gets the type of a person.
     * </p>
     * 
     * @return type of person as String
     **************************************************************************/
    public String getPersontype() {
        return this.persontype;
    }

    /***************************************************************************
     * <p>
     * Gets the displayname of a person. The displayname is the Name, which can be displayed and should contain the right order of lastname, firstname
     * and affiliation of a person.
     * </p>
     * 
     * @return the displayname
     **************************************************************************/
    public String getDisplayname() {
        return this.displayname;
    }

    /***************************************************************************
     * <p>
     * Sets the displayname of a person.
     * </p>
     * 
     * @param displayname the displayname to set
     **************************************************************************/
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        // MetadataType is needed. Furthermore there should be at least one name part available.
        if (this.getType() != null && (StringUtils.isNotBlank(this.getFirstname()) || StringUtils.isNotBlank(this.getLastname()))) {
            // Get person type and value.
            result.append("Person (")
                    .append(this.getType().getName())
                    .append("): ")
                    .append(this.getLastname() == null ? "NULL" : "\""
                            + this.getLastname() + "\"")
                    .append(", ")
                    .append(this.getFirstname() == null ? "NULL" : "\""
                            + this.getFirstname() + "\"")
                    .append("\n");
        } else if (this.getType() == null) {
            // But if the MetadataType is null, then there would be no limits at all on both name parts. Feature OR Bug? - Zehong
            result.append("Person (WITHOUT TYPE!!): ")
                    .append(this.getLastname() == null ? "NULL" : "\""
                            + this.getLastname() + "\"")
                    .append(", ")
                    .append(this.getFirstname() == null ? "NULL" : "\""
                            + this.getFirstname() + "\"")
                    .append("\n");
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(additionalNameParts, affiliation, displayname, firstname, institution, lastname, persontype, role);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Person other = (Person) obj;
        return Objects.equals(additionalNameParts, other.additionalNameParts) && Objects.equals(affiliation, other.affiliation)
                && Objects.equals(displayname, other.displayname) && Objects.equals(firstname, other.firstname)
                && Objects.equals(institution, other.institution) && Objects.equals(lastname, other.lastname)
                && Objects.equals(persontype, other.persontype) && Objects.equals(role, other.role);
    }

    public List<NamePart> getAdditionalNameParts() {
        return additionalNameParts;
    }

    public void setAdditionalNameParts(List<NamePart> additionalNameParts) {
        this.additionalNameParts = new ArrayList<>(additionalNameParts);
    }

    public void addNamePart(NamePart part) {
        if (additionalNameParts == null) {
            additionalNameParts = new ArrayList<>();
        }
        if (part == null) {
            throw new IllegalArgumentException("Cannot add null as NamePart!");
        }
        additionalNameParts.add(part);
    }

}
