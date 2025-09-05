document.addEventListener('DOMContentLoaded', () => {
    const tabCustomer = document.getElementById('tab-customer');
    const tabStaff = document.getElementById('tab-staff');
    const formCustomer = document.getElementById('form-customer');
    const formStaff = document.getElementById('form-staff');

    tabCustomer.addEventListener('click', () => {
        tabCustomer.classList.add('active');
        tabStaff.classList.remove('active');
        formCustomer.style.display = 'block';
        formStaff.style.display = 'none';
    });

    tabStaff.addEventListener('click', () => {
        tabStaff.classList.add('active');
        tabCustomer.classList.remove('active');
        formStaff.style.display = 'block';
        formCustomer.style.display = 'none';
    });
});
