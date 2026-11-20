// 1. Pegar os elementos do HTML (agora com querySelector)
const form = document.querySelector("#form");
const inputNome = document.querySelector("#nome");
const selectTipo = document.querySelector("#tipo");
const inputValor = document.querySelector("#valor");
const lista = document.querySelector("#lista");
const vazio = document.querySelector("#vazio");
const total = document.querySelector("#total");
const quantidade = document.querySelector("#quantidade");
const maior = document.querySelector("#maior");

// Objeto usado como "dicionário": valor do select -> texto bonito
const NOMES_TIPO = {
  "acao": "Ação",
  "fii": "FII",
  "renda-fixa": "Renda Fixa",
  "cripto": "Cripto",
};

// 2. Dados
const ativos = [];

// Função pequena e reutilizável: 1500 -> "R$ 1.500,00"
function formatarMoeda(numero) {
  return numero.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

// 3. Adicionar
form.addEventListener("submit", function (evento) {
  evento.preventDefault();

  ativos.push({
    nome: inputNome.value.trim().toUpperCase(),
    tipo: selectTipo.value,
    valor: Number(inputValor.value),
  });

  form.reset();
  inputNome.focus(); // devolve o cursor pro primeiro campo
  desenhar();
});

// 4. Desenhar a lista
function desenhar() {
  lista.innerHTML = "";

  ativos.forEach(function (ativo, indice) {
    const li = document.createElement("li");
    li.classList.add("item");

    // Template literal: texto com `crases` e ${variaveis} dentro
    li.innerHTML = `
      <span class="tag tag-${ativo.tipo}">${NOMES_TIPO[ativo.tipo]}</span>
      <span class="item-nome">${ativo.nome}</span>
      <span>${formatarMoeda(ativo.valor)}</span>
      <button class="btn-remover">X</button>
    `;

    li.querySelector(".btn-remover").addEventListener("click", function () {
      ativos.splice(indice, 1);
      desenhar();
    });

    lista.appendChild(li);
  });

  // Mostra ou esconde a mensagem de "lista vazia"
  vazio.classList.toggle("escondido", ativos.length > 0);

  desenharResumo();
}

// 5. Desenhar os cards de resumo
function desenharResumo() {
  // reduce: "reduz" o array a um único valor (a soma)
  const soma = ativos.reduce(function (acumulado, ativo) {
    return acumulado + ativo.valor;
  }, 0);

  total.textContent = formatarMoeda(soma);
  quantidade.textContent = ativos.length;

  if (ativos.length === 0) {
    maior.textContent = "-";
    return;
  }

  let maiorAtivo = ativos[0];
  for (const ativo of ativos) {
    if (ativo.valor > maiorAtivo.valor) {
      maiorAtivo = ativo;
    }
  }
  maior.textContent = maiorAtivo.nome;
}

desenhar();
