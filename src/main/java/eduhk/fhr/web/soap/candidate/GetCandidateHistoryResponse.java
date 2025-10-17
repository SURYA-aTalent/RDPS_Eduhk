
package eduhk.fhr.web.soap.candidate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getCandidateHistoryResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getCandidateHistoryResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="candidateHistory" type="{http://ws.mrted.com/}candidateHistoryDto" minOccurs="0" form="qualified"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getCandidateHistoryResponse", propOrder = {
    "candidateHistory"
})
public class GetCandidateHistoryResponse {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected CandidateHistoryDto candidateHistory;

    /**
     * Gets the value of the candidateHistory property.
     * 
     * @return
     *     possible object is
     *     {@link CandidateHistoryDto }
     *     
     */
    public CandidateHistoryDto getCandidateHistory() {
        return candidateHistory;
    }

    /**
     * Sets the value of the candidateHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link CandidateHistoryDto }
     *     
     */
    public void setCandidateHistory(CandidateHistoryDto value) {
        this.candidateHistory = value;
    }

}
