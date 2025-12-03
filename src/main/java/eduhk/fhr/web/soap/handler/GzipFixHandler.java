package eduhk.fhr.web.soap.handler;

import java.io.IOException;
import java.util.Set;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOAPHandler to fix GZIP decompression issues
 *
 * TalentLink sends "Content-Encoding: gzip" header but the data is NOT actually gzipped,
 * causing "Not in GZIP format" errors in HttpClientTransport.
 *
 * This handler removes the problematic Content-Encoding header from responses
 * before Metro tries to decompress them.
 */
public class GzipFixHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(GzipFixHandler.class);

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        // Only process inbound (response) messages
        if (outbound != null && !outbound) {
            try {
                // Remove Content-Encoding header to prevent automatic GZIP decompression
                @SuppressWarnings("unchecked")
                java.util.Map<String, java.util.List<String>> headers =
                    (java.util.Map<String, java.util.List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS);

                if (headers != null) {
                    boolean removed = false;

                    // Try different case variations of the header name
                    if (headers.remove("Content-Encoding") != null) {
                        removed = true;
                    }
                    if (headers.remove("content-encoding") != null) {
                        removed = true;
                    }
                    if (headers.remove("CONTENT-ENCODING") != null) {
                        removed = true;
                    }

                    if (removed) {
                        logger.debug("Removed Content-Encoding header from response to prevent GZIP decompression");
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to remove Content-Encoding header: {}", e.getMessage());
            }
        }

        return true; // Continue handler chain
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true; // Continue handler chain
    }

    @Override
    public void close(MessageContext context) {
        // Nothing to clean up
    }

    @Override
    public Set<javax.xml.namespace.QName> getHeaders() {
        return null; // No specific headers required
    }
}
