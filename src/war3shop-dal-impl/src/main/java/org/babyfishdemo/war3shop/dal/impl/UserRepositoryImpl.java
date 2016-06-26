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

import org.babyfish.lang.Nulls;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.war3shop.dal.UserRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Administrator__;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.Customer_;
import org.babyfishdemo.war3shop.entities.Customer__;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer_;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege_;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role_;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.User_;
import org.babyfishdemo.war3shop.entities.User__;
import org.babyfishdemo.war3shop.entities.specification.AdministratorSpecification;
import org.babyfishdemo.war3shop.entities.specification.CustomerSpecification;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class UserRepositoryImpl extends AbstractRepositoryImpl<User, Long> implements UserRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public User getUserById(long id, User__... queryPaths) {
        return this.em.find(User.class, id, queryPaths);
    }

    @Override
    public Administrator getAdministratorById(long id, Administrator__... queryPaths) {
        return this.em.find(Administrator.class, id, queryPaths);
    }

    @Override
    public List<Administrator> getAdministratorsByIds(Collection<Long> ids, Administrator__... queryPaths) {
        return this.em.find(Administrator.class, ids, queryPaths);
    }

    @Override
    public User getUserByNameInsensitively(String name, User__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        XCriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);
        cq.where(cb.equal(cb.upper(user.get(User_.name)), name.toUpperCase()));
        return 
                this
                .em
                .createQuery(cq)
                .setQueryPaths(queryPaths)
                .getSingleResult(true);
    }

    @Override
    public Page<Customer> getCustomers(
            CustomerSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Customer__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        XCriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> customer = cq.from(Customer.class);
        cq.orderBy(cb.asc(customer.get(Customer_.name)));
        Predicate idPredicate = null;
        Predicate namePredicate = null;
        Predicate activePredicate = null;
        Predicate emailPredicate = null;
        Predicate creationTimePredicate = null;
        Predicate phonePredicate = null;
        Predicate addressPredicate = null;
        if (specification != null) {
            if (!Nulls.isNullOrEmpty(specification.getIds())) {
                idPredicate = cb.in(customer.get(Customer_.id), specification.getIds());
            }
            if (!Nulls.isNullOrEmpty(specification.getLikeName())) {
                namePredicate = cb.insensitivelyLike(
                        customer.get(Customer_.name), 
                        specification.getLikeName(), 
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getActive() != null) {
                activePredicate = cb.equal(
                        customer.get(Customer_.active), 
                        specification.getActive()
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getLikeEmail())) {
                emailPredicate = cb.insensitivelyLike(
                        customer.get(Customer_.email), 
                        specification.getLikeEmail(),
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getMinCreationTime() != null || specification.getMaxCreationTime() != null) {
                creationTimePredicate = cb.between(
                        customer.get(Customer_.creationTime), 
                        specification.getMinCreationTime(), 
                        specification.getMaxCreationTime()
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getLikePhone())) {
                phonePredicate = cb.insensitivelyLike(
                        customer.get(Customer_.phone), 
                        specification.getLikePhone(),
                        LikeMode.ANYWHERE
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getLikeAddress())) {
                addressPredicate = cb.insensitivelyLike(
                        customer.get(Customer_.address), 
                        specification.getLikeAddress(),
                        LikeMode.ANYWHERE
                );
            }
        }
        cq.where(
                idPredicate,
                namePredicate,
                activePredicate,
                emailPredicate,
                creationTimePredicate,
                phonePredicate,
                addressPredicate
        );
        return new PageBuilder<>(
                this.em.createQuery(cq).setQueryPaths(queryPaths), 
                pageIndex, 
                pageSize
        ).build();
    }

    @Override
    public Page<Administrator> getAdministrators(
            AdministratorSpecification specification, 
            int pageIndex,
            int pageSize, 
            Administrator__... queryPaths) {
        
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Administrator> cq = cb.createQuery(Administrator.class);
        Root<Administrator> administrator = cq.from(Administrator.class);
        
        if (specification != null) {
            
            Predicate idsPredicate = null;
            Predicate activePredicate = null;
            Predicate likeNamePredicate = null;
            Predicate likeEmailPredicate = null;
            Predicate creationTimePredicate = null;
            
            if (specification.getIds() != null) {
                idsPredicate = cb.in(
                        administrator.get(Administrator_.id), 
                        specification.getIds()
                );
            }
            if (specification.getActive() != null) {
                activePredicate = cb.equal(
                        administrator.get(Administrator_.active), 
                        specification.getActive()
                );
            }
            if (specification.getLikeName() != null) {
                likeNamePredicate = cb.insensitivelyLike(
                        administrator.get(Administrator_.name), 
                        specification.getLikeName(), 
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getLikeEmail() != null) {
                likeEmailPredicate = cb.insensitivelyLike(
                        administrator.get(Administrator_.email), 
                        specification.getLikeEmail(), 
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getMinCreationTime() != null || specification.getMaxCreationTime() != null) {
                creationTimePredicate = cb.between(
                        administrator.get(Administrator_.creationTime), 
                        specification.getMinCreationTime(),
                        specification.getMaxCreationTime()
                );
            }
            
            cq.where(
                    idsPredicate,
                    activePredicate,
                    likeNamePredicate,
                    likeEmailPredicate,
                    creationTimePredicate,
                    
                    cb
                    .dependencyPredicateBuilder(administrator, Manufacturer.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(manufacturer -> manufacturer.join(Manufacturer_.purchasers))
                    .addSelfGetter(new SelfGetter<Administrator, Manufacturer>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Manufacturer> manufacturer) {
                            return manufacturer.join(Manufacturer_.purchasers);
                        }
                    })
                    .includeAny(Manufacturer_.id, specification.getIncludedPurchasedManufacturerIds())
                    .excludeAll(Manufacturer_.id, specification.getExcludedPurchasedManufacturerIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(administrator, Product.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(product -> product.join(Product_.purchasers))
                    .addSelfGetter(new SelfGetter<Administrator, Product>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Product> product) {
                            return product.join(Product_.purchasers);
                        }
                    })
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(product -> product.join(Product_.manufacturers).join(Manufacturer_.purchasers))
                    .addSelfGetter(new SelfGetter<Administrator, Product>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Product> product) {
                            return product.join(Product_.manufacturers).join(Manufacturer_.purchasers);
                        }
                    })
                    .includeAny(Product_.id, specification.getIncludedPurchasedProductIds())
                    .excludeAll(Product_.id, specification.getExcludedPurchasedProductIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(administrator, Role.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(role -> role.join(Role_.administrators))
                    .addSelfGetter(new SelfGetter<Administrator, Role>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Role> role) {
                            return role.join(Role_.administrators);
                        }
                    })
                    .includeAny(Role_.id, specification.getIncludedRoleIds())
                    .excludeAll(Role_.id, specification.getExcludedRoleIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(administrator, Role.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(role -> role.join(Role_.administrators))
                    .addSelfGetter(new SelfGetter<Administrator, Role>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Role> role) {
                            return role.join(Role_.administrators);
                        }
                    })
                    .includeAny(Role_.name, specification.getIncludedRoleNames())
                    .excludeAll(Role_.name, specification.getExcludedRoleNames())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(administrator, Privilege.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(privilege -> privilege.join(Privilege_.administrators))
                    .addSelfGetter(new SelfGetter<Administrator, Privilege>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Privilege> privilege) {
                            return privilege.join(Privilege_.administrators);
                        }
                    })
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(privilege -> privilege.join(Privilege_.roles).join(Role_.administrators))
                    .addSelfGetter(new SelfGetter<Administrator, Privilege>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Privilege> privilege) {
                            return privilege.join(Privilege_.roles).join(Role_.administrators);
                        }
                    })
                    .includeAny(Privilege_.id, specification.getIncludedPrivilegeIds())
                    .excludeAll(Privilege_.id, specification.getExcludedPrivilegeIds())
                    .build(),
                    
                    cb
                    .dependencyPredicateBuilder(administrator, Privilege.class)
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(privilege -> privilege.join(Privilege_.administrators))
                    .addSelfGetter(new SelfGetter<Administrator, Privilege>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Privilege> privilege) {
                            return privilege.join(Privilege_.administrators);
                        }
                    })
                    //Under java8, please change this invocation to be
                    //.addSelfGetter(privilege -> privilege.join(Privilege_.roles).join(Role_.administrators))
                    .addSelfGetter(new SelfGetter<Administrator, Privilege>() {
                        @Override
                        public Path<Administrator> getSelf(XRoot<Privilege> privilege) {
                            return privilege.join(Privilege_.roles).join(Role_.administrators);
                        }
                    })
                    .includeAny(Privilege_.name, specification.getIncludedPrivilegeNames())
                    .excludeAll(Privilege_.name, specification.getExcludedPrivilegeNames())
                    .build()
            );
        }
        return new PageBuilder<>(
                this.em.createQuery(cq).setQueryPaths(queryPaths), 
                pageIndex, 
                pageSize
        )
        .build();
    }
}
