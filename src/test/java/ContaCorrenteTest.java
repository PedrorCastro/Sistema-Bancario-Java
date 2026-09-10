import org.junit.jupiter.api.Test;
import org.pedro.*;
import static org.junit.jupiter.api.Assertions.*;

public class ContaCorreteTest {


    @Test
    void deveLancarExcecaoAoDepositarValorNegativo() {
        Cliente cliente = new Cliente("Pedro","123456789-00","teste@teste.com");
        ContaCorrente conta = new ContaCorrente(1, cliente, 5000.00);

        ValorInvalidoException excecao = assertThrows(ValorInvalidoException.class, () -> {conta.depositar(-100.0);});

        assertEquals("Transação invalido", excecao.getMessage());

    }
}

