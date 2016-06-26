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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfishdemo.war3shop.dal.PreferentialItemRepository;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem_;
import org.babyfishdemo.war3shop.entities.PreferentialItem__;
import org.babyfishdemo.war3shop.entities.Preferential_;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class PreferentialItemRepositoryImpl implements PreferentialItemRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Page<PreferentialItem> getPreferentialItemsByParent(
            long preferentialId,
            int pageIndex, 
            int pageSize, 
            PreferentialItem__... queryPaths) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<PreferentialItem> cq = cb.createQuery(PreferentialItem.class);
        Root<PreferentialItem> preferentialItem = cq.from(PreferentialItem.class);
        cq.where(
                cb.equal(
                        preferentialItem.get(PreferentialItem_.preferential).get(Preferential_.id), 
                        preferentialId
                )
        );
        XTypedQuery<PreferentialItem> typedQuery = this.em.createQuery(cq).setQueryPaths(queryPaths);
        return new PageBuilder<>(typedQuery, pageIndex, pageSize).build();
    }
}
