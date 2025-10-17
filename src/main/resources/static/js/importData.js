// File Path: src/main/resources/static/js/importData.js
// Purpose: Handle import data page functionality

$(document).ready(function() {

    // Import button click handler
    $('#btnImportData').on('click', function() {
        $('#confirmImportModal').modal('show');
    });

    // Confirm import button handler
    $('#btnConfirmImport').on('click', function() {
        $('#confirmImportModal').modal('hide');
        triggerImport();
    });

    // OK button on results
    $('#btnOkResults').on('click', function() {
        $('#importResultsSection').hide();
        location.reload(); // Refresh to show updated last import info
    });

    // Trigger the actual import
    function triggerImport() {
        // Show progress section
        $('#importProgressSection').show();
        $('#importResultsSection').hide();
        $('#btnImportData').prop('disabled', true);

        // Get CSRF token from meta tags
        var csrfToken = $('meta[name="_csrf"]').attr('content');
        var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

        var url = contextPath + 'import/trigger';
        console.log('Triggering import to URL:', url);
        console.log('CSRF Token:', csrfToken);
        console.log('CSRF Header:', csrfHeader);

        // Make AJAX POST to trigger endpoint
        $.ajax({
            url: url,
            type: 'POST',
            data: {},
            beforeSend: function(xhr) {
                // Add CSRF token to request header
                if (csrfToken && csrfHeader) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                }
            },
            success: function(response) {
                console.log('Import completed:', response);
                displayResults(response);
            },
            error: function(xhr, status, error) {
                console.error('Import failed - Status:', status);
                console.error('Import failed - Error:', error);
                console.error('Import failed - Response:', xhr.responseText);
                console.error('Import failed - Status Code:', xhr.status);
                var errorMsg = xhr.responseText || error || 'Unknown error (status: ' + xhr.status + ')';
                displayError(errorMsg);
            },
            complete: function() {
                $('#importProgressSection').hide();
                $('#btnImportData').prop('disabled', false);
            }
        });
    }

    // Display import results
    function displayResults(result) {
        $('#resultTotal').text(result.totalCandidatesProcessed || 0);
        $('#resultSuccess').text(result.successfulImports || 0);
        $('#resultErrors').text(result.failedImports || 0);

        // Calculate duration
        if (result.importStartTime && result.importEndTime) {
            var duration = Math.round((new Date(result.importEndTime) - new Date(result.importStartTime)) / 1000);
            $('#resultDuration').text(duration + ' seconds');
        }

        // Set alert style based on results
        var alertDiv = $('#resultsAlert');
        alertDiv.removeClass('alert-success alert-warning alert-danger');

        if (result.failedImports > 0) {
            alertDiv.addClass('alert-warning');
            $('#btnViewErrorDetails').show();
        } else if (result.successfulImports > 0) {
            alertDiv.addClass('alert-success');
        } else {
            alertDiv.addClass('alert-info');
        }

        $('#importResultsSection').show();
    }

    // Display error message
    function displayError(errorMessage) {
        $('#resultTotal').text('Error');
        $('#resultSuccess').text('0');
        $('#resultErrors').text('1');
        $('#resultDuration').text('N/A');

        $('#resultsAlert').removeClass('alert-success alert-warning alert-info').addClass('alert-danger');
        $('#resultsAlert h5').text('Import Failed: ' + errorMessage);

        $('#importResultsSection').show();
    }

    // View error details button
    $('#btnViewErrorDetails').on('click', function() {
        window.location.href = contextPath + 'import/errors';
    });

    // Download log button
    $('#btnDownloadLog').on('click', function() {
        window.location.href = contextPath + 'import/downloadLog';
    });

});
