package opendial.modules;

import java.util.Collection;
import opendial.DialogueState;
import opendial.DialogueSystem;
import opendial.domains.Domain;
import opendial.modules.grammarBuilder.Grammar;
import opendial.modules.grammarBuilder.GrammarBuilder;
import opendial.modules.grammarBuilder.GrammarDirector;
import opendial.modules.grammarBuilder.GrammarObserver;
import opendial.modules.grammarBuilder.GrxmlBuilder;

public class GrxmlModule implements Module {

	private boolean paused = true;
	private DialogueSystem dsys;
	private Domain domain;
	private String domainFileName;
	private static Grammar grammar;
	private static GrammarObserver grammarObserver;

	public GrxmlModule(DialogueSystem system) {
		// Set initial parameters
		dsys = system;
		domain = system.getDomain();
		domainFileName = domain.getSourceFile().getPath();
		
		grammar = Grammar.getInstance();
		grammarObserver = GrammarObserver.getInstance();
		grammarObserver.setGrammar(grammar);

		constructGrammar();

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		new Thread(new GrammarProcess()).start();
		paused = false;

	}

	@Override
	public void trigger(DialogueState state, Collection<String> updatedVars) {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause(boolean toPause) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	class GrammarProcess implements Runnable {

		public void run() {

		}
	}

	private void constructGrammar() {
		
		GrammarDirector director = new GrammarDirector();
		GrammarBuilder builder = new GrxmlBuilder();

		director.setGrammarBuilder(builder);
		director.constructGrammar(domainFileName);

	}

}
