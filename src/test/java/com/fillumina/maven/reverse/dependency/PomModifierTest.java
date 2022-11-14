package com.fillumina.maven.reverse.dependency;

import static com.fillumina.maven.reverse.dependency.PomFixture.*;
import static com.fillumina.maven.reverse.dependency.PomModifier.countOccurrences;
import static com.fillumina.maven.reverse.dependency.PomModifier.createProperty;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomModifierTest {

    @Test
    public void shouldCountOccurrences() {
        assertEquals(0, countOccurrences("one two three", "four"));
        assertEquals(1, countOccurrences("one two three", "one"));
        assertEquals(1, countOccurrences("one two three", "two"));
        assertEquals(1, countOccurrences("one two three", "three"));
        assertEquals(2, countOccurrences("one two three two", "two"));
        assertEquals(2, countOccurrences("onetwothreetwo", "two"));
        assertEquals(3, countOccurrences("onetwotwothreetwo", "two"));
    }

    @Test
    public void shouldFindIndexOf() {
        assertEquals("one two three".indexOf("one"),
                PomModifier.indexOf("one two three", "one"));
        assertEquals("one two three".indexOf("two"),
                PomModifier.indexOf("one two three", "two"));
        assertEquals("one two three".indexOf("three"),
                PomModifier.indexOf("one two three", "three"));
        assertEquals("one two three two".indexOf("two"),
                PomModifier.indexOf("one two three two", "two"));
        assertEquals("one two three".indexOf("four"),
                PomModifier.indexOf("one two three", "four"));
    }

    @Test
    public void shouldFindLastIndexOf() {
        assertEquals("one two three".lastIndexOf("one"),
                PomModifier.lastIndexOf("one two three", "one"));
        assertEquals("one two three".lastIndexOf("two"),
                PomModifier.lastIndexOf("one two three", "two"));
        assertEquals("one two three".lastIndexOf("three"),
                PomModifier.lastIndexOf("one two three", "three"));
        assertEquals("one two three two".lastIndexOf("two"),
                PomModifier.lastIndexOf("one two three two", "two"));
        assertEquals("one two three".lastIndexOf("four"),
                PomModifier.lastIndexOf("one two three", "four"));
    }

    @Test
    public void shouldModifyDependency() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.junit.jupiter", "junit-jupiter-api", "5.6.0");

        Map<String,String> propMap = Map.of("jupiter.version", "5.6.0");
        CharSequence modified = PomModifier.modifyBuffer(buf, propMap, packageId, "5.6.0-SNAPSHOT");

        assertNotNull(modified);

        final String result = buf.toString();

        // only one occurrence
        assertEquals(2, result.split("5\\.6\\.0\\-SNAPSHOT").length);

        assertEquals(POM,
                result.replace("-SNAPSHOT", ""));

        final String noSpaceResult = result.replaceAll("\\s", "");

        assertVersionJupiterApi(noSpaceResult, "5.6.0-SNAPSHOT");

        assertProperty(noSpaceResult, "jupiter.version", "5.6.0");
        //assertVersionJupiterEngine(noSpaceResult, "5.6.0");
        assertVersionJupiterParams(noSpaceResult, "5.6.0");
        assertVersionJarPlugin(noSpaceResult, "3.2.2");
        assertVersionCompilerPlugin(noSpaceResult, "3.8.1");
        assertVersionSurefirePlugin(noSpaceResult, "2.22.2");
    }

    @Test
    public void shouldModifyDependencyWithProperty() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.junit.jupiter", "junit-jupiter-engine", "5.6.0");

        Map<String,String> propMap = Map.of("jupiter.version", "5.6.0");
        CharSequence modified = PomModifier.modifyBuffer(buf, propMap, packageId, "5.6.0-SNAPSHOT");

        assertNotNull(modified);

        final String result = buf.toString();

        // only one occurrence
        assertEquals(2, result.split("5\\.6\\.0\\-SNAPSHOT").length);

        assertEquals(POM,
                result.replace("-SNAPSHOT", ""));

        final String noSpaceResult = result.replaceAll("\\s", "");

        assertProperty(noSpaceResult, "jupiter.version", "5.6.0-SNAPSHOT");
        //assertVersionJupiterEngine(noSpaceResult, "5.6.0-SNAPSHOT");

        assertVersionJupiterApi(noSpaceResult, "5.6.0");
        assertVersionJupiterParams(noSpaceResult, "5.6.0");
        assertVersionJarPlugin(noSpaceResult, "3.2.2");
        assertVersionCompilerPlugin(noSpaceResult, "3.8.1");
        assertVersionSurefirePlugin(noSpaceResult, "2.22.2");
    }

    @Test
    public void shouldModifyPlugin() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.apache.maven.plugins", "maven-surefire-plugin", "2.22.2");

        Map<String,String> propMap = Map.of("jupiter.version", "5.6.0");
        CharSequence modified = PomModifier.modifyBuffer(buf, propMap, packageId, "3.33.3");

        assertNotNull(modified);

        final String result = buf.toString();

        // only one occurrence
        assertEquals(2, result.split("3\\.33\\.3").length);

        assertEquals(POM,
                result.replace("3.33.3", "2.22.2"));

        final String noSpaceResult = result.replaceAll("\\s", "");

        assertVersionSurefirePlugin(noSpaceResult, "3.33.3");

        assertVersionJupiterApi(noSpaceResult, "5.6.0");
        assertProperty(noSpaceResult, "jupiter.version", "5.6.0");
        //assertVersionJupiterEngine(noSpaceResult, "5.6.0");
        assertVersionJupiterParams(noSpaceResult, "5.6.0");
        assertVersionJarPlugin(noSpaceResult, "3.2.2");
        assertVersionCompilerPlugin(noSpaceResult, "3.8.1");
    }

    @Test
    public void shouldNotModifyAnything() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.apache.maven.plugins", "maven-NON_PRESENT", "2.22.2");

        Map<String,String> propMap = Map.of("jupiter.version", "5.6.0");
        StringBuffer modified = PomModifier.modifyBuffer(buf, propMap, packageId, "3.33.3");

        assertNull(modified);

        assertEquals(POM, buf.toString());
    }

    @Test
    public void shouldNotModifyDependencyTwice() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.junit.jupiter", "junit-jupiter-api", "5.6.0");

        Map<String,String> propMap = Map.of();
        StringBuffer modified = PomModifier.modifyBuffer(buf, propMap, packageId, "5.6.0-SNAPSHOT");
        assertNotNull(modified);

        StringBuffer input = new StringBuffer(modified);
        CharSequence twiceModified = PomModifier.modifyBuffer(input, propMap, packageId, "5.6.0-SNAPSHOT");

        assertNull(twiceModified);
    }

    @Test
    public void shouldModifyComplexPlugin() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.liquibase", "liquibase-maven-plugin", "4.15.0");

        Map<String,String> propMap = Map.of("liquibase.version", "4.15.0");
        CharSequence modified = PomModifier.modifyBuffer(buf, propMap, packageId, "1.2.3");

        assertNotNull(modified);

        final String result = buf.toString();

        // only one occurrence
        assertEquals(2, result.split("1\\.2\\.3").length);

        final String noSpaceResult = result.replaceAll("\\s", "");

        assertVersionSurefirePlugin(noSpaceResult, "2.22.2");
        assertVersionJupiterApi(noSpaceResult, "5.6.0");
        assertProperty(noSpaceResult, "jupiter.version", "5.6.0");
        //assertVersionJupiterEngine(noSpaceResult, "5.6.0");
        assertVersionJupiterParams(noSpaceResult, "5.6.0");
        assertVersionJarPlugin(noSpaceResult, "3.2.2");
        assertVersionCompilerPlugin(noSpaceResult, "3.8.1");
    }

    @Test
    public void shouldNotUpdateThePropertyIfItsUsedByMoreThanOneArtifact() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.springframework.boot", "spring-boot-starter-data-jpa", "2.7.3");

        Map<String,String> propMap = Map.of("spring-boot.version", "2.7.3");
        CharSequence modified = PomModifier.modifyBuffer(buf, propMap, packageId, "1.2.3");

        assertNotNull(modified);

        final String result = buf.toString().replaceAll("\\s", "");

        String expected = "<groupId>org.springframework.boot</groupId>" +
            "<artifactId>spring-boot-starter-data-jpa</artifactId>" +
            "<version>1.2.3</version>";

        assertTrue(result.contains(expected));
    }


    private void assertProperty(String content, String propertyName, String value) {
        String property = createProperty(propertyName, value);
        assertTrue(content.contains(property));
    }
}