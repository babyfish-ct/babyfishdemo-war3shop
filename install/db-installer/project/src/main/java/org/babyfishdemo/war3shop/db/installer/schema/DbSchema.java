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

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Tao Chen
 */
public class DbSchema {
    
    private Map<String, Table> tables;
    
    public DbSchema(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory()) {
            throw new IllegalArgumentException("'" + dir + "'" + "is not directory");
        }
        File[] subFiles = dirFile.listFiles();
        Map<String, Table> map = new LinkedHashMap<>((subFiles.length * 4 + 2) / 3);
        for (File subFile : subFiles) {
            if (subFile.isFile() && subFile.getName().endsWith(".xml")) {
                Table table = new Table(subFile);
                if (table.getName() == null) {
                    throw new SchemaException(
                            "The xml \""
                            + subFile.getAbsolutePath()
                            + "\" is invalid, it does not contain <table xmlns='"
                            + Table.XMLNS
                            + "'/>");
                }
                Table conflictTable = map.put(table.getName(), table);
                if (conflictTable != null) {
                    throw new SchemaException(
                            "Duplicated tables \""
                            + table.getName()
                            + "\" are declared in \""
                            + conflictTable.getXmlFile().getAbsolutePath()
                            + "\" and \""
                            + table.getXmlFile().getAbsolutePath()
                            + "\"");
                }
            }
        }
        this.tables = Collections.unmodifiableMap(map);
        for (Table table : map.values()) {
            table.resolveForeignKeys(this);
        }
    }

    public Map<String, Table> getTables() {
        return this.tables;
    }
}
