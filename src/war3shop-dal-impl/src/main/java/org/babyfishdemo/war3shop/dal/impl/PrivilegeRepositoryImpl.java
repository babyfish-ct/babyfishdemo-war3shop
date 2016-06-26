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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfishdemo.war3shop.dal.PrivilegeRepository;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege_;
import org.babyfishdemo.war3shop.entities.Privilege__;
import org.babyfishdemo.war3shop.entities.Role_;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class PrivilegeRepositoryImpl implements PrivilegeRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public List<Privilege> getAllPrivileges(Privilege__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Privilege> cq = cb.createQuery(Privilege.class);
        Root<Privilege> privilege = cq.from(Privilege.class);
        cq.orderBy(cb.asc(privilege.get(Privilege_.name)));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getResultList();
    }

    @Override
    public Privilege getPrivilegeByName(String name, Privilege__... queryPaths) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Privilege> cq = cb.createQuery(Privilege.class);
        Root<Privilege> privilege = cq.from(Privilege.class);
        cq.where(cb.equal(privilege.get(Privilege_.name), name));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getSingleResult(true);
    }

    @Override
    public List<Privilege> getPrivilegesByIds(Collection<Long> ids, Privilege__... queryPaths) {
        return this.em.find(Privilege.class, ids, queryPaths);
    }

    @Override
    public List<Privilege> getPrivilegesByNamesAndAdministratorId(Collection<String> names, long administratorId, Privilege__...queryPaths) {
        Arguments.mustNotBeNull("names", names);
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Privilege> cq = cb.createQuery(Privilege.class);
        Root<Privilege> privilege = cq.from(Privilege.class);
        cq
        .where(
                cb.in(privilege.get(Privilege_.name), names),
                cb.or(
                        cb.equal(
                                privilege.join(Privilege_.roles, JoinType.LEFT).join(Role_.administrators).get(Administrator_.id), 
                                administratorId
                        ),
                        cb.equal(
                                privilege.join(Privilege_.administrators, JoinType.LEFT).get(Administrator_.id),
                                administratorId
                        )
                )
        )
        .select(privilege);
        return this
                .em
                .createQuery(cq)
                .setQueryPaths(queryPaths)
                .getResultList();
    }

    @Override
    public Privilege getPrivilegeByNameAndRoleIds(String name, Collection<Long> roleIds, Privilege__... queryPaths) {
        Arguments.mustNotBeNull("name", name);
        Arguments.mustNotBeNull("roleIds", roleIds);
        Arguments.mustNotContainNullElements("roleIds", roleIds);
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Privilege> cq = cb.createQuery(Privilege.class);
        Root<Privilege> privilege = cq.from(Privilege.class);
        cq.where(
                cb.equal(privilege.get(Privilege_.name), name),
                cb.in(
                        privilege.join(Privilege_.roles).get(Role_.id), 
                        roleIds
                )
        );
        return this
                .em
                .createQuery(cq)
                .setQueryPaths(queryPaths)
                .getSingleResult(true);
    }
}
