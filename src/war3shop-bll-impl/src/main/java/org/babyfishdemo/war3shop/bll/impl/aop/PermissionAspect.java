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
package org.babyfishdemo.war3shop.bll.impl.aop;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.babyfish.collection.ArrayList;
import org.babyfish.lang.Strings;
import org.babyfishdemo.war3shop.bll.AuthorizationException;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.springframework.stereotype.Component;

/**
 * @author Tao Chen
 */
@Aspect
@Component
public class PermissionAspect {
    
    @Resource
    private AuthorizationService authorizationService;

    @Pointcut(
            value = "execution(public * org.babyfishdemo.war3shop.bll.impl.*.*(..)) && " +
                    "@annotation(requirePrivilege)",
            argNames = "requirePrivilege"
    )
    public void underRequirePrivilege(RequirePermission requirePrivilege) {}
    
    @Before(
            value = "underRequirePrivilege(requirePrivilege)",
            argNames = "requirePrivilege")
    public void beforeServiceMethod(JoinPoint jp, RequirePermission requirePermission) {
        if (!this.authorizationService.hasPermissions(requirePermission.value())) {
            boolean beAccountManager = false;
            boolean beCustomer = false;
            Collection<String> administratorPermissions = new ArrayList<>(requirePermission.value().length);
            for (String permission : requirePermission.value()) {
                if (Constants.ACCOUNT_MANAGER_PERMISSION.equals(permission)) {
                    beAccountManager = true;
                } if (Constants.CUSTOMER_PERMISSION.equals(permission)) {
                    beCustomer = true;
                } else {
                    administratorPermissions.add(permission);
                }
            }
            List<String> actions = new ArrayList<>();
            if (beAccountManager) {
                actions.add("login as account manager");
            }
            if (beCustomer) {
                actions.add("login as customer");
            }
            if (!administratorPermissions.isEmpty()) {
                if (administratorPermissions.size() == 1) {
                    actions.add(
                            "login as administrator with the privilege \"" +
                            administratorPermissions.iterator().next() +
                            "\"");
                } else {
                    actions.add(
                            "login as administrator with any privilege of \"" +
                            Strings.join(administratorPermissions) +
                            "\""
                    );
                }
            }
            throw new AuthorizationException(
                    "The current operation \""
                    + jp.getSignature()
                    + "\" is forbidden, you must " +
                    Strings.join(actions, " or ")
            );
        }
    }
}
