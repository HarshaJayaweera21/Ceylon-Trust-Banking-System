// Loan Application Page JavaScript

const toast = document.getElementById('toast');
const loanModal = document.getElementById('loanModal');
const loanDetailsModal = document.getElementById('loanDetailsModal');

// CSRF Token Helper
function csrfHeaders() {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    return { [header]: token };
}

// Toast Notification
function showToast(message, type = 'success') {
    toast.textContent = message;
    toast.className = 'toast ' + (type === 'error' ? 'error' : 'success');
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.style.display = 'none', 300);
    }, 4000);
}

// Handle URL Parameters for Notifications
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    if (success) {
        showToast(success, 'success');
    } else if (error) {
        showToast(error, 'error');
    }
    
    // Clean URL
    if (success || error) {
        history.replaceState(null, '', window.location.pathname);
    }
});

// Open Loan Application Modal
function openLoanModal() {
    loanModal.style.display = 'flex';
    document.body.classList.add('no-scroll');
    
    // Reset form
    document.getElementById('loanForm').reset();
    document.getElementById('interestRate').value = '';
    document.getElementById('estimatedEMI').textContent = 'LKR 0.00';
}

// Close Loan Application Modal
function closeLoanModal() {
    loanModal.style.display = 'none';
    document.body.classList.remove('no-scroll');
}

// Update Interest Rate based on loan type selection
function updateInterestRate() {
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
}

// Calculate EMI
function calculateEMI() {
    const amount = parseFloat(document.getElementById('loanAmount').value) || 0;
    const rate = parseFloat(document.getElementById('interestRate').value) || 0;
    const term = parseInt(document.getElementById('termMonths').value) || 0;
    
    if (amount > 0 && rate > 0 && term > 0) {
        // EMI Calculation: EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
        // P = Principal amount, R = Monthly interest rate, N = Number of months
        const monthlyRate = rate / 100 / 12;
        const emi = (amount * monthlyRate * Math.pow(1 + monthlyRate, term)) / 
                   (Math.pow(1 + monthlyRate, term) - 1);
        
        document.getElementById('estimatedEMI').textContent = 
            'LKR ' + emi.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    } else {
        document.getElementById('estimatedEMI').textContent = 'LKR 0.00';
    }
}

// Submit Loan Application
function submitLoanApplication() {
    const form = document.getElementById('loanForm');
    const formData = new FormData(form);
    
    // Validate form
    if (!validateLoanForm()) {
        return;
    }
    
    // Show loading state
    const submitBtn = document.querySelector('.btn-process');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Submitting...';
    submitBtn.disabled = true;
    
    fetch('/loan-application/apply', {
        method: 'POST',
        headers: {
            ...csrfHeaders()
        },
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            return response.json().then(data => {
                throw new Error(data.error || 'Failed to submit loan application');
            });
        }
    })
    .then(data => {
        showToast('Loan application submitted successfully! You will be notified about the status.', 'success');
        closeLoanModal();
        
        // Reload page to show updated loan list
        setTimeout(() => {
            window.location.reload();
        }, 2000);
    })
    .catch(error => {
        showToast(error.message || 'Failed to submit loan application. Please try again.', 'error');
    })
    .finally(() => {
        // Reset button state
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    });
}

// Validate Loan Form
function validateLoanForm() {
    const loanType = document.getElementById('loanType').value;
    const amount = document.getElementById('loanAmount').value;
    const term = document.getElementById('termMonths').value;
    const terms = document.getElementById('loanTerms').checked;
    
    if (!loanType) {
        showToast('Please select a loan type.', 'error');
        return false;
    }
    
    if (!amount || amount < 10000 || amount > 10000000) {
        showToast('Please enter a valid amount between 10,000 and 10,000,000 LKR.', 'error');
        return false;
    }
    
    if (!term) {
        showToast('Please select a repayment term.', 'error');
        return false;
    }
    
    if (!terms) {
        showToast('Please agree to the loan terms and conditions.', 'error');
        return false;
    }
    
    return true;
}

