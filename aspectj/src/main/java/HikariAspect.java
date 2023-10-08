import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
public class HikariAspect {

    private static final Logger LOG = LoggerFactory.getLogger(HikariAspect.class);

    private static final Set<String> BORROWERS = new HashSet<>();

    @AfterReturning("execution(* com.zaxxer.hikari.pool.HikariPool.getConnection(..))")
    public void afterAcquisition(JoinPoint joinPoint) {
        LOG.debug("hello there");
        BORROWERS.add(Thread.currentThread().getName());
    }

    @AfterThrowing("execution(* com.zaxxer.hikari.pool.HikariPool.getConnection(..))")
    public void afterFailedAcquisition(JoinPoint joinPoint) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        StringBuilder threadDump = new StringBuilder(System.lineSeparator());
        for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
            threadDump.append(threadInfo.toString());
        }
        LOG.debug("Threads which borrowed connections : {} \n  ThreadDump: {}", BORROWERS, threadDump);
    }

    @After("execution(* com.zaxxer.hikari.pool.PoolEntry.recycle(..))")
    public void afterRelease(JoinPoint joinPoint) {
        BORROWERS.remove(Thread.currentThread().getName());
        LOG.debug("hello there");
    }
}

