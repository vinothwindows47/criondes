/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.npm;
package test;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.*;
import org.eclipse.orion.internal.server.core.metastore.SimpleMetaStoreUtil;
import org.eclipse.orion.server.core.*;
import org.eclipse.orion.server.core.metastore.ProjectInfo;
import org.eclipse.orion.server.core.metastore.WorkspaceInfo;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.json.JSONException;
import org.json.JSONObject; 

/*
 * 
 */
public class ZideServlet extends OrionServlet {

	@Override /* Build Started Post Method Calling Here ... */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String pathInfo = req.getPathInfo();
			IPath path = pathInfo == null ? Path.ROOT : new Path(pathInfo); /* Get Current Project Path for putting build */
			Map buildStart = buildStarted(req, path);
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("Build_Started", buildStart);
			OrionServlet.writeJSONResponse(req, resp, jsonResult);
		}
		catch (JSONException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
  	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String buildLogFile = req.getParameter("BuildLog");
			int StartLine = Integer.parseInt(req.getParameter("startLine"));
			int EndLine = Integer.parseInt(req.getParameter("endLine"));
			String pathInfo = req.getPathInfo();
			IPath path = pathInfo == null ? Path.ROOT : new Path(pathInfo);
			Map buildLogRes = readFileString(path, buildLogFile, StartLine, EndLine);
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("buildLog_status", buildLogRes);
			OrionServlet.writeJSONResponse(req, resp, jsonResult);
		}
		catch (JSONException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} 
		catch (CoreException e) {
			// TODO Auto-generated catch block
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	private Map<String, String> buildStarted(HttpServletRequest request, IPath path) {
		Map<String, String> buildStarted_res = new HashMap<String, String>();
		try {
			if(path.segmentCount() == 0) {
        			return null;
        	} 
			WorkspaceInfo workspace = OrionConfiguration.getMetaStore().readWorkspace(path.segment(0));
    		ProjectInfo project = OrionConfiguration.getMetaStore().readProject(path.segment(0), path.segment(1));
    		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    		Date date = new Date();
    		String buildLogName = dateFormat.format(date) + ".buildlog";
    		buildStarted_res.put("buildLog", buildLogName);
    		Hashtable BuildInfo = new Hashtable();
    		BuildInfo.put("Project", path.segment(1));
    		BuildInfo.put("command", "ant");
    		BuildInfo.put("BuildLog", buildLogName);
    		Thread buildStart = new Thread(new ZideThread(project, workspace, BuildInfo));
    		buildStart.start();
    		buildStarted_res.put("status", "success");
        } 
		catch (Exception err) {
				buildStarted_res.put("status", "error");
				return buildStarted_res;
		}
		return buildStarted_res;
	}
	
	public Map<String, Object> readFileString(IPath path, String fileName, int startLine, int endLine) throws CoreException  {
		Map<String, Object> readFile_res = new HashMap<String, Object>();
		readFile_res.put("fileRead", true);
		String line = null;
		String LogOutput = "";
        int currentLineNo = 0;
        WorkspaceInfo workspace = OrionConfiguration.getMetaStore().readWorkspace(path.segment(0));
		ProjectInfo project = OrionConfiguration.getMetaStore().readProject(path.segment(0), path.segment(1));
		IFileStore userHome = OrionConfiguration.getUserHome(workspace.getUserId());
		String encodedWorkspaceName = SimpleMetaStoreUtil.decodeWorkspaceNameFromWorkspaceId(workspace.getUniqueId());
		String fileNamePath = userHome + File.separator + encodedWorkspaceName + File.separator + ".Zide" + path.segment(1) + File.separator + fileName;
		BufferedReader in = null;
        try {
        	in = new BufferedReader (new FileReader(fileNamePath));
            //read to startLine
            while(currentLineNo<startLine) {
            	if (in.readLine()==null) {
            		readFile_res.put("fileRead", false);
                    break;
            	}
                currentLineNo++;
           }
           //read until endLine
           while(currentLineNo<=endLine) {
        	   line = in.readLine();
               if (line==null) {
            	   // here, we'll forgive a short file
            	   // note finally still cleans up
            	   readFile_res.put("fileRead", false);
                   break;
                }
                LogOutput += line + "\r\n";
                currentLineNo++;
           }
        } 
        catch (IOException ex) {
                System.out.println("Problem reading file.\n" + ex.getMessage());
        } 
        readFile_res.put("LogOutput", LogOutput);
        readFile_res.put("nextStartLine", endLine + 1);
        readFile_res.put("nextEndLine", endLine + 51);
		return readFile_res;
	}
}



