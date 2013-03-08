package org.dawnsci.plotting.tools.history;

import java.util.Iterator;
import java.util.Map;

import org.dawb.common.services.IVariableManager;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawnsci.plotting.Activator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public abstract class AbstractHistoryTool extends AbstractToolPage implements MouseListener, KeyListener, IVariableManager {

	protected final static Logger logger = LoggerFactory.getLogger(AbstractHistoryTool.class);
	
	public enum HistoryType {
    	HISTORY_PLOT;
    }

    protected Composite      composite;
    protected TableViewer    viewer;
    protected ITraceListener traceListener;
	
	public AbstractHistoryTool(boolean requireCreateListener) {
		if (requireCreateListener) this.traceListener = new ITraceListener.Stub() {
			
			@Override
			public void tracesAdded(TraceEvent evt) {
				updatePlots(false);
			}
			
			@Override
			public void tracesRemoved(TraceEvent evet) {
				updatePlots(false);
			}
		};
	}

	/**
	 * Implement to return the cache of HistoryBeans. Normally this is a 
	 * static map kept by the sub-classes.
	 * 
	 * @return
	 */
    protected abstract Map<String, HistoryBean> getHistoryCache();

	/**
	 * Call to process history beans and update the plot with the
	 * compare data
	 */
	protected abstract void updatePlot(HistoryBean bean, boolean force);
	

	/**
	 * Called to return an IAction for adding a history bean to the cache.
	 */
	protected abstract IAction createAddAction();
	
	/**
	 * Called to create the table columns for interacting with the history
	 */
	protected abstract void createColumns(TableViewer viewer);

	
	/**
	 * 
	 */
	protected boolean updatingAPlotAlready = false;
	protected boolean updatingPlotsAlready = false;
    
	protected void updatePlots(boolean force) {
		
		if (updatingPlotsAlready) return;
		try {
			updatingPlotsAlready = true;
			// Loop over history and reprocess it all.
			for (String key : getHistoryCache().keySet()) {
				updatePlot(getHistoryCache().get(key), force);
			}
		} finally {
			updatingPlotsAlready = false;
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		viewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		createColumns(viewer);
		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		createActions(new MenuManager());
				
		getSite().setSelectionProvider(viewer);
		
		viewer.setContentProvider(new IStructuredContentProvider() {			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub				
			}			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub				
			}		
			@Override
			public Object[] getElements(Object inputElement) {
				return getHistoryCache().values().toArray(new HistoryBean[getHistoryCache().size()]);
			}
		});
		viewer.setInput(new Object());

		viewer.getTable().addMouseListener(this);
		viewer.getTable().addKeyListener(this);
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.character==SWT.DEL) {
			deleteSelectedBean();
		}
	}
	
	public void keyReleased(KeyEvent e) {
		
	}

	
	/**
	 * May be overridden to provide additional actions.
	 */
	protected MenuManager createActions(final MenuManager rightClick) {
		
		final IAction addPlot = createAddAction();
		getSite().getActionBars().getToolBarManager().add(addPlot);
		rightClick.add(addPlot);
		
		final Action deletePlot = new Action("Delete selected", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				deleteSelectedBean();
			}
		};
		getSite().getActionBars().getToolBarManager().add(deletePlot);
		rightClick.add(deletePlot);
		
		final Action clearPlots = new Action("Clear history", Activator.getImageDescriptor("icons/plot-tool-history-clear.png")) {
			public void run() {
				for (HistoryBean bean : getHistoryCache().values()) {
					if (!bean.isModifiable()) continue;
					bean.setSelected(false);
				}
				updatePlots(false);
				clearCache();
			    refresh();
			}
		};
		getSite().getActionBars().getToolBarManager().add(clearPlots);
		rightClick.add(clearPlots);

		rightClick.add(new Separator());
		final Action checkAll = new Action("Select all") {
			public void run() {
				setAllChecked(true);
			}
		};
		rightClick.add(checkAll);
		final Action checkNone = new Action("Select none") {
			public void run() {
				setAllChecked(false);
			}
		};
		rightClick.add(checkNone);
		getSite().getActionBars().getToolBarManager().add(new Separator());
		rightClick.add(new Separator());

		final Action editAction = new Action("Rename selected", IAction.AS_PUSH_BUTTON) {
			public void run() {
				viewer.editElement(getSelectedPlot(), 1);
			}
		};
		editAction.setImageDescriptor(Activator.getImageDescriptor("icons/rename_plot.png"));
		getSite().getActionBars().getToolBarManager().add(editAction);
		rightClick.add(editAction);
		
		getSite().getActionBars().getToolBarManager().add(new Separator());
		final Action addExpression = new Action("Add expression") {
			public void run() {
				final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
				try {
					cs.getCommand("org.dawb.workbench.editors.addExpression").executeWithChecks(new ExecutionEvent());
				} catch (Exception e) {
					logger.error("Cannot run action", e);
				} 
				
			}
		};
		addExpression.setImageDescriptor(Activator.getImageDescriptor("icons/add_expression.png"));
		addExpression.setToolTipText("Adds an expression which can be plotted. Must be function of other data sets.");
		getSite().getActionBars().getToolBarManager().add(addExpression);
		rightClick.add(addExpression);
		
		final Action deleteExpression = new Action("Delete expression") {
			public void run() {
				final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
				try {
				    cs.getCommand("org.dawb.workbench.editors.deleteExpression").executeWithChecks(new ExecutionEvent());
				} catch (Exception e) {
					logger.error("Cannot run action", e);
				} 
			}
		};
		deleteExpression.setImageDescriptor(Activator.getImageDescriptor("icons/delete_expression.png"));
		deleteExpression.setToolTipText("Deletes an expression.");
		getSite().getActionBars().getToolBarManager().add(deleteExpression);
		rightClick.add(deleteExpression);

		
		final Menu menu = rightClick.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		
		return rightClick;
	}

	protected void deleteSelectedBean() {
		final HistoryBean bean = getSelectedPlot();
		if (bean==null) return;
		if (!bean.isModifiable()) return;
		bean.setSelected(false);
		updatePlot(bean, false);
		getHistoryCache().remove(bean.getTraceKey());
	    refresh();
	}

	protected void setAllChecked(boolean checked) {
		for (HistoryBean bean : getHistoryCache().values()) {
			if (!bean.isModifiable()) continue;
			bean.setSelected(checked);
		}
		updatePlots(false);
	    refresh();		
	}

	protected void clearCache() {
		final Iterator<String> it = getHistoryCache().keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!getHistoryCache().get(key).isModifiable()) continue;
			it.remove();
		}
	}

	@Override
	public Control getControl() {
		return this.composite;
	}

	@Override
	public void setFocus() {
        if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}
	
	
	protected void refresh() {
		if (viewer==null || viewer.getControl().isDisposed()) return;
		viewer.refresh();
	}
	
	@Override
	public void activate() {
		
        updatePlots(false);
        
        if (getPlottingSystem()!=null) {
        	getPlottingSystem().addTraceListener(this.traceListener);
        }
        super.activate();
        refresh();
	}
	
	@Override
	public void deactivate() {
        if (getPlottingSystem()!=null) {
        	getPlottingSystem().removeTraceListener(this.traceListener);
        }
		super.deactivate();
	}

	@Override
	public void dispose() {
	    if (viewer!=null&&!viewer.getControl().isDisposed()) {
	    	viewer.getControl().removeMouseListener(this);
	    	viewer.getControl().removeKeyListener(this);
	    }
	    deactivate();
		super.dispose();
	}


	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button==1) {
			
			// Toggle if first column clicked.
			Point     pnt  = new Point(e.x, e.y);
			TableItem item = viewer.getTable().getItem(pnt);
	        if (item == null) return;
            Rectangle rect = item.getBounds(0);
            if (rect.contains(pnt)) {
  				toggleSelection();
	        } 	          
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	protected HistoryBean toggleSelection() {
		final HistoryBean bean = getSelectedPlot();
		if (bean!=null) {
 			viewer.cancelEditing();
			bean.setSelected(!bean.isSelected());
			viewer.update(bean, null);
			updatePlot(bean, true);
			return bean;
		}
		return null;
	}
	
	protected HistoryBean getSelectedPlot() {
		final StructuredSelection sel = (StructuredSelection)viewer.getSelection();
		if (sel.getFirstElement()!=null && sel.getFirstElement() instanceof HistoryBean) {
			return (HistoryBean)sel.getFirstElement();
		}
		return null;
	}
	

	@Override
	public boolean isVariableName(String name, IMonitor monitor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractDataset getVariableValue(String expressionName, IMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}	

	@Override
	public void deleteExpression() {
		
	}


	@Override
	public void addExpression() {
		
	}


	@Override
	public boolean isAlwaysSeparateView() {
		return true;
	}

}
