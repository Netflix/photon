
package org.smpte_ra.schemas.st2067_2_2013;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for timeUnitToken.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="timeUnitToken">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="week"/>
 *     &lt;enumeration value="day"/>
 *     &lt;enumeration value="hour"/>
 *     &lt;enumeration value="minute"/>
 *     &lt;enumeration value="second"/>
 *     &lt;enumeration value="millisecond"/>
 *     &lt;enumeration value="microsecond"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "timeUnitToken", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/")
@XmlEnum
public enum TimeUnitToken {

    @XmlEnumValue("week")
    WEEK("week"),
    @XmlEnumValue("day")
    DAY("day"),
    @XmlEnumValue("hour")
    HOUR("hour"),
    @XmlEnumValue("minute")
    MINUTE("minute"),
    @XmlEnumValue("second")
    SECOND("second"),
    @XmlEnumValue("millisecond")
    MILLISECOND("millisecond"),
    @XmlEnumValue("microsecond")
    MICROSECOND("microsecond");
    private final String value;

    TimeUnitToken(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TimeUnitToken fromValue(String v) {
        for (TimeUnitToken c: TimeUnitToken.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
