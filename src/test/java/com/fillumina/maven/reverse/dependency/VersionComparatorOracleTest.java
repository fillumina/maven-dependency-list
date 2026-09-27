package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.jupiter.api.Test;

/**
 * Holds {@link VersionComparator} to Maven's own ordering, so that "behind" means
 * what Maven would say it means. Maven's {@code ComparableVersion} is the oracle,
 * and maven-artifact is a test dependency for that reason alone.
 *
 * <p>Two corpora. The first is the versions people actually publish, and this
 * implementation must order every pair of them the way Maven does. The second is
 * deliberate awkwardness, kept as a total order test and as the record of the
 * places where this implementation deliberately decides differently from Maven,
 * each with the example that shows it.
 */
public class VersionComparatorOracleTest {

    private static final List<String> PUBLISHED = List.of(
            "0.9", "1", "1.0", "1.0.0", "1.0.1", "1.1", "1.2.3", "2", "2.0", "2.0.1", "2.7.3",
            "3.0.0", "4.2", "4.13.2", "4.15.0", "5.3.20", "5.6.0", "5.9.0", "6.1.3", "10.0",
            "17.0.1", "1.0-alpha1", "1.0-beta1", "1.0-rc1", "1.0-SNAPSHOT", "2.0.1-SNAPSHOT",
            "1.0-ga", "1.0-final", "1.0-cr1", "1.0-M1", "3.0.0-M1", "1.0.0.Final", "1.0.0.RELEASE",
            "8.0.0-RC1", "20030203.000550");

    private static final List<String> AWKWARD = List.of(
            "1.0.0.0", "1.10", "1.0-alpha2", "1.0-milestone1", "1.0-snapshot", "1.0-release",
            "1.0-sp", "1.0-sp1", "1.0-sp2", "1.0-whatever", "1.0.0-alpha1", "1.0.0-beta1",
            "1.0.0-rc1", "1.0.0-SNAPSHOT", "1.0.0-sp1", "1.0-alpha-1", "1.0.0.1", "1.2.3.4.5",
            "3.0.0-M2", "1.0.0-M1", "1.0-20240101.120000-3", "1.0.1-2", "1.2.3.RELEASE",
            "1.0-alpha1-SNAPSHOT", "1.0.0-alpha-1");

    @Test
    public void shouldOrderThePublishedVersionsTheWayMavenDoes() {
        assertAgreesWithMaven(PUBLISHED);
    }

    @Test
    public void shouldBeATotalOrderOverAwkwardVersions() {
        List<String> broken = new ArrayList<>();
        for (String left : AWKWARD) {
            for (String right : AWKWARD) {
                if (sign(VersionComparator.compare(left, right))
                        != -sign(VersionComparator.compare(right, left))) {
                    broken.add(left + " vs " + right);
                }
            }
        }
        assertTrue(broken.isEmpty(),
                broken.size() + " pairs are not symmetric: " + String.join(", ", broken));
    }

    @Test
    public void shouldDecideWhereMavenDoesNot() {
        // Maven flattens 1.0.0-alpha1 onto 1.0-alpha1 by dropping the zero that opens
        // the qualifier, and calls them the same version. Here the zero is compared
        // where it is written, and a number beats a qualifier below the release, so
        // the longer one is the newer of the two.
        assertEquals(1, sign(VersionComparator.compare("1.0.0-alpha1", "1.0-alpha1")), "flattening");

        // and it reads a separator before a number as nothing, where this reads it as
        // part of the version
        assertEquals(-1, sign(VersionComparator.compare("1.0-alpha-1", "1.0-alpha1")), "separator");
    }

    private static void assertAgreesWithMaven(List<String> versions) {
        List<String> disagreements = new ArrayList<>();
        for (String left : versions) {
            for (String right : versions) {
                int mine = sign(VersionComparator.compare(left, right));
                int maven = sign(new ComparableVersion(left).compareTo(new ComparableVersion(right)));
                if (mine != maven) {
                    disagreements.add(left + " vs " + right + ": mine=" + mine + " maven=" + maven);
                }
            }
        }
        assertTrue(disagreements.isEmpty(),
                disagreements.size() + " of " + versions.size() * versions.size()
                        + " pairs disagree with Maven: " + String.join(", ", disagreements));
    }

    private static int sign(int comparison) {
        return Integer.signum(comparison);
    }
}
