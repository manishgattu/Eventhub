# Eventhub

## Database connection

The Java backend connects to MySQL using the following environment variables:

- `DB_URL` (optional; defaults to `jdbc:mysql://localhost:3306/event_booking_system`)
- `DB_USER` (required; use a MySQL account with only the permissions the app needs)
- `DB_PASSWORD` (required; set this to the password for the configured MySQL user)

For example, in PowerShell:

```powershell
$env:DB_USER = "eventhub_app"
$env:DB_PASSWORD = "<your-mysql-password>"
mvn dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
mvn package
$classpath = Get-Content -Raw target\classpath.txt
java -cp "target\classes;$classpath" com.Eventhub.util.DBTest
```

Tomcat must inherit these variables when it starts. For a manually started
Tomcat, set them in the PowerShell process that runs `startup.bat`; a Tomcat
Windows service must be configured with the same variables and restarted.

Create the `event_booking_system` database with `database/setup.sql`, then apply
the numbered SQL files in `database/migrations` in ascending order before using
the application.

## Backend API

The servlet API is mounted below `/api`. Register or log in first; responses set
an `HttpOnly`, `SameSite=Lax` session cookie and return a CSRF token. Include the
cookie on later requests and send `X-CSRF-Token: <csrfToken>` on every `POST`,
`PUT`, or `DELETE`. Sessions expire after 30 minutes of inactivity.

| Servlet | Route | Access |
| --- | --- | --- |
| Authentication | `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/me` | Registration is customer-only; logout requires CSRF |
| Users | `/api/users/{id}` | Own user record or admin |
| Events | `/api/events` | Admin |
| Event types | `/api/event-types` | Authenticated read; admin create |
| Service categories | `/api/service-categories` | Authenticated read; admin writes |
| Service providers | `/api/service-providers` | Authenticated read; provider profile owner or admin writes |
| Services | `/api/services` | Authenticated read; provider owner or admin writes |
| Venues | `/api/venues` | Authenticated read; provider owner or admin writes |
| Availability | `/api/availability` | Authenticated read; owner of the linked service/venue or admin writes |
| Bookings | `/api/bookings` | Customer-owned records or admin |
| Addresses | `/api/addresses` | Customer-owned records or admin |
| Reviews | `/api/reviews` | Authenticated read; customer-owned writes or admin |
| Coupons | `/api/coupons` | Authenticated read (customers see active coupons); admin writes |
| Booking items/coupons | `/api/booking-items`, `/api/booking-coupons` | Admin |
| Payments | `/api/payments` | Admin |

Collection endpoints use `GET`; supported resources also accept `GET /{id}`,
`POST` on the collection, `PUT /{id}`, and `DELETE /{id}`. Some operations are
intentionally unavailable where the DAO has no corresponding operation. Filter
collections with `?categoryId=`, `?providerId=`, `?city=`, `?serviceId=`,
`?venueId=`, `?bookingId=`, `?userId=`, or `?code=` where applicable. Get a
default address with `GET /api/addresses?default=true`; admins must also
provide its owner as `userId`. Set an address as default with
`POST /api/addresses/{id}/default` and the CSRF header.

Self-registration always creates a `CUSTOMER`; clients cannot select their
role. Provision the first administrator out-of-band after registering its
account, for example with
`UPDATE users SET role = 'ADMIN' WHERE email = '<admin-email>';` in MySQL. An
administrator may promote an active customer to provider with
`PUT /api/users/{id}/role` and `{"role":"PROVIDER"}`. Create and activate the
provider profile before the provider logs in so the session receives its
provider ID. Never expose administrator bootstrap credentials or SQL access to
clients.

The login and registration pages call these authentication endpoints directly.
Successful authentication establishes the servlet session cookie and redirects
to the dashboard. The dashboard itself still contains demo booking data and is
not yet connected to the bookings API.

New passwords are stored as BCrypt hashes (with a SHA-256 pre-hash so the
existing 12–1024 character password limit remains supported). Existing PBKDF2
password hashes remain verifiable and are upgraded to BCrypt after a successful
login; no schema migration is required.

Customer booking creation records a `PENDING` booking with a zero amount because
this project does not yet calculate prices or integrate a payment processor.
Payments and privileged booking changes are admin-only; this is not a complete
production checkout flow. Serve the application over HTTPS in production so
session cookies are secure.