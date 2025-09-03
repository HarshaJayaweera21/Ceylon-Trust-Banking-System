const overlay = document.getElementById('overlay');
const modal = document.getElementById('txnModal');
const toast = document.getElementById('toast');
function csrfHeaders() {
  const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  return { [header]: token };
}
const txnTypeInput = document.getElementById('txnType');
const title = document.getElementById('txnTitle');
const srcGroup = document.getElementById('srcGroup');
const destGroup = document.getElementById('destGroup');

function showToast(message, type = 'success') {
  if (!toast) {
    console.error('Toast element not found!');
    return;
  }
  toast.textContent = message;
  toast.className = 'toast ' + (type === 'error' ? 'error' : 'success');
  toast.classList.add('show');
  window.setTimeout(() => {
    toast.classList.remove('show');
  }, 3500);
}

function openTxnModal(kind) {
  txnTypeInput.value = kind;
  const modalIcon = document.getElementById('modalIcon');
  const title = document.getElementById('txnTitle');
  const subtitle = document.getElementById('modalSubtitle');
  const processBtn = document.getElementById('processBtn');
  
  // Update modal content based on transaction type
  if (kind === 'DEPOSIT') {
    modalIcon.innerHTML = '<i class="fa-solid fa-circle-arrow-down"></i>';
    title.textContent = 'Deposit Transaction';
    subtitle.textContent = 'Process a deposit to customer account';
    processBtn.innerHTML = '<i class="fa-solid fa-circle-arrow-down"></i> Process Deposit';
    processBtn.className = 'btn btn-process deposit-process';
  } else if (kind === 'WITHDRAWAL') {
    modalIcon.innerHTML = '<i class="fa-solid fa-circle-arrow-up"></i>';
    title.textContent = 'Withdrawal Transaction';
    subtitle.textContent = 'Process a withdrawal from customer account';
    processBtn.innerHTML = '<i class="fa-solid fa-circle-arrow-up"></i> Process Withdrawal';
    processBtn.className = 'btn btn-process withdrawal-process';
  }
  
  destGroup.style.display = 'none';
  overlay.style.display = 'block';
  modal.style.display = 'flex';
}

function closeTxnModal() {
  overlay.style.display = 'none';
  modal.style.display = 'none';
  document.getElementById('txnForm').reset();
}

overlay?.addEventListener('click', closeTxnModal);

