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

import ch.uzh.ifi.attempto.base.PredictiveParser;
import ch.uzh.ifi.attempto.base.NextTokenOptions;
import ch.uzh.ifi.attempto.base.ConcreteOption;

import ch.uzh.ifi.attempto.acewiki.core.OntologyElement;
import ch.uzh.ifi.attempto.acewiki.core.WordIndex;
import ch.uzh.ifi.attempto.acewiki.core.Ontology;
import ch.uzh.ifi.attempto.acewiki.core.AceWikiEngine;
import ch.uzh.ifi.attempto.acewiki.core.AceWikiStorage;
import ch.uzh.ifi.attempto.acewiki.core.FileBasedStorage;
import ch.uzh.ifi.attempto.acewiki.core.StatementFactory;
import ch.uzh.ifi.attempto.acewiki.core.Comment;
import ch.uzh.ifi.attempto.acewiki.core.Article;
import ch.uzh.ifi.attempto.acewiki.core.LanguageHandler;


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
    private final StatementFactory sf;

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
        sf = ontology.getStatementFactory();
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

    // calc position, return range 0 ~ size
    public int calcPosition(int size, int pos) {
        if (pos > size) return size;
        if (pos >= 0) return pos;
        if (pos < -size-1) return 0;
        if (pos < 0) return size + 1 + pos;
        return size;
    }

    public Map<String, Object> addStatement(OntologyElement oe, Statement st) {
        Map<String, Object> ret = new HashMap();

        if (oe == null) {
            ret.put("result", "error");
            ret.put("reason", "no such article.");
            return ret;
        }

        ret.put("position", new Long(-1));
        ret.put("complete", true);
        ret.put("candidates", new HashMap());
        ret.put("valid", true);

        // NOTE: thread safe
        Article a = oe.getArticle();
        List<ch.uzh.ifi.attempto.acewiki.core.Statement>
            stList = a.getStatements();
        // calc position
        int pos = calcPosition(stList.size(), st.getPosition());

        if (st.getComment()) {
            if (st.getCommit()) {
                // update position
                ret.put("position", pos);
                Comment c = sf.createComment(st.getStatement(), a);
                a.add((pos < stList.size()) ? stList.get(pos) : null, c);
            }

            return ret;
        }

        Candidates can = getCandidates(st.getStatement());
        ret.put("valid", can.valid);
        ret.put("complete", can.complete);
        ret.put("tokens", can.tokens);
        ret.put("remain", can.remain);
        ret.put("candidates", can.candidates);

        if (st.getCommit() && can.complete) {
            // TODO: commit

        }

        return ret;
    }

    public static class Candidates {
        boolean valid;
        boolean complete;
        List<String> tokens;
        String remain;
        Map<String, ArrayList<String>> candidates;

        public Candidates() {
            valid = true;
            complete = false;
            candidates = new HashMap();
            tokens = new ArrayList();
            remain = "";
        }
    };

    // synchronized
    public synchronized Candidates getCandidates(String text) {
        LanguageHandler lh = engine.getLanguageHandler();
        PredictiveParser pp = lh.getPredictiveParser();
        pp.removeAllTokens();
        return getCandidates(pp, lh.getTextOperator().splitIntoTokens(text));
    }

    // here, should not synchronized
    private Candidates getCandidates(PredictiveParser pp, List<String> subtoks) {
        Candidates ret = new Candidates();

        String text = "";
        while (subtoks.size() > 0) {
            if (text.length() > 0) text += " ";
            text += subtoks.remove(0);

            if (pp.isPossibleNextToken(text)) {
                pp.addToken(text);
                ret.tokens.add(text);
                text = "";
            }
        }

        ret.remain = text;
        if (ret.remain.equals("")) {
            ret.valid = true;
            ret.complete = pp.isComplete();

            if (!ret.complete) {
                for (ConcreteOption o: pp.getNextTokenOptions().getConcreteOptions()) {
                    String cat = o.getCategoryName();
                    String word = o.getWord();

                    if (cat == null) {
                        cat = "";
                    }
                    if (ret.candidates.get(cat) == null) {
                        ret.candidates.put(cat, new ArrayList<String>());
                    }
                    ret.candidates.get(cat).add(word);
                }
            }
        }
        else {
            ret.valid = false;
            ret.complete = false;
        }

        return ret;
    }
}

