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

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.getConfigurationBuilder;

/**
 * Generates uber IDE.gwt.xml from part found in class path.
 * @author Sergii Kabashniuk
 */
public class CompilingGwtXmlGenerator {

    public static final String DEFAULT_GWT_XML_PATH    = "org/eclipse/che/ide/IDE.gwt.xml";
    public static final String DEFAULT_GWT_ETNRY_POINT = "org.eclipse.che.ide.client.IDE";
    public static final String DEFAULT_STYLESHEET      = "IDE.css";

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
            File rootFolder = GeneratorUtils.getRootFolder(args);
            System.out.println(" ------------------------------------------------------------------------ ");
            System.out.println("Searching for GWT");
            System.out.println(" ------------------------------------------------------------------------ ");

            // find all gwt.xml
            //findGwtXml();
            //generateGwtXml(rootFolder);
            throw new IOException();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // error
            System.exit(1);//NOSONAR
        }
    }

//    private static void generateGwtXml(File rootFolder) throws IOException {
//        File extManager = new File(rootFolder, GWT_XML_PATH);
//        StringBuilder builder = new StringBuilder();
//        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
//        builder.append("<module rename-to='_app'>").append("\n");
//        for (String getXmlLocation : gwtXml) {
//            builder.append("    <inherits name=\"")
//                   .append(getXmlLocation.replace("/", ".").substring(0, getXmlLocation.length() - 8))
//                   .append("\"/>").append("\n");
//        }
//        builder.append("<stylesheet src=\"IDE.css\"/>").append("\n");
//        builder.append("<entry-point class='org.eclipse.che.ide.client.IDE'/>").append("\n");
//        builder.append("</module>").append("\n");
//
//        // flush content
//        FileUtils.writeStringToFile(extManager, builder.toString());
//    }

//    private static void findGwtXml() {
//        ConfigurationBuilder configurationBuilder = getConfigurationBuilder();
//        configurationBuilder.setScanners(new ResourcesScanner())
//                            .filterInputsBy(new FilterBuilder()
//                                                    .excludePackage("com.google")
//                                                    .excludePackage("elemental")
//                                                    .excludePackage("java.util")
//                                                    .excludePackage("java.lang"));
//
//        Reflections reflection = new Reflections(configurationBuilder);
//        gwtXml.addAll(reflection.getResources(name -> name.endsWith(".gwt.xml")));
//        gwtXml.forEach(System.out::println);
//
//    }

    /**
     * Class provides functionality of searching XXX.gwt.xml files in class path
     */
    public static class GwtXmlModuleSearcher {
        private final Set<String> excludePackages;
        private final Set<String> includePackages;

        public GwtXmlModuleSearcher(Set<String> excludePackages, Set<String> includePackages) {
            this.excludePackages = excludePackages;
            this.includePackages = includePackages;
        }

        /**
         * Searches XXX.gwt.xml files in class path
         *

         * @return - set of XXX.gwt.xml files found in class path
         */
        public Set<String> getGwtModulesFromClassPath() {
            ConfigurationBuilder configurationBuilder = getConfigurationBuilder();
            FilterBuilder filterBuilder = new FilterBuilder();
            for (String excludePackage : excludePackages) {
                filterBuilder.excludePackage(excludePackage);
            }
            for (String includePackage : includePackages) {
                filterBuilder.includePackage(includePackage);
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
            this(gwtXmlModules, generationRoot, DEFAULT_GWT_XML_PATH, DEFAULT_GWT_ETNRY_POINT, DEFAULT_STYLESHEET, false);
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
