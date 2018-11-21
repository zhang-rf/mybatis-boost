package cn.mybatisboost.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PropertyUtils {

    public static String normalizeProperty(String property) {
        return StringUtils.uncapitalize
                (Arrays.stream(property.split("_")).map(StringUtils::capitalize).collect(Collectors.joining()));
    }

    public static List<String> buildPropertiesWithCandidates
            (String[] candidateProperties, Object entity, boolean selective) {
        if (candidateProperties.length > 0 && !Objects.equals(candidateProperties[0], "!")) {
            return Arrays.stream(candidateProperties)
                    .map(PropertyUtils::normalizeProperty).collect(Collectors.toList());
        } else {
            List<String> properties = EntityUtils.getProperties(entity, selective);
            if (candidateProperties.length > 0) {
                properties.removeAll(Arrays.stream(candidateProperties)
                        .map(PropertyUtils::normalizeProperty).collect(Collectors.toList()));
            }
            return properties;
        }
    }

    public static void rebuildPropertiesWithConditions
            (List<String> properties, Class<?> type, String[] conditionalProperties) {
        if (conditionalProperties.length == 0) {
            conditionalProperties = new String[]{EntityUtils.getIdProperty(type)};
        }
        List<String> conditionalPropertyList = Arrays.stream(conditionalProperties)
                .map(PropertyUtils::normalizeProperty).collect(Collectors.toList());
        properties.removeAll(conditionalPropertyList);
        properties.addAll(conditionalPropertyList);
    }
}
