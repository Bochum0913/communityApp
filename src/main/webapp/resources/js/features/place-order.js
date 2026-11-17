(function(){
 'use strict';
 var button=document.getElementById('ca-place-order');if(!button)return;
 var qty=document.getElementById('ca-qty'),message=document.getElementById('ca-order-message');
 var overlay=document.getElementById('ca-order-overlay'),confirmButton=document.getElementById('ca-confirm-order');
 var cancelButton=document.getElementById('ca-cancel-order');
 var productId=new URLSearchParams(location.search).get('productId');
 var quantity=1;
 function close(){overlay.classList.add('ca-hidden');button.focus();}
 cancelButton.addEventListener('click',close);
 overlay.addEventListener('click',function(e){if(e.target===overlay)close();});
 document.addEventListener('keydown',function(e){if(e.key==='Escape'&&!overlay.classList.contains('ca-hidden')&&!confirmButton.disabled)close();});
 button.addEventListener('click',function(){
  quantity=Number(qty.value);
  if(!/^\d+$/.test(productId||'')||!Number.isInteger(quantity)||quantity<1||quantity>20){message.textContent='Enter a quantity from 1 to 20.';return;}
  CAFeatures.session().then(function(s){if(!CAFeatures.requireLogin(s))return;
   if(Number(s.userType)!==1){message.textContent='Only Customer accounts can pay for orders.';return;}
   var name=document.getElementById('product-display-name').textContent||'Product';
   var priceText=document.getElementById('promotionPrice').textContent.trim();
   var price=Number(priceText.replace(/[^0-9.]/g,''));
   document.getElementById('ca-confirm-summary').textContent=name+' x '+quantity;
   document.getElementById('ca-confirm-total').textContent=Number.isFinite(price)&&price>0?'Estimated total: $'+(price*quantity).toFixed(2):'Final price is calculated on the server.';
   overlay.classList.remove('ca-hidden');confirmButton.focus();
  }).catch(function(e){message.textContent=e.message;});
 });
 confirmButton.addEventListener('click',function(){
  confirmButton.disabled=true;button.disabled=true;confirmButton.textContent='Opening Stripe...';
  CAFeatures.post('/frontend/api/stripe-checkout',{productId:productId,quantity:quantity}).then(function(r){
   if(!r.success || !r.data || !r.data.checkoutUrl)throw new Error(r.message||'Unable to begin Checkout.');
   var url=new URL(r.data.checkoutUrl);
   if(url.protocol!=='https:' || url.hostname!=='checkout.stripe.com')throw new Error('Unexpected Checkout host.');
   window.location.assign(url.href);
  }).catch(function(e){
   message.textContent=e.message;close();button.disabled=false;confirmButton.disabled=false;
   confirmButton.textContent='Continue to Stripe';
  });
 });
})();
