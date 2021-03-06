/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import javax.measure.Quantity;

import si.uom.SI;
import tec.units.indriya.unit.ProductUnit;

public interface Resolution extends Quantity<Resolution> {
	public static final ProductUnit<Resolution> UNIT
		= new ProductUnit<Resolution>(SI.METRE.pow(-1));
}
