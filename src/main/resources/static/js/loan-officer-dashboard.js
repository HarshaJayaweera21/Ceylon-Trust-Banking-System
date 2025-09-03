// Loan Officer Dashboard JavaScript

const toast = document.getElementById('toast');
const reviewModal = document.getElementById('reviewModal');
const confirmModal = document.getElementById('confirmModal');
let currentLoanId = null;
let pendingAction = null;

// CSRF Token Helper
function csrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    return { [header]: token };
}

// Toast Notification
function showToast(message, type = 'success') {
    toast.textContent = message;
    toast.className = 'toast ' + (type === 'error' ? 'error' : 'success') + ' show';
    setTimeout(() => {
        toast.className = toast.className.replace(' show', '');
    }, 4000);
}

// Handle URL Parameters for Notifications
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    if (success) {
        showToast(success, 'success');
    } else if (error) {
        showToast(error, 'error');
    }
    
    // Clean URL
    if (success || error) {
        history.replaceState(null, '', window.location.pathname);
    }
});

// Approve Loan
function approveLoan(button) {
    const loanId = button.getAttribute('data-loan-id');
    const row = button.closest('tr');
    
    // Store the action and loan details for confirmation
    pendingAction = {
        type: 'approve',
        loanId: loanId,
        row: row
    };
    
    // Show custom confirmation modal
    showConfirmModal(
        'Approve Loan Application',
        'Are you sure you want to approve this loan application? This action cannot be undone.',
        'approve'
    );
}

// Reject Loan
function rejectLoan(button) {
    const loanId = button.getAttribute('data-loan-id');
    const row = button.closest('tr');
    
    // Store the action and loan details for confirmation
    pendingAction = {
        type: 'reject',
        loanId: loanId,
        row: row
    };
    
    // Show custom confirmation modal
    showConfirmModal(
        'Reject Loan Application',
        'Are you sure you want to reject this loan application? This action cannot be undone.',
        'reject'
    );
}

// Review Loan
function reviewLoan(button) {
    const loanId = button.getAttribute('data-loan-id');
    currentLoanId = loanId;
    
    // Fetch loan details
    fetch(`/loan-officer/loan/${loanId}/details`)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                showToast(data.error, 'error');
                return;
            }
            
            // Populate modal with loan details
            document.getElementById('reviewLoanId').textContent = '#' + data.loanId;
            document.getElementById('reviewLoanType').textContent = data.loanType;
            document.getElementById('reviewAmount').textContent = parseFloat(data.amount).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }) + ' LKR';
            document.getElementById('reviewInterestRate').textContent = data.interestRate + '%';
            document.getElementById('reviewTerm').textContent = data.termMonths + ' months';
            document.getElementById('reviewAppliedDate').textContent = data.appliedAt;
            document.getElementById('reviewEMI').textContent = parseFloat(data.emi).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }) + ' LKR';
            
            // Load existing comments (if any) or clear for new review
            document.getElementById('officerComments').value = data.comments || '';
            
            // Load and display documents
            try {
                loadLoanDocuments(data.documents || []);
            } catch (docError) {
                console.error('Error loading documents:', docError);
                // Initialize empty documents list if there's an error
                const documentsList = document.getElementById('loanDocumentsList');
                if (documentsList) {
                    documentsList.innerHTML = `
                        <div class="no-documents" style="display: flex;">
                            <i class="fa-solid fa-file-circle-exclamation"></i>
                            <span>Error loading documents</span>
                        </div>
                    `;
                }
            }
            
            // Show modal
            reviewModal.style.display = 'flex';
            document.body.classList.add('no-scroll');
        })
        .catch(error => {
            console.error('Error fetching loan details:', error);
            showToast('Failed to load loan details. Please try again.', 'error');
        });
}

// Delete Loan
function deleteLoan(button) {
    const loanId = button.getAttribute('data-loan-id');
    const row = button.closest('tr');
    
    // Store the action and loan details for confirmation
    pendingAction = {
        type: 'delete',
        loanId: loanId,
        row: row
    };
    
    // Show custom confirmation modal
    showConfirmModal(
        'Delete Loan Record',
        'Are you sure you want to delete this loan record? This action cannot be undone.',
        'delete'
    );
}

// Close Review Modal
function closeReviewModal() {
    reviewModal.style.display = 'none';
    document.body.classList.remove('no-scroll');
    currentLoanId = null;
    
    // Reset documents list to initial state
    const documentsList = document.getElementById('loanDocumentsList');
    if (documentsList) {
        documentsList.innerHTML = `
            <div class="no-documents" style="display: none;">
                <i class="fa-solid fa-file-circle-exclamation"></i>
                <span>No supporting documents uploaded</span>
            </div>
        `;
    }
    
    // Don't clear comments - let them persist for next review
}

