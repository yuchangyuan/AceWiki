// Copyright 2011, Yu Changyuan
//
// AceWiki is free software: you can redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// AceWiki is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
// even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License along with AceWiki. If
// not, see http://www.gnu.org/licenses/.

package ch.uzh.ifi.attempto.acewiki.api;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Reader;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import ch.uzh.ifi.attempto.acewiki.core.OntologyElement;
import ch.uzh.ifi.attempto.acewiki.core.WordIndex;
import ch.uzh.ifi.attempto.acewiki.core.Ontology;
import ch.uzh.ifi.attempto.acewiki.core.AceWikiEngine;
import ch.uzh.ifi.attempto.acewiki.core.AceWikiStorage;
import ch.uzh.ifi.attempto.acewiki.core.FileBasedStorage;

public class Backend {
    private final Ontology ontology;
    private static AceWikiStorage storage;
    private final Map<String, String> parameters;

    public Backend(Map<String, String> parameters) {
        this.parameters = parameters;
        if (storage == null) {
            storage = new FileBasedStorage(parameters.get("context:datadir"));
        }
        ontology = getStorage().getOntology(parameters.get("ontology"), parameters);
    }

    public AceWikiStorage getStorage() { return storage; }
    public Ontology getOntology() { return ontology; }
    public Map<String, String> getParameters() { return parameters; }
    public String getParameter(String param) { return parameters.get(param); }
}
