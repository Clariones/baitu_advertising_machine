package com.skynet.mediaserver.utils;

import java.util.List;
import java.util.Map;

public class ParameterUtils {

	public static boolean getBoolean(Map<String, Object> parameters,String key, boolean defaultValue) {
		Object value = parameters.get(key);
		if (value == null){
			return defaultValue;
		}
		if (value instanceof List){
			List<Object> valueList = (List<Object>) value;
			if (valueList.size() != 1){
				return defaultValue;
			}
			Object valObj = valueList.get(0);
			return getValueAsBoolean(valObj, defaultValue);
		}
		return getValueAsBoolean(value, defaultValue);
	}

	private static boolean getValueAsBoolean(Object valObj, boolean defaultValue) {
		if (valObj == null){
			return defaultValue;
		}
		if (valObj instanceof String){
			String valStr = ((String) valObj).toLowerCase();
			if (valStr.equals("true") || valStr.equals("yes")){
				return true;
			}else if (valStr.equals("false") || valStr.equals("no")){
				return false;
			}else{
				return defaultValue;
			}
		}
		if (valObj instanceof Boolean){
			return ((Boolean) valObj).booleanValue();
		}
		if (valObj instanceof Number){
			return !valObj.equals(0);
		}
		return defaultValue;
	}

	public static String getString(Map<String, Object> parameters,String key, String defaultValue) {
		Object value = parameters.get(key);
		if (value == null){
			return defaultValue;
		}
		if (value instanceof List){
			List<Object> valueList = (List<Object>) value;
			if (valueList.size() != 1){
				return defaultValue;
			}
			Object valObj = valueList.get(0);
			return getValueAsString(valObj, defaultValue);
		}
		return getValueAsString(value, defaultValue);
	}

	private static String getValueAsString(Object valObj, String defaultValue) {
		if (valObj == null){
			return defaultValue;
		}
		return String.valueOf(valObj);
	}
}
