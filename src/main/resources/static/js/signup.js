// src/main/resources/static/js/signup.js

function isEmail(v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
}

function isNIC(v) {
    return /^[0-9]{9}[Vv]$/.test(v) || /^[0-9]{12}$/.test(v);
}

function showError(id, msg) {
    const el = document.getElementById('err-' + id);
    if (el) {
        el.textContent = msg;
        el.classList.add('show');
    }
    
    // Add error styling to input
    const input = document.getElementById(id);
    if (input) {
        input.classList.add('error-state');
    }
    
    // Special handling for phone type buttons
    if (id === 'phoneType') {
        const phoneInput = document.querySelector('.phone-input');
        if (phoneInput) phoneInput.classList.add('error-state');
    }
}

function clearErrors(fields) {
    fields.forEach(f => {
        const el = document.getElementById('err-' + f);
        if (el) {
            el.textContent = '';
            el.classList.remove('show');
        }
        
        // Remove error styling from input
        const input = document.getElementById(f);
        if (input) {
            input.classList.remove('error-state');
        }
    });
    
    // Clear phone input error styling
    const phoneInput = document.querySelector('.phone-input');
    if (phoneInput) phoneInput.classList.remove('error-state');
}

function formatPhone(e) {
    let v = e.target.value.replace(/\D/g, '').slice(0, 9);
    e.target.value = v; // Remove formatting, just allow 9 digits
}

function loadStepData(stepNumber) {
    const data = JSON.parse(localStorage.getItem(`step${stepNumber}`) || '{}');
    return data;
}

function populateStep1Data(data) {
    if (data.firstName) document.getElementById('firstName').value = data.firstName;
    if (data.lastName) document.getElementById('lastName').value = data.lastName;
    if (data.email) document.getElementById('email').value = data.email;
    if (data.phone) document.getElementById('phone').value = data.phone;
    if (data.phoneType) {
        document.getElementById('phoneType').value = data.phoneType;
        // Activate the corresponding button
        const buttons = document.querySelectorAll('.phone-type-btn');
        buttons.forEach(btn => {
            if (btn.dataset.type === data.phoneType) {
                btn.classList.add('active');
            }
        });
    }
    if (data.nic) document.getElementById('nic').value = data.nic;
}

function populateStep2Data(data) {
    if (data.street) document.getElementById('street').value = data.street;
    if (data.street2) document.getElementById('street2').value = data.street2;
    if (data.city) document.getElementById('city').value = data.city;
    if (data.postalCode) document.getElementById('postalCode').value = data.postalCode;
    if (data.dob) {
        document.getElementById('dob').value = data.dob;
        // Convert DD/MM/YYYY to YYYY-MM-DD for date picker
        const [day, month, year] = data.dob.split('/');
        if (day && month && year) {
            document.getElementById('dobPicker').value = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
        }
    }
}

function populateStep3Data(data) {
    if (data.username) document.getElementById('username').value = data.username;
    if (data.password) document.getElementById('password').value = data.password;
    if (data.confirmPassword) document.getElementById('confirmPassword').value = data.confirmPassword;
    if (data.securityQuestion1) document.getElementById('secQ1').value = data.securityQuestion1;
    if (data.securityAnswer1) document.getElementById('ans1').value = data.securityAnswer1;
    if (data.securityQuestion2) document.getElementById('secQ2').value = data.securityQuestion2;
    if (data.securityAnswer2) document.getElementById('ans2').value = data.securityAnswer2;
}

function formatDOB(e) {
    let v = e.target.value.replace(/\D/g, '').slice(0, 8);
    if (v.length > 4) e.target.value = v.slice(0, 2) + '/' + v.slice(2, 4) + '/' + v.slice(4);
    else if (v.length > 2) e.target.value = v.slice(0, 2) + '/' + v.slice(2);
    else e.target.value = v;
}

