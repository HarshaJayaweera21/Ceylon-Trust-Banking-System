// Bank Manager Dashboard JavaScript - Matching Loan Officer Dashboard Design

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
    toast.className = 'toast ' + (type === 'error' ? 'error' : 'success');
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.style.display = 'none', 300);
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
        'Approve High-Value Loan',
        'Are you sure you want to approve this high-value loan application? This action cannot be undone.',
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
        'Reject High-Value Loan',
        'Are you sure you want to reject this high-value loan application? This action cannot be undone.',
        'reject'
    );
}

// Review Loan
function reviewLoan(button) {
    const loanId = button.getAttribute('data-loan-id');
    currentLoanId = loanId;
    
    // Fetch loan details
    fetch(`/bank-manager/loan/${loanId}/details`)
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
            
            // Show modal
            reviewModal.style.display = 'flex';
            document.body.classList.add('no-scroll');
        })
        .catch(error => {
            showToast('Failed to load loan details. Please try again.', 'error');
        });
}

// Close Review Modal
function closeReviewModal() {
    reviewModal.style.display = 'none';
    document.body.classList.remove('no-scroll');
    currentLoanId = null;
    // Don't clear comments - let them persist for next review
}

// Submit Review
function submitReview() {
    if (!currentLoanId) {
        showToast('No loan selected for review.', 'error');
        return;
    }
    
    const comments = document.getElementById('officerComments').value.trim();
    
    fetch(`/bank-manager/review/${currentLoanId}`, {
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
            showToast('Review comments saved successfully!', 'success');
        } else {
            showToast('Failed to save review comments. Please try again.', 'error');
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
    
    // Set appropriate icon
    const icon = document.getElementById('confirmIcon');
    if (type === 'approve') {
        icon.innerHTML = '<i class="fa-solid fa-check-circle"></i>';
    } else if (type === 'reject') {
        icon.innerHTML = '<i class="fa-solid fa-times-circle"></i>';
    } else {
        icon.innerHTML = '<i class="fa-solid fa-question-circle"></i>';
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
    
    const { type } = pendingAction;
    
    if (type === 'approve') {
        const { loanId, row } = pendingAction;
        executeApprove(loanId, row);
    } else if (type === 'reject') {
        const { loanId, row } = pendingAction;
        executeReject(loanId, row);
    } else if (type === 'changeRole') {
        const { userId, newRole, row, select, button } = pendingAction;
        executeChangeRole(userId, newRole, row, select, button);
    }
    
    closeConfirmModal();
}

// Execute Approve Action
function executeApprove(loanId, row) {
    fetch(`/bank-manager/approve/${loanId}`, {
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
            
            showToast('High-value loan approved successfully!', 'success');
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
    fetch(`/bank-manager/reject/${loanId}`, {
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
            
            showToast('High-value loan rejected successfully!', 'success');
        } else {
            showToast('Failed to reject loan. Please try again.', 'error');
        }
    })
    .catch(error => {
        showToast('An error occurred. Please try again.', 'error');
    });
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

// Role Management Functions
function initializeRoleManagement() {
    // Add event listeners to all role select dropdowns
    const roleSelects = document.querySelectorAll('.role-select');
    roleSelects.forEach(select => {
        select.addEventListener('change', handleRoleSelectChange);
    });
}

function handleRoleSelectChange(event) {
    const select = event.target;
    const userId = select.getAttribute('data-user-id');
    const button = select.closest('tr').querySelector('.action-buttons .btn');
    
    if (select.value && select.value !== '') {
        button.disabled = false;
        button.classList.remove('btn-primary');
        button.classList.add('btn-warning');
        button.innerHTML = '<i class="fa-solid fa-arrows-rotate"></i> Change Role';
    } else {
        button.disabled = true;
        button.classList.remove('btn-warning');
        button.classList.add('btn-primary');
        button.innerHTML = '<i class="fa-solid fa-arrows-rotate"></i> Change Role';
    }
}

function changeUserRole(button) {
    const userId = button.getAttribute('data-user-id');
    const row = button.closest('tr');
    const select = row.querySelector('.role-select');
    const newRole = select.value;
    const currentRole = select.getAttribute('data-current-role');
    
    if (!newRole || newRole === '') {
        showToast('Please select a new role first.', 'error');
        return;
    }
    
    if (newRole === currentRole) {
        showToast('User already has this role.', 'error');
        return;
    }
    
    // Store the action for confirmation
    pendingAction = {
        type: 'changeRole',
        userId: userId,
        newRole: newRole,
        currentRole: currentRole,
        row: row,
        select: select,
        button: button
    };
    
    // Show confirmation modal
    showConfirmModal(
        'Change User Role',
        `Are you sure you want to change this user's role from "${currentRole}" to "${newRole}"? This action will affect their system permissions.`,
        'changeRole'
    );
}

function executeChangeRole(userId, newRole, row, select, button) {
    fetch(`/bank-manager/change-role/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...csrfHeaders()
        },
        body: `newRole=${encodeURIComponent(newRole)}`
    })
    .then(response => {
        if (response.ok) {
            // Update UI
            const currentRoleBadge = row.querySelector('.role-badge');
            const newRoleDisplay = newRole.replace(/([A-Z])/g, ' $1').trim();
            
            // Update role badge
            currentRoleBadge.textContent = newRoleDisplay;
            currentRoleBadge.className = 'role-badge role-' + newRole.toLowerCase();
            
            // Update select attributes and options
            select.setAttribute('data-current-role', newRole);
            select.value = '';
            
            // Update select options to exclude the new current role
            const options = select.querySelectorAll('option');
            options.forEach(option => {
                if (option.value === newRole) {
                    option.style.display = 'none';
                } else if (option.value !== '' && option.value !== newRole) {
                    option.style.display = 'block';
                }
            });
            
            // Reset button
            button.disabled = true;
            button.classList.remove('btn-warning');
            button.classList.add('btn-primary');
            button.innerHTML = '<i class="fa-solid fa-arrows-rotate"></i> Change Role';
            
            showToast(`User role changed to ${newRoleDisplay} successfully!`, 'success');
        } else {
            showToast('Failed to change user role. Please try again.', 'error');
        }
    })
    .catch(error => {
        showToast('An error occurred. Please try again.', 'error');
    });
}


// Add refresh button functionality if needed
document.addEventListener('DOMContentLoaded', () => {
    // Initialize role management
    initializeRoleManagement();
});