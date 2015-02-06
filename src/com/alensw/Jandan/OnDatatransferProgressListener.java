package com.alensw.Jandan;

/**
 * Created by yw07 on 15-2-6.
 */
public interface OnDatatransferProgressListener {
	public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName);
}
