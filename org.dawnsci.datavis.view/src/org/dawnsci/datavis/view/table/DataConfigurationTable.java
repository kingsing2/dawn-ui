package org.dawnsci.datavis.view.table;

import org.dawnsci.datavis.model.ISliceAssist;
import org.dawnsci.datavis.model.NDimensions;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DataConfigurationTable {

	private TableViewer       tableViewer;
	private TableViewerColumn options;
	private TableViewerColumn slice;
	private NDimensions nDimension;
	
	private SliceEditingSupport sliceSupport;
	private DimensionEditSupport dimensionSupport;
	private AxisEditSupport axisSupport;
	
	private Composite tableComposite;
	
//	private HashSet<ISliceChangeListener > listeners;
	
	public DataConfigurationTable() {
//		listeners = new HashSet<>();
	}
	
	public void createControl(Composite parent) {
		
		tableComposite = new Composite(parent, SWT.NONE);
		
		tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.BORDER);
		
		final TableViewerColumn dimension  = new TableViewerColumn(tableViewer, SWT.LEFT, 0);
		dimension.getColumn().setText("Dimension");
		dimension.getColumn().setWidth(80);
		dimension.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return nDimension.getDimensionWithSize((int)element);
			}
		});
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof NDimensions) {
					Integer[] vals = new Integer[((NDimensions)inputElement).getRank()];
					for (int i = 0; i < vals.length; i++) vals[i] = i;
					return vals;
				}
				return null;
			}
		});
		
		tableViewer.getTable().setHeaderVisible(true);
		
		options = new TableViewerColumn(tableViewer, SWT.CENTER, 1);
		options.getColumn().setText("Display");
		options.getColumn().setWidth(60);
		options.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String description = nDimension.getDescription((int)element);
			  return description == null ? "" : description;
			}
		});
		dimensionSupport = new DimensionEditSupport(tableViewer);
		options.setEditingSupport(dimensionSupport);
		
		
		slice = new TableViewerColumn(tableViewer, SWT.CENTER, 2);
		slice.getColumn().setText("Start:Stop:Step");
		slice.getColumn().setWidth(120);
		slice.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
			  Slice slice = nDimension.getSlice((int)element);
			  return slice == null ? "" : slice.toString();
			}
		});
		
		sliceSupport = new SliceEditingSupport(tableViewer);
		
		slice.setEditingSupport(sliceSupport);

		final TableViewerColumn axis   = new TableViewerColumn(tableViewer, SWT.CENTER, 3);
		axis.getColumn().setText("Axes");
		axis.getColumn().setWidth(120);
		axis.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String axes = nDimension.getAxis((int)element);
				
			  return axes == null ? "" : axes;
			}
		});
		axisSupport = new AxisEditSupport(tableViewer);
		axis.setEditingSupport(axisSupport);
		
		
		TableColumnLayout columnLayout = new TableColumnLayout();
	    columnLayout.setColumnData(dimension.getColumn(), new ColumnWeightData(15,20));
	    columnLayout.setColumnData(options.getColumn(), new ColumnWeightData(15,20));
	    columnLayout.setColumnData(slice.getColumn(), new ColumnWeightData(20,20));
	    columnLayout.setColumnData(axis.getColumn(), new ColumnWeightData(50,20));
	    
	    tableComposite.setLayout(columnLayout);
	}
	
	public void setLayoutData(Object layoutData) {
		tableComposite.setLayoutData(layoutData);
	}
	
	public void setInput(NDimensions ndims) {
		if (ndims == nDimension) {
			if (!tableViewer.isCellEditorActive()) {
				tableViewer.refresh();
			}
			return;
		}
		nDimension = ndims;
		tableViewer.setInput(ndims);
		tableViewer.getTable().getParent().layout();
	}
	
	public void clearAll() {
		tableViewer.getTable().clearAll();
	}
	
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	public Control getControl() {
		return tableViewer.getControl();
	}
	
	public void setMaxSliceNumber(int n){
		sliceSupport.setMaxSliceSize(n);
	}
	
	public void refresh() {
		if (!sliceSupport.isActive() && !dimensionSupport.isActive() && !axisSupport.isActive()) {
			tableViewer.refresh();
		}
		
	}
	
	public void setSliceAssist(ISliceAssist sliceAssist) {
		sliceSupport.setSliceAssist(sliceAssist);
	}
	
}
