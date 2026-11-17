(function(){
 'use strict';
 CAFeatures.session().then(function(s){
  if(!CAFeatures.requireLogin(s))return;
  return CAFeatures.api('/frontend/api/points').then(function(r){
   var node=document.getElementById('ca-balance');
   if(!r.success){node.textContent=r.message||'Could not load points.';return;}
   node.replaceChildren();node.classList.add('ca-points-card');
   var title=document.createElement('span');title.textContent='Available Points';
   var value=document.createElement('strong');value.textContent=String(r.data);
   node.append(title,value);
  });
 }).catch(function(e){CAFeatures.message(e.message,true);});
})();
