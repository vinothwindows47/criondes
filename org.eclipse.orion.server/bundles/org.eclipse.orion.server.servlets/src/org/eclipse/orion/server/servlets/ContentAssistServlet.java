package org.eclipse.orion.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContentAssistServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		String userid = req.getRemoteUser();
		String filepath = req.getParameter("file");
		String offset = req.getParameter("offset");

		int idx = -1;
		idx = filepath.indexOf("/");

		String project = filepath.substring(0, idx);
		String projfile = filepath.substring(idx + 1);

		String projectname = userid + "_" + project;

		System.out.println("CONTENT ASSIST IS OLD PACKAGE");
		String eclipse_server = System.getProperty("eclipse.server");
		String url = eclipse_server + "/codeassist?project=" + projectname + "&filepath=" + projfile + "&offset=" + offset;
		byte[] op = UrlUtil.sendRequest(url, null, Collections.<String, String> emptyMap(), "POST");
		String output = new String(op);
		writer.println(output);
	}
}
