/**
 * @(#)BeanUtil.java
 */
package com.gustz.beehive.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Depict: Dyna bean
 *
 * @author zhangzhenfeng
 * @date [2011-4-15]
 */
public abstract class BeanUtil extends BeanUtils {

    public static FastDateFormat datetimeDf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    public static FastDateFormat dateDf = FastDateFormat.getInstance("yyyy-MM-dd");

    public static FastDateFormat timeDf = FastDateFormat.getInstance("HH:mm:ss");

    static {
        ConvertUtils.register(new SqlDateConverter(null), java.sql.Date.class);
        ConvertUtils.register(new BigDecimalConverter(null), java.math.BigDecimal.class);
        ConvertUtils.register(new DateConverter(null), java.util.Date.class);
    }

    /**
     * Set properties method
     *
     * @param bean
     * @param name
     * @param value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void setProperty(Object bean, String name, Object value) throws IllegalAccessException,
            InvocationTargetException {
        BeanUtils.setProperty(bean, name, value);
    }

    /**
     * Copy properties
     *
     * @param dest
     * @param orig
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void copyProps(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {
        BeanUtils.copyProperties(dest, orig);
    }

    /**
     * Do bean method by method
     *
     * @param bean
     * @param method
     */
    public static void doBeanMethod(Object bean, Method method) throws Exception {
        // set access->public
        method.setAccessible(true);
        method.invoke(bean, new Object[0]);
    }

    /**
     * Do bean static method
     *
     * @param method
     * @throws Exception
     */
    public static void doStaticMethod(Method method) throws Exception {
        // set access->public
        method.setAccessible(true);
        method.invoke(null, new Object[0]);
    }

    /**
     * Read bean value
     *
     * @param bean
     * @param field
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T readBeanVal(Object bean, String field) throws Exception {
        if (bean == null || field == null || field.isEmpty()) {
            return null;
        }
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
        for (PropertyDescriptor desc : descriptors) {
            if (desc.getName().equals(field)) {
                return (T) desc.getReadMethod().invoke(bean);
            }
        }
        return null;
    }

    /**
     * Write bean value
     *
     * @param bean
     * @param field
     * @param value
     * @return
     * @throws Exception
     */
    public static void writeBeanVal(Object bean, String field, Object value) throws Exception {
        if (bean == null || field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Args 'bean/field' is null.");
        }
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
        for (PropertyDescriptor desc : descriptors) {
            if (desc.getName().equals(field)) {
                desc.getWriteMethod().invoke(bean, value);
            }
        }
    }

    /**
     * Bean values to list value
     *
     * @param fields
     * @param beanList
     */
    public static List<Map<String, Object>> beansToList(final String[] fields, List<?> beanList) {
        List<Map<String, Object>> _tmpList = null;
        Map<String, Object> _map = null;
        if (beanList != null && fields != null && fields.length > 0) {
            _tmpList = new LinkedList<Map<String, Object>>();
            for (Object _obj : beanList) {
                _map = beanToMap(fields, _obj);
                if (_map != null && !_map.isEmpty()) {
                    _tmpList.add(_map);
                }
            }
        }
        return _tmpList;
    }

