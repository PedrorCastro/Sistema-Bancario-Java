// Teste de carga realista do Sistema Bancário com k6.
//
// Diferenças em relação ao carga.js:
//   - modelo de chegada aberto: define-se ações por segundo, não usuários fixos;
//   - mistura de operações parecida com a de um banco (muito mais leitura que escrita);
//   - banco pré-carregado com histórico de transações;
//   - contas "quentes" que recebem muitas transferências ao mesmo tempo;
//   - login fazendo parte do tráfego e renovação automática do token expirado;
//   - cinco perfis de teste no mesmo arquivo: smoke, carga, estresse, pico e resistencia.
//
// Uso (a partir da raiz do projeto, com a API ligada):
//   docker run --rm -i --network host grafana/k6 run \
//     -e BASE_URL=http://localhost:8080 -e PERFIL=carga \
//     - < load-tests/carga-realista.js
//
// Parâmetros opcionais (-e NOME=valor):
//   PERFIL     smoke | carga | estresse | pico | resistencia (padrão: smoke)
//   TAXA       ações por segundo no pico do perfil (cada perfil tem um padrão)
//   DURACAO    tempo na carga máxima, para smoke, carga e resistencia (ex.: 10m)
//   USUARIOS   quantidade de clientes criados na preparação (padrão: 200)
//   HISTORICO  depósitos pré-carregados por conta (padrão: 50)
//   QUENTES    quantidade de contas "quentes" (padrão: 3)

import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PERFIL = __ENV.PERFIL || 'smoke';
const TAXA = Number(__ENV.TAXA || 0);
const DURACAO = __ENV.DURACAO;
const USUARIOS = Number(__ENV.USUARIOS || 200);
const HISTORICO = Number(__ENV.HISTORICO || 50);
const QUENTES = Number(__ENV.QUENTES || 3);
const SENHA = 'SenhaCarga123!';
const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ---------------------------------------------------------------------------
// Perfis de teste
// ---------------------------------------------------------------------------

function cenario() {
  switch (PERFIL) {
    case 'smoke': // poucos acessos, só para confirmar que tudo funciona
      return {
        executor: 'constant-arrival-rate',
        rate: TAXA || 2, timeUnit: '1s', duration: DURACAO || '1m',
        preAllocatedVUs: 5, maxVUs: 20,
      };
    case 'carga': { // o tráfego esperado no dia a dia
      const alvo = TAXA || 50;
      return {
        executor: 'ramping-arrival-rate',
        startRate: 0, timeUnit: '1s', preAllocatedVUs: 50, maxVUs: 300,
        stages: [
          { duration: '2m', target: alvo },
          { duration: DURACAO || '5m', target: alvo },
          { duration: '1m', target: 0 },
        ],
      };
    }
    case 'estresse': { // sobe em degraus até passar do limite
      const alvo = TAXA || 300;
      return {
        executor: 'ramping-arrival-rate',
        startRate: 0, timeUnit: '1s', preAllocatedVUs: 100, maxVUs: 1000,
        stages: [
          { duration: '2m', target: Math.round(alvo * 0.25) },
          { duration: '2m', target: Math.round(alvo * 0.5) },
          { duration: '2m', target: Math.round(alvo * 0.75) },
          { duration: '2m', target: alvo },
          { duration: '2m', target: alvo },
          { duration: '1m', target: 0 },
        ],
      };
    }
    case 'pico': { // salto repentino, como num dia de pagamento
      const alvo = TAXA || 200;
      const base = Math.max(1, Math.round(alvo / 10));
      return {
        executor: 'ramping-arrival-rate',
        startRate: 0, timeUnit: '1s', preAllocatedVUs: 100, maxVUs: 1000,
        stages: [
          { duration: '1m', target: base },
          { duration: '2m', target: base },
          { duration: '20s', target: alvo },
          { duration: '1m', target: alvo },
          { duration: '20s', target: base },
          { duration: '2m', target: base },
          { duration: '30s', target: 0 },
        ],
      };
    }
    case 'resistencia': // carga moderada por muito tempo
      return {
        executor: 'constant-arrival-rate',
        rate: TAXA || 30, timeUnit: '1s', duration: DURACAO || '30m',
        preAllocatedVUs: 50, maxVUs: 300,
      };
    default:
      throw new Error(`PERFIL inválido: ${PERFIL}. Use smoke, carga, estresse, pico ou resistencia.`);
  }
}

