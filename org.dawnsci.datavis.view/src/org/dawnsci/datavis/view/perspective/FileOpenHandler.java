package org.dawnsci.datavis.view.perspective;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class FileOpenHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell,SWT.MULTI);
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IRecentPlaces recentPlaces = bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));
		final EventAdmin admin = bundleContext.getService(bundleContext.getServiceReference(EventAdmin.class));

		if (!recentPlaces.getRecentPlaces().isEmpty()) {
			dialog.setFilterPath(recentPlaces.getRecentPlaces().get(0));
		}
		
		if (dialog.open() == null) return null;

		String[] fileNames = dialog.getFileNames();
		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];

		Map<String,String[]> props = new HashMap<>();
		props.put("paths", fileNames);
		
		admin.sendEvent(new Event(DataVisConstants.FILE_OPEN_EVENT, props));
		
		return null;
	}


}
