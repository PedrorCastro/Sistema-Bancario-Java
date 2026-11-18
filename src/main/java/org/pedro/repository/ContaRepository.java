
package org.pedro.repository;

import java.util.List;
import java.util.Optional;

import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumero(Integer numero);

    List<Conta> findByTitular(Cliente titular);

    boolean existsByNumero(Integer numero);

    Optional<Conta> findByIdAndTitular_Email(Long id, String email);

    boolean existsByIdAndTitular_Email(Long id, String email);

    List<Conta> findByTitular_Email(String email);

    Optional<Conta> findByNumeroAndTitular_Email(Integer numero, String email);

    @Query(value = "select id from tb_contas where id = :id for update", nativeQuery = true)
    Optional<Long> lockRowById(@Param("id") Long id);

    @Query(value = """
            select c.id
            from tb_contas c
            join tb_clientes t on t.id = c.titular_id
            where c.id = :id and t.email = :email
            for update of c
            """, nativeQuery = true)
    Optional<Long> lockRowByIdAndTitularEmail(
            @Param("id") Long id,
            @Param("email") String email);
}
