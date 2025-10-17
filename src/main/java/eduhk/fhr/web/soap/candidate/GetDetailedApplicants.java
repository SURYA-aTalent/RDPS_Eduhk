
package eduhk.fhr.web.soap.candidate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getDetailedApplicants complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getDetailedApplicants">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ws.mrted.com/}detailedSearchCriteriaDto" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDetailedApplicants", propOrder = {
    "detailedSearchCriteriaDto"
})
public class GetDetailedApplicants {

    @XmlElement(namespace = "http://ws.mrted.com/")
    protected DetailedSearchCriteriaDto detailedSearchCriteriaDto;

    /**
     * Gets the value of the detailedSearchCriteriaDto property.
     * 
     * @return
     *     possible object is
     *     {@link DetailedSearchCriteriaDto }
     *     
     */
    public DetailedSearchCriteriaDto getDetailedSearchCriteriaDto() {
        return detailedSearchCriteriaDto;
    }

    /**
     * Sets the value of the detailedSearchCriteriaDto property.
     * 
     * @param value
     *     allowed object is
     *     {@link DetailedSearchCriteriaDto }
     *     
     */
    public void setDetailedSearchCriteriaDto(DetailedSearchCriteriaDto value) {
        this.detailedSearchCriteriaDto = value;
    }

}
