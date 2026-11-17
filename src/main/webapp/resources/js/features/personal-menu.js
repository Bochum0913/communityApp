(function () {
    'use strict';

    function initialize() {
        if (!window.CAFeatures || !document.querySelector('.panel-right .content-block')) return;
        CAFeatures.session().then(function (session) {
            var panel = document.querySelector('.panel-right .content-block');
            if (!panel) return;
            var authenticated = !!session.authenticated;
            var isMerchant = authenticated && Number(session.userType) === 2;
            var label = panel.querySelector('.ca-session-label');
            if (label) {
                label.textContent = authenticated
                    ? (String(session.username || 'Account') + ' (' + (isMerchant ? 'Merchant' : 'Customer') + ')')
                    : 'Guest';
                label.style.overflowWrap = 'anywhere';
            }

            // Rebuild role-specific menu after session verification, not based on query parameters.
            var items = authenticated
                ? (isMerchant ? [
                    ['Merchant Dashboard', '/communityApp/shopadmin/dashboard'],
                    ['Manage Shops', '/communityApp/shopadmin/shoplist'],
                    ['Merchant Orders', '/communityApp/shopadmin/ordermanagement'],
                    ['Account Settings', '/communityApp/local/account'],
                    ['Change Password', '/communityApp/local/changepsw'],
                    ['Log Out', '#', 'log-out']
                ] : [
                    ['Customer Home', '/communityApp/frontend/index'],
                    ['Account Settings', '/communityApp/local/account'],
                    ['Change Password', '/communityApp/local/changepsw'],
                    ['Your Orders', '/communityApp/frontend/myrecord'],
                    ['Your Points', '/communityApp/frontend/mypoint'],
                    ['Points History', '/communityApp/frontend/pointrecord'],
                    ['Log Out', '#', 'log-out']
                ]) : [
                    ['Sign In', '/communityApp/local/login'],
                    ['Create Account', '/communityApp/local/register']
                ];
            Array.prototype.forEach.call(Array.from(panel.children), function (child) {
                if (!child.classList.contains('ca-session-label')) child.remove();
            });
            items.forEach(function (item) {
                var row = document.createElement('p');
                var link = document.createElement('a');
                link.href = item[1];
                link.className = 'close-panel';
                link.textContent = item[0];
                if (item[2]) link.id = item[2];
                row.appendChild(link);
                panel.appendChild(row);
            });
            // Existing logout.js delegated only to an element that no longer exists.
            // Bind directly to the newly rendered link.
            var logout = panel.querySelector('#log-out');
            if (logout) logout.addEventListener('click', function (event) {
                event.preventDefault();
                logout.style.pointerEvents = 'none';
                CAFeatures.post('/local/logout', {}).then(function (result) {
                    if (result.success) window.location.href = '/communityApp/local/login';
                    else {
                        alert(result.message || 'Unable to sign out.');
                        logout.style.pointerEvents = '';
                    }
                }).catch(function () {
                    alert('Unable to sign out.');
                    logout.style.pointerEvents = '';
                });
            });
        }).catch(function () {
            var label = document.querySelector('.panel-right .ca-session-label');
            if (label) label.textContent = 'Account unavailable';
        });
    }

    if (window.CAFeatures) initialize();
    else {
        var script = document.createElement('script');
        script.src = '/communityApp/resources/js/common/communityapp-features.js';
        script.onload = initialize;
        document.body.appendChild(script);
    }
})();
