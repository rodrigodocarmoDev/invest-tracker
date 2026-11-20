---
name: nova-versao
description: Gera a próxima versão (vN) do material de aula de front-end básico (HTML/CSS/JS puro) da "Minha Carteira" em basic_frontend/. Use quando o professor pedir "nova versão", "próxima versão", "v3", "/nova-versao" ou ajustes numa versão existente do material.
---

# Gerar nova versão do material de front-end básico

Contexto: um professor ensina programação a um aluno. O backend (Java) do invest-tracker já foi feito. Este material ensina **HTML, CSS e JavaScript puro**, sem Node e sem build. Cada versão é a mesma app, a **Minha Carteira**, um pouco mais evoluída, e deve ser possível **apresentar inteira funcionando em ~1h de aula**.

Pasta base: `basic_frontend/` (onde está o `README.md` com a trilha).

## Passo a passo

1. **Ler o estado atual:** `README.md` (trilha e status) e a última versão `vN-*/` inteira (index.html, style.css, script.js, AULA.md). Se o professor pediu um tema diferente da trilha, siga o pedido e atualize a trilha.
2. **Copiar a versão anterior** para `v{N+1}-{tema-em-kebab-case}/` e evoluir a partir dela. O aluno deve reconhecer o código. Não reescreva do zero.
3. **Implementar só o tema da versão** (a linha correspondente da trilha no README). De 3 a 8 conceitos novos por linguagem, no máximo. Se uma versão for só de CSS, o JS pode ficar praticamente igual.
4. **Escrever o `AULA.md`** seguindo o modelo abaixo.
5. **Gerar as imagens** em `imagens/` (print + esquema). Veja a seção "Imagens".
6. **Atualizar o `README.md`**: link para a versão nova, status ✅ e ajuste das próximas linhas, se for o caso.
7. **Verificar:** abrir o print gerado (Read do PNG) e conferir se a página aparece corretamente, sem erro visual. Se tiver JS, confira se os dados de demonstração aparecem.

## Regras do código

- Português em nomes de variáveis, funções, classes CSS e comentários (`ativos`, `desenhar`, `.card`).
- **Simples primeiro:** `function` nomeada em vez de truques. Arrow functions só a partir da versão em que forem ensinadas (registre na AULA quando aparecerem pela primeira vez).
- Abrir com dois cliques no `index.html` tem que funcionar. Atenção: `import/export` (módulos ES) **não funciona via `file://`**. Na versão que usar módulos, explique na AULA como abrir com a extensão Live Server do VS Code ou com `npx serve`, e use `type="module"`.
- Sem bibliotecas e sem CDN. Tudo na mão.
- Comentários numerados no JS (`// 1. ...`, `// 2. ...`) para guiar a leitura em aula.
- Arquivos pequenos: se o CSS ou o JS passar de ~200 linhas, divida em arquivos/componentes (ex: `componentes/card.js`, `css/layout.css`) e explique a divisão na AULA.
- Imagens usadas **na tela do site** ficam em `assets/` (SVG feito à mão é o preferido, porque é leve e sem copyright).
- Mantenha os IDs/variáveis principais das versões anteriores (`form`, `inputNome`, `selectTipo`, `inputValor`, `ativos`, `desenhar`) para o `demos/*.js` e o aluno continuarem reconhecendo o código.

## Modelo do `AULA.md`

```
# Versão N — Título

> **Objetivo:** uma frase.

## Onde queremos chegar
![Resultado final](imagens/resultado.png)
Frase curta + ![Esquema](imagens/esquema.svg)
**O que mudou em relação à vN-1:** lista curta, do ponto de vista de quem usa a página.

---
## O que tem de novo
### HTML (`index.html`)   → tabela: Tag/atributo | Pra que serve
### CSS (`style.css`)     → tabelas de seletores/propriedades + mini-exemplos de código para conceitos grandes (flex, grid, media query...)
### JavaScript (`script.js`) → tabela: Comando | O que faz
(Notas com > e ⚠️ para armadilhas, boas práticas e segurança.)

---
## Comparando com Java   → tabela Java | JavaScript (o aluno já sabe Java)
## Roteiro do professor (~1h)   → 3 a 5 blocos com minutagem e o que mostrar ao vivo
## Exercícios   → 3 ou 4, do mais fácil pro mais difícil, com dica
> **Gancho pra próxima versão:** um problema que a versão atual tem e que a próxima resolve.
```

Só entra nas tabelas o que é **novo** naquela versão. O que já foi ensinado não se repete. A linguagem deve ser leiga e direta, para o aluno ler sozinho depois da aula.

## Imagens (`vN-*/imagens/`)

1. **`resultado.png`**: print real da página com dados de exemplo. Para isso:
   - Crie `.claude/skills/nova-versao/demos/vN.js`: um trecho que preenche o formulário e chama `form.requestSubmit()` (veja `demos/v2.js`). Se a versão usar `localStorage`, limpe-o antes no demo (`localStorage.clear()`).
   - Rode a partir da pasta da skill:
     ```bash
     bash screenshot.sh ../../../vN-tema demos/vN.js 1000 760
     ```
     O script injeta o demo numa cópia temporária do HTML e tira o print com o Chrome headless. Os parâmetros são largura e altura. Para versões responsivas, gere também `resultado-celular.png` com `390 844` (renomeie o arquivo depois de gerar).
2. **`esquema.svg`**: wireframe feito à mão em SVG. Caixas com o nome da tag/classe, anotações do conceito novo da versão (ex: "display: grid → 3 colunas") e cores diferentes por bloco. Use `esquema.svg` da v2 como referência de estilo. Confira renderizando com o Chrome headless (`--screenshot` apontando para o .svg, salvando no scratchpad) e lendo o PNG.
3. Opcional, quando ajudar: imagens extras de "antes x depois" ou de um conceito (ex: diagrama do eixo principal/cruzado do flexbox), referenciadas no AULA.md.

## Ao terminar

Responda ao professor com: a pasta criada, a lista do que é novo (HTML/CSS/JS), o roteiro de 1h em 3 linhas e qualquer cuidado para abrir (ex: Live Server em versões com módulos).
