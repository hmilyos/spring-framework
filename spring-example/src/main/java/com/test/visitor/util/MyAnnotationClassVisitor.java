package com.test.visitor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.util.ClassUtils;

import java.util.List;

@Slf4j(topic = "e")
public class MyAnnotationClassVisitor extends AnnotationVisitor {


	public MyAnnotationClassVisitor(String descriptor, List<AnnotaionMetaDataInfo> list, String sourceName) throws ClassNotFoundException {
		super(Opcodes.ASM7);
		String className = Type.getType(descriptor).getClassName();
		Class<?> aClass = ClassUtils.forName(className, MyAnnotationClassVisitor.class.getClassLoader());
		AnnotaionMetaDataInfo info = new AnnotaionMetaDataInfo();
		info.setAnnotationType(aClass);
		info.setAnnotationName(aClass.getSimpleName());
		info.setAnnotationSource(sourceName);
		list.add(info);
	}
}
