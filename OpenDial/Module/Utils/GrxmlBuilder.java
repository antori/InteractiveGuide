package opendial.modules.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.sql.rowset.spi.TransactionalWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import opendial.utils.XMLUtils;

public class GrxmlBuilder extends GrammarBuilder {
	
	private Node rootNode;
	private Document domainDoc;
	private ArrayList<Node> modelNodes = new ArrayList<Node>();
	private ArrayList<Node> modelGrammarNodes = new ArrayList<Node>();
	private ArrayList<Node> modelEffectNodes = new ArrayList<Node>();
	private boolean modelCheck = true;
	private boolean modelParsed = false;
	private String msg = "[GrxmlModule] Error in xml syntact!";
	private String msg1 = "[GrxmlModule] Module not arledy parsed!";

	@Override
	public void parseModel() {	
		System.out.println("[GrxmlBuilder]Parsing model...");
		
		domainDoc = XMLUtils.getXMLDocument(super.grammar.getDomainFileName());
	    rootNode = XMLUtils.getMainNode(domainDoc);
	    NodeList childs = rootNode.getChildNodes();
	    
	    //searching for models have trigger equals to "u_u"
	    for(int i=0; i<childs.getLength(); i++){	    	
	    	Node child = childs.item(i);
	    
	    	if(child.getNodeName().equals("model") 
	    		&& child.getAttributes().getNamedItem("trigger").getNodeValue().equals("u_u")){	    		
	    		modelNodes.add(child);
	    	}
	    }
	    //checking for found models
	    if(modelNodes.isEmpty()){
	    	System.out.println("GrxmlModule: No models have trigger equals to u_u");
	    	
	    }else{
	    	
	    	for (Node node : modelNodes) {    		
				NodeList rules = node.getChildNodes();
				
				for (int i = 0; i < rules.getLength(); i++) {				
					NodeList cases = rules.item(i).getChildNodes();
					
					for (int j = 0; j < cases.getLength(); j++) {						
				     	NodeList caseChilds = cases.item(j).getChildNodes();
						boolean checkGrammar = false;
						boolean checkEffect = false;

						for (int k = 0; k < caseChilds.getLength(); k++) {						
							
							if(caseChilds.item(k).getNodeName().equals("grammar")){
								//non ho trovato ne grammatica ne effetto
								if(!checkGrammar && !checkEffect){
									modelGrammarNodes.add(caseChilds.item(k));
								}else{
								//ho trovato gia' una grammatica o un effetto 
									modelCheck = false;
									throw new RuntimeException(msg);
								}
								checkGrammar = true;
								
							} else if(caseChilds.item(k).getNodeName().equals("effect")){
								if(!checkEffect && checkGrammar){ 
									//ho trovato una grammatica precedente ma non ancora un effetto
									modelEffectNodes.add(caseChilds.item(k));
									
								}else if(checkGrammar){
									//ho trovato una grammatica precedente e un effetto
									modelCheck = false;
									throw new RuntimeException(msg);
								}
								checkEffect = true;
							}
						}	
						
						if(checkGrammar && !checkEffect){
							//grammatica deve implicare effetto
							modelCheck = false;
							throw new RuntimeException(msg);
						}
					}
				}
	    	}	
	    }
	    
	    modelParsed = true;
	}
	
	@Override
	public void buildObject(){
		System.out.println("[GrxmlBuilder] Building grammar object...");
	}

	@Override
	public void buildFile() {
		
		if(!modelParsed)
			throw new RuntimeException(msg1);
		
		System.out.println("[GrxmlBuilder] Building grammar file...");
		
		if(modelCheck && !modelGrammarNodes.isEmpty() && !modelEffectNodes.isEmpty()){    		
		    Document grammar = XMLUtils.newXMLDocument();
		    Element root = grammar.createElement("grammar");
		    Element firstRule = grammar.createElement("rule");
		    Element firstOneOf = grammar.createElement("one-of");
		    
		    for( int i = 0; i < modelGrammarNodes.size(); i++){	 
		    	System.out.println("[GrxmlBuilder] Parsing grammar node");
			   
		    	Node grammarNode = modelGrammarNodes.get(i);
		    	Node effectNode = modelEffectNodes.get(i);		    	
		    	
		    	NodeList ruleNodes = grammarNode.getChildNodes();
		    	NodeList setNodes = effectNode.getChildNodes();
		    	
		    	for(int j = 0; j < ruleNodes.getLength(); j++){
		    		
		    		if(ruleNodes.item(j).getNodeName().equals("rule")){
			    		System.out.println("[GrxmlBuilder] Parsing rule node");
			    		
			    		Element item = grammar.createElement("item");
			    		Node ruleNode = ruleNodes.item(j);  
					    // Create a duplicate node
					    Node newNode = ruleNode.cloneNode(true);
					    // Transfer ownership of the new node into the destination document
					    grammar.adoptNode(newNode);	    
			    		item.appendChild(newNode);
			    		
			    		for(int z = 0; z < setNodes.getLength(); z++){
			    			
			    		    if(setNodes.item(z).getNodeName().equals("set")){
					    	System.out.println("[GrxmlBuilder] Parsing effect node");
			    			
					    	String var = setNodes.item(z).getAttributes().getNamedItem("var").getNodeValue();
			    			String value = setNodes.item(z).getAttributes().getNamedItem("value").getNodeValue();
			    			
			    			Element tag = grammar.createElement("tag"); 	    						    			
			    			tag.appendChild(grammar.createTextNode("out."+var+" = \""+value+"\";"));		    					    			   
							
			    			item.appendChild(tag);		    		    	
			    		    }
			    		}
				    	firstOneOf.appendChild(item);
		    		}    		
		    	}
		    }    
		    firstRule.appendChild(firstOneOf);
		    root.appendChild(firstRule);
	    	grammar.appendChild(root);
			
	    	try {	
	    		System.out.println("[GrxmlBuilder] Creating file...");
	    		
				Source source = new DOMSource(grammar); 		
				String pattern = Pattern.quote(System.getProperty("file.separator"));
				String[] pathSplitted = super.grammar.getDomainFileName().split(pattern);
				String path = "";
				
				for(int i = 0; i < pathSplitted.length - 1; i++){
					path += pathSplitted[i] + File.separator;
				}			
				
				path += "grammar.grxml";
	            File xmlFile = new File(path);            
	            
	            StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(xmlFile)));
	            Transformer xformer = TransformerFactory.newInstance().newTransformer();                        
	            
	            xformer.transform(source, result);
	            
	            super.grammar.setPath(path);
	            
	            System.out.println("[GrxmlBuilder] Grammar created. Path = "+ path);
	            
			} catch (TransformerException | TransformerFactoryConfigurationError e1) {
				e1.printStackTrace();
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}		
	}
}
