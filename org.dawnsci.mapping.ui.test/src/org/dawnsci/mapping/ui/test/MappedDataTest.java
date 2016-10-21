package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.*;

import java.io.File;

import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataType;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class MappedDataTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static MappedDataBlock gridScanBlock = null;
	private static MappedData gridScanMap = null;
	private static File file = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		ILazyDataset lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		
		gridScanBlock = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, 1, 0, file.getAbsolutePath());
		
		ILazyDataset sum = data.getLazyDataset(MapNexusFileBuilderUtils.SUM_PATH);
		
		gridScanMap = new MappedData(MapNexusFileBuilderUtils.SUM_PATH, sum.getSlice(), gridScanBlock,file.getAbsolutePath());
	}
	
	
	
	@Test
	public void testGetSpectrum() {
		IDataset spectrum = gridScanMap.getSpectrum(0, 0);
		Dataset d = DatasetUtils.convertToDataset(spectrum);
		assertEquals(d.getElementDoubleAbs(0), 0,0);
		assertEquals(d.getElementDoubleAbs(d.getSize()-1), d.getSize()-1,0);
	}

	@Ignore
	@Test
	public void testMakeNewMapWithParent() {
		DoubleDataset rand = Random.rand(gridScanMap.getData().getShape());
		AxesMetadata ax = gridScanMap.getData().getFirstMetadata(AxesMetadata.class);
		MetadataType clone = ax.clone();
		rand.setMetadata(clone);
		MappedData map = gridScanMap.makeNewMapWithParent("random", Random.rand(gridScanMap.getData().getShape()));
		assertEquals(gridScanBlock, map.getParent());
	}

	@Test
	public void testIsLive() {
		assertFalse(gridScanMap.isLive());
	}

	@Test
	public void testGetData() {
		IDataset d = gridScanMap.getData();
		assertNotNull(d);
	}

	@Test
	public void testHasChildren() {
		assertFalse(gridScanMap.hasChildren());
	}

	@Test
	public void testGetChildren() {
		assertNull(gridScanMap.getChildren());
	}

	@Test
	public void testGetTransparency() {
		int t = gridScanMap.getTransparency();
		assertEquals(-1, t);
	}

	@Test
	public void testSetTransparency() {
		gridScanMap.setTransparency(10);
		int t = gridScanMap.getTransparency();
		assertEquals(10, t);
		gridScanMap.setTransparency(-1);
		t = gridScanMap.getTransparency();
		assertEquals(-1, t);
	}

	@Test
	public void testGetParent() {
		assertEquals(gridScanBlock, gridScanMap.getParent());
	}


	@Test
	public void testGetRange() {
		double[] range = gridScanMap.getRange();
		assertArrayEquals(new double[]{-0.5, 10.5, -0.5, 9.5}, range, 1);
	}

	@Test
	public void testGetLongName() {
		assertEquals(file.getAbsolutePath() +" : "+MapNexusFileBuilderUtils.SUM_PATH, gridScanMap.getLongName());
	}


}