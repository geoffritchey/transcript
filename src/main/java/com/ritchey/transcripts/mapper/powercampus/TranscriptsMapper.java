package com.ritchey.transcripts.mapper.powercampus;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TranscriptsMapper {

    @Select("  SELECT CODE_PROGRAM.MEDIUM_DESC as program,  \r\n"
    		+ "       CODE_DEGREE.MEDIUM_DESC as degree,  \r\n"
    		+ "       TRANSCRIPTDEGREE.FORMAL_TITLE as title  \r\n"
    		+ "FROM CODE_PROGRAM,  \r\n"
    		+ "     CODE_DEGREE,  \r\n"
    		+ "     TRANSCRIPTDEGREE\r\n"
    		+ "WHERE TRANSCRIPTDEGREE.PROGRAM =CODE_PROGRAM.CODE_VALUE  \r\n"
    		+ "  AND TRANSCRIPTDEGREE.DEGREE =CODE_DEGREE.CODE_VALUE  \r\n"
    		+ "  AND TRANSCRIPTDEGREE.PEOPLE_CODE_ID = #{campusId} \r\n"
    		+ "  AND TRANSCRIPTDEGREE.TRANSCRIPT_SEQ = #{sequence} \r\n"
    		+ "  AND TRANSCRIPTDEGREE.TRANSCRIPT_PRINT ='Y'  \r\n"
    		+ "ORDER BY TRANSCRIPTDEGREE.GRADUATION_DATE DESC,  \r\n"
    		+ "         CODE_PROGRAM.MEDIUM_DESC ASC,  \r\n"
    		+ "         CODE_DEGREE.MEDIUM_DESC ASC,  \r\n"
    		+ "         TRANSCRIPTDEGREE.FORMAL_TITLE ASC ")
    Map selectProgramDegree(String campusId, String sequence);
    
    @Select("SELECT top 1 TRANSCRIPTDETAIL.EVENT_ID, TRANSCRIPTDETAIL.ACADEMIC_YEAR, TRANSCRIPTDETAIL.ACADEMIC_TERM, TRANSCRIPTDETAIL.PEOPLE_CODE_ID, TRANSCRIPTDETAIL.EVENT_MED_NAME, TRANSCRIPTDETAIL.FINAL_GRADE,\r\n"
    		+ "           TRANSCRIPTDETAIL.FINAL_QUALITY_PNTS, TRANSCRIPTDETAIL.CONTACT_HOURS, TRANSCRIPTDETAIL.PEOPLE_ID, TRANSCRIPTDETAIL.ACADEMIC_SESSION, TRANSCRIPTDETAIL.EVENT_SUB_TYPE, TRANSCRIPTDETAIL.SECTION, TRANSCRIPTDETAIL.CREDIT_GRADE,\r\n"
    		+ "           TRANSCRIPTDETAIL.REPEATED, TRANSCRIPTDETAIL.ORG_CODE_ID, vwstrangpat.ATTEMPTED_CREDITS, vwstrangpat.EARNED_CREDITS, VWSTRANGPAT.GPA, vwstrangpat.GPA_CREDITS,\r\n"
    		+ "           vwstrangpat.QUALITY_POINTS, vwstrangpat.TOTAL_CREDITS, vwstrangpao.ATTEMPTED_CREDITS, vwstrangpao.EARNED_CREDITS, vwstrangpao.GPA_CREDITS, vwstrangpao.QUALITY_POINTS, VWSTRANGPAO.GPA,\r\n"
    		+ "           vwstrangpao.TOTAL_CREDITS, PEOPLE.FIRST_NAME, PEOPLE.LAST_NAME, PEOPLE.GOVERNMENT_ID, TRANSCRIPTCOMMENT.COMMENTS, CODE_ACATERM.MEDIUM_DESC, CODE_ACATERM.SORT_ORDER,\r\n"
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
    		+ "WHERE TRANSCRIPTDETAIL.PEOPLE_CODE_ID = #{campusId} \r\n"
    		+ "  AND TRANSCRIPTDETAIL.TRANSCRIPT_SEQ = #{sequence} \r\n"
    		+ "  AND (TRANSCRIPTDETAIL.ABT_JOIN <> 'Y' + 'N') \r\n"
    		+ "  AND (TRANSCRIPTDETAIL.ABT_JOIN <> 'UNOFFICIAL') \r\n"
    		+ "  AND (TRANSCRIPTDETAIL.FINAL_GRADE <> ''  OR (#{getIfFinalGradeBlank} = 'Y' AND TRANSCRIPTDETAIL.FINAL_GRADE = '')) ")
    Map selectDetails(String campusId, String sequence, String getIfFinalGradeBlank); 
    
    @Select("SELECT GPA\r\n"
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
    
    @Select("SELECT MEDIUM_DESC\r\n"
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
    Map selectGraduationDate(String campusId, String sequence);
}