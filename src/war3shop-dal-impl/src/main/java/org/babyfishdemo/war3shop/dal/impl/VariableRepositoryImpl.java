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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.babyfishdemo.war3shop.dal.VariableRepository;
import org.babyfishdemo.war3shop.entities.Variable;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class VariableRepositoryImpl 
extends AbstractRepositoryImpl<Variable, String> 
implements VariableRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Variable getVariableByName(String variableName) {
        return this.em.find(Variable.class, variableName);
    }
}
