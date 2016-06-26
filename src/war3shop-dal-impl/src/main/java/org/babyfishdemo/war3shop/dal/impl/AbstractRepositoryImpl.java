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
package org.babyfishdemo.war3shop.dal.impl;

import java.lang.reflect.ParameterizedType;

import javax.persistence.PersistenceContext;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.util.reflect.ClassInfo;
import org.babyfish.util.reflect.GenericTypes;
import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.war3shop.dal.AbstractRepository;

/**
 * @author Tao Chen
 */
public abstract class AbstractRepositoryImpl<E, I> implements AbstractRepository<E, I> {

    @PersistenceContext
    protected XEntityManager em;
    
    protected Class<E> entityType;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected AbstractRepositoryImpl() {
        Class<?> thisClass = this.getClass();
        if (thisClass.getTypeParameters().length != 0) {
            throw new IllegalProgramException(
                    "The class \"" +
                    thisClass.getName() +
                    "\" must contain no type parameter");
        }
        ParameterizedType parameterizedType = (ParameterizedType)
                ClassInfo
                .of(thisClass)
                .getAncestor(AbstractRepositoryImpl.class)
                .getRawType();
        this.entityType = (Class)GenericTypes.eraseGenericType(parameterizedType.getActualTypeArguments()[0]);
    }

    @Override
    public E mergeEntity(E entity) {
        return this.em.merge(entity);
    }

    @Override
    public void removeEntity(E entity) {
        this.em.remove(entity);
    }

    @Override
    public boolean removeEntityById(I id) {
        if (id != null) {
            E entity = this.em.find(this.entityType, id);
            if (entity != null) {
                this.em.remove(entity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void flush() {
        this.em.flush();
    }
}
