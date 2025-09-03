(() => {
    'use strict';
    
    // Ensure header functions are available globally
    window.reinitializeHeaderFunctionality = function() {
        try {
            // Re-initialize mobile menu if the function exists
            if (typeof initializeMobileMenu === 'function') {
                initializeMobileMenu();
            }
            
            // Re-initialize notifications if the function exists
            if (typeof initializeNotifications === 'function') {
                initializeNotifications();
            }
            
            // Re-bind hamburger menu click event
            const hamburgerMenu = document.getElementById('hamburgerMenu');
            if (hamburgerMenu) {
                // Remove existing event listeners
                const newHamburger = hamburgerMenu.cloneNode(true);
                hamburgerMenu.parentNode.replaceChild(newHamburger, hamburgerMenu);
                
                // Add new event listener
                newHamburger.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    if (typeof toggleMobileMenu === 'function') {
                        toggleMobileMenu();
                    }
                });
            }
            
            // Re-bind notification bell click event
            const notificationBell = document.getElementById('notificationBell');
            if (notificationBell) {
                // Remove existing event listeners
                const newBell = notificationBell.cloneNode(true);
                notificationBell.parentNode.replaceChild(newBell, notificationBell);
                
                // Add new event listener
                newBell.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    if (typeof toggleNotificationPopup === 'function') {
                        toggleNotificationPopup();
                    }
                });
            }
            
            console.log('Header functionality re-initialized');
        } catch (error) {
            console.error('Error re-initializing header functionality:', error);
        }
    };

    // DOM Elements
    const form = document.getElementById('supportTicketForm');
    const notification = document.getElementById('notification');
    const subjectInput = document.getElementById('subject');
    const messageInput = document.getElementById('message');

    // CSRF Token
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // Form validation
    function validateForm() {
        const issues = [];
        
        // Subject validation
        const subject = subjectInput.value.trim();
        if (!subject) {
            issues.push('Subject is required.');
        } else if (subject.length < 5) {
            issues.push('Subject must be at least 5 characters long.');
        } else if (subject.length > 100) {
            issues.push('Subject must be less than 100 characters.');
        }

        // Message validation
        const message = messageInput.value.trim();
        if (!message) {
            issues.push('Message is required.');
        } else if (message.length < 10) {
            issues.push('Message must be at least 10 characters long.');
        } else if (message.length > 1000) {
            issues.push('Message must be less than 1000 characters.');
        }

        return issues;
    }

    // Show field validity
    function showFieldValidity(field, isValid) {
        if (isValid) {
            field.style.borderColor = '#28a745';
        } else {
            field.style.borderColor = '#dc3545';
        }
    }

    // Real-time validation
    function setupRealTimeValidation() {
        subjectInput.addEventListener('input', () => {
            const subject = subjectInput.value.trim();
            const isValid = subject.length >= 5 && subject.length <= 100;
            showFieldValidity(subjectInput, isValid);
        });

        messageInput.addEventListener('input', () => {
            const message = messageInput.value.trim();
            const isValid = message.length >= 10 && message.length <= 1000;
            showFieldValidity(messageInput, isValid);
        });
    }

    // Show notification
    function showNotification(message, type = 'success') {
        // Clear any existing notification and timer
        if (notification.autoHideTimer) {
            clearTimeout(notification.autoHideTimer);
        }
        notification.style.display = 'none';
        
        // Add icon based on type
        let icon = '';
        if (type === 'success') {
            icon = '<i class="fa-solid fa-check-circle"></i>';
        } else if (type === 'error') {
            icon = '<i class="fa-solid fa-exclamation-circle"></i>';
        }
        
        // Set notification content with icon and close button
        notification.innerHTML = `
            <div style="display: flex; align-items: flex-start; gap: 12px; flex: 1;">
                ${icon}
                <div style="flex: 1;">${message}</div>
            </div>
            <button onclick="hideNotification()" style="background: none; border: none; color: inherit; cursor: pointer; font-size: 18px; padding: 0; margin-left: 10px; opacity: 0.7; transition: opacity 0.2s; flex-shrink: 0;">
                <i class="fa-solid fa-times"></i>
            </button>
        `;
        notification.className = `notification ${type}`;
        
        // Force a reflow to ensure the element is ready
        notification.offsetHeight;
        
        // Show the notification
        notification.style.display = 'flex';
        
        // Add slide-in animation after a small delay
        setTimeout(() => {
            notification.style.animation = 'slideInRight 0.6s ease-out';
        }, 50);
        
        // Auto hide after 25 seconds with slide-out animation
        const autoHideTimer = setTimeout(() => {
            hideNotification();
        }, 25000);
        
        // Store timer reference for manual dismissal
        notification.autoHideTimer = autoHideTimer;
        
        // Pause auto-hide on hover
        notification.addEventListener('mouseenter', () => {
            if (notification.autoHideTimer) {
                clearTimeout(notification.autoHideTimer);
            }
        });
        
        // Resume auto-hide when mouse leaves
        notification.addEventListener('mouseleave', () => {
            if (!notification.autoHideTimer) {
                notification.autoHideTimer = setTimeout(() => {
                    hideNotification();
                }, 8000); // Give 8 more seconds after hover
            }
        });
    }
    
    // Hide notification function
    window.hideNotification = function() {
        if (notification.autoHideTimer) {
            clearTimeout(notification.autoHideTimer);
            notification.autoHideTimer = null;
        }
        
        // Add slide-out animation
        notification.style.animation = 'slideOutRight 0.5s ease-in';
        
        // Hide after animation completes
        setTimeout(() => {
            notification.style.display = 'none';
            notification.style.animation = 'slideInRight 0.6s ease-out';
        }, 500);
    };

    // Reset form
    window.resetForm = function() {
        form.reset();
        subjectInput.style.borderColor = '#e9ecef';
        messageInput.style.borderColor = '#e9ecef';
        showNotification('Form cleared successfully.', 'success');
    };

    // Form submission
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Validate form
        const validationIssues = validateForm();
        if (validationIssues.length > 0) {
            showNotification(validationIssues.join(' '), 'error');
            return;
        }

        // Show loading state
        const submitBtn = form.querySelector('.submit-btn');
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Submitting...';
        submitBtn.disabled = true;

        try {
            // Prepare form data
            const formData = new FormData(form);
            
            // Add CSRF token
            formData.append('_token', csrfToken);

            // Submit form
            const response = await fetch(form.action, {
                method: 'POST',
                body: formData,
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (response.ok) {
                const result = await response.text();
                
                // Check if it's a redirect or success response
                if (response.redirected || result.includes('success')) {
                    // Show success notification with countdown
                    let countdown = 3;
                    let refreshTimer = null;
                    
                    const updateNotification = () => {
                        const notificationText = document.querySelector('#notification span');
                        if (notificationText) {
                            notificationText.innerHTML = `🎉 Support ticket submitted successfully! Our team will review your request and get back to you within 24 hours. <br><small style="opacity: 0.8; margin-top: 8px; display: block;">Page will refresh in ${countdown} second${countdown !== 1 ? 's' : ''}... <a href="#" onclick="cancelRefresh()" style="color: inherit; text-decoration: underline; font-weight: bold;">Cancel</a></small>`;
                        }
                    };
                    
                    // Show initial notification
                    showNotification('🎉 Support ticket submitted successfully! Our team will review your request and get back to you within 24 hours. <br><small style="opacity: 0.8; margin-top: 8px; display: block;">Page will refresh in 3 seconds... <a href="#" onclick="cancelRefresh()" style="color: inherit; text-decoration: underline; font-weight: bold;">Cancel</a></small>', 'success');
                    
                    // Reset form
                    form.reset();
                    subjectInput.style.borderColor = '#e9ecef';
                    messageInput.style.borderColor = '#e9ecef';
                    
                    // Scroll to top to show the notification
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                    
                    // Start countdown timer
                    refreshTimer = setInterval(() => {
                        countdown--;
                        if (countdown > 0) {
                            updateNotification();
                        } else {
                            clearInterval(refreshTimer);
                            // Refresh the page to restore all functionality
                            window.location.reload();
                        }
                    }, 1000);
                    
                    // Make cancelRefresh function available globally
                    window.cancelRefresh = function() {
                        clearInterval(refreshTimer);
                        const notificationText = document.querySelector('#notification span');
                        if (notificationText) {
                            notificationText.innerHTML = `🎉 Support ticket submitted successfully! Our team will review your request and get back to you within 24 hours.`;
                        }
                        // Hide notification after 3 seconds
                        setTimeout(() => {
                            hideNotification();
                        }, 3000);
                    };
                } else {
                    throw new Error('Unexpected response format');
                }
            } else {
                const errorText = await response.text();
                throw new Error(`Server error: ${response.status}`);
            }
        } catch (error) {
            console.error('Form submission error:', error);
            showNotification('Failed to submit support ticket. Please try again.', 'error');
        } finally {
            // Reset button state
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    });

    // Character counter for message
    function setupCharacterCounter() {
        const messageLabel = document.querySelector('label[for="message"]');
        const counter = document.createElement('small');
        counter.className = 'char-counter';
        counter.style.float = 'right';
        counter.style.color = '#6c757d';
        messageLabel.appendChild(counter);

        messageInput.addEventListener('input', () => {
            const length = messageInput.value.length;
            counter.textContent = `${length}/1000`;
            
            if (length > 800) {
                counter.style.color = '#dc3545';
            } else if (length > 600) {
                counter.style.color = '#ffc107';
            } else {
                counter.style.color = '#6c757d';
            }
        });
    }


    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', () => {
        setupRealTimeValidation();
        setupCharacterCounter();
        
        // Check for URL parameters for success/error messages
        const urlParams = new URLSearchParams(window.location.search);
        const success = urlParams.get('success');
        const error = urlParams.get('error');
        
        if (success === 'true') {
            showNotification('Support ticket submitted successfully! We will get back to you soon.', 'success');
        } else if (error) {
            showNotification(decodeURIComponent(error), 'error');
        }
    });

})();
