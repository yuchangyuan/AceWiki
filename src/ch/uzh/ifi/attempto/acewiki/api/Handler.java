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
import java.util.Arrays;

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
import ch.uzh.ifi.attempto.acewiki.core.Statement;
import ch.uzh.ifi.attempto.acewiki.core.Comment;
import ch.uzh.ifi.attempto.acewiki.core.Article;
import ch.uzh.ifi.attempto.acewiki.core.Sentence;
import ch.uzh.ifi.attempto.acewiki.core.Question;
import ch.uzh.ifi.attempto.acewiki.core.LanguageHandler;
import ch.uzh.ifi.attempto.acewiki.core.CachingReasoner;
import ch.uzh.ifi.attempto.acewiki.core.AnswerElement;

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

    public RestResult list(String search) {
        List<OntologyElement> elemList;
        if (search != null) {
            WordIndex index = engine.getWordIndex();
            elemList = index.searchForElements(search);
        }
        else {
            elemList = ontology.getOntologyElements();
        }

        // return { id : { words: [string], type: string } }
        RestListResult ret = new RestListResult();
        for (OntologyElement e: elemList) {
            List<String> words =
                new ArrayList<String>(Arrays.asList(e.getWords()));
            ret.addArticle(e.getId(), words, e.getType());
        }

        return ret;
    }


    public RestResult addStatement(String word, RestStatement st) {
        return addStatement(ontology.getElement(word), st);
    }

    public RestResult addStatement(long id, RestStatement st) {
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

    public RestResult addStatement(OntologyElement oe, RestStatement st) {
        RestStatementResult ret = new RestStatementResult();

        if (oe == null) {
            ret.setError("no such article.");
            return ret;
        }

        ret.setPosition(-1);
        ret.setComplete(true);
        ret.setValid(true);

        // NOTE: thread safe, article position might change
        Article a = oe.getArticle();
        List<Statement> stList = a.getStatements();
        // calc position
        int pos = calcPosition(stList.size(), st.getPosition());

        if (st.getComment()) {
            if (st.getCommit()) {
                // update position
                ret.setPosition(pos);
                Comment c = sf.createComment(st.getStatement(), a);
                a.add((pos < stList.size()) ? stList.get(pos) : null, c);
            }

            return ret;
        }

        if (st.getTokens() != null) {
            getCandidates(st.getTokens(), ret);
        }
        else {
            getCandidates(st.getStatement(), ret);
        }

        if (st.getCommit()) {
            if (!ret.isComplete()) {
                ret.setError("sentence not complete.");
            }
            else if (!ret.isValid()) {
                ret.setError("sentence not valid.");
            }
            else {
                ret.setPosition(pos);
                commitSentence(a, (pos < stList.size()) ? stList.get(pos) : null,
                               ret.getTokens());
            }
        }

        return ret;
    }

    private void commitSentence(Article a, Statement st, List<String> tokens) {
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
            return new ParseResult(new ArrayList<String>(tokens),
                                   new ArrayList<String>(subtokens));
        }
    }

    public synchronized void getCandidates(List<String> tokens,
                                           RestStatementResult ret) {
        LanguageHandler lh = engine.getLanguageHandler();
        PredictiveParser pp = lh.getPredictiveParser();
        pp.removeAllTokens();

        String remain = "";

        List<String> list = new ArrayList<String>(tokens);
        while (list.size() > 0) {
            String tok = list.remove(0);
            if (!pp.isPossibleNextToken(tok)) {
                remain = tok;
                break;
            }
            pp.addToken(tok);
        }

        while (list.size() > 0) {
            if (remain.length() > 0) remain += " ";
            remain += list.remove(0);
        }
        ret.setRemain(remain);

        updateCandidates(pp, ret);
    }

    // synchronized
    public synchronized void getCandidates(String text,
                                           RestStatementResult ret) {
        LanguageHandler lh = engine.getLanguageHandler();
        PredictiveParser pp = lh.getPredictiveParser();
        pp.removeAllTokens();
        getCandidates(pp, lh.getTextOperator().splitIntoTokens(text), ret);
    }

    // here, should not synchronized
    private void getCandidates(PredictiveParser pp,
                                     List<String> subtoks,
                                     RestStatementResult ret) {
        ParseResult pr = new ParseResult(new ArrayList<String>(), subtoks);
        pr = parseAsFarAsPossible(pp, pr);

        String remain = "";
        List<String> subtoks1 = pr.getSubtokens();
        if (subtoks1.size() > 0) {
            remain = subtoks1.remove(0);
            while (subtoks1.size() > 0) {
                remain += " " + subtoks1.remove(0);
            }
        }
        ret.setRemain(remain);

        // after parseAsFarAsPossible, pp is still empty
        pp.setTokens(pr.getTokens());

        updateCandidates(pp, ret);
    }

    // all tokens should already in pp, and tokens and remain
    // are setted in can.
    private void updateCandidates(PredictiveParser pp, RestStatementResult ret) {
        ret.setTokens(pp.getTokens());

        if (ret.getRemain().equals("")) {
            ret.setValid(true);
            ret.setComplete(pp.isComplete() && (ret.getTokens().size() > 0));

            if (!ret.isComplete()) {
                Map<String, List<String>> can = new HashMap<String, List<String>>();
                for (ConcreteOption o: pp.getNextTokenOptions().getConcreteOptions()) {
                    String cat = o.getCategoryName();
                    String word = o.getWord();

                    if (cat == null) {
                        cat = "";
                    }
                    if (can.get(cat) == null) {
                        can.put(cat, new ArrayList<String>());
                    }
                    can.get(cat).add(word);
                }
                ret.setCandidates(can);
            }
        }
        else {
            ret.setValid(false);
            ret.setComplete(false);
        }
    }

    private ParseResult parseAsFarAsPossible(PredictiveParser pp,
                                             ParseResult pr) {
        NextTokenOptions opts = pp.getNextTokenOptions();
        List<ParseResult> nextResultList = new ArrayList<ParseResult>();
        String text = "";

        // find all candidates once, avoid re-cache of NextTokenOptions.
        List<String> subtoks = new ArrayList<String>(pr.getSubtokens());
        while (subtoks.size() > 0) {
            if (text.length() > 0) text += " ";
            text += subtoks.remove(0);

            if (opts.containsToken(text)) {
                List<String> toks1 = new ArrayList<String>(pr.getTokens());
                toks1.add(text);
                nextResultList.add(new ParseResult(toks1, new ArrayList<String>(subtoks)));
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

    public RestArticleResult getArticle(long id, boolean includeAnswer) {
        return getArticle(ontology.get(id), includeAnswer);
    }

    public RestArticleResult getArticle(String word, boolean includeAnswer) {
        return getArticle(ontology.getElement(word), includeAnswer);
    }

    public RestArticleResult getArticle(OntologyElement oe, 
                                        boolean includeAnswer) {
        RestArticleResult ret = new RestArticleResult();
        final CachingReasoner cr = ontology.getReasoner();

        if (oe == null) {
            ret.setError("no such article.");
            return ret;
        }

        Article a = oe.getArticle();
        List<Statement> stList = a.getStatements();
        for (Statement st: stList) {
            if (st instanceof Comment) {
                ret.addComment(st.getText());
            }
            else if (st instanceof Question) {
                Question q = (Question) st;
                List<String> answers = null;
                if (includeAnswer) {
                    answers = new ArrayList<String>();
                    List<AnswerElement> cachedAnswer = cr.getCachedAnswer(q);
                    if (cachedAnswer == null || !cr.isCachedAnswerUpToDate(q)) {
                        cachedAnswer = cr.getAnswer(q);
                    }
                    for (AnswerElement ae: cr.getAnswer(q)) {
                        answers.add(ae.getAnswerText().getText());
                    }
                }
                ret.addQuestion(q.getText(), q.isReasonable(), 
                                q.isIntegrated(), answers);
            }
            else if (st instanceof Sentence) {
                Sentence s = (Sentence) st;
                ret.addSentence(s.getText(), s.isReasonable(), s.isIntegrated());
            }
        }

        return ret;
    }
}

