document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('openAccountForm');
    const notification = document.getElementById('notification');
    const modal = document.getElementById('addAccountModal');
    const table = document.getElementById('transactionTable');
    const paginationContainer = document.querySelector('.pagination');
    const pieCanvas = document.getElementById('txPie');

    // --- 1. Handle Notifications from URL Parameters ---
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    if (success) {
        // Check if it's a loan application success message
        if (success === 'Loan application submitted successfully!') {
            showNotification('Loan application submitted successfully! It is now pending for approval.', 'success');
        } else if (success === 'true') {
            showNotification('Account created successfully! It is now pending for approval.', 'success');
        } else {
            showNotification(success, 'success');
        }
    } else if (error) {
        showNotification(error, 'error');
    }
    // Clean the URL to remove the parameters after showing the notification
    if (success || error) {
        history.replaceState(null, '', window.location.pathname);
    }

    // --- 2. Form Validation ---
    if (form) {
        const typeField = document.getElementById('typeId');
        const nicField = document.getElementById('nic');
        const depositField = document.getElementById('initialDeposit');
        const termsField = document.getElementById('terms');

        const nicRegex = /^(\d{9}[VvXx]|\d{12})$/; // Sri Lankan NIC: 9 digits + V/v/X/x OR 12 digits

        const validate = () => {
            const issues = [];
            if (!typeField.value) issues.push('Select an account type.');
            if (!nicRegex.test(nicField.value.trim())) issues.push('Enter a valid NIC (9 digits + V/X or 12 digits).');
            const deposit = parseFloat(depositField.value);
            if (Number.isNaN(deposit) || deposit < 0) issues.push('Deposit must be 0.00 LKR or more.');
            if (!termsField.checked) issues.push('You must agree to the terms.');
            return issues;
        };

        const showFieldValidity = () => {
            // simple field styling feedback
            nicField.classList.toggle('invalid', nicField.value && !nicRegex.test(nicField.value.trim()));
            depositField.classList.toggle('invalid', depositField.value !== '' && (parseFloat(depositField.value) < 0));
        };

        form.addEventListener('input', showFieldValidity);

        form.addEventListener('submit', (e) => {
            const issues = validate();
            if (issues.length) {
                e.preventDefault();
                showNotification(issues[0], 'error'); // show first error
            }
        });
    }

    // --- 3. Modal Control Functions (exposed to global scope) ---
    window.openAddModal = function() {
        if (modal) {
            modal.style.display = 'block';
            document.body.classList.add('no-scroll'); // prevent page scroll while modal open
        }
    };

    window.closeAddModal = function() {
        if (modal) {
            modal.style.display = 'none';
            document.body.classList.remove('no-scroll');
        }
    };

    // Close modal if user clicks outside of the modal content
    window.onclick = function(event) {
        if (event.target === modal) {
            closeAddModal();
        }
        if (event.target === document.getElementById('loanModal')) {
            closeLoanModal();
        }
    };


    // --- 4. Client-Side Pagination for Transactions ---
    function setupPagination() {
        const rows = document.querySelectorAll('#transactionBody tr');
        const perPage = 5;
        if (rows.length === 0) return;

        const totalPages = Math.ceil(rows.length / perPage);
        let currentPage = 1;
        paginationContainer.innerHTML = ''; // Clear old buttons

        const showPage = (pageNumber) => {
            const start = (pageNumber - 1) * perPage;
            const end = start + perPage;
            rows.forEach((row, index) => {
                row.style.display = (index >= start && index < end) ? '' : 'none';
            });
            currentPage = pageNumber;
            updatePaginationButtons();
        };

        const updatePaginationButtons = () => {
            paginationContainer.innerHTML = ''; // Clear existing buttons

            // Previous button
            const prevBtn = document.createElement('button');
            prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i> Previous';
            prevBtn.className = 'nav-btn';
            prevBtn.disabled = currentPage === 1;
            prevBtn.addEventListener('click', () => {
                if (currentPage > 1) {
                    showPage(currentPage - 1);
                }
            });
            paginationContainer.appendChild(prevBtn);

            // Page number buttons
            const maxVisiblePages = 5;
            let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
            let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

            // Adjust start page if we're near the end
            if (endPage - startPage + 1 < maxVisiblePages) {
                startPage = Math.max(1, endPage - maxVisiblePages + 1);
            }

            // First page and ellipsis
            if (startPage > 1) {
                const firstBtn = document.createElement('button');
                firstBtn.textContent = '1';
                firstBtn.className = 'page-btn';
                firstBtn.addEventListener('click', () => showPage(1));
                paginationContainer.appendChild(firstBtn);

                if (startPage > 2) {
                    const ellipsis = document.createElement('span');
                    ellipsis.textContent = '...';
                    ellipsis.style.padding = '0 8px';
                    ellipsis.style.color = '#6c757d';
                    paginationContainer.appendChild(ellipsis);
                }
            }

            // Page number buttons
            for (let i = startPage; i <= endPage; i++) {
                const btn = document.createElement('button');
                btn.textContent = i;
                btn.className = 'page-btn';
                if (i === currentPage) {
                    btn.classList.add('active');
                }
                btn.addEventListener('click', () => showPage(i));
                paginationContainer.appendChild(btn);
            }

            // Last page and ellipsis
            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    const ellipsis = document.createElement('span');
                    ellipsis.textContent = '...';
                    ellipsis.style.padding = '0 8px';
                    ellipsis.style.color = '#6c757d';
                    paginationContainer.appendChild(ellipsis);
                }

                const lastBtn = document.createElement('button');
                lastBtn.textContent = totalPages;
                lastBtn.className = 'page-btn';
                lastBtn.addEventListener('click', () => showPage(totalPages));
                paginationContainer.appendChild(lastBtn);
            }

            // Next button
            const nextBtn = document.createElement('button');
            nextBtn.innerHTML = 'Next <i class="fas fa-chevron-right"></i>';
            nextBtn.className = 'nav-btn';
            nextBtn.disabled = currentPage === totalPages;
            nextBtn.addEventListener('click', () => {
                if (currentPage < totalPages) {
                    showPage(currentPage + 1);
                }
            });
            paginationContainer.appendChild(nextBtn);
        };

        showPage(1); // Show the first page initially
    }

    // Initialize pagination if the table exists and has transactions
    const transactionRows = document.querySelectorAll('#transactionBody tr');
    
    if (table && transactionRows.length > 0) {
        setupPagination();
    }


    // --- 5. Show Notification Function ---
    function showNotification(message, type) {
        console.log('showNotification called with:', message, type); // Debug log
        
        // Remove any existing notifications
        const existingNotifications = document.querySelectorAll('.notification-toast');
        existingNotifications.forEach(notification => notification.remove());
        
        // Create a new notification element
        const notification = document.createElement('div');
        notification.className = 'notification-toast';
        notification.textContent = message;
        
        // Apply styles directly
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
        
        console.log('Created notification element:', notification); // Debug log
        
        // Add to body
        document.body.appendChild(notification);
        
        console.log('Notification added to body'); // Debug log

        // Auto-hide after 4 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 4000);
    }

    // --- 6. Transactions Pie Chart ---
    function buildPieData() {
        const rows = document.querySelectorAll('#transactionBody tr');
        let depositTotal = 0, withdrawalTotal = 0, transferTotal = 0;
        rows.forEach(row => {
            const type = row.querySelector('td span')?.textContent?.trim();
            const amountText = row.children[3]?.textContent || '0';
            const amount = parseFloat((amountText.replace(/[^0-9.\-]/g, '')) || '0');
            if (!type || isNaN(amount)) return;
            if (type === 'Deposit') depositTotal += amount;
            else if (type === 'Withdrawal') withdrawalTotal += Math.abs(amount);
            else if (type === 'Transfer') transferTotal += Math.abs(amount);
        });
        return {
            labels: ['Deposited', 'Withdrawn', 'Transferred'],
            datasets: [{
                data: [depositTotal, withdrawalTotal, transferTotal],
                backgroundColor: ['#16A249', '#F43E5C', '#3281ED'],
                borderColor: '#ffffff',
                borderWidth: 2,
            }]
        };
    }

    function renderPie() {
        if (!pieCanvas) {
            return;
        }

        if (typeof Chart === 'undefined') {
            showStaticChart();
            return;
        }
        
        const ctx = pieCanvas.getContext('2d');
        const data = buildPieData();
        
        // Destroy previous chart if re-rendering
        if (pieCanvas._chartInstance) {
            pieCanvas._chartInstance.destroy();
        }
        
        try {
            const chart = new Chart(ctx, {
                type: 'pie',
                data,
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: (ctx) => {
                                    const label = ctx.label || '';
                                    const value = ctx.parsed || 0;
                                    return `${label}: ${value.toLocaleString()} LKR`;
                                }
                            }
                        }
                    }
                }
            });
            pieCanvas._chartInstance = chart;
        } catch (error) {
            // Fallback to static chart if chart creation fails
            showStaticChart();
        }
    }

    // Render pie once DOM is ready and transactions exist
    if (pieCanvas) {
        // Wait a bit for Chart.js to load
        setTimeout(() => {
            renderPie();
        }, 100);
        
        // Re-render pie when pagination buttons are clicked (data visibility changes)
        const observer = new MutationObserver(() => {
            setTimeout(() => renderPie(), 50);
        });
        const tbody = document.getElementById('transactionBody');
        if (tbody) observer.observe(tbody, { attributes: true, childList: false, subtree: true });
    }

    // Static chart fallback when Chart.js fails to load
    function showStaticChart() {
        const data = buildPieData();
        const total = data.datasets[0].data.reduce((sum, value) => sum + value, 0);
        
        if (total === 0) {
            pieCanvas.parentElement.innerHTML = '<div style="text-align: center; color: #666; padding: 20px;">No transaction data available</div>';
            return;
        }
        
        // Create a simple HTML-based chart
        const chartHtml = `
            <div style="text-align: center; padding: 20px;">
                <h4 style="margin-bottom: 15px; color: #333;">Transaction Breakdown</h4>
                <div style="display: flex; flex-direction: column; gap: 10px; align-items: center;">
                    ${data.labels.map((label, index) => {
                        const value = data.datasets[0].data[index];
                        const percentage = total > 0 ? Math.round((value / total) * 100) : 0;
                        const color = data.datasets[0].backgroundColor[index];
                        return `
                            <div style="display: flex; align-items: center; gap: 10px; width: 200px;">
                                <div style="width: 20px; height: 20px; background-color: ${color}; border-radius: 3px;"></div>
                                <span style="flex: 1; text-align: left; font-size: 14px;">${label}</span>
                                <span style="font-weight: bold; color: #333;">${percentage}%</span>
                            </div>
                        `;
                    }).join('')}
                </div>
                <div style="margin-top: 15px; font-size: 12px; color: #666;">
                    Total Transactions: ${total}
                </div>
            </div>
        `;
        
        pieCanvas.parentElement.innerHTML = chartHtml;
    }

    // If future transactions are appended dynamically, re-render
    const tbody = document.getElementById('transactionBody');
    const tableObserver = new MutationObserver(() => renderPie());
    if (tbody) tableObserver.observe(tbody, { childList: true });

    // --- 7. Loan Application Modal Functions ---
    window.openLoanModal = function() {
        const loanModal = document.getElementById('loanModal');
        if (loanModal) {
            loanModal.style.display = 'block';
            document.body.classList.add('no-scroll');
        }
    };

    window.closeLoanModal = function() {
        const loanModal = document.getElementById('loanModal');
        if (loanModal) {
            loanModal.style.display = 'none';
            document.body.classList.remove('no-scroll');
            // Reset form
            document.getElementById('loanForm').reset();
            document.getElementById('interestRate').value = '';
            document.getElementById('estimatedEMI').textContent = 'LKR 0.00';
        }
    };

    window.updateInterestRate = function() {
        const loanTypeSelect = document.getElementById('loanType');
        const interestRateInput = document.getElementById('interestRate');
        const selectedOption = loanTypeSelect.options[loanTypeSelect.selectedIndex];
        
        if (selectedOption && selectedOption.dataset.rate) {
            interestRateInput.value = selectedOption.dataset.rate;
            calculateEMI();
        } else {
            interestRateInput.value = '';
            document.getElementById('estimatedEMI').textContent = 'LKR 0.00';
        }
    };

    window.calculateEMI = function() {
        const amount = parseFloat(document.getElementById('loanAmount').value) || 0;
        const loanType = document.getElementById('loanType').value;
        const termMonths = parseInt(document.getElementById('termMonths').value) || 0;
        const emiDisplay = document.getElementById('estimatedEMI');

        if (amount > 0 && loanType && termMonths > 0) {
            // Make AJAX call to calculate EMI
            fetch(`/dashboard/calculate-emi?amount=${amount}&loanType=${encodeURIComponent(loanType)}&termMonths=${termMonths}`)
                .then(response => response.text())
                .then(emi => {
                    emiDisplay.textContent = `LKR ${parseFloat(emi).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
                })
                .catch(error => {
                    emiDisplay.textContent = 'LKR 0.00';
                });
        } else {
            emiDisplay.textContent = 'LKR 0.00';
        }
    };

    // Handle loan form validation
    const loanForm = document.getElementById('loanForm');
    if (loanForm) {
        loanForm.addEventListener('submit', function(e) {
            const loanType = document.getElementById('loanType').value;
            const amount = parseFloat(document.getElementById('loanAmount').value);
            const termMonths = parseInt(document.getElementById('termMonths').value);
            const termsChecked = document.getElementById('loanTerms').checked;

            const issues = [];
            if (!loanType) issues.push('Please select a loan type.');
            if (!amount || amount < 10000 || amount > 10000000) {
                issues.push('Loan amount must be between 10,000 and 10,000,000 LKR.');
            }
            if (!termMonths) issues.push('Please select a repayment term.');
            if (!termsChecked) issues.push('You must agree to the loan terms and conditions.');

            if (issues.length > 0) {
                e.preventDefault();
                showNotification(issues[0], 'error');
            }
        });
    }

    // --- 8. Transfer Modal Functions ---
    window.openTransferModal = function() {
        const transferModal = document.getElementById('transferModal');
        if (transferModal) {
            transferModal.style.display = 'block';
            document.body.classList.add('no-scroll');
        }
    };

    window.closeTransferModal = function() {
        const transferModal = document.getElementById('transferModal');
        if (transferModal) {
            transferModal.style.display = 'none';
            document.body.classList.remove('no-scroll');
            // Reset form
            document.getElementById('transferForm').reset();
        }
    };

    // Close transfer modal if user clicks outside of the modal content
    window.onclick = function(event) {
        if (event.target === modal) {
            closeAddModal();
        }
        if (event.target === document.getElementById('loanModal')) {
            closeLoanModal();
        }
        if (event.target === document.getElementById('transferModal')) {
            closeTransferModal();
        }
    };

    // Handle transfer form validation and submission
    const transferForm = document.getElementById('transferForm');
    if (transferForm) {
        transferForm.addEventListener('submit', function(e) {
            const sourceAccount = document.getElementById('sourceAccount').value;
            const targetAccount = document.getElementById('targetAccount').value;
            const amount = parseFloat(document.getElementById('transferAmount').value);
            const description = document.getElementById('transferDescription').value;

            const issues = [];
            if (!sourceAccount) issues.push('Please select a source account.');
            if (!targetAccount) issues.push('Please enter the target account number.');
            if (!amount || amount <= 0) issues.push('Please enter a valid amount.');
            if (sourceAccount === targetAccount) issues.push('Source and target accounts cannot be the same.');
            
            // Check if source account is pending
            const sourceSelect = document.getElementById('sourceAccount');
            const selectedOption = sourceSelect.options[sourceSelect.selectedIndex];
            if (selectedOption && selectedOption.text.includes('[PENDING]')) {
                issues.push('Cannot perform transactions with pending accounts.');
            }
            
            // Check if source account has sufficient balance
            if (selectedOption && selectedOption.text) {
                const balanceMatch = selectedOption.text.match(/LKR\s+([0-9,]+\.?[0-9]*)/);
                if (balanceMatch) {
                    const currentBalance = parseFloat(balanceMatch[1].replace(/,/g, ''));
                    if (amount > currentBalance) {
                        issues.push(`Insufficient balance. Current balance: Rs. ${currentBalance.toLocaleString()}, Required: Rs. ${amount.toLocaleString()}`);
                    }
                }
            }

            if (issues.length > 0) {
                e.preventDefault();
                showNotification(issues[0], 'error');
                return;
            }

            // Let the form submit naturally - no need for AJAX
            // The form will submit to /dashboard/transfer and redirect back with success/error message
        });
    }
});
