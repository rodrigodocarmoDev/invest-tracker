// Preenche o formulário como se o usuário tivesse digitado
[["PETR4", 1500], ["MXRF11", 800], ["Tesouro Selic", 2000]].forEach(function ([n, v]) {
  inputNome.value = n; inputValor.value = v; form.requestSubmit();
});
