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
package org.babyfishdemo.war3shop.bll.impl.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.babyfishdemo.war3shop.bll.Constants;

/**
 * @author Tao Chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequirePermission {

    /**
     * When it is {@link Constants#ACCOUNT_MANAGER_PERMISSION }, that means requires the AccountManager;
     * When it is {@link Constants#CUSTOMER_PERMISSION }, that means requires the the Customer;
     * otherwise, that means requires the privilege configured in the database.
     */
    String[] value();
    
    boolean forGuest() default false;
}
