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

package org.devefx.httpmapper.spring.handler;

import org.devefx.httpmapper.http.HandlerListener;
import org.springframework.util.PathMatcher;

public final class MappedListener {
	
	private final String[] includePatterns;

	private final String[] excludePatterns;
	
	private final HandlerListener listener;
	
	private PathMatcher pathMatcher;
	
	/**
	 * Create a new MappedListener instance.
	 * @param includePatterns the path patterns to map with a {@code null} value matching to all paths
	 * @param listener the HandlerListener instance to map to the given patterns
	 */
	public MappedListener(String[] includePatterns, HandlerListener listener) {
		this(includePatterns, null, listener);
	}

	/**
	 * Create a new MappedListener instance.
	 * @param includePatterns the path patterns to map with a {@code null} value matching to all paths
	 * @param excludePatterns the path patterns to exclude
	 * @param listener the HandlerListener instance to map to the given patterns
	 */
	public MappedListener(String[] includePatterns, String[] excludePatterns, HandlerListener listener) {
		this.includePatterns = includePatterns;
		this.excludePatterns = excludePatterns;
		this.listener = listener;
	}
	
	/**
	 * Configure a PathMatcher to use with this MappedListener instead of the
	 * one passed by default to the {@link #matches(String, org.springframework.util.PathMatcher)}
	 * method. This is an advanced property that is only required when using custom
	 * PathMatcher implementations that support mapping metadata other than the
	 * Ant-style path patterns supported by default.
	 *
	 * @param pathMatcher the path matcher to use
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	/**
	 * The configured PathMatcher, or {@code null}.
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}
	
	/**
	 * The path into the application the listener is mapped to.
	 */
	public String[] getPathPatterns() {
		return this.includePatterns;
	}
	
	/**
	 * The actual Listener reference.
	 */
	public HandlerListener getListener() {
		return this.listener;
	}
	
	/**
	 * Returns {@code true} if the listener applies to the given request path.
	 * @param lookupPath the current request path
	 * @param pathMatcher a path matcher for path pattern matching
	 */
	public boolean matches(String lookupPath, PathMatcher pathMatcher) {
		PathMatcher pathMatcherToUse = (this.pathMatcher != null) ? this.pathMatcher : pathMatcher;
		if (this.excludePatterns != null) {
			for (String pattern : this.excludePatterns) {
				if (pathMatcherToUse.match(pattern, lookupPath)) {
					return false;
				}
			}
		}
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (String pattern : this.includePatterns) {
				if (pathMatcherToUse.match(pattern, lookupPath)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
