// FAQ Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeFAQ();
});

// Pagination variables
let currentPage = 1;
const itemsPerPage = 10;
let totalItems = 0;
let currentFilter = 'All';

function initializeFAQ() {
    // Initialize category filter
    initializeCategoryFilter();
    
    // Initialize modal
    initializeModal();
    
    // Initialize pagination
    initializePagination();
    
    // Load initial FAQs
    loadFAQs();
}

// Category filter functionality
function initializeCategoryFilter() {
    const categorySelect = document.getElementById('categorySelect');
    
    categorySelect.addEventListener('change', function() {
        const selectedCategory = this.value;
        currentFilter = selectedCategory;
        currentPage = 1; // Reset to first page when filter changes
        if (selectedCategory === 'All') {
            loadFAQs();
        } else {
            loadFAQsByCategory(selectedCategory);
        }
    });
}

// Modal functionality
function initializeModal() {
    const modal = document.getElementById('faqModal');
    const closeBtn = document.getElementById('closeModal');
    
    // Close modal when clicking close button
    closeBtn.addEventListener('click', function() {
        closeModal();
    });
    
    // Close modal when clicking outside
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeModal();
        }
    });
    
    // Close modal with Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && modal.style.display === 'block') {
            closeModal();
        }
    });
}

// Initialize pagination functionality
function initializePagination() {
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    
    prevBtn.addEventListener('click', function() {
        if (currentPage > 1) {
            currentPage--;
            loadCurrentPage();
        }
    });
    
    nextBtn.addEventListener('click', function() {
        const totalPages = Math.ceil(totalItems / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            loadCurrentPage();
        }
    });
}

// Load current page based on filter
function loadCurrentPage() {
    if (currentFilter === 'All') {
        loadFAQs();
    } else {
        loadFAQsByCategory(currentFilter);
    }
}

// Load all FAQs
function loadFAQs() {
    showLoadingState();
    
    // Use the proper API endpoint to get all FAQs
    fetch('/faq/stats')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Get all FAQs by fetching each category
                loadAllFAQsFromCategories();
            } else {
                showErrorState(data.error || 'Failed to load FAQs');
            }
        })
        .catch(error => {
            showErrorState('Failed to load FAQs. Please try again later.');
        });
}

// Load all FAQs from all categories
function loadAllFAQsFromCategories() {
    fetch('/faq/categories')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.categories.length > 0) {
                // Fetch FAQs from all categories
                const promises = data.categories.map(category => 
                    fetch(`/faq/category/${encodeURIComponent(category)}`)
                        .then(response => response.json())
                        .then(categoryData => categoryData.success ? categoryData.faqs : [])
                        .catch(() => [])
                );
                
                Promise.all(promises)
                    .then(results => {
                        // Flatten all FAQs from all categories
                        const allFAQs = results.flat();
                        if (allFAQs.length === 0) {
                            showNoResultsState();
                        } else {
                            displayFAQs(allFAQs);
                        }
                    })
                    .catch(() => {
                        showErrorState('Failed to load FAQs. Please try again later.');
                    });
            } else {
                showNoResultsState();
            }
        })
        .catch(error => {
            showErrorState('Failed to load FAQs. Please try again later.');
        });
}

