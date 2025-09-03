// Customer Service Executive Dashboard JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

function initializeDashboard() {
    // Initialize search functionality
    initializeCustomerSearch();
    
    // Initialize modals
    initializeModals();
    
}

// Customer Search Functionality
function initializeCustomerSearch() {
    const searchInput = document.getElementById('customerSearchInput');
    const searchBtn = document.getElementById('searchCustomerBtn');
    const resultsContainer = document.getElementById('customerSearchResults');
    
    // Search on button click
    searchBtn.addEventListener('click', function() {
        const searchTerm = searchInput.value.trim();
        if (searchTerm) {
            searchCustomers(searchTerm);
        }
    });
    
    // Search on Enter key
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const searchTerm = searchInput.value.trim();
            if (searchTerm) {
                searchCustomers(searchTerm);
            }
        }
    });
    
    // Clear results when input is cleared
    searchInput.addEventListener('input', function() {
        if (this.value.trim() === '') {
            resultsContainer.style.display = 'none';
        }
    });
}

function searchCustomers(searchTerm) {
    const resultsContainer = document.getElementById('customerSearchResults');
    
    // Show loading state
    resultsContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #6c7993;"><i class="fa-solid fa-spinner fa-spin"></i> Searching...</div>';
    resultsContainer.style.display = 'block';
    
    fetch(`/support/search/customer?searchTerm=${encodeURIComponent(searchTerm)}`)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                showNotification('Error: ' + data.error, 'error');
                resultsContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">No customers found</div>';
                return;
            }
            
            if (data.customers && data.customers.length > 0) {
                displayCustomerResults(data.customers);
            } else {
                resultsContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #6c7993;">No customers found</div>';
            }
        })
        .catch(error => {
            showNotification('Error searching customers', 'error');
            resultsContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">Search failed</div>';
        });
}

function displayCustomerResults(customers) {
    const resultsContainer = document.getElementById('customerSearchResults');
    
    const html = customers.map(customerData => {
        const customer = customerData.customer;
        const accounts = customerData.accounts || [];
        const recentTransactions = customerData.recentTransactions || [];
        
        return `
            <div class="customer-result" onclick="showCustomerDetails(${customer.userId})">
                <div class="customer-name">${customer.firstName} ${customer.lastName}</div>
                <div class="customer-details">
                    <div>NIC: ${customer.nic || 'N/A'}</div>
                    <div>Email: ${customer.email}</div>
                    <div>Accounts: ${accounts.length}</div>
                    <div>Recent Transactions: ${recentTransactions.length}</div>
                </div>
            </div>
        `;
    }).join('');
    
    resultsContainer.innerHTML = html;
}


// Ticket Actions
function viewTicket(ticketId) {
    fetch(`/support/ticket/${ticketId}`)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                showNotification('Error: ' + data.error, 'error');
                return;
            }
            
            showTicketDetails(data);
        })
        .catch(error => {
            showNotification('Error fetching ticket details', 'error');
        });
}

function resolveTicket(ticketId) {
    showConfirmationModal(
        'Are you sure you want to mark this ticket as resolved?',
        function() {
            // Get CSRF token
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            
            fetch(`/support/ticket/${ticketId}/resolve`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showNotification('Ticket resolved successfully', 'success');
                        // Reload the page to refresh the ticket list
                        setTimeout(() => {
                            location.reload();
                        }, 1500);
                    } else {
                        showNotification('Error: ' + (data.error || 'Failed to resolve ticket'), 'error');
                    }
                })
                .catch(error => {
                    showNotification('Error resolving ticket', 'error');
                });
        }
    );
}

