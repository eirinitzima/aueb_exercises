# LearningHub Web Application

## Overview

This web application provides a dynamic interface to explore the educational materials offered by the LearningHub API. The system allows users to navigate through categories and subcategories of educational content, view detailed information about books and lectures, and add items to their shopping cart. The application uses Web Browser APIs, including DOM API and Fetch API, to interact with the backend service and provide real-time content updates.

## Features

### PΧ1: Navigation Through Categories and Subcategories 
- **Homepage (`index.html`)** displays a list of categories available in the LearningHub service. Each category includes:
  - Title of the category
  - Image representing the category (linked to a page showing all items in the category)
  - List of subcategories (each subcategory links to a page displaying educational materials specific to it)

- **Category Page (`category.html`)** shows:
  - All learning materials within the selected category (Books or Lectures)
  - For Books: title, author, publisher, publication date, cost, description, image
  - For Lectures: title, cost, description, image
  - The page dynamically loads content based on the category ID passed in the URL.

- **Subcategory Page (`subcategory.html`)** displays:
  - All learning materials within the selected subcategory
  - Detailed list of features for each item (e.g., ISBN, pages, etc.)

### PΧ2: Add Educational Materials to Shopping Cart 
- **User Authentication (Login Form)**: Users must log in to add items to their shopping cart.
  - A login form is displayed in the `category.html` page, accepting the user's `username` and `password`.
  - The login form sends a request to a Login Service (LS) using Fetch API, which authenticates the user and returns a session ID for further interactions.

- **Add Items to Cart**: Users can add educational materials to their shopping cart.
  - Each learning material on the category page has a "Buy" button.
  - Clicking the button sends a request to the Cart Item Service (CIS), which adds the item to the cart.
  - If the item is already in the cart, it will show an appropriate message or update the cart.

### Technical Details

- **Backend API**: The LearningHub API provides access to various categories, subcategories, and learning materials in JSON format. The following endpoints are available:
  - `GET /categories`: Retrieves a list of all categories.
  - `GET /categories/:id/subcategories`: Retrieves subcategories for a specific category.
  - `GET /subcategories`: Retrieves all subcategories.
  - `GET /learning-items?subcategory={id}`: Retrieves learning materials for a given subcategory.
  - `GET /learning-items?category={id}`: Retrieves learning materials for a given category.

- **Frontend**: The frontend uses HTML, CSS, and JavaScript, with Handlebars.js for templating and Fetch API to interact with the backend.

- **Session Management**: Session IDs are generated using the `uuid` library for user authentication. The session is maintained for the duration of the session but is lost upon page refresh.
