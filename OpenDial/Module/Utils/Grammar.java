package opendial.modules.utils;

import javax.xml.transform.dom.DOMSource;

public class Grammar {

	private String domainFileName;
	private DOMSource source;

	public Grammar(String fileName) {
		this.setDomainFileName(fileName);
	}

	public String getDomainFileName() {
		return this.domainFileName;
	}

	public void setDomainFileName(String fileName) {
		this.domainFileName = fileName;
	}

	public DOMSource getSource() {
		return source;
	}

	public void setSource(DOMSource source) {
		this.source = source;
	}

}
