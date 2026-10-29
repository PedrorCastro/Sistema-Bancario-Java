import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.model.Cliente;
import org.pedro.model.ContaPoupanca;

public class ContaPoupancaTest {

    @Test
    void deveLancarExcecaoAoDepositarValorNegativo() {
        Cliente cliente = new Cliente("Pedro","123456789-00","");
        ContaPoupanca conta = new ContaPoupanca(1, cliente);
        assertThrows(ValorInvalidoException.class, () -> conta.depositar(-1000L));
    }


    @Test 
    void deveLancarExcecaoAoSacarValorNegativo() {
        Cliente cliente = new Cliente("Pedro","123456789-00","");
        ContaPoupanca conta = new ContaPoupanca(1, cliente);
        assertThrows(ValorInvalidoException.class, () -> conta.sacar(-1000L));
    }
}