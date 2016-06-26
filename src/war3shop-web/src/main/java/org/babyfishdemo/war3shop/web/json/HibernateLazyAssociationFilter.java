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

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * @author Tao Chen
 */
public class HibernateLazyAssociationFilter implements PropertyFilter {
    
    public static final HibernateLazyAssociationFilter INSTANCE =
            new HibernateLazyAssociationFilter();
    
    private HibernateLazyAssociationFilter() {
        
    }

    @Override
    public boolean apply(Object object, String name, Object value) {
        if (value instanceof HibernateProxy) {
            HibernateProxy hibernateProxy = (HibernateProxy)value;
            if (hibernateProxy.getHibernateLazyInitializer().isUninitialized()) {
                return false;
            }
        } else if (value instanceof PersistentCollection) {
            PersistentCollection persistentCollection = (PersistentCollection)value;
            if (!persistentCollection.wasInitialized()) {
                return false;
            }
        }
        return true;
    }
}
