package toubani.badr.chatbotservice.web;

import org.springframework.web.bind.annotation.*;
import toubani.badr.chatbotservice.agent.BankAgent;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final BankAgent agent;

    public ChatController(BankAgent agent) {
        this.agent = agent;
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String answer) {
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(agent.ask(request.message()));
    }

    @GetMapping
    public ChatResponse chatGet(@RequestParam String message) {
        return new ChatResponse(agent.ask(message));
    }

    @GetMapping("/tools")
    public Map<String, List<String>> tools() {
        return Map.of("tools", agent.toolNames());
    }
}
