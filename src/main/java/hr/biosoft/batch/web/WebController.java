package hr.biosoft.batch.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/jobs")
    public String jobs() {
        log.info("Accessing jobs page");
        return "jobs";
    }

    @GetMapping("/")
    public String index() {
        return "jobs";
    }
}
