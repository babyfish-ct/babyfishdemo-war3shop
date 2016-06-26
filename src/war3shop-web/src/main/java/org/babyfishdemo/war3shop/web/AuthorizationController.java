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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.UploadService;
import org.babyfishdemo.war3shop.entities.AccountManager;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator__;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.Customer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege__;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role__;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.User_;
import org.babyfishdemo.war3shop.entities.User__;
import org.babyfishdemo.war3shop.entities.specification.AdministratorSpecification;
import org.babyfishdemo.war3shop.entities.specification.CustomerSpecification;
import org.babyfishdemo.war3shop.web.captcha.CaptchaHolder;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/authorization")
public class AuthorizationController implements ServletContextAware {
    
    private static final Log log = LogFactory.getLog(AuthorizationController.class);
    
    private List<SiteMapNode> allRootSiteMapNodes;

    @Resource
    private AuthorizationService authorizationService;
    
    @Resource
    private UploadService uploadService;
    
    @Resource(name = "loginCaptchaHolder")
    private CaptchaHolder loginCaptchaHolder;
    
    @Resource(name = "registerCaptchaHolder")
    private CaptchaHolder registerCaptchaHolder;
    
    @RequestMapping("/is-registerable")
    public JsonpModelAndView isRegisterable(
            @RequestParam(value = "name", required = true) String name) {
        return new JsonpModelAndView(this.authorizationService.isRegisterable(name));
    }
    
    @RequestMapping("/login")
    public JsonpModelAndView login(
            @RequestParam("name") String name, 
            @RequestParam("password") String password,
            @RequestParam("captcha") String captcha) {
        this.loginCaptchaHolder.validate(captcha);
        User user = this.authorizationService.login(name, password);
        return new JsonpModelAndView(user);
    }
    