function showTicketDetails(data) {
    const modal = document.getElementById('ticketDetailsModal');
    const content = document.getElementById('ticketDetailsContent');
    
    const ticket = data.ticket;
    const customer = data.customer;
    const accounts = data.accounts || [];
    const recentTransactions = data.recentTransactions || [];
    
    const createdDate = new Date(ticket.createdAt).toLocaleDateString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric'
    }) + ' ' + new Date(ticket.createdAt).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });
    
    const resolvedDate = ticket.resolvedAt ? new Date(ticket.resolvedAt).toLocaleDateString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric'
    }) + ' ' + new Date(ticket.resolvedAt).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    }) : 'Not resolved';
    
    content.innerHTML = `
        <div class="modal-section">
            <h4>Ticket Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Ticket ID</span>
                    <span class="info-value">#${ticket.ticketId}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Subject</span>
                    <span class="info-value">${ticket.subject}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Status</span>
                    <span class="info-value">
                        <span class="status-badge ${ticket.status.toLowerCase()}">${ticket.status}</span>
                    </span>
                </div>
                <div class="info-item">
                    <span class="info-label">Created</span>
                    <span class="info-value">${createdDate}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Resolved</span>
                    <span class="info-value">${resolvedDate}</span>
                </div>
            </div>
            <div style="margin-top: 16px;">
                <span class="info-label">Message</span>
                <div class="ticket-message-container">
                    ${ticket.message}
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>Customer Information</h4>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Name</span>
                    <span class="info-value">${customer.firstName} ${customer.lastName}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">NIC</span>
                    <span class="info-value">${customer.nic || 'N/A'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Email</span>
                    <span class="info-value">${customer.email}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Phone Numbers</span>
                    <span class="info-value">${customer.phones && customer.phones.length > 0 ? 
                        customer.phones.map(phone => `${phone.phoneNumber} (${phone.phoneType})`).join(', ') : 'N/A'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Address</span>
                    <span class="info-value">${formatAddress(customer)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Date of Birth</span>
                    <span class="info-value">${customer.dateOfBirth ? new Date(customer.dateOfBirth).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    }) : 'N/A'}</span>
                </div>
            </div>
        </div>
        
        <div class="modal-section">
            <h4>Account Information (${accounts.length} account${accounts.length !== 1 ? 's' : ''})</h4>
            ${accounts.length > 0 ? `
                <div class="accounts-container">
                    ${accounts.map((account, index) => `
                        <div class="account-card ${account.isActive ? 'active' : 'inactive'}">
                            <div class="account-header">
                                <div class="account-icon">
                                    <i class="fas fa-${account.typeName && account.typeName.toLowerCase().includes('savings') ? 'piggy-bank' : 'wallet'}"></i>
                                </div>
                                <div class="account-status">
                                    <span class="status-badge ${account.status.toLowerCase()}">${account.status}</span>
                                    <span class="active-status ${account.isActive ? 'active' : 'inactive'}">
                                        <i class="fas fa-${account.isActive ? 'check-circle' : 'times-circle'}"></i>
                                        ${account.isActive ? 'Active' : 'Inactive'}
                                    </span>
                                </div>
                            </div>
                            <div class="account-details">
                                <div class="account-number">
                                    <span class="label">Account Number:</span>
                                    <span class="value">${account.accountNumber}</span>
                                </div>
                                <div class="account-type">
                                    <span class="label">Account Type:</span>
                                    <span class="value">${account.typeName || 'N/A'}</span>
                                </div>
                                <div class="account-balance">
                                    <span class="label">Balance:</span>
                                    <span class="value">LKR ${account.balance ? parseFloat(account.balance).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2}) : '0.00'}</span>
                                </div>
                                <div class="account-id">
                                    <span class="label">Account ID:</span>
                                    <span class="value">${account.accountId}</span>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            ` : '<div class="no-accounts"><i class="fas fa-inbox"></i><p>No accounts found for this customer</p></div>'}
        </div>
        
        <div class="modal-section">
            <h4>Recent Transactions (Last 5)</h4>
            ${recentTransactions.length > 0 ? `
                <table class="modal-table">
                    <thead>
                        <tr>
                            <th>Type</th>
                            <th>Amount</th>
                            <th>Description</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${recentTransactions.map(transaction => `
                            <tr>
                                <td>
                                    <span style="color: ${getTransactionColor(transaction.type)}">
                                        ${getTransactionIcon(transaction.type)} ${transaction.type}
                                    </span>
                                </td>
                                <td>LKR ${transaction.amount}</td>
                                <td>${transaction.description || 'N/A'}</td>
                                <td>${new Date(transaction.createdAt).toLocaleDateString('en-US', {
                                    month: 'short',
                                    day: '2-digit',
                                    year: 'numeric'
                                })}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            ` : '<p style="color: #6c7993; text-align: center; padding: 20px;">No recent transactions</p>'}
        </div>
    `;
    
    modal.style.display = 'block';
    document.body.classList.add('no-scroll');
}

function showCustomerDetails(customerId) {
    // This would show detailed customer information in a modal
    // For now, we'll just show a notification
    showNotification('Customer details feature coming soon', 'success');
}

// Custom Confirmation Modal Functions
function showConfirmationModal(message, onConfirm) {
    const modal = document.getElementById('confirmationModal');
    const messageElement = document.getElementById('confirmationMessage');
    const confirmButton = document.getElementById('confirmButton');
    
    // Set the message
    messageElement.textContent = message;
    
    // Remove any existing event listeners
    const newConfirmButton = confirmButton.cloneNode(true);
    confirmButton.parentNode.replaceChild(newConfirmButton, confirmButton);
    
    // Add new event listener
    newConfirmButton.addEventListener('click', function() {
        closeConfirmationModal();
        if (onConfirm) {
            onConfirm();
        }
    });
    
    // Show the modal
    modal.style.display = 'block';
    document.body.classList.add('no-scroll');
}

function closeConfirmationModal() {
    const modal = document.getElementById('confirmationModal');
    modal.style.display = 'none';
    document.body.classList.remove('no-scroll');
}

// Modal Management
function initializeModals() {
    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        const ticketModal = document.getElementById('ticketDetailsModal');
        const customerModal = document.getElementById('customerDetailsModal');
        const confirmationModal = document.getElementById('confirmationModal');
        
        if (event.target === ticketModal) {
            closeTicketModal();
        }
        if (event.target === customerModal) {
            closeCustomerModal();
        }
        if (event.target === confirmationModal) {
            closeConfirmationModal();
        }
    });
    
    // Close modals with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeTicketModal();
            closeCustomerModal();
            closeConfirmationModal();
        }
    });
}