// View Loan Details
function viewLoanDetails(button) {
    const loanId = button.getAttribute('data-loan-id');
    
    fetch(`/loan-application/details/${loanId}`)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                showToast(data.error, 'error');
                return;
            }
            
            populateLoanDetails(data.loan);
            loanDetailsModal.style.display = 'flex';
            document.body.classList.add('no-scroll');
        })
        .catch(error => {
            showToast('Failed to load loan details. Please try again.', 'error');
        });
}

// Populate Loan Details Modal
function populateLoanDetails(loan) {
    const content = document.getElementById('loanDetailsContent');
    
    content.innerHTML = `
        <div class="loan-details-grid">
            <div class="detail-section">
                <h4><i class="fa-solid fa-file-contract"></i> Loan Information</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Loan ID:</label>
                        <span>#${loan.loanId}</span>
                    </div>
                    <div class="detail-item">
                        <label>Loan Type:</label>
                        <span>${loan.loanType}</span>
                    </div>
                    <div class="detail-item">
                        <label>Requested Amount:</label>
                        <span>LKR ${parseFloat(loan.amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                    </div>
                    <div class="detail-item">
                        <label>Interest Rate:</label>
                        <span>${loan.interestRate}% p.a.</span>
                    </div>
                    <div class="detail-item">
                        <label>Repayment Term:</label>
                        <span>${loan.termMonths} months</span>
                    </div>
                    <div class="detail-item">
                        <label>Applied Date:</label>
                        <span>${loan.appliedAt}</span>
                    </div>
                </div>
            </div>
            
            <div class="detail-section">
                <h4><i class="fa-solid fa-info-circle"></i> Status Information</h4>
                <div class="status-display">
                    <span class="status-badge ${getStatusClass(loan.status)}">${loan.status}</span>
                    <p class="status-description">${getStatusDescription(loan.status)}</p>
                </div>
            </div>
            
            ${loan.status === 'Approved' ? `
            <div class="detail-section">
                <h4><i class="fa-solid fa-calendar-check"></i> Approval Details</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Approved By:</label>
                        <span class="user-name">${loan.approvedBy && loan.approvedBy !== 'N/A' ? loan.approvedBy : 'N/A'}</span>
                    </div>
                </div>
            </div>
            ` : ''}
            
            ${loan.status === 'Reviewed' ? `
            <div class="detail-section">
                <h4><i class="fa-solid fa-eye"></i> Review Details</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <label>Reviewed By:</label>
                        <span class="user-name">${loan.reviewedBy && loan.reviewedBy !== 'N/A' ? loan.reviewedBy : 'N/A'}</span>
                    </div>
                    ${loan.comments && loan.comments !== 'N/A' ? `
                    <div class="detail-item">
                        <label>Comments:</label>
                        <span class="loan-comments">${loan.comments}</span>
                    </div>
                    ` : ''}
                </div>
            </div>
            ` : ''}
        </div>
    `;
}

// Get Status Class
function getStatusClass(status) {
    switch(status) {
        case 'Pending': return 'status-pending';
        case 'Approved': return 'status-approved';
        case 'Rejected': return 'status-rejected';
        case 'Reviewed': return 'status-reviewed';
        default: return 'status-pending';
    }
}

// Get Status Description
function getStatusDescription(status) {
    switch(status) {
        case 'Pending': return 'Your loan application is currently under review by our loan officers.';
        case 'Approved': return 'Congratulations! Your loan application has been approved.';
        case 'Rejected': return 'Your loan application has been rejected. Please contact customer service for more information.';
        case 'Reviewed': return 'Your loan application has been reviewed and is pending final approval.';
        default: return 'Your loan application status is being processed.';
    }
}

