package com.buschmais.jqassistant.build.modeldoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.build.modeldoc.DocletRelation.Cardinality;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

/**
 * Renderer for asciidoc files. Renders information about nodes and relations.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
public class ModelDocletRenderer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ModelDocletRenderer.class);
	private List<DocletNode> docletNodes = new ArrayList<>();
	private Map<String, String> knownClassLabels = new HashMap<>();

	private File target;

	ModelDocletRenderer(File target) {
		this.target = target;
	}

	/**
	 * Renders asciidoc information for given class.
	 * 
	 * @param root
	 *            RootDoc for given class.
	 * @return true, if rendering succeeded, false otherwise
	 */
	boolean render(RootDoc root) {
		try {
			ClassDoc[] classes = root.classes();
			for (int i = 0; i < classes.length; i++) {
				addNodeInformation(classes[i]);
			}
			clear();
			writeAsciiDocFiles();

		} catch (Exception e) {
			System.out
					.println("##################### Fehler " + e.getMessage());
			LOGGER.warn("Error creating asciidoc.", e);
			return false;
		}
		return true;
	}

	private void writeLine(BufferedWriter writer, String text)
			throws IOException {
		writer.write(text);
		writer.newLine();
	}

	private void clear() throws IOException {
		if (this.target.exists()) {
			this.target.delete();
		}
	}

	private void writeAsciiDocFiles() throws IOException {
		Set<DocletRelation> allDocletRelations = new LinkedHashSet<>();
		for (DocletNode node : docletNodes) {
			if (!this.target.exists()) {
				this.target.getParentFile().mkdirs();
				this.target.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(this.target, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos,
					StandardCharsets.UTF_8);
			BufferedWriter writer = new BufferedWriter(osw);

			writeLine(writer, "[[" + node.getLabel() + "]]");
			writeLine(writer, "==== " + node.getLabel());
			String description = node.getDescription();
			if (!StringUtils.isBlank(description)) {
				writeLine(writer, description);
			}
			Set<DocletProperty> docletProperties = node.getProperties();
			if (!docletProperties.isEmpty()) {
				writer.newLine();
				writeLine(writer, ".Properties of " + node.getLabel());
				writeLine(writer, "[options=\"header\"]");
				writeLine(writer, "|====");
				writeLine(writer, "| Name | Description");

				for (DocletProperty docletProperty : docletProperties) {
					writeLine(writer, "| " + docletProperty.getName() + " | "
							+ docletProperty.getDescription());
				}
				writeLine(writer, "|====");
			}
			List<DocletRelation> docletRelations = node.getRelations();
			if (!docletRelations.isEmpty()) {
				writer.newLine();
				writeLine(writer, ".Relations of " + node.getLabel());
				writeLine(writer, "[options=\"header\"]");
				writeLine(writer, "|====");
				writeLine(writer,
						"| Name | Target label(s) | Cardinality | Description");
				for (DocletRelation docletRelation : docletRelations) {
					writeLine(writer, "| "
							+ docletRelation.getName()
							+ " | <<"
							+ docletRelation.getTargetLabel()
							+ ">> | "
							+ docletRelation.getCardinality()
									.getRepresentation() + " | "
							+ docletRelation.getDescription());
					if (!docletRelation.getProperties().isEmpty()) {
						allDocletRelations.add(docletRelation);
					}
				}
				writeLine(writer, "|====");
			}
			writer.newLine();
			writer.close();
		}
		if (!this.target.exists()) {
			return;
		}
		// Render relation properties
		FileOutputStream fos = new FileOutputStream(this.target, true);
		OutputStreamWriter osw = new OutputStreamWriter(fos,
				StandardCharsets.UTF_8);
		BufferedWriter writer = new BufferedWriter(osw);
		for (DocletRelation docletRelation : allDocletRelations) {
			Set<DocletProperty> properties = docletRelation.getProperties();
			if (!properties.isEmpty()) {
				writer.newLine();
				writeLine(writer, ".Properties of :" + docletRelation.getName());
				writeLine(writer, "[options=\"header\"]");
				writeLine(writer, "|====");
				writeLine(writer, "| Name | Description");
				for (DocletProperty docletProperty : properties) {
					writeLine(writer, "| " + docletProperty.getName() + " | "
							+ docletProperty.getDescription());
				}
				writeLine(writer, "|====");
			}
		}
		writer.close();

	}

	/**
	 * Adds information about given class if it is a node.
	 * 
	 * @param classDoc
	 *            The given class.
	 */
	private void addNodeInformation(ClassDoc classDoc) {
		if (isAbstract(classDoc)) {
			return;
		}
		String label = getLabel(classDoc);
		if (null == label) {
			return;
		}

		DocletNode docletNode = new DocletNode();
		docletNode.setLabel(label);
		docletNodes.add(docletNode);

		docletNode.setDescription(classDoc.commentText());
		addNodeInformation(classDoc, docletNode);
	}

	/**
	 * Adds information about given class and all its super interfaces to the
	 * docletNode.
	 * 
	 * @param classDoc
	 *            The given class.
	 * @param docletNode
	 *            The DocletNode object.
	 */
	private void addNodeInformation(ClassDoc classDoc, DocletNode docletNode) {
		addRelationsAndProperties(classDoc, docletNode);
		for (ClassDoc doc : classDoc.interfaces()) {
			addNodeInformation(doc, docletNode);
		}
	}

	/**
	 * Get label information for given class.
	 * 
	 * @param classDoc
	 *            The given class.
	 * @return The complete label.
	 */
	private String getLabel(ClassDoc classDoc) {
		String label = knownClassLabels.get(classDoc.qualifiedName());
		if (null == label) {
			Set<String> labelSet = new LinkedHashSet<>();
			addLabel(classDoc, labelSet);
			if (!labelSet.isEmpty()) {
				label = StringUtils.join(labelSet, "");
				knownClassLabels.put(classDoc.qualifiedName(), label);
			}
		}
		return label;
	}

	/**
	 * Adds label information of given class and all super interfaces to the set
	 * of labels.
	 * 
	 * @param classDoc
	 *            The given class.
	 * @param labelSet
	 *            The set of labels.
	 */
	private void addLabel(ClassDoc classDoc, Set<String> labelSet) {
		AnnotationDesc[] annotations = classDoc.annotations();
		String label = null;
		label = getAnnotationValue(annotations, Label.class);
		if (null != label) {
			labelSet.add(":" + label);
		}
		for (ClassDoc doc : classDoc.interfaces()) {
			addLabel(doc, labelSet);
		}
	}

	/**
	 * Searches through all annotation descriptions for an annotation of the
	 * given type and returns its value.
	 * 
	 * @param annotations
	 *            All annotation descriptions.
	 * @param annotationClass
	 *            The given annotation class.
	 * @return The value of the annotation or null, if there is no annotation of
	 *         the given class.
	 */
	private String getAnnotationValue(AnnotationDesc[] annotations,
			Class<?> annotationClass) {
		for (AnnotationDesc annotationDesc : annotations) {
			if (annotationClass.getCanonicalName().equals(
					annotationDesc.annotationType().qualifiedName())) {
				for (ElementValuePair elementValue : annotationDesc
						.elementValues()) {
					if ("value".equals(elementValue.element().name())) {
						return elementValue.value().toString()
								.replaceAll("\"", "");
					}

				}
			}
		}
		return null;
	}

	/**
	 * Check if the given class is annotated by @Abstract.
	 * 
	 * @param classDoc
	 *            The given class.
	 * @return true, if the given class is annotated by @Abstract.
	 */
	private boolean isAbstract(ClassDoc classDoc) {
		AnnotationDesc[] annotations = classDoc.annotations();
		for (AnnotationDesc annotationDesc : annotations) {
			if (Abstract.class.getCanonicalName().equals(
					annotationDesc.annotationType().qualifiedName())) {
				return true;
			}
		}
		return false;
	}

	private void addRelationsAndProperties(ClassDoc classDoc,
			DocletNode docletNode) {
		MethodDoc[] methods = classDoc.methods();
		for (MethodDoc methodDoc : methods) {
			DocletRelation docletRelation = getRelation(methodDoc);
			if (null != docletRelation) {
				docletNode.getRelations().add(docletRelation);
				continue;
			}
			DocletProperty property = getProperty(methodDoc);
			if (null != property) {
				docletNode.getProperties().add(property);
			}
		}

	}

	private DocletProperty getProperty(MethodDoc methodDoc) {
		String annotationValue = getAnnotationValue(methodDoc.annotations(),
				Property.class);
		DocletProperty docletProperty = new DocletProperty();
		docletProperty.setDescription(methodDoc.commentText());
		if (null != annotationValue) {
			docletProperty.setName(annotationValue);
			return docletProperty;
		} else {
			String name = methodDoc.name();
			if (name.startsWith("is")) {
				docletProperty.setName(name.substring(2));
				return docletProperty;
			} else if (name.startsWith("get")) {
				docletProperty.setName(name.substring(3));
				return docletProperty;
			}
		}
		return null;
	}

	private Cardinality getRelationCardinality(Type returnType) {
		try {
			Class<?> clazz = Class.forName(returnType.qualifiedTypeName());
			if (java.util.Collection.class.isAssignableFrom(clazz)) {
				return Cardinality.ToMany;
			}
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			return Cardinality.ToOne;
		}
		return null;
	}

	private ClassDoc getRelationTargetClass(Type returnType,
			Cardinality cardinality) {
		if (cardinality == Cardinality.ToOne) {
			return returnType.asClassDoc();
		}
		ParameterizedType asParameterizedType = returnType
				.asParameterizedType();
		Type[] typeArguments = asParameterizedType.typeArguments();
		return typeArguments[0].asClassDoc();
	}

	private DocletRelation getRelation(MethodDoc methodDoc) {
		DocletRelation docletRelation = null;
		Type returnType = methodDoc.returnType();
		// relation without attributes
		String annotationValueRelation = getAnnotationValue(
				methodDoc.annotations(), Relation.class);
		if (null != annotationValueRelation) {
			docletRelation = new DocletRelation();
			docletRelation.setName(annotationValueRelation);
			docletRelation.setDescription(methodDoc.commentText());
			docletRelation.setCardinality(getRelationCardinality(returnType));
			docletRelation.setTargetLabel(getLabel(getRelationTargetClass(
					returnType, docletRelation.getCardinality())));
			return docletRelation;
		}

		// relation with attributes
		for (AnnotationDesc annotationDesc : methodDoc.annotations()) {
			if (Outgoing.class.getCanonicalName().equals(
					annotationDesc.annotationType().qualifiedName())) {
				docletRelation = new DocletRelation();
				docletRelation.setDescription(methodDoc.commentText());
				docletRelation
						.setCardinality(getRelationCardinality(returnType));
				ClassDoc relation = getRelationTargetClass(returnType,
						docletRelation.getCardinality());

				docletRelation.setName(getAnnotationValue(
						relation.annotations(), Relation.class));
				MethodDoc[] methods = relation.methods();
				for (MethodDoc mDoc : methods) {
					DocletProperty property = getProperty(mDoc);
					if (null != property) {
						docletRelation.getProperties().add(property);
					}
					for (AnnotationDesc ad : mDoc.annotations()) {
						if (Incoming.class.getCanonicalName().equals(
								ad.annotationType().qualifiedName())) {
							docletRelation
									.setTargetLabel(getLabel(getRelationTargetClass(
											mDoc.returnType(),
											Cardinality.ToOne)));
							break;
						}
					}
				}
			}
		}
		return docletRelation;
	}

}
