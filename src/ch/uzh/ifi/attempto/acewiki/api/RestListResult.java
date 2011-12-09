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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class RestListResult extends RestResult {
    public static class Article {
        private long id;
        private List<String> words;
        private String type;

        public Article() {
            this(-1, new ArrayList<String>(), "");
        }

        public Article(long id, List<String> words, String type) {
            this.id = id;
            this.words = words;
            this.type = type;
        }
    }

    private List<Article> articles;

    public RestListResult() {
        this.articles = new ArrayList<Article>();
    }

    public void addArticle(long id, List<String> words, String type) {
        articles.add(new Article(id, words, type));
    }
}
