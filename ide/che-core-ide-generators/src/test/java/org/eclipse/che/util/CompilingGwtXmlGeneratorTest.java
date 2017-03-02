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
import com.google.common.io.Files;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.xml.XMLTree;
import org.eclipse.che.util.CompilingGwtXmlGenerator.GwtXmlGeneratorConfig;
import org.eclipse.che.util.CompilingGwtXmlGenerator.GwtXmlModuleSearcher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CompilingGwtXmlGeneratorTest {

    File testRoot;

    @BeforeMethod
    public void setUp() {
        testRoot = Files.createTempDir();
    }

    @AfterMethod
    public void cleanup() {
        IoUtil.deleteRecursive(testRoot);
    }

    @Test
    public void shouldFindGwtXmlModules() {
        //given
        Set<String> excludePackages = Collections.emptySet();
        Set<String> includePackages = Collections.emptySet();
        GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(excludePackages, includePackages);

        //when
        Set<String> actual = searcher.getGwtModulesFromClassPath();
        //then
        assertFalse(actual.isEmpty());
    }

    @Test
    public void shouldBeAbleToIncludeOnlyGwtModuleFrom() {
        //given
        Set<String> excludePackages = Collections.emptySet();
        Set<String> includePackages = ImmutableSet.of("elemental");
        GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(excludePackages, includePackages);

        //when
        Set<String> actual = searcher.getGwtModulesFromClassPath();
        //then
        assertFalse(actual.isEmpty());
        assertEquals(actual.size(), 3);

        assertTrue(actual.contains("elemental/Json.gwt.xml"));
        assertTrue(actual.contains("elemental/Collections.gwt.xml"));
        assertTrue(actual.contains("elemental/Elemental.gwt.xml"));
    }


    @Test
    public void shouldBeAbleToExcludeGwtModuleFrom() {
        //given
        Set<String> excludePackages = ImmutableSet.of("elemental");
        Set<String> includePackages = Collections.emptySet();
        GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(excludePackages, includePackages);
        //when
        Set<String> actual = searcher.getGwtModulesFromClassPath();
        //then
        assertFalse(actual.isEmpty());
        assertFalse(actual.contains("elemental/Json.gwt.xml"));
        assertFalse(actual.contains("elemental/Collections.gwt.xml"));
        assertFalse(actual.contains("elemental/Elemental.gwt.xml"));
    }

    @Test
    public void shouldGenerateGwtXml() throws IOException {
        //given
        Set<String> gwtModule = ImmutableSet.of("org/mydomain/Printer.gwt.xml");
        GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                new GwtXmlGeneratorConfig(gwtModule, testRoot);
        CompilingGwtXmlGenerator gwtXmlGenerator = new CompilingGwtXmlGenerator(gwtXmlGeneratorConfig);
        //when
        File actual = gwtXmlGenerator.generateGwtXml();
        //then
        XMLTree tree = XMLTree.from(actual);

        assertNotNull(actual);
    }


}
