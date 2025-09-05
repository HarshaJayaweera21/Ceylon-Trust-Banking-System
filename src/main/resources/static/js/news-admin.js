// News Admin JavaScript
let newsTable;
let currentNewsId = null;
let isEditMode = false;

document.addEventListener('DOMContentLoaded', function() {
    initializeNewsAdmin();
});

function initializeNewsAdmin() {
    // Initialize DataTable
    initializeDataTable();
    
    // Search removed per request
    
    // Initialize filters
    initializeFilters();
    
    // Initialize modals
    initializeModals();
    
    // Initialize action buttons
    initializeActionButtons();
    
    // Initialize delete confirmation button
    initializeDeleteButton();
}

// Initialize DataTable
function initializeDataTable() {
    newsTable = $('#newsTable').DataTable({
        responsive: true,
        pageLength: 10,
        order: [[0, 'desc']], // Sort by ID descending
        columnDefs: [
            { orderable: false, targets: [8] }, // Actions column
            { width: "80px", targets: [0] }, // ID column
            { width: "300px", targets: [1] }, // Title column
            { width: "120px", targets: [2, 3, 4] }, // Category, Visibility, Status columns
            { width: "150px", targets: [5, 6, 7] }, // Posted By, Posted Date, Expiry Date columns
            { width: "120px", targets: [8] } // Actions column
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
            emptyTable: "No news found",
            zeroRecords: "No matching news found"
        }
    });

    // Build custom pagination after DataTable init
    buildCustomPagination();

    // Rebuild on table draw (e.g., after search/filter)
    $('#newsTable').on('draw.dt', function () {
        buildCustomPagination();
    });
}

// Initialize action buttons
function initializeActionButtons() {
    // Use event delegation for dynamically added buttons
    document.addEventListener('click', function(e) {
        if (e.target.closest('.view-btn')) {
            e.preventDefault();
            const button = e.target.closest('.view-btn');
            const newsId = parseInt(button.getAttribute('data-news-id'));
            viewNews(newsId);
        }
        
        if (e.target.closest('.edit-btn')) {
            e.preventDefault();
            const button = e.target.closest('.edit-btn');
            const newsId = parseInt(button.getAttribute('data-news-id'));
            editNews(newsId);
        }
        
        if (e.target.closest('.delete-btn')) {
            e.preventDefault();
            const button = e.target.closest('.delete-btn');
            const newsId = parseInt(button.getAttribute('data-news-id'));
            deleteNews(newsId);
        }
    });
}

// Initialize delete button
function initializeDeleteButton() {
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', confirmDelete);
    }
}

// Search functionality removed

// Build custom pagination UI synced with DataTables
function buildCustomPagination() {
    const api = newsTable;
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
    // Adjust start if we don't have enough pages at the end
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

// Initialize filters
function initializeFilters() {
    const categorySelect = document.getElementById('categorySelect');
    const statusSelect = document.getElementById('statusSelect');
    
    // Category filter
    categorySelect.addEventListener('change', function() {
        const selectedCategory = this.value;
        if (selectedCategory === 'All') {
            newsTable.column(2).search('').draw();
        } else {
            newsTable.column(2).search('^' + selectedCategory + '$', true, false).draw();
        }
    });
    
    // Status filter
    statusSelect.addEventListener('change', function() {
        const selectedStatus = this.value;
        if (selectedStatus === 'All') {
            newsTable.column(4).search('').draw();
        } else {
            newsTable.column(4).search(selectedStatus, true, false).draw();
        }
    });
}

// Initialize modals
function initializeModals() {
    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        const newsModal = document.getElementById('newsModal');
        const viewModal = document.getElementById('viewModal');
        const deleteModal = document.getElementById('deleteModal');
        
        if (event.target === newsModal) {
            closeModal();
        }
        if (event.target === viewModal) {
            closeViewModal();
        }
        if (event.target === deleteModal) {
            closeDeleteModal();
        }
    });
    
    // Form submission
    const newsForm = document.getElementById('newsForm');
    newsForm.addEventListener('submit', function(e) {
        e.preventDefault();
        if (isEditMode) {
            updateNews();
        } else {
            createNews();
        }
    });
}

// Open create modal
function openCreateModal() {
    isEditMode = false;
    currentNewsId = null;
    
    document.getElementById('modalTitle').textContent = 'Create News';
    document.getElementById('submitBtn').textContent = 'Create News';
    document.getElementById('newsForm').reset();
    
    // Set default values
    document.getElementById('newsVisibility').value = 'true';
    
    document.getElementById('newsModal').style.display = 'block';
}