// Close Loan Details Modal
function closeLoanDetailsModal() {
    loanDetailsModal.style.display = 'none';
    document.body.classList.remove('no-scroll');
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === loanModal) {
        closeLoanModal();
    }
    if (event.target === loanDetailsModal) {
        closeLoanDetailsModal();
    }
}

// Close modal with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        if (loanModal.style.display === 'flex') {
            closeLoanModal();
        }
        if (loanDetailsModal.style.display === 'flex') {
            closeLoanDetailsModal();
        }
    }
});

// Initialize page
document.addEventListener('DOMContentLoaded', () => {
    // Add any additional initialization code here
    initializeDocumentUpload();
});

// Document Upload Functionality
let selectedLoanId = null;
let uploadedDocuments = [];

function initializeDocumentUpload() {
    const uploadZone = document.getElementById('uploadZone');
    const fileInput = document.getElementById('fileInput');
    const loanSelect = document.getElementById('loanSelect');
    
    // Check if upload elements exist (they won't exist if customer has no loans)
    if (!uploadZone || !fileInput) {
        console.log('Upload elements not found - customer may not have any loans');
        return;
    }
    
    // Drag and drop functionality
    uploadZone.addEventListener('dragover', handleDragOver);
    uploadZone.addEventListener('dragleave', handleDragLeave);
    uploadZone.addEventListener('drop', handleDrop);
    
    // File input change
    fileInput.addEventListener('change', handleFileSelect);
    
    // Loan selection change (only if loan select exists)
    if (loanSelect) {
        loanSelect.addEventListener('change', handleLoanSelection);
        // Initialize upload zone state
        updateUploadZoneState();
    } else {
        console.log('Loan select not found - customer may not have any loans');
        // Disable upload zone if no loan select
        uploadZone.classList.add('disabled');
        uploadZone.style.opacity = '0.6';
        uploadZone.style.cursor = 'not-allowed';
    }
}

function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    
    const loanSelect = document.getElementById('loanSelect');
    if (loanSelect && loanSelect.value) {
        document.getElementById('uploadZone').classList.add('drag-over');
    }
}

function handleDragLeave(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('uploadZone').classList.remove('drag-over');
}

function handleDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('uploadZone').classList.remove('drag-over');
    
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        processFiles(files);
    }
}

function handleFileSelect(e) {
    const files = e.target.files;
    if (files.length > 0) {
        processFiles(files);
    }
    // Reset input value to allow selecting the same file again
    e.target.value = '';
}

function handleLoanSelection(e) {
    const loanId = e.target.value;
    selectedLoanId = loanId;
    
    // Update upload zone state
    updateUploadZoneState();
    
    // Load documents for selected loan
    if (loanId) {
        loadExistingDocuments(loanId);
    } else {
        // Clear documents list
        const documentsList = document.getElementById('documentsList');
        documentsList.innerHTML = `
            <div class="no-documents">
                <i class="fa-solid fa-file-circle-plus"></i>
                <p>Select a loan to view uploaded documents</p>
                <small>Choose a loan application from the dropdown above</small>
            </div>
        `;
    }
}

function updateUploadZoneState() {
    const uploadZone = document.getElementById('uploadZone');
    const loanSelect = document.getElementById('loanSelect');
    const selectedLoanId = loanSelect.value;
    
    if (selectedLoanId) {
        uploadZone.classList.remove('disabled');
        uploadZone.style.opacity = '1';
        uploadZone.style.cursor = 'pointer';
    } else {
        uploadZone.classList.add('disabled');
        uploadZone.style.opacity = '0.6';
        uploadZone.style.cursor = 'not-allowed';
    }
}

function processFiles(files) {
    // Check if a loan is selected
    const loanSelect = document.getElementById('loanSelect');
    const selectedLoanId = loanSelect.value;
    
    if (!selectedLoanId) {
        showToast('Please select a loan application first before uploading documents.', 'warning');
        return;
    }
    
    // Filter valid files
    const validFiles = Array.from(files).filter(validateFile);
    
    if (validFiles.length === 0) {
        showToast('No valid files to upload.', 'warning');
        return;
    }
    
    // Show loading overlay
    showUploadLoadingOverlay(validFiles.length);
    
    // Upload files sequentially
    uploadFilesSequentially(validFiles, selectedLoanId);
}

