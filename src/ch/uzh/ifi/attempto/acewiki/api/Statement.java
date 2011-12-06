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

public class Statement {
    private final String statement;
    private final boolean comment;
    private final boolean commit;

    public Statement() {
        this("", false, false);
    }

    public Statement(String s, boolean c0, boolean c1) {
        this.statement = s;
        this.comment = c0;
        this.commit = c1;
    }

    public String getStatement() { return statement; }
    public boolean getComment() { return comment; }
    public boolean getCommit() { return commit; }
}
