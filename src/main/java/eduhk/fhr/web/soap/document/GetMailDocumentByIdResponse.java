
package eduhk.fhr.web.soap.document;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getMailDocumentByIdResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getMailDocumentByIdResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ws.mrted.com/}mailDocument" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getMailDocumentByIdResponse", propOrder = {
    "mailDocument"
})
public class GetMailDocumentByIdResponse {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected MailDto mailDocument;

    /**
     * Gets the value of the mailDocument property.
     * 
     * @return
     *     possible object is
     *     {@link MailDto }
     *     
     */
    public MailDto getMailDocument() {
        return mailDocument;
    }

    /**
     * Sets the value of the mailDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailDto }
     *     
     */
    public void setMailDocument(MailDto value) {
        this.mailDocument = value;
    }

}
