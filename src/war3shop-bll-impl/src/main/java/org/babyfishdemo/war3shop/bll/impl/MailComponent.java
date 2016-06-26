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
package org.babyfishdemo.war3shop.bll.impl;

import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.babyfishdemo.war3shop.bll.ConfigurationService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/*
 * No interface, requires <task:annotation-driven proxy-target-class="true"/>
 */
/**
 * @author Tao Chen
 */
public class MailComponent {
    
    private static final Log LOG = LogFactory.getLog(MailComponent.class);

    @Resource
    private ConfigurationService configurationService;
    
    @Resource
    private PlatformTransactionManager transactionManager;
    
    private String contentTemplate;
    
    public MailComponent(String contentTemplate) {
        this.contentTemplate = contentTemplate.trim();
    }
    
    public void send(String emailAddress, String ... parameterNameValuePairs) {
        this.send(false, emailAddress, parameterNameValuePairs);
    }
    
    @Async
    public void asyncSendAndIgnoreException(String emailAddress, String ... parameterNameValuePairs) {
        this.send(true, emailAddress, parameterNameValuePairs);
    }
    
    private void send(boolean ingnoreException, String emailAddress, String[] parameterNameValuePairs) {
        if (parameterNameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("The count of arguments must be odd number");
        }
        String message = this.contentTemplate;
        for (int i = 0; i < parameterNameValuePairs.length; i += 2) {
            String name = parameterNameValuePairs[i];
            String value = parameterNameValuePairs[i + 1];
            if (!isValidName(name)) {
                throw new IllegalArgumentException(name + " is not valid name");
            }
            message = message.replaceAll("\\$\\{" + name + "\\}", value);
        }
        JavaMailSenderImpl sender = this.createJavaMailSender();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sender.getUsername());
        simpleMailMessage.setTo(emailAddress);
        simpleMailMessage.setSubject("babyfishdemo-war3shop: no-reply");
        simpleMailMessage.setText(message);
        try {
            sender.send(simpleMailMessage);
        } catch (RuntimeException | Error ex) {
            LOG.error(
                "Send the email to \"" + 
                emailAddress + 
                "\" failed",
                ex
            );
            if (!ingnoreException) {
                throw ex;
            }
        }
        LOG.info(
            "Send the email to \"" + 
            emailAddress + 
            "\" successed"
        );
    }
    
    private static boolean isValidName(String name) {
        for (int i = name.length() - 1; i >= 0; i--) {
            char c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c >= 'a' && c <= 'z') {
                continue;
            }
            if (c >= '0' && c <= '9') {
                continue;
            }
            return false;
        }
        return true;
    }
    
    /*
     * Let several database accessing share on connection,
     * but private method can not use @Transactional,
     * so use TransactionTemplate in code.
     */
    private JavaMailSenderImpl createJavaMailSender() {
        return new TransactionTemplate(this.transactionManager, new DefaultTransactionDefinition()).execute(
                new TransactionCallback<JavaMailSenderImpl>() {
                    @Override
                    public JavaMailSenderImpl doInTransaction(TransactionStatus arg0) {
                        ConfigurationService cs = MailComponent.this.configurationService;
                        int port = cs.getVariableInt(ConfigurationService.SYS_EMAIL_PORT, 25);
                        JavaMailSenderImpl impl = new JavaMailSenderImpl();
                        impl.setProtocol(cs.getVariable(ConfigurationService.SYS_EMAIL_PROTOCOL, "smtp"));
                        impl.setPort(port);
                        impl.setHost(cs.getVariable(ConfigurationService.SYS_EMAIL_HOST));
                        impl.setUsername(cs.getVariable(ConfigurationService.SYS_EMAIL_USER));
                        impl.setPassword(cs.getVariable(ConfigurationService.SYS_EMAIL_PASSWORD));
                        boolean ssl = "true".equals(cs.getVariable(ConfigurationService.SYS_EMAIL_SSL));
                        if (ssl) {
                            Properties properties = new Properties();
                            properties.put("mail.smtp.auth", "true");
                            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                            properties.put("mail.smtp.socketFactory.port", Integer.toString(port));
                            properties.put("mail.smtp.socketFactory.fallback", "false");
                            properties.put("mail.smtp.starttls.enable", "false");
                            impl.setJavaMailProperties(properties);
                        }
                        return impl;
                    }
                }
        );
    }
}
