package com.test.visitor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;

@Slf4j(topic = "e")
public class MyClassVisitor extends ClassVisitor {
	MetadataInfo metadataInfo = new MetadataInfo();
	public MyClassVisitor(int api) {
		super(api);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		log.debug("name:[{}]",name);
		log.debug("interfaces:[{}]",interfaces);
		metadataInfo.setClazzFullName(name);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		log.debug("descriptor:[{}]",descriptor);

		try {
			return new MyAnnotationClassVisitor(descriptor,metadataInfo.getList(),metadataInfo.getClazzFullName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}



	@Override
	public void visitEnd() {
		log.debug("end");
	}

	public MetadataInfo getMetadataInfo() {
		return metadataInfo;
	}
}
