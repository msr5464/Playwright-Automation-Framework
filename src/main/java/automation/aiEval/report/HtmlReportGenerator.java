package automation.aiEval.report;

import automation.aiEval.model.AggregatedEvaluation;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.BaselineComparison;
import automation.aiEval.model.EvaluationResult;
import automation.aiEval.model.RunSummary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlReportGenerator
{
    public void generate(
        RunSummary summary,
        List<AggregatedEvaluation> evals,
        BaselineComparison baseline,
        File outputFile) throws IOException
    {
        generate(summary, evals, Collections.emptyList(), baseline, outputFile);
    }

    public void generate(
        RunSummary summary,
        List<AggregatedEvaluation> evals,
        List<AITestCase> testCases,
        BaselineComparison baseline,
        File outputFile) throws IOException
    {
        Map<String, String> caseIdToSuite = new HashMap<>();
        for (AITestCase tc : testCases)
        {
            if (tc.getCaseId() != null && tc.getSuite() != null)
            {
                caseIdToSuite.put(tc.getCaseId(), tc.getSuite());
            }
        }
        outputFile.getParentFile().mkdirs();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>AI Evaluation Report - ").append(escape(summary.getRunId())).append("</title>\n");
        html.append("<style>\n");
        html.append(getInlineCss());
        html.append("</style>\n</head>\n<body>\n");

        // 1. Header
        html.append("<div class=\"header\">\n");
        html.append("<h1>AI Evaluation Report</h1>\n");
        html.append("<div class=\"meta-grid\">\n");
        html.append(metaItem("Run ID", summary.getRunId()));
        html.append(metaItem("Environment", summary.getEnvironment()));
        html.append(metaItem("Timestamp", summary.getTimestamp()));
        html.append(metaItem("Build Version", summary.getBuildVersion()));
        html.append(metaItem("Model Version", summary.getModelVersion()));
        html.append("</div>\n</div>\n");

        // 2. Score summary cards
        html.append("<div class=\"cards\">\n");
        html.append(scoreCard("Total Cases", String.valueOf(summary.getTotalCases()), "neutral"));
        html.append(scoreCard("Passed", String.valueOf(summary.getPassedCases()), "pass"));
        html.append(scoreCard("Failed", String.valueOf(summary.getFailedCases()), "fail"));
        html.append(scoreCard("Avg Score", String.format("%.1f%%", summary.getAverageScore() * 100), "neutral"));
        html.append(scoreCard("Pass Rate", String.format("%.1f%%", summary.getPassRate() * 100),
            summary.getPassRate() >= 0.8 ? "pass" : "fail"));
        html.append("</div>\n");

        // 3. Suite breakdown table
        html.append("<div class=\"section\">\n<h2>Suite Breakdown</h2>\n");
        html.append("<table>\n<thead><tr><th>Suite</th><th>Total</th><th>Passed</th><th>Pass Rate</th></tr></thead>\n<tbody>\n");
        if (summary.getSuiteBreakdown() != null)
        {
            for (Map.Entry<String, Integer> entry : summary.getSuiteBreakdown().entrySet())
            {
                String suite = entry.getKey();
                int total = entry.getValue();
                int passed = summary.getSuitePassBreakdown() != null
                    ? summary.getSuitePassBreakdown().getOrDefault(suite, 0)
                    : 0;
                double rate = total > 0 ? (double) passed / total : 0.0;
                html.append("<tr><td>").append(escape(suite)).append("</td>")
                    .append("<td>").append(total).append("</td>")
                    .append("<td>").append(passed).append("</td>")
                    .append("<td class=\"").append(rate >= 0.8 ? "pass" : "fail").append("\">")
                    .append(String.format("%.1f%%", rate * 100)).append("</td></tr>\n");
            }
        }
        html.append("</tbody>\n</table>\n</div>\n");

        // 4. Per-case results table
        html.append("<div class=\"section\">\n<h2>Per-Case Results</h2>\n");
        html.append("<table>\n<thead><tr><th>Case ID</th><th>Suite</th><th>Score</th><th>Result</th><th>Top Issue</th></tr></thead>\n<tbody>\n");
        for (AggregatedEvaluation eval : evals)
        {
            String topIssue = (eval.getTopIssues() != null && !eval.getTopIssues().isEmpty())
                ? eval.getTopIssues().get(0)
                : "";
            html.append("<tr>")
                .append("<td>").append(escape(eval.getCaseId())).append("</td>")
                .append("<td>").append(escape(caseIdToSuite.getOrDefault(eval.getCaseId(), ""))).append("</td>")
                .append("<td>").append(String.format("%.2f", eval.getOverallScore())).append("</td>")
                .append("<td class=\"").append(eval.isFinalPass() ? "pass" : "fail").append("\">")
                .append(eval.isFinalPass() ? "PASS" : "FAIL").append("</td>")
                .append("<td>").append(escape(topIssue)).append("</td>")
                .append("</tr>\n");
        }
        html.append("</tbody>\n</table>\n</div>\n");

        // 5. Failed cases detail
        html.append("<div class=\"section\">\n<h2>Failed Cases Detail</h2>\n");
        boolean anyFailed = false;
        for (AggregatedEvaluation eval : evals)
        {
            if (!eval.isFinalPass())
            {
                anyFailed = true;
                html.append("<div class=\"case-detail\">\n");
                html.append("<h3 class=\"fail\">").append(escape(eval.getCaseId()))
                    .append(" — ").append(escape(eval.getRiskStatus())).append("</h3>\n");
                html.append("<p>Overall Score: ").append(String.format("%.4f", eval.getOverallScore())).append("</p>\n");

                if (eval.getEvaluatorResults() != null)
                {
                    html.append("<ul>\n");
                    for (EvaluationResult er : eval.getEvaluatorResults())
                    {
                        if (!er.isPassed() && er.getFindings() != null)
                        {
                            for (String finding : er.getFindings())
                            {
                                html.append("<li>[").append(escape(er.getEvaluatorName()))
                                    .append("] ").append(escape(finding)).append("</li>\n");
                            }
                        }
                    }
                    html.append("</ul>\n");
                }
                html.append("</div>\n");
            }
        }
        if (!anyFailed)
        {
            html.append("<p class=\"pass\">All cases passed!</p>\n");
        }
        html.append("</div>\n");

        // 6. Baseline comparison (only if baseline != null)
        if (baseline != null)
        {
            html.append("<div class=\"section\">\n<h2>Baseline Comparison</h2>\n");
            html.append("<div class=\"meta-grid\">\n");
            html.append(metaItem("Baseline Run", baseline.getBaselineRunId()));
            html.append(metaItem("Current Run", baseline.getCurrentRunId()));
            html.append(metaItem("Score Delta", String.format("%+.4f", baseline.getScoreDelta())));
            html.append(metaItem("Latency Delta (ms)", String.format("%+.1f", baseline.getLatencyDelta())));
            html.append("</div>\n");

            if (baseline.getNewlyFailedCases() != null && !baseline.getNewlyFailedCases().isEmpty())
            {
                html.append("<h3 class=\"fail\">Newly Failed Cases</h3>\n<ul>\n");
                for (String cid : baseline.getNewlyFailedCases())
                {
                    html.append("<li>").append(escape(cid)).append("</li>\n");
                }
                html.append("</ul>\n");
            }

            if (baseline.getRecoveredCases() != null && !baseline.getRecoveredCases().isEmpty())
            {
                html.append("<h3 class=\"pass\">Recovered Cases</h3>\n<ul>\n");
                for (String cid : baseline.getRecoveredCases())
                {
                    html.append("<li>").append(escape(cid)).append("</li>\n");
                }
                html.append("</ul>\n");
            }

            if (baseline.getNotableChanges() != null && !baseline.getNotableChanges().isEmpty())
            {
                html.append("<h3>Notable Changes</h3>\n<ul>\n");
                for (String change : baseline.getNotableChanges())
                {
                    html.append("<li>").append(escape(change)).append("</li>\n");
                }
                html.append("</ul>\n");
            }
            html.append("</div>\n");
        }

        html.append("</body>\n</html>\n");

        try (FileWriter writer = new FileWriter(outputFile))
        {
            writer.write(html.toString());
        }
    }


    private String metaItem(String label, String value)
    {
        return "<div class=\"meta-item\"><span class=\"label\">" + escape(label)
            + ":</span> <span class=\"value\">" + escape(value != null ? value : "") + "</span></div>\n";
    }

    private String scoreCard(String label, String value, String cssClass)
    {
        return "<div class=\"card " + cssClass + "\">"
            + "<div class=\"card-value\">" + escape(value) + "</div>"
            + "<div class=\"card-label\">" + escape(label) + "</div>"
            + "</div>\n";
    }

    private String escape(String text)
    {
        if (text == null)
        {
            return "";
        }
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private String getInlineCss()
    {
        return "* { box-sizing: border-box; margin: 0; padding: 0; }\n"
            + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;"
            + " background: #f5f7fa; color: #333; padding: 24px; }\n"
            + ".header { background: #1e3a5f; color: white; padding: 24px; border-radius: 8px;"
            + " margin-bottom: 24px; }\n"
            + ".header h1 { font-size: 1.8rem; margin-bottom: 16px; }\n"
            + ".meta-grid { display: flex; flex-wrap: wrap; gap: 12px; }\n"
            + ".meta-item { background: rgba(255,255,255,0.1); padding: 8px 12px;"
            + " border-radius: 4px; font-size: 0.9rem; }\n"
            + ".meta-item .label { font-weight: 600; }\n"
            + ".cards { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 24px; }\n"
            + ".card { flex: 1; min-width: 120px; padding: 20px; border-radius: 8px;"
            + " text-align: center; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n"
            + ".card-value { font-size: 2rem; font-weight: 700; }\n"
            + ".card-label { font-size: 0.85rem; color: #666; margin-top: 4px; }\n"
            + ".card.pass .card-value { color: #22863a; }\n"
            + ".card.fail .card-value { color: #cb2431; }\n"
            + ".card.neutral .card-value { color: #1e3a5f; }\n"
            + ".section { background: white; border-radius: 8px; padding: 24px;"
            + " margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n"
            + ".section h2 { font-size: 1.2rem; margin-bottom: 16px; color: #1e3a5f; }\n"
            + "table { width: 100%; border-collapse: collapse; }\n"
            + "th { background: #f0f4f8; padding: 10px 12px; text-align: left;"
            + " font-weight: 600; border-bottom: 2px solid #ddd; }\n"
            + "td { padding: 10px 12px; border-bottom: 1px solid #eee; }\n"
            + "tr:hover td { background: #f9fafb; }\n"
            + ".pass { color: #22863a; font-weight: 600; }\n"
            + ".fail { color: #cb2431; font-weight: 600; }\n"
            + ".case-detail { border-left: 4px solid #cb2431; padding-left: 16px; margin-bottom: 16px; }\n"
            + ".case-detail h3 { margin-bottom: 8px; }\n"
            + ".case-detail ul { margin-top: 8px; padding-left: 20px; }\n"
            + ".case-detail li { margin-bottom: 4px; font-size: 0.9rem; }\n";
    }
}
