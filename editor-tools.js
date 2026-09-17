(() => {
  const UI={
    de:{strict:'STRICT MODE',strictNote:'Strenge Bewertung aktiv: negative Normalisierung wird nicht durch positive Signalwörter ausgeglichen.',apply:'Verbesserungen anwenden',undo:'Rückgängig',reply:'Freundliche Kritik erstellen',copy:'Antwort kopieren',refine:'Mit lokaler KI verfeinern',working:'Lokale KI überarbeitet den Text …',applied:'Verbesserungen wurden auf den Text angewendet und neu analysiert.',fallback:'Lokale KI nicht verfügbar – sichere Regelkorrekturen wurden angewendet.',replyTitle:'Freundliche Wording-Kritik',replyHint:'Editierbarer Vorschlag zum Antworten auf den analysierten Post.',review:'Automatische Umschreibung bitte vor Veröffentlichung kurz gegenlesen.'},
    en:{strict:'STRICT MODE',strictNote:'Strict rating active: negative normalization is not cancelled out by positive signal words.',apply:'Apply improvements',undo:'Undo',reply:'Create friendly critique',copy:'Copy reply',refine:'Refine with local AI',working:'Local AI is rewriting the text …',applied:'Improvements were applied to the text and it was re-analyzed.',fallback:'Local AI unavailable – safe rule-based corrections were applied.',replyTitle:'Friendly wording critique',replyHint:'Editable reply suggestion for the analyzed post.',review:'Please review automatic rewrites briefly before publishing.'},
    fr:{strict:'MODE STRICT',strictNote:'Évaluation stricte : un cadrage négatif n’est pas annulé par des mots positifs.',apply:'Appliquer les améliorations',undo:'Annuler',reply:'Créer une critique bienveillante',copy:'Copier la réponse',refine:'Affiner avec IA locale',working:'L’IA locale reformule le texte …',applied:'Les améliorations ont été appliquées et le texte a été réanalysé.',fallback:'IA locale indisponible – corrections sûres basées sur des règles appliquées.',replyTitle:'Critique bienveillante du wording',replyHint:'Proposition modifiable pour répondre au post analysé.',review:'Relisez brièvement la reformulation automatique avant publication.'},
    es:{strict:'MODO ESTRICTO',strictNote:'Evaluación estricta: el encuadre negativo no se compensa con palabras positivas.',apply:'Aplicar mejoras',undo:'Deshacer',reply:'Crear crítica amable',copy:'Copiar respuesta',refine:'Mejorar con IA local',working:'La IA local está reescribiendo el texto …',applied:'Las mejoras se aplicaron y el texto fue analizado de nuevo.',fallback:'IA local no disponible; se aplicaron correcciones seguras basadas en reglas.',replyTitle:'Crítica amable del wording',replyHint:'Propuesta editable para responder al post analizado.',review:'Revisa brevemente la reescritura automática antes de publicar.'},
    it:{strict:'MODALITÀ RIGOROSA',strictNote:'Valutazione rigorosa: il framing negativo non viene compensato da parole positive.',apply:'Applica miglioramenti',undo:'Annulla',reply:'Crea critica gentile',copy:'Copia risposta',refine:'Migliora con IA locale',working:'L’IA locale sta riscrivendo il testo …',applied:'I miglioramenti sono stati applicati e il testo è stato rianalizzato.',fallback:'IA locale non disponibile – applicate correzioni sicure basate su regole.',replyTitle:'Critica gentile del wording',replyHint:'Proposta modificabile per rispondere al post analizzato.',review:'Rileggi brevemente la riscrittura automatica prima di pubblicare.'},
    nl:{strict:'STRIKTE MODUS',strictNote:'Strenge beoordeling: negatieve normalisering wordt niet gecompenseerd door positieve signaalwoorden.',apply:'Verbeteringen toepassen',undo:'Ongedaan maken',reply:'Vriendelijke kritiek maken',copy:'Antwoord kopiëren',refine:'Verfijnen met lokale AI',working:'Lokale AI herschrijft de tekst …',applied:'Verbeteringen zijn toegepast en de tekst is opnieuw geanalyseerd.',fallback:'Lokale AI niet beschikbaar – veilige regelcorrecties zijn toegepast.',replyTitle:'Vriendelijke wording-kritiek',replyHint:'Bewerkbaar antwoordvoorstel voor de geanalyseerde post.',review:'Lees de automatische herschrijving kort na voor publicatie.'}
  };

  const STRICT_PAT={
    de:{abs:['immer','nie','niemals','alle','jeder','keiner','ständig','grundsätzlich'],modal:['muss','müssen','soll','sollte','unbedingt','darf nicht']},
    en:{abs:['always','never','everyone','nobody','all','every','constantly','completely'],modal:['must','should','have to','need to','cannot','can’t']},
    fr:{abs:['toujours','jamais','tout le monde','personne','tous','toutes'],modal:['doit','devez','devrait','il faut','ne peut pas']},
    es:{abs:['siempre','nunca','todos','nadie','cada','completamente'],modal:['debe','debes','debería','hay que','no puede']},
    it:{abs:['sempre','mai','tutti','nessuno','ogni','completamente'],modal:['deve','devi','dovrebbe','bisogna','non può']},
    nl:{abs:['altijd','nooit','iedereen','niemand','alle','elk','volledig'],modal:['moet','moeten','zou moeten','mag niet','kan niet']}
  };

  const REWRITE={
    de:[[/(\b)niemals\b/giu,'$1selten'],[/(\b)nie\b/giu,'$1selten'],[/(\b)immer\b/giu,'$1häufig'],[/(\b)jeder\b/giu,'$1viele'],[/(\b)keiner\b/giu,'$1wenige'],[/\bman\b/giu,'die verantwortliche Person'],[/\bunmöglich\b/giu,'derzeit schwer umsetzbar'],[/\bmachtlos\b/giu,'mit begrenztem Handlungsspielraum'],[/\bBedrohungen?\b/giu,'Risiko'],[/\bProbleme?\b/giu,'Herausforderung'],[/\bFehler\b/giu,'Verbesserungspunkt'],[/\bscheitern\b/giu,'das Ziel verfehlen']],
    en:[[/(\b)never\b/giu,'$1rarely'],[/(\b)always\b/giu,'$1often'],[/(\b)everyone\b/giu,'$1many people'],[/(\b)nobody\b/giu,'$1few people'],[/\bimpossible\b/giu,'currently difficult to achieve'],[/\bpowerless\b/giu,'with limited room to act'],[/\bthreats?\b/giu,'risk'],[/\bproblems?\b/giu,'challenge'],[/\berrors?\b/giu,'improvement point'],[/\bfail(?:s|ed|ing)?\b/giu,'does not achieve the intended result']],
    fr:[[/\bjamais\b/giu,'rarement'],[/\btoujours\b/giu,'souvent'],[/\bpersonne\b/giu,'peu de personnes'],[/\bimpossible\b/giu,'difficile à réaliser actuellement'],[/\bimpuissant(?:e)?\b/giu,'avec une marge d’action limitée'],[/\bmenaces?\b/giu,'risque'],[/\bproblèmes?\b/giu,'défi'],[/\berreurs?\b/giu,'point à améliorer'],[/\béchoue(?:r|nt)?\b/giu,'n’atteint pas le résultat attendu']],
    es:[[/\bnunca\b/giu,'rara vez'],[/\bsiempre\b/giu,'a menudo'],[/\bnadie\b/giu,'pocas personas'],[/\bimposible\b/giu,'difícil de lograr actualmente'],[/\bimpotente\b/giu,'con margen de acción limitado'],[/\bamenazas?\b/giu,'riesgo'],[/\bproblemas?\b/giu,'reto'],[/\berrores?\b/giu,'punto de mejora'],[/\bfracasa(?:r|n)?\b/giu,'no alcanza el resultado previsto']],
    it:[[/\bmai\b/giu,'raramente'],[/\bsempre\b/giu,'spesso'],[/\bnessuno\b/giu,'poche persone'],[/\bimpossibile\b/giu,'attualmente difficile da realizzare'],[/\bimpotente\b/giu,'con margine d’azione limitato'],[/\bminacce?\b/giu,'rischio'],[/\bproblemi?\b/giu,'sfida'],[/\berrori?\b/giu,'punto di miglioramento'],[/\bfallisce?\b/giu,'non raggiunge il risultato previsto']],
    nl:[[/\bnooit\b/giu,'zelden'],[/\baltijd\b/giu,'vaak'],[/\bniemand\b/giu,'weinig mensen'],[/\bonmogelijk\b/giu,'momenteel moeilijk haalbaar'],[/\bmachteloos\b/giu,'met beperkte handelingsruimte'],[/\bdreigingen?\b/giu,'risico'],[/\bproblemen?\b/giu,'uitdaging'],[/\bfouten?\b/giu,'verbeterpunt'],[/\bmislukt?\b/giu,'bereikt het beoogde resultaat niet']]
  };

  let undoText='';
  const baseClassify=classifySentence;
  const countPattern=(lower,list)=>list.reduce((n,p)=>n+(hasTerm(lower,p)?1:0),0);
  classifySentence=function(sentence,lang,index){
    const r=baseClassify(sentence,lang,index);
    const p=STRICT_PAT[lang]||STRICT_PAT.en;
    const lower=sentence.toLocaleLowerCase(lang);
    const abs=countPattern(lower,p.abs),modal=countPattern(lower,p.modal);
    r.absolutism=abs;r.modalPressure=modal;r.strict=true;
    if(r.d==='D-2+') {r.rating='SCHLECHT';r.risk='KRITISCH';}
    else if(r.neg>0){r.rating='SCHLECHT';r.risk=(r.neg>=2||abs>0||modal>0)?'KRITISCH':'HOCH';}
    else if(r.d==='D-1'){r.rating='AMBIVALENT';r.risk='HOCH';}
    else if(r.diff>0||r.over>0||abs>0||modal>0){r.rating='AMBIVALENT';r.risk=(r.diff+r.over+abs+modal>1)?'HOCH':'MITTEL';}
    else if(r.pos>0){r.rating='GUT';r.risk='-';}
    else {r.rating='NEUTRAL';r.risk='-';}
    return r;
  };

  function dict(){return UI[ui.lang.value]||UI.en}
  function results(){return splitSentences(ui.source.value.trim()).map((s,i)=>classifySentence(s,ui.lang.value,i))}
  function deterministicRewrite(text,lang){let out=text;for(const [re,to] of (REWRITE[lang]||REWRITE.en))out=out.replace(re,to);return out}
  function chunks(text,max=5200){const s=splitSentences(text),out=[];let cur='';for(const line of s){if(cur&&cur.length+line.length+1>max){out.push(cur);cur=''}cur+=(cur?' ':'')+line}if(cur)out.push(cur);return out}
  function languageName(lang){return ({de:'German',en:'English',fr:'French',es:'Spanish',it:'Italian',nl:'Dutch'})[lang]||'English'}
  function stripModelWrapper(s){return String(s||'').trim().replace(/^```(?:text|markdown)?\s*/i,'').replace(/```$/,'').trim()}

  async function smartRewrite(text,lang,setStatus){
    if(!window.AntiGolemLocalAI?.ask)return null;
    const parts=chunks(text),out=[];
    for(let i=0;i<parts.length;i++){
      setStatus(`${dict().working} ${i+1}/${parts.length}`);
      const prompt=`Rewrite the following ${languageName(lang)} text for publication. Preserve factual claims, names, numbers, links, chronology, viewpoint and intended meaning. Do not add facts. Apply a STRICT wording audit: make subjects and references explicit where possible; reduce unnecessary absolutism and coercive modal pressure; avoid presenting failure, powerlessness or threat as the default; retain necessary warnings but bind them to concrete conditions, agency or next steps; reduce vague responsibility diffusion; keep tone respectful and precise. Preserve paragraph structure when practical. Output ONLY the revised text, no commentary.\n\nTEXT:\n${parts[i]}`;
      const r=await window.AntiGolemLocalAI.ask(prompt,setStatus);if(!r)return null;out.push(stripModelWrapper(r));
    }
    return out.join('\n\n');
  }

  function friendlyReply(lang,rs){
    const bad=rs.filter(r=>r.rating==='SCHLECHT').length,amb=rs.filter(r=>r.rating==='AMBIVALENT').length,d2=rs.filter(r=>r.d==='D-2+').length,d1=rs.filter(r=>r.d==='D-1').length,neg=rs.reduce((n,r)=>n+r.neg,0),diff=rs.reduce((n,r)=>n+r.diff+r.over,0),abs=rs.reduce((n,r)=>n+(r.absolutism||0),0);
    const issues=[];
    const add=(de,en,fr,es,it,nl)=>issues.push(({de,en,fr,es,it,nl})[lang]||en);
    if(neg)add('negative Erwartungen teils als Normalzustand','some negative expectations are framed as the default','certaines attentes négatives sont présentées comme la norme','algunas expectativas negativas se presentan como norma','alcune aspettative negative sono presentate come norma','sommige negatieve verwachtingen als standaard worden neergezet');
    if(d2||d1)add('einige Bezüge oder Pronomen nicht ganz eindeutig sind','some references or pronouns are not fully explicit','certaines références ou certains pronoms manquent de clarté','algunas referencias o pronombres no son del todo claros','alcuni riferimenti o pronomi non sono del tutto espliciti','sommige verwijzingen of voornaamwoorden niet helemaal expliciet zijn');
    if(diff)add('Verantwortung teilweise sehr allgemein verteilt wird','responsibility is sometimes framed too generally','la responsabilité est parfois formulée de façon trop générale','la responsabilidad se formula a veces de forma demasiado general','la responsabilità viene talvolta formulata in modo troppo generico','verantwoordelijkheid soms te algemeen wordt geformuleerd');
    if(abs)add('einige Aussagen sehr absolut klingen','some statements sound very absolute','certaines formulations semblent très absolues','algunas afirmaciones suenan muy absolutas','alcune affermazioni suonano molto assolute','sommige uitspraken erg absoluut klinken');
    const issue=issues.slice(0,3).join(lang==='de'?', ':lang==='fr'?', ':', ');
    const templates={
      de:`Danke für den Beitrag. Inhaltlich finde ich den Punkt gut nachvollziehbar. Beim Wording ist mir allerdings aufgefallen, dass ${issue||'einige Formulierungen unnötig hart oder unpräzise wirken'}. Könntest du die betreffenden Stellen etwas präziser, weniger absolut und stärker lösungsorientiert formulieren? Klare Subjekte, eindeutige Bezüge und konkrete nächste Schritte würden die Aussage aus meiner Sicht konstruktiver und leichter einzuordnen machen.${bad+amb?` AntiGolem hat dabei ${bad} klar problematische und ${amb} ambivalente Formulierung(en) markiert.`:''}`,
      en:`Thanks for the post. I can follow the substantive point well. On the wording, though, I noticed that ${issue||'some formulations come across as unnecessarily harsh or imprecise'}. Could you make those parts a little more precise, less absolute and more solution-oriented? Clear subjects, explicit references and concrete next steps would make the message easier to interpret and more constructive.${bad+amb?` AntiGolem flagged ${bad} clearly problematic and ${amb} ambivalent formulation(s).`:''}`,
      fr:`Merci pour ce post. Le fond est bien compréhensible. Sur la formulation, j’ai toutefois remarqué que ${issue||'certains passages paraissent inutilement durs ou imprécis'}. Pourrais-tu les rendre un peu plus précis, moins absolus et davantage orientés vers les solutions ? Des sujets clairement nommés, des références explicites et des prochaines étapes concrètes rendraient le message plus constructif et plus facile à interpréter.`,
      es:`Gracias por la publicación. El punto de fondo se entiende bien. En cuanto al wording, he notado que ${issue||'algunas formulaciones resultan innecesariamente duras o imprecisas'}. ¿Podrías hacer esas partes un poco más precisas, menos absolutas y más orientadas a soluciones? Sujetos claros, referencias explícitas y próximos pasos concretos harían el mensaje más constructivo y fácil de interpretar.`,
      it:`Grazie per il post. Il punto di fondo è comprensibile. Nel wording, però, ho notato che ${issue||'alcune formulazioni risultano inutilmente dure o poco precise'}. Potresti renderle un po’ più precise, meno assolute e più orientate alle soluzioni? Soggetti chiari, riferimenti espliciti e prossimi passi concreti renderebbero il messaggio più costruttivo e facile da interpretare.`,
      nl:`Dank voor de post. De inhoudelijke kern is goed te volgen. Bij de wording viel me wel op dat ${issue||'sommige formuleringen onnodig hard of onnauwkeurig overkomen'}. Zou je die delen iets preciezer, minder absoluut en meer oplossingsgericht kunnen formuleren? Duidelijke onderwerpen, expliciete verwijzingen en concrete vervolgstappen zouden de boodschap constructiever en makkelijker te duiden maken.`
    };
    return templates[lang]||templates.en;
  }

  function mount(){
    if(document.getElementById('editorTools'))return;
    const style=document.createElement('style');style.textContent=`#editorTools{margin:14px 0;padding:14px;border:1px solid #304d78;border-radius:18px;background:linear-gradient(145deg,#0a1728,#0c1220)}#editorTools .eg-head{display:flex;justify-content:space-between;gap:12px;align-items:center;flex-wrap:wrap}.eg-strict{font-size:.7rem;font-weight:900;letter-spacing:.12em;color:#ffb86b;border:1px solid #7a4a23;background:#2b1b0e;border-radius:999px;padding:5px 9px}.eg-tools{display:flex;gap:8px;flex-wrap:wrap;margin-top:12px}.eg-note{font-size:.76rem;color:#8da6c9;margin-top:8px}.eg-reply{display:none;margin-top:12px}.eg-reply textarea{width:100%;min-height:155px;resize:vertical;background:#07111f;color:#eaf4ff;border:1px solid #2b466c;border-radius:14px;padding:12px;box-sizing:border-box;line-height:1.5}.eg-reply-actions{display:flex;gap:8px;flex-wrap:wrap;margin-top:8px}`;document.head.appendChild(style);
    const box=document.createElement('section');box.id='editorTools';box.innerHTML=`<div class="eg-head"><div><b id="egStrict"></b><div id="egStrictNote" class="eg-note"></div></div><span class="eg-strict">STRICT</span></div><div class="eg-tools"><button id="egApply" class="button primary" type="button"></button><button id="egUndo" class="button ghost" type="button" disabled></button><button id="egReplyBtn" class="button ghost" type="button"></button></div><div id="egStatus" class="eg-note"></div><div id="egReply" class="eg-reply"><b id="egReplyTitle"></b><div id="egReplyHint" class="eg-note"></div><textarea id="egReplyText"></textarea><div class="eg-reply-actions"><button id="egCopy" class="button ghost" type="button"></button><button id="egRefine" class="button ghost" type="button"></button></div></div>`;
    ui.report.parentNode.insertBefore(box,ui.report);
    box.querySelector('#egApply').addEventListener('click',applyImprovement);
    box.querySelector('#egUndo').addEventListener('click',undoImprovement);
    box.querySelector('#egReplyBtn').addEventListener('click',makeReply);
    box.querySelector('#egCopy').addEventListener('click',()=>navigator.clipboard.writeText(box.querySelector('#egReplyText').value));
    box.querySelector('#egRefine').addEventListener('click',refineReply);
    labels();
  }
  function labels(){if(!document.getElementById('editorTools'))return;const x=dict();document.getElementById('egStrict').textContent=x.strict;document.getElementById('egStrictNote').textContent=x.strictNote;document.getElementById('egApply').textContent=x.apply;document.getElementById('egUndo').textContent=x.undo;document.getElementById('egReplyBtn').textContent=x.reply;document.getElementById('egCopy').textContent=x.copy;document.getElementById('egRefine').textContent=x.refine;document.getElementById('egReplyTitle').textContent=x.replyTitle;document.getElementById('egReplyHint').textContent=x.replyHint}

  async function applyImprovement(){
    const text=ui.source.value.trim();if(!text){ui.source.focus();return}const status=document.getElementById('egStatus'),btn=document.getElementById('egApply');undoText=ui.source.value;btn.disabled=true;status.textContent=dict().working;
    let improved=null;
    try{if(window.AntiGolemLocalAI?.isPotentiallyAvailable())improved=await smartRewrite(text,ui.lang.value,s=>status.textContent=s)}catch(e){console.warn(e)}
    if(!improved||improved.length<Math.min(20,text.length*.25)){improved=deterministicRewrite(text,ui.lang.value);status.textContent=dict().fallback}else status.textContent=dict().applied;
    ui.source.value=improved;document.getElementById('egUndo').disabled=false;updatePreview();run();status.textContent+=` ${dict().review}`;btn.disabled=false;
  }
  function undoImprovement(){if(!undoText)return;const current=ui.source.value;ui.source.value=undoText;undoText=current;updatePreview();run();document.getElementById('egStatus').textContent='↶'}
  function makeReply(){const box=document.getElementById('egReply');box.style.display='block';document.getElementById('egReplyText').value=friendlyReply(ui.lang.value,results());box.scrollIntoView({behavior:'smooth',block:'nearest'})}
  async function refineReply(){const ta=document.getElementById('egReplyText'),status=document.getElementById('egStatus');if(!window.AntiGolemLocalAI?.ask){status.textContent=dict().fallback;return}const prompt=`Improve the following ${languageName(ui.lang.value)} reply to an online post. Keep it friendly, respectful, non-accusatory and concise. It should ask for clearer, less harmful, less absolute and more solution-oriented wording without claiming psychological harm as a fact. Preserve the substantive request. Output ONLY the revised reply.\n\n${ta.value}`;status.textContent=dict().working;const r=await window.AntiGolemLocalAI.ask(prompt,s=>status.textContent=s);if(r)ta.value=stripModelWrapper(r);status.textContent=r?dict().applied:dict().fallback}

  const oldRun=run;run=function(){oldRun();setTimeout(()=>window.AntiGolemAdvancedStats?.compute?.(),0)};
  document.addEventListener('DOMContentLoaded',mount,{once:true});if(document.readyState!=='loading')mount();ui.lang.addEventListener('change',labels);
})();