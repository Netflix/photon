
package org.smpte_ra.schemas.st2067_2_2013;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for currentModeToken.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="currentModeToken">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="AC"/>
 *     &lt;enumeration value="DC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "currentModeToken", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/")
@XmlEnum
public enum CurrentModeToken {

    AC,
    DC;

    public String value() {
        return name();
    }

    public static CurrentModeToken fromValue(String v) {
        return valueOf(v);
    }

}
