package org.dawnsci.datavis.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IDataPackage;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDatasetBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;


public class LoadedFile implements IDataObject, IDataFilePackage {

	private final static Logger logger = LoggerFactory.getLogger(LoadedFile.class);
	
	protected AtomicReference<IDataHolder> dataHolder;
	protected Map<String,DataOptions> dataOptions;
	protected Map<String, ILazyDataset> possibleLabels;
	protected boolean onlySignals = false;
	protected Set<String> signals;
	private boolean selected = false;
	private String labelName = "";
	private String label = "";

	public LoadedFile(IDataHolder dataHolder) {
		this.dataHolder = new AtomicReference<IDataHolder>(dataHolder.clone());
		this.signals = new LinkedHashSet<>();
		dataOptions = new LinkedHashMap<>();
		possibleLabels = new TreeMap<>();
		String[] names = null;
		if (dataHolder.getTree() != null) {
			try {
				Map<DataNode, String> uniqueDataNodes = getUniqueDataNodes(dataHolder.getTree().getGroupNode());
				Collection<String> values = uniqueDataNodes.values();
				names = new String[values.size()];
				int count = 0;
				for (String v : values) {
					names[count++] = "/" + v;
				}
//				names = values.toArray(new String[values.size()]);
			} catch ( Exception e) {
				logger.error("Could not get unique nodes",e);
				this.signals = new HashSet<>();
			}
			
		}
		
		if (names == null) names = dataHolder.getNames();
		for (String n : names) {
			ILazyDataset lazyDataset = dataHolder.getLazyDataset(n);
			
			if (lazyDataset != null && ((LazyDatasetBase)lazyDataset).getDType() != Dataset.STRING && lazyDataset.getSize() != 1) {
				DataOptions d = new DataOptions(n, this);
				dataOptions.put(d.getName(),d);
			} else {
				if (signals.contains(n)) {
					signals.remove(n);
				}
			}
			
			if (lazyDataset != null && lazyDataset.getSize() == 1) {
				possibleLabels.put(n,lazyDataset);
			}
		}
	}

	public List<DataOptions> getDataOptions() {
		
		if (onlySignals && !signals.isEmpty()) {
			return signals.stream().map(s -> dataOptions.get(s)).collect(Collectors.toList());
		}
		return new ArrayList<>(dataOptions.values());
	}

	public List<DataOptions> getSelectedDataOptions() {
		List<DataOptions> list = dataOptions.values().stream()
		.filter(dOp -> dOp.isSelected())
		.collect(Collectors.toList());
		return list;
	}
	
	public DataOptions getDataOption(String name) {
		
		return dataOptions.get(name);
	}
	
	@Override
	public String getName() {
		File f = new File(dataHolder.get().getFilePath());
		return f.getName();
	}
	
	public String getParent() {
		File f = new File(dataHolder.get().getFilePath());
		return f.getParent();
	}
	
	public String getFilePath() {
		return dataHolder.get().getFilePath();
	}
	
	public ILazyDataset getLazyDataset(String name){
		return dataHolder.get().getLazyDataset(name);
	}
	
	public Tree getTree() {
		return dataHolder.get().getTree();
	}
	
