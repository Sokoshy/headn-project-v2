package com.bibliotheque.repository;

import com.bibliotheque.model.Agent;
import com.bibliotheque.model.AuditAction;
import com.bibliotheque.model.AuditLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLoanRepository extends JpaRepository<AuditLoan, Long>, JpaSpecificationExecutor<AuditLoan> {

    List<AuditLoan> findByLoanIdOrderByDateActionAsc(Long loanId);

    List<AuditLoan> findByLoanIdAndActionOrderByDateActionAsc(Long loanId, AuditAction action);

    List<AuditLoan> findByAgent(Agent agent);
}
