import React, { useEffect, useState } from 'react';
import { useSession } from './SessionContext';

const CartPage = () => {
    const { session, logOut } = useSession(); 
    const [cartItems, setCartItems] = useState([]);
    const [totalCost, setTotalCost] = useState(0);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    const API_BASE_URL = 'http://localhost:3000';

    const fetchCart = async () => {
        setIsLoading(true);
        try {
            console.log(`Fetching cart for user: ${session.username}`);
            const response = await fetch(
                `${API_BASE_URL}/cart?username=${session.username}&sessionId=${session.sessionId}`
            );
            if (!response.ok) {
                throw new Error('Failed to fetch cart.');
            }
            const data = await response.json();
            console.log('Cart fetched:', data);
            setCartItems(data.cartItems);
            setTotalCost(data.totalCost);
        } catch (err) {
            console.error('Error fetching cart:', err.message);
            setError('Failed to load cart. Please try again later.');
        } finally {
            setIsLoading(false);
        }
    };

    const removeCartItem = async (itemId) => {
        try {
            const response = await fetch(
                `${API_BASE_URL}/cart?username=${session.username}&sessionId=${session.sessionId}&itemId=${itemId.toString()}`,
                { method: 'DELETE' }
            );
            if (!response.ok) {
                throw new Error('Failed to remove item.');
            }
            const data = await response.json();
            setCartItems(data.cartItems); // Update cart items
            setTotalCost(data.totalCost); // Update total cost
        } catch (err) {
            console.error('Error removing item:', err.message);
            setError('Failed to remove item. Please try again.');
        }
    };

    useEffect(() => {
        if (session?.username && session?.sessionId) {
            fetchCart();
        } else {
            setError('You must log in to access the cart.');
            setIsLoading(false);
        }
    }, [session]);

    if (isLoading) {
        return <p>Loading cart...</p>;
    }

    if (error) {
        return <p style={{ color: 'red' }}>{error}</p>;
    }

    return (
        <div>
            <header
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    padding: '10px',
                    color: 'white',
                    backgroundColor: 'transparent',
                }}
            >
                <h1>Shopping Cart</h1>
                {session && (
                    <div>
                        <p>Welcome, {session.username}!</p>
                        <button className="logout-button" onClick={logOut}>
                            Log Out
                        </button>
                    </div>
                )}
            </header>


            <table className="cart-table">
                <thead className='cart-table-header'>
                    <tr>
                        <th>Title</th>
                        <th>Cost</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {cartItems.map((item) => (
                        <tr key={item.id}>
                            <td>{item.title}</td>
                            <td>{item.cost}€</td>
                            <td>
                                <button onClick={() => removeCartItem(item.id)}>Remove</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <h3>Total Cost: {totalCost}€</h3>
        </div>
    );
};

export default CartPage;
