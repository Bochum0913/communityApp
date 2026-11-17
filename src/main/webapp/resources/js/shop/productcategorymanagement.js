$(function () {
    var root = '/communityApp/shopadmin/';
    function notify(message) { $.toast(message); }
    function validPriority(value) {
        return /^\d+$/.test(String(value)) && Number(value) <= 1000000;
    }
    function loadCategories() {
        $.getJSON(root + 'getproductcategorylist', function (result) {
            if (!result.success) { notify(result.errMsg || 'Unable to load categories'); return; }
            var $wrap = $('.category-wrap').empty();
            (result.data || []).forEach(function (category) {
                var $row = $('<div class="row row-product-category now"></div>')
                    .attr('data-id', category.productCategoryId);
                $('<div class="col-33 product-category-name"></div>')
                    .text(category.productCategoryName || '').appendTo($row);
                var $priorityCell = $('<div class="col-33"></div>').appendTo($row);
                $('<input type="number" class="category-input priority" min="0" max="1000000">')
                    .val(category.priority).attr('data-original', category.priority)
                    .appendTo($priorityCell);
                var $action = $('<div class="col-33"></div>').appendTo($row);
                $('<a href="#" class="button save-priority">Save</a>').appendTo($action);
                $('<a href="#" class="button delete">Delete</a>').appendTo($action);
                $wrap.append($row);
            });
        }).fail(function () { notify('Unable to load categories'); });
    }
    loadCategories();
    $('#new').on('click', function (event) {
        event.preventDefault();
        var $row = $('<div class="row row-product-category temp"></div>');
        $('<div class="col-33"><input class="category-input category" type="text" placeholder="Category name" maxlength="100"></div>').appendTo($row);
        $('<div class="col-33"><input class="category-input priority" type="number" min="0" max="1000000" placeholder="Priority"></div>').appendTo($row);
        $('<div class="col-33"><a href="#" class="button delete">Remove</a></div>').appendTo($row);
        $('.category-wrap').append($row);
    });
    $('#submit').on('click', function (event) {
        event.preventDefault();
        var categories = [], valid = true;
        $('.category-wrap .temp').each(function () {
            var name = $(this).find('.category').val().trim();
            var priority = $(this).find('.priority').val();
            if (!name || !validPriority(priority)) { valid = false; return; }
            categories.push({ productCategoryName: name, priority: Number(priority) });
        });
        if (!valid) { notify('Enter a category name and a priority from 0 to 1000000'); return; }
        if (!categories.length) { notify('Add a category first'); return; }
        $.ajax({ url: root + 'addproductcategorys', type: 'POST',
            data: JSON.stringify(categories), contentType: 'application/json', dataType: 'json',
            success: function (result) { if (result.success) { notify('Categories added'); loadCategories(); }
                else { notify(result.errMsg || 'Unable to add categories'); } },
            error: function () { notify('Network error'); }
        });
    });
    $('.category-wrap').on('click', '.temp .delete', function (event) {
        event.preventDefault(); $(this).closest('.temp').remove();
    }).on('click', '.now .save-priority', function (event) {
        event.preventDefault();
        var $row = $(this).closest('.now');
        var $input = $row.find('.priority');
        var value = $input.val();
        if (!validPriority(value)) { notify('Priority must be from 0 to 1000000'); return; }
        var $button = $(this).addClass('disabled');
        $.ajax({ url: root + 'modifyproductcategory', type: 'POST', dataType: 'json',
            data: { productCategoryId: $row.attr('data-id'), priority: Number(value) },
            success: function (result) { if (result.success) { notify('Priority updated'); loadCategories(); }
                else { notify(result.errMsg || 'Unable to update priority'); } },
            error: function () { notify('Network error'); },
            complete: function () { $button.removeClass('disabled'); }
        });
    }).on('click', '.now .delete', function (event) {
        event.preventDefault();
        var categoryId = $(this).closest('.now').attr('data-id');
        $.confirm('Delete this category? Products in this category will become uncategorized.', function () {
            $.ajax({ url: root + 'removeproductcategory', type: 'POST', dataType: 'json',
                data: { productCategoryId: categoryId },
                success: function (result) { if (result.success) { notify('Category deleted'); loadCategories(); }
                    else { notify(result.errMsg || 'Unable to delete category'); } },
                error: function () { notify('Network error'); }
            });
        });
    });
});
