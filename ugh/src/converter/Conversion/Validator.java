package converter.Conversion;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.PreferencesException;

public class Validator {

	protected final Logger myLogger = Logger.getLogger(Validator.class);
	
	List<DocStruct> docStructsOhneSeiten;

	boolean autoSave = false;

	public boolean validate(Prefs myPrefs, Fileformat gdzfile, String id) {
		
	    /*
	     * -------------------------------- Fileformat einlesen --------------------------------
	     */
		
		return validate(gdzfile, myPrefs, id);
	}

	public boolean validate(Fileformat gdzfile, Prefs inPrefs, String id) {
		boolean ergebnis = true;

		DigitalDocument dd = null;
		try {
			dd = gdzfile.getDigitalDocument();
		} catch (Exception e) {
			myLogger.error("Can not get DigitalDocument[" + id + "]", e);
			ergebnis = false;
		}


		/*
		 * -------------------------------- auf Docstructs ohne Seiten prüfen
		 * --------------------------------
		 */
		DocStruct logicalTop = dd.getLogicalDocStruct();
		if (logicalTop == null) {
			myLogger.info("[" + id + "] " + "Verifizierung nicht erfolgreich, keine Seiten zugewiesen");
			ergebnis = false;
		}

		docStructsOhneSeiten = new ArrayList<DocStruct>();
		this.checkDocStructsOhneSeiten(logicalTop);
		if (docStructsOhneSeiten.size() != 0) {
			for (Iterator<DocStruct> iter = docStructsOhneSeiten.iterator(); iter.hasNext();) {
				DocStruct ds = (DocStruct) iter.next();
				myLogger.info("[" +id + "] Strukturelement ohne Seiten: " + ds.getType().getName());
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf Seiten ohne Docstructs prüfen
		 * --------------------------------
		 */
		List<String> seitenOhneDocstructs = null;
		try {
			seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
		} catch (PreferencesException e1) {
			myLogger.info("[" +id + "] Can not check pages without docstructs: ");
			ergebnis = false;
		}
		if (seitenOhneDocstructs != null && seitenOhneDocstructs.size() != 0) {
			for (Iterator<String> iter = seitenOhneDocstructs.iterator(); iter.hasNext();) {
				String seite = (String) iter.next();
				myLogger.info("[" + id + "] " + "Seiten ohne Strukturelement: " + seite);
			}
			ergebnis = false;
		}

		/*
		 * -------------------------------- auf mandatory Values der Metadaten
		 * prüfen --------------------------------
		 */
		List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<String>());
		if (mandatoryList.size() != 0) {
			for (Iterator<String> iter = mandatoryList.iterator(); iter.hasNext();) {
				String temp = (String) iter.next();
				myLogger.info("[" + id + "] " + "Pflichtelement: " + temp);
			}
			ergebnis = false;
		}
		return ergebnis;
	}

	private void checkDocStructsOhneSeiten(DocStruct inStruct) {
		if (inStruct.getAllToReferences().size() == 0 && !inStruct.getType().isAnchor())
			docStructsOhneSeiten.add(inStruct);
		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (Iterator<DocStruct> iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct child = (DocStruct) iter.next();
				checkDocStructsOhneSeiten(child);
			}
		}
	}

	private List<String> checkSeitenOhneDocstructs(Fileformat inRdf) throws PreferencesException {
		List<String> rueckgabe = new ArrayList<String>();
		DocStruct boundbook = inRdf.getDigitalDocument().getPhysicalDocStruct();
		/* wenn boundbook null ist */
		if (boundbook == null || boundbook.getAllChildren() == null)
			return rueckgabe;

		/* alle Seiten durchlaufen und pruefen ob References existieren */
		for (Iterator<DocStruct> iter = boundbook.getAllChildren().iterator(); iter.hasNext();) {
			DocStruct ds = (DocStruct) iter.next();
			List<Reference> refs = ds.getAllFromReferences();
			String physical = "";
			String logical = "";
			if (refs.size() == 0) {
				// System.out.println("   >>> Keine Seiten: "
				// + ((Metadata) ds.getAllMetadata().getFirst()).getValue());
				for (Iterator<Metadata> iter2 = ds.getAllMetadata().iterator(); iter2.hasNext();) {
					Metadata md = (Metadata) iter2.next();
					if (md.getType().getName().equals("logicalPageNumber"))
						logical = " (" + md.getValue() + ")";
					if (md.getType().getName().equals("physPageNumber"))
						physical = md.getValue();
				}
				rueckgabe.add(physical + logical);
			}
		}
		return rueckgabe;
	}

	private List<String> checkMandatoryValues(DocStruct inStruct, ArrayList<String> inList) {
		DocStructType dst = inStruct.getType();
		// System.out.println("----------------------- " + dst.getName());
		List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
		for (MetadataType mdt : allMDTypes) {
			String number = dst.getNumberOfMetadataType(mdt);
			// System.out.println(mdt.getName());
			List<? extends ugh.dl.Metadata> ll = inStruct.getAllMetadataByType(mdt);
			int real = 0;
			if (ll != null && ll.size() > 0) {
				real = ll.size();

				if (number.equals("1m") && real == 1 && (ll.get(0).getValue() == null || ll.get(0).getValue().length() == 0)) {
					inList.add(mdt.getName() + " in " + dst.getName() + " is empty.");
				}
				/* jetzt die Typen pruefen */
				if (number.equals("1m") && real != 1) {
					inList.add(mdt.getName() + " in " + dst.getName() + " must exist 1 time but exists " + real + " times");
				}
				if ((number.equals("+") || number.equals("1o")) && real > 1) {
					inList.add(mdt.getName() + " in " + dst.getName() + " must not exist more than 1 time but exists " + real + " times");
				}
			}
		}

		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (DocStruct child : inStruct.getAllChildren())
				checkMandatoryValues(child, inList);
		}
		return inList;
	}


