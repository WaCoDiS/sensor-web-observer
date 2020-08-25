/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.observer.decode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author matthes
 */
public class SimpleNamespaceContext implements NamespaceContext {

    private final Map<String, String> PREF_MAP = new HashMap<>();

    public SimpleNamespaceContext(final Map<String, String> prefMap) {
        PREF_MAP.putAll(prefMap);       
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return PREF_MAP.get(prefix);
    }

    @Override
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}