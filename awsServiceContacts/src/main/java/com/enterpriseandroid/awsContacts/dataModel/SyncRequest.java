package com.enterpriseandroid.awsContacts.dataModel;

import java.io.Serializable;
import java.util.List;

public class SyncRequest implements Serializable {
	private static final long serialVersionUID = 1033801078240655539L;
	private List<Contact> modified; 
	private long syncTime;
		
	public SyncRequest() {
	}

	public SyncRequest(List<Contact> modified,
			long syncTime) {
		this.modified = modified;
		this.syncTime = syncTime;
	}


	public List<Contact> getModified() {
		return modified;
	}


	public void setModified(List<Contact> modified) {
		this.modified = modified;
	}




	public long getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(long syncTime) {
		this.syncTime = syncTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((modified == null) ? 0 : modified.hashCode());
		result = prime * result + (int) (syncTime ^ (syncTime >>> 32));
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
		SyncRequest other = (SyncRequest) obj;
		if (modified == null) {
			if (other.modified != null)
				return false;
		} else if (!modified.equals(other.modified))
			return false;
		if (syncTime != other.syncTime)
			return false;
		return true;
	}

}
