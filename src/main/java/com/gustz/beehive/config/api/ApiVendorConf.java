package com.gustz.beehive.config.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * API vendor config
 *
 * @author zhangzhenfeng
 * @since 2016-04-23
 */
@Component
public class ApiVendorConf {

    @Value("${api_vendor.codes}")
    private List<String> codes;

    @Value("${api_vendor.code.aks}")
    private List<String> codeAks;

    @Value("${api_vendor.code.aks}")
    private List<String> codeSks;

    @PostConstruct
    private void init() {
        List<String> list = this.codeAks;
        for (String s : list) {
            System.out.println("***=:" + s);
        }
    }

    public List<String> getCodes() {
        return codes;
    }

    public List<String> getCodeAks() {
        return codeAks;
    }

    public List<String> getCodeSks() {
        return codeSks;
    }
}
