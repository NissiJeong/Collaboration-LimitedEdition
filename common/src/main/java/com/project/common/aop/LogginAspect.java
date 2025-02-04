package com.project.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class LogginAspect {
    private Logger logger = LoggerFactory.getLogger(LogginAspect.class);

    @Around("execution(* com.project..service..*(..))") // 모든 서비스의 Service 메서드
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 메서드명 & 클래스명 가져오기
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 메서드 파라미터 가져오기
        Object[] args = joinPoint.getArgs();
        String arguments = args.length > 0 ? Arrays.toString(args) : "No arguments";

        logger.info("[Service Logging] Executing {}.{}() with args: {}", className, methodName, arguments);

        // 메서드 실행
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        logger.info("[Service Logging] {}.{}() executed in {} ms, returned: {}",
                className, methodName, (endTime - startTime), result);

        return result;
    }

    @Around("execution(* com.project..feignclient..*(..))")
    public Object logFeignClient(ProceedingJoinPoint joinPoint) throws Throwable {
        // 요청 메서드 및 대상 서비스 정보
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String arguments = args.length > 0 ? Arrays.toString(args) : "No arguments";

        logger.info("[Feign Client Request] Calling {}.{}() with args: {}", className, methodName, arguments);

        // 메서드 실행
        Object result = joinPoint.proceed();

        logger.info("[Feign Client Response] {}.{}() returned: {}", className, methodName, result);

        return result;
    }
}
