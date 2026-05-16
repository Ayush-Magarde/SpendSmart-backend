package com.spendsmart.recurring.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.spendsmart.recurring.controller.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("Entering {}.{} with {} arguments", 
                className, methodName, joinPoint.getArgs().length);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            
            log.info("Successfully completed {}.{} in {} ms", 
                    className, methodName, (endTime - startTime));
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            
            log.error("Exception in {}.{} after {} ms: {}", 
                    className, methodName, (endTime - startTime), e.getMessage(), e);
            
            throw e;
        }
    }

    @Around("execution(* com.spendsmart.recurring.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.debug("Entering service method {}.{}", className, methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            
            log.debug("Completed service method {}.{} in {} ms", 
                    className, methodName, (endTime - startTime));
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            
            log.error("Exception in service method {}.{} after {} ms: {}", 
                    className, methodName, (endTime - startTime), e.getMessage(), e);
            
            throw e;
        }
    }
}
