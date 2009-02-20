package org.workcraft.framework.interop;

public interface ExternalProcessListener {
	public void outputData(final byte[] data);
	public void errorData(final byte[] data);
	public void processFinished(int returnCode);
}