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
package org.babyfishdemo.war3shop.dal.impl;

import java.util.EnumSet;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.babyfish.model.jpa.path.TypedQueryPaths;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.war3shop.dal.ProductRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Inventory;
import org.babyfishdemo.war3shop.entities.Inventory_;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer_;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.ProductType;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Race;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class ProductRepositoryImpl 
extends AbstractRepositoryImpl<Product, Long> 
implements ProductRepository {

    @Override
    public Product getProductById(long id, Product__... queryPaths) {
        return 
                this.em.find(
                        Product.class, 
                        id, 
                        // product.inventory is marked by @OneToOne with the attribute "mappedBy",
                        // it can not be implemented by real lazy association by Hibernate,
                        // so always use Product__.begin().inventory().end() to avoid
                        // the N + 1 query for it.
                        TypedQueryPaths.combine(
                                queryPaths, 
                                Product__.begin().inventory().end()
                        )
                );
    }

    @Override
    public List<Product> getProductsByIds(
            Iterable<Long> ids, 
            Product__... queryPaths) {
        return 
                this.em.find(
                        Product.class, 
                        ids, 
                        // product.inventory is marked by @OneToOne with the attribute "mappedBy",
                        // it can not be implemented by real lazy association by Hibernate,
                        // so always use Product__.begin().inventory().end() to avoid
                        // the N + 1 query for it.
                        TypedQueryPaths.combine(
                                queryPaths, 
                                Product__.begin().inventory().end()
                        )
                );
    }

    @Override
    public Product getProductLikeNameInsensitively(String name, Product__... queryPaths) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);
        
        // product.inventory is marked by @OneToOne with the attribute "mappedBy",
        // it can not be implemented by real lazy association by Hibernate,
        // so always use Product__.begin().inventory().end() to avoid
        // the N + 1 query for it.
        product.fetch(Product_.inventory, JoinType.LEFT);
        
        cq
        .where(cb.equal(cb.upper(product.get(Product_.name)), name.toUpperCase()))
        .orderBy(cb.asc(product.get(Product_.name)));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getSingleResult(true);
    }

    @Override
    public Page<Product> getProducts(
            ProductSpecification specification,
            int pageIndex, 
            int pageSize, 
            Product__... queryPaths) {
        
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);
        
        // product.inventory is marked by @OneToOne with the attribute "mappedBy",
        // it can not be implemented by real lazy association by Hibernate,
        // so always use Product__.begin().inventory().end() to avoid
        // the N + 1 query for it.
        product.fetch(Product_.inventory, JoinType.LEFT);
                
        cq.orderBy(cb.asc(product.get(Product_.name)));
        
        if (specification != null) {
            
            Predicate activePridcate = null;
            Predicate namePredicate = null;
            Predicate creationTimePredicate = null;
            Predicate priceRangePredicate = null;
            Predicate invetoryQuantityRangePredicate = null;
            Predicate typesPredicate = null;
            Predicate racesPredicate = null;
            Predicate inventoryPredicate = null;
            
            if (specification.getActive() != null) {
                activePridcate = 
                        specification.getActive() ?
                        cb.isTrue(product.get(Product_.active)) :
                        cb.isFalse(product.get(Product_.active));
            }
            if (specification.getLikeName() != null) {
                namePredicate = cb.insensitivelyLike(
                        product.get(Product_.name), 
                        specification.getLikeName(), 
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getMinCreationTime() != null || specification.getMaxCreationTime() != null) {
                priceRangePredicate = cb.between(
                        product.get(Product_.creationTime), 
                        specification.getMinCreationTime(), 
                        specification.getMaxCreationTime());
            }
            if (specification.getMinPrice() != null || specification.getMaxPrice() != null) {
                priceRangePredicate = cb.between(
                        product.get(Product_.price), 
                        specification.getMinPrice(), 
                        specification.getMaxPrice()
                );
            }
            if (specification.getTypes() != null) {
                EnumSet<ProductType> set = EnumSet.noneOf(ProductType.class);
                set.addAll(specification.getTypes());
                if (set.size() < ProductType.values().length) {
                    typesPredicate = cb.in(product.get(Product_.type), set);
                }
            }
            if (specification.getRaces() != null) {
                EnumSet<Race> set = EnumSet.noneOf(Race.class);
                set.addAll(specification.getRaces());
                if (set.size() < Race.values().length) {
                    racesPredicate = cb.in(product.get(Product_.race), set);
                }
            }
            if ((specification.getMinInventory() != null && specification.getMinInventory() != 0) || specification.getMaxInventory() != null) {
                Join<Product, Inventory> inventory = product.join(Product_.inventory, JoinType.LEFT);
                inventoryPredicate = cb.between(
                        cb.coalesce(
                                inventory.get(Inventory_.quantity), 
                                cb.constant(0)
                        ), 
                        specification.getMinInventory() != null && specification.getMinInventory() != 0 ?
                        specification.getMinInventory() :
                        null,
                        specification.getMaxInventory() != null ?
                        specification.getMaxInventory() :
                        null
                );
            }
            
            cq.where(
                    activePridcate,
                    namePredicate,
                    creationTimePredicate,
                    priceRangePredicate,
                    invetoryQuantityRangePredicate,
                    typesPredicate,
                    racesPredicate,
                    inventoryPredicate,
                    
                    cb
                    .dependencyPredicateBuilder(product, Manufacturer.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(manufacture -> manufacture.join(Manufacturer_.products))
                    .addSelfGetter(new SelfGetter<Product, Manufacturer>() {
                        @Override
                        public Path<Product> getSelf(XRoot<Manufacturer> manufacturer) {
                            return manufacturer.join(Manufacturer_.products);
                        }
                    })
                    .has(specification.getHasManufacturers())
                    .includeAny(Manufacturer_.id, specification.getIncludedManufacturerIds())
                    .excludeAll(Manufacturer_.id, specification.getExcludedManufacturerIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(product, Administrator.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(administrator -> administrator.join(Administrator_.purchasedProducts))
                    .addSelfGetter(new SelfGetter<Product, Administrator>() {
                        @Override
                        public Path<Product> getSelf(XRoot<Administrator> administrator) {
                            return administrator.join(Administrator_.purchasedProducts);
                        }
                    })
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(
                    //      administrator -> 
                    //      administrator
                    //      .join(Administrator_.purchasedManufacturers)
                    //      .join(Manufacturer_.products)
                    //)
                    .addSelfGetter(new SelfGetter<Product, Administrator>() {
                        @Override
                        public Path<Product> getSelf(XRoot<Administrator> administrator) {
                            return administrator
                                    .join(Administrator_.purchasedManufacturers)
                                    .join(Manufacturer_.products);
                        }
                    })
                    .has(specification.getHasPurchasers())
                    .includeAny(Administrator_.id, specification.getIncludedPurchaserIds())
                    .excludeAll(Administrator_.id, specification.getExcludedPurchaserIds())
                    .build()
            );
        }
        
        XTypedQuery<Product> typedQuery = this.em.createQuery(cq).setQueryPaths(queryPaths);
        return new PageBuilder<>(typedQuery, pageIndex, pageSize).build();
    }
}
