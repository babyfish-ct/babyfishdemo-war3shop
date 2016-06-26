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
import javax.persistence.Embeddable;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Embeddable
public class TotalPreferential {

    @Column(name = "TOTAL_REDUCED_MONEY", nullable = false)
    private BigDecimal reducedMoney;
    
    @Column(name = "TOTAL_GIFT_MONEY", nullable = false)
    private BigDecimal giftMoney;
    
    public TotalPreferential() {
        this.reducedMoney = BigDecimal.ZERO;
        this.giftMoney = BigDecimal.ZERO;
    }

    public BigDecimal getReducedMoney() {
        return reducedMoney;
    }

    public void setReducedMoney(BigDecimal reducedMoney) {
        this.reducedMoney = reducedMoney;
    }

    public BigDecimal getGiftMoney() {
        return giftMoney;
    }

    public void setGiftMoney(BigDecimal giftMoney) {
        this.giftMoney = giftMoney;
    }
}
