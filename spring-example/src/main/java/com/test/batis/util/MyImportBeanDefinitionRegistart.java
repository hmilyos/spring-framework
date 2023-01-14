package com.test.batis.util;

import com.test.batis.dao.AMapper;
import com.test.batis.dao.TMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j(topic = "e")
public class MyImportBeanDefinitionRegistart implements ImportBeanDefinitionRegistrar {
	Map<String,BeanDefinition> map= new HashMap<>();

	Set<BeanDefinitionHolder> beanDefinitionHolders = null;

	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
		//完成了扫描 --put bdmap会多两个bd
		Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(MyScan.class.getName());
		scan(registry,annotationAttributes);
		//a和T 替换成为fb
		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
			AbstractBeanDefinition mapperBd = (AbstractBeanDefinition) beanDefinitionHolder.getBeanDefinition();
			String beanClassName = mapperBd.getBeanClassName();
			mapperBd.setBeanClass(MyFactoryBean.class);
			mapperBd.getPropertyValues().add("mapperInterface",beanClassName);
		}
	}

	public void scan(BeanDefinitionRegistry registry,Map<String, Object> annotationAttributes){
		String scanPackageName = (String) annotationAttributes.get("value");
		MyMapperScaner scaner = new MyMapperScaner(registry,true);
		beanDefinitionHolders = scaner.doScan(scanPackageName);

//		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TMapper.class);
//		map.put("tmapper",builder.getBeanDefinition());
//		builder = BeanDefinitionBuilder.genericBeanDefinition(AMapper.class);
//		map.put("amapper",builder.getBeanDefinition());
	}


}
