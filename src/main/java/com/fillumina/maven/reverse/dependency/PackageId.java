package com.fillumina.maven.reverse.dependency;

import java.util.Objects;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PackageId {
    public static final String SEPARATOR = ":";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String str;

    public static PackageId parse(String str) {
        String[] fields = str.split(SEPARATOR);
        switch (fields.length) {
            case 2: return new PackageId(fields[0], fields[1], null);
            case 3: return new PackageId(fields[0], fields[1], fields[2]);
        }
        throw new IllegalArgumentException("expected 2 or 3 fields, was: " + str);
    }

    public PackageId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.str = describe(groupId, artifactId, version);
    }

    /**
     * The group, the artifact and the version, joined by {@value #SEPARATOR}, with
     * whatever is missing left out rather than printed as null. A pom that declares
     * neither a groupId of its own nor a parent to inherit one from has no group,
     * and saying so is better than a literal null in a listing.
     */
    private static String describe(String groupId, String artifactId, String version) {
        StringBuilder description = new StringBuilder();
        if (groupId != null && !groupId.isEmpty()) {
            description.append(groupId).append(SEPARATOR);
        }
        if (artifactId != null) {
            description.append(artifactId);
        }
        if (version != null) {
            description.append(SEPARATOR).append(version);
        }
        return description.toString();
    }

    public String getName() {
        return describe(groupId, artifactId, null);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PackageId other = (PackageId) obj;
        return Objects.equals(this.str, other.str);
    }

    @Override
    public String toString() {
        return str;
    }
}
