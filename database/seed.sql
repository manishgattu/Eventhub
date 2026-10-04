-- Event Booking System sample data
-- Generated to match V001__create_users.sql through V015__create_booking_coupons.sql
-- Run this file only after all 15 tables have been created.

USE event_booking_system;

-- -----------------------------------------------------
-- 1. USERS
-- -----------------------------------------------------
INSERT INTO users
(id, first_name, last_name, email, password_hash, phone, role, status)
VALUES
(1, 'Manish', 'Gattu', 'manish@example.com', '$2a$10$examplehashmanish', '9876543210', 'CUSTOMER', 'ACTIVE'),
(2, 'Rahul', 'Sharma', 'rahul@example.com', '$2a$10$examplehashrahul', '9876543211', 'CUSTOMER', 'ACTIVE'),
(3, 'Priya', 'Reddy', 'priya@example.com', '$2a$10$examplehashpriya', '9876543212', 'CUSTOMER', 'ACTIVE'),
(4, 'Arjun', 'Events', 'arjun.events@example.com', '$2a$10$examplehasharjun', '9876543213', 'PROVIDER', 'ACTIVE'),
(5, 'Sneha', 'Decorators', 'sneha.decor@example.com', '$2a$10$examplehashsneha', '9876543214', 'PROVIDER', 'ACTIVE'),
(6, 'Admin', 'User', 'admin@example.com', '$2a$10$examplehashadmin', '9876543215', 'ADMIN', 'ACTIVE');

-- -----------------------------------------------------
-- 2. EVENT TYPES
-- -----------------------------------------------------
INSERT INTO event_types
(id, name, description)
VALUES
(1, 'Wedding', 'Wedding ceremonies and related celebrations'),
(2, 'Birthday', 'Birthday parties and celebrations'),
(3, 'Corporate', 'Corporate meetings, parties and events'),
(4, 'Conference', 'Conferences, seminars and business events');

-- -----------------------------------------------------
-- 3. SERVICE CATEGORIES
-- -----------------------------------------------------
INSERT INTO service_categories
(id, name, description)
VALUES
(1, 'Photography', 'Professional event photography services'),
(2, 'Catering', 'Food and catering services for events'),
(3, 'Decoration', 'Event decoration and stage decoration'),
(4, 'Music and DJ', 'DJ, music and entertainment services');

-- -----------------------------------------------------
-- 4. SERVICE PROVIDERS
-- -----------------------------------------------------
INSERT INTO service_providers
(id, user_id, business_name, description, phone, email, website, status)
VALUES
(1, 4, 'Arjun Events Photography', 'Professional photography and videography for events',
 '9876500010', 'arjun.events@example.com', 'https://example.com/arjun-events', 'ACTIVE'),
(2, 5, 'Sneha Decorators', 'Wedding and party decoration services',
 '9876500011', 'sneha.decor@example.com', 'https://example.com/sneha-decorators', 'ACTIVE');

-- -----------------------------------------------------
-- 5. SERVICES
-- -----------------------------------------------------
INSERT INTO services
(id, provider_id, category_id, name, description, price, price_type, capacity, status)
VALUES
(1, 1, 1, 'Wedding Photography',
 'Full-day professional wedding photography', 45000.00, 'FIXED', 500, 'ACTIVE'),
(2, 1, 1, 'Event Videography',
 'Professional event video coverage', 30000.00, 'FIXED', 500, 'ACTIVE'),
(3, 2, 3, 'Wedding Decoration',
 'Complete wedding stage and venue decoration', 60000.00, 'FIXED', 1000, 'ACTIVE'),
(4, 2, 3, 'Birthday Decoration',
 'Theme-based birthday party decoration', 15000.00, 'FIXED', 200, 'ACTIVE');

-- -----------------------------------------------------
-- 6. VENUES
-- -----------------------------------------------------
INSERT INTO venues
(id, provider_id, name, description, address_line1, address_line2, city, state,
 postal_code, country, capacity, price, status)
VALUES
(1, 2, 'Grand Celebration Hall',
 'Large indoor venue suitable for weddings and conferences',
 '123 Main Road', 'Near City Center', 'Hyderabad', 'Telangana',
 '500001', 'India', 800, 75000.00, 'AVAILABLE'),
(2, 2, 'Royal Garden Venue',
 'Open garden venue suitable for parties and weddings',
 '45 Lake Road', NULL, 'Hyderabad', 'Telangana',
 '500032', 'India', 500, 50000.00, 'AVAILABLE');

-- -----------------------------------------------------
-- 7. ADDRESSES
-- -----------------------------------------------------
INSERT INTO addresses
(id, user_id, address_type, address_line1, address_line2, city, state,
 postal_code, country, is_default)
VALUES
(1, 1, 'HOME', '12 Green Park', 'Miyapur', 'Hyderabad', 'Telangana',
 '500049', 'India', TRUE),
(2, 2, 'HOME', '24 Lake View Road', NULL, 'Hyderabad', 'Telangana',
 '500081', 'India', TRUE),
