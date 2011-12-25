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

import static ch.uzh.ifi.attempto.acewiki.jsonrpc.RPC.*;

/**
 * This interface defines all json-rpc calls for the AceWiki.
 *
 * @author Yu Changyuan
 */
public interface Interface {
    /**
     * Return a list of Article.
     */
    public List<ArticleInfo> list();

    /**
     * Return an Article by id.
     *
     * @param id The id of the article.
     * @param language The language for return article object.
     * @param includeAnswer Whether include answer in return.
     * @return An article object.
     */
    public Article get(long id, String language, boolean includeAnswer);
}
