document.addEventListener('DOMContentLoaded', () => {
    // Fade-in animation for nav links and auth buttons
    const navLinks = document.querySelectorAll('.nav-link, .auth-buttons a, .auth-buttons button');
    navLinks.forEach((link, index) => {
        link.style.opacity = 0;
        setTimeout(() => {
            let opacity = 0;
            const fadeIn = () => {
                opacity += 0.05;
                link.style.opacity = opacity;
                if (opacity < 1) requestAnimationFrame(fadeIn);
            };
            fadeIn();
        }, index * 100); // Staggered animation
    });

    // Hover effect for nav links
    const navItems = document.querySelectorAll('.nav-link');
    navItems.forEach(item => {
        item.addEventListener('mouseenter', () => {
            item.style.transform = 'translateY(-2px)';
        });
        item.addEventListener('mouseleave', () => {
            item.style.transform = 'translateY(0)';
        });
        
        // Close mobile menu when nav link is clicked
        item.addEventListener('click', () => {
            closeMobileMenu();
        });
    });

    // Initialize notification system
    initializeNotifications();
    
    // Initialize mobile menu
    initializeMobileMenu();
});

// Mobile Menu Functions
function initializeMobileMenu() {
    // Create mobile menu overlay
    const overlay = document.createElement('div');
    overlay.className = 'mobile-menu-overlay';
    overlay.id = 'mobileMenuOverlay';
    document.body.appendChild(overlay);
    
    // Add click event to overlay to close menu
    overlay.addEventListener('click', closeMobileMenu);
    
    console.log('Mobile menu initialized');
}

function toggleMobileMenu() {
    const navbar = document.getElementById('navbar');
    const hamburgerMenu = document.getElementById('hamburgerMenu');
    const overlay = document.getElementById('mobileMenuOverlay');
    
    console.log('Toggle mobile menu:', {
        navbar: !!navbar,
        hamburgerMenu: !!hamburgerMenu,
        overlay: !!overlay
    });
    
    if (navbar && hamburgerMenu && overlay) {
        const isActive = navbar.classList.contains('active');
        
        if (isActive) {
            closeMobileMenu();
        } else {
            openMobileMenu();
        }
    } else {
        console.log('Mobile menu elements not found');
    }
}

function openMobileMenu() {
    const navbar = document.getElementById('navbar');
    const hamburgerMenu = document.getElementById('hamburgerMenu');
    const overlay = document.getElementById('mobileMenuOverlay');
    
    if (navbar && hamburgerMenu && overlay) {
        navbar.classList.add('active');
        hamburgerMenu.classList.add('active');
        overlay.classList.add('active');
        overlay.style.display = 'block';
        document.body.style.overflow = 'hidden'; // Prevent background scrolling
    }
}

function closeMobileMenu() {
    const navbar = document.getElementById('navbar');
    const hamburgerMenu = document.getElementById('hamburgerMenu');
    const overlay = document.getElementById('mobileMenuOverlay');
    
    if (navbar && hamburgerMenu && overlay) {
        navbar.classList.remove('active');
        hamburgerMenu.classList.remove('active');
        overlay.classList.remove('active');
        setTimeout(() => {
            overlay.style.display = 'none';
        }, 300);
        document.body.style.overflow = 'auto'; // Restore scrolling
    }
}

// Notification System Functions
let notificationPopup = null;
let notificationBadge = null;
let notificationList = null;

function initializeNotifications() {
    notificationPopup = document.getElementById('notificationPopup');
    notificationBadge = document.getElementById('notificationBadge');
    notificationList = document.getElementById('notificationList');
    
    console.log('Initializing notifications:', {
        popup: !!notificationPopup,
        badge: !!notificationBadge,
        list: !!notificationList
    });
    
    if (notificationPopup && notificationBadge && notificationList) {
        // Load initial notification count
        loadNotificationCount();
        
        // Set up periodic refresh (every 30 seconds)
        setInterval(loadNotificationCount, 30000);
        
        // Close popup when clicking outside
        document.addEventListener('click', (event) => {
            if (!event.target.closest('.notification-container')) {
                closeNotificationPopup();
            }
        });
    } else {
        console.log('Notification elements not found');
    }
}

function toggleNotificationPopup() {
    if (!notificationPopup) {
        console.log('Notification popup not found');
        return;
    }
    
    const isVisible = notificationPopup.classList.contains('show') || 
                     notificationPopup.style.display === 'block';
    
    if (isVisible) {
        closeNotificationPopup();
    } else {
        showNotificationPopup();
    }
}

function showNotificationPopup() {
    if (!notificationPopup) return;
    
    notificationPopup.style.display = 'block';
    notificationPopup.classList.add('show');
    console.log('Showing notification popup');
    loadNotifications();
}

function closeNotificationPopup() {
    if (!notificationPopup) return;
    
    notificationPopup.classList.remove('show');
    setTimeout(() => {
        notificationPopup.style.display = 'none';
    }, 300);
    console.log('Closing notification popup');
}

function loadNotificationCount() {
    fetch('/api/notifications/count')
        .then(response => response.json())
        .then(data => {
            if (data.unreadCount > 0) {
                notificationBadge.textContent = data.unreadCount;
                notificationBadge.style.display = 'flex';
            } else {
                notificationBadge.style.display = 'none';
            }
        })
        .catch(error => {
            // Silently handle error
        });
}

function loadNotifications() {
    if (!notificationList) {
        return;
    }
    
    notificationList.innerHTML = '<div class="notification-loading">Loading notifications...</div>';
    
    fetch('/api/notifications/all')
        .then(response => {
            return response.json();
        })
        .then(data => {
            if (data.notifications && data.notifications.length > 0) {
                displayNotifications(data.notifications);
            } else {
                notificationList.innerHTML = '<div class="notification-empty">No notifications</div>';
            }
        })
        .catch(error => {
            notificationList.innerHTML = '<div class="notification-empty">Error loading notifications</div>';
        });
}

function displayNotifications(notifications) {
    if (!notificationList) return;
    
    notificationList.innerHTML = '';
    
    notifications.forEach(notification => {
        const notificationItem = document.createElement('div');
        notificationItem.className = `notification-item ${!notification.isRead ? 'unread' : ''}`;
        notificationItem.onclick = () => markNotificationAsRead(notification.notificationId);
        
        const timeAgo = getTimeAgo(notification.sentAt);
        
        notificationItem.innerHTML = `
            <div class="notification-message">${notification.message}</div>
            <div class="notification-time">${timeAgo}</div>
            <div class="notification-type">${notification.type}</div>
        `;
        
        notificationList.appendChild(notificationItem);
    });
}

function markNotificationAsRead(notificationId) {
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    const headers = {
        'Content-Type': 'application/json',
    };
    
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    
    fetch(`/api/notifications/${notificationId}/read`, {
        method: 'POST',
        headers: headers
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Reload notifications and count
            loadNotifications();
            loadNotificationCount();
        }
    })
    .catch(error => {
        // Silently handle error
    });
}

function markAllAsRead() {
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    const headers = {
        'Content-Type': 'application/json',
    };
    
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    
    fetch('/api/notifications/mark-all-read', {
        method: 'POST',
        headers: headers
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Reload notifications and count
            loadNotifications();
            loadNotificationCount();
        }
    })
    .catch(error => {
        // Silently handle error
    });
}

function getTimeAgo(dateString) {
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

