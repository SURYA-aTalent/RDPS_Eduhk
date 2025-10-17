var EditableDatatable = function(tableId, datatableConfig, emptyRowObj, customConfig) {
	this.tableId = tableId;
	var self = this;
	this.saveData = {};
	this.saveData.update = [];
	this.saveData.insert = [];
	this.saveData.remove = [];
	this.data = [];
//	this.emptyRowObj = $.isFunction(emptyRowObj) ? emptyRowObj() : emptyRowObj;
	this.emptyRowObj = emptyRowObj;
	if (datatableConfig) {
//		datatableConfig.language = {loadingRecords: "<strong>Loading...</strong>"};
		datatableConfig.language = {
			loadingRecords: "<div class='ph-item'><div class='ph-col-12'><div class='ph-row'><div class='ph-col-12'></div><div class='ph-col-8'></div><div class='ph-col-4 empty'></div><div class='ph-col-12'></div></div></div></div><div class='ph-item'><div class='ph-col-12'><div class='ph-row'><div class='ph-col-12'></div><div class='ph-col-8'></div><div class='ph-col-4 empty'></div><div class='ph-col-12'></div></div></div></div><div class='ph-item'><div class='ph-col-12'><div class='ph-row'><div class='ph-col-12'></div><div class='ph-col-8'></div><div class='ph-col-4 empty'></div><div class='ph-col-12'></div></div></div></div>"
		};
		datatableConfig.processing = true;		
		datatableConfig.createdRow = (function() {
			var cachedFunction = datatableConfig.createdRow;
			return function(row, data, dataIndex, cells) {
				data.rowId = dataIndex + 1;
				self.data.push(data);
				$(row).attr("data-row", dataIndex + 1);	
//				$(row).find(".selectric").selectric();
				var result = cachedFunction.apply(this, arguments);
				return result;
			}		
		})();
//		datatableConfig.drawCallback = (function() {
//			var cachedFunction = datatableConfig.drawCallback;
//			return function(settings) {
//				if (refreshFontSize) refreshFontSize();
//				if (cachedFunction) {
//					var result = cachedFunction.apply(this, arguments);
//					return result;					
//				}
//			}
//		})();
	}
	if (customConfig) {
		if (customConfig.footer) {
			var colIdx = customConfig.footer.colIdx;
		    var fields = customConfig.footer.fields;
			datatableConfig.footerCallback = function(row, data, start, end, display) {
				var api = this.api();
				for (var i=0; i<colIdx.length; i++) {
					var colId = colIdx[i];
					$(api.column(colIdx[i]).footer()).html("");
					if (!Array.isArray(fields[i])) {
						fields[i] = [customConfig.footer.fields[i]];
					}
					for (var j=0; j<fields[i].length; j++) {
						var field = fields[i][j];
						var fieldName = typeof field === "string" ? field : field.fieldName;
						var className = typeof field === "string" ? "" : field.className ;
						var total = 0;
						api.column(colId).nodes().each(function(element) {
							var val = $(element).find("input." + fieldName).val() || $(element).find("span." + fieldName).text();
							total += parseFloat(val ? val : 0);							
						});
						$(api.column(colId).footer()).append($("<div>").text(roundNumber(total)).addClass(className));
					}
				}
			}
		}
	}
	
	$("#" + tableId).on("processing.dt", function(e, settings, processing) {

	});	
	
	this.datatable = $("#" + tableId).DataTable(datatableConfig)
		
	
	window.onbeforeunload = function(e) {
		if (!self.isUnchanged()) {
			return "All data will be lost. Confirm?";	
		}
	};	
	
	$("#"+tableId+" tbody").on("change", "td input,select,textarea,.toggle-checkbox", function (e) {
		var row = $(this).closest("tr");
		var rowId = $(row).attr("data-row");
		if (!rowId) return;
		var originalResult = findOriginalData(rowId);
		var result = {};
		var changed = false;
		var isKeyChange = false;
		var isNew = false;
		var changedFieldName = $(this).attr("name");
		
		var data = self.datatable.row(row).data();
		$(row).find("input, select, textarea, .toggle-checkbox").each(function() {
			var inputName = $(this).attr("name");	
			var inputValue = $(this).val();	
			
			if (!inputName) return;
			if ($(this).data("uppercase")) {
				inputValue = inputValue.toUpperCase();
				$(this).val(inputValue);
			}				
			if ($(this).attr("type") == "checkbox") {
				inputValue = ($(this).is(":checked")) ? "Y" : "N";				 
			}
			
//			data[inputName] = inputValue;
			result[inputName] = inputValue;
						
			if ($(this).closest("tr").data("insert") != null) {
				isNew = true;
			} else {
//				originalResult[$(this).attr("name")] = $(this).data("original");
			}
			
			
			if (inputValue != originalResult[inputName]) {
				changed = true;
				if ($(this).data("key") && $(this).data("key") == "Y") {
					isKeyChange = true;
				}
			}
		});		
		
		result.rowId = rowId;
		originalResult.rowId = rowId;
		if (isNew) {
			replaceItemOrInsert("insert", result);			
		} else if (changed) {			
			$(row).addClass("bg-orange-300");
			if (isKeyChange) {
				removeItem("update", result);
				replaceItemOrInsert("remove", originalResult);
				replaceItemOrInsert("insert", result);	
			} else {
				removeItem("remove", originalResult);
				removeItem("insert", result);
				replaceItemOrInsert("update", result);				
			}	
		} else {
			$(row).removeClass("bg-orange-300");
			removeItem("update", result);
			removeItem("remove", result);
			removeItem("insert", result);
		}
		/** re-initialize datepicker **/
//		$(row).find(".datepicker").datepicker({
//			dateFormat: "dd/mm/yy",
//			changeYear: true,
//			changeMonth: true,
//			showButtonPanel: true
//		});	
		
//		$(row).find(".toggle-checkbox").bootstrapToggle();
		
	});
	
	$("#" + tableId).on("click", ".deleteBtn", function() {
		var row = $(this).closest("tr");		
		var rowId = $(row).attr("data-row");
		var result = findOriginalData(rowId);		
		var changed = false;
		var isKeyChange = false;
		var isNew = false;
		
		$(row).find("input, select, textarea, .toggle-checkbox").each(function() {
			if ($(this).closest("tr").data("insert") != null) {
				isNew = true;
			}
		});	
		if (isNew) {
			self.datatable.row(row).remove().draw();
			removeItem("insert", result);
		} else {
			if ($(row).hasClass("danger")) {
	 			removeItem("remove", result);
				$(row).find("input, select, textarea, .toggle-checkbox").each(function() {
					$(this).prop("disabled", $(this).data("original-disabled-state"));
				});				
				$(row).removeClass("danger bg-light font-italic");
				$(this).removeClass("btn-danger");
				$(this).addClass("btn-outline-danger");
			} else {
	 			replaceItemOrInsert("remove", result);		
				$(row).find("input, select, textarea, .toggle-checkbox").each(function() {
					$(this).data("original-disabled-state", $(this).prop("disabled"));
					$(this).prop("disabled", true);
				});						
				$(row).addClass("danger bg-light font-italic");					
				$(this).removeClass("btn-outline-danger");
				$(this).addClass("btn-danger");
			}	
		}				
	});	
	
	/* On click handler of Insert row button */
	$(".insertRowBtn").on("click", function() {
		var columnIdx = $(this).data("column") ? $(this).data("column") : 0;		
		self.addNewRow(columnIdx);		
	});
	
	this.addNewRow = function(column) {
		var addedRow = this.datatable.row.add($.extend({},$.isFunction(self.emptyRowObj) ? self.emptyRowObj() : self.emptyRowObj)).node();
		$(addedRow).addClass("bg-orange-300");
		$(addedRow).attr("data-insert", "true");
		this.datatable.draw(false);
//		this.datatable.page.jumpToData("", column);		
		$($(addedRow).find("td > select, input[type!='hidden'], textarea")[0]).focus();
	}

	this.isUnchanged = function() {
		if (self.saveData.update.length > 0 || self.saveData.insert.length > 0 || self.saveData.remove.length > 0) {
			return false;
		} else {
			return true;
		}
	}

	var replaceItemOrInsert = function(listName, item) {
		var array = self.saveData[listName];
		for (var i=0; i < array.length; i++) {
			if (array[i].rowId == item.rowId) {
				array[i] = item;
				return;
			}
		}
		array.push(item);
	}

	var removeItem = function(listName, item) {
		var removeIndex = -1;
		var array = self.saveData[listName];
		for (var i=0; i < array.length; i++) {
			if (array[i].rowId == item.rowId) {
				removeIndex = i;
			}
		}
		if (removeIndex != -1) {
			array.splice(removeIndex, 1);
		}
	}
	
	var findOriginalData = function(rowId) {
		return self.data.filter(function(d) {return d.rowId == rowId})[0];
	}

	this.resetTable = function(ajaxReload) {
		if (ajaxReload === undefined) {
			ajaxReload = true;
		}
		$('.searchFields input,.searchFields select').val("");	
		this.data = [];
		if (ajaxReload) {
			this.datatable.ajax.reload(function(json) {

			});			
		}
		this.saveData.remove = [];
		this.saveData.insert = [];
		this.saveData.update = [];	
	}	
	
}