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
package org.babyfishdemo.war3shop.bll;

import java.util.Map;

/**
 * @author Tao Chen
 */
public interface ConfigurationService {
    
    String SYS_EMAIL_PROTOCOL = "sys.email.protocol";
    
    String SYS_EMAIL_HOST = "sys.email.host";
    
    String SYS_EMAIL_SSL = "sys.email.ssl";
    
    String SYS_EMAIL_PORT = "sys.email.port";
    
    String SYS_EMAIL_USER = "sys.email.user";
    
    String SYS_EMAIL_PASSWORD = "*sys.email.password";

    String getVariable(String variableName);
    
    String getVariable(String variableName, String defaultValue);
    
    Map<String, String> getVariables(String ... variableNames);
    
    int getVariableInt(String variableName, int defaultValue);
    
    void setVariable(String variableName, String variableValue);
            
    void setSysMailConfiguration(
            Map<String, String> sysMailConfiguration,
            String password);
}
