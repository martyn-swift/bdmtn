package dev.martynswift.bdmtn.player;

import dev.martynswift.bdmtn.session.BdmtnSession;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
public class Player {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    private boolean checkedIn;

    @ManyToOne
    private BdmtnSession session;

    protected Player() {
    }

    public Player(String name, boolean checkedIn) {
        this.name = name;
        this.checkedIn = checkedIn;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public BdmtnSession getSession() {
        return session;
    }

    public void setSession(BdmtnSession session) {
        this.session = session;
    }
}
