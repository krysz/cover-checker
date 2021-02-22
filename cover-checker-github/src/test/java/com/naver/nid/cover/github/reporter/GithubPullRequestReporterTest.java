package com.naver.nid.cover.github.reporter;

import com.naver.nid.cover.checker.Range;
import com.naver.nid.cover.checker.model.CommitState;
import com.naver.nid.cover.checker.model.NewCoverageCheckReport;
import com.naver.nid.cover.checker.model.NewCoveredFile;
import com.naver.nid.cover.github.manager.GithubCommentManager;
import com.naver.nid.cover.github.manager.GithubPullRequestManager;
import com.naver.nid.cover.github.manager.GithubStatusManager;
import com.naver.nid.cover.github.manager.model.CommitStatusCreate;
import com.naver.nid.cover.parser.coverage.model.CoverageStatus;

import org.eclipse.egit.github.core.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static javax.swing.UIManager.put;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GithubPullRequestReporterTest {

	private static final String COMMENT_WITH_FILE = "#### [PR Coverage check]\n" +
			"\n" +
			":heart_eyes: **pass** : 2 / 3 (66.67%)\n" +
			"\n" +
			"\n" +
			"\n\n" +
			"#### file detail\n" +
			"\n" +
			"|   |path|covered line|new line|coverage|\n" +
			"|----|----|----|----|----|\n" +
			"|:large_blue_circle:|<details close><summary>test.java(test.java.html)</summary><ul>"
			+ "<li>:green_heart:[Line 1](test.java.html#L1)</li>"
			+ "<li>:heart:[Line 2](test.java.html#L2)</li>"
			+ "<li>:green_heart:[Line 3](test.java.html#L3)</li>"
			+ "<li>:heart:[Line 4](test.java.html#L4)</li>"
			+ "</ul></details>|2|4|50.00%|\n" +
			"|:large_blue_circle:|<details close><summary>test2.java(test2.java.html)</summary><ul>"
			+ "<li>:green_heart:[Line 1-2](test2.java.html#L1)</li>"
			+ "<li>:heart:[Line 3-4](test2.java.html#L3)</li>"
			+ "</ul></details>|2|4|50.00%|\n\n\n";

	private static final String COMMENT_WITH_CONFUSE = "#### [PR Coverage check]\n" +
			"\n" +
			":confused: **check** : 2 / 3 (66.67%)\n" +
			"\n" +
			"\n" +
			"\n\n" +
			"#### file detail\n" +
			"\n" +
			"|   |path|covered line|new line|coverage|\n" +
			"|----|----|----|----|----|\n" +
			"|:large_blue_circle:|<details close><summary>test.java(test.java.html)</summary><ul>"
			+ "<li>:green_heart:[Line 1-2](test.java.html#L1)</li>"
			+ "<li>:heart:[Line 3](test.java.html#L3)</li></ul></details>|2|3|66.67%|\n" +
			"|:red_circle:|<details close><summary>test2.java(test2.java.html)</summary><ul>"
			+ "<li>:green_heart:[Line 1](test2.java.html#L1)</li>"
			+ "<li>:heart:[Line 2-3](test2.java.html#L2)</li></ul></details>|1|3|33.33%|\n\n\n";

	private static final String COMMENT_WITHOUT_FILE = "#### [PR Coverage check]\n" +
			"\n\n" +
			":heart_eyes: **pass** : 0 / 0 (0%)\n\n\n";

	private static final String COMMENT_ERROR = "#### [PR Coverage check]\n" +
			"\n" +
			"coverage check fail. please retry. :fearful:\n" +
			"\n" +
			"[Please let me know](https://github.com/naver/cover-checker/issues/new) when error again.\n" +
			"\n" +
			"test error";

	private GithubPullRequestManager mockPrManager;
	private GithubCommentManager mockCommentManager;
	private GithubStatusManager mockStatusManager;
	private User mockUser;

	@BeforeEach
	public void init() throws IOException {
		mockPrManager = mock(GithubPullRequestManager.class);
		mockCommentManager = mock(GithubCommentManager.class);
		mockStatusManager = mock(GithubStatusManager.class);

		when(mockPrManager.commentManager()).thenReturn(mockCommentManager);
		when(mockPrManager.statusManager()).thenReturn(mockStatusManager);
		mockUser = new User().setId(1);

		when(mockPrManager.getUser()).thenReturn(mockUser);
	}

	@Test
	public void reportTest() throws IOException {
		GithubPullRequestReporter reporter = new GithubPullRequestReporter(mockPrManager);
		when(mockCommentManager.deleteComment(reporter.oldReport(mockUser))).thenReturn(1);
		doNothing().when(mockCommentManager).addComment(COMMENT_WITH_FILE);

		CommitStatusCreate commitStatus = CommitStatusCreate.builder()
				.state(CommitState.SUCCESS)
				.description("2 / 3 (66%) - pass")
				.context("coverchecker").build();
		doNothing().when(mockStatusManager).setStatus(commitStatus);

		Map<Range, CoverageStatus> addedLine1 = new LinkedHashMap<>();
		Map<Range, CoverageStatus> addedLine2 = new LinkedHashMap<>();
		IntStream.rangeClosed(1, 4).forEach(i-> addedLine1.put(new Range(i, i), i % 2 != 0 ? CoverageStatus.COVERED : CoverageStatus.UNCOVERED));

		addedLine2.put(new Range(1,2), CoverageStatus.COVERED);
		addedLine2.put(new Range(3,4), CoverageStatus.UNCOVERED);

		NewCoverageCheckReport result = NewCoverageCheckReport.builder()
				.totalNewLine(3)
				.coveredNewLine(2)
				.threshold(50)
				.coveredFilesInfo(Arrays.asList(NewCoveredFile.builder().name("test.java").url("test.java.html").addedLine(addedLine1).addedCoverLine(2).addedLineCount(4).build(),
						NewCoveredFile.builder().name("test2.java").url("test2.java.html").addedLine(addedLine2).addedCoverLine(2).addedLineCount(4).build()))
				.build();

		reporter.report(result);

		verify(mockCommentManager).addComment(COMMENT_WITH_FILE);
		verify(mockStatusManager).setStatus(commitStatus);
	}


	@Test
	public void reportConfuseTest() throws IOException {
		GithubPullRequestReporter reporter = new GithubPullRequestReporter(mockPrManager);
		when(mockCommentManager.deleteComment(reporter.oldReport(mockUser))).thenReturn(1);
		doNothing().when(mockCommentManager).addComment(COMMENT_WITH_CONFUSE);

		CommitStatusCreate commitStatus = CommitStatusCreate.builder()
				.state(CommitState.PENDING)
				.description("2 / 3 (66%) - check")
				.context("coverchecker").build();
		doNothing().when(mockStatusManager).setStatus(commitStatus);

		Map<Range, CoverageStatus> twoThirdCovered = new LinkedHashMap<>();
		Map<Range, CoverageStatus> oneThirdCovered = new LinkedHashMap<>();
		twoThirdCovered.put(new Range(1,2), CoverageStatus.COVERED);
		twoThirdCovered.put(new Range(3,3), CoverageStatus.UNCOVERED);

		oneThirdCovered.put(new Range(1,1), CoverageStatus.COVERED);
		oneThirdCovered.put(new Range(2,3), CoverageStatus.UNCOVERED);

		NewCoverageCheckReport result = NewCoverageCheckReport.builder()
				.totalNewLine(3)
				.coveredNewLine(2)
				.threshold(50)
				.coveredFilesInfo(Arrays.asList(NewCoveredFile.builder().name("test.java").url("test.java.html").addedLine(twoThirdCovered).addedLineCount(3).addedCoverLine(2).build(),
						NewCoveredFile.builder().name("test2.java").url("test2.java.html").addedLine(oneThirdCovered).addedLineCount(3).addedCoverLine(1).build()))
				.build();
		result.setFileThreshold(50);

		reporter.report(result);

		verify(mockCommentManager).addComment(COMMENT_WITH_CONFUSE);
		verify(mockStatusManager).setStatus(commitStatus);
	}

	@Test
	public void reportNoneSourceTest() throws IOException {
		GithubPullRequestReporter reporter = new GithubPullRequestReporter(mockPrManager);
		when(mockCommentManager.deleteComment(reporter.oldReport(mockUser))).thenReturn(1);
		doNothing().when(mockCommentManager).addComment(COMMENT_WITHOUT_FILE);

		CommitStatusCreate commitStatus = CommitStatusCreate.builder()
				.state(CommitState.SUCCESS)
				.description("0 / 0 (100%) - pass")
				.context("coverchecker").build();
		doNothing().when(mockStatusManager).setStatus(commitStatus);

		NewCoverageCheckReport result = NewCoverageCheckReport.builder()
				.totalNewLine(0)
				.coveredNewLine(0)
				.threshold(50)
				.build();

		reporter.report(result);

		verify(mockCommentManager).addComment(COMMENT_WITHOUT_FILE);
		verify(mockStatusManager).setStatus(commitStatus);
	}

	@Test
	public void reportError() throws IOException {
		GithubPullRequestReporter reporter = new GithubPullRequestReporter(mockPrManager);
		when(mockCommentManager.deleteComment(reporter.oldReport(mockUser))).thenReturn(1);

		doNothing().when(mockCommentManager).addComment(COMMENT_ERROR);
		CommitStatusCreate commitStatus = CommitStatusCreate.builder()
				.state(CommitState.ERROR)
				.description("error - test error")
				.context("coverchecker").build();
		doNothing().when(mockStatusManager).setStatus(commitStatus);

		NewCoverageCheckReport result = NewCoverageCheckReport.builder()
				.error(new Exception("test error"))
				.build();
		reporter.report(result);


		verify(mockCommentManager).addComment(COMMENT_ERROR);
		verify(mockStatusManager).setStatus(commitStatus);
	}
}