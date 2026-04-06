# Flight Booking System - Technical Implementation Reference

## Backend Architecture

### Controller Layer

#### AuthController.java
```java
@GetMapping("/login") - Display login page
@PostMapping("/auth/login") 
    - Parameters: email, password, session
    - Authenticates user
    - Creates session if valid
    - Attributes set:
        * session.setAttribute("userId", user.getUserId());
        * session.setAttribute("firstName", user.getFirstName());
        * session.setAttribute("lastName", user.getLastName());
        * session.setAttribute("email", user.getEmail());
        * session.setAttribute("loggedInUser", user);
    - Returns: redirect:/dashboard

@GetMapping("/logout")
    - Invalidates session
    - Returns: redirect:/

@GetMapping("/register") - Display registration page
@PostMapping("/auth/register") - Register new user
```

#### FlightController.java
```java
@GetMapping("/flight-details")
    - Parameters: flightId, passengers, session
    - Session Check: if (session.getAttribute("userId") == null) return "redirect:/login";
    - Returns: flights/flight-details

@GetMapping("/flight-results")
    - Parameters: source, destination, date, passengers
    - Returns: flights/results
    - No session check needed (read-only)
```

#### UserController.java
```java
@GetMapping("/dashboard")
    - Session Check: Required (userId needed)
    - Loads: User object (from userId)
    - Loads: List<Booking> (from userId)
    - Model: user, bookings, totalBookings
    - Returns: user/dashboard

@GetMapping("/profile")
    - Session Check: Required
    - Loads: User object
    - Loads: totalBookings count
    - Model: user, totalBookings
    - Returns: user/profile

@GetMapping("/edit-profile")
    - Session Check: Required
    - Loads: User object
    - Model: user
    - Returns: user/edit-profile

@GetMapping("/booking-history")
    - Session Check: Required
    - Loads: List<Booking>
    - Model: bookings
    - Returns: user/booking-history
```

### Service Layer

#### BookingService.java
```java
public List<Booking> getUserBookings(Long userId)
    - Calls: bookingRepository.findByUserId(userId)
    - Returns: List<Booking> ordered by date DESC
    - Performance: Single query, no loops
```

#### UserService.java
```java
public User getUserById(Long id)
    - Returns: User object or null
    - Used by: UserController for profile/dashboard

public User authenticateUser(String email, String password)
    - Finds user by email
    - Compares password with encoded stored password
    - Returns: User if valid, null if invalid
```

#### FlightService.java
```java
public Flight getFlightById(Long id)
    - Returns: Flight details with airline info
```

### Repository Layer

#### BookingRepository.java
```java
// NEW QUERY
@Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.bookingDate DESC")
List<Booking> findByUserId(@Param("userId") Long userId);
    - Purpose: Fetch all bookings for a user
    - Order: Newest bookings first
    - Optimized: Single database query

// EXISTING
@Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId")
Long countByUserId(@Param("userId") Long userId);
```

#### UserRepository.java
```java
findByEmail(String email) - Find user by email address
findById(Long id) - Find user by ID
save(User user) - Save or update user
```

#### FlightRepository.java
```java
findById(Long id) - Find flight by ID
searchFlights(source, destination) - Search flights
```

---

## Frontend Architecture

### Thymeleaf Session Access

#### Check if User Logged In
```html
<div th:if="${session.userId != null}">
    <!-- User is logged in -->
</div>

<div th:if="${session.userId == null}">
    <!-- User is NOT logged in -->
</div>
```

#### Display User Information
```html
<span th:text="${session.firstName}">First Name</span>
<span th:text="${session.lastName}">Last Name</span>
<span th:text="${session.email}">Email</span>

<!-- Profile Icon (Initials) -->
<span th:text="${session.firstName?.substring(0,1)} + 
             ${session.lastName?.substring(0,1)}">IK</span>
```

#### Loop Through User Bookings
```html
<tr th:each="booking : ${bookings}">
    <td th:text="${booking.bookingId}">ID</td>
    <td th:text="${booking.bookingDate}">Date</td>
    <td th:text="${booking.status}">Status</td>
    <td th:text="'₹' + ${booking.totalPrice}">Price</td>
</tr>
```

#### Display User Statistics
```html
<div th:text="${totalBookings}">0</div>  <!-- Total Bookings -->
<div th:text="${user.userId}">ID</div>   <!-- User ID -->
```

### JavaScript Functions

