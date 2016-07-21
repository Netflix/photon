
package org.smpte_ra.schemas.st2067_2_2016;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for currentUnitsToken.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="currentUnitsToken">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="amps"/>
 *     &lt;enumeration value="milliamps"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "currentUnitsToken", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/")
@XmlEnum
public enum CurrentUnitsToken {

    @XmlEnumValue("amps")
    AMPS("amps"),
    @XmlEnumValue("milliamps")
    MILLIAMPS("milliamps");
    private final String value;

    CurrentUnitsToken(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CurrentUnitsToken fromValue(String v) {
        for (CurrentUnitsToken c: CurrentUnitsToken.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
