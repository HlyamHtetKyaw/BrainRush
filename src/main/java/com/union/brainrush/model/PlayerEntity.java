package com.union.brainrush.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "players")
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    private int mark;

    private LocalDateTime createdAt;

    public PlayerEntity() {
        this.uuid = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.mark = 0;
    }

    // Getters and Setters
    public String getUuid() { return uuid; }
    public int getMark() { return mark; }
    public void setMark(int mark) { this.mark = mark; }
    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
