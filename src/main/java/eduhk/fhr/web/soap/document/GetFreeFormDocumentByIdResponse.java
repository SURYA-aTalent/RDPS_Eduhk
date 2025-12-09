
package eduhk.fhr.web.soap.document;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getFreeFormDocumentByIdResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getFreeFormDocumentByIdResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ws.mrted.com/}freeFormDocument" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getFreeFormDocumentByIdResponse", propOrder = {
    "freeFormDocument"
})
public class GetFreeFormDocumentByIdResponse {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected FreeFormDocumentDto freeFormDocument;

    /**
     * Gets the value of the freeFormDocument property.
     * 
     * @return
     *     possible object is
     *     {@link FreeFormDocumentDto }
     *     
     */
    public FreeFormDocumentDto getFreeFormDocument() {
        return freeFormDocument;
    }

    /**
     * Sets the value of the freeFormDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link FreeFormDocumentDto }
     *     
     */
    public void setFreeFormDocument(FreeFormDocumentDto value) {
        this.freeFormDocument = value;
    }

}
