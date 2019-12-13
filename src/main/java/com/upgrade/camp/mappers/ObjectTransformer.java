package com.upgrade.camp.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static java.lang.String.format;
import static com.upgrade.camp.mappers.PropertyMapper.*;


@Slf4j
public class ObjectTransformer {

    private static final String DOT = ".";
    private static final Integer ONE = 1;
    private static final Integer ZERO = 0;
    private static final String MESSAGE = "message=\"Mapping property of entity={}\", source={}, target={}";
    private static final String MESSAGE_LIST_CONVERSION = "message=\"Property marked as list, changing type\", property={}";
    private static final String RESOURCE_MAP = "transformation/%s_map";
    private static final String LIST_PREFIX = "l_";
    private static final String COMMA = ",";

    @SneakyThrows
    public JsonNode transformIntoFormat(String objectApiName, JsonNode source) {
        log.debug("message=\"Transformation for {} initialized\"", objectApiName);
        List<String> lines = Resources.readLines(Resources.getResource(format(RESOURCE_MAP, objectApiName)), StandardCharsets.UTF_8);
        ObjectNode target = instance.objectNode();
        lines.stream().forEach(property -> {
            String targetKey = property.split(COMMA)[0];
            String sourceKey = property.split(COMMA)[1];
            log.trace(MESSAGE, objectApiName, sourceKey, targetKey);
            fixIncompatibleTypesAndMap(source, target, sourceKey, targetKey);
        });
        log.debug("message=\"Transformation for {} ended\"", objectApiName);
        return target;
    }

    /**
     * method used to choose correct method call between compatible and incompatible types in source and target
     * add case if (targetKey.startsWith(LIST_PREFIX)
     * undone until we figure out how to do properly the mapping of several objects into a list
     */
    private void fixIncompatibleTypesAndMap(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        if (targetKey.startsWith(LIST_PREFIX) && sourceKey.startsWith(LIST_PREFIX)) {
            mapPropertiesListToList(source, target, sourceKey, targetKey);
            return;
        }
        if (sourceKey.startsWith(LIST_PREFIX)) {
            fixIncompatibleTypesInSourceAndMap(source, target, sourceKey, targetKey);
            return;
        }
        chooseAndExecutePropertyMapperMethod(source, target, sourceKey, targetKey);
    }

    private void mapPropertiesListToList(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        String targetFirstKey = targetKey.substring(ZERO, targetKey.indexOf(DOT)).replaceFirst(LIST_PREFIX, StringUtils.EMPTY);
        String targetPropertyChain = targetKey.substring(targetKey.indexOf(DOT) + ONE);
        String sourcePropertyChain = sourceKey.substring(sourceKey.indexOf(DOT) + ONE);
        ArrayNode sourceList = (ArrayNode) source.get(sourceKey.substring(ZERO, sourceKey.indexOf(DOT)).replaceFirst(LIST_PREFIX, StringUtils.EMPTY));
        ArrayNode targetList = getArrayNodeFrom(target, targetFirstKey);
        IntStream.range(ZERO, sourceList.size())
                .forEach(
                        index ->
                                fixIncompatibleTypesAndMap(getObjectAtIndex(sourceList, index), getObjectAtIndex(targetList, index), sourcePropertyChain,
                                        targetPropertyChain));
    }

    /**
     * method used when trying to map list property of source into single property of target.
     * current approach only takes the first value in list to be in the single property of target
     */
    private void fixIncompatibleTypesInSourceAndMap(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        String withoutPrefix = sourceKey.replace(LIST_PREFIX, StringUtils.EMPTY);
        JsonNode newSource = buildFacadeObject(source, withoutPrefix);
        chooseAndExecutePropertyMapperMethod(newSource, target, withoutPrefix, targetKey);
    }

    /**
     * method uses to call property mapper method according to the nested level of the properties
     * between source and target
     */
    private void chooseAndExecutePropertyMapperMethod(JsonNode source, ObjectNode target, String sourceKey, String targetKey) {
        Integer quantityOfDotsInSourceKey = StringUtils.countMatches(sourceKey, DOT);
        Integer quantityOfDotsInTargetKey = StringUtils.countMatches(targetKey, DOT);
        if (quantityOfDotsInSourceKey >= ONE && quantityOfDotsInTargetKey >= ONE) {
            nestedPropertyToNestedProperty(source, target, sourceKey, targetKey);
            return;
        }
        if (quantityOfDotsInSourceKey >= ONE && quantityOfDotsInTargetKey.equals(ZERO)) {
            nestedPropertyToProperty(source, target, sourceKey, targetKey);
            return;
        }
        if (quantityOfDotsInSourceKey.equals(ZERO) && quantityOfDotsInTargetKey >= ONE) {
            propertyToNestedProperty(source, target, sourceKey, targetKey);
            return;
        }
        propertyToProperty(source, target, sourceKey, targetKey);
    }

    private ObjectNode getObjectAtIndex(ArrayNode list, Integer index) {
        return list.get(index) != null ? (ObjectNode) list.get(index) : list.addObject();
    }

    private ArrayNode getArrayNodeFrom(ObjectNode target, String key) {
        return target.has(key) ? (ArrayNode) target.get(key) : target.putArray(key);
    }

    /**
     * this method will be used until we decide how to treat mapping between a list into
     * a single property of target
     * First value will be taken
     */
    private ObjectNode buildFacadeObject(JsonNode source, String key) {
        try {
            ObjectNode newSource = instance.objectNode();
            String lastProperty = key.substring(key.lastIndexOf(DOT) + ONE);
            buildSubObjectsAndReturnLastNode(newSource, key);
            ArrayNode arrayNode = (ArrayNode) (getLastNode(source, key)).get(lastProperty);
            newSource.set(key, arrayNode.get(ZERO));
            return newSource;
        } catch (Exception e) {
            log.info("message=\"Property not found in object source\", property={}", key);
            throw e;
        }
    }
}
