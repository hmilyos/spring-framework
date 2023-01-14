package com.test.visitor.util;

import java.util.ArrayList;
import java.util.List;

public class MetadataInfo {
	List<AnnotaionMetaDataInfo> list = new ArrayList<>();

	String clazzFullName;


	public List<AnnotaionMetaDataInfo> getList() {
		return list;
	}

	public String getClazzFullName() {
		return clazzFullName;
	}

	public void setClazzFullName(String clazzFullName) {
		this.clazzFullName = clazzFullName;
	}

	public void setList(List<AnnotaionMetaDataInfo> list) {
		this.list = list;
	}
}
