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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devefx.httpmapper.Configuration;
import org.devefx.httpmapper.annotate.Bean;
import org.devefx.httpmapper.http.RequestEntity;
import org.devefx.httpmapper.spring.handler.HandlerExecutionChain;
import org.devefx.httpmapper.utils.ParamNameResolver;
import org.devefx.httpmapper.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

/**
 * MapperMethod
 * @author Youqian Yue
 * @since 1.0
 */
public class MapperMethod {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ObjectMapper mapper;
	private final MapType mapType;
	private final HttpCommand command;
	private final MethodSignature method;
	private HandlerExecutionChain mappedHandler;
	
	public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
		this.command = new HttpCommand(config, mapperInterface, method);
		this.method = new MethodSignature(config, mapperInterface, method);
		this.mappedHandler = config.getHandlerExecutionChain(command.getUrl());
		
		this.mapper = new ObjectMapper();
		this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
	}
	
	@SuppressWarnings("unchecked")
	public Object execute(RestTemplate restTemplate, Object[] args) throws Exception {
		Object result = null;
		
		do {
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.valueOf(command.getContentType()));
			
			Map<String, Object> paramMap = null;
			Object param = method.convertArgsToCommandParam(args);
			if (param instanceof Map) {
				paramMap = (Map<String, Object>) param;
				body.setAll(paramMap);
			}
			
			URI uri = expandURI(command.getUrl(), args);
			
			RequestEntity requestEntity = new RequestEntity(body, headers, command.getHttpMethod(),
					uri, method.getReturnType());
			
			mappedHandler.onRequest(requestEntity);
			
			// FIXME: application/x-www-form-urlencoded
			if (headers.getContentType().includes(MediaType.APPLICATION_FORM_URLENCODED)) {
				if (paramMap != null) {
					for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
						Object value = entry.getValue();
						if (value != null && !ReflectUtils.isUserType(value)) {
							entry.setValue(String.valueOf(value));
						}
					}
					body.setAll(paramMap);
				} else if (param != null && ReflectUtils.isUserType(param)) {
					body.setAll(mapper.<Map<String, Object>>convertValue(param, mapType));
				}
			}
			
			if (requestEntity.getMethod() == HttpMethod.GET) {
				uri = appendUrlParams(requestEntity.getUrl(), body);
				requestEntity.setUrl(uri);
			}
			
			if (logger.isInfoEnabled()) {
				String preStr = command.getName() + " ====> ";
				logger.info(preStr + "Request: " + requestEntity.getUrl());
				logger.info(preStr + "Parameters: " + requestEntity.getBody());
				logger.info(preStr + "Headers: " + requestEntity.getHeaders());
			}
			
			ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity.getUrl(), requestEntity.getMethod(),
					new HttpEntity<>(requestEntity.getBody(), requestEntity.getHeaders()), JsonNode.class);
			
			if (logger.isInfoEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append(command.getName() + " ====> ");
				buf.append("Response: [status=").append(responseEntity.getStatusCode()).append("] ");
				if (responseEntity.hasBody()) {
					buf.append(responseEntity.getBody());
				}
				logger.info(buf.toString());
			}
			
			if (responseEntity != null) {
				org.devefx.httpmapper.http.ResponseEntity entity = new org.devefx.httpmapper.http.ResponseEntity(responseEntity.getBody(),
						responseEntity.getHeaders(), responseEntity.getStatusCode());
				
				mappedHandler.onResponse(requestEntity, entity);
				
				if (entity.hasBody()) {
					Object responseBody = entity.getBody();
					if (method.getRawType().isInstance(responseBody)) {
						result = responseBody;
						break;
					}
					
					JavaType valueType = mapper.getTypeFactory().constructType(method.getReturnType());
					if (responseBody instanceof String) {
						result = mapper.readValue((String) responseBody, valueType);
					} else {
						result = mapper.convertValue(responseBody, valueType);
					}
				}
			}
			
		} while (false);
		
		if (result == null && method.returnsPrimitive() && !method.returnsVoid()) {
			throw new BindingException("Mapper method '" + command.getUrl()
			          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
		}
		return result;
	}
	
	private URI expandURI(String url, Object[] args) 
			throws URISyntaxException {
		if (args != null) {
			return new UriTemplate(command.getUrl()).expand(args);
		}
		return new URI(url);
	}
	
	private URI appendUrlParams(URI uri, MultiValueMap<String, Object> body) throws URISyntaxException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
		for (Map.Entry<String, List<Object>> entry : body.entrySet()) {
			String key = entry.getKey();
			for (Object value : entry.getValue()) {
				if (value instanceof String) {
					builder.queryParam(key, (String) value);
				}
			}
		}
		UriComponents uriComponents = builder.build();
		return uriComponents.toUri();
	}
	
	public static class HttpCommand {
		
		private final String name;
		private final String url;
		private final HttpMethod httpMethod;
		private final String contentType;
		
		public HttpCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
			Bean beanInfo = mapperInterface.getAnnotation(Bean.class);
			if (beanInfo == null) {
				throw new BindingException("Invalid class for: " + mapperInterface.getName());
			}
			org.devefx.httpmapper.annotate.Method methodInfo = method.getAnnotation(org.devefx.httpmapper.annotate.Method.class);
			if (methodInfo == null) {
				throw new BindingException("Invalid method for: " + method.getName());
			} else {
				String baseUrl = beanInfo.baseUrl();
				if (StringUtils.hasText(baseUrl) ||
						StringUtils.hasText((baseUrl = configuration.getGlobalBaseUrl()))) {
					this.url = baseUrl.concat(methodInfo.value());
				} else {
					this.url = methodInfo.value();
				}
				this.httpMethod = methodInfo.httpMethod();
				this.contentType = methodInfo.contentType();
			}
			this.name = method.getName();
		}

		public String getName() {
			return name;
		}
		
		public String getUrl() {
			return url;
		}
		
		public HttpMethod getHttpMethod() {
			return httpMethod;
		}
		
		public String getContentType() {
			return contentType;
		}
	}

	public static class MethodSignature {
		
		private final boolean returnsVoid;
		private final boolean returnsPrimitive;
		private final Type returnType;
		private final Class<?> rawType;
		private final ParamNameResolver paramNameResolver;
		
		public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
			this.returnType = method.getGenericReturnType();
			if (returnType instanceof ParameterizedType) {
				this.rawType = (Class<?>) ((ParameterizedType) returnType).getRawType();
			} else {
				this.rawType = method.getReturnType();
			}
			this.returnsVoid = void.class.equals(this.returnType);
			this.returnsPrimitive = method.getReturnType().isPrimitive();
			this.paramNameResolver = new ParamNameResolver(configuration, method);
		}
		
		public Object convertArgsToCommandParam(Object[] args) {
			return paramNameResolver.getNamedParams(args);
		}
		
		public Type getReturnType() {
			return returnType;
		}
		
		public Class<?> getRawType() {
			return rawType;
		}
		
		public boolean returnsVoid() {
			return returnsVoid;
		}
		
		public boolean returnsPrimitive() {
			return returnsPrimitive;
		}
		
	}
	
	public static class ParamMap<V> extends HashMap<String, V> {

		private static final long serialVersionUID = 3143944370579285319L;
		
		@Override
		public V get(Object key) {
			if (!super.containsKey(key)) {
				throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
			}
			return super.get(key);
		}
	}
	
}
