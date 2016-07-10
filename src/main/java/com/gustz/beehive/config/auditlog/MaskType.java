package com.gustz.beehive.config.auditlog;

/*
 * Mask type enum　
 *
 * <p>
 * <pre>
 * 身份证号: 171232********328X <br/>
 * 手机号: 139****8678 <br/>
 * 银行卡号: 6225****5736 <br/>
 * cvn2: **5 <br/>
 * </pre>
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
public enum MaskType {

    IdCard {
        /**
         * Get id card mask
         *
         * @param str
         * @return
         */
        @Override
        public String getMaskText(String str) {
            if (str == null || str.length() <= 10) {
                return str;
            }
            return str.substring(0, 6) + "********" + str.substring(str.length() - 4, str.length());
        }
    }, PhoneNum {
        /**
         * Get phone number mask
         *
         * @param str
         * @return
         */
        @Override
        public String getMaskText(String str) {
            if (str == null || str.length() <= 7) {
                return str;
            }
            return str.substring(0, 3) + "****" + str.substring(str.length() - 4, str.length());
        }
    }, BankCard {
        /**
         * Get bank card mask
         *
         * @param str
         * @return
         */
        @Override
        public String getMaskText(String str) {
            if (str == null || str.length() <= 8) {
                return str;
            }
            return str.substring(0, 4) + "****" + str.substring(str.length() - 4, str.length());
        }
    }, Cvn2 {
        /**
         * Get CVN2 mask
         *
         * @param str
         * @return
         */
        @Override
        public String getMaskText(String str) {
            if (str == null || str.length() != 3) {
                return str;
            }
            return "**" + str.substring(str.length() - 1, str.length());
        }
    };

    public abstract String getMaskText(String str);

}
