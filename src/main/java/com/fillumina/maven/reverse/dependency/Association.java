package com.fillumina.maven.reverse.dependency;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Association {
    private final PackageId project;
    private final Set<PackageId> set = new HashSet<>();

    public Association(PackageId project) {
        this.project = project;
    }

    public void add(PackageId associate) {
        set.add(associate);
    }

    public PackageId getProject() {
        return project;
    }

    public Set<PackageId> getSet() {
        return set;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(project).append(System.lineSeparator());
        set.forEach(p ->
                buf.append('\t').append(p).append(System.lineSeparator()));
        return buf.toString();
    }
}
