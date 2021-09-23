package com.ritchey.transcripts.components;

import java.math.BigDecimal;

public class Totals {
	String attempt = "0.00";
	String earned ="0.00";
	String total = "0.00";
	String credit = "0.00";
	String quality = "0.00";
	String gpa = "0.00";
	
	public Totals(String attempt, String earned, String total, String credit, String quality, String gpa) {
		this.attempt = attempt;
		this.earned = earned;
		this.total = total;
		this.credit = credit;
		this.quality = quality;
		this.gpa = gpa;
	}
	
	public Totals(BigDecimal attempt, BigDecimal earned, BigDecimal total, BigDecimal credit, BigDecimal quality, BigDecimal gpa) {
		this.attempt = String.format("%.2f", (attempt == null)?0d:attempt.doubleValue());
		this.earned = String.format("%.2f", (earned == null)?0d:earned.doubleValue());
		this.total = String.format("%.2f", (total == null)?0d:total.doubleValue());
		this.credit = String.format("%.2f", (credit == null)?0d:credit.doubleValue());
		this.quality = String.format("%.2f", (quality == null)?0d:quality.doubleValue());
		this.gpa = String.format("%.2f", (gpa == null)?0d:gpa.doubleValue());
	}
}
