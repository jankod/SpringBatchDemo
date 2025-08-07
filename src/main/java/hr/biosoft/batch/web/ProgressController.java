package hr.biosoft.batch.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProgressController {

    private final List<String> progressLog;

    public ProgressController(List<String> progressLog) {
        this.progressLog = progressLog;
    }

    @GetMapping("/progress")
    public List<String> getProgress() {
        return progressLog;
    }
}
