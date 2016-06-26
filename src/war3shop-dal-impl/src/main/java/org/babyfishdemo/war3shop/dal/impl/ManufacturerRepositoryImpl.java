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

import java.util.Collection;
import java.util.List;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Nulls;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.war3shop.dal.ManufacturerRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer_;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.Race;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class ManufacturerRepositoryImpl 
extends AbstractRepositoryImpl<Manufacturer, Long>
implements ManufacturerRepository {
    
    @PersistenceContext
    private XEntityManager em;

    @Override
    public Page<Manufacturer> getManufacturers(
            ManufacturerSpecification specification, 
            int pageIndex,
            int pageSize, 
            Manufacturer__... queryPaths) {
        
        XCriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Manufacturer> cq = cb.createQuery(Manufacturer.class);
        Root<Manufacturer> manufacturer = cq.from(Manufacturer.class);
        cq.orderBy(cb.asc(manufacturer.get(Manufacturer_.name)));
        
        if (specification != null) {
            
            Predicate likeNamePredicate = null;
            Predicate likeEmailPredicate = null;
            Predicate likePhonePredicate = null;
            Predicate racesPredicate = null;
            
            if (!Nulls.isNullOrEmpty(specification.getLikeName())) {
                likeNamePredicate = cb.insensitivelyLike(
                        manufacturer.get(Manufacturer_.name), 
                        specification.getLikeName(), 
                        LikeMode.ANYWHERE
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getLikeEmail())) {
                likeEmailPredicate = cb.insensitivelyLike(
                        manufacturer.get(Manufacturer_.email), 
                        specification.getLikeEmail(), 
                        LikeMode.ANYWHERE
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getLikePhone())) {
                likePhonePredicate = cb.insensitivelyLike(
                        manufacturer.get(Manufacturer_.phone), 
                        specification.getLikePhone(),
                        LikeMode.ANYWHERE
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getRaces()) && 
                    !specification.getRaces().containsAll(MACollections.wrap(Race.values()))) {
                racesPredicate = cb.in(manufacturer.get(Manufacturer_.race), specification.getRaces());
            }
            cq.where(
                    likeNamePredicate,
                    likeEmailPredicate,
                    likePhonePredicate,
                    racesPredicate,
                    
                    cb
                    .dependencyPredicateBuilder(manufacturer, Product.class)
                    //Under java8, please change this invocation to be 
                    //.addSelfGetter(product -> product.join(Product_.manufacturers))
                    .addSelfGetter(new SelfGetter<Manufacturer, Product>() {
                        @Override
                        public Path<Manufacturer> getSelf(XRoot<Product> product) {
                            return product.join(Product_.manufacturers);
                        }
                    })
                    .has(specification.getHasProducts())
                    .includeAny(Product_.id, specification.getIncludedProductIds())
                    .excludeAll(Product_.id, specification.getExcludedProductIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(manufacturer, Administrator.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(administrator -> administrator.join(Administrator_.purchasedManufacturers))
                    .addSelfGetter(new SelfGetter<Manufacturer, Administrator>() {
                        @Override
                        public Path<Manufacturer> getSelf(XRoot<Administrator> administrator) {
                            return administrator.join(Administrator_.purchasedManufacturers);
                        }
                    })
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(administrator -> administrator.join(Administrator_.purchasedProducts).join(Product_.manufacturers))
                    .addSelfGetter(new SelfGetter<Manufacturer, Administrator>() {
                        @Override
                        public Path<Manufacturer> getSelf(XRoot<Administrator> administrator) {
                            return administrator.join(Administrator_.purchasedProducts).join(Product_.manufacturers);
                        }
                    })
                    .has(specification.getHasPurchasers())
                    .includeAny(Administrator_.id, specification.getIncludedPurchaserIds())
                    .excludeAll(Administrator_.id, specification.getExcludedPurchaserIds())
                    .build()
            );
        }
        return new PageBuilder<>(
                this.em.createQuery(cq).setQueryPaths(queryPaths),
                pageIndex,
                pageSize
        ).build();
    }

    @Override
    public Manufacturer getManufacturerById(long id, Manufacturer__... queryPaths) {
        return this.em.find(Manufacturer.class, id, queryPaths);
    }

    @Override
    public List<Manufacturer> getManufacturerByIds(Collection<Long> ids, Manufacturer__... queryPaths) {
        return this.em.find(Manufacturer.class, ids, queryPaths);
    }

    @Override
    public Manufacturer getManufacturerByNameInsensitively(String name, Manufacturer__... queryPaths) {
        XCriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Manufacturer> cq = cb.createQuery(Manufacturer.class);
        Root<Manufacturer> manufacturer = cq.from(Manufacturer.class);
        cq
        .where(cb.equal(cb.upper(manufacturer.get(Manufacturer_.name)), name.toUpperCase()))
        .orderBy(cb.asc(manufacturer.get(Manufacturer_.name)));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getSingleResult(true);
    }

    @Override
    public void persistManufacturer(Manufacturer manufacturer) {
        this.em.persist(manufacturer);
    }
}
