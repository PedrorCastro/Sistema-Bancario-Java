import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.model.Cliente;
import org.pedro.model.ContaCorrente;

public class ContaCorrenteTest {


    @Test
    void deveLancarExcecaoAoDepositarValorNegativo() {
        Cliente cliente = new Cliente("Pedro","123456789-00","teste@teste.com");
        ContaCorrente conta = new ContaCorrente(1, cliente, 5000.00);

        ValorInvalidoException excecao = assertThrows(ValorInvalidoException.class, () -> {conta.depositar(-100L);});

        // [ALTERADO] A mensagem da exceção foi corrigida ortograficamente ("Transação inválida")
        assertTrue(excecao.getMessage().contains("Transação inválida"));

    }
}

