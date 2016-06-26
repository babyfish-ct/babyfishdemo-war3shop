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
import java.util.Date;

import org.babyfishdemo.war3shop.entities.PreferentialActionType;
import org.babyfishdemo.war3shop.entities.PreferentialThresholdType;

/**
 * @author Tao Chen
 */
public class PreferentialSpecification {
    
    private Boolean active;
    
    private Date minDate;
    
    private Date maxDate;
    
    private Collection<PreferentialThresholdType> thresholdTypes;
    
    private Collection<PreferentialActionType> actionTypes;
    
    private Collection<Long> includedProductIds;
    
    private Collection<Long> excludedProductIds;
    
    private Boolean hasGiftProducts;
    
    private Collection<Long> includedGiftProductIds;
    
    private Collection<Long> excludedGiftProductIds;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    public Collection<PreferentialThresholdType> getThresholdTypes() {
        return thresholdTypes;
    }

    public void setThresholdTypes(Collection<PreferentialThresholdType> thresholdTypes) {
        this.thresholdTypes = thresholdTypes;
    }

    public Collection<PreferentialActionType> getActionTypes() {
        return actionTypes;
    }

    public void setActionTypes(Collection<PreferentialActionType> actionTypes) {
        this.actionTypes = actionTypes;
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

    public Boolean getHasGiftProducts() {
        return hasGiftProducts;
    }

    public void setHasGiftProducts(Boolean hasGiftProducts) {
        this.hasGiftProducts = hasGiftProducts;
    }

    public Collection<Long> getIncludedGiftProductIds() {
        return includedGiftProductIds;
    }

    public void setIncludedGiftProductIds(Collection<Long> includedGiftProductIds) {
        this.includedGiftProductIds = includedGiftProductIds;
    }

    public Collection<Long> getExcludedGiftProductIds() {
        return excludedGiftProductIds;
    }

    public void setExcludedGiftProductIds(Collection<Long> excludedGiftProductIds) {
        this.excludedGiftProductIds = excludedGiftProductIds;
    }
}
