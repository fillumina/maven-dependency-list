package com.fillumina.maven.reverse.dependency;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class AssociationBuilder {

    private final Pattern modPattern;
    private final Pattern depPattern;
    private final boolean reverse;
    private final boolean isOmitNullVersion;

    private final Map<String,Association> map = new HashMap<>();

    public AssociationBuilder(Pattern modPattern, Pattern depPattern,
            boolean reverse, boolean isOmitNullVersion) {
        this.modPattern = modPattern;
        this.depPattern = depPattern;
        this.reverse = reverse;
        this.isOmitNullVersion = isOmitNullVersion;
    }

    public void add(PackageId source, PackageId dependency) {
        if (dependency != null && isOmitNullVersion && dependency.getVersion() == null) {
            return;
        }
        if (modPattern != null && !modPattern.matcher(source.toString()).matches()) {
            return;
        }
        if (dependency == null) {
            // in reverse mode the dependency is the key, a project without one has nothing to list
            if (!reverse) {
                innerAdd(source, null);
            }
            return;
        }
        if (depPattern != null && !depPattern.matcher(dependency.toString()).matches()) {
            return;
        }
        if (reverse) {
            innerAdd(dependency, source);
        } else {
            innerAdd(source, dependency);
        }
    }

    private void innerAdd(PackageId source, PackageId dependency) {
        final String str = source.toString();
        Association association = map.get(str);
        if (association == null) {
            association = new Association(source);
            map.put(str, association);
        }
        if (dependency != null) {
            association.add(dependency);
        }
    }

    public Map<String, Association> getMap() {
        return map;
    }
}
