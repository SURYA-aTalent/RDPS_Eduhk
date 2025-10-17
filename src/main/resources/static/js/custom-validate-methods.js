$.validator.setDefaults({
	ignore: [],
	errorClass: "is-invalid",
	errorElement: "span",
	errorPlacement: function(error, element) {
		var errorPlacementElement = element;
		error.addClass("help-block invalid-feedback");
		if (element.parent("div").hasClass("input-group")) {
			errorPlacementElement = element.parent("div");
		}
		error.insertAfter(errorPlacementElement);
	},
	highlight: function(element, errorClass) {
		$(element).addClass(errorClass)
	},
	unhighlight: function(element, errorClass) {
		$(element).removeClass(errorClass);
		$(element).parent().find(".help-block").remove();
	}	
});

$.validator.addMethod("dateFormat", function(value, element, params){
	return this.optional(element) || moment(value, "DD/MM/YYYY").isValid()
}, "Please specify correct date format");

jQuery.validator.addMethod("decimalPlaces", function(value, element) {
    return this.optional(element) || /^-?\d+(\.\d{0,3})?$/i.test(value);
}, "Upto 3 decimal places only");