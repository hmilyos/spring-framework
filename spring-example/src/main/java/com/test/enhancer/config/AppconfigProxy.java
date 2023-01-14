package com.test.enhancer.config;

import com.test.enhancer.bean.X;
import com.test.enhancer.bean.Y;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassEnhancer;

import java.io.File;

@Configuration
@ComponentScan("com.test.enhancer.bean")
@Slf4j(topic = "e")
public class AppconfigProxy extends Appconfig implements ConfigurationClassEnhancer.EnhancedConfiguration {
	//cglib在生成这个代理类的时候动态添加的一个属性
	BeanFactory $$beanFactory;

	@Bean
	public X x(){
		//BeanMethodInterceptor.intercept
		return null;
	}



	@Bean
	public Y y(){
		//BeanMethodInterceptor.intercept
		return y();
	}


	// 什么时候调用？----？
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		//BeanFactoryAwareMethodInterceptor.intercept(this)
	}

}
