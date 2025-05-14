package dev.martynswift.bdmtn.session.web;

import dev.martynswift.bdmtn.session.BdmtnSession;
import dev.martynswift.bdmtn.session.Court;
import dev.martynswift.bdmtn.session.BdmtnSessionRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class SessionController {
    private static final Logger log = LoggerFactory.getLogger(SessionController.class);
    private final BdmtnSessionRepository sessionRepository;

    public SessionController(BdmtnSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/")
    public String createNewSession() {
        String sessionId = UUID.randomUUID().toString();
        BdmtnSession session = new BdmtnSession(sessionId);
        sessionRepository.save(session);
        return "redirect:/players?sessionId=" + sessionId;
    }

    @GetMapping("/courts")
    public String courts(@RequestParam String sessionId, Model model) {
        BdmtnSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        model.addAttribute("session", session);
        model.addAttribute("sessionId", sessionId);
        return "courts";
    }

    @PostMapping("/courts")
    public String updateNumberOfCourts(@RequestParam String sessionId, @RequestParam int numberOfCourts) {
        BdmtnSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        log.info("number of courts: {}", numberOfCourts);
        session.setNumberOfCourts(numberOfCourts);
        sessionRepository.save(session);
        return "redirect:/play?sessionId=" + sessionId;
    }

    @GetMapping("/play")
    public String play(@RequestParam String sessionId, Model model) {
        BdmtnSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        log.info("Session {} has {} courts", sessionId, session.getNumberOfCourts());
        if (session.getNumberOfCourts() < 1) {
            return "redirect:/courts?sessionId=" + sessionId;
        }
        List<Court> courts = new ArrayList<>();
        for (int i = 1; i <= session.getNumberOfCourts(); i++) {
            courts.add(new Court(i));
        }
        model.addAttribute("courts", courts);
        model.addAttribute("session", session);
        model.addAttribute("sessionId", sessionId);
        return "play";
    }
} 