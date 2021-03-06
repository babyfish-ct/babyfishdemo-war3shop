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
package org.babyfishdemo.war3shop.bll;

import java.util.Collection;
import java.util.List;

import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator__;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.Customer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege__;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role__;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.User__;
import org.babyfishdemo.war3shop.entities.specification.AdministratorSpecification;
import org.babyfishdemo.war3shop.entities.specification.CustomerSpecification;

/**
 * @author Tao Chen
 */
public interface AuthorizationService {

    User getUserById(long id, User__ ... queryPaths);
    
    boolean isRegisterable(String name);
    
    void register(Customer customer, String password);
    
    User login(String name, String password);
    
    void logout();
    
    Long getCurrentUserId();
    
    User getCurrentUser(User__... queryPaths);
    
    Administrator getCurrentAdministrator(Administrator__... queryPaths);
    
    boolean hasPermissions(String ... privilegeNames);
    
    List<Role> getAllRoles(Role__ ... queryPaths);
    
    List<Privilege> getAllPrivileges(Privilege__... queryPaths);
    
    Privilege getPrivilegeByName(String name, Privilege__... queryPaths);

    Administrator getAdministratorById(long id, Administrator__... queryPaths);
    
    Page<Customer> getCustomers(
            CustomerSpecification specification,
            int pageIndex,
            int pageSize,
            Customer__ ... queryPaths);
    
    Page<Administrator> getAdministrators(
            AdministratorSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Administrator__... queryPaths);
    
    void activeCustomer(Customer customer);
    
    void submitAdministrator(Administrator administrator);
    
    void deleteUser(long id);

    void configureCurrentUser(User currentUser);
    
    void changePassword(User currentUser, String originalPassword, String newPassword);

    void updateRole(Long id, Integer version, String name, Collection<Long> privilegeIds);

    void deleteRole(long id);
}
