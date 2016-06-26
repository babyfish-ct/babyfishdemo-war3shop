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
package org.babyfishdemo.war3shop.web.aop;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Strings;
import org.babyfish.lang.UncheckedException;
import org.babyfishdemo.war3shop.entities.PreferentialActionType;
import org.babyfishdemo.war3shop.entities.PreferentialThresholdType;
import org.babyfishdemo.war3shop.entities.ProductType;
import org.babyfishdemo.war3shop.entities.Race;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Tao Chen
 */
@ControllerAdvice(basePackages = "org.babyfishdemo.war3shop.web")
@EnableWebMvc
public class DataBinderAspect {
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Date.class, new DateEditor());
        binder.registerCustomEditor(ProductType.class, new EnumEditor(ProductType.class));
        binder.registerCustomEditor(Race.class, new EnumEditor(Race.class));    
        binder.registerCustomEditor(PreferentialThresholdType.class, new EnumEditor(PreferentialThresholdType.class));
        binder.registerCustomEditor(PreferentialActionType.class, new EnumEditor(PreferentialActionType.class));
        binder.registerCustomEditor(Collection.class, new CollectionEditor());
    }
    
    private static class DateEditor extends PropertyEditorSupport {

        @Override
        public String getAsText() {
            if (this.getValue() == null) {
                return "";
            }
            //DateFormat is thread-unself, so uses it as local variable, not member field.
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return format.format((Date)this.getValue());
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (Nulls.isNullOrEmpty(text)) {
                this.setValue(null);
                return;
            }
            //DateFormat is thread-unself, so uses it as local variable, not member field.
            DateFormat format =
                    text.indexOf(':') != -1 ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm") :
                    new SimpleDateFormat("yyyy-MM-dd");
            try {
                this.setValue(format.parse(text));
            } catch (ParseException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }
    
    private static class EnumEditor extends PropertyEditorSupport {
        
        private Class<? extends Enum<?>> enumType;
        
        private Enum<?>[] values;
        
        public EnumEditor(Class<? extends Enum<?>> enumType) {
            this.enumType = Arguments.mustBeCompatibleWithValue(
                    "enumType", 
                    Arguments.mustNotBeNull("enumType", enumType), 
                    Enum.class);
            try {
                this.values = (Enum<?>[])this.enumType.getMethod("values").invoke(null);
            } catch (IllegalAccessException | 
                    InvocationTargetException | 
                    NoSuchMethodException | 
                    SecurityException ex) {
                throw UncheckedException.rethrow(ex);
            }
        }
        
        @Override
        public String getAsText() {
            return Integer.toString(((Enum<?>)this.getValue()).ordinal());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            try {
                this.setValue(this.values[Integer.parseInt(text)]);
            } catch (NumberFormatException ex) {
                this.setValue(Enum.valueOf((Class)this.enumType, text));
            }
        }
    }
    
    private static class CollectionEditor extends PropertyEditorSupport {
        
        private static final String COLLECTION = "collection";
        
        @Override
        public String getAsText() {
            Collection<?> c = (Collection<?>)this.getValue();
            if (c == null) {
                return null;
            }
            return Strings.join(c, ",");
        }
    
        @SuppressWarnings("unchecked")
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (text == null || text.isEmpty()) {
                this.setValue(null);
                return;
            }
            String elementTypeName = getElementTypeName(text);
            if (elementTypeName != null) {
                text = text.substring(text.indexOf(':') + 1);
                if (text.isEmpty()) {
                    this.setValue(null);
                    return;
                }
            }
            String[] values = text.split(",");
            Collection<Object> c = new LinkedHashSet<>((values.length * 4 + 2) / 3);
            if (elementTypeName != null) {
                if (elementTypeName.equals("string") || elementTypeName.equals("java.lang.String")) {
                    for (String value : values) {
                        c.add(value);
                    }
                }
                else if (elementTypeName.equals("int") || elementTypeName.equals("java.lang.Integer")) {
                    for (String value : values) {
                        c.add(Integer.parseInt(value));
                    }
                }
                else if (elementTypeName.equals("long") || elementTypeName.equals("java.lang.Long")) {
                    for (String value : values) {
                        c.add(Long.parseLong(value));
                    }
                } else {
                    Class<?> elementType = null;
                    try {
                        elementType = Thread.currentThread().getContextClassLoader().loadClass(elementTypeName);
                    } catch (ClassNotFoundException ex) {
                    }
                    if (elementType == null) {
                        try {
                            elementType = Class.forName(elementTypeName);
                        } catch (ClassNotFoundException ex) {
                        }
                    }
                    if (elementType == null) {
                        throw new IllegalArgumentException(
                                "Can not parse the string \"" +
                                text +
                                "\" to be collection because the element type \"" +
                                elementTypeName +
                                "\" is not a valid java type(or int, long).");
                    }
                    if (!elementType.isEnum()) {
                        throw new IllegalArgumentException(
                                "Can not parse the string \"" +
                                text +
                                "\" to be collection because the element type \"" +
                                elementTypeName +
                                "\" is neither int or long nor enum type.");
                        
                    }
                    EnumEditor enumEditor = new EnumEditor((Class<? extends Enum<?>>)elementType);
                    for (String value : values) {
                        enumEditor.setAsText(value);
                        c.add(enumEditor.getValue());
                    }
                }
            } else {
                boolean hasId = false;
                boolean hasString = false;
                for (String value : values) {
                    value = value.trim();
                    boolean isId = !hasString;
                    if (isId) {
                        for (int i = value.length() - 1; i >= 0; i--) {
                            if (!Character.isDigit(value.charAt(i))) {
                                isId = false;
                                break;
                            }
                        }
                    }
                    if (!hasString && isId) {
                        c.add(Long.parseLong(value));
                        hasId = true;
                    } else {
                        c.add(value);
                        hasString = true;
                    }
                }
                if (hasId && hasString) {
                    Collection<Object> newC = new LinkedHashSet<>((c.size() * 4 + 2) / 3);
                    for (Object o : c) {
                        newC.add(o.toString());
                    }
                    c = newC;
                }
            }
            this.setValue(c);
        }
        
        private static String getElementTypeName(String text) {
            if (text.startsWith(COLLECTION)) {
                int ltIndex = text.indexOf('<');
                int gtIndex = text.indexOf('>');
                int clIndex = text.indexOf(':');
                if (ltIndex < gtIndex && gtIndex < clIndex) {
                    for (int i = COLLECTION.length(); i < ltIndex; i++) {
                        if (!Character.isWhitespace(text.charAt(i))) {
                            return null;
                        }
                    }
                    for (int i = gtIndex + 1; i < clIndex; i++) {
                        if (!Character.isWhitespace(text.charAt(i))) {
                            return null;
                        }
                    }
                    return text.substring(ltIndex + 1, gtIndex).trim();
                }
            }
            return null;
        }
    } 
}
