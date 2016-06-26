/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfishdemo.war3shop.web.json;

import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;

/**
 * @author Tao Chen
 */
public class HibernateLazyScalarFilter implements PropertyPreFilter {
    
    public static final HibernateLazyScalarFilter INSTANCE = 
            new HibernateLazyScalarFilter();
    
    private static final String FIELD_HANDLER = "fieldHandler";
    
    private HibernateLazyScalarFilter() {}
    
    @Override
    public boolean apply(JSONSerializer serializer, Object object, String name) {
        if (object instanceof FieldHandled) {
            if (FIELD_HANDLER.equals(name)) {
                return false;
            }
            FieldHandled fieldHandled = (FieldHandled)object;
            FieldHandler fieldHandler = fieldHandled.getFieldHandler();
            if (fieldHandler != null) {
                FieldInterceptor fieldInterceptor = (FieldInterceptor)fieldHandler;
                if (!fieldInterceptor.isInitialized(name)) {
                    return false;
                }
            }
        }
        return true;
    }
}
