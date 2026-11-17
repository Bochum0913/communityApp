$(function() {

	getlist();

	function getlist(e) {
		$.ajax({
			url : "/communityApp/shopadmin/getshoplist",
			type : "get",
			dataType : "json",
			success : function(data) {
				if (data.success) {
					handleList(data.shopList);
					handleUser(data.user);
				}
			}
		});
	}

	function handleUser(data) {
		$('#user-name').text(data.name);
	}

	function handleList(data) {
		var html = '';
		data.map(function(item, index) {
			html += '<div class="row row-shop"><div class="col-40">'
					+ $('<div/>').text(item.shopName || '').html() + '</div><div class="col-40">'
					+ shopStatus(item.enableStatus)
					+ '</div><div class="col-20">'
					+ goShop(item.enableStatus, item.shopId) + '</div></div>';

		});
		$('.shop-wrap').html(html);
	}

	function shopStatus(status) {
		if (status == 0) {
			return '<span class="ca-shop-status pending">Pending</span>';
		} else if (status == -1) {
			return '<span class="ca-shop-status rejected">Rejected</span>';
		} else if (status == 1) {
			return '<span class="ca-shop-status approved">Approved</span>';
		}
	}

	function goShop(status, id) {
		if (status == 1) {
			return '<a href="/communityApp/shopadmin/shopmanagement?shopId=' + id
					+ '">Manage Shop</a>';
		} else {
			return '';
		}
	}
});