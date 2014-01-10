package com.buschmais.jqassistant.plugin.osgi.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.osgi.test.api.data.Request;
import com.buschmais.jqassistant.plugin.osgi.test.api.service.Service;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Contains tests regarding manifest files.
 */
public class OsgiBundleIT extends AbstractPluginIT {

    /**
     * Verifies the concept "osgi:Bundle".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void bundle() throws IOException, AnalyzerException {
        URL[] urls = getManifestUrls();
        scanURLs(urls);
        applyConcept("osgi-bundle:Bundle");
        store.beginTransaction();
        assertThat(query("MATCH (bundle:OSGI:BUNDLE) WHERE bundle.BUNDLESYMBOLICNAME='com.buschmais.jqassistant.plugin.osgi.test' RETURN bundle").getColumn("bundle").size(), equalTo(1));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "osgi:ExportPackage".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void exportedPackages() throws IOException, AnalyzerException {
        URL[] urls = getManifestUrls();
        scanURLs(urls);
        scanClassesDirectory(Service.class);
        applyConcept("osgi-bundle:ExportPackage");
        store.beginTransaction();
        List<PackageDescriptor> packages = query("MATCH (b:OSGI:BUNDLE)-[:EXPORTS]->(p:PACKAGE) RETURN p").getColumn("p");
        assertThat(packages.size(), equalTo(2));
        assertThat(packages, hasItems(packageDescriptor(Request.class.getPackage()), packageDescriptor(Service.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Retrieves the URLs of all MANIFEST.MF file available in classpath directories (i.e. no JAR files).
     *
     * @return The URLs.
     * @throws IOException If a problem occurs.
     */
    private URL[] getManifestUrls() throws IOException {
        Enumeration<URL> resources = OsgiBundleIT.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        List<URL> urls = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if ("file".equals(url.getProtocol())) {
                urls.add(url);
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }
}