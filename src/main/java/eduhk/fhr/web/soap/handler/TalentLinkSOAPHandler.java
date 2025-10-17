package eduhk.fhr.web.soap.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOAP Handler for TalentLink API Authentication
 * Adds WS-Security header with username and password credentials
 */
public class TalentLinkSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkSOAPHandler.class);

    public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String PASSWORD_TEXT_TYPE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
    public static final String WSSE_SECURITY_LNAME = "Security";
    public static final String WSSE_NS_PREFIX = "wsse";

    // These will be set from the service that uses this handler
    private static String username;
    private static String password;

    private boolean mustUnderstand = false;

    /**
     * Set credentials for SOAP authentication
     * This should be called before making any SOAP calls
     */
    public static void setCredentials(String user, String pass) {
        username = user;
        password = pass;
        logger.info("SOAP credentials configured for user: {}", user);
    }

    @Override
    public boolean handleMessage(SOAPMessageContext messageContext) {
        Object bOutbound = messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (bOutbound == Boolean.TRUE) {
            try {
                if (username != null && username.length() != 0) {
                    addSecurityHeader(messageContext);
                    logger.debug("Added WS-Security header to SOAP request");
                } else {
                    logger.warn("No username configured - SOAP request may fail authentication");
                }
            } catch (Exception e) {
                logger.error("Exception in handleMessage: {}", e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    private void addSecurityHeader(SOAPMessageContext messageContext) throws SOAPException {
        SOAPFactory sf = SOAPFactory.newInstance();
        SOAPHeader header = messageContext.getMessage().getSOAPPart().getEnvelope().getHeader();
        if (header == null) {
            header = messageContext.getMessage().getSOAPPart().getEnvelope().addHeader();
        }

        Name securityName = sf.createName(WSSE_SECURITY_LNAME, WSSE_NS_PREFIX, WSSE_NS);
        SOAPHeaderElement securityElem = header.addHeaderElement(securityName);
        securityElem.setMustUnderstand(mustUnderstand);

        Name usernameTokenName = sf.createName("UsernameToken", WSSE_NS_PREFIX, WSSE_NS);
        SOAPElement usernameTokenMsgElem = sf.createElement(usernameTokenName);

        Name usernameName = sf.createName("Username", WSSE_NS_PREFIX, WSSE_NS);
        SOAPElement usernameMsgElem = sf.createElement(usernameName);
        usernameMsgElem.addTextNode(username);
        usernameTokenMsgElem.addChildElement(usernameMsgElem);

        Name passwordName = sf.createName("Type", WSSE_NS_PREFIX, WSSE_NS);
        SOAPElement passwordMsgElem = sf.createElement("Password", WSSE_NS_PREFIX, WSSE_NS);

        passwordMsgElem.addAttribute(passwordName, PASSWORD_TEXT_TYPE);
        passwordMsgElem.addTextNode(password);
        usernameTokenMsgElem.addChildElement(passwordMsgElem);

        securityElem.addChildElement(usernameTokenMsgElem);
    }

    @Override
    public void close(MessageContext context) {
        // No cleanup needed
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        logger.error("SOAP Fault occurred");
        return false;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
