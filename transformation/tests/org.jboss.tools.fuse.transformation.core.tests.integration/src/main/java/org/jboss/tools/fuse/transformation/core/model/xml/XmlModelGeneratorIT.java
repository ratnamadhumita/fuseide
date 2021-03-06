/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.tools.fuse.transformation.core.model.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.assertj.core.api.Assertions;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.codemodel.JCodeModel;


public class XmlModelGeneratorIT {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	private static String XML_INST_PATH = "abc-order.xml";
	private static String XML_INST_PATH_2 = "doc-with-namespace.xml";
	private static String XML_INST_PATH_3 = "doc-with-multiple-namespaces.xml";
	private static String XML_SCHEMA_PATH = "multi-element.xsd";
	private static String XML_SCHEMA_PATH_2 = "abc-order.xsd";
	private static String XML_SCHEMA_PATH_3 = "Application.xsd";
	private static String XML_SCHEMA_GEN_PATH = "abc-order.xsd";

    @Test
    public void generateFromInstance() throws Exception {
		File xmlInst = getFile(XML_INST_PATH);
		File targetFolder = tmpFolder.newFolder("target");
		File genSchema = new File(targetFolder, XML_SCHEMA_GEN_PATH);
        XmlModelGenerator modelGen = new XmlModelGenerator();
		modelGen.generateFromInstance(xmlInst, genSchema, "test.generateFromInstance", targetFolder);
        // Check for generated schema
        Assert.assertTrue(genSchema.exists());
        // Check for generated classes
		File genDir = new File(targetFolder, "test/generateFromInstance");
        Assert.assertTrue(new File(genDir, "ObjectFactory.java").exists());
        Assert.assertTrue(new File(genDir, "ABCOrder.java").exists());
    }

    @Test
    public void generateFromInstanceWithMultipleNamespaces() throws Exception {
		// The test need to be launched in an OSGi platform to have a real check
		Assertions.assertThat(PlatformUI.getWorkbench()).isNotNull();
		File xmlInstance = getFile(XML_INST_PATH_3);
		File generatedSchema = new File(xmlInstance.getParentFile(), XML_INST_PATH_3 + ".xsd");
		File generatedSchemaA = new File(xmlInstance.getParentFile(), "bogus.com.a.xsd");
		File generatedSchemaB = new File(xmlInstance.getParentFile(), "bogus.com.b.xsd");
		File targetFolder = tmpFolder.newFolder("target");
        generatedSchema.deleteOnExit();
        generatedSchemaA.deleteOnExit();
        generatedSchemaB.deleteOnExit();
		File generatedObjectFactoryA = new File(targetFolder, "com/bogus/a/ObjectFactory.java");
		File generatedObjectFactoryB = new File(targetFolder, "com/bogus/b/ObjectFactory.java");
		File generatedObjectFactoryC = new File(targetFolder, "com/bogus/c/ObjectFactory.java");
		File generatedRoot = new File(targetFolder, "com/bogus/a/ObjectFactory.java");
        XmlModelGenerator modelGen = new XmlModelGenerator();

		JCodeModel codeModel = modelGen.generateFromInstance(xmlInstance, generatedSchema, null, targetFolder);

        Assert.assertTrue(generatedSchema.exists());
        Assert.assertTrue(generatedSchemaA.exists());
        Assert.assertTrue(generatedSchemaB.exists());
        Assert.assertTrue(generatedObjectFactoryA.exists());
        Assert.assertTrue(generatedObjectFactoryB.exists());
        Assert.assertTrue(generatedObjectFactoryC.exists());
        Assert.assertTrue(generatedRoot.exists());
        Map<String, String> mappings = modelGen.elementToClassMapping(codeModel);
        Assert.assertEquals(3, mappings.size());
        Assert.assertEquals("com.bogus.c.Root", mappings.get("root"));
        Assert.assertEquals("com.bogus.a.Element1", mappings.get("element1"));
        Assert.assertEquals("com.bogus.b.Element2", mappings.get("element2"));
    }

    @Test
    public void getGeneratedElementsMultipleElements() throws Exception {
		File xmlSchema = getFile(XML_SCHEMA_PATH);
        XmlModelGenerator modelGen = new XmlModelGenerator();
        JCodeModel codeModel = modelGen.generateFromSchema(
                                                           xmlSchema, "test.getGeneratedElementsMultipleElements", new File("target"));
        Map<String, String> mappings = modelGen.elementToClassMapping(codeModel);
        Assert.assertEquals(3, mappings.size());
        Assert.assertEquals("test.getGeneratedElementsMultipleElements.Header", mappings.get("header"));
        Assert.assertEquals("test.getGeneratedElementsMultipleElements.OrderItems", mappings.get("order-items"));
        Assert.assertEquals("test.getGeneratedElementsMultipleElements.ZABCOrder", mappings.get("ZABCOrder"));

    }

    @Test
    public void getGeneratedElementsSingleElement() throws Exception {
		File xmlSchema = getFile(XML_SCHEMA_PATH_2);
        XmlModelGenerator modelGen = new XmlModelGenerator();
        JCodeModel codeModel = modelGen.generateFromSchema(
                                                           xmlSchema, "test.getGeneratedElementsSingleElement", new File("target"));
        Map<String, String> mappings = modelGen.elementToClassMapping(codeModel);
        Assert.assertEquals(1, mappings.size());
        Assert.assertEquals("test.getGeneratedElementsSingleElement.ABCOrder", mappings.get("ABCOrder"));

    }

    @Test
    public void getGeneratedElementsXmlElementDecl() throws Exception {
		File xmlSchema = getFile(XML_SCHEMA_PATH_3);
        XmlModelGenerator modelGen = new XmlModelGenerator();
        JCodeModel codeModel = modelGen.generateFromSchema(
                                                           xmlSchema, "test.getGeneratedElementsXmlElementDecl", new File("target"));
        Map<String, String> mappings = modelGen.elementToClassMapping(codeModel);
        Assert.assertEquals(2, mappings.size());
        Assert.assertEquals("test.getGeneratedElementsXmlElementDecl.ApplicationType", mappings.get("Application"));
        // Assert.assertEquals("test.getGeneratedElementsXmlElementDecl.ApplicationType", mappings.get("Application"));

    }

    @Test
    public void getRootElement() throws Exception {
        XmlModelGenerator modelGen = new XmlModelGenerator();
		QName rootName = modelGen.getRootElementName(getFile(XML_INST_PATH));
        Assert.assertEquals(new QName("ABCOrder"), rootName);
    }

    @Test
    public void getRootElementNamespace() throws Exception {
        XmlModelGenerator modelGen = new XmlModelGenerator();
		QName rootName = modelGen.getRootElementName(getFile(XML_INST_PATH_2));
        Assert.assertEquals(new QName("http://example.org", "ABCOrder"), rootName);
    }

    @Test
    public void listElementsFromSchema() throws Exception {
        XmlModelGenerator modelGen = new XmlModelGenerator();
		List<QName> elements = modelGen.getElementsFromSchema(getFile(XML_SCHEMA_PATH));
        Assert.assertEquals(3, elements.size());
        Assert.assertTrue(elements.contains(new QName("urn:abc", "order-items")));
        Assert.assertTrue(elements.contains(new QName("urn:abc", "ZABCOrder")));
        Assert.assertTrue(elements.contains(new QName("urn:abc", "header")));
    }

	private File getFile(String fileName) {
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(fileName, ".xml", tmpFolder.getRoot());
			Files.copy(this.getClass().getResourceAsStream(fileName), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tmpFile;
	}
}
