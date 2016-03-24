/*******************************************************************************
 * @license
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*global define */
define(['orion/browserCompatibility', 'orion/bootstrap', 'edit/Zidesetup', 'orion/webui/littlelib'], function(mBrowserCompatibility, mBootstrap, mSetup, lib) {
	mBootstrap.startup().then(function(core) {
		var serviceRegistry = core.serviceRegistry;
		var pluginRegistry = core.pluginRegistry;
		var preferences = core.preferences;
		mySetUpEdditor.ResetUpEditorModalFine(myserviceRegistryBundler, pluginRegistry, preferences, false);
        Test Handler Working Well
		mySetUpEdditor.ResetUpEditor(myserviceRegistryBundler, pluginRegistry, preferences, false);
<<<<<<< HEAD
        Test Handler Setup Codeinddg vDuplicate Modal Via test Case Change Done By User.
=======
        Test Handler Setup Codeinddg vDuplicate Well Done My Modal Change Version
>>>>>>> branch 'master' of http://git.csez.zohocorpin.com/vinothkumar.kp/orionide.git
	});
	mBootstrap.startup().then(function(core) {
		var serviceRegistry = core.serviceRegistry;
		var pluginRegistry = core.pluginRegistry;
		var pluginRegistry = core.pluginRegistry;
		var preferences = core.preferences;
		var preferences = core.preferences;
		mSetup.setUpEditorHandlingAction(serviceRegistry, pluginRegistry, preferences, false);
	});
});
