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

package ch.uzh.ifi.attempto.acewiki.jsonrpc;

import java.util.*;

import ch.uzh.ifi.attempto.acewiki.Backend;
import ch.uzh.ifi.attempto.acewiki.core.Ontology;
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
 * This class implements all AceWiki json-rpc calls.
 *
 * @author Yu Changyuan
 */
public class Handler implements Interface {
    private final Backend backend;
    private final Ontology ontology;
    private final AceWikiEngine engine;

    public Handler(Backend backend) {
        this.backend = backend;
        this.ontology = backend.getOntology();
        this.engine = ontology.getEngine();
    }

    public List<RPC.ArticleInfo> list() {
        List<RPC.ArticleInfo> ret = new ArrayList<RPC.ArticleInfo>();

        for (OntologyElement e: ontology.getOntologyElements()) {
            List<String> words =
                new ArrayList<String>(Arrays.asList(e.getWords()));
            ret.add(new RPC.ArticleInfo(e.getId(), words, e.getType()));
        }

        return ret;
    }

    public RPC.Article get(long id, String language, boolean includeAnswer) {
        OntologyElement oe = ontology.get(id);
        if (oe == null) {
            throw new RuntimeException("Can not find article with id: " + id);
        }

        final CachingReasoner cr = ontology.getReasoner();

        List<String> languages = Arrays.asList(engine.getLanguages());
        if (!languages.contains(language)) {
            language = languages.get(0);
        }

        List<String> words = Arrays.asList(oe.getWords());
        List<String> headwords = Arrays.asList(oe.getHeadwords());

        Article a = oe.getArticle();
        List<RPC.Statement> statements =
            new ArrayList<RPC.Statement>();
        for (Statement st: a.getStatements()) {
            if (st instanceof Comment) {
                statements.add(new RPC.Comment(st.getText(language)));
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
                statements.add(new RPC.Question(q.getText(language),
                                                q.isReasonable(),
                                                q.isIntegrated(),
                                                answers));
            }
            else if (st instanceof Sentence) {
                Sentence s = (Sentence) st;
                statements.add(new RPC.Sentence(s.getText(language),
                                                s.isReasonable(),
                                                s.isIntegrated()));
            }
        }

        return new RPC.Article(statements, words, headwords, language);
    }
}
