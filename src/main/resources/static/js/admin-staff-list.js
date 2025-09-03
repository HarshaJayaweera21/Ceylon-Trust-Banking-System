document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Staff List page loaded');
    
    // Initialize staff management functionality
    initStaffManagement();
    
    // Ensure global functions are available after a short delay
    setTimeout(() => {
        console.log('Global functions should be available now');
        console.log('viewStaff available:', typeof window.viewStaff);
        console.log('editStaff available:', typeof window.editStaff);
        console.log('deactivateStaff available:', typeof window.deactivateStaff);
        console.log('activateStaff available:', typeof window.activateStaff);
    }, 100);
});

function initStaffManagement() {
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
                initStaffManagement();
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
                initStaffManagement();
            })
            .catch(error => {
                console.error('Filter error:', error);
                // Fallback to normal form submission
                searchForm.submit();
            });
        });
    }
    
    // Handle role filter change
    const roleFilter = document.getElementById('roleFilter');
    if (roleFilter) {
        roleFilter.addEventListener('change', function() {
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
                initStaffManagement();
            })
            .catch(error => {
                console.error('Filter error:', error);
                // Fallback to normal form submission
                searchForm.submit();
            });
        });
    }
    
    // Handle modal close buttons
    const closeButtons = document.querySelectorAll('.close-btn');
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) {
                modal.classList.remove('show');
            }
        });
    });
    
    // Handle modal backdrop clicks
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                // Close the appropriate modal based on its ID
                if (this.id === 'staffModal') {
                    closeStaffModal();
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

// Global functions for onclick attributes
window.viewStaff = async function(id) {
    console.log('viewStaff called with ID:', id);
    try {
        const response = await fetch(`/admin/staff/${id}`, {
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            showNotification('Unable to load staff details', 'error');
            return;
        }
        
        const staffData = await response.json();
        fillStaffModal(staffData);
        const modal = document.getElementById('staffViewModal');
        modal.style.display = 'flex';
        modal.classList.add('show');
    } catch (error) {
        console.error('Error loading staff details:', error);
        showNotification('Error loading staff details', 'error');
    }
};

window.editStaff = async function(id) {
    console.log('editStaff called with ID:', id);
    try {
        const response = await fetch(`/admin/staff/${id}`, {
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            showNotification('Unable to load staff details for editing', 'error');
            return;
        }
        
        const staffData = await response.json();
        fillEditStaffModal(staffData);
        const modal = document.getElementById('staffEditModal');
        modal.style.display = 'flex';
        modal.classList.add('show');
    } catch (error) {
        console.error('Error loading staff details for editing:', error);
        showNotification('Error loading staff details for editing', 'error');
    }
};

window.deactivateStaff = function(id) {
    console.log('=== DEACTIVATE STAFF CALLED ===');
    console.log('Deactivate staff called with ID:', id);
    showConfirmationModal(
        'Are you sure you want to deactivate this staff member?',
        () => {
            console.log('Confirmation callback executed');
            const csrfMeta = document.querySelector('meta[name="_csrf"]');
            console.log('CSRF Meta element:', csrfMeta);
            const csrfToken = csrfMeta?.getAttribute('content');
            console.log('CSRF Token:', csrfToken);
            console.log('Making request to:', `/admin/staff/${id}/deactivate`);
            
            fetch(`/admin/staff/${id}/deactivate`, {
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

window.activateStaff = function(id) {
    console.log('=== ACTIVATE STAFF CALLED ===');
    console.log('Activate staff called with ID:', id);
    showConfirmationModal(
        'Are you sure you want to activate this staff member?',
        () => {
            console.log('Confirmation callback executed');
            const csrfMeta = document.querySelector('meta[name="_csrf"]');
            console.log('CSRF Meta element:', csrfMeta);
            const csrfToken = csrfMeta?.getAttribute('content');
            console.log('CSRF Token:', csrfToken);
            console.log('Making request to:', `/admin/staff/${id}/activate`);
            
            fetch(`/admin/staff/${id}/activate`, {
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

function fillStaffModal(staffData) {
    console.log('Filling staff modal with data:', staffData);
    
    const content = `
        <div class="modal-section">
            <h4>Personal Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Staff ID</span>
                    <span class="info-value">${staffData.userId || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Username</span>
                    <span class="info-value">${staffData.username || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Full Name</span>
                    <span class="info-value">${(staffData.firstName || '') + ' ' + (staffData.lastName || '')}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Email</span>
                    <span class="info-value">${staffData.email || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">NIC</span>
                    <span class="info-value">${staffData.nic || ''}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Date of Birth</span>
                    <span class="info-value">${staffData.dob ? new Date(staffData.dob).toLocaleDateString() : 'Not provided'}</span>
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>Role & Status</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Role</span>
                    <span class="info-value">${staffData.roleName || 'Not assigned'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Status</span>
                    <span class="info-value">${staffData.isActive ? '<span class="status-badge active">Active</span>' : '<span class="status-badge inactive">Inactive</span>'}</span>
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>Address Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Street Address</span>
                    <span class="info-value">${staffData.street || 'Not provided'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">City</span>
                    <span class="info-value">${staffData.city || 'Not provided'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Postal Code</span>
                    <span class="info-value">${staffData.postalCode || 'Not provided'}</span>
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>System Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Created At</span>
                    <span class="info-value">${staffData.createdAt ? new Date(staffData.createdAt).toLocaleString() : 'Not available'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Last Updated</span>
                    <span class="info-value">${staffData.updatedAt ? new Date(staffData.updatedAt).toLocaleString() : 'Not available'}</span>
                </div>
            </div>
        </div>
    `;
    
    // Update the modal content
    const staffDetailsContent = document.getElementById('staffDetailsContent');
    if (staffDetailsContent) {
        staffDetailsContent.innerHTML = content;
    }
}

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
    modal.style.display = 'flex';
    modal.classList.add('show');
    console.log('Modal should be visible now');
    
    confirmBtn.onclick = function() {
        console.log('Confirm button clicked');
        modal.style.display = 'none';
        modal.classList.remove('show');
        console.log('Executing confirmation callback immediately');
        confirmCallback();
    };
}

function closeConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
    }
}

function closeStaffModal() {
    const modal = document.getElementById('staffViewModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
    }
}

function fillEditStaffModal(staffData) {
    console.log('Filling edit staff modal with data:', staffData);
    
    // Populate form fields
    document.getElementById('editStaffId').value = staffData.userId || '';
    document.getElementById('editEmail').value = staffData.email || '';
    document.getElementById('editFirstName').value = staffData.firstName || '';
    document.getElementById('editLastName').value = staffData.lastName || '';
    document.getElementById('editDateOfBirth').value = staffData.dob || '';
    document.getElementById('editStreet').value = staffData.street || '';
    document.getElementById('editCity').value = staffData.city || '';
    document.getElementById('editPostalCode').value = staffData.postalCode || '';
    
    // Populate role field
    document.getElementById('editRoleId').value = staffData.roleId || '';
}

function closeEditStaffModal() {
    const modal = document.getElementById('staffEditModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
    }
}

function saveStaffChanges() {
    const staffId = document.getElementById('editStaffId').value;
    const form = document.getElementById('editStaffForm');
    const formData = new FormData(form);
    
    // Convert FormData to URLSearchParams for proper encoding
    const params = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        params.append(key, value);
    }
    
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    
    console.log('Saving staff changes for ID:', staffId);
    
    fetch(`/admin/staff/${staffId}/edit`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken || ''
        },
        body: params.toString()
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => Promise.reject(text));
        }
    })
    .then(data => {
        console.log('Staff update response:', data);
        showNotification('Staff member updated successfully', 'success');
        closeEditStaffModal();
        setTimeout(() => location.reload(), 1500);
    })
    .catch(error => {
        console.error('Error updating staff:', error);
        showNotification('Failed to update staff member: ' + error, 'error');
    });
}

// Reset filters function
function clearFilters() {
    const searchInput = document.getElementById('search');
    const roleFilter = document.getElementById('roleFilter');
    const hideInactiveCheckbox = document.getElementById('hideInactive');
    
    if (searchInput) searchInput.value = '';
    if (roleFilter) roleFilter.value = '';
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
            initStaffManagement();
        })
        .catch(error => {
            console.error('Reset error:', error);
            // Fallback to normal form submission
            searchForm.submit();
        });
    }
}

// Notification system
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

// Add Staff Modal Functions
function openAddStaffModal() {
    console.log('Opening add staff modal');
    const modal = document.getElementById('addStaffModal');
    if (modal) {
        modal.style.display = 'flex';
        modal.classList.add('show');
        // Clear form
        document.getElementById('addStaffForm').reset();
        // Clear any errors
        clearAllErrors();
        // Add phone formatting
        const phoneInput = document.getElementById('addPhone');
        if (phoneInput) {
            phoneInput.addEventListener('input', formatPhone);
        }
    }
}

function closeAddStaffModal() {
    console.log('Closing add staff modal');
    const modal = document.getElementById('addStaffModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('show');
        // Clear form
        document.getElementById('addStaffForm').reset();
    }
}

// Validation functions (same as signup.js)
function isEmail(v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
}

function isNIC(v) {
    return /^[0-9]{9}[Vv]$/.test(v) || /^[0-9]{12}$/.test(v);
}

function formatPhone(e) {
    let v = e.target.value.replace(/\D/g, '').slice(0, 9);
    e.target.value = v; // Remove formatting, just allow 9 digits
}

function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.add('error-state');
        // Create or update error message
        let errorEl = document.getElementById('err-' + fieldId);
        if (!errorEl) {
            errorEl = document.createElement('div');
            errorEl.id = 'err-' + fieldId;
            errorEl.className = 'field-error';
            errorEl.style.cssText = 'color: #dc3545; font-size: 12px; margin-top: 4px;';
            field.parentNode.appendChild(errorEl);
        }
        errorEl.textContent = message;
    }
}

