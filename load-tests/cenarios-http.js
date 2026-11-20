// Verificação dos códigos HTTP da API com os três tipos de usuário, usando k6.
// Não é teste de carga: roda uma única vez e confere a resposta de cada cenário.
//
// Uso (a partir da raiz do projeto, com a API ligada):
//   docker run --rm -i --network host grafana/k6 run \
//     -e BASE_URL=http://localhost:8080 \
//     -e ADMIN_EMAIL=admin@teste.com -e ADMIN_SENHA=SuaSenhaAdmin \
//     - < load-tests/cenarios-http.js
//
// ADMIN_EMAIL e ADMIN_SENHA são opcionais. Sem eles, os cenários de ADMIN são pulados.
// O 500 não aparece aqui: a API não tem como gerar um erro interno de propósito.
// Ele é verificado no teste JUnit ErroInternoTests.

import http from 'k6/http';
import { check, group } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JSON_HEADERS = { 'Content-Type': 'application/json' };
const ID_INEXISTENTE = 999999999;

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    checks: ['rate==1.0'], // qualquer cenário com status inesperado reprova a execução
  },
};

// Aqui os 4xx são esperados, então não devem contar como falha de requisição.
http.setResponseCallback(http.expectedStatuses({ min: 200, max: 599 }));

function gerarCpf() {
  const d = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
  for (const tamanho of [9, 10]) {
    let soma = 0;
    for (let i = 0; i < tamanho; i++) soma += d[i] * (tamanho + 1 - i);
    const resto = soma % 11;
    d.push(resto < 2 ? 0 : 11 - resto);
  }
  return d.join('');
}

function esperar(res, status, descricao) {
  const nome = `[${status}] ${descricao}`;
  const ok = check(res, { [nome]: (r) => r.status === status });
  if (!ok) {
    console.error(`${nome} -> recebido ${res.status}: ${String(res.body).slice(0, 200)}`);
  }
  return ok;
}

function comToken(token, extra = {}) {
  return { headers: { ...JSON_HEADERS, Authorization: `Bearer ${token}`, ...extra } };
}

function postJson(caminho, corpo, params = { headers: JSON_HEADERS }) {
  return http.post(`${BASE_URL}${caminho}`, JSON.stringify(corpo), params);
}

function login(email, senha) {
  const res = postJson('/auth/login', { email, senha });
  return res.status === 200 ? res.json('token') : null;
}

