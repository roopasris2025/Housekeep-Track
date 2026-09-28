/* Shared helpers used by every page: api(), toast(), badge(), modal, layout */
const LOGO = `<svg viewBox="0 0 40 40" width="36" height="36" aria-label="StayEase logo"><circle cx="20" cy="20" r="20" fill="#C9A227"/><rect x="11" y="10" width="18" height="20" rx="1.5" fill="#0F2A43"/><g fill="#C9A227"><rect x="14" y="13" width="3" height="3"/><rect x="18.5" y="13" width="3" height="3"/><rect x="23" y="13" width="3" height="3"/><rect x="14" y="18" width="3" height="3"/><rect x="18.5" y="18" width="3" height="3"/><rect x="23" y="18" width="3" height="3"/><rect x="17.5" y="24" width="5" height="6"/></g></svg>`;

function getUser() { try { return JSON.parse(sessionStorage.getItem('user')); } catch (e) { return null; } }
function esc(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function fmt(dt) { return dt ? new Date(dt).toLocaleString('en-IN', {day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit'}) : '-'; }
function badge(s) { return `<span class="badge b-${esc(s)}">${esc(String(s).replace('_', ' '))}</span>`; }

/* one function for every REST call. Throws Error(message) using the backend's {error, message} */
async function api(path, method = 'GET', body) {
  const u = getUser();
  const res = await fetch('/api' + path, {
    method,
    headers: {'Content-Type': 'application/json', 'X-Performed-By': u ? u.name : 'System'},
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 204) return null;
  let data = null;
  try { data = await res.json(); } catch (e) {}
  if (!res.ok) {
    const err = new Error(data && data.message ? data.message : 'Request failed (' + res.status + ')');
    err.code = data && data.error;
    throw err;
  }
  return data;
}

function toast(msg, type = 'info') {
  let box = document.getElementById('toasts');
  if (!box) { box = document.createElement('div'); box.id = 'toasts'; document.body.appendChild(box); }
  const t = document.createElement('div');
  t.className = 'toast ' + type; t.textContent = msg; box.appendChild(t);
  setTimeout(() => t.remove(), 4500);
}
/* run an action, show success toast or the backend error message */
async function run(action, okMsg) {
  try { const r = await action(); if (okMsg) toast(okMsg, 'ok'); return r; }
  catch (e) { toast(e.message, 'error'); }
}

function openModal(title, html, onSubmit, submitLabel = 'Save') {
  closeModal();
  const o = document.createElement('div');
  o.className = 'modal-overlay'; o.id = 'modal';
  o.innerHTML = `<div class="modal" role="dialog" aria-modal="true"><div class="modal-head"><h3>${title}</h3><button class="icon-btn" type="button" onclick="closeModal()" aria-label="Close">✕</button></div>
    <form id="modalForm">${html}<div class="form-error" id="formError"></div>
    <div class="modal-actions"><button type="button" class="btn btn-ghost" onclick="closeModal()">${onSubmit ? 'Cancel' : 'Close'}</button>${onSubmit ? `<button class="btn btn-primary">${submitLabel}</button>` : ''}</div></form></div>`;
  document.body.appendChild(o);
  document.getElementById('modalForm').addEventListener('submit', async e => {
    e.preventDefault();
    if (!onSubmit) return;
    try { await onSubmit(new FormData(e.target)); closeModal(); }
    catch (err) { document.getElementById('formError').textContent = err.message; }
  });
}
function closeModal() { const m = document.getElementById('modal'); if (m) m.remove(); }

function pager(id, p, go) {
  const el = document.getElementById(id);
  if (!p || p.totalPages <= 1) { el.innerHTML = ''; return; }
  el.innerHTML = `<button class="btn btn-ghost btn-sm" ${p.first ? 'disabled' : ''} id="${id}Prev">‹ Prev</button><span>Page ${p.number + 1} of ${p.totalPages}</span><button class="btn btn-ghost btn-sm" ${p.last ? 'disabled' : ''} id="${id}Next">Next ›</button>`;
  document.getElementById(id + 'Prev').onclick = () => go(p.number - 1);
  document.getElementById(id + 'Next').onclick = () => go(p.number + 1);
}

const NAV = [['dashboard', '📊', 'Dashboard'], ['rooms', '🛏️', 'Rooms'], ['tasks', '🧹', 'Cleaning Tasks'], ['housekeepers', '👥', 'Housekeepers'],
             ['inspections', '✅', 'Inspections'], ['bookings', '🗓️', 'Bookings'], ['reports', '📈', 'Reports'], ['audit', '📜', 'Audit Logs']];

/* builds sidebar + top header; redirects to login if nobody is signed in */
function initLayout(active, title) {
  const u = getUser();
  if (!u) { location.href = 'login.html'; return; }
  document.getElementById('sidebar').innerHTML =
    `<div class="brand">${LOGO}<b>StayEase</b></div><nav class="nav">` +
    NAV.map(n => `<a href="${n[0]}.html" class="${n[0] === active ? 'active' : ''}"><span>${n[1]}</span>${n[2]}</a>`).join('') +
    `<div class="spacer"></div><a href="#" id="logout"><span>🚪</span>Logout</a></nav>`;
  document.getElementById('logout').onclick = e => { e.preventDefault(); sessionStorage.removeItem('user'); location.href = 'login.html'; };
  document.getElementById('topbar').innerHTML =
    `<button class="menu-btn" onclick="document.getElementById('sidebar').classList.toggle('open')" aria-label="Menu">☰</button>
     <h2>${title}</h2>
     <a href="inspections.html" class="bell" title="Rooms waiting for inspection" style="text-decoration:none">🔔<span id="bellCount" hidden>0</span></a>
     <div class="profile"><div class="avatar">${esc(u.name[0])}</div><div>${esc(u.name)}<small>${esc(u.role)}</small></div></div>`;
  api('/inspections/pending').then(l => { const b = document.getElementById('bellCount'); if (l.length) { b.textContent = l.length; b.hidden = false; } }).catch(() => {});
}
