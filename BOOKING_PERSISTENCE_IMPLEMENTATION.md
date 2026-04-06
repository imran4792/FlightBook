# Flight Booking Application - Authentication UI & Booking Persistence Implementation

## Overview
This document outlines the comprehensive implementation of user authentication-based UI and booking data persistence in the Spring Boot flight booking application.

## Implementation Summary

### 1. **User Profile Visibility (Requirement 1)**

#### Header Fragment Created
**File:** `src/main/resources/templates/fragments/header.html`

Features:
- **Reusable component**: Included in all pages using Thymeleaf fragment syntax
- **Conditional visibility**: User profile logo displays only when logged in
- **Profile avatar**: Shows user initials (first letter of first name + first letter of last name)
- **Gradient styling**: Modern blue-to-purple gradient background
- **Responsive design**: Works on desktop, tablet, and mobile devices

**Key Features:**
```html
<!-- Visible when user is logged in -->
<div th:if="${session.loggedInUser != null}" class="profile-section">
    <div class="user-name">
        <span class="name" th:text="${session.firstName} + ' ' + ${session.lastName}"></span>
        <span class="email" th:text="${session.email}"></span>
    </div>
    <div class="user-initials" onclick="toggleUserMenu(event)">
        <span th:text="${session.firstName.charAt(0)} + ${session.lastName.charAt(0)}"></span>
    </div>
    
    <!-- Dropdown menu with options -->
    <div class="user-menu" id="userMenu">
        <a href="/user/dashboard">Dashboard</a>
        <a href="/user/booking-history">Booking History</a>
        <a href="/user/edit-profile">Edit Profile</a>
        <form th:action="@{/logout}" method="POST">
            <button type="submit">Logout</button>
        </form>
    </div>
</div>

<!-- Visible when user is NOT logged in -->
<div th:unless="${session.loggedInUser != null}" class="auth-buttons">
    <a href="/login" class="btn-login">Login</a>
    <a href="/register" class="btn-register">Register</a>
</div>
```

**Updated Templates:**
1. `src/main/resources/templates/index.html` - Included header fragment
2. `src/main/resources/templates/booking/payment.html` - Included header fragment
3. `src/main/resources/templates/booking/booking-success.html` - Included header fragment

### 2. **Booking Data Persistence (Requirement 2)**

#### Booking Entity Extension
**File:** `src/main/java/com/imran/flightbooking/entity/Booking.java`

Added fields:
```java
private String passengerName;    // Name of the passenger
private int age;                 // Age of the passenger
private String flightNumber;     // Flight number for quick reference
private String route;            // Route (source to destination)
```

With corresponding getters and setters for all fields.

#### BookingService Enhancement
**File:** `src/main/java/com/imran/flightbooking/service/BookingService.java`

**Key improvements:**
1. **Transaction Management**: All methods annotated with `@Transactional` for data consistency
2. **Validation**: Comprehensive validation of required fields before saving
3. **Auto-population**: Automatically fetches flight details from database
4. **Date handling**: Sets booking date to current date if not provided
5. **Default status**: Sets booking status to "CONFIRMED" for new bookings

**New Methods:**

```java
@Transactional
public Booking createBooking(Booking booking)
```
- Validates required fields (userId, flightId)
- Fetches flight from database for validation
- Sets booking date if not provided
- Auto-populates flight number and route from Flight entity
- Throws IllegalArgumentException for invalid data

```java
@Transactional
public Booking createBookingFromPassengerDetails(Long userId, Long flightId, 
                                                 String passengerName, int age, 
                                                 double totalPrice)
```
- Convenience method for creating booking from passenger details
- Creates new Booking object with all required data
- Calls createBooking() for centralized validation and processing

```java
@Transactional
public void updateBookingStatus(Long bookingId, String status)
```
- Updates booking status after events (confirmation, cancellation)
- Helps track booking lifecycle

```java
@Transactional
public void cancelBooking(Long id)
```
- Updated to set status to "CANCELLED" instead of deleting
- Maintains booking history in database

#### BookingController Updates
**File:** `src/main/java/com/imran/flightbooking/controller/BookingController.java`

**New Endpoint:**

```java
@PostMapping("/save-booking")
public String saveBooking(
    @RequestParam Long flightId,
    @RequestParam String passengerName,
    @RequestParam int age,
    @RequestParam double totalPrice,
    HttpSession session,
    RedirectAttributes redirectAttributes)
```

