
package eduhk.fhr.web.soap.candidate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getCandidatesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getCandidatesResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="candidates" type="{http://ws.mrted.com/}candidateIdsDTO" minOccurs="0" form="qualified"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getCandidatesResponse", propOrder = {
    "candidates"
})
public class GetCandidatesResponse {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected CandidateIdsDTO candidates;

    /**
     * Gets the value of the candidates property.
     * 
     * @return
     *     possible object is
     *     {@link CandidateIdsDTO }
     *     
     */
    public CandidateIdsDTO getCandidates() {
        return candidates;
    }

    /**
     * Sets the value of the candidates property.
     * 
     * @param value
     *     allowed object is
     *     {@link CandidateIdsDTO }
     *     
     */
    public void setCandidates(CandidateIdsDTO value) {
        this.candidates = value;
    }

}
