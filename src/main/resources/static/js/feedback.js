// Feedback JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeFeedback();
});

function initializeFeedback() {
    // Initialize form functionality
    initializeForm();
    
    // Initialize modals
    initializeModals();
    
    // Load feedback history
    loadFeedbackHistory();
    

}

// Initialize form functionality
function initializeForm() {
    const feedbackForm = document.getElementById('feedbackForm');
    const messageTextarea = document.getElementById('feedbackMessage');
    const charCount = document.getElementById('charCount');
    const submitBtn = document.getElementById('submitBtn');
    
    // Character counter
    messageTextarea.addEventListener('input', function() {
        const length = this.value.length;
        charCount.textContent = length;
        
        // Update character counter styling
        const charCounter = document.querySelector('.char-counter');
        charCounter.classList.remove('warning', 'danger');
        
        if (length > 1800) {
            charCounter.classList.add('danger');
        } else if (length > 1500) {
            charCounter.classList.add('warning');
        }
        
        // Enable/disable submit button
        submitBtn.disabled = length < 10 || length > 2000;
    });
    
    // Form submission
    feedbackForm.addEventListener('submit', function(e) {
        e.preventDefault();
        submitFeedback();
    });
    
    // Auto-resize textarea
    messageTextarea.addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = this.scrollHeight + 'px';
    });
}

// Initialize modals
function initializeModals() {
    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        const viewModal = document.getElementById('viewModal');
        if (event.target === viewModal) {
            closeViewModal();
        }
    });
}

// Submit feedback
function submitFeedback() {
    const message = document.getElementById('feedbackMessage').value.trim();
    const submitBtn = document.getElementById('submitBtn');
    
    if (message.length < 10) {
        showNotification('Message must be at least 10 characters long', 'error');
        return;
    }
    
    if (message.length > 2000) {
        showNotification('Message must be less than 2000 characters', 'error');
        return;
    }
    
    // Disable submit button
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';
    
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    fetch('/feedback/submit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ message: message })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification('Feedback submitted successfully!', 'success');
            clearForm();
            loadFeedbackHistory(); // Reload history
        } else {
            showNotification('Error: ' + data.error, 'error');
        }
    })
    .catch(error => {

        showNotification('Error submitting feedback. Please try again.', 'error');
    })
    .finally(() => {
        // Re-enable submit button
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit Feedback';
    });
}

// Clear form
function clearForm() {
    document.getElementById('feedbackForm').reset();
    document.getElementById('charCount').textContent = '0';
    document.querySelector('.char-counter').classList.remove('warning', 'danger');
    document.getElementById('submitBtn').disabled = true;
}

// Load feedback history
function loadFeedbackHistory() {
    const loadingState = document.getElementById('loadingState');
    const noFeedbackState = document.getElementById('noFeedbackState');
    const feedbackList = document.getElementById('feedbackList');
    
    // Show loading state
    loadingState.style.display = 'block';
    noFeedbackState.style.display = 'none';
    feedbackList.innerHTML = '';
    
    fetch('/feedback/history')
        .then(response => response.json())
        .then(data => {
            loadingState.style.display = 'none';
            
            if (data.success) {
                if (data.feedback.length === 0) {
                    noFeedbackState.style.display = 'block';
                } else {
                    displayFeedbackHistory(data.feedback);
                }
            } else {
                showNotification('Error loading feedback history: ' + data.error, 'error');
                noFeedbackState.style.display = 'block';
            }
        })
        .catch(error => {

            loadingState.style.display = 'none';
            showNotification('Error loading feedback history', 'error');
            noFeedbackState.style.display = 'block';
        });
}

// Display feedback history
function displayFeedbackHistory(feedbackList) {
    const container = document.getElementById('feedbackList');
    
    if (feedbackList.length === 0) {
        document.getElementById('noFeedbackState').style.display = 'block';
        return;
    }
    
    const feedbackHTML = feedbackList.map(feedback => {
        const submittedDate = new Date(feedback.submittedAt);
        const isRecent = feedback.isRecent;
        
        return `
            <div class="feedback-item ${isRecent ? 'recent' : ''}">
                <div class="feedback-header">
                    <div class="feedback-meta">
                        <div class="feedback-date">
                            <i class="fas fa-calendar-alt"></i>
                            ${formatDateTime(submittedDate)}
                        </div>
                        ${isRecent ? '<span class="recent-badge"><i class="fas fa-clock"></i> Recent</span>' : ''}
                    </div>
                </div>
                <div class="feedback-content">
                    ${escapeHtml(feedback.message)}
                </div>
                <div class="feedback-actions">
                    <button class="btn-sm btn-info" onclick="viewFeedback(${feedback.feedbackId})" title="View Details">
                        <i class="fas fa-eye"></i> View
                    </button>
                </div>
            </div>
        `;
    }).join('');
    
    container.innerHTML = feedbackHTML;
}

// View feedback details
function viewFeedback(feedbackId) {
    fetch(`/feedback/history`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const feedback = data.feedback.find(f => f.feedbackId === feedbackId);
                if (feedback) {
                    showFeedbackModal(feedback);
                } else {
                    showNotification('Feedback not found', 'error');
                }
            } else {
                showNotification('Error loading feedback details: ' + data.error, 'error');
            }
        })
        .catch(error => {

            showNotification('Error loading feedback details', 'error');
        });
}

// Show feedback modal
function showFeedbackModal(feedback) {
    const submittedDate = new Date(feedback.submittedAt);
    
    const viewContent = document.getElementById('viewContent');
    viewContent.innerHTML = `
        <div class="feedback-detail">
            <div class="meta-info">
                <div class="meta-item">
                    <span class="meta-label">Submitted:</span>
                    <span class="meta-value">${formatDateTime(submittedDate)}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Status:</span>
                    <span class="meta-value">${feedback.isRecent ? 'Recent' : 'Older'}</span>
                </div>
            </div>
            <div class="content">${escapeHtml(feedback.message)}</div>
        </div>
    `;
    
    document.getElementById('viewModal').style.display = 'block';
}

// Close view modal
function closeViewModal() {
    document.getElementById('viewModal').style.display = 'none';
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

// Format date and time
function formatDateTime(date) {
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}
