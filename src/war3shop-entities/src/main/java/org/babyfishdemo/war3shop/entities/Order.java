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

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "\"ORDER\"")
@SequenceGenerator(
        name = "orderSequence",
        sequenceName = "ORDER_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Order {
    
    @Id
    @Column(name = "ORDER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderSequence")
    private Long id;
    
    @Version
    @Column(name = "VERSION")
    private int version;
    
    /**
     * <ul>
     *    <li>
     *    If this field is not null, it means Temporary Order(You can call it Cart too).
     *    if the client-side crash, temporary orders will be deleted by GC service.
     *  </li>
     *  <li>Otherwise, it means Formal order, it must be deleted manually.</li>
     * </ul>
     */
    @Column(name = "GC_THRESHOLD")
    @Temporal(TemporalType.TIMESTAMP)
    private Date gcThreshold;
    
    @Column(name = "CREATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    
    @Column(name = "DELIVERED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DELIVERYMAN_ID")
    private Administrator deliveryman;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GiftItem> giftItems;
    
    @Embedded
    private TotalMoney totalMoney;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "address", length = 100)
    private String address;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getGcThreshold() {
        return gcThreshold;
    }

    public void setGcThreshold(Date gcThreshold) {
        this.gcThreshold = gcThreshold;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(Date deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Administrator getDeliveryman() {
        return deliveryman;
    }

    public void setDeliveryman(Administrator deliveryman) {
        this.deliveryman = deliveryman;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public Set<GiftItem> getGiftItems() {
        return giftItems;
    }

    public void setGiftItems(Set<GiftItem> giftItems) {
        this.giftItems = giftItems;
    }

    public TotalMoney getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(TotalMoney totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
