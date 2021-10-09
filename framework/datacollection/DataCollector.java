package dbexp.framework.datacollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbexp.framework.progressmonitor.LightWeightLogger;
import dbexp.framework.progressmonitor.Logger;

/**
 * A DataCollector is an in-memory store of experimental results. Each thread at least should have its own instance of a data collector
 * @author Shirley Goldrei
 *
 */
public class DataCollector {
	
	Logger LOG = new LightWeightLogger(DataCollector.class.getName());
	
	protected final String mDataSetName;
	protected final Map<String,List<Object>> mDatapoints;
	
	public DataCollector(String aDataSetName) {
		mDataSetName = aDataSetName;
		mDatapoints = new HashMap<String, List<Object>>();
	}
	
	public DataCollector(String aDataSetName, String aDataFormat) {
		mDataSetName = aDataSetName;
		mDatapoints = new HashMap<String, List<Object>>();
	}
	
	public String getName() {
		return mDataSetName;
	}
	
	public Set<String> getDataNames() {
		return mDatapoints.keySet();
	}
	
	public List<Object> getDataValues(String aDataName) {
		return mDatapoints.get(aDataName);
	}
	
	public void add(String aDataName, Object aDataValue) {
		List<Object> values = mDatapoints.get(aDataName);
		if (values == null) {
			values = new ArrayList<Object>();
			mDatapoints.put(aDataName, values);
		}
		values.add(aDataValue);
		LOG.debug("Collect data for " + aDataName + " Value is " + aDataValue.toString());
	}
	
	public void addAll(String aDataName, Collection<? extends Object> someDataValues) {
		List<Object> values = mDatapoints.get(aDataName);
		if (values == null) {
			values = new ArrayList<Object>();
			mDatapoints.put(aDataName, values);
		}
		values.addAll(someDataValues);
	}

	public void splice(DataCollector anotherCollector) {
		Set<String> keySet  = anotherCollector.mDatapoints.keySet();
		for (String label : keySet) {
			this.addAll(label, anotherCollector.mDatapoints.get(label));
		}	
	}
	
	public void concat(DataCollector anotherCollector) {
		Set<String> keySet  = anotherCollector.mDatapoints.keySet();
		for (String label : keySet) {
			this.addAll(anotherCollector.getName() + '.' + label, anotherCollector.mDatapoints.get(label));
		}
	}
	
	public Object getData(String aDataName, int index) {
		return getDataValues(aDataName).get(index);
	}
	
	public void setData(String aDataName, int index, Object value) {
		getDataValues(aDataName).set(index, value);
	}
	
	public void clear() {
		mDatapoints.clear();
	}
	
}
