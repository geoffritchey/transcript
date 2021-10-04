package com.ritchey.transcripts.components;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
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

	List<PageEssentials> contentStreams = new ArrayList<PageEssentials>();
	PageEssentials pageEssentials = null;

	private static final PDFont DEFAULT_FONT = PDType1Font.HELVETICA;
	private static final int DEFAULT_FONT_SIZE = 12;
	private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
	private static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
	private static final BorderStyleInterface DEFAULT_BORDER_STYLE = BorderStyle.SOLID;
	private static final float DEFAULT_PADDING = 4f;

	private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.LEFT;
	private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.MIDDLE;

	private static final int FONT_SIZE = 7;
	private static final float HEADER_FONT_SIZE = 8;
	private static final float BODY_ROW_HEIGHT = 10f;

	private static final float OFFSET = 15f;
	
	static final int END_OF_COLUMN_INDEX = 0;
	static final int COLUMN_INDEX = 1;
	static final int GRADES_BUILDER_INDEX = 2;
	static final int PAGE_NUMBER_INDEX = 3;
	static final int PAGE_INDEX = 4;
	static final int PAGE_ESSENTIALS_INDEX = 5;
	static final int STREAM_INDEX = 6;

	SimpleDateFormat df = new SimpleDateFormat("MMMMM dd, yyyy");

	@Override
	public void run(String... args) throws Exception {

		LOG.info("EXECUTING : command line runner");
		String orgCode = "O000000001";

		// String campusId = "P000100013"; // jodi
		// String campusId = "P000001032";// Sarah Womble
		// String campusId = "P000124143";// Daniel Trent
		//String campusId = "P001161065";// Laurie Needham
		
		boolean all = false;
		List<String> campusIds = null;
		if (args.length > 0) {
			
			campusIds = new ArrayList<String>();
			for (String a: args) {
				LOG.debug("ARGS0: " + a.toString());
				if ("--all".equalsIgnoreCase(a)) {
					all = true;
				}
				else {
					campusIds.add(a);
				}
			}
		}
		if (all || campusIds == null) {
			campusIds = mapper.selectStudents();
			if (!all) {
				campusIds = Arrays.asList(campusIds.get(0));
			}
		}

		

		int count = 0;
		for (String campusId : campusIds) {
			LOG.debug("campus id = " + campusId + "   " + count);
			count++;
			LOG.debug(campusId);
			List<String> sequences = mapper.selectTranscriptSequences(campusId);
			for (String sequence : sequences) {

				LOG.debug("sequence = " + sequence);
				String outputFileName = String.format("%s-%s.pdf", campusId, sequence);

				List<Map> details = mapper.selectDetails(campusId, sequence, "Y");
				if (details.size() <1) {
					continue;
				}
				
				try (PDDocument document = new PDDocument()) {
					contentStreams = new ArrayList<PageEssentials>();

					String orgName = mapper.selectOrgName(orgCode);
					String orgAddress = mapper.selectOrgStreetAddress(orgCode);
					Map orgCityStateZip = mapper.selectOrgCityStateZip(orgCode);
					


					pageEssentials = new PageEssentials(document);
					PDPage page = pageEssentials.getPage();
					document.addPage(page);
					PDPageContentStream contentStream = pageEssentials.getContentStream();

					String governmentId = (String) details.get(0).get("GOVERNMENT_ID");
					governmentId = governmentId==null?"":governmentId.replaceFirst("(...)(..)(....)", "$1-$2-$3");
					String fullname = (String) details.get(0).get("TRANSCRIPTHEADER_fullName");
					
					HeaderInfo header = new HeaderInfo(campusId, sequence, governmentId, fullname, orgName, orgAddress, orgCityStateZip);

					List<Map> testScores = mapper.selectTestScores(campusId);

					printDetails(document, page, contentStream, header, details, testScores, orgCode);

					int totalPages = contentStreams.size();
					int pageNumber = 0;
					for (PageEssentials cs : contentStreams) {
						pageNumber++;
						Table myTable = Table.builder()

								.addColumnsOfWidth(140, 300, 140)
								.addRow(Row.builder().add(TextCell.builder().text("").build())
										.add(TextCell.builder().text("").borderWidth(0).build())
										.add(TextCell.builder().text("Page " + pageNumber + " of " + totalPages)
												.horizontalAlignment(HorizontalAlignment.RIGHT)
												.verticalAlignment(VerticalAlignment.TOP).font(PDType1Font.TIMES_BOLD)
												.fontSize(FONT_SIZE).borderWidth(0).build())
										.build())
								.build();

						TableDrawer tableDrawer = TableDrawer.builder().contentStream(cs.getContentStream())
								.page(cs.getPage()).startX(20f).startY(page.getMediaBox().getUpperRightY() - 20f)
								.table(myTable).build();

						tableDrawer.draw();

						Table myTable2 = Table.builder()

								.addColumnsOfWidth(290, 290)
								.addRow(Row.builder().add(ParagraphCell.builder()
										.paragraph(ParagraphCell.Paragraph.builder()
												.append(StyledText.builder().fontSize(HEADER_FONT_SIZE)
														.font(PDType1Font.TIMES_ROMAN)
														.text(pageNumber == totalPages ? "End of Transcript"
																: "*** CONTINUED ON NEXT PAGE ***")
														.build())
												.build())
										.horizontalAlignment(HorizontalAlignment.CENTER).colSpan(2)
										.verticalAlignment(VerticalAlignment.TOP).borderWidth(0)
										.backgroundColor(Color.WHITE).build()).build())
								.build();

						TableDrawer tableDrawer2 = TableDrawer.builder().contentStream(cs.getContentStream())
								.page(cs.getPage()).startX(20f).startY(page.getMediaBox().getUpperRightY() - 710f)
								.table(myTable2).build();

						tableDrawer2.draw();

						cs.getContentStream().close();
					}

					document.save(outputFileName);

					document.close();
				}
			}
		}

	}

	public void printDetails(PDDocument document, PDPage page, PDPageContentStream contentStream, HeaderInfo header,
			List<Map> details, List<Map> testScores,
			String orgCode) throws IOException {

		boolean endOfData = false;
		boolean endOfColumn = false;
		TableBuilder gradesBuilder = null;
		Integer column = 0;
		int pageNumber = 1;

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
				endOfData = true;
			}
			String lastYearTerm = null;
			String lastSchool = "";
			if (i > 0) {
				Map lastDetail = details.get(i - 1);
				lastYearTerm = (String) lastDetail.get("ACADEMIC_TERM") + " "
						+ (String) lastDetail.get("ACADEMIC_YEAR");
				lastSchool = (String) lastDetail.get("ORG_CODE_ID");
			}

			String academicYear = (String) detail.get("ACADEMIC_YEAR");
			String academicTerm = (String) detail.get("ACADEMIC_TERM");
			String academicTermYear = academicTerm + " " + academicYear;
			String school = (String) detail.get("ORG_CODE_ID");
			


			List<Map> awards = mapper.selectAwards(header.campusId, header.sequence, "TERMSTND", "Y", academicYear, academicTerm);

			boolean printSummary = (nextYearTerm == null) || !academicTermYear.equals(nextYearTerm);
			boolean printNewTerm = (i == 0) || !academicTermYear.equals(lastYearTerm);
			boolean printSchoolChange = !school.equals(orgCode) && ((i == 0) ||  !school.equals(lastSchool) || printNewTerm);

			String event = (String) detail.get("EVENT_ID");
			String eventName = (String) detail.get("EVENT_MED_NAME");
			String eventSubType = (String) detail.get("EVENT_SUB_TYPE");
			String session = (String) detail.get("ACADEMIC_SESSION");

			String section = (String) detail.get("SECTION");

			String creditType = (String) detail.get("CREDIT_TYPE");

			String repeat = (String) detail.get("REPEATED");

			String grade = (String) detail.get("FINAL_GRADE");
			if ("Y".equals(repeat)) {
				grade = "[" + grade + "]";
			}
			String qpoints = String.format("%.1f", ((BigDecimal) detail.get("FINAL_QUALITY_PNTS")).doubleValue());
			String credit = String.format("%.2f", ((BigDecimal) detail.get("CREDIT_GRADE")).doubleValue());

			if (!pageHasHeader) {
				// createNewPage
				pageHasHeader = true;
				printHeader(document, page, contentStream, header);
				printFooter(document, page, contentStream, endOfData);
				columnOutline(document, page, contentStream);

				gradesBuilder = createTable();
			}

			if (printNewTerm) {
				gradesBuilder.addRow(Row.builder()
						.add(TextCell.builder().font(PDType1Font.TIMES_BOLD).fontSize(FONT_SIZE).borderWidthLeft(1)
								.borderWidthRight(1).colSpan(7).text(academicTermYear)
								.horizontalAlignment(HorizontalAlignment.CENTER).build())
						.build());
			}
			
			Object[] ret = checkEndOfColumn(page, document, contentStream 
					, gradesBuilder, endOfColumn, column
					, pageNumber,  endOfData, header);
			
			endOfColumn = (Boolean) ret[END_OF_COLUMN_INDEX];
			column = (Integer) ret[COLUMN_INDEX];
			gradesBuilder = (TableBuilder) ret[GRADES_BUILDER_INDEX];
			pageNumber = (Integer) ret[PAGE_NUMBER_INDEX];
			page = (PDPage) ret[PAGE_INDEX];
			pageEssentials = (PageEssentials) ret[PAGE_ESSENTIALS_INDEX];
			contentStream = (PDPageContentStream) ret[STREAM_INDEX];
			
			if (printSchoolChange) {
				Map schoolDetail = mapper.selectOtherSchools(header.campusId, academicYear, academicTerm, session, event,
						eventSubType, section, header.sequence, orgCode);
				String otherSchool = school==null?"":(String) schoolDetail.get("ORG_NAME_1");

				gradesBuilder.addRow(Row.builder()
						.add(TextCell.builder().font(PDType1Font.TIMES_BOLD).fontSize(FONT_SIZE).borderWidthLeft(1)
								.borderWidthRight(1).colSpan(7).text("   " + otherSchool)
								.horizontalAlignment(HorizontalAlignment.LEFT).build())
						.build());
			}
			
			ret = checkEndOfColumn(page, document, contentStream 
					, gradesBuilder, endOfColumn, column
					, pageNumber,  endOfData, header);
			
			endOfColumn = (Boolean) ret[END_OF_COLUMN_INDEX];
			column = (Integer) ret[COLUMN_INDEX];
			gradesBuilder = (TableBuilder) ret[GRADES_BUILDER_INDEX];
			pageNumber = (Integer) ret[PAGE_NUMBER_INDEX];
			page = (PDPage) ret[PAGE_INDEX];
			pageEssentials = (PageEssentials) ret[PAGE_ESSENTIALS_INDEX];
			contentStream = (PDPageContentStream) ret[STREAM_INDEX];
			

			// BigDecimal attempt, BigDecimal earned, BigDecimal total, BigDecimal credit,
			// BigDecimal quality, BigDecimal gpa)
			Totals term = new Totals((BigDecimal) detail.get("ATTEMPTED_CREDITS"),
					(BigDecimal) detail.get("EARNED_CREDITS"), (BigDecimal) detail.get("TOTAL_CREDITS"),
					(BigDecimal) detail.get("GPA_CREDITS"), (BigDecimal) detail.get("QUALITY_POINTS"),
					(BigDecimal) detail.get("GPA"));
			Totals cum = new Totals((BigDecimal) detail.get("CUM_ATTEMPTED_CREDITS"),
					(BigDecimal) detail.get("CUM_EARNED_CREDITS"), (BigDecimal) detail.get("CUM_TOTAL_CREDITS"),
					(BigDecimal) detail.get("CUM_GPA_CREDITS"), (BigDecimal) detail.get("CUM_QUALITY_POINTS"),
					(BigDecimal) detail.get("CUM_GPA"));

			gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
					.add(TextCell.builder().colSpan(1).text(event).borderWidthLeft(1).fontSize(FONT_SIZE).build())
					.add(TextCell.builder().colSpan(3).text(eventName).fontSize(FONT_SIZE).build())
					.add(TextCell.builder().text(grade).fontSize(FONT_SIZE).build())
					.add(TextCell.builder().text(credit).horizontalAlignment(HorizontalAlignment.RIGHT)
							.fontSize(FONT_SIZE).build())
					.add(TextCell.builder().text(qpoints).horizontalAlignment(HorizontalAlignment.RIGHT)
							.borderWidthRight(1).fontSize(FONT_SIZE).build())
					.build());


			ret = checkEndOfColumn(page, document, contentStream 
					, gradesBuilder, endOfColumn, column
					, pageNumber,  endOfData, header);
			
			endOfColumn = (Boolean) ret[END_OF_COLUMN_INDEX];
			column = (Integer) ret[COLUMN_INDEX];
			gradesBuilder = (TableBuilder) ret[GRADES_BUILDER_INDEX];
			pageNumber = (Integer) ret[PAGE_NUMBER_INDEX];
			page = (PDPage) ret[PAGE_INDEX];
			pageEssentials = (PageEssentials) ret[PAGE_ESSENTIALS_INDEX];
			contentStream = (PDPageContentStream) ret[STREAM_INDEX];
			

			if (printSummary) {
				printSummary(gradesBuilder, term, cum, awards, endOfData, testScores);

			}

			if (endOfData) {

				Table grades = gradesBuilder.build();

				float startX = (column == 0) ? 20f + OFFSET : 300f + OFFSET;

				// Set up the drawer
				TableDrawer tableDrawer = TableDrawer.builder().contentStream(contentStream).page(page).startX(startX)
						.startY(page.getMediaBox().getUpperRightY() - 175f).table(grades).build();

				// And go for it!
				tableDrawer.draw();
				contentStreams.add(pageEssentials);
				// contentStream.close();

			}

		}

	}
	
	public Object[] checkEndOfColumn(PDPage page, PDDocument document, PDPageContentStream contentStream 
			, TableBuilder gradesBuilder, boolean endOfColumn, int column
			, int pageNumber,  boolean endOfData, HeaderInfo header) throws IOException {
		try {
			float height = gradesBuilder.build().getHeight();
			if (height > 445f) {
				endOfColumn = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (endOfColumn) {
			float startX = (column == 0) ? 20f + OFFSET : 300f + OFFSET;
			Table grades = gradesBuilder.build();
			// Set up the drawer
			TableDrawer tableDrawer = TableDrawer.builder().contentStream(contentStream).page(page).startX(startX)
					.startY(page.getMediaBox().getUpperRightY() - 175f).table(grades).build();

			// And go for it!
			tableDrawer.draw();
			// gradesBuilder = Table.builder();

			if (column == 1) {
				// contentStream.close();
				contentStreams.add(pageEssentials);
				column = 0;
				pageEssentials = new PageEssentials(document);
				page = pageEssentials.getPage();
				pageNumber++;
				document.addPage(page);
				contentStream = pageEssentials.getContentStream();
				printHeader(document, page, contentStream, header);
				printFooter(document, page, contentStream, endOfData);
				columnOutline(document, page, contentStream);

			} else
				column = 1;

			endOfColumn = false;

			gradesBuilder = createTable();

		}
		return (Object[]) new Object[] {endOfColumn, column, gradesBuilder, pageNumber, page, pageEssentials, contentStream};
	}

	public TableBuilder createTable() {
		TableBuilder gradesBuilder = Table.builder().addColumnsOfWidth(55).addColumnsOfWidth(38).addColumnOfWidth(40)
				.addColumnOfWidth(35).addColumnOfWidth(35).addColumnOfWidth(36).addColumnOfWidth(36)
				.addRow(Row.builder()
						.add(TextCell.builder().text("").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
								.borderWidthLeft(1).fontSize(FONT_SIZE).build())
						.add(TextCell.builder().text("").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
								.fontSize(FONT_SIZE).build())
						.add(TextCell.builder().text("").borderWidthBottom(1).borderWidthTop(1).fontSize(FONT_SIZE)
								.build())
						.add(TextCell.builder().text("").borderWidthBottom(1).borderWidthTop(1).fontSize(FONT_SIZE)
								.build())
						.add(TextCell.builder().text("").borderWidthBottom(1).borderWidthTop(1).borderWidthRight(1)
								.fontSize(FONT_SIZE).build())
						.build());

		gradesBuilder
				.addRow(Row.builder().add(TextCell.builder().fontSize(FONT_SIZE).borderWidthLeft(1).borderWidthRight(1)
						.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build()).build());

		return gradesBuilder;
	}

	public void printSummary(TableBuilder gradesBuilder, Totals term, Totals cum, List<Map> awards,
			boolean endTranscript, List<Map> testScores) {

		gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
				.add(TextCell.builder().fontSize(FONT_SIZE).borderWidthLeft(1).borderWidthRight(1).colSpan(7).text("")
						.horizontalAlignment(HorizontalAlignment.CENTER).build())
				.build());

		for (Map award : awards) {
			String strAward = (String) award.get("MEDIUM_DESC");
			if (strAward != null) {
				gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT).add(TextCell.builder()
						.font(PDType1Font.TIMES_BOLD).colSpan(7).text(strAward).fontSize(FONT_SIZE).build()).build());
			}
		}

		gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
				.add(TextCell.builder().text("").borderWidthLeft(1).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text("Attempt").font(PDType1Font.TIMES_BOLD)
						.horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text("Earned").font(PDType1Font.TIMES_BOLD)
						.horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text("Total").font(PDType1Font.TIMES_BOLD)
						.horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text("GPACrd").font(PDType1Font.TIMES_BOLD)
						.horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text("QPnts").font(PDType1Font.TIMES_BOLD)
						.horizontalAlignment(HorizontalAlignment.RIGHT).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text("GPA").font(PDType1Font.TIMES_BOLD)
						.horizontalAlignment(HorizontalAlignment.RIGHT).borderWidthRight(1).fontSize(FONT_SIZE).build())
				.build());
		gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
				.add(TextCell.builder().text("Term").horizontalAlignment(HorizontalAlignment.LEFT).borderWidthLeft(1)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(term.attempt).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(term.earned).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(term.total).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(term.credit).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(term.quality).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(term.gpa).horizontalAlignment(HorizontalAlignment.RIGHT)
						.borderWidthRight(1).fontSize(FONT_SIZE).build())
				.build());
		gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
				.add(TextCell.builder().text("Cum").borderWidthLeft(1).fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(cum.attempt).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(cum.earned).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(cum.total).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(cum.credit).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(cum.quality).horizontalAlignment(HorizontalAlignment.RIGHT)
						.fontSize(FONT_SIZE).build())
				.add(TextCell.builder().text(cum.gpa).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidthRight(1)
						.fontSize(FONT_SIZE).build())
				.build());
		gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
				.add(TextCell.builder().fontSize(FONT_SIZE).borderWidthLeft(1).borderWidthRight(1).colSpan(7).text("")
						.horizontalAlignment(HorizontalAlignment.CENTER).build())
				.build());

		if (endTranscript) {
			if (testScores.size() > 0) {

				gradesBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
						.add(TextCell.builder().fontSize(FONT_SIZE).colSpan(7).text("Test Scores")
								.font(PDType1Font.TIMES_BOLD).horizontalAlignment(HorizontalAlignment.CENTER).build())
						.build());

				for (Map testScore : testScores) {

					String text = String.format("%s  %s  %s  Score: %.2f", (String) testScore.get("TestID"),
							(String) testScore.get("TestType"), df.format((Date) testScore.get("TEST_DATE")),
							((BigDecimal) testScore.get("RAW_SCORE")).doubleValue());

					gradesBuilder.addRow(
							Row.builder().height(BODY_ROW_HEIGHT).add(TextCell.builder().text("").colSpan(1).build())
									.add(TextCell.builder().fontSize(FONT_SIZE).colSpan(6).text(text)
											.horizontalAlignment(HorizontalAlignment.LEFT).build())
									.build());

				}

			}

			String text = "End of Transcript";
			text = "";// disable 'end of transcript' at bottom of column
			gradesBuilder
					.addRow(Row.builder().height(BODY_ROW_HEIGHT)
							.add(TextCell.builder().fontSize(FONT_SIZE).colSpan(7).text("")
									.horizontalAlignment(HorizontalAlignment.CENTER).build())
							.build())
					.addRow(Row.builder().height(BODY_ROW_HEIGHT).add(TextCell.builder().fontSize(FONT_SIZE).colSpan(7)
							.text(text).horizontalAlignment(HorizontalAlignment.CENTER).build()).build());

		}
	}

	public void columnOutline(PDDocument document, PDPage page, PDPageContentStream contentStream) {
		TableBuilder gradesBuilder = Table.builder().addColumnsOfWidth(35).addColumnsOfWidth(38).addColumnOfWidth(50)
				.addColumnOfWidth(40).addColumnOfWidth(40).addColumnOfWidth(36).addColumnOfWidth(36)
				.addRow(Row.builder()
						.add(TextCell.builder().text("Course Id").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
								.borderWidthLeft(1).fontSize(FONT_SIZE).build())
						.add(TextCell.builder().text("Title").colSpan(2).borderWidthBottom(1).borderWidthTop(1)
								.fontSize(FONT_SIZE).build())
						.add(TextCell.builder().text("Grade").borderWidthBottom(1).borderWidthTop(1).fontSize(FONT_SIZE)
								.build())
						.add(TextCell.builder().text("Credits").borderWidthBottom(1).borderWidthTop(1)
								.fontSize(FONT_SIZE).build())
						.add(TextCell.builder().text("QPnts").borderWidthBottom(1).borderWidthTop(1).borderWidthRight(1)
								.fontSize(FONT_SIZE).build())
						.build());

		gradesBuilder
				.addRow(Row.builder().add(TextCell.builder().fontSize(FONT_SIZE).borderWidthLeft(1).borderWidthRight(1)
						.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build()).build());

		for (int i = 0; i < 37; i++) {
			gradesBuilder
					.addRow(Row.builder()
							.add(TextCell.builder().fontSize(FONT_SIZE).borderWidthLeft(1).borderWidthRight(1)
									.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build())
							.build());
		}
		gradesBuilder.addRow(Row.builder()
				.add(TextCell.builder().fontSize(FONT_SIZE).borderWidthLeft(1).borderWidthRight(1).borderWidthBottom(1)
						.colSpan(7).text("").horizontalAlignment(HorizontalAlignment.CENTER).build())
				.build());

		// Set up the drawer
		TableDrawer tableDrawer = TableDrawer.builder().contentStream(contentStream).page(page).startX(20f + OFFSET)
				.startY(page.getMediaBox().getUpperRightY() - 175f).table(gradesBuilder.build()).build();

		// And go for it!
		tableDrawer.draw();

		// Set up the drawer
		TableDrawer tableDrawer2 = TableDrawer.builder().contentStream(contentStream).page(page).startX(300f + OFFSET)
				.startY(page.getMediaBox().getUpperRightY() - 175f).table(gradesBuilder.build()).build();

		// And go for it!
		tableDrawer2.draw();
	}

	public void printHeader(PDDocument document, PDPage page, PDPageContentStream contentStream, HeaderInfo header) {

		List<String> programs = new ArrayList<String>();
		for (Map programDegree : mapper.selectProgramDegree(header.campusId, header.sequence)) {
			String program = (String) programDegree.get("program");
			String degree = (String) programDegree.get("degree");
			String title = (String) programDegree.get("title");

			programs.add(program + "/" + degree + "/" + title);
		}

		List<Map> previousInstitutions = mapper.selectDegree(header.campusId, header.sequence);
		List<String> previous = new ArrayList<String>();
		for (Map previousInstitution : previousInstitutions) {
			previous.add((String) previousInstitution.get("ORG_NAME_1")
					+ ((String) previousInstitution.get("SHORT_DESC") == null ? ""
							: ", " + (String) previousInstitution.get("SHORT_DESC")));
		}
		if (previous.size() == 0) {
			previous.add("");
		}

		Double cumulativeGpa = mapper.cumulativeGPA(header.campusId, header.sequence);

		String honors = mapper.selectHonors(header.campusId, header.sequence);

		List<Map> graduations = mapper.selectGraduationDate(header.campusId, header.sequence);
		List<String> strGraduations = new ArrayList<String>();
		for (Map graduation: graduations) {
			strGraduations.add(((graduation == null) ? null : (String) graduation.get("SHORT_DESC") ) + "   " + 
					((graduation == null) ? null : df.format((Date) graduation.get("GRADUATION_DATE"))));
		}
		if (strGraduations.size() == 0) {
			strGraduations.add(" ");
		}

		String todaysDate = df.format(new Date());
		// Build the table

		Table myTable = Table.builder()

				.addColumnsOfWidth(140, 300, 140).addRow(Row
						.builder().add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()

								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
										.text("Date Printed:  ").build())
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text(todaysDate).build())
								.build()).horizontalAlignment(HorizontalAlignment.LEFT)
								.verticalAlignment(VerticalAlignment.TOP).borderWidth(0).backgroundColor(Color.WHITE)
								.build())
						.add(TextCell.builder().text(header.orgName).font(PDType1Font.TIMES_BOLD).fontSize(16)
								.horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(0).build())
						.add(TextCell.builder().text("").horizontalAlignment(HorizontalAlignment.RIGHT)
								.verticalAlignment(VerticalAlignment.TOP).font(PDType1Font.TIMES_BOLD)
								.fontSize(FONT_SIZE).borderWidth(0).build())
						.build())
				.addRow(Row.builder().add(TextCell.builder().text("").build())
						.add(TextCell.builder().text("Office of the Registrar")
								.horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN)
								.fontSize(10).build())
						.add(TextCell.builder().text("").build()).build())
				.addRow(Row.builder().add(TextCell.builder().text("").build())
						.add(TextCell.builder().text(header.orgAddress).horizontalAlignment(HorizontalAlignment.CENTER)
								.font(PDType1Font.TIMES_ROMAN).fontSize(10).build())
						.add(TextCell.builder().text("").build()).build())
				.addRow(Row.builder().add(TextCell.builder().text("").build()).add(TextCell.builder()
						.text(header.orgCity.get("CITY") + ", " + header.orgCity.get("STATE") + "  " + header.orgCity.get("ZIP_CODE"))
						.horizontalAlignment(HorizontalAlignment.CENTER).font(PDType1Font.TIMES_ROMAN).fontSize(10)
						.build()).add(TextCell.builder().text("").build()).build())
				.build();

		TableBuilder personalDetailsBuilder = Table.builder();
		personalDetailsBuilder.addColumnsOfWidth(340, 300)
				.addRow(Row.builder().add(ParagraphCell.builder()
						.paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
										.text("Name:  ").build())
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text("" + header.fullname).build())
								.build())
						.build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(HEADER_FONT_SIZE)
												.font(PDType1Font.TIMES_BOLD).text("Id:  ").build())
										.append(StyledText.builder().fontSize(HEADER_FONT_SIZE)
												.font(PDType1Font.TIMES_ROMAN).text("" + header.governmentId).build())
										.build())
								.build())
						.build()).build();
	
		for (int i = 0; i < 4; i++) {
			String program = "";
			if (i < programs.size()) {
				program = programs.get(i);
			}
			String grad = "";
			if (i < strGraduations.size()) {
				grad = strGraduations.get(i);
			}
			personalDetailsBuilder.addRow(Row.builder().height(BODY_ROW_HEIGHT)
							.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
									.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
											.text(i==0?"Program/Degree/Curriculum:  ":"                                                     ").build())
									.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
											.text(program).build())
									.build()).build())
							.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
									.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
											.text(i == 0?"Degree/Date Granted:  ":"                                        ").build())
									.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
											.text(grad)
											.build())
									.build()).build())
							.build());
		}
		
		personalDetailsBuilder
				.addRow(Row.builder().height(BODY_ROW_HEIGHT)
						.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
										.text("Previous Institution:  ").build())
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text(previous.size() < 1 ? "" : previous.get(0)).build())
								.build()).build())
						.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
										.text("Honors:  ").build())
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text(honors == null ? "" : honors).build())
								.build()).build())
						.build())
				.addRow(Row.builder().height(BODY_ROW_HEIGHT)
						.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
										.text("                                      ").build())
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text(previous.size() < 2 ? "" : previous.get(1)).build())
								.build()).build())
						.add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_BOLD)
										.text("Cumulative GPA:  ").build())
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text(String.format("%3.2f", cumulativeGpa)).build())
								.build()).build())
						.build())
				.build();
		
		Table personalDetails = personalDetailsBuilder.build();

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

	public void printFooter(PDDocument document, PDPage page, PDPageContentStream contentStream, boolean endOfData) {
		Table myTable = Table.builder()

				.addColumnsOfWidth(290, 290)
				.addRow(Row.builder().add(ParagraphCell.builder()
						.paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text("").build())
								.build())
						.horizontalAlignment(HorizontalAlignment.CENTER).colSpan(2)
						.verticalAlignment(VerticalAlignment.TOP).borderWidth(0).backgroundColor(Color.WHITE).build())
						.build())
				.addRow(Row.builder().add(ParagraphCell.builder()
						.paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text("").build())
								.build())
						.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.TOP)
						.borderWidth(0).backgroundColor(Color.WHITE).build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(HEADER_FONT_SIZE)
												.font(PDType1Font.TIMES_ROMAN).text("").build())
										.build())
								.horizontalAlignment(HorizontalAlignment.CENTER)
								.verticalAlignment(VerticalAlignment.TOP).borderWidthBottom(1)
								.backgroundColor(Color.WHITE).build())
						.build())
				.addRow(Row.builder().add(ParagraphCell.builder()
						.paragraph(ParagraphCell.Paragraph.builder()
								.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN)
										.text("").build())
								.build())
						.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.TOP)
						.borderWidth(0).backgroundColor(Color.WHITE).build())
						.add(ParagraphCell.builder()
								.paragraph(ParagraphCell.Paragraph.builder()
										.append(StyledText.builder().fontSize(HEADER_FONT_SIZE)
												.font(PDType1Font.TIMES_ROMAN).text("DATE PROCESSED").build())
										.build())
								.horizontalAlignment(HorizontalAlignment.CENTER)
								.verticalAlignment(VerticalAlignment.TOP).borderWidth(0).backgroundColor(Color.WHITE)
								.build())
						.build())
				.addRow(Row.builder().add(ParagraphCell.builder().paragraph(ParagraphCell.Paragraph.builder()

						.append(StyledText.builder().fontSize(HEADER_FONT_SIZE).font(PDType1Font.TIMES_ROMAN).text(
								"IN ACCORDANCE WITH THE FAMILY EDUCATIONAL RIGHTS AND PRIVACY ACT OF 1974, AS AMENDED, TRANSCRIPTS MAY NOT BE RELEASED TO A THIRD PARTY WITHOUT THE WRITTEN CONSENT OF THE STUDENT.")
								.build())
						.build()).horizontalAlignment(HorizontalAlignment.CENTER).colSpan(2)
						.verticalAlignment(VerticalAlignment.TOP).borderWidth(0).backgroundColor(Color.WHITE).build())
						.build())
				.build();

		TableDrawer tableDrawer2 = TableDrawer.builder().contentStream(contentStream).page(page).startX(20f)
				.startY(page.getMediaBox().getUpperRightY() - 710f).table(myTable).build();

		tableDrawer2.draw();
	}

}
