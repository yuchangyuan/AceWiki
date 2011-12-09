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

public class RestResult {
    private String result;
    private String reason;

    public RestResult() {
        this.result = "ok";
        this.reason = null;
    }

    public RestResult(String reason) {
        this.reason = reason;
        this.result = "error";
    }

    public String getResult() { return result; }
    public String getReason() { return reason; }
    public void setOk() {
        result = "ok";
        reason = null;
    }
    public void setError(String reason) {
        result = "error";
        this.reason = reason;
    }
}
