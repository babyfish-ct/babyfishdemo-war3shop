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

import javax.annotation.PostConstruct;

import org.springframework.orm.jpa.vendor.Database;

/**
 * @author Tao Chen
 */
public class DatabaseEnvironment {

    private Database defaultDatabase;
    
    private String defaultDriverClassName;
    
    private String defaultUrl;
    
    private String defaultUsername;
    
    private String defaultPassword;

    private Database database;
    
    private String driverClassName;
    
    private String url;
    
    private String username;
    
    private String password;
    
    @PostConstruct
    public void initialize() {
        String oracle = System.getProperty("oracle");
        if (oracle != null) {
            // For eclipse run/debug configuration, "-Doracle" means 
            // empty string, but for shell, it means "-Doracle=true"
            if (!oracle.startsWith("jdbc:oracle:")) { 
                oracle = "jdbc:oracle:thin:@localhost:1521:babyfish";
            }
            String oracleUser = System.getProperty("oracle.user", "war3shop");
            String oraclePassword = System.getProperty("oracle.password", "123");
            this.database = Database.ORACLE;
            this.driverClassName = "oracle.jdbc.OracleDriver";
            this.url = oracle;
            this.username = oracleUser;
            this.password = oraclePassword;
        } else {
            this.database = this.defaultDatabase;
            this.driverClassName = this.defaultDriverClassName;
            this.url = this.defaultUrl;
            this.username = this.defaultUsername;
            this.password = this.defaultPassword;
        }
    }
    
    public Database getDatabase() {
        return this.database;
    }
    
    public String getDriverClassName() {
        return this.driverClassName;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public void setDefaultDatabase(Database defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }

    public void setDefaultDriverClassName(String defaultDriverClassName) {
        this.defaultDriverClassName = defaultDriverClassName;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
}
