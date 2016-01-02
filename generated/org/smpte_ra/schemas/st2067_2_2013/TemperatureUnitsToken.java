
package org.smpte_ra.schemas.st2067_2_2013;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for temperatureUnitsToken.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="temperatureUnitsToken">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="celsius"/>
 *     &lt;enumeration value="fahrenheit"/>
 *     &lt;enumeration value="kelvin"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "temperatureUnitsToken", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/")
@XmlEnum
public enum TemperatureUnitsToken {

    @XmlEnumValue("celsius")
    CELSIUS("celsius"),
    @XmlEnumValue("fahrenheit")
    FAHRENHEIT("fahrenheit"),
    @XmlEnumValue("kelvin")
    KELVIN("kelvin");
    private final String value;

    TemperatureUnitsToken(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TemperatureUnitsToken fromValue(String v) {
        for (TemperatureUnitsToken c: TemperatureUnitsToken.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
