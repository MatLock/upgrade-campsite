package com.upgrade.camp.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class SchemaToXSDTemplateTransformer {


    /**
     * class used to transform JSON Schema to XSD Template
     */
    private static final String DATE_FORMAT = "(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}).(\\d{4})Z";


    @SneakyThrows
    public static Document toXSDTemplate(JsonNode schema) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootTag = createSchemaElement(document);
        document.appendChild(rootTag);
        Element element = createRootElement(document,schema);
        Element complexType = document.createElement("xs:complexType");
        element.appendChild(complexType);
        Element elt = document.createElement("xs:sequence");
        complexType.appendChild(elt);
        rootTag.appendChild(element);

        JsonNode propertiesNode = schema.get("properties");
        propertiesNode.fieldNames().forEachRemaining(prop -> elt.appendChild(mapProperty(prop,propertiesNode.get(prop),document)));
        return document;
    }

    private static Element mapProperty(String key,JsonNode jsonNode,Document document){
        if(isSingleProperty(jsonNode)){
            return mapSingleProperty(key,jsonNode,document);
        }
        if(isObjectProperty(jsonNode)){
            return mapComplexProperty(key,jsonNode,document);
        }
        if(isEnumProperty(jsonNode)){
            return mapEnumProperty(key,jsonNode,document);
        }
        return mapListProperty(key,jsonNode,document);
    }


    private static Element mapListProperty(String key,JsonNode schema,Document document){
        Element listElement = document.createElement("xs:element");
        Element complexType = document.createElement("xs:complexType");
        Element sequence = document.createElement("xs:sequence");
        listElement.setAttribute("name",key);
        listElement.setAttribute("maxOccurs","unbounded");
        listElement.setAttribute("minOccurs","0");
        JsonNode itemsNode = schema.get("items");
        JsonNode propertiesNode = itemsNode.get("properties");
        propertiesNode.fieldNames().forEachRemaining( prop -> sequence.appendChild(mapProperty(prop,propertiesNode.get(prop),document)));
        listElement.appendChild(complexType);
        complexType.appendChild(sequence);
        return listElement;
    }

    private static Element mapEnumProperty(String key,JsonNode schema, Document document){
        Element element = document.createElement("xs:element");
        Element simpleType = document.createElement("xs:simpleType");
        Element restriction = document.createElement("xs:restriction");
        element.setAttribute("name",key);
        restriction.setAttribute("base","xs:string");
        element.appendChild(simpleType);
        simpleType.appendChild(restriction);
        ArrayNode enums = (ArrayNode) schema.get("enum");
        enums.elements().forEachRemaining(en -> {
            Element enumeration = document.createElement("xs:enumeration");
            enumeration.setAttribute("value",en.asText());
            restriction.appendChild(enumeration);
        });
        return element;
    }

    private static Element mapComplexProperty(String key,JsonNode schema,Document document){
        Element element = document.createElement("xs:element");
        Element complexType = document.createElement("xs:complexType");
        Element sequence = document.createElement("xs:sequence");
        element.setAttribute("name",key);
        element.appendChild(complexType);
        complexType.appendChild(sequence);
        JsonNode properties = schema.get("properties");
        properties.fieldNames().forEachRemaining(prop -> sequence.appendChild(mapProperty(prop,properties.get(prop),document)));
        return element;
    }

    private static Element mapSingleProperty(String key,JsonNode jsonNode,Document document){
        String type = jsonNode.get("type").asText();
        JsonNode enumNode = jsonNode.get("enum");
        JsonNode minNode = jsonNode.get("minLength");
        JsonNode maxNode = jsonNode.get("maxLength");
        JsonNode patternNode = jsonNode.get("pattern");
        if("string".equalsIgnoreCase(type) && enumNode != null && !enumNode.isNull()){
            return mapStringEnumProperty(key,jsonNode,document);
        }
        if("string".equalsIgnoreCase(type) && (minNode != null  || maxNode != null || patternNode != null)){
            return mapStringWithConditionsProperty(key,jsonNode,document);
        }
        return mapCommonProperty(key,jsonNode,document);
    }

    private static Element createSchemaElement(Document document){
        Element element = document.createElement("xs:schema");
        element.setAttribute("xmlns:xs","http://www.w3.org/2001/XMLSchema");
        return element;
    }

    private static Element createRootElement(Document document,JsonNode schema){
        Element eltm = document.createElement("xs:element");
        eltm.setAttribute("name",schema.get("title").asText());
        return eltm;
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

    private static Element mapCommonProperty(String key,JsonNode jsonNode,Document document){
        Element element = document.createElement("xs:element");
        element.setAttribute("name",key);
        if("number".equalsIgnoreCase(jsonNode.get("type").asText())){
            // element.setAttribute("type","xs:integer");
            completeNumberElementWithRestrictions(element,jsonNode,document);
        }else if(jsonNode.get("format") != null){
            Element restriction = createDateFormatRestriction(document);
            element.appendChild(restriction);
        }
        else{
            element.setAttribute("type","xs:" + jsonNode.get("type").asText());
        }
        return element;
    }

    private static void completeNumberElementWithRestrictions(Element element,JsonNode jsonNode,Document document){
        if(jsonNode.get("maximum") == null && jsonNode.get("minimum") == null){
            element.setAttribute("type","xs:integer");
            return;
        }
        Element simpleType = document.createElement("xs:simpleType");
        Element restriction = document.createElement("xs:restriction");
        Element min = document.createElement("xs:minInclusive");
        Element max = document.createElement("xs:maxInclusive");
        if(jsonNode.get("maximum") != null){
            max.setAttribute("value",jsonNode.get("maximum").asText());
            restriction.appendChild(max);
        }
        if(jsonNode.get("minimum") != null){
            min.setAttribute("value",jsonNode.get("minimum").asText());
            restriction.appendChild(min);
        }
        restriction.setAttribute("base","xs:integer");
        simpleType.appendChild(restriction);
        element.appendChild(simpleType);
    }

    private static Element createDateFormatRestriction(Document document){
        Element simpleType = document.createElement("xs:simpleType");
        Element restriction = document.createElement("xs:restriction");
        Element pattern = document.createElement("xs:pattern");
        pattern.setAttribute("value",DATE_FORMAT);
        restriction.setAttribute("base","xs:string");
        restriction.appendChild(pattern);
        simpleType.appendChild(restriction);
        return simpleType;
    }

    private static Element mapStringEnumProperty(String key,JsonNode jsonNode,Document document){
        Element element = document.createElement("xs:element");
        Element simpleType = document.createElement("xs:simpleType");
        Element restriction = document.createElement("xs:restriction");
        restriction.setAttribute("base","xs:string");
        simpleType.appendChild(restriction);
        element.appendChild(simpleType);
        element.setAttribute("name",key);
        ArrayNode enums = (ArrayNode) jsonNode.get("enum");
        enums.elements().forEachRemaining(elem ->{
            Element enumeration = document.createElement("xs:enumeration");
            enumeration.setAttribute("value",elem.asText());
            restriction.appendChild(enumeration);
        });
        return element;
    }

    private static Element mapStringWithConditionsProperty(String key,JsonNode jsonNode,Document document){
        Element element = document.createElement("xs:element");
        Element simpleType = document.createElement("xs:simpleType");
        Element restriction = document.createElement("xs:restriction");
        restriction.setAttribute("base","xs:string");
        simpleType.appendChild(restriction);
        element.appendChild(simpleType);
        element.setAttribute("name",key);
        if(jsonNode.get("pattern") != null && !jsonNode.get("pattern").isNull()){
            Element pattern = document.createElement("xs:pattern");
            pattern.setAttribute("value",jsonNode.get("pattern").asText());
            restriction.appendChild(pattern);
        }
        if(jsonNode.get("minLength") != null && !jsonNode.get("minLength").isNull()){
            Element minLength = document.createElement("xs:minLength");
            minLength.setAttribute("value",jsonNode.get("minLength").asText());
            restriction.appendChild(minLength);
        }
        if(jsonNode.get("maxLength") != null && !jsonNode.get("maxLength").isNull()){
            Element minLength = document.createElement("xs:maxLength");
            minLength.setAttribute("value",jsonNode.get("maxLength").asText());
            restriction.appendChild(minLength);
        }
        return element;
    }

}
