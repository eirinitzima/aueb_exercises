$(document).ready(function () {
    // Set the maximum and minimum date for the date of birth field
    $('#validationCustom50').prop('max', new Date().toISOString().split("T")[0]);
    $('#validationCustom50').prop('min', '1920-01-01');

    // Function to validate passwords and update styles
function validatePasswords() {
    const pwd1 = $('#pswdf1');
    const pwd2 = $('#pswdf2');

    // Check if passwords match
    const passwordsMatch = pwd1.val() === pwd2.val();

    // Update validity and styling based on the conditions
    if (!passwordsMatch && pwd2.val().length > 0) {
        pwd1.addClass('is-invalid').removeClass('is-valid');
        pwd2.addClass('is-invalid').removeClass('is-valid');
        pwd2[0].setCustomValidity('Passwords do not match');
    } else {
        pwd1.removeClass('is-invalid').addClass('is-valid');
        pwd2.removeClass('is-invalid').addClass('is-valid');
        pwd2[0].setCustomValidity(''); // Clear the error
    }

    // Check if pwd1 is valid on its own
    if (pwd1.val().length < 8) {
        pwd1.addClass('is-invalid').removeClass('is-valid');
        pwd1[0].setCustomValidity('Password must be at least 8 characters');
    } else {
        pwd1.removeClass('is-invalid').addClass('is-valid');
        pwd1[0].setCustomValidity(''); // Clear the error
    }
}

// Enable/disable and validate confirm password field
$('#pswdf1').on('input', function () {
    const pwd2 = $('#pswdf2');

    if ($(this).val().length > 0) {
        pwd2.prop('disabled', false);
    } else {
        pwd2.val('');
        pwd2.prop('disabled', true);
        pwd2.removeClass('is-valid is-invalid'); // Reset styles for pwd2
    }

    // Validate both passwords
    validatePasswords();
});

// Validate confirm password field on input
$('#pswdf2').on('input', function () {
    validatePasswords();
});


    // Communication method and contact info validation
    const contactInfo = document.getElementById('contactInfo');
    const communicationMethods = document.querySelectorAll('input[name="communicationMethod"]');
    const patterns = {
        SMS: '^\\+?\\d+$',
        Viber: '^\\+?\\d+$',
        WhatsApp: '^\\+?\\d+$',
        Email: ''
    };

    communicationMethods.forEach(method => {
        method.addEventListener('change', () => {
            const selectedMethod = document.querySelector('input[name="communicationMethod"]:checked').value;
            if (selectedMethod === 'Email') {
                contactInfo.setAttribute('type', 'email');
                contactInfo.removeAttribute('pattern');
            } else {
                contactInfo.setAttribute('type', 'text');
                contactInfo.setAttribute('pattern', patterns[selectedMethod]);
            }
            contactInfo.setAttribute('placeholder', `Enter your ${selectedMethod} details`);
            contactInfo.value = ''; // Clear the input field
            validateField($(contactInfo)); // Validate the contact info field
        });
    });

    // Validate fields on input
    $('input, textarea, select').on('input change', function () {
        validateField($(this));
    });

    // General validation function
    function validateField($field) {
        const field = $field[0];
        const feedback = $field.siblings('.feedback');

        if (field.checkValidity()) {
            $field.css('border-color', 'green');
            feedback.text('Looks good!').removeClass('invalid').addClass('valid');
        } else {
            $field.css('border-color', 'red');
            const errorMessage = field.validationMessage || 'Invalid input.';
            feedback.text(errorMessage).removeClass('valid').addClass('invalid');
        }
    }

    // Handle form submission
    $('.needs-validation').on('submit', function (event) {
        event.preventDefault();
        event.stopPropagation();

        const form = $(this);

        // Custom password match validation
        const pwd = $('#pswdf1');
        const pwdVerify = $('#pswdf2');
        if (pwd.val() !== pwdVerify.val()) {
            pwdVerify[0].setCustomValidity('Passwords do not match');
        } else {
            pwdVerify[0].setCustomValidity('');
        }

        // Age validation
        const dobField = $('#validationCustom50');
        const dobValue = new Date(dobField.val());
        const today = new Date();
        const minDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
        if (dobValue > minDate || isNaN(dobValue)) {
            dobField[0].setCustomValidity('You must be at least 18 years old.');
        } else {
            dobField[0].setCustomValidity('');
        }

        // Validate all fields
        form.find('input, textarea, select').each(function () {
            validateField($(this));
        });

        if (form[0].checkValidity()) {
            alert('Form submitted successfully!');
            form[0].reset();
            form.find('input, textarea, select').css('border-color', '').siblings('.feedback').text('');
        } else {
            form.addClass('was-validated');
        }
    });
});
