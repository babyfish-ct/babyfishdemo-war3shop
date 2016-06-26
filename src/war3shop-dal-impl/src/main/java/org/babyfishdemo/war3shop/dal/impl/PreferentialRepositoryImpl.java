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

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.war3shop.dal.PreferentialRepository;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.PreferentialActionType;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem_;
import org.babyfishdemo.war3shop.entities.PreferentialThresholdType;
import org.babyfishdemo.war3shop.entities.Preferential_;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.specification.PreferentialSpecification;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class PreferentialRepositoryImpl
extends AbstractRepositoryImpl<Preferential, Long>
implements PreferentialRepository {
    
    @PersistenceContext
    private XEntityManager em;

    @Override
    public Preferential getPreferentialById(long id, Preferential__ ... queryPaths) {
        return this.em.find(Preferential.class, id, queryPaths);
    }

    @Override
    public Page<Preferential> getPreferentials(
            PreferentialSpecification specification,
            int pageIndex, 
            int pageSize, 
            Preferential__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Preferential> cq = cb.createQuery(Preferential.class);
        Root<Preferential> preferential = cq.from(Preferential.class);
        if (specification != null) {
            Predicate activePredicate = null;
            Predicate minDatePredicate = null;
            Predicate maxDatePredicate = null;
            Predicate thresholdTypePredicate = null;
            Predicate actionTypePredicate = null;
            if (specification.getActive() != null) {
                activePredicate = cb.equal(
                        preferential.get(Preferential_.active), 
                        specification.getActive()
                );
            }
            if (specification.getMinDate() != null) {
                minDatePredicate = cb.greaterThanOrEqualTo(
                        preferential.get(Preferential_.endDate), 
                        specification.getMinDate()
                );
            }
            if (specification.getMaxDate() != null) {
                maxDatePredicate = cb.lessThanOrEqualTo(
                        preferential.get(Preferential_.startDate), 
                        specification.getMaxDate()
                );
            }
            if (specification.getThresholdTypes() != null) {
                Set<PreferentialThresholdType> set = EnumSet.noneOf(PreferentialThresholdType.class);
                set.addAll(specification.getThresholdTypes());
                if (set.size() < PreferentialThresholdType.values().length) {
                    thresholdTypePredicate = cb.in(
                            preferential.get(Preferential_.thresholdType), 
                            specification.getThresholdTypes()
                    );
                }
            }
            if (specification.getActionTypes() != null) {
                Set<PreferentialActionType> set = EnumSet.noneOf(PreferentialActionType.class);
                set.addAll(specification.getActionTypes());
                if (set.size() < PreferentialActionType.values().length) {
                    actionTypePredicate = cb.in(
                            preferential.get(Preferential_.actionType),
                            specification.getActionTypes()
                    );
                }
            }
            cq
            .where(
                    activePredicate,
                    minDatePredicate,
                    maxDatePredicate,
                    thresholdTypePredicate,
                    actionTypePredicate,
                    
                    cb
                    .dependencyPredicateBuilder(preferential, Product.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(product -> product.join(Product_.preferentials))
                    .addSelfGetter(new SelfGetter<Preferential, Product>() {
                        @Override
                        public Path<Preferential> getSelf(XRoot<Product> product) {
                            return product.join(Product_.preferentials);
                        }
                    })
                    .includeAny(Product_.id, specification.getIncludedProductIds())
                    .excludeAll(Product_.id, specification.getExcludedProductIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(preferential, Product.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(
                    //      product -> product
                    //      .join(Product_.preferentialItemsThatUseMeToBeGift)
                    //      .get(PreferentialItem_.preferential)
                    //)
                    .addSelfGetter(new SelfGetter<Preferential, Product>() {
                        @Override
                        public Path<Preferential> getSelf(XRoot<Product> product) {
                            return product
                                    .join(Product_.preferentialItemsThatUseMeToBeGift)
                                    .get(PreferentialItem_.preferential);
                        }
                    })
                    .has(specification.getHasGiftProducts())
                    .includeAny(Product_.id, specification.getIncludedGiftProductIds())
                    .excludeAll(Product_.id, specification.getExcludedGiftProductIds())
                    .build()
            )
            .orderBy( //Default order, it can be overridden by the parameter "queryPaths"
                    cb.desc(preferential.get(Preferential_.endDate))
            );
        }
        XTypedQuery<Preferential> typedQuery = this.em.createQuery(cq).setQueryPaths(queryPaths);
        return new PageBuilder<>(typedQuery, pageIndex, pageSize).build();
    }

    @Override
    public List<Preferential> getPreferentialsThatCanBeAffectedByProduct(long productId, Preferential__ ... queryPaths) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Preferential> cq = cb.createQuery(Preferential.class);
        Root<Preferential> preferential = cq.from(Preferential.class);
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Subquery<PreferentialItem> sq = cq.subquery(PreferentialItem.class);
        {
            Root<PreferentialItem> preferentialItem = sq.from(PreferentialItem.class);
            sq.where(
                    cb.equal(preferentialItem.get(PreferentialItem_.preferential), preferential),
                    cb.equal(preferentialItem.get(PreferentialItem_.giftProduct).get(Product_.id), productId)
            );
        }
        cq.where(
                cb.greaterThanOrEqualTo(preferential.get(Preferential_.endDate), today.getTime()),
                cb.or(
                        cb.equal(preferential.get(Preferential_.product).get(Product_.id), productId),
                        cb.exists(sq)
                )
        );
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getResultList();
    }

    @Override
    public void mergePreferential(Preferential preferential) {
        this.em.merge(preferential);
    }
}
