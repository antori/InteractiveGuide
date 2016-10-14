package opendial.modules.grammarBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GrammarObserver implements Observer{

	private String grammarFileName;
	private Grammar grammar;
	private static final GrammarObserver grammarObserver = new GrammarObserver();
	private ServerSocket serverListen= null;
	private Socket listen= null;
	
	private GrammarObserver() {	
		try {
			this.serverListen= new ServerSocket(5531);
		} catch (IOException e) {
			System.out.println("Could not open receiver port");
		}

		try {
			System.out.println("Waiting on 6531");
			this.listen = this.serverListen.accept();
			System.out.println("Accept ok on 6531");
		} catch (IOException e) {
			System.out.println("Accept failed: 6531");
 		}
	}
	
	public static GrammarObserver getInstance(){
		return grammarObserver;
	}
	
	public void setGrammar(Grammar grammar){
		this.grammar = grammar;
		this.grammar.subscribe(this);
	}
	@Override
	public void update() {		
		grammarFileName = grammar.getState();
		System.out.println("[GrammarObserver] Grammar updated! Sending on socket:"+ grammarFileName);		
		try {
			PrintWriter out = new PrintWriter(this.listen.getOutputStream(), true);
			out.println(grammarFileName);
		} catch (IOException e) {
		System.out.println("Print failed");
		System.exit(-1);
 		}
	}
}
