package com.fillumina.maven.reverse.dependency;

import static com.fillumina.maven.reverse.dependency.PomFixture.POM;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomTest {

    @Test
    public void shouldReadTheVersions() {
        AssociationBuilder associationBuilder = new AssociationBuilder(null, null, false, false);

        Pom pom = new Pom(POM, associationBuilder, false);
        assertNotNull(pom);

        // associationBuilder.getMap().values().stream().forEach(System.out::println);

        Association association = associationBuilder.getMap().values().iterator().next();

        PackageId project = association.getProject();
        assertPackageId(project, "com.fillumina.maven.reverse.dependency:maven-dependency-list:1.2");

        Map<String, PackageId> map = association.getSet()
                .stream()
                .collect(Collectors.toMap(PackageId::getArtifactId, Function.identity()));

        assertEquals(11, map.size());

        assertPackageId(map.get("junit-jupiter-params"), "org.junit.jupiter:junit-jupiter-params:5.6.0");
        assertPackageId(map.get("maven-jar-plugin"), "org.apache.maven.plugins:maven-jar-plugin:3.2.2");
        assertPackageId(map.get("maven-compiler-plugin"), "org.apache.maven.plugins:maven-compiler-plugin:3.8.1");
        assertPackageId(map.get("junit-jupiter-api"), "org.junit.jupiter:junit-jupiter-api:5.6.0");
        assertPackageId(map.get("maven-surefire-plugin"), "org.apache.maven.plugins:maven-surefire-plugin:2.22.2");
        assertPackageId(map.get("junit-jupiter-engine"), "org.junit.jupiter:junit-jupiter-engine:5.6.0");

        assertPackageId(map.get("liquibase-maven-plugin"), "org.liquibase:liquibase-maven-plugin:4.15.0");
        assertPackageId(map.get("spring-boot-starter-data-jpa"), "org.springframework.boot:spring-boot-starter-data-jpa:2.7.3");
        assertPackageId(map.get("spring-boot"), "org.springframework.boot:spring-boot:2.7.3");
        assertPackageId(map.get("liquibase-core"), "org.liquibase:liquibase-core:4.15.0");
        assertPackageId(map.get("liquibase-hibernate5"), "org.liquibase.ext:liquibase-hibernate5:4.15.0");
    }

    static void assertPackageId(PackageId pkg, String str) {
        PackageId parsed = PackageId.parse(str);
        assertNotNull(parsed);
        assertNotNull(parsed.getArtifactId());
        assertNotNull(parsed.getGroupId());
        assertNotNull(parsed.getVersion());
        assertEquals(pkg, parsed);
    }

}
