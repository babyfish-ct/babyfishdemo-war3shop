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
package org.babyfishdemo.war3shop.web.js;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
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
import org.babyfish.lang.Nulls;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Tao Chen
 */
@WebServlet(
        name = "templateScript", 
        urlPatterns = "/template-script.js",
        initParams = {
                /* (1) For developing mode, please use this parameter to make the changes 
                 * of template files does NOT require restart web server;
                 * 
                 * (2) For product mode, please delete this parameter to optimize the 
                 * performance, but the changes of template files require restart web server.
                 */
                @WebInitParam(name = "dynamic-loadable", value = "true")
        }
)
public class TemplateScriptServlet extends HttpServlet {

    private static final long serialVersionUID = 6670878645574758474L;
    
    private static final Log LOG = LogFactory.getLog(TemplateScriptServlet.class);
    
    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
    
    private static final String JS_NS = "http://www.babyfishdemo.org/javascript";
    
    private static final String GENERATE_CLIENT_ID = "generate-client-id";
    
    private String templateLocation;
    
    private String script;
    
    private WatchService watchService;
    
    private ReadWriteLock scriptLock;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String templateLocation = config.getInitParameter("template-location");
        if (Nulls.isNullOrEmpty(templateLocation)) {
            templateLocation = "/WEB-INF/templates";
        }
        templateLocation = this.getServletContext().getRealPath(templateLocation);
        this.templateLocation = templateLocation;
        
        if (!"true".equalsIgnoreCase(config.getInitParameter("dynamic-loadable"))) {
            this.script = this.createScript();
            return;
        }
        
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.scriptLock = new ReentrantReadWriteLock();
            Path path = Paths.get(this.templateLocation);
            path.register(
                    this.watchService, 
                    StandardWatchEventKinds.ENTRY_CREATE, 
                    StandardWatchEventKinds.ENTRY_DELETE, 
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
        new Thread() {
            @Override
            public void run() {
                TemplateScriptServlet that = TemplateScriptServlet.this;
                WatchService watchService = that.watchService;
                while (true) {
                    try {
                        WatchKey watchKey = watchService.take();
                        
                        //It's very important to register the watched path again
                        //I don't want to use try-finally for reset so I reset it as soon as possible
                        watchKey.reset();
                        
                        List<WatchEvent<?>> events = watchKey.pollEvents();
                        for (WatchEvent<?> event : events) {
                            Path path = (Path)event.context();
                            LOG.info(
                                    "The file \"" + path.toString() + "\" has been changed(" +
                                    event.kind().name() +
                                    "), clear the cache of script");
                        }
                        if (!events.isEmpty()) {
                            Lock lock;
                            (lock = that.scriptLock.writeLock()).lock();
                            try {
                                that.script = null; //Clear the cache
                            } finally {
                                lock.unlock();
                            }
                        }
                    } catch (ClosedWatchServiceException | InterruptedException e) {
                        LOG.info("File system watch is stopped");
                        return;
                    }
                }
            }
        }.start();
        LOG.info("Start to watch the file system change of dir \"" + this.templateLocation + "\"");
    }
    
