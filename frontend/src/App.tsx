import { FormEvent, useEffect, useState } from "react";
import { api, hasToken, setToken, Asset, AuthResponse, DailyQuote, Transaction, User, Wallet } from "./api";

// Endpoints de listagem devolvem 204 (sem corpo) quando vazios.
const list = async <T,>(path: string) => (await api<T[] | undefined>(path)) ?? [];
const money = (v: number | null) => (v ?? 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

function useForm<T extends Record<string, string>>(initial: T) {
  const [values, setValues] = useState(initial);
  const field = (name: keyof T) => ({
    value: values[name],
    onChange: (e: { target: { value: string } }) => setValues({ ...values, [name]: e.target.value }),
  });
  return { values, field, reset: () => setValues(initial) };
}

function Login({ onLogin }: { onLogin: (u: User) => void }) {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [msg, setMsg] = useState("");
  const { values, field } = useForm({ name: "", email: "", password: "" });

  async function submit(e: FormEvent) {
    e.preventDefault();
    setMsg("");
    try {
      if (mode === "register") {
        const r = await api<{ message: string }>("/auth/register", "POST", values);
        setMsg(r.message);
        setMode("login");
      } else {
        const r = await api<AuthResponse>("/auth/login", "POST", { email: values.email, password: values.password });
        setToken(r.accessToken);
        onLogin(r.user);
      }
    } catch (err) {
      setMsg((err as Error).message);
    }
  }

  return (
    <form className="card auth" onSubmit={submit}>
      <h1>Invest Tracker</h1>
      {mode === "register" && <input placeholder="Nome" {...field("name")} />}
      <input type="email" placeholder="E-mail" required {...field("email")} />
      <input type="password" placeholder="Senha" required minLength={6} {...field("password")} />
      <button>{mode === "login" ? "Entrar" : "Cadastrar"}</button>
      <a href="#" onClick={() => setMode(mode === "login" ? "register" : "login")}>
        {mode === "login" ? "Criar conta" : "Já tenho conta"}
      </a>
      {msg && <p className="msg">{msg}</p>}
    </form>
  );
}

function Wallets({ wallets, reload }: { wallets: Wallet[]; reload: () => void }) {
  const { values, field, reset } = useForm({ name: "" });
  return (
    <section>
      <form className="row" onSubmit={async (e) => { e.preventDefault(); await run(() => api("/wallets", "POST", values)); reset(); reload(); }}>
        <input placeholder="Nome da carteira" required {...field("name")} />
        <button>Adicionar</button>
      </form>
      <table>
        <thead><tr><th>Nome</th><th>Criada em</th><th></th></tr></thead>
        <tbody>
          {wallets.map((w) => (
            <tr key={w.id}>
              <td>{w.name}</td>
              <td>{new Date(w.createdAt).toLocaleDateString("pt-BR")}</td>
              <td><button className="danger" onClick={async () => { await run(() => api(`/wallets/${w.id}`, "DELETE")); reload(); }}>Excluir</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

function Transactions({ wallets, assets }: { wallets: Wallet[]; assets: Asset[] }) {
  const [items, setItems] = useState<Transaction[]>([]);
  const { values, field, reset } = useForm({ walletId: "", assetId: "", operationType: "BUY", quantity: "", unitPrice: "", fees: "" });
  const reload = () => list<Transaction>("/transactions").then(setItems);
  useEffect(() => { reload(); }, []);

  async function submit(e: FormEvent) {
    e.preventDefault();
    await run(() => api("/transactions", "POST", {
      ...values,
      quantity: Number(values.quantity),
      unitPrice: Number(values.unitPrice),
      fees: values.fees ? Number(values.fees) : null,
    }));
    reset();
    reload();
  }

  return (
    <section>
      <form className="row" onSubmit={submit}>
        <select required {...field("walletId")}>
          <option value="">Carteira</option>
          {wallets.map((w) => <option key={w.id} value={w.id}>{w.name}</option>)}
        </select>
        <select required {...field("assetId")}>
          <option value="">Ativo</option>
          {assets.map((a) => <option key={a.id} value={a.id}>{a.ticker}</option>)}
        </select>
        <select {...field("operationType")}>
          <option value="BUY">Compra</option>
          <option value="SELL">Venda</option>
        </select>
        <input type="number" step="any" min="0" placeholder="Qtd" required {...field("quantity")} />
        <input type="number" step="0.01" min="0" placeholder="Preço" required {...field("unitPrice")} />
        <input type="number" step="0.01" min="0" placeholder="Taxas" {...field("fees")} />
        <button>Registrar</button>
      </form>
      <table>
        <thead><tr><th>Carteira</th><th>Ativo</th><th>Op.</th><th>Qtd</th><th>Preço</th><th>Taxas</th><th>Total</th><th></th></tr></thead>
        <tbody>
          {items.map((t) => (
            <tr key={t.id}>
              <td>{t.walletName}</td>
              <td>{t.assetTicker}</td>
              <td className={t.operationType === "BUY" ? "buy" : "sell"}>{t.operationType === "BUY" ? "Compra" : "Venda"}</td>
              <td>{t.quantity}</td>
              <td>{money(t.unitPrice)}</td>
              <td>{money(t.fees)}</td>
              <td>{money(t.quantity * t.unitPrice + (t.fees ?? 0))}</td>
              <td><button className="danger" onClick={async () => { await run(() => api(`/transactions/${t.id}`, "DELETE")); reload(); }}>Excluir</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

function Assets({ assets, isAdmin, reload }: { assets: Asset[]; isAdmin: boolean; reload: () => void }) {
  const { values, field, reset } = useForm({ ticker: "", type: "", name: "" });
  return (
    <section>
      {isAdmin && (
        <form className="row" onSubmit={async (e) => { e.preventDefault(); await run(() => api("/assets", "POST", values)); reset(); reload(); }}>
          <input placeholder="Ticker" required minLength={2} maxLength={10} {...field("ticker")} />
          <input placeholder="Tipo (ex: ACAO, FII)" required {...field("type")} />
          <input placeholder="Nome" required {...field("name")} />
          <button>Adicionar</button>
        </form>
      )}
      <table>
        <thead><tr><th>Ticker</th><th>Tipo</th><th>Nome</th>{isAdmin && <th></th>}</tr></thead>
        <tbody>
          {assets.map((a) => (
            <tr key={a.id}>
              <td><b>{a.ticker}</b></td>
              <td>{a.type}</td>
              <td>{a.name}</td>
              {isAdmin && <td><button className="danger" onClick={async () => { await run(() => api(`/assets/${a.id}`, "DELETE")); reload(); }}>Excluir</button></td>}
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

function Quotes({ isAdmin }: { isAdmin: boolean }) {
  const [items, setItems] = useState<DailyQuote[]>([]);
  const reload = () => list<DailyQuote>("/daily-quotes").then(setItems);
  useEffect(() => { reload(); }, []);
  return (
    <section>
      {isAdmin && (
        <div className="row">
          <button onClick={async () => { await run(() => api("/daily-quotes/sync", "POST")); reload(); }}>Sincronizar com brapi</button>
        </div>
      )}
      <table>
        <thead><tr><th>Ativo</th><th>Data</th><th>Fechamento</th></tr></thead>
        <tbody>
          {items.map((q) => (
            <tr key={q.id}>
              <td><b>{q.assetTicker}</b></td>
              <td>{new Date(q.quoteDate + "T00:00").toLocaleDateString("pt-BR")}</td>
              <td>{money(q.closePrice)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

// Posição consolidada por ativo: preço médio (com taxas) x última cotação.
function Portfolio() {
  const [tx, setTx] = useState<Transaction[]>([]);
  const [quotes, setQuotes] = useState<DailyQuote[]>([]);
  useEffect(() => {
    list<Transaction>("/transactions").then(setTx);
    list<DailyQuote>("/daily-quotes").then(setQuotes);
  }, []);

  const lastPrice = new Map<string, number>();
  [...quotes].sort((a, b) => a.quoteDate.localeCompare(b.quoteDate)).forEach((q) => lastPrice.set(q.assetId, q.closePrice));

  const pos = new Map<string, { ticker: string; qty: number; cost: number }>();
  for (const t of tx) {
    const p = pos.get(t.assetId) ?? { ticker: t.assetTicker, qty: 0, cost: 0 };
    if (t.operationType === "BUY") {
      p.qty += t.quantity;
      p.cost += t.quantity * t.unitPrice + (t.fees ?? 0);
    } else if (p.qty > 0) {
      p.cost -= (p.cost / p.qty) * Math.min(t.quantity, p.qty);
      p.qty = Math.max(0, p.qty - t.quantity);
    }
    pos.set(t.assetId, p);
  }
  const rows = [...pos.entries()].filter(([, p]) => p.qty > 0).map(([id, p]) => {
    const price = lastPrice.get(id);
    const value = price !== undefined ? price * p.qty : p.cost;
    return { id, ...p, avg: p.cost / p.qty, price, value, pl: value - p.cost };
  });
  const invested = rows.reduce((s, r) => s + r.cost, 0);
  const current = rows.reduce((s, r) => s + r.value, 0);
  const pl = current - invested;

  return (
    <section>
      <div className="stats">
        <div className="card"><small>Investido</small><strong>{money(invested)}</strong></div>
        <div className="card"><small>Valor atual</small><strong>{money(current)}</strong></div>
        <div className="card"><small>Resultado</small>
          <strong className={pl >= 0 ? "buy" : "sell"}>{money(pl)} ({invested ? ((pl / invested) * 100).toFixed(2) : "0.00"}%)</strong>
        </div>
      </div>
      <table>
        <thead><tr><th>Ativo</th><th>Qtd</th><th>Preço médio</th><th>Última cotação</th><th>Valor</th><th>Resultado</th></tr></thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.id}>
              <td><b>{r.ticker}</b></td>
              <td>{r.qty}</td>
              <td>{money(r.avg)}</td>
              <td>{r.price !== undefined ? money(r.price) : "—"}</td>
              <td>{money(r.value)}</td>
              <td className={r.pl >= 0 ? "buy" : "sell"}>{money(r.pl)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

// Mostra erros de operações de forma simples, sem interromper a tela.
async function run(fn: () => Promise<unknown>) {
  try {
    await fn();
  } catch (err) {
    window.dispatchEvent(new CustomEvent("app-error", { detail: (err as Error).message }));
  }
}

const TABS = { portfolio: "Resumo", wallets: "Carteiras", transactions: "Transações", assets: "Ativos", quotes: "Cotações" } as const;

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [tab, setTab] = useState<keyof typeof TABS>("portfolio");
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [error, setError] = useState("");

  const loadWallets = () => list<Wallet>("/wallets").then(setWallets);
  const loadAssets = () => list<Asset>("/assets").then(setAssets);

  useEffect(() => {
    const onError = (e: Event) => setError((e as CustomEvent<string>).detail);
    window.addEventListener("app-error", onError);
    if (hasToken()) api<User>("/auth/me").then(setUser).catch(() => setToken(null));
    return () => window.removeEventListener("app-error", onError);
  }, []);

  useEffect(() => {
    if (user) { loadWallets(); loadAssets(); }
  }, [user]);

  if (!user) return <Login onLogin={setUser} />;
  const isAdmin = user.role === "ADMIN";

  return (
    <div className="app">
      <header>
        <h1>Invest Tracker</h1>
        <nav>
          {(Object.keys(TABS) as (keyof typeof TABS)[]).map((k) => (
            <button key={k} className={tab === k ? "active" : "ghost"} onClick={() => setTab(k)}>{TABS[k]}</button>
          ))}
        </nav>
        <span>{user.name || user.email} <button className="ghost" onClick={() => { setToken(null); setUser(null); }}>Sair</button></span>
      </header>
      {error && <p className="msg" onClick={() => setError("")}>{error} ✕</p>}
      {tab === "portfolio" && <Portfolio />}
      {tab === "wallets" && <Wallets wallets={wallets} reload={loadWallets} />}
      {tab === "transactions" && <Transactions wallets={wallets} assets={assets} />}
      {tab === "assets" && <Assets assets={assets} isAdmin={isAdmin} reload={loadAssets} />}
      {tab === "quotes" && <Quotes isAdmin={isAdmin} />}
    </div>
  );
}
