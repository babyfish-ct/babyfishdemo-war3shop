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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Tao Chen
 */
public class Table {
    
    private static final String[] EMPTY_STRING_ARR = new String[0];
    
    static final String XMLNS = "http://www.babyfishdemo.org/war3shop/db-installer";
    
    private static final Schema SCHEMA;

    private File xmlFile;
    
    private String name;
    
    private String sequenceName;
    
    private long sequenceBase;
    
    // The wrapper of java.util.LinkedHashMap with order.
    private Map<String, Column> columns;
    
    private PrimaryKeyConstraint primaryKeyConstraint;
    
    private List<UniqueConstraint> uniqueConstraints;
    
    private List<ForeignKeyConstraint> foreignKeyConstraints;
    
    private Map<Integer, List<Column>> unresolvedForeignKeyColumnMap;
    
    private String[] referencedTableNames;
    
    private int hasLobColumns;
    
    Table(File xmlFile) {
        this.xmlFile = xmlFile;
        this.parse();
    }
    
    public File getXmlFile() {
        return this.xmlFile;
    }
    
    public String getName() {
        return this.name;
    }

    public String getSequenceName() {
        return this.sequenceName;
    }

    public long getSequenceBase() {
        return this.sequenceBase;
    }

    public Map<String, Column> getColumns() {
        return this.columns;
    }

    public PrimaryKeyConstraint getPrimaryConstraint() {
        return this.primaryKeyConstraint;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return this.uniqueConstraints;
    }

    public List<ForeignKeyConstraint> getForeignKeyConstraints() {
        return this.foreignKeyConstraints;
    }
    
    public boolean hasLobColumns() {
        int has = this.hasLobColumns;
        if (has == 0) {
            for (Column column : this.columns.values()) {
                Type type = column.getType();
                if (type == Type.CLOB || type == Type.BLOB) {
                    has = +1;
                    break;
                }
            }
            if (has == 0) {
                has = -1;
            }
            this.hasLobColumns = has;
        }
        return has == +1;
    }
    
