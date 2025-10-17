/* ------------------------------------------------------------------------------
 *
 *  # Custom JS code
 *
 *  Place here all your custom js. Make sure it's loaded after app.js
 *
 * ---------------------------------------------------------------------------- */
var swalInit = Swal.mixin({
        buttonsStyling: false,
        confirmButtonClass: 'btn btn-primary',
        cancelButtonClass: 'btn btn-light'
});

$.validator.setDefaults({
    ignore: 'input[type=hidden], .select2-search__field', // ignore hidden fields
    errorClass: 'validation-invalid-label',
    successClass: 'validation-valid-label',
    validClass: 'validation-valid-label',
    highlight: function(element, errorClass) {
        $(element).removeClass(errorClass);
    },
    unhighlight: function(element, errorClass) {
        $(element).removeClass(errorClass);
    },
//        success: function(label) {
//            label.addClass('validation-valid-label').text('Success.'); // remove to hide Success message
//        },

    // Different components require proper error label placement
    errorPlacement: function(error, element) {

        // Unstyled checkboxes, radios
        if (element.parents().hasClass('form-check')) {
            error.appendTo( element.parents('.form-check').parent() );
        }

        // Input with icons and Select2
        else if (element.parents().hasClass('form-group-feedback') || element.hasClass('select2-hidden-accessible')) {
            error.appendTo( element.parent() );
        }

        // Input group, styled file input
        else if (element.parent().is('.uniform-uploader, .uniform-select') || element.parents().hasClass('input-group')) {
            error.appendTo( element.parent().parent() );
        }

        // Other elements
        else {
            error.insertAfter(element);
        }
    }	
});

$.extend(true, $.fn.DataTable.defaults, {
  autoWidth: false,

  // v1.x 'dom' replacement
  layout: {
    top:    '<"datatable-header"fl>',
    body:   '<"datatable-scroll"t>',
    bottom: '<"datatable-footer"ip">'
  },

  language: {
    search:           '<span>Filter:</span> _INPUT_',
    searchPlaceholder:'Type to filter...',
    lengthMenu:       '<span>Show:</span> _MENU_',
    paginate: {
      first:    'First',
      last:     'Last',
      next:     '&rarr;',
      previous: '&larr;'
    }
  },

  // v1.x drawCallback → v2.x on.draw
  on: {
    draw: function (e, settings) {
      // 'settings.nTable' is the table's <table> element
      $(settings.nTable)
        .find('tbody tr').slice(-3)
        .find('.dropdown, .btn-group')
        .addClass('dropup');
    },
    preDraw: function (e, settings) {
      $(settings.nTable)
        .find('tbody tr').slice(-3)
        .find('.dropdown, .btn-group')
        .removeClass('dropup');
    }
  }
});

$.blockUI.defaults.message = '<span class="font-weight-semibold"><i class="icon-spinner4 spinner mr-2"></i>&nbsp; Updating data</span>';
$.blockUI.defaults.overlayCSS = {
    backgroundColor: '#fff',
    opacity: 0.8,
    cursor: 'wait'
};
$.blockUI.defaults.css = {
    border: 0,
    padding: '10px 15px',
    color: '#fff',
    width: 'auto',
    '-webkit-border-radius': 3,
    '-moz-border-radius': 3,
    backgroundColor: '#333'
};

var errorBlock = function (element) {
	$(element).block({
		blockMsgClass: "bg-danger text-center",
		message: '<i class="icon-warning mt-1"></i> <span class="font-weight-semibold d-block mt-2">Load error</span>'
	})
}


$.ajaxSetup({
	beforeSend: function(xhr, settings) {
		if (settings.type == "POST") {
			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
			xhr.setRequestHeader(header, token);					
		}
	}
});

var ajaxUtils = {
	downloadFile: function(type, url, data, mimeType) {
		var req = $.ajax({
			type: type,
			url: url,
			data: data,
	        xhrFields: {
	            responseType: 'blob'
	        }										
		});
		
		req.done(function(res, textStatus, request) {
			var pattern = /filename=\"(.*?)\"/g
			var filename = pattern.exec(request.getResponseHeader("content-disposition"))[1];
			var blob = new Blob([res], {type: mimeType});
			saveAs(blob, filename);			
		});
		
		return req;
	}
}

function null2Empty(input) {
	return (input)?input:"";
}
