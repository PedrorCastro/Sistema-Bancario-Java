void main(){
    Cliente p = new Cliente("Pedro", "455.660.458-39", "teste@teste.com");
    ContaCorrente c1 = new ContaCorrente(1, p, 5000.00);
	
	Cliente p2 = new Cliente("João", "123.456.789-00", "Teste@Teste.com.br");
	ContaPoupanca c2 = new ContaPoupanca(1, p2);
    c1.depositar(1000);
    c2.depositar(5000);
    System.out.println(c2.toString());
    try {
        c1.sacar(100);
    }
    catch (SaldoInsuficienteException e) {
        System.out.println("Erro: " + e.getMessage());
    }


    for (Transacao t: c1.getHistorico()){
        System.out.println(t);
    }
    for (Transacao t : c2.getHistorico()){
        System.out.println(t);
    }




}