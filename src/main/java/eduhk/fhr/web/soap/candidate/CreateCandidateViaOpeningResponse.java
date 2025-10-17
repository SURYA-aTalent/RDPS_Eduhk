
package eduhk.fhr.web.soap.candidate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createCandidateViaOpeningResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createCandidateViaOpeningResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ws.mrted.com/}candidate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createCandidateViaOpeningResponse", propOrder = {
    "candidate"
})
public class CreateCandidateViaOpeningResponse {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected Profile candidate;

    /**
     * Gets the value of the candidate property.
     * 
     * @return
     *     possible object is
     *     {@link Profile }
     *     
     */
    public Profile getCandidate() {
        return candidate;
    }

    /**
     * Sets the value of the candidate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Profile }
     *     
     */
    public void setCandidate(Profile value) {
        this.candidate = value;
    }

}
