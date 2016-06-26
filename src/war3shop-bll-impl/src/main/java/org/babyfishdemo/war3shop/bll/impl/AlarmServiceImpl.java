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

import java.util.Date;

import javax.annotation.Resource;

import org.babyfishdemo.war3shop.bll.AlarmService;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.impl.aop.RequirePermission;
import org.babyfishdemo.war3shop.dal.AlarmRepository;
import org.babyfishdemo.war3shop.entities.Alarm;
import org.babyfishdemo.war3shop.entities.Alarm__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.specification.AlarmSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
public class AlarmServiceImpl implements AlarmService {
    
    @Resource
    private AlarmRepository alarmRepository;
    
    @Resource
    private AuthorizationService authorizationService;
    
    @Resource(name = "new-alarm-mail-component")
    private MailComponent newAlarmMailComponent;

    @Transactional
    @Override
    public void sendAlarm(User targetUser, String message) {
        Alarm alarm = new Alarm();
        alarm.setCreationTime(new Date());
        alarm.setUser(targetUser);
        alarm.setMessage(message);
        alarm = this.alarmRepository.mergeEntity(alarm);
        this.newAlarmMailComponent.asyncSendAndIgnoreException(
                targetUser.getEmail(), 
                "userName",
                targetUser.getName(),
                "id",
                Long.toString(alarm.getId()),
                "message",
                alarm.getMessage()
        );
    }

    /*
     * Use the transaction propagation REQUIRES_NEW so that open a new data base connection to 
     * insert the new Alarm even if the caller business logic is rollbacked because of some exceptions 
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void sendAlarmForcibly(User targetUser, String message) {
        this.sendAlarm(targetUser, message);
    }

    @RequirePermission({ Constants.PURCHASE_PRODUCTS, Constants.DELIVERY_ORDERS })
    @Transactional(readOnly = true)
    @Override
    public Page<Alarm> getMyAlarms(
            AlarmSpecification specification,
            int pageIndex, 
            int pageSize, 
            Alarm__... queryPaths) {
        specification.setUserId(this.authorizationService.getCurrentUserId());
        return this.alarmRepository.getAlarms(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }
    
    @RequirePermission({ Constants.PURCHASE_PRODUCTS, Constants.DELIVERY_ORDERS })
    @Transactional
    @Override
    public void acknowledgeAlarm(int alarmId) {
        Alarm alarm = this.alarmRepository.getAlarmById(alarmId);
        if (alarm == null) {
            throw new IllegalArgumentException("No such alarm");
        }
        if (!alarm.getUser().getId().equals(this.authorizationService.getCurrentUserId())) {
            throw new IllegalArgumentException("The specified alarm can not be acknownledged by you");
        }
        alarm.setAcknowledged(true);
        this.alarmRepository.mergeEntity(alarm);
    }

    @RequirePermission({ Constants.PURCHASE_PRODUCTS, Constants.DELIVERY_ORDERS })
    @Transactional
    @Override
    public void unacknowledgeAlarm(int alarmId) {
        Alarm alarm = this.alarmRepository.getAlarmById(alarmId);
        if (alarm == null) {
            throw new IllegalArgumentException("No such alarm");
        }
        if (!alarm.getUser().getId().equals(this.authorizationService.getCurrentUserId())) {
            throw new IllegalArgumentException("The specified alarm can not be unacknownledged by you");
        }
        alarm.setAcknowledged(false);
        this.alarmRepository.mergeEntity(alarm);
    }

    @RequirePermission({ Constants.PURCHASE_PRODUCTS, Constants.DELIVERY_ORDERS })
    @Transactional
    @Override
    public void deleteAlarm(int alarmId) {
        Alarm alarm = this.alarmRepository.getAlarmById(alarmId);
        if (alarm == null) {
            throw new IllegalArgumentException("No such alarm");
        }
        if (!alarm.getUser().getId().equals(this.authorizationService.getCurrentUserId())) {
            throw new IllegalArgumentException("The specified alarm can not be deleted by you");
        }
        this.alarmRepository.removeEntity(alarm);
    }
}
