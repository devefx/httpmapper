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

package org.devefx.httpmapper.spring.config;

import java.util.List;

import org.devefx.httpmapper.spring.handler.MappedListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class ListenersBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
		parserContext.pushContainingComponent(compDefinition);
		
		RuntimeBeanReference pathMatcherRef = null;
		if (element.hasAttribute("path-matcher")) {
			pathMatcherRef = new RuntimeBeanReference(element.getAttribute("path-matcher"));
		}
		
		List<Element> listeners = DomUtils.getChildElementsByTagName(element, "bean", "ref", "listener");
		for (Element listener : listeners) {
			RootBeanDefinition mappedListenerDef = new RootBeanDefinition(MappedListener.class);
			mappedListenerDef.setSource(parserContext.extractSource(listener));
			mappedListenerDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			
			ManagedList<String> includePatterns = null;
			ManagedList<String> excludePatterns = null;
			Object listenerBean;
			if ("listener".equals(listener.getLocalName())) {
				includePatterns = getIncludePatterns(listener, "mapping");
				excludePatterns = getIncludePatterns(listener, "exclude-mapping");
				Element beanElem = DomUtils.getChildElementsByTagName(listener, "bean", "ref").get(0);
				listenerBean = parserContext.getDelegate().parsePropertySubElement(beanElem, null);
			} else {
				listenerBean = parserContext.getDelegate().parsePropertySubElement(listener, null);
			}
			mappedListenerDef.getConstructorArgumentValues().addIndexedArgumentValue(0, includePatterns);
			mappedListenerDef.getConstructorArgumentValues().addIndexedArgumentValue(1, excludePatterns);
			mappedListenerDef.getConstructorArgumentValues().addIndexedArgumentValue(2, listenerBean);
			
			if (pathMatcherRef != null) {
				mappedListenerDef.getPropertyValues().add("pathMatcher", pathMatcherRef);
			}
			
			String beanName = parserContext.getReaderContext().registerWithGeneratedName(mappedListenerDef);
			parserContext.registerComponent(new BeanComponentDefinition(mappedListenerDef, beanName));
		}
		
		parserContext.popAndRegisterContainingComponent();
		return null;
	}

	private ManagedList<String> getIncludePatterns(Element listener, String elementName) {
		List<Element> paths = DomUtils.getChildElementsByTagName(listener, elementName);
		ManagedList<String> patterns = new ManagedList<String>(paths.size());
		for (Element path : paths) {
			patterns.add(path.getAttribute("path"));
		}
		return patterns;
	}
}