function closeTicketModal() {
    const modal = document.getElementById('ticketDetailsModal');
    modal.style.display = 'none';
    document.body.classList.remove('no-scroll');
}

function closeCustomerModal() {
    const modal = document.getElementById('customerDetailsModal');
    modal.style.display = 'none';
    document.body.classList.remove('no-scroll');
}

// Utility Functions
function formatAddress(customer) {
    const parts = [];
    if (customer.street) parts.push(customer.street);
    if (customer.city) parts.push(customer.city);
    if (customer.postalCode) parts.push(customer.postalCode);
    return parts.length > 0 ? parts.join(', ') : 'N/A';
}

function getTransactionColor(type) {
    switch (type) {
        case 'Deposit': return '#16A249';
        case 'Withdrawal': return '#F43E5C';
        case 'Transfer': return '#3281ED';
        default: return '#6c757d';
    }
}

function getTransactionIcon(type) {
    switch (type) {
        case 'Deposit': return '<i class="fas fa-arrow-down"></i>';
        case 'Withdrawal': return '<i class="fas fa-arrow-up"></i>';
        case 'Transfer': return '<i class="fas fa-exchange-alt"></i>';
        default: return '<i class="fas fa-question"></i>';
    }
}


function showNotification(message, type) {
    const notification = document.getElementById('notification');
    notification.textContent = message;
    notification.className = `notification ${type}`;
    notification.style.display = 'block';
    
    setTimeout(() => {
        notification.style.display = 'none';
    }, 3000);
}

// Delete Solved Case Functionality
function deleteSolvedCase(ticketId) {
    showConfirmationModal(
        'Are you sure you want to permanently delete this solved case? This action cannot be undone.',
        function() {
            // Get CSRF token
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            
            fetch(`/support/ticket/${ticketId}/delete`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (response.status === 404) {
                        // Fallback to frontend-only deletion if endpoint doesn't exist
                        console.warn('Delete endpoint not found, using frontend-only deletion');
                        const cardToRemove = document.querySelector(`[data-ticket-id="${ticketId}"]`);
                        if (cardToRemove) {
                            cardToRemove.style.opacity = '0';
                            cardToRemove.style.transform = 'scale(0.8)';
                            cardToRemove.style.transition = 'all 0.3s ease';
                            setTimeout(() => {
                                cardToRemove.remove();
                                updateSolvedCount();
                                showNotification('Solved case removed from view (Backend endpoint not implemented)', 'success');
                            }, 300);
                        }
                        return;
                    } else if (response.status === 403) {
                        // Handle forbidden access
                        return response.json().then(data => {
                            showNotification('Access denied: ' + (data.error || 'You do not have permission to delete tickets'), 'error');
                            throw new Error('Forbidden');
                        });
                    } else if (response.status === 401) {
                        // Handle unauthorized access
                        showNotification('You must be logged in to delete tickets', 'error');
                        throw new Error('Unauthorized');
                    } else if (!response.ok) {
                        // Handle other HTTP errors
                        return response.json().then(data => {
                            showNotification('Error: ' + (data.error || 'Failed to delete ticket'), 'error');
                            throw new Error('HTTP Error: ' + response.status);
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    if (data && data.success) {
                        showNotification('Solved case deleted successfully', 'success');
                        // Remove the card from the DOM
                        const cardToRemove = document.querySelector(`[data-ticket-id="${ticketId}"]`);
                        if (cardToRemove) {
                            cardToRemove.style.opacity = '0';
                            cardToRemove.style.transform = 'scale(0.8)';
                            cardToRemove.style.transition = 'all 0.3s ease';
                            setTimeout(() => {
                                cardToRemove.remove();
                                updateSolvedCount();
                            }, 300);
                        }
                    } else if (data && data.error) {
                        showNotification('Error: ' + data.error, 'error');
                    }
                })
                .catch(error => {
                    console.error('Delete error:', error);
                    if (error.message !== 'Forbidden' && error.message !== 'Unauthorized' && !error.message.startsWith('HTTP Error:')) {
                        showNotification('Network error: Please check your connection', 'error');
                    }
                });
        }
    );
}

// Update solved cases count
function updateSolvedCount() {
    const solvedCards = document.querySelectorAll('.solved-case-card');
    const countElement = document.querySelector('.solved-count');
    if (countElement) {
        countElement.textContent = `${solvedCards.length} Total`;
    }
}
