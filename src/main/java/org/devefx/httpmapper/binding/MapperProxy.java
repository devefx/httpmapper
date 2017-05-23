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

package org.devefx.httpmapper.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import org.devefx.httpmapper.Configuration;

/**
 * MapperProxy
 * @author Youqian Yue
 * @since 1.0
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 6041102287181454721L;
	private final Configuration configuration;
	private final Class<T> mapperInterface;
	private final Map<Method, MapperMethod> methodCache;

	public MapperProxy(Configuration configuration, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
		this.configuration = configuration;
		this.mapperInterface = mapperInterface;
		this.methodCache = methodCache;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			try {
				return method.invoke(this, args);
			} catch (Throwable wrapped) {
				throw unwrapThrowable(wrapped);
			}
		}
		final MapperMethod mapperMethod = cachedMapperMethod(method);
		return mapperMethod.execute(configuration.getRestTemplate(), args);
	}

	private MapperMethod cachedMapperMethod(Method method) {
		MapperMethod mapperMethod = methodCache.get(method);
		if (mapperMethod == null) {
			mapperMethod = new MapperMethod(mapperInterface, method, configuration);
			methodCache.put(method, mapperMethod);
		}
		return mapperMethod;
	}
	
	private Throwable unwrapThrowable(Throwable wrapped) {
		Throwable unwrapped = wrapped;
		while (true) {
			if (unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
			} else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
			} else {
				return unwrapped;
			}
		}
	}
	
}
