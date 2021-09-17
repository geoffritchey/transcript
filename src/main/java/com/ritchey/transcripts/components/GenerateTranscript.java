package com.ritchey.transcripts.components;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.swing.text.TableView.TableCell;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.BorderStyle;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;
import org.vandeseer.easytable.structure.cell.paragraph.ParagraphCell;
import org.vandeseer.easytable.structure.cell.paragraph.StyledText;

import com.ritchey.transcripts.mapper.powercampus.TranscriptsMapper;

@Component
public class GenerateTranscript implements CommandLineRunner {
	private static Logger LOG = LoggerFactory.getLogger(GenerateTranscript.class);

	@Autowired
	TranscriptsMapper mapper;

	@Override
	public void run(String... args) throws Exception {

		Map programDegree = mapper.selectProgramDegree("P000321735", "001");
		String program = (String) programDegree.get("program");
		String degree = (String) programDegree.get("degree");
		String title = (String) programDegree.get("title");
		
		Map details = mapper.selectDetails("P000321735", "001", "Y");
		LOG.debug("details = " + details);
		String governmentId = (String) details.get("GOVERNMENT_ID");
		governmentId = governmentId.replaceFirst("(...)(..)(....)",  "$1-$2-$3");
		String fullname = (String) details.get("TRANSCRIPTHEADER_fullName");
		
		
		Double cumulativeGpa = mapper.cumulativeGPA("P000321735", "001");
		
		String honors = mapper.selectHonors("P000321735", "001");
		
		Map graduation = mapper.selectGraduationDate("P000321735", "001");
		Date graduationDate = (graduation==null)?null:(Date) graduation.get("GRADUATION_DATE");
		String graduationDegree = (graduation==null)?null:(String) graduation.get("SHORT_DESC");

		LOG.info("EXECUTING : command line runner");
		String outputFileName = "Simple.pdf";
		if (args.length > 0)
			outputFileName = args[0];

		SimpleDateFormat df = new SimpleDateFormat("MMMMM dd, yyyy");
		try (PDDocument document = new PDDocument()) {
			final PDPage page = new PDPage(PDRectangle.LETTER);
			document.addPage(page);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

				String todaysDate = df.format(new Date());
				// Build the table

				Table myTable = Table.builder()
						
						.addColumnsOfWidth(140, 300, 140)
						.addRow(Row.builder()
						.add(ParagraphCell.builder()
                                .paragraph(ParagraphCell.Paragraph.builder()
             
                                        .append(StyledText.builder()
                                        		.fontSize(8f)
                    							.font(PDType1Font.TIMES_BOLD)
                                        		.text("Date Printed:  ")
                                        		.build())
                                        .append(StyledText.builder()
                                        		.fontSize(8f)
                    							.font(PDType1Font.TIMES_ROMAN)
                                        		.text(todaysDate)
                                        .build())
                                .build())
							.horizontalAlignment(HorizontalAlignment.LEFT)
							.verticalAlignment(VerticalAlignment.TOP)
							.borderWidth(0)
							.backgroundColor(Color.WHITE)
							.build())
						.add(TextCell.builder()
								.text("Lubbock Christian University")
								.font(PDType1Font.TIMES_BOLD)
								.fontSize(16)
								.horizontalAlignment(HorizontalAlignment.CENTER)
								.borderWidth(0)
							.build())
						.add(TextCell.builder().text("Page 1 of 1")
								.horizontalAlignment(HorizontalAlignment.RIGHT)
								.verticalAlignment(VerticalAlignment.TOP)
								.font(PDType1Font.TIMES_BOLD)
								.fontSize(8)
								.borderWidth(0)
							.build())
						.build())
						.addRow(Row.builder().add(TextCell.builder().text("").build()).add(TextCell.builder().text("Office of the Registrar").horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN).fontSize(10).build()).add(TextCell.builder().text("").build()).build())
						.addRow(Row.builder().add(TextCell.builder().text("").build()).add(TextCell.builder().text("5601 19th St").horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN).fontSize(10).build()).add(TextCell.builder().text("").build()).build())
						.addRow(Row.builder().add(TextCell.builder().text("").build()).add(TextCell.builder().text("Lubbock, TX 79407-2099").horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN).fontSize(10).build()).add(TextCell.builder().text("").build()).build())
						.build();

					Table personalDetails = Table.builder()
						.addColumnsOfWidth(340, 300)
						.addRow(Row.builder()
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Name:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("" + fullname)
		                                		.build()).build()).build())
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Id:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("" + governmentId)
		                                		.build()).build()).build())
								.build()
								)
						.addRow(Row.builder()
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Program/Degree/Curriculum:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("" + program + "/" + degree + "/" + title)
		                                		.build()).build()).build())
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Degree/Date Granted:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text(graduationDegree==null?"":graduationDegree + (graduationDate == null?"":df.format(graduationDate)))
		                                		.build()).build()).build())
								.build()
								)
						.addRow(Row.builder()
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text(" ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("")
		                                		.build()).build()).build())
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text(" ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("")
		                                		.build()).build()).build())
								.build()
								)
						.addRow(Row.builder()
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Previous Institution:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("????")
		                                		.build()).build()).build())
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Honors:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text(honors==null?"":honors)
		                                		.build()).build()).build())
								.build()
								)
						.addRow(Row.builder()
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text("")
		                                		.build()).build()).build())
								.add(ParagraphCell.builder()
		                                .paragraph(ParagraphCell.Paragraph.builder()
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_BOLD)
		                                        		.text("Cumulative GPA:  ")
		                                        		.build())
		                                        .append(StyledText.builder()
		                                        		.fontSize(8f)
		                    							.font(PDType1Font.TIMES_ROMAN)
		                                        		.text(String.format("%3.2f", cumulativeGpa))
		                                		.build()).build()).build())
								.build()
								)
						.build();
					
					
					Table grades = Table.builder()
							.addColumnsOfWidth(300, 300)
							.build();
					
					
				// Set up the drawer
				TableDrawer tableDrawer = TableDrawer.builder()
						.contentStream(contentStream)
						.page(page)
						.startX(20f)
						.startY(page.getMediaBox().getUpperRightY() - 20f)
						.table(myTable)
						.build();

				// And go for it!
				tableDrawer.draw();
				
				TableDrawer tableDrawer2 = TableDrawer.builder()
						.contentStream(contentStream)
						.page(page)
						.startX(20f)
						.startY(page.getMediaBox().getUpperRightY() - 90f)
						.table(personalDetails)
						.build();

				// And go for it!
				tableDrawer2.draw();
			}

			// Save the results and ensure that the document is properly closed:
			document.save(outputFileName);
			document.close();
		}
	}
}
