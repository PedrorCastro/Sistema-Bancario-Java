// Teste de carga do Sistema Bancário com k6
// Uso: k6 run -e BASE_URL=http://localhost:8080 carga.js
//
// Simula até 100 clientes usando a API ao mesmo tempo. Cada usuário virtual
// tem o próprio cadastro, login e conta, para não disputarem o mesmo
// registro (o que mediria a trava de linha, não a capacidade da API).

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const MAX_VUS = 500;
const JSON_HEADERS = { 'Content-Type': 'application/json' };

export const options = {
  setupTimeout: '5m',
  stages: [
    { duration: '30s', target: MAX_VUS }, // sobe de 0 a 100 usuários
    { duration: '1m', target: MAX_VUS },  // mantém 100 usuários
    { duration: '15s', target: 0 },       // desce até 0
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],   // menos de 1% de erros
    http_req_duration: ['p(95)<500'], // 95% das respostas em menos de 500 ms
  },
};

// Gera um CPF válido (o CPFValidator da API rejeita CPFs inválidos).
function gerarCpf() {
  const d = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
  for (const tamanho of [9, 10]) {
    let soma = 0;
    for (let i = 0; i < tamanho; i++) soma += d[i] * (tamanho + 1 - i);
    const resto = (soma * 10) % 11;
    d.push(resto === 10 ? 0 : resto);
  }
  return d.join('');
}

function auth(token) {
  return { headers: { ...JSON_HEADERS, Authorization: `Bearer ${token}` } };
}

// Executa uma vez antes do teste: cria os clientes, as contas e faz login.
// O login fica fora da carga porque o BCrypt é lento de propósito.
export function setup() {
  const execucao = Date.now();
  const baseNumeroConta = (execucao % 10000000) * 100;
  const usuarios = [];

  for (let i = 0; i < MAX_VUS; i++) {
    const email = `carga.${execucao}.${i}@teste.com`;
    const senha = 'SenhaCarga123!';

    const cadastro = http.post(`${BASE_URL}/clientes`, JSON.stringify({
      nome: `Usuario Carga ${i}`,
      cpf: gerarCpf(),
      email,
      senha,
    }), { headers: JSON_HEADERS });
    check(cadastro, { 'cadastro 201': (r) => r.status === 201 });

    const login = http.post(`${BASE_URL}/auth/login`,
      JSON.stringify({ email, senha }), { headers: JSON_HEADERS });
    if (!check(login, { 'login 200': (r) => r.status === 200 })) {
      throw new Error(`Falha no login do usuário ${i}: ${login.status} ${login.body}`);
    }
    const token = login.json('token');

    // clienteId é obrigatório no DTO, mas o service ignora para CLIENTE.
    const conta = http.post(`${BASE_URL}/contas`, JSON.stringify({
      numero: baseNumeroConta + i,
      clienteId: 1,
      tipoConta: 'CORRENTE',
      limite: 500,
      saldo: 1000,
    }), auth(token));
    if (!check(conta, { 'conta 201': (r) => r.status === 201 })) {
      throw new Error(`Falha ao criar conta do usuário ${i}: ${conta.status} ${conta.body}`);
    }

    usuarios.push({ token, contaId: conta.json('id') });
  }

  return { usuarios };
}

// Executado repetidamente por cada usuário virtual durante o teste.
export default function (data) {
  const eu = data.usuarios[(__VU - 1) % data.usuarios.length];
  const vizinho = data.usuarios[__VU % data.usuarios.length];
  const params = auth(eu.token);

  const deposito = http.post(`${BASE_URL}/transacoes/depositar`,
    JSON.stringify({ contaId: eu.contaId, valor: 10 }),
    { ...params, tags: { name: 'depositar' } });
  check(deposito, { 'depósito 200': (r) => r.status === 200 });
  sleep(1); // "tempo de pensar" de um usuário real

  const saldo = http.get(`${BASE_URL}/contas/${eu.contaId}`,
    { ...params, tags: { name: 'consultar conta' } });
  check(saldo, { 'consulta 200': (r) => r.status === 200 });
  sleep(1);

  const transferencia = http.post(`${BASE_URL}/transacoes/transferir`,
    JSON.stringify({ contaOrigemId: eu.contaId, contaDestinoId: vizinho.contaId, valor: 5 }),
    { ...params, tags: { name: 'transferir' } });
  check(transferencia, { 'transferência 200': (r) => r.status === 200 });
  sleep(1);

  const extrato = http.get(`${BASE_URL}/transacoes/conta/${eu.contaId}`,
    { ...params, tags: { name: 'extrato' } });
  check(extrato, { 'extrato 200': (r) => r.status === 200 });
  sleep(1);
}