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
package org.babyfishdemo.war3shop.db.installer.shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.babyfishdemo.war3shop.db.installer.executor.Executor;
import org.babyfishdemo.war3shop.db.installer.executor.HsqldbExecutor;
import org.babyfishdemo.war3shop.db.installer.executor.OracleExecutor;
import org.babyfishdemo.war3shop.db.installer.executor.SimplestDataSource;

/**
 * @author Tao Chen
 */
public class Shell {
    
	private static final String SRC = "src";
	
    private static final String WAR3SHOP_WEB = "war3shop-web";

    private static final String LOB = "lob";

    private static final String DATA = "db-installer/data";

    private File dataDir;
    
    private File webDir;
    
    private File lobDir;
    
    private String database;
    
    private String driverClassName;
    
    private String connectionString;
    
    private String username;
    
    private String password;
    
    private Executor executor;
    
    private void run() throws SQLException, IOException {
        if (System.console() == null) {
            throw new ShellException(
                    "Don't run this application by IDE(eg: eclipse), "
                    + "please run it by real Shell"
            );
        }
        
        this.determineDirs();
        this.determineDatabase();
        
        this.createExecutor();
        this.determinSysMailVariables();
        this.determineWar3shopUserVariable();
        this.executor.setBaseDir(this.lobDir);
        
        writeTitle("Install data");
        this.executor.install();
        System.console().printf(
                "The database of \"%s\" is installed successfully\n",
                this.database
        );
        
        // Finally, generate the web server starting script
        this.generateWebShell();
    }
    
    private void determineDirs() {
        
        writeTitle("Determine the directories");
        
        File currentDir = new File("").getAbsoluteFile();
        File dataDir = null;
        while (currentDir != null) {
            dataDir = new File(currentDir, DATA);
            if (dataDir.isDirectory()) {
                System.console().printf(
                        "Found the \"%s\" at \"%s\" automactially\n", 
                        DATA, dataDir.getAbsolutePath()
                );
                this.dataDir = dataDir;
                break;
            }
            currentDir = currentDir.getParentFile();
        }
        if (this.dataDir == null) {
            this.dataDir = readDirectory(
                    "Can not find the " + 
                    DATA + 
                    "directory automatically, please specify it manually"
            );
        }
        
        File lobDir = new File(this.dataDir, LOB);
        if (lobDir.isDirectory()) {
            System.console().printf("Found the lob directory at \"%s\" automactially\n", lobDir.getAbsoluteFile());
            this.lobDir = lobDir;
        } else {
            this.lobDir = readDirectory(
                    "Can not find the " + 
                    LOB + 
                    " directory automatically, please specify it manually"
            );
        }
        
        currentDir = this.dataDir.getParentFile();
        while (currentDir != null) {
            File srcDir = new File(currentDir, SRC);
            if (srcDir.isDirectory()) {
                File webDir = new File(srcDir, WAR3SHOP_WEB);
                if (webDir.isDirectory()) {
                    System.console().printf(
                            "Found the "
                    		+ WAR3SHOP_WEB
                            + " at \"%s\" automactially\n", 
                            webDir.getAbsoluteFile()
                    );
                    this.webDir = webDir;
                    break;
                }
            }
            currentDir = currentDir.getParentFile();
        }
        if (this.webDir == null) {
            this.webDir = readDirectory(
                    "Can not find the " 
                    + WAR3SHOP_WEB
                    + " directory automatically, please specify it manually"
            );
        }
    }
    
    private void determineDatabase() throws SQLException {
        
        writeTitle("Determine the database");
        
        this.chooseDatabase();
        if (database.equals("ORACLE")) {
            this.installOracleUser();
        } else {
            this.installHsqldbUser();
        }
    }
    