async function uploadFilesSequentially(files, loanId) {
    const startTime = Date.now();
    const minimumLoadingTime = 4000; // 4 seconds minimum
    
    let successCount = 0;
    let errorCount = 0;
    
    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const progress = ((i + 1) / files.length) * 100;
        
        // Update progress text
        updateUploadProgress(`Uploading ${i + 1} of ${files.length}: ${file.name}`, progress);
        
        try {
            await uploadSingleFile(file, loanId);
            successCount++;
        } catch (error) {
            console.error('Upload error for file:', file.name, error);
            errorCount++;
        }
    }
    
    // Calculate elapsed time
    const elapsedTime = Date.now() - startTime;
    const remainingTime = Math.max(0, minimumLoadingTime - elapsedTime);
    
    console.log(`Upload completed in ${elapsedTime}ms, ensuring minimum 4s display time`);
    
    // Wait for minimum loading time if needed
    if (remainingTime > 0) {
        updateUploadProgress('Processing documents...', 100);
        await new Promise(resolve => setTimeout(resolve, remainingTime));
    }
    
    // Hide loading overlay
    hideUploadLoadingOverlay();
    
    // Show final result
    if (successCount > 0 && errorCount === 0) {
        showToast(`All ${successCount} document(s) uploaded successfully!`, 'success');
    } else if (successCount > 0 && errorCount > 0) {
        showToast(`${successCount} document(s) uploaded successfully, ${errorCount} failed.`, 'warning');
    } else {
        showToast('Failed to upload documents. Please try again.', 'error');
    }
}

async function uploadSingleFile(file, loanId) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`/loan-application/upload-document/${loanId}`, {
        method: 'POST',
        body: formData,
        headers: {
            'X-CSRF-TOKEN': getCSRFToken()
        }
    });
    
    const result = await response.json();
    
    if (result.success) {
        // Add to uploaded documents list
        addDocumentToList({
            documentId: result.documentId,
            originalFileName: result.fileName,
            fileSize: result.fileSize,
            uploadedAt: result.uploadedAt,
            fileIcon: getFileIcon(file.type)
        });
    } else {
        throw new Error(result.error || 'Upload failed');
    }
}

function validateFile(file) {
    // Check file size (10MB limit)
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
        showToast(`File "${file.name}" is too large. Maximum size is 10MB.`, 'error');
        return false;
    }
    
    // Check file type
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
        showToast(`File "${file.name}" is not supported. Please upload PDF or image files.`, 'error');
        return false;
    }
    
    return true;
}


function addDocumentToList(doc) {
    const documentsList = document.getElementById('documentsList');
    
    // Remove no-documents message if it exists
    const noDocuments = documentsList.querySelector('.no-documents');
    if (noDocuments) {
        noDocuments.remove();
    }
    
    const documentItem = document.createElement('div');
    documentItem.className = 'document-item';
    documentItem.innerHTML = `
        <div class="document-icon">
            <i class="${doc.fileIcon}"></i>
        </div>
        <div class="document-info">
            <div class="document-name">${doc.originalFileName}</div>
            <div class="document-meta">
                <span class="document-size">${doc.fileSize}</span>
                <span class="document-date">${formatDate(doc.uploadedAt)}</span>
            </div>
        </div>
        <div class="document-actions">
            <button class="btn btn-sm btn-info" onclick="downloadDocument(${doc.documentId})" title="Download">
                <i class="fa-solid fa-download"></i>
            </button>
            <button class="btn btn-sm btn-danger" onclick="deleteDocument(${doc.documentId})" title="Delete">
                <i class="fa-solid fa-trash"></i>
            </button>
        </div>
    `;
    
    documentsList.appendChild(documentItem);
}

