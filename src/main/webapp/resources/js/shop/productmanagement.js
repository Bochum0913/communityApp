$(function () {

    var listUrl = '/communityApp/shopadmin/getproductlistbyshop?pageIndex=1&pageSize=999';

    var statusUrl = '/communityApp/shopadmin/modifyproduct';
    getList();

    /** Load and render products for the selected shop. */
    function getList() {

        $.getJSON(listUrl, function (data) {
            if (data.success) {
                var productList = data.productList;
                var tempHtml = '';



                productList.map(function (item, index) {
                    var textOp = "not available";
                    var contraryStatus = 0;
                    if (item.enableStatus == 0) {

                        textOp = "available";
                        contraryStatus = 1;
                    } else {
                        contraryStatus = 0;
                    }

                    var safeText = function(value) { return $('<div/>').text(value == null ? '' : String(value)).html(); };
                    var mainImg = item.imgAddr ? String(item.imgAddr).replace(/\\/g, '/') : '';
                    var thumb = mainImg ? '<img class="ca-product-thumb" src="' + mainImg.replace(/&/g, '&amp;').replace(/"/g, '&quot;') + '" alt="" onerror="this.style.display=\'none\'">' : '<span class="ca-product-thumb-placeholder">☕</span>';
                    var price = item.promotionPrice != null ? item.promotionPrice : item.normalPrice;
                    var priceText = (price != null && !isNaN(Number(price)) ? '$' + Number(price).toFixed(2) : 'N/A');
                    var originalPriceText = (item.normalPrice != null && item.promotionPrice != null && Number(item.normalPrice) > Number(item.promotionPrice)) ? '<del class="ca-old-price">$' + Number(item.normalPrice).toFixed(2) + '</del>' : ''; 
                    var available = item.enableStatus == 1;
                    tempHtml += '<div class="row row-product ca-product-row">'
                        + '<div class="col-33 ca-product-cell">' + thumb + '<span class="ca-product-name">' + safeText(item.productName) + '</span></div>'
                        + '<div class="col-20 ca-product-metrics"><strong>' + priceText + '</strong>' + originalPriceText + '<small>' + safeText(item.point) + ' pts</small></div>'
                        + '<div class="col-40 ca-product-actions"><span class="ca-status ' + (available ? 'is-live' : 'is-off') + '">' + (available ? 'Available' : 'Unavailable') + '</span>'
                        + '<a href="#" class="edit" data-id="' + item.productId + '" data-status="' + item.enableStatus + '">Edit</a>'
                        + '<a href="#" class="status" data-id="' + item.productId + '" data-status="' + contraryStatus + '">' + (available ? 'Disable' : 'Enable') + '</a>'
                        + '<a href="#" class="preview" data-id="' + item.productId + '" data-status="' + item.enableStatus + '">Preview</a></div></div>';
                });

                $('.product-wrap').html(tempHtml);
            }
        });
    }

    $('.product-wrap')
        .on(
            'click',
            'a',
            function (e) {
                e.preventDefault();
                var target = $(e.currentTarget);
                if (target.hasClass('edit')) {

                    window.location.href = '/communityApp/shopadmin/productoperation?productId='
                        + e.currentTarget.dataset.id;
                } else if (target.hasClass('status')) {

                    changeItemStatus(e.currentTarget.dataset.id,
                        e.currentTarget.dataset.status);
                } else if (target.hasClass('preview')) {

                    window.location.href = '/communityApp/frontend/productdetail?productId='
                        + e.currentTarget.dataset.id;
                }
            });

    // Show a styled dialog without changing the existing status API.
    function changeItemStatus(id, enableStatus) {
        var enabling = String(enableStatus) === '1';
        var action = enabling ? 'Enable' : 'Disable';
        var $overlay = $('<div class="ca-confirm-overlay"></div>');
        var $dialog = $('<div class="ca-confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="ca-dialog-title"></div>');
        var $icon = $('<div class="ca-confirm-icon"></div>').text(enabling ? '+' : '!');
        var $heading = $('<h3 id="ca-dialog-title"></h3>').text(action + ' Product');
        var $message = $('<p></p>').text(enabling
            ? 'This product will become available to customers.'
            : 'Customers will no longer see this product as available. You can enable it again later.');
        var $buttons = $('<div class="ca-confirm-actions"></div>');
        var $cancel = $('<button type="button" class="ca-confirm-cancel">Cancel</button>');
        var $accept = $('<button type="button" class="ca-confirm-submit"></button>').text(action + ' Product');
        $buttons.append($cancel, $accept);
        $dialog.append($icon, $heading, $message, $buttons);
        $('body').append($overlay.append($dialog));
        function closeDialog() { $(document).off('keydown.caProductConfirm'); $overlay.remove(); }
        $cancel.on('click', closeDialog);
        $(document).on('keydown.caProductConfirm', function (event) {
            if (event.key === 'Escape' && !$accept.prop('disabled')) closeDialog();
            // Keep keyboard focus inside the confirmation dialog.
            if (event.key === 'Tab') {
                var first = $cancel[0], last = $accept[0];
                if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
                else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
            }
        });
        $accept.on('click', function () {
            $accept.prop('disabled', true).text('Saving...');
            $.ajax({
                url: statusUrl, type: 'POST', dataType: 'json',
                data: { productStr: JSON.stringify({ productId: id, enableStatus: enableStatus }), statusChange: true },
                success: function (result) {
                    if (result.success) { closeDialog(); $.toast('Product updated'); getList(); }
                    else { $.toast(result.errMsg || 'Unable to update product'); $accept.prop('disabled', false).text(action + ' Product'); }
                },
                error: function () { $.toast('Network error'); $accept.prop('disabled', false).text(action + ' Product'); }
            });
        });
        $cancel.trigger('focus');
    }
});
