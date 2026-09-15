package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.service.reporting.PostMortemReportGenerator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final PostMortemReportGenerator reportGenerator;

    public ReportController(PostMortemReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    @GetMapping(value = "/post-mortem", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getPostMortemReport(@RequestParam(required = false) String experimentId) {
        String report = reportGenerator.generateMarkdownReport(experimentId);
        return ResponseEntity.ok(report);
    }
}
