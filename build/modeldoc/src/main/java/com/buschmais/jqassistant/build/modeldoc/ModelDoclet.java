package com.buschmais.jqassistant.build.modeldoc;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;

/**
 * Doclet for rendering information about xo nodes and relations.
 * 
 * @author ronald.kunzmann@buschmais.com
 *
 */
public class ModelDoclet extends Doclet {

	private static final String TARGET_FOLDER_PARAM = "-targetFolder";
	private static final String DEFAULT_TARGET_FOLDER = ".." + File.separator
			+ "jqassiatant" + File.separator + "plugindoc";
	private static final String MODEL_ADOC = "model.adoc";

	public static boolean start(RootDoc root) {
		return new ModelDocletRenderer(getOutputFile(root.options()))
				.render(root);
	}

	public static int optionLength(String option) {
		if (TARGET_FOLDER_PARAM.equals(option)) {
			return 2;
		}
		return Standard.optionLength(option);
	}

	public static boolean validOptions(String[][] options,
			DocErrorReporter errorReporter) {
		for (String[] strings : options) {
			System.out.println(Arrays.toString(strings));
		}
		for (String[] o : options) {
			if (o[0].equals(TARGET_FOLDER_PARAM) && StringUtils.isBlank(o[1])) {
				return false;
			}
		}
		return Standard.validOptions(options, errorReporter);
	}

	private static File getOutputFile(String[][] options) {
		System.out.println(Arrays.toString(options));
		for (String[] o : options) {
			if (o[0].equals(TARGET_FOLDER_PARAM)) {
				String pathname = o[1] + File.separator + MODEL_ADOC;
				System.out.println(pathname);
				return new File(pathname);
			}
		}
		// Fall back to the current working directory.

		return new File(DEFAULT_TARGET_FOLDER + File.separator + MODEL_ADOC);

	}

	/**
	 * NOTE: Without this method present and returning LanguageVersion.JAVA_1_5,
	 * Javadoc will not process generics because it assumes
	 * LanguageVersion.JAVA_1_1
	 * 
	 * @return language version (hard coded to LanguageVersion.JAVA_1_5)
	 */
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

}
