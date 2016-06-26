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

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.war3shop.dal.PurchasingRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer_;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.Purchasing;
import org.babyfishdemo.war3shop.entities.PurchasingItem_;
import org.babyfishdemo.war3shop.entities.Purchasing_;
import org.babyfishdemo.war3shop.entities.Purchasing__;
import org.babyfishdemo.war3shop.entities.specification.PurchasingSpecification;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class PurchasingRepositoryImpl 
extends AbstractRepositoryImpl<Purchasing, Long> 
implements PurchasingRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Page<Purchasing> getPurchasings(
            PurchasingSpecification specification, 
            int pageIndex, 
            int pageSize,
            Purchasing__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Purchasing> cq = cb.createQuery(Purchasing.class);
        Root<Purchasing> purchasing = cq.from(Purchasing.class);
        if (specification != null) {
            cq.where(
                    cb.between(
                            purchasing.get(Purchasing_.creationTime), 
                            specification.getMinCreationTime(), 
                            specification.getMaxCreationTime()
                    ),
                    cb.between(
                            purchasing.get(Purchasing_.totalPurchasedPrice), 
                            specification.getMinTotalPurchasedPrice(), 
                            specification.getMaxTotalPurchasedPrice()
                    ),
                    
                    cb
                    .dependencyPredicateBuilder(purchasing, Product.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(product -> product.join(Product_.purchasingItems).get(PurchasingItem_.purchasing))
                    .addSelfGetter(new SelfGetter<Purchasing, Product>() {
                        @Override
                        public Path<Purchasing> getSelf(XRoot<Product> product) {
                            return product.join(Product_.purchasingItems).get(PurchasingItem_.purchasing);
                        }
                    })
                    .includeAny(Product_.id, specification.getIncludedProductIds())
                    .excludeAll(Product_.id, specification.getExcludedProductIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(purchasing, Manufacturer.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(
                    //      manufacture -> manufacture
                    //      .join(Manufacturer_.products)
                    //      .join(Product_.purchasingItems)
                    //      .get(PurchasingItem_.purchasing)
                    //)
                    .addSelfGetter(new SelfGetter<Purchasing, Manufacturer>() {
                        @Override
                        public Path<Purchasing> getSelf(XRoot<Manufacturer> manufacturer) {
                            return manufacturer
                                    .join(Manufacturer_.products)
                                    .join(Product_.purchasingItems)
                                    .get(PurchasingItem_.purchasing);
                        }
                    })
                    .includeAny(Manufacturer_.id, specification.getIncludedManufacturerIds())
                    .excludeAll(Manufacturer_.id, specification.getExcludedManufacturerIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(purchasing, Administrator.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(administrator -> administrator.join(Administrator_.purchasings))
                    .addSelfGetter(new SelfGetter<Purchasing, Administrator>() {
                        @Override
                        public Path<Purchasing> getSelf(XRoot<Administrator> administrator) {
                            return administrator.join(Administrator_.purchasings);
                        }
                    })
                    .includeAny(Administrator_.id, specification.getIncludedPurchaserIds())
                    .excludeAll(Administrator_.id, specification.getExcludedPurchaserIds())
                    .build()
            );
        }
        XTypedQuery<Purchasing> typedQuery = this.em.createQuery(cq).setQueryPaths(queryPaths);
        return new PageBuilder<>(typedQuery, pageIndex, pageSize).build();
    }

}
