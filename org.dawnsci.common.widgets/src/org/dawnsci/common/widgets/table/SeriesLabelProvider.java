package org.dawnsci.common.widgets.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class SeriesLabelProvider extends ColumnLabelProvider implements ILabelProvider {

	private IStyledLabelProvider delegate;
	
	public SeriesLabelProvider(IStyledLabelProvider delegate) {
		this.delegate = delegate;
	}
	
	public String getText(Object element) {
		return "  "+((SeriesItemContentProposal)element).getLabel();
	}
	
	public Image getImage(Object element) {
		SeriesItemContentProposal prop = (SeriesItemContentProposal)element;
		return delegate.getImage(prop.getDescriptor());
	}
	
	public void dispose() {
		super.dispose();
	}

}