async function fetchApprovals() {
  const res = await fetch('/cashier/api/approvals', { headers: { ...csrfHeaders() } });
  if (!res.ok) return;
  const rows = await res.json();
  const body = document.getElementById('approvalsBody');
  const emptyRow = document.getElementById('approvalsEmpty');
  const noApprovalsDiv = document.getElementById('noApprovals');
  
  body.innerHTML = '';
  
  // Update pending count
  const pendingCount = document.getElementById('pendingCount');
  const approvalCount = document.getElementById('approvalCount');
  if (pendingCount) pendingCount.textContent = rows.length;
  if (approvalCount) approvalCount.textContent = `${rows.length} Pending`;
  
  if (!rows.length) {
    body.innerHTML = '<tr id="approvalsEmpty"><td colspan="6">No pending approvals</td></tr>';
    if (noApprovalsDiv) noApprovalsDiv.style.display = 'block';
    return;
  }
  
  if (noApprovalsDiv) noApprovalsDiv.style.display = 'none';
  for (const r of rows) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.customerName}</td>
      <td>${r.accountNumber}</td>
      <td>${r.accountType}</td>
      <td>Rs. ${Number(r.initialDeposit).toLocaleString()}</td>
      <td>${r.requestedAt}</td>
      <td class="actions">
        <button class="action-btn-table approve-btn" data-id="${r.accountId}" data-action="approve" title="Approve">
          <i class="fa-solid fa-check"></i>
        </button>
        <button class="action-btn-table reject-btn" data-id="${r.accountId}" data-action="reject" title="Reject">
          <i class="fa-solid fa-times"></i>
        </button>
      </td>`;
    body.appendChild(tr);
  }
  body.addEventListener('click', async (e) => {
    const btn = e.target.closest('button');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const action = btn.getAttribute('data-action');
    const res2 = await fetch(`/cashier/api/approvals/${id}/${action}`, { method: 'POST', headers: { ...csrfHeaders() } });
    const payload = await res2.json().catch(() => ({}));
    if (res2.ok) { showToast('Request ' + action + 'd'); fetchApprovals(); fetchHistory(); }
    else { showToast(payload.message || 'Action failed', 'error'); }
  }, { once: true });
}

async function submitTxn(e) {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target).entries());
  // map to API contract
  const payload = {
    type: data.type,
    sourceAccountNumber: data.sourceAccountNumber,
    destinationAccountNumber: data.destinationAccountNumber || null,
    amount: Number(data.amount),
    description: data.description || null
  };
  const res = await fetch('/cashier/api/transactions', {
    method: 'POST', headers: { 'Content-Type': 'application/json', ...csrfHeaders() }, body: JSON.stringify(payload)
  });
  const out = await res.json().catch(() => ({}));
  if (res.ok) {
    showToast('Transaction processed. Ref: ' + out.referenceNumber);
    closeTxnModal();
    fetchHistory();
    refreshSummary();
  } else {
    showToast(out.message || 'Transaction failed', 'error');
  }
}

async function fetchHistory() {
  const res = await fetch('/cashier/api/history/today', { headers: { ...csrfHeaders() } });
  if (!res.ok) return;
  const rows = await res.json();
  const body = document.getElementById('historyBody');
  const noHistoryDiv = document.getElementById('noHistory');
  
  body.innerHTML = '';
  
  // Update history count
  const historyCount = document.getElementById('historyCount');
  if (historyCount) historyCount.textContent = `${rows.length} Today`;
  
  if (!rows.length) {
    body.innerHTML = '<tr id="historyEmpty"><td colspan="7">No transactions today</td></tr>';
    if (noHistoryDiv) noHistoryDiv.style.display = 'block';
    return;
  }
  
  if (noHistoryDiv) noHistoryDiv.style.display = 'none';
  for (const r of rows) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.transactionId}</td>
      <td>${r.accountNumbers}</td>
      <td>${r.type}</td>
      <td>Rs. ${Number(r.amount).toLocaleString()}</td>
      <td>${r.referenceNumber}</td>
      <td>${r.timestamp}</td>
      <td>${r.status}</td>`;
    body.appendChild(tr);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  initializeCustomerSearch();
  fetchApprovals();
  fetchHistory();
  refreshSummary();
});

// Customer Search Functionality
let searchTimeout;

function initializeCustomerSearch() {
    const searchInput = document.getElementById('customerSearchInput');
    const searchBtn = document.getElementById('searchCustomerBtn');
    const resultsContainer = document.getElementById('customerSearchResults');
    
    if (!searchInput || !searchBtn || !resultsContainer) return;
    
    // Real-time search as user types (with debouncing)
    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.trim();
        
        // Clear previous timeout
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }
        
        if (searchTerm === '') {
            resultsContainer.style.display = 'none';
            return;
        }
        
        // Only search if the term has at least 3 characters for NIC search
        if (searchTerm.length >= 3) {
            // Debounce the search to avoid too many API calls
            searchTimeout = setTimeout(() => {
                searchCustomers(searchTerm);
            }, 300); // 300ms delay
        } else {
            resultsContainer.style.display = 'none';
        }
    });
    
    // Search on button click
    searchBtn.addEventListener('click', function() {
        const searchTerm = searchInput.value.trim();
        if (searchTerm) {
            // Clear timeout and search immediately
            if (searchTimeout) {
                clearTimeout(searchTimeout);
            }
            searchCustomers(searchTerm);
        }
    });
    
    // Search on Enter key
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const searchTerm = searchInput.value.trim();
            if (searchTerm) {
                // Clear timeout and search immediately
                if (searchTimeout) {
                    clearTimeout(searchTimeout);
                }
                searchCustomers(searchTerm);
            }
        }
    });
}

function searchCustomers(searchTerm) {
    const resultsContainer = document.getElementById('customerSearchResults');
    
    // Show loading state
    resultsContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #6c7993;"><i class="fa-solid fa-spinner fa-spin"></i> Searching...</div>';
    resultsContainer.style.display = 'block';
    
    fetch(`/cashier/api/search/customer?searchTerm=${encodeURIComponent(searchTerm)}`)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                showToast('Error: ' + data.error, 'error');
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
            showToast('Error searching customers', 'error');
            resultsContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">Search failed</div>';
        });
}

