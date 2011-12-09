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

public class RestStatementResult extends RestResult {
    private List<String> tokens;
    private String remain;
    private Map<String, List<String>> candidates;
    private boolean valid;
    private boolean complete;
    private int position;

    public RestStatementResult() {
        this(new ArrayList<String>(), "",
             new HashMap<String, List<String>>(),
             true, false, -1);
    }

    public RestStatementResult(List<String> tokens,
                               String remain,
                               Map<String, List<String>> candidates,
                               boolean valid,
                               boolean complete,
                               int position) {
        super();

        this.tokens = tokens;
        this.remain = remain;
        this.candidates = candidates;
        this.valid = valid;
        this.complete = complete;
        this.position = position;
    }

    public List<String> getTokens() { return tokens; }
    public void setTokens(List<String> tokens) { this.tokens = tokens; }
    public String getRemain() { return remain; }
    public void setRemain(String remain) { this.remain = remain; }
    public boolean isValid() { return valid; }
    public Map<String, List<String>> getCandidates() { return candidates; }
    public void setCandidates(Map<String, List<String>> candidates) {
        this.candidates = candidates;
    }
    public void setValid(boolean valid) { this.valid = valid; }
    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

}
