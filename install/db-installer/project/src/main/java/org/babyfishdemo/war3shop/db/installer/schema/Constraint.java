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
package org.babyfishdemo.war3shop.db.installer.schema;

import java.util.Collections;
import java.util.List;

/**
 * @author Tao Chen
 */
public abstract class Constraint {

    private Table table;
    
    private List<Column> columns;
    
    Constraint(List<Column> columns) {
        this.table = validate(columns, "columns");
        this.columns = Collections.unmodifiableList(columns);
    }
    
    public Table getTable() {
        return this.table;
    }
    
    public List<Column> getColumns() {
        return this.columns;
    }
    
    static Table validate(List<Column> columns, String paramterName) {
        if (columns == null) {
            throw new IllegalArgumentException("The \"" + paramterName + "\" must not be null");
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("The \"" + paramterName + "\" must not be empty");
        }
        Table table = null;
        for (Column column : columns) {
            if (table == null) {
                table = column.getTable();
            } else if (column.getTable() != table) {
                throw new IllegalArgumentException("All the columns \"" + paramterName + "\" must belong to one table");                
            }
        }
        return table;
    }
}
