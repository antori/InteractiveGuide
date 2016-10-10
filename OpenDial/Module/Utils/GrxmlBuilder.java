package opendial.modules.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
	private Document grammarDoc;
	private DOMSource grammarSource;
	private ArrayList<Node> modelNodes = new ArrayList<Node>();
	private ArrayList<Node> modelGrammarNodes = new ArrayList<Node>();
	private ArrayList<Node> modelEffectNodes = new ArrayList<Node>();
	private boolean modelCheck = true;
	private boolean modelParsed = false;
	private final static String msg = "[GrxmlModule] Error in xml syntax!";
	private final static String msg1 = "[GrxmlModule] Can not create object!";
	private final static String msg2 = "[GrxmlModule] Can not create file!";

	@Override
	public void parseModel() {

		System.out.println("[GrxmlBuilder]Parsing model...");
		// parse xml domain
		domainDoc = XMLUtils.getXMLDocument(super.grammar.getDomainFileName());
		rootNode = XMLUtils.getMainNode(domainDoc);
		NodeList childs = rootNode.getChildNodes();
		// searching for models have trigger equals to "u_u"
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			// model trigger = "u_u"
			if (child.getNodeName().equals("model")
					&& child.getAttributes().getNamedItem("trigger").getNodeValue().equals("u_u")) {
				modelNodes.add(child);
			}
		}
		// checking for found models
		if (modelNodes.isEmpty()) {
			System.out.println("GrxmlModule: No models have trigger equals to u_u");

		} else {
			// for each node in the model
			for (Node node : modelNodes) {
				NodeList rules = node.getChildNodes();
				// for each rules in the model
				for (int i = 0; i < rules.getLength(); i++) {
					NodeList cases = rules.item(i).getChildNodes();
					// for each cases in the model
					for (int j = 0; j < cases.getLength(); j++) {
						NodeList caseChilds = cases.item(j).getChildNodes();
						boolean checkGrammar = false;
						boolean checkEffect = false;
						// for each case childs in the model
						for (int k = 0; k < caseChilds.getLength(); k++) {
							// parsing grammar node
							if (caseChilds.item(k).getNodeName().equals("grammar")) {
								// checking if the grammar is unique
								if (!checkGrammar && !checkEffect) {
									modelGrammarNodes.add(caseChilds.item(k));
									NodeList ruleNodes = caseChilds.item(k).getChildNodes();
									boolean checkRule = false;
									// checking if the grammar contains only one
									// root rule
									for (int z = 0; z < ruleNodes.getLength(); z++) {

										if (ruleNodes.item(z).getNodeName().equals("rule")) {

											if (!checkRule)
												checkRule = true;
											else {
												// model contains syntax error
												modelCheck = false;
												throw new RuntimeException(msg);
											}
										}
									}
								} else {
									// model contains syntax error
									modelCheck = false;
									throw new RuntimeException(msg);
								}
								checkGrammar = true;
								// checking for effects
							} else if (caseChilds.item(k).getNodeName().equals("effect")) {
								if (!checkEffect && checkGrammar) {
									modelEffectNodes.add(caseChilds.item(k));

								} else if (checkGrammar) {
									// model contains syntax error
									modelCheck = false;
									throw new RuntimeException(msg);
								}
								checkEffect = true;
							}
						}
						if (checkGrammar && !checkEffect) {
							// checkGrammar should implies checkEffect
							modelCheck = false;
							throw new RuntimeException(msg);
						}
					}
				}
			}
			// checking items attributes
			NodeList items = domainDoc.getElementsByTagName("item");
			for (int i = 0; i < items.getLength(); i++) {
				Node item = items.item(i);

				if (item.getAttributes().getNamedItem("include") != null
						&& (item.getAttributes().getNamedItem("type") == null
								|| item.getAttributes().getNamedItem("sem_field") == null)) {
					// include should implies type and sem_field
					modelCheck = false;
					throw new RuntimeException(msg);
				}
			}
		}
		modelParsed = true;
	}

	@Override
	public void buildObject() {

		if (!modelParsed || !modelCheck)
			throw new RuntimeException(msg1);

		System.out.println("[GrxmlBuilder] Building grammar object...");

		if (!modelGrammarNodes.isEmpty() && !modelEffectNodes.isEmpty()) {
			grammarDoc = XMLUtils.newXMLDocument();
			Element root = grammarDoc.createElement("grammar");
			Element firstRule = grammarDoc.createElement("rule");
			Element firstOneOf = grammarDoc.createElement("one-of");

			for (int i = 0; i < modelGrammarNodes.size(); i++) {
				System.out.println("[GrxmlBuilder] Building grammar node");

				Node grammarNode = modelGrammarNodes.get(i);
				Node effectNode = modelEffectNodes.get(i);

				NodeList ruleNodes = grammarNode.getChildNodes();
				NodeList setNodes = effectNode.getChildNodes();

				Element item = grammarDoc.createElement("item");

				for (int j = 0; j < ruleNodes.getLength(); j++) {

					// Just one rule for grammar
					if (ruleNodes.item(j).getNodeName().equals("rule")) {
						System.out.println("[GrxmlBuilder] Building rule node");

						Node ruleNode = ruleNodes.item(j);
						// Create a duplicate node
						Node newNode = ruleNode.cloneNode(true);
						// Transfer ownership of the new node into the
						// destination document
						grammarDoc.adoptNode(newNode);
						item.appendChild(newNode);
						break;

					}
				}

				for (int z = 0; z < setNodes.getLength(); z++) {

					if (setNodes.item(z).getNodeName().equals("set")) {
						System.out.println("[GrxmlBuilder] Building effect node");
						String var = setNodes.item(z).getAttributes().getNamedItem("var").getNodeValue();
						String value = setNodes.item(z).getAttributes().getNamedItem("value").getNodeValue();
						Element tag = grammarDoc.createElement("tag");

						tag.appendChild(grammarDoc.createTextNode("out." + var + " = \"" + value + "\";"));
						item.appendChild(tag);
					}
				}

				firstOneOf.appendChild(item);
			}

			firstRule.appendChild(firstOneOf);
			root.appendChild(firstRule);
			grammarDoc.appendChild(root);

			NodeList items = grammarDoc.getElementsByTagName("item");
			ArrayList<Node> removedItems = new ArrayList<Node>();

			for (int i = 0; i < items.getLength(); i++) {
				Node item = items.item(i);

				if (item.getAttributes().getNamedItem("include") != null
						&& item.getAttributes().getNamedItem("include").getNodeValue().equals("syn")) {
					String content = item.getTextContent();
					System.out.println(content);
					Neo4j db = new Neo4j();
					String term = item.getTextContent();
					String type = item.getAttributes().getNamedItem("type").getNodeValue();
					String semanticField = item.getAttributes().getNamedItem("sem_field").getNodeValue();
					ArrayList<String> results = db.getSynonimus(term, type, semanticField);
					Node parent = item.getParentNode();
					Node oneOfSyn = grammarDoc.createElement("one-of");
					for (String s : results) {
						System.out.println(s);
						Node synItem = grammarDoc.createElement("item");
						synItem.setTextContent(s);
						oneOfSyn.appendChild(synItem);
					}
					Node synItem = grammarDoc.createElement("item");
					synItem.setTextContent(item.getTextContent());
					oneOfSyn.appendChild(synItem);
					parent.appendChild(oneOfSyn);
					removedItems.add(item);
				}
			}
			for (Node n : removedItems) {
				n.getParentNode().removeChild(n);
			}
		}
	}

	@Override
	public void buildFile() {

		if (grammarDoc == null)
			throw new RuntimeException(msg2);

		try {
			System.out.println("[GrxmlBuilder] Creating file...");
			grammarSource = new DOMSource(grammarDoc);
			String pattern = Pattern.quote(System.getProperty("file.separator"));
			String[] pathSplitted = super.grammar.getDomainFileName().split(pattern);
			String path = "";

			for (int i = 0; i < pathSplitted.length - 1; i++) {
				path += pathSplitted[i] + File.separator;
			}

			path += "grammar.grxml";
			File xmlFile = new File(path);

			StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(xmlFile)));
			Transformer xformer = TransformerFactory.newInstance().newTransformer();

			xformer.transform(grammarSource, result);

			super.grammar.setSource(grammarSource);
			System.out.println("[GrxmlBuilder] Grammar created. Path = " + path);

		} catch (TransformerException | TransformerFactoryConfigurationError e1) {
			e1.printStackTrace();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
