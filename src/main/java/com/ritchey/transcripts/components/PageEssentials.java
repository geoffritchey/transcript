package com.ritchey.transcripts.components;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class PageEssentials {
	PDPage page = null;
	PDPageContentStream contentStream = null;
	
	public PageEssentials(PDDocument document) throws IOException {
		page = new PDPage(PDRectangle.LETTER);
		contentStream = new PDPageContentStream(document, page);
	}
	

	public PDPage getPage() {
		return page;
	}

	public PDPageContentStream getContentStream() {
		return contentStream;
	}
	
}
