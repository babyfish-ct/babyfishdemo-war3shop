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
import java.io.OutputStream;
import java.io.Writer;
import java.sql.Date;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Tao Chen
 */
public interface ParsingContext {

    File getBaseDir();
    
    long getMillis();
    
    Map<String, Object> getVariables();
    
    Date parseDate(String text) throws ParseException;
    
    void loadText(String relativePath, Writer writer);
    
    void loadImage(String relativePath, OutputStream outputStream);
}