// Submit Review
function submitReview() {
    if (!currentLoanId) {
        showToast('No loan selected for review.', 'error');
        return;
    }
    
    const comments = document.getElementById('officerComments').value.trim();
    
    fetch(`/loan-officer/review/${currentLoanId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...csrfHeaders()
        },
        body: `comments=${encodeURIComponent(comments)}`
    })
    .then(response => {
        if (response.ok) {
            // Update UI - Only change status badge, keep action buttons
            const row = document.querySelector(`tr[data-loan-id="${currentLoanId}"]`);
            if (row) {
                const statusBadge = row.querySelector('.status-badge');
                statusBadge.textContent = 'Reviewed';
                statusBadge.className = 'status-badge status-reviewed';
                
                // Keep action buttons unchanged - user can still approve/reject/review
            }
            
            closeReviewModal();
            showToast('Loan reviewed successfully! Comments saved.', 'success');
        } else {
            showToast('Failed to submit review. Please try again.', 'error');
        }
    })
    .catch(error => {
        showToast('An error occurred. Please try again.', 'error');
    });
}

// Show Confirmation Modal
function showConfirmModal(title, message, type) {
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmMessage').textContent = message;
    
    // Set modal variant based on type
    const modalContent = document.querySelector('.confirm-modal');
    modalContent.className = 'modal-content confirm-modal ' + type;
    
    // Set appropriate icon
    const icon = document.getElementById('confirmIcon');
    if (type === 'approve') {
        icon.className = 'fa-solid fa-check-circle';
    } else if (type === 'reject') {
        icon.className = 'fa-solid fa-times-circle';
    } else if (type === 'delete') {
        icon.className = 'fa-solid fa-trash';
    } else {
        icon.className = 'fa-solid fa-question-circle';
    }
    
    // Show modal
    confirmModal.style.display = 'flex';
    document.body.classList.add('no-scroll');
}

// Close Confirmation Modal
function closeConfirmModal() {
    confirmModal.style.display = 'none';
    document.body.classList.remove('no-scroll');
    pendingAction = null;
}

// Execute Confirmed Action
function executeConfirmedAction() {
    if (!pendingAction) {
        showToast('No action to execute.', 'error');
        return;
    }
    
    const { type, loanId, row } = pendingAction;
    
    if (type === 'approve') {
        executeApprove(loanId, row);
    } else if (type === 'reject') {
        executeReject(loanId, row);
    } else if (type === 'delete') {
        executeDelete(loanId, row);
    }
    
    closeConfirmModal();
}

// Execute Approve Action
function executeApprove(loanId, row) {
    fetch(`/loan-officer/approve/${loanId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...csrfHeaders()
        }
    })
    .then(response => {
        if (response.ok) {
            // Update UI
            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = 'Approved';
            statusBadge.className = 'status-badge status-approved';
            
            // Disable action buttons
            const actionButtons = row.querySelector('.action-buttons');
            actionButtons.innerHTML = '<span class="text-success"><i class="fa-solid fa-check"></i> Approved</span>';
            
            showToast('Loan approved successfully!', 'success');
        } else {
            showToast('Failed to approve loan. Please try again.', 'error');
        }
    })
    .catch(error => {
        showToast('An error occurred. Please try again.', 'error');
    });
}

// Execute Reject Action
function executeReject(loanId, row) {
    fetch(`/loan-officer/reject/${loanId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...csrfHeaders()
        }
    })
    .then(response => {
        if (response.ok) {
            // Update UI
            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = 'Rejected';
            statusBadge.className = 'status-badge status-rejected';
            
            // Disable action buttons
            const actionButtons = row.querySelector('.action-buttons');
            actionButtons.innerHTML = '<span class="text-danger"><i class="fa-solid fa-times"></i> Rejected</span>';
            
            showToast('Loan rejected successfully!', 'success');
        } else {
            showToast('Failed to reject loan. Please try again.', 'error');
        }
    })
    .catch(error => {
        showToast('An error occurred. Please try again.', 'error');
    });
}

// Execute Delete Action
function executeDelete(loanId, row) {
    console.log('Attempting to delete loan with ID:', loanId);
    console.log('Row to be deleted:', row);
    
    fetch(`/loan-officer/delete/${loanId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...csrfHeaders()
        }
    })
    .then(response => {
        console.log('Delete response status:', response.status);
        console.log('Delete response ok:', response.ok);
        
        if (response.ok) {
            // Remove the row from the table
            row.remove();
            console.log('Row removed from DOM');
            
            // Update application counts
            updateApplicationCounts();
            
            showToast('Loan record deleted successfully!', 'success');
        } else {
            console.error('Delete failed with status:', response.status);
            showToast('Failed to delete loan record. Please try again.', 'error');
        }
    })
    .catch(error => {
        console.error('Delete error:', error);
        showToast('An error occurred. Please try again.', 'error');
    });
}

