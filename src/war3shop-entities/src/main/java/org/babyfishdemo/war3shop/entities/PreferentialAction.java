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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "PREFERENTIAL_ACTION")
@SequenceGenerator(
        name = "preferentialActionSequence",
        sequenceName = "PREFERENTIAL_ACTION_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class PreferentialAction {

    @Id
    @Column(name = "PREFERENTIAL_ACTION_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preferentialActionSequence")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ITEM_ID", nullable = false)
    private OrderItem orderItem;
    
    @Column(name = "MATCHED_COUNT")
    private int matchedCount;
    
    @Column(name = "THRESHOLD_TYPE", nullable = false)
    private PreferentialThresholdType thresholdType;
    
    @Column(name = "ACTION_TYPE", nullable = false)
    private PreferentialActionType actionType;
    
    @Column(name = "THRESHOLD_QUANTITY")
    private Integer thresholdQuantity;

    @Column(name = "THRESHOLD_MONEY")
    private BigDecimal thresholdMoney;

    @Column(name = "REDUCED_MONEY")
    private BigDecimal reducedMoney;

    @Column(name = "PERCENTAGE_FACTOR")
    private Integer percentageFactor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GIFT_PRODUCT_ID")
    private Product giftProduct;

    @Column(name = "GIFT_QUANTITY")
    private Integer giftQuantity;
    
    @Embedded
    private TotalPreferential totalPreferential;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public PreferentialThresholdType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(PreferentialThresholdType thresholdType) {
        this.thresholdType = thresholdType;
    }

    public PreferentialActionType getActionType() {
        return actionType;
    }

    public void setActionType(PreferentialActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getThresholdQuantity() {
        return thresholdQuantity;
    }

    public void setThresholdQuantity(Integer thresholdQuantity) {
        this.thresholdQuantity = thresholdQuantity;
    }

    public BigDecimal getThresholdMoney() {
        return thresholdMoney;
    }

    public void setThresholdMoney(BigDecimal thresholdMoney) {
        this.thresholdMoney = thresholdMoney;
    }

    public BigDecimal getReducedMoney() {
        return reducedMoney;
    }

    public void setReducedMoney(BigDecimal reducedMoney) {
        this.reducedMoney = reducedMoney;
    }

    public Integer getPercentageFactor() {
        return percentageFactor;
    }

    public void setPercentageFactor(Integer percentageFactor) {
        this.percentageFactor = percentageFactor;
    }

    public Product getGiftProduct() {
        return giftProduct;
    }

    public void setGiftProduct(Product giftProduct) {
        this.giftProduct = giftProduct;
    }

    public Integer getGiftQuantity() {
        return giftQuantity;
    }

    public void setGiftQuantity(Integer giftQuantity) {
        this.giftQuantity = giftQuantity;
    }

    public TotalPreferential getTotalPreferential() {
        return totalPreferential;
    }

    public void setTotalPreferential(TotalPreferential totalPreferential) {
        this.totalPreferential = totalPreferential;
    }
}
