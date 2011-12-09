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
import ch.uzh.ifi.attempto.base.TextContainer;
import ch.uzh.ifi.attempto.base.TextElement;

import ch.uzh.ifi.attempto.acewiki.core.OntologyElement;
import ch.uzh.ifi.attempto.acewiki.core.WordIndex;
import ch.uzh.ifi.attempto.acewiki.core.Ontology;
import ch.uzh.ifi.attempto.acewiki.core.AceWikiEngine;
import ch.uzh.ifi.attempto.acewiki.core.AceWikiStorage;
import ch.uzh.ifi.attempto.acewiki.core.FileBasedStorage;
import ch.uzh.ifi.attempto.acewiki.core.StatementFactory;
import ch.uzh.ifi.attempto.acewiki.core.Comment;
import ch.uzh.ifi.attempto.acewiki.core.Article;
import ch.uzh.ifi.attempto.acewiki.core.Sentence;
import ch.uzh.ifi.attempto.acewiki.core.LanguageHandler;


/**
 * This class is used by servlet to handle api call. The api is RESTful, use
 * GET to retrieve article or get a list of article, use POST to add sentence
 * to a page, use PUT to modify a sentence or add a new word(article).
 *
 * API(draft):
 * - list pages or search page P: GET / or GET /?search=P
 * - get main page or any article A(A is string or int): GET /0 or GET /A
 *   + will return a list of statement with id or word.
 * - create word W: PUT /W
 *   + { 'category':string, ... }
 * - add statement on page P: POST /P
 *   + { 'statement':string, 'comment': bool, 'commit': bool }
 *     - 'commit' is optional, when ommit, is false.
 *     - 'comment' is optional, when ommit, is false.
 *   + return
 *     - complete: bool
 *     - candidate: { category: [string] }
 *     - valid: whether statement is valid sentence.
 *     - tokens: valid token list of the sentence.
 *     - remain: remain of the sentence which can not parse.
 *     - position: int, position in article when commit, or -1
 * - delete statement with position Pos on page P: DELETE /P/Pos
 * - modify statement with position Pos on page P: PUT /P/Pos
 *   + the same as add statement.
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

        // return { id : { words: [string], type: string } }
        HashMap<String, Object> ret = new HashMap();
        for (OntologyElement e: elemList) {
            HashMap<String, Object> w = new HashMap();
            w.put("words", e.getWords());
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

        // NOTE: thread safe, article position might change
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

        if (st.getCommit()) {
            if (!can.complete) {
                ret.put("result", "error");
                ret.put("reason", "sentence not complete.");
            }
            else if (!can.valid) {
                ret.put("result", "error");
                ret.put("reason", "sentence not valid.");
            }
            else {
                ret.put("position", pos);
                commitSentence(a, (pos < stList.size()) ? stList.get(pos) : null,
                               can.tokens);
            }
        }

        return ret;
    }

    private void commitSentence(Article a,
                                ch.uzh.ifi.attempto.acewiki.core.Statement st,
                                List<String> tokens) {
        LanguageHandler lh = engine.getLanguageHandler();
        PredictiveParser pp = lh.getPredictiveParser();
        TextContainer tc = new TextContainer();
        tc.setTextOperator(lh.getTextOperator());

        pp.removeAllTokens();

        for (String tok: tokens) {
            pp.addToken(tok);
            tc.addElement(tc.getTextOperator().createTextElement(tok));
        }

        List<Sentence> sl = sf.extractSentences(tc, pp, a);
        if (sl.size() > 0) {
            a.add(st, sl.get(0));
        }
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

    public static class ParseResult {
        private List<String> tokens;
        private List<String> subtokens;

        public ParseResult(List<String> tokens, List<String> subtokens) {
            this.tokens = tokens;
            this.subtokens = subtokens;
        }

        public List<String> getTokens() { return tokens; }
        public List<String> getSubtokens() { return subtokens; }

        @Override public ParseResult clone() {
            return new ParseResult(new ArrayList(tokens),
                                   new ArrayList(subtokens));
        }
    }

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

        ParseResult pr = new ParseResult(new ArrayList<String>(), subtoks);
        pr = parseAsFarAsPossible(pp, pr);

        ret.remain = "";
        List<String> subtoks1 = pr.getSubtokens();
        if (subtoks1.size() > 0) {
            ret.remain = subtoks1.remove(0);
            while (subtoks1.size() > 0) {
                ret.remain += " " + subtoks1.remove(0);
            }
        }

        ret.tokens = pr.getTokens();

        if (ret.remain.equals("")) {
            ret.valid = true;
            // after parseAsFarAsPossible, pp is still empty
            pp.setTokens(ret.tokens);
            ret.complete = pp.isComplete() && (ret.tokens.size() > 0);

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


    private ParseResult parseAsFarAsPossible(PredictiveParser pp,
                                             ParseResult pr) {
        NextTokenOptions opts = pp.getNextTokenOptions();
        List<ParseResult> nextResultList = new ArrayList<ParseResult>();
        String text = "";

        // find all candidates once, avoid re-cache of NextTokenOptions.
        List<String> subtoks = new ArrayList(pr.getSubtokens());
        while (subtoks.size() > 0) {
            if (text.length() > 0) text += " ";
            text += subtoks.remove(0);

            if (opts.containsToken(text)) {
                List<String> toks1 = new ArrayList(pr.getTokens());
                toks1.add(text);
                nextResultList.add(new ParseResult(toks1, new ArrayList(subtoks)));
            }
        }

        // iterate in nextResultList
        ParseResult pr1 = pr.clone();

        for (ParseResult i: nextResultList) {
            List<String> toks = i.getTokens();
            // avoid use pp.setTokens, which slow
            pp.addToken(toks.get(toks.size() - 1));
            ParseResult r = parseAsFarAsPossible(pp, i);
            pp.removeToken();
            if (r.getSubtokens().size() < pr1.getSubtokens().size()) {
                pr1 = r;
            }
        }

        return pr1;
    }
}

