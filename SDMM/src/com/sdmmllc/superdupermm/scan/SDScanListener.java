package com.sdmmllc.superdupermm.scan;

public interface SDScanListener {
	public void updateScanProgress(int scanItemsTotal, int scanItemsCompleted);
	public void compilingResults();
	public void scanComplete(String pkgName, String action);
}
