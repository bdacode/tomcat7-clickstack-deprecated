package com.cloudbees.genapp.tomcat7;

import java.io.File;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import com.cloudbees.genapp.metadata.Metadata;
import com.cloudbees.genapp.metadata.resource.*;

public class ContextXmlBuilder {

    private Document contextDocument;
    private Metadata metadata;
    private List<String> databaseProperties = Arrays.asList("minIdle", "maxIdle", "maxActive", "maxWait",
            "initialSize",
            "validationQuery", "validationQueryTimeout", "testOnBorrow", "testOnReturn",
            "timeBetweenEvictionRunsMillis", "numTestsPerEvictionRun", "minEvictableIdleTimeMillis", "testWhileIdle",
            "removeAbandoned", "removeAbandonedTimeout", "logAbandoned", "defaultAutoCommit", "defaultReadOnly",
            "defaultTransactionIsolation", "poolPreparedStatements", "maxOpenPreparedStatements", "defaultCatalog",
            "connectionInitSqls", "connectionProperties", "accessToUnderlyingConnectionAllowed",
            /* Tomcat JDBC Enhanced Attributes */
            "factory", "type", "validatorClassName", "initSQL", "jdbcInterceptors", "validationInterval", "jmxEnabled",
            "fairQueue", "abandonWhenPercentageFull", "maxAge", "useEquals", "suspectTimeout", "rollbackOnReturn",
            "commitOnReturn", "alternateUsernameAllowed", "useDisposableConnectionFacade", "logValidationErrors",
            "propagateInterruptState");


    public ContextXmlBuilder (Metadata metadata) {
        this.metadata = metadata;
    }

    private ContextXmlBuilder addResources(Metadata metadata) {
        for (Resource resource : metadata.getResources().values()) {
            if (resource instanceof Database) {
                addDatabase((Database) resource);
            } else if (resource instanceof Email) {
                addEmail((Email) resource);
            } else if (resource instanceof SessionStore) {
                addSessionStore((SessionStore) resource);
            }

        }
        return this;
    }

    private ContextXmlBuilder addDatabase(Database database) {
        Element e = contextDocument.createElement("Resource");
        e.setAttribute("name", "jdbc/" + database.getName());
        e.setAttribute("auth", "Container");
        e.setAttribute("type", "javax.sql.DataSource");
        e.setAttribute("url", "jdbc:" + database.getUrl());
        e.setAttribute("driverClassName", database.getJavaDriver());
        e.setAttribute("username", database.getUsername());
        e.setAttribute("password", database.getPassword());

        Map<String, String> optionalParameters = database.filterProperties(databaseProperties);
        for (Map.Entry<String, String> entry : optionalParameters.entrySet()) {
            e.setAttribute(entry.getKey(), entry.getValue());
        }

        contextDocument.getDocumentElement().appendChild(e);
        return this;
    }
    
    private ContextXmlBuilder addEmail(Email email) {
        Element e = contextDocument.createElement("Resource");
        e.setAttribute("name", email.getName());
        e.setAttribute("auth", "Container");
        e.setAttribute("type", "javax.mail.Session");
        e.setAttribute("mail.smtp.user", email.getUsername());
        e.setAttribute("mail.smtp.password", email.getPassword());
        e.setAttribute("mail.smtp.host", email.getHost());
        e.setAttribute("mail.smtp.auth", "true");

        contextDocument.getDocumentElement().appendChild(e);
        return this;
    }


    private ContextXmlBuilder addSessionStore(SessionStore store) {
        Element e = contextDocument.createElement("Manager");
        e.setAttribute("className", "de.javakaffee.web.msm.MemcachedBackupSessionManager");
        e.setAttribute("transcoderFactoryClass", "de.javakaffee.web.msm.serializer.kryo.KryoTranscoderFactory");
        e.setAttribute("memcachedProtocol", "binary");
        e.setAttribute("requestUriIgnorePattern", ".*\\.(ico|png|gif|jpg|css|js)$");
        e.setAttribute("sessionBackupAsync", "false");
        e.setAttribute("sticky", "false");
        e.setAttribute("memcachedNodes", store.getNodes());
        e.setAttribute("username", store.getUsername());
        e.setAttribute("password", store.getPassword());

        contextDocument.getDocumentElement().appendChild(e);
        return this;
    }

    private ContextXmlBuilder fromExistingDocument(Document contextDocument) {
        String rootElementName = contextDocument.getDocumentElement().getNodeName();
        if (!rootElementName.equals("Context"))
            throw new IllegalArgumentException("Document is missing root <Context> element");
        this.contextDocument = contextDocument;
        return this;
    }

    private ContextXmlBuilder fromExistingDocument(File file) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        fromExistingDocument(document);
        return this;
    }

    private Document buildContextDocument() throws ParserConfigurationException {
        if (contextDocument == null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            contextDocument = documentBuilder.newDocument();
            Element rootContextDocumentElement = contextDocument.createElement("Context");
            contextDocument.appendChild(rootContextDocumentElement);
        }

        addResources(metadata);
        return contextDocument;
    }

    public void injectToFile(String contextPath) throws Exception {
        Map<String, String> env = System.getenv();
        String contextAbsolutePath = env.get("app_dir") + contextPath;
        File contextFile = new File(contextAbsolutePath);

        Document contextXml = fromExistingDocument(contextFile).buildContextDocument();

        // Write the content into XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        transformer.transform(new DOMSource(contextXml), new StreamResult(contextFile));
    }
}
