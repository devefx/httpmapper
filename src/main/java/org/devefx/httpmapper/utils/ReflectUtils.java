package org.devefx.httpmapper.utils;

public abstract class ReflectUtils {

	public static boolean isUserType(Object object) {
		return isUserType(object.getClass());
	}
	
	public static boolean isUserType(Class<?> clazz) {
		if (clazz != null && clazz.getClassLoader() != null) {
			return true;
		}
		return false;
	}
	
}
