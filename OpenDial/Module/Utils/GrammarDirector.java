package opendial.modules.utils;

public class GrammarDirector {
	
	private GrammarBuilder builder;
	
	public void setGrammarBuilder(GrammarBuilder builder){ this.builder = builder; }
	
	public Grammar getGrammar(){ return builder.getGrammar(); }
	
	public void constructGrammar(String fileName){
		
		builder.createNewGrammar(fileName);
		builder.parseModel();
		builder.parseRules();
		builder.parseEffects();
		
	}
	

}
