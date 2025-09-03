document.addEventListener('DOMContentLoaded', () => {
    const heroText = document.querySelector('.hero h1');
    const heroSubtext = document.querySelector('.hero p');
    let opacity = 0;

    function fadeIn() {
        opacity += 0.05;
        heroText.style.opacity = opacity;
        heroSubtext.style.opacity = opacity;
        if (opacity < 1) requestAnimationFrame(fadeIn);
    }

    fadeIn();

    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'scale(1.05)';
        });
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'scale(1)';
        });
    });
});
