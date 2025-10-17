
package eduhk.fhr.web.soap.user;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getUsers complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getUsers">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestByGetUsersDto" type="{http://ws.mrted.com/}requestByGetUsersDto" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getUsers", propOrder = {
    "requestByGetUsersDto"
})
public class GetUsers {

    protected RequestByGetUsersDto requestByGetUsersDto;

    /**
     * Gets the value of the requestByGetUsersDto property.
     * 
     * @return
     *     possible object is
     *     {@link RequestByGetUsersDto }
     *     
     */
    public RequestByGetUsersDto getRequestByGetUsersDto() {
        return requestByGetUsersDto;
    }

    /**
     * Sets the value of the requestByGetUsersDto property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestByGetUsersDto }
     *     
     */
    public void setRequestByGetUsersDto(RequestByGetUsersDto value) {
        this.requestByGetUsersDto = value;
    }

}