	public Map<String, int[]> getDataShapes(){
		
		IDataHolder dh = dataHolder.get();
		
		//use metadata if possible
		if (dh.getMetadata() != null && dh.getMetadata().getDataShapes() != null) {
			Map<String, int[]> ds = dataHolder.get().getMetadata().getDataShapes();
			ds = new HashMap<>(ds);
			for (String s : ds.keySet()) {
				if (ds.get(s) == null) {
					ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(s);
					if (lazyDataset != null) ds.put(s, lazyDataset.getShape());
				}
			}
			
			return ds;
		} else {
			String[] ds = dataHolder.get().getNames();
			Map<String, int[]> dsmap = new HashMap<>();
			for (String s : ds) {

				ILazyDataset lazyDataset = dataHolder.get().getLazyDataset(s);
				if (lazyDataset != null) dsmap.put(s, lazyDataset.getShape());

			}

			return dsmap;
		}
		
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public List<DataOptions> getChecked() {
		
		List<DataOptions> checked = new ArrayList<>();
		
		for (DataOptions op : dataOptions.values()) {
			if (op.isSelected()) {
				checked.add(op);
			}
		}
		
		return checked;
	}

	@Override
	public IDataPackage[] getDataPackages() {
		return dataOptions.values().stream().toArray(size ->new IDataPackage[size]);
	}
	
	public Map<DataNode,String> getUniqueDataNodes(GroupNode node) {
		Set<DataNode> nodes = new HashSet<>();
		
		IFindInTree tree = new IFindInTree() {
			
			@Override
			public boolean found(NodeLink node) {
				Node d = node.getDestination();
				Node s = node.getSource();
				
				boolean nxData = false;
				
				if (s != null && s.containsAttribute(NexusTreeUtils.NX_CLASS) && NexusTreeUtils.NX_DATA.equals(s.getAttribute(NexusTreeUtils.NX_CLASS).getFirstElement())) {
					nxData = true;
				}
				
				if (d != null && d instanceof DataNode) {
					
					if (nxData || !nodes.contains(d)) {
						nodes.add((DataNode)d);
						return true;
					}
				}
				return false;
			}
		};
		
		Map<String, NodeLink> results = TreeUtils.treeBreadthFirstSearch(node, tree, false, true, null);
		
		Map<DataNode, String> out = new LinkedHashMap<DataNode, String>();
		
		for (Entry<String, NodeLink> e: results.entrySet()) {
			Node d = e.getValue().getDestination();
			if (d instanceof DataNode) {
				out.put((DataNode)d, e.getKey());
			}
			
			Node s = e.getValue().getSource();
			
			if (s != null && s.containsAttribute(NexusTreeUtils.NX_CLASS) && NexusTreeUtils.NX_DATA.equals(s.getAttribute(NexusTreeUtils.NX_CLASS).getFirstElement())) {
				if (s.containsAttribute(NexusTreeUtils.NX_SIGNAL)) {
					String name = s.getAttribute(NexusTreeUtils.NX_SIGNAL).getFirstElement();
					
					String key = e.getKey();
					String[] split = key.split("/");
					
					if (split[split.length-1].equals(name)) {
						signals.add("/" + key);
						ILazyDataset lz = NexusTreeUtils.getAugmentedSignalDataset((GroupNode)s);
						if (lz != null) {
							dataHolder.get().addDataset("/" + name, lz);
						}
					}
				} else if (d.containsAttribute(NexusTreeUtils.NX_SIGNAL)) {
					signals.add("/" + e.getKey());
					ILazyDataset lz = NexusTreeUtils.getAugmentedSignalDataset((GroupNode)s);
					if (lz != null) {
						dataHolder.get().addDataset("/" + e.getKey(), lz);
					}
				}
				
			}
		}
		
		return out;
		
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabelName() {
		return labelName;
	}
	
	public Collection<String> getLabelOptions() {
		return possibleLabels.keySet();
	}

	public void setLabelName(String labelName) {
		if (labelName == null) {
			this.labelName = "";
			this.label = "";
			return;
		}
		this.labelName = labelName;
		if (possibleLabels.containsKey(labelName)) {
			ILazyDataset l = possibleLabels.get(labelName);
			
			try {
				IDataset slice = l.getSlice();
				slice = slice.squeeze();
				label = slice.getString();
			} catch (DatasetException e) {
				logger.error("Could not read label {}", labelName,e);
				label = "";
			}
		} else {
			label = "";
		}
	}
	
	public boolean isOnlySignals() {
		return onlySignals;
	}

	public void setOnlySignals(boolean onlySignals) {
		this.onlySignals = onlySignals;
	}
}
