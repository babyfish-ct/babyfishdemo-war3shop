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

import org.babyfishdemo.war3shop.bll.model.Sale;
import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;

/**
 * @author Tao Chen
 */
public interface SaleService {

    Page<Sale> getSales(
            ProductSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Product__[] productQueryPaths,
            Preferential__[] preferentialQueryPaths);
    
    Order getTemporaryOrder(Order__ ... queryPaths);
    
    void addProductIntoCart(long productId);
    
    void setProductQuantityOfCart(long productId, int quantity);
    
    void removeProductFromCart(long productId);

    void createOrder(String address, String phone);
    
    Page<Order> getAssuredOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__ ... queryPaths);
    
    Page<Order> getMyOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__ ... queryPaths);
    
    Page<Order> getMyRepsonsibleOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__ ... queryPaths);

    void assignDeliveryman(long orderId, long deliverymanId);

    void deliveryOrder(long orderId);

    void changeOrderAddress(long orderId, String address, String phone);
}
