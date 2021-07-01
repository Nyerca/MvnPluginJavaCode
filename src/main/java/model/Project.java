package model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "project", namespace = "http://maven.apache.org/POM/4.0.0")
// order of the fields in XML
// @XmlType(propOrder = {"price", "name"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Project {

    public String modelVersion;
    public String groupId;
    public String artifactId;
    public String version;
    public Object packaging;
    public Object licenses;
    public Object properties;
    public Object repositories;
    public Object dependencies;
    public Object build;

}