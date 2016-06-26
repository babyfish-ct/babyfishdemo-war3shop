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

import org.babyfishdemo.war3shop.entities.AccountManager;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Customer;

import com.alibaba.fastjson.serializer.AfterFilter;

/**
 * @author Tao Chen
 */
public class PolymorphismFilter extends AfterFilter {
    
    public static final PolymorphismFilter INSTANCE = new PolymorphismFilter();
    
    private PolymorphismFilter() {
        
    }

    @Override
    public void writeAfter(Object object) {
        if (object instanceof Customer) {
            this.writeKeyValue("isCustomer", true);
        } else if (object instanceof Administrator) {
            this.writeKeyValue("isAdministrator", true);
        } else if (object instanceof AccountManager) {
            this.writeKeyValue("isAccountManager", true);
        }
    }
}
