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

/**
 * @author Tao Chen
 */
public class OrderSpecification {
    
    private Boolean delivered;
    
    private BigDecimal minActualMoney;

    private BigDecimal maxActualMoney;

    private Date minCreationTime;

    private Date maxCreationTime;

    private Boolean assigned;

    private Date minDeliveredTime;
    
    private Date maxDeliveredTime;
    
    private Collection<Long> includedCustomerIds;

    private Collection<Long> excludedCustomerIds;

    private Collection<Long> includedProductIds;

    private Collection<Long> excludedProductIds;
    
    private Boolean hasDeliverymans;
    
    private Collection<Long> includedDeliverymanIds;

    private Collection<Long> excludedDeliverymanIds;

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public BigDecimal getMinActualMoney() {
        return minActualMoney;
    }

    public void setMinActualMoney(BigDecimal minMoney) {
        this.minActualMoney = minMoney;
    }

    public BigDecimal getMaxActualMoney() {
        return maxActualMoney;
    }

    public void setMaxActualMoney(BigDecimal maxMoney) {
        this.maxActualMoney = maxMoney;
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

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public Date getMinDeliveredTime() {
        return minDeliveredTime;
    }

    public void setMinDeliveredTime(Date minDeliveredTime) {
        this.minDeliveredTime = minDeliveredTime;
    }

    public Date getMaxDeliveredTime() {
        return maxDeliveredTime;
    }

    public void setMaxDeliveredTime(Date maxDeliveredTime) {
        this.maxDeliveredTime = maxDeliveredTime;
    }

    public Collection<Long> getIncludedCustomerIds() {
        return includedCustomerIds;
    }

    public void setIncludedCustomerIds(Collection<Long> includedCustomerIds) {
        this.includedCustomerIds = includedCustomerIds;
    }

    public Collection<Long> getExcludedCustomerIds() {
        return excludedCustomerIds;
    }

    public void setExcludedCustomerIds(Collection<Long> excludedCustomerIds) {
        this.excludedCustomerIds = excludedCustomerIds;
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

    public Boolean getHasDeliverymans() {
        return hasDeliverymans;
    }

    public void setHasDeliverymans(Boolean hasDeliverymans) {
        this.hasDeliverymans = hasDeliverymans;
    }

    public Collection<Long> getIncludedDeliverymanIds() {
        return includedDeliverymanIds;
    }

    public void setIncludedDeliverymanIds(Collection<Long> includedDeliverymanIds) {
        this.includedDeliverymanIds = includedDeliverymanIds;
    }

    public Collection<Long> getExcludedDeliverymanIds() {
        return excludedDeliverymanIds;
    }

    public void setExcludedDeliverymanIds(Collection<Long> excludedDeliverymanIds) {
        this.excludedDeliverymanIds = excludedDeliverymanIds;
    }
}
