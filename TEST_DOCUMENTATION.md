# Test Documentation & Lifecycle Implementation

This document explains how the tests in this project function, their lifecycle, and the specific behaviors they verify.

## Test Types Overview

We have two main categories of tests:

1.  **Unit Tests** (`ReservationServiceImplTest`)
    *   **Does the App Start?** **NO**.
    *   **Speed**: Very Fast (milliseconds).
    *   **Infrastructure**: Uses Mockito to mock dependencies. No database, no Spring context.
    *   **Lifecycle**:
        1.  JUnit 5 creates an instance of the test class.
        2.  `@ExtendWith(MockitoExtension.class)` initializes mocks.
        3.  `@InjectMocks` creates the Service instance and injects the mocks.
        4.  Test method runs.

2.  **Integration/Repository Tests** (`ReservationRepositoryTest`, `ReservationSlotRepositoryTest`)
    *   **Does the App Start?** **YES**.
    *   **Speed**: Slower (seconds to minutes).
    *   **Infrastructure**: Uses **Testcontainers** to spin up a real PostgreSQL Docker container. Bootstraps the full Spring Boot application context (or a slice of it).
    *   **Lifecycle** (Managed by `AbstractIntegrationTest`):
        1.  **Container Startup**: Docker spins up a `postgres:15-alpine` container.
        2.  **Spring Context Load**: The application starts (`LibraryWeek1Application`).
        3.  **Properties Override**: Dynamic properties point the app's DataSource to the Testcontainer's JDBC URL.
        4.  **Schema Creation**: Hibernate creates the database schema (`create-drop`).
        5.  **Test Execution**: Tests run against the real DB.
        6.  **Cleanup**: `@DirtiesContext` ensures the context is refreshed/closed to prevent connection leaks or dirty state between classes.
        7.  **Teardown**: Docker container stops.

---

## 1. Unit Tests: `ReservationServiceImplTest`

These tests verify the **Business Logic** in `ReservationServiceImpl`. They assume the database works (because we mock it) and focus on logical branches (if statements, loops, exceptions).

### Lifecycle for Logical Tests
- **Setup**: Mocks (`ReservationRepository`, `ReservationSlotRepository`, etc.) are reset.
- **Execution**: A method on `ReservationService` is called.
- **Verification**: We assert the return value and verify that specific methods on the mocks were called (e.g., `verify(repository).save(...)`).

### Test Cases

| Test Method | Scenario | What it Tests |
| :--- | :--- | :--- |
| `getFreeSlots_shouldReturnSlots_whenFound` | **Happy Path** | Asking for free slots for a desk returns a list of mapped `SlotsDto`. |
| `getFreeSlots_shouldThrowException...` | **Exception** | If the repository returns empty, a `ResourceNotFoundException` is thrown. |
| `getFreeSlotsRoom_shouldReturnSlots...` | **Happy Path** | Similar to above, but by Room ID. |
| `getActiveReservations_shouldReturn...` | **Happy Path** | Verifies retrieval of active reservations for a room. |
| `cancelReservation_shouldCancel...` | **State Change** | Verifies that calling cancel updates the reservation status to `CANCELLED` and saves it. |
| `makeReservation_shouldCreate...` | **Complex Flow** | Verifies the "golden path" of making a reservation: User validation -> Slot availability check -> Slot locking. |
| `makeReservation_shouldThrow...UserNotFound` | **Validation** | Fails if user ID doesn't exist. |
| `makeReservation_shouldThrow...SlotsNotFound` | **Validation** | Fails if no pessimistic slots are found. |
| `makeReservation_shouldThrow...NotConsecutive` | **Business Rule** | **Crucial Logic**: Checks if the found slots are contiguous (e.g., 10:00-10:30 and 10:30-11:00). If there is a gap, it throws `NotConsecutiveSlotsException`. |

---

## 2. Integration Tests: `ReservationRepositoryTest`

These tests verify that our **JPQL Queries** in `ReservationRepository` interact correctly with the Database.

### Lifecycle
- **@Transactional**: Each test method runs in a transaction that is **rolled back** at the end. This keeps the DB clean for the next test.
- **Setup (@BeforeEach)**: We manually save a User and a Reservation to the real Testcontainer DB.

### Test Cases

| Test Method | Query Tested | What it Tests |
| :--- | :--- | :--- |
| `findByUserId...` | `findByUserId` | Can we fetch reservations joined with slots for a specific user? |
| `findByReservationId...` | `findByReservationId` | Does looking up by ID work? |
| `findByStudentId...` | `findByStudentId` | Can we find reservations by the user's `studentId` (business key)? |
| `findStartedByStudentId...` | `findStartedByStudentId` | Fetches only `CONFIRMED` reservations for a specific student. |
| `findStartedByRoomId...` | `findStartedByRoomId` | Fetches `CONFIRMED` reservations for a whole room (e.g., for display boards). |
| `findPendingByStudentId...` | `findPendingByStudentId` | Fetches `PENDING` reservations (different status filter). |
| `findPendingByRoomId...` | `findPendingByRoomId` | Fetches `PENDING` reservations for a room. |
| `findActiveReservations...` | `findActiveReservations` | Fetches ALL confirmed reservations (e.g., for reporting). |

---

## 3. Integration Tests: `ReservationSlotRepositoryTest`

These tests verify **Data Access** for slots, including time-range queries and existence checks.

### Lifecycle
- **@Transactional**: Rollback after each test.
- **Setup**: Deletes all slots before test to ensure clean state.

### Test Cases

| Test Method | Query Tested | What it Tests |
| :--- | :--- | :--- |
| `existsByDeskIdAndSlotStart...` | `existsBy...` | Verifies the existence check used during slot generation. |
| `deleteBySlotEndBefore...` | `deleteBy...` | Verifies the cleanup job query. Creates old and new slots, calls delete, ensures only new slots remain. |
| `findSlotsByDeskId...` | `findSlotsBy...` | Verifies finding unbooked (`isBooked=false`) slots for a desk. |
| `findFilledSlotsByDeskId...` | `findFilledSlots...` | Verifies finding booked (`isBooked=true`) slots. |
| `findSlotsByRangeAndDeskId...` | `findSlotsByRange...` | **Complex Query**: Verifies finding slots that fall within a specific Start/End time window. |

## How to Run
```bash
# Run All Tests
./mvnw test

# Run Only Unit Tests
./mvnw -Dtest=ReservationServiceImplTest test

# Run Only Integration Tests
./mvnw -Dtest=ReservationRepositoryTest,ReservationSlotRepositoryTest test
```
