package com.gustz.beehive.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB CDATA Adapter
 *
 * @author zhangzhenfeng
 * @date [Nov 7, 2014]
 */
public class CDataJaxbAdapter extends XmlAdapter<String, String> {

    private static final String PREFIX_CDATA = "<![CDATA[";

    private static final String SUFFIX_CDATA = "]]>";

    /**
     * XML text to bean value
     *
     * @param xml
     * @return
     * @throws Exception
     */
    @Override
    public String unmarshal(String xml) throws Exception {
        if (xml != null && !xml.isEmpty()) {
            xml = xml.replace(PREFIX_CDATA, "").replace(SUFFIX_CDATA, "");
        }
        return xml;
    }

    /**
     * Bean value to XML text
     *
     * @param val
     * @return
     * @throws Exception
     */
    @Override
    public String marshal(String val) throws Exception {
        if (val != null && !val.isEmpty() && !val.contains(PREFIX_CDATA)) {
            val = PREFIX_CDATA + val + SUFFIX_CDATA;
        }
        return val;
    }

    public static String getMarshal(String val) {
        if (val != null && !val.isEmpty() && !val.contains(PREFIX_CDATA)) {
            val = PREFIX_CDATA + val + SUFFIX_CDATA;
        }
        return val;
    }

    public static String getUnmarshal(String xml) {
        if (xml != null && !xml.isEmpty()) {
            xml = xml.replace(PREFIX_CDATA, "").replace(SUFFIX_CDATA, "");
        }
        return xml;
    }

}
