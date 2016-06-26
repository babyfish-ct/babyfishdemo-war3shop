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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "MANUFACTURER")
@SequenceGenerator(
        name = "manufacturerSequence",
        sequenceName = "MANUFACTURER_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Manufacturer {

    @Id
    @Column(name = "MANUFACTURER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manufacturerSequence")
    private Long id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;

    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "RACE", nullable = false)
    private Race race;

    @Column(name = "EMAIL", length = 50, nullable = false)
    private String email;

    @Column(name = "PHONE", length = 50, nullable = false)
    private String phone;
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "DESCRIPTION", columnDefinition = "CLOB", nullable = false)
    private String description;

    @Column(name = "IMAGE_MIME_TYPE", length = 20, nullable = false)
    private String imageMimeType;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "IMAGE", nullable = false)
    private byte[] image;
    
    @ManyToMany(mappedBy = "manufacturers")
    private Set<Product> products;

    @ManyToMany
    @JoinTable(
            name = "MANUFACTURER_PURCHASER_MAPPING",
            joinColumns = @JoinColumn(name = "MANUFACTURER_ID"),
            inverseJoinColumns = @JoinColumn(name = "PURCHASER_ID")
    )
    private Set<Administrator> purchasers;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<Administrator> getPurchasers() {
        return purchasers;
    }

    public void setPurchasers(Set<Administrator> purchasers) {
        this.purchasers = purchasers;
    }
}
