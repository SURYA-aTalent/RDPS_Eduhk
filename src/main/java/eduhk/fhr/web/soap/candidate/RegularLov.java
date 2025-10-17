
package eduhk.fhr.web.soap.candidate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for regularLov complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="regularLov">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.mrted.com/}reflectiveLov">
 *       &lt;sequence>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "regularLov")
@XmlSeeAlso({
    RateType.class,
    WorkUnit.class,
    ContractorType.class,
    CompensationPeriod.class,
    ApplicationSrcType.class,
    ScheduleType.class,
    CandidateStatus.class,
    Currency.class,
    Sex.class,
    DataPrivacy.class,
    WorkPeriod.class,
    RateUnitType.class,
    DefCondFrom.class,
    MaritalStatus.class,
    SourcingMedium.class,
    Citizenship.class
})
public abstract class RegularLov
    extends ReflectiveLov
{


}