#### Profile Dropdown Toggle
```javascript
function toggleProfileDropdown() {
    const dropdown = document.getElementById('profileDropdown');
    dropdown.classList.toggle('show');
}
```

#### Auto-Close Dropdown
```javascript
document.addEventListener('click', function(event) {
    const profileIcon = document.querySelector('.profile-icon');
    const dropdown = document.getElementById('profileDropdown');
    
    if (profileIcon && dropdown && 
        !profileIcon.contains(event.target) && 
        !dropdown.contains(event.target)) {
        dropdown.classList.remove('show');
    }
});
```

### CSS Classes

#### Profile Icon Styles
```css
.profile-icon {
    width: 45px;
    height: 45px;
    border-radius: 50%;
    background: linear-gradient(45deg, #667eea, #764ba2);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 700;
    font-size: 16px;
    cursor: pointer;
}

.profile-dropdown {
    position: absolute;
    top: 60px;
    right: 0;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    width: 200px;
    display: none;
    z-index: 1001;
}

.profile-dropdown.show {
    display: block;
}
```

---

## Request/Response Flow

### Login Flow
```
1. User submits login form
   POST /auth/login
   → email=user@example.com
   → password=password123

2. AuthController.loginUser()
   → Find user by email
   → Compare passwords
   → If valid:
       ✓ Create session
       ✓ Set session attributes
       ✓ Redirect to /dashboard

3. Dashboard Loads
   UserController.dashboardPage()
   → Check session.userId
   → Load user from database
   → Load bookings for user
   → Render user/dashboard.html
```

### Flight Booking Flow
```
1. User on results page
   GET /flight-results
   → Check: Is user logged in?
   → Display: Login link if not logged in
   → Display: Profile icon if logged in

2. User clicks "Select Flight"
   → Navigate to /flight-details?flightId=123&passengers=2

3. FlightController.flightDetailsPage()
   → Check: session.getAttribute("userId") != null
   → If NULL: return "redirect:/login"
   → If VALID: Load flight details, show booking page
```

### Logout Flow
```
1. User clicks "Logout" in dropdown
   GET /logout

2. AuthController.logoutUser()
   → Session invalidated
   → All session attributes cleared
   → Redirect to /

3. Home Page Loads
   → Session is empty
   → Profile icon disappears
   → Login link appears
```

---

## Database Schema Impact

### Existing Tables Used
```sql
-- User table (unchanged)
SELECT * FROM user WHERE user_id = ?;

-- Booking table (unchanged but with new queries)
SELECT b.* FROM booking b 
WHERE b.user_id = ? 
ORDER BY b.booking_date DESC;

-- Flight table (unchanged)
SELECT * FROM flight WHERE flight_id = ?;
```

### JPA Queries Added
```java
// BookingRepository - NEW
@Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.bookingDate DESC")
List<Booking> findByUserId(@Param("userId") Long userId);

// Equivalent SQL:
// SELECT * FROM booking WHERE user_id = ? ORDER BY booking_date DESC;
```

---

## Error Handling

### Session Errors
```java
// If session is null after login
if (user != null) {
    // Create session
    session.setAttribute("userId", user.getUserId());
} else {
    // Show error message
    model.addAttribute("errorMessage", "Invalid credentials");
    return "auth/login";
}
```

### Protected Route Errors
```java
// If accessing /flight-details without login
if (session.getAttribute("userId") == null) {
    return "redirect:/login";  // Redirect, not error
}
```

### No Whitelabel Errors
- All error cases redirected
- No 404s for valid routes
- No 500s from code
- No Thymeleaf parsing errors

---

## Performance Optimization

### Query Optimization
```java
// GOOD: Single query per user
bookings = bookingRepository.findByUserId(userId);
// ~ 1 database query

// BAD (NOT USED): Loop through all bookings
for (Booking b : allBookings) {
    if (b.getUserId() == userId) { ... }
}
// ~ N database queries + filtering in app
```

### Session Optimization
```java
// Session stored in memory (HttpSession)
// Lookup time: O(1)
// Memory per session: ~1KB

session.getAttribute("userId")  // Fast lookup
```

### Lazy Loading Issues
- Spring JPA open-in-view enabled
- Database queries allowed during view rendering
- Performance impact: Minimal for this use case

---

## Security Considerations

### Session Security
```java
// Session is server-side only
// No sensitive data in cookies
// Session ID is random and secure

// Each request includes JSESSIONID cookie
// Server validates session on each request
// Session invalidation clears all data
```

