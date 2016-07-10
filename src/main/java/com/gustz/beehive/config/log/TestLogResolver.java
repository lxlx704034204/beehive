package com.gustz.beehive.config.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger resolver for test log
 *
 * @author zhangzhenfeng
 * @since [May 3, 2015]
 */
@Aspect
//@Component
public class TestLogResolver {

    private Logger logger;

    private ThreadLocal<Long> bt = new ThreadLocal<Long>();

    private ThreadLocal<String> sn = new ThreadLocal<String>();

    private final Lock lock = new ReentrantLock();

    private static final ObjectMapper objMapper = new ObjectMapper();

    @Pointcut(value = "@annotation(com.gustz.beehive.config.log.TestLogger)")
    private void testLoggerPct() {
        // point cut method
    }

    /**
     * Do before
     *
     * @param jp
     */
    @Before("testLoggerPct()")
    public void doBefore(JoinPoint jp) {
        lock.lock();
        try {
            this.bt.remove();
            this.sn.remove();
            // set
            this.bt.set(System.currentTimeMillis());
            this.sn.set(this.bt.get() + "");
            // do before
            this.begin(jp);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Do after
     *
     * @param jp
     */
    @After("testLoggerPct()")
    public void doAfter(JoinPoint jp) {
        this.end(jp);
    }

    /**
     * Do after throwing
     *
     * @param jp
     * @param t
     */
    @AfterThrowing(value = "testLoggerPct()", throwing = "t")
    public void doAfterThrow(JoinPoint jp, Throwable t) {
        this.error(jp, t);
    }

    /**
     * Do around
     *
     * @param pjp
     * @throws Throwable
     */
    @Around("testLoggerPct()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object retVal = pjp.proceed();
        this.return_(retVal);
        return retVal;
    }

    /**
     * Get message
     *
     * @param method
     * @return
     */
    private static String getMsg(Method method) {
        if (method.isAnnotationPresent(TestLogger.class)) {
            return method.getAnnotation(TestLogger.class).value();
        }
        return "";
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
     * Begin: SN[ XX ] method[ XX ] args[ XX ]
     * </p>
     *
     * @param jp
     */
    private void begin(JoinPoint jp) {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        logger = LoggerFactory.getLogger(ms.getDeclaringTypeName());
        // content
        final String format = "Begin: SN[ {} ] method[ {} ] args[ {} ] ";
        logger.debug(format, this.sn.get(), ms.toShortString(), getArgsText(jp.getArgs()));
    }

    /**
     * End log format
     * <p>
     * End: SN[ XX ] msg[ XX ] use time[ XX ]ms
     * </p>
     *
     * @param jp
     */
    private void end(JoinPoint jp) {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        // content
        final String format = "End: SN[ {} ] msg[ {} ] use time[ {} ]ms ";
        logger.debug(format, this.sn.get(), getMsg(ms.getMethod()), (System.currentTimeMillis() - this.bt.get()) + "");
    }

    /**
     * Exception log format
     * <p>
     * Exception: SN[ XX ] method[ XX ] msg[ XX ]
     * </p>
     *
     * @param jp
     * @param t  Throwable
     */
    private void error(JoinPoint jp, Throwable t) {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        // content
        StringBuilder sbd = new StringBuilder();
        sbd.append("Exception: SN[ ").append(this.sn.get()).append(" ] ");
        sbd.append("method[ ").append(ms.toShortString()).append(" ] ");
        sbd.append("msg[ ").append(getMsg(ms.getMethod())).append(" ] ");
        logger.error(sbd.toString(), t); // no format
    }

    /**
     * Return log format
     * <p>
     * Return: SN[ XX ] msg[ XX ]
     * </p>
     *
     * @param rs
     */
    private void return_(Object rs) {
        final String format = "Return: SN[ {} ] msg[ {} ] ";
        logger.debug(format, this.sn.get(), (rs != null ? rs.toString() : "null"));
    }

}
