package com.buschmais.jqassistant.build.modeldoc;

import java.util.LinkedHashSet;
import java.util.Set;

public class DocletNodeOrRelation {
    private String description;
    private Set<DocletProperty> properties = new LinkedHashSet<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<DocletProperty> getProperties() {
        return properties;
    }
}
