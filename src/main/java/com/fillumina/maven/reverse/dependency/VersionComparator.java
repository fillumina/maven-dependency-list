package com.fillumina.maven.reverse.dependency;

import java.util.List;
import java.util.Locale;

/**
 * Orders two version strings the way a reader of a pom would expect, so that a
 * tree can be asked which projects are behind rather than left to a regexp.
 *
 * <p>The rules, in full, so that a surprising answer can be traced to one of
 * them:
 * <ol>
 *   <li>a version is split on {@code .} and {@code -} into tokens;</li>
 *   <li>two numeric tokens compare as numbers;</li>
 *   <li>a numeric token is greater than a non-numeric one, which is what puts
 *       {@code 1.0.0} above {@code 1.0-alpha};</li>
 *   <li>two non-numeric tokens compare by qualifier, using the order
 *       {@code alpha}, {@code beta}, {@code milestone}, {@code rc},
 *       {@code snapshot}, the plain release, {@code sp}. An unrecognised
 *       qualifier sorts after all of those, and two unrecognised ones compare
 *       alphabetically. When the qualifiers are the same, the numbers that follow
 *       them compare as numbers, which is what tells {@code alpha1} from
 *       {@code alpha2};</li>
 *   <li>a token one side has run out of counts as {@code 0} against a number,
 *       so {@code 1.0} and {@code 1.0.0} are the same version, and as the plain
 *       release against a qualifier, so {@code 1.0} is newer than
 *       {@code 1.0-SNAPSHOT} and older than {@code 1.0-sp1};</li>
 *   <li>the comparison ignores case.</li>
 * </ol>
 *
 * <p>This is not Maven's {@code ComparableVersion} and does not agree with it on
 * every exotic version. It is used to produce a report, never to decide what to
 * write into a pom, so an ordering this tool gets wrong misleads a listing
 * rather than a build.
 */
public final class VersionComparator {

    /** The plain release, the qualifier a version carries when it has none. */
    private static final String RELEASE = "";

    private static final List<String> QUALIFIERS =
            List.of("alpha", "beta", "milestone", "rc", "snapshot", RELEASE, "sp");

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
            String leftToken = i < leftTokens.size() ? leftTokens.get(i) : null;
            String rightToken = i < rightTokens.size() ? rightTokens.get(i) : null;
            int result = compareToken(leftToken, rightToken);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static List<String> tokenize(String version) {
        return List.of(version.split("[" + SEPARATORS + "]"));
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
            return leftIsNumber ? 1 : -1;
        }
        String leftQualifier = qualifier(left);
        String rightQualifier = qualifier(right);
        int leftRank = rank(leftQualifier);
        int rightRank = rank(rightQualifier);
        if (leftRank == rightRank && leftRank == QUALIFIERS.size()) {
            // two qualifiers this tool does not know fall back to alphabetical
            return leftQualifier.compareTo(rightQualifier);
        }
        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }
        return Long.compare(remainder(left), remainder(right));
    }

    /**
     * A token one side has run out of is zero against a number and the plain
     * release against a qualifier, which is what makes {@code 1.0} equal to
     * {@code 1.0.0} but newer than {@code 1.0-SNAPSHOT}.
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
        return leftIsMissing ? Integer.compare(release, presentRank) : Integer.compare(presentRank, release);
    }

    /**
     * Where a qualifier sits in {@link #QUALIFIERS}, and after every one of them
     * when it is not a qualifier this tool knows.
     */
    private static int rank(String qualifier) {
        int known = QUALIFIERS.indexOf(qualifier);
        return known < 0 ? QUALIFIERS.size() : known;
    }

    /**
     * The leading letters of a token, lower cased, or nothing when it starts with
     * a digit.
     */
    private static String qualifier(String token) {
        int end = 0;
        while (end < token.length() && Character.isLetter(token.charAt(end))) {
            end++;
        }
        return token.substring(0, end).toLowerCase(Locale.ROOT);
    }

    /**
     * What is left of a token once its letters are taken off, as a number, so that
     * {@code alpha1} and {@code alpha2} can be told apart.
     */
    private static long remainder(String token) {
        return isNumber(token) ? number(token) : number(token.substring(qualifier(token).length()));
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
