package ugh.dl;

import java.io.Serializable;

import org.w3c.dom.Node;

public class Md implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6784880447020540980L;
	private Node content;
	private String id;
	private String type;
	
	public Md(Node content) {
		super();
		this.content = content;
	}

	public Node getContent() {
		return content;
	}

	public void setContent(Node content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