    @Override
    public void destroy() {
        WatchService ws = this.watchService;
        if (ws != null) {
            this.watchService = null;
            try {
                ws.close(); //The watch thread will exit automatically because of this closing operation.
            } catch (IOException ex) {
                LOG.error(ex);
            }
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result;
        if (this.watchService == null) {
            result = this.script;
        } else {
            Lock lock;
            (lock = this.scriptLock.readLock()).lock(); //1st locking
            try {
                result = this.script; //1st reading
            } finally {
                lock.unlock();
            }
            if (result == null) { //1st checking
                (lock = this.scriptLock.writeLock()).lock(); //2nd locking
                try {
                    result = this.script; //2nd reading
                    if (result == null) { //2nd checking
                        this.script = result = this.createScript();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        resp.setContentType("text/javascript");
        resp.getWriter().print(result);
        resp.getWriter().flush();
    }
    
    private String createScript() throws ServletException {
        File dir = new File(this.templateLocation);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new ServletException(
                    "The real path \"" +
                    this.templateLocation +
                    "\" of the init parameter \"template-location\" for \"" +
                    TemplateScriptServlet.class +
                    "\" must be a directory.");
        }
        
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        saxParserFactory.setValidating(false);
        SAXParser saxParser;
        try {
            saxParser = saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException ex) {
            throw new ServletException("Can not create " + SAXParser.class.getName(), ex);
        }
        
        StringBuilder builder = new StringBuilder();
        builder
        .append("var createTemplate = function(templateName, param) {\n")
        .append("var r = function(elementName) { return document.createElement(elementName); };\n")
        .append("var e = function(parent, elementName) { var e = document.createElement(elementName); parent.appendChild(e); return e; };\n")
        .append("var a = function(element, name, value) { element.setAttribute(name, value); };\n")
        .append("var a2 = function(jq, name, value) { jq.attr(name, value); };\n")
        .append("var t = function(parent, text) { parent.appendChild(document.createTextNode(text)); };\n")
        .append("var i = function(parent, templateName, param) { \n")
        .append("var e = createTemplate(templateName, param); \n")
        .append("if (e.parent().length != 0) { \n")
        .append("throw new Error(\n")
        .append("\"The template \" + \n")
        .append("templateName + \n")
        .append("\" is self managment so that it cant not be used by <js:include/> to reference it.\"); \n")
        .append("} \n")
        .append("e.appendTo(parent); \n")
        .append("return e; };\n")
        .append("if(isNaN(createTemplate.clientIdSequence) || createTemplate.clientIdSequence < 0) {\n")
        .append("createTemplate.clientIdSequence = 0;\n")
        .append("}\n")
        .append("if(typeof(param) == \"undefined\"){\n")
        .append("param={};\n")
        .append("}\n");
        
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".xhtml")) {
                String templateName = file.getName().substring(0, file.getName().length() - 6);
                builder
                .append("if(templateName == \"")
                .append(templateName).append("\"){\n")
                .append("if(typeof(")
                .append(templateName)
                .append(")==\"undefined\"){\n")
                .append("throw new Error(\"")
                .append(file.getName())
                .append(" requires the java script class ")
                .append(templateName)
                .append("\");\n")
                .append("}\n");
                createTemplateScript(saxParser, file, templateName, builder);
                builder.append("}\n");
            }
        }
        
        builder
        .append("throw new Error(\"Invalid templateName: \\\"\" + templateName + \"\\\"\");\n")
        .append("}\n");
        
        return builder.toString();
    }
    
    private static void createTemplateScript(SAXParser saxParser, File file, String templateName, StringBuilder builder) throws ServletException {
        try {
            saxParser.parse(file, new HandlerImpl(builder, file, templateName));
        } catch (IOException | SAXException ex) {
            throw new ServletException("Can not parse the template: " + file.getAbsolutePath(), ex);
        }
        LOG.info("Genearted javscript for template: " + file.getAbsolutePath());
    }
    
    private static class HandlerImpl extends DefaultHandler {
        
        private static final Pattern IDENTIFER_PATTERN = Pattern.compile("^[A-Za-z_$][A-Za-z0-9_$]*$");
        
        private StringBuilder builder;
        
        private int variableSequnce;
        
        private File file;
        
        private String templateName;
        
        private List<String> stack = new ArrayList<>();
        
        private Set<String> jsIdSet = new HashSet<>();
        
        private boolean parsingJsInclude = false;
        
        HandlerImpl(StringBuilder builder, File file, String templateName) {
            this.builder = builder;
            this.file = file;
            this.templateName = templateName;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!isUIElement(uri, localName)) {
                return;
            }
            if (this.parsingJsInclude) {
                throw new SAXException("<js:include/> can not contain child elements");
            }
            boolean isHTML = XHTML_NS.equals(uri);
            if (!isHTML) {
                if (this.stack.isEmpty()) {
                    throw new SAXException(
                            "The root element of \"" +
                            this.file.getAbsolutePath() +
                            "\"must be HTML element.");
                }
                this.parsingJsInclude = true;
            }
            StringBuilder builder = this.builder;
            
            String variableName = "e" + this.variableSequnce++;
            this.stack.add(variableName);
            
            if (this.stack.size() == 1) {
                builder
                .append("var c=new ")
                .append(this.templateName)
                .append("();\n")
                .append("var e0 = r(\"")
                .append(localName.toUpperCase())
                .append("\");\n");
            } else {
                if (isHTML) {
                    builder
                    .append("var ")
                    .append(variableName)
                    .append("=e(")
                    .append(this.stack.get(this.stack.size() - 2))
                    .append(",\"")
                    .append(localName.toUpperCase())
                    .append("\");\n");
                } else {
                    String src = attributes.getValue(XMLConstants.NULL_NS_URI, "src");
                    if (src == null) {
                        throw new SAXException(
                                "The <js:include/> element of \"" +
                                this.file.getAbsolutePath() +
                                "\"must has attribute \"src\".");
                    }
                    String param = attributes.getValue(XMLConstants.NULL_NS_URI, "param");
                    builder
                    .append("var ")
                    .append(variableName)
                    .append("=i(")
                    .append(this.stack.get(this.stack.size() - 2))
                    .append(",\"")
                    .append(src)
                    .append("\",");
                    if (param != null) {
                        builder.append(param);
                    } else {
                        builder.append("{}");
                    }
                    builder.append(");\n");
                }
            }
            
            int attrSize = attributes.getLength();
            boolean generateClientId = false;
            String jsId = null;
            boolean hasHandlers = false;
            for (int i = 0; i < attrSize; i++) {
                String attrUri = attributes.getURI(i);
                String name = attributes.getLocalName(i).toLowerCase();
                String value = attributes.getValue(i);
                if (javax.xml.XMLConstants.NULL_NS_URI.equals(attrUri)) {
                    if ("id".equals(name)) {
                        throw new SAXException(
                                "The html tag declared in \"" + 
                                this.file.getAbsolutePath() + 
                                "\" can not declared \"id\" attribute, please use \"js:id\"(xmlns:js=\"" +
                                JS_NS +
                                "\")");
                    }
                    if ("name".equals(name)) {
                        throw new SAXException(
                                "The html tag declared in \"" + 
                                this.file.getAbsolutePath() + 
                                "\" can not declared \"name\" attribute, please use \"js:id\"(xmlns:js=\"" +
                                JS_NS +
                                "\")");
                    }
                    if (name.startsWith("on")) {
                        throw new SAXException(
                                "The html tag declared in \"" + 
                                this.file.getAbsolutePath() + 
                                "\" can not declared event handler attribute, please use the special event handler such as \"js:click\"(xmlns:js=\"" +
                                JS_NS +
                                "\")");
                    }
                    if (isHTML || (!"src".equals(name) && !"param".equals(name))) {
                        builder
                        .append(isHTML ? "a(" : "a2(")
                        .append(variableName)
                        .append(", \"")
                        .append(name)
                        .append("\", \"")
                        .append(value.replace("\\", "\\\\"))
                        .append("\");\n");
                    }
                } else if (JS_NS.equals(attrUri)) {
                    if (GENERATE_CLIENT_ID.equals(name)) {
                        if (!"true".equals(value)) {
                            throw new SAXException(
                                    "The attribute \"js:" +
                                    GENERATE_CLIENT_ID +
                                    "\" of html tag declared in \"" + 
                                    this.file.getAbsolutePath() + 
                                    "is invalid, its value must be \"true\"");
                        }
                        generateClientId = true;
                    } else {
                        if (!IDENTIFER_PATTERN.matcher(name).matches()) {
                            throw new SAXException(
                                    "The html tag declared in \"" + 
                                    this.file.getAbsolutePath() + 
                                    "\" can not has an attribute name \"" +
                                    name +
                                    "\", it must match the regex \"" +
                                    IDENTIFER_PATTERN.toString() +
                                    "\"");
                        }
                        if (!IDENTIFER_PATTERN.matcher(value).matches()) {
                            throw new SAXException(
                                    "The html tag declared in \"" + 
                                    this.file.getAbsolutePath() + 
                                    "\" can not has an event handler attribute value \"" +
                                    value +
                                    "\", it must match the regex \"" +
                                    IDENTIFER_PATTERN.toString() +
                                    "\"");
                        }
                        if ("id".equals(name)) {
                            if (value.equals("root")) {
                                throw new SAXException(
                                        "There is element whose js:id is \"root\" in \"" +
                                        this.file.getAbsolutePath() +
                                        "\", this is not allowed");
                            }
                            if (!this.jsIdSet.add(value)) {
                                throw new SAXException(
                                        "Duplated js:id \"" +
                                        value +
                                        "\" in \"" + 
                                        this.file.getAbsolutePath() +
                                        "\"");
                            }
                            jsId = value;
                        } else {
                            hasHandlers = true;
                        }
                    }
                }
            }
            if (jsId != null) {
                builder
                .append("c.")
                .append(jsId)
                .append("=$(")
                .append(variableName)
                .append(");\n");
            }
            if (generateClientId) {
                if (localName.equals("input") || localName.equals("select") || localName.equals("textarea")) {
                    builder
                    .append(isHTML ? "a(" : "a2(")
                    .append(variableName)
                    .append(", \"name\", \"")
                    .append("n_\" + createTemplate.clientIdSequence);\n");
                }
                builder
                .append(isHTML ? "a(" : "a2(")
                .append(variableName)
                .append(", \"id\", \"i_\" + createTemplate.clientIdSequence++);\n");
            }
            if (hasHandlers) {
                builder
                .append("$(")
                .append(variableName)
                .append(")");
                for (int i = 0; i < attrSize; i++) {
                    String attrUri = attributes.getURI(i);
                    String name = attributes.getLocalName(i).toLowerCase();
                    String value = attributes.getValue(i);
                    if (JS_NS.equals(attrUri)) {
                        if (!"id".equals(name) && !GENERATE_CLIENT_ID.equals(name)) {
                            builder
                            .append("\n.bind(\"")
                            .append(name)
                            .append("\",function(e){\n")
                            .append("c.")
                            .append(value)
                            .append("(e);\n")
                            .append("})");
                        }
                    }
                }
                builder.append(";\n");
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!isUIElement(uri, localName)) {
                return;
            }
            this.stack.remove(this.stack.size() - 1);
            if (this.stack.isEmpty()) {
                this
                .builder
                .append("(c.root=$(e0)).data(\"controller\",c);\n")
                .append("if(c.constructor.clinit){\n")
                .append("c.constructor.clinit();\n")
                .append("delete c.constructor.clinit;\n")
                .append("}\n")
                .append("if(c.init){\n")
                .append("c.init(param);\n")
                .append("}\n")
                .append("return $(e0);\n");
            }
            this.parsingJsInclude = false;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.stack.isEmpty()) {
                return;
            }
            
            boolean empty = true;
            for (int i = start + length - 1; i >= start; i--) {
                if (!Character.isWhitespace(ch[i])) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                return;
            }
            
            StringBuilder builder = this.builder;
            
            String id = this.stack.get(this.stack.size() - 1);
            builder.append("t(").append(id).append(", \"");
            for (int i = 0; i < length; i++) {
                char c = ch[start + i];
                switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                default:
                    builder.append(c);
                    break;
                }
            }
            builder.append("\");\n");
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

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            return new InputSource(new StringReader(""));
        }
        
        private static boolean isUIElement(String uri, String localName) {
            return XHTML_NS.equals(uri) || (JS_NS.equals(uri) && localName.equals("include"));
        }
    }
}
