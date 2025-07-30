
---

## Project Description

This project is a **static educational website** simulating an **e-learning platform**. It is implemented using:

- **HTML5**
- **CSS3**
- **JavaScript (only in Exercise 5)**

The website is developed progressively in **5 structured exercises**, each adding new features and improvements.

---

##  Exercise Breakdown

###  Exercise 1 – HTML Structure & Content

- Includes:
  - `index.html` (Home Page)
  - Categories page (`categories.html`)
  - 2 Subcategory content pages (books + lectures)
  - Full book page (`design-patterns.html`)
  - Full lecture page (`java-introduction.html`)
  - "About Us" page
- Navigation menu, header, footer, and metadata are required
- All content is **static**

---

### Exercise 2 – CSS Styling

- External CSS file for site-wide styling
- Custom styles for:
  - Text (fonts, sizes, colors)
  - Backgrounds and sections
  - Layout box model (padding, margins, borders)
  - Image placement (float)
- Pseudo-classes for links: `:link`, `:visited`, `:hover`, `:active`
- CSS must pass **W3C validation**

---

### 🧱 Exercise 3 – Page Layout with CSS

- Layout using **CSS Grid** (3 columns on desktop)
- Inner content arranged with **Flexbox**
- Uniform layout across all pages
- Embedded web font with `@font-face`
- Layout rules included in the main CSS file with comments

---

### 📱 Exercise 4 – Responsive Design

- Mobile-first single-column layout
- Tablet layout (≥768px): 2 columns  
- Desktop layout (≥1024px): 3 columns
- Use of:
  - `@media` queries
  - `minmax()`, `%`, and `fr` units
  - Responsive images with `<img srcset>` or `<picture>`
- Fully responsive UI tested on different screen sizes

---

### 📝 Exercise 5 – HTML Forms & Validation

- Implements a user registration form:
  - Personal information
  - Account credentials (with password confirmation)
  - Communication preferences
  - Questionnaire (min. 5 questions)
- Uses:
  - Semantic HTML5 elements (`<fieldset>`, `<details>`, `<label>`)
  - Validation with HTML5 attributes and `Constraint Validation API`
  - Autocomplete and `<datalist>` usage
  - Responsive form styling
- External JavaScript file handles custom validation rules

---

## 🛠️ Tools Used

- **HTML5**, **CSS3**
- **JavaScript (Vanilla)**
- Visual Studio Code / Sublime / Brackets
- Chrome Developer Tools
- W3C Validator (HTML & CSS)

---

##  Notes

- The project follows **semantic structure**, **modular CSS**, and **responsive design** principles.
- All exercises are organized in separate folders and linked from the main `index.html`.

---
