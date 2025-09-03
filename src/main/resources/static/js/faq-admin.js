// FAQ Admin Management JavaScript - Updated
// Cache busting: v2.0

document.addEventListener('DOMContentLoaded', function() {
    initializeFAQAdmin();
});

function initializeFAQAdmin() {
    // Initialize event listeners
    initializeEventListeners();
    
    // Initialize modals
    initializeModals();
    
    // Load initial FAQs
    loadFAQs();
    

}

// Event Listeners
function initializeEventListeners() {
    // Create FAQ button
    const createFAQBtn = document.getElementById('createFAQBtn');
    createFAQBtn.addEventListener('click', function() {
        openCreateModal();
    });
    
    // Category filter
    initializeCategoryFilter();
}


// Category filter functionality
function initializeCategoryFilter() {
    const categorySelect = document.getElementById('categorySelect');
    
    categorySelect.addEventListener('change', function() {
        const selectedCategory = this.value;
        updateFilterInfo(selectedCategory);
        
        if (selectedCategory === 'All') {
            loadFAQs();
        } else {
            loadFAQsByCategory(selectedCategory);
        }
    });
}

// Update filter info display
function updateFilterInfo(filterType) {
    const filterInfoElement = document.getElementById('filterInfo');
    if (filterInfoElement) {
        const filterTexts = {
            'All': 'Showing all FAQs',
            'General': 'Showing General FAQs',
            'Account': 'Showing Account FAQs',
            'Loan': 'Showing Loan FAQs',
            'Security': 'Showing Security FAQs',
            'Technical': 'Showing Technical FAQs'
        };
        filterInfoElement.textContent = filterTexts[filterType] || `Showing ${filterType} FAQs`;
    }
}

// Modal functionality
function initializeModals() {
    const faqModal = document.getElementById('faqModal');
    const deleteModal = document.getElementById('deleteModal');
    const closeModal = document.getElementById('closeModal');
    const closeDeleteModal = document.getElementById('closeDeleteModal');
    const cancelBtn = document.getElementById('cancelBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    
    // Close modals
    closeModal.addEventListener('click', closeFAQModal);
    closeDeleteModal.addEventListener('click', closeDeleteModal);
    cancelBtn.addEventListener('click', closeFAQModal);
    cancelDeleteBtn.addEventListener('click', closeDeleteModal);
    
    // Close modals when clicking outside
    faqModal.addEventListener('click', function(e) {
        if (e.target === faqModal) {
            closeFAQModal();
        }
    });
    
    deleteModal.addEventListener('click', function(e) {
        if (e.target === deleteModal) {
            closeDeleteModal();
        }
    });
    
    // Close modals with Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (faqModal.style.display === 'block') {
                closeFAQModal();
            }
            if (deleteModal.style.display === 'block') {
                closeDeleteModal();
            }
        }
    });
    
    // Form submission
    const faqForm = document.getElementById('faqForm');
    faqForm.addEventListener('submit', handleFormSubmit);
    
    // Delete confirmation
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    confirmDeleteBtn.addEventListener('click', handleDeleteConfirm);
}

// Load all FAQs
function loadFAQs() {
    showLoadingState();
    
    fetch('/admin/faq/list')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (data.faqs.length === 0) {
                    showNoResultsState();
                } else {
                    displayFAQs(data.faqs);
                }
            } else {
                showErrorState(data.error || 'Failed to load FAQs');
            }
        })
        .catch(error => {

            showErrorState('Failed to load FAQs. Please try again later.');
        });
}

// Load FAQs by category
function loadFAQsByCategory(category) {
    showLoadingState();
    
    const url = `/admin/faq/category/${encodeURIComponent(category)}`;
    console.log('Loading FAQs by category:', category, 'URL:', url);
    
    fetch(url)
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('FAQ data received:', data);
            if (data.success) {
                if (data.faqs.length === 0) {
                    showNoResultsState();
                } else {
                    displayFAQs(data.faqs);
                }
            } else {
                showErrorState(data.error || 'Failed to load FAQs');
            }
        })
        .catch(error => {
            console.error('Error loading FAQs by category:', error);
            showErrorState(`Failed to load FAQs: ${error.message}`);
        });
}


// Display FAQs in table
function displayFAQs(faqs) {
    const tableBody = document.getElementById('faqTableBody');
    const loadingState = document.getElementById('loadingState');
    const noResultsState = document.getElementById('noResultsState');
    const tableContainer = document.getElementById('faqTableContainer');
    
    // Hide loading and no results states
    loadingState.style.display = 'none';
    noResultsState.style.display = 'none';
    
    if (!faqs || faqs.length === 0) {
        showNoResultsState();
        return;
    }
    
    // Generate table rows
    const tableRows = faqs.map(faq => `
        <tr>
            <td class="question-cell">${escapeHtml(faq.question)}</td>
            <td class="category-cell">
                <span class="category-badge">${escapeHtml(faq.category)}</span>
            </td>
            <td class="date-cell">${formatDate(faq.createdAt)}</td>
            <td class="date-cell">${formatDate(faq.updatedAt) || 'Never'}</td>
            <td class="actions-cell">
                <div class="action-buttons">
                    <button class="action-btn edit-btn" onclick="editFAQ(${faq.faqId})" title="Edit FAQ">
                        <i class="fa-solid fa-edit"></i> Edit
                    </button>
                    <button class="action-btn delete-btn" onclick="deleteFAQ(${faq.faqId}, '${escapeHtml(faq.question)}')" title="Delete FAQ">
                        <i class="fa-solid fa-trash"></i> Delete
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
    
    tableBody.innerHTML = tableRows;
    tableContainer.style.display = 'block';
}

// Open create modal
function openCreateModal() {
    const modal = document.getElementById('faqModal');
    const modalTitle = document.getElementById('modalTitle');
    const form = document.getElementById('faqForm');
    const saveBtn = document.getElementById('saveBtn');
    
    // Reset form
    form.reset();
    form.dataset.mode = 'create';
    
    // Update modal title and button
    modalTitle.textContent = 'Create New FAQ';
    saveBtn.innerHTML = '<i class="fa-solid fa-save"></i> Create FAQ';
    
    // Show modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Focus on first input
    document.getElementById('questionInput').focus();
}

// Edit FAQ
function editFAQ(faqId) {
    fetch(`/admin/faq/${faqId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                openEditModal(data.faq);
            } else {
                showNotification('Failed to load FAQ details', 'error');
            }
        })
        .catch(error => {

            showNotification('Failed to load FAQ details', 'error');
        });
}

