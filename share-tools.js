(() => {
  async function shareText(text){
    text=String(text||'').trim();if(!text)return;
    try{
      if(globalThis.AntiGolemAndroid?.shareText){globalThis.AntiGolemAndroid.shareText(text);return;}
    }catch(e){console.warn(e)}
    if(navigator.share){try{await navigator.share({text});return}catch(e){if(e?.name==='AbortError')return;}}
    try{await navigator.clipboard.writeText(text);alert('Reply copied to clipboard.');}catch(e){console.warn(e)}
  }
  function mountShare(){
    const area=document.getElementById('agReplyText');if(!area)return;
    const copy=document.getElementById('agReplyCopy');if(!copy||document.getElementById('agReplyShare'))return;
    const b=document.createElement('button');b.id='agReplyShare';b.className='button ghost';b.type='button';b.textContent='↗ Share';b.onclick=()=>shareText(area.value);copy.insertAdjacentElement('afterend',b);
  }
  const mo=new MutationObserver(mountShare);mo.observe(document.documentElement,{childList:true,subtree:true});
  document.addEventListener('DOMContentLoaded',mountShare,{once:true});if(document.readyState!=='loading')mountShare();
  window.AntiGolemShare={shareText};
})();