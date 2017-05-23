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

import org.devefx.httpmapper.annotate.Bean;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

	private String basePackage;
	
	private String configBeanName;

	private Class<? extends Annotation> annotationClass = Bean.class;

	private Class<?> markerInterface;

	private ApplicationContext applicationContext;

	private String beanName;
	
	private boolean processPropertyPlaceHolders;

	private BeanNameGenerator nameGenerator;
	
	/**
	 * This property lets you set the base package for your mapper interface files.
	 * <p>
	 * You can set more than one package by using a semicolon or comma as a separator.
	 * <p>
	 * Mappers will be searched for recursively starting in the specified package(s).
	 *
	 * @param basePackage base package name
	 */
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	/**
	 * This property specifies the annotation that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also have the
	 * specified annotation.
	 * <p>
	 * Note this can be combined with markerInterface.
	 *
	 * @param annotationClass annotation class
	 */
	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	/**
	 * This property specifies the parent that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also have the
	 * specified interface class as a parent.
	 * <p>
	 * Note this can be combined with annotationClass.
	 *
	 * @param superClass parent class
	 */
	public void setMarkerInterface(Class<?> superClass) {
		this.markerInterface = superClass;
	}
	
	public void setConfigBeanName(String configBeanName) {
		this.configBeanName = configBeanName;
	}
	
	public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
		this.processPropertyPlaceHolders = processPropertyPlaceHolders;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	public BeanNameGenerator getNameGenerator() {
		return nameGenerator;
	}
	
	public void setNameGenerator(BeanNameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.basePackage, "Property 'basePackage' is required");
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// left intentionally blank
	}

	@Override
	public void postProcessBeanDefinitionRegistry(
			BeanDefinitionRegistry registry) throws BeansException {
		if (processPropertyPlaceHolders) {
			processPropertyPlaceHolders();
		}
		
		ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
		scanner.setAnnotationClass(this.annotationClass);
		scanner.setMarkerInterface(this.markerInterface);
		scanner.setConfigBeanName(this.configBeanName);
		scanner.setResourceLoader(this.applicationContext);
		scanner.setBeanNameGenerator(this.nameGenerator);
		scanner.registerFilters();
		scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
	}

	/**
	 * BeanDefinitionRegistries are called early in application startup, before
	 * BeanFactoryPostProcessors. This means that PropertyResourceConfigurers will not have been
	 * loaded and any property substitution of this class' properties will fail. To avoid this, find
	 * any PropertyResourceConfigurers defined in the context and run them on this class' bean
	 * definition. Then update the values.
	 */
	private void processPropertyPlaceHolders() {
		Map<String, PropertyResourceConfigurer> prcs = applicationContext.getBeansOfType(PropertyResourceConfigurer.class);
		
		if (!prcs.isEmpty() && applicationContext instanceof GenericApplicationContext) {
			BeanDefinition mapperScannerBean = ((GenericApplicationContext) applicationContext)
			          .getBeanFactory().getBeanDefinition(beanName);
			
			// PropertyResourceConfigurer does not expose any methods to explicitly perform
			// property placeholder substitution. Instead, create a BeanFactory that just
			// contains this mapper scanner and post process the factory.
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			factory.registerBeanDefinition(beanName, mapperScannerBean);
			
			for (PropertyResourceConfigurer prc : prcs.values()) {
				prc.postProcessBeanFactory(factory);
			}
			
			PropertyValues values = mapperScannerBean.getPropertyValues();
			
			this.basePackage = updatePropertyValue("basePackage", values);
			this.configBeanName = updatePropertyValue("configBeanName", values);
		}
	}

	private String updatePropertyValue(String propertyName, PropertyValues values) {
		PropertyValue property = values.getPropertyValue(propertyName);
		
		if (property == null) {
			return null;
		}
		
		Object value = property.getValue();
		if (value == null) {
			return null;
		} else if (value instanceof String) {
			return value.toString();
		} else if (value instanceof TypedStringValue) {
			return ((TypedStringValue) value).getValue();
		}
		return null;
	}
	
}
