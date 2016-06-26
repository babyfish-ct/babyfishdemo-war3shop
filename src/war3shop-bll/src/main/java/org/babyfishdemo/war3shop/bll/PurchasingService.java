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

import java.util.Collection;

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

/**
 * @author Tao Chen
 */
public interface PurchasingService {

    Page<Product> getMyPurchasedProducts(
            ProductSpecification specification,
            int pageIndex, 
            int pageSize, 
            Product__... queryPaths);
    
    Page<Manufacturer> getMyPurchasedManufacturers(
            ManufacturerSpecification specification,
            int pageIndex,
            int pageSize,
            Manufacturer__ ... queryPaths);
    
    Page<Purchasing> getMyPurchasings(
            PurchasingSpecification specification,
            int pageIndex,
            int pageSize,
            Purchasing__ ... queryPaths);
    
    Page<PurchasingItem> getPurchasingItems(
            long purchasingId, 
            int pageIndex,
            int pageSize,
            PurchasingItem__ ... queryPaths);
    
    void createPurchasing(Collection<PurchasingItem> purchasingItems);
}
