package com.devvault.model;

import jakarta.persistence.*;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String difficulty; // EASY, MEDIUM, HARD
    private String status;     // OPEN, CLAIMED, CLOSED

    @ManyToOne
    @JoinColumn(name = "user_id")  // FK to User table
    private User assignedTo;

    // Constructors
    public Issue() {
    }

    public Issue(String title, String description, String difficulty, String status, User assignedTo) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.status = status;
        this.assignedTo = assignedTo;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
}
