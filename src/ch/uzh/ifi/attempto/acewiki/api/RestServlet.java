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

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Reader;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import com.google.gson.*;

/**
 * This class is RESTful api servlet.
 */
public class RestServlet extends HttpServlet {
    private Handler handler;
    private Backend backend;
    private Gson gson;

    @SuppressWarnings("rawtypes")
        private Map<String, String> getInitParameters(ServletConfig config) {

        Map<String, String> initParameters = new HashMap<String, String>();
        Enumeration paramEnum = config.getInitParameterNames();
        while (paramEnum.hasMoreElements()) {
            String n = paramEnum.nextElement().toString();
            initParameters.put(n, config.getInitParameter(n));
        }
        Enumeration contextParamEnum = config.getServletContext().getInitParameterNames();
        while (contextParamEnum.hasMoreElements()) {
            String n = contextParamEnum.nextElement().toString();
            initParameters.put("context:" + n, config.getServletContext().getInitParameter(n));
        }
        return initParameters;
    }

    public void init(ServletConfig config) throws ServletException {
        Map<String, String> parameters = getInitParameters(config);
        String name = config.getServletName();

        System.out.println("AceWiki backend " + name + " initing...");

        if (parameters.get("context:apecommand") == null) {
            parameters.put("context:apecommand", "ape.exe");
        }

        if (parameters.get("context:logdir") == null) {
            parameters.put("context:logdir", "logs");
        }

        if (parameters.get("context:datadir") == null) {
            parameters.put("context:datadir", "data");
        }

        // TODO: init ape related.

        backend = new Backend(parameters);
        handler = new Handler(backend);

        ServletContext ctx = config.getServletContext();
        ctx.setAttribute(name, backend);

        // init gson
        gson = new GsonBuilder().setPrettyPrinting().create();

        super.init(config);

        System.out.println("AceWiki backend " + name + " init ok.");
    }

    public RestResult resultError(String reason) {
        return new RestResult(reason);
    }

    public RestResult resultError() {
        return resultError("unknow");
    }

    private void output(RestResult json, HttpServletResponse res) throws
        IOException, ServletException {
        PrintWriter w = res.getWriter();
        w.write(gson.toJson(json));
    }

    public ArrayList<String> splitPath(HttpServletRequest req) {
        ArrayList<String> path = new ArrayList();
        String pi = req.getPathInfo();

        if (pi != null) {
            for (String s: pi.split("/")) {
                if (!s.equals("")) { path.add(s); }
            }
        }

        return path;
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res) throws
        IOException, ServletException {
        RestResult json = resultError();
        ArrayList<String> path = splitPath(req);

        switch (path.size()) {
        case 0:
            json = handler.list(req.getParameter("search"));
            break;
        case 1:
            // TODO;
            break;
        }

        output(json, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws
        IOException, ServletException {
        ArrayList<String> path = splitPath(req);
        RestResult json = resultError();

        if (path.size() != 1) {
            output(resultError("path error."), res);
            return;
        }

        // statement
        Reader r = req.getReader();
        if (r == null) {
            r = new StringReader("");
        }

        RestStatement st = null;
        try {
            st = gson.fromJson(r, RestStatement.class);
        }
        catch (Exception e) {
            output(resultError(e.getMessage()), res);
            return;
        }

        if ((st == null) || ((st.getStatement() == null) &&
                             st.getTokens() == null)) {
            output(resultError("missing statement."), res);
            return;
        }

        // get id or word
        String a = path.get(0);
        if (a.matches("[0-9]+")) {
            json = handler.addStatement(Long.parseLong(a), st);
        }
        else {
            json = handler.addStatement(path.get(0), st);
        }

        output(json, res);
    }

}

