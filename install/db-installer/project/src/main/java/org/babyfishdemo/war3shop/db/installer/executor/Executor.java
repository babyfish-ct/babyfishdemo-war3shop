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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.babyfishdemo.war3shop.db.installer.schema.Column;
import org.babyfishdemo.war3shop.db.installer.schema.DataVisitingMode;
import org.babyfishdemo.war3shop.db.installer.schema.DataVisitor;
import org.babyfishdemo.war3shop.db.installer.schema.DbSchema;
import org.babyfishdemo.war3shop.db.installer.schema.ForeignKeyConstraint;
import org.babyfishdemo.war3shop.db.installer.schema.ParsingContext;
import org.babyfishdemo.war3shop.db.installer.schema.Table;
import org.babyfishdemo.war3shop.db.installer.schema.Type;
import org.babyfishdemo.war3shop.db.installer.schema.UniqueConstraint;

/**
 * @author Tao Chen
 */
public abstract class Executor {

    private static final char BEGIN_QUOTE = '"';
    
    private static final char END_QUOTE = '"';
    
    private static final String IDENT = "\t";
    
    private static final ThreadLocal<ParsingContext> PARSING_CONTEXT_LOCAL = new ThreadLocal<>();
    
    private static final ThreadLocal<Connection> CONNECTION_LOCAL = new ThreadLocal<>();
    
    private DbSchema schema;
    
    private DataSource dataSource;
    
    private File baseDir;

    private Map<String, Object> variables = new HashMap<>();
    
    private int constraintIdSequence;
    
    public Executor(String dir, DataSource dataSource) {
        this.schema = new DbSchema(dir);
        this.dataSource = dataSource;
        this.baseDir = new File(dir);
    }

    public File getBaseDir() {
        return this.baseDir;
    }

    public void setBaseDir(File baseDir) {
        if (baseDir == null) {
            throw new IllegalArgumentException("baseDir can not be null");
        }
        if (!baseDir.isDirectory()) {
            throw new IllegalArgumentException("baseDir must be directory");
        }
        this.baseDir = baseDir;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }
    
    public final void install() throws SQLException {
        this.constraintIdSequence = 0;
        PARSING_CONTEXT_LOCAL.set(new ParsingContextImpl());
        try  {
            this.execute(new SQLAction() {
                @Override
                public void execute(Connection con) throws SQLException {
                    Executor that = Executor.this;
                    that.uninstall0();
                    Collection<Table> orderedTables = that.getTableByInstallationOrder();
                    for (Table table : orderedTables) {
                        that.installTable(table);
                    }
                    con.setAutoCommit(false);
                    for (Table table : orderedTables) {
                        try {
                            System.out.println("Insert data for table \"" + table.getName() + "\"");
                            that.insertData(table);
                        } catch (SQLException | RuntimeException | Error ex) {
                            con.rollback();
                            throw ex;
                        }
                        con.commit();
                    }
                }
            });
        } finally {
            PARSING_CONTEXT_LOCAL.remove();
        }
    }
    
    public final void uninstall() throws SQLException {
        this.execute(new SQLAction() {
            @Override
            public void execute(Connection con) throws SQLException {
                Executor that = Executor.this;
                that.uninstall0();
            }
        });
    }
    
    private final void uninstall0() throws SQLException {
        List<Table> tables = new ArrayList<>(this.getTableByInstallationOrder());
        Collections.reverse(tables);
        for (Table table : tables) {
            this.uninstallTable(table);
        }
    }
    
    private void installTable(Table table) throws SQLException {
        this.createTable(table);
        this.addConstraints(table);
        this.createSequence(table);
    }

    private void uninstallTable(Table table) throws SQLException {
        this.dropObjectsIfExsits(table);
    }
    
    private void createTable(Table table) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder
        .append("CREATE TABLE ")
        .append(this.getObjectName(table.getName()))
        .append("(\n");
        
        boolean addComma = false;
        for (Column column : table.getColumns().values()) {
            if (addComma) {
                builder.append(",\n");
            } else {
                addComma = true;
            }
            builder
            .append(IDENT)
            .append(BEGIN_QUOTE)
            .append(column.getName())
            .append(END_QUOTE)
            .append(' ')
            .append(this.mapType(column.getType()));
            if (column.getLength() != 0) {
                builder
                .append('(')
                .append(column.getLength())
                .append(')');
            }
            if (!column.isNullable()) {
                builder.append(" NOT NULL");
            }
        }
        
