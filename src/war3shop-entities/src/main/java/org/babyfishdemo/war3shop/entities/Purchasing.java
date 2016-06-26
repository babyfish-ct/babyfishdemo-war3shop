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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "PURCHASING")
@SequenceGenerator(
        name = "purchasingSequence",
        sequenceName = "PURCHASING_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Purchasing {

    @Id
    @Column(name = "PURCHASING_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchasingSequence")
    private Long id;

    @Column(name = "CREATION_TIME")
    private Date creationTime;

    // Redundant field for performance optimization
    @Column(name = "TOTAL_PURCHASED_PRICE", nullable = false)
    private BigDecimal totalPurchasedPrice;

    @OneToMany(mappedBy = "purchasing", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<PurchasingItem> items;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PURCHASER_ID")
    private Administrator purchaser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public BigDecimal getTotalPurchasedPrice() {
        return totalPurchasedPrice;
    }

    public void setTotalPurchasedPrice(BigDecimal totalPurchasedPrice) {
        this.totalPurchasedPrice = totalPurchasedPrice;
    }

    public Set<PurchasingItem> getItems() {
        return items;
    }

    public void setItems(Set<PurchasingItem> items) {
        this.items = items;
    }

    public Administrator getPurchaser() {
        return purchaser;
    }

    public void setPurchaser(Administrator purchaser) {
        this.purchaser = purchaser;
    }
}