    private void generateWebShell() throws IOException {
        
        writeTitle("Generate war3shop web start shell file");
        
        File file = new File( 
                "start-war3shop-by-"
                + this.database.toLowerCase() 
                + (File.separatorChar == '/' ? ".sh" : ".bat")
        );
        String filePath = file.getAbsolutePath();
        System.console().printf("Generate the file: \"%s\"\n", filePath);
        String lineSeparator = System.getProperty("line.separator");
        if (lineSeparator == null || lineSeparator.isEmpty()) {
            lineSeparator = "\n";
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath))) {
            writer.write("cd ");
            writer.write(this.webDir.getAbsolutePath());
            writer.write(lineSeparator);
            writer.write("mvn jetty:run");
            if (this.database.equals("ORACLE")) {
                writer.write(" -Doracle=");
                writer.write(this.connectionString);
                writer.write(" -Doracle.user=");
                writer.write(this.username);
                writer.write(" -Doracle.password=");
                writer.write(this.password);
            }
            writer.write(lineSeparator);
        }
    }
    
    private void determinSysMailVariables() {
        writeTitle("Determin system email variables");
        this.executor.getVariables().put(
                "sys.email.protocol", 
                readValue("Please input the protocol of war3shop system email(nothing means smtp)", "smtp")
        );
        boolean ssl = "y".equals(
                readRestrictedValue(
                        "Please choose whether the war3shop system email server supports SSL(y/n, 'y' is required for gmail or qqmail)", 
                        true, 
                        "y", 
                        "n"
                )
        );
        this.executor.getVariables().put("sys.email.ssl", ssl ? "true" : "false");
        if (ssl) {
            this.executor.getVariables().put(
                    "sys.email.port", 
                    readValue("Please input the port of war3shop system email(nothing means 465)", "465")
            );
        } else {
            this.executor.getVariables().put(
                    "sys.email.port", 
                    readValue("Please input the port of war3shop system email(nothing means 25)", "25")
            );
        }
        this.executor.getVariables().put(
                "sys.email.host", 
                readValue("Please input the host of war3shop system email server", null)
        );
        this.executor.getVariables().put(
                "sys.email.user", 
                readValue("Please input the user of war3shop system email", null)
        );
        this.executor.getVariables().put(
                "*sys.email.password", 
                readPassword("Please input the password of war3shop system email")
        );
    }
    
    private void determineWar3shopUserVariable() {
        writeTitle("Determin war3shop user variables");
        this.executor.getVariables().put(
                "user.password", 
                readPassword("Please input the password of war3shop user")
        );
        this.executor.getVariables().put(
                "user.email", 
                readValue("Please enter the email address of the war3shop user", null)
        );
    }
    
    private void chooseDatabase() {
        if (System.getProperty("oracle") != null) {
            this.database = "ORACLE";
        } else {
            this.database = "HSQLDB";
        }
    }

    private void installOracleUser() throws SQLException {
        this.driverClassName = "oracle.jdbc.OracleDriver";
        String hostName = readValue("Please input the host name of the oracle server(nothing means \"localhost\")", "localhost");
        String portText = readValue("Please input the ip port of the oracle server(nothing means 1521)", "1521");
        String sid = readValue("Please input the SID of the oracle", null);
        this.connectionString = 
                "jdbc:oracle:thin:@"
                + hostName
                + ":"
                + portText
                + ":"
                + sid;
        String dbaUserName = readValue("Please input the dba user name of oracle(nothing means \"system\")", "system");
        String dbaPassword = readPassword("Please input the dba password of oracle");
        this.username = "war3shop";
        this.password = "123";
        SimplestDataSource dbaDataSource = new SimplestDataSource();
        dbaDataSource.setDriverClassName(this.driverClassName);
        dbaDataSource.setUrl(this.connectionString);
        dbaDataSource.setUsername(dbaUserName);
        dbaDataSource.setPassword(dbaPassword);
        try (Connection con = dbaDataSource.getConnection()) {
            boolean userExisting = false;
            try (PreparedStatement pstmt = con.prepareStatement(
                    "SELECT ACCOUNT_STATUS FROM DBA_USERS WHERE USERNAME=?")
            ) {
                pstmt.setString(1, "WAR3SHOP");
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        if (!rs.getString(1).equals("OPEN")) {
                            throw new IllegalStateException(
                                    String.format(
                                            "The oracle user \"%s\" is existing "
                                            + "but its status is not \"Open\"\n", 
                                            "WAR3SHOP"
                                    )
                            );
                        }
                        System.console().printf(
                                "The oracle user \"WAR3SHOP\" is already existing\n"
                        );
                        userExisting = true;
                    }
                }
            }
            if (!userExisting) {
                this.createTablespace(con, false);
                this.createTablespace(con, true);
                System.console().printf("The oracle user \"%s\" is not existing, create it\n", "WAR3SHOP");
                String sql = 
                        "CREATE USER WAR3SHOP " +
                        "IDENTIFIED BY 123 " +
                        "DEFAULT TABLESPACE WAR3SHOP " +
                        "TEMPORARY TABLESPACE WAR3SHOP_TEMP";
                System.console().printf("sql> ");
                System.console().printf(sql);
                System.console().printf("\n");
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate(sql);
                }
                
                sql = "GRANT CONNECT, RESOURCE TO WAR3SHOP";
                System.console().printf("sql> ");
                System.console().printf(sql);
                System.console().printf("\n");
                try (Statement stmt = con.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }
        }
    }

    private void createTablespace(Connection con, boolean temp) throws SQLException {
        String tablespaceName = temp ? "WAR3SHOP_TEMP" : "WAR3SHOP";
        String contents = temp ? "TEMPORARY" : "PERMANENT";
        boolean tablespaceExisting = false;
        try (PreparedStatement pstmt = con.prepareStatement("SELECT CONTENTS FROM DBA_TABLESPACES WHERE TABLESPACE_NAME = ?")) {
            pstmt.setString(1, tablespaceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (!rs.getString(1).equals(contents)) {
                        throw new IllegalStateException(
                                String.format(
                                        "The oracle tablespace \"%s\" is existing, but its conents is not \"%s\"\n", 
                                        tablespaceName, 
                                        contents
                                )
                        );
                    }
                    System.console().printf("The oracle tablespace \"%s\" is already existing\n", tablespaceName);
                    tablespaceExisting = true;
                }
            }
        }
        if (!tablespaceExisting) {
            System.console().printf("The tablespace \"" +tablespaceName + "\" is not existing, create it\n");
            String sql =
                    "CREATE" + (temp ? " TEMPORARY" : "") + " TABLESPACE " + tablespaceName + " " +
                    (temp ? "TEMPFILE" : "DATAFILE") + " '" + tablespaceName + ".dbf' " +
                    "SIZE 128M " +
                    "AUTOEXTEND ON NEXT 32M " + 
                    "MAXSIZE UNLIMITED " +
                    "EXTENT MANAGEMENT LOCAL";
            System.console().printf("sql> ");
            System.console().printf(sql);
            System.console().printf("\n");
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    private void installHsqldbUser() {
        this.driverClassName = "org.hsqldb.jdbcDriver";
        this.connectionString = "jdbc:hsqldb:hsql://localhost:9999/babyfishdemo-war3shop";
        this.username = "sa";
        
        SimplestDataSource testDataSource = new SimplestDataSource();
        testDataSource.setDriverClassName(this.driverClassName);
        testDataSource.setUrl(this.connectionString);
        testDataSource.setUsername(this.username);
        testDataSource.setPassword(this.password);
        
        try {
            testDataSource.getConnection().close();
        } catch (SQLException ex) {
            throw new IllegalArgumentException(
                    "Can't connection to the hsqldb \""
                    + this.connectionString
                    + "\" by username \""
                    + this.username
                    + "\"");
        }
    }
    
    private void createExecutor() {
        
        SimplestDataSource war3shopDataSource = new SimplestDataSource();
        war3shopDataSource.setDriverClassName(this.driverClassName);
        war3shopDataSource.setUrl(this.connectionString);
        war3shopDataSource.setUsername(this.username);
        war3shopDataSource.setPassword(this.password);
        
        if (this.database == "ORACLE") {
            this.executor = new OracleExecutor(this.dataDir.getAbsolutePath(), war3shopDataSource);
        } else {
            this.executor = new HsqldbExecutor(this.dataDir.getAbsolutePath(), war3shopDataSource);
        }
    }

    private static void writeTitle(String title) {
        System.console().printf("\n");
        System.console().printf("****************************************\n");
        System.console().printf("  ");
        System.console().printf(title);
        System.console().printf("\n");
        System.console().printf("****************************************\n");
    }
    
    private static File readDirectory(String prompt) {
        System.console().printf(prompt);
        System.console().printf(": ");
        while (true) {
            File dir = new File(System.console().readLine());
            if (dir.isDirectory()) {
                return dir;
            }
            System.console().printf(
                    "Your input is not an existing directory, "
                    + "please try again: "
            );
        }
    }
    
    private static String readValue(String prompt, String defaultValue) {
        System.console().printf(prompt);
        System.console().printf(": ");
        while (true) {
            String value = System.console().readLine();
            if (!value.isEmpty()) {
                return value;
            }
            if (defaultValue != null) {
                return defaultValue;
            }
            System.console().printf("You entered nothing, please input again: ");
        }
    }
    
    private static String readRestrictedValue(String prompt, boolean required, String ... restrictions) {
        System.console().printf(prompt);
        System.console().printf(": ");
        while (true) {
            String value = System.console().readLine();
            if (value.isEmpty()) {
                if (!required) {
                    return null;
                }
                System.console().printf("You entered nothing, please input again: ");
            } else {
                for (String restriction : restrictions) {
                    if (restriction == null || restriction.isEmpty()) {
                        throw new IllegalArgumentException("restriction should not be null or empty");
                    }
                    if (value.equals(restriction)) {
                        return value;
                    }
                }
                String prefix = "The input must be ";
                if (!required) {
                    prefix += "nothing or ";
                }
                System.console().printf(prefix + "one of " + Arrays.toString(restrictions) + ", please input again: ");
            }
        }
    }
    
    private static String readPassword(String prompt) {
        System.console().printf(prompt);
        System.console().printf(": ");
        while (true) {
            String value = new String(System.console().readPassword());
            if (!value.isEmpty()) {
                return value;
            }
            System.console().printf("Empty password is not allowed, please input again: ");
        }
    }
    
    public static void main(String[] args) throws SQLException, IOException {
        try {
            new Shell().run();
        } catch (Exception ex) {
            System.console().printf(ex.getMessage() + "\n");
            ex.printStackTrace(System.err);
        }
    }
}