### Authentication Security
```java
// Passwords encoded before storage
passwordEncoder.encode(password)

// Login verification
if (userService.authenticateUser(email, password) != null) {
    // Credentials valid
}
```

### Authorization Security
```java
// Users can only see their own bookings
bookings = bookingRepository.findByUserId(session.userId)

// Profile protected
if (session.userId == null) return "redirect:/login"
```

### Potential Vulnerabilities (Not Addressed)
- CSRF attacks (requires CSRF tokens on forms)
- Session fixation (requires session rotation on login)
- Clickjacking (requires X-Frame-Options headers)
- SQL injection (JPA protects, but monitor queries)

---

## Deployment Checklist

- [x] Build: `mvn clean package -DskipTests`
- [x] JAR created: `target/flightbooking-1.0.jar`
- [x] Port 8080 available
- [x] MySQL running on localhost:3306
- [x] Database created: flight_booking_db
- [x] All JPA repositories initialized
- [x] Tomcat started successfully
- [x] No errors in startup logs
- [x] Application accessible on http://localhost:8080

---

## API Endpoints Reference

### Authentication
| Endpoint | Method | Session | Purpose |
|----------|--------|---------|---------|
| /login | GET | No | Display login form |
| /auth/login | POST | Create | Submit login |
| /logout | GET | Delete | Clear session |
| /register | GET | No | Display registration form |
| /auth/register | POST | No | Submit registration |

### Flight Management
| Endpoint | Method | Session | Purpose |
|----------|--------|---------|---------|
| /search-flights | GET | No | Search form |
| /flight-results | GET | No | Display results |
| /flight-details | GET | Required | Show details |

### User Management
| Endpoint | Method | Session | Purpose |
|----------|--------|---------|---------|
| /profile | GET | Required | User profile page |
| /dashboard | GET | Required | User dashboard |
| /booking-history | GET | Required | Booking history |
| /edit-profile | GET | Required | Edit profile form |

---

## Troubleshooting Guide

### Issue: Profile Icon Not Showing
- Solution: Check `session.userId` is set
- Verify: User logged in successfully
- Check: Browser cookies enabled for session ID

### Issue: Dropdown Menu Not Opening
- Solution: Check JavaScript `toggleProfileDropdown()` is called
- Verify: Console for JavaScript errors
- Check: CSS `.profile-dropdown.show` is defined

### Issue: Redirected to Login After Every Click
- Solution: Session may have expired (default 30 mins)
- Verify: Server time sync correct
- Check: session timeout in application.properties

### Issue: Booking Query Returns Empty List
- Solution: User ID may not match booking records
- Verify: User ID in session matches database
- Check: Bookings exist for user in database

### Issue: Build Fails
- Solution: Check Java version (need Java 21+)
- Verify: Maven installed and configured
- Check: Dependencies download without errors

---

## Code Snippets

### Complete Login Controller Method
```java
@PostMapping("/auth/login")
public String loginUser(@RequestParam String email,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes,
                       Model model) {
    try {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPassword = password.trim();

        User user = userService.authenticateUser(normalizedEmail, normalizedPassword);
        if (user != null) {
            // Store user in session
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("firstName", user.getFirstName());
            session.setAttribute("lastName", user.getLastName());
            session.setAttribute("email", user.getEmail());
            
            // Redirect to dashboard
            redirectAttributes.addFlashAttribute("successMessage", "Login Successful");
            return "redirect:/dashboard";
        } else {
            // Invalid credentials
            model.addAttribute("errorMessage", "Wrong credentials, please try again");
            return "auth/login";
        }
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Wrong credentials, please try again");
        return "auth/login";
    }
}
```

### Complete Flight Details Protection
```java
@GetMapping("/flight-details")
public String flightDetailsPage(@RequestParam Long flightId, 
                               @RequestParam(required = false) String passengers, 
                               HttpSession session,
                               Model model) {
    // Check if user is logged in
    if (session.getAttribute("userId") == null) {
        return "redirect:/login";
    }
    
    Flight flight = flightService.getFlightById(flightId);
    if (flight != null) {
        model.addAttribute("flight", flight);
    }
    model.addAttribute("passengers", passengers);
    return "flights/flight-details";
}
```

This implementation provides a solid foundation for a secure, scalable flight booking system with proper session management and user authentication.
