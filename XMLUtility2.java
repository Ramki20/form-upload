package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Christophor.Caruthers
 * 
 */
public class XMLUtility {
	/**
	 * Constructor for XMLUtility.
	 */
	public XMLUtility() {
		super();
	}

	/**
	 * This method may be used to specify a URL to an XML document. The XML is
	 * parsed and a Document object is returned.
	 * 
	 * @return org.w3c.dom.Document
	 * @param url java.lang.String
	 */
	public static Document getParsedDocumentFromURL(String url) throws Exception {
		return getParsedDocumentFromURL(url, true, true);
	}

	/**
	 * This method may be used to specify a URL to an XML document. The XML is
	 * parsed and a Document object is returned.
	 * 
	 * @return org.w3c.dom.Document
	 * @param url              java.lang.String
	 * @param validating       boolean
	 * @param ignoreWhiteSpace boolean
	 */
	public static Document getParsedDocumentFromURL(String url, boolean validating, boolean ignoreWhiteSpace)
			throws XMLParsingException {
		DocumentBuilder db = null;
		Document doc = null;
		String errMsg = "";
		try {
			db = getDocumentBuilder(validating, ignoreWhiteSpace);
			doc = db.parse(url);
		} catch (SAXException se) {
			errMsg = "XMLUtility#getParsedDocumentFromURL() - Error parsing " + url + " || " + se.getMessage();

			throw new XMLParsingException(errMsg);
		} catch (IOException ioe) {
			errMsg = "XMLUtility#getParsedDocumentFromURL() - Could not find " + url + " || " + ioe.getMessage();
			throw new XMLParsingException(errMsg);
		}

		return doc;
	}

	/**
	 * This method may be used to parse an XML document String. Once the String is
	 * parsed, an XML Document object is returned.
	 * 
	 * @return org.w3c.dom.Document
	 * @param url java.lang.String
	 */
	public static Document getParsedDocumentFromXMLString(String xmlString) throws XMLParsingException {
		DocumentBuilder db = null;
		Document doc = null;
		String errMsg = "";

		try {
			db = getDocumentBuilder(false, true);
			doc = db.parse(new InputSource(new StringReader(xmlString)));
		} catch (IOException ioe) {
			errMsg = "XMLUtility#getParsedDocumentFromXMLString() - " + ioe.getMessage();
			throw new XMLParsingException(errMsg);
		} catch (SAXException se) {
			errMsg = "XMLUtility#getParsedDocumentFromXMLString() - " + se.getMessage();
			throw new XMLParsingException(errMsg);
		}
		return doc;
	}

	private static DocumentBuilder getDocumentBuilder(boolean validating, boolean ignoreWhiteSpace)
			throws XMLParsingException {
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		String errMsg = "";
		try {
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setValidating(validating);
			dbf.setIgnoringElementContentWhitespace(ignoreWhiteSpace);
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			errMsg = "XMLUtility#getDocumentBuilder() - Could not create DocumentBuilder for requested Parser Configuration. || "
					+ pce.getMessage();
			throw new XMLParsingException(errMsg);
		}
		return db;
	}

