package com.ritchey.transcripts.components;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.BorderStyle;
import org.vandeseer.easytable.settings.BorderStyleInterface;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.Table.TableBuilder;
import org.vandeseer.easytable.structure.cell.TextCell;
import org.vandeseer.easytable.structure.cell.paragraph.ParagraphCell;
import org.vandeseer.easytable.structure.cell.paragraph.StyledText;

import com.ritchey.transcripts.mapper.powercampus.TranscriptsMapper;

@Component
public class GenerateTranscript implements CommandLineRunner {
	private static Logger LOG = LoggerFactory.getLogger(GenerateTranscript.class);

	@Autowired
	TranscriptsMapper mapper;

	private static final PDFont DEFAULT_FONT = PDType1Font.HELVETICA;
	private static final int DEFAULT_FONT_SIZE = 12;
	private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
	private static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
	private static final BorderStyleInterface DEFAULT_BORDER_STYLE = BorderStyle.SOLID;
	private static final float DEFAULT_PADDING = 4f;

	private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.LEFT;
	private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.MIDDLE;

	@Override
	public void run(String... args) throws Exception {

		LOG.info("EXECUTING : command line runner");
		String outputFileName = "Simple.pdf";
		if (args.length > 0)
			outputFileName = args[0];

		String campusId = "P000321735";

		try (PDDocument document = new PDDocument()) {
			final PDPage page = new PDPage(PDRectangle.LETTER);
			document.addPage(page);
			PDPageContentStream contentStream = new PDPageContentStream(document, page);

			List<Map> details = mapper.selectDetails(campusId, "001", "Y");

			String governmentId = (String) details.get(0).get("GOVERNMENT_ID");
			governmentId = governmentId.replaceFirst("(...)(..)(....)", "$1-$2-$3");
			String fullname = (String) details.get(0).get("TRANSCRIPTHEADER_fullName");

			printDetails(document, page, contentStream, campusId, governmentId, fullname, "001", details);

			contentStream.close();
			document.save(outputFileName);
			document.close();
		}

	}

	public void printDetails(PDDocument document, PDPage page, PDPageContentStream contentStream, String campusId,
			String governmentId, String fullname, String sequence, List<Map> details) {

		boolean endOfPage = false;
		TableBuilder gradesBuilder = null;
		Integer column = 1;

		int size = details.size();

		boolean pageHasHeader = false;
		for (int i = 0; i < details.size(); i++) {
			Map detail = details.get(i);
			String nextYearTerm = null;
			if (i + 1 < details.size()) {
				Map nextDetail = details.get(i + 1);
				nextYearTerm = (String) nextDetail.get("ACADEMIC_TERM") + " "
						+ (String) nextDetail.get("ACADEMIC_YEAR");
			} else {
				endOfPage = true;
			}
			String lastYearTerm = null;
			if (i > 0) {
				Map lastDetail = details.get(i - 1);
				lastYearTerm = (String) lastDetail.get("ACADEMIC_TERM") + " "
						+ (String) lastDetail.get("ACADEMIC_YEAR");
			}

			String academicYear = (String) detail.get("ACADEMIC_YEAR");
			String academicTerm = (String) detail.get("ACADEMIC_TERM");
			String academicTermYear = academicTerm + " " + academicYear;

			boolean printSummary = (nextYearTerm == null) || !academicTermYear.equals(nextYearTerm);
			boolean printNewTerm = (i == 0) || !academicTermYear.equals(lastYearTerm);

			String event = (String) detail.get("EVENT_ID");
			String eventName = (String) detail.get("EVENT_MED_NAME");
			String eventSubType = (String) detail.get("EVENT_SUB_TYPE");
			String session = (String) detail.get("ACADEMIC_SESSION");

			String section = (String) detail.get("SECTION");

			String grade = (String) detail.get("FINAL_GRADE");
			String qpoints = String.format("%.2f", ((BigDecimal) detail.get("FINAL_QUALITY_PNTS")).doubleValue());
			String credit = String.format("%.2f", ((BigDecimal) detail.get("CREDIT_GRADE")).doubleValue());

			if (!pageHasHeader) {
				// createNewPage
				pageHasHeader = true;
				printHeader(document, page, contentStream, campusId, "001", governmentId, fullname);
				footer(document, page, contentStream);
				columnOutline(document, page, contentStream);

				gradesBuilder = Table.builder().addColumnsOfWidth(35).addColumnsOfWidth(38).addColumnOfWidth(50)
						.addColumnOfWidth(40).addColumnOfWidth(40).addColumnOfWidth(36).addColumnOfWidth(36)
						.addRow(Row.builder()
								.add(TextCell.builder().text("Course Id").colSpan(2).borderWidthBottom(1)
										.borderWidthTop(1).borderWidthLeft(1).fontSize(8).build())
								.add(TextCell.builder().text("Title").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
										.fontSize(8).build())
								.add(TextCell.builder().text("Grade").borderWidthBottom(1).borderWidthTop(1).fontSize(8)
										.build())
								.add(TextCell.builder().text("Credits").borderWidthBottom(1).borderWidthTop(1)
										.fontSize(8).build())
								.add(TextCell.builder().text("QPnts").borderWidthBottom(1).borderWidthTop(1)
										.borderWidthRight(1).fontSize(8).build())
								.build());

				gradesBuilder
						.addRow(Row
								.builder().add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1)
										.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build())
								.build());
			}

