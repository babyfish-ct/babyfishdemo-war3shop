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
public class ForeignKeyConstraint extends Constraint {

    private Table referencedTable;
    
    private List<Column> referencedColumns;
    
    ForeignKeyConstraint(List<Column> columns, List<Column> referencedColumns) {
        super(columns);
        if (columns.size() != referencedColumns.size()) {
            throw new IllegalArgumentException("The size of columns and referencedColumns must be equal");
        }
        this.referencedTable = validate(referencedColumns, "referencedColumns");
        this.referencedColumns = Collections.unmodifiableList(referencedColumns);
    }

    public Table getReferencedTable() {
        return this.referencedTable;
    }
    
    public List<Column> getReferencedColumns() {
        return this.referencedColumns;
    }
}
