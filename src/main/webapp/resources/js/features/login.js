(function(){
 document.getElementById('ca-form').addEventListener('submit',function(e){
 e.preventDefault();var f=new FormData(this);
 CAFeatures.post('/local/api/login',{username:f.get('username'),password:f.get('password')}).then(function(r){
 if(r.success){CAFeatures.session().then(function(s){location.href=Number(s.userType)===2?'/communityApp/shopadmin/dashboard':'/communityApp/frontend/index';}).catch(function(){location.href='/communityApp/frontend/index';});}else CAFeatures.message(r.message,true);
 }).catch(function(e){CAFeatures.message(e.message,true);});
 });
})();
