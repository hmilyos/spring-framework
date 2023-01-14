package com.test.visitor.test;

import com.test.visitor.util.MetadataInfo;
import com.test.visitor.util.MyClassVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.SpringAsmInfo;

import java.io.IOException;

public class TestVisitor {
	private static final int PARSING_OPTIONS = ClassReader.SKIP_DEBUG
			| ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES;
	public static void main(String[] args) {
		MyClassVisitor myClassVisitor = new MyClassVisitor(SpringAsmInfo.ASM_VERSION);
		try {
			ClassReader classReader = new ClassReader("com.test.visitor.bean.A");
			classReader.accept(myClassVisitor,PARSING_OPTIONS);
		} catch (IOException e) {
			e.printStackTrace();
		}

		MetadataInfo metadataInfo = myClassVisitor.getMetadataInfo();


	}
}
