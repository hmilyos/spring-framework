package com.test.visitor.util;

import com.test.lifeCycle.util.JDKProxyBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface VisitorAnno {
}
