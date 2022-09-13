package ugh.fileformats.slimjson;

import java.util.List;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import ugh.dl.DigitalDocument;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Data
@Log4j
public class SlimPerson extends SlimMetadata {

    private String firstname = null;
    private String lastname = null;
    private String displayname = null;
    private String affiliation = null;
    private String institution = null;
    private String role = null;
    private String persontype = null;

    private List<NamePart> additionalNameParts = null;
    
    public static SlimPerson fromPerson(Person person, SlimDigitalDocument sdd) {
        SlimPerson sm = new SlimPerson();
        sm = (SlimPerson) SlimMetadata.fromMetadata(sm, person, sdd);
        sm.firstname = person.getFirstname();
        sm.lastname = person.getLastname();
        sm.displayname = person.getDisplayname();
        sm.affiliation = person.getAffiliation();
        sm.institution = person.getAffiliation();
        sm.role = person.getRole();
        sm.persontype = person.getPersontype();
        
        return sm;
    }
    
    public Person toPerson(DigitalDocument dd) {
        try {
            Person sm = new Person(getDigitalDocument().getMetadataTypeMap().get(this.getMdTypeId()));
            sm = (Person) super.toMetadata(sm, dd);
            sm.setFirstname(this.firstname);
            sm.setLastname(this.lastname);
            sm.setDisplayname(this.displayname);
            sm.setAffiliation(this.affiliation);
            sm.setInstitution(this.institution);
            sm.setRole(this.role);
            sm.setPersontype(this.persontype);
            return sm;
        } catch (MetadataTypeNotAllowedException e) {
            log.error(e);
            return null;
        }

    }
}