// Update Application Counts
function updateApplicationCounts() {
    // Update approved loans count
    const approvedRows = document.querySelectorAll('.approved-section tbody tr:not(.no-data)');
    const approvedCount = document.querySelector('.approved-section .application-count');
    if (approvedCount) {
        approvedCount.textContent = approvedRows.length + ' Applications';
    }
    
    // Update rejected loans count
    const rejectedRows = document.querySelectorAll('.rejected-section tbody tr:not(.no-data)');
    const rejectedCount = document.querySelector('.rejected-section .application-count');
    if (rejectedCount) {
        rejectedCount.textContent = rejectedRows.length + ' Applications';
    }
    
    // Show no-data message if no records
    const approvedTbody = document.querySelector('.approved-section tbody');
    const rejectedTbody = document.querySelector('.rejected-section tbody');
    
    if (approvedRows.length === 0 && approvedTbody) {
        const noDataRow = approvedTbody.querySelector('.no-data');
        if (!noDataRow) {
            approvedTbody.innerHTML = `
                <tr class="no-data">
                    <td colspan="7" class="no-data">
                        <div class="no-data-content">
                            <i class="fa-solid fa-check-circle"></i>
                            <p>No approved loans</p>
                        </div>
                    </td>
                </tr>
            `;
        }
    }
    
    if (rejectedRows.length === 0 && rejectedTbody) {
        const noDataRow = rejectedTbody.querySelector('.no-data');
        if (!noDataRow) {
            rejectedTbody.innerHTML = `
                <tr class="no-data">
                    <td colspan="7" class="no-data">
                        <div class="no-data-content">
                            <i class="fa-solid fa-times-circle"></i>
                            <p>No rejected loans</p>
                        </div>
                    </td>
                </tr>
            `;
        }
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === reviewModal) {
        closeReviewModal();
    }
    if (event.target === confirmModal) {
        closeConfirmModal();
    }
}

// Close modal with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        if (reviewModal.style.display === 'flex') {
            closeReviewModal();
        }
        if (confirmModal.style.display === 'flex') {
            closeConfirmModal();
        }
    }
});

// Auto-refresh functionality (optional)
function refreshDashboard() {
    location.reload();
}

// Load and display loan documents
function loadLoanDocuments(documents) {
    console.log('Loading documents:', documents);
    const documentsList = document.getElementById('loanDocumentsList');
    
    if (!documentsList) {
        console.error('Documents list element not found!');
        return;
    }
    
    // Clear existing documents but preserve the no-documents element
    const noDocuments = documentsList.querySelector('.no-documents');
    
    // Remove all document items but keep the no-documents element
    const documentItems = documentsList.querySelectorAll('.document-item');
    documentItems.forEach(item => item.remove());
    
    if (!documents || documents.length === 0) {
        // Show no documents message
        if (noDocuments) {
            noDocuments.style.display = 'flex';
        } else {
            // Create no documents element if it doesn't exist
            const noDocumentsElement = document.createElement('div');
            noDocumentsElement.className = 'no-documents';
            noDocumentsElement.style.display = 'flex';
            noDocumentsElement.innerHTML = `
                <i class="fa-solid fa-file-circle-exclamation"></i>
                <span>No supporting documents uploaded</span>
            `;
            documentsList.appendChild(noDocumentsElement);
        }
    } else {
        // Hide no documents message
        if (noDocuments) {
            noDocuments.style.display = 'none';
        }
        
        // Create document items
        documents.forEach(doc => {
            const documentItem = createDocumentItem(doc);
            documentsList.appendChild(documentItem);
        });
    }
}

// Create a document item element
function createDocumentItem(doc) {
    const documentItem = document.createElement('div');
    documentItem.className = 'document-item';
    documentItem.innerHTML = `
        <i class="fa-solid ${doc.fileIcon}"></i>
        <div class="document-info">
            <span class="document-name">${doc.originalFileName}</span>
            <span class="document-details">${doc.fileSize} • ${formatDate(doc.uploadedAt)}</span>
        </div>
        <button class="btn btn-sm btn-outline" onclick="downloadDocument(${doc.documentId})" title="Download Document">
            <i class="fa-solid fa-download"></i> View
        </button>
    `;
    return documentItem;
}

// Download document
function downloadDocument(documentId) {
    // Create a temporary link to trigger download
    const downloadUrl = `/loan-officer/download-document/${documentId}`;
    
    // Create hidden link and trigger download
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Format date for display
function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return 'Unknown date';
    }
}

// Add refresh button functionality if needed
document.addEventListener('DOMContentLoaded', () => {
    // Add any additional initialization code here
});
