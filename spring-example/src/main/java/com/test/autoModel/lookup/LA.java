package com.test.autoModel.lookup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "e")
public class LA {

	@Autowired
	LB lb;
	public void printInfo(){
		log.debug("lb-[{}]",lb);
		log.debug("lb-[{}]",lb);
	}
}