    @RequestMapping("/logout")
    public JsonpModelAndView logout() {
        this.authorizationService.logout();
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/register")
    public JsonpModelAndView register(
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            @RequestParam("passwordAgain") String passwordAgain,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("captcha") String captcha) {
        
        this.registerCaptchaHolder.validate(captcha);
        if (!password.equals(passwordAgain)) {
            throw new IllegalArgumentException("The passwords are not same");
        }
        
        Customer customer = new Customer();
        JPAEntities.disableAll(customer);
        
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        
        TemporaryUploadedFile temporaryUploadedFile = this.uploadService.getAndDeleteUploadedImage(
                TemporaryUploadedFile__.begin().content().end());
        if (temporaryUploadedFile != null) {
            customer.setImageMimeType(temporaryUploadedFile.getMimeType());
            customer.setImage(temporaryUploadedFile.getContent());
        }
        
        this.authorizationService.register(customer, password);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/current-user")
    public JsonpModelAndView currentUser(@RequestParam(value = "queryPath", required = false) String queryPath) {
        User__[] queryPaths = User__.compile(queryPath);
        return new JsonpModelAndView(this.authorizationService.getCurrentUser(queryPaths));
    }
    
    @RequestMapping("/configure-current-user")
    public JsonpModelAndView configureCurrentUser(
            @RequestParam("version") int version,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address) {
        
        User currentUser = this.authorizationService.getCurrentUser();
        
        JPAEntities.disableAll(currentUser);
        currentUser.setVersion(version);
        currentUser.setName(name);
        currentUser.setEmail(email);
        if (currentUser instanceof Customer) {
            ((Customer)currentUser).setPhone(phone);
            ((Customer)currentUser).setAddress(address);
        }
        
        TemporaryUploadedFile temporaryUploadedFile = this.uploadService.getAndDeleteUploadedImage(
                TemporaryUploadedFile__.begin().content().end());
        if (temporaryUploadedFile != null) {
            currentUser.setImageMimeType(temporaryUploadedFile.getMimeType());
            currentUser.setImage(temporaryUploadedFile.getContent());
        }
        
        this.authorizationService.configureCurrentUser(currentUser);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/change-password")
    public JsonpModelAndView changePassword(
            @RequestParam("version") int version,
            @RequestParam("originalPassword") String originalPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("newPasswordAgain") String newPasswordAgain) {
        
        User currentUser = this.authorizationService.getCurrentUser();
        
        JPAEntities.disableAll(currentUser);
        currentUser.setVersion(version);
        JPAEntities.enable(currentUser, User_.password);
        
        this.authorizationService.changePassword(currentUser, originalPassword, newPassword);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/sitemapnodes")
    public JsonpModelAndView siteMapDataSource() {
        Set<String> permissionNames = null;
        User currentUser = this.authorizationService.getCurrentUser();
        if (currentUser != null) {
            permissionNames = new HashSet<>();
            if (currentUser instanceof AccountManager) {
                permissionNames.add(Constants.ACCOUNT_MANAGER_PERMISSION);
            } else if (currentUser instanceof Administrator) {
                Administrator administrator = this.authorizationService.getCurrentAdministrator(
                        Administrator__.begin().roles().privileges().end(),
                        Administrator__.begin().privileges().end());
                if (administrator != null) {
                    for (Role role : administrator.getRoles()) {
                        for (Privilege privilege : role.getPrivileges()) {
                            permissionNames.add(privilege.getName());
                        }
                    }
                    for (Privilege privilege : administrator.getPrivileges()) {
                        permissionNames.add(privilege.getName());
                    }
                }
            } else {
                permissionNames.add(Constants.CUSTOMER_PERMISSION);
            }
        }
        List<SiteMapNode> usedRootSiteMapNodes = new ArrayList<>(this.allRootSiteMapNodes.size());
        for (SiteMapNode rootNode : this.allRootSiteMapNodes) {
            SiteMapNode usedRootNode = rootNode.cloneUsedNodes(permissionNames);
            if (usedRootNode != null) {
                usedRootSiteMapNodes.add(usedRootNode);
            }
        }
        return new JsonpModelAndView(usedRootSiteMapNodes);
    }
    
    @RequestMapping("/all-roles")
    public JsonpModelAndView allRoles(@RequestParam(value = "queryPath", required=false) String queryPath) {
        Role__[] queryPaths = Role__.compile(queryPath);
        List<Role> roles = 
                this
                .authorizationService
                .getAllRoles(queryPaths);
        return new JsonpModelAndView(roles);
    }
    
    @RequestMapping("/all-privileges")
    public JsonpModelAndView allPrivileges(@RequestParam(value = "queryPath", required=false) String queryPath) {
        Privilege__[] queryPaths = Privilege__.compile(queryPath);
        List<Privilege> privileges = 
                this
                .authorizationService
                .getAllPrivileges(queryPaths);
        return new JsonpModelAndView(privileges);
    }
    
    @RequestMapping("/privilege-by-name")
    public JsonpModelAndView privilegeByName(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "queryPath", required=false) String queryPath) {
        Privilege__[] queryPaths = Privilege__.compile(queryPath);
        Privilege privilege = 
                this
                .authorizationService
                .getPrivilegeByName(name, queryPaths);
        return new JsonpModelAndView(privilege);
    }
    
    @RequestMapping("/customers")
    public JsonpModelAndView customers(
            CustomerSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Customer__[] queryPaths = Customer__.compile(queryPath);
        Page<Customer> page = this.authorizationService.getCustomers(
                        specification, 
                        pageIndex, 
                        pageSize, 
                        queryPaths);
        return new JsonpModelAndView(page);
    }

    @RequestMapping("/administrators")
    public JsonpModelAndView administrators(
            AdministratorSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Administrator__[] queryPaths = Administrator__.compile(queryPath);
        Page<Administrator> page = this.authorizationService.getAdministrators(
                specification,
                pageIndex, 
                pageSize,
                queryPaths);
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/administrator")
    public JsonpModelAndView administrator(
            @RequestParam("id") long id, 
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Administrator__[] queryPaths = Administrator__.compile(queryPath);
        Administrator administrator = this.authorizationService.getAdministratorById(
                id, 
                queryPaths);
        return new JsonpModelAndView(administrator);
    }
    
    @RequestMapping("/active-customer")
    public JsonpModelAndView activeCustomer(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "version") int version,
            @RequestParam("active") boolean active) {
        Customer customer = new Customer();
        JPAEntities.disableAll(customer);   
        customer.setId(id);
        customer.setVersion(version);
        customer.setActive(active);
        this.authorizationService.activeCustomer(customer);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/submit-administrator")
    public JsonpModelAndView submitAdministrator(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "version", defaultValue = "0") int version,
            @RequestParam("name") String name,
            @RequestParam("active") boolean active,
            @RequestParam("email") String email,
            @RequestParam("roleIds") Collection<Long> roleIds,
            @RequestParam("privilegeIds") Collection<Long> privilegeIds) {
        
        Administrator administrator = new Administrator();
        JPAEntities.disableAll(administrator);
        
        administrator.setId(id);
        administrator.setVersion(version);
        if (!Nulls.isNullOrEmpty(name)) {
            // updating name is optional operation.
            administrator.setName(name);
        }
        administrator.setActive(active);
        administrator.setEmail(email);
        
        administrator.setRoles(
                JPAEntities.createFakeEntities(Role.class, roleIds)
        );
        administrator.setPrivileges(
                JPAEntities.createFakeEntities(Privilege.class, privilegeIds)
        );
        
        this.authorizationService.submitAdministrator(administrator);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delete-user")
    public JsonpModelAndView deleteUser(@RequestParam("id") long id) {
        this.authorizationService.deleteUser(id);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/update-role")
    public JsonpModelAndView updateRole(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "version", required = false) Integer version, 
            @RequestParam("name") String name,
            @RequestParam("privilegeIds") Collection<Long> privilegeIds) {
        this.authorizationService.updateRole(id, version, name, privilegeIds);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delete-role")
    public JsonpModelAndView deleteRole(long id) {
        this.authorizationService.deleteRole(id);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/login-captcha")
    public void loginCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCaptchaHeader(response);
        this.loginCaptchaHolder.generateImage(response.getOutputStream());
    }
    
    @RequestMapping("/register-captcha")
    public void registerCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCaptchaHeader(response);
        this.registerCaptchaHolder.generateImage(response.getOutputStream());
    }
    
    @Override
    public void setServletContext(ServletContext ctx) {
        String siteMapFilePath = ctx.getRealPath("/WEB-INF/site-map.xml");
        log.info("Parsing site-map: " + siteMapFilePath);
        try {
            try (InputStream xmlStream = new FileInputStream(siteMapFilePath)) {
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setNamespaceAware(true);
                SAXParser saxParser = saxParserFactory.newSAXParser();
                HandlerImpl handler = new HandlerImpl();
                saxParser.parse(xmlStream, handler);
                this.allRootSiteMapNodes = MACollections.unmodifiable(handler.rootNodes);
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            log.error("Failed to parse site-map: " + siteMapFilePath, ex);
            UncheckedException.rethrow(ex);
        }
    }
    
    private static void setCaptchaHeader(HttpServletResponse response) {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        long time = System.currentTimeMillis();
        response.setDateHeader("Last-Modified", time);
        response.setDateHeader("Date", time);
        response.setDateHeader("Expires", time);
    }
    
    private static class SiteMapNode {
        
        /*
         * Public fields can be serialized by alibaba fastjson
         */
        public String nodeId;
        
        public String html;
        
        public String text;
        
        public String template;
        
        public String click;
        
        public String permission;
        
        public List<SiteMapNode> childNodes;
        
        boolean visibleForGuest; //Not public field for alibaba fastjson, js client does not need it.
    
        void add(SiteMapNode childNode) {
            List<SiteMapNode> childNodes = this.childNodes;
            if (childNodes == null) {
                this.childNodes = childNodes = new ArrayList<>();
            }
            childNodes.add(childNode);
        }
        
        void finish() {
            List<SiteMapNode> childNodes = this.childNodes;
            if (childNodes != null) {
                this.childNodes = MACollections.unmodifiable(this.childNodes);
            } else {
                this.childNodes = MACollections.emptyList();
            }
        }
        
        SiteMapNode cloneUsedNodes(Collection<String> privilegeNames) {
            Set<SiteMapNode> usedNodes = new HashSet<>(ReferenceEqualityComparator.getInstance());
            this.findUsedNodes(privilegeNames, usedNodes);
            return this.cloneUsedNodesImpl(usedNodes);
        }
        
        private boolean findUsedNodes(Collection<String> permissionNames, /* output */Set<SiteMapNode> usedNodes) {
            boolean used;
            if (this.childNodes.isEmpty()) {
                if (permissionNames == null) {
                    used = this.visibleForGuest;
                } else {
                    used = Nulls.isNullOrEmpty(this.permission) || 
                            permissionNames.contains(this.permission);
                }
            } else {
                used = false;
                for (SiteMapNode childNode : this.childNodes) {
                    used |= childNode.findUsedNodes(permissionNames, usedNodes);
                }
            }
            if (used) {
                usedNodes.add(this);
            }
            return used;
        }
        
        private SiteMapNode cloneUsedNodesImpl(Set<SiteMapNode> usedNodes) {
            if (!usedNodes.contains(this)) {
                return null;
            }
            SiteMapNode clonedNode = new SiteMapNode();
            clonedNode.nodeId = this.nodeId; 
            clonedNode.html = this.html;
            clonedNode.text = this.text;
            clonedNode.permission = this.permission;
            clonedNode.template = this.template;
            clonedNode.click = this.click;
            clonedNode.visibleForGuest = this.visibleForGuest;
            for (SiteMapNode childNode : this.childNodes) {
                SiteMapNode clonedChildNode = childNode.cloneUsedNodesImpl(usedNodes);
                if (clonedChildNode != null) {
                    clonedNode.add(clonedChildNode);
                }
            }
            clonedNode.finish();
            return clonedNode;
        }
    }

    private static class HandlerImpl extends DefaultHandler {
        
        private Deque<SiteMapNode> stack = new ArrayDeque<>();
        
        private List<SiteMapNode> rootNodes = new ArrayList<>();
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (XMLConstants.NULL_NS_URI.equals(uri) && "site-map-node".equals(localName)) {
                SiteMapNode node = new SiteMapNode();
                node.nodeId = attributes.getValue(XMLConstants.NULL_NS_URI, "node-id");
                node.text = attributes.getValue(XMLConstants.NULL_NS_URI, "text");
                node.template = attributes.getValue(XMLConstants.NULL_NS_URI, "template");
                node.click = attributes.getValue(XMLConstants.NULL_NS_URI, "click");
                node.permission = attributes.getValue(XMLConstants.NULL_NS_URI, "permission");
                node.visibleForGuest = "true".equals(attributes.getValue(XMLConstants.NULL_NS_URI, "visible-for-guest"));
                if (this.stack.isEmpty()) {
                    this.rootNodes.add(node);
                }
                if (node.text == null) {
                    throw new SAXException("<site-map-node/> requires attribute \"text\"");
                }
                this.stack.push(node);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (XMLConstants.NULL_NS_URI.equals(uri) && "site-map-node".equals(localName)) {
                SiteMapNode node = this.stack.peek();
                if (node.childNodes != null) {
                    if (node.permission != null) {
                        throw new SAXException("<site-map-node/> with child nodes can not have attribute \"privilege\"");
                    }
                    if (node.template != null) {
                        throw new SAXException("<site-map-node/> with child nodes can not have attribute \"template\"");
                    }
                    if (node.click != null) {
                        throw new SAXException("<site-map-node/> with child nodes can not have attribute \"click\"");
                    }
                } else if (node.template == null && node.click == null) {
                    throw new SAXException("<site-map-node/> without child nodes and attribute \"click\" requires the attribute \"template\" or \"click\"");
                }
                node.finish();
                this.stack.pop();
                if (!this.stack.isEmpty()) {
                    SiteMapNode parentNode = this.stack.peek();
                    parentNode.add(node);
                }
            }
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}
