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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.babyfishdemo.war3shop.db.installer.executor.Executor;

import com.alibaba.fastjson.util.Base64;

/**
 * @author Tao Chen
 */
public enum Type {

    BOOLEAN(Boolean.class, Types.BIT) {
        @Override
        Boolean onParseValue(String text, ParsingContext ctx) {
            return Boolean.parseBoolean(text);
        }
    },
    INT(Integer.class, Types.INTEGER) {
        @Override
        Object onParseValue(String text, ParsingContext ctx) {
            return Integer.parseInt(text);
        }
    },
    LONG(Long.class, Types.BIGINT) {
        @Override
        Object onParseValue(String text, ParsingContext ctx) {
            return Long.parseLong(text);
        }
    },
    DECIMAL(BigDecimal.class, Types.DECIMAL) {
        @Override
        Object onParseValue(String text, ParsingContext ctx) {
            return new BigDecimal(text);
        }
    },
    DATE(Date.class, Types.DATE) {
        @Override
        Date onParseValue(String text, ParsingContext ctx) {
            char c = text.charAt(0);
            if (c == '+' || c == '-') {
                String[] nums = STAR_PATTERN.split(text.substring(1).trim());
                long diff = 1;
                for (String num : nums) {
                    diff *= Integer.parseInt(num.trim());
                }
                long millis = c == '+' ? ctx.getMillis() + diff : ctx.getMillis() - diff;
                return new Date(millis);
            }
            try {
                return ctx.parseDate(text);
            } catch (ParseException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    },
    TIMESTAMP(Date.class, Types.TIMESTAMP) {
        @Override
        Date onParseValue(String text, ParsingContext ctx) {
            return (Date)DATE.onParseValue(text, ctx);
        }
    },
    STRING(String.class, Types.VARCHAR) {
        @Override
        String onParseValue(String text, ParsingContext ctx) {
            return (String)text;
        }
    },
    BINARY(byte[].class, Types.VARBINARY) {
        @Override
        byte[] onParseValue(String text, ParsingContext ctx) {
            String trimed = text.trim();
            if (trimed.startsWith("sha1(") && trimed.endsWith(")")) {
                text = (String)STRING.parseValue(trimed.substring(5, trimed.length() - 1).trim(), ctx);
                return SHA1_DIGEST.digest(text.getBytes());
            } else if (trimed.startsWith("rsa(") && trimed.endsWith(")")) {
                text = (String)STRING.parseValue(trimed.substring(4, trimed.length() - 1).trim(), ctx);
                try {
                    return RSA_CIPHER.doFinal(text.getBytes());
                } catch (IllegalBlockSizeException | BadPaddingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return Base64.decodeFast(text);
        }
    },
    CLOB(String.class, Types.CLOB) {
        @Override
        Clob onParseValue(String text, ParsingContext ctx) {
            try {
                Clob clob = Executor.getConnection().createClob();
                try (Writer writer = clob.setCharacterStream(1)) {
                    String trimed = text.trim();
                    if (trimed.startsWith("file(") && trimed.endsWith(")")) {
                        text = (String)STRING.parseValue(trimed.substring(5, trimed.length() - 1).trim(), ctx);
                        ctx.loadText(text, writer);
                    } else {
                        writer.write(text);
                    }
                }
                return clob;
            } catch (SQLException | IOException ex) {
                throw new DataException(ex);
            }
        }
        @Override
        public boolean isLob() {
            return true;
        }
    },
    BLOB(byte[].class, Types.BLOB) {
        @Override
        Blob onParseValue(String text, ParsingContext ctx) {
            try {
                Blob blob = Executor.getConnection().createBlob();
                try (OutputStream outputStream = blob.setBinaryStream(1)) {
                    String trimed = text.trim();
                    if (trimed.startsWith("file(") && trimed.endsWith(")")) {
                        text = (String)STRING.parseValue(trimed.substring(5, trimed.length() - 1).trim(), ctx);
                        ctx.loadImage(text, outputStream);
                    } else {
                        outputStream.write(Base64.decodeFast(text));
                    }
                }
                return blob;
            } catch (SQLException | IOException ex) {
                throw new DataException(ex);
            }
        }
        @Override
        public boolean isLob() {
            return true;
        }
    };
    
    private static final Pattern STAR_PATTERN = Pattern.compile("\\*");
    
    private static final MessageDigest SHA1_DIGEST;
    
    private static final Cipher RSA_CIPHER;
    
    private Type(Class<?> javaType, int sqlType) {
        this.javaType = javaType;
        this.sqlType = sqlType;
    }
    
    private Class<?> javaType;
    
    private int sqlType;
    
    public static Type of(String type) {
        switch (type) {
        case "boolean":
            return BOOLEAN;
        case "int":
            return INT;
        case "long":
            return LONG;
        case "decimal":
            return DECIMAL;
        case "date":
            return DATE;
        case "timestamp":
            return TIMESTAMP;
        case "string":
            return STRING;
        case "binary":
            return BINARY;
        case "clob":
            return CLOB;
        case "blob":
            return BLOB;
        }
        throw new IllegalArgumentException("Invalid type string: " + type);
    }
    
    public Object parseValue(String text, ParsingContext ctx) {
        String trimed = text.trim();
        if (trimed.startsWith("${") && trimed.endsWith("}")) {
            String variableName = trimed.substring(2, trimed.length() - 1).trim();
            Object parsedValue = ctx.getVariables().get(variableName);
            if (parsedValue != null && !this.javaType.isAssignableFrom(parsedValue.getClass())) {
                throw new IllegalArgumentException(
                        "The type of variable \""
                        + variableName
                        + "\" is not \""
                        + this.javaType.getName()
                        + "\"");
            }
            return parsedValue;
        }
        return this.onParseValue(text, ctx);
    }
    
    public Class<?> getJavaType() {
        return this.javaType;
    }
    
    public int getSqlType() {
        return this.sqlType;
    }
    
    public boolean isLob() {
        return false;
    }
    
    abstract Object onParseValue(String text, ParsingContext ctx);
    
    static {
        try {
            SHA1_DIGEST = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        
        String publicKeyText = 
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnljdDcU8WCbjrcfU9sEZHnf" +
                "DZlMi5Hg987f7MmtwkLN53gWiMFLnH3Sj6lC1A2pvws+BrLXbyzAipn4vqve+lPcWg5P" +
                "TokswWTCR8lbpo1/8GKG9NcgnbR0Tn2ioNhlLrjYLSOr2eFf2KmgZqVRu0yGutAXBd6b" +
                "THbEFWvy8O+lgayAHI+RGobQUnRS/09D0PNDKEzcxq3XbT0uRsUo/ASMBXNGC9aexytz" +
                "qFkhcjmrjEwmHFmfxsqzSCJmTHaPdQJ24y8kVQ9qUYSs0mLKj99H7p7ZqHiQGUuI7J/U" +
                "p9bKbHzEuTc24bUdIpksLfSH9Zh1PeSVHIAD9UWZGlm/mwIDAQAB";
        byte[] keyBytes = Base64.decodeFast(publicKeyText);  
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  
        PublicKey publicKey;
        try {
            publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA");  
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            RSA_CIPHER = cipher;
        } catch (InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
