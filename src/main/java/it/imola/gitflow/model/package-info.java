@XmlSchema(
        namespace="http://maven.apache.org/POM/4.0.0",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns={
                @XmlNs(prefix="xsi", namespaceURI="http://www.w3.org/2001/XMLSchema-instance")
        }
)
package it.imola.gitflow.model;

import javax.xml.bind.annotation.*;