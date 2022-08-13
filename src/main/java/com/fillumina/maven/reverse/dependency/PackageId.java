package com.fillumina.maven.reverse.dependency;

import java.util.Objects;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PackageId {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String str;

    public PackageId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.str = groupId + ":" + artifactId + (version == null ? "" : ":" + version);
    }

    public String getName() {
        return groupId + ":" + artifactId;
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
