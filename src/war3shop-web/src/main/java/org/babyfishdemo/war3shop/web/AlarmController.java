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
package org.babyfishdemo.war3shop.web;

import javax.annotation.Resource;

import org.babyfishdemo.war3shop.bll.AlarmService;
import org.babyfishdemo.war3shop.entities.Alarm;
import org.babyfishdemo.war3shop.entities.Alarm__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.AlarmSpecification;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/alarm")
public class AlarmController {

    @Resource
    private AlarmService alarmService;
    
    @RequestMapping("/my-alarms")
    public JsonpModelAndView myAlarms(
            AlarmSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Alarm__[] queryPaths = Alarm__.compile(queryPath);
        Page<Alarm> page = this.alarmService.getMyAlarms(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths); 
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/acknowledge-alarm")
    public JsonpModelAndView acknowledgeAlarm(
            @RequestParam("alarmId") int alarmId) {
        this.alarmService.acknowledgeAlarm(alarmId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/unacknowledge-alarm")
    public JsonpModelAndView unacknowledgeAlarm(
            @RequestParam("alarmId") int alarmId) {
        this.alarmService.unacknowledgeAlarm(alarmId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delete-alarm")
    public JsonpModelAndView deleteAlarm(
            @RequestParam("alarmId") int alarmId) {
        this.alarmService.deleteAlarm(alarmId);
        return new JsonpModelAndView(null);
    }
}
