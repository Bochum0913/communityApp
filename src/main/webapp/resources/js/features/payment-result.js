(function(){
 'use strict';
 var params=new URLSearchParams(location.search),id=params.get('orderId');
 var target=document.getElementById('payment-status');
 if(params.get('cancelled')==='1'){target.textContent='Checkout was cancelled. No payment has been confirmed. You can place a new order.';return;}
 if(!/^\d+$/.test(id||'')){target.textContent='Order number unavailable. Open Your Orders to check payment status.';return;}
 CAFeatures.session().then(function(s){if(!s.authenticated){target.textContent='Sign in to check your order status.';return;}
  return CAFeatures.api('/frontend/api/orders').then(function(r){
   if(!r.success){target.textContent='Unable to check status. View Your Orders shortly.';return;}
   var order=(r.data||[]).find(function(o){return String(o.order_id)===id;});
   if(!order){target.textContent='This order was not found for the signed-in account.';return;}
   if(order.payment_status==='PAID')target.textContent='Test payment confirmed. Order #'+id+' is ready for the merchant.';
   else target.textContent='Order #'+id+': '+(order.payment_status||'PENDING')+'. Payment confirmation may still be processing. Refresh Your Orders shortly.';
  });
 }).catch(function(){target.textContent='Payment confirmation is pending. Please check Your Orders.';});
})();