/**
 * Notification System JavaScript
 * Handles notification bell interactions, dropdown display, and AJAX calls
 */

class NotificationSystem {
    constructor() {
        this.isDropdownOpen = false;
        this.notifications = [];
        this.unreadCount = 0;
        this.init();
    }

    init() {
        this.createNotificationElements();
        this.bindEvents();
        this.loadNotifications();
        this.startPolling();
    }

    createNotificationElements() {
        // Notification elements are now hardcoded in templates
        // This method is kept for compatibility but does nothing
        console.log('Notification elements are pre-rendered in templates');
    }

    bindEvents() {
        console.log('Binding notification events...');
        const bell = document.getElementById('notificationBell');
        const dropdown = document.getElementById('notificationDropdown');
        const markAllReadBtn = document.getElementById('markAllReadBtn');

        console.log('Found elements:', {
            bell: !!bell,
            dropdown: !!dropdown,
            markAllReadBtn: !!markAllReadBtn
        });

        if (bell) {
            bell.addEventListener('click', (e) => {
                console.log('Notification bell clicked');
                e.stopPropagation();
                this.toggleDropdown();
            });
        } else {
            console.error('Notification bell element not found!');
        }

        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', (e) => {
                console.log('Mark All as Read button clicked');
                e.stopPropagation();
                this.markAllAsRead();
            });
        } else {
            console.error('Mark All as Read button not found!');
        }

        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (dropdown && bell && !dropdown.contains(e.target) && !bell.contains(e.target)) {
                this.closeDropdown();
            }
        });

        // Prevent dropdown from closing when clicking inside
        if (dropdown) {
            dropdown.addEventListener('click', (e) => {
                e.stopPropagation();
            });
        }
    }

    toggleDropdown() {
        if (this.isDropdownOpen) {
            this.closeDropdown();
        } else {
            this.openDropdown();
        }
    }

    openDropdown() {
        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown) {
            dropdown.classList.add('show');
            this.isDropdownOpen = true;
            this.loadNotifications(); // Refresh notifications when opening
        }
    }

    closeDropdown() {
        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown) {
            dropdown.classList.remove('show');
            this.isDropdownOpen = false;
        }
    }

    async loadNotifications() {
        try {
            console.log('Loading notifications...');
            const response = await fetch('/api/notifications', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            console.log('Notifications response status:', response.status);

            if (response.ok) {
                const data = await response.json();
                console.log('Notifications data:', data);
                this.notifications = data.notifications || [];
                this.unreadCount = data.unreadCount || 0;
                this.updateUI();
            } else {
                const errorText = await response.text();
                console.error('Failed to load notifications:', response.status, errorText);
                this.showError('Failed to load notifications');
            }
        } catch (error) {
            console.error('Error loading notifications:', error);
            this.showError('Error loading notifications');
        }
    }

    updateUI() {
        this.updateBadge();
        this.renderNotifications();
    }

    updateBadge() {
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            if (this.unreadCount > 0) {
                badge.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        }
    }

    renderNotifications() {
        const list = document.getElementById('notificationList');
        if (!list) return;

        // Filter out read notifications for display
        const unreadNotifications = this.notifications.filter(notification => !notification.isRead);

        if (unreadNotifications.length === 0) {
            list.innerHTML = `
                <div class="notification-empty">
                    <i class="fas fa-bell-slash"></i>
                    <p>No unread notifications</p>
                </div>
            `;
            return;
        }

        const notificationsHtml = unreadNotifications.map(notification => `
            <div class="notification-item ${!notification.isRead ? 'unread' : ''}" 
                 data-notification-id="${notification.notificationId}"
                 onclick="notificationSystem.markAsRead(${notification.notificationId})">
                <div class="notification-content">
                    <p class="notification-message">${this.escapeHtml(notification.message)}</p>
                    <p class="notification-time">${this.formatTimeAgo(notification.sentAt)}</p>
                    <span class="notification-type ${notification.type.toLowerCase()}">${notification.type}</span>
                </div>
            </div>
        `).join('');

        list.innerHTML = notificationsHtml;
    }

    async markAsRead(notificationId) {
        try {
            const response = await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                // Update local state
                const notification = this.notifications.find(n => n.notificationId === notificationId);
                if (notification && !notification.isRead) {
                    notification.isRead = true;
                    this.unreadCount = Math.max(0, this.unreadCount - 1);
                    this.updateUI();
                }
            } else {
                console.error('Failed to mark notification as read');
            }
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
    }

    /**
     * Mark all notifications as read
     */
    async markAllAsRead() {
        try {
            console.log('Marking all notifications as read...');
            
            const response = await fetch('/api/notifications/mark-all-read-test', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('All notifications marked as read:', result);
                
                // Update UI to show all notifications as read
                this.updateUIAfterMarkAllRead();
                
                // Show success message
                this.showMessage('All notifications marked as read', 'success');
            } else {
                const error = await response.json();
                console.error('Failed to mark all as read:', error);
                this.showMessage('Failed to mark all notifications as read', 'error');
            }
        } catch (error) {
            console.error('Error marking all as read:', error);
            this.showMessage('Failed to mark all notifications as read', 'error');
        }
    }

    /**
     * Update UI after marking all notifications as read
     */
    updateUIAfterMarkAllRead() {
        // Hide all unread indicators
        const unreadDots = document.querySelectorAll('.notification-item .unread-dot');
        unreadDots.forEach(dot => {
            dot.style.display = 'none';
        });

        // Update badge to show 0
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            badge.textContent = '0';
            badge.style.display = 'none';
        }

        // Disable the button temporarily
        const markAllBtn = document.getElementById('markAllReadBtn');
        if (markAllBtn) {
            markAllBtn.disabled = true;
            markAllBtn.textContent = 'All Read';
            
            // Re-enable after 2 seconds
            setTimeout(() => {
                markAllBtn.disabled = false;
                markAllBtn.textContent = 'Mark All as Read';
            }, 2000);
        }
    }

    startPolling() {
        // Poll for new notifications every 30 seconds
        setInterval(() => {
            if (!this.isDropdownOpen) {
                this.loadNotifications();
            }
        }, 30000);
    }

    formatTimeAgo(dateString) {
        const now = new Date();
        const date = new Date(dateString);
        const diffInSeconds = Math.floor((now - date) / 1000);

        if (diffInSeconds < 60) {
            return 'Just now';
        } else if (diffInSeconds < 3600) {
            const minutes = Math.floor(diffInSeconds / 60);
            return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
        } else if (diffInSeconds < 86400) {
            const hours = Math.floor(diffInSeconds / 3600);
            return `${hours} hour${hours > 1 ? 's' : ''} ago`;
        } else {
            const days = Math.floor(diffInSeconds / 86400);
            return `${days} day${days > 1 ? 's' : ''} ago`;
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showNotification(message, type) {
        const notification = document.getElementById('notification');
        if (notification) {
            notification.textContent = message;
            notification.className = `notification ${type}`;
            notification.style.display = 'block';
            
            setTimeout(() => {
                notification.style.display = 'none';
            }, 3000);
        }
    }
}

// Initialize notification system when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    console.log('Initializing notification system...');
    try {
        window.notificationSystem = new NotificationSystem();
        console.log('Notification system initialized successfully');
    } catch (error) {
        console.error('Failed to initialize notification system:', error);
    }
});

// Also try to initialize after a short delay in case DOM is not ready
setTimeout(() => {
    if (!window.notificationSystem) {
        console.log('Retrying notification system initialization...');
        try {
            window.notificationSystem = new NotificationSystem();
            console.log('Notification system initialized on retry');
        } catch (error) {
            console.error('Failed to initialize notification system on retry:', error);
        }
    }
}, 1000);

// Export for global access
window.NotificationSystem = NotificationSystem;
