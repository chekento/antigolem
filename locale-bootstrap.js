(() => {
  const supported = new Set(['de','en','fr','es','it','nl']);
  if (localStorage.getItem('antigolem.lang')) return;
  const candidates = Array.isArray(navigator.languages) && navigator.languages.length ? navigator.languages : [navigator.language || 'en'];
  const detected = candidates.map(x => String(x).toLowerCase().split('-')[0]).find(x => supported.has(x)) || 'en';
  localStorage.setItem('antigolem.lang', detected);
  document.documentElement.lang = detected;
})();