**Features:**
- Retrieves logged-in user ID from session
- Validates passenger details (name, age)
- Creates booking record via BookingService
- Stores booking ID in session for confirmation page
- Provides appropriate error messages if booking fails
- Requires user authentication (redirects to login if not authenticated)

#### PaymentController Updates
**File:** `src/main/java/com/imran/flightbooking/controller/PaymentController.java`

**Enhanced `/process-payment` Endpoint:**

```java
@PostMapping("/process-payment")
public String processPayment(
    @RequestParam String cardHolderName,
    @RequestParam String cardNumber,
    @RequestParam String expiryDate,
    @RequestParam String cvv,
    @RequestParam(required = false) Long flightId,
    @RequestParam(required = false) String passengerName,
    @RequestParam(required = false) Integer age,
    @RequestParam(required = false) Double totalPrice,
    HttpSession session,
    RedirectAttributes redirectAttributes)
```

**Workflow:**
1. Processes payment transaction
2. If payment is successful and booking details are provided:
   - Validates user is logged in
   - Creates booking record via BookingService
   - Stores booking ID in session
   - Returns success message
3. If payment fails, redirects to failure page
4. Handles exceptions gracefully with appropriate messages

### 3. **MVC Architecture & Clean Code (Requirement 3)**

#### Architecture Pattern: Controller → Service → Repository → Entity

```
HTTP Request
    ↓
BookingController (receives request parameters)
    ↓
BookingService (business logic, validation, transactions)
    ↓
BookingRepository (database operations)
    ↓
Booking Entity (ORM mapping)
    ↓
Database
```

**Error Handling:**
- Centralized validation in service layer
- Specific exception messages in controller
- Graceful error pages with user-friendly messages
- Logging of errors for debugging

**Transaction Management:**
- All data modifications wrapped in @Transactional
- Ensures ACID properties
- Automatic rollback on exceptions

### 4. **Session Management & Security (Requirement 3)**

#### Session Data Storage
**AuthController** stores after successful login:
```java
session.setAttribute("loggedInUser", user);        // User object
session.setAttribute("userId", user.getUserId()); // User ID
session.setAttribute("firstName", user.getFirstName());  // First name
session.setAttribute("lastName", user.getLastName());    // Last name
session.setAttribute("email", user.getEmail());          // Email
```

#### Session Usage
- **Booking creation**: Uses `session.getAttribute("userId")` to associate booking with user
- **Profile display**: Uses session attributes to display user info in header
- **Authentication check**: Verifies `userId` exists in session before allowing booking

#### Password Security
- Uses BCryptPasswordEncoder for password hashing
- Passwords never stored in session

### 5. **Database Schema**

#### Booking Table Schema
```sql
CREATE TABLE booking (
    booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flight_id BIGINT NOT NULL,
    booking_date VARCHAR(255),
    status VARCHAR(50),           -- CONFIRMED, CANCELLED, PENDING
    total_price DOUBLE,
    passenger_name VARCHAR(255),  -- NEW
    age INT,                       -- NEW
    flight_number VARCHAR(50),    -- NEW
    route VARCHAR(255),           -- NEW
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (flight_id) REFERENCES flight(flight_id)
);
```

## User Flow

### Booking Workflow
1. **User logs in** → Session created with user info
2. **Search & select flight** → Booking data stored in localStorage
3. **Payment page** → Shows user profile logo in header, hidden booking fields
4. **Submit payment** → Payment processed and booking saved to database
5. **Confirmation** → Display booking ID and details
6. **User can view bookings** → Via "My Bookings" link in profile menu

### First-Time Booking
1. User visits payment page (not logged in)
2. Header shows Login/Register buttons
3. User clicks Login → Redirected to login page
4. After login → Redirected back to payment page
5. Profile logo now visible
6. User can complete booking

## Frontend Integration

### Payment Form Hidden Fields
```html
<!-- Hidden fields to pass booking data from localStorage to server -->
<input type="hidden" id="hiddenFlightId" name="flightId" />
<input type="hidden" id="hiddenPassengerName" name="passengerName" />
<input type="hidden" id="hiddenAge" name="age" />
<input type="hidden" id="hiddenTotalPrice" name="totalPrice" />
```

