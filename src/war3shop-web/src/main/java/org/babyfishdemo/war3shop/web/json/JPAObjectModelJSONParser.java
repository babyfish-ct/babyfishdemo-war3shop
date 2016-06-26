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
package org.babyfishdemo.war3shop.web.json;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.Entity;

import org.babyfish.collection.ArrayList;
import org.babyfish.model.jpa.JPAEntities;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

/**
 * For any JPA entity type, 
 * all the non-id properties that do not appear in JSON 
 * will be disabled.
 * 
 * Add an interceptor to alibaba fast-json deserializer, 
 * when an instance of entity type is created,
 * call the org.babyfish.persistence.model.JPAEntities.disableAll(instance)
 * at first, then the deserialization work will enable the properties
 * that appear in JSON text.
 *
 * @author Tao Chen
 */
public class JPAObjectModelJSONParser {
    
    private static final OMParserConfig OM_PARSER_CONFIG = new OMParserConfig();
    
    private JPAObjectModelJSONParser() {}
    
    public static <T> T parseObject(String text, Class<T> type) {
        return JSON.parseObject(
                text, 
                type, 
                OM_PARSER_CONFIG, 
                JSON.DEFAULT_PARSER_FEATURE, 
                new Feature[0]
        );
    }
    
    public static <T> List<T> parseArray(String text, Class<T> elementType) {

        DefaultJSONParser parser = new DefaultJSONParser(text, OM_PARSER_CONFIG);
        JSONLexer lexer = parser.getLexer();
        List<T> list;
        if (lexer.token() == 8) {
            lexer.nextToken();
            list = null;
        } else {
            list = new ArrayList<>();
            parser.parseArray(elementType, list);
            parser.handleResovleTask(list);
        }
        parser.close();
        return list;
    }

    private static class OMParserConfig extends ParserConfig {
        
        @Override
        public ObjectDeserializer createJavaBeanDeserializer(Class<?> clazz, Type type) {
            if (clazz.isAnnotationPresent(Entity.class)) {
                return new OMEntityDeserializer(this, clazz, type);
            }
            return super.createJavaBeanDeserializer(clazz, type);
        }
    }
    
    /*
     * Time is not enough, 
     * so ignore ASMJavaBeanDeserializer,
     * only override JavaBeanDeserializer.
     */
    private static class OMEntityDeserializer extends JavaBeanDeserializer {

        public OMEntityDeserializer(ParserConfig config, Class<?> clazz, Type type) {
            super(config, clazz, type);
        }

        @Override
        public Object createInstance(DefaultJSONParser parser, Type type) {
            Object entity = super.createInstance(parser, type);
            JPAEntities.disableAll(entity);
            return entity;
        }
    }
}
