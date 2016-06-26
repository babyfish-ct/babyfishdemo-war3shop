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

import java.math.BigDecimal;
import java.util.Collection;

import javax.annotation.Resource;

import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.war3shop.bll.ProductService;
import org.babyfishdemo.war3shop.bll.UploadService;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem__;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.ProductType;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Race;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.PreferentialSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.babyfishdemo.war3shop.web.json.JPAObjectModelJSONParser;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;
    
    @Resource
    private UploadService uploadService;
    
    @RequestMapping("/manufacturer")
    public JsonpModelAndView manufacturer(
            @RequestParam("id") long id,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Manufacturer__[] queryPaths = Manufacturer__.compile(queryPath);
        Manufacturer manufacturer = this.productService.getManufacturerById(id, queryPaths);
        return new JsonpModelAndView(manufacturer);
    }
    
    @RequestMapping("/manufacturers")
    public JsonpModelAndView manufacturers(
            ManufacturerSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Manufacturer__[] queryPaths = Manufacturer__.compile(queryPath);
        Page<Manufacturer> page = this.productService.getManufacturers(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/submit-manufacturer")
    public JsonpModelAndView sumbitManufacturer(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "version", defaultValue = "0") int version,
            @RequestParam("name") String name,
            @RequestParam("race") Race race,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("description") String description,
            @RequestParam("purchaserIds") Collection<Long> purchaserIds) {
        
        Manufacturer manufacturer = new Manufacturer();
        JPAEntities.disableAll(manufacturer);
        
        manufacturer.setId(id);
        manufacturer.setVersion(version);
        manufacturer.setName(name);
        manufacturer.setRace(race);
        manufacturer.setEmail(email);
        manufacturer.setPhone(phone);
        manufacturer.setDescription(description);
    
        manufacturer.setPurchasers(
                JPAEntities.createFakeEntities(Administrator.class, purchaserIds)
        );
        
        TemporaryUploadedFile temporaryUploadedFile = 
                this.uploadService.getAndDeleteUploadedImage(
                        TemporaryUploadedFile__.begin().content().end()
                );
        if (temporaryUploadedFile != null) {
            manufacturer.setImageMimeType(temporaryUploadedFile.getMimeType());
            manufacturer.setImage(temporaryUploadedFile.getContent());
        }
        
        this.productService.submitManufacturer(manufacturer);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delete-manufacturer")
    public JsonpModelAndView deleteManufacturer(@RequestParam("id") long id) {
        this.productService.deleteManufacturer(id);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/product")
    public JsonpModelAndView product(
            @RequestParam("id") long id,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Product__[] queryPaths = Product__.compile(queryPath);
        Product product = this.productService.getProductById(id, queryPaths);
        return new JsonpModelAndView(product);
    }
    
    @RequestMapping("/products")
    public JsonpModelAndView products(
            ProductSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Product__[] queryPaths = Product__.compile(queryPath);
        Page<Product> page = this.productService.getProducts(specification, pageIndex, pageSize, queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/submit-product")
    public JsonpModelAndView submitProduct(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "version", defaultValue = "0") int version,
            @RequestParam("name") String name,
            @RequestParam("active") boolean active,
            @RequestParam("type") ProductType type,
            @RequestParam("race") Race race,
            @RequestParam("price") BigDecimal price,
            @RequestParam("description") String description,
            @RequestParam("manufacturerIds") Collection<Long> manufacturerIds,
            @RequestParam("purchaserIds") Collection<Long> purchaserIds) {
        
        Product product = new Product();
        JPAEntities.disableAll(product);
        
        product.setId(id);
        product.setVersion(version);
        product.setName(name);
        product.setActive(active);
        product.setType(type);
        product.setRace(race);
        product.setPrice(price);
        product.setDescription(description);
        product.setManufacturers(JPAEntities.createFakeEntities(Manufacturer.class, manufacturerIds));
        product.setPurchasers(JPAEntities.createFakeEntities(Administrator.class, purchaserIds));
        
        product.setManufacturers(
                JPAEntities.createFakeEntities(Manufacturer.class, manufacturerIds)
        );
        product.setPurchasers(
                JPAEntities.createFakeEntities(Administrator.class, purchaserIds)
        );
        
        TemporaryUploadedFile temporaryUploadedFile = 
                this.uploadService.getAndDeleteUploadedImage(
                        TemporaryUploadedFile__.begin().content().end()
                );
        if (temporaryUploadedFile != null) {
            product.setImageMimeType(temporaryUploadedFile.getMimeType());
            product.setImage(temporaryUploadedFile.getContent());
        }
        
        this.productService.submitProduct(product);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delete-product")
    public JsonpModelAndView deleteProduct(@RequestParam("id") long id) {
        this.productService.deleteProduct(id);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/preferential")
    public JsonpModelAndView preferential(
            @RequestParam("id") long id,
            @RequestParam("queryPath") String queryPath) {
        Preferential__[] queryPaths = Preferential__.compile(queryPath);
        Preferential preferential = this.productService.getPreferentialById(id, queryPaths);
        return new JsonpModelAndView(preferential);
    }
    
    @RequestMapping("/preferentials")
    public JsonpModelAndView preferentials(
            PreferentialSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Preferential__[] queryPaths = Preferential__.compile(queryPath);
        Page<Preferential> preferentials = this.productService.getPreferentials(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(preferentials);
    }
    
    @RequestMapping("/submit-preferential")
    public JsonpModelAndView submitPreferential(@RequestParam("json") String json) {
        Preferential preferential = JPAObjectModelJSONParser.parseObject(json, Preferential.class);
        this.productService.submitPreferential(preferential);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/preferential-items")
    public JsonpModelAndView preferentials(
            @RequestParam("preferentialId") long preferentialId,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        PreferentialItem__[] queryPaths = PreferentialItem__.compile(queryPath);
        Page<PreferentialItem> preferentials = this.productService.getPreferentialItemsByParent(
                preferentialId, 
                pageIndex, 
                pageSize, 
                queryPaths);
        return new JsonpModelAndView(preferentials);
    }
    
    @RequestMapping("/delete-preferential")
    public JsonpModelAndView deletePreferential(long id) {
        this.productService.deletePreferential(id);
        return new JsonpModelAndView(null);
    }
}
