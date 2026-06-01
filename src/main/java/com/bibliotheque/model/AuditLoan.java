package com.bibliotheque.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "audit_loans")
public class AuditLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Emprunt loan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "date_action", nullable = false, updatable = false)
    private LocalDateTime dateAction;

    public AuditLoan() {
    }

    public AuditLoan(Emprunt loan, Agent agent, AuditAction action) {
        this.loan = loan;
        this.agent = agent;
        this.action = action;
    }

    @PrePersist
    protected void onCreate() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Emprunt getLoan() {
        return loan;
    }

    public Agent getAgent() {
        return agent;
    }

    public AuditAction getAction() {
        return action;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    /**
     * Setter for test use only.
     * The field is updatable=false in JPA so this has no effect after first persist.
     */
    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLoan that = (AuditLoan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLoan{" +
                "id=" + id +
                ", loanId=" + (loan != null ? loan.getId() : null) +
                ", agentId=" + (agent != null ? agent.getId() : null) +
                ", action=" + action +
                '}';
    }
}