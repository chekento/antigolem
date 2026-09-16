(() => {
  const WORD=/[\p{L}\p{N}’'-]+/gu;
  const MAP={
    de:{title:'Erweiterte Statistik',words:'Wörter',avg:'Ø Wörter/Satz',lex:'Lexikalische Vielfalt',abs:'Absolutismen',modal:'Modaler Druck',blame:'Schuldzuweisung',quest:'Fragesätze',excl:'Ausrufe',d2:'D-2+ Risiko',risk:'Kritisch/hoch',construct:'Konstruktiver Anteil',clarity:'Klarheitsindex'},
    en:{title:'Extended statistics',words:'Words',avg:'Avg words/sentence',lex:'Lexical diversity',abs:'Absolutism',modal:'Modal pressure',blame:'Blame cues',quest:'Questions',excl:'Exclamations',d2:'D-2+ risk',risk:'Critical/high',construct:'Constructive share',clarity:'Clarity index'},
    fr:{title:'Statistiques étendues',words:'Mots',avg:'Mots/phrase',lex:'Diversité lexicale',abs:'Absolutismes',modal:'Pression modale',blame:'Culpabilisation',quest:'Questions',excl:'Exclamations',d2:'Risque D-2+',risk:'Critique/élevé',construct:'Part constructive',clarity:'Indice de clarté'},
    es:{title:'Estadística ampliada',words:'Palabras',avg:'Palabras/frase',lex:'Diversidad léxica',abs:'Absolutismos',modal:'Presión modal',blame:'Culpa',quest:'Preguntas',excl:'Exclamaciones',d2:'Riesgo D-2+',risk:'Crítico/alto',construct:'Parte constructiva',clarity:'Índice de claridad'},
    it:{title:'Statistiche estese',words:'Parole',avg:'Parole/frase',lex:'Diversità lessicale',abs:'Assolutismi',modal:'Pressione modale',blame:'Colpevolizzazione',quest:'Domande',excl:'Esclamazioni',d2:'Rischio D-2+',risk:'Critico/alto',construct:'Quota costruttiva',clarity:'Indice di chiarezza'},
    nl:{title:'Uitgebreide statistiek',words:'Woorden',avg:'Woorden/zin',lex:'Lexicale diversiteit',abs:'Absolutismen',modal:'Modale druk',blame:'Verwijt-signalen',quest:'Vragen',excl:'Uitroepen',d2:'D-2+ risico',risk:'Kritiek/hoog',construct:'Constructief aandeel',clarity:'Helderheidsindex'}
  };
  const PAT={
    de:{abs:['immer','nie','niemals','alle','jeder','keiner','ständig','grundsätzlich'],modal:['muss','müssen','soll','sollte','darf nicht','unbedingt'],you:['du','dich','dir','dein','ihr','euch','euer']},
    en:{abs:['always','never','everyone','nobody','all','every','constantly','completely'],modal:['must','should','have to','need to','cannot','can’t'],you:['you','your','yours']},
    fr:{abs:['toujours','jamais','tout le monde','personne','tous','toutes'],modal:['doit','devez','devrait','il faut','ne peut pas'],you:['tu','vous','ton','ta','tes','votre','vos']},
    es:{abs:['siempre','nunca','todos','nadie','cada','completamente'],modal:['debe','debes','debería','hay que','no puede'],you:['tú','usted','ustedes','tu','tus','su','sus']},
    it:{abs:['sempre','mai','tutti','nessuno','ogni','completamente'],modal:['deve','devi','dovrebbe','bisogna','non può'],you:['tu','voi','tuo','tua','vostro','vostra']},
    nl:{abs:['altijd','nooit','iedereen','niemand','alle','elk','volledig'],modal:['moet','moeten','zou moeten','mag niet','kan niet'],you:['jij','je','jou','jouw','u','uw']}
  };
  const hit=(text,list)=>list.reduce((n,p)=>n+(text.toLowerCase().match(new RegExp(`(^|[^\\p{L}])${p.replace(/[.*+?^${}()|[\\]\\]/g,'\\$&')}([^\\p{L}]|$)`,'giu'))||[]).length,0);
  function compute(){
    const text=ui.source.value.trim(); if(!text) return;
    const lang=ui.lang.value,dict=MAP[lang]||MAP.en,pat=PAT[lang]||PAT.en;
    const sentences=splitSentences(text),results=sentences.map((s,i)=>classifySentence(s,lang,i));
    const words=text.match(WORD)||[],uniq=new Set(words.map(w=>w.toLocaleLowerCase(lang)));
    const bad=results.filter(r=>r.rating==='SCHLECHT').length,amb=results.filter(r=>r.rating==='AMBIVALENT').length,good=results.filter(r=>r.rating==='GUT').length,d2=results.filter(r=>r.d==='D-2+').length,hi=results.filter(r=>r.risk==='KRITISCH'||r.risk==='HOCH').length;
    const neg=results.reduce((n,r)=>n+r.neg,0),you=hit(text,pat.you),blame=Math.min(99,Math.round((you*(neg+1)/Math.max(1,sentences.length))*10));
    const construct=Math.round(good/Math.max(1,bad+amb+good)*100);
    const clarity=Math.max(0,Math.min(100,Math.round(100-(d2/Math.max(1,sentences.length)*42)-(bad/Math.max(1,sentences.length)*32)-(amb/Math.max(1,sentences.length)*12)+construct*.16)));
    const data=[[dict.words,words.length],[dict.avg,(words.length/Math.max(1,sentences.length)).toFixed(1)],[dict.lex,`${Math.round(uniq.size/Math.max(1,words.length)*100)}%`],[dict.abs,hit(text,pat.abs)],[dict.modal,hit(text,pat.modal)],[dict.blame,`${blame}`],[dict.quest,sentences.filter(s=>s.trim().endsWith('?')).length],[dict.excl,sentences.filter(s=>s.trim().endsWith('!')).length],[dict.d2,`${Math.round(d2/Math.max(1,sentences.length)*100)}%`],[dict.risk,hi],[dict.construct,`${construct}%`],[dict.clarity,`${clarity}/100`]];
    let box=document.getElementById('advancedStats'); if(!box){box=document.createElement('details');box.id='advancedStats';box.style.margin='16px 0';box.open=true;ui.metrics.insertAdjacentElement('afterend',box)}
    box.innerHTML=`<summary style="cursor:pointer;font-weight:800;margin:10px 0">${dict.title}</summary><div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(145px,1fr));gap:10px">${data.map(([k,v])=>`<div style="background:#0b1525;border:1px solid #243f68;border-radius:14px;padding:12px"><small style="display:block;color:#87a1c8">${k}</small><b style="font-size:1.25rem;color:#eaf4ff">${v}</b></div>`).join('')}</div><p style="font-size:.78rem;color:#718bad;margin:10px 2px 0">Heuristic indexes for editorial comparison; not clinical or causal measurements.</p>`;
  }
  ['click','change'].forEach(evt=>document.addEventListener(evt,e=>{if(e.target?.id==='analyzeBtn'||e.target?.id==='demoBtn'||e.target?.id==='languageSelect')setTimeout(compute,0)},true));
  const original=window.AntiGolem?.setCapturedText; if(original) window.AntiGolem.setCapturedText=(text)=>{original(text);setTimeout(compute,0)};
  window.AntiGolemAdvancedStats={compute};
})();
