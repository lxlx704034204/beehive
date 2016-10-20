package com.gustz.beehive.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JAXB XML parser
 *
 * @author zhangzhenfeng
 * @since 2015-08-11
 */
public abstract class JaxbParser {

    private static final String ENCODING = "utf-8";

    private static ConcurrentMap<Class<?>, JAXBContext> jaxbContextMap = new ConcurrentHashMap<Class<?>, JAXBContext>();

    /**
     * Bean --> XML
     *
     * @param root
     * @return
     */
    public static String toXml(Object root) {
        return toXml(root, ENCODING);
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param isFormat
     * @return
     */
    public static String toXml(Object root, boolean isFormat) {
        return toXml(root, ENCODING, isFormat);
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param encoding
     * @return
     */
    public static String toXml(Object root, String encoding) {
        return toXml(root, encoding, root.getClass());
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param encoding
     * @param isFormat
     * @return
     */
    public static String toXml(Object root, String encoding, boolean isFormat) {
        return toXml(root, encoding, isFormat, root.getClass());
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param encoding
     * @param retType
     * @return
     */
    public static String toXml(Object root, String encoding, Class<?> retType) {
        StringWriter writer = new StringWriter();
        try {
            createMarshaller(retType, encoding).marshal(root, writer);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
        return writer.toString();
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param encoding
     * @param isFormat
     * @param retType
     * @return
     */
    public static String toXml(Object root, String encoding, boolean isFormat, Class<?> retType) {
        StringWriter writer = new StringWriter();
        try {
            createMarshaller(retType, encoding, null, isFormat, null).marshal(root, writer);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
        return writer.toString();
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param rootName
     * @param retType
     * @return
     */
    public static String toXml(Collection<?> root, String rootName, Class<?> retType) {
        return toXml(root, rootName, ENCODING, retType);
    }

    /**
     * Bean --> XML
     *
     * @param root
     * @param rootName
     * @param encoding
     * @param retType
     * @return
     */
    public static String toXml(Collection<?> root, String rootName, String encoding, Class<?> retType) {
        CollectionWrapper wrapper = new CollectionWrapper();
        wrapper.collection = root;
        JAXBElement<CollectionWrapper> wrapperElement = new JAXBElement<CollectionWrapper>(new QName(rootName),
                CollectionWrapper.class, wrapper);
        //
        StringWriter writer = new StringWriter();
        try {
            createMarshaller(retType, encoding).marshal(wrapperElement, writer);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
        return writer.toString();
    }

    /**
     * XML --> Bean
     *
     * @param xml
     * @param retType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String xml, Class<T> retType) {
        try {
            return (T) createUnmarshaller(retType).unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * XML --> Bean
     *
     * @param xml
     * @param retType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXmlByMap(String xml, Class<T> retType) {
        try {
            return (T) createUnmarshaller(retType, new InnerMapAdapter()).unmarshal(new StringReader(xml));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * XML --> Bean
     *
     * @param xml
     * @param retType
     * @param adapter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String xml, Class<T> retType, XmlAdapter<?, ?> adapter) {
        try {
            return (T) createUnmarshaller(retType, adapter).unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * XML --> Bean
     *
     * @param ins
     * @param retType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(InputStream ins, Class<T> retType) {
        try {
            return (T) createUnmarshaller(retType).unmarshal(ins);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * XML --> Bean
     *
     * @param ins
     * @param retType
     * @param adapter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(InputStream ins, Class<T> retType, XmlAdapter<?, ?> adapter) {
        try {
            return (T) createUnmarshaller(retType, adapter).unmarshal(ins);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * XML --> Bean
     *
     * @param ins
     * @param retType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXmlByMap(InputStream ins, Class<T> retType) {
        try {
            return (T) createUnmarshaller(retType, new InnerMapAdapter()).unmarshal(ins);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * XML --> Bean
     *
     * @param reader
     * @param retType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(Reader reader, Class<T> retType) {
        try {
            return (T) createUnmarshaller(retType).unmarshal(reader);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * create JAXB marshaller
     *
     * @param retType
     * @param encoding
     * @param decl
     * @param isFormat
     * @param adapter
     * @return
     */
    public static Marshaller createMarshaller(Class<?> retType, String encoding, String decl, boolean isFormat,
                                              XmlAdapter<?, ?> adapter) {
        Marshaller marshaller;
        try {
            marshaller = getJaxbContext(retType).createMarshaller();
            if (isFormat) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            }
            if (encoding != null && !encoding.isEmpty()) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            }
            if (decl != null && !decl.isEmpty()) {
                marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
            }
            if (adapter != null) {
                marshaller.setAdapter(adapter);
            }
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
        return marshaller;
    }

    private static Marshaller createMarshaller(Class<?> retType, String encoding) {
        Marshaller marshaller;
        try {
            marshaller = getJaxbContext(retType).createMarshaller();
            if (encoding != null && !encoding.isEmpty()) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            }
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
        return marshaller;
    }

    private static Unmarshaller createUnmarshaller(Class<?> retType) {
        try {
            return getJaxbContext(retType).createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Unmarshaller createUnmarshaller(Class<?> retType, XmlAdapter<?, ?> adapter) {
        Unmarshaller unm = createUnmarshaller(retType);
        unm.setAdapter(adapter);
        return unm;
    }

    private static JAXBContext getJaxbContext(Class<?> retType) {
        JAXBContext cxt = null;
        if (jaxbContextMap.containsKey(retType)) {
            cxt = jaxbContextMap.get(retType);
        }
        if (cxt == null) {
            try {
                cxt = JAXBContext.newInstance(retType, CollectionWrapper.class);
                jaxbContextMap.putIfAbsent(retType, cxt);
            } catch (JAXBException e) {
                throw new IllegalStateException(e);
            }
        }
        return cxt;
    }

    private static class CollectionWrapper {

        @XmlAnyElement
        protected Collection<?> collection;
    }

}

class InnerMapAdapter extends XmlAdapter<InnerMapAdapter.InnerAdaptedMap, Map<String, String>> {

    private DocumentBuilder docBuilder;

    public InnerMapAdapter() throws ParserConfigurationException {
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    static class InnerAdaptedMap {
        @XmlAnyElement
        public List<Element> elements = new ArrayList<Element>();
    }

    @Override
    public InnerAdaptedMap marshal(Map<String, String> map) {
        Document doc = docBuilder.newDocument();
        InnerAdaptedMap adaptedMap = new InnerAdaptedMap();
        for (Entry<String, String> entry : map.entrySet()) {
            Element el = doc.createElement(entry.getKey());
            el.setTextContent(entry.getValue());
            adaptedMap.elements.add(el);
        }
        return adaptedMap;
    }

    @Override
    public Map<String, String> unmarshal(InnerAdaptedMap adaptedMap) {
        Map<String, String> map = new HashMap<String, String>();
        for (Element element : adaptedMap.elements) {
            map.put(element.getLocalName(), element.getTextContent());
        }
        return map;
    }

}
