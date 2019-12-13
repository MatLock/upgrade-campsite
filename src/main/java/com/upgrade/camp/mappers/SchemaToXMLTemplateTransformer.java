package com.upgrade.camp.mappers;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class SchemaToXMLTemplateTransformer {


    /**
     * class used to transform JSON Schema to XML template
     */

    @SneakyThrows
    public static Document toXMLTemplate(JsonNode schema){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement(schema.get("title").asText());
        document.appendChild(root);
        JsonNode propertiesNode = schema.get("properties");
        propertiesNode.fieldNames().forEachRemaining(prop -> root.appendChild(mapProperty(prop,propertiesNode.get(prop),document)));

        return document;
    }

    private static Element mapProperty(String key,JsonNode jsonNode,Document document){
        if(isSingleProperty(jsonNode)){
            return mapSingleProperty(key,jsonNode,document);
        }
        if(isObjectProperty(jsonNode)){
            return mapObjectProperty(key,jsonNode,document);
        }
        if(isEnumProperty(jsonNode)){
            return mapEnumProperty(key,jsonNode,document);
        }
        return mapListProperty(key,jsonNode,document);
    }


    private static Element mapSingleProperty(String key,JsonNode schema,Document document){
        Element element = document.createElement(key);
        schema.fieldNames().forEachRemaining(prop ->{
            Attr attribute = document.createAttribute(prop);
            attribute.setValue(schema.get(prop).asText());
            element.setAttributeNode(attribute);
        });
        return element;
    }

    private static Element mapEnumProperty(String key,JsonNode schema, Document document){
        Element element = document.createElement(key);
        element.setAttribute("type","string");
        element.setAttribute("value","enum");
        return element;
    }

    private static Element mapObjectProperty(String key,JsonNode schema, Document document){
        Element element = document.createElement(key);
        element.setAttribute("type","object");
        JsonNode additionalProperties =  schema.get("additionalProperties");
        element.setAttribute("additionalProperties",additionalProperties != null ? additionalProperties.asText() : "false");

        JsonNode properties = schema.get("properties");
        properties.fieldNames().forEachRemaining(prop -> element.appendChild(mapProperty(prop,properties.get(prop),document)));
        return element;
    }

    private static Element mapListProperty(String key,JsonNode schema,Document document){
        Element element = document.createElement(key);
        Element listElement = document.createElement("item");
        element.appendChild(listElement);
        element.setAttribute("type","array");
        JsonNode itemsNode = schema.get("items");
        JsonNode propertiesNode = itemsNode.get("properties");
        propertiesNode.fieldNames().forEachRemaining(prop -> listElement.appendChild(mapProperty(prop,propertiesNode.get(prop),document)));
        return element;
    }

    private static  Boolean isObjectProperty(JsonNode jsonNode){
        JsonNode typeNode =  jsonNode.get("type");
        return typeNode != null && typeNode.asText().equalsIgnoreCase("object");
    }

    private static Boolean isSingleProperty(JsonNode jsonNode){
        JsonNode typeNode =  jsonNode.get("type");
        return typeNode != null
                && !typeNode.asText().equalsIgnoreCase("array")
                && !typeNode.asText().equalsIgnoreCase("enum")
                && !typeNode.asText().equalsIgnoreCase("object");
    }

    private static Boolean isEnumProperty(JsonNode jsonNode){
        JsonNode typeNode =  jsonNode.get("type");
        return (typeNode != null && typeNode.asText().equalsIgnoreCase("enum"))
                || (jsonNode.get("enum") != null);
    }


}
