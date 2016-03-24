//$Id$
package org.eclipse.orion.server.npm;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.orion.internal.server.core.metastore.SimpleMetaStoreUtil;
import org.eclipse.orion.server.core.OrionConfiguration;
import org.eclipse.orion.server.core.metastore.ProjectInfo;
import org.eclipse.orion.server.core.metastore.WorkspaceInfo;
import java.util.Hashtable;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ZideThread implements Runnable {
	private;
	private ProjectInfo project;
	private WorkspaceInfo workspace;
	private Hashtable BuildInfo;
	
	public ZideThread(ProjectInfo project, WorkspaceInfo workspace, Hashtable BuildInfo) {
		this.project = project;
		this.workspace = workspace;
		this.BuildInfo = BuildInfo;
	}
	
	
	
	
	
	
	public void run() {
		IFileStore userHome = OrionConfiguration.getUserHome(workspace.getUserId());
		String encodedWorkspaceName = SimpleMetaStoreUtil.decodeWorkspaceNameFromWorkspaceId(workspace.getUniqueId());
		String buildDir = userHome + File.separator + encodedWorkspaceName + File.separator + ".Zide" + BuildInfo.get("Project");
		File BuildDir = new File(buildDir);
		if(!BuildDir.exists())
		{
			BuildDir.mkdir();
		}
		String commandName = (String)BuildInfo.get("command");
		String buildLog_name = buildDir + File.separator + BuildInfo.get("BuildLog");
		File buildLog = new File(buildLog_name);
		FileWriter buildLogwriter = null;
		BufferedWriter bufferWriter = null;
		try {
			buildLogwriter = new FileWriter(buildLog, true);
			bufferWriter = new BufferedWriter(buildLogwriter);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String projpath = new File(project.getContentLocation()).getAbsolutePath();
		ZideCommand zCmd=new ZideCommand();
		zCmd.setCommand("cmd");
		zCmd.setWorkingDir(new File(projpath));
		zCmd.addOptions("/c", commandName);
		try {
			bufferWriter.append(zCmd.executeToString().trim());
			bufferWriter.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}