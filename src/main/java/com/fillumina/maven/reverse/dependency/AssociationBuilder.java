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
        if ((!(isOmitNullVersion && dependency.getVersion() == null)) &&
                (modPattern == null || modPattern.matcher(source.toString()).matches()) &&
                (dependency == null || depPattern == null || depPattern.matcher(dependency.toString()).matches()) ) {
            if (reverse) {
                innerAdd(dependency, source);
            } else {
                innerAdd(source, dependency);
            }
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
