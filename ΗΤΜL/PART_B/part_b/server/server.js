const express = require('express');
const cors = require('cors');
const uuid = require('uuid');
const app = express();
const port = 3000;

app.use(express.json());

app.use(
    cors({
        origin: 'http://127.0.0.1:5500', 
        methods: ['GET', 'POST', 'DELETE'],
        allowedHeaders: ['Content-Type'],
    })
);
app.use((req, res, next) => {
    console.log('Middleware executed');
    console.log(`Request Method: ${req.method}, Request Path: ${req.path}`);
    console.log('Active Sessions:', JSON.stringify(active_sessions, null, 2));
    next();
});

const active_sessions = {
    user1: {
        sessionId: 'test-session-id',
        cart: [{ id: 1, description: 'Example Book', title: 'Learn Node.js', cost: 50 }],
    },
};

const users = [
    { username: 'user1', password: 'pass1' },
    { username: 'user2', password: 'pass2' },
    { username: 'user3', password: 'pass3' },
];

function validateSession(username, sessionId) {
    return active_sessions[username]?.sessionId === sessionId;
}

app.post('/login', (req, res) => {
    const { username, password } = req.body;
    console.log('Login attempt:', { username, password });

    const user = users.find((u) => u.username === username && u.password === password);

    if (user) {
        const sessionId = uuid.v4(); // Generate a new session ID
        active_sessions[username] = { sessionId, cart: [] };
        console.log('Updated Active Sessions:', active_sessions);
        res.status(200).json({ sessionId });
    } else {
        res.status(401).json({ message: 'Invalid credentials' });
    }
});

app.post('/add-to-cart', (req, res) => {
    const { itemId, description, title, price, username, sessionId } = req.body;

    if (!validateSession(username, sessionId)) {
        console.warn(`Unauthorized access attempt by ${username}.`);
        return res.status(403).json({ message: 'Unauthorized access.' });
    }

    const cart = active_sessions[username]?.cart || [];
    const itemInCart = cart.some((item) => item.id === itemId.toString());

    if (itemInCart) {
        console.warn(`Item with id ${itemId} is already in the cart for user ${username}.`);
        return res.status(400).json({ message: 'Item already in your cart.' });
    }

    cart.push({
        id: itemId.toString(),
        description,
        title,
        cost: parseFloat(price),
    });
    active_sessions[username].cart = cart;

    console.log(`Item added to cart for user ${username}. Updated cart:`, cart);

    res.status(200).json({ message: 'Item added to cart successfully.', cart });
});

app.get('/cart', (req, res) => {
    const { username, sessionId } = req.query;

    if (!validateSession(username, sessionId)) {
        return res.status(403).json({ message: 'Unauthorized access' });
    }

    const cart = active_sessions[username]?.cart || [];
    const totalCost = cart.reduce((sum, item) => sum + parseFloat(item.cost || 0), 0);

    res.status(200).json({ cartItems: cart, totalCost });
});

app.delete('/cart', (req, res) => {
    const { username, sessionId, itemId } = req.query;

    console.log(`Received DELETE request: username=${username}, sessionId=${sessionId}, itemId=${itemId}`);

    if (!validateSession(username, sessionId)) {
        console.warn('Unauthorized access attempt.');
        return res.status(403).json({ message: 'Unauthorized access' });
    }

    const cart = active_sessions[username]?.cart || [];
    const itemIndex = cart.findIndex((item) => item.id === itemId.toString());

    if (itemIndex === -1) {
        console.warn(`Item with id ${itemId} not found in cart.`);
        return res.status(404).json({ message: 'Item not found in cart' });
    }

    cart.splice(itemIndex, 1);
    active_sessions[username].cart = cart;

    const totalCost = cart.reduce((sum, item) => sum + parseFloat(item.cost || 0), 0);
    console.log(`Item removed successfully. Updated cart:`, active_sessions[username].cart);

    res.status(200).json({ cartItems: cart, totalCost });
});


app.listen(port, () => {
    console.log(`Server running on port ${port}`);
});
