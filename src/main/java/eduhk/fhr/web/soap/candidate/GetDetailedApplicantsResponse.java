
package eduhk.fhr.web.soap.candidate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getDetailedApplicantsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getDetailedApplicantsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ws.mrted.com/}detailedApplicationDto" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDetailedApplicantsResponse", propOrder = {
    "detailedApplicationDto"
})
public class GetDetailedApplicantsResponse {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected DetailedApplicationDto detailedApplicationDto;

    /**
     * Gets the value of the detailedApplicationDto property.
     * 
     * @return
     *     possible object is
     *     {@link DetailedApplicationDto }
     *     
     */
    public DetailedApplicationDto getDetailedApplicationDto() {
        return detailedApplicationDto;
    }

    /**
     * Sets the value of the detailedApplicationDto property.
     * 
     * @param value
     *     allowed object is
     *     {@link DetailedApplicationDto }
     *     
     */
    public void setDetailedApplicationDto(DetailedApplicationDto value) {
        this.detailedApplicationDto = value;
    }

}
