package opendial.modules.utils;

public abstract class GrammarBuilder {

	protected Grammar grammar;
	
	public Grammar getGrammar(){ return grammar; }
	
	public void createNewGrammar(String fileName){ grammar = new Grammar(fileName); }
	
	public abstract void parseModel();
	
	public abstract void parseRules();
	
	public abstract void parseEffects();
	
	
}