// Open edit modal
function editNews(newsId) {
    isEditMode = true;
    currentNewsId = newsId;
    
    // Fetch news details
    fetch(`/admin/news/${newsId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const news = data.news;
                
                document.getElementById('modalTitle').textContent = 'Edit News';
                document.getElementById('submitBtn').textContent = 'Update News';
                
                // Populate form
                document.getElementById('newsTitle').value = news.title;
                document.getElementById('newsContent').value = news.content;
                document.getElementById('newsCategory').value = news.category;
                document.getElementById('newsVisibility').value = news.isPublic ? 'true' : 'false';
                
                if (news.expiryDate) {
                    document.getElementById('newsExpiryDate').value = news.expiryDate;
                } else {
                    document.getElementById('newsExpiryDate').value = '';
                }
                
                document.getElementById('newsModal').style.display = 'block';
            } else {
                showNotification('Error fetching news details: ' + data.error, 'error');
            }
        })
        .catch(error => {

            showNotification('Error fetching news details', 'error');
        });
}

// Close modal
function closeModal() {
    document.getElementById('newsModal').style.display = 'none';
    document.getElementById('newsForm').reset();
    isEditMode = false;
    currentNewsId = null;
}

// Create news
function createNews() {
    const formData = {
        title: document.getElementById('newsTitle').value.trim(),
        content: document.getElementById('newsContent').value.trim(),
        category: document.getElementById('newsCategory').value,
        expiryDate: document.getElementById('newsExpiryDate').value || null,
        isPublic: document.getElementById('newsVisibility').value === 'true'
    };
    
    // Validate form
    if (!formData.title || !formData.content || !formData.category) {
        showNotification('Please fill in all required fields', 'error');
        return;
    }
    
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    const headers = {
        'Content-Type': 'application/json'
    };
    headers[csrfHeader] = csrfToken;
    
    fetch('/admin/news/create', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification('News created successfully', 'success');
            closeModal();
            // Reload the page to show updated data
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showNotification('Error creating news: ' + data.error, 'error');
        }
    })
    .catch(error => {

        showNotification('Error creating news', 'error');
    });
}

// Update news
function updateNews() {
    const formData = {
        title: document.getElementById('newsTitle').value.trim(),
        content: document.getElementById('newsContent').value.trim(),
        category: document.getElementById('newsCategory').value,
        expiryDate: document.getElementById('newsExpiryDate').value || null,
        isPublic: document.getElementById('newsVisibility').value === 'true'
    };
    
    // Validate form
    if (!formData.title || !formData.content || !formData.category) {
        showNotification('Please fill in all required fields', 'error');
        return;
    }
    
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    const headers = {
        'Content-Type': 'application/json'
    };
    headers[csrfHeader] = csrfToken;
    
    fetch(`/admin/news/update/${currentNewsId}`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification('News updated successfully', 'success');
            closeModal();
            // Reload the page to show updated data
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            showNotification('Error updating news: ' + data.error, 'error');
        }
    })
    .catch(error => {

        showNotification('Error updating news', 'error');
    });
}

// View news
function viewNews(newsId) {
    fetch(`/admin/news/${newsId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const news = data.news;
                
                const viewContent = document.getElementById('viewContent');
                viewContent.innerHTML = `
                    <div class="news-detail">
                        <h3>${escapeHtml(news.title)}</h3>
                        <div class="meta-info">
                            <div class="meta-item">
                                <span class="meta-label">Category:</span>
                                <span class="meta-value">${escapeHtml(news.category)}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Visibility:</span>
                                <span class="meta-value">${news.isPublic ? 'Public' : 'Staff Only'}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Status:</span>
                                <span class="meta-value">${news.isExpired ? 'Expired' : 'Active'}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Posted By:</span>
                                <span class="meta-value">${escapeHtml(news.postedBy)}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Posted Date:</span>
                                <span class="meta-value">${formatDateTime(news.postedAt)}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Expiry Date:</span>
                                <span class="meta-value">${news.expiryDate ? formatDate(news.expiryDate) : 'Never'}</span>
                            </div>
                        </div>
                        <div class="content">${escapeHtml(news.content)}</div>
                    </div>
                `;
                
                document.getElementById('viewModal').style.display = 'block';
            } else {
                showNotification('Error fetching news details: ' + data.error, 'error');
            }
        })
        .catch(error => {

            showNotification('Error fetching news details', 'error');
        });
}

// Close view modal
function closeViewModal() {
    document.getElementById('viewModal').style.display = 'none';
}

// Delete news
function deleteNews(newsId) {
    currentNewsId = newsId;
    document.getElementById('deleteModal').style.display = 'block';
}

// Close delete modal
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    currentNewsId = null;
}

// Confirm delete
function confirmDelete() {
    if (currentNewsId) {
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        
        const headers = {};
        headers[csrfHeader] = csrfToken;
        
        fetch(`/admin/news/delete/${currentNewsId}`, {
            method: 'DELETE',
            headers: headers
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification('News deleted successfully', 'success');
                closeDeleteModal();
                // Reload the page to show updated data
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showNotification('Error deleting news: ' + data.error, 'error');
            }
        })
        .catch(error => {
            showNotification('Error deleting news', 'error');
        });
    }
}

// Show notification
function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    
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
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Format date and time
function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Make functions globally available
window.viewNews = viewNews;
window.editNews = editNews;
window.deleteNews = deleteNews;
window.openCreateModal = openCreateModal;
window.closeModal = closeModal;
window.closeDeleteModal = closeDeleteModal;
window.confirmDelete = confirmDelete;