function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('error-state');
        const errorEl = document.getElementById('err-' + fieldId);
        if (errorEl) {
            errorEl.textContent = '';
        }
    }
}

function clearAllErrors() {
    const fields = ['addFirstName', 'addLastName', 'addEmail', 'addPhone', 'addNic', 'addUsername', 'addPassword', 'addConfirmPassword', 'addStreet', 'addCity', 'addPostalCode', 'addDateOfBirth'];
    fields.forEach(field => clearFieldError(field));
}

function submitAddStaff() {
    console.log('Submitting add staff form');
    
    // Clear previous errors
    clearAllErrors();
    
    const form = document.getElementById('addStaffForm');
    const formData = new FormData(form);
    
    // Get form values
    const firstName = document.getElementById('addFirstName').value.trim();
    const lastName = document.getElementById('addLastName').value.trim();
    const email = document.getElementById('addEmail').value.trim();
    const phone = document.getElementById('addPhone').value.replace(/\s/g, '');
    const nic = document.getElementById('addNic').value.trim();
    const username = document.getElementById('addUsername').value.trim();
    const password = document.getElementById('addPassword').value;
    const confirmPassword = document.getElementById('addConfirmPassword').value;
    const street = document.getElementById('addStreet').value.trim();
    const city = document.getElementById('addCity').value.trim();
    const postalCode = document.getElementById('addPostalCode').value.trim();
    const dateOfBirth = document.getElementById('addDateOfBirth').value;
    
    let valid = true;
    
    // Validation (same as signup.js)
    if (!firstName) { 
        showFieldError('addFirstName', 'First name is required'); 
        valid = false; 
    }
    
    if (!lastName) { 
        showFieldError('addLastName', 'Last name is required'); 
        valid = false; 
    }
    
    if (!email) { 
        showFieldError('addEmail', 'Email is required'); 
        valid = false; 
    } else if (!isEmail(email)) { 
        showFieldError('addEmail', 'Please enter a valid email address'); 
        valid = false; 
    }
    
    if (!phone) { 
        showFieldError('addPhone', 'Phone number is required'); 
        valid = false; 
    } else if (!/^\d{9}$/.test(phone)) { 
        showFieldError('addPhone', 'Phone number must be exactly 9 digits'); 
        valid = false; 
    }
    
    if (!nic) { 
        showFieldError('addNic', 'NIC is required'); 
        valid = false; 
    } else if (!isNIC(nic)) { 
        showFieldError('addNic', 'Please enter a valid NIC (9 digits + V or 12 digits)'); 
        valid = false; 
    }
    
    if (!username) { 
        showFieldError('addUsername', 'Username is required'); 
        valid = false; 
    }
    
    const passRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#\$%\^&\*()_\-+=]).{8,20}$/;
    if (!password) { 
        showFieldError('addPassword', 'Password is required'); 
        valid = false; 
    } else if (!passRegex.test(password)) { 
        showFieldError('addPassword', 'Password must be 8-20 characters with uppercase, lowercase, digit, and special character'); 
        valid = false; 
    }
    
    if (!confirmPassword) { 
        showFieldError('addConfirmPassword', 'Please confirm your password'); 
        valid = false; 
    } else if (password !== confirmPassword) { 
        showFieldError('addConfirmPassword', 'Passwords do not match'); 
        valid = false; 
    }
    
    if (!street) { 
        showFieldError('addStreet', 'Street address is required'); 
        valid = false; 
    }
    
    if (!city) { 
        showFieldError('addCity', 'City is required'); 
        valid = false; 
    }
    
    if (!postalCode) { 
        showFieldError('addPostalCode', 'Postal code is required'); 
        valid = false; 
    }
    
    if (!dateOfBirth) { 
        showFieldError('addDateOfBirth', 'Date of birth is required'); 
        valid = false; 
    }
    
    if (!valid) {
        showNotification('Please fix the validation errors', 'error');
        return;
    }
    
    // Convert FormData to URLSearchParams for proper encoding
    const params = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        params.append(key, value);
    }
    
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    
    console.log('Adding new staff member');
    
    fetch('/admin/staff/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken || ''
        },
        body: params.toString()
    })
    .then(response => {
        console.log('Response status:', response.status);
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => {
                console.log('Error response:', text);
                throw new Error(text);
            });
        }
    })
    .then(data => {
        console.log('Staff registration response:', data);
        showNotification('Staff member added successfully', 'success');
        closeAddStaffModal();
        setTimeout(() => location.reload(), 1500);
    })
    .catch(error => {
        console.error('Error adding staff:', error);
        showNotification('Failed to add staff member: ' + error.message, 'error');
    });
}

// Make functions globally available
window.clearFilters = clearFilters;
window.closeStaffModal = closeStaffModal;
window.closeConfirmationModal = closeConfirmationModal;
window.closeEditStaffModal = closeEditStaffModal;
window.saveStaffChanges = saveStaffChanges;
window.openAddStaffModal = openAddStaffModal;
window.closeAddStaffModal = closeAddStaffModal;
window.submitAddStaff = submitAddStaff;
