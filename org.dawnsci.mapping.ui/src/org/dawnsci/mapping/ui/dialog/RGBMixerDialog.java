package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.mapping.ui.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.rangeSlider.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBMixerDialog extends Dialog {

	private static Logger logger = LoggerFactory.getLogger(RGBMixerDialog.class);

	private List<Dataset> data;
	private CompoundDataset compData;
	private IPlottingSystem system;

	private int idxR = -1, idxG = -1, idxB = -1;
	private Dataset zeros;

	private RangeSlider redRangeSlider;

	private RangeSlider greenRangeSlider;

	private RangeSlider blueRangeSlider;

	public RGBMixerDialog(Shell parentShell, List<IDataset> data) throws Exception {
		super(parentShell);
		if (data.isEmpty())
			throw new Exception("No data is available to visualize in the RGB Mixer dialog.");
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setDefaultImage(Activator.getImageDescriptor("icons/rgb.png").createImage());
		this.data = new ArrayList<Dataset>();
		int width = data.get(0).getShape()[0];
		int height = data.get(0).getShape()[1];
		for (IDataset d : data) {
			if (width != d.getShape()[0] || height != d.getShape()[1]) {
				throw new Exception("Data has not the same size");
			}
			double max = d.max().doubleValue();
			double min = d.min().doubleValue();
			
			Dataset da = DatasetUtils.convertToDataset(d.clone());
			da.isubtract(min).idivide(max-min).imultiply(255);
			this.data.add(da);
		}
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			String error = "Error creating RGB plotting system:" + e.getMessage();
			logger.error("Error creating RGB plotting system:", e);
			throw new Exception(error);
		}
	}

	@Override
	public Control createContents(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topPane = new Composite(container, SWT.NONE);
		topPane.setLayout(new GridLayout(1, false));
		topPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(topPane, null);
		system.createPlotPart(topPane, "RGB Plot", actionBarWrapper, PlotType.IMAGE, null);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite bottomPane = new Composite(container, SWT.NONE);
		bottomPane.setLayout(new GridLayout(3, false));
		bottomPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		//generate combos
		String[] dataNames = new String[data.size() + 1];
		dataNames[0] = "None";
		for (int i = 0; i < data.size(); i ++) {
			dataNames[i + 1] = data.get(i).getName();
		}

		Composite redComp = new Composite(bottomPane, SWT.NONE);
		redComp.setLayout(new GridLayout(2, false));
		Label redLabel = new Label(redComp, SWT.RIGHT);
		redLabel.setText("Red:");
		final Combo redCombo = new Combo(redComp, SWT.CENTER);
		redCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		redCombo.setItems(dataNames);
		redCombo.select(0);
		redCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				idxR = redCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});
		redRangeSlider = new RangeSlider(redComp, SWT.HORIZONTAL);
		redRangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		redRangeSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				System.out.println("moving");
			}
		});

		Composite greenComp = new Composite(bottomPane, SWT.NONE);
		greenComp.setLayout(new GridLayout(2, false));
		Label greenLabel = new Label(greenComp, SWT.RIGHT);
		greenLabel.setText("Green:");
		final Combo greenCombo = new Combo(greenComp, SWT.CENTER);
		greenCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		greenCombo.setItems(dataNames);
		greenCombo.select(0);
		greenCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				idxG = greenCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});
		greenRangeSlider = new RangeSlider(greenComp, SWT.HORIZONTAL);
		greenRangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		greenRangeSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				System.out.println("moving");

			}
		});

		Composite blueComp = new Composite(bottomPane, SWT.NONE);
		blueComp.setLayout(new GridLayout(2, false));
		Label blueLabel = new Label(blueComp, SWT.RIGHT);
		blueLabel.setText("Blue:");
		final Combo blueCombo = new Combo(blueComp, SWT.CENTER);
		blueCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		blueCombo.setItems(dataNames);
		blueCombo.select(0);
		blueCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				idxB = blueCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});
		blueRangeSlider = new RangeSlider(blueComp, SWT.HORIZONTAL);
		blueRangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		blueRangeSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				System.out.println("moving");

			}
		});

		Button closeButton = new Button(container, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				RGBMixerDialog.this.close();
			}
		});

		return container;
	}

	private void updatePlot() {
		if (data.isEmpty())
			return;
		zeros = new IntegerDataset(data.get(0).getSize());
		zeros.setShape(data.get(0).getShape());
		
		if (idxR >= 0 && idxG >= 0 && idxB >= 0) {
			
			compData = new RGBDataset(data.get(idxR), data.get(idxG), data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR >= 0 && idxG < 0 && idxB < 0) {
			compData = new RGBDataset(data.get(idxR), zeros, zeros);
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG >= 0 && idxB <0) {
			compData = new RGBDataset(zeros, data.get(idxG), zeros);
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG < 0 && idxB >= 0) {
			compData = new RGBDataset(zeros, zeros, data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR >= 0 && idxG >= 0 && idxB < 0) {
			compData = new RGBDataset(data.get(idxR), data.get(idxG), zeros);
			system.updatePlot2D(compData, null, null);
		} else if (idxR >= 0 && idxG < 0 && idxB >= 0) {
			compData = new RGBDataset(data.get(idxR), zeros, data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG >= 0 && idxB >= 0) {
			compData = new RGBDataset(zeros, data.get(idxG), data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG < 0 && idxB < 0) {
			system.clear();
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("RGB Mixer");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}
}
