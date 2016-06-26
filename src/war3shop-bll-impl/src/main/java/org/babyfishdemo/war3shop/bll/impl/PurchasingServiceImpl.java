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
package org.babyfishdemo.war3shop.bll.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Strings;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfish.model.jpa.path.SimpleOrderPath;
import org.babyfish.model.jpa.path.TypedQueryPaths;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.PurchasingService;
import org.babyfishdemo.war3shop.bll.impl.aop.RequirePermission;
import org.babyfishdemo.war3shop.dal.InventoryRepository;
import org.babyfishdemo.war3shop.dal.ManufacturerRepository;
import org.babyfishdemo.war3shop.dal.ProductRepository;
import org.babyfishdemo.war3shop.dal.PurchasingItemRepository;
import org.babyfishdemo.war3shop.dal.PurchasingRepository;
import org.babyfishdemo.war3shop.entities.Inventory;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Purchasing;
import org.babyfishdemo.war3shop.entities.PurchasingItem;
import org.babyfishdemo.war3shop.entities.PurchasingItem_;
import org.babyfishdemo.war3shop.entities.PurchasingItem__;
import org.babyfishdemo.war3shop.entities.Purchasing__;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.babyfishdemo.war3shop.entities.specification.PurchasingSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
public class PurchasingServiceImpl implements PurchasingService {

    @Resource
    private PurchasingRepository purchasingRepository;
    
    @Resource
    private InventoryRepository inventoryRepository;
    
    @Resource
    private ProductRepository productRepository;
    
    @Resource
    private ManufacturerRepository manufacturerRepository;
    
    @Resource
    private PurchasingItemRepository purchasingItemRepository;
    
