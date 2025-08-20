package gov.usda.fsa.fcao.flp.ola.core.shared.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BaseSoapClient {

	private static final Logger logger = LogManager.getLogger(BaseSoapClient.class);
	
	// Generic SOAP service caller
	protected String callSoapService(String soapRequest, String action, String url, RestTemplate restTemplate) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_XML);
			headers.set("SOAPAction", "");

			HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);

			logger.info("Sending SOAP request to: {}", url);

			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if(!"getCustomers".equalsIgnoreCase(action))
            {
    			logger.info("SOAP Response: {}", response.getBody());
            }

			return response.getBody();

		} catch (Exception e) {
			logger.error("Error calling SOAP service for action: {}", action, e);
			throw new RuntimeException("SOAP service call failed: " + e.getMessage(), e);
		}
	}
	
    public static DocumentBuilderFactory createSecureDocumentBuilderFactory()
            throws ParserConfigurationException {
       
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       
        // Disable DOCTYPE declaration
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
       
        // Disable external entities
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
       
        // Disable external DTDs
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
       
        // Additional security configurations
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
       
        return factory;
    }	
	
	protected String getElementText(Document doc, String tagName) {
		NodeList nodes = doc.getElementsByTagName(tagName);
		if (nodes.getLength() > 0) {
			return nodes.item(0).getTextContent();
		}
		return null;
	}
	
    protected String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String textContent = nodes.item(0).getTextContent();
            return textContent != null ? textContent.trim() : null;
        }
        return null;
    }
    
    protected boolean getBooleanElementText(Element parent, String tagName) {
        String text = getElementText(parent, tagName);
        return text != null && ("true".equalsIgnoreCase(text.trim()) || "1".equals(text.trim()));
    }
    
    protected BigDecimal getBigDecimalElementText(Element parent, String tagName) {
        String text = getElementText(parent, tagName);
        if (text != null && !text.trim().isEmpty()) {
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse BigDecimal from text: {}", text);
            }
        }
        return null;
    }
	
	// Helper method to create namespace-aware document
	protected Document createNamespaceAwareDocument(String xmlContent) throws Exception {
	    DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
	    factory.setNamespaceAware(true);
        //factory.setValidating(false);
        //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        //factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        //factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	    
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
        //return builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
	}	
	
    protected String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    protected void appendOptionalField(StringBuilder request, String fieldName, String value) {
        if (value != null && !value.trim().isEmpty()) {
            request.append(String.format("<%s>%s</%s>", fieldName, escapeXml(value), fieldName));
        }
    }
    
    // Response parsers
    protected boolean parseHealthCheckResponse(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return false;
            }
           
            return response.contains("<return>true</return>") ||
                   response.contains("<ns2:return>true</ns2:return>") ||
                   response.contains(">true<");
        } catch (Exception e) {
            logger.error("Error parsing health check response", e);
            return false;
        }
    }
	
	protected String extractFaultString(String response) {
		try {
			DocumentBuilder builder = createSecureDocumentBuilderFactory().newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));

			NodeList faultStringNodes = doc.getElementsByTagName("faultstring");
			if (faultStringNodes.getLength() > 0) {
				return faultStringNodes.item(0).getTextContent();
			}

			return "Unknown SOAP fault";
		} catch (Exception e) {
			logger.error("Error extracting fault string", e);
			return "Error parsing SOAP fault";
		}
	}
    
}
