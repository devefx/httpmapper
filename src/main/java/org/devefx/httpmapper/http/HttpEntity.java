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

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * HttpEntity
 * @author Youqian Yue
 * @since 1.0
 */
public class HttpEntity<T> {
	
	private HttpHeaders headers;
	
	private T body;
	
	/**
	 * Create a new {@code HttpEntity} with the given body and no headers.
	 * @param body the entity body
	 */
	public HttpEntity(T body) {
		this(body, null);
	}
	
	/**
	 * Create a new {@code HttpEntity} with the given headers and no body.
	 * @param headers the entity headers
	 */
	public HttpEntity(MultiValueMap<String, String> headers) {
		this(null, headers);
	}
	
	/**
	 * Create a new {@code HttpEntity} with the given body and headers.
	 * @param body the entity body
	 * @param headers the entity headers
	 */
	public HttpEntity(T body, MultiValueMap<String, String> headers) {
		this.body = body;
		HttpHeaders tempHeaders = new HttpHeaders();
		if (headers != null) {
			tempHeaders.putAll(headers);
		}
		this.headers = tempHeaders;
	}
	
	/**
	 * Returns the headers of this entity.
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}
	
	/**
	 * Set the headers of this entity.
	 */
	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}
	
	/**
	 * Returns the body of this entity.
	 */
	public T getBody() {
		return this.body;
	}
	
	/**
	 * Set the body of this entity.
	 */
	public void setBody(T body) {
		this.body = body;
	}
	
	/**
	 * Indicates whether this entity has a body.
	 */
	public boolean hasBody() {
		return (this.body != null);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || other.getClass() != getClass()) {
			return false;
		}
		HttpEntity<?> otherEntity = (HttpEntity<?>) other;
		return (ObjectUtils.nullSafeEquals(this.headers, otherEntity.headers) &&
				ObjectUtils.nullSafeEquals(this.body, otherEntity.body));
	}

	@Override
	public int hashCode() {
		return (ObjectUtils.nullSafeHashCode(this.headers) * 29 + ObjectUtils.nullSafeHashCode(this.body));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		if (this.body != null) {
			builder.append(this.body);
			if (this.headers != null) {
				builder.append(',');
			}
		}
		if (this.headers != null) {
			builder.append(this.headers);
		}
		builder.append('>');
		return builder.toString();
	}
	
}
