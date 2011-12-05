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
import ch.uzh.ifi.attempto.acewiki.Wiki;

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
    private Wiki wiki;
    private Map<String, String> parameters;
    private final Ontology ontology;
    private final AceWikiEngine engine;
    private static AceWikiStorage storage;

    private Gson gson;
    static String res_ok = "{\"result\":\"ok\"}";
    static String res_error = "{\"result\":\"error\", \"reason\":\"unknown\"}";

    public Handler(Map<String, String> parameters) {
        this.parameters = parameters;

        if (storage == null) {
            storage = new FileBasedStorage(parameters.get("context:datadir"));
        }

        ontology = storage.getOntology(parameters.get("ontology"), parameters);
        engine = ontology.getEngine();

        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void process(HttpServletRequest req, HttpServletResponse res) throws
        IOException, ServletException {
        String json = res_error;
        ArrayList<String> path = new ArrayList();
        for (String s: req.getPathInfo().split("/")) {
            if (!s.equals("")) { path.add(s); }
        }
        if (path.size() > 0) {
            if (path.get(0).equals("api")) path.remove(0);
        }
        String method = req.getMethod();

        if (method == "GET") {
            json = doGet(path, req.getParameterMap());
        }
        else if (method == "POST") {
            // TODO
        }
        else if (method == "PUT") {
            // TODO
        }
        PrintWriter w = res.getWriter();
        w.write(json);
    }

    public String list(String search) {
        List<OntologyElement> elemList;
        if (search != null) {
            WordIndex index = engine.getWordIndex();
            elemList = index.searchForElements(search);
        }
        else {
            elemList = ontology.getOntologyElements();
        }

        // return { id : { word: [string], type: string } }
        HashMap<Long, Object> ret = new HashMap();
        for (OntologyElement e: elemList) {
            HashMap<String, Object> w = new HashMap();
            w.put("word", e.getWords());
            w.put("type", e.getType());
            ret.put(e.getId(), w);
        }
        return gson.toJson(ret);
    }

    public String doGet(ArrayList<String> path, Map<String, String[]> params) {
        String ret = res_error;
        //System.out.println("path=" + path);

        switch (path.size()) {
        case 0:
            String[] vals = params.get("search");
            if (vals == null) {
                ret = list(null);
            }
            else {
                ret = list(vals[0]);
            }
            break;
        case 1:
            // TODO;
            break;
        }

        return ret;
    }
}

