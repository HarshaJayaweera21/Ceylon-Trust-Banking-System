// News Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeNews();
});

// Pagination variables
let currentPage = 1;
const itemsPerPage = 10;
let totalItems = 0;
let currentFilter = 'All';

function initializeNews() {
    // Initialize category filter
    initializeCategoryFilter();
    
    // Initialize modal
    initializeModal();
    
    // Initialize pagination
    initializePagination();
    
    // Load initial news
    loadNews();
}


// Category filter functionality
function initializeCategoryFilter() {
    const categorySelect = document.getElementById('categorySelect');
    
    categorySelect.addEventListener('change', function() {
        const selectedCategory = this.value;
        currentFilter = selectedCategory;
        currentPage = 1; // Reset to first page when filter changes
        if (selectedCategory === 'All') {
            loadNews();
        } else {
            loadNewsByCategory(selectedCategory);
        }
    });
}

// Modal functionality
function initializeModal() {
    const modal = document.getElementById('newsModal');
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
        loadNews();
    } else {
        loadNewsByCategory(currentFilter);
    }
}

// Load all news
function loadNews() {
    showLoadingState();
    
    // Use the proper API endpoint to get all news
    fetch('/news/stats')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Get all news by fetching each category
                loadAllNewsFromCategories();
            } else {
                showErrorState(data.error || 'Failed to load news');
            }
        })
        .catch(error => {
            showErrorState('Failed to load news. Please try again later.');
        });
}

// Load all news from all categories
function loadAllNewsFromCategories() {
    fetch('/news/categories')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.categories.length > 0) {
                // Fetch news from all categories
                const promises = data.categories.map(category => 
                    fetch(`/news/category/${encodeURIComponent(category)}`)
                        .then(response => response.json())
                        .then(categoryData => categoryData.success ? categoryData.news : [])
                        .catch(() => [])
                );
                
                Promise.all(promises)
                    .then(results => {
                        // Flatten all news from all categories
                        const allNews = results.flat();
                        if (allNews.length === 0) {
                            showNoResultsState();
                        } else {
                            // Sort news by posted date (latest first)
                            const sortedNews = allNews.sort((a, b) => {
                                const dateA = new Date(a.postedAt);
                                const dateB = new Date(b.postedAt);
                                return dateB - dateA; // Latest first (descending order)
                            });
                            displayNews(sortedNews);
                        }
                    })
                    .catch(() => {
                        showErrorState('Failed to load news. Please try again later.');
                    });
            } else {
                showNoResultsState();
            }
        })
        .catch(error => {
            showErrorState('Failed to load news. Please try again later.');
        });
}

// Load news by category
function loadNewsByCategory(category) {
    showLoadingState();
    
    fetch(`/news/category/${encodeURIComponent(category)}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (data.news.length === 0) {
                    showNoResultsState();
                } else {
                    displayNews(data.news);
                }
            } else {
                showErrorState(data.error || 'Failed to load news');
            }
        })
        .catch(error => {

            showErrorState('Failed to load news. Please try again later.');
        });
}


// Display news
function displayNews(news) {
    const newsList = document.getElementById('newsList');
    const loadingState = document.getElementById('loadingState');
    const noResultsState = document.getElementById('noResultsState');
    const paginationContainer = document.getElementById('paginationContainer');
    
    // Hide loading and no results states
    loadingState.style.display = 'none';
    noResultsState.style.display = 'none';
    
    if (!news || news.length === 0) {
        showNoResultsState();
        paginationContainer.style.display = 'none';
        return;
    }
    
    // Update total items count
    totalItems = news.length;
    
    // Ensure news is sorted by posted date (latest first)
    const sortedNews = news.sort((a, b) => {
        const dateA = new Date(a.postedAt);
        const dateB = new Date(b.postedAt);
        return dateB - dateA; // Latest first (descending order)
    });
    
    // Calculate pagination
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, news.length);
    const paginatedNews = sortedNews.slice(startIndex, endIndex);
    
    // Show pagination if there are more items than per page
    if (totalItems > itemsPerPage) {
        paginationContainer.style.display = 'flex';
        updatePagination();
    } else {
        paginationContainer.style.display = 'none';
    }
    
    // Generate news HTML for paginated items
    const newsHTML = paginatedNews.map((newsItem, index) => {
        const isRecent = isRecentNews(newsItem.postedAt);
        const latestBadge = index === 0 ? '<span class="latest-badge">Latest</span>' : '';
        const newBadge = isRecent ? '<span class="new-badge">New</span>' : '';
        const cssClass = index === 0 ? 'latest-news' : '';
        
        return `
        <div class="news-item ${cssClass}" data-news-id="${newsItem.newsId}" onclick="showNewsModal(${newsItem.newsId})">
            <div class="news-header">
                <div class="news-title">
                    ${latestBadge}
                    ${newBadge}
                    ${escapeHtml(newsItem.title)}
                </div>
            </div>
            <div class="news-meta">
                <span class="category-badge">${escapeHtml(newsItem.category)}</span>
                <span class="date-info">${formatDate(newsItem.postedAt)}</span>
                <span class="author-info">By ${escapeHtml(newsItem.postedBy)}</span>
            </div>
            <div class="news-content-preview">
                ${escapeHtml(truncateText(newsItem.content, 200))}
            </div>
            <button class="read-more-btn" onclick="event.stopPropagation(); showNewsModal(${newsItem.newsId})">
                <i class="fa-solid fa-eye"></i> Read More
            </button>
        </div>
        `;
    }).join('');
    
    newsList.innerHTML = newsHTML;
    newsList.style.display = 'block';
}

// Show news in modal
function showNewsModal(newsId) {
    fetch(`/news/${newsId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const news = data.news;
                const modal = document.getElementById('newsModal');
                const modalTitle = document.getElementById('modalTitle');
                const modalContent = document.getElementById('modalContent');
                const modalCategory = document.getElementById('modalCategory');
                const modalDate = document.getElementById('modalDate');
                const modalAuthor = document.getElementById('modalAuthor');
                
                modalTitle.textContent = news.title;
                modalContent.textContent = news.content;
                modalCategory.textContent = news.category;
                modalDate.textContent = formatDate(news.postedAt);
                modalAuthor.textContent = `By ${news.postedBy}`;
                
                modal.style.display = 'block';
                document.body.style.overflow = 'hidden';
            } else {
                showNotification('Failed to load news details', 'error');
            }
        })
        .catch(error => {

            showNotification('Failed to load news details', 'error');
        });
}

