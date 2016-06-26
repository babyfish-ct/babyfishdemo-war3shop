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

import java.util.List;

import javax.annotation.Resource;

import org.babyfishdemo.war3shop.bll.PurchasingService;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Purchasing;
import org.babyfishdemo.war3shop.entities.PurchasingItem;
import org.babyfishdemo.war3shop.entities.PurchasingItem__;
import org.babyfishdemo.war3shop.entities.Purchasing__;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.babyfishdemo.war3shop.entities.specification.PurchasingSpecification;
import org.babyfishdemo.war3shop.web.json.JPAObjectModelJSONParser;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/purchasing")
public class PurchasingController {
    
    @Resource
    private PurchasingService purchasingService;
    
    @RequestMapping("/my-purchased-products")
    public JsonpModelAndView myPurchasedProducts(
            ProductSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Product__[] queryPaths = Product__.compile(queryPath);
        Page<Product> page = this.purchasingService.getMyPurchasedProducts(specification, pageIndex, pageSize, queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/my-purchased-manufacturers")
    public JsonpModelAndView myPurchasedManufacturers(
            ManufacturerSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Manufacturer__[] queryPaths = Manufacturer__.compile(queryPath);
        Page<Manufacturer> page = this.purchasingService.getMyPurchasedManufacturers(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/my-purchasings")
    public JsonpModelAndView myPurchasings(
            PurchasingSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Purchasing__[] queryPaths = Purchasing__.compile(queryPath);
        Page<Purchasing> page = this.purchasingService.getMyPurchasings(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/purchasing-items")
    public JsonpModelAndView purchasingItems(
            @RequestParam("purchasingId") long purchasingId,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        PurchasingItem__[] queryPaths = PurchasingItem__.compile(queryPath);
        Page<PurchasingItem> page = this.purchasingService.getPurchasingItems(
                purchasingId, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/create-purchasing")
    public JsonpModelAndView createPurchasing(@RequestParam("json") String json) {
        List<PurchasingItem> purchasingItems = JPAObjectModelJSONParser.parseArray(json, PurchasingItem.class);
        this.purchasingService.createPurchasing(purchasingItems);
        return new JsonpModelAndView(null);
    }
}
