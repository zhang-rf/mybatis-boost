package cn.mybatisboost.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PropertyUtils {

    public static List<String> buildPropertiesWithCandidates
            (String[] candidateProperties, Object entity, boolean isSelectiveUpdating) {
        if (candidateProperties.length > 0 && !Objects.equals(candidateProperties[0], "!")) {
            return new ArrayList<>(Arrays.asList(candidateProperties));
        } else {
            List<String> properties = EntityUtils.getProperties(entity, isSelectiveUpdating);
            if (candidateProperties.length > 0) {
                properties.removeAll(Arrays.asList(candidateProperties));
            }
            return properties;
        }
    }

    public static void rebuildPropertiesWithConditions
            (List<String> properties, Class<?> type, String[] conditionalProperties) {
        if (conditionalProperties.length == 0) {
            conditionalProperties = new String[]{EntityUtils.getIdProperty(type)};
        }
        List<String> conditionalPropertyList = Arrays.asList(conditionalProperties);
        properties.removeAll(conditionalPropertyList);
        properties.addAll(conditionalPropertyList);
    }

    public static String normalizeProperty(String property) {
        return StringUtils.uncapitalize
                (Arrays.stream(property.split("_")).map(StringUtils::capitalize).collect(Collectors.joining()));
    }
}
