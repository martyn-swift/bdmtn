package dev.martynswift.bdmtn.session;

import dev.martynswift.bdmtn.player.Player;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class BdmtnSession {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String sessionId;

    private int numberOfCourts = 1;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Player> players = new ArrayList<>();

    protected BdmtnSession() {
    }

    public BdmtnSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getNumberOfCourts() {
        return numberOfCourts;
    }

    public void setNumberOfCourts(int numberOfCourts) {
        this.numberOfCourts = numberOfCourts;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setSession(this);
    }
} 