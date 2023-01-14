package com.test.batis.config;

import com.test.batis.util.MyScan;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.test.batis")
@MapperScan(value = "com.test.batis.dao")
//@ImportResource("classpath:spring-batis.xml")
//@Import(MyImportBeanDefinitionRegistart.class)
@MyScan("com.test.batis.dao")
public class BatisConfig {

	@Bean
	public DataSource dataSource(){
		DriverManagerDataSource driverManagerDataSource
				= new DriverManagerDataSource();
		driverManagerDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		driverManagerDataSource.setPassword("123456");
		driverManagerDataSource.setUsername("root");
		driverManagerDataSource.setUrl("jdbc:mysql://localhost:3306/shadow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
		return driverManagerDataSource;
	}
//
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource());
		return factoryBean.getObject();
	}

	@Configuration
	@Import(Bean1.class)
	public static class AutoConfig{


	}


//	@Bean
//	public TMapper tMapper(){
//		TMapper mapper = (TMapper) MySqlSession.getMapper(TMapper.class);
//		return mapper;
//	}
}
