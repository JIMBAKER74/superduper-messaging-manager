package com.sdmmllc.superdupermm.scan;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sdmmllc.superdupermm.data.SDIntentFilters;
import com.sdmmllc.superdupermm.data.SDIntentFilters.Filter;

public class AppUsesPermissions {

	public static final String APP_USES_PERMISSIONS_TBL = "UsesPermission";
	
	public static final String[] APP_USES_PERMISSIONS_COLUMNS = {
    	"up_ID",
	    "up_permission_ID", 
	    "up_package_ID",
	    "up_priority_num",
	    "up_protection_lvl_num",
	};
	
	public int pkgId = -1;

	public AppUsesPermissions() {
		perms = new Hashtable<Integer, UsesFilter>();
	}
	
	private Hashtable<Integer, UsesFilter> perms;
	
	public void setFilter(SDIntentFilters.Filter filter, int level, int priority) {
		UsesFilter mFilter = new UsesFilter(perms.size(), level, priority, filter);
		perms.put(perms.size(), mFilter);
	}
	
	public void setFilter(UsesFilter filter) {
		perms.put(perms.size(), filter);
	}
	
	public UsesFilter getFilter(int idx) {
		return perms.get(idx);
	}
	
	public boolean contains(UsesFilter filter) {
		return perms.containsValue(filter);
	}
	
	public boolean contains(Filter filter) {
		for (UsesFilter mFilter : perms.values()) {
			if (mFilter.mFilter.equals(filter)) return true;
		}
		return false;
	}
	
	public void setFilterLevel(UsesFilter filter, int level) {
		for (UsesFilter mFilter : perms.values()) {
			if (mFilter.mFilter.equals(filter.mFilter)) mFilter.permProtectionLvl = level;
		}
	}
	
	public void setFilterLevel(Filter filter, int level) {
		for (UsesFilter mFilter : perms.values()) {
			if (mFilter.mFilter.equals(filter)) mFilter.permProtectionLvl = level;
		}
	}
	
	public void setFilterPriority(UsesFilter filter, int priority) {
		for (UsesFilter mFilter : perms.values()) {
			if (mFilter.mFilter.equals(filter.mFilter)) mFilter.permPriority = priority;
		}
	}
	
	public void setFilterPriority(Filter filter, int priority) {
		for (UsesFilter mFilter : perms.values()) {
			if (mFilter.mFilter.equals(filter)) mFilter.permPriority = priority;
		}
	}
	
	public int size() {
		return perms.size();
	}
	
	public Enumeration<Integer> keys() {
		return perms.keys();
	}
	
	public class UsesFilter {
		public int permId,
			permProtectionLvl,
			permPriority;

		public SDIntentFilters.Filter mFilter;
		
		public UsesFilter(int id, int lvl, int priority, SDIntentFilters.Filter filter ) {
			permId = id;
			permProtectionLvl = lvl;
			permPriority = priority;
			mFilter = filter;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((mFilter == null) ? 0 : mFilter.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UsesFilter other = (UsesFilter) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (mFilter == null) {
				if (other.mFilter != null)
					return false;
			} else if (!mFilter.equals(other.mFilter))
				return false;
			return true;
		}

		private AppUsesPermissions getOuterType() {
			return AppUsesPermissions.this;
		}
	}
}
