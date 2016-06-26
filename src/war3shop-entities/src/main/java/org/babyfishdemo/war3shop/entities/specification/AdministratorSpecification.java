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

/**
 * @author Tao Chen
 */
public class AdministratorSpecification extends UserSpecification {
    
    private Collection<Long> includedRoleIds;
    
    private Collection<Long> excludedRoleIds;
    
    private Collection<Long> includedPrivilegeIds;
    
    private Collection<Long> excludedPrivilegeIds;
    
    private Collection<String> includedRoleNames;
    
    private Collection<String> excludedRoleNames;
    
    private Collection<String> includedPrivilegeNames;
    
    private Collection<String> excludedPrivilegeNames;

    private Collection<Long> includedPurchasedManufacturerIds;
    
    private Collection<Long> excludedPurchasedManufacturerIds;
    
    private Collection<Long> includedPurchasedProductIds;
    
    private Collection<Long> excludedPurchasedProductIds;

    public Collection<Long> getIncludedRoleIds() {
        return includedRoleIds;
    }

    public void setIncludedRoleIds(Collection<Long> includedRoleIds) {
        this.includedRoleIds = includedRoleIds;
    }

    public Collection<Long> getExcludedRoleIds() {
        return excludedRoleIds;
    }

    public void setExcludedRoleIds(Collection<Long> excludedRoleIds) {
        this.excludedRoleIds = excludedRoleIds;
    }

    public Collection<Long> getIncludedPrivilegeIds() {
        return includedPrivilegeIds;
    }

    public void setIncludedPrivilegeIds(Collection<Long> includedPrivilegeIds) {
        this.includedPrivilegeIds = includedPrivilegeIds;
    }

    public Collection<Long> getExcludedPrivilegeIds() {
        return excludedPrivilegeIds;
    }

    public void setExcludedPrivilegeIds(Collection<Long> excludedPrivilegeIds) {
        this.excludedPrivilegeIds = excludedPrivilegeIds;
    }

    public Collection<String> getIncludedRoleNames() {
        return includedRoleNames;
    }

    public void setIncludedRoleNames(Collection<String> includedRoleNames) {
        this.includedRoleNames = includedRoleNames;
    }

    public Collection<String> getExcludedRoleNames() {
        return excludedRoleNames;
    }

    public void setExcludedRoleNames(Collection<String> excludedRoleNames) {
        this.excludedRoleNames = excludedRoleNames;
    }

    public Collection<String> getIncludedPrivilegeNames() {
        return includedPrivilegeNames;
    }

    public void setIncludedPrivilegeNames(Collection<String> includedPrivilegeNames) {
        this.includedPrivilegeNames = includedPrivilegeNames;
    }

    public Collection<String> getExcludedPrivilegeNames() {
        return excludedPrivilegeNames;
    }

    public void setExcludedPrivilegeNames(Collection<String> excludedPrivilegeNames) {
        this.excludedPrivilegeNames = excludedPrivilegeNames;
    }

    public Collection<Long> getIncludedPurchasedManufacturerIds() {
        return includedPurchasedManufacturerIds;
    }

    public void setIncludedPurchasedManufacturerIds(
            Collection<Long> includedPurchasedManufacturerIds) {
        this.includedPurchasedManufacturerIds = includedPurchasedManufacturerIds;
    }

    public Collection<Long> getExcludedPurchasedManufacturerIds() {
        return excludedPurchasedManufacturerIds;
    }

    public void setExcludedPurchasedManufacturerIds(Collection<Long> excludedPurchasedManufacturerIds) {
        this.excludedPurchasedManufacturerIds = excludedPurchasedManufacturerIds;
    }

    public Collection<Long> getIncludedPurchasedProductIds() {
        return includedPurchasedProductIds;
    }

    public void setIncludedPurchasedProductIds(
            Collection<Long> includedPurchasedProductIds) {
        this.includedPurchasedProductIds = includedPurchasedProductIds;
    }

    public Collection<Long> getExcludedPurchasedProductIds() {
        return excludedPurchasedProductIds;
    }

    public void setExcludedPurchasedProductIds(Collection<Long> excludedPurchasedProductIds) {
        this.excludedPurchasedProductIds = excludedPurchasedProductIds;
    }
}
