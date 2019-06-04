/**
 * 
 */
package com.icil.elsa.wpg.common.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.icil.elsa.wpg.cat.intercepter.CatRestInterceptor;

/*****************************************************************************
 * <PRE>
 * 
 * Project Name : wpg-tms-service
 * 
 * File Name : RestTemplateConfig.java
 * 
 * Creation Date : 2019年2月26日下午6:26:41
 * 
 * Author : ☋☋☋ ianguo ☋☋☋
 * 
 * Purpose: This class is the configuration for HTTP client.
 * 
 * History :
 * 
 * </PRE>
 ******************************************************************************/
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
    	 RestTemplate restTemplate = new RestTemplate(factory);
         // 保存和传递调用链上下文
         restTemplate.setInterceptors( Collections.singletonList(new CatRestInterceptor()));
         return restTemplate;
    }
 
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      //当发送大量数据时，比如put/post的保存和修改，那么可能内存消耗严重。需要设置false.
        factory.setBufferRequestBody(false);
        factory.setReadTimeout(30000);//30s
        factory.setConnectTimeout(15000);//15s
        return factory;
    }
}