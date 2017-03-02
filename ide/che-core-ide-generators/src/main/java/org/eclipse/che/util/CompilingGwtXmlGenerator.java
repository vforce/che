/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.util;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.getConfigurationBuilder;

/**
 * Generates uber IDE.gwt.xml from part found in class path.
 * @author Sergii Kabashniuk
 */
public class CompilingGwtXmlGenerator {

    public static final String DEFAULT_GWT_XML_PATH    = "org/eclipse/che/ide/IDE.gwt.xml";
    public static final String DEFAULT_GWT_ETNRY_POINT = "org.eclipse.che.ide.client.IDE";
    public static final String DEFAULT_STYLE_SHEET     = "IDE.css";

    private final GwtXmlGeneratorConfig config;

    public CompilingGwtXmlGenerator(GwtXmlGeneratorConfig config) {
        this.config = config;
    }

    public File generateGwtXml() throws IOException {
        File gwtXml = new File(config.getGenerationRoot(), config.getGwtFileName());
        if (gwtXml.isDirectory() || gwtXml.exists()) {
            throw new IOException(gwtXml.getAbsolutePath() + " already exists or directory");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
        builder.append("<module rename-to='_app'>").append("\n");
        for (String gwtModule : config.getGwtXmlModules()) {
            builder.append("    <inherits name=\"")
                   .append(gwtModule.replace("/", ".").substring(0, gwtModule.length() - 8))
                   .append("\"/>").append("\n");
        }
        builder.append("    <stylesheet src=\"").append(config.getStylesheet()).append("\"/>").append("\n");
        builder.append("    <entry-point class='").append(config.getEntryPoint()).append("'/>").append("\n");
        if (config.isLoggingEnabled()) {
            builder.append("    <set-property name=\"gwt.logging.simpleRemoteHandler\" value=\"ENABLED\"/>").append("\n");
            builder.append("    <set-property name=\"gwt.logging.developmentModeHandler\" value=\"ENABLED\"/>").append("\n");
            builder.append("    <set-property name=\"gwt.logging.consoleHandler\" value=\"ENABLED\"/>").append("\n");
        } else {
            builder.append("    <set-property name=\"gwt.logging.simpleRemoteHandler\" value=\"DISABLED\"/>").append("\n");
            builder.append("    <set-property name=\"gwt.logging.developmentModeHandler\" value=\"DISABLED\"/>").append("\n");
            builder.append("    <set-property name=\"gwt.logging.consoleHandler\" value=\"DISABLED\"/>").append("\n");

        }
        builder.append("</module>").append("\n");

        // flush content
        FileUtils.writeStringToFile(gwtXml, builder.toString());
        return gwtXml;
    }


    /**
     * Entry point. --rootDir is the optional parameter.
     *
     * @param args
     */
    public static void main(String[] args) {

        try {
            System.out.println(" ------------------------------------------------------------------------ ");
            System.out.println("Searching for GWT");
            System.out.println(" ------------------------------------------------------------------------ ");
            Map<String, Set<String>> parsedArgs = GeneratorUtils.parseArgs(args);

            GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(parsedArgs.getOrDefault("excludePackages",
                                                                                             ImmutableSet.of("com.google",
                                                                                                             "elemental",
                                                                                                             "java.util",
                                                                                                             "java.lang"
                                                                                                            )),
                                                                     parsedArgs.getOrDefault("includePackages", Collections.emptySet()),
                                                                     Collections.emptySet());
            Set<String> gwtModules = searcher.getGwtModulesFromClassPath();
            System.out.println("Found " + gwtModules.size() + " gwt modules");


            GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                    new GwtXmlGeneratorConfig(gwtModules,
                                              new File(getSingleValueOrDefault(parsedArgs, "generationRoot", ".")),
                                              getSingleValueOrDefault(parsedArgs, "gwtFileName", DEFAULT_GWT_XML_PATH),
                                              getSingleValueOrDefault(parsedArgs, "entryPoint", DEFAULT_GWT_ETNRY_POINT),
                                              getSingleValueOrDefault(parsedArgs, "styleSheet", DEFAULT_STYLE_SHEET),
                                              Boolean.parseBoolean(getSingleValueOrDefault(parsedArgs, "loggingEnabled", "false"))
                    );
            CompilingGwtXmlGenerator gwtXmlGenerator = new CompilingGwtXmlGenerator(gwtXmlGeneratorConfig);
            gwtXmlGenerator.generateGwtXml();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // error
            System.exit(1);//NOSONAR
        }
    }

    private static String getSingleValueOrDefault(Map<String, Set<String>> parsedArgs, String key, String defaultValue) {
        Set<String> values = parsedArgs.get(key);
        return values != null ? values.iterator().next() : defaultValue;
    }


    /**
     * Class provides functionality of searching XXX.gwt.xml files in class path
     */
    public static class GwtXmlModuleSearcher {
        private final Set<String> excludePackages;
        private final Set<String> includePackages;
        private final Set<URL>    urls;

        public GwtXmlModuleSearcher(Set<String> excludePackages, Set<String> includePackages, Set<URL> urls) {
            this.excludePackages = excludePackages;
            this.includePackages = includePackages;
            this.urls = urls;
        }

        /**
         * Searches XXX.gwt.xml files in class path
         *

         * @return - set of XXX.gwt.xml files found in class path
         */
        public Set<String> getGwtModulesFromClassPath() {
            ConfigurationBuilder configurationBuilder = getConfigurationBuilder();
            if (urls != null && urls.size() > 0) {
                configurationBuilder.addUrls(urls);
            }
            FilterBuilder filterBuilder = new FilterBuilder();
            for (String includePackage : includePackages) {
                filterBuilder.includePackage(includePackage);
            }
            for (String excludePackage : excludePackages) {
                filterBuilder.excludePackage(excludePackage);
            }


            configurationBuilder.setScanners(new ResourcesScanner()).filterInputsBy(filterBuilder);

            Reflections reflection = new Reflections(configurationBuilder);
            return reflection.getResources(name -> name.endsWith(".gwt.xml"));

        }
    }

    public static class GwtXmlGeneratorConfig {
        private final Set<String> gwtXmlModules;

        private final File generationRoot;

        private final String gwtFileName;

        private final String entryPoint;

        private final String stylesheet;

        private final boolean isLoggingEnabled;

        public GwtXmlGeneratorConfig(Set<String> gwtXmlModules,
                                     File generationRoot,
                                     String gwtFileName,
                                     String entryPoint,
                                     String stylesheet,
                                     boolean isLoggingEnabled) {
            this.gwtXmlModules = gwtXmlModules;
            this.generationRoot = generationRoot;
            this.gwtFileName = gwtFileName;
            this.entryPoint = entryPoint;
            this.stylesheet = stylesheet;
            this.isLoggingEnabled = isLoggingEnabled;
        }

        public GwtXmlGeneratorConfig(Set<String> gwtXmlModules,
                                     File generationRoot) {
            this(gwtXmlModules, generationRoot, DEFAULT_GWT_XML_PATH, DEFAULT_GWT_ETNRY_POINT, DEFAULT_STYLE_SHEET, false);
        }

        public Set<String> getGwtXmlModules() {
            return gwtXmlModules;
        }

        public File getGenerationRoot() {
            return generationRoot;
        }

        public String getGwtFileName() {
            return gwtFileName;
        }

        public String getEntryPoint() {
            return entryPoint;
        }

        public String getStylesheet() {
            return stylesheet;
        }

        public boolean isLoggingEnabled() {
            return isLoggingEnabled;
        }
    }
}
