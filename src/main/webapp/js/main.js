
"use strict";

// ==========================================
// EVENTHUB - HOMEPAGE INTERACTIONS
// Demo data will later come from the backend.
// ==========================================

const services = [
    {
        id: 1,
        name: "The Grand Palace",
        category: "Venue",
        location: "Madhapur, Hyderabad",
        price: 45000,
        rating: "4.9",
        badge: "Popular choice",
        image: "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 2,
        name: "Royal Feast Catering",
        category: "Catering",
        location: "Kukatpally, Hyderabad",
        price: 650,
        rating: "4.8",
        badge: "Top rated",
        image: "https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 3,
        name: "Moments Photography",
        category: "Photography",
        location: "Gachibowli, Hyderabad",
        price: 18000,
        rating: "4.9",
        badge: "Featured",
        image: "https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 4,
        name: "Rhythm Beats DJ",
        category: "Music",
        location: "Kondapur, Hyderabad",
        price: 12000,
        rating: "4.7",
        badge: "Crowd favourite",
        image: "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 5,
        name: "Petals & Dreams",
        category: "Decoration",
        location: "Banjara Hills, Hyderabad",
        price: 15000,
        rating: "4.8",
        badge: "Trending",
        image: "https://images.unsplash.com/photo-1519225421980-715cb0215aed?auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 6,
        name: "Shubh Muhurtham",
        category: "Priest",
        location: "Secunderabad, Hyderabad",
        price: 5000,
        rating: "4.8",
        badge: "Trusted service",
        image: "https://images.unsplash.com/photo-1606800052052-a08af7148866?auto=format&fit=crop&w=800&q=80"
    }
];

// These values only exist in this browser session.
let activeCategory = "All";
let searchKeyword = "";
let searchLocation = "";
const favourites = new Set();

const serviceGrid = document.getElementById("serviceGrid");
const emptyState = document.getElementById("emptyState");
const filterRow = document.getElementById("filterRow");
const searchForm = document.getElementById("searchForm");
const serviceSearch = document.getElementById("serviceSearch");
const locationSearch = document.getElementById("locationSearch");
const toast = document.getElementById("toast");

let toastTimeout;

// Format prices in Indian Rupees.
function formatPrice(price) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0
    }).format(price);
}

// Display a small notification.
function showToast(message) {
    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(toastTimeout);

    toastTimeout = setTimeout(() => {
        toast.classList.remove("show");
    }, 2800);
}

// Escape text before inserting it into HTML.
function escapeHTML(value) {
    return String(value).replace(/[&<>"']/g, character => {
        const entities = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };

        return entities[character];
    });
}

// Generate one service card.
function createServiceCard(service) {
    const isFavourite = favourites.has(service.id);

    const categoryNames = {
        Venue: "Venue",
        Catering: "Catering",
        Photography: "Photography",
        Music: "DJ & Music",
        Decoration: "Decoration",
        Priest: "Pandit"
    };

    const priceUnit = service.category === "Catering"
        ? "/ person"
        : "onwards";

    return `
        <article class="service-card">
            <div class="service-image-wrap">
                <img
                    class="service-image"
                    src="${escapeHTML(service.image)}"
                    alt="${escapeHTML(service.name)}"
                    loading="lazy"
                >

                <span class="service-badge">
                    ${escapeHTML(service.badge)}
                </span>

                <button
                    class="favorite-btn ${isFavourite ? "favorited" : ""}"
                    type="button"
                    data-favourite="${service.id}"
                    aria-label="${isFavourite ? "Remove" : "Add"} ${escapeHTML(service.name)} ${isFavourite ? "from" : "to"} favourites"
                    aria-pressed="${isFavourite}"
                >
                    ${isFavourite ? "♥" : "♡"}
                </button>
            </div>

            <div class="service-body">
                <div class="service-meta">
                    <span class="service-category">
                        ${escapeHTML(categoryNames[service.category] || service.category)}
                    </span>

                    <span class="service-rating">
                        <span aria-hidden="true">★</span>
                        ${escapeHTML(service.rating)}
                    </span>
                </div>

                <h3 class="service-title">
                    ${escapeHTML(service.name)}
                </h3>

                <p class="service-location">
                    <span aria-hidden="true">⌖</span>
                    ${escapeHTML(service.location)}
                </p>

                <div class="service-bottom">
                    <div>
                        <span class="price-label">Starting from</span>
                        <strong class="service-price">
                            ${formatPrice(service.price)}
                            <small>${priceUnit}</small>
                        </strong>
                    </div>

                    <button
                        class="details-btn"
                        type="button"
                        data-details="${service.id}"
                    >
                        View details →
                    </button>
                </div>
            </div>
        </article>
    `;
}

