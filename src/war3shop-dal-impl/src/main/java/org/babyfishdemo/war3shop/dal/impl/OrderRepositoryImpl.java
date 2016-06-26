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

import java.util.Date;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.war3shop.dal.OrderRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.Customer_;
import org.babyfishdemo.war3shop.entities.GiftItem_;
import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.OrderItem_;
import org.babyfishdemo.war3shop.entities.Order_;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.TotalMoney_;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class OrderRepositoryImpl 
extends AbstractRepositoryImpl<Order, Long> 
implements OrderRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Order getOrderById(long id, Order__... queryPaths) {
        return this.em.find(Order.class, id, queryPaths);
    }

    @Override
    public Order getNewestTemporaryOrderByCustomerId(long customerId, Order__ ... queryPaths) {
        
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        
        Long newestId;
        {
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<Order> order = cq.from(Order.class);
            cq
            .where(
                    cb.isNotNull(order.get(Order_.gcThreshold)),
                    cb.equal(
                            order.get(Order_.customer).get(Customer_.id),
                            customerId
                    )
            )
            .orderBy(
                    cb.desc(order.get(Order_.id))
            )
            .select(
                    order.get(Order_.id)
            );
            newestId = 
                    this
                    .em
                    .createQuery(cq)
                    .setMaxResults(1)
                    .getSingleResult(true);
        }
        
        if (newestId == null) {
            return null;
        }
        
        return this.em.find(Order.class, newestId, queryPaths);
    }

    @Override
    public Page<Order> getAssuredOrders(
            OrderSpecification specification,
            int pageIndex, 
            int pageSize, 
            Order__... queryPaths) {
        
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);
        Predicate predicate = null;
        
        if (specification != null) {
            
            Predicate deliveredPredicate = null;
            Predicate creationTimePredicate = null;
            Predicate deliveredTimePredicate = null;
            Predicate moneyPredicate = null;
            Predicate assignedPredicate = null;
            
            if (specification.getDelivered() != null) {
                deliveredPredicate = specification.getDelivered().booleanValue() ?
                        cb.isNotNull(order.get(Order_.deliveredTime)) :
                        cb.isNull(order.get(Order_.deliveredTime));
            }
            if (specification.getMinCreationTime() != null || specification.getMaxCreationTime() != null) {
                creationTimePredicate = cb.between(
                        order.get(Order_.creationTime), 
                        specification.getMinCreationTime(), 
                        specification.getMaxCreationTime()
                );
            }
            if (specification.getMinDeliveredTime() != null || specification.getMaxDeliveredTime() != null) {
                deliveredTimePredicate = cb.between(
                        order.get(Order_.deliveredTime), 
                        specification.getMinDeliveredTime(), 
                        specification.getMaxCreationTime()
                );
            }
            if (specification.getMinActualMoney() != null || specification.getMaxActualMoney() != null) {
                moneyPredicate = cb.between(
                        order.get(Order_.totalMoney).get(TotalMoney_.actualMoney), 
                        specification.getMinActualMoney(), 
                        specification.getMaxActualMoney()
                );
            }
            if (specification.getAssigned() != null) {
                assignedPredicate = 
                        specification.getAssigned() ?
                                cb.isNotNull(order.get(Order_.deliveryman)) :
                                cb.isNull(order.get(Order_.deliveryman));
            }
            predicate = cb.and(
                    deliveredPredicate,
                    creationTimePredicate,
                    deliveredTimePredicate,
                    moneyPredicate,
                    assignedPredicate,
                    deliveredPredicate,
                    
                    cb
                    .dependencyPredicateBuilder(order, Customer.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(customer -> customer.join(Customer_.orders))
                    .addSelfGetter(new SelfGetter<Order, Customer>() {
                        @Override
                        public Path<Order> getSelf(XRoot<Customer> customer) {
                            return customer.join(Customer_.orders);
                        }
                    })
                    .includeAny(Customer_.id, specification.getIncludedCustomerIds())
                    .excludeAll(Customer_.id, specification.getExcludedCustomerIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(order, Product.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(product -> product.join(Product_.orderItems).get(OrderItem_.order))
                    .addSelfGetter(new SelfGetter<Order, Product>() {
                        @Override
                        public Path<Order> getSelf(XRoot<Product> product) {
                            return product.join(Product_.orderItems).get(OrderItem_.order);
                        }
                    })
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(product -> product.join(Product_.giftItems).get(GiftItem_.order))
                    .addSelfGetter(new SelfGetter<Order, Product>() {
                        @Override
                        public Path<Order> getSelf(XRoot<Product> product) {
                            return product.join(Product_.giftItems).get(GiftItem_.order);
                        }
                    })
                    .includeAny(Product_.id, specification.getIncludedProductIds())
                    .excludeAll(Product_.id, specification.getExcludedProductIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(order, Administrator.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(administrator -> administrator.join(Administrator_.deliveredOrders))
                    .addSelfGetter(new SelfGetter<Order, Administrator>() {
                        @Override
                        public Path<Order> getSelf(XRoot<Administrator> administrator) {
                            return administrator.join(Administrator_.deliveredOrders);
                        }
                    })
                    .has(specification.getHasDeliverymans())
                    .includeAny(Administrator_.id, specification.getIncludedDeliverymanIds())
                    .excludeAll(Administrator_.id, specification.getExcludedDeliverymanIds())
                    .build()
            );
        }
        cq
        .where(
                cb.isNotNull(order.get(Order_.creationTime)), //AssuredOrder
                predicate
        )
        .orderBy(cb.desc(order.get(Order_.id)));
        return new PageBuilder<Order>(
                this.em.createQuery(cq).setQueryPaths(queryPaths), 
                pageIndex, 
                pageSize)
        .build();
    }

    @Override
    public void removeTemporaryOrderByGcThreshold(Date gcThreshold) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaDelete<Order> cd = cb.createCriteriaDelete(Order.class);
        Root<Order> order = cd.from(Order.class);
        cd.where(cb.lessThan(order.get(Order_.gcThreshold), gcThreshold));
        int removedCount = this.em.createQuery(cd).executeUpdate();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Removed " + removedCount + " temporary orders");
        }
    }
}
