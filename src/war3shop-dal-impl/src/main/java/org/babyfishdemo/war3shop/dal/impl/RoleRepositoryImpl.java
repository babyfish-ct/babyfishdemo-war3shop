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

import java.util.List;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfishdemo.war3shop.dal.RoleRepository;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role_;
import org.babyfishdemo.war3shop.entities.Role__;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class RoleRepositoryImpl extends AbstractRepositoryImpl<Role, Long> implements RoleRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Role getRoleById(Long id, Role__... queryPaths) {
        return this.em.find(Role.class, id, queryPaths);
    }

    @Override
    public List<Role> getRolesByIds(Iterable<Long> ids, Role__... queryPaths) {
        return this.em.find(Role.class, ids, queryPaths);
    }

    @Override
    public List<Role> getAllRoles(Role__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> role = cq.from(Role.class);
        cq.orderBy(cb.asc(role.get(Role_.name)));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getResultList();
    }

    @Override
    public Role getRoleByNameInsensitively(String name, Role__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> role = cq.from(Role.class);
        cq.where(cb.equal(cb.upper(role.get(Role_.name)), name.toUpperCase()));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getSingleResult(true);
    }
}