// Filter the service list using category, keyword and location.
function getFilteredServices() {
    return services.filter(service => {
        const matchesCategory =
            activeCategory === "All" ||
            service.category === activeCategory;

        const searchableText = [
            service.name,
            service.category,
            service.location,
            service.badge
        ].join(" ").toLowerCase();

        const matchesKeyword =
            searchableText.includes(searchKeyword.toLowerCase());

        const matchesLocation =
            service.location.toLowerCase()
                .includes(searchLocation.toLowerCase());

        return matchesCategory && matchesKeyword && matchesLocation;
    });
}

// Render cards on the page.
function renderServices() {
    const filteredServices = getFilteredServices();

    serviceGrid.innerHTML = filteredServices
        .map(createServiceCard)
        .join("");

    emptyState.hidden = filteredServices.length !== 0;

    serviceGrid.hidden = filteredServices.length === 0;
}

// Highlight the selected category filter.
function updateCategoryButtons() {
    document.querySelectorAll("[data-filter]").forEach(button => {
        const isActive = button.dataset.filter === activeCategory;

        button.classList.toggle("active", isActive);

        if (isActive) {
            button.setAttribute("aria-pressed", "true");
        } else {
            button.setAttribute("aria-pressed", "false");
        }
    });

    document.querySelectorAll(".category-card").forEach(button => {
        button.classList.toggle(
            "selected",
            button.dataset.category === activeCategory
        );
    });
}

// Category filter buttons.
filterRow.addEventListener("click", event => {
    const button = event.target.closest("[data-filter]");

    if (!button) return;

    activeCategory = button.dataset.filter;
    updateCategoryButtons();
    renderServices();

    document.getElementById("featured").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
});

// Large category cards.
document.querySelectorAll(".category-card").forEach(button => {
    button.addEventListener("click", () => {
        activeCategory = button.dataset.category;

        updateCategoryButtons();
        renderServices();

        document.getElementById("featured").scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    });
});

// Search form.
searchForm.addEventListener("submit", event => {
    event.preventDefault();

    searchKeyword = serviceSearch.value.trim();
    searchLocation = locationSearch.value.trim();

    renderServices();

    document.getElementById("featured").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

    const count = getFilteredServices().length;

    showToast(
        count === 1
            ? "Found 1 matching service."
            : `Found ${count} matching services.`
    );
});

// Update search results as the user types.
serviceSearch.addEventListener("input", () => {
    searchKeyword = serviceSearch.value.trim();
    renderServices();
});

locationSearch.addEventListener("input", () => {
    searchLocation = locationSearch.value.trim();
    renderServices();
});

// Handle favourite and details buttons.
serviceGrid.addEventListener("click", event => {
    const favouriteButton = event.target.closest("[data-favourite]");
    const detailsButton = event.target.closest("[data-details]");

    if (favouriteButton) {
        const id = Number(favouriteButton.dataset.favourite);
        const service = services.find(item => item.id === id);

        if (favourites.has(id)) {
            favourites.delete(id);
            showToast(`${service.name} removed from favourites.`);
        } else {
            favourites.add(id);
            showToast(`${service.name} added to favourites.`);
        }

        renderServices();
        return;
    }

if (detailsButton) {
    const id = Number(detailsButton.dataset.details);
    const service = services.find(item => item.id === id);

    if (!service) return;

    window.location.href =
        `service-details.html?service=${encodeURIComponent(service.name)}`;
}

});

// Reset all filters.
document.getElementById("resetFilters").addEventListener("click", () => {
    activeCategory = "All";
    searchKeyword = "";
    searchLocation = "";

    serviceSearch.value = "";
    locationSearch.value = "";

    updateCategoryButtons();
    renderServices();

    showToast("All services are now displayed.");
});

// Mobile navigation.
const menuToggle = document.getElementById("menuToggle");
const navLinks = document.getElementById("navLinks");

menuToggle.addEventListener("click", () => {
    const isOpen = navLinks.classList.toggle("open");

    menuToggle.setAttribute("aria-expanded", String(isOpen));
    menuToggle.setAttribute(
        "aria-label",
        isOpen ? "Close navigation" : "Open navigation"
    );
});

navLinks.querySelectorAll("a").forEach(link => {
    link.addEventListener("click", () => {
        navLinks.classList.remove("open");
        menuToggle.setAttribute("aria-expanded", "false");
        menuToggle.setAttribute("aria-label", "Open navigation");
    });
});

// Footer year.
document.getElementById("currentYear").textContent =
    new Date().getFullYear();

// Initial page rendering.
updateCategoryButtons();
renderServices();
