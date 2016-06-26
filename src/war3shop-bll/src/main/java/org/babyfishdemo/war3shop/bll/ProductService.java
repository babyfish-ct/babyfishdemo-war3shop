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

import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem__;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.specification.PreferentialSpecification;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;

/**
 * @author Tao Chen
 */
public interface ProductService {
    
    Manufacturer getManufacturerById(long id, Manufacturer__ ... queryPaths);

    Page<Manufacturer> getManufacturers(
            ManufacturerSpecification specification,
            int pageIndex,
            int pageSize,
            Manufacturer__ ... queryPaths);
    
    void submitManufacturer(Manufacturer manufacturer);
    
    void deleteManufacturer(long id);
    
    Product getProductById(long id, Product__ ... queryPaths);
    
    Page<Product> getProducts(
            ProductSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Product__ ... queryPaths);
    
    void submitProduct(Product product);
    
    void deleteProduct(long id);
    
    Preferential getPreferentialById(long id, Preferential__ ... queryPaths);
    
    Page<Preferential> getPreferentials(
            PreferentialSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Preferential__ ... queryPaths);
    
    void submitPreferential(Preferential preferential);
    
    void validatePreferential(Preferential preferential);
    
    Page<PreferentialItem> getPreferentialItemsByParent(
            long preferentialId,
            int pageIndex, 
            int pageSize, 
            PreferentialItem__... queryPaths);
    
    void deletePreferential(long id);
}
