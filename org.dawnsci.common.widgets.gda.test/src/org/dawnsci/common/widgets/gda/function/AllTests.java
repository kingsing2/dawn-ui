/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ModelTest.class, FunctionTreeViewerJexlPluginTest.class,
		FunctionTreeViewerPluginTest.class,
		FunctionTreeViewerHandlersIsHandledPluginTest.class,
		FunctionTreeViewerHandlersExecutePluginTest.class})
public class AllTests {

}
