$(document).ready(function() {
	// Wait for page to fully load before initializing DataTable
	setTimeout(function() {
		if (typeof $.fn.DataTable !== 'undefined' && $('#validationErrorsTable').length) {
			$('#validationErrorsTable').DataTable({
				"order": [[7, "desc"]], // Sort by created date descending (column 7)
				"pageLength": 10, // Show 10 records per page
				"lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
				"responsive": true,
				"autoWidth": false,
				"processing": false,
				"serverSide": false,
				"columnDefs": [
					{ "width": "5%", "targets": 0 },  // Error ID
					{ "width": "8%", "targets": 1 },  // Candidate ID
					{ "width": "8%", "targets": 2 },  // Req Number
					{ "width": "10%", "targets": 3 }, // Error Type
					{ "width": "10%", "targets": 4 }, // Field Name
					{ "width": "25%", "targets": 5 }, // Error Message
					{ "width": "12%", "targets": 6 }, // Batch ID
					{ "width": "12%", "targets": 7 }, // Created Date
					{ "width": "10%", "targets": 8 }  // Status
				],
				"language": {
					"emptyTable": "No validation errors found for the specified period.",
					"info": "Showing _START_ to _END_ of _TOTAL_ validation errors",
					"infoEmpty": "Showing 0 to 0 of 0 validation errors",
					"infoFiltered": "(filtered from _MAX_ total validation errors)",
					"lengthMenu": "Show _MENU_ errors per page",
					"search": "Search errors:",
					"paginate": {
						"first": "First",
						"last": "Last",
						"next": "Next",
						"previous": "Previous"
					}
				},
				"dom": '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
				       '<"row"<"col-sm-12"tr>>' +
				       '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p">>'
			});
			console.log('DataTable initialized successfully');
		} else {
			console.error('DataTables library not loaded or table not found');
		}
	}, 500);

	// Refresh button handler
	$('#btnRefreshErrors').click(function() {
		location.reload();
	});
});
