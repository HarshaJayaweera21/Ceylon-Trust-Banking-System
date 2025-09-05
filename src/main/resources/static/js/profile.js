document.addEventListener('DOMContentLoaded', () => {
    console.log('Profile management page loaded');
    
    // Ensure notification dropdown is closed on page load
    const notificationDropdown = document.getElementById('notificationDropdown');
    if (notificationDropdown) {
        notificationDropdown.classList.remove('show');
        console.log('Notification dropdown closed on page load');
    }
    
    // Initialize profile management functionality
    initProfileManagement();
});

function initProfileManagement() {
    // Profile Edit Functionality
    const editProfileBtn = document.getElementById('editProfileBtn');
    const cancelProfileBtn = document.getElementById('cancelProfileBtn');
    const saveProfileBtn = document.getElementById('saveProfileBtn');
    const profileForm = document.getElementById('profileForm');
    const profileActions = document.getElementById('profileActions');
    const profileSection = document.querySelector('.profile-section');
    
    // Store original values for cancel functionality
    let originalValues = {};
    let isEditMode = false;
    
    // Profile Edit Handlers
    if (editProfileBtn && profileForm) {
        editProfileBtn.addEventListener('click', () => {
            console.log('Edit profile clicked');
            enableEditMode();
        });
    }
    
    if (cancelProfileBtn && profileForm) {
        cancelProfileBtn.addEventListener('click', () => {
            console.log('Cancel profile edit clicked');
            cancelEditMode();
        });
    }
    
    if (saveProfileBtn && profileForm) {
        saveProfileBtn.addEventListener('click', (e) => {
            console.log('Save profile clicked');
            // Let the form submit naturally
        });
    }
    
    // Add click outside functionality to cancel edit mode
    if (profileSection) {
        profileSection.addEventListener('click', (e) => {
            // Only cancel if clicking outside the form container and in edit mode
            if (isEditMode && !profileForm.contains(e.target) && !editProfileBtn.contains(e.target)) {
                console.log('Clicked outside form - cancelling edit mode');
                cancelEditMode();
            }
        });
    }
    
    // Delete Profile Functionality
    const deleteProfileBtn = document.getElementById('deleteProfileBtn');
    const passwordModal = document.getElementById('passwordModal');
    const closeModal = document.getElementById('closeModal');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordError = document.getElementById('passwordError');
    
    if (deleteProfileBtn) {
        deleteProfileBtn.addEventListener('click', () => {
            console.log('Delete profile clicked');
            showPasswordModal();
        });
    }
    
    if (closeModal) {
        closeModal.addEventListener('click', () => {
            hidePasswordModal();
        });
    }
    
    if (cancelDeleteBtn) {
        cancelDeleteBtn.addEventListener('click', () => {
            hidePasswordModal();
        });
    }
    
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', () => {
            console.log('Confirm delete clicked');
            confirmAccountDeletion();
        });
    }
    
    // Close modal when clicking outside
    if (passwordModal) {
        passwordModal.addEventListener('click', (e) => {
            if (e.target === passwordModal) {
                hidePasswordModal();
            }
        });
    }
    
    function enableEditMode() {
        const inputs = profileForm.querySelectorAll('input');
        
        // Store original values
        originalValues = {};
        inputs.forEach(input => {
            originalValues[input.name] = input.value;
        });
        
        // Enable inputs (except NIC field)
        inputs.forEach(input => {
            if (input.id !== 'nic') {
                input.removeAttribute('readonly');
                input.style.backgroundColor = 'white';
                input.style.color = '#333';
                input.style.borderColor = '#0066cc';
            }
        });
        
        // Show action buttons
        profileActions.style.display = 'flex';
        
        // Hide edit button
        editProfileBtn.style.display = 'none';
        
        isEditMode = true;
        console.log('Edit mode enabled (NIC field remains readonly)');
    }
    
    function cancelEditMode() {
        const inputs = profileForm.querySelectorAll('input');
        
        // Restore original values
        inputs.forEach(input => {
            input.value = originalValues[input.name] || '';
            input.setAttribute('readonly', 'readonly');
            
            if (input.id === 'nic') {
                // Keep NIC field with special styling
                input.style.backgroundColor = '#f8f9fa';
                input.style.color = '#6c757d';
                input.style.borderColor = '#e9ecef';
                input.style.cursor = 'not-allowed';
            } else {
                // Regular fields
                input.style.backgroundColor = '#f8f9fa';
                input.style.color = '#6c757d';
                input.style.borderColor = '#e9ecef';
            }
        });
        
        // Hide action buttons
        profileActions.style.display = 'none';
        
        // Show edit button
        editProfileBtn.style.display = 'flex';
        
        isEditMode = false;
        console.log('Edit mode cancelled');
    }
    
    // Form submission is handled by Spring Boot controller
    
    function showLoading() {
        if (profileForm) {
            profileForm.classList.add('loading');
        }
    }
    
    function hideLoading() {
        if (profileForm) {
            profileForm.classList.remove('loading');
        }
    }
    
    function showNotification(message, type) {
        const notification = document.getElementById('notification');
        if (notification) {
            notification.textContent = message;
            notification.className = `notification ${type}`;
            notification.style.display = 'block';
            
            // Trigger animation
            setTimeout(() => {
                notification.classList.add('show');
            }, 10);
            
            // Hide notification after 4 seconds
            setTimeout(() => {
                notification.classList.remove('show');
                setTimeout(() => {
                    notification.style.display = 'none';
                }, 300); // Wait for animation to complete
            }, 4000);
        }
    }
    
    function showPasswordModal() {
        if (passwordModal) {
            passwordModal.style.display = 'block';
            confirmPasswordInput.value = '';
            passwordError.style.display = 'none';
            confirmPasswordInput.focus();
        }
    }
    
    function hidePasswordModal() {
        if (passwordModal) {
            passwordModal.style.display = 'none';
            confirmPasswordInput.value = '';
            passwordError.style.display = 'none';
        }
    }
    
    function confirmAccountDeletion() {
        const password = confirmPasswordInput.value;
        
        if (!password) {
            showPasswordError('Please enter your password');
            return;
        }
        
        console.log('Submitting delete request with password...');
        
        // Show loading state
        confirmDeleteBtn.disabled = true;
        confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deactivating...';
        
        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        
        // Submit delete request
        fetch('/profile/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: `password=${encodeURIComponent(password)}`
        })
        .then(response => {
            if (response.ok) {
                // If response is ok, it means the account was deactivated successfully
                console.log('Account deactivated successfully');
                // Redirect to login page with success message
                window.location.href = '/login?deactivated=true';
            } else {
                throw new Error('Password verification failed');
            }
        })
        .catch(error => {
            console.error('Delete error:', error);
            showPasswordError('Incorrect password. Please try again.');
            confirmDeleteBtn.disabled = false;
            confirmDeleteBtn.innerHTML = '<i class="fas fa-user-slash"></i> Deactivate Account';
        });
    }
    
    function showPasswordError(message) {
        if (passwordError) {
            passwordError.querySelector('span').textContent = message;
            passwordError.style.display = 'flex';
        }
    }
}
