package org.dawnsci.plotting.tools.reduction;

import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;

class DataReduction2DToolAvgSpectraRegionDataNode extends DataReduction2DToolSpectraRegionDataNode {

	public DataReduction2DToolAvgSpectraRegionDataNode(IRegion plotRegion, final DataReduction2DToolModel toolModel, final DataReduction2DToolRegionData regionData) {
		super(plotRegion, toolModel, regionData);
	}
	
	@Override
	public DoubleDataset getDataset(DoubleDataset fullData) {
		DoubleDataset result = DatasetFactory.zeros(DoubleDataset.class, 0, fullData.getShapeRef()[1]);
		for (DataReduction2DToolSpectrumDataNode node : this.getSpectra()) {
			int i = node.getIndex();
			DoubleDataset data = (DoubleDataset) fullData.getSliceView(new int[]{i, 0}, new int[]{i + 1, fullData.getShape()[1]}, new int[]{1,1});
			data.setShape(1, fullData.getShape()[1]);
			result = (DoubleDataset) DatasetUtils.append(result, data, 0);
		}
		result = (DoubleDataset) result.mean(0);
		result.setShape(1, fullData.getShape()[1]);
		return result;
	}
	
	@Override
	public String toString() {
		
		int noAve = getTotalSpectra();
		
		return super.toString() + " avg(" + noAve + ")";
	}

	public int getNoOfSpectraToAvg() {
		return getTotalSpectra();
	}
}
