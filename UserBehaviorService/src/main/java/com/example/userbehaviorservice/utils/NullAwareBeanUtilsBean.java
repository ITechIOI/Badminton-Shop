package com.example.userbehaviorservice.utils;

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;

public class NullAwareBeanUtilsBean extends BeanUtilsBean {
    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (value != null) { // Bỏ qua nếu giá trị là null
            super.copyProperty(dest, name, value);
        }
    }
}

