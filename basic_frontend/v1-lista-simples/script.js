// 1. Pegar os elementos do HTML
const form = document.getElementById("form");
const inputNome = document.getElementById("nome");
const inputValor = document.getElementById("valor");
const lista = document.getElementById("lista");
const total = document.getElementById("total");

// 2. Guardar os dados num array
const ativos = [];

// 3. Reagir ao clique em "Adicionar"
form.addEventListener("submit", function (evento) {
  evento.preventDefault(); // impede a página de recarregar

  ativos.push({
    nome: inputNome.value,
    valor: Number(inputValor.value),
  });

  form.reset();
  desenhar();
});

// 4. Redesenhar a lista e o total na tela
function desenhar() {
  lista.innerHTML = "";
  let soma = 0;

  ativos.forEach(function (ativo, indice) {
    const li = document.createElement("li");
    li.textContent = ativo.nome + " - R$ " + ativo.valor.toFixed(2);

    const botaoRemover = document.createElement("button");
    botaoRemover.textContent = "X";
    botaoRemover.style.width = "auto";
    botaoRemover.onclick = function () {
      ativos.splice(indice, 1);
      desenhar();
    };

    li.appendChild(botaoRemover);
    lista.appendChild(li);
    soma += ativo.valor;
  });

  total.textContent = "R$ " + soma.toFixed(2);
}
