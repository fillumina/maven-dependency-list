package com.fillumina.maven.reverse.dependency;

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

        CharSequence modified = PomModifier.modifyBuffer(buf, packageId, "5.6.0-SNAPSHOT");

        assertNotNull(modified);

        final String result = buf.toString();

        // only one occurrence
        assertEquals(2, result.split("5\\.6\\.0\\-SNAPSHOT").length);

        assertEquals(POM,
                result.replace("-SNAPSHOT", ""));

        final String noSpaceResult = result.replaceAll("\\s", "");

        assertVersionJupiterApi(noSpaceResult, "5.6.0-SNAPSHOT");

        assertVersionJupiterEngine(noSpaceResult, "5.6.0");
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

        CharSequence modified = PomModifier.modifyBuffer(buf, packageId, "3.33.3");

        assertNotNull(modified);

        final String result = buf.toString();

        // only one occurrence
        assertEquals(2, result.split("3\\.33\\.3").length);

        assertEquals(POM,
                result.replace("3.33.3", "2.22.2"));

        final String noSpaceResult = result.replaceAll("\\s", "");

        assertVersionSurefirePlugin(noSpaceResult, "3.33.3");

        assertVersionJupiterApi(noSpaceResult, "5.6.0");
        assertVersionJupiterEngine(noSpaceResult, "5.6.0");
        assertVersionJupiterParams(noSpaceResult, "5.6.0");
        assertVersionJarPlugin(noSpaceResult, "3.2.2");
        assertVersionCompilerPlugin(noSpaceResult, "3.8.1");
    }

    @Test
    public void shouldNotModifyAnything() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.apache.maven.plugins", "maven-NON_PRESENT", "2.22.2");

        StringBuffer modified = PomModifier.modifyBuffer(buf, packageId, "3.33.3");

        assertNull(modified);

        assertEquals(POM, buf.toString());
    }

    @Test
    public void shouldNotModifyDependencyTwice() {
        StringBuffer buf = new StringBuffer(POM);
        PackageId packageId = new PackageId(
                "org.junit.jupiter", "junit-jupiter-api", "5.6.0");

        StringBuffer modified = PomModifier.modifyBuffer(buf, packageId, "5.6.0-SNAPSHOT");

        StringBuffer input = new StringBuffer(modified);
        CharSequence twiceModified = PomModifier.modifyBuffer(input, packageId, "5.6.0-SNAPSHOT");

        assertNull(twiceModified);
    }

    private void assertVersionJupiterApi(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<dependency>" +
                "<groupId>org.junit.jupiter</groupId>" +
                "<artifactId>junit-jupiter-api</artifactId>" +
                "<version>" + version + "</version>" +
                "<scope>test</scope>" +
                "</dependency>"));
    }

    private void assertVersionJupiterEngine(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<dependency>" +
                "<groupId>org.junit.jupiter</groupId>" +
                "<artifactId>junit-jupiter-engine</artifactId>" +
                "<version>" + version + "</version>" +
                "<scope>test</scope>" +
                "</dependency>"));
    }

    private void assertVersionJupiterParams(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<dependency>" +
                "<groupId>org.junit.jupiter</groupId>" +
                "<artifactId>junit-jupiter-params</artifactId>" +
                "<version>" + version + "</version>" +
                "<scope>test</scope>" +
                "</dependency>"));
    }

    private void assertVersionJarPlugin(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<groupId>org.apache.maven.plugins</groupId>" +
                "<artifactId>maven-jar-plugin</artifactId>" +
                "<version>" + version + "</version>"));
    }

    private void assertVersionCompilerPlugin(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<groupId>org.apache.maven.plugins</groupId>" +
                "<artifactId>maven-compiler-plugin</artifactId>" +
                "<version>" + version + "</version>"));
    }

    private void assertVersionSurefirePlugin(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<groupId>org.apache.maven.plugins</groupId>" +
                "<artifactId>maven-surefire-plugin</artifactId>" +
                "<version>" + version + "</version>"));
    }

    private static final String POM = """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.fillumina.maven.reverse.dependency</groupId>
  <artifactId>maven-dependency-list</artifactId>
  <version>1.2</version>
  <packaging>jar</packaging>

  <licenses>
    <license>
      <name>The Apache Software License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>

  <scm>
    <connection>scm:git:git@github.com:maven-dependency-list.git</connection>
    <url>git@github.com:fillumina/maven-dependency-list</url>
    <developerConnection>scm:git:git@github.com:fillumina/maven-dependency-list.git</developerConnection>
  </scm>

  <developers>
    <developer>
      <name>Francesco Illuminati</name>
      <email>fillumina@gmail.com</email>
    </developer>
  </developers>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <exec.mainClass>com.fillumina.maven.reverse.dependency.MavenReverseDependency</exec.mainClass>
  </properties>

  <build>
    <plugins>
      <plugin>
        <!-- Build an executable JAR -->
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.2.2</version>
        <configuration>
          <archive>
            <manifest>
              <addClasspath>true</addClasspath>
              <classpathPrefix>lib/</classpathPrefix>
              <mainClass>com.fillumina.maven.reverse.dependency.MavenReverseDependency</mainClass>
            </manifest>
          </archive>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.22.2</version>
        <configuration>
          <trimStackTrace>false</trimStackTrace>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <compilerArgs>
            <arg>-verbose</arg>
            <!--              <arg>-Xlint:all,-options,-path</arg>-->
            <arg>-Xlint:unchecked</arg>
          </compilerArgs>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <name>maven-dependency-list</name>
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <version>5.6.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-params</artifactId>
      <version>5.6.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
      <version>5.6.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
""";
}