async function loadExistingDocuments(loanId) {
    if (!loanId) {
        // If no loanId provided, try to get from dropdown
        const loanSelect = document.getElementById('loanSelect');
        loanId = loanSelect.value;
    }
    
    if (!loanId) return;
    
    try {
        const response = await fetch(`/loan-application/documents/${loanId}`);
        const result = await response.json();
        
        if (result.success && result.documents) {
            const documentsList = document.getElementById('documentsList');
            documentsList.innerHTML = '';
            
            if (result.documents.length === 0) {
                documentsList.innerHTML = `
                    <div class="no-documents">
                        <i class="fa-solid fa-file-circle-plus"></i>
                        <p>No documents uploaded yet</p>
                        <small>Upload documents to support your loan application</small>
                    </div>
                `;
            } else {
                result.documents.forEach(doc => {
                    addDocumentToList(doc);
                });
            }
        }
    } catch (error) {
        console.error('Error loading documents:', error);
    }
}

async function downloadDocument(documentId) {
    try {
        const response = await fetch(`/loan-application/download-document/${documentId}`);
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = ''; // Let the server determine the filename
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } else {
            showToast('Failed to download document', 'error');
        }
    } catch (error) {
        console.error('Download error:', error);
        showToast('Failed to download document', 'error');
    }
}

async function deleteDocument(documentId) {
    if (!confirm('Are you sure you want to delete this document?')) {
        return;
    }
    
    try {
        const response = await fetch(`/loan-application/delete-document/${documentId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': getCSRFToken()
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast('Document deleted successfully', 'success');
            // Remove from UI
            const documentItem = document.querySelector(`[onclick="deleteDocument(${documentId})"]`).closest('.document-item');
            if (documentItem) {
                documentItem.remove();
            }
            
            // Show no-documents message if no documents left
            const documentsList = document.getElementById('documentsList');
            if (documentsList.children.length === 0) {
                documentsList.innerHTML = `
                    <div class="no-documents">
                        <i class="fa-solid fa-file-circle-plus"></i>
                        <p>No documents uploaded yet</p>
                        <small>Upload documents to support your loan application</small>
                    </div>
                `;
            }
        } else {
            showToast(`Failed to delete document: ${result.error}`, 'error');
        }
    } catch (error) {
        console.error('Delete error:', error);
        showToast('Failed to delete document', 'error');
    }
}

function getFileIcon(fileType) {
    if (fileType === 'application/pdf') return 'fa-file-pdf';
    if (fileType.startsWith('image/')) return 'fa-file-image';
    return 'fa-file';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

function getCSRFToken() {
    return document.querySelector('meta[name="_csrf"]').getAttribute('content');
}

// Loading Overlay Functions
function showUploadLoadingOverlay(fileCount) {
    const overlay = document.getElementById('uploadLoadingOverlay');
    const progressText = document.getElementById('uploadProgressText');
    const progressFill = document.getElementById('uploadProgressFill');
    
    if (overlay) {
        if (progressText) {
            progressText.textContent = `Preparing to upload ${fileCount} file(s)...`;
        }
        if (progressFill) {
            progressFill.style.width = '0%';
        }
        overlay.style.display = 'flex';
        
        // Prevent body scroll
        document.body.style.overflow = 'hidden';
        
        // Prevent closing by clicking outside (optional)
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    }
}

function hideUploadLoadingOverlay() {
    const overlay = document.getElementById('uploadLoadingOverlay');
    
    if (overlay) {
        overlay.style.display = 'none';
        
        // Restore body scroll
        document.body.style.overflow = '';
    }
}

function updateUploadProgress(text, progress) {
    const progressText = document.getElementById('uploadProgressText');
    const progressFill = document.getElementById('uploadProgressFill');
    
    if (progressText) {
        progressText.textContent = text;
    }
    
    if (progressFill) {
        progressFill.style.width = `${progress}%`;
    }
}
