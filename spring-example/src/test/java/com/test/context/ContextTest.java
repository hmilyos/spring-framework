package com.test.context;

import com.test.batis.bean.F;
import com.test.context.bean.A;
import com.test.context.bfpp.B;
import com.test.context.bfpp.C;
import com.test.context.bfpp.X;
import com.test.context.config.ContextConfig;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class ContextTest {

	@Test
	public void defaultContext(){

		//bean工厂
//		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
//
//
//		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(A.class);
//		//builder.getBeanDefinition().setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
//
//		beanFactory.registerBeanDefinition("a",builder.getBeanDefinition());
//
//		builder = BeanDefinitionBuilder.genericBeanDefinition(C.class);
//////
//		beanFactory.registerBeanDefinition("c",builder.getBeanDefinition());
//		AutowiredAnnotationBeanPostProcessor auto =
//				new AutowiredAnnotationBeanPostProcessor();
//		auto.setBeanFactory(beanFactory);
//		//beanFactory.addBeanPostProcessor(auto);
//		beanFactory.getBean(C.class);
//		A a = beanFactory.getBean(A.class);
//		a.getC();
//
		AnnotationConfigApplicationContext
				context = new AnnotationConfigApplicationContext();
		context.addBeanFactoryPostProcessor(new B());
		context.addBeanFactoryPostProcessor(new C());
		context.register(ContextConfig.class);
		context.refresh();
		System.out.println(context.getBean(X.class));


	}

	@Test
	public void xmlBeanFactoryScanContext(){
		//ClassPathXmlApplicationContext

		ClassPathResource classPathResource = new ClassPathResource("spring-context.xml");
		XmlBeanFactory beanFactory = new XmlBeanFactory(classPathResource);
		A a = beanFactory.getBean(A.class);
		a.getC();


//		AnnotationConfigApplicationContext
//				context = new AnnotationConfigApplicationContext(ContextConfig.class);


	}


	@Test
	public void ignoreContext(){
		AnnotationConfigApplicationContext
				context = new AnnotationConfigApplicationContext();

		context.scan("com.test.batis.bean");
		context.refresh();
		System.out.println(context.getBean(F.class).getK());
	}
}
