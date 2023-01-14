package com.test.enhancer;

import com.test.enhancer.config.Appconfig;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class EnhancerTest {
	@Test
	public void defaultTest(){
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(Appconfig.class);

		BeanDefinition appconfig = context.getBeanDefinition("appconfig");
		Appconfig bean = context.getBean(Appconfig.class);
		System.out.println("");
	}
}
