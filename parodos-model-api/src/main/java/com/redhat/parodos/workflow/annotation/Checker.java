package com.redhat.parodos.workflow.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Checker {
    String nextWorkFlowName();
    String cronExpression();
}
