package org.dawnsci.volumerender.tool;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class VolumeRenderTool extends AbstractSlicingTool
{
	private static AbstractUIPlugin plugin;
	
	private VolumeRenderJob job;
	
	private Composite comp;
	private Button generateButton;
	private Button deleteButton;
	private Slider resolutionSlider;
	private Slider transparencySlider;
//	private ColorSelectorWrapper colourSelector;
	
	private DimensionalListener dimensionalListener;
	private AxisChoiceListener axisChoiceListener;
	
	private final String TRACE_ID = "iuhdiamd8oa"; // mash the key board
	
	static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}
	
	public VolumeRenderTool(){
		
		this.dimensionalListener = new DimensionalListener() // !! what are these fore
		{
			@Override
			public void dimensionsChanged(DimensionalEvent evt)
			{
				//update();
			}
		};
		
		this.axisChoiceListener = new AxisChoiceListener()
		{
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt)
			{
				//update();
			}
		};
		
	}
	
	public void createToolComponent(Composite parent)
	{
		comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(data);
		data.exclude = true;
		
		Label disclaimer1 = new Label(comp, SWT.NONE);
		disclaimer1.setText("Volume renderer pre-Alpha snapshot:");
		disclaimer1.setForeground(new Color(parent.getDisplay(), new RGB(255,0,0)));
		disclaimer1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
				
		Label disclaimer2 = new Label(comp, SWT.NONE);
		disclaimer2.setText("- Please use nightly build if possible");
		disclaimer2.setForeground(new Color(parent.getDisplay(), new RGB(255,0,0)));
		disclaimer2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
						
		Label resolutionLabel = new Label(comp, SWT.NONE);
		resolutionLabel.setText("Resolution");
		new Label(comp, SWT.NONE);
		
		resolutionSlider = new Slider(comp, SWT.NONE);
		resolutionSlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		resolutionSlider.setMaximum(100);
		resolutionSlider.setMinimum(0);
		
		Label transparencyLabel = new Label(comp, SWT.NONE);
		transparencyLabel.setText("Opacity");
		new Label(comp, SWT.NONE);
		
		transparencySlider = new Slider(comp, SWT.NONE);
		transparencySlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		transparencySlider.setMaximum(100);
		transparencySlider.setMinimum(0);
		
		generateButton = new Button(comp, SWT.NONE);
		generateButton.setText("Generate");
		generateButton.setVisible(true);
		
		deleteButton = new Button(comp, SWT.NONE);
		deleteButton.setText("Delete");
		deleteButton.setVisible(true);
		
//		Label colourLabel = new Label(comp, SWT.NONE);
//		colourLabel.setText("Colour");
//		
//		colourSelector = new ColorSelectorWrapper(comp, SWT.NONE);
//		colourSelector.setValue(new RGB(255, 0, 0));
				
		comp.setVisible(false);
	}
	
	@Override
	public void militarize(boolean newSlice) 
	{
		
		getSlicingSystem().setSliceType(getSliceType());

		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList != null)
			dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);
		
		job = new VolumeRenderJob(
				"Volume renderer job", 
				getSlicingSystem().getPlottingSystem());

		transparencySlider.addSelectionListener(new SelectionListener() {
		      public void handleEvent(Event e) 
		      {
		          if (e.type == SWT.Selection) 
		          {
		        	  job.compute(
		        			  TRACE_ID,
		        			  resolutionSlider.getSelection(),
		        			  transparencySlider.getSelection(),
		        			  getSlicingSystem().getData().getLazySet());
		          }
		        }

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		generateButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) 
		      {
		          if (e.type == SWT.Selection) 
		          {
		        	  job.compute(
		        			  TRACE_ID,
		        			  resolutionSlider.getSelection(),
		        			  transparencySlider.getSelection(),
		        			  getSlicingSystem().getData().getLazySet());
		          }
		        }
		});
		
		deleteButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) 
		      {
		          if (e.type == SWT.Selection) 
		          {
		        	  job.destroy(TRACE_ID);;
		          }
		        }
		});
		
//		colourSelector.addListener(new IPropertyChangeListener() {
//			
//			@Override
//			public void propertyChange(PropertyChangeEvent event) {
//				RGB rgb = (RGB) event.getNewValue();
//				job.setColour(rgb.red, rgb.green, rgb.blue);
//				
//			}
//		});
		
		comp.setVisible(true);
		((GridData) comp.getLayoutData()).exclude = false;
		comp.getParent().pack();
		comp.getParent().update();
		
		
		getSlicingSystem().update(false);
		getSlicingSystem().addDimensionalListener(dimensionalListener);
		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
		
	}
	
	@Override
	public void demilitarize()
	{
		comp.setVisible(false);
		((GridData) comp.getLayoutData()).exclude = true;
		comp.getParent().pack();
		comp.getParent().update();
		
		
	}
	
	
	@Override
	public Enum<?> getSliceType() {
		// TODO Auto-generated method stub
		return PlotType.VOLUME;
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
	}
	
}
