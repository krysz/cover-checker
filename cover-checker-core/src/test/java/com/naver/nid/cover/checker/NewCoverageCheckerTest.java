package com.naver.nid.cover.checker;

import com.naver.nid.cover.checker.model.NewCoverageCheckReport;
import com.naver.nid.cover.checker.model.NewCoveredFile;
import com.naver.nid.cover.parser.coverage.model.CoverageStatus;
import com.naver.nid.cover.parser.coverage.model.FileCoverageReport;
import com.naver.nid.cover.parser.coverage.model.LineCoverageReport;
import com.naver.nid.cover.parser.diff.model.Diff;
import com.naver.nid.cover.parser.diff.model.DiffSection;
import com.naver.nid.cover.parser.diff.model.Line;
import com.naver.nid.cover.parser.diff.model.ModifyType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NewCoverageCheckerTest {

    @Test
    public void coverCheckTest() {
        NewCoverageChecker checker = new NewCoverageChecker();

        List<Line> lines = Arrays.asList(
                Line.builder().lineNumber(1).type(ModifyType.ADD).build()
                , Line.builder().lineNumber(2).type(ModifyType.ADD).build());

        List<DiffSection> diffSectionList = Collections.singletonList(DiffSection.builder().lineList(lines).build());
        List<Diff> diffList = Collections.singletonList(Diff.builder().fileName("test.java").diffSectionList(diffSectionList).build());


        LineCoverageReport lineCoverageReport = new LineCoverageReport();
        lineCoverageReport.setStatus(CoverageStatus.COVERED);
        lineCoverageReport.setLineNum(1);

        LineCoverageReport lineCoverageReport2 = new LineCoverageReport();
        lineCoverageReport2.setStatus(CoverageStatus.UNCOVERED);
        lineCoverageReport2.setLineNum(2);

        FileCoverageReport fileCoverageReport = new FileCoverageReport();
        fileCoverageReport.setType("java");
        fileCoverageReport.setFileName("test.java");
        fileCoverageReport.setLineCoverageReportList(Arrays.asList(lineCoverageReport, lineCoverageReport2));
        List<FileCoverageReport> coverage = Collections.singletonList(fileCoverageReport);

        Map<Range, CoverageStatus> addedLine = new LinkedHashMap<>();
        addedLine.put(new Range(1, 1), CoverageStatus.COVERED);
        addedLine.put(new Range(2, 2), CoverageStatus.UNCOVERED);

        NewCoverageCheckReport newCoverageCheckReport = NewCoverageCheckReport.builder()
                .threshold(60)
                .totalNewLine(2)
                .coveredNewLine(1)
                .coveredFilesInfo(
                        Collections.singletonList(NewCoveredFile.builder()
                                .name("[test.java]")
                                .url("test.java.html")
                                .addedLine(addedLine)
                                .addedLineCount(2)
                                .addedCoverLine(1)
                                .build()))
                .build();

        NewCoverageCheckReport check = checker.check(coverage, diffList, 60, 0, "");
        assertEquals(newCoverageCheckReport, check);

    }

    @Test
    public void coverCheckTestForNonJava() {
        NewCoverageChecker checker = new NewCoverageChecker();

        List<Line> lines = Arrays.asList(
                Line.builder().lineNumber(1).type(ModifyType.ADD).build()
                , Line.builder().lineNumber(2).type(ModifyType.ADD).build());

        List<DiffSection> diffSectionList = Collections.singletonList(DiffSection.builder().lineList(lines).build());
        List<Diff> diffList = Collections.singletonList(Diff.builder().fileName("test.kt").diffSectionList(diffSectionList).build());


        LineCoverageReport lineCoverageReport = new LineCoverageReport();
        lineCoverageReport.setStatus(CoverageStatus.COVERED);
        lineCoverageReport.setLineNum(1);

        LineCoverageReport lineCoverageReport2 = new LineCoverageReport();
        lineCoverageReport2.setStatus(CoverageStatus.UNCOVERED);
        lineCoverageReport2.setLineNum(2);

        FileCoverageReport fileCoverageReport = new FileCoverageReport();
        fileCoverageReport.setType("java");
        fileCoverageReport.setFileName("test.kt");
        fileCoverageReport.setLineCoverageReportList(Arrays.asList(lineCoverageReport, lineCoverageReport2));
        List<FileCoverageReport> coverage = Collections.singletonList(fileCoverageReport);

        Map<Range, CoverageStatus> addedLine = new LinkedHashMap<>();
        addedLine.put(new Range(1, 1), CoverageStatus.COVERED);
        addedLine.put(new Range(2, 2), CoverageStatus.UNCOVERED);

        NewCoverageCheckReport newCoverageCheckReport = NewCoverageCheckReport.builder()
                .threshold(60)
                .totalNewLine(2)
                .coveredNewLine(1)
                .coveredFilesInfo(
                        Collections.singletonList(NewCoveredFile.builder()
                                .name("[test.kt]")
                                .url("test.kt.html")
                                .addedLine(addedLine)
                                .addedLineCount(2)
                                .addedCoverLine(1)
                                .build()))
                .build();

        NewCoverageCheckReport check = checker.check(coverage, diffList, 60, 0, "");
        assertEquals(newCoverageCheckReport, check);
    }

    @Test
    public void coverCheckTestForMultiModule() {
        NewCoverageChecker checker = new NewCoverageChecker();

        List<Line> lines = Arrays.asList(
                Line.builder().lineNumber(1).type(ModifyType.ADD).build()
                , Line.builder().lineNumber(2).type(ModifyType.ADD).build());

        List<DiffSection> diffSectionList = Collections.singletonList(DiffSection.builder().lineList(lines).build());
        List<Diff> diffList = Arrays.asList(
                Diff.builder().fileName("Module1/src/main/kotlin/test.kt").diffSectionList(diffSectionList).build(),
                Diff.builder().fileName("Module2/src/main/kotlin/test.kt").diffSectionList(diffSectionList).build());


        LineCoverageReport lineCoverageReport = new LineCoverageReport();
        lineCoverageReport.setStatus(CoverageStatus.COVERED);
        lineCoverageReport.setLineNum(1);

        LineCoverageReport lineCoverageReport2 = new LineCoverageReport();
        lineCoverageReport2.setStatus(CoverageStatus.UNCOVERED);
        lineCoverageReport2.setLineNum(2);

        FileCoverageReport fileCoverageReport = new FileCoverageReport();
        fileCoverageReport.setType("kt");
        fileCoverageReport.setFileName("Module1/src/main/kotlin/test.kt");
        fileCoverageReport.setLineCoverageReportList(Arrays.asList(lineCoverageReport, lineCoverageReport2));

        FileCoverageReport fileCoverageReport2 = new FileCoverageReport();
        fileCoverageReport2.setType("kt");
        fileCoverageReport2.setFileName("Module2/src/main/kotlin/test.kt");
        fileCoverageReport2.setLineCoverageReportList(Arrays.asList(lineCoverageReport, lineCoverageReport2));

        List<FileCoverageReport> coverage = Arrays.asList(fileCoverageReport, fileCoverageReport2);

        Map<Range, CoverageStatus> addedLine = new LinkedHashMap<>();
        addedLine.put(new Range(1, 1), CoverageStatus.COVERED);
        addedLine.put(new Range(2, 2), CoverageStatus.UNCOVERED);

        NewCoverageCheckReport newCoverageCheckReport = NewCoverageCheckReport.builder()
                .threshold(60)
                .totalNewLine(4)
                .coveredNewLine(2)
                .coveredFilesInfo(
                        Arrays.asList(NewCoveredFile.builder()
                                        .name("[test.kt]")
                                        .url("Module2.src.main.kotlin/test.kt.html")
                                        .addedLine(addedLine)
                                        .addedCoverLine(1)
                                        .addedLineCount(2)
                                        .build(),
                                NewCoveredFile.builder()
                                        .name("[test.kt]")
                                        .url("Module1.src.main.kotlin/test.kt.html")
                                        .addedLine(addedLine)
                                        .addedCoverLine(1)
                                        .addedLineCount(2)
                                        .build()))
                .build();

        NewCoverageCheckReport check = checker.check(coverage, diffList, 60, 0, "");
        assertEquals(newCoverageCheckReport, check);
    }
}
