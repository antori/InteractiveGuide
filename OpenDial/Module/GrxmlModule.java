package opendial.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import opendial.DialogueState;
import opendial.DialogueSystem;
import opendial.domains.Domain;
import opendial.modules.StringReceiver.ReceiverProcess;
import opendial.modules.utils.Grammar;
import opendial.modules.utils.GrammarBuilder;
import opendial.modules.utils.GrammarDirector;
import opendial.modules.utils.GrammarObserver;
import opendial.modules.utils.GrxmlBuilder;
import opendial.utils.XMLUtils;

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
