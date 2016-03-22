/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;

import org.dawnsci.plotting.javafx.trace.FXIsosurfaceTrace;
import org.dawnsci.plotting.javafx.trace.VolumeTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

/**
 * TODO The implementation of this plotter viewer is not complete.
 * 
 * @author Matthew Gerring
 *
 */

/**
 * @author uij85458
 *  // added comments
 * viewer
 * 		include the scene
 * 		holds a trace list
 * 		draws trace list within scene root
 * 
 */
public class FXPlotViewer extends IPlottingSystemViewer.Stub<Composite>
{
	// the scene class -> uses an inherited class to hold the camera translation data
	private SurfaceDisplayer scene;
	// root node
	private Group root;
	// node to hold the isosurface data -> a pointer is declared within the scene (surfacedisplayer)
	// this pointer is edited within the scene via listeners
	private Group isoSurfaceGroup;
	
	// the canvas for drawing -> not sure if this is needed but will keep it for now
	private FXCanvas canvas;
	// not sure what this does currently
	private FXPlotActions plotActions;
	
	
	/**
	 * Must have no-argument constructor.
	 */
	public FXPlotViewer()
	{		
		
		
	}
	
	/**
	 * Call to create plotting
	 * 
	 * @param parent
	 * @param initialMode
	 *            may be null
	 */
	public void createControl(final Composite parent)
	{
		Platform.setImplicitExit(false);
		
		// declare the canvas in memory
		this.canvas = new FXCanvas(parent, SWT.NONE);
		
		// create the root node
		this.root = new Group();
		// create the group for the isosurfaces
		this.isoSurfaceGroup = new Group();
		
		// create the scene -> most of the changes will be done within here
		scene = new SurfaceDisplayer(root, isoSurfaceGroup);
		
		// set the scene to the canvas
		this.canvas.setScene(scene);
		
		this.plotActions = new FXPlotActions(this, system);
		this.plotActions.createActions();
		this.system.getActionBars().getToolBarManager().update(true);
		this.system.getActionBars().updateActionBars();
		
	}	
	
	// change the cursor -> does isFxApplicationThread make it thread safe??
	// potentially redundant but makes the code easier to read which is nice!!
	public void setDefaultCursor(final int cursorFlag)
	{
		
		if (Platform.isFxApplicationThread())
		{
			setCursor(cursorFlag);
		}
		else
		{
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					setCursor(cursorFlag);
				}
			});
		}
	}
	
	// declare the cursor depending on the flag
	private void setCursor(int cursorFlag)
	{
		if (canvas.getScene() == null)
			return;
		
		Cursor cursor = Cursor.DEFAULT;
		if (cursorFlag == IPlottingSystem.CROSS_CURSOR)
			cursor = Cursor.CROSSHAIR;
		if (cursorFlag == IPlottingSystem.WAIT_CURSOR)
			cursor = Cursor.WAIT;
		canvas.getScene().setCursor(cursor);
	}
	

	public void saveScreenShotOfSceneToFile()
	{
		String fileURL = null;
		
		FileDialog dialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.SAVE);

		String [] filterExtensions = new String [] {".png"};
		
		dialog.setFilterPath(File.listRoots()[0].getAbsolutePath());
		dialog.setFilterNames(new String[]{".png"});
		dialog.setFilterExtensions (filterExtensions);
		fileURL = dialog.open();
		
		WritableImage wi = scene.snapshot(null);
		BufferedImage rawImage;
		rawImage = SwingFXUtils.fromFXImage(wi, null);
		
		try {
			ImageIO.write(rawImage, "png", new File(fileURL));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ITrace createTrace(String name, Class<? extends ITrace> clazz)
	{
		if (IIsosurfaceTrace.class.isAssignableFrom(clazz))
		{
			if (name == null || "".equals(name))
				throw new RuntimeException("Cannot create trace with no name!");
			return new FXIsosurfaceTrace(this, scene, name);
		}
		else if (IVolumeRenderTrace.class.isAssignableFrom(clazz))
		{
			if (name == null || "".equals(name))
				throw new RuntimeException("Cannot create trace with no name!");
			return new VolumeTrace(this, scene, name);
		}			
		else
		{
			throw new RuntimeException("Trace type not supported " + clazz.getSimpleName());
		}
	}

	// add the trace, ie create a new isosurface
	// !!look into later!!
	public boolean addTrace(ITrace trace)
	{
		if (trace instanceof IIsosurfaceTrace)
		{
			// declare the trace from the parameter trace
			FXIsosurfaceTrace itrace = (FXIsosurfaceTrace) trace;
			if (itrace.getData() == null)
				throw new RuntimeException("Trace has no data " + trace.getName());
			// create the trace
			itrace.create();
			
			// add the trace into the list of current traces
			scene.addSurfaceTrace(itrace);
			scene.setAxesData(itrace.getAxes());
		}
		else if (trace instanceof IVolumeRenderTrace)
		{
			// declare the trace from the parameter trace
			VolumeTrace itrace = (VolumeTrace) trace;
			if (itrace.getData() == null)
				throw new RuntimeException("Trace has no data " + trace.getName());
			
			// add the trace into the list of current traces
			scene.addVolumeTrace(itrace);
		}
		
		else
		{
			throw new RuntimeException("Trace type not supported " + trace.getClass().getSimpleName());
		}
		return true;
		
	}
		
	// i dont like this
	// these act as intermediates between the action and the scene
	// feels horrible
	public void flipScaleAxesVisibility()
	{
		scene.flipScaleAxesVisibility();
	}
	
	public void flipAxisGridVisibility()
	{
		scene.flipAxisGridVisibility();
	}
	
	public void flipBoundingBoxVisibility()
	{
		scene.flipBoundingBoxVisibility();
	}
	
	public void flipCameraType()
	{
		scene.flipCameraType();
	}
	
	/**
	 * 
	 * @param type
	 * @return true if this viewer deals with this plot type.
	 */
	public boolean isPlotTypeSupported(PlotType type)
	{
		switch (type)
		{
			case ISOSURFACE:
				return true;
			case VOLUME:
				return true;
			default:
				return false;
		}
	}
	
	// simple checks is the trace is supported
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace)
	{
		if (IIsosurfaceTrace.class.isAssignableFrom(trace))
		{
			return true;
		}
		else if (IVolumeRenderTrace.class.isAssignableFrom(trace))
		{
			return true;
		}
			
		return false;
	}
	
	// get the composite being used within the class
	public Composite getControl()
	{
		return canvas;
	}
	
	
}