        builder.append("\n)");
        this.executeDDL(builder.toString());
    }
    
    private void createSequence(Table table) throws SQLException {
        if (table.getSequenceName() != null) {
            StringBuilder builder = new StringBuilder();
            builder
            .append("CREATE SEQUENCE ")
            .append(this.getObjectName(table.getSequenceName()))
            .append(" START WITH ")
            .append(table.getSequenceBase());
            this.executeDDL(builder.toString());
        }
    }
    
    private void addConstraints(Table table) throws SQLException {
        StringBuilder builder;
        
        if (table.getPrimaryConstraint() != null) {
            builder = new StringBuilder();
            builder
            .append("ALTER TABLE ")
            .append(this.getObjectName(table.getName()))
            .append('\n')
            .append(IDENT)
            .append("ADD CONSTRAINT ")
            .append(this.getConstraintName("PK_"))
            .append('\n')
            .append(IDENT)
            .append(IDENT)
            .append("PRIMARY KEY(");
            appendColumns(builder, table.getPrimaryConstraint().getColumns());
            builder.append(")");
            this.executeDDL(builder.toString());
        }
        
        for (UniqueConstraint uniqueConstraint : table.getUniqueConstraints()) {
            builder = new StringBuilder();
            builder
            .append("ALTER TABLE ")
            .append(this.getObjectName(table.getName()))
            .append('\n')
            .append(IDENT)
            .append("ADD CONSTRAINT ")
            .append(this.getConstraintName("U_"))
            .append('\n')
            .append(IDENT)
            .append(IDENT)
            .append("UNIQUE(");
            appendColumns(builder, uniqueConstraint.getColumns());
            builder.append(")");
            this.executeDDL(builder.toString());
        }
        
        for (ForeignKeyConstraint foreignKeyConstraint : table.getForeignKeyConstraints()) {
            builder = new StringBuilder();
            builder
            .append("ALTER TABLE ")
            .append(this.getObjectName(table.getName()))
            .append('\n')
            .append(IDENT)
            .append("ADD CONSTRAINT ")
            .append(this.getConstraintName("FK_"))
            .append('\n')
            .append(IDENT)
            .append(IDENT)
            .append("FOREIGN KEY(");
            appendColumns(builder, foreignKeyConstraint.getColumns());
            builder
            .append(")\n")
            .append(IDENT)
            .append(IDENT)
            .append(IDENT)
            .append("REFERENCES ")
            .append(BEGIN_QUOTE)
            .append(foreignKeyConstraint.getReferencedTable().getName())
            .append(END_QUOTE)
            .append('(');
            appendColumns(builder, foreignKeyConstraint.getReferencedColumns());
            builder
            .append(")\n")
            .append(IDENT)
            .append(IDENT)
            .append(IDENT)
            .append(IDENT)
            .append("ON DELETE CASCADE");
            
            this.executeDDL(builder.toString());
        }
    }
    
    private String getConstraintName(String prefix) {
        String value = Integer.toString(this.constraintIdSequence++);
        StringBuilder builder = new StringBuilder();
        builder.append(BEGIN_QUOTE).append(prefix);
        for (int i = 8; i > value.length(); i--) {
            builder.append('0');
        }
        builder.append(value).append(END_QUOTE);
        return builder.toString();
    }
    
    private static void appendColumns(StringBuilder builder, Iterable<Column> columns) {
        boolean addComma = false;
        for (Column column : columns) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder
            .append(BEGIN_QUOTE)
            .append(column.getName())
            .append(END_QUOTE);
        }
    }
    
    private Collection<Table> getTableByInstallationOrder() {
        LinkedHashSet<Table> orderedTables = new LinkedHashSet<Table>();
        for (Table table : this.schema.getTables().values()) {
            collectTablesByInstallationOrder(table, orderedTables);
        }
        return orderedTables;
    }
    
    private static void collectTablesByInstallationOrder(Table table, LinkedHashSet<Table> orderedTables) {
        for (ForeignKeyConstraint foreignKeyConstraint : table.getForeignKeyConstraints()) {
            collectTablesByInstallationOrder(foreignKeyConstraint.getReferencedTable(), orderedTables);
        }
        orderedTables.add(table);
    }
    
    public static ParsingContext getParsingContext() {
        ParsingContext parsingContext = PARSING_CONTEXT_LOCAL.get();
        if (parsingContext == null) {
            throw new IllegalStateException();
        }
        return parsingContext;
    }
    
    public static Connection getConnection() {
        final Connection con = CONNECTION_LOCAL.get();
        if (con == null) {
            throw new IllegalStateException();
        }
        return safeConnection(con);
    }
    
    protected final String getObjectName(String name) {
        StringBuilder builder = new StringBuilder();
        builder
        .append(BEGIN_QUOTE)
        .append(name)
        .append(END_QUOTE);
        return builder.toString();
    }

    protected abstract String mapType(Type type);
    
    protected void executeDDL(String sql) throws SQLException {
        System.out.println("Execute DDL: ------------------------------------------\n" + sql);
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    protected void insertData(final Table table) throws SQLException{
        StringBuilder builder = new StringBuilder();
        builder
        .append("INSERT INTO ")
        .append(this.getObjectName(table.getName()))
        .append('(');
        boolean addComma = false;
        for (Column column : table.getColumns().values()) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder
            .append(BEGIN_QUOTE)
            .append(column.getName())
            .append(END_QUOTE);
        }
        builder.append(") VALUES(");
        
        addComma = false;
        for (int i = table.getColumns().size() - 1; i >= 0; i--) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder.append('?');
        }
        builder.append(')');
        
        try (final PreparedStatement statement = getConnection().prepareStatement(builder.toString())) {
            table.visitData(getParsingContext(), DataVisitingMode.ALL, new DataVisitor() {
                @Override
                public void visitRow(Object[] dataItem) throws SQLException {
                    for (Column column : table.getColumns().values()) {
                        int colIndex = column.getIndex();
                        Object value = dataItem[colIndex];
                        setParameter(statement, colIndex + 1, value, column);
                    }
                    statement.executeUpdate();
                }
            });
        }
    }
    
    protected void postConnect(Connection con) throws SQLException {
        
    }
    
    protected void preUnconnect(Connection con) throws SQLException {
        
    }
    
    protected final void setParameter(PreparedStatement statement, int parameterIndex, Object value, Column column) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, column.getType().getSqlType());
        } else if (column.getLength() == 0) {
            statement.setObject(parameterIndex, value, column.getType().getSqlType());
        } else {
            statement.setObject(parameterIndex, value, column.getType().getSqlType(), column.getLength());
        }
    }
    
    private void execute(SQLAction sqlAction) throws SQLException {
        try (Connection con = this.dataSource.getConnection()) {
            CONNECTION_LOCAL.set(con);
            try {
                SafeConnection safeCon = safeConnection(con);
                this.postConnect(safeCon);
                try {
                    sqlAction.execute(con);
                } finally {
                    this.preUnconnect(safeCon);
                }
            } finally {
                CONNECTION_LOCAL.remove();
            }
        }
    }
    
    private static SafeConnection safeConnection(final Connection con) {
        if (con instanceof SafeConnection) {
            return (SafeConnection)con;
        }
        return (SafeConnection)
                Proxy
                .newProxyInstance(
                        SafeConnection.class.getClassLoader(), 
                        new Class[] { SafeConnection.class }, 
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                switch (method.getName()) {
                                case "close":
                                    throw new UnsupportedOperationException();
                                case "setAutoCommit":
                                    throw new UnsupportedOperationException();
                                case "commit":
                                    throw new UnsupportedOperationException();
                                case "rollback":
                                    throw new UnsupportedOperationException();
                                }
                                return method.invoke(con, args);
                            }
                        }
                );
    }
    
    protected abstract void dropObjectsIfExsits(Table table) throws SQLException;
    
    private class ParsingContextImpl implements ParsingContext {
        
        private DateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        private DateFormat shortFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        private long millis = System.currentTimeMillis();
        
        private char[] cbuf = new char[1024];
        
        private byte[] bbuf = new byte[1024];
        
        @Override
        public File getBaseDir() {
             return Executor.this.baseDir;
        }
        
        @Override
        public long getMillis() {
            return this.millis;
        }
        
        @Override
        public Map<String, Object> getVariables() {
            return Collections.unmodifiableMap(Executor.this.variables);
        }

        @Override
        public Date parseDate(String text) throws ParseException {
            if (text.indexOf(':') == -1) {
                return new Date(this.shortFormat.parse(text).getTime());
            }
            return new Date(this.longFormat.parse(text).getTime());
        }

        @Override
        public void loadText(String relativePath, Writer writer) {
            File file = new File(this.getBaseDir().getAbsolutePath(), relativePath.replace('/', File.separatorChar));
            if (!file.isFile()) {
                throw new IllegalArgumentException(
                        "No file \"" 
                        + relativePath 
                        + "\" under \""
                        + this.getBaseDir().getAbsolutePath()
                        + "\"");
            }
            if (file.length() >= 100 * 1024 * 1024) {
                throw new IllegalArgumentException(
                        "The file \""
                        + relativePath
                        + "\" is too large, it must be less than or equal to 100M");
            }
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                while (true) {
                    int readLen = reader.read(this.cbuf);
                    if (readLen == -1) {
                        break;
                    }
                    writer.write(this.cbuf, 0, readLen);
                }
            } catch (IOException ex) {
                throw new IllegalArgumentException(
                        "Failed to read the file \""
                        + relativePath
                        + "\"");
            }
        }

        @Override
        public void loadImage(String relativePath, OutputStream outputStream) {
            File file = new File(this.getBaseDir().getAbsolutePath(), relativePath.replace('/', File.separatorChar));
            if (!file.isFile()) {
                throw new IllegalArgumentException(
                        "No file \"" 
                        + relativePath 
                        + "\" under \""
                        + this.getBaseDir().getAbsolutePath()
                        + "\"");
            }
            if (file.length() >= 100 * 1024 * 1024) {
                throw new IllegalArgumentException(
                        "The file \""
                        + relativePath
                        + "\" is too large, it must be less than or equal to 100M");
            }
            try (InputStream inputStream = new FileInputStream(file)) {
                while (true) {
                    int readLen = inputStream.read(this.bbuf);
                    if (readLen == -1) {
                        break;
                    }
                    outputStream.write(this.bbuf, 0, readLen);
                }
            } catch (IOException ex) {
                throw new IllegalArgumentException(
                        "Failed to read the file \""
                        + relativePath
                        + "\"");
            }
        }
    }
    
    private interface SQLAction {
        void execute(Connection con) throws SQLException;
    }
    
    private interface SafeConnection extends Connection {
        
    }
}