### JavaScript Population
```javascript
function updatePaymentSummary() {
    const summary = JSON.parse(localStorage.getItem('bookingSummary') || '{}');
    const passengerDetails = JSON.parse(localStorage.getItem('passengerDetails') || '[]');
    
    // Populate hidden fields
    document.getElementById('hiddenFlightId').value = summary.flightId || '';
    document.getElementById('hiddenPassengerName').value = 
        passengerDetails[0]?.firstName + ' ' + passengerDetails[0]?.lastName || '';
    document.getElementById('hiddenAge').value = passengerDetails[0]?.age || 0;
    document.getElementById('hiddenTotalPrice').value = total || 0;
}
```

## Error Handling

### Validation Errors
- Empty passenger name → "Passenger name is required"
- Invalid age (≤ 0) → "Invalid age"
- Flight not found → "Flight not found with ID: X"
- User not logged in → "You must be logged in to complete a booking"

### Transaction Errors
- Database connection issues → Transactional rollback, user-friendly error message
- Unique constraint violations → Caught and reported

## Key Security Features

1. **Session-based authentication**: User ID stored in session
2. **Transaction safety**: ACID properties for financial transactions
3. **Input validation**: All parameters validated before processing
4. **SQL injection prevention**: Using JPA/Hibernate ORM
5. **Logout functionality**: Invalidates session on logout
6. **Profile visibility**: Only authenticated users see profile info

## Testing

### Compilation Status
✅ **Build Successful** - 44 source files compiled without errors
- Compilation time: 6.293 seconds
- No warnings or errors

### Manual Testing Steps
1. **Login Test**
   - Register new user
   - Login with credentials
   - Verify profile logo appears in header

2. **Booking Creation Test**
   - Select flight
   - Enter passenger details
   - Proceed to payment
   - Complete payment
   - Verify booking record created in database

3. **Session Persistence Test**
   - Login and navigate to different pages
   - Verify user info persists across pages
   - Logout and verify profile logo disappears

4. **Booking Retrieval Test**
   - Create booking
   - Query database to verify all fields populated
   - Check booking date, status, flight details stored correctly

## Database Queries

### Retrieve user's bookings
```sql
SELECT * FROM booking WHERE user_id = ? ORDER BY booking_date DESC;
```

### Get booking with flight details
```sql
SELECT b.*, f.flight_number, f.source, f.destination 
FROM booking b 
JOIN flight f ON b.flight_id = f.flight_id 
WHERE b.booking_id = ?;
```

### Get confirmed bookings for user
```sql
SELECT * FROM booking 
WHERE user_id = ? AND status = 'CONFIRMED' 
ORDER BY booking_date DESC;
```

## Future Enhancements

1. **Email Notifications**: Send booking confirmation emails
2. **Ticket Generation**: Generate PDF tickets for bookings
3. **Refund Processing**: Handle cancellation and refunds
4. **Loyalty Points**: Track and award loyalty points
5. **Payment Integration**: Integrate with real payment gateways
6. **Multi-passenger Bookings**: Handle group bookings
7. **Seat Management**: Save selected seats with booking
8. **Itinerary Management**: Store complete trip itinerary

## Deployment Checklist

- [x] Code compiles successfully
- [x] All new methods have proper documentation
- [x] Transaction boundaries defined
- [x] Error handling implemented
- [x] Sessions properly managed
- [x] UI components responsive
- [ ] Database migrations created (if needed)
- [ ] Performance testing done
- [ ] Security review completed
- [ ] User acceptance testing scheduled

## Files Modified/Created

### Created Files
1. `src/main/resources/templates/fragments/header.html` - Header component

### Modified Files
1. `src/main/java/com/imran/flightbooking/entity/Booking.java` - Extended entity
2. `src/main/java/com/imran/flightbooking/service/BookingService.java` - Enhanced service
3. `src/main/java/com/imran/flightbooking/controller/BookingController.java` - Added endpoint
4. `src/main/java/com/imran/flightbooking/controller/PaymentController.java` - Enhanced payment
5. `src/main/resources/templates/index.html` - Integrated header
6. `src/main/resources/templates/booking/payment.html` - Integrated header
7. `src/main/resources/templates/booking/booking-success.html` - Integrated header

---

**Implementation Date:** April 2, 2026  
**Framework:** Spring Boot 3.3+  
**Java Version:** 21  
**Build Status:** ✅ SUCCESS
