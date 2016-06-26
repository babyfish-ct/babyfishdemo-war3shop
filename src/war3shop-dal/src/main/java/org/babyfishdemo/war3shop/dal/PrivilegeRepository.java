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
package org.babyfishdemo.war3shop.dal;

import java.util.Collection;
import java.util.List;

import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege__;

/**
 * @author Tao Chen
 */
public interface PrivilegeRepository {
    
    List<Privilege> getAllPrivileges(Privilege__ ... queryPaths);
    
    Privilege getPrivilegeByName(String name, Privilege__ ... queryPaths);

    List<Privilege> getPrivilegesByIds(Collection<Long> ids, Privilege__ ... queryPaths);
    
    List<Privilege> getPrivilegesByNamesAndAdministratorId(Collection<String> names, long administratorId, Privilege__ ...queryPaths);
    
    Privilege getPrivilegeByNameAndRoleIds(String name, Collection<Long> ids, Privilege__ ...queryPaths);
}