export default function () {
  const execucao = Date.now();
  const senha = 'SenhaForte123';
  const a = { email: `cenario.a.${execucao}@teste.com`, cpf: gerarCpf() };
  const b = { email: `cenario.b.${execucao}@teste.com`, cpf: gerarCpf() };
  const numeroBase = (execucao % 10000000) * 100;

  group('Anônimo', () => {
    esperar(postJson('/clientes', { nome: 'Cliente A', cpf: a.cpf, email: a.email, senha }),
      201, 'cadastro de cliente');
    esperar(postJson('/clientes', { nome: 'Cliente B', cpf: b.cpf, email: b.email, senha }),
      201, 'cadastro de outro cliente');

    esperar(postJson('/clientes', { nome: 'Repetido', cpf: a.cpf, email: `x.${execucao}@teste.com`, senha }),
      400, 'cadastro com CPF já existente');
    esperar(postJson('/clientes', { nome: 'X', cpf: '12345678900', email: 'nao-e-email', senha: 'curta' }),
      400, 'cadastro com campos inválidos');
    esperar(http.post(`${BASE_URL}/clientes`, '{"nome": "sem fechar', { headers: JSON_HEADERS }),
      400, 'JSON mal formatado');

    esperar(postJson('/auth/login', { email: 'nao-e-email', senha: '' }),
      400, 'login com corpo inválido');
    esperar(postJson('/auth/login', { email: a.email, senha: 'SenhaErrada999' }),
      401, 'login com senha errada');
    esperar(postJson('/auth/login', { email: `ninguem.${execucao}@teste.com`, senha }),
      401, 'login com e-mail inexistente');

    esperar(http.get(`${BASE_URL}/contas/minhas`), 401, 'rota protegida sem token');
    esperar(http.get(`${BASE_URL}/contas/minhas`, comToken('token-invalido')),
      401, 'rota protegida com token inválido');
  });

  const tokenA = login(a.email, senha);
  const tokenB = login(b.email, senha);
  if (!check(null, { 'login dos clientes de teste': () => tokenA !== null && tokenB !== null })) {
    return;
  }

  let contaA;
  let contaB;

  group('CLIENTE', () => {
    const criacaoA = postJson('/contas',
      { numero: numeroBase + 1, clienteId: 1, tipoConta: 'CORRENTE', limite: 0, saldo: 100 },
      comToken(tokenA));
    esperar(criacaoA, 201, 'criar conta corrente');
    contaA = criacaoA.json('id');

    const criacaoB = postJson('/contas',
      { numero: numeroBase + 2, clienteId: 1, tipoConta: 'POUPANCA', saldo: 100 },
      comToken(tokenB));
    esperar(criacaoB, 201, 'outro cliente cria conta poupança');
    contaB = criacaoB.json('id');

    esperar(postJson('/contas', { numero: numeroBase + 1, clienteId: 1, tipoConta: 'CORRENTE' }, comToken(tokenA)),
      400, 'criar conta com número repetido');
    esperar(postJson('/contas', { numero: numeroBase + 3, clienteId: 1, tipoConta: 'INVESTIMENTO' }, comToken(tokenA)),
      400, 'criar conta com tipo inexistente');

    esperar(http.get(`${BASE_URL}/clientes/me`, comToken(tokenA)), 200, 'consultar os próprios dados');
    esperar(http.get(`${BASE_URL}/contas/minhas`, comToken(tokenA)), 200, 'listar as próprias contas');
    esperar(http.get(`${BASE_URL}/contas/${contaA}`, comToken(tokenA)), 200, 'consultar a própria conta');
    esperar(postJson('/transacoes/depositar', { contaId: contaA, valor: 10 }, comToken(tokenA)),
      200, 'depositar na própria conta');
    esperar(http.get(`${BASE_URL}/transacoes/conta/${contaA}`, comToken(tokenA)), 200, 'ver o próprio histórico');

    esperar(postJson('/transacoes/depositar', { contaId: contaA, valor: -5 }, comToken(tokenA)),
      400, 'depósito com valor negativo');
    esperar(postJson('/transacoes/sacar', { contaId: contaA, valor: 100000 }, comToken(tokenA)),
      400, 'saque maior que saldo e limite');
    esperar(postJson('/transacoes/transferir', { contaOrigemId: contaA, contaDestinoId: contaA, valor: 1 }, comToken(tokenA)),
      400, 'transferência para a própria conta');
    esperar(http.get(`${BASE_URL}/transacoes/extrato?contaId=${contaA}`, comToken(tokenA)),
      400, 'extrato sem as datas obrigatórias');
    esperar(http.get(`${BASE_URL}/transacoes/extrato?contaId=${contaA}&dataInicio=2027-01-01T00:00:00&dataFim=2026-01-01T00:00:00`, comToken(tokenA)),
      400, 'extrato com período invertido');
    esperar(http.get(`${BASE_URL}/contas/abc`, comToken(tokenA)), 400, 'ID de conta que não é número');

    esperar(http.get(`${BASE_URL}/contas/${contaB}`, comToken(tokenA)), 404, 'consultar conta de outro cliente');
    esperar(http.get(`${BASE_URL}/transacoes/conta/${contaB}`, comToken(tokenA)), 404, 'ver histórico de outro cliente');
    esperar(http.get(`${BASE_URL}/rota-que-nao-existe`, comToken(tokenA)), 404, 'rota inexistente');

    esperar(http.get(`${BASE_URL}/admin/clientes`, comToken(tokenA)), 403, 'CLIENTE na área de admin');
    esperar(http.get(`${BASE_URL}/contas`, comToken(tokenA)), 403, 'CLIENTE listando todas as contas');
    esperar(http.get(`${BASE_URL}/clientes/1`, comToken(tokenA)), 403, 'CLIENTE buscando cliente por ID');

    esperar(http.put(`${BASE_URL}/contas`, null, comToken(tokenA)), 405, 'método não suportado');
    esperar(http.post(`${BASE_URL}/transacoes/depositar`, 'contaId=1&valor=10', comToken(tokenA, { 'Content-Type': 'text/plain' })),
      415, 'corpo em texto em vez de JSON');

    esperar(http.del(`${BASE_URL}/contas/${contaA}`, null, comToken(tokenA)), 204, 'encerrar a própria conta');
    esperar(postJson('/transacoes/depositar', { contaId: contaA, valor: 10 }, comToken(tokenA)),
      409, 'depositar em conta encerrada');
    esperar(postJson('/transacoes/transferir', { contaOrigemId: contaB, contaDestinoId: contaA, valor: 1 }, comToken(tokenB)),
      409, 'transferir para conta encerrada');
  });

  group('ADMIN', () => {
    if (!__ENV.ADMIN_EMAIL || !__ENV.ADMIN_SENHA) {
      console.warn('ADMIN_EMAIL/ADMIN_SENHA não informados: cenários de ADMIN pulados.');
      return;
    }
    const tokenAdmin = login(__ENV.ADMIN_EMAIL, __ENV.ADMIN_SENHA);
    if (!check(null, { 'login do ADMIN': () => tokenAdmin !== null })) {
      return;
    }

    esperar(http.get(`${BASE_URL}/admin/clientes`, comToken(tokenAdmin)), 200, 'ADMIN lista clientes');
    esperar(http.get(`${BASE_URL}/contas`, comToken(tokenAdmin)), 200, 'ADMIN lista todas as contas');
    esperar(http.get(`${BASE_URL}/contas/${contaB}`, comToken(tokenAdmin)), 200, 'ADMIN consulta conta de qualquer cliente');
    esperar(http.get(`${BASE_URL}/contas/${ID_INEXISTENTE}`, comToken(tokenAdmin)), 404, 'ADMIN busca conta inexistente');
    esperar(http.get(`${BASE_URL}/clientes/${ID_INEXISTENTE}`, comToken(tokenAdmin)), 404, 'ADMIN busca cliente inexistente');
    esperar(http.get(`${BASE_URL}/clientes/me`, comToken(tokenAdmin)), 403, 'ADMIN em /clientes/me (regra atual)');
  });
}
