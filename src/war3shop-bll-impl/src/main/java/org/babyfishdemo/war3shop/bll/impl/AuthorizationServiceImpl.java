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
package org.babyfishdemo.war3shop.bll.impl;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.war3shop.bll.AuthorizationException;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.impl.aop.RequirePermission;
import org.babyfishdemo.war3shop.dal.ManufacturerRepository;
import org.babyfishdemo.war3shop.dal.OrderRepository;
import org.babyfishdemo.war3shop.dal.PrivilegeRepository;
import org.babyfishdemo.war3shop.dal.ProductRepository;
import org.babyfishdemo.war3shop.dal.RoleRepository;
import org.babyfishdemo.war3shop.dal.UserRepository;
import org.babyfishdemo.war3shop.entities.AccountManager;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Administrator__;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.Customer_;
import org.babyfishdemo.war3shop.entities.Customer__;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege__;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role_;
import org.babyfishdemo.war3shop.entities.Role__;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.User_;
import org.babyfishdemo.war3shop.entities.User__;
import org.babyfishdemo.war3shop.entities.specification.AdministratorSpecification;
import org.babyfishdemo.war3shop.entities.specification.CustomerSpecification;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class AuthorizationServiceImpl implements AuthorizationService {
    
    private static final Log log = LogFactory.getLog(AuthorizationServiceImpl.class);
    
    private static final char[] GENERATED_PASSWORD_CHARS = 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    
    private Long currentUserId;
    
    @Resource
    private UserRepository userRepository;
    
    @Resource
    private RoleRepository roleRepository;
    
    @Resource
    private PrivilegeRepository privilegeRepository;
    
    @Resource
    private ManufacturerRepository manufacturerRepository;
    
    @Resource
    private ProductRepository productRepository;
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource(name = "welcome-new-customer-mail-component")
    private MailComponent welcomeNewCustomerMailComponent;
    
    @Resource(name = "welcome-new-administrator-mail-component")
    private MailComponent welcomeNewAdministratorMailComponent;
    
    @Resource(name = "unassign-order-mail-component")
    private MailComponent unassignOrderMailComponent;
    
    @Transactional(readOnly = true)
    @Override
    public User getUserById(long id, User__... queryPaths) {
        return this.userRepository.getUserById(id, queryPaths);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isRegisterable(String name) {
        return this.userRepository.getUserByNameInsensitively(name) == null;
    }

    @Transactional
    @Override
    public void register(Customer customer, String password) {
        
        Arguments.mustBeNull("customer.getId()", customer.getId());
        JPAEntities.validateMaxEnabledRange(
                customer, 
                JPAEntities.required(Customer_.name),
                JPAEntities.required(Customer_.email),
                JPAEntities.required(Customer_.phone),
                JPAEntities.required(Customer_.address),
                User_.imageMimeType,
                User_.image);
        
        User conflict = this.userRepository.getUserByNameInsensitively(customer.getName());
        if (conflict != null && !JPAEntities.isIdEquals(customer, conflict)) {
            throw new AuthorizationException(
                    "Can not register the user becuase the name \"" +
                    customer.getName() +
                    "\" is already existing");
        }
        
        customer.setActive(true);
        customer.setPassword(toBinaryPassword(password));
        customer.setPasswordTime(new Date());
        customer.setCreationTime(new Date());
        
        this.userRepository.mergeEntity(customer);
        this.userRepository.flush();
        log.info(
                "An new customer \"" +
                customer.getName() +
                "\" is registered");
        
        this.welcomeNewCustomerMailComponent.asyncSendAndIgnoreException(
                customer.getEmail(),
                "userName",
                customer.getName(),
                "password",
                password);
    }

    @Transactional
    @Override
    public User login(String name, String password) {
        
        User user = this.userRepository.getUserByNameInsensitively(name);
        if (user == null) {
            throw new AuthorizationException(
                    "The name \"" +
                    name +
                    "\" is error, there is no such login name");
        }
        if (!user.isActive()) {
            throw new AuthorizationException("Can not login because you're inactive");
        }
        
        byte[] passwordBytes = toBinaryPassword(password);
        
        if (!Arrays.equals(passwordBytes, user.getPassword())) {
            throw new IllegalArgumentException("The password is error");
        }
        
        user.setLastLoginTime(new Date());
        this.userRepository.mergeEntity(user);
        
        this.currentUserId = user.getId();
        
        long diffTime = new Date().getTime() - user.getPasswordTime().getTime();
        if (diffTime >= 90 * 24 * 60 * 60 * 1000000) {
            throw new AuthorizationException("The password is expired");
        }
        
        return user;
    }
    
    @Transactional
    public void changePhoto(InputStream inputStream) {
        if (this.currentUserId == null) {
            throw new AuthorizationException("Can not change the phote because you've not logined");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public void logout() {
        this.currentUserId = null;
    }

    @Override
    public Long getCurrentUserId() {
        return this.currentUserId;
    }

    @Transactional(readOnly = true)
    @Override
    public User getCurrentUser(User__ ... queryPaths) {
        if (this.currentUserId != null) {
            return this.userRepository.getUserById(this.currentUserId, queryPaths);
        }
        return null;
    }
    
    @Transactional(readOnly = true)
    @Override
    public Administrator getCurrentAdministrator(Administrator__ ... queryPaths) {
        if (this.currentUserId != null) {
            return this.userRepository.getAdministratorById(this.currentUserId, queryPaths);
        }
        return null;
    }
    
    @Transactional(readOnly = true)
    @Override
    public boolean hasPermissions(String ... permissionNames) {
        User currentUser = this.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        Set<String> privilageNames = new LinkedHashSet<>((permissionNames.length * 4 + 2) / 3);
        for (String permissionName : permissionNames) {
            if (Constants.ACCOUNT_MANAGER_PERMISSION.equals(permissionName)) {
                if (currentUser instanceof AccountManager) {
                    return true;
                }
            } else if (Constants.CUSTOMER_PERMISSION.equals(permissionName)) {
                if (currentUser instanceof Customer) {
                    return true;
                }
            } else {
                privilageNames.add(permissionName);
            }
        }
        return !this
                .privilegeRepository
                .getPrivilegesByNamesAndAdministratorId(privilageNames, this.currentUserId) 
                .isEmpty();
    }

    @Transactional(readOnly = true)
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public List<Role> getAllRoles(Role__... queryPaths) {
        return this.roleRepository.getAllRoles(queryPaths);
    }

    @Transactional(readOnly = true)
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public List<Privilege> getAllPrivileges(Privilege__... queryPaths) {
        return this.privilegeRepository.getAllPrivileges(queryPaths);
    }

    @Transactional(readOnly = true)
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public Privilege getPrivilegeByName(String name, Privilege__ ... queryPaths) {
        return this.privilegeRepository.getPrivilegeByName(name, queryPaths);
    }

    @Transactional(readOnly = true)
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public Administrator getAdministratorById(long id, Administrator__... queryPaths) {
        return this.userRepository.getAdministratorById(id, queryPaths);
    }
    
    @Transactional(readOnly = true)
    @RequirePermission({ 
        Constants.ACCOUNT_MANAGER_PERMISSION, 
        Constants.MANAGE_ORDERS,
        Constants.DELIVERY_ORDERS
    })
    @Override
    public Page<Customer> getCustomers(
            CustomerSpecification specification, 
            int pageIndex,
            int pageSize, 
            Customer__... queryPaths) {
        return this.userRepository.getCustomers(specification, pageIndex, pageSize, queryPaths);
    }

    @Transactional(readOnly = true)
    @RequirePermission({ 
        Constants.ACCOUNT_MANAGER_PERMISSION, 
        Constants.MANAGE_MANUFACTURERS, 
        Constants.MANAGE_PRODUCTS,
        Constants.MANAGE_ORDERS
    })
    @Override
    public Page<Administrator> getAdministrators(
            AdministratorSpecification specification, 
            int pageIndex,
            int pageSize, 
            Administrator__... queryPaths) {
        return this.userRepository.getAdministrators(specification, pageIndex, pageSize, queryPaths);
    }
    
    @Transactional
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public void activeCustomer(Customer customer) {
        JPAEntities.validateMaxEnabledRange(
                customer, 
                JPAEntities.required(Customer_.id),
                JPAEntities.required(Customer_.version),
                JPAEntities.required(Customer_.active)
        );
        this.userRepository.mergeEntity(customer);
    }

    @Transactional
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public void submitAdministrator(Administrator administrator) {
        
        JPAEntities.validateMaxEnabledRange(
                administrator, 
                JPAEntities.required(Administrator_.id),
                JPAEntities.required(Administrator_.version),
                Administrator_.name,
                Administrator_.active,
                Administrator_.email,
                Administrator_.roles,
                Administrator_.privileges
        );
        
        String generatedPasswordForNewUser = null;
        boolean originalCanPurchaseProducts = false;
        boolean originalCanDeliveryOrders = false;
        
        if (JPAEntities.isEnabled(administrator, Administrator_.name)) {
            if (Nulls.isNullOrEmpty(administrator.getName())) {
                throw new IllegalArgumentException("Can not add add/update administartor with no name.");
            }
            User conflict = this.userRepository.getUserByNameInsensitively(administrator.getName());
            if (conflict != null && !JPAEntities.isIdEquals(administrator, conflict)) {
                throw new IllegalArgumentException(
                        "There is already an user whose name is \"" +
                        administrator.getName() +
                        "\".");
            }
        }
        
        if (administrator.getId() == null) {
            if (JPAEntities.isDisabled(administrator, Administrator_.name)) {
                throw new IllegalArgumentException("Can not add new administartor with no name.");
            }
            //Generate random password for the new user, the password will be sent to user by email later.
            char[] buf = new char[8];
            Random random = new Random();
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = GENERATED_PASSWORD_CHARS[random.nextInt(GENERATED_PASSWORD_CHARS.length)];
            }
            generatedPasswordForNewUser = new String(buf);
            administrator.setPassword(toBinaryPassword(generatedPasswordForNewUser));
            administrator.setPasswordTime(new Date());
            administrator.setCreationTime(new Date());
            administrator.setActive(true);
        } else {
            Administrator orignalAdministrator = this.userRepository.getAdministratorById(
                    administrator.getId(), 
                    Administrator__.begin().roles().privileges().end(),
                    Administrator__.begin().privileges().end());
            if (orignalAdministrator == null) {
                throw new IllegalArgumentException(
                        "Can not update the administrator, there is no such administarator whose id is \"" +
                        administrator.getId() +
                        "\"");
            }
            originalCanPurchaseProducts = this.checkPrivilege(orignalAdministrator, Constants.PURCHASE_PRODUCTS);
            originalCanDeliveryOrders = this.checkPrivilege(orignalAdministrator, Constants.DELIVERY_ORDERS);
        }
        
        // If the privilege "purchase-products" has been deleted.
        if (originalCanPurchaseProducts && !this.checkPrivilege(administrator, Constants.PURCHASE_PRODUCTS)) {
            
            // Delete the dynamic purchasing privileges between administrator and manufactures
            ManufacturerSpecification mspec = new ManufacturerSpecification();
            mspec.setIncludedPurchaserIds(MACollections.wrap(administrator.getId()));
            List<Manufacturer> manufacturers = this.manufacturerRepository.getManufacturers(
                    mspec, 
                    0, 
                    Integer.MAX_VALUE,
                    Manufacturer__.begin().purchasers().end()
            )
            .getEntities();
            for (Manufacturer manufacturer : manufacturers) {
                log.info(
                        "Delete the purchase relationship between the manufacturer:" +
                        manufacturer.getId() +
                        " and the administrator: " +
                        administrator.getId() +
                        ", because the privilege \"" +
                        Constants.PURCHASE_PRODUCTS +
                        "\" of this administrator has been removed.");
                manufacturer.getPurchasers().remove(administrator);
                this.manufacturerRepository.mergeEntity(manufacturer);
            }
            
            // Delete the dynamic purchasing privileges between administrator and products
            ProductSpecification pspec = new ProductSpecification();
            pspec.setIncludedPurchaserIds(MACollections.wrap(administrator.getId()));
            List<Product> products = this.productRepository.getProducts(
                    pspec, 
                    0, 
                    Integer.MAX_VALUE, 
                    Product__.begin().purchasers().end()
            )
            .getEntities();
            for (Product product : products) {
                log.info(
                        "Delete the purchase relationship between the product:" +
                        product.getId() +
                        " and the administrator: " +
                        administrator.getId() +
                        ", because the privilege \"" +
                        Constants.PURCHASE_PRODUCTS +
                        "\" of this administrator has been removed.");
                product.getPurchasers().remove(administrator);
                this.productRepository.mergeEntity(product);
            }
        }
        
        if (originalCanDeliveryOrders && !this.checkPrivilege(administrator, Constants.DELIVERY_ORDERS)) {
            OrderSpecification ospec = new OrderSpecification();
            ospec.setIncludedDeliverymanIds(MACollections.wrapLong(administrator.getId()));
            List<Order> orders = this.orderRepository.getAssuredOrders(
                    ospec, 
                    0, 
                    Integer.MAX_VALUE,
                    Order__.begin().customer().end()
            ).getEntities();
            for (Order order : orders) {
                log.info(
                        "Delete the delivery relationship between the order:" +
                        order.getId() +
                        " and the administrator: " +
                        administrator.getId() +
                        ", because the privilege \"" +
                        Constants.DELIVERY_ORDERS +
                        "\" of this administrator has been removed.");
                order.setDeliveryman(null);
                this.orderRepository.mergeEntity(order);
                this.unassignOrderMailComponent.asyncSendAndIgnoreException(
                        order.getCustomer().getEmail(),
                        "userName",
                        order.getCustomer().getName(),
                        "orderId",
                        order.getId().toString()
                );
            }
        }
        
        this.userRepository.mergeEntity(administrator);
        
        if (generatedPasswordForNewUser != null) {
            // Use flush to update the database immediately.
            // If the database is changed successfully, the send the email
            // if the email send failed, rollback the whole business operations.
            this.userRepository.flush();
            
            log.info(
                    "An new addministrator \"" +
                    administrator.getName() +
                    "\" is created");
            
            this.welcomeNewAdministratorMailComponent.send(
                    administrator.getEmail(), 
                    "userName",
                    administrator.getName(),
                    "initializedPassword",
                    generatedPasswordForNewUser
            );
        }
    }
    
    @Transactional
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public void deleteUser(long id) {
        User user = this.userRepository.getUserById(id);
        if (user != null) {
            this.userRepository.removeEntity(user);
        }
    }

    @Transactional
    @Override
    public void configureCurrentUser(User currentUser) {
        if (this.currentUserId == null) {
            throw new AuthorizationException("You have not logined");
        }
        if (!Nulls.equals(this.currentUserId, currentUser.getId())) {
            throw new IllegalArgumentException("Invalid id of current user");
        }
        
        if (currentUser instanceof Customer) {
            JPAEntities.validateMaxEnabledRange(
                    (Customer)currentUser,
                    JPAEntities.required(User_.version),
                    User_.name, 
                    User_.email,
                    User_.imageMimeType,
                    User_.image,
                    Customer_.phone,
                    Customer_.address);
        } else {
            JPAEntities.validateMaxEnabledRange(
                    currentUser,
                    JPAEntities.required(User_.version),
                    User_.name, 
                    User_.email,
                    User_.imageMimeType,
                    User_.image);
        }
        
        if (currentUser.getId() != null) {
            if (!currentUser.getId().equals(this.currentUserId)) {
                throw new IllegalArgumentException("Illegal id of currentUser");
            }
        } else {
            currentUser.setId(this.currentUserId);
        }
        
        if (JPAEntities.isEnabled(currentUser, User_.name)) {
            User conflict = this.userRepository.getUserByNameInsensitively(currentUser.getName());
            if (conflict != null && !JPAEntities.isIdEquals(currentUser, conflict)) {
                throw new AuthorizationException(
                        "Can not register the user becuase the name \"" +
                        currentUser.getName() +
                        "\" is already existing");
            }
        }
        
        this.userRepository.mergeEntity(currentUser);
    }

    @Transactional
    @Override
    public void changePassword(User currentUser, String originalPassword, String newPassword) {
        if (this.currentUserId == null) {
            throw new AuthorizationException("You have not logined");
        }
        if (!Nulls.equals(this.currentUserId, currentUser.getId())) {
            throw new IllegalArgumentException("Invalid id of current user");
        }
        JPAEntities.validateMaxEnabledRange(
                currentUser, 
                JPAEntities.required(User_.version), 
                JPAEntities.required(User_.password)
        );
        if (!Arrays.equals(currentUser.getPassword(), toBinaryPassword(originalPassword))) {
            throw new IllegalArgumentException("The orignial password is not concurrent");
        }
        currentUser.setPassword(toBinaryPassword(newPassword));
        this.userRepository.mergeEntity(currentUser);
    }

    @Transactional
    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Override
    public void updateRole(Long id, Integer version, String name, Collection<Long> privilegeIds) {
        Role role;
        if (id == null) {
            role = new Role();
        } else {
            role = this.roleRepository.getRoleById(id, Role__.begin().privileges().end());
            if (role == null || (version != null && version.intValue() != role.getVersion())) {
                throw new OptimisticLockException();
            }
            role.getPrivileges().clear();
        }
        Role conflictRole = this.roleRepository.getRoleByNameInsensitively(name);
        if (conflictRole != null && conflictRole != role) {
            throw new IllegalArgumentException(
                    "The user whose name is \"" +
                    name +
                    "\" is already exists");
        }
        
        role.setName(name.toLowerCase());
        
        if (!Nulls.isNullOrEmpty(privilegeIds)) {
            role.getPrivileges().addAll(this.privilegeRepository.getPrivilegesByIds(privilegeIds));
        }
        
        this.roleRepository.mergeEntity(role);
    }

    @Transactional
    @Override
    public void deleteRole(long id) {
        Role role = this.roleRepository.getRoleById(id);
        if (role != null) {
            this.roleRepository.removeEntity(role);
        }
    }

    private static byte[] toBinaryPassword(String password) throws AssertionError {
        byte[] passwordBytes;
        try {
            passwordBytes = MessageDigest.getInstance("SHA-1").digest(password.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            log.fatal("No SHA-1 algorithm");
            throw new AssertionError(ex);
        }
        return passwordBytes;
    }
    
    private boolean checkPrivilege(Administrator administrator, String privilegeName) {
        if (!Persistence.getPersistenceUtil().isLoaded(administrator.getPrivileges())) {
            throw new AssertionError("Internal bug");
        }
        for (Privilege privilege : administrator.getPrivileges()) {
            if (privilegeName.equals(privilege.getName())) {
                return true;
            }
        }
        if (!Persistence.getPersistenceUtil().isLoaded(administrator.getRoles())) {
            throw new AssertionError("Internal bug");
        }
        Collection<Role> roles = null;
        for (Role role : administrator.getRoles()) {
            if (JPAEntities.isDisabled(role, Role_.privileges) ||
                    !Persistence.getPersistenceUtil().isLoaded(role.getPrivileges())) {
                roles = this.roleRepository.getRolesByIds(
                        JPAEntities.extractAttribute(administrator.getRoles(), Role_.id), 
                        Role__.begin().privileges().end()
                ); 
                break;
            }
        }
        if (roles == null) {
            roles = administrator.getRoles();
        }
        for (Role role : roles) {
            for (Privilege privilege : role.getPrivileges()) {
                if (privilegeName.equals(privilege.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