// Load FAQs by category
function loadFAQsByCategory(category) {
    showLoadingState();
    
    fetch(`/faq/category/${encodeURIComponent(category)}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (data.faqs.length === 0) {
                    showNoResultsState();
                } else {
                    displayFAQs(data.faqs);
                }
            } else {
                showErrorState(data.error || 'Failed to load FAQs');
            }
        })
        .catch(error => {
            showErrorState('Failed to load FAQs. Please try again later.');
        });
}

// Display FAQs
function displayFAQs(faqs) {
    const faqList = document.getElementById('faqList');
    const loadingState = document.getElementById('loadingState');
    const noResultsState = document.getElementById('noResultsState');
    const paginationContainer = document.getElementById('paginationContainer');
    
    // Hide loading and no results states
    loadingState.style.display = 'none';
    noResultsState.style.display = 'none';
    
    if (!faqs || faqs.length === 0) {
        showNoResultsState();
        paginationContainer.style.display = 'none';
        return;
    }
    
    // Update total items count
    totalItems = faqs.length;
    
    // Calculate pagination
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, faqs.length);
    const paginatedFAQs = faqs.slice(startIndex, endIndex);
    
    // Show pagination if there are more items than per page
    if (totalItems > itemsPerPage) {
        paginationContainer.style.display = 'flex';
        updatePagination();
    } else {
        paginationContainer.style.display = 'none';
    }
    
    // Generate FAQ HTML for paginated items
    const faqHTML = paginatedFAQs.map(faq => `
        <div class="faq-item" data-faq-id="${faq.faqId}">
            <div class="faq-header">
                <div class="faq-question">${escapeHtml(faq.question)}</div>
                <button class="faq-toggle" onclick="toggleFAQ(${faq.faqId})">
                    <i class="fa-solid fa-chevron-down"></i>
                </button>
            </div>
            <div class="faq-meta">
                <span class="category-badge">${escapeHtml(faq.category)}</span>
                <span class="date-info">${formatDate(faq.createdAt)}</span>
            </div>
            <div class="faq-answer" id="answer-${faq.faqId}">
                ${escapeHtml(faq.answer)}
            </div>
        </div>
    `).join('');
    
    faqList.innerHTML = faqHTML;
    faqList.style.display = 'block';
}

// Toggle FAQ answer visibility
function toggleFAQ(faqId) {
    const answer = document.getElementById(`answer-${faqId}`);
    const toggle = document.querySelector(`[onclick="toggleFAQ(${faqId})"] i`);
    
    if (answer.classList.contains('show')) {
        answer.classList.remove('show');
        toggle.style.transform = 'rotate(0deg)';
    } else {
        // Close all other open FAQs
        document.querySelectorAll('.faq-answer.show').forEach(openAnswer => {
            openAnswer.classList.remove('show');
        });
        document.querySelectorAll('.faq-toggle i').forEach(icon => {
            icon.style.transform = 'rotate(0deg)';
        });
        
        // Open this FAQ
        answer.classList.add('show');
        toggle.style.transform = 'rotate(180deg)';
    }
}

// Show FAQ in modal
function showFAQModal(faq) {
    const modal = document.getElementById('faqModal');
    const modalQuestion = document.getElementById('modalQuestion');
    const modalAnswer = document.getElementById('modalAnswer');
    const modalCategory = document.getElementById('modalCategory');
    const modalDate = document.getElementById('modalDate');
    
    modalQuestion.textContent = faq.question;
    modalAnswer.textContent = faq.answer;
    modalCategory.textContent = faq.category;
    modalDate.textContent = formatDate(faq.createdAt);
    
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

// Close modal
function closeModal() {
    const modal = document.getElementById('faqModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Show loading state
function showLoadingState() {
    const loadingState = document.getElementById('loadingState');
    const faqList = document.getElementById('faqList');
    const noResultsState = document.getElementById('noResultsState');
    
    loadingState.style.display = 'block';
    faqList.style.display = 'none';
    noResultsState.style.display = 'none';
}

// Show no results state
function showNoResultsState() {
    const noResultsState = document.getElementById('noResultsState');
    const faqList = document.getElementById('faqList');
    const loadingState = document.getElementById('loadingState');
    
    noResultsState.style.display = 'block';
    faqList.style.display = 'none';
    loadingState.style.display = 'none';
}

// Show error state
function showErrorState(message) {
    const faqList = document.getElementById('faqList');
    const loadingState = document.getElementById('loadingState');
    const noResultsState = document.getElementById('noResultsState');
    
    faqList.innerHTML = `
        <div class="error-message">
            <i class="fa-solid fa-exclamation-triangle"></i>
            <span>${escapeHtml(message)}</span>
        </div>
    `;
    faqList.style.display = 'block';
    loadingState.style.display = 'none';
    noResultsState.style.display = 'none';
}

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return 'Unknown date';
    
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } catch (error) {
        return dateString;
    }
}

// Update pagination controls
function updatePagination() {
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    const pageNumbers = document.getElementById('pageNumbers');
    const paginationInfo = document.getElementById('paginationInfo');
    
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage + 1;
    const endIndex = Math.min(currentPage * itemsPerPage, totalItems);
    
    // Update prev/next buttons
    prevBtn.disabled = currentPage === 1;
    nextBtn.disabled = currentPage === totalPages;
    
    // Update pagination info
    paginationInfo.textContent = `Showing ${startIndex}-${endIndex} of ${totalItems} FAQs`;
    
    // Generate page numbers
    pageNumbers.innerHTML = '';
    
    // Show ellipsis and first page if needed
    if (totalPages > 7) {
        if (currentPage > 4) {
            createPageNumber(1);
            if (currentPage > 5) {
                createEllipsis();
            }
        }
        
        // Show pages around current page
        const startPage = Math.max(1, currentPage - 2);
        const endPage = Math.min(totalPages, currentPage + 2);
        
        for (let i = startPage; i <= endPage; i++) {
            createPageNumber(i);
        }
        
        // Show ellipsis and last page if needed
        if (currentPage < totalPages - 3) {
            if (currentPage < totalPages - 4) {
                createEllipsis();
            }
            createPageNumber(totalPages);
        }
    } else {
        // Show all pages if 7 or fewer
        for (let i = 1; i <= totalPages; i++) {
            createPageNumber(i);
        }
    }
}

// Create page number button
function createPageNumber(pageNum) {
    const pageNumbers = document.getElementById('pageNumbers');
    const pageBtn = document.createElement('button');
    pageBtn.className = `page-number ${pageNum === currentPage ? 'active' : ''}`;
    pageBtn.textContent = pageNum;
    pageBtn.addEventListener('click', function() {
        currentPage = pageNum;
        loadCurrentPage();
    });
    pageNumbers.appendChild(pageBtn);
}

// Create ellipsis element
function createEllipsis() {
    const pageNumbers = document.getElementById('pageNumbers');
    const ellipsis = document.createElement('span');
    ellipsis.className = 'page-number ellipsis';
    ellipsis.textContent = '...';
    pageNumbers.appendChild(ellipsis);
}

// Make functions globally available
window.toggleFAQ = toggleFAQ;
window.showFAQModal = showFAQModal;
window.closeModal = closeModal;
