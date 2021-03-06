package com.ritchey.transcripts.mapper.powercampus;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TranscriptsMapper {
	
	@Select("select People_code_id\r\n"
			+ "from student\r\n"
			+ "where last_year is not null and last_year <> ''\r\n"
			+ "order by people_code_id")
	List<String> selectStudents();
	
	@Select("SELECT ISNULL (ADDRESS.HOUSE_NUMBER,\r\n"
			+ "               '') + ' ' + ISNULL (ADDRESS.ADDRESS_LINE_1,\r\n"
			+ "                                   '') + ' ' + ISNULL (ADDRESS.ADDRESS_LINE_2,\r\n"
			+ "                                                       '') + ' ' + ISNULL (ADDRESS.ADDRESS_LINE_3,\r\n"
			+ "                                                                           '') + ' ' + ISNULL (ADDRESS.ADDRESS_LINE_4,\r\n"
			+ "                                                                                               '')\r\n"
			+ "FROM ORGANIZATION,\r\n"
			+ "     ADDRESS\r\n"
			+ "WHERE ORGANIZATION.ORG_CODE_ID =ADDRESS.PEOPLE_ORG_CODE_ID\r\n"
			+ "  AND ORGANIZATION.PREFERRED_ADD =ADDRESS.ADDRESS_TYPE\r\n"
			+ "  AND ORGANIZATION.ORG_CODE_ID =#{organizationId}")
	String selectOrgStreetAddress(String organizationId);
	
	@Select("  SELECT CITY,\r\n"
			+ "       STATE,\r\n"
			+ "       ZIP_CODE\r\n"
			+ "FROM ORGANIZATION,\r\n"
			+ "     ADDRESS\r\n"
			+ "WHERE ORGANIZATION.ORG_CODE_ID =ADDRESS.PEOPLE_ORG_CODE_ID\r\n"
			+ "  AND ORGANIZATION.PREFERRED_ADD =ADDRESS.ADDRESS_TYPE\r\n"
			+ "  AND ORGANIZATION.ORG_CODE_ID =#{organizationId}")
	Map selectOrgCityStateZip(String organizationId);
	
	@Select("SELECT ORG_NAME_1,\r\n"
			+ "       FICE_CODE\r\n"
			+ "FROM ORGANIZATION\r\n"
			+ "WHERE ORG_CODE_ID =#{organizationId}\r\n"
			+ "")
	String selectOrgName(String organizationId);

    @Select("SELECT CODE_PROGRAM.MEDIUM_DESC as program, \r\n"
    		+ "       CODE_DEGREE.MEDIUM_DESC as degree, \r\n"
    		+ "       TRANSCRIPTDEGREE.FORMAL_TITLE as title \r\n"
    		+ "FROM CODE_PROGRAM,\r\n"
    		+ "     CODE_DEGREE,\r\n"
    		+ "     TRANSCRIPTDEGREE\r\n"
    		+ "WHERE TRANSCRIPTDEGREE.PROGRAM =CODE_PROGRAM.CODE_VALUE\r\n"
    		+ "  AND TRANSCRIPTDEGREE.DEGREE =CODE_DEGREE.CODE_VALUE\r\n"
    		+ "  AND TRANSCRIPTDEGREE.PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "  AND TRANSCRIPTDEGREE.TRANSCRIPT_SEQ =#{sequence}\r\n"
    		+ "  AND TRANSCRIPTDEGREE.TRANSCRIPT_PRINT ='Y'\r\n"
    		+ "ORDER BY TRANSCRIPTDEGREE.GRADUATION_DATE DESC,\r\n"
    		+ "         CODE_PROGRAM.MEDIUM_DESC ASC,\r\n"
    		+ "         CODE_DEGREE.MEDIUM_DESC ASC,\r\n"
    		+ "         TRANSCRIPTDEGREE.FORMAL_TITLE ASC")
    List<Map> selectProgramDegree(String campusId, String sequence);
    
    @Select("SELECT TRANSCRIPTDETAIL.EVENT_ID, TRANSCRIPTDETAIL.ACADEMIC_YEAR, TRANSCRIPTDETAIL.ACADEMIC_TERM, TRANSCRIPTDETAIL.PEOPLE_CODE_ID, TRANSCRIPTDETAIL.EVENT_MED_NAME, TRANSCRIPTDETAIL.FINAL_GRADE,\r\n"
    		+ "           TRANSCRIPTDETAIL.FINAL_QUALITY_PNTS, TRANSCRIPTDETAIL.CONTACT_HOURS, TRANSCRIPTDETAIL.PEOPLE_ID, TRANSCRIPTDETAIL.ACADEMIC_SESSION, TRANSCRIPTDETAIL.EVENT_SUB_TYPE, TRANSCRIPTDETAIL.SECTION, TRANSCRIPTDETAIL.CREDIT_GRADE,\r\n"
    		+ "           TRANSCRIPTDETAIL.REPEATED, TRANSCRIPTDETAIL.ORG_CODE_ID, vwstrangpat.ATTEMPTED_CREDITS as ATTEMPTED_CREDITS, vwstrangpat.EARNED_CREDITS as EARNED_CREDITS, VWSTRANGPAT.GPA as GPA, vwstrangpat.GPA_CREDITS as GPA_CREDITS,\r\n"
    		+ "           vwstrangpat.QUALITY_POINTS as QUALITY_POINTS, vwstrangpat.TOTAL_CREDITS as TOTAL_CREDITS, vwstrangpao.ATTEMPTED_CREDITS as CUM_ATTEMPTED_CREDITS, vwstrangpao.EARNED_CREDITS as CUM_EARNED_CREDITS, vwstrangpao.GPA_CREDITS as CUM_GPA_CREDITS, vwstrangpao.QUALITY_POINTS as CUM_QUALITY_POINTS, VWSTRANGPAO.GPA as CUM_GPA,\r\n"
    		+ "           vwstrangpao.TOTAL_CREDITS as CUM_TOTAL_CREDITS, PEOPLE.FIRST_NAME, PEOPLE.LAST_NAME, PEOPLE.GOVERNMENT_ID, TRANSCRIPTCOMMENT.COMMENTS, CODE_ACATERM.MEDIUM_DESC, CODE_ACATERM.SORT_ORDER,\r\n"
    		+ "           TRANSCRIPTDETAIL.TRANSCRIPT_SEQ, CODE_EVENTSUBTYPE.SORT_ORDER, TRANSCRIPTCOMMENT.COMMENT_PRINT_TRAN, TRANSCRIPTDETAIL.CREDIT_TYPE, TRANSCRIPTDETAIL.REFERENCE_EVENT_ID, TRANSCRIPTCOMMENT.COMMENT_TYPE, GETGRADECOMMENT.COMMENTS,\r\n"
    		+ "           dbo.fnFormatFullName(TRANSCRIPTHEADER.PREFIX, TRANSCRIPTHEADER.FIRST_NAME, TRANSCRIPTHEADER.MIDDLE_NAME, TRANSCRIPTHEADER.LastNamePrefix, TRANSCRIPTHEADER.LAST_NAME, TRANSCRIPTHEADER.SUFFIX, '') TRANSCRIPTHEADER_fullName,\r\n"
    		+ "           COALESCE(AlternateGrade.AlternateGradeValue, '') [ALTERNATEGRADE]\r\n"
    		+ "FROM TRANSCRIPTDETAIL\r\n"
    		+ "JOIN PEOPLE ON TRANSCRIPTDETAIL.PEOPLE_CODE_ID = PEOPLE.PEOPLE_CODE_ID\r\n"
    		+ "AND TRANSCRIPTDETAIL.ADD_DROP_WAIT = 'A'\r\n"
    		+ "AND TRANSCRIPTDETAIL.COURSE_PRINT_TRAN = 'Y'\r\n"
    		+ "JOIN CODE_ACATERM ON TRANSCRIPTDETAIL.ACADEMIC_TERM = CODE_ACATERM.CODE_VALUE\r\n"
    		+ "JOIN CODE_EVENTSUBTYPE ON TRANSCRIPTDETAIL.EVENT_SUB_TYPE = CODE_EVENTSUBTYPE.CODE_VALUE\r\n"
    		+ "LEFT OUTER JOIN TRANSCRIPTCOMMENT ON (TRANSCRIPTDETAIL.PEOPLE_CODE_ID = TRANSCRIPTCOMMENT.PEOPLE_CODE_ID)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_YEAR = TRANSCRIPTCOMMENT.ACADEMIC_YEAR)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_TERM = TRANSCRIPTCOMMENT.ACADEMIC_TERM)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_SESSION = TRANSCRIPTCOMMENT.ACADEMIC_SESSION)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.EVENT_ID = TRANSCRIPTCOMMENT.EVENT_ID)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.EVENT_SUB_TYPE = TRANSCRIPTCOMMENT.EVENT_SUB_TYPE)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.SECTION = TRANSCRIPTCOMMENT.SECTION)\r\n"
    		+ "AND (TRANSCRIPTCOMMENT.COMMENT_TYPE = 'C')\r\n"
    		+ "LEFT OUTER JOIN VWSTRANGPAT ON (TRANSCRIPTDETAIL.PEOPLE_CODE_ID = VWSTRANGPAT.PEOPLE_CODE_ID)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_YEAR = VWSTRANGPAT.ACADEMIC_YEAR)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_TERM = VWSTRANGPAT.ACADEMIC_TERM)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.TRANSCRIPT_SEQ = VWSTRANGPAT.TRANSCRIPT_SEQ)\r\n"
    		+ "LEFT OUTER JOIN VWSTRANGPAO ON (TRANSCRIPTDETAIL.PEOPLE_CODE_ID = VWSTRANGPAO.PEOPLE_CODE_ID)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_YEAR = VWSTRANGPAO.ACADEMIC_YEAR)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_TERM = VWSTRANGPAO.ACADEMIC_TERM)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.TRANSCRIPT_SEQ = VWSTRANGPAO.TRANSCRIPT_SEQ)\r\n"
    		+ "LEFT OUTER JOIN TRANSCRIPTCOMMENT GETGRADECOMMENT ON (TRANSCRIPTDETAIL.PEOPLE_CODE_ID = GETGRADECOMMENT.PEOPLE_CODE_ID)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_YEAR = GETGRADECOMMENT.ACADEMIC_YEAR)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_TERM = GETGRADECOMMENT.ACADEMIC_TERM)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.ACADEMIC_SESSION = GETGRADECOMMENT.ACADEMIC_SESSION)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.EVENT_ID = GETGRADECOMMENT.EVENT_ID)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.EVENT_SUB_TYPE = GETGRADECOMMENT.EVENT_SUB_TYPE)\r\n"
    		+ "AND (TRANSCRIPTDETAIL.SECTION = GETGRADECOMMENT.SECTION)\r\n"
    		+ "AND (GETGRADECOMMENT.COMMENT_TYPE = 'F')\r\n"
    		+ "AND (GETGRADECOMMENT.COMMENT_PRINT_TRAN = 'Y')\r\n"
    		+ "LEFT OUTER JOIN TRANSCRIPTHEADER ON TRANSCRIPTDETAIL.PEOPLE_CODE_ID = TRANSCRIPTHEADER.PEOPLE_CODE_ID\r\n"
    		+ "AND TRANSCRIPTDETAIL.TRANSCRIPT_SEQ = TRANSCRIPTHEADER.TRANSCRIPT_SEQ\r\n"
    		+ "LEFT OUTER JOIN AlternateGrade ON TRANSCRIPTDETAIL.TranscriptDetailId = AlternateGrade.TranscriptDetailId\r\n"
    		+ "left join academiccalendar ac on ac.ACADEMIC_YEAR = TRANSCRIPTDETAIL.ACADEMIC_YEAR\r\n"
    		+ " and ac.ACADEMIC_TERM = TRANSCRIPTDETAIL.ACADEMIC_TERM\r\n"
    		+ " and ac.ACADEMIC_SESSION = TRANSCRIPTDETAIL.ACADEMIC_SESSION \r\n"
    		+ "WHERE TRANSCRIPTDETAIL.PEOPLE_CODE_ID = #{campusId} \r\n"
    		+ "  AND TRANSCRIPTDETAIL.TRANSCRIPT_SEQ = #{sequence} \r\n"
    		+ "  AND (TRANSCRIPTDETAIL.ABT_JOIN <> 'Y' + 'N') \r\n"
    		+ "  AND (TRANSCRIPTDETAIL.ABT_JOIN <> 'UNOFFICIAL') \r\n"
    		+ "  AND (TRANSCRIPTDETAIL.FINAL_GRADE <> ''  OR (#{getIfFinalGradeBlank} = 'Y' AND TRANSCRIPTDETAIL.FINAL_GRADE = ''))"
    		+ "order by academic_year, CODE_ACATERM.SORT_ORDER, ORG_CODE_ID, event_id ")
    List<Map> selectDetails(String campusId, String sequence, String getIfFinalGradeBlank); 
    
    //This query can return more than one result but it is ordered by term sort order to get the correct one as the top 1
    @Select("SELECT top 1 GPA\r\n"
    		+ "FROM TRANSCRIPTGPA,\r\n"
    		+ "     CODE_ACATERM\r\n"
    		+ "WHERE PROGRAM =''\r\n"
    		+ "  AND DEGREE =''\r\n"
    		+ "  AND CURRICULUM =''\r\n"
    		+ "  AND RECORD_TYPE ='O'\r\n"
    		+ "  AND PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "  AND TRANSCRIPT_SEQ =#{sequence}\r\n"
    		+ "  AND TRANSCRIPTGPA.ACADEMIC_TERM =CODE_ACATERM.CODE_VALUE\r\n"
    		+ "  AND ACADEMIC_YEAR =\r\n"
    		+ "    (SELECT MAX (ACADEMIC_YEAR)\r\n"
    		+ "     FROM TRANSCRIPTGPA\r\n"
    		+ "     WHERE PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "       AND TRANSCRIPT_SEQ =#{sequence} )\r\n"
    		+ "ORDER BY CODE_ACATERM.SORT_ORDER DESC ")
    Double cumulativeGPA(String campusId, String sequence);
    
    @Select("SELECT top 1 MEDIUM_DESC\r\n"
    		+ "FROM CODE_HONORS,\r\n"
    		+ "     TRANSCRIPTHONORS\r\n"
    		+ "WHERE CODE_HONORS.CODE_VALUE =TRANSCRIPTHONORS.HONORS\r\n"
    		+ "  AND PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "  AND TRANSCRIPT_SEQ =#{sequence}\r\n"
    		+ "ORDER BY HONORS")
    String selectHonors(String campusId, String sequence);
    
    @Select("SELECT SHORT_DESC, GRADUATION_DATE\r\n"
    		+ "FROM CODE_DEGREE,\r\n"
    		+ "     TRANSCRIPTDEGREE\r\n"
    		+ "WHERE PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "  AND TRANSCRIPT_SEQ =#{sequence}\r\n"
    		+ "  AND (GRADUATION_DATE <> ''\r\n"
    		+ "       OR GRADUATION_DATE IS NOT NULL)\r\n"
    		+ "  AND CODE_VALUE =DEGREE\r\n"
    		+ "  AND TRANSCRIPTDEGREE.TRANSCRIPT_PRINT ='Y'\r\n"
    		+ "ORDER BY GRADUATION_DATE DESC,\r\n"
    		+ "         CODE_DEGREE.SHORT_DESC ASC")
    List<Map> selectGraduationDate(String campusId, String sequence);
    
    
    @Select("SELECT o.ORG_NAME_1,\r\n"
    		+ "       cd.SHORT_DESC\r\n"
    		+ "FROM dbo.EDUCATION e\r\n"
    		+ "INNER JOIN dbo.ORGANIZATION o ON e.ORG_CODE_ID =o.ORG_CODE_ID\r\n"
    		+ "LEFT OUTER JOIN dbo.CODE_DEGREE cd ON e.DEGREE =cd.CODE_VALUE\r\n"
    		+ "INNER JOIN dbo.TRANSEDUCATION t ON e.ORG_CODE_ID =t.ORG_CODE_ID\r\n"
    		+ "AND e.PEOPLE_CODE_ID =t.PEOPLE_CODE_ID\r\n"
    		+ "AND e.DEGREE =t.DEGREE\r\n"
    		+ "AND e.CURRICULUM =t.CURRICULUM\r\n"
    		+ "WHERE t.PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "  AND t.TRANSCRIPT_SEQ =#{sequence}\r\n"
    		+ "  AND t.TRANSCRIPT_PRINT ='Y'\r\n"
    		+ "  AND e.END_DATE IS NOT NULL\r\n"
    		+ "ORDER BY e.END_DATE DESC")
    List<Map> selectDegree(String campusId, String sequence);
    
    @Select("SELECT TRANSCRIPT_SEQ "
    		+ "FROM TRANSCRIPTDEGREE "
    		+ "WHERE PEOPLE_CODE_ID = #{campusId} "
    		+ "GROUP BY TRANSCRIPT_SEQ")
    List<String> selectTranscriptSequences(String campusId);
    
    @Select("SELECT COALESCE (CODE_TEST.SHORT_DESC, TESTSCORES.TEST_ID) AS TestID, COALESCE (CODE_TESTTYPE.SHORT_DESC,\r\n"
    		+ "                          TESTSCORES.TEST_TYPE) AS TestType, TESTSCORES.TEST_DATE, TESTSCORES.RAW_SCORE,\r\n"
    		+ "                         TESTSCORES.ALPHA_SCORE, TESTSCORES.ALPHA_SCORE_1, TESTSCORES.ALPHA_SCORE_2, TESTSCORES.ALPHA_SCORE_3\r\n"
    		+ "FROM dbo.TESTSCORES\r\n"
    		+ "LEFT OUTER JOIN dbo.CODE_TEST ON TESTSCORES.TEST_ID =CODE_TEST.CODE_VALUE_KEY\r\n"
    		+ "LEFT OUTER JOIN dbo.CODE_TESTTYPE ON TESTSCORES.TEST_TYPE =CODE_TESTTYPE.CODE_VALUE_KEY\r\n"
    		+ "WHERE (TESTSCORES.PEOPLE_CODE_ID =#{campusId})\r\n"
    		+ "  AND (TESTSCORES.TRANSCRIPT_PRINT ='Y')\r\n"
    		+ "ORDER BY TESTSCORES.TEST_DATE ASC")
    List<Map> selectTestScores(String campusId);
    
    @Select("SELECT NOTES\r\n"
    		+ "FROM NOTES\r\n"
    		+ "WHERE (ACADEMIC_YEAR =''\r\n"
    		+ "       OR ACADEMIC_YEAR =NULL)\r\n"
    		+ "  AND (ACADEMIC_TERM =''\r\n"
    		+ "       OR ACADEMIC_TERM =NULL)\r\n"
    		+ "  AND PRINT_ON_TRANS ='Y'\r\n"
    		+ "  AND PEOPLE_ORG_CODE_ID =#{campusId}\r\n"
    		+ "  AND TRANSCRIPT_SEQ =#{sequence}")
    List<Map> selectNotes(String campusId, String sequence);
    
    
    @Select("SELECT O.ORG_NAME_1, FICE_CODE\r\n"
    		+ "FROM ORGANIZATION O,\r\n"
    		+ "     TRANSCRIPTDETAIL T\r\n"
    		+ "WHERE T.ORG_CODE_ID =O.ORG_CODE_ID\r\n"
    		+ "  AND T.PEOPLE_CODE_ID =#{campusId}\r\n"
    		+ "  AND T.ACADEMIC_YEAR =#{year}\r\n"
    		+ "  AND T.ACADEMIC_TERM =#{term}\r\n"
    		+ "  AND T.ACADEMIC_SESSION =#{session}\r\n"
    		+ "  AND T.EVENT_ID =#{eventId}\r\n"
    		+ "  AND T.EVENT_SUB_TYPE =#{eventSubType}\r\n"
    		+ "  AND T.SECTION =#{section}\r\n"
    		+ "  AND T.TRANSCRIPT_SEQ =#{sequence}\r\n"
    		+ "  AND T.ORG_CODE_ID <> #{orgCode}")
    Map selectOtherSchools(String campusId, String year, String term, String session, String eventId, String eventSubType, String section
    		, String sequence, String orgCode);
    
    @Select("SELECT DISTINCT c.MEDIUM_DESC \r\n"
    		+ "FROM TRANSCRIPTAWARD A \r\n"
    		+ "	join CODE_AWARDVALUE C on  A.AWARD_VALUE =C.CODE_VALUE\r\n"
    		+ "WHERE 1=1\r\n"
    		+ "	and A.PEOPLE_CODE_ID = #{campusId} \r\n"
    		+ "  AND A.TRANSCRIPT_SEQ = #{sequence} \r\n"
    		+ "   AND A.AWARD_TYPE = #{awardType} \r\n"
    		+ "  AND A.PRINT_TRANSCRIPT =#{printTranscript} \r\n"
    		+ "    AND A.ACADEMIC_YEAR =#{year} \r\n"
    		+ "  AND A.ACADEMIC_TERM =#{term} ")
    List<Map> selectAwards(String campusId, String sequence, String awardType, String printTranscript, String year, String term);
}
