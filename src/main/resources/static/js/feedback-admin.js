// Feedback Admin JavaScript
let feedbackTable;
let currentFeedbackId = null;
let currentDateFilter = 'all';

document.addEventListener('DOMContentLoaded', function() {
    initializeFeedbackAdmin();
});

function initializeFeedbackAdmin() {
    // Initialize DataTable
    initializeDataTable();
    
    // Initialize filters
    initializeFilters();
    
    // Initialize modals
    initializeModals();
    
    // Load initial data
    loadAllFeedback();
}

// Initialize DataTable
function initializeDataTable() {
    feedbackTable = $('#feedbackTable').DataTable({
        responsive: true,
        pageLength: 10,
        order: [[0, 'desc']], // Sort by ID descending
        columnDefs: [
            { orderable: false, targets: [5] }, // Actions column
            { width: "80px", targets: [0] }, // ID column
            { width: "200px", targets: [1] }, // User column
            { width: "300px", targets: [2] }, // Message column
            { width: "150px", targets: [3] }, // Date column
            { width: "120px", targets: [4] }, // Status column
            { width: "100px", targets: [5] } // Actions column
        ],
        language: {
            search: "Search:",
            lengthMenu: "Show _MENU_ entries",
            info: "Showing _START_ to _END_ of _TOTAL_ entries",
            paginate: {
                first: "First",
                last: "Last",
                next: "Next",
                previous: "Previous"
            },
            emptyTable: "No feedback found",
            zeroRecords: "No matching feedback found"
        }
    });

    // Build custom pagination after DataTable init
    buildCustomPagination();

    // Rebuild on table draw (e.g., after filter/date change)
    $('#feedbackTable').on('draw.dt', function () {
        buildCustomPagination();
    });
}

// Initialize filters
function initializeFilters() {
    const dateFilterSelect = document.getElementById('dateFilterSelect');
    
    if (dateFilterSelect) {
        // Date filter
        dateFilterSelect.addEventListener('change', function() {
            currentDateFilter = this.value;
            filterByDate(currentDateFilter);
        });
    }
}

// Initialize modals
function initializeModals() {
    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        const viewModal = document.getElementById('viewModal');
        const deleteModal = document.getElementById('deleteModal');
        
        if (event.target === viewModal) {
            closeViewModal();
        }
        if (event.target === deleteModal) {
            closeDeleteModal();
        }
    });

    // Confirm delete button
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function() {
            if (currentFeedbackId) {
                deleteFeedbackConfirm();
            }
        });
    }
}

// Filter by date
function filterByDate(filterType) {
    // Update filter info display
    updateFilterInfo(filterType);
    // Reload and apply client-side filter after fetch
    loadAllFeedback();
}

// Load all feedback
function loadAllFeedback() {
    fetch('/admin/feedback/list')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const filtered = filterFeedbackData(data.feedback, currentDateFilter);
                updateTable(filtered);
                // After loading, ensure pagination sync
                buildCustomPagination();
            } else {
                showNotification('Error loading feedback: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error loading feedback:', error);
            showNotification('Error loading feedback', 'error');
        });
}

