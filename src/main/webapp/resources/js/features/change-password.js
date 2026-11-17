(function(){
 CAFeatures.session().then(function(s){if(!CAFeatures.requireLogin(s))return;
 var back=document.querySelector('.ca-back-link');var home=document.getElementById('ca-success-home');
 if(Number(s.userType)===2)home.href='/communityApp/shopadmin/dashboard';
 if(back)back.href='/communityApp/local/account';
 var current=document.getElementById('ca-current-email');if(current)current.textContent=s.email||'Not set';
 });
 document.getElementById('ca-form').addEventListener('submit',function(e){e.preventDefault();var f=new FormData(this);
 CAFeatures.post('/local/api/change-password',{oldPassword:f.get('oldPassword'),newPassword:f.get('newPassword')})
 .then(function(r){CAFeatures.message(r.message,!r.success);if(r.success){document.getElementById('ca-success-home').classList.remove('ca-hidden');var current=document.getElementById('ca-current-email');if(current)current.textContent=f.get('email');}if(r.success)document.getElementById('ca-form').reset();})
 .catch(function(e){CAFeatures.message(e.message,true);});});
})();
