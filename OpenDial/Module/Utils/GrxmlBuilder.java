package opendial.modules.utils;

import java.io.StringWriter;
import java.util.ArrayList;

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

import opendial.utils.XMLUtils;

public class GrxmlBuilder extends GrammarBuilder {
	
	private Node rootNode;
	private Document domainDoc;
	private ArrayList<Node> modelNodes = new ArrayList<Node>();
	private ArrayList<Node> modelGrammarNodes = new ArrayList<Node>();
	private ArrayList<Node> modelEffectNodes = new ArrayList<Node>();

	@Override
	public void parseModel() {	
		domainDoc = XMLUtils.getXMLDocument(super.grammar.getFileName());
	    rootNode = XMLUtils.getMainNode(domainDoc);
		  
	    NodeList childs = rootNode.getChildNodes();
	    //searching for models have trigger equals to "u_u"
	    for(int i=0; i<childs.getLength(); i++){
	    	
	    	Node child = childs.item(i);
	    
	    	if(child.getNodeName().equals("model") 
	    		&& child.getAttributes().getNamedItem("trigger").getNodeValue().equals("u_u")){
	    		
	    		System.out.println(child.getNodeName()+" "+child.getAttributes().getNamedItem("trigger").getNodeValue());
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
						int grammarCount = 0;

						for (int k = 0; k < caseChilds.getLength(); k++) {						
							//NECESSARIO CONTROLLO SULLA CONSISTENZA (PER OGNI GRAMMAR UN EFFECT SEGUENTE)						
							if(caseChilds.item(k).getNodeName().equals("grammar") && grammarCount == 0){	
								modelGrammarNodes.add(caseChilds.item(k));
								grammarCount ++;
								
							} else if(caseChilds.item(k).getNodeName().equals("effect") && grammarCount == 1){
								modelEffectNodes.add(caseChilds.item(k));
								grammarCount --;
							}
						}		
						
						if(grammarCount!=0){
							System.out.println("GrxmlModule: Error in xml syntact. You must define an effect for each grammar");
						}
					}
				}
	    	}
	    	
	    	if(!modelGrammarNodes.isEmpty() && !modelEffectNodes.isEmpty()){    		
	    		Transformer transformer;
	    		
				try {
					transformer = TransformerFactory.newInstance().newTransformer();
		    		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    		
		    		//initialize StreamResult with File object to save to file
		    		StreamResult result = new StreamResult(new StringWriter());
		    		DOMSource source = new DOMSource(modelGrammarNodes.get(0));
		    		
		    		try {
						transformer.transform(source, result);
						
					} catch (TransformerException e) {
						e.printStackTrace();
					}
		    		
		    		String xmlString = result.getWriter().toString();
		    		System.out.println(xmlString);
		    		
				} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e1) {
						e1.printStackTrace();
				}
	    	}
	    }	
	}
	
	@Override
	public void parseRules(){
		
	}

	@Override
	public void parseEffects() {
		
	}

}
