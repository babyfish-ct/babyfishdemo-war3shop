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
package org.babyfishdemo.war3shop.dal;

import java.util.Date;

import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;

/**
 * @author Tao Chen
 */
public interface OrderRepository extends AbstractRepository<Order, Long> {
    
    Order getOrderById(long id, Order__ ... queryPaths);

    Order getNewestTemporaryOrderByCustomerId(long customerId, Order__ ... queryPaths);
    
    Page<Order> getAssuredOrders(OrderSpecification specification, int pageIndex, int pageSize, Order__ ... queryPaths);
    
    void removeTemporaryOrderByGcThreshold(Date gcThreshold);
}
