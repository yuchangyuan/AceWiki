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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import ch.uzh.ifi.attempto.acewiki.Backend;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.codehaus.jackson.JsonParseException;

/**
 * This servlet handles json-rpc call for AceWiki.
 *
 * @author Yu Changyuan
 */
public class JsonRpcServlet extends HttpServlet {
    private static final long serialVersionUID = 63221980646692L;

    private Handler handler;
    private JsonRpcServer server;

    public void init(ServletConfig config) throws ServletException {
        String backendName = config.getInitParameter("backend");
        Backend backend = null;

        if (backendName != null) {
            while (true) {
                backend = (Backend) config.getServletContext().getAttribute(backendName);
                if (backend != null) break;
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (backend == null) {
            String mesg = "Can not find backend.";
            if (backendName != null) {
                mesg = "Can not find backend: " + backendName + ".";
            }
            throw new ServletException(mesg);
        }

        handler = new Handler(backend);
        server = new JsonRpcServer(handler, Interface.class);
    }

    @Override public void service(HttpServletRequest req,
                                  HttpServletResponse res) throws ServletException {
        try {
            server.handle(req, res);
        }
        catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
