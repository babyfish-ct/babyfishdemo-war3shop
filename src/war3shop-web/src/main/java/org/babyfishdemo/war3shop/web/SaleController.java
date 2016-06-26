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
package org.babyfishdemo.war3shop.web;

import javax.annotation.Resource;

import org.babyfishdemo.war3shop.bll.SaleService;
import org.babyfishdemo.war3shop.bll.model.Sale;
import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/sale")
public class SaleController {
    
    @Resource
    private SaleService saleService;
    
    @RequestMapping("/sales")
    public JsonpModelAndView sales(
            ProductSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "productQueryPath", required = false) String productQueryPath,
            @RequestParam(value = "preferentialQueryPath", required = false) String preferentialQueryPath) {
        Product__[] productQueryPaths = Product__.compile(productQueryPath);
        Preferential__[] preferentialQueryPaths = Preferential__.compile(preferentialQueryPath);
        Page<Sale> page = this.saleService.getSales(
                specification, 
                pageIndex, 
                pageSize, 
                productQueryPaths,
                preferentialQueryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/temporary-order")
    public JsonpModelAndView temporaryOrder(
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Order__[] queryPaths = Order__.compile(queryPath);
        Order order = this.saleService.getTemporaryOrder(queryPaths);
        return new JsonpModelAndView(order);
    }
    
    @RequestMapping("/add-product-into-cart")
    public JsonpModelAndView addProductIntoCart(
            @RequestParam("productId") long productId) {
        this.saleService.addProductIntoCart(productId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/set-product-quantity-of-cart")
    public JsonpModelAndView setProductQuantityOfCart(
            @RequestParam("productId") long productId,
            @RequestParam("quantity") int quantity) {
        this.saleService.setProductQuantityOfCart(productId, quantity);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/remove-product-from-cart")
    public JsonpModelAndView removeProductFromCart(
            @RequestParam("productId") long productId) {
        this.saleService.removeProductFromCart(productId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/create-order")
    public JsonpModelAndView createOrder(
            @RequestParam("address") String address,
            @RequestParam("phone") String phone) {
        this.saleService.createOrder(address, phone);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/assured-orders")
    public JsonpModelAndView assuredOrders(
            OrderSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Order__[] queryPaths = Order__.compile(queryPath);
        Page<Order> page = this.saleService.getAssuredOrders(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/my-orders")
    public JsonpModelAndView myOrders(
            OrderSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Order__[] queryPaths = Order__.compile(queryPath);
        Page<Order> page = this.saleService.getMyOrders(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/my-responsible-orders")
    public JsonpModelAndView myResponsibleOrders(
            OrderSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Order__[] queryPaths = Order__.compile(queryPath);
        Page<Order> page = this.saleService.getMyRepsonsibleOrders(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/assign-deliveryman")
    public JsonpModelAndView assignDeliveryman(
            @RequestParam("orderId") long orderId,
            @RequestParam("deliverymanId") long deliverymanId) {
        this.saleService.assignDeliveryman(orderId, deliverymanId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delivery-order")
    public JsonpModelAndView deliveryOrder(
            @RequestParam("orderId") long orderId) {
        this.saleService.deliveryOrder(orderId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/change-order-address")
    public JsonpModelAndView changeOrderAddress(
            @RequestParam("orderId") long orderId,
            @RequestParam("address") String address,
            @RequestParam("phone") String phone) {
        this.saleService.changeOrderAddress(orderId, address, phone);
        return new JsonpModelAndView(null);
    }
}
