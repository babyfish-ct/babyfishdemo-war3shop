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
package org.babyfishdemo.war3shop.entities.specification;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.babyfishdemo.war3shop.entities.ProductType;
import org.babyfishdemo.war3shop.entities.Race;

/**
 * @author Tao Chen
 */
public class ProductSpecification {

    private Boolean active;
    
    private String likeName;
    
    private Date minCreationTime;
    
    private Date maxCreationTime;
    
    private BigDecimal minPrice;
    
    private BigDecimal maxPrice;

    private Collection<ProductType> types;
    
    private Collection<Race> races;
    
    private Integer minInventory;
    
    private Integer maxInventory;
    
    private Boolean hasManufacturers;
    
    private Collection<Long> includedManufacturerIds;

    private Collection<Long> excludedManufacturerIds;
    
    private Boolean hasPurchasers;
    
    private Collection<Long> includedPurchaserIds;
    
    private Collection<Long> excludedPurchaserIds;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getLikeName() {
        return likeName;
    }

    public void setLikeName(String likeName) {
        this.likeName = likeName;
    }

    public Date getMinCreationTime() {
        return minCreationTime;
    }

    public void setMinCreationTime(Date minCreationTime) {
        this.minCreationTime = minCreationTime;
    }

    public Date getMaxCreationTime() {
        return maxCreationTime;
    }

    public void setMaxCreationTime(Date maxCreationTime) {
        this.maxCreationTime = maxCreationTime;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Collection<ProductType> getTypes() {
        return types;
    }

    public void setTypes(Collection<ProductType> types) {
        this.types = types;
    }

    public Collection<Race> getRaces() {
        return races;
    }

    public void setRaces(Collection<Race> races) {
        this.races = races;
    }

    public Integer getMinInventory() {
        return minInventory;
    }

    public void setMinInventory(Integer minInventory) {
        this.minInventory = minInventory;
    }

    public Integer getMaxInventory() {
        return maxInventory;
    }

    public void setMaxInventory(Integer maxInventory) {
        this.maxInventory = maxInventory;
    }

    public Boolean getHasManufacturers() {
        return hasManufacturers;
    }

    public void setHasManufacturers(Boolean hasManufacturers) {
        this.hasManufacturers = hasManufacturers;
    }

    public Collection<Long> getIncludedManufacturerIds() {
        return includedManufacturerIds;
    }

    public void setIncludedManufacturerIds(Collection<Long> includedManufacturerIds) {
        this.includedManufacturerIds = includedManufacturerIds;
    }

    public Collection<Long> getExcludedManufacturerIds() {
        return excludedManufacturerIds;
    }

    public void setExcludedManufacturerIds(Collection<Long> excludedManufacturerIds) {
        this.excludedManufacturerIds = excludedManufacturerIds;
    }

    public Boolean getHasPurchasers() {
        return hasPurchasers;
    }

    public void setHasPurchasers(Boolean hasPurchasers) {
        this.hasPurchasers = hasPurchasers;
    }

    public Collection<Long> getIncludedPurchaserIds() {
        return includedPurchaserIds;
    }

    public void setIncludedPurchaserIds(Collection<Long> includedPurchaserIds) {
        this.includedPurchaserIds = includedPurchaserIds;
    }

    public Collection<Long> getExcludedPurchaserIds() {
        return excludedPurchaserIds;
    }

    public void setExcludedPurchaserIds(Collection<Long> excludedPurchaserIds) {
        this.excludedPurchaserIds = excludedPurchaserIds;
    }
}
