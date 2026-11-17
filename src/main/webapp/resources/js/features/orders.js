(function(){
 'use strict';
 var wrap=document.getElementById('ca-orders');
 var merchantPage=location.pathname.indexOf('/shopadmin/')!==-1;
 function draw(){
  CAFeatures.session().then(function(session){
   if(!CAFeatures.requireLogin(session))return;
   if(merchantPage && Number(session.userType)!==2){
    location.replace('/communityApp/frontend/index');return;
   }
   return CAFeatures.api('/frontend/api/orders').then(function(response){
    if(!response.success){CAFeatures.message(response.message||'Could not load orders.',true);return;}
    wrap.replaceChildren();
    if(!response.data || !response.data.length){
     var empty=document.createElement('p');empty.textContent='No orders yet.';wrap.appendChild(empty);return;
    }
    response.data.forEach(function(order){
     var card=document.createElement('article');card.className='ca-order ca-order-card';
     var heading=document.createElement('div');heading.className='ca-order-heading';
     var title=document.createElement('strong');title.textContent='Order #'+order.order_id+' - '+(order.product_name||'Product');
     var status=document.createElement('span');status.className='ca-order-status status-'+String(order.status||'').toLowerCase();
     status.textContent=order.status||'UNKNOWN';heading.append(title,status);
     var details=document.createElement('p');details.className='ca-order-summary';
     var count=Number(order.quantity)||0;
     details.textContent='Quantity: '+count+'  |  Total: '+CAFeatures.money(order.total_amount)+'  |  Payment: '+(order.payment_status||'NOT_REQUIRED');
     var stamp=document.createElement('small');stamp.textContent='Placed: '+CAFeatures.date(order.created_at);
     card.append(heading,details,stamp);
     if(order.status==='PLACED' && (merchantPage || order.payment_status==='NOT_REQUIRED')){
      var action=document.createElement('button');action.type='button';
      var label=merchantPage?'Mark Completed':'Cancel Order';
      action.textContent=label;
      action.addEventListener('click',function(){
       CAFeatures.confirmAction({
        title:merchantPage?'Complete Order #'+order.order_id:'Cancel Order #'+order.order_id,
        message:merchantPage?'Confirm that this order was fulfilled? Reward points will be credited once.':'Cancel this order? This action cannot be undone.',
        confirmText:merchantPage?'Complete Order':'Cancel Order',danger:!merchantPage
       }).then(function(confirmed){
        if(!confirmed)return;
        action.disabled=true;action.textContent='Saving...';
        var endpoint=merchantPage?'/frontend/api/complete-order':'/frontend/api/cancel-order';
        CAFeatures.post(endpoint,{orderId:order.order_id}).then(function(result){
         if(result.success){CAFeatures.message(result.data||'Order updated.');draw();}
         else {CAFeatures.message(result.message||'Action failed.',true);action.disabled=false;action.textContent=label;}
        }).catch(function(error){CAFeatures.message(error.message,true);action.disabled=false;action.textContent=label;});
       });
      });card.appendChild(action);
     }
     wrap.appendChild(card);
    });
   });
  }).catch(function(error){CAFeatures.message(error.message,true);});
 }
 draw();
})();
