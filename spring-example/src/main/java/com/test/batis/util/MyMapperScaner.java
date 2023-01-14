package com.test.batis.util;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 1、不要试用spring那套过滤器
 * 2、注册了一个过滤器Include --match方法永远返回true
 * 3、重写他的isCandidateComponent(BeanDefinition bd)
 * 4、为了获取这一次扫描出来的bd 需要重写doScan
 * 只有doScan方法才会返回扫描出来的bd给我们
 */
public class MyMapperScaner extends ClassPathBeanDefinitionScanner {
	public MyMapperScaner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		super(registry, useDefaultFilters);
	}
	@Override
	protected void registerDefaultFilters() {
		TypeFilter typeFilter = new TypeFilter() {
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
				return true;
			}
		};
		addIncludeFilter(typeFilter);
	}

	public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition){
		return beanDefinition.getMetadata().isInterface();
	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		return  super.doScan(basePackages);
	}
}