//	/**
//	 * Create Element From - fuer alle Strukturelemente ein bestimmtes Metadatum erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
//	 * vorhanden ================================================================
//	 */
//	private void checkCreateElementFrom(ArrayList inFehlerList, ArrayList<MetadataType> inListOfFromMdts, DocStruct myStruct, MetadataType mdt, int id) {
//
//		/*
//		 * -------------------------------- existiert das zu erzeugende Metadatum schon, dann ueberspringen, ansonsten alle Daten zusammensammeln und
//		 * in das neue Element schreiben --------------------------------
//		 */
//		List createMetadaten = myStruct.getAllMetadataByType(mdt);
//		if (createMetadaten.size() == 0) {
//			try {
//				Metadata createdElement = new Metadata(mdt);
//				// createdElement.setType(mdt);
//				StringBuffer myValue = new StringBuffer();
//				/*
//				 * alle anzufuegenden Metadaten durchlaufen und an das Element anhuengen
//				 */
//				for (MetadataType mdttemp : inListOfFromMdts) {
//
//					
//					// MetadataType mdttemp = (MetadataType) iter.next();
//
//					// List fromElemente = myStruct.getAllMetadataByType(mdttemp);
//					List<Person> fromElemente = myStruct.getAllPersons();
//					if (fromElemente != null && fromElemente.size() > 0) {
//						/*
//						 * wenn Personen vorhanden sind (z.B. Illustrator), dann diese durchlaufen
//						 */
//						for (Person p : fromElemente) {
//
//							
//							// Person p = (Person) iter2.next();
//
//							if (p.getRole() == null) {
//								myLogger.info("[" + id + " " + myStruct.getType() + "] Person without role");
//								break;
//							} else {
//								if (p.getRole().equals(mdttemp.getName())) {
//									if (myValue.length() > 0)
//										myValue.append("; ");
//									myValue.append(p.getLastname());
//									myValue.append(", ");
//									myValue.append(p.getFirstname());
//								}
//							}
//						}
//					}
//				}
//
//				if (myValue.length() > 0) {
//					createdElement.setValue(myValue.toString());
//				}
//				myStruct.addMetadata(createdElement);
//			} catch (DocStructHasNoTypeException e) {
//				// e.printStackTrace();
//			} catch (MetadataTypeNotAllowedException e) {
//				// e.printStackTrace();
//			}
//
//		}
//
//		/*
//		 * -------------------------------- alle Kinder durchlaufen --------------------------------
//		 */
//		List children = myStruct.getAllChildren();
//		if (children != null && children.size() > 0)
//			for (Iterator iter = children.iterator(); iter.hasNext();) {
//				checkCreateElementFrom(inFehlerList, inListOfFromMdts, (DocStruct) iter.next(), mdt, id);
//			}
//	}

	/**
	 * Metadatum soll mit bestimmten String beginnen oder enden ================================================================
	 */
//	private void checkStartsEndsWith(List inFehlerList, String prop_startswith, String prop_endswith, DocStruct myStruct, MetadataType mdt) {
//		/* startswith oder endswith */
//		List alleMetadaten = myStruct.getAllMetadataByType(mdt);
//		if (alleMetadaten != null && alleMetadaten.size() > 0)
//			for (Iterator iter = alleMetadaten.iterator(); iter.hasNext();) {
//				Metadata md = (Metadata) iter.next();
//
//				/* pruefen, ob es mit korrekten Werten beginnt */
//				if (prop_startswith != null) {
//					boolean isOk = false;
//					StringTokenizer tokenizer = new StringTokenizer(prop_startswith, "|");
//					while (tokenizer.hasMoreTokens()) {
//						String tok = tokenizer.nextToken();
//						if (md.getValue() != null && md.getValue().startsWith(tok))
//							isOk = true;
//					}
//					if (!isOk && !autoSave)
//						inFehlerList.add(md.getType().getName() + " with value " + md.getValue() + " does not start with " + prop_startswith);
//					if (!isOk && autoSave)
//						md.setValue(new StringTokenizer(prop_startswith, "|").nextToken() + md.getValue());
//				}
//				/* pruefen, ob es mit korrekten Werten endet */
//				if (prop_endswith != null) {
//					boolean isOk = false;
//					StringTokenizer tokenizer = new StringTokenizer(prop_endswith, "|");
//					while (tokenizer.hasMoreTokens()) {
//						String tok = tokenizer.nextToken();
//						if (md.getValue() != null && md.getValue().endsWith(tok))
//							isOk = true;
//					}
//					if (!isOk && !autoSave) {
//						inFehlerList.add(md.getType().getName() + " with value " + md.getValue() + " does not end with " + prop_endswith);
//					}
//					if (!isOk && autoSave) {
//						md.setValue(md.getValue() + new StringTokenizer(prop_endswith, "|").nextToken());
//					}
//				}
//			}
//	}
//
//	/**
//	 * automatisch speichern lassen, wenn Aenderungen noetig waren ================================================================
//	 */
//	public boolean isAutoSave() {
//		return autoSave;
//	}
//
//	public void setAutoSave(boolean autoSave) {
//		this.autoSave = autoSave;
//	}

}
