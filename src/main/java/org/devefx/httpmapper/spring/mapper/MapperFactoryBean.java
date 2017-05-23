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

package org.devefx.httpmapper.spring.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.devefx.httpmapper.Configuration;
import org.devefx.httpmapper.spring.handler.MappedListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

public class MapperFactoryBean<T> implements FactoryBean<T>, InitializingBean, ApplicationContextAware {

	protected final Log logger = LogFactory.getLog(getClass());
	
	private Class<T> mapperInterface;
	
	private Configuration config;
	
	public MapperFactoryBean() {
		//intentionally empty
	}
	
	public MapperFactoryBean(Class<T> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.config, "Property 'config' is required");
		Assert.notNull(this.mapperInterface, "Property 'mapperInterface' is required");
		try {
			config.addMapper(this.mapperInterface);
		} catch (Exception e) {
			logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		final List<MappedListener> mappedListeners = new ArrayList<MappedListener>();
		mappedListeners.addAll(
				BeanFactoryUtils.beansOfTypeIncludingAncestors(
						applicationContext, MappedListener.class, true, false).values());
		config.setMappedListeners(mappedListeners);
	}
	
	@Override
	public T getObject() throws Exception {
		return config.getMapper(this.mapperInterface);
	}

	@Override
	public Class<?> getObjectType() {
		return this.mapperInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	//------------- mutators --------------

	/**
	 * Sets the mapper interface of the Hrmi mapper
	 *
	 * @param mapperInterface class of the interface
	 */
	public void setMapperInterface(Class<T> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	/**
	 * Return the mapper interface of the Hrmi mapper
	 *
	 * @return class of the interface
	 */
	public Class<T> getMapperInterface() {
		return mapperInterface;
	}
	
	public void setConfig(Configuration config) {
		this.config = config;
	}
	
	public Configuration getConfig() {
		return config;
	}

}
