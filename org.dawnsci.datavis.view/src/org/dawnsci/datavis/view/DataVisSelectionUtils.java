package org.dawnsci.datavis.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class DataVisSelectionUtils {

	public static <U> List<U> getFromSelection(ISelection selection, Class<U> clazz){

		if (selection instanceof StructuredSelection) {

			return Arrays.stream(((StructuredSelection)selection).toArray())
					.filter(clazz::isInstance)
					.map(clazz::cast).collect(Collectors.toList());

		}

		return Collections.emptyList();
	}
	
	
}
