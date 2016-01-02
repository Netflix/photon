
package org.smpte_ra.schemas.st2067_2_2013;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for voltageUnitsToken.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="voltageUnitsToken">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="volts"/>
 *     &lt;enumeration value="millivolts"/>
 *     &lt;enumeration value="microvolts"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "voltageUnitsToken", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/")
@XmlEnum
public enum VoltageUnitsToken {

    @XmlEnumValue("volts")
    VOLTS("volts"),
    @XmlEnumValue("millivolts")
    MILLIVOLTS("millivolts"),
    @XmlEnumValue("microvolts")
    MICROVOLTS("microvolts");
    private final String value;

    VoltageUnitsToken(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VoltageUnitsToken fromValue(String v) {
        for (VoltageUnitsToken c: VoltageUnitsToken.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
