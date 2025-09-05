// Login Page JavaScript
document.addEventListener('DOMContentLoaded', () => {
    // Read CSRF token and header
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // Password toggle functionality
    function setupPasswordToggle(toggleId, inputId) {
        const toggle = document.getElementById(toggleId);
        const input = document.getElementById(inputId);
        
        if (toggle && input) {
            let isPressed = false;
            
            toggle.addEventListener('mousedown', () => {
                isPressed = true;
                input.type = 'text';
                toggle.innerHTML = '<i class="fas fa-eye-slash"></i>';
            });
            
            toggle.addEventListener('mouseup', () => {
                if (isPressed) {
                    isPressed = false;
                    input.type = 'password';
                    toggle.innerHTML = '<i class="fas fa-eye"></i>';
                }
            });
            
            toggle.addEventListener('mouseleave', () => {
                if (isPressed) {
                    isPressed = false;
                    input.type = 'password';
                    toggle.innerHTML = '<i class="fas fa-eye"></i>';
                }
            });
            
            // Prevent context menu on right click
            toggle.addEventListener('contextmenu', (e) => {
                e.preventDefault();
            });
        }
    }

    // Form validation
    function showError(inputId, message) {
        const input = document.getElementById(inputId);
        const errorElement = document.getElementById(`err-${inputId}`);
        
        if (input) {
            input.classList.add('error-state');
        }
        
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
    }

    function clearErrors() {
        const errorElements = document.querySelectorAll('.error');
        const errorInputs = document.querySelectorAll('.error-state');
        
        errorElements.forEach(el => {
            el.textContent = '';
            el.classList.remove('show');
        });
        
        errorInputs.forEach(input => {
            input.classList.remove('error-state');
        });
    }

    // Unified form validation
    function validateLoginForm(formData) {
        let isValid = true;
        clearErrors();

        const { username, password } = formData;

        if (!username || username.trim() === '') {
            showError('username', 'Username is required');
            isValid = false;
        }

        if (!password || password.trim() === '') {
            showError('password', 'Password is required');
            isValid = false;
        } else if (!validatePasswordStrength(password)) {
            showError('password', 'Password must be 8-20 characters with uppercase, lowercase, digit, and special character');
            isValid = false;
        }

        return isValid;
    }

    // Password strength validation
    function validatePasswordStrength(password) {
        // Password must be 8-20 characters with uppercase, lowercase, digit, and special character
        const minLength = 8;
        const maxLength = 20;
        
        if (password.length < minLength || password.length > maxLength) {
            return false;
        }
        
        // Check for uppercase letter
        if (!/[A-Z]/.test(password)) {
            return false;
        }
        
        // Check for lowercase letter
        if (!/[a-z]/.test(password)) {
            return false;
        }
        
        // Check for digit
        if (!/\d/.test(password)) {
            return false;
        }
        
        // Check for special character
        if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/.test(password)) {
            return false;
        }
        
        return true;
    }

    // Form validation (client-side only, forms will submit to Spring Security)
    function setupFormValidation() {
        // Unified form validation on input
        const username = document.getElementById('username');
        const password = document.getElementById('password');
        
        if (username) {
            username.addEventListener('blur', () => {
                if (!username.value.trim()) {
                    showError('username', 'Username is required');
                } else {
                    clearError('username');
                }
            });
        }
        
        if (password) {
            password.addEventListener('blur', () => {
                if (!password.value.trim()) {
                    showError('password', 'Password is required');
                } else if (!validatePasswordStrength(password.value)) {
                    showError('password', 'Password must be 8-20 characters with uppercase, lowercase, digit, and special character');
                } else {
                    clearError('password');
                }
            });
            
            // Real-time validation as user types
            password.addEventListener('input', () => {
                if (password.value.trim() && !validatePasswordStrength(password.value)) {
                    showError('password', 'Password must be 8-20 characters with uppercase, lowercase, digit, and special character');
                } else if (password.value.trim()) {
                    clearError('password');
                }
            });
        }
    }

    function clearError(inputId) {
        const input = document.getElementById(inputId);
        const errorElement = document.getElementById(`err-${inputId}`);
        
        if (input) {
            input.classList.remove('error-state');
        }
        
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }
    }

    // Form submission validation
    function setupFormSubmission() {
        const form = document.getElementById('loginForm');
        
        if (form) {
            form.addEventListener('submit', (e) => {
                const formData = {
                    username: document.getElementById('username').value,
                    password: document.getElementById('password').value
                };
                
                if (!validateLoginForm(formData)) {
                    e.preventDefault();
                    return false;
                }
            });
        }
    }

    // Initialize all functionality
    setupPasswordToggle('togglePassword', 'password');
    setupFormValidation();
    setupFormSubmission();
});