document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Customers page loaded');
    
    // Initialize customer management functionality
    initCustomerManagement();
    
    // Ensure global functions are available after a short delay
    setTimeout(() => {
        console.log('Global functions should be available now');
        console.log('viewCustomer available:', typeof window.viewCustomer);
        console.log('deactivateCustomer available:', typeof window.deactivateCustomer);
        console.log('activateCustomer available:', typeof window.activateCustomer);
    }, 100);
});

function initCustomerManagement() {
    // Customer view modal functionality
    const customerViewModal = document.getElementById('customerViewModal');
    
    // Event delegation is handled by onclick attributes in HTML
    // No need for additional event listeners here
    
    // Filter form functionality
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault(); // Prevent default form submission to avoid scrolling
            
            const submitBtn = this.querySelector('.search-btn');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Searching...';
            submitBtn.disabled = true;
            
            // Get form data
            const formData = new FormData(this);
            const params = new URLSearchParams(formData);
            
            // Use AJAX to submit the form without page reload
            fetch(`${this.action}?${params.toString()}`, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.text())
            .then(html => {
                // Parse the response to extract only the content we need
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                // Replace only the main content area, preserving header and footer
                const newContent = doc.querySelector('.main-content');
                const currentContent = document.querySelector('.main-content');
                
                if (newContent && currentContent) {
                    currentContent.innerHTML = newContent.innerHTML;
                }
                
                // Re-initialize the page
                initCustomerManagement();
            })
            .catch(error => {
                console.error('Search error:', error);
                // Fallback to normal form submission
                this.submit();
            })
            .finally(() => {
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            });
        });
    }
    
    // Handle hideInactive checkbox change
    const hideInactiveCheckbox = document.getElementById('hideInactive');
    if (hideInactiveCheckbox) {
        hideInactiveCheckbox.addEventListener('change', function() {
            // Update the hidden input in the form
            const hiddenInput = searchForm.querySelector('input[name="hideInactive"]');
            if (hiddenInput) {
                hiddenInput.value = this.checked;
            }
            // Submit the form to refresh results using AJAX
            const formData = new FormData(searchForm);
            const params = new URLSearchParams(formData);
            
            fetch(`${searchForm.action}?${params.toString()}`, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.text())
            .then(html => {
                // Parse the response to extract only the content we need
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                // Replace only the main content area, preserving header and footer
                const newContent = doc.querySelector('.main-content');
                const currentContent = document.querySelector('.main-content');
                
                if (newContent && currentContent) {
                    currentContent.innerHTML = newContent.innerHTML;
                }
                
                // Re-initialize the page
                initCustomerManagement();
            })
            .catch(error => {
                console.error('Filter error:', error);
                // Fallback to normal form submission
                searchForm.submit();
            });
        });
    }
    
    // City filter removed - no longer needed
    
    // Handle modal close buttons
    const closeButtons = document.querySelectorAll('.close');
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) {
                modal.style.display = 'none';
            }
        });
    });
    
    // Handle modal close when clicking outside
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                // Close the appropriate modal based on its ID
                if (this.id === 'customerModal') {
                    closeCustomerModal();
                } else if (this.id === 'confirmationModal') {
                    closeConfirmationModal();
                } else if (this.id === 'staffEditModal') {
                    closeEditStaffModal();
                } else if (this.id === 'addStaffModal') {
                    closeAddStaffModal();
                } else {
                    // Generic close for any other modals
                    this.style.display = 'none';
                    this.classList.remove('show');
                }
            }
        });
    });
}

