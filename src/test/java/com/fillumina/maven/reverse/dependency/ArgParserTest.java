package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Covers the accepted command line, the regexps it builds and the two combinations it rejects.
 */
public class ArgParserTest {

    @Test
    public void shouldReportAnErrorWhenNoArgumentIsGiven() {
        ArgParser arguments = new ArgParser(new String[0]);

        assertTrue(arguments.isError());
        assertFalse(arguments.isHelp());
    }

    @Test
    public void shouldReportHelpOnBothHelpFlags() {
        assertTrue(new ArgParser(new String[]{"-h"}).isHelp());
        assertTrue(new ArgParser(new String[]{"--help"}).isHelp());
    }

    @Test
    public void shouldTreatUnknownWordsAsPaths() {
        ArgParser arguments = new ArgParser(new String[]{"one", "two", "three"});

        assertEquals(List.of("one", "two", "three"), arguments.getFolderNames());
    }

    @Test
    public void shouldWrapTheProjectRegexp() {
        ArgParser arguments = new ArgParser(new String[]{"-p", "fillumina", "."});

        assertEquals("^.*fillumina.*$", arguments.getModuleRegexp().pattern());
    }

    @Test
    public void shouldWrapTheDependencyRegexp() {
        ArgParser arguments = new ArgParser(new String[]{"-d", "jupiter", "."});

        assertEquals("^.*jupiter.*$", arguments.getDependencyRegexp().pattern());
    }

    @Test
    public void shouldLeaveTheRegexpsNullWhenNotGiven() {
        ArgParser arguments = new ArgParser(new String[]{"."});

        assertNull(arguments.getModuleRegexp());
        assertNull(arguments.getDependencyRegexp());
    }

    @Test
    public void shouldSplitTheArtifactToChangeInFourFields() {
        ArgParser arguments = new ArgParser(new String[]{"-c", "javax.validation:validation-api:2.0.1:3.0.0", "."});

        assertEquals(new PackageId("javax.validation", "validation-api", "2.0.1"), arguments.getArtifactToChange());
        assertEquals("3.0.0", arguments.getNewVersion());
    }

    @Test
    public void shouldRejectAnArtifactToChangeWithoutFourFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArgParser(new String[]{"-c", "javax.validation:validation-api:2.0.1", "."}));
    }

    @Test
    public void shouldRejectAnArtifactToChangeMixedWithADependencyFilter() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArgParser(new String[]{"-c", "g:a:1.0:2.0", "-d", "jupiter", "."}));
    }

    @Test
    public void shouldSetEveryFlag() {
        ArgParser arguments = new ArgParser(new String[]{"-r", "-n", "-b", "-j", "-v", "."});

        assertTrue(arguments.isReverse());
        assertTrue(arguments.isNoDependencies());
        assertTrue(arguments.isMakeBackupCopy());
        assertTrue(arguments.isFullStacktrace());
        assertTrue(arguments.isOmitNullVersion());
    }

    @Test
    public void shouldPrintOnlyTheOptionsInUse() {
        String text = new ArgParser(new String[]{"-r", "."}).toString();

        assertTrue(text.contains("reverse=true"), text);
        assertTrue(text.contains("paths=[.]"), text);
        assertFalse(text.contains("project regexp"), text);
    }
}
