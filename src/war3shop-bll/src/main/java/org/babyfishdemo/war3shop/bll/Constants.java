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
package org.babyfishdemo.war3shop.bll;

/**
 * @author Tao Chen
 */
public class Constants {

    /**
     * The special permission of AccountManager,
     * it is NOT a generic privilege that can be found in the database!
     */
    public static final String ACCOUNT_MANAGER_PERMISSION = "{special-privildege}:account-manager";
    
    /**
     * The special permission of Customer
     * it is NOT a generic privilege that can be found in the database!
     */
    public static final String CUSTOMER_PERMISSION = "{special-privildege}:customer";
    
    // This permission is generic privilege that can be found in the database.  
    public static final String PURCHASE_PRODUCTS = "purchase-products";
    
    // This permission is generic privilege that can be found in the database.
    public static final String DELIVERY_ORDERS = "delivery-orders";

    // This permission is generic privilege that can be found in the database.
    public static final String MANAGE_MANUFACTURERS = "manage-manufacturers";

    // This permission is generic privilege that can be found in the database.
    public static final String MANAGE_PRODUCTS = "manage-products";

    // This permission is generic privilege that can be found in the database.
    public static final String MANAGE_PREFERENTIALS = "manage-preferentials";

    // This permission is generic privilege that can be found in the database.
    public static final String MANAGE_ORDERS = "manage-orders";

    public static final String UPLOADED_KEY_IMAGE = "image";
    
    protected Constants() {
        throw new UnsupportedOperationException();
    }
}
