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

package org.devefx.httpmapper.http;

import java.lang.reflect.Type;
import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * RequestEntity
 * @author Youqian Yue
 * @since 1.0
 */
public class RequestEntity extends HttpEntity<MultiValueMap<String, Object>> {
	
	private HttpMethod method;

	private URI url;

	private Type type;
	
	/**
	 * Constructor with method and URL but without body nor headers.
	 * @param method the method
	 * @param url the URL
	 */
	public RequestEntity(HttpMethod method, URI url) {
		this(null, null, method, url);
	}
	
	/**
	 * Constructor with method, URL, body and type but without headers.
	 * @param body the body
	 * @param method the method
	 * @param url the URL
	 * @param type the type used for generic type resolution
	 */
	public RequestEntity(MultiValueMap<String, Object> body, HttpMethod method, URI url, Type type) {
		this(body, null, method, url, type);
	}
	
	/**
	 * Constructor with method, URL, headers and body.
	 * @param body the body
	 * @param headers the headers
	 * @param method the method
	 * @param url the URL
	 */
	public RequestEntity(MultiValueMap<String, Object> body, MultiValueMap<String, String> headers, HttpMethod method, URI url) {
		this(body, headers, method, url, null);
	}

	/**
	 * Constructor with method, URL, headers, body and type.
	 * @param body the body
	 * @param headers the headers
	 * @param method the method
	 * @param url the URL
	 * @param type the type used for generic type resolution
	 */
	public RequestEntity(MultiValueMap<String, Object> body, MultiValueMap<String, String> headers, HttpMethod method, URI url, Type type) {
		super(body, headers);
		this.method = method;
		this.url = url;
		this.type = type;
	}
	
	/**
	 * Return the HTTP method of the request.
	 * @return the HTTP method as an {@code HttpMethod} enum value
	 */
	public HttpMethod getMethod() {
		return this.method;
	}
	
	/**
	 * Set the HTTP method of the request.
	 * @param the HTTP method as an {@code HttpMethod} enum value
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	/**
	 * Return the URL of the request.
	 * @return the URL as a {@code URI}
	 */
	public URI getUrl() {
		return this.url;
	}

	/**
	 * Set the URL of the request.
	 * @return the URL as a {@code URI}
	 */
	public void setUrl(URI url) {
		this.url = url;
	}
	
	/**
	 * Return the type of the request's body.
	 * @return the request's body type, or {@code null} if not known
	 */
	public Type getType() {
		if (this.type == null) {
			MultiValueMap<String, Object> body = getBody();
			if (body != null) {
				this.type = body.getClass();
			}
		}
		return this.type;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		RequestEntity otherEntity = (RequestEntity) other;
		return (ObjectUtils.nullSafeEquals(getMethod(), otherEntity.getMethod()) &&
				ObjectUtils.nullSafeEquals(getUrl(), otherEntity.getUrl()));
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.method);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.url);
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		builder.append(getMethod());
		builder.append(' ');
		builder.append(getUrl());
		builder.append(',');
		MultiValueMap<String, Object> body = getBody();
		HttpHeaders headers = getHeaders();
		if (body != null) {
			builder.append(body);
			if (headers != null) {
				builder.append(',');
			}
		}
		if (headers != null) {
			builder.append(headers);
		}
		builder.append('>');
		return builder.toString();
	}
	
}
