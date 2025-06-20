package com.devvault.Controller;

import com.devvault.dto.IssueDTO;
import com.devvault.exception.ResourceNotFoundException;
import com.devvault.model.Difficulty;
import com.devvault.model.Issue;
import com.devvault.model.IssueStatus;
import com.devvault.model.User;
import com.devvault.repository.IssueRepository;
import com.devvault.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/issues")
@Slf4j
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔐 Create new issue - auto assign to logged-in user (USER role)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<Issue> createIssue(@Valid @RequestBody IssueDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating issue for user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        Issue issue = new Issue();
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setDifficulty(dto.getDifficulty());
        issue.setStatus(IssueStatus.CLAIMED); // default on creation
        issue.setAssignedTo(user);

        Issue savedIssue = issueRepository.save(issue);
        log.info("Issue created with ID: {}", savedIssue.getId());
        return ResponseEntity.ok(savedIssue);
    }


    // ✅ Filter + Pagination: GET /issues/filter?status=OPEN&difficulty=EASY&page=0&size=5
    @GetMapping("/filter")
    public ResponseEntity<Page<Issue>> filterIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        log.info("Filtering issues: status={}, difficulty={}, page={}, size={}", status, difficulty, page, size);

        if (status != null && difficulty != null) {
            return ResponseEntity.ok(issueRepository.findByStatusIgnoreCaseAndDifficultyIgnoreCase(status, difficulty, pageable));
        } else if (status != null) {
            return ResponseEntity.ok(issueRepository.findByStatusIgnoreCase(status, pageable));
        } else if (difficulty != null) {
            return ResponseEntity.ok(issueRepository.findByDifficultyIgnoreCase(difficulty, pageable));
        } else {
            return ResponseEntity.ok(issueRepository.findAll(pageable));
        }
    }

    // 🔓 Get issue by ID - public
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long id) {
        log.info("Fetching issue with ID: {}", id);
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Issue not found with ID: {}", id);
                    return new ResourceNotFoundException("Issue not found with ID: " + id);
                });
        return ResponseEntity.ok(issue);
    }

    // 🔐 Assign issue to any user - only ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{issueId}/assign/{userId}")
    public ResponseEntity<Issue> assignIssueToUser(@PathVariable Long issueId, @PathVariable Long userId) {
        log.info("Assigning issue ID {} to user ID {}", issueId, userId);

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> {
                    log.warn("Issue not found with ID: {}", issueId);
                    return new ResourceNotFoundException("Issue not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        issue.setAssignedTo(user);
        issue.setStatus(IssueStatus.CLAIMED);
        Issue updated = issueRepository.save(issue);
        log.info("Issue ID {} assigned to user ID {}", issueId, userId);
        return ResponseEntity.ok(updated);
    }

    // 🔐 Update issue - only assigned user or ADMIN
    @PreAuthorize("hasRole('ADMIN') or @issueSecurity.isOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long id, @Valid @RequestBody IssueDTO dto) {
        log.info("Updating issue ID {}", id);

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Issue not found with ID: {}", id);
                    return new ResourceNotFoundException("Issue not found");
                });

        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setDifficulty(dto.getDifficulty());

        IssueStatus prevStatus = issue.getStatus();
        IssueStatus newStatus = dto.getStatus();

        if (prevStatus != IssueStatus.CLOSED && newStatus == IssueStatus.CLOSED) {
            issue.setStatus(IssueStatus.CLOSED);

            User assignee = issue.getAssignedTo();
            if (assignee != null) {
                int reward = switch (issue.getDifficulty()) {
                    case EASY -> 10;
                    case MEDIUM -> 20;
                    case HARD -> 30;
                };
                assignee.setRewardPoints(assignee.getRewardPoints() + reward);
                userRepository.save(assignee);
                log.info("Issue closed. Reward {} points to user {}", reward, assignee.getId());
            }
        } else {
            issue.setStatus(newStatus);
        }

        Issue saved = issueRepository.save(issue);
        log.info("Issue ID {} updated successfully", id);
        return ResponseEntity.ok(saved);
    }


    // 🔐 Delete issue - only assigned user or ADMIN
    @PreAuthorize("hasRole('ADMIN') or @issueSecurity.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteIssue(@PathVariable Long id) {
        log.info("Deleting issue ID: {}", id);
        if (!issueRepository.existsById(id)) {
            log.warn("Issue not found with ID: {}", id);
            throw new ResourceNotFoundException("Issue not found with ID: " + id);
        }
        issueRepository.deleteById(id);
        log.info("Issue ID {} deleted", id);
        return ResponseEntity.ok("Issue deleted successfully.");
    }
}
