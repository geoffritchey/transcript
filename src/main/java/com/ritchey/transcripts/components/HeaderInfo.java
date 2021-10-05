package com.ritchey.transcripts.components;

import java.util.Map;

public class HeaderInfo {
	String campusId;
	String sequence;
	String governmentId;
	String fullname;
	String orgName;
	String orgAddress;
	Map orgCity;
	
	public HeaderInfo(String campusId, String sequence, String governmentId, String fullname, 
			String orgName, String orgAddress, Map orgCity) {
		super();
		this.campusId = campusId;
		this.sequence = sequence;
		this.governmentId = governmentId;
		this.fullname = fullname;
		this.orgName = orgName;
		this.orgAddress = orgAddress;
		this.orgCity = orgCity;
	}


	public String getCampusId() {
		return campusId;
	}


	public String getSequence() {
		return sequence;
	}


	public String getGovernmentId() {
		return governmentId;
	}


	public String getFullname() {
		return fullname;
	}

	public String getOrgName() {
		return orgName;
	}


	public String getOrgAddress() {
		return orgAddress;
	}


	public Map getOrgCity() {
		return orgCity;
	}
	
	
}
