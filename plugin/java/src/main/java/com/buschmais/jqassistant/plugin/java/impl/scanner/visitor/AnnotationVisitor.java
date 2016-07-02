package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotationTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

/**
 * An annotation visitor.
 *
 * Adds a dependency from the annotated types to the types of the annotation
 * values.
 *
 */
public class AnnotationVisitor extends AbstractAnnotationVisitor<AnnotationTypeDescriptor> {

    /**
     * Constructor.
     * 
     * @param visitorHelper
     *            The
     *            {@link com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper}
     *            .
     */
    protected AnnotationVisitor(TypeCache.CachedType containingType, AnnotationTypeDescriptor descriptor, VisitorHelper visitorHelper) {
        super(containingType, descriptor, visitorHelper);
    }

    @Override
    protected void setValue(AnnotationTypeDescriptor descriptor, ValueDescriptor<?> value) {
        descriptor.getValue().add(value);
    }
}
