package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Pins the ordering rules {@link VersionComparator} documents, one test per rule,
 * so that a surprising answer in a report can be traced to the rule behind it.
 */
public class VersionComparatorTest {

    @Test
    public void shouldCompareNumericPartsAsNumbers() {
        assertOlder("2.0", "10.0");
        assertOlder("1.9", "1.10");
        assertSame("1.0.0", "1.0.0");
    }

    @Test
    public void shouldTreatAMissingTokenAsZero() {
        assertSame("1.0", "1.0.0");
        assertSame("1", "1.0.0.0");
    }

    @Test
    public void shouldPutANumberAboveAQualifier() {
        assertOlder("1.0-alpha", "1.0.0");
    }

    @Test
    public void shouldOrderTheKnownQualifiers() {
        assertOlder("1.0-alpha1", "1.0-beta1");
        assertOlder("1.0-beta1", "1.0-milestone1");
        assertOlder("1.0-milestone1", "1.0-rc1");
        assertOlder("1.0-rc1", "1.0-SNAPSHOT");
        assertOlder("1.0-SNAPSHOT", "1.0");
        assertOlder("1.0", "1.0-sp1");
    }

    @Test
    public void shouldTellTwoVersionsOfTheSameQualifierApart() {
        assertOlder("1.0-alpha1", "1.0-alpha2");
        assertOlder("1.0-rc1", "1.0-rc2");
    }

    @Test
    public void shouldSortAnUnknownQualifierAfterEveryKnownOne() {
        assertOlder("1.0-sp1", "1.0-whatever1");
        assertOlder("1.0", "1.0-whatever1");
        assertOlder("1.0-alpha", "1.0-bravo");
    }

    @Test
    public void shouldOrderTwoUnknownQualifiersAlphabetically() {
        assertOlder("1.0-bravo", "1.0-charlie");
    }

    @Test
    public void shouldIgnoreCase() {
        assertSame("1.0-SNAPSHOT", "1.0-snapshot");
        assertOlder("1.0-ALPHA", "1.0-beta");
    }

    @Test
    public void shouldOrderTheCommonLibraryVersions() {
        String[] ordered = {"1.0-alpha1", "1.0-beta1", "1.0-rc1", "1.0-SNAPSHOT", "1.0", "1.0.1", "1.1", "2.0",
                "2.0.1-SNAPSHOT", "10.0"};

        for (int i = 0; i < ordered.length - 1; i++) {
            assertOlder(ordered[i], ordered[i + 1]);
        }
    }

    private static void assertOlder(String left, String right) {
        assertTrue(VersionComparator.compare(left, right) < 0, left + " should be older than " + right);
        assertTrue(VersionComparator.compare(right, left) > 0, right + " should be newer than " + left);
    }

    private static void assertSame(String left, String right) {
        assertEquals(0, VersionComparator.compare(left, right), left + " should equal " + right);
        assertEquals(0, VersionComparator.compare(right, left), right + " should equal " + left);
    }
}
