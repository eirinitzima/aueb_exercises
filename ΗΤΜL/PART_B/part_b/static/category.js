console.log('Initializing Category Page...');

window.addEventListener('DOMContentLoaded', main);

function main() {
    createNavBar();
    createFooter();

    const container = document.getElementById('category-items');
    const categoryId = new URLSearchParams(window.location.search).get('id');

    if (!categoryId) {
        container.innerHTML = '<p>Error: No category ID provided in the URL.</p>';
        return;
    }

    toggleLoading(true);
    fetchCategoryItems(categoryId, container);

    document.getElementById('login-form').addEventListener('submit', logIn);
    document.addEventListener('click', (event) => {
        if (event.target.classList.contains('buyButton')) {
            addToCart(event);
        }
    });
}

function getSessionData() {
    const username = localStorage.getItem('username');
    const sessionId = localStorage.getItem('sessionId');
    return { username, sessionId };
}

function fetchCategoryItems(categoryId, container) {
    fetch(`https://learning-hub-1whk.onrender.com/learning-items?category=${categoryId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch data for category ID: ${categoryId}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.length === 0) {
                container.innerHTML = '<p>No items found for this category.</p>';
            } else {
                renderCategoryItems(data, container);
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
            container.innerHTML = '<p>Error loading category data. Please try again later.</p>';
        })
        .finally(() => {
            toggleLoading(false);
        });
}

function renderCategoryItems(items, container) {
    const baseURL = "https://learning-hub-1whk.onrender.com/";

    const updatedItems = items.map(item => ({
        ...item,
        fullImagePath: item.image && item.image.trim()
            ? `${baseURL}${item.image}`
            : 'https://via.placeholder.com/300'
    }));

    const source = document.getElementById('category-items-template').innerHTML;
    const template = Handlebars.compile(source);

    const html = template({ items: updatedItems });
    container.innerHTML = html;
}


async function logIn(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    console.log('Login attempt:', { username, password });

    try {
        const response = await fetch('http://localhost:3000/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });

        if (response.ok) {
            const { sessionId } = await response.json();
            localStorage.setItem('username', username);
            localStorage.setItem('sessionId', sessionId);

            // Hide login form and display success message
            const loginForm = document.getElementById('login-form');
            loginForm.style.display = 'none'; // Hide the form

            const message = document.getElementById('login-message');
            showMessage(message,`Welcome, ${username}! You have successfully logged in.`,'rgb(1, 94, 1)',undefined)

        } else {
            alert('Invalid credentials. Please try again.');
        }
    } catch (error) {
        console.error('Login error:', error.message);
        alert('An error occurred. Please try again.');
    }
}

async function addToCart(event) {
    event.preventDefault();

    const base = event.target; // Button clicked
    const item = event.target.closest('.item'); // Parent item container

    // Extract item details from the DOM
    const itemId = base.dataset.id;
    const title = item.querySelector('h3').textContent.trim();
    const description = item.querySelector('p').textContent.trim();
    const price = item.querySelector('p + p').textContent.trim().replace('Price: ', '').replace('€', '').trim();
    const sessionId = localStorage.getItem('sessionId');
    const username = localStorage.getItem('username');
    const message = item.querySelector('#buy-message');

    // Validate session
    if (!sessionId || !username) {
        showMessage(message, 'Please login to purchase the training material.', 'rgb(135, 4, 4)', 2000);
        return;
    }

    // Prepare payload for the request
    const payload = {
        itemId,
        description,
        title,
        price: parseFloat(price), // Ensure price is sent as a number
        username,
        sessionId,
    };

    const cartURL = 'http://localhost:3000/add-to-cart';

    try {
        const response = await fetch(cartURL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });

        const data = await response.json();

        if (response.ok) {
            showMessage(message, data.message, 'rgb(2, 39, 2)', 3000); // Success message
        } else {
            showMessage(message, data.message, 'rgb(135, 4, 4)', 3000); // Error message
        }
    } catch (error) {
        console.error('Error adding to cart:', error.message);
        showMessage(message, 'Error: Unable to add item to cart', 'rgb(135, 4, 4)', 3000);
    }
}

// Helper function to show messages
function showMessage(element, text, color, duration) {
    element.style.display = 'block';
    element.textContent = text;
    element.style.color = color;

    if(!(duration == undefined)){
        setTimeout(() => {
            element.style.display = 'none';
        }, duration);
    }
}

