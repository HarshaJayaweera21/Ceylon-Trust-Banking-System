// Staff Registration JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('Staff Registration page loaded');
    
    // Initialize form functionality
    initStaffRegistration();
});

function initStaffRegistration() {
    // Password toggle functionality
    setupPasswordToggles();
    
    // Form validation
    setupFormValidation();
    
    // Form submission handling
    setupFormSubmission();
}

function setupPasswordToggles() {
    // Password toggle for main password field
    const passwordToggle = document.querySelector('.password-input .password-toggle');
    if (passwordToggle) {
        passwordToggle.addEventListener('click', togglePassword);
    }
    
    // Password toggle for confirm password field
    const confirmPasswordToggle = document.querySelector('#confirmPassword').parentElement.querySelector('.password-toggle');
    if (confirmPasswordToggle) {
        confirmPasswordToggle.addEventListener('click', toggleConfirmPassword);
    }
}

function togglePassword() {
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.querySelector('.password-input .password-toggle');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.remove('fa-eye');
        toggleIcon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.remove('fa-eye-slash');
        toggleIcon.classList.add('fa-eye');
    }
}

function toggleConfirmPassword() {
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const toggleIcon = document.querySelector('#confirmPassword').parentElement.querySelector('.password-toggle');
    
    if (confirmPasswordInput.type === 'password') {
        confirmPasswordInput.type = 'text';
        toggleIcon.classList.remove('fa-eye');
        toggleIcon.classList.add('fa-eye-slash');
    } else {
        confirmPasswordInput.type = 'password';
        toggleIcon.classList.remove('fa-eye-slash');
        toggleIcon.classList.add('fa-eye');
    }
}

function setupFormValidation() {
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirmPassword');
    
    if (passwordField && confirmPasswordField) {
        // Real-time password matching validation
        function validatePasswordMatch() {
            if (confirmPasswordField.value && passwordField.value !== confirmPasswordField.value) {
                confirmPasswordField.setCustomValidity('Passwords do not match');
                confirmPasswordField.classList.add('error');
            } else {
                confirmPasswordField.setCustomValidity('');
                confirmPasswordField.classList.remove('error');
            }
        }
        
        passwordField.addEventListener('input', validatePasswordMatch);
        confirmPasswordField.addEventListener('input', validatePasswordMatch);
    }
    
    // Phone number validation
    const phoneField = document.getElementById('phone');
    if (phoneField) {
        phoneField.addEventListener('input', function() {
            const value = this.value.replace(/\D/g, ''); // Remove non-digits
            if (value.length > 9) {
                this.value = value.substring(0, 9);
            }
        });
    }
    
    // NIC validation
    const nicField = document.getElementById('nic');
    if (nicField) {
        nicField.addEventListener('input', function() {
            const value = this.value.replace(/\D/g, ''); // Remove non-digits
            if (value.length > 12) {
                this.value = value.substring(0, 12);
            }
        });
    }
    
    // Postal code validation
    const postalCodeField = document.getElementById('postalCode');
    if (postalCodeField) {
        postalCodeField.addEventListener('input', function() {
            const value = this.value.replace(/\D/g, ''); // Remove non-digits
            if (value.length > 5) {
                this.value = value.substring(0, 5);
            }
        });
    }
}

function setupFormSubmission() {
    const form = document.querySelector('.registration-form');
    const submitBtn = document.querySelector('.submit-btn');
    
    if (form && submitBtn) {
        form.addEventListener('submit', function(e) {
            // Validate form before submission
            if (!validateForm()) {
                e.preventDefault();
                return false;
            }
            
            // Show loading state
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Registering...';
            
            // Re-enable button after 5 seconds as fallback
            setTimeout(() => {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-check"></i> Register Staff';
            }, 5000);
        });
    }
}

function validateForm() {
    let isValid = true;
    
    // Check required fields
    const requiredFields = [
        'firstName', 'lastName', 'email', 'phone', 'nic', 'roleId',
        'username', 'password', 'confirmPassword', 'street', 'city', 'postalCode', 'dob'
    ];
    
    requiredFields.forEach(fieldName => {
        const field = document.getElementById(fieldName);
        if (field && !field.value.trim()) {
            field.classList.add('error');
            isValid = false;
        } else if (field) {
            field.classList.remove('error');
        }
    });
    
    // Validate email format
    const emailField = document.getElementById('email');
    if (emailField && emailField.value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailField.value)) {
            emailField.classList.add('error');
            isValid = false;
        }
    }
    
    // Validate phone number
    const phoneField = document.getElementById('phone');
    if (phoneField && phoneField.value) {
        if (phoneField.value.length !== 9) {
            phoneField.classList.add('error');
            isValid = false;
        }
    }
    
    // Validate NIC length
    const nicField = document.getElementById('nic');
    if (nicField && nicField.value) {
        if (nicField.value.length !== 12) {
            nicField.classList.add('error');
            isValid = false;
        }
    }
    
    // Validate password match
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirmPassword');
    if (passwordField && confirmPasswordField) {
        if (passwordField.value !== confirmPasswordField.value) {
            confirmPasswordField.classList.add('error');
            isValid = false;
        }
    }
    
    // Validate password strength
    if (passwordField && passwordField.value.length < 6) {
        passwordField.classList.add('error');
        isValid = false;
    }
    
    if (!isValid) {
        showNotification('Please fill in all required fields correctly', 'error');
    }
    
    return isValid;
}

function showNotification(message, type) {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create new notification
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-triangle'}"></i>
        <span>${message}</span>
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Show notification
    setTimeout(() => {
        notification.style.display = 'block';
    }, 100);
    
    // Hide notification after 4 seconds
    setTimeout(() => {
        notification.style.display = 'none';
    }, 4000);
}

// Clear form functionality
function clearForm() {
    const form = document.querySelector('.registration-form');
    if (form) {
        form.reset();
        
        // Remove error classes
        const errorFields = form.querySelectorAll('.error');
        errorFields.forEach(field => field.classList.remove('error'));
        
        // Reset password toggle icons
        const passwordToggles = form.querySelectorAll('.password-toggle');
        passwordToggles.forEach(toggle => {
            toggle.classList.remove('fa-eye-slash');
            toggle.classList.add('fa-eye');
        });
        
        showNotification('Form cleared successfully', 'success');
    }
}

// Make clearForm globally available
window.clearForm = clearForm;
