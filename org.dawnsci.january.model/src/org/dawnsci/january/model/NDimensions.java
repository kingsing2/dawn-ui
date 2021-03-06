package org.dawnsci.january.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;

public class NDimensions {

	public static final String INDICES = "indices";
	
	private Dimension[] dimensions;
	private LinkedHashSet<ISliceChangeListener > listeners;
	private Object[] options = null;
	private Object parent = null;
	private boolean sliceFullRange = false;
	
	public NDimensions(int[] shape, Object parent) {
		dimensions = new Dimension[shape.length];
		for (int i = 0; i < dimensions.length; i++) dimensions[i] = new Dimension(i, shape[i]);
		listeners = new LinkedHashSet<>();
		this.parent = parent;
	}
	
	public NDimensions(NDimensions toCopy) {
		this.dimensions = new Dimension[toCopy.dimensions.length];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = new Dimension(toCopy.dimensions[i]);
		}
		this.options = toCopy.options != null ? toCopy.options.clone() : null;
		listeners = new LinkedHashSet<>();
		this.parent = toCopy.parent;
		this.sliceFullRange = toCopy.sliceFullRange;
	}
	
	public void setOptions(Object[] options) {
		this.options = options;
		int c = 0;
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i].setSlice(sliceFullRange ? new Slice(dimensions[i].getSize()) : new Slice(0, 1, 1));
			dimensions[i].setDescription(null);
		}
		
		for (int i = dimensions.length-1 ; i >=0 ; i-- ) {
			if (c >= options.length) break;
			if (dimensions[i].getSize() == 1) continue;
			dimensions[i].setDescription(options[c++].toString());
			dimensions[i].setSlice(new Slice(0,dimensions[i].getSize()));
		}
		update(true);
	}
	
	public String[] getDimensionOptions(){

		int[] shape = Arrays.stream(dimensions).mapToInt(d -> d.getSize()).toArray();
		//check whether a blank needs to be added
		int[] squeezed = ShapeUtils.squeezeShape(shape, false);
		int length = options.length;
		if (options.length != squeezed.length) length++;
		
		String[] o = new String[length];
		for (int i = 0; i < options.length; i++) o[i] = options[i].toString();
		//dont put blank if squeezed shape equals options length
		if (o.length > options.length) o[options.length] = "";
		return o;
	}
	
	public int getRank() {
		return dimensions.length;
	}
	
	public Slice getSlice(int i) {
		return dimensions[i].getSlice();
	}
	
	public void setSlice(int i, Slice slice) {
		dimensions[i].setSlice(slice);
		update(false);
	}

	public String getDescription(int i) {
		return dimensions[i].getDescription();
	}
	
	public int getSize(int i) {
		return dimensions[i].getSize();
	}

	public String getAxis(int i) {
		return dimensions[i].getAxis();
	}
	
	public void setAxis(int i, String axis) {
		dimensions[i].setAxis(axis);
		updateAxis();
	}
	
	public String[] getAxisOptions(int i) {
		return dimensions[i].getAxisOptions();
	}

	public void setDescription(int i, String description) {
		updateDescription(dimensions[i], description);
		updateOption();
	}
	
	public String getDimensionWithSize(int i) {
		return dimensions[i].getDimensionWithSize();
	}
	
	private void update(boolean optionChange) {
		SliceChangeEvent sliceChangeEvent = new SliceChangeEvent(new NDimensions(this), optionChange, parent);
		fireSliceListeners(sliceChangeEvent);
	}
	
	private void updateAxis() {
		SliceChangeEvent sliceChangeEvent = new SliceChangeEvent(new NDimensions(this), false, parent);
		fireAxisListeners(sliceChangeEvent);
	}
	
	private void updateOption() {
		SliceChangeEvent sliceChangeEvent = new SliceChangeEvent(new NDimensions(this), true, parent);
		fireOptionListeners(sliceChangeEvent);
	}
	
	private void updateDescription(Dimension dim, String desc) {
		String description = null;
		String oldDescription = dim.getDescription();
		
		if (dim.getSize() == 1) {
			description = dim.getDescription();
			dim.setDescription(null);
			dim.setSlice(sliceFullRange ? new Slice(dim.getSize()) : new Slice(0, 1, 1));
			return;
		}
		
		Arrays.stream(dimensions)
			.filter(d -> d.getDescription() != null && d.getDescription().equals(desc))
			.findAny()
			.ifPresent(d -> {
				d.setDescription(null);
				d.setSlice(sliceFullRange ? new Slice(d.getSize()) : new Slice(0, 1, 1));
			});
		
		if (desc == null || desc.isEmpty()) {
			description = dim.getDescription();
			dim.setDescription(null);
			dim.setSlice(sliceFullRange ? new Slice(dim.getSize()) : new Slice(1));
		} else if (dim.getDescription().isEmpty()) {
			dim.setSlice(new Slice(dim.getSize()));
		} else {
			description = dim.getDescription();
			for (int i = dimensions.length-1; i >= 0 ; i--) {
				if (dimensions[i].getDescription().isEmpty()) {
					if (dimensions[i] == dim || dimensions[i].getSize() ==1 ) continue;
					else {
						dimensions[i].setDescription(description);
						dimensions[i].setSlice(new Slice(dimensions[i].getSize()));
						break;
					}
				}
			}
		}
		
		dim.setDescription(desc);

		for (Dimension d : dimensions) {
			if (d != dim && desc != null && desc.equals(d.getDescription())) {
				d.setDescription(null);
				d.setSlice(sliceFullRange ? new Slice(dim.getSize()) : new Slice(1));
			}
		}
		
		if (!oldDescription.isEmpty() && getDimensionFromDescription(oldDescription) < 0){
			int fastest = getFastestFreeDimension();
			if (fastest > -1) {
				dimensions[fastest].setDescription(oldDescription);
				dimensions[fastest].setSlice(new Slice(dimensions[fastest].getSize()));
			}
		}
	}
	
	private int getDimensionFromDescription(String description){
		for (int i = dimensions.length-1; i >= 0; i--){
			Dimension d = dimensions[i];
			if (d.getDescription().equals(description)) return i;
		}
		
		return -1;
	}
	
	private int getFastestFreeDimension(){
		for (int i = dimensions.length-1; i >= 0; i--){
			Dimension d = dimensions[i];
			if (d.getDescription().isEmpty() && d.getSize() != 1) return i;
		}
		
		return -1;
	}
	
	public void updateShape(int[] shape) {
		if (shape.length != dimensions.length) throw new IllegalArgumentException("shape length must match dimension length");
		for (int i = 0 ; i < shape.length; i++) {
			boolean containsEnd = false;
			boolean isSingle = false;
			
	
			if (dimensions[i].getSlice() != null && dimensions[i].getSize() != 1) {
				int size = dimensions[i].getSize();
				Slice slice = dimensions[i].getSlice();

				if (size == slice.getStop()) {
					containsEnd = true;
				}
				
				Integer start = slice.getStart();
				
				if (start == null) start = 0;
				
				if (size-1 == start) {
					isSingle = true;
				}
			}
			dimensions[i].setSize(shape[i]);
			if (containsEnd) {
				Slice slice = dimensions[i].getSlice();
				slice.setStop(shape[i]);
			}
			
			if (isSingle) {
				Slice slice = dimensions[i].getSlice();
				slice.setStart(shape[i]-1);
			}
			
			update(false);
		}
	}
	
	public void setUpAxes(String name, Map<String,int[]> axes, String[] primary) {
		
		@SuppressWarnings("unchecked")
		List<String>[] options = new List[dimensions.length];
		for (int i = 0 ; i < options.length; i++) {
			options[i] = new ArrayList<String>();
			if (primary != null &&  primary.length > i && primary[i] != null) {
				options[i].add(primary[i]);
				dimensions[i].setAxis(primary[i]);
			} else {
				dimensions[i].setAxis(INDICES);
			}
			options[i].add(INDICES);
		}
		
		for (Entry<String,int[]> e : axes.entrySet()) {
			if (e.getValue() != null) {
				for (Integer i : e.getValue()) {
					for (int j = 0; j < dimensions.length ; j++) {
						if (dimensions[j].getSize() == i && !e.getKey().equals(name))
							options[j].add(e.getKey());
					}
				}
			}	
		}
		
		for (int i = 0 ; i < dimensions.length ; i++) {
			String[] uniqueOptions = options[i].stream().distinct().toArray(String[]::new);
			dimensions[i].setAxisOptions(uniqueOptions);
		}
		
	}
	
	public SliceND buildSliceND() {
		int[] shape = new int[dimensions.length];
		for (int i = 0; i < dimensions.length;i++) shape[i] = dimensions[i].getSize();
		SliceND slice = new SliceND(shape);
		for (int i = 0; i < dimensions.length;i++) {
			Slice s = dimensions[i].getSlice();
			slice.setSlice(i, s.getStart(), s.getStop(), s.getStep());
		}
		return slice;
	}
	
	public void addSliceListener(ISliceChangeListener listener) {
		listeners.add(listener);
	}

	public void removeSliceListener(ISliceChangeListener listener) {
		listeners.remove(listener);
	}

	private void fireSliceListeners(SliceChangeEvent event) {
		for (ISliceChangeListener listener : listeners)
			listener.sliceChanged(event);
	}
	
	private void fireAxisListeners(SliceChangeEvent event) {
		for (ISliceChangeListener listener : listeners)
			listener.axisChanged(event);
	}
	
	private void fireOptionListeners(SliceChangeEvent event) {
		for (ISliceChangeListener listener : listeners)
			listener.optionsChanged(event);
	}
	
	public Object[] getOptions(){
		Object[] object = new Object[dimensions.length];

		for (int i = 0; i < dimensions.length; i++) {
			object[i] = dimensions[i].getDescription();
		}

		return object;
	}
	
	public String[] buildAxesNames() {
		String[] names = new String[dimensions.length];
		for (int i = 0; i < dimensions.length;i++) {
			String name = dimensions[i].getAxis();
			if (INDICES.equals(name)) name = "";
			names[i] = name;
		}

		return names;
	}
	
	public void setSliceFullRange(boolean sliceFullRange) {
		this.sliceFullRange = sliceFullRange;
	}

}
