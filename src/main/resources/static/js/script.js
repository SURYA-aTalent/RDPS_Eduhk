$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    
    for (prop in o) {
    	if (Array.isArray(o[prop])) {
    		var value = o[prop];
    		delete o[prop];
    		for (var i = 0; i < value.length; i++) {
    			o[prop + "[" + i + "]"] = value[i];
    		}
    		
    	}
    }   	

    return o;
};

Number.prototype.round = function(places) {
//	var placesFactor = 10 * places;
//	return (Math.round(this * placesFactor) / placesFactor).toLocaleString("en");
	return math.round(this.valueOf(), places);
}

function bsAlerts(type, message, location, scroll) {
	var alert = $('<div class="alert alert-'+type+' alert-dismissable fade in show message-box"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'+message+'</div>');
	$(alert).prependTo("#"+location).hide().slideDown();
	if (scroll) {
	    $('html, body').animate({
	        scrollTop: $(alert).offset().top
	    }, 1000);
	}
	return alert;	
}

function bsPinAlerts(type, message, location, timeout) {
//	$("#"+location).prepend('<div class="alert alert-'+type+' alert-dismissable fade in"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'+message+'</div>');
	var alert = $('<div style="position: fixed; top: 8px; width: 96%; z-index: 1; left: 0; right: 0; margin-left: auto; margin-right: auto;" class="alert alert-'+type+' alert-dismissable fade in message-box pin-alert"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'+message+'</div>')
	$(alert).prependTo("#"+location).hide().slideDown();
	if (timeout) {
		setTimeout(function() {
			$(alert).slideUp("normal", function() {
				$(this).remove();
			});
		}, timeout);		
	}			
	return alert;	
}

function bsPanels(type, header,message, location) {
	$("#"+location).prepend('<div class="custom-panel card border-'+type+'"><div class="card-header bg-'+type+'">'+header+'</div><div class="card-body text-'+type+'">'+message+'</div></div>');	
}

function clearAllBsPanels() {
	$(".custom-panel").remove();
}

function clearAllBsAlerts() {
	$(".alert:not(.pin-alert,.persist)").slideUp("normal", function() {
		$(this).remove();
	});
	resetClass("has-error");
	resetClass("has-warning");
	resetClass("has-success");
}

function clearAllBsPinAlerts() {
	$(".alert.pin-alert").slideUp("normal", function() {
		$(this).remove();
	});
	resetClass("has-error");
	resetClass("has-warning");
	resetClass("has-success");
}

function clearElementById(id) {
	$("#"+id).remove();
}

function resetClass(cssClass) {
	$("."+ cssClass).removeClass(cssClass);
}

function verifyDateRange(from, to) {
	var fromDate = parseDate(from, "/");
	var toDate = parseDate(to, "/");
	
	return fromDate > toDate;
}

function parseDate(date, delimiter) {
	/* delimiter is '/' if the date is DD/MM/2017 */
	var format = date.split(delimiter);
	return new Date(format[2], format[1] - 1, format[0]);
}

function isValidDate(date, delimiter) {
	var format = date.split(delimiter);
	var d = new Date(format[2], format[1] - 1, format[0]);
	return d && (d.getMonth() + 1) == format[1];
}

function buttonLoading(btn, state) {
	if (btn.prop("disabled") == state) {
		return;
	}
	
	var loadingStr = btn.data("loading-text");	
	if (!loadingStr) {
		loadingStr = "<i class='fas fa-spinner fa-spin'></i> Loading...";
	}
	if (state) {		
		btn.data("original", btn.html()); /* Save original text */
		btn.html(loadingStr);
		btn.prop("disabled", true);
	} else {
		btn.html(btn.data("original"));
		btn.data("original", "");
		btn.prop("disabled", false);
	}
}

function getLastUpdate(url, params, updateEle) {
	$.get(url, params, function (res) {
		$(updateEle).text(res.result);
	});
}

function post(path, parameters) {
  var form = $('<form></form>');

  form.attr("method", "post");
//  form.attr("target", "_blank");
  form.attr("target", "HostelWindow");
  form.attr("action", path);

  $.each(parameters, function(key, value) {
      var field = $('<input></input>');

      field.attr("type", "hidden");
      field.attr("name", key);
      field.attr("value", value);

      form.append(field);
  });

  // The form needs to be a part of the document in
  // order for us to be able to submit it.
  $(document.body).append(form);
  window.open('', 'HostelWindow');
  form.submit();
}


function download(path, parameters) {
	var userAgent = (navigator.userAgent || navigator.vendor || window.opera).toLowerCase();
	var isIos;
	var isAndroid;
	var isOtherMobileBrowser;
	if (/ip(ad|hone|od)/.test(userAgent)) {
		isIos = true;
	} else if (userAgent.indexOf('android') !== -1) {
		isAndroid = true;
	} else {
		isOtherMobileBrowser = /avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|playbook|silk|iemobile|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i
				.test(userAgent)
				|| /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i
						.test(userAgent.substr(0, 4));
	}

	var form = $('<form></form>');

	form.attr("method", "GET");
	form.attr("action", path);

	$.each(parameters, function(key, value) {
		var field = $('<input></input>');

		field.attr("type", "hidden");
		field.attr("name", value.name);
		field.attr("value", value.value);

		form.append(field);
	});
	var frameContent = "";
//	$(document.body).append(form);
	if (isIos || isAndroid || isOtherMobileBrowser) {
		var downloadWindow = window.open("about:blank");		
		frameContent = downloadWindow.document;
		frameContent.write("<html><head></head><body>"+$(form)[0].outerHTML+"</body></html>");
		form = $(frameContent).find("form");
		window.focus();
	} else {
		$iframe = $("<iframe style='display: none' src='about:blank'></iframe>").appendTo("body");
		$iframe.append(form);
	}
	form.submit();
}

function isEmptyProperties(obj) {
    for (var key in obj) {
        if (obj[key] != null && obj[key] != "")
            return false;
    }
    return true;
}

function printErrors(allErrors) {
	var result = "There are "+allErrors.length+" errors: <br/>";
	result += "<ol>";
	for (var i=0; i<allErrors.length; i++) {
		result += "<li>" + allErrors[i] + "</li>";
	}
	result += "</ol>";
	return result;
}

function genDatepickerClearBtn() {
	var old_fn = $.datepicker._updateDatepicker;
	$.datepicker._updateDatepicker = function(inst) {
		old_fn.call(this, inst);
		var buttonPane = $(this).datepicker("widget").find(".ui-datepicker-buttonpane");
		$("<button type='button' class='ui-datepicker-clean ui-state-default ui-priority-primary ui-corner-all'>Clear</button>").appendTo(buttonPane).click(function(ev) {
			$.datepicker._clearDate(inst.input);
		});
	}
}

function roundNumber(number) {
	if (isNaN(number)) {
		return 0;
	}
	return (Math.round(number * 10) / 10).toLocaleString("en");
}

function escapeRegExp(string){
	return string.replace(/"/g, "\\\\\"");
}

String.prototype.format = function() {
	a = this;
	for (k in arguments) {
		a = a.replace("{" + k + "}", arguments[k])
	}
	return a
}

if (!String.prototype.includes) {
	Object.defineProperty(String.prototype, 'includes', {
		value: function(search, start) {
			if (typeof start !== 'number') {
				start = 0
			}

			if (start + search.length > this.length) {
				return false
			} else {
				return this.indexOf(search, start) !== -1
			}
		}
	})
}