// Close modal
function closeModal() {
    const modal = document.getElementById('newsModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Show loading state
function showLoadingState() {
    const loadingState = document.getElementById('loadingState');
    const newsList = document.getElementById('newsList');
    const noResultsState = document.getElementById('noResultsState');
    
    loadingState.style.display = 'block';
    newsList.style.display = 'none';
    noResultsState.style.display = 'none';
}

// Show no results state
function showNoResultsState() {
    const noResultsState = document.getElementById('noResultsState');
    const newsList = document.getElementById('newsList');
    const loadingState = document.getElementById('loadingState');
    
    noResultsState.style.display = 'block';
    newsList.style.display = 'none';
    loadingState.style.display = 'none';
}

// Show error state
function showErrorState(message) {
    const newsList = document.getElementById('newsList');
    const loadingState = document.getElementById('loadingState');
    const noResultsState = document.getElementById('noResultsState');
    
    newsList.innerHTML = `
        <div class="error-message">
            <i class="fa-solid fa-exclamation-triangle"></i>
            <span>${escapeHtml(message)}</span>
        </div>
    `;
    newsList.style.display = 'block';
    loadingState.style.display = 'none';
    noResultsState.style.display = 'none';
}

// Show notification
function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    const iconClass = type === 'success' ? 'check-circle' : 'exclamation-triangle';
    notification.innerHTML = `
        <i class="fa-solid fa-${iconClass}"></i>
        <span>${escapeHtml(message)}</span>
    `;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 10px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 10px;
        animation: slideInRight 0.3s ease;
        max-width: 400px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    `;
    
    // Set background color based on type
    if (type === 'success') {
        notification.style.background = 'linear-gradient(135deg, #27ae60, #229954)';
    } else {
        notification.style.background = 'linear-gradient(135deg, #e74c3c, #c0392b)';
    }
    
    // Add to page
    document.body.appendChild(notification);
    
    // Remove after 5 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
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

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function isRecentNews(dateString) {
    if (!dateString) return false;
    
    try {
        const newsDate = new Date(dateString);
        const now = new Date();
        const daysDiff = (now - newsDate) / (1000 * 60 * 60 * 24); // Convert to days
        return daysDiff <= 7; // Consider news as "recent" if posted within last 7 days
    } catch (error) {
        return false;
    }
}

// Add CSS animations and styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    /* Latest news highlighting */
    .news-item.latest-news {
        border-left: 4px solid #ffd700;
        background: linear-gradient(135deg, rgba(255, 215, 0, 0.05), rgba(255, 215, 0, 0.02));
        box-shadow: 0 4px 15px rgba(255, 215, 0, 0.1);
    }
    
    .news-item.latest-news:hover {
        box-shadow: 0 6px 20px rgba(255, 215, 0, 0.15);
        transform: translateY(-2px);
    }
    
    /* Latest badge */
    .latest-badge {
        display: inline-block;
        background: linear-gradient(135deg, #ffd700, #ffed4e);
        color: #1a1a1a;
        font-size: 0.75rem;
        font-weight: 700;
        padding: 4px 8px;
        border-radius: 12px;
        margin-right: 8px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        box-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
        animation: pulse 2s infinite;
    }
    
    /* New badge */
    .new-badge {
        display: inline-block;
        background: linear-gradient(135deg, #22c55e, #16a34a);
        color: white;
        font-size: 0.7rem;
        font-weight: 600;
        padding: 3px 6px;
        border-radius: 8px;
        margin-right: 6px;
        text-transform: uppercase;
        letter-spacing: 0.3px;
        box-shadow: 0 2px 4px rgba(34, 197, 94, 0.3);
    }
    
    /* Pulse animation for latest badge */
    @keyframes pulse {
        0% {
            box-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
        }
        50% {
            box-shadow: 0 4px 8px rgba(255, 215, 0, 0.5);
        }
        100% {
            box-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
        }
    }
    
    /* Enhanced news title styling */
    .news-title {
        position: relative;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 6px;
    }
`;
document.head.appendChild(style);

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
    paginationInfo.textContent = `Showing ${startIndex}-${endIndex} of ${totalItems} News`;
    
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
window.showNewsModal = showNewsModal;
window.closeModal = closeModal;
