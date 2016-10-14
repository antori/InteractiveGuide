package opendial.modules.grammarBuilder;

import java.util.ArrayList;

import javax.xml.transform.dom.DOMSource;

public class Grammar implements Observable{

	private String domainFileName;
	private String grammarFileName;
	private DOMSource source;
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	private static final Grammar grammar = new Grammar();
	
	private Grammar(){
		
	}
	
	public static Grammar getInstance(){
		return grammar;
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
	
	public String getGrammarFileName() {
		return grammarFileName;
	}

	public void setGrammarFileName(String grammarFileName) {
		this.grammarFileName = grammarFileName;
		notifyObservers();
	} 

	@Override
	public void subscribe(Observer o) {
		observers.add(o);
		
	}

	@Override
	public void notifyObservers() {
		for(Observer o:observers){
			o.update();
		}
		
	}

	@Override
	public String getState() {
		return getGrammarFileName();
		
	}

}
