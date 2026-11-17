(function(){
    'use strict';
    window.CAFeatures = {
        api: function(url, options){
            return fetch('/communityApp'+url, Object.assign({credentials:'same-origin'}, options||{}))
                .then(function(r){ if(!r.ok)throw new Error('Server returned '+r.status); return r.json(); });
        },
        session: function(){return this.api('/local/api/session');},
        post: function(url, values){
            return this.session().then(function(session){
                var body = new URLSearchParams();
                Object.keys(values).forEach(function(k){body.append(k,values[k]);});
                return window.CAFeatures.api(url,{
                    method:'POST',body:body,
                    headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8','X-CSRF-Token':session.csrfToken}
                });
            });
        },
        message: function(text,error){
            var el=document.getElementById('ca-message');if(!el)return;
            el.className='ca-feature-message'+(error?' error':'');el.textContent=text;
        },
        requireLogin: function(session){if(!session.authenticated){
            location.href='/communityApp/local/login';return false;}return true;},
        text: function(value){return value==null?'':String(value);},
        money: function(value){
            var n=Number(value);
            return Number.isFinite(n)?'$'+n.toFixed(2):'N/A';
        },
        date: function(value){
            if(value==null || value==='')return '';
            var d=null;
            if(Array.isArray(value) && value.length>=3){
                d=new Date(Number(value[0]),Number(value[1])-1,Number(value[2]),Number(value[3]||0),Number(value[4]||0),Number(value[5]||0));
            }else if(typeof value==='object'){
                if(value.time!=null && Number.isFinite(Number(value.time)))d=new Date(Number(value.time));
                var y=value.year, m=value.monthValue != null ? value.monthValue : value.month, day=value.dayOfMonth != null ? value.dayOfMonth : value.date != null ? value.date : value.day;
                if(typeof m==='string')m=({JANUARY:1,FEBRUARY:2,MARCH:3,APRIL:4,MAY:5,JUNE:6,JULY:7,AUGUST:8,SEPTEMBER:9,OCTOBER:10,NOVEMBER:11,DECEMBER:12})[m.toUpperCase()];
                if(!d && y!=null && m!=null && day!=null){
                    var legacy=Number(y)<1900 && value.monthValue==null;
                    d=new Date(legacy?Number(y)+1900:Number(y),legacy?Number(m):Number(m)-1,Number(day),Number(value.hours!=null?value.hours:value.hour||0),Number(value.minutes!=null?value.minutes:value.minute||0),Number(value.seconds!=null?value.seconds:value.second||0));
                }
            }else if(typeof value==='number')d=new Date(value);
            else if(typeof value==='string'){
                var text=value.trim();
                // MySQL local time has no offset; show the stored wall-clock time.
                var match=text.match(/^(\d{4}-\d\d-\d\d)[ T](\d\d:\d\d)/);
                if(match)return match[1]+' '+match[2];
                d=new Date(text);
            }
            if(!d || Number.isNaN(d.getTime()))return 'Date unavailable';
            var pad=function(x){return String(x).padStart(2,'0');};
            return d.getFullYear()+'-'+pad(d.getMonth()+1)+'-'+pad(d.getDate())+' '+pad(d.getHours())+':'+pad(d.getMinutes());
        },
        confirmAction: function(options){
            // Returns a promise that resolves true on confirmation, false on cancellation.
            options=options||{};
            return new Promise(function(resolve){
                var backdrop=document.createElement('div');backdrop.className='ca-modal-backdrop';
                var box=document.createElement('section');box.className='ca-modal';
                box.setAttribute('role','dialog');box.setAttribute('aria-modal','true');
                box.setAttribute('aria-labelledby','ca-modal-title');
                var title=document.createElement('h2');title.id='ca-modal-title';title.textContent=options.title||'Confirm Action';
                var detail=document.createElement('p');detail.textContent=options.message||'Continue?';
                var actions=document.createElement('div');actions.className='ca-modal-actions';
                var cancel=document.createElement('button');cancel.type='button';cancel.className='secondary';cancel.textContent='Cancel';
                var accept=document.createElement('button');accept.type='button';accept.textContent=options.confirmText||'Confirm';
                if(options.danger)accept.classList.add('ca-danger');
                actions.append(cancel,accept);box.append(title,detail,actions);backdrop.appendChild(box);
                var previous=document.activeElement;
                function finish(answer){
                    document.removeEventListener('keydown',keyDown);
                    backdrop.remove();
                    if(previous && typeof previous.focus==='function')previous.focus();
                    resolve(answer);
                }
                function keyDown(event){
                    if(event.key==='Escape'){event.preventDefault();finish(false);}
                    if(event.key==='Tab'){
                        var target=document.activeElement===accept?cancel:accept;
                        event.preventDefault();target.focus();
                    }
                }
                cancel.addEventListener('click',function(){finish(false);});
                accept.addEventListener('click',function(){finish(true);});
                backdrop.addEventListener('click',function(e){if(e.target===backdrop)finish(false);});
                document.addEventListener('keydown',keyDown);
                document.body.appendChild(backdrop);cancel.focus();
            });
        }
    };
})();
