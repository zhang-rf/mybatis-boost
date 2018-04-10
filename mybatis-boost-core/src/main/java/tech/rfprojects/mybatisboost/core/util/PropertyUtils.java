package tech.rfprojects.mybatisboost.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class PropertyUtils {

    public static List<String> buildPropertiesWithCandidates(String[] candidateProperties,
                                                             Object parameterObject, boolean isSelectiveUpdating) {
        if (candidateProperties.length > 0 && !Objects.equals(candidateProperties[0], "!")) {
            return new ArrayList<>(Arrays.asList(candidateProperties));
        } else {
            List<String> properties = EntityUtils.getProperties(parameterObject, isSelectiveUpdating);
            if (candidateProperties.length > 0) {
                properties.removeAll(Arrays.asList(candidateProperties));
            }
            return properties;
        }
    }

    public static void rebuildPropertiesWithConditions(List<String> properties,
                                                       Class<?> type, String[] conditionalProperties) {
        if (conditionalProperties.length == 0) {
            conditionalProperties = new String[]{EntityUtils.getIdProperty(type)};
        }
        List<String> conditionalPropertyList = Arrays.asList(conditionalProperties);
        properties.removeAll(conditionalPropertyList);
        properties.addAll(conditionalPropertyList);
    }
}
