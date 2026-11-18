
package org.pedro.repository;

import java.util.List;
import java.util.Optional;

import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumero(Integer numero);

    List<Conta> findByTitular(Cliente titular);

    boolean existsByNumero(Integer numero);

    Optional<Conta> findByIdAndTitular_Email(Long id, String email);

    List<Conta> findByTitular_Email(String email);

    Optional<Conta> findByNumeroAndTitular_Email(Integer numero, String email);
}
