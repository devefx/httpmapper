/*
 * Copyright 2016-2017, Youqian Yue (devefx@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devefx.httpmapper.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.devefx.httpmapper.Configuration;
import org.devefx.httpmapper.annotate.Param;
import org.devefx.httpmapper.binding.MapperMethod.ParamMap;

public class ParamNameResolver {

	private static final String PARAMETER_CLASS = "java.lang.reflect.Parameter";
	private static Method GET_NAME = null;
	private static Method GET_PARAMS = null;

	static {
		try {
			Class<?> paramClass = Resources.classForName(PARAMETER_CLASS);
			GET_NAME = paramClass.getMethod("getName");
			GET_PARAMS = Method.class.getMethod("getParameters");
		} catch (Exception e) {
			// ignore
		}
	}
	
	private final SortedMap<Integer, String> names;
	
	private boolean hasParamAnnotation;
	
	public ParamNameResolver(Configuration config, Method method) {
		final Annotation[][] paramAnnotations = method.getParameterAnnotations();
		final SortedMap<Integer, String> map = new TreeMap<>();
		int paramCount = paramAnnotations.length;
		// get names from @Param annotations
		for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
			String name = null;
			for (Annotation annotation : paramAnnotations[paramIndex]) {
				if (annotation instanceof Param) {
					hasParamAnnotation = true;
					name = ((Param) annotation).value();
					break;
				}
			}
			if (name == null) {
				if (config.isUseActualParamName()) {
					name = getActualParamName(method, paramIndex);
				}
				if (name == null) {
					// use the parameter index as the name ("0", "1", ...)
					name = String.valueOf(map.size());
				}
			}
			map.put(paramIndex, name);
		}
		names = Collections.unmodifiableSortedMap(map);
	}

	/**
	 * Returns parameter names referenced by SQL providers.
	 */
	private String getActualParamName(Method method, int paramIndex) {
		if (GET_PARAMS == null) {
			return null;
		}
		try {
			Object[] params = (Object[]) GET_PARAMS.invoke(method);
			return (String) GET_NAME.invoke(params[paramIndex]);
		} catch (Exception e) {
			throw new RuntimeException("Error occurred when invoking Method#getParameters().", e);
		}
	}

	/**
	 * <p>
	 * A single non-special parameter is returned without a name.<br />
	 * Multiple parameters are named using the naming rule.<br />
	 * In addition to the default names, this method also adds the generic names (param1, param2,
	 * ...).
	 * </p>
	 */
	public Object getNamedParams(Object[] args) {
		final int paramCount = names.size();
		if (args == null || paramCount == 0) {
			return null;
		} else if (!hasParamAnnotation && paramCount == 1) {
			return args[names.firstKey()];
		} else {
			final Map<String, Object> param = new ParamMap<Object>();
			for (Map.Entry<Integer, String> entry : names.entrySet()) {
				param.put(entry.getValue(), args[entry.getKey()]);
			}
			return param;
		}
	}
	
}
