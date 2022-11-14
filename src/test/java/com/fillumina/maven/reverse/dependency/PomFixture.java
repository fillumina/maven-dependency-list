package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomFixture {

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

    static final String POM = """
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

    <jupiter.version>5.6.0</jupiter.version>
    <liquibase-hibernate5.version>4.15.0</liquibase-hibernate5.version>
    <spring-boot.version>2.7.3</spring-boot.version>
  </properties>

  <build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.liquibase</groupId>
                <artifactId>liquibase-maven-plugin</artifactId>
                <configuration>
                    <changeLogFile>${project.basedir}/src/main/resources/config/liquibase/master.xml</changeLogFile>
                    <diffChangeLogFile>${project.basedir}/src/main/resources/config/liquibase/changelog/${maven.build.timestamp}_changelog.xml</diffChangeLogFile>
                    <driver>org.postgresql.Driver</driver>
                    <url>jdbc:postgresql://localhost:5432/testtest</url>
                    <defaultSchemaName></defaultSchemaName>
                    <contexts>!test</contexts>
                    <diffExcludeObjects>oauth_access_token, oauth_approvals, oauth_client_details, oauth_client_token, oauth_code, oauth_refresh_token</diffExcludeObjects>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.liquibase</groupId>
                        <artifactId>liquibase-core</artifactId>
                        <version>4.15.0</version>
                    </dependency>
                    <dependency>
                        <groupId>org.liquibase.ext</groupId>
                        <artifactId>liquibase-hibernate5</artifactId>
                        <version>${liquibase-hibernate5.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-jpa</artifactId>
                        <version>${spring-boot.version}</version>
                    </dependency>
                </dependencies>
                <!-- moved to the bottom to make it harder to guess -->
                <version>4.15.0</version>
            </plugin>
        </plugins>
    </pluginManagement>
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
      <version>${jupiter.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
""";
}

