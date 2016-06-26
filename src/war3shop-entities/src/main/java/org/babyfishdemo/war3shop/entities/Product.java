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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Table(name = "PRODUCT")
@SequenceGenerator(
        name = "productSequence",
        sequenceName = "PRODUCT_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Product {
    
    @Id
    @Column(name = "PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productSequence")
    private Long id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "TYPE", nullable = false)
    private ProductType type;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "RACE", nullable = false)
    private Race race;

    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private String name;
    
    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;
    
    @Column(name = "CREATION_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "IMAGE_MIME_TYPE", length = 20, nullable = false)
    private String imageMimeType;
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "IMAGE", nullable = false)
    private byte[] image;
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;
    
    @OneToMany(mappedBy = "product")
    private Set<OrderItem> orderItems; 
    
    @OneToMany(mappedBy = "product")
    private Set<GiftItem> giftItems;

    @ManyToMany
    @JoinTable(
            name = "PRODUCT_MANUFACTURER_MAPPING",
            joinColumns = @JoinColumn(name = "PRODUCT_ID"),
            inverseJoinColumns = @JoinColumn(name = "MANUFACTURER_ID")
    )
    private Set<Manufacturer> manufacturers;
    
    @ManyToMany
    @JoinTable(
            name = "PRODUCT_PURCHASER_MAPPING",
            joinColumns = @JoinColumn(name = "PRODUCT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PURCHASER_ID")
    )
    private Set<Administrator> purchasers;

    @OneToOne(
            mappedBy = "product", 
            optional = true,
            
            // Emphasize the fetch is EAGER!
            // For hibernate, optional="true" means FetchType.LAZY will be ignored. T_T
            fetch = FetchType.EAGER 
    )
    private Inventory inventory;
    
    @OneToMany(mappedBy = "product")
    private Set<PurchasingItem> purchasingItems;

    @OneToMany(mappedBy = "product")
    private Set<Preferential> preferentials;

    @OneToMany(mappedBy = "giftProduct")
    private Set<PreferentialItem> preferentialItemsThatUseMeToBeGift;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Manufacturer> getManufacturers() {
        return manufacturers;
    }

    public void setManufacturers(Set<Manufacturer> manufacturers) {
        this.manufacturers = manufacturers;
    }

    public Set<Administrator> getPurchasers() {
        return purchasers;
    }

    public void setPurchasers(Set<Administrator> purchasers) {
        this.purchasers = purchasers;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Set<PurchasingItem> getPurchasingItems() {
        return purchasingItems;
    }

    public void setPurchasingItems(Set<PurchasingItem> purchasingItems) {
        this.purchasingItems = purchasingItems;
    }

    public Set<Preferential> getPreferentials() {
        return preferentials;
    }

    public void setPreferentials(Set<Preferential> preferentials) {
        this.preferentials = preferentials;
    }

    public Set<PreferentialItem> getPreferentialItemsThatUseMeToBeGift() {
        return preferentialItemsThatUseMeToBeGift;
    }

    public void setPreferentialItemsThatUseMeToBeGift(
            Set<PreferentialItem> preferentialItemsThatUseMeToBeGift) {
        this.preferentialItemsThatUseMeToBeGift = preferentialItemsThatUseMeToBeGift;
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
}
