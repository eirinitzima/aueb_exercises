console.log("Initializing Subcategory Page...");

window.addEventListener('DOMContentLoaded', main);

function main() {
    const itemsContainer = document.querySelector('.items-container');

    createNavBar();
    createFooter(footer[0].link, footer[1].link);
    
    if (!itemsContainer) {
        console.error('Error: .items-container element not found in the DOM.');
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const subcategoryId = params.get('id');

    if (!subcategoryId) {
        itemsContainer.innerHTML = '<p>Error: No subcategory ID provided in the URL.</p>';
        return;
    }

    toggleLoading(true);
    fetchItems(subcategoryId);
}

// Register Handlebars helpers
Handlebars.registerHelper('eq', (a, b, options) => a === b ? options.fn(this) : options.inverse(this));

Handlebars.registerHelper('parseFeatures', (featuresString) => 
    Object.fromEntries(
        (featuresString || "")
            .split(';')
            .map(feature => feature.split(':').map(part => part.trim()))
            .filter(([key, value]) => key && value)
    )
);

function fetchItems(subcategoryId) {
    fetch(`https://learning-hub-1whk.onrender.com/learning-items?subcategory=${subcategoryId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch items for this subcategory');
            }
            return response.json();
        })
        .then(items => {
            if (items.length === 0) {
                document.querySelector('.items-container').innerHTML = '<p>No items found for this subcategory.</p>';
            } else {
                renderItemsWithHandlebars(items);
            }
        })
        .catch(error => {
            console.error('Error fetching items:', error);
            document.querySelector('.items-container').innerHTML = '<p>Error loading items. Please try again later.</p>';
        })
        .finally(() => {
            toggleLoading(false);
        });
}

function renderItemsWithHandlebars(items) {
    const source = document.getElementById('items-template');
    if (!source) {
        console.error('Error: items-template not found in the DOM.');
        return;
    }

    const template = Handlebars.compile(source.innerHTML);

    const html = template({ items });
    const container = document.querySelector('.items-container');
    container.innerHTML = html;
}
