package com.gustz.beehive.config.auditlog.resolver;

import com.gustz.beehive.config.auditlog.AuditLogArg;
import com.gustz.beehive.config.auditlog.MaskType;
import com.gustz.beehive.config.auditlog.MaskTypeLog;
import com.gustz.beehive.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * logger resolver for base audit log
 *
 * @author zhangzhenfeng
 * @since 2016-02-17
 */
public abstract class BaseLogResolver {

    private static final Logger logger = LoggerFactory.getLogger(BaseLogResolver.class);

    protected enum LogHelpers {
        stateless {
            //@Override
            //public AuditLogHelper getAuditLogHelper(AuditLogInfo auditLogInfo) {
            //    return AuditLogHelper.getLogger(auditLogInfo.getModule());
            //}
        }, session {
            //@Override
            //public AuditLogHelper getAuditLogHelper(AuditLogInfo auditLogInfo) {
            //    return AuditLogHelper.getSessionLogger(auditLogInfo.getModule());
            //}
        };

        //public abstract AuditLogHelper getAuditLogHelper(AuditLogInfo auditLogInfo);
    }

    protected class AuditLogInfo {

        private String module;

        private String metric;

        public AuditLogInfo() {
        }

        public AuditLogInfo(String module, String metric) {
            this.module = module;
            this.metric = metric;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }
    }

    /**
     * write before audit log
     *
     * @param logHelpers
     * @param auditLogInfo
     * @param parameters
     * @param args
     */
    @SuppressWarnings("unchecked")
    protected void writeBeforeLog(LogHelpers logHelpers, AuditLogInfo auditLogInfo, Parameter[] parameters, final Object[] args) {
        try {
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
            logger.error("writeBeforeLog: catch t.msg={}", t.getMessage());
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
            logger.error("writeAfterLog: catch t.msg={}", t.getMessage());
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
                logger.warn("getMaskLog: catch e.msg={}", e.getMessage());
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
        return JsonMapper.writeValueAsString(newAnnoArgs);
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
            logger.warn("getMethod: catch e.msg={}", e.getMessage());
        }
        return null;
    }

}
