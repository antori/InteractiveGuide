package opendial.modules.utils;

public class Grammar {

	private String domainFileName;
	private String path;
	
	public Grammar(String fileName){ this.setDomainFileName(fileName); }

	public String getDomainFileName() {
		return this.domainFileName;
	}

	public void setDomainFileName(String fileName) {
		this.domainFileName = fileName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	
}
