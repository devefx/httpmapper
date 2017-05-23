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
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * ResponseEntity
 * @author Youqian Yue
 * @since 1.0
 */
public class ResponseEntity extends HttpEntity<Object> {
	
	private final Object statusCode;


	/**
	 * Create a new {@code ResponseEntity} with the given status code, and no body nor headers.
	 * @param status the status code
	 */
	public ResponseEntity(HttpStatus status) {
		this(null, null, status);
	}

	/**
	 * Create a new {@code ResponseEntity} with the given body and status code, and no headers.
	 * @param body the entity body
	 * @param status the status code
	 */
	public ResponseEntity(Object body, HttpStatus status) {
		this(body, null, status);
	}

	/**
	 * Create a new {@code HttpEntity} with the given headers and status code, and no body.
	 * @param headers the entity headers
	 * @param status the status code
	 */
	public ResponseEntity(MultiValueMap<String, String> headers, HttpStatus status) {
		this(null, headers, status);
	}

	/**
	 * Create a new {@code HttpEntity} with the given body, headers, and status code.
	 * @param body the entity body
	 * @param headers the entity headers
	 * @param status the status code
	 */
	public ResponseEntity(Object body, MultiValueMap<String, String> headers, HttpStatus status) {
		super(body, headers);
		Assert.notNull(status, "HttpStatus must not be null");
		this.statusCode = status;
	}

	/**
	 * Create a new {@code HttpEntity} with the given body, headers, and status code.
	 * Just used behind the nested builder API.
	 * @param body the entity body
	 * @param headers the entity headers
	 * @param statusCode the status code (as {@code HttpStatus} or as {@code Integer} value)
	 */
	private ResponseEntity(Object body, MultiValueMap<String, String> headers, Object statusCode) {
		super(body, headers);
		this.statusCode = statusCode;
	}


	/**
	 * Return the HTTP status code of the response.
	 * @return the HTTP status as an HttpStatus enum entry
	 */
	public HttpStatus getStatusCode() {
		if (this.statusCode instanceof HttpStatus) {
			return (HttpStatus) this.statusCode;
		}
		else {
			return HttpStatus.valueOf((Integer) this.statusCode);
		}
	}

	/**
	 * Return the HTTP status code of the response.
	 * @return the HTTP status as an int value
	 */
	public int getStatusCodeValue() {
		if (this.statusCode instanceof HttpStatus) {
			return ((HttpStatus) this.statusCode).value();
		}
		else {
			return (Integer) this.statusCode;
		}
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		ResponseEntity otherEntity = (ResponseEntity) other;
		return ObjectUtils.nullSafeEquals(this.statusCode, otherEntity.statusCode);
	}

	@Override
	public int hashCode() {
		return (super.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.statusCode));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		builder.append(this.statusCode.toString());
		if (this.statusCode instanceof HttpStatus) {
			builder.append(' ');
			builder.append(((HttpStatus) this.statusCode).getReasonPhrase());
		}
		builder.append(',');
		Object body = getBody();
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
