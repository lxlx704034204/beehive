package com.gustz.beehive.util;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean util test
 *
 * @author zhangzhenfeng
 * @since 2016-02-18
 */
public class BeanUtilTest {

    public static void main(String[] args) {
        List list = new ArrayList();
        TestBean t = getTestBean();
        TestBean t2 = getTestBean();
        list.add(t);
        list.add(t2);
        list.add("123");
        //
        System.out.println(new Gson().toJson(list));
    }

    private static TestBean getTestBean() {
        TestBean t = new TestBean();
        t.setAge(1);
        t.setName("name");
        t.setId("id");
        return t;
    }

    @Test
    public void testDepthClone() throws Exception {
        TestBean t = getTestBean();
        System.out.println("before t: " + new Gson().toJson(t));
        //
        TestBean t2 = BeanUtil.depthClone(t);
        t2.setAge(2);
        System.out.println("after t: " + new Gson().toJson(t));
        System.out.println("after t2: " + new Gson().toJson(t2));
    }
}

class TestBean implements Serializable {

    private String id;

    private String name;

    private Integer age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}