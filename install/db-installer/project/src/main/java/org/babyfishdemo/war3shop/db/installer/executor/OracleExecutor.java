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
package org.babyfishdemo.war3shop.db.installer.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.babyfishdemo.war3shop.db.installer.schema.Table;
import org.babyfishdemo.war3shop.db.installer.schema.Type;

/**
 * @author Tao Chen
 */
public class OracleExecutor extends Executor {
    
    private PreparedStatement findTableStatement;
    
    private PreparedStatement findSequenceStatement;

    public OracleExecutor(String dir, DataSource dataSource) {
        super(dir, dataSource);
    }

    @Override
    protected String mapType(Type type) {
        switch (type) {
        case BOOLEAN:
            return "NUMERIC(1)";
        case INT:
            return "NUMERIC";
        case LONG:
            return "NUMERIC";
        case DECIMAL:
            return "NUMERIC(38, 2)";
        case DATE:
            return "DATE";
        case TIMESTAMP:
            return "DATE";
        case STRING:
            return "VARCHAR2";
        case BINARY:
            return "RAW";
        case CLOB:
            return "CLOB";
        case BLOB:
            return "BLOB";
        }
        throw new IllegalArgumentException("Invalid argument");
    }

    @Override
    protected void dropObjectsIfExsits(Table table) throws SQLException {
        boolean dropTable = false;
        this.findTableStatement.setString(1, table.getName());
        try (ResultSet rs = this.findTableStatement.executeQuery()) {
            dropTable = rs.next();
        }
        if (dropTable) {
            this.executeDDL("DROP TABLE " + this.getObjectName(table.getName()));
        } else {
            System.out.println(
                    "The table \""
                    + table.getName()
                    + "\" is not existing, need not drop");
        }
        
        if (table.getSequenceName() != null) {
            boolean dropSequence = false;
            this.findSequenceStatement.setString(1,  table.getSequenceName());
                try (ResultSet rs = this.findSequenceStatement.executeQuery()) {
                    dropSequence = rs.next();
                }
            if (dropSequence) {
                this.executeDDL("DROP SEQUENCE" + this.getObjectName(table.getSequenceName()));
            } else {
                System.out.println(
                        "The sequence \""
                        + table.getSequenceName()
                        + "\" is not existing, need not drop");
            }
        }
    }

    @Override
    protected void postConnect(Connection con) throws SQLException {
        this.findTableStatement = con.prepareStatement("SELECT NULL FROM USER_TABLES WHERE TABLE_NAME = ?");
        this.findSequenceStatement = con.prepareStatement("SELECT NULL FROM USER_SEQUENCES WHERE SEQUENCE_NAME = ?");
    }

    @Override
    protected void preUnconnect(Connection con) throws SQLException {
        PreparedStatement[] arr = new PreparedStatement[] { this.findTableStatement, this.findSequenceStatement };
        this.findTableStatement = null;
        this.findSequenceStatement = null;
        for (PreparedStatement ps : arr) {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