document.addEventListener('DOMContentLoaded', () => {
    // read CSRF token and header
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // Password toggle functionality
    function setupPasswordToggle(toggleId, inputId) {
        const toggle = document.getElementById(toggleId);
        const input = document.getElementById(inputId);
        
        if (toggle && input) {
            let isPressed = false;
            
            toggle.addEventListener('mousedown', () => {
                isPressed = true;
                input.type = 'text';
                toggle.innerHTML = '<i class="fas fa-eye-slash"></i>';
            });
            
            toggle.addEventListener('mouseup', () => {
                if (isPressed) {
                    isPressed = false;
                    input.type = 'password';
                    toggle.innerHTML = '<i class="fas fa-eye"></i>';
                }
            });
            
            toggle.addEventListener('mouseleave', () => {
                if (isPressed) {
                    isPressed = false;
                    input.type = 'password';
                    toggle.innerHTML = '<i class="fas fa-eye"></i>';
                }
            });
            
            // Prevent context menu on right click
            toggle.addEventListener('contextmenu', (e) => {
                e.preventDefault();
            });
        }
    }

    // Step navigation functionality
    function setupStepNavigation() {
        const stepButtons = document.querySelectorAll('.step-btn');
        
        stepButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const step = btn.dataset.step;
                
                // Check if user has completed previous steps
                if (step === '2') {
                    const step1Data = loadStepData(1);
                    if (Object.keys(step1Data).length === 0) {
                        alert('Please complete Step 1 first');
                        return;
                    }
                } else if (step === '3') {
                    const step1Data = loadStepData(1);
                    const step2Data = loadStepData(2);
                    if (Object.keys(step1Data).length === 0 || Object.keys(step2Data).length === 0) {
                        alert('Please complete previous steps first');
                        return;
                    }
                }
                
                // Navigate to the selected step
                if (step === '1') {
                    window.location.href = '/signup';
                } else if (step === '2') {
                    window.location.href = '/signup-step2';
                } else if (step === '3') {
                    window.location.href = '/signup-step3';
                }
            });
        });
    }

    // Success popup functionality
    function showSuccessPopup() {
        const modal = document.getElementById('successModal');
        if (modal) {
            modal.style.display = 'flex';
            
            // Redirect to login page after 3 seconds
            setTimeout(() => {
                window.location.href = '/login';
            }, 3000);
        }
    }

    // Setup step navigation for all pages
    setupStepNavigation();

    // STEP 1
    const form1 = document.getElementById('form-step1');
    if (form1) {
        document.getElementById('phone')?.addEventListener('input', formatPhone);
        document.getElementById('nic').placeholder = 'XXXXXXXXXV';

        // Load data from Step 1 when coming back
        const step1Data = loadStepData(1);
        populateStep1Data(step1Data);

        // Phone type button functionality
        const phoneTypeButtons = document.querySelectorAll('.phone-type-btn');
        const phoneTypeInput = document.getElementById('phoneType');
        
        phoneTypeButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                // Remove active class from all buttons
                phoneTypeButtons.forEach(b => b.classList.remove('active'));
                // Add active class to clicked button
                btn.classList.add('active');
                // Set the hidden input value
                phoneTypeInput.value = btn.dataset.type;
                // Clear any error state
                clearErrors(['phoneType']);
            });
        });

        document.getElementById('reset1')?.addEventListener('click', () => {
            // Reset form
            form1.reset();
            // Reset phone type buttons
            phoneTypeButtons.forEach(btn => btn.classList.remove('active'));
            phoneTypeInput.value = '';
            // Clear all errors
            clearErrors(['firstName','lastName','email','phone','phoneType','nic']);
            // Clear localStorage
            localStorage.removeItem('step1');
        });

        document.getElementById('next1')?.addEventListener('click', () => {
            clearErrors(['firstName','lastName','email','phone','phoneType','nic']);
            let valid = true;

            const fn = document.getElementById('firstName')?.value.trim() || '';
            const ln = document.getElementById('lastName')?.value.trim() || '';
            const em = document.getElementById('email')?.value.trim() || '';
            const ph = document.getElementById('phone')?.value.replace(/\s/g, '') || '';
            const type = document.getElementById('phoneType')?.value || '';
            const nic = document.getElementById('nic')?.value.trim() || '';

            if (!fn) { showError('firstName','First name is required'); valid = false; }
            if (!ln) { showError('lastName','Last name is required'); valid = false; }
            if (!em) { showError('email','Email is required'); valid = false; }
            else if (!isEmail(em)) { showError('email','Please enter a valid email address'); valid = false; }
            if (!ph) { showError('phone','Phone number is required'); valid = false; }
            else if (!/^\d{9}$/.test(ph)) { showError('phone','Phone number must be exactly 9 digits'); valid = false; }
            if (!type) { showError('phoneType','Please select a phone type'); valid = false; }
            if (!nic) { showError('nic','NIC is required'); valid = false; }
            else if (!isNIC(nic)) { showError('nic','Please enter a valid NIC (9 digits + V or 12 digits)'); valid = false; }

            if (!valid) return;
            localStorage.setItem('step1', JSON.stringify({ firstName: fn, lastName: ln, email: em, phone: ph, phoneType: type, nic }));
            window.location.href = '/signup-step2';
        });
    }

    // STEP 2
    const form2 = document.getElementById('form-step2');
    if (form2) {
        document.getElementById('dob')?.addEventListener('input', formatDOB);

        // Load data from Step 2 when coming back
        const step2Data = loadStepData(2);
        if (Object.keys(step2Data).length > 0) {
            populateStep2Data(step2Data);
        }

        document.getElementById('reset2')?.addEventListener('click', () => {
            // Reset form
            form2.reset();
            // Clear all errors
            clearErrors(['street','city','postalCode','dob']);
            // Clear localStorage
            localStorage.removeItem('step2');
        });
        document.getElementById('prev2')?.addEventListener('click', () => {
            window.location.href = '/signup';
        });
        document.getElementById('next2')?.addEventListener('click', () => {
            clearErrors(['street','city','postalCode','dob']);
            let valid = true;

            const street = document.getElementById('street')?.value.trim() || '';
            const city = document.getElementById('city')?.value.trim() || '';
            const postal = document.getElementById('postalCode')?.value.trim() || '';
            const dob = document.getElementById('dob')?.value.trim() || '';

            if (!street) { showError('street','Address line 1 is required'); valid = false; }
            if (!city) { showError('city','City is required'); valid = false; }
            if (!postal) { showError('postalCode','Postal code is required'); valid = false; }
            if (!dob) { showError('dob','Date of birth is required'); valid = false; }
            else if (!/^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/\d{4}$/.test(dob)) { showError('dob','Please use DD/MM/YYYY format'); valid = false; }

            if (!valid) return;
            localStorage.setItem('step2', JSON.stringify({
                street,
                street2: document.getElementById('street2')?.value.trim() || '',
                city,
                postalCode: postal,
                dob
            }));
            window.location.href = '/signup-step3';
        });
    }

    // STEP 3
    const form3 = document.getElementById('form-step3');
    if (form3) {
        // Load data from Step 3 when coming back
        const step3Data = loadStepData(3);
        if (Object.keys(step3Data).length > 0) {
            populateStep3Data(step3Data);
        }

        // Setup password toggles
        setupPasswordToggle('togglePassword', 'password');
        setupPasswordToggle('toggleConfirmPassword', 'confirmPassword');

        const secQ1 = document.getElementById('secQ1');
        const secQ2 = document.getElementById('secQ2');
        if (secQ1 && secQ2) {
            secQ1.addEventListener('change', () => {
                Array.from(secQ2.options).forEach(o => o.disabled = (o.text === secQ1.value));
            });
            secQ2.addEventListener('change', () => {
                Array.from(secQ1.options).forEach(o => o.disabled = (o.text === secQ2.value));
            });
        }

        document.getElementById('reset3')?.addEventListener('click', () => {
            // Reset form
            form3.reset();
            // Reset security question dropdowns
            if (secQ1 && secQ2) {
                Array.from(secQ1.options).forEach(o => o.disabled = false);
                Array.from(secQ2.options).forEach(o => o.disabled = false);
            }
            // Clear all errors
            clearErrors(['username','password','confirmPassword','secQ1','secQ2']);
            // Clear localStorage
            localStorage.removeItem('step3');
        });
        document.getElementById('prev3')?.addEventListener('click', () => {
            window.location.href = '/signup-step2';
        });
        document.getElementById('finish3')?.addEventListener('click', () => {
            clearErrors(['username','password','confirmPassword','secQ1','secQ2']);
            let valid = true;

            const user = document.getElementById('username')?.value.trim() || '';
            const pass = document.getElementById('password')?.value || '';
            const confirmPass = document.getElementById('confirmPassword')?.value || '';
            const q1 = secQ1?.value || '';
            const a1 = document.getElementById('ans1')?.value.trim() || '';
            const q2 = secQ2?.value || '';
            const a2 = document.getElementById('ans2')?.value.trim() || '';

            const passRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#\$%\^&\*()_\-+=]).{8,20}$/;

            if (!user) { showError('username','Username is required'); valid = false; }
            if (!pass) { showError('password','Password is required'); valid = false; }
            else if (!passRegex.test(pass)) { showError('password','Password must be 8-20 characters with uppercase, lowercase, digit, and special character'); valid = false; }
            if (!confirmPass) { showError('confirmPassword','Please confirm your password'); valid = false; }
            else if (pass !== confirmPass) { showError('confirmPassword','Passwords do not match'); valid = false; }
            if (!q1) { showError('secQ1','Please select security question 1'); valid = false; }
            if (!a1) { showError('secQ1','Please answer security question 1'); valid = false; }
            if (!q2) { showError('secQ2','Please select security question 2'); valid = false; }
            if (!a2) { showError('secQ2','Please answer security question 2'); valid = false; }
            
            if (!valid) return;

            // Save Step 3 data
            localStorage.setItem('step3', JSON.stringify({
                username: user,
                password: pass,
                confirmPassword: confirmPass,
                securityQuestion1: q1,
                securityAnswer1: a1,
                securityQuestion2: q2,
                securityAnswer2: a2
            }));

            const data = {
                ...JSON.parse(localStorage.getItem('step1') || '{}'),
                ...JSON.parse(localStorage.getItem('step2') || '{}'),
                username: user,
                password: pass,
                securityQuestion1: q1,
                securityAnswer1: a1,
                securityQuestion2: q2,
                securityAnswer2: a2
            };

            fetch('/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(data)
            })
                .then(res => {
                    if (res.ok) {
                        // Show success popup
                        showSuccessPopup();
                        // Clear localStorage
                        localStorage.clear();
                    } else {
                        res.text().then(msg => alert('Registration failed: ' + msg));
                    }
                })
                .catch(err => {
                    console.error('Registration error', err);
                    alert('Registration error: ' + err);
                });
        });
    }
});