function fillCustomerModal(customerData) {
    const toDate = (dateString) => dateString ? new Date(dateString) : null;
    
    const content = `
        <div class="modal-section">
            <h4>Personal Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">User ID</span>
                    <span class="info-value">${customerData.userId || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Username</span>
                    <span class="info-value">${customerData.username || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Email</span>
                    <span class="info-value">${customerData.email || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">NIC</span>
                    <span class="info-value">${customerData.nic || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">First Name</span>
                    <span class="info-value">${customerData.firstName || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Last Name</span>
                    <span class="info-value">${customerData.lastName || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Date of Birth</span>
                    <span class="info-value">${customerData.dob ? new Date(customerData.dob).toLocaleDateString() : ''}</span>
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>Address Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Street</span>
                    <span class="info-value">${customerData.street || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">City</span>
                    <span class="info-value">${customerData.city || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Postal Code</span>
                    <span class="info-value">${customerData.postalCode || ''}</span>
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>System Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Status</span>
                    <span class="info-value">
                        <span class="status-badge ${customerData.isActive ? 'active' : 'inactive'}">
                            ${customerData.isActive ? 'Active' : 'Inactive'}
                        </span>
                    </span>
                </div>
                <div class="info-item">
                    <span class="info-label">Created At</span>
                    <span class="info-value">${toDate(customerData.createdAt)?.toLocaleString() || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Updated At</span>
                    <span class="info-value">${toDate(customerData.updatedAt)?.toLocaleString() || ''}</span>
                </div>
            </div>
        </div>
    `;
    
    document.getElementById('customerDetailsContent').innerHTML = content;
}

// These functions are now handled by the global window functions below

function showConfirmationModal(message, confirmCallback) {
    console.log('showConfirmationModal called with message:', message);
    const modal = document.getElementById('confirmationModal');
    const messageEl = document.getElementById('confirmationMessage');
    const confirmBtn = document.getElementById('confirmButton');
    
    console.log('Modal element:', modal);
    console.log('Message element:', messageEl);
    console.log('Confirm button:', confirmBtn);
    
    if (!modal || !messageEl || !confirmBtn) {
        console.error('Missing modal elements!');
        return;
    }
    
    messageEl.textContent = message;
    modal.classList.add('show');
    console.log('Modal should be visible now');
    console.log('Modal computed style:', window.getComputedStyle(modal).display);
    console.log('Modal z-index:', window.getComputedStyle(modal).zIndex);
    
    confirmBtn.onclick = function() {
        console.log('Confirm button clicked');
        modal.classList.remove('show');
        console.log('Executing confirmation callback immediately');
        confirmCallback();
    };
}

function closeConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    modal.classList.remove('show');
}

function closeCustomerModal() {
    const modal = document.getElementById('customerViewModal');
    modal.classList.remove('show');
}

function showNotification(message, type) {
    console.log('showNotification called with:', message, type);
    
    // Remove any existing notifications
    const existingNotifications = document.querySelectorAll('.notification-toast');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create a new notification element
    const notification = document.createElement('div');
    notification.className = 'notification-toast';
    notification.textContent = message;
    
    // Apply styles directly (same as dashboard.js)
    notification.style.cssText = `
        position: fixed !important;
        top: 20px !important;
        left: 50% !important;
        transform: translateX(-50%) !important;
        padding: 16px 24px !important;
        border-radius: 12px !important;
        color: #fff !important;
        font-size: 16px !important;
        font-weight: 500 !important;
        display: block !important;
        z-index: 9999 !important;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3) !important;
        min-width: 300px !important;
        max-width: 500px !important;
        width: auto !important;
        text-align: center !important;
        word-wrap: break-word !important;
        line-height: 1.4 !important;
        background-color: ${type === 'success' ? '#28a745' : '#dc3545'} !important;
    `;
    
    console.log('Created notification element:', notification);
    
    // Add to body
    document.body.appendChild(notification);
    
    console.log('Notification added to body');
    
    // Auto-hide after 4 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 4000);
}