(3, 3, 'HOME', '18 Jubilee Hills', NULL, 'Hyderabad', 'Telangana',
 '500033', 'India', TRUE);

-- -----------------------------------------------------
-- 8. EVENTS
-- -----------------------------------------------------
INSERT INTO events
(id, user_id, event_type_id, name, description, event_date, start_time,
 end_time, guest_count, status)
VALUES
(1, 1, 1, 'Manish Wedding',
 'Wedding celebration for family and friends',
 '2026-11-15', '10:00:00', '22:00:00', 400, 'CONFIRMED'),
(2, 2, 2, 'Rahul Birthday Party',
 'Birthday celebration with family and friends',
 '2026-12-05', '18:00:00', '22:00:00', 100, 'PLANNED'),
(3, 3, 3, 'Annual Corporate Meet',
 'Annual company meeting and dinner',
 '2026-11-28', '09:00:00', '18:00:00', 250, 'PLANNED');

-- -----------------------------------------------------
-- 9. AVAILABILITY
-- CHECK CONSTRAINT: exactly one of service_id / venue_id must be non-NULL.
-- -----------------------------------------------------
INSERT INTO availability
(id, service_id, venue_id, available_date, start_time, end_time, status)
VALUES
(1, 1, NULL, '2026-11-15', '10:00:00', '22:00:00', 'BOOKED'),
(2, 2, NULL, '2026-11-15', '10:00:00', '22:00:00', 'BOOKED'),
(3, 3, NULL, '2026-11-15', '10:00:00', '22:00:00', 'BOOKED'),
(4, NULL, 1, '2026-11-15', '10:00:00', '22:00:00', 'BOOKED'),
(5, NULL, 2, '2026-12-05', '18:00:00', '22:00:00', 'AVAILABLE'),
(6, 4, NULL, '2026-12-05', '18:00:00', '22:00:00', 'AVAILABLE');

-- -----------------------------------------------------
-- 10. COUPONS
-- -----------------------------------------------------
INSERT INTO coupons
(id, code, description, discount_type, discount_value, minimum_amount,
 maximum_discount, valid_from, valid_until, usage_limit, used_count, status)
VALUES
(1, 'WELCOME10', '10 percent discount for eligible bookings',
 'PERCENTAGE', 10.00, 10000.00, 5000.00,
 '2026-10-01 00:00:00', '2026-12-31 23:59:59', 100, 0, 'ACTIVE'),
(2, 'EVENT5000', 'Fixed discount on eligible bookings',
 'FIXED', 5000.00, 50000.00, NULL,
 '2026-10-01 00:00:00', '2026-12-31 23:59:59', 50, 0, 'ACTIVE');

-- -----------------------------------------------------
-- 11. BOOKINGS
-- -----------------------------------------------------
INSERT INTO bookings
(id, event_id, user_id, venue_id, booking_date, total_amount, status, notes)
VALUES
(1, 1, 1, 1, '2026-10-03', 180000.00, 'CONFIRMED',
 'Wedding booking including venue, photography and decoration'),
(2, 2, 2, 2, '2026-10-03', 65000.00, 'PENDING',
 'Birthday party venue and decoration'),
(3, 3, 3, 1, '2026-10-03', 105000.00, 'CONFIRMED',
 'Corporate event venue and photography');

-- -----------------------------------------------------
-- 12. BOOKING ITEMS
-- -----------------------------------------------------
INSERT INTO booking_items
(id, booking_id, service_id, quantity, unit_price, total_price)
VALUES
(1, 1, 1, 1, 45000.00, 45000.00),
(2, 1, 3, 1, 60000.00, 60000.00),
(3, 2, 4, 1, 15000.00, 15000.00),
(4, 3, 2, 1, 30000.00, 30000.00);

-- -----------------------------------------------------
-- 13. BOOKING COUPONS
-- -----------------------------------------------------
INSERT INTO booking_coupons
(id, booking_id, coupon_id, discount_amount)
VALUES
(1, 1, 1, 5000.00);

-- -----------------------------------------------------
-- 14. PAYMENTS
-- -----------------------------------------------------
INSERT INTO payments
(id, booking_id, amount, payment_method, transaction_id, status, paid_at)
VALUES
(1, 1, 100000.00, 'UPI', 'TXN-WED-001', 'SUCCESS', '2026-10-03 10:30:00'),
(2, 3, 105000.00, 'CARD', 'TXN-CORP-001', 'SUCCESS', '2026-10-03 11:15:00'),
(3, 2, 10000.00, 'UPI', 'TXN-BDAY-001', 'SUCCESS', '2026-10-03 12:00:00');

-- -----------------------------------------------------
-- 15. REVIEWS
-- -----------------------------------------------------
INSERT INTO reviews
(id, booking_id, user_id, service_id, rating, comment)
VALUES
(1, 1, 1, 1, 5, 'Excellent photography service and professional team.'),
(2, 3, 3, 2, 4, 'Good videography service and timely delivery.');

-- -----------------------------------------------------
-- End of seed data
-- -----------------------------------------------------