    public void visitData(
            final ParsingContext parsingContext, 
            final DataVisitingMode dataVisitingMode, 
            final DataVisitor dataVisitor) {
        
        Visitor visitor = new Visitor() {
            
            private int rowIndex = -1;
            
            private Object[] dataItem = new Object[Table.this.columns.size()];
            
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (!XMLNS.equals(uri) || !"row".equals(localName)) {
                    return;
                }
                Arrays.fill(this.dataItem, null);
                this.rowIndex++;
                
                Table that = Table.this;
                for (int i = attributes.getLength() - 1; i >= 0; i--) {
                    if (!XMLConstants.NULL_NS_URI.equals(attributes.getURI(i))) {
                        continue; 
                    }
                    String columnName = attributes.getLocalName(i).toUpperCase();
                    Column column = that.columns.get(columnName);
                    if (column == null) {
                        throw new DataException(
                                "The \""
                                + that.xmlFile.getAbsolutePath() 
                                + "\" is invalid, its row whose index is \""
                                + this.rowIndex
                                + "\" has a column \""
                                + columnName
                                + "\" which is not existing");
                    }
                    if (!dataVisitingMode.isVisitable(column)) {
                        continue;
                    }
                    Object value;
                    try {
                        if (dataVisitingMode == DataVisitingMode.PK_AND_UNRESOLVED_LOB && column.getType().isLob()) {
                            String str = (String)Type.STRING.parseValue(attributes.getValue(i), parsingContext);
                            if (!str.startsWith("file(") || !str.endsWith(")")) {
                                throw new DataException("The lob value must be configured as \"file(...)\"");
                            }
                            value = str.substring(5, str.length() - 1).trim();
                        } else {
                            value = column.getType().parseValue(attributes.getValue(i), parsingContext);
                        }
                    } catch (RuntimeException | Error ex) {
                        throw new DataException(
                                "The \""
                                + that.xmlFile.getAbsolutePath() 
                                + "\" is invalid, its row whose index is \""
                                + this.rowIndex
                                + "\" is invalid, its value of column \""
                                + columnName
                                + "\" is \""
                                + attributes.getValue(i)
                                + "\" that can not be parsed",
                                ex);
                    }
                    this.dataItem[column.getIndex()] = value;
                }
                for (Column column : that.columns.values()) {
                    if (dataVisitingMode.isVisitable(column) && !column.isNullable() && this.dataItem[column.getIndex()] == null) {
                        throw new DataException(
                                "The \""
                                + that.xmlFile.getAbsolutePath() 
                                + "\" is invalid, its row whose index is \""
                                + this.rowIndex
                                + "\" is invalid, its value of column \""
                                + column.getName()
                                + "\" can not be null");
                    }
                }
                try {
                    dataVisitor.visitRow(this.dataItem);
                } catch (Exception ex) {
                    throw new DataException(
                            "Failed to visit the data whose index is \""
                            + rowIndex
                            + "\"",
                            ex);
                }
            }
        };
        this.visitXml(visitor);
    }

    private void parse() {
        class VisitorImpl extends Visitor {
            String name;
            String sequenceName;
            int sequenceBase;
            LinkedHashMap<String, Column> columnMap = new LinkedHashMap<>();
            List<Column> primaryColumns = new ArrayList<>();
            Map<Integer, List<Column>> uniqueColumnMap = new HashMap<>();
            Map<Integer, List<Column>> foreignKeyColumnMap = new HashMap<>();
            List<String> referencedTables = new ArrayList<>();
            private boolean primaryKey;
            private boolean unique;
            private int uniqueId = -1;
            private int foreignKeyId = -1;
            private String referncedTable;
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (XMLNS.equals(uri)) {
                    switch (localName) {
                    case "table":
                        this.name = attributes.getValue(XMLConstants.NULL_NS_URI, "name").toUpperCase();
                        break;
                    case "primary-key":
                        this.primaryKey = true;
                        this.sequenceName = attributes.getValue(XMLConstants.NULL_NS_URI, "sequence");
                        if (this.sequenceName != null) {
                            this.sequenceName = this.sequenceName.toUpperCase();
                        }
                        break;
                    case "unique":
                        this.unique = true;
                        this.uniqueId++;
                        break;
                    case "foreign-key":
                        this.referncedTable = attributes.getValue(XMLConstants.NULL_NS_URI, "reference").toUpperCase();
                        this.foreignKeyId++;
                        this.referencedTables.add(this.referncedTable);
                        break;
                    case "column":
                        Column column = new Column(Table.this, this.columnMap.size(), attributes);
                        if (this.primaryKey) {
                            if (this.sequenceName != null) {
                                if (!this.primaryColumns.isEmpty()) {
                                    throw new SchemaException(
                                            "The \""
                                            + Table.this.xmlFile.getAbsolutePath()
                                            + "\" is invalid, the primary key must have only one column when the sequence is specified");
                                }
                                if (column.isNullable()) {
                                    throw new SchemaException(
                                            "The \""
                                            + Table.this.xmlFile.getAbsolutePath()
                                            + "\" is invalid, the primary key column can not be nullable");
                                }
                                Type type = column.getType();
                                if (type != Type.INT && type != Type.LONG) {
                                    throw new SchemaException(
                                            "The \""
                                            + Table.this.xmlFile.getAbsolutePath()
                                            + "\" is invalid, the primary key must be int or long when the sequence is specified");
                                }
                            }
                            if (column.getType().isLob()) {
                                throw new SchemaException(
                                        "The \""
                                        + Table.this.xmlFile.getAbsolutePath()
                                        + "\" is invalid, the primary key must not be lob");
                            }
                            this.primaryColumns.add(column);
                        }
                        if (this.unique) {
                            List<Column> cols = this.uniqueColumnMap.get(this.uniqueId);
                            if (cols == null) {
                                this.uniqueColumnMap.put(this.uniqueId, cols = new ArrayList<>());
                            }
                            cols.add(column);
                        }
                        if (this.referncedTable != null) {
                            List<Column> cols = this.foreignKeyColumnMap.get(this.foreignKeyId);
                            if (cols == null) {
                                this.foreignKeyColumnMap.put(this.foreignKeyId, cols = new ArrayList<>());
                            }
                            cols.add(column);
                        }
                        this.columnMap.put(column.getName(), column);
                        break;
                    case "row":
                        if (this.sequenceName != null) {
                            String primaryKeyColumnName = this.primaryColumns.get(0).getName();
                            int length = attributes.getLength();
                            String value = null;
                            for (int i = 0; i < length; i++) {
                                if (attributes.getURI(i).isEmpty() && 
                                        attributes.getLocalName(i).equalsIgnoreCase(primaryKeyColumnName)) {
                                    value = attributes.getValue(i).trim();
                                    break;
                                }
                            }
                            if (value != null) {
                                int intValue = Integer.parseInt(value);
                                this.sequenceBase = this.sequenceBase > intValue ? this.sequenceBase : intValue;
                            }
                        }
                    }
                }
            }
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (XMLNS.equals(uri)) {
                    switch (localName) {
                    case "primary-key":
                        this.primaryKey = false;
                        break;
                    case "unique":
                        this.unique = false;
                        break;
                    case "foreign-key":
                        this.referncedTable = null;
                        break;
                    }
                }
            }
        };
        VisitorImpl visitorImpl = new VisitorImpl();
        this.visitXml(visitorImpl);
        this.name = visitorImpl.name;
        this.sequenceName = visitorImpl.sequenceName;
        this.sequenceBase = visitorImpl.sequenceBase + 1;
        this.columns = Collections.unmodifiableMap(visitorImpl.columnMap);
        if (!visitorImpl.primaryColumns.isEmpty()) {
            this.primaryKeyConstraint = new PrimaryKeyConstraint(visitorImpl.primaryColumns);
        }
        List<UniqueConstraint> uniqueConstraints = new ArrayList<>(visitorImpl.uniqueColumnMap.size());
        for (List<Column> cols : visitorImpl.uniqueColumnMap.values()) {
            uniqueConstraints.add(new UniqueConstraint(cols));
        }
        this.uniqueConstraints = Collections.unmodifiableList(uniqueConstraints);
        if (!visitorImpl.foreignKeyColumnMap.isEmpty()) {
            this.unresolvedForeignKeyColumnMap = visitorImpl.foreignKeyColumnMap;
            this.referencedTableNames = visitorImpl.referencedTables.toArray(EMPTY_STRING_ARR);
        }
    }
    
    private void visitXml(Visitor visitor) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            parserFactory.setSchema(SCHEMA);
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(this.xmlFile, visitor);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new SchemaException(ex);
        }
    }
    
    void resolveForeignKeys(DbSchema dbSchema) {
        if (this.unresolvedForeignKeyColumnMap == null) {
            this.foreignKeyConstraints = Collections.emptyList();
            return;
        }
        List<ForeignKeyConstraint> resultList = new ArrayList<>();
        for (Entry<Integer, List<Column>> entry : this.unresolvedForeignKeyColumnMap.entrySet()) {
            String referencedTableName = this.referencedTableNames[entry.getKey()];
            Table referencedTable = dbSchema.getTables().get(referencedTableName);
            if (referencedTable == null) {
                throw new SchemaException(
                        "The \""
                        + Table.this.xmlFile.getAbsoluteFile()
                        + "\" is invalid, the referenced table \""
                        + referencedTableName
                        + "\" is not existing");
            }
            List<Column> cols = entry.getValue();
            List<Column> referencedCols = new ArrayList<>(cols.size());
            if (cols.size() == 1) {
                Column col = cols.get(0);
                if (col.getUnresolvedReference() == null) {
                    if (referencedTable.getPrimaryConstraint() == null) {
                        throw new SchemaException(
                                "The \""
                                + Table.this.xmlFile.getAbsoluteFile()
                                + "\" is invalid, the column \""
                                + col.getName()
                                + "\" must have the attribute \"reference\" because the referenced table \""
                                + referencedTable.getName()
                                + "\" does not have primary key constraint");
                    }
                    if (referencedTable.getPrimaryConstraint().getColumns().size() > 1) {
                        throw new SchemaException(
                                "The \""
                                + Table.this.xmlFile.getAbsoluteFile()
                                + "\" is invalid, the column \""
                                + col.getName()
                                + "\" must have the attribute \"reference\" because the referenced table \""
                                + referencedTable.getName()
                                + "\" has a primary key with several columns");
                    }
                    referencedCols.add(referencedTable.getPrimaryConstraint().getColumns().get(0));
                }
            } 
            if (referencedCols.isEmpty()) {
                for (Column col : cols) {
                    if (col.getUnresolvedReference() == null) {
                        throw new SchemaException(
                                "The \""
                                + Table.this.xmlFile.getAbsoluteFile()
                                + "\" is invalid, the column \""
                                + col.getName()
                                + "\" must have the attribute \"reference\" because the current foreign key contains several columns");
                    }
                    Column referenceCol = referencedTable.getColumns().get(col.getUnresolvedReference());
                    if (referenceCol == null) {
                        throw new SchemaException(
                                "The \""
                                + Table.this.xmlFile.getAbsoluteFile()
                                + "\" is invalid, the column \""
                                + col.getName()
                                + "\" reference the column \""
                                + col.getUnresolvedReference()
                                + "\" of table \""
                                + referencedTable.getName()
                                + "\" but that referenced column is not existing");
                    }
                    referencedCols.add(referenceCol);
                }
            }
            for (int i = referencedCols.size() - 1; i >= 0; i--) {
                cols.get(i).setReferencedColumn(referencedCols.get(i));
            }
            resultList.add(new ForeignKeyConstraint(cols, referencedCols));
        }
        this.foreignKeyConstraints = Collections.unmodifiableList(resultList);
        this.unresolvedForeignKeyColumnMap = null;
    }
    
    private class Visitor extends DefaultHandler {

        @Override
        public void warning(SAXParseException ex) throws SAXException {
            this.reportError(ex);
        }

        @Override
        public void error(SAXParseException ex) throws SAXException {
            this.reportError(ex);
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            this.reportError(ex);
        }
        
        private void reportError(SAXParseException ex) {
            throw new SchemaException(
                    "The \""
                    + Table.this.xmlFile.getAbsoluteFile()
                    + "\" is invalid, it can not be validated by the schema. ",
                    ex);
        }
    }
    
    static {
        try {
            SCHEMA = 
                    SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(Table.class.getResource("table.xsd"));
        } catch (SAXException ex) {
            throw new AssertionError(ex);
        }
    }
}
