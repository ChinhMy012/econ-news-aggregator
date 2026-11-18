package com.econnews.collection.adapter.out.rss;

import com.econnews.collection.application.exception.FeedReadException;
import com.econnews.collection.domain.model.FeedEntry;
import java.io.ByteArrayInputStream;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.xml.sax.*;

public class SecureFeedParser {

    public List<FeedEntry> parse(byte[] bytes) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setAttribute("http://www.oracle.com/xml/jaxp/properties/maxElementDepth", 64);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("External entities disabled");
            });
            builder.setErrorHandler(
                new org.xml.sax.helpers.DefaultHandler() {
                    @Override
                    public void error(SAXParseException e) throws SAXException {
                        throw e;
                    }

                    @Override
                    public void fatalError(SAXParseException e) throws SAXException {
                        throw e;
                    }
                }
            );
            Element root = builder.parse(new ByteArrayInputStream(bytes)).getDocumentElement();
            List<FeedEntry> entries = new ArrayList<>();
            if (name(root).equals("rss")) {
                Element channel = child(root, "channel");
                if (channel == null) throw new FeedReadException("RSS_FORMAT_UNSUPPORTED");
                for (Element item : children(channel, "item"))
                    entries.add(
                        new FeedEntry(
                            text(item, "title"),
                            text(item, "link"),
                            text(item, "description"),
                            date(text(item, "pubDate"))
                        )
                    );
            } else if (
                name(root).equals("feed") &&
                "http://www.w3.org/2005/Atom".equals(root.getNamespaceURI())
            ) {
                for (Element item : children(root, "entry")) {
                    String url = null;
                    for (Element link : children(item, "link"))
                        if (
                            !link.hasAttribute("rel") ||
                            link.getAttribute("rel").equals("alternate")
                        ) {
                            url = link.getAttribute("href");
                            break;
                        }
                    String summary = text(item, "summary");
                    if (summary == null) summary = text(item, "content");
                    // updated is not substituted for published: they describe different moments.
                    entries.add(
                        new FeedEntry(
                            text(item, "title"),
                            url,
                            summary,
                            date(text(item, "published"))
                        )
                    );
                }
            } else throw new FeedReadException("RSS_FORMAT_UNSUPPORTED");
            if (entries.size() > 500) throw new FeedReadException("RSS_TOO_MANY_ENTRIES");
            return List.copyOf(entries);
        } catch (FeedReadException e) {
            throw e;
        } catch (Exception e) {
            throw new FeedReadException("RSS_XML_INVALID");
        }
    }

    private String name(Element e) {
        return e.getLocalName() == null ? e.getTagName() : e.getLocalName();
    }

    private List<Element> children(Element e, String name) {
        List<Element> found = new ArrayList<>();
        for (Node node = e.getFirstChild(); node != null; node = node.getNextSibling()) if (
            node instanceof Element child && name(child).equals(name)
        ) found.add(child);
        return found;
    }

    private Element child(Element e, String name) {
        return children(e, name).stream().findFirst().orElse(null);
    }

    private String text(Element e, String name) {
        Element found = child(e, name);
        return found == null ? null : found.getTextContent();
    }

    private Instant date(String value) {
        if (value == null || value.isBlank()) return null;
        for (var format : List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("EEE, d MMM yyyy HH:mm:ss Z")
                .toFormatter(Locale.ENGLISH)
        )) {
            try {
                return ZonedDateTime.parse(value.strip(), format).toInstant();
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}
