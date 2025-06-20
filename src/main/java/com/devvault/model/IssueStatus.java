package com.devvault.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public enum IssueStatus {
    OPEN, CLAIMED, CLOSED
}
