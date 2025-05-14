package dev.martynswift.bdmtn.player.web;

import javax.validation.constraints.NotBlank;

public class PlayerFormData {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
