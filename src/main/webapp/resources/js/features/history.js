(function(){
 'use strict';
 CAFeatures.session().then(function(s){
  if(!CAFeatures.requireLogin(s))return;
  return CAFeatures.api('/frontend/api/point-history').then(function(r){
   var node=document.getElementById('ca-ledger');node.replaceChildren();
   if(!r.success){node.textContent=r.message||'Could not load points history.';return;}
   if(!r.data || !r.data.length){node.textContent='No points history yet.';return;}
   r.data.forEach(function(entry){
    var card=document.createElement('div');card.className='ca-order ca-ledger-row';
    var title=document.createElement('strong');title.textContent='Order #'+entry.order_id;
    var delta=document.createElement('span');delta.className='ca-points-earned';
    delta.textContent=(Number(entry.delta)>=0?'+':'')+Number(entry.delta||0)+' points';
    var row=document.createElement('div');row.className='ca-order-heading';row.append(title,delta);
    var when=document.createElement('small');when.textContent=CAFeatures.date(entry.created_at);
    card.append(row,when);node.appendChild(card);
   });
  });
 }).catch(function(e){CAFeatures.message(e.message,true);});
})();
