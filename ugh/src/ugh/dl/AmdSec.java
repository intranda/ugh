package ugh.dl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class AmdSec implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2651069769792564435L;
	private String id;
	private ArrayList<Md> techMdList;

	public AmdSec(ArrayList<Md> techMdList) {
		super();
		this.techMdList = techMdList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Md> getTechMdList() {
		return techMdList;
	}

	public void setTechMdList(ArrayList<Md> techMdList) {
		this.techMdList = techMdList;
	}

	public void addTechMd(Md techMd) {
		if (techMdList == null) {
			techMdList = new ArrayList<Md>();
		}
		this.techMdList.add(techMd);
	}

	public List<Node> getTechMdsAsNodes() {
		List<Node> nodeList = new ArrayList<Node>();
		if (this.techMdList != null) {
			for (Md techMd : this.techMdList) {
				nodeList.add(techMd.getContent());
			}
		}
		return nodeList;
	}

}
