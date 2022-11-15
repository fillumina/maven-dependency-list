package com.fillumina.maven.reverse.dependency;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomFixture {

    static final String POM = readTestPom();

    static void assertVersionJupiterApi(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<dependency>" +
                "<groupId>org.junit.jupiter</groupId>" +
                "<artifactId>junit-jupiter-api</artifactId>" +
                "<version>" + version + "</version>" +
                "<scope>test</scope>" +
                "</dependency>"));
    }

    static void assertVersionJupiterEngine(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<dependency>" +
                "<groupId>org.junit.jupiter</groupId>" +
                "<artifactId>junit-jupiter-engine</artifactId>" +
                "<version>" + version + "</version>" +
                "<scope>test</scope>" +
                "</dependency>"));
    }

    static void assertVersionJupiterParams(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<dependency>" +
                "<groupId>org.junit.jupiter</groupId>" +
                "<artifactId>junit-jupiter-params</artifactId>" +
                "<version>" + version + "</version>" +
                "<scope>test</scope>" +
                "</dependency>"));
    }

    static void assertVersionJarPlugin(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<groupId>org.apache.maven.plugins</groupId>" +
                "<artifactId>maven-jar-plugin</artifactId>" +
                "<version>" + version + "</version>"));
    }

    static void assertVersionCompilerPlugin(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<groupId>org.apache.maven.plugins</groupId>" +
                "<artifactId>maven-compiler-plugin</artifactId>" +
                "<version>" + version + "</version>"));
    }

    static void assertVersionSurefirePlugin(String noSpaceString, String version) {
        assertTrue(noSpaceString.contains(
                "<groupId>org.apache.maven.plugins</groupId>" +
                "<artifactId>maven-surefire-plugin</artifactId>" +
                "<version>" + version + "</version>"));
    }

    private static String readTestPom() {
        InputStream is = PomFixture.class.getClassLoader().getResourceAsStream("test-pom.xml");
        String text = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return text;
    }
}