// Open edit modal
function openEditModal(faq) {
    const modal = document.getElementById('faqModal');
    const modalTitle = document.getElementById('modalTitle');
    const form = document.getElementById('faqForm');
    const saveBtn = document.getElementById('saveBtn');
    
    // Set form data
    document.getElementById('questionInput').value = faq.question;
    document.getElementById('answerInput').value = faq.answer;
    document.getElementById('categoryInput').value = faq.category || '';
    
    // Set form mode and FAQ ID
    form.dataset.mode = 'edit';
    form.dataset.faqId = faq.faqId;
    
    // Update modal title and button
    modalTitle.textContent = 'Edit FAQ';
    saveBtn.innerHTML = '<i class="fa-solid fa-save"></i> Update FAQ';
    
    // Show modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Focus on first input
    document.getElementById('questionInput').focus();
}

// Handle form submission
function handleFormSubmit(e) {
    e.preventDefault();
    
    const form = e.target;
    const mode = form.dataset.mode;
    const formData = new FormData(form);
    
    const requestData = {
        question: formData.get('question'),
        answer: formData.get('answer'),
        category: formData.get('category')
    };
    
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    let url, method;
    
    if (mode === 'create') {
        url = '/admin/faq/create';
        method = 'POST';
    } else {
        url = `/admin/faq/update/${form.dataset.faqId}`;
        method = 'PUT';
    }
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification(data.message, 'success');
            closeFAQModal();
            loadFAQs(); // Reload the FAQ list
        } else {
            showNotification(data.error || 'Operation failed', 'error');
        }
    })
    .catch(error => {

        showNotification('Failed to save FAQ. Please try again.', 'error');
    });
}

// Delete FAQ
function deleteFAQ(faqId, question) {
    const modal = document.getElementById('deleteModal');
    const deleteQuestion = document.getElementById('deleteQuestion');
    
    // Set the question to be deleted
    deleteQuestion.textContent = question;
    
    // Store FAQ ID for deletion
    modal.dataset.faqId = faqId;
    
    // Show modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

// Handle delete confirmation
function handleDeleteConfirm() {
    const modal = document.getElementById('deleteModal');
    const faqId = modal.dataset.faqId;
    
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    fetch(`/admin/faq/delete/${faqId}`, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification(data.message, 'success');
            closeDeleteModal();
            loadFAQs(); // Reload the FAQ list
        } else {
            showNotification(data.error || 'Delete failed', 'error');
        }
    })
    .catch(error => {

        showNotification('Failed to delete FAQ. Please try again.', 'error');
    });
}

// Close FAQ modal
function closeFAQModal() {
    const modal = document.getElementById('faqModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Close delete modal
function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Show loading state
function showLoadingState() {
    const loadingState = document.getElementById('loadingState');
    const tableContainer = document.getElementById('faqTableContainer');
    const noResultsState = document.getElementById('noResultsState');
    
    loadingState.style.display = 'block';
    tableContainer.style.display = 'none';
    noResultsState.style.display = 'none';
}

// Show no results state
function showNoResultsState() {
    const noResultsState = document.getElementById('noResultsState');
    const tableContainer = document.getElementById('faqTableContainer');
    const loadingState = document.getElementById('loadingState');
    
    noResultsState.style.display = 'block';
    tableContainer.style.display = 'none';
    loadingState.style.display = 'none';
}

// Show error state
function showErrorState(message) {
    const tableContainer = document.getElementById('faqTableContainer');
    const loadingState = document.getElementById('loadingState');
    const noResultsState = document.getElementById('noResultsState');
    
    tableContainer.innerHTML = `
        <div class="error-message">
            <i class="fa-solid fa-exclamation-triangle"></i>
            <span>${escapeHtml(message)}</span>
        </div>
    `;
    tableContainer.style.display = 'block';
    loadingState.style.display = 'none';
    noResultsState.style.display = 'none';
}

// Show notification
function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fa-solid fa-${type === 'success' ? 'check-circle' : 'exclamation-triangle'}"></i>
        <span>${escapeHtml(message)}</span>
    `;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 10px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 10px;
        animation: slideInRight 0.3s ease;
        max-width: 400px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    `;
    
    // Set background color based on type
    if (type === 'success') {
        notification.style.background = 'linear-gradient(135deg, #27ae60, #229954)';
    } else {
        notification.style.background = 'linear-gradient(135deg, #e74c3c, #c0392b)';
    }
    
    // Add to page
    document.body.appendChild(notification);
    
    // Remove after 5 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
}

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return 'Never';
    
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    } catch (error) {
        return dateString;
    }
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Make functions globally available
window.editFAQ = editFAQ;
window.deleteFAQ = deleteFAQ;
window.closeFAQModal = closeFAQModal;
window.closeDeleteModal = closeDeleteModal;