function limites() {
  const t = {
    // As respostas de cada operação aparecem separadas no resumo.
    'http_req_duration{name:consultar}': ['p(95)<300', 'p(99)<800'],
    'http_req_duration{name:extrato}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:extrato periodo}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:transferir}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:depositar}': ['p(95)<300', 'p(99)<800'],
    'http_req_duration{name:sacar}': ['p(95)<300', 'p(99)<800'],
    'http_req_duration{name:login}': ['p(95)<1000'], // BCrypt é lento de propósito
    'http_req_failed{fase:teste}': ['rate<0.01'],
  };
  // No estresse e no pico, iterações descartadas são o resultado esperado ao passar do limite.
  if (PERFIL !== 'estresse' && PERFIL !== 'pico') {
    t.dropped_iterations = ['count<10'];
  }
  return t;
}

export const options = {
  setupTimeout: '20m',
  batchPerHost: 20, // paralelismo das requisições em lote da preparação
  scenarios: { [PERFIL]: cenario() },
  thresholds: limites(),
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// ---------------------------------------------------------------------------
// Auxiliares
// ---------------------------------------------------------------------------

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

function aleatorio(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function paramsSetup(token) {
  const headers = token ? { ...JSON_HEADERS, Authorization: `Bearer ${token}` } : JSON_HEADERS;
  return { headers, tags: { name: 'preparacao', fase: 'preparacao' } };
}

// Envia as requisições em lotes paralelos para a preparação não demorar demais.
function emLotes(requisicoes, tamanho) {
  const respostas = [];
  for (let i = 0; i < requisicoes.length; i += tamanho) {
    respostas.push(...http.batch(requisicoes.slice(i, i + tamanho)));
  }
  return respostas;
}

function dataLocal(diasAtras) {
  const d = new Date(Date.now() - diasAtras * 24 * 60 * 60 * 1000);
  return d.toISOString().slice(0, 19); // formato aceito pelo LocalDateTime da API
}

// ---------------------------------------------------------------------------
// Preparação: cria clientes, contas e histórico antes do teste
// ---------------------------------------------------------------------------

export function setup() {
  const execucao = Date.now();
  const baseNumero = (execucao % 1000000) * 1000;
  const emails = Array.from({ length: USUARIOS }, (_, i) => `realista.${execucao}.${i}@teste.com`);

  console.log(`Preparando ${USUARIOS} clientes...`);
  const cadastros = emLotes(emails.map((email, i) => ['POST', `${BASE_URL}/clientes`,
    JSON.stringify({ nome: `Cliente Realista ${i}`, cpf: gerarCpf(), email, senha: SENHA }),
    paramsSetup()]), 10);
  const falhasCadastro = cadastros.filter((r) => r.status !== 201).length;
  if (falhasCadastro > 0) {
    throw new Error(`${falhasCadastro} cadastros falharam. Primeiro erro: ${cadastros.find((r) => r.status !== 201).body}`);
  }

  const logins = emLotes(emails.map((email) => ['POST', `${BASE_URL}/auth/login`,
    JSON.stringify({ email, senha: SENHA }), paramsSetup()]), 10);
  const tokens = logins.map((r) => (r.status === 200 ? r.json('token') : null));
  if (tokens.includes(null)) {
    throw new Error('Falha no login de algum cliente durante a preparação.');
  }

  console.log('Criando contas...');
  const contas = emLotes(tokens.map((token, i) => ['POST', `${BASE_URL}/contas`,
    JSON.stringify({ numero: baseNumero + i, clienteId: 1, tipoConta: 'CORRENTE', limite: 1000, saldo: 5000 }),
    paramsSetup(token)]), 20);
  if (contas.some((r) => r.status !== 201)) {
    throw new Error(`Falha ao criar conta: ${contas.find((r) => r.status !== 201).body}`);
  }
  const contaIds = contas.map((r) => r.json('id'));

  if (HISTORICO > 0) {
    console.log(`Pré-carregando ${HISTORICO} transações por conta (${USUARIOS * HISTORICO} no total)...`);
    const depositos = [];
    for (let i = 0; i < USUARIOS; i++) {
      for (let j = 0; j < HISTORICO; j++) {
        depositos.push(['POST', `${BASE_URL}/transacoes/depositar`,
          JSON.stringify({ contaId: contaIds[i], valor: aleatorio(1, 100) }),
          paramsSetup(tokens[i])]);
      }
    }
    const falhasHistorico = emLotes(depositos, 50).filter((r) => r.status !== 200).length;
    if (falhasHistorico > 0) {
      console.warn(`${falhasHistorico} depósitos do histórico falharam.`);
    }
  }

  const usuarios = emails.map((email, i) => ({ email, token: tokens[i], contaId: contaIds[i] }));
  const quentes = contaIds.slice(0, Math.min(QUENTES, USUARIOS));
  console.log(`Preparação concluída. Iniciando perfil "${PERFIL}".`);
  return { usuarios, quentes };
}

// ---------------------------------------------------------------------------
// Tráfego
// ---------------------------------------------------------------------------

// Tokens renovados ficam guardados por usuário virtual (cada um tem sua própria memória).
const tokensRenovados = {};

function fazerLogin(usuario, indice) {
  const res = http.post(`${BASE_URL}/auth/login`,
    JSON.stringify({ email: usuario.email, senha: SENHA }),
    { headers: JSON_HEADERS, tags: { name: 'login', fase: 'teste' } });
  check(res, { 'login 200': (r) => r.status === 200 });
  if (res.status === 200) {
    tokensRenovados[indice] = res.json('token');
  }
  return res;
}

// Faz a chamada com o token atual; se ele tiver expirado (401), faz login e tenta de novo.
function chamar(usuario, indice, nome, requisicao) {
  const params = (token) => ({
    headers: { ...JSON_HEADERS, Authorization: `Bearer ${token}` },
    tags: { name: nome, fase: 'teste' },
  });

  let res = requisicao(params(tokensRenovados[indice] || usuario.token));
  if (res.status === 401 && fazerLogin(usuario, indice).status === 200) {
    res = requisicao(params(tokensRenovados[indice]));
  }
  check(res, { [`${nome} 200`]: (r) => r.status === 200 });
  return res;
}

function escolherDestino(data, origemId) {
  // 40% das transferências vão para as contas quentes (ex.: comerciantes).
  if (data.quentes.length > 0 && Math.random() < 0.4) {
    const destino = data.quentes[aleatorio(0, data.quentes.length - 1)];
    if (destino !== origemId) return destino;
  }
  let destino;
  do {
    destino = data.usuarios[aleatorio(0, data.usuarios.length - 1)].contaId;
  } while (destino === origemId);
  return destino;
}

export default function (data) {
  const indice = aleatorio(0, data.usuarios.length - 1);
  const usuario = data.usuarios[indice];
  const conta = usuario.contaId;
  const sorteio = Math.random() * 100;

  if (sorteio < 50) {
    // 50%: consultar saldo (metade lista as contas, metade abre uma conta)
    if (Math.random() < 0.5) {
      chamar(usuario, indice, 'consultar', (p) => http.get(`${BASE_URL}/contas/minhas`, p));
    } else {
      chamar(usuario, indice, 'consultar', (p) => http.get(`${BASE_URL}/contas/${conta}`, p));
    }
  } else if (sorteio < 75) {
    // 25%: extrato (histórico completo ou últimos 30 dias)
    if (Math.random() < 0.5) {
      chamar(usuario, indice, 'extrato', (p) => http.get(`${BASE_URL}/transacoes/conta/${conta}`, p));
    } else {
      const url = `${BASE_URL}/transacoes/extrato?contaId=${conta}&dataInicio=${dataLocal(30)}&dataFim=${dataLocal(-1)}`;
      chamar(usuario, indice, 'extrato periodo', (p) => http.get(url, p));
    }
  } else if (sorteio < 87) {
    // 12%: transferência
    const corpo = JSON.stringify({
      contaOrigemId: conta, contaDestinoId: escolherDestino(data, conta), valor: aleatorio(1, 50),
    });
    chamar(usuario, indice, 'transferir', (p) => http.post(`${BASE_URL}/transacoes/transferir`, corpo, p));
  } else if (sorteio < 95) {
    // 8%: depósito ou saque
    const corpo = JSON.stringify({ contaId: conta, valor: aleatorio(1, 30) });
    if (Math.random() < 0.5) {
      chamar(usuario, indice, 'depositar', (p) => http.post(`${BASE_URL}/transacoes/depositar`, corpo, p));
    } else {
      chamar(usuario, indice, 'sacar', (p) => http.post(`${BASE_URL}/transacoes/sacar`, corpo, p));
    }
  } else {
    // 5%: login (pessoas abrindo o app)
    fazerLogin(usuario, indice);
  }
}

