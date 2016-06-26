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

import java.util.Collection;

import org.babyfishdemo.war3shop.entities.Race;

/**
 * @author Tao Chen
 */
public class ManufacturerSpecification {

    private String likeName;
    
    private String likeEmail;
    
    private String likePhone;
    
    private Collection<Race> races;
    
    private Boolean hasProducts;

    private Collection<Long> includedProductIds;
    
    private Collection<Long> excludedProductIds;
    
    private Boolean hasPurchasers;
    
    private Collection<Long> includedPurchaserIds;
    
    private Collection<Long> excludedPurchaserIds;

    public String getLikeName() {
        return likeName;
    }

    public void setLikeName(String likeName) {
        this.likeName = likeName;
    }

    public String getLikeEmail() {
        return likeEmail;
    }

    public void setLikeEmail(String likeEmail) {
        this.likeEmail = likeEmail;
    }

    public String getLikePhone() {
        return likePhone;
    }

    public void setLikePhone(String likePhone) {
        this.likePhone = likePhone;
    }

    public Collection<Race> getRaces() {
        return races;
    }

    public void setRaces(Collection<Race> races) {
        this.races = races;
    }

    public Boolean getHasProducts() {
        return hasProducts;
    }

    public void setHasProducts(Boolean hasProducts) {
        this.hasProducts = hasProducts;
    }

    public Collection<Long> getIncludedProductIds() {
        return includedProductIds;
    }

    public void setIncludedProductIds(Collection<Long> includedProductIds) {
        this.includedProductIds = includedProductIds;
    }

    public Collection<Long> getExcludedProductIds() {
        return excludedProductIds;
    }

    public void setExcludedProductIds(Collection<Long> excludedProductIds) {
        this.excludedProductIds = excludedProductIds;
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