			if (printNewTerm) {
				gradesBuilder.addRow(Row.builder()
						.add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1).colSpan(7)
								.text(academicTermYear).horizontalAlignment(HorizontalAlignment.CENTER).build())
						.build());

				if ("TRANSFER".equals(academicTerm)) {
					Map school = mapper.selectOtherSchools(campusId, academicYear, academicTerm, session, event,
							eventSubType, section, sequence, "O000000001");
					String otherSchool = (String) school.get("ORG_NAME_1");

					gradesBuilder.addRow(Row
							.builder().add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1)
									.colSpan(7).text(otherSchool).horizontalAlignment(HorizontalAlignment.LEFT).build())
							.build());
				}
			}

			Totals term = new Totals();
			Totals cum = new Totals();

			gradesBuilder.addRow(
					Row.builder().add(TextCell.builder().colSpan(2).text(event).borderWidthLeft(1).fontSize(8).build())
							.add(TextCell.builder().colSpan(2).text(eventName).fontSize(8).build())
							.add(TextCell.builder().text(grade).fontSize(8).build())
							.add(TextCell.builder().text(credit).fontSize(8).build())
							.add(TextCell.builder().text(qpoints).borderWidthRight(1).fontSize(8).build()).build());

			if (printSummary) {

				printSummary(gradesBuilder, term, cum);

			}

			if (endOfPage) {

				Table grades = gradesBuilder.build();

				
				float startX =  (column ==1)?20f:300f;
				
				// Set up the drawer
				TableDrawer tableDrawer = TableDrawer.builder().contentStream(contentStream).page(page).startX(startX)
						.startY(page.getMediaBox().getUpperRightY() - 175f).table(grades).build();

				// And go for it!
				tableDrawer.draw();
			}

		}

	}

	public void printSummary(TableBuilder gradesBuilder, Totals term, Totals cum) {
		boolean endTranscript = false;

		gradesBuilder.addRow(Row.builder().add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1)
				.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build()).build());

		gradesBuilder.addRow(Row.builder().add(TextCell.builder().text("").borderWidthLeft(1).fontSize(8).build())
				.add(TextCell.builder().text("Attempt").horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text("Earned").horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text("Total").horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text("GPACrd").horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text("QPnts").horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text("GPA").horizontalAlignment(HorizontalAlignment.RIGHT).borderWidthRight(1)
						.fontSize(8).build())
				.build());
		gradesBuilder.addRow(Row.builder()
				.add(TextCell.builder().text("Term").horizontalAlignment(HorizontalAlignment.LEFT).borderWidthLeft(1)
						.fontSize(8).build())
				.add(TextCell.builder().text(term.attempt).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(term.earned).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(term.total).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(term.credit).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(term.quality).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(term.gpa).horizontalAlignment(HorizontalAlignment.RIGHT)
						.borderWidthRight(1).fontSize(8).build())
				.build());
		gradesBuilder.addRow(Row.builder().add(TextCell.builder().text("Cum").borderWidthLeft(1).fontSize(8).build())
				.add(TextCell.builder().text(cum.attempt).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(cum.earned).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(cum.total).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(cum.credit).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(cum.quality).horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(8)
						.build())
				.add(TextCell.builder().text(cum.gpa).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidthRight(1)
						.fontSize(8).build())
				.build());
		gradesBuilder.addRow(Row.builder().add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1)
				.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build()).build());
		if (endTranscript) {
			gradesBuilder.addRow(Row.builder()
					.add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1).colSpan(7)
							.text("End of Transcript").horizontalAlignment(HorizontalAlignment.CENTER).build())
					.build());
		}
	}

	public void columnOutline(PDDocument document, PDPage page, PDPageContentStream contentStream) {
		TableBuilder gradesBuilder = Table.builder().addColumnsOfWidth(35).addColumnsOfWidth(38).addColumnOfWidth(50)
				.addColumnOfWidth(40).addColumnOfWidth(40).addColumnOfWidth(36).addColumnOfWidth(36)
				.addRow(Row.builder()
						.add(TextCell.builder().text("Course Id").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
								.borderWidthLeft(1).fontSize(8).build())
						.add(TextCell.builder().text("Title").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
								.fontSize(8).build())
						.add(TextCell.builder().text("Grade").borderWidthBottom(1).borderWidthTop(1).fontSize(8)
								.build())
						.add(TextCell.builder().text("Credits").borderWidthBottom(1).borderWidthTop(1).fontSize(8)
								.build())
						.add(TextCell.builder().text("QPnts").borderWidthBottom(1).borderWidthTop(1).borderWidthRight(1)
								.fontSize(8).build())
						.build());

		gradesBuilder.addRow(Row.builder().add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1)
				.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build()).build());

		for (int i = 0; i < 39; i++) {
			gradesBuilder
					.addRow(Row
							.builder().add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1)
									.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build())
							.build());
		}
		gradesBuilder.addRow(Row.builder()
				.add(TextCell.builder().fontSize(8).borderWidthLeft(1).borderWidthRight(1).borderWidthBottom(1)
						.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build())
				.build());
		
		// Set up the drawer
		TableDrawer tableDrawer = TableDrawer.builder().contentStream(contentStream).page(page).startX(20f)
				.startY(page.getMediaBox().getUpperRightY() - 175f).table(gradesBuilder.build()).build();

		// And go for it!
		tableDrawer.draw();

		// Set up the drawer
		TableDrawer tableDrawer2 = TableDrawer.builder().contentStream(contentStream).page(page).startX(300f)
				.startY(page.getMediaBox().getUpperRightY() - 175f).table(gradesBuilder.build()).build();

		// And go for it!
		tableDrawer2.draw();
	}

	public void printHeader(PDDocument document, PDPage page, PDPageContentStream contentStream, String campusId,
			String sequence, String governmentId, String fullname) {
		Map programDegree = mapper.selectProgramDegree(campusId, sequence);
		String program = (String) programDegree.get("program");
		String degree = (String) programDegree.get("degree");
		String title = (String) programDegree.get("title");

		String degreeTitle = "";
		String degreeOrganization = "";
		Map previousInstitution = mapper.selectDegree(campusId, sequence);
		if (previousInstitution != null) {
			degreeTitle = (String) previousInstitution.get("SHORT_DESC");
			degreeTitle = (degreeTitle == null) ? "" : degreeTitle;
			degreeOrganization = (String) previousInstitution.get("ORG_NAME_1");
			degreeOrganization = (degreeOrganization == null) ? "" : degreeOrganization;
		}

		Double cumulativeGpa = mapper.cumulativeGPA(campusId, sequence);

		String honors = mapper.selectHonors(campusId, sequence);

		Map graduation = mapper.selectGraduationDate(campusId, sequence);
		Date graduationDate = (graduation == null) ? null : (Date) graduation.get("GRADUATION_DATE");
		String graduationDegree = (graduation == null) ? null : (String) graduation.get("SHORT_DESC");

		SimpleDateFormat df = new SimpleDateFormat("MMMMM dd, yyyy");

		String todaysDate = df.format(new Date());
		// Build the table

		Table myTable = Table.builder()

				.addColumnsOfWidth(140, 300, 140).addRow(Row
						.builder().add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()

								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
										.text("Date Printed:  ").build())
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN).text(todaysDate)
										.build())
								.build()).horizontalAlignment(HorizontalAlignment.LEFT)
								.verticalAlignment(VerticalAlignment.TOP).borderWidth(0).backgroundColor(Color.WHITE)
								.build())
						.add(TextCell.builder().text("Lubbock Christian University").font(PDType1Font.TIMES_BOLD)
								.fontSize(16).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(0).build())
						.add(TextCell.builder().text("Page 1 of 1").horizontalAlignment(HorizontalAlignment.RIGHT)
								.verticalAlignment(VerticalAlignment.TOP).font(PDType1Font.TIMES_BOLD).fontSize(8)
								.borderWidth(0).build())
						.build())
				.addRow(Row.builder().add(TextCell.builder().text("").build())
						.add(TextCell.builder().text("Office of the Registrar")
								.horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN)
								.fontSize(10).build())
						.add(TextCell.builder().text("").build()).build())
				.addRow(Row.builder().add(TextCell.builder().text("").build())
						.add(TextCell.builder().text("5601 19th St").horizontalAlignment(HorizontalAlignment.CENTER)
								.font(PDType1Font.TIMES_ROMAN).fontSize(10).build())
						.add(TextCell.builder().text("").build()).build())
				.addRow(Row.builder().add(TextCell.builder().text("").build())
						.add(TextCell.builder().text("Lubbock, TX 79407-2099")
								.horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN)
								.fontSize(10).build())
						.add(TextCell.builder().text("").build()).build())
				.build();

		Table personalDetails = Table.builder().addColumnsOfWidth(340, 300)
				.addRow(Row.builder().add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
						.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD).text("Name:  ").build())
						.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN).text("" + fullname)
								.build())
						.build()).build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
												.text("Id:  ").build())
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN)
												.text("" + governmentId).build())
										.build())
								.build())
						.build())
				.addRow(Row.builder().add(ParagraphCell.builder()
						.paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
										.text("Program/Degree/Curriculum:  ").build())
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN)
										.text("" + program + "/" + degree + "/" + title).build())
								.build())
						.build())
						.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
										.text("Degree/Date Granted:  ").build())
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN)
										.text((graduationDegree == null) ? ""
												: graduationDegree
														+ (graduationDate == null ? "" : df.format(graduationDate)))
										.build())
								.build()).build())
						.build())
				.addRow(Row.builder().add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
						.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD).text(" ").build())
						.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN).text("").build())
						.build()).build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD).text(" ")
												.build())
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN).text("")
												.build())
										.build())
								.build())
						.build())
				.addRow(Row.builder().add(ParagraphCell.builder()
						.paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
										.text("Previous Institution:  ").build())
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN)
										.text(degreeOrganization + ", " + degreeTitle).build())
								.build())
						.build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
												.text("Honors:  ").build())
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN)
												.text(honors == null ? "" : honors).build())
										.build())
								.build())
						.build())
				.addRow(Row.builder()
						.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD).text("").build())
								.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN).text("")
										.build())
								.build()).build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_BOLD)
												.text("Cumulative GPA:  ").build())
										.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN)
												.text(String.format("%3.2f", cumulativeGpa)).build())
										.build())
								.build())
						.build())
				.build();

		// Set up the drawer
		TableDrawer tableDrawer = TableDrawer.builder().contentStream(contentStream).page(page).startX(20f)
				.startY(page.getMediaBox().getUpperRightY() - 20f).table(myTable).build();

		// And go for it!
		tableDrawer.draw();

		TableDrawer tableDrawer2 = TableDrawer.builder().contentStream(contentStream).page(page).startX(20f)
				.startY(page.getMediaBox().getUpperRightY() - 90f).table(personalDetails).build();

		// And go for it!
		tableDrawer2.draw();

	}

	public void footer(PDDocument document, PDPage page, PDPageContentStream contentStream) {
		Table myTable = Table.builder()

				.addColumnsOfWidth(580)
				.addRow(Row.builder().add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()

						.append(StyledText.builder().fontSize(8f).font(PDType1Font.TIMES_ROMAN).text(
								"IN ACCORDANCE WITH THE FAMILY EDUCATIONAL RIGHTS AND PRIVACY ACT OF 1974, AS AMENDED, TRANSCRIPTS MAY NOT BE RELEASED TO A THIRD PARTY WITHOUT THE WRITTEN CONSENT OF THE STUDENT.")
								.build())
						.build()).horizontalAlignment(HorizontalAlignment.CENTER)
						.verticalAlignment(VerticalAlignment.TOP).borderWidth(0).backgroundColor(Color.WHITE).build())
						.build())
				.build();

		TableDrawer tableDrawer2 = TableDrawer.builder().contentStream(contentStream).page(page).startX(20f)
				.startY(page.getMediaBox().getUpperRightY() - 760f).table(myTable).build();

		// And go for it!
		tableDrawer2.draw();
	}

}
