package dev.martynswift.bdmtn.player.web;

import dev.martynswift.bdmtn.player.Player;
import dev.martynswift.bdmtn.player.PlayerNotFoundException;
import dev.martynswift.bdmtn.player.PlayerRepository;
import dev.martynswift.bdmtn.session.BdmtnSession;
import dev.martynswift.bdmtn.session.BdmtnSessionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/players")
public class PlayerController {

    private final PlayerRepository repository;
    private final BdmtnSessionRepository sessionRepository;

    public PlayerController(PlayerRepository repository, BdmtnSessionRepository sessionRepository) {
        this.repository = repository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping(params = "sessionId")
    public String index(@RequestParam String sessionId, Model model) {
        BdmtnSession session = getOrCreateSession(sessionId);
        addAttributesForIndex(model, ListFilter.ALL, session);
        return "players";
    }

    @GetMapping(value = "/checked-in", params = "sessionId")
    public String indexCompleted(@RequestParam String sessionId, Model model) {
        BdmtnSession session = getOrCreateSession(sessionId);
        addAttributesForIndex(model, ListFilter.CHECKED_IN, session);
        return "players";
    }

    @PostMapping(params = "sessionId")
    public String addNewPlayer(@RequestParam String sessionId, @Valid @ModelAttribute("player") PlayerFormData formData) {
        BdmtnSession session = getOrCreateSession(sessionId);
        Player player = new Player(formData.getName(), false);
        session.addPlayer(player);
        repository.save(player);

        return "redirect:/players?sessionId=" + sessionId;
    }

    @PutMapping(value = "/{id}/toggle", params = "sessionId")
    public String toggleSelection(@RequestParam String sessionId, @PathVariable("id") Long id) {
        Player player = repository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));

        player.setCheckedIn(!player.isCheckedIn());
        repository.save(player);
        return "redirect:/players?sessionId=" + sessionId;
    }

    @PutMapping(value = "/toggle-all", params = "sessionId")
    public String toggleAll(@RequestParam String sessionId) {
        BdmtnSession session = getOrCreateSession(sessionId);
        List<Player> players = session.getPlayers();
        for (Player player : players) {
            player.setCheckedIn(!player.isCheckedIn());
            repository.save(player);
        }
        return "redirect:/players?sessionId=" + sessionId;
    }

    @DeleteMapping(value = "/{id}", params = "sessionId")
    public String deletePlayer(@RequestParam String sessionId, @PathVariable("id") Long id) {
        repository.deleteById(id);
        return "redirect:/players?sessionId=" + sessionId;
    }

    @DeleteMapping(value = "/checked-in", params = "sessionId")
    public String deleteCheckedInPlayers(@RequestParam String sessionId) {
        BdmtnSession session = getOrCreateSession(sessionId);
        List<Player> players = session.getPlayers().stream()
                .filter(Player::isCheckedIn)
                .collect(Collectors.toList());
        for (Player player : players) {
            repository.deleteById(player.getId());
        }
        return "redirect:/players?sessionId=" + sessionId;
    }

    @PostMapping(headers = "HX-Request", params = "sessionId")
    public String htmxAddPlayer(@RequestParam String sessionId, PlayerFormData formData,
                              Model model, HttpServletResponse response) {
        BdmtnSession session = getOrCreateSession(sessionId);
        Player player = new Player(formData.getName(), false);
        session.addPlayer(player);
        repository.save(player);
        model.addAttribute("player", toDto(player));
        model.addAttribute("sessionId", sessionId);

        response.setHeader("HX-Trigger", "playerAdded");
        return "fragments :: player";
    }

    @PutMapping(value = "/{id}/toggle", headers = "HX-Request", params = "sessionId")
    public String htmxTogglePlayer(@RequestParam String sessionId, @PathVariable("id") Long id,
                                 Model model, HttpServletResponse response) {
        Player player = repository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));

        player.setCheckedIn(!player.isCheckedIn());
        repository.save(player);

        model.addAttribute("player", toDto(player));
        model.addAttribute("sessionId", sessionId);

        response.setHeader("HX-Trigger", "playerCheckedInToggled");
        return "fragments :: player";
    }

    @DeleteMapping(value = "/{id}", headers = "HX-Request", params = "sessionId")
    @ResponseBody
    public String htmxDeletePlayer(@RequestParam String sessionId, @PathVariable("id") Long id,
                                 HttpServletResponse response) {
        repository.deleteById(id);

        response.setHeader("HX-Trigger", "playerDeleted");
        return "";
    }

    @GetMapping(value = "/checkedin-players-count", headers = "HX-Request", params = "sessionId")
    public String htmxCheckedInPlayersCount(@RequestParam String sessionId, Model model) {
        BdmtnSession session = getOrCreateSession(sessionId);
        model.addAttribute("numberOfCheckedInPlayers", getNumberOfCheckedInPlayers(session));

        return "fragments :: checkedin-players-count";
    }

    private void addAttributesForIndex(Model model, ListFilter listFilter, BdmtnSession session) {
        model.addAttribute("player", new PlayerFormData());
        model.addAttribute("filter", listFilter);
        model.addAttribute("players", getPlayers(listFilter, session));
        model.addAttribute("totalNumberOfPlayers", session.getPlayers().size());
        model.addAttribute("numberOfCheckedInPlayers", getNumberOfCheckedInPlayers(session));
        model.addAttribute("sessionId", session.getSessionId());
    }

    private List<PlayerDto> getPlayers(ListFilter filter, BdmtnSession session) {
        return switch (filter) {
            case ALL -> convertToDto(session.getPlayers());
            case CHECKED_IN -> convertToDto(session.getPlayers().stream()
                    .filter(Player::isCheckedIn)
                    .collect(Collectors.toList()));
        };
    }

    private List<PlayerDto> convertToDto(List<Player> players) {
        return players
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PlayerDto toDto(Player player) {
        return new PlayerDto(player.getId(),
                player.getName(),
                player.isCheckedIn());
    }

    private int getNumberOfCheckedInPlayers(BdmtnSession session) {
        return (int) session.getPlayers().stream()
                .filter(Player::isCheckedIn)
                .count();
    }

    private BdmtnSession getOrCreateSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    BdmtnSession newSession = new BdmtnSession(sessionId);
                    return sessionRepository.save(newSession);
                });
    }

    public static record PlayerDto(long id, String name, boolean checkedIn) {
    }

    public enum ListFilter {
        ALL,
        CHECKED_IN
    }
}
