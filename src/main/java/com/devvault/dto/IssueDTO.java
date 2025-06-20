package com.devvault.dto;

import com.devvault.model.Difficulty;
import com.devvault.model.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty must be provided")
    private Difficulty difficulty;

    @NotNull(message = "Status must be provided")
    private IssueStatus status;
}
