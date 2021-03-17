package ugh.dl;

import java.util.List;

import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;

public interface HoldingElement {

    public String getIdentifier();

    public void setIdentifier(String identifier);

    /***************************************************************************
     * <p>
     * Adds a metadata object to this instance; The method checks, if it is allowed to add one (based on the configuration). If so, the object is
     * added and returns true; otherwise it returns false.
     * </p>
     * <p>
     * The Metadata object must already include all necessary information as MetadataType and value.
     * </p>
     * <p>
     * For internal reasons this method changes the MetadataType object against a local copy, which is retrieved from the appropriate DocStructType of
     * this DocStruct instance. The internal name of both MetadataType objects must be identical. If a local copy cannot be found (which means, the
     * metadata type is NOT valid for this kind of DocStruct object), false is returned.
     * </p>
     * 
     * @param theMetadata Metadata object to be added.
     * @throws MetadataTypeNotAllowedException If the DocStructType of this DocStruct instance does not allow the MetadataType or if the maximum
     *             number of Metadata (of this type) is already available.
     * @throws DocStructHasNoTypeException If no DocStruct Type is set for the DocStruct object; for this reason the metadata can't be added, because
     *             we cannot check, wether if the metadata type is allowed or not.
     * @see Metadata
     **************************************************************************/
    public void addMetadata(Metadata theMetadata) throws MetadataTypeNotAllowedException, DocStructHasNoTypeException;

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed. By setting the second parameter to true, this behaviour can be influenced. This can
     * be necessary e.g. when programming user interfaces etc.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param theMd Metadata object which should be removed
     * @param force set to true, the Metadata is removed even if it is not allowed to. You can create not validateable documents.
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public void removeMetadata(Metadata theMd, boolean force);

    /***************************************************************************
     * <p>
     * Checks, if Metadata of a special kind can be removed. There is ni special function, to check wether persons can be removed. As the
     * <code>Person</code> object is just inheirited from the <code>Metadata</code> it has a <code>MetadataType</code>. Therefor this method can be
     * used, to check if a person is removable or not.
     * </p>
     * 
     * @see #removeMetadata
     * @see #removePerson
     * @param inMDType MetadataType object
     * @return true, if it can be removed; otherwise false
     **************************************************************************/
    public boolean isMetadataTypeBeRemoved(PrefsType inMDType);

    public void addPerson(Person person) throws MetadataTypeNotAllowedException;

    /***************************************************************************
     * <p>
     * Removes a Person object.
     * </p>
     * 
     * @param in Person object to be removed
     * @param force if set to true, person is removed, even if invalid document is the result
     * @return true, if removed; otherwise false
     * @throws IncompletePersonObjectException if the first parameter is not a complete person object
     **************************************************************************/
    public void removePerson(Person in, boolean force) throws IncompletePersonObjectException;

    public void addCorporate(Corporate corporate) throws MetadataTypeNotAllowedException;

    public void removeCorporate(Corporate in, boolean force) throws IncompletePersonObjectException;

    /***************************************************************************
     * <p>
     * Get all metadatatypes, which can be added to an element. This method considers already added metadata (and persons!); e.g. metadata types which
     * can only be available once cannot be added a second time. Therefore this metadata type will not be included in this list.<br/>
     * 
     * "Internal" metadata, which start with the HIDDEN_METADATA_CHAR, will also not be included.
     * </p>
     * 
     * @return List containing MetadataType objects.
     **************************************************************************/
    public List<MetadataType> getAddableMetadataTypes();

    /***************************************************************************
     * <p>
     * Gets all MetadataGroups for this DocStruct instance.
     * </p>
     * 
     * @return List containing MetadataGroup instances; if no MetadataGroup is available, null is returned.
     **************************************************************************/
    public List<MetadataGroup> getAllMetadataGroups();

    /***************************************************************************
     * <p>
     * Allows to set all MetadataGroup. The MetadataGroup objects are contained in a List. This method sets all MetadataGroup; they are NOT added.
     * MetadataGroup which is already available will be overwritten.
     * </p>
     * 
     * @param inList List containing MetadataGroup objects.
     **************************************************************************/
    public void setAllMetadataGroups(List<MetadataGroup> inList);

    /***************************************************************************
     * <p>
     * Adds a metadata object to this instance; The method checks, if it is allowed to add one (based on the configuration). If so, the object is
     * added and returns true; otherwise it returns false.
     * </p>
     * <p>
     * The Metadata object must already include all necessary information as MetadataType and value.
     * </p>
     * <p>
     * For internal reasons this method changes the MetadataType object against a local copy, which is retrieved from the appropriate DocStructType of
     * this DocStruct instance. The internal name of both MetadataType objects must be identical. If a local copy cannot be found (which means, the
     * metadata type is NOT valid for this kind of DocStruct object), false is returned.
     * </p>
     * 
     * @param theMetadataGroup Metadata object to be added.
     * @return TRUE if metadata was added succesfully, FALSE otherwise.
     * @throws MetadataTypeNotAllowedException If the DocStructType of this DocStruct instance does not allow the MetadataType or if the maximum
     *             number of Metadata (of this type) is already available.
     * @throws DocStructHasNoTypeException If no DocStruct Type is set for the DocStruct object; for this reason the metadata can't be added, because
     *             we cannot check, wether if the metadata type is allowed or not.
     * @see Metadata
     **************************************************************************/
    public boolean addMetadataGroup(MetadataGroup theMetadataGroup) throws MetadataTypeNotAllowedException, DocStructHasNoTypeException;

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed. By setting the second parameter to true, this behaviour can be influenced. This can
     * be necessary e.g. when programming user interfaces etc.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param theMd Metadata object which should be removed
     * @param force set to true, the Metadata is removed even if it is not allowed to. You can create not validateable documents.
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public boolean removeMetadataGroup(MetadataGroup theMd, boolean force);

    /***************************************************************************
     * <p>
     * Exchanges a Metadata object against an old one. Only metadata objects of the same type (of the same MetadataType object) can be exchanged. The
     * Metadata-Type object of the new Metadata object is copied locally (as it is done, when adding metadata).
     * </p>
     * 
     * <p>
     * OLD COMMENT? : exchanges two metadata objects; can be used instead of doing a remove and an add later on. Must be used, if a Metadata object
     * cannot be removed because of DTD (there must always be at least one object). Therefore we can only change Metadata objects of the same
     * MetadataType.
     * </p>
     * 
     * @param theOldMd Metadata object which should be replaced.
     * @param theNewMd New Metadata object.
     * @return True, if Metadata object could be exchanged; otherwise false.
     **************************************************************************/
    public void changeMetadataGroup(MetadataGroup theOldMd, MetadataGroup theNewMd);

    /***************************************************************************
     * <p>
     * Retrieves all Metadata object, which belong to this DocStruct and have a special type. Can be used to get all titles, authors etc... includes
     * Persons!
     * </p>
     * 
     * PLEASE NOTE This method no longer returns NULL, if no MetadataTypes are available! An empty list is returned now!
     * 
     * @param inType MetadataType we are looking for.
     * @return List containing Metadata objects; if no metadata ojects are available, an empty list is returned.
     **************************************************************************/
    public List<MetadataGroup> getAllMetadataGroupsByType(MetadataGroupType inType);

    public PrefsType getType();
}
