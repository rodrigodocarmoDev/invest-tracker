# Front-end básico: HTML, CSS e JavaScript puro

Material de aula em versões. Cada pasta `vN-...` é a mesma aplicação, a **Minha Carteira** (um mini invest-tracker), um pouco mais evoluída que a anterior.

**Como estudar cada versão:**
1. Abra o `AULA.md` da pasta. Ele mostra onde queremos chegar (print + esquema) e explica **tudo que é novo**.
2. Abra o `index.html` no navegador (dois cliques).
3. Leia o código na ordem HTML → CSS → JS.
4. Faça os exercícios do fim do `AULA.md`.

Cada versão é **completa e independente**: dá pra abrir qualquer uma sem depender das outras.

## Trilha

| Versão | Tema | HTML | CSS | JS | Status |
|--------|------|------|-----|----|--------|
| [v1](v1-lista-simples/AULA.md) | Lista simples | estrutura básica, form, input, ul/li | seletores de tag, cores, margin/padding | getElementById, eventos, array, createElement | ✅ |
| [v2](v2-layout-flex-grid/AULA.md) | Layout com Flexbox e Grid | header/main/section/footer, img, select, class | classes, **flexbox**, **grid**, :hover/:focus, sombras | querySelector, template literal, classList, reduce, toLocaleString | ✅ |
| v3 | Tabela + dados salvos | table/thead/tbody/th/td, label | estilo de tabela, :nth-child, border-collapse | localStorage, JSON, filter/find, busca/filtro por tipo | ⏳ |
| v4 | Responsivo (celular) | picture/ícones, atributos de acessibilidade | media queries, grid auto-fit/minmax, rem/%/vw, mobile-first | — (foco em CSS) | ⏳ |
| v5 | Várias páginas + componentes | nav, a, várias páginas .html | layout compartilhado, position (sticky/absolute) | módulos (import/export), funções-componente, URLSearchParams | ⏳ |
| v6 | Tema escuro + animações | atributos data-*, button de alternância | variáveis CSS (:root), transition, @keyframes, transform | dataset, matchMedia, salvar preferência | ⏳ |
| v7 | Formulários de verdade + gráfico | fieldset/legend, validações nativas, dialog | :invalid/:valid, gráfico de barras só com CSS | validação customizada, editar item, ordenar (sort), map | ⏳ |
| v8 | Conectando no backend Java | — | estados de carregando/erro | fetch, async/await, try/catch, Promise | ⏳ |

> Pra gerar a próxima versão, peça ao Claude Code (com esta pasta aberta): **"/nova-versao"**. A skill está em `.claude/skills/nova-versao/`.