// Reset filters function
function clearFilters() {
    const searchInput = document.getElementById('search');
    const hideInactiveCheckbox = document.getElementById('hideInactive');
    
    if (searchInput) searchInput.value = '';
    if (hideInactiveCheckbox) hideInactiveCheckbox.checked = false;
    
    // Submit the form to refresh results using AJAX
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        const formData = new FormData(searchForm);
        const params = new URLSearchParams(formData);
        
        fetch(`${searchForm.action}?${params.toString()}`, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => response.text())
        .then(html => {
            // Parse the response to extract only the content we need
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            // Replace only the main content area, preserving header and footer
            const newContent = doc.querySelector('.main-content');
            const currentContent = document.querySelector('.main-content');
            
            if (newContent && currentContent) {
                currentContent.innerHTML = newContent.innerHTML;
            }
            
            // Re-initialize the page
            initCustomerManagement();
        })
        .catch(error => {
            console.error('Reset error:', error);
            // Fallback to normal form submission
            searchForm.submit();
        });
    }
}

// Global functions for onclick attributes
window.viewCustomer = async function(id) {
    console.log('viewCustomer called with ID:', id);
    try {
        const response = await fetch(`/admin/customers/${id}`, {
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            showNotification('Unable to load customer details', 'error');
            return;
        }
        
        const customerData = await response.json();
        fillCustomerModal(customerData);
        const modal = document.getElementById('customerViewModal');
        modal.classList.add('show');
    } catch (error) {
        console.error('Error loading customer details:', error);
        showNotification('Error loading customer details', 'error');
    }
};

window.deactivateCustomer = function(id) {
    console.log('=== DEACTIVATE CUSTOMER CALLED ===');
    console.log('Deactivate customer called with ID:', id);
    console.log('About to call showConfirmationModal');
    showConfirmationModal(
        'Are you sure you want to deactivate this customer?',
        () => {
            console.log('Confirmation callback executed');
            // Get CSRF token
            const csrfMeta = document.querySelector('meta[name="_csrf"]');
            console.log('CSRF Meta element:', csrfMeta);
            const csrfToken = csrfMeta?.getAttribute('content');
            console.log('CSRF Token:', csrfToken);
            console.log('Making request to:', `/admin/customers/${id}/deactivate`);
            
            fetch(`/admin/customers/${id}/deactivate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfToken || ''
                }
            })
            .then(response => {
                console.log('Deactivate response status:', response.status);
                if (response.ok) {
                    return response.text();
                } else {
                    return response.text().then(text => {
                        console.log('Deactivate error response:', text);
                        return Promise.reject(text);
                    });
                }
            })
            .then(data => {
                console.log('Deactivate success response:', data);
                showNotification(data, 'success');
                setTimeout(() => location.reload(), 1500);
            })
            .catch(error => {
                console.error('Deactivate error:', error);
                showNotification(error, 'error');
            });
        }
    );
};

window.activateCustomer = function(id) {
    console.log('=== ACTIVATE CUSTOMER CALLED ===');
    console.log('Activate customer called with ID:', id);
    console.log('About to call showConfirmationModal');
    showConfirmationModal(
        'Are you sure you want to activate this customer?',
        () => {
            console.log('Confirmation callback executed');
            // Get CSRF token
            const csrfMeta = document.querySelector('meta[name="_csrf"]');
            console.log('CSRF Meta element:', csrfMeta);
            const csrfToken = csrfMeta?.getAttribute('content');
            console.log('CSRF Token:', csrfToken);
            console.log('Making request to:', `/admin/customers/${id}/activate`);
            
            fetch(`/admin/customers/${id}/activate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfToken || ''
                }
            })
            .then(response => {
                console.log('Activate response status:', response.status);
                if (response.ok) {
                    return response.text();
                } else {
                    return response.text().then(text => {
                        console.log('Activate error response:', text);
                        return Promise.reject(text);
                    });
                }
            })
            .then(data => {
                console.log('Activate success response:', data);
                showNotification(data, 'success');
                setTimeout(() => location.reload(), 1500);
            })
            .catch(error => {
                console.error('Activate error:', error);
                showNotification(error, 'error');
            });
        }
    );
};

window.clearFilters = clearFilters;
window.closeCustomerModal = closeCustomerModal;
window.closeConfirmationModal = closeConfirmationModal;
