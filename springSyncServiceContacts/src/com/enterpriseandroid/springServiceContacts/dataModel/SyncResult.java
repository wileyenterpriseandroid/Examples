package com.enterpriseandroid.springServiceContacts.dataModel;

import java.io.Serializable;
import java.util.List;

public class SyncResult implements Serializable {
	private static final long serialVersionUID = -4202285332791622654L;
	private List<Contact> modified;
	private List<Contact> conflicts;
	private long syncTime;
	
	public SyncResult() {
		
	}

	public List<Contact> getModified() {
		return modified;
	}

	public SyncResult(List<Contact> modified, List<Contact> conflicts,
			long syncTime) {
		super();
		this.modified = modified;
		this.conflicts = conflicts;
		this.syncTime = syncTime;
	}

	public void setModified(List<Contact> modified) {
		this.modified = modified;
	}

	public List<Contact> getConflicts() {
		return conflicts;
	}

	public void setConflicts(List<Contact> conflicts) {
		this.conflicts = conflicts;
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
				+ ((conflicts == null) ? 0 : conflicts.hashCode());
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
		SyncResult other = (SyncResult) obj;
		if (conflicts == null) {
			if (other.conflicts != null)
				return false;
		} else if (!conflicts.equals(other.conflicts))
			return false;
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
