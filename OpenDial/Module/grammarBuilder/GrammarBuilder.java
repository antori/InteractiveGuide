package opendial.modules.grammarBuilder;

public abstract class GrammarBuilder {

	protected Grammar grammar;

	public Grammar getGrammar() {
		return grammar;
	}

	public void createNewGrammar(String fileName) {
		grammar = Grammar.getInstance();
		grammar.setDomainFileName(fileName);
	}

	public abstract void parseModel();

	public abstract void buildObject();

	public abstract void buildFile();

}
