console.log("Initializing application...");

window.addEventListener('DOMContentLoaded', main);

function main() {
    createNavBar();
    createFooter(footer[0].link,footer[1].link);

    const welcomeSection = document.querySelector('#welcome');
    const welcomeTemplateSource = document.getElementById('welcome-template').innerHTML;
    const welcomeTemplate = Handlebars.compile(welcomeTemplateSource);
    const welcomeHTML = welcomeTemplate(welcome[0]);
    welcomeSection.innerHTML = welcomeHTML;

    toggleLoading(true);

    fetchCategoriesAndSubcategories();
    console.log("Content populated successfully.");
}

// Example usage
function fetchCategoriesAndSubcategories() {
    toggleLoading(true);

    fetch('https://learning-hub-1whk.onrender.com/categories')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch categories');
            }
            return response.json();
        })
        .then(categories => {
            console.log("Categories fetched:", categories);
            populateCategories(categories);
        })
        .catch(error => {
            console.error('Error fetching categories:', error);
            const categoriesContainer = document.querySelector('.category-container');
            categoriesContainer.innerHTML = '<p>Failed to load categories. Please try again later.</p>';
        })
        .finally(() => {
            toggleLoading(false);
        });
}


function populateCategories(categories) {
    const categoriesContainer = document.querySelector('.category-container');
    const categoryTemplateSource = document.getElementById('category-template').innerHTML;
    const categoryTemplate = Handlebars.compile(categoryTemplateSource);

    const categoryData = categories.map(category => ({
        ...category,
        backgroundStyle: `background-image: url('images/category-${category.id}.jpg'); background-size: cover; background-position: center;`,
        subcategories: []
    }));

    const fetchPromises = categoryData.map(category =>
        fetchSubcategories(category.id).then(subcategories => {
            category.subcategories = subcategories.map(subcategory => ({
                ...subcategory,
                backgroundStyle: `background-image: url('images/subcategory-${subcategory.id}.jpg'); background-size: cover; background-position: center; padding: 20px; margin: 10px 0; border-radius: 8px;`
            }));
        })
    );

    Promise.all(fetchPromises).then(() => {
        const categoryHTML = categoryTemplate({ categories: categoryData });
        categoriesContainer.innerHTML = categoryHTML;
    }).catch(error => {
        console.error('Error fetching subcategories:', error);
        categoriesContainer.innerHTML = '<p>Failed to load categories. Please try again later.</p>';
    });
}

function fetchSubcategories(categoryId) {
    return fetch(`https://learning-hub-1whk.onrender.com/categories/${categoryId}/subcategories`)
        .then(response => {
            if (!response.ok) throw new Error(`Failed to fetch subcategories for category ID ${categoryId}`);
            return response.json();
        });
}



