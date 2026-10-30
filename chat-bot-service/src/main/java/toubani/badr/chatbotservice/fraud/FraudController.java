package toubani.badr.chatbotservice.fraud;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final FraudDetectionService service;

    public FraudController(FraudDetectionService service) {
        this.service = service;
    }

    @PostMapping("/score")
    public FraudDetectionService.FraudVerdict score(@RequestBody FraudDetectionService.Tx tx) {
        return service.score(tx);
    }

    @GetMapping("/evaluate")
    public FraudDetectionService.Evaluation evaluate(@RequestParam(defaultValue = "10") int n) {
        return service.evaluate(Math.min(n, 40));
    }

    @GetMapping("/sample")
    public List<FraudDetectionService.Tx> sample(@RequestParam(defaultValue = "5") int n) {
        return service.sample(n);
    }
}