function displayCustomerResults(customers) {
    const resultsContainer = document.getElementById('customerSearchResults');
    
    const html = customers.map((customerData, index) => {
        const customer = customerData.customer;
        const accounts = customerData.accounts || [];
        const recentTransactions = customerData.recentTransactions || [];
        
        return `
            <div class="customer-result" onclick="showCustomerDetailsFromIndex(${index})">
                <div class="customer-name">${customer.firstName} ${customer.lastName}</div>
                <div class="customer-details">
                    <div><strong>NIC:</strong> ${customer.nic || 'N/A'}</div>
                    <div>Email: ${customer.email}</div>
                    <div>Accounts: ${accounts.length}</div>
                    <div>Recent Transactions: ${recentTransactions.length}</div>
                </div>
            </div>
        `;
    }).join('');
    
    resultsContainer.innerHTML = html;
    
    // Store the customers data globally for access by the onclick handler
    window.currentSearchResults = customers;
}

function showCustomerDetailsFromIndex(index) {
    const modal = document.getElementById('customerDetailsModal');
    const content = document.getElementById('customerDetailsContent');
    
    try {
        // Get the customer data from the stored search results
        if (!window.currentSearchResults || !window.currentSearchResults[index]) {
            content.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">Customer data not found</div>';
            modal.style.display = 'flex';
            return;
        }
        
        const customerData = window.currentSearchResults[index];
        const customer = customerData.customer;
        const accounts = customerData.accounts || [];
        const recentTransactions = customerData.recentTransactions || [];
        
        // Store current customer ID for refresh purposes
        window.currentCustomerId = customer.userId;
        
        content.innerHTML = `
            <div class="modal-section">
                <h4>Customer Information</h4>
                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">Name</span>
                        <span class="info-value">${customer.firstName} ${customer.lastName}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Email</span>
                        <span class="info-value">${customer.email}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">NIC</span>
                        <span class="info-value">${customer.nic || 'N/A'}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">City</span>
                        <span class="info-value">${customer.city || 'N/A'}</span>
                    </div>
                </div>
            </div>
            
            <div class="modal-section">
                <h4>Accounts (${accounts.length})</h4>
                ${accounts.length > 0 ? `
                    <table class="modal-table">
                        <thead>
                            <tr>
                                <th>Account Number</th>
                                <th>Type</th>
                                <th>Balance</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${accounts.map(account => `
                                <tr>
                                    <td>${account.accountNumber}</td>
                                    <td>${account.typeName}</td>
                                    <td>Rs. ${Number(account.balance).toLocaleString()}</td>
                                    <td>
                                        <span class="status-badge ${account.status.toLowerCase()}">${account.status}</span>
                                    </td>
                                    <td>
                                        ${account.status === 'Approved' && account.isActive ? 
                                            `<button class="btn-close-account" onclick="closeAccount(${account.accountId}, ${account.balance})" title="Close Account">
                                                <i class="fa-solid fa-times-circle"></i> Close
                                            </button>` : 
                                        account.status === 'Closed' && !account.isActive ?
                                            `<button class="btn-open-account" onclick="openAccount(${account.accountId})" title="Open Account">
                                                <i class="fa-solid fa-check-circle"></i> Open
                                            </button>` :
                                            `<span class="account-action-disabled">N/A</span>`
                                        }
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                ` : '<p>No accounts found</p>'}
            </div>
            
            <div class="modal-section">
                <h4>Recent Transactions (${recentTransactions.length})</h4>
                ${recentTransactions.length > 0 ? `
                    <table class="modal-table">
                        <thead>
                            <tr>
                                <th>Type</th>
                                <th>Amount</th>
                                <th>Date</th>
                                <th>Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${recentTransactions.map(transaction => `
                                <tr>
                                    <td>${transaction.type}</td>
                                    <td>Rs. ${Number(transaction.amount).toLocaleString()}</td>
                                    <td>${new Date(transaction.createdAt).toLocaleDateString()}</td>
                                    <td>${transaction.description || 'N/A'}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                ` : '<p>No recent transactions</p>'}
            </div>
        `;
        
        modal.style.display = 'flex';
    } catch (error) {
        console.error('Error displaying customer data:', error);
        content.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">Error loading customer details</div>';
        modal.style.display = 'flex';
    }
}

// Keep the old function for backward compatibility
function showCustomerDetails(customerId) {
    const modal = document.getElementById('customerDetailsModal');
    const content = document.getElementById('customerDetailsContent');
    
    // Show loading state
    content.innerHTML = '<div style="padding: 20px; text-align: center;"><i class="fa-solid fa-spinner fa-spin"></i> Loading customer details...</div>';
    modal.style.display = 'flex';
    
    // Fetch customer details using cashier endpoint
    fetch(`/cashier/api/search/customer?searchTerm=${customerId}`)
        .then(response => response.json())
        .then(data => {
            // Find the specific customer by user ID from the search results
            let customerData = null;
            if (data.customers && data.customers.length > 0) {
                // Look for exact match by user ID
                customerData = data.customers.find(c => c.customer.userId == customerId);
                
                // If not found by exact ID match, try the first result
                if (!customerData && data.customers.length > 0) {
                    customerData = data.customers[0];
                }
            }
            
            if (customerData) {
                const customer = customerData.customer;
                const accounts = customerData.accounts || [];
                const recentTransactions = customerData.recentTransactions || [];
                
                content.innerHTML = `
                    <div class="modal-section">
                        <h4>Customer Information</h4>
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="info-label">Name</span>
                                <span class="info-value">${customer.firstName} ${customer.lastName}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">Email</span>
                                <span class="info-value">${customer.email}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">NIC</span>
                                <span class="info-value">${customer.nic || 'N/A'}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">City</span>
                                <span class="info-value">${customer.city || 'N/A'}</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="modal-section">
                        <h4>Accounts (${accounts.length})</h4>
                        ${accounts.length > 0 ? `
                            <table class="modal-table">
                                <thead>
                                    <tr>
                                        <th>Account Number</th>
                                        <th>Type</th>
                                        <th>Balance</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${accounts.map(account => `
                                        <tr>
                                            <td>${account.accountNumber}</td>
                                            <td>${account.typeName}</td>
                                            <td>Rs. ${Number(account.balance).toLocaleString()}</td>
                                            <td><span class="status-badge ${account.status.toLowerCase()}">${account.status}</span></td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        ` : '<p>No accounts found</p>'}
                    </div>
                    
                    <div class="modal-section">
                        <h4>Recent Transactions (${recentTransactions.length})</h4>
                        ${recentTransactions.length > 0 ? `
                            <table class="modal-table">
                                <thead>
                                    <tr>
                                        <th>Type</th>
                                        <th>Amount</th>
                                        <th>Date</th>
                                        <th>Description</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${recentTransactions.map(transaction => `
                                        <tr>
                                            <td>${transaction.type}</td>
                                            <td>Rs. ${Number(transaction.amount).toLocaleString()}</td>
                                            <td>${new Date(transaction.createdAt).toLocaleDateString()}</td>
                                            <td>${transaction.description || 'N/A'}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        ` : '<p>No recent transactions</p>'}
                    </div>
                `;
            } else {
                content.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">Customer not found</div>';
            }
        })
        .catch(error => {
            content.innerHTML = '<div style="padding: 20px; text-align: center; color: #dc3545;">Error loading customer details</div>';
        });
}

function closeCustomerModal() {
    const modal = document.getElementById('customerDetailsModal');
    modal.style.display = 'none';
}

async function closeAccount(accountId, balance) {
    // Check if account has non-zero balance
    if (Number(balance) > 0) {
        showCustomAlert(
            'warning',
            'Cannot Close Account',
            `Account balance must be zero to close this account.\nCurrent balance: Rs. ${Number(balance).toLocaleString()}`,
            'fa-exclamation-triangle'
        );
        return;
    }
    
    const confirmed = await showCustomConfirm(
        'warning',
        'Close Account',
        'Are you sure you want to close this account? This action cannot be undone.',
        'fa-exclamation-triangle'
    );
    
    if (!confirmed) {
        return;
    }
    
    try {
        const response = await fetch(`/cashier/api/account/${accountId}/close`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...csrfHeaders()
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast(result.message, 'success');
            // Automatically refresh the customer details to show updated status
            await refreshCustomerDetails();
        } else {
            showToast(result.error || 'Failed to close account', 'error');
        }
    } catch (error) {
        console.error('Error closing account:', error);
        showToast('Error closing account', 'error');
    }
}

async function openAccount(accountId) {
    const confirmed = await showCustomConfirm(
        'info',
        'Open Account',
        'Are you sure you want to open this account?',
        'fa-check-circle'
    );
    
    if (!confirmed) {
        return;
    }
    
    try {
        const response = await fetch(`/cashier/api/account/${accountId}/open`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...csrfHeaders()
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast(result.message, 'success');
            // Automatically refresh the customer details to show updated status
            await refreshCustomerDetails();
        } else {
            showToast(result.error || 'Failed to open account', 'error');
        }
    } catch (error) {
        console.error('Error opening account:', error);
        showToast('Error opening account', 'error');
    }
}

// Custom Alert Functions
function showCustomAlert(type, title, message, iconClass) {
    const modal = document.getElementById('customAlertModal');
    const icon = document.querySelector('.alert-icon');
    const titleElement = document.querySelector('.alert-title');
    const messageElement = document.querySelector('.alert-message');
    
    // Set icon and class
    icon.className = `alert-icon ${type}`;
    icon.innerHTML = `<i class="fa-solid ${iconClass}"></i>`;
    
    // Set title and message
    titleElement.textContent = title;
    messageElement.textContent = message;
    
    // Show modal
    modal.style.display = 'flex';
}

function closeCustomAlert() {
    const modal = document.getElementById('customAlertModal');
    modal.style.display = 'none';
}

// Custom Confirmation Functions
function showCustomConfirm(type, title, message, iconClass) {
    return new Promise((resolve) => {
        const modal = document.getElementById('customConfirmModal');
        const icon = document.querySelector('.confirm-icon');
        const titleElement = document.querySelector('.confirm-title');
        const messageElement = document.querySelector('.confirm-message');
        
        // Set icon and class
        icon.className = `confirm-icon ${type}`;
        icon.innerHTML = `<i class="fa-solid ${iconClass}"></i>`;
        
        // Set title and message
        titleElement.textContent = title;
        messageElement.textContent = message;
        
        // Show modal
        modal.style.display = 'flex';
        
        // Store the resolve function globally so buttons can access it
        window.confirmResolve = resolve;
    });
}

function closeCustomConfirm(result) {
    const modal = document.getElementById('customConfirmModal');
    modal.style.display = 'none';
    
    // Call the stored resolve function with the result
    if (window.confirmResolve) {
        window.confirmResolve(result);
        window.confirmResolve = null;
    }
}

// Helper function to refresh customer details
async function refreshCustomerDetails() {
    const modal = document.getElementById('customerDetailsModal');
    if (modal.style.display === 'flex') {
        // Find the current customer data and refresh
        const searchInput = document.getElementById('customerSearchInput');
        if (searchInput && searchInput.value.trim()) {
            await searchCustomers(searchInput.value.trim());
            
            // Re-open the customer details modal with updated data
            setTimeout(() => {
                // Find the customer in the updated search results and show their details
                if (window.currentSearchResults && window.currentSearchResults.length > 0) {
                    const currentCustomerIndex = window.currentSearchResults.findIndex(
                        customer => customer.customer.userId === window.currentCustomerId
                    );
                    if (currentCustomerIndex !== -1) {
                        showCustomerDetailsFromIndex(currentCustomerIndex);
                    }
                }
            }, 500); // Small delay to ensure search results are updated
        }
    }
}

async function refreshSummary() {
  const res = await fetch('/cashier/api/summary/today', { headers: { ...csrfHeaders() } });
  if (!res.ok) return;
  const s = await res.json();
  const cnt = document.getElementById('txnCount');
  const tot = document.getElementById('totalProcessed');
  if (cnt) cnt.textContent = s.transactionCount;
  if (tot) tot.textContent = s.totalAmount;
}


