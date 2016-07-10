package com.gustz.beehive.config.auditlog.resolver;

import com.google.gson.Gson;
import com.gustz.beehive.config.auditlog.AuditLogArg;
import com.gustz.beehive.config.auditlog.MaskType;
import com.gustz.beehive.config.auditlog.MaskTypeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger resolver for base audit log
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
public abstract class BaseLogResolver {

    private static final Logger logger = LoggerFactory.getLogger(BaseLogResolver.class);

    private final Lock lock = new ReentrantLock();

    private static final AtomicLong SEQ = new AtomicLong(0);

    // hostname
    private static String hostName;

    static {
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    protected enum LogHelpers {
        stateless {

        }, session {

        };

    }

    protected class AuditLogInfo {

        private String module;

        private String item;

        public AuditLogInfo() {
        }

        public AuditLogInfo(String module, String item) {
            this.module = module;
            this.item = item;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }
    }

    /**
     * Write before audit log
     *
     * @param logHelpers
     * @param auditLogInfo
     * @param parameters
     * @param args
     */
    @SuppressWarnings("unchecked")
    protected void writeBeforeLog(LogHelpers logHelpers, AuditLogInfo auditLogInfo, Parameter[] parameters, final Object[] args) {
        try {
            lock.lock();
            // get method annotation args
            if (args == null || args.length == 0 || args[0] == null || parameters == null) {
                logger.debug("writeBeforeLog: args/parameters is null.");
                return;
            }
            final List annoArgs = new ArrayList<>();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == null) {
                    logger.debug("writeBeforeLog: annotation args is null.(for in)");
                    continue;
                }
                if (parameters[i].isAnnotationPresent(AuditLogArg.class)) {
                    annoArgs.add(args[i]);
                }
            }
            if (annoArgs.isEmpty()) {
                logger.warn("writeBeforeLog: annotation args list is null.");
                return;
            }
            // get audit log helper
            //AuditLogHelper helper = logHelpers.getAuditLogHelper(auditLogInfo);
            // replace mask and write log
            //helper.log(auditLogInfo.getMetric(), this.getMaskLog(annoArgs));
        } catch (Throwable t) {
            logger.error("writeBeforeLog: is fail.", t);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Write after return audit log
     *
     * @param logHelpers
     * @param auditLogInfo
     * @param args
     */
    @SuppressWarnings("unchecked")
    protected void writeAfterLog(LogHelpers logHelpers, AuditLogInfo auditLogInfo, final Object args) {
        try {
            lock.lock();
            // get method annotation args
            if (args == null) {
                logger.debug("writeAfterLog: args is null.");
                return;
            }
            // get audit log helper
            //AuditLogHelper helper = logHelpers.getAuditLogHelper(auditLogInfo);
            // replace mask and write log
            //helper.log(auditLogInfo.getMetric(), this.getMaskLog(Arrays.asList(args)));
        } catch (Throwable t) {
            logger.error("writeAfterLog: is fail.", t);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get mask log for fields
     *
     * @param annoArgs
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static String getMaskLog(final List annoArgs) throws Exception {
        final List newAnnoArgs = new ArrayList<>();
        for (final Object srcArg : annoArgs) {
            // copy arg object properties
            Object targetArg;
            try {
                targetArg = srcArg.getClass().newInstance();
                BeanUtils.copyProperties(srcArg, targetArg);
            } catch (Exception e) {
                logger.warn("getMaskLog: is fail.[ {} ]", e.getMessage());
                continue;
            }
            final Class cls = targetArg.getClass();
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (!f.isAnnotationPresent(MaskTypeLog.class)) {
                    continue;
                }
                String fieldName = f.getName();
                // get value
                Method getMethod = getMethod("get", cls, f);
                if (getMethod == null) {
                    logger.warn("getMaskLog: get GetMethod is null.fieldName={}", fieldName);
                    continue;
                }
                getMethod.setAccessible(true);
                final String val = (String) getMethod.invoke(targetArg);
                if (val == null || val.isEmpty()) {
                    logger.debug("getMaskLog: get GetMethod value is null.fieldName={}", fieldName);
                    continue;
                }
                // set mask value
                Method setMethod = getMethod("set", cls, f);
                if (setMethod == null) {
                    logger.warn("getMaskLog: get SetMethod is null.fieldName={}", fieldName);
                    continue;
                }
                MaskType maskType = f.getAnnotation(MaskTypeLog.class).value();
                setMethod.setAccessible(true);
                setMethod.invoke(targetArg, maskType.getMaskText(val));
            }
            newAnnoArgs.add(targetArg);
        }
        return new Gson().toJson(newAnnoArgs);
    }

    @SuppressWarnings("unchecked")
    private static Method getMethod(String flag, Class cls, Field field) {
        try {
            String fieldName = field.getName();
            final String methodName = flag + fieldName.replaceFirst(fieldName.substring(0, 1), fieldName.substring(0, 1).toUpperCase());
            //
            if ("get".equals(flag)) {
                return cls.getDeclaredMethod(methodName);
            } else if ("set".equals(flag)) {
                return cls.getDeclaredMethod(methodName, field.getType());
            }
        } catch (Exception e) {
            logger.warn("getMethod: is fail.", e);
        }
        return null;
    }

    /**
     * @param item the log's metric id. Its format is like 'WITH01', 'BCV01', etc.
     * @param logs the log's contents. It MUST a json string.
     */
    private void log(String appId, String module, String item, String logs) {
        if (item == null || logs == null) {
            return;
        }
        StringBuffer sb = new StringBuffer("{");
        sb.append(String.format("\"m_name\": \"%s\", ", item));
        sb.append(String.format("\"m_host\": \"%s\", ", hostName));
        sb.append(String.format("\"m_time\": %.3f, ", System.currentTimeMillis() / 1000.0));
        sb.append(String.format("\"m_appid\": \"%s\", ", appId));
        sb.append(String.format("\"m_module\": \"%s\", ", module));
        sb.append(String.format("\"m_id\": \"%s\", ", getUniqId(appId)));
        if (logs.startsWith("{")) {
            // "{}"
            if (logs.substring(1).trim().equals("}")) {
                sb.append("\"value\": \"{}\"}");
            } else {
                sb.append(logs.substring(1));
            }
        } else if (logs.startsWith("[")) {
            sb.append("\"value\": ");
            sb.append(logs).append("}");
        } else {
            sb.append(String.format("\"value\": \"%s\"}", logs.replace("\"", "'")));
        }

        logger.info("{}", sb.toString());
    }

    private static String getUniqId(String appId) {
        return String.format("%s-%x-", appId, System.currentTimeMillis()
                - 45L * 365 * 24 * 3600 * 1000) + SEQ.getAndIncrement();
    }

}