// Update table with new data
function updateTable(feedbackData) {
    // Clear existing data
    feedbackTable.clear();
    
    // Update record count
    updateRecordCount(feedbackData.length);
    
    // Add new data
    feedbackData.forEach(feedback => {
        const submittedDate = new Date(feedback.submittedAt);
        const statusBadge = feedback.isRecent ? 
            '<span class="status-badge recent"><i class="fas fa-clock"></i> Recent</span>' :
            '<span class="status-badge older"><i class="fas fa-history"></i> Older</span>';
        
        const rawRole = (feedback.user && feedback.user.role) ? feedback.user.role : (feedback.userRole || 'Unknown');
        const roleText = extractRoleName(rawRole);
        
        const actionButtons = `
            <div class="action-buttons">
                <button class="btn btn-sm btn-info" onclick="viewFeedback(${feedback.feedbackId})" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteFeedback(${feedback.feedbackId})" title="Delete Feedback">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
        
        feedbackTable.row.add([
            feedback.feedbackId,
            `
                <div class="user-cell">
                    <div class="user-name">${escapeHtml((feedback.user && (feedback.user.firstName + ' ' + feedback.user.lastName)) || feedback.userName || 'Unknown')}</div>
                    <div class="user-email">${escapeHtml((feedback.user && feedback.user.email) || feedback.userEmail || 'Unknown')}</div>
                    <div class="user-role">${escapeHtml(roleText)}</div>
                </div>
            `,
            `<div class="message-cell"><span>${escapeHtml(feedback.message)}</span></div>`,
            formatDateTime(submittedDate),
            statusBadge,
            actionButtons
        ]);
    });
    
    // Redraw table
    feedbackTable.draw();
    // Sync pagination
    buildCustomPagination();
}

// Update record count display
function updateRecordCount(count) {
    const recordCountElement = document.getElementById('recordCount');
    if (recordCountElement) {
        recordCountElement.textContent = `${count} record${count !== 1 ? 's' : ''}`;
    }
}

// Update filter info display
function updateFilterInfo(filterType) {
    const filterInfoElement = document.getElementById('filterInfo');
    if (filterInfoElement) {
        const filterTexts = {
            'all': 'Showing all feedback',
            'today': 'Showing today\'s feedback',
            'week': 'Showing this week\'s feedback',
            'month': 'Showing this month\'s feedback'
        };
        filterInfoElement.textContent = filterTexts[filterType] || 'Showing all feedback';
    }
}

// Apply date filter to list returned from server
function filterFeedbackData(list, filterType) {
    if (!Array.isArray(list) || !filterType || filterType === 'all') {
        return list || [];
    }
    const now = new Date();
    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startOfWeek = new Date(startOfToday);
    // Treat Sunday as start of week
    startOfWeek.setDate(startOfToday.getDate() - startOfToday.getDay());
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    return list.filter(item => {
        const ts = new Date(item.submittedAt).getTime();
        if (Number.isNaN(ts)) return false;
        switch (filterType) {
            case 'today':
                return ts >= startOfToday.getTime();
            case 'week':
                return ts >= startOfWeek.getTime();
            case 'month':
                return ts >= startOfMonth.getTime();
            default:
                return true;
        }
    });
}

// Build custom pagination UI synced with DataTables
function buildCustomPagination() {
    const api = feedbackTable;
    if (!api) return;

    const info = api.page.info();
    const container = document.getElementById('customPagination');
    if (!container) return;
    container.innerHTML = '';

    const createBtn = (label, disabled, onClick, className = 'page-btn') => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = className;
        btn.textContent = label;
        if (disabled) btn.disabled = true;
        btn.addEventListener('click', onClick);
        return btn;
    };

    // Prev button
    const prevBtn = createBtn('Prev', info.page === 0, () => api.page('previous').draw(false));
    container.appendChild(prevBtn);

    // Page numbers (compact around current)
    const maxNumbers = 5;
    const totalPages = info.pages;
    const currentPage = info.page; // zero-indexed
    let start = Math.max(0, currentPage - Math.floor(maxNumbers / 2));
    let end = Math.min(totalPages - 1, start + maxNumbers - 1);
    start = Math.max(0, Math.min(start, end - maxNumbers + 1));

    for (let i = start; i <= end; i++) {
        const isActive = i === currentPage;
        const numberBtn = createBtn(String(i + 1), false, () => api.page(i).draw(false), 'page-number' + (isActive ? ' active' : ''));
        container.appendChild(numberBtn);
    }

    // Next button
    const nextBtn = createBtn('Next', currentPage >= totalPages - 1, () => api.page('next').draw(false));
    container.appendChild(nextBtn);
}

// View feedback
function viewFeedback(feedbackId) {
    console.log('Viewing feedback:', feedbackId);
    fetch(`/admin/feedback/${feedbackId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showFeedbackModal(data.feedback);
            } else {
                showNotification('Error loading feedback details: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error loading feedback details:', error);
            showNotification('Error loading feedback details', 'error');
        });
}

// Show feedback modal
function showFeedbackModal(feedback) {
    const submittedDate = new Date(feedback.submittedAt);
    
    // Extract user info from various possible formats
    const userName = (feedback.user && feedback.user.firstName && feedback.user.lastName) 
        ? `${feedback.user.firstName} ${feedback.user.lastName}` 
        : (feedback.userName || 'Unknown');
    const userEmail = (feedback.user && feedback.user.email) 
        ? feedback.user.email 
        : (feedback.userEmail || 'Unknown');
    const userNic = (feedback.user && feedback.user.nic) 
        ? feedback.user.nic 
        : (feedback.userNic || 'N/A');
    
    const viewContent = document.getElementById('viewContent');
    if (viewContent) {
        viewContent.innerHTML = `
            <div class="feedback-detail">
                <!-- Feedback Header -->
                <div class="feedback-header">
                    <div class="feedback-title">
                        <h3><i class="fas fa-comment-dots"></i> Feedback Details</h3>
                        <div class="feedback-status">
                            <span class="status-badge ${feedback.isRecent ? 'recent' : 'older'}">
                                <i class="fas fa-${feedback.isRecent ? 'clock' : 'history'}"></i> 
                                ${feedback.isRecent ? 'Recent' : 'Older'}
                            </span>
                        </div>
                    </div>
                    <div class="feedback-meta">
                        <div class="meta-item">
                            <i class="fas fa-calendar"></i>
                            <span>Submitted: ${formatDateTime(submittedDate)}</span>
                        </div>
                    </div>
                </div>

                <!-- User Information Card -->
                <div class="user-card">
                    <div class="card-header">
                        <h4><i class="fas fa-user"></i> User Information</h4>
                    </div>
                    <div class="card-body">
                        <div class="user-grid">
                            <div class="user-field">
                                <label><i class="fas fa-user-circle"></i> Full Name</label>
                                <span>${escapeHtml(userName)}</span>
                            </div>
                            <div class="user-field">
                                <label><i class="fas fa-envelope"></i> Email Address</label>
                                <span>${escapeHtml(userEmail)}</span>
                            </div>
                            <div class="user-field">
                                <label><i class="fas fa-id-card"></i> NIC Number</label>
                                <span>${escapeHtml(userNic)}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Feedback Content Card -->
                <div class="content-card">
                    <div class="card-header">
                        <h4><i class="fas fa-message"></i> Feedback Message</h4>
                    </div>
                    <div class="card-body">
                        <div class="feedback-content">
                            ${escapeHtml(feedback.message)}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    const viewModal = document.getElementById('viewModal');
    if (viewModal) {
        viewModal.style.display = 'block';
    }
}

// Close view modal
function closeViewModal() {
    const viewModal = document.getElementById('viewModal');
    if (viewModal) {
        viewModal.style.display = 'none';
    }
}

// Delete feedback
function deleteFeedback(feedbackId) {
    console.log('Deleting feedback:', feedbackId);
    currentFeedbackId = feedbackId;
    const deleteModal = document.getElementById('deleteModal');
    if (deleteModal) {
        deleteModal.style.display = 'block';
    }
}

// Close delete modal
function closeDeleteModal() {
    const deleteModal = document.getElementById('deleteModal');
    if (deleteModal) {
        deleteModal.style.display = 'none';
    }
    currentFeedbackId = null;
}

// Confirm delete
function deleteFeedbackConfirm() {
    if (currentFeedbackId) {
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        
        fetch(`/admin/feedback/delete/${currentFeedbackId}`, {
            method: 'DELETE',
            headers: {
                [csrfHeader]: csrfToken
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification('Feedback deleted successfully', 'success');
                closeDeleteModal();
                // Reload the page to show updated data
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showNotification('Error deleting feedback: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting feedback:', error);
            showNotification('Error deleting feedback', 'error');
        });
    }
}

// Show notification
function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    if (!container) return;
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    const icon = getNotificationIcon(type);
    notification.innerHTML = `
        <i class="fas ${icon}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

// Get notification icon
function getNotificationIcon(type) {
    switch (type) {
        case 'success':
            return 'fa-check-circle';
        case 'error':
            return 'fa-exclamation-circle';
        case 'warning':
            return 'fa-exclamation-triangle';
        case 'info':
        default:
            return 'fa-info-circle';
    }
}

// Escape HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Format date and time
function formatDateTime(date) {
    if (!date) return 'Unknown';
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Extract role name from possible object/string formats
function extractRoleName(roleValue) {
    if (!roleValue) return 'Unknown';
    if (typeof roleValue === 'string') {
        // Handle stringified object like: Role(roleId=1, roleName=Customer, description=...)
        const match = roleValue.match(/roleName\s*=\s*([^,\)]+)/i);
        if (match && match[1]) {
            return match[1].trim();
        }
        return roleValue;
    }
    if (typeof roleValue === 'object') {
        if (roleValue.roleName) return String(roleValue.roleName);
        if (roleValue.name) return String(roleValue.name);
    }
    return String(roleValue);
}