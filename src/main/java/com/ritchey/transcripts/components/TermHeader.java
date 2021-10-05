package com.ritchey.transcripts.components;

public class TermHeader {
	String yearTerm;
	String otherSchool;
	
	public TermHeader() {
		
	}
	
	public TermHeader(String yearTerm) {
		this.yearTerm = yearTerm;
	}
	
	public String getYearTerm() {
		return yearTerm;
	}
	public TermHeader setYearTerm(String yearTerm) {
		this.yearTerm = yearTerm;
		return this;
	}
	public String getOtherSchool() {
		return otherSchool;
	}
	public TermHeader setOtherSchool(String otherSchool) {
		this.otherSchool = otherSchool;
		return this;
	}
	
}
