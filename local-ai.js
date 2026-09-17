(() => {
  const I18N={
    de:{title:'Lokaler KI-Cross-Check',run:'Lokale KI prüfen',ready:'Regel-Engine aktiv · lokale KI optional',builtin:'Browser-LLM',webllm:'Qwen2.5-0.5B lokal',no:'Kein lokales LLM verfügbar – Regel-Engine bleibt vollständig nutzbar.',loading:'Lokales Modell wird vorbereitet',working:'Lokale KI analysiert',note:'Keine Inference-API: Text bleibt für die Modellinferenz auf dem Gerät. Beim ersten WebLLM-Start werden nur Runtime/Modell-Dateien geladen.'},
    en:{title:'Local AI cross-check',run:'Run local AI',ready:'Rule engine active · local AI optional',builtin:'Browser LLM',webllm:'Qwen2.5-0.5B local',no:'No local LLM available – the full rule engine remains usable.',loading:'Preparing local model',working:'Local AI is analyzing',note:'No inference API: text stays on-device for model inference. First WebLLM use downloads runtime/model files only.'},
    fr:{title:'Contre-vérification IA locale',run:'Lancer IA locale',ready:'Moteur de règles actif · IA locale optionnelle',builtin:'LLM du navigateur',webllm:'Qwen2.5-0.5B local',no:'Aucun LLM local disponible – le moteur de règles reste utilisable.',loading:'Préparation du modèle local',working:'Analyse locale en cours',note:'Aucune API d’inférence : le texte reste sur l’appareil. WebLLM télécharge seulement le runtime/modèle au premier usage.'},
    es:{title:'Verificación con IA local',run:'Ejecutar IA local',ready:'Motor de reglas activo · IA local opcional',builtin:'LLM del navegador',webllm:'Qwen2.5-0.5B local',no:'No hay LLM local disponible; el motor de reglas sigue operativo.',loading:'Preparando modelo local',working:'La IA local está analizando',note:'Sin API de inferencia: el texto permanece en el dispositivo. WebLLM solo descarga runtime/modelo la primera vez.'},
    it:{title:'Controllo IA locale',run:'Avvia IA locale',ready:'Motore a regole attivo · IA locale opzionale',builtin:'LLM del browser',webllm:'Qwen2.5-0.5B locale',no:'Nessun LLM locale disponibile; il motore a regole resta utilizzabile.',loading:'Preparazione modello locale',working:'Analisi IA locale',note:'Nessuna API di inferenza: il testo resta sul dispositivo. WebLLM scarica solo runtime/modello al primo uso.'},
    nl:{title:'Lokale AI-controle',run:'Lokale AI uitvoeren',ready:'Regel-engine actief · lokale AI optioneel',builtin:'Browser-LLM',webllm:'Qwen2.5-0.5B lokaal',no:'Geen lokale LLM beschikbaar; de regel-engine blijft volledig bruikbaar.',loading:'Lokaal model voorbereiden',working:'Lokale AI analyseert',note:'Geen inference-API: tekst blijft voor modelinference op het apparaat. WebLLM downloadt alleen runtime/modelbestanden bij eerste gebruik.'}
  };
  let browserSession=null,webEngine=null,mode='';
  const d=()=>I18N[ui.lang.value]||I18N.en;
  function mount(){
    if(document.getElementById('localAiBox')) return;
    const box=document.createElement('section');box.id='localAiBox';box.style.cssText='margin:16px 0;padding:16px;border:1px solid #27466f;border-radius:18px;background:#091524';
    box.innerHTML=`<div style="display:flex;gap:12px;align-items:center;justify-content:space-between;flex-wrap:wrap"><div><b id="localAiTitle" style="font-size:1.05rem"></b><div id="localAiStatus" style="color:#88a6ce;font-size:.82rem;margin-top:4px"></div></div><button id="localAiBtn" class="button ghost" type="button"></button></div><p id="localAiNote" style="font-size:.76rem;color:#718bad;margin:10px 0 0"></p><pre id="localAiOutput" class="report" style="margin-top:12px;white-space:pre-wrap;display:none"></pre>`;
    ui.report.parentNode.insertBefore(box,ui.report);
    box.querySelector('#localAiBtn').addEventListener('click',run);
    renderLabels();
  }
  function renderLabels(){mount();const x=d();document.getElementById('localAiTitle').textContent=x.title;document.getElementById('localAiBtn').textContent=x.run;document.getElementById('localAiStatus').textContent=x.ready;document.getElementById('localAiNote').textContent=x.note}
  function chunks(text,max=6500){const s=splitSentences(text),out=[];let cur='';for(const line of s){if(cur&&cur.length+line.length+1>max){out.push(cur);cur=''}cur+=(cur?' ':'')+line}if(cur)out.push(cur);return out}
  function systemPrompt(lang){return `You are AntiGolem's on-device linguistic cross-check. Analyze rhetoric and syntax carefully in language ${lang}. Do not claim that wording literally programs brains or proves subconscious causation. Treat Golem/Pygmalion, mantra, ghost-context and psychological programming as heuristic editorial labels. Focus on: ambiguous reference and pronoun binding; negative-normalization framing; absolutism; modal pressure; blame; responsibility diffusion; agency; constructive framing; target-group effects; and concrete rewrites. Quote only short exact fragments. Be precise, concise and evidence-led.`}
  async function ensureModel(setStatus){
    if(browserSession){mode='browser';return async p=>browserSession.prompt(p)}
    if('LanguageModel' in globalThis){
      try{
        const availability=await LanguageModel.availability();
        if(availability!=='unavailable'){
          setStatus(`${d().loading} · ${d().builtin}${availability==='downloading'?' · download':''}`);
          browserSession=await LanguageModel.create({initialPrompts:[{role:'system',content:systemPrompt(ui.lang.value)}],monitor(m){m.addEventListener('downloadprogress',e=>setStatus(`${d().loading} · ${Math.round(e.loaded*100)}%`))}});
          mode='browser';return async p=>browserSession.prompt(p);
        }
      }catch(e){console.warn('Prompt API unavailable',e)}
    }
    if(navigator.gpu){
      try{
        setStatus(`${d().loading} · ${d().webllm}`);
        if(!webEngine){
          const webllm=await import('https://cdn.jsdelivr.net/npm/@mlc-ai/web-llm/+esm');
          const appConfig={...webllm.prebuiltAppConfig,cacheBackend:'indexeddb'};
          webEngine=await webllm.CreateMLCEngine('Qwen2.5-0.5B-Instruct-q4f16_1-MLC',{appConfig,initProgressCallback:r=>setStatus(r.text||d().loading)});
        }
        mode='webllm';return async p=>{const r=await webEngine.chat.completions.create({messages:[{role:'system',content:systemPrompt(ui.lang.value)},{role:'user',content:p}],temperature:.15,max_tokens:1200});return r.choices?.[0]?.message?.content||''};
      }catch(e){console.warn('WebLLM unavailable',e)}
    }
    return null;
  }
  async function askLocal(prompt,setStatus=()=>{}){
    try{const ask=await ensureModel(setStatus);if(!ask)return null;return await ask(prompt)}catch(e){console.warn('Local AI generation unavailable',e);return null}
  }
  async function run(){
    const text=ui.source.value.trim();if(!text){ui.source.focus();return}
    const btn=document.getElementById('localAiBtn'),status=document.getElementById('localAiStatus'),out=document.getElementById('localAiOutput');
    btn.disabled=true;out.style.display='block';out.textContent='';
    const setStatus=s=>status.textContent=s;
    try{
      const ask=await ensureModel(setStatus);if(!ask){setStatus(d().no);out.textContent=d().no;return}
      const parts=chunks(text),notes=[];
      for(let i=0;i<parts.length;i++){
        setStatus(`${d().working} · ${i+1}/${parts.length} · ${mode==='browser'?d().builtin:d().webllm}`);
        notes.push(await ask(`Audit this chunk (${i+1}/${parts.length}) sentence by sentence where relevant. Return concise structured findings with exact short fragments and constructive corrections.\n\n${parts[i]}`));
      }
      let result=notes.join('\n\n---\n\n');
      if(notes.length>1){setStatus(`${d().working} · synthesis`);result=await ask(`Synthesize the following chunk audits into one non-duplicative cross-check. Preserve concrete findings, contradictions and correction priorities. Do not invent findings that are absent from the notes.\n\n${result.slice(0,24000)}`)}
      out.textContent=result;setStatus(`✓ ${mode==='browser'?d().builtin:d().webllm}`);
    }catch(e){console.error(e);setStatus(`${d().no} (${e?.name||'error'})`);out.textContent=String(e?.message||e)}finally{btn.disabled=false}
  }
  window.AntiGolemLocalAI={ask:askLocal,getMode:()=>mode,isPotentiallyAvailable:()=>('LanguageModel' in globalThis)||!!navigator.gpu};
  document.addEventListener('DOMContentLoaded',mount,{once:true});
  if(document.readyState!=='loading')mount();
  ui.lang.addEventListener('change',renderLabels);
})();
