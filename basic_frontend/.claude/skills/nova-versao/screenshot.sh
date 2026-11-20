#!/usr/bin/env bash
# Tira um print da versão já com dados de exemplo preenchidos.
# Uso: bash screenshot.sh <pasta-da-versao> <demos/vN.js> [largura] [altura]
# Saída: <pasta-da-versao>/imagens/resultado.png
set -e
VERSAO="$(cd "$1" && pwd)"
DEMO="$2"
LARGURA="${3:-1000}"
ALTURA="${4:-800}"
CHROME="/c/Program Files/Google/Chrome/Application/chrome.exe"
[ -x "$CHROME" ] || CHROME="/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"

TMP="$(mktemp -d)"
DEMO_HTML="$TMP/demo.html"
# Copia o index.html com <base> apontando pra pasta da versão (CSS/JS/imagens relativos funcionam)
# e injeta o script de demonstração antes do </body>.
sed "s#<head>#<head><base href=\"file:///$(cygpath -m "$VERSAO")/\">#" "$VERSAO/index.html" | sed '/<\/body>/,$d' > "$DEMO_HTML"
{ echo "<script>"; cat "$DEMO"; echo "</script></body></html>"; } >> "$DEMO_HTML"

mkdir -p "$VERSAO/imagens"
"$CHROME" --headless=new --disable-gpu --hide-scrollbars --allow-file-access-from-files \
  --virtual-time-budget=2000 --window-size="$LARGURA,$ALTURA" \
  --screenshot="$(cygpath -w "$VERSAO/imagens/resultado.png")" "file:///$(cygpath -m "$DEMO_HTML")" 2>/dev/null
rm -rf "$TMP"
echo "OK: $VERSAO/imagens/resultado.png"
