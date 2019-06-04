/**
 * 
 */
package com.icil.elsa.wpg.cat.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Cat;

/*****************************************************************************
 * <PRE>
 * 
 * Project Name : icil-customer-service
 * 
 * File Name : CatContextImpl.java
 * 
 * Creation Date : 2019年6月3日下午2:01:57
 * 
 * Author : ☋☋☋ ianguo ☋☋☋
 * 
 * Purpose: 
 * 
 * History :
 * 
 * </PRE>
 ******************************************************************************/
/**
 * Cat.context接口实现类，用于context调用链传递，相关方法Cat.logRemoteCall()和Cat.logRemoteServer()
 */
public class CatContextImpl implements Cat.Context,Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, String> properties = new HashMap<>(16);

    @Override
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }
}