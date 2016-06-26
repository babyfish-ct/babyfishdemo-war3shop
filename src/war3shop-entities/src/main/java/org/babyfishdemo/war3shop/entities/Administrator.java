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
package org.babyfishdemo.war3shop.entities;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@DiscriminatorValue("1")
public class Administrator extends User {

    @ManyToMany
    @JoinTable(
            name = "ADMINISTRATOR_ROLE_MAPPING",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID")
    )
    private Set<Role> roles;
    
    @ManyToMany
    @JoinTable(
            /*
             * The table name should be "ADMINISTRATOR_PRIVILEGE_MAPPING",
             * but the identifier length of oracle must <= 30,
             * so change the table name to be "ADMIN_PRIVILEGE_MAPPING".
             * T_T
             */
            name = "ADMIN_PRIVILEGE_MAPPING",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "PRIVILEGE_ID")
    )
    private Set<Privilege> privileges;
    
    @ManyToMany(mappedBy = "purchasers")
    private Set<Manufacturer> purchasedManufacturers;

    @ManyToMany(mappedBy = "purchasers")
    private Set<Product> purchasedProducts;
    
    @OneToMany(mappedBy = "purchaser")
    private Set<Purchasing> purchasings;
    
    @ManyToMany(mappedBy = "deliveryman")
    private Set<Order> deliveredOrders;

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

    public Set<Manufacturer> getPurchasedManufacturers() {
        return purchasedManufacturers;
    }

    public void setPurchasedManufacturers(Set<Manufacturer> purchasedManufacturers) {
        this.purchasedManufacturers = purchasedManufacturers;
    }

    public Set<Product> getPurchasedProducts() {
        return purchasedProducts;
    }

    public void setPurchasedProducts(Set<Product> purchasedProducts) {
        this.purchasedProducts = purchasedProducts;
    }

    public Set<Purchasing> getPurchasings() {
        return purchasings;
    }

    public void setPurchasings(Set<Purchasing> purchasings) {
        this.purchasings = purchasings;
    }

    public Set<Order> getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(Set<Order> deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }
}
