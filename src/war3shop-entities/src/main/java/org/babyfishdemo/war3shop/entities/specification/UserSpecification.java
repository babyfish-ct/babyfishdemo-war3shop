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

/**
 * @author Tao Chen
 */
public abstract class UserSpecification {
    
    private Collection<Long> ids;
    
    private Boolean active;

    private String likeName;
    
    private String likeEmail;
    
    private Date minCreationTime;
    
    private Date maxCreationTime;

    public Collection<Long> getIds() {
        return ids;
    }

    public void setIds(Collection<Long> ids) {
        this.ids = ids;
    }

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

    public String getLikeEmail() {
        return likeEmail;
    }

    public void setLikeEmail(String likeEmail) {
        this.likeEmail = likeEmail;
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
}