    /**
     * Bean value to map value
     *
     * @param bean
     */
    public static Map<String, Object> beanToMap(final String[] fields, Object bean) {
        Map<String, Object> _map = new HashMap<String, Object>();
        if (bean instanceof DynaBean) {
            DynaProperty[] descriptors = ((DynaBean) bean).getDynaClass().getDynaProperties();
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (name != null && !name.isEmpty()) {
                    L1:
                    for (String _field : fields) {
                        if (name.equals(_field)) {
                            try {
                                _map.put(name, getProperty(bean, name));
                                break L1;
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }
                }
            }
        } else {
            PropertyUtilsBean utilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            PropertyDescriptor[] descriptors = utilsBean.getPropertyDescriptors(bean);
            Class<?> clazz = bean.getClass();
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (name != null && !name.isEmpty()) {
                    L1:
                    for (String _field : fields) {
                        if (name.equals(_field) && MethodUtils.getAccessibleMethod(clazz, descriptors[i].getReadMethod()) != null) {
                            try {
                                _map.put(name, getProperty(bean, name));
                                break L1;
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }
                }
            }
        }
        return _map;
    }

    /**
     * Bean value to map value
     *
     * @param bean
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> beanToMap(Object bean) throws Exception {
        return describe(bean);
    }

    /**
     * List map to list beans
     *
     * @param clazz
     * @param dataList
     * @throws Exception
     */
    public static List<?> listMapToBeans(Class<?> clazz, List<Map<String, ?>> dataList) throws Exception {
        if (clazz == null || dataList == null || dataList.isEmpty()) {
            return dataList;
        }
        Object _obj = null;
        List<Object> _tmpList = new LinkedList<Object>();
        for (Map<String, ?> _map : dataList) {
            if (_map != null) {
                _obj = mapToBean(_map, clazz);
                if (_obj != null) {
                    _tmpList.add(_obj);
                }
            }
        }
        dataList = null; // to null
        return _tmpList;
    }

    /**
     * List value to bean values
     *
     * @param clsList
     * @param dataList
     * @throws Exception
     */
    public static List<?> listToBeans(List<Class<?>> clsList, List<Map<String, Object>> dataList) throws Exception {
        Object _obj = null;
        List<Object> _tmpList = null;
        if (dataList != null && !dataList.isEmpty()) {
            _tmpList = new ArrayList<Object>();
            for (int i = 0, s = dataList.size(); i < s; i++) {
                _obj = mapToBean(dataList.get(i), clsList.get(i));
                if (_obj != null) {
                    _tmpList.add(_obj);
                }
            }
            clsList = null;
            dataList = null;
        }
        return _tmpList;
    }

    /**
     * Map value to bean value
     *
     * @param map
     * @param retType
     * @throws Exception
     */
    public static <T> T mapToBean(Map<String, ?> map, Class<T> retType) throws Exception {
        T _bean = retType.newInstance();
        BeanUtils.populate(_bean, map);
        return _bean;
    }

    /**
     * Do getMethod by method name
     *
     * @param bean
     * @param method
     */
    public static Object doGet(Object bean, Method method) {
        Object obj = null;
        if (bean != null && method != null) {
            try {
                // set access->public
                method.setAccessible(true);
                obj = method.invoke(bean, new Object[0]);
            } catch (Exception ex) {
                obj = null;
            }
        }
        return obj;
    }

    public static String setFirstUpper(String str) {
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toUpperCase());
    }

    /**
     * String array to list
     *
     * @param strs
     * @return
     */
    public static List<String> toList(String[] strs) {
        return Arrays.asList(strs);
    }

    /**
     * Properties to map
     *
     * @param props
     * @return map<String, String>
     */
    public static Map<?, ?> toMap(Properties props) {
        Map<Object, Object> _map = null;
        if (props != null && !props.isEmpty()) {
            _map = new ConcurrentHashMap<Object, Object>();
            _map.putAll(props);
        }
        return _map;
    }

    /**
     * String array to string
     *
     * @param strs
     * @return 1, 2, 3
     */
    public static String toString(String[] strs) {
        String _str = Arrays.toString(strs);
        _str = _str.replace("[", "");
        return _str.replace("]", "");
    }

    /**
     * Bean To string
     *
     * @param bean
     * @return
     */
    public static String beanToStr(Object bean) {
        Class<?> clazz = bean.getClass();
        StringBuilder sbd = new StringBuilder(clazz.getName());
        sbd.append("@").append(clazz.hashCode());
        try {
            sbd.append("[");
            int i = 0;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (i > 0) {
                    sbd.append(", ");
                }
                field.setAccessible(true);
                sbd.append(field.getName());
                sbd.append("=");
                sbd.append(field.get(bean));
                i++;
            }
            sbd.append("]");
        } catch (Exception e) {
            //ignore exception
        }
        return sbd.toString();
    }

    /**
     * Merge array
     *
     * @param array
     * @param subArray
     */
    public static String[] mergeArray(String[] array, String[] subArray) {
        int _index = 0;
        String[] _newArray = null;
        String _str = null;
        if (array != null && subArray != null) {
            _newArray = new String[array.length + subArray.length];
            for (int i = 0, len = array.length; i < len; i++) {
                _str = array[i];
                if (i == (len - 1)) {
                    _newArray[_index] = _str;
                    for (int j = 0, jlen = subArray.length; j < jlen; j++) {
                        _newArray[++_index] = subArray[j];
                    }
                    return _newArray;
                } else {
                    _newArray[_index] = _str;
                }
                _index++;
            }
        }
        return array;
    }

    /**
     * Collection to list,list element is object.
     *
     * @param coll
     * @return
     */
    public static List<?> toList(Collection<?> coll) {
        List<Object> _list = null;
        if (coll != null && !coll.isEmpty()) {
            _list = new LinkedList<Object>();
            for (Iterator<?> iter = coll.iterator(); iter.hasNext(); ) {
                _list.add(iter.next());
            }
        }
        return _list;
    }

    /**
     * T[] to set,set element is T.
     *
     * @param ts
     * @return
     */
    public static <T> Set<T> toSet(T[] ts) {
        Set<T> _set = null;
        if (ts != null) {
            _set = new HashSet<T>();
            for (T _t : ts) {
                _set.add(_t);
            }
        }
        return _set;
    }

