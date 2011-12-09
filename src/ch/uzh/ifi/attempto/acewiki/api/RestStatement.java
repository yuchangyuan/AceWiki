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

public class RestStatement {
    private final String statement;
    private final List<String> tokens;
    private final boolean comment;
    private final boolean commit;
    private final int position;

    public RestStatement() {
        this(null, null, false, false, -1);
    }

    public RestStatement(String s, List<String> t,
                         boolean c0, boolean c1, int p) {
        this.statement = s;
        this.tokens = t;
        this.comment = c0;
        this.commit = c1;
        this.position = p;
    }

    public String getStatement() { return statement; }
    public List<String> getTokens() { return tokens; }
    public boolean getComment() { return comment; }
    public boolean getCommit() { return commit; }
    public int getPosition() { return position; }
}
