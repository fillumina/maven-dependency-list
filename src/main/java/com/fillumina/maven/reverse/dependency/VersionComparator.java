package com.fillumina.maven.reverse.dependency;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Orders two version strings the way Maven does, so that a tree can be asked
 * which projects are behind rather than left to a regexp.
 *
 * <p>This is a reimplementation of Maven's own ordering, and
 * {@code VersionComparatorOracleTest} holds it to Maven's
 * {@code ComparableVersion} pair by pair over a corpus of awkward versions, so a
 * divergence shows up as a failing test rather than as a wrong report.
 *
 * <p>The rules, in full, so that a surprising answer can be traced to one of
 * them:
 * <ol>
 *   <li>a version is lower cased and split on {@code .} and {@code -} into
 *       tokens;</li>
 *   <li>a token that is one letter followed by a digit is that qualifier:
 *       {@code a1} is {@code alpha1}, {@code b1} is {@code beta1} and
 *       {@code m1} is {@code milestone1};</li>
 *   <li>{@code ga}, {@code final} and {@code release} are the plain release, and
 *       {@code cr} is {@code rc};</li>
 *   <li>two numeric tokens compare as numbers, and two qualifiers compare by
 *       {@code alpha}, {@code beta}, {@code milestone}, {@code rc},
 *       {@code snapshot}, the plain release, {@code sp}, with the numbers that
 *       follow deciding between two of the same qualifier. A qualifier this tool
 *       does not know sorts after all of them, and two of those sort
 *       alphabetically;</li>
 *   <li>a number and a qualifier in the same position: a qualifier below the
 *       release is beaten by the number, a qualifier that means the release is the
 *       same as a zero, and a qualifier above the release sits above a zero and
 *       below anything larger;</li>
 *   <li>a token one side has run out of is the plain release, so {@code 1.0}
 *       equals {@code 1.0.0} and {@code 1} is newer than {@code 1.0-SNAPSHOT}.</li>
 * </ol>
 */
public final class VersionComparator {

    /** The plain release, the qualifier a version carries when it has none. */
    private static final String RELEASE = "";

    private static final List<String> QUALIFIERS =
            List.of("alpha", "beta", "milestone", "rc", "snapshot", RELEASE, "sp");

    private static final Map<String, String> ALIASES = Map.of(
            "ga", RELEASE,
            "final", RELEASE,
            "release", RELEASE,
            "cr", "rc");

    private static final Map<String, String> SINGLE_LETTER_QUALIFIERS = Map.of(
            "a", "alpha",
            "b", "beta",
            "m", "milestone");

    private static final String SEPARATORS = ".-";

    private VersionComparator() {
    }

    /**
     * Returns a negative number when {@code left} is older than {@code right}, zero
     * when they are the same version, and a positive number when it is newer.
     */
    public static int compare(String left, String right) {
        List<String> leftTokens = tokenize(left);
        List<String> rightTokens = tokenize(right);
        int length = Math.max(leftTokens.size(), rightTokens.size());
        for (int i = 0; i < length; i++) {
            int result = compareToken(tokenAt(leftTokens, i), tokenAt(rightTokens, i));
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static List<String> tokenize(String version) {
        return List.of(version.toLowerCase(Locale.ROOT).split("[" + SEPARATORS + "]"));
    }

    private static String tokenAt(List<String> tokens, int index) {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    private static int compareToken(String left, String right) {
        if (left == null || right == null) {
            return compareWithMissingToken(left, right);
        }
        boolean leftIsNumber = isNumber(left);
        boolean rightIsNumber = isNumber(right);
        if (leftIsNumber && rightIsNumber) {
            return Long.compare(number(left), number(right));
        }
        if (leftIsNumber != rightIsNumber) {
            return leftIsNumber
                    ? numberAgainstQualifier(number(left), rank(qualifier(right)))
                    : -numberAgainstQualifier(number(right), rank(qualifier(left)));
        }
        String leftQualifier = qualifier(left);
        String rightQualifier = qualifier(right);
        int leftRank = rank(leftQualifier);
        int rightRank = rank(rightQualifier);
        if (leftRank == rightRank && leftRank == QUALIFIERS.size()) {
            return leftQualifier.compareTo(rightQualifier);
        }
        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }
        return Long.compare(remainder(left), remainder(right));
    }

    /**
     * A token one side has run out of, which is the plain release: the same as a
     * zero against a number, so {@code 1.0} equals {@code 1.0.0}, and the release
     * against a qualifier, so {@code 1} is newer than {@code 1.0-SNAPSHOT}.
     */
    private static int compareWithMissingToken(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        boolean leftIsMissing = left == null;
        String present = leftIsMissing ? right : left;
        if (isNumber(present)) {
            return leftIsMissing ? Long.compare(0, number(present)) : Long.compare(number(present), 0);
        }
        int release = rank(RELEASE);
        int presentRank = rank(qualifier(present));
        return leftIsMissing
                ? Integer.compare(release, presentRank)
                : Integer.compare(presentRank, release);
    }

    /**
     * A number and a qualifier in the same position. A qualifier below the release
     * ({@code alpha} through {@code snapshot}) is beaten by any number, and one that
     * means the plain release is the same as a zero, so {@code 1.0-ga} equals
     * {@code 1.0} but is older than {@code 1.0.1}. A qualifier above the release
     * ({@code sp}) sits above a zero and below anything larger, which puts
     * {@code 1.0-sp1} above {@code 1.0.0} and below {@code 1.0.1}.
     *
     * @return a positive number when the number is the newer of the two
     */
    private static int numberAgainstQualifier(long number, int qualifierRank) {
        int release = rank(RELEASE);
        if (qualifierRank == release) {
            return Long.compare(number, 0);
        }
        if (qualifierRank < release) {
            return 1;
        }
        return number == 0 ? -1 : 1;
    }

    /**
     * Where a qualifier sits in {@link #QUALIFIERS}, and after every one of them
     * when it is not a qualifier this tool knows.
     */
    private static int rank(String qualifier) {
        int known = QUALIFIERS.indexOf(ALIASES.getOrDefault(qualifier, qualifier));
        return known < 0 ? QUALIFIERS.size() : known;
    }

    /**
     * The letters of a token, with a single letter taken to mean the qualifier it
     * stands for, or nothing when the token starts with a digit.
     */
    private static String qualifier(String token) {
        String letters = letters(token);
        if (letters.length() == 1 && letters.length() < token.length()) {
            return SINGLE_LETTER_QUALIFIERS.getOrDefault(letters, letters);
        }
        return letters;
    }

    /**
     * The letters a token starts with, before any of them is taken to mean a
     * longer qualifier.
     */
    private static String letters(String token) {
        int end = 0;
        while (end < token.length() && Character.isLetter(token.charAt(end))) {
            end++;
        }
        return token.substring(0, end);
    }

    /**
     * What is left of a token once its letters are taken off, as a number, so that
     * {@code alpha1} and {@code alpha2} can be told apart.
     */
    private static long remainder(String token) {
        return number(token.substring(letters(token).length()));
    }

    private static boolean isNumber(String token) {
        return !token.isEmpty() && Character.isDigit(token.charAt(0));
    }

    private static long number(String token) {
        int end = 0;
        while (end < token.length() && Character.isDigit(token.charAt(end))) {
            end++;
        }
        return end == 0 ? 0 : Long.parseLong(token.substring(0, end));
    }
}
