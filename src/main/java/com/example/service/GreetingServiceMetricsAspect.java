package com.example.service;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Aspect
@Component
class GreetingServiceMetricsAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingServiceMetricsAspect.class);
    private final CounterService counterService;

    @Autowired
    public GreetingServiceMetricsAspect(CounterService counterService) {
        this.counterService = counterService;
    }

    @AfterReturning(pointcut = "execution(* com.example.service.GreetingService.getGreeting(int)) && args(number)", argNames = "number")
    public void afterCallingGetGreeting(int number) {
        LOGGER.debug("Triggered after calling getGreeting()");
        counterService.increment("counter.calls.get_greeting");
        counterService.increment("counter.calls.get_greeting." + number);
    }

    @AfterThrowing(pointcut = "execution(* com.example.service.GreetingService.getGreeting(int))", throwing = "e")
    public void afterGetGreetingThrowsException(NoSuchElementException e) {
        LOGGER.debug("Triggered after getGreeting() throws exception");
        counterService.increment("counter.errors.get_greeting");
    }

}
