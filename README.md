# httpmapper

<a href="http://jq.qq.com/?_wv=1027&k=2JEFdFZ" target="_blank">【Java技术交流群】</a>

## 构建源码

  执行命令前请确保已经安装`git`和`maven`命令

```bash
git clone https://github.com/devefx/httpmapper.git

cd httpmapper

mvn clean install -Dmaven.test.skip
```
  
  在`Maven`构建的项目中添加下面的配置
  
```xml
<dependency>
  <groupId>org.devefx</groupId>
  <artifactId>httpmapper</artifactId>
  <version>beta-1.0.0</version>
</dependency>
```

## 示例

Spring Config: `spring-httpmapper.xml`

```xml
<bean id="restTemplate" class="org.springframework.web.client.RestTemplate">
  <!-- your config -->
  <!-- config ssl -->
</bean>

<bean id="configuration" class="org.devefx.httpmapper.Configuration">
  <property name="globalBaseUrl" value="https://api.weixin.qq.com/cgi-bin/"/>
  <property name="restTemplate" ref="restTemplate"/>
</bean>

<bean class="org.devefx.httpmapper.spring.mapper.MapperScannerConfigurer">
  <property name="basePackage" value="org.foo.httpservice"/>
  <property name="configBeanName" value="configuration"/>
</bean>
```

HttpService:

```java
package org.foo.httpservice;

import org.devefx.httpmapper.annotate.Bean;
import org.devefx.httpmapper.annotate.Method;
import org.devefx.httpmapper.annotate.Param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Bean
public interface WechatService {

  // 获取access_token
  @Method(value="token?grant_type=client_credential&appid={appid}&secret={appSecret}")
  AccessToken token(String appid, String appSecret);
  
  // 获取api_ticket
  @Method(value="ticket/getticket")
  Ticket ticket(@Param("access_token") String accessToken, @Param("type") String type);


  // POJOs
  
  public static class AccessToken {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private int expires;
    // getter & setter
    ... 
  }

  public static class Ticket {
    private String ticket;
    @JsonProperty("expires_in")
    private int expires;
    // getter & setter
    ... 
  }
  
}

```

Run:

```java

package hello;

import org.foo.httpservice.WechatService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HttpMapperTest {
  
  public static void main(String[] args) throws Exception {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-httpmapper.xml");
    WechatService wechatService = applicationContext.getBean(WechatService.class);
    AccessToken token = wechatService.token("your appid", "your appsecret");
    // ...
  }
  
}

```

## 监听器

可以对请求实体和响应实体进行需改

Spring Config:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:httpmapper="http://www.devefx.org/schema/httpmapper"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-4.1.xsd
        http://www.devefx.org/schema/httpmapper
        http://www.devefx.org/schema/httpmapper/spring-httpmapper-1.0.xsd">
        
  <httpmapper:listeners>
    <httpmapper:listener>
      <httpmapper:mapping path="https://api.weixin.qq.com/cgi-bin/**"/>
      <bean class="com.foo.interceptors.WechatListener"/>
    </httpmapper:listener>
  </httpmapper:listeners>
    
</beans>
```

```java
package com.foo.interceptors;

import org.devefx.httpmapper.http.HandlerListener;
import org.devefx.httpmapper.http.RequestEntity;
import org.devefx.httpmapper.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WechatListener implements HandlerListener {

  private ObjectMapper objectWrapper = new ObjectMapper();
  
  @Override
  public void onRequest(RequestEntity requestEntity) throws Exception {
    
  }
  
  @Override
  public void onResponse(RequestEntity requestEntity,
    ResponseEntity responseEntity) throws Exception {
    if (responseEntity.hasBody()) {
      JsonNode node = objectWrapper.convertValue(responseEntity.getBody(), JsonNode.class);
      if (node.has("errcode") || node.has("errmsg")) {
        int errcode = node.get("errcode").intValue();
        String errmsg = String.valueOf(node.get("errmsg"));
        throw new RuntimeException("[" + errcode + "] ==> " + errmsg);
      }
    }
  }
  
}
```
