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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.devefx.httpmapper.http.HandlerListener;
import org.devefx.httpmapper.http.RequestEntity;
import org.devefx.httpmapper.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

public class HandlerExecutionChain {
	
	private HandlerListener[] listeners;
	
	private List<HandlerListener> listenerList;
	
	public void addListener(HandlerListener listener) {
		initListenerList().add(listener);
	}
	
	public void addListeners(HandlerListener... listeners) {
		if (!ObjectUtils.isEmpty(listeners)) {
			initListenerList().addAll(Arrays.asList(listeners));
		}
	}
	
	private List<HandlerListener> initListenerList() {
		if (this.listenerList == null) {
			this.listenerList = new ArrayList<HandlerListener>();
			if (this.listeners != null) {
				// An listener array specified through the constructor
				this.listenerList.addAll(Arrays.asList(this.listeners));
			}
		}
		this.listeners = null;
		return this.listenerList;
	}
	
	/**
	 * Return the array of listeners to apply (in the given order).
	 * @return the array of HandlerListeners instances (may be {@code null})
	 */
	public HandlerListener[] getListeners() {
		if (this.listeners == null && this.listenerList != null) {
			this.listeners = this.listenerList.toArray(new HandlerListener[this.listenerList.size()]);
		}
		return this.listeners;
	}
	
	public void onRequest(RequestEntity requestEntity) throws Exception {
		HandlerListener[] listeners = getListeners();
		if (!ObjectUtils.isEmpty(listeners)) {
			for (HandlerListener listener  : listeners) {
				listener.onRequest(requestEntity);
			}
		}
	}
	
	public void onResponse(RequestEntity requestEntity, ResponseEntity responseEntity) throws Exception {
		HandlerListener[] listeners = getListeners();
		if (!ObjectUtils.isEmpty(listeners)) {
			for (int i = listeners.length - 1; i >= 0; i--) {
				HandlerListener listener = listeners[i];
				listener.onResponse(requestEntity, responseEntity);
			}
		}
	}
	
}
