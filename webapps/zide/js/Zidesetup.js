/*$Id$*/

/*******************************************************************************
 *
 * @license
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html).
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*jslint browser:true devel:true sub:true*/
/*global define eclipse:true orion:true window*/

define([
	'i18n!orion/edit/nls/messages',
	'orion/sidebar',
	'orion/inputManager',
	'orion/globalCommands',
	'orion/editor/textModel',
	'orion/folderVisddew',
<<<<<<< HEAD
	'orion/editordiffMergeToolViewChanger',
	'orion/i18nUtil',
	'orion/MergeToolHandling',
	'orion/changeTool',
=======
	'orion/editordiffVersionToolViewChanger',
	'orion/i18nUtilManager',
	'orion/changeToolDoneToolVersion',
>>>>>>> branch 'master' of http://git.csez.zohocorpin.com/vinothkumar.kp/orionide.git
	'orion/PagedffUtil',
	'orion/objedffcts',
	'orion/webui/littlelib',
	'orion/projectClient',
	'orion/commands',
	'orion/xhr',
	'orion/webui/splitter'
], function(
	messages, Sidebar, mInputManager, mGlobalCommands,
	mTextModel, mFolderView, mEditorView, mPluginEditorView , mMarkdownView,
	mCommandRegistry, mContentTypes, mFileClient, mFileCommands, mSelection, mStatus, mProgress, mOperationsClient, mOutliner, mDialogs, mExtensionCommands, ProjectCommands, mSearchClient,
	mProblems, mBlameAnnotation,
	Deferred, EventTarget, URITemplate, i18nUtil, PageUtil, objects, lib, mProjectClient, mCommands, xhr, splitter
) {

	var exports = {};
	
	function MenuBar(options) {
		this.parentNode = options.parentNode;
		this.zidedisplayNode = options.zidedisplayNode;
		this.commandRegistry = options.commandRegistry;
		this.serviceRegistry = options.serviceRegistry;
		this.fileClient = options.fileClient;
		this.inputManager = options.inputManager;
		this.createActionSections();
		
		/* Console Management on click function */
		$(function() {
			$(document).ready(function () {
				$('.console-delete').click(function () {
					$('.zideConsole').hide();
	            });
				$('.console-hiddenpart').click(function () {
					$('.zideConsole').show();
	            });
				$('.console-max').click(function() {
					$('.zideConsole').css('height','590px');
					$('.consoleOutputDiv').css('height','590px');
				});
				$('.console-min').click(function() {
					$('.zideConsole,.consoleOutputDiv').css('height','300px');
				});
	        });
		});
	}
	
	MenuBar.prototype = {};
	objects.mixin(MenuBar.prototype, {
		/* zide new tab creation in menu bar */
		createActionSections: function() {
			var _self = this;
			[this.zideActionsScope].reverse().forEach(function(id) {
				if (!_self[id]) {
					var elem = document.createElement("ul"); //$NON-NLS-0$
					elem.id = id;
					elem.classList.add("commandList"); //$NON-NLS-0$
					elem.classList.add("layoutLeft"); //$NON-NLS-0$
					elem.classList.add("pageActions"); //$NON-NLS-0$
					_self.parentNode.insertBefore(elem, _self.zidedisplayNode);
					_self[id] = elem;
				}
			});
			var commandRegistry = this.commandRegistry;
			var zideActionsScope = this.zideActionsScope;
			commandRegistry.addCommandGroup(zideActionsScope, "orion.menuBarZideGroup", 1000, "Zide", null, "noActions", null, null, "dropdownSelection"); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
			this._registerCommand(commandRegistry); // register the command to show in menubar .
			
			/* Edit page console div create onload in edit.html */
			OrionConsoleManageDiv = "<div class='consoleTxt'>Console</div><div class='console-max'><span class='commandSprite core-sprite-expandAll'></span></div><div class='console-min'><span class='commandSprite core-sprite-collapseAll'></span></div><div class='console-delete'><span class='commandSprite core-sprite-delete'></span></div>"
			OrionConsole = "<div id='Orion-zideconsole' class='zideConsole'><div class='consoleManagediv' id='consoleManagediv'>" + OrionConsoleManageDiv + "<div class='consoleOutputDiv' id='consoleOutputDiv'></div></div>"
			OrionConsole_hiddenprint = "<div class='console-hiddenpart'>Zide Console</div>";
			$("#pageContent").append(OrionConsole);
			$("#pageContent").append(OrionConsole_hiddenprint);
			
			/* Edit.html page UI changes in the favour of zoho */
			$(".mainToolbar,.auxpane").css("background-color","#FAFAFA");
			$(".mainpane").css("background","#FFFFFF");
		},
		createCommands: function() {
			var serviceRegistry = this.serviceRegistry;
			var commandRegistry = this.commandRegistry;
			var fileClient = this.fileClient;
			return mFileCommands.createFileCommands(serviceRegistry, commandRegistry, fileClient).then(function() {
				return mExtensionCommands.createFileCommands(serviceRegistry, null, "all", true, commandRegistry).then(function() { //$NON-NLS-0$
					if (serviceRegistry.getServiceReferences("orion.projects").length > 0) { //$NON-NLS-0$
						var projectClient = serviceRegistry.getService("orion.project.client"); //$NON-NLS-0$
						return projectClient.getProjectHandlerTypes().then(function(dependencyTypes){
							return projectClient.getProjectDeployTypes().then(function(deployTypes){
								return ProjectCommands.createProjectCommands(serviceRegistry, commandRegistry, fileClient, projectClient, dependencyTypes, deployTypes);
							}, function(error){
								return ProjectCommands.createProjectCommands(serviceRegistry, commandRegistry, fileClient, projectClient, dependencyTypes);
							});
						});
					}
				});
			});
		},
		updateCommands: function() {
			var explorer = this.explorer;
                        var visible, selection, treeRoot;
                        if (explorer) {
                                visible = explorer.isCommandsVisible();
                                selection = explorer.selection;
                                treeRoot = explorer.treeRoot;
                        }
			var metadata = this.inputManager.getFileMetadata();
			var commandRegistry = this.commandRegistry, serviceRegistry = this.serviceRegistry;
			commandRegistry.registerSelectionService(this.zideActionsScope, visible ? selection : null);
			mFileCommands.setExplorer(explorer);
                        ProjectCommands.setExplorer(explorer);
			mFileCommands.updateNavTools(serviceRegistry, commandRegistry, explorer, null, [this.zideActionsScope], null, true);
		},
		_registerCommand: function(cmdReg) {
			/* zide Menubar option added */
			var zideMenu = this.zideActionsScope;
			var zideMenuNode = lib.node(zideMenu);
			serviceRegistry = this.serviceRegistry;
			progress= serviceRegistry.getService("orion.page.progress");
			cmdReg.registerCommandContribution(zideMenu, "orion.zide.build", 1, "orion.menuBarZideGroup/orion.zideTestGroup");
			
			/* Get current workspace name for the current user */ 
			var xhr = new XMLHttpRequest();
			xhr.open("GET","/workspace",false);
			xhr.send();
			var workspace = JSON.parse(xhr.response);
			workspaceName = workspace.Workspaces[0].Id; // get result current workspace name .
			
			myCmd = new mCommands.Command({
				name : "Build",
				tooltip: "zide Build",
                id: "orion.zide.build", //$NON-NLS-0$
                visibleWhen: function(item) {
                	return true;
                },
                callback: function(data) {  // zide Menu bar build option onclick functionality .
                	var currProjval = $("#location span a:eq(1)").html();
                    if(currProjval === undefined)
                    {
                    	currProjval = "";
                    	var display = [];

                        display.Severity = "Error"; //$NON-NLS-0$
                        display.HTML = false;
                        display.Message = "Select a project ...";
                    	serviceRegistry.getService("orion.page.message").setProgressResult(display);
                    	return;
                    }
                    zideUrl = workspaceName + "/" +  currProjval; 
                    var deferred = new Deferred();
                    progress.showWhile(deferred, currProjval + " Build Started ...");
                    $('.zideConsole').show();
                    xhr.open("POST", "/zide" + "/" + zideUrl, false); // Build started Url call for the specific product .
                    xhr.send();
                    if(xhr.status == 200)
                    {
                    	buildOutput_resp = JSON.parse(xhr.response);	// Build started response.
						buildLogName = buildOutput_resp["Build_Started"]["buildLog"];
						startLine = 0;
						endLine = 50;
						/* Build started log file retrieve every 500 millisecond for 50 lines .*/
						setTimeout(function(){_getBuildLog(xhr, serviceRegistry,  zideUrl, buildLogName, startLine, endLine);},1000);
					}
                }
			});
            cmdReg.addCommand(myCmd);
            
		}
	});

/* get Build Log file for the  running build */
function _getBuildLog(xhr, serviceRegistry, zideUrl, buildLogName, startLine, endLine) {
	
	xhr.open("GET", "/zide" + "/" + zideUrl + "?BuildLog=" + buildLogName + "&startLine=" + startLine + "&endLine=" + endLine , false);
	xhr.send();
	if(xhr.status == 200)
	{
		buildLog_resp = JSON.parse(xhr.response);
		fileRead = buildLog_resp["buildLog_status"]["fileRead"];
		LogOutput = buildLog_resp["buildLog_status"]["LogOutput"]
		$container = $("#consoleOutputDiv")
		$container.append(LogOutput.replace(/\r\n/g,'<br/>'));
		$container.animate({ scrollTop: $container[0].scrollHeight }, "slow");
		serviceRegistry.getService("orion.page.message").setProgressResult("finished");
		if(fileRead)
		{
			serviceRegistry.getService("orion.page.message").setProgressResult("Build is running ....");
			startLine = buildLog_resp["buildLog_status"]["nextStartLine"];
			endLine = buildLog_resp["buildLog_status"]["nextEndLine"];
			setTimeout(function(){_getBuildLog(xhr, serviceRegistry,  zideUrl, buildLogName, startLine, endLine);},500);
		}
		else
		{
			serviceRegistry.getService("orion.page.message").setProgressResult("Build Completed ....");
		}
	}
}

exports.setUpEditor = function(serviceRegistry, pluginRegistry, preferences, isReadOnly) {
	var selection;
	var commandRegistry;
	var statusService;
	var problemService;
	var outlineService;
	var contentTypeRegistry;
	var progressService;
	var dialogService;
	var fileClient;
	var projectClient;
	var searcher;

	// Initialize the plugin registry
	(function() {
		selection = new mSelection.Selection(serviceRegistry);
		var operationsClient = new mOperationsClient.OperationsClient(serviceRegistry);
		statusService = new mStatus.StatusReportingService(serviceRegistry, operationsClient, "statusPane", "notifications", "notificationArea"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
		dialogService = new mDialogs.DialogService(serviceRegistry);
		commandRegistry = new mCommandRegistry.CommandRegistry({selection: selection});
		progressService = new mProgress.ProgressService(serviceRegistry, operationsClient, commandRegistry);

		// Editor needs additional services
		problemService = new mProblems.ProblemService(serviceRegistry);
		outlineService = new mOutliner.OutlineService({serviceRegistry: serviceRegistry, preferences: preferences});
		contentTypeRegistry = new mContentTypes.ContentTypeRegistry(serviceRegistry);
		fileClient = new mFileClient.FileClient(serviceRegistry);
		projectClient = new mProjectClient.ProjectClient(serviceRegistry, fileClient);
		searcher = new mSearchClient.Searcher({serviceRegistry: serviceRegistry, commandService: commandRegistry, fileService: fileClient});
	}());

	pageToolbar = lib.node("pageToolbar"), //$NON-NLS-0$
	toolsActions = lib.node("toolsActions"),
	editorDomNode = lib.node("editor"); //$NON-NLS-0$
	var inputManager, editorView, menuBar;
	
	function renderToolbars() {
		menuBar.updateCommands();
	}

	inputManager = new mInputManager.InputManager({
		serviceRegistry: serviceRegistry,
		fileClient: fileClient,
		progressService: progressService,
		selection: selection,
		contentTypeRegistry: contentTypeRegistry
	});
	
	menuBar = new MenuBar({
		parentNode: pageToolbar,
		zidedisplayNode: toolsActions,
		fileClient: fileClient,
		inputManager: inputManager,
		commandRegistry: commandRegistry,
		serviceRegistry: serviceRegistry
	});
	menuBar.createCommands().then(function() {
		
		var defaultOptions = {
			parent: editorDomNode,
			menuBar: menuBar,
			serviceRegistry: serviceRegistry,
			pluginRegistry: pluginRegistry,
			commandRegistry: commandRegistry,
			contentTypeRegistry: contentTypeRegistry,
			renderToolbars: renderToolbars,
			inputManager: inputManager,
			readonly: isReadOnly,
			preferences: preferences,
			searcher: searcher,
			selection: selection,
			fileService: fileClient,
			statusService: statusService,
			progressService: progressService
		};
		editorView = new mEditorView.EditorView(defaultOptions);	
	});

};
return exports;
});