    /**
     * Collection to object array
     *
     * @param coll
     * @return
     */
    public static <T> T[] toArray(Collection<T> coll, Class<T> type) {
        T[] ts = null;
        int i = 0;
        if (coll != null && coll.size() > 0) {
            ts = createArray(coll.size(), type);
            for (Iterator<T> iter = coll.iterator(); iter.hasNext(); ) {
                ts[i] = iter.next();
                i++;
            }
        }
        return ts;
    }

    /**
     * Create array
     *
     * @param size
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(int size, Class<T> type) {
        return (T[]) Array.newInstance(type, size);
    }

    /**
     * Collection to method array
     *
     * @param coll
     * @return
     */
    public static Method[] toMethod(Collection<Method> coll) {
        Method[] _tmps = null;
        int i = 0;
        if (coll != null && !coll.isEmpty()) {
            _tmps = new Method[coll.size()];
            for (Iterator<Method> iter = coll.iterator(); iter.hasNext(); ) {
                _tmps[i] = iter.next();
                i++;
            }
        }
        return _tmps;
    }

    /**
     * Collection to field array
     *
     * @param coll
     * @return
     */
    public static Field[] toField(Collection<Field> coll) {
        Field[] _tmps = null;
        int i = 0;
        if (coll != null && !coll.isEmpty()) {
            _tmps = new Field[coll.size()];
            for (Iterator<Field> iter = coll.iterator(); iter.hasNext(); ) {
                _tmps[i] = iter.next();
                i++;
            }
        }
        return _tmps;
    }

    /**
     * Collection to string array
     *
     * @param coll
     */
    public static String[] toStrArray(Collection<String> coll) {
        return toArray(coll, String.class);
    }

    /**
     * Collection to int array
     *
     * @param coll
     */
    public static int[] toIntArray(Collection<String> coll) {
        int[] array = null;
        String[] ss = toStrArray(coll);
        if (ss != null) {
            array = new int[ss.length];
            for (int i = 0, len = ss.length; i < len; i++) {
                array[i] = Integer.parseInt(String.valueOf(ss[i]));
            }
        }
        return array;
    }

    /**
     * Remove string array
     *
     * @param rmflag
     * @param array
     */
    public static String[] removeArray(final String rmflag, String[] array) {
        String[] _temps = null;
        String temp = null;
        List<String> tempList = null;
        if (array != null) {
            tempList = new LinkedList<String>();
            for (int i = 0, len = array.length; i < len; i++) {
                temp = array[i];
                if (temp != null) {
                    if (!temp.equals(rmflag)) {
                        tempList.add(temp);
                    }
                }
            }
            _temps = toStrArray(tempList);
        }
        return _temps;
    }

    /**
     * Remove string array
     *
     * @param rmIndex
     * @param array
     */
    public static String[] removeArray(final int rmIndex, String[] array) {
        String[] _temps = null;
        List<String> tempList = null;
        if (array != null) {
            tempList = new LinkedList<String>();
            for (int i = 0, len = array.length; i < len; i++) {
                if (i != rmIndex) {
                    tempList.add(array[i]);
                }
            }
            _temps = toStrArray(tempList);
        }
        return _temps;
    }

    /**
     * Clear string null/blank of array
     *
     * @param array
     */
    public static String[] clearNullArray(String[] array) {
        String[] _temps = null;
        String temp = null;
        List<String> tempList = null;
        if (array != null) {
            tempList = new LinkedList<String>();
            for (int i = 0, len = array.length; i < len; i++) {
                temp = array[i];
                if (temp != null) {
                    tempList.add(temp.trim());
                }
            }
            _temps = toStrArray(tempList);
        }
        return _temps;
    }

    /**
     * Clear object null
     *
     * @param array
     */
    public static Object[] clearArray(Object[] array) {
        Object[] _objs = null;
        if (array != null) {
            _objs = new Object[array.length];
            for (int i = 0, len = array.length; i < len; i++) {
                if (array[i] != null) {
                    _objs[i] = array[i];
                }
            }
        }
        return _objs;
    }

    /**
     * Get property value by key
     *
     * @param prop
     * @param value
     * @return
     */
    public static final String getPropertyKey(Properties prop, String value) {
        if (value != null && !value.isEmpty() && prop != null && prop.containsValue(value)) {
            for (Map.Entry<Object, Object> _entry : prop.entrySet()) {
                if (value.equals(_entry.getValue())) {
                    return (String) _entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Get current parent class name
     *
     * @return
     */
    public static String getCurrPtClsName() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 3) {
            return (stes[2].getClassName());
        }
        return null;
    }

    /**
     * Get current parent method name
     *
     * @return
     */
    public static String getCurrPtMethodName() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 3) {
            return stes[2].getMethodName();
        }
        return null;
    }

    /**
     * Get current parent line number
     *
     * @return
     */
    public static long getCurrPtLineNum() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 3) {
            return stes[2].getLineNumber();
        }
        return 0L;
    }

    /**
     * Get current class file name
     *
     * @return
     */
    public static String getCurrClsFileName() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 2) {
            return stes[1].getFileName();
        }
        return null;
    }

    /**
     * Get current class name
     *
     * @return
     */
    public static String getCurrClsName() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 2) {
            return stes[1].getClassName();
        }
        return null;
    }

    /**
     * Get current method name
     *
     * @return
     */
    public static String getCurrMethodName() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 2) {
            return stes[1].getMethodName();
        }
        return null;
    }

    /**
     * Get current class line number
     *
     * @return
     */
    public static long getCurrClsLineNum() {
        StackTraceElement[] stes = (new Exception()).getStackTrace();
        if (stes != null && stes.length >= 2) {
            return stes[1].getLineNumber();
        }
        return 0L;
    }

    /**
     * Get list first string
     *
     * @param list [0,1,2]
     * @return
     */
    public static String getListFirstStr(String list) {
        if (list != null) {
            list = (list.replace("[", "").replace("]", "")).split(",")[0];
        }
        return list;
    }

    /**
     * String list to List
     *
     * @param list ["a","b","c"]
     * @return List<\String>
     */
    public static List<String> toList(String list) {
        if (list != null && list.contains("[") && list.contains("]")) {
            list = list.replace("[", "").replace("]", "");
            return Arrays.asList(list.trim().split(","));
        }
        return null;
    }

    /**
     * String list to string array
     *
     * @param list ["a","b","c"]
     * @return String[]
     */
    public static String[] toStrArray(String list) {
        return toStrArray(toList(list));
    }

    /**
     * Depth copy object
     *
     * @param srcObj
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> T depthClone(Object srcObj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = null;
        ObjectOutputStream objOut = null;
        ObjectInputStream objIn = null;
        try {
            // write object
            out = new ByteArrayOutputStream();
            objOut = new ObjectOutputStream(out);
            objOut.writeObject(srcObj);
            // read object
            objIn = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
            return (T) objIn.readObject();
        } finally {
            if (out != null) {
                out.close();
            }
            if (objOut != null) {
                objOut.close();
            }
            if (objIn != null) {
                objIn.close();
            }
        }
    }

    /**
     * Is chinese
     * <p>
     * chinese sign and chinese
     * </p>
     *
     * @param str
     * @return
     */
    public static boolean isChinese(String str) {
        if (StringUtils.isNotBlank(str)) {
            char[] chs = str.toCharArray();
            for (int i = 0, len = chs.length; i < len; i++) {
                if (isChinese(chs[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Is chinese
     * <p>
     * chinese sign and chinese
     * </p>
     *
     * @param ch
     * @return
     */
    public static boolean isChinese(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * Is letter
     *
     * @param str
     * @return
     */
    public static boolean isLetter(String str) {
        return Pattern.compile("^[a-zA-Z]*").matcher(str).matches();
    }

    /**
     * Is number and letter
     *
     * @param str
     * @return
     */
    public static boolean isNumAndLetter(String str) {
        return Pattern.compile("^[A-Za-z0-9]+$").matcher(str).matches();
    }

    /**
     * Delete repeat value
     *
     * @param map
     */
    public static void delRepeatVal(Map<String, StringBuilder> map) {
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, StringBuilder> _entry : map.entrySet()) {
                if (_entry.getValue() != null) {
                    String[] _tmps = _entry.getValue().toString().split(",");
                    List<String> _tmpList = new CopyOnWriteArrayList<String>();
                    for (int i = 0, len = _tmps.length; i < len; i++) {
                        if (StringUtils.isNotBlank(_tmps[i]) && !_tmpList.contains(_tmps[i])) {
                            _tmpList.add(_tmps[i]);
                        }
                    }
                    map.put(_entry.getKey(), new StringBuilder(BeanUtil.toString(BeanUtil.toStrArray(_tmpList))));
                }
            }
        }
    }

    /**
     * filter error char like SQL
     *
     * @param sql
     * @param parArr
     * @return
     */
    public static String filterLikeSql(String sql, final Object[] parArr) {
        if (parArr == null || parArr.length == 0 || sql == null || !sql.contains("like")) {
            return sql;
        }
        boolean flag = false;
        final int len = parArr.length;
        for (int i = 0; i < len; i++) {
            Object _par = parArr[i];
            if (_par instanceof String && _par.toString().contains("_")) {
                flag = true;
                _par = _par.toString().replace("_", "/_");
            }
            parArr[i] = _par;
        }
        if (flag) {
            sql += " escape '/' ";
        }
        return sql;
    }

}
