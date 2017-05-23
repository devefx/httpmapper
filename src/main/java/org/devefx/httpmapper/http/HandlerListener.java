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

/**
 * HandlerListener
 * @author Youqian Yue
 * @since 1.0
 */
public interface HandlerListener {

	void onRequest(RequestEntity requestEntity) throws Exception;
	
	void onResponse(RequestEntity requestEntity, ResponseEntity responseEntity) throws Exception;
	
}
