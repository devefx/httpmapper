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

package org.devefx.httpmapper;

import java.util.List;

import org.devefx.httpmapper.binding.MapperRegistry;
import org.devefx.httpmapper.spring.handler.HandlerExecutionChain;
import org.devefx.httpmapper.spring.handler.MappedListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration
 * @author Youqian Yue
 * @since 1.0
 */
public class Configuration {
	
	protected boolean useActualParamName = true;
	protected String globalBaseUrl;
	protected RestTemplate restTemplate;
	
	protected List<MappedListener> mappedListeners;
	protected PathMatcher pathMatcher = new AntPathMatcher();
	protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
	
	public boolean isUseActualParamName() {
		return useActualParamName;
	}
	
	public void setUseActualParamName(boolean useActualParamName) {
		this.useActualParamName = useActualParamName;
	}
	
	public String getGlobalBaseUrl() {
		return globalBaseUrl;
	}
	
	public void setGlobalBaseUrl(String globalBaseUrl) {
		this.globalBaseUrl = globalBaseUrl;
	}
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	public MapperRegistry getMapperRegistry() {
		return mapperRegistry;
	}
	
	public <T> void addMapper(Class<T> type) {
		mapperRegistry.addMapper(type);
	}

	public <T> T getMapper(Class<T> type) {
		return mapperRegistry.getMapper(type);
	}
	
	public boolean hasMapper(Class<?> type) {
		return mapperRegistry.hasMapper(type);
	}
	
	public void setMappedListeners(List<MappedListener> mappedListeners) {
		this.mappedListeners = mappedListeners;
	}
	
	protected final MappedListener[] getMappedListeners() {
		int count = this.mappedListeners.size();
		return (count > 0 ? this.mappedListeners.toArray(new MappedListener[count]) : null);
	}
	
	public HandlerExecutionChain getHandlerExecutionChain(String lookupPath) {
		HandlerExecutionChain chain = new HandlerExecutionChain();
		for (MappedListener mappedListener : this.mappedListeners) {
			if (mappedListener.matches(lookupPath, this.pathMatcher)) {
				chain.addListener(mappedListener.getListener());
			}
		}
		return chain;
	}
}
