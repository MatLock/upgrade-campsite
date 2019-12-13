package com.upgrade.camp.mappers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

public class PropertyMapper {

    private static final String DOT = ".";
    private static final Integer ONE = 1;
    private static final Integer ZERO = 0;
    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    private PropertyMapper() {}

    public static void propertyToProperty(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        JsonNode sourceValue = source.get(sourceKey);
        target.set(targetKey, sourceValue);
    }

    public static void propertyToNestedProperty(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        String lastPropertyToBeInserted = targetKey.substring(targetKey.lastIndexOf(DOT) + ONE, targetKey.length());
        String chainOfProperties = targetKey.substring(ZERO, targetKey.lastIndexOf(DOT));
        ObjectNode lastBuiltNode = buildSubObjectsAndReturnLastNode(target, chainOfProperties);
        propertyToProperty(source, lastBuiltNode, sourceKey, lastPropertyToBeInserted);
    }

    public static void nestedPropertyToProperty(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        try {
            JsonNode nodeValue = getLastNode(source, sourceKey);
            String lastProperty = sourceKey.substring(sourceKey.lastIndexOf(DOT) + ONE);
            propertyToProperty(nodeValue, target, lastProperty, targetKey);
        } catch (Exception e) {
            target.set(targetKey, null);
        }
    }

    public static void nestedPropertyToNestedProperty(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        String lastKey = targetKey.substring(targetKey.lastIndexOf(DOT) + ONE);
        String keyChain = targetKey.substring(ZERO, targetKey.lastIndexOf(DOT));
        ObjectNode lastNode = buildSubObjectsAndReturnLastNode(target, keyChain);
        nestedPropertyToProperty(source, lastNode, sourceKey, lastKey);
    }

    public static ObjectNode buildSubObjectsAndReturnLastNode(ObjectNode target, String targetKey) {
        if (!targetKey.contains(DOT)) {
            return buildOrReturnNodeFound(target, targetKey);
        }
        String keyRoot = targetKey.substring(ZERO, targetKey.indexOf(DOT));
        String chainOfProperties = targetKey.substring(targetKey.indexOf(DOT) + ONE);
        ObjectNode middleObject = buildOrReturnNodeFound(target, keyRoot);
        return buildSubObjectsAndReturnLastNode(middleObject, chainOfProperties);
    }

    @SneakyThrows
    public static JsonNode getLastNode(JsonNode source, String sourceKey) {
        if (source != null && sourceKey.contains(DOT)) {
            String keyRoot = sourceKey.substring(0, sourceKey.indexOf(DOT));
            String keyChain = sourceKey.substring(sourceKey.indexOf(DOT) + ONE);
            return getLastNode(source.get(keyRoot), keyChain);
        }
        if (source != null && source.has(sourceKey)) {
            return source;
        }
        throw new Exception(sourceKey);
    }

    private static ObjectNode buildOrReturnNodeFound(ObjectNode target, String key) {
        if (target.get(key) != null) {
            return (ObjectNode) target.get(key);
        }
        ObjectNode middleObject = FACTORY.objectNode();
        target.set(key, middleObject);
        return middleObject;
    }
}
