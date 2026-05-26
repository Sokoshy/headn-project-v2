package com.bibliotheque.service;

import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.preparation.LoanPreparation;
import com.bibliotheque.model.preparation.SelectableBook;
import com.bibliotheque.model.preparation.SelectableUser;
import com.bibliotheque.repository.LivreRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanPreparationServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private LoanPreparationService loanPreparationService;

    @BeforeEach
    void setUp() {
        loanPreparationService = new LoanPreparationService(livreRepository, utilisateurRepository);
    }

    @Test
    void getPreparation_returnsOnlyAvailableBooksAndAllUsers() {
        Livre disponible = new Livre("Dune", "Frank Herbert");
        disponible.setId(1L);
        Livre emprunte = new Livre("1984", "George Orwell");
        emprunte.setId(2L);

        Utilisateur alice = new Utilisateur("Alice", "alice@example.com");
        alice.setId(1L);
        Utilisateur bob = new Utilisateur("Bob", "bob@example.com");
        bob.setId(2L);

        when(livreRepository.findDisponibles()).thenReturn(List.of(disponible));
        when(utilisateurRepository.findAll()).thenReturn(List.of(alice, bob));

        LoanPreparation preparation = loanPreparationService.getPreparation();

        assertThat(preparation.availableBooks()).hasSize(1);
        SelectableBook selectableBook = preparation.availableBooks().get(0);
        assertThat(selectableBook.id()).isEqualTo(1L);
        assertThat(selectableBook.title()).isEqualTo("Dune");
        assertThat(selectableBook.author()).isEqualTo("Frank Herbert");

        assertThat(preparation.users()).hasSize(2);
        SelectableUser firstUser = preparation.users().get(0);
        assertThat(firstUser.id()).isEqualTo(1L);
        assertThat(firstUser.name()).isEqualTo("Alice");

        assertThat(preparation.defaultExpectedReturnDate())
                .isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void getPreparation_handlesEmptyData() {
        when(livreRepository.findDisponibles()).thenReturn(List.of());
        when(utilisateurRepository.findAll()).thenReturn(List.of());

        LoanPreparation preparation = loanPreparationService.getPreparation();

        assertThat(preparation.availableBooks()).isEmpty();
        assertThat(preparation.users()).isEmpty();
        assertThat(preparation.defaultExpectedReturnDate())
                .isEqualTo(LocalDate.now().plusDays(30));
    }
}
