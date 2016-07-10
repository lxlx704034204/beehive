package com.gustz.beehive.config.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger resolver for base log
 *
 * @author zhangzhenfeng
 * @since [May 3, 2015]
 */
public abstract class BaseLogResolver {

    private Logger logger;

    private final ThreadLocal<Long> bt = new ThreadLocal<Long>();

    private final ThreadLocal<String> sn = new ThreadLocal<String>();

    private final Lock lock = new ReentrantLock();

    private static final ObjectMapper objMapper = new ObjectMapper();

    /**
     * Do write log
     *
     * @param prefix
     * @param msg
     * @param jp
     * @return
     * @throws Throwable
     */
    protected Object doWriteLog(String prefix, String msg, ProceedingJoinPoint jp) throws Throwable {
        if (prefix == null) {
            prefix = " ";
        }
        if (msg == null) {
            msg = " ";
        }
        Object retVal = null;
        try {
            // s1:
            this.doBefore(prefix, jp);
            // s2:
            retVal = jp.proceed();
            this.return_(prefix, retVal);
        } catch (Throwable t) {
            // s2:
            this.error(prefix, msg, jp, t);
            throw t;
        } finally {
            // s3:
            this.end(prefix, msg, jp);
        }
        return retVal;
    }

    /**
     * Do before
     *
     * @param prefix
     * @param jp
     */
    private void doBefore(String prefix, JoinPoint jp) {
        lock.lock();
        try {
            this.bt.remove();
            this.sn.remove();
            // set
            this.bt.set(System.currentTimeMillis());
            this.sn.set(this.bt.get() + "");
            // do before
            this.begin(prefix, jp);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get args text
     *
     * @param args
     * @return
     */
    private static String getArgsText(Object[] args) {
        StringBuilder sbd = new StringBuilder();
        for (int i = 0, len = args.length; i < len; i++) {
            if (i > 0) {
                sbd.append(", ");
            }
            sbd.append("index=" + i).append(",v=");
            try {
                sbd.append(objMapper.writeValueAsString(args[i]));
            } catch (Exception e) {
                sbd.append(args[i]);
            }
        }
        return sbd.toString();
    }

    /**
     * Begin log format
     * <p>
     * (prefix) Begin: SN=XX method=XX args[ XX ]
     * </p>
     *
     * @param prefix
     * @param jp
     */
    private void begin(String prefix, JoinPoint jp) {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        logger = LoggerFactory.getLogger(ms.getDeclaringTypeName());
        // content
        final String format = "(" + prefix + ") Begin: SN={} method={} args[ {} ] ";
        logger.info(format, this.sn.get(), ms.toShortString(), getArgsText(jp.getArgs()));
    }

    /**
     * End log format
     * <p>
     * (prefix) End: SN=XX msg[ XX ] use time=XXms
     * </p>
     *
     * @param prefix
     * @param msg
     * @param jp
     */
    private void end(String prefix, String msg, JoinPoint jp) {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        // content
        final String format = "(" + prefix + ") End: SN={} msg[ {} ] use time={} ms ";
        logger.info(format, this.sn.get(), msg, (System.currentTimeMillis() - this.bt.get()) + "");
    }

    /**
     * Exception log format
     * <p>
     * (prefix) Exception: SN=XX method=XX msg[ XX ]
     * </p>
     *
     * @param prefix
     * @param msg
     * @param jp
     * @param t      Throwable
     */
    private void error(String prefix, String msg, JoinPoint jp, Throwable t) {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        // content
        StringBuilder sbd = new StringBuilder();
        sbd.append("(").append(prefix).append(") ");
        sbd.append("Exception: SN=").append(this.sn.get());
        sbd.append(" method=").append(ms.toShortString());
        sbd.append(" msg[ ").append(msg).append(" ] ");
        logger.error(sbd.toString()+"- {} ", t.getMessage()); // no format
    }

    /**
     * Return log format
     * <p>
     * (prefix) Return: SN=XX msg[ XX ]
     * </p>
     *
     * @param prefix
     * @param rs
     */
    private void return_(String prefix, Object rs) {
        final String format = "(" + prefix + ") Return: SN={} msg[ {} ] ";
        logger.info(format, this.sn.get(), (rs != null ? rs.toString() : "null"));
    }

}
