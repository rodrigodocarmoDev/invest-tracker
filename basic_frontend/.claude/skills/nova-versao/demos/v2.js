[["PETR4", "acao", 1500], ["MXRF11", "fii", 800], ["Tesouro Selic", "renda-fixa", 2000], ["BTC", "cripto", 650]]
  .forEach(function ([n, t, v]) {
    inputNome.value = n; selectTipo.value = t; inputValor.value = v; form.requestSubmit();
  });
