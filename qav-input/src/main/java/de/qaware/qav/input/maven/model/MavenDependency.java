package de.qaware.qav.input.maven.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

/**
 * Represents a Maven dependency, with groupId, artifactId, and scope.
 */
@Data
@JacksonXmlRootElement(localName = "project")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenDependency {

    private String groupId;
    private String artifactId;
    private String scope;
}
