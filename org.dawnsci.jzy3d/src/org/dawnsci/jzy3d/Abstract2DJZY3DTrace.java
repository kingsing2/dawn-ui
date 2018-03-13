package org.dawnsci.jzy3d;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapGrayscale;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;

public abstract class Abstract2DJZY3DTrace extends AbstractColorMapTrace {
	
	public Abstract2DJZY3DTrace(IPaletteService paletteService, IImageService imageService, String pallette) {
		super(paletteService, imageService, pallette);
	}

	private AbstractDrawable shape;
	private IDataset data;
	private IDataset xAxis;
	private IDataset yAxis;
	
	protected ColorMapper colorMapper;

	public void setData(IDataset data, IDataset[] axes) {
		
		if (getImageServiceBean() == null) {
			bean = new ImageServiceBean(data, HistoType.OUTLIER_VALUES);
		}
		
		double[] fs = imageService.getFastStatistics(bean);
		
		bean.setMin(fs[0]);
		bean.setMax(fs[1]);
		
		float max = bean.getMax().floatValue();
		float min = bean.getMin().floatValue();

		setPaletteData(getPaletteData());
		
		this.data = data;
		int x = data.getShape()[1];
		int y = data.getShape()[0];
		
		FloatDataset z = DatasetUtils.cast(FloatDataset.class, data);
		float[] xArray = null;
		float[] yArray = null;
		
		xAxis = axes[0];
		yAxis = axes[1];
		
		if (axes[0] != null && axes[0].getSize() != data.getShape()[1]) {
			axes[0] = null;
		}
		
		if (axes[1] != null && axes[1].getSize() != data.getShape()[0]) {
			axes[1] = null;
		}

		xArray = (axes[0] != null) ? DatasetUtils.cast(FloatDataset.class, axes[0]).getData() : getRange(x);
		yArray = (axes[1] != null) ? DatasetUtils.cast(FloatDataset.class, axes[1]).getData() : getRange(y);

		final AbstractDrawable surface = buildShape(xArray, yArray, z.getData(), colorMapper);
		
////		final AbstractDrawable surface  = MeshTessellator.buildShape(xArray, yArray, z.getData(),colorMapper);
//		final AbstractDrawable surface  = MeshTessellator.buildShape(xArray, yArray, z.getData());
	
		if (colorMapper == null) {
			colorMapper = new ColorMapper(new ColorMapGrayscale(), min, max, new Color(1, 1, 1, .5f));
		}
		
		if (surface instanceof Shape) {
			configureShape((Shape)surface);
		}
		
		shape = surface;
	       
	}
	
	protected abstract AbstractDrawable buildShape(float[] x, float[] y, float[] z, ColorMapper mapper);
	
	protected abstract void configureShape(Shape shape);
	
	protected float[] getRange(int n) {
		float[] array = new float[n];
		fillRange(array);
		return array;
	}
	
	private void fillRange(float[] array) {

		for (int i = 0; i < array.length; i++) {

			array[i] = i;

		}

	}
	
	
	public AbstractDrawable getShape(){
		return shape;
	}
	

	@Override
	public IDataset getData() {
		return data;
	}


	@Override
	protected void setColorMap(ColorMapper mapper) {
		colorMapper = mapper;
		if (shape instanceof Shape) {
			((Shape)this.shape).setColorMapper(colorMapper);
		}
		
		
	}

	@Override
	public List<IDataset> getAxes() {
		return Arrays.asList(new IDataset[] {xAxis,yAxis});
	}

	@Override
	public boolean isActive() {
		return false;
	}

}