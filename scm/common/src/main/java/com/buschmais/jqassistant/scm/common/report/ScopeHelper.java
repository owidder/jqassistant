package com.buschmais.jqassistant.scm.common.report;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scope;

import org.slf4j.Logger;

/**
 * Provides common functionality for working with scopes.
 */
public class ScopeHelper {

    private final Logger logger;

    /**
     * Constructor.
     * 
     * @param log
     *            The logger used to log all messages
     */
    public ScopeHelper(Logger log) {
        this.logger = log;
    }

    /**
     * Print a list of available scopes to the console.
     * 
     * @param scopes
     *            The available scopes.
     */
    public void printScopes(Map<String, Scope> scopes) {
        logger.info("Scopes [" + scopes.size() + "]");
        for (String scopeName : scopes.keySet()) {
            logger.info("\t" + scopeName);
        }
    }
}
