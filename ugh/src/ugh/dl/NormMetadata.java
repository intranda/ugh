package ugh.dl;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class NormMetadata extends Metadata {

	private static final long serialVersionUID = -7641352011647275840L;

	private String dataBase;
	private String identifier;
	
	
	public NormMetadata (MetadataType theType)  throws MetadataTypeNotAllowedException {
		super(theType);
	}


	/**
	 * @param dataBase the dataBase to set
	 */
	public void setDataBase(String dataBase) {
		this.dataBase = dataBase;
	}


	/**
	 * @return the dataBase
	 */
	public String getDataBase() {
		return dataBase;
	}


	/**
	 * @param id the id to set
	 */
	public void setIdentifier(String id) {
		this.identifier = id;
	}


	/**
	 * @return the id
	 */
	public String getIdentifier() {
		return identifier;
	}
	
}
