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

import java.util.*;

public class RestArticleResult extends RestResult {
    public static abstract class Statement {
        protected String type;
        protected String text;

        public String getType() {
            return type;
        }
    }

    public static class Comment extends Statement {
        public Comment() {
            this(null);
        }

        public Comment(String text) {
            this.type = "comment";
            this.text = text;
        }
    }

    public static class Sentence extends Statement {
        private boolean reasonable;
        private boolean integrated;

        public Sentence() { this(null, false, false); }
        public Sentence(String text, boolean reasonable, boolean integrated) {
            this.type = "sentence";
            this.text = text;
            this.reasonable = reasonable;
            this.integrated = integrated;
        }
    }

    public static class Question extends Sentence {
        private List<String> answers;

        public Question() { this(null, false, false, null); }
        public Question(String text, boolean reasonable, boolean integrated,
                        List<String> answers) {
            super(text, reasonable, integrated);
            this.answers = answers;
            this.type = "question";
        }
    }

    private List<Statement> statements;
    private List<String> words;
    private List<String> headwords;

    public RestArticleResult() {
        statements = new ArrayList<Statement>();
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public void setHeadwords(List<String> headwords) {
        this.headwords = headwords;
    }

    public void addComment(String text) {
        statements.add(new Comment(text));
    }

    public void addSentence(String text, boolean reasonable,
                            boolean integrated) {
        statements.add(new Sentence(text, reasonable, integrated));
    }

    public void addQuestion(String text,
                            boolean reasonable, boolean integrated,
                            List<String> answers) {
        statements.add(new Question(text, reasonable, integrated, answers));
    }
}
