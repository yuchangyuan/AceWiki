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

import java.util.Map;
import java.util.List;

/**
 * This interface include all data types for json-rpc.
 *
 * @author Yu Changyuan
 */
public interface RPC {
    /**
     * Basic infomation for an article
     */
    public static class ArticleInfo {
        private long id;
        private List<String> words;
        private String type;

        public ArticleInfo(long id, List<String> words, String type) {
            this.id = id;
            this.words = words;
            this.type = type;
        }

        public long getId() { return id; }
        public List<String> getWords() { return words; }
        public String getType() { return type; }
    }

    /**
     * This class defines an Statement
     */
    public static abstract class Statement {
        protected String type;
        protected String text;

        public String getType() { return type; }
        public String getText() { return text; }
    }

    /**
     * This class defines a Comment
     */
    public static class Comment extends Statement {
        public Comment(String text) {
            this.type = "comment";
            this.text = text;
        }
    }

    /**
     * This class defines a Statement
     */
    public static class Sentence extends Statement {
        private boolean reasonable;
        private boolean integrated;

        public Sentence(String text, boolean reasonable, boolean integrated) {
            this.type = "sentence";
            this.text = text;
            this.reasonable = reasonable;
            this.integrated = integrated;
        }

        public boolean getReasonable() { return reasonable; }
        public boolean getIntegrated() { return integrated; }
    }

    /**
     * This class defines a Sentence
     */
    public static class Question extends Sentence {
        private List<String> answers;

        public Question(String text, boolean reasonable, boolean integrated,
                        List<String> answers) {
            super(text, reasonable, integrated);
            this.answers = answers;
            this.type = "question";
        }

        public List<String> getAnswers() { return answers; }
    }

    /**
     * This class defines an article.
     */
    public static class Article {
        private List<Statement> statements;
        private List<String> words;
        private List<String> headwords;
        private String language;

        public Article(List<Statement> statements,
                       List<String> words,
                       List<String> headwords,
                       String language) {
            this.statements = statements;
            this.words = words;
            this.headwords = headwords;
            this.language = language;
        }

        public List<Statement> getStatements() { return statements; }
        public List<String> getWords() { return words; }
        public List<String> getHeadwords() { return headwords; }
        public String getLanguage() { return language; }
    }
}
