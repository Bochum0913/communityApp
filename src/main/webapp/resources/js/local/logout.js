$(function(){
    $('#log-out').on('click', function(e){
        e.preventDefault();
        fetch('/communityApp/local/api/session', {credentials:'same-origin'})
          .then(function(r){return r.json();})
          .then(function(session){
              return fetch('/communityApp/local/logout', {
                  method:'POST',credentials:'same-origin',
                  headers:{'X-CSRF-Token':session.csrfToken}
              });
          }).then(function(r){return r.json();})
          .then(function(result){
              if(result.success){window.location.href='/communityApp/local/login';}
              else {alert(result.message||'Unable to sign out.');}
          }).catch(function(){alert('Unable to sign out.');});
    });
});
