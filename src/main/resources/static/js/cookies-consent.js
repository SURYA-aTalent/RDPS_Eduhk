$(document).ready(function() {
	var content = "We use cookies to ensure that we give you the best experience on our website. By using this website, you consent to the use of cookies. ";
	var template = 
		'<div class="bottom-banner" id="cookie-consent-banner" style="display:none; left: 0px; right: 0px; bottom: 0px; background-color: rgb(53, 52, 52); color: rgb(255, 255, 255); padding: 10px; position: fixed; z-index: 9999;">'+
			'<div class="container">'+
				'<div class="row">'+
					'<div class="col-sm-11" style="padding: 4px 0px;">'+
						content+' <a href="https://www.eduhk.hk/main/cookies/" target="_blank">Learn More</a>'+
					'</div>'+
					'<div class="col-sm-1" style="text-align: right;">'+
						'<button id="cookies-consent-continue-btn" type="button" class="btn btn-default btn-sm">Got it!</button>'+
					'</div>'+
				'</div>'+			
			'</div>'+
		'</div>'
	;
	var cookieConsent = $.cookie("cookies-consent");
	if (!cookieConsent) {
		var div = $(template);
		$("body").append(div);
		div.fadeIn();
	}
	
	$(document).on("click", "#cookies-consent-continue-btn", function(e) {
		$.cookie("cookies-consent", "allow");
		$("#cookie-consent-banner").fadeOut();
	});
});