    @Resource
    private AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    @RequirePermission(Constants.PURCHASE_PRODUCTS)
    @Override
    public Page<Product> getMyPurchasedProducts(
            ProductSpecification specification,
            int pageIndex, 
            int pageSize, 
            Product__... queryPaths) {
        if (specification != null) {
            Arguments.mustBeNull(
                    "specification.getIncludedPurchaserIds()", 
                    specification.getIncludedPurchaserIds());
            Arguments.mustBeNull(
                    "specification.getExcludedPurchaserIds()", 
                    specification.getExcludedPurchaserIds());
        } else {
            specification = new ProductSpecification();
        }
        specification.setIncludedPurchaserIds(
                MACollections.wrap(this.authorizationService.getCurrentAdministrator().getId())
        );
        return this.productRepository.getProducts(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }
    
    @Transactional(readOnly = true)
    @RequirePermission(Constants.PURCHASE_PRODUCTS)
    @Override
    public Page<Manufacturer> getMyPurchasedManufacturers(
            ManufacturerSpecification specification,
            int pageIndex,
            int pageSize,
            Manufacturer__ ... queryPaths) {
        if (specification != null) {
            Arguments.mustBeNull(
                    "specification.getIncludedPurchaserIds()", 
                    specification.getIncludedPurchaserIds());
            Arguments.mustBeNull(
                    "specification.getExcludedPurchaserIds()", 
                    specification.getExcludedPurchaserIds());
        } else {
            specification = new ManufacturerSpecification();
        }
        specification.setIncludedPurchaserIds(
                MACollections.wrap(this.authorizationService.getCurrentAdministrator().getId())
        );
        return this.manufacturerRepository.getManufacturers(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }

    @Transactional(readOnly = true)
    @RequirePermission(Constants.PURCHASE_PRODUCTS)
    @Override
    public Page<Purchasing> getMyPurchasings(
            PurchasingSpecification specification, 
            int pageIndex, 
            int pageSize,
            Purchasing__... queryPaths) {
        if (specification != null) {
            Arguments.mustBeNull(
                    "specification.getIncludedPurchaserIds()", 
                    specification.getIncludedPurchaserIds());
            Arguments.mustBeNull(
                    "specification.getExcludedPurchaserIds()", 
                    specification.getExcludedPurchaserIds());
        } else {
            specification = new PurchasingSpecification();
        }
        specification.setIncludedPurchaserIds(
                MACollections.wrap(this.authorizationService.getCurrentAdministrator().getId())
        );
        boolean addDefaultSimpleOrderPath = true;
        for (Purchasing__ queryPath : queryPaths) {
            if (queryPath instanceof SimpleOrderPath) {
                addDefaultSimpleOrderPath = false;
                break;
            }
        }
        if (addDefaultSimpleOrderPath) {
            queryPaths = TypedQueryPaths.combine(
                    queryPaths, 
                    Purchasing__.preOrderBy().creationTime().desc()
            );
        }
        return this.purchasingRepository.getPurchasings(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }
    
    @Transactional(readOnly = true)
    @RequirePermission(Constants.PURCHASE_PRODUCTS)
    @Override
    public Page<PurchasingItem> getPurchasingItems(
            long purchasingId, 
            int pageIndex,
            int pageSize,
            PurchasingItem__ ... queryPaths) {
        return this.purchasingItemRepository.getPurchasingItemsByPurchasingId(
                purchasingId, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }

    @Transactional
    @RequirePermission(Constants.PURCHASE_PRODUCTS)
    @Override
    public void createPurchasing(Collection<PurchasingItem> purchasingItems) {
        Arguments.mustNotBeNull("purchasingItems", purchasingItems);
        Arguments.mustNotBeEmpty("purchasingItems", purchasingItems);
        
        Set<Long> productIds = new HashSet<>((purchasingItems.size() * 4 + 2) / 3);
        BigDecimal totalPurchasedPrice = new BigDecimal(0);
        for (PurchasingItem purchasingItem : purchasingItems) {
            JPAEntities.validateMaxEnabledRange(
                    purchasingItem, 
                    JPAEntities.required(JPAEntities.notNull(PurchasingItem_.product)),
                    JPAEntities.required(JPAEntities.notNull(PurchasingItem_.quantity)),
                    JPAEntities.required(PurchasingItem_.purchasedUnitPrice));
            if (purchasingItem.getId() != null) {
                throw new IllegalArgumentException("The id of purshasing item must be null");
            }
            if (purchasingItem.getProduct() == null) {
                throw new IllegalArgumentException("The product of purchasing item must not be null");
            }
            JPAEntities.validateMaxEnabledRange(purchasingItem.getProduct());
            if (purchasingItem.getQuantity() < 1) {
                throw new IllegalArgumentException("The quantity of purchasing item must >= 0");
            }
            if (!productIds.add(purchasingItem.getProduct().getId())) {
                throw new IllegalArgumentException(
                        "The Purchasing Items contain duplicated productId: " + 
                        purchasingItem.getProduct().getId()
                );
            }
            totalPurchasedPrice = totalPurchasedPrice.add(
                    purchasingItem.getPurchasedUnitPrice().multiply(
                            new BigDecimal(purchasingItem.getQuantity())
                    )
            );
        }
        
        List<Product> productList = this.productRepository.getProductsByIds(
                productIds, Product__.begin().inventory().end());
        Map<Long, Product> productMap = new HashMap<>((productList.size() * 4 + 2) / 3);
        List<String> inactiveProductNames = new ArrayList<>();
        for (Product product : productList) {
            if (!product.isActive()) {
                inactiveProductNames.add(product.getName());
            }
            productMap.put(product.getId(), product);
        }
        if (!inactiveProductNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Can not purchase the product(s) \"" +
                    Strings.join(inactiveProductNames)
                    + "\" because it(they) is(are) inactive");
        }
        
        Purchasing purchasing = new Purchasing();
        purchasing.setCreationTime(new Date());
        purchasing.setPurchaser(this.authorizationService.getCurrentAdministrator());
        purchasing.setTotalPurchasedPrice(totalPurchasedPrice);
        for (PurchasingItem purchasingItem : purchasingItems) {
            
            Product product = productMap.get(purchasingItem.getProduct().getId());
            if (product.getInventory() == null) {
                Inventory inventory = new Inventory();
                inventory.setQuantity(purchasingItem.getQuantity());
                inventory.setProduct(product);
                this.inventoryRepository.mergeEntity(inventory);
            } else {
                product.getInventory().setQuantity(
                        product.getInventory().getQuantity() +
                        purchasingItem.getQuantity()
                );
                this.inventoryRepository.mergeEntity(product.getInventory());
            }
            purchasingItem.setPurchasing(purchasing);
        }
        this.purchasingRepository.mergeEntity(purchasing);
    }
}