	/**
	 * for debugging - Updated to use javax.xml.transform instead of internal serialize classes
	 */
	public static String serializeDocument(Document doc) throws XMLParsingException {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			// Set secure processing to prevent XXE attacks
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			
			Transformer transformer = transformerFactory.newTransformer();
			
			// Configure output properties to match the original formatting
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			
			transformer.transform(source, result);
			
			return sw.toString();
		} catch (TransformerException te) {
			throw new XMLParsingException("XMLUtility#serializeDocument() || " + te.getMessage());
		}
	}

	/**
	 * Given an org.w3c.dom.Node, and a String representing a sub-node, finds and
	 * returns the sub-node if present. Otherwise, returns null.
	 * 
	 * @param        org.w3c.dom.Node The base node to start with
	 * @param String name The name of the node to find.
	 * @return org.w3c.dom.Node node
	 */
	public static Node findNode(Node node, String name) {
		if (node.getNodeName().equals(name))
			return node;
		if (node.hasChildNodes()) {
			NodeList list = node.getChildNodes();
			int size = list.getLength();
			for (int i = 0; i < size; i++) {
				Node found = XMLUtility.findNode(list.item(i), name);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	/**
	 * Given an org.w3c.dom.Node, and a String representing a sub-node, finds and
	 * returns all the sub-nodes with the given name if present. Otherwise, returns
	 * null.
	 * 
	 * @param        org.w3c.dom.Node The base node to start with
	 * @param String name The name of the node to find.
	 * @return org.w3c.dom.Node[] nodes
	 */
	public static Node[] findNodes(Node node, String name) {
		if (node.getNodeName().equals(name))
			return new Node[] { node };
		Node[] nodes = null;
		if (node.hasChildNodes()) {
			NodeList list = node.getChildNodes();
			int size = list.getLength();
			Vector<Node> vec = new Vector<Node>(size);
			for (int i = 0; i < size; i++) {
				Node found = XMLUtility.findNode(list.item(i), name);
				if (found != null)
					vec.add(found);
			}
			vec.trimToSize();
			if (!vec.isEmpty()) {
				nodes = (Node[]) vec.toArray(new Node[vec.size()]);
			}
		}
		return nodes;
	}

	/**
	 * This method is used to get the text value of a node that does not contain
	 * subnodes - only text. If the node contains more than a text value, an
	 * ApplicationException is thrown.
	 * 
	 * @param org.w3c.dom.Node mainNode The node for which to return the value.
	 * @throws ApplicationException
	 * @return java.lang.String The String value of the specified Node.
	 */
	public static String getMainElementValue(Node mainNode) throws XMLParsingException {
		String elementValue = "";
		if (mainNode.hasChildNodes()) {
			NodeList mainNodeChildren = mainNode.getChildNodes();
			if (mainNodeChildren.getLength() == 1) {
				elementValue = mainNodeChildren.item(0).getNodeValue();
			} else {

				throw new XMLParsingException("Node returned multiple values, only 1 allowed");
			}
		}
		/*
		 *  If elementValue is null, return an empty String, otherwise return the value.
		 */
		return ((elementValue == null) ? "" : elementValue);
	}

	/**
	 * Given an XML Document node and two sub-element names, returns the value. An
	 * example of this would be a [customer node], "name", and "first".
	 * 
	 * @return java.lang.String
	 * @param node                   org.w3c.dom.Node
	 * @param firstLevelElementName  java.lang.String The first subelement name
	 * @param secondLevelElementName java.lang.String The second subelement name
	 */
	public static String getSecondLevelElementValue(Node node, String firstLevelElementName,
			String secondLevelElementName) throws XMLParsingException {
		return XMLUtility.getMainElementValue(
				XMLUtility.findNode(XMLUtility.findNode(node, firstLevelElementName), secondLevelElementName));
	}

	/**
	 * Given a Node from an XML Document, find all Elements that have text values
	 * and return a Hashtable containing those Element name - value pairs. If an
	 * Element contains something other than Text, such as another Element, it is
	 * <b>NOT</b> returned.
	 * 
	 * @param node org.w3c.dom.Node
	 * @return java.util.Map Representing the Element names and values.
	 */
	public static java.util.Map<String, String> getSubelementValues(Node startNode) {
		Map<String, String> htSubelements = new Hashtable<String, String>();
		if (startNode.hasChildNodes()) {
			NodeList subElements = startNode.getChildNodes();
			for (int i = 0; i < subElements.getLength(); i++) {
				if (subElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if (subElements.item(i).hasChildNodes() && subElements.item(i).getChildNodes().getLength() > 1)
						continue;
					else if ((subElements.item(i).hasChildNodes())
							&& (subElements.item(i).getFirstChild().getNodeType() == Node.TEXT_NODE))
						htSubelements.put(subElements.item(i).getNodeName(),
								((subElements.item(i).getFirstChild().getNodeValue() != null
										&& !(subElements.item(i).getFirstChild().getNodeValue().equals(" ")))
												? subElements.item(i).getFirstChild().getNodeValue()
												: ""));
					else
						htSubelements.put(subElements.item(i).getNodeName(), "");
				}
			}
		}
		return htSubelements;
	}
}