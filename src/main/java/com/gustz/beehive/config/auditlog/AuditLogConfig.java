package com.gustz.beehive.config.auditlog;

/*
 * Audit log config　
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
public interface AuditLogConfig {

    /**
     * offer prize log
     */
    enum OfferPrizeLog {

        ;
        public static final String MODULE_NAME = "OFFER_PRIZE_LOG";

        public static final String MN1 = "OPL001";
        public static final String MN2 = "OPL002";
    }
}
