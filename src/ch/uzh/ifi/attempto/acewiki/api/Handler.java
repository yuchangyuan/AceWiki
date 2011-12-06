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

import java.io.IOException;

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

/**
 * This class is used by servlet to handle api call. The api is RESTful, use
 * GET to retrieve article or get a list of article, use POST to add sentence
 * to a page, use PUT to modify a sentence or add a new word(article).
 *
 * API(draft):
 * - list pages or search page P: GET / or GET /?search=P
 * - get main page or any article A(A is string or int): GET /0 or GET /A
 *   + will return a list of sentence with id
 * - create word W: PUT /W
 *   + { 'category':string, ... }
 * - add sentence on page P: POST /P
 *   + { 'statement':string, 'comment': bool, 'commit': bool }
 *     - 'commit' is optional, when ommit, is false.
 *   + return
 *     - complete: bool
 *     - candidate: { category: [string] }
 *     - id: int, when sentence commit to article, or -1
 * - delete sentence Id on page P: DELETE /P/Id
 * - modify sentence Id on page P: PUT /P/Id
 *   + the same as add sentence.
 */
public class Handler {
    private final Map<String, String> parameters;
    private final Ontology ontology;
    private final AceWikiEngine engine;
    private final Backend backend;

    public AceWikiStorage getStorage() {
        return backend.getStorage();
    }

    public Ontology getOntology() {
        return ontology;
    }

    public Handler(Backend backend) {
        this.backend = backend;
        parameters = backend.getParameters();
        ontology = backend.getOntology();
        engine = ontology.getEngine();
    }

    public Map<String,Object> list(String search) {
        List<OntologyElement> elemList;
        if (search != null) {
            WordIndex index = engine.getWordIndex();
            elemList = index.searchForElements(search);
        }
        else {
            elemList = ontology.getOntologyElements();
        }

        // return { id : { word: [string], type: string } }
        HashMap<String, Object> ret = new HashMap();
        for (OntologyElement e: elemList) {
            HashMap<String, Object> w = new HashMap();
            w.put("word", e.getWords());
            w.put("type", e.getType());
            ret.put(new Long(e.getId()).toString(), w);
        }
        return ret;
    }


    public Map<String, Object> addStatement(String word, Statement st) {
        return addStatement(ontology.getElement(word), st);
    }

    public Map<String, Object> addStatement(long id, Statement st) {
        return addStatement(ontology.get(id), st);
    }

    public Map<String, Object> addStatement(OntologyElement oe, Statement st) {
        Map<String, Object> ret = new HashMap();

        if (oe == null) {
            ret.put("result", "error");
            ret.put("reason", "no such article.");
            return ret;
        }

        return ret;
    }
}

