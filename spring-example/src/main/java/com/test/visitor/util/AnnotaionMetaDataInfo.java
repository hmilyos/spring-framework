package com.test.visitor.util;

public class AnnotaionMetaDataInfo {
	//注解类型
	Class annotationType;
	//源
	String annotationSource;
	//注解的名字
	String annotationName;

	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}

	public void setAnnotationSource(String annotationSource) {
		this.annotationSource = annotationSource;
	}

	public void setAnnotationType(Class annotationType) {
		this.annotationType = annotationType;
	}
}
