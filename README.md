# StayEase - Hotel Housekeeping Management System

Java 17 + Spring Boot 3.2 + Spring Data JPA + MySQL + REST + plain HTML/CSS/JS frontend (served by Spring Boot).

## Run
1. Install JDK 17, Maven and MySQL. Start MySQL.
2. Open `src/main/resources/application.properties` and put your MySQL password. The database `hotel_housekeeping` is created automatically.
3. In this folder run `mvn spring-boot:run` (or open the folder in IntelliJ / Eclipse / VS Code and run `HousekeepingApplication`).
4. Open http://localhost:8080 . Login: `admin@stayease.com / admin123` or `supervisor@stayease.com / super123`.
On first start 12 sample rooms (all READY) and 3 housekeepers are added by `config/DataSeeder`.

## Demo flow (UI)
Rooms -> **Mark Dirty** on a READY room -> Cleaning Tasks (already assigned) -> **Start** -> **Complete** -> Inspections -> **Inspect** (Pass) -> room READY -> Bookings -> New Booking.
Try a Fail inspection (remarks required) and try to book a DIRTY room through the normal REST API to see the error messages.

## Layers
Controller (REST + @Valid) -> Service (all business rules, @Transactional) -> Repository (Spring Data JPA) -> MySQL

| Class | Job |
|---|---|
| `RoomStatusManager` | The only place with the lifecycle map READY->DIRTY->CLEANING->INSPECTED->READY. Rejects everything else before saving. |
| `RoomService.markDirty` | status -> DIRTY, pick housekeeper, create task. All in one transaction: if nobody is free, everything rolls back. |
| `CleaningTaskService` | start (task IN_PROGRESS, room CLEANING) and complete (task COMPLETED). |
| `InspectionService` | Task must be COMPLETED. PASS: room -> INSPECTED -> READY. FAIL: remarks required, new task for re-cleaning. |
| `BookingService` | Rejects booking if room is not READY (409 ROOM_NOT_READY) or dates overlap. Checkout makes the room DIRTY. |
| `HousekeeperService` | Auto-assign = AVAILABLE housekeeper with fewest active tasks. BUSY at 3 active tasks. |
| `ReportService` | Workload, turnaround (average of inspection PASSED time - dirty time), dashboard summary. |
| `AuditService` | Saves old/new values in `audit_logs`; user name comes from the `X-Performed-By` header. |
| `GlobalExceptionHandler` | `@RestControllerAdvice` turning exceptions into `{ "error": "...", "message": "..." }`. |

## Design decisions
- "Failed inspection" is stored as `Inspection.result = FAILED`; the room simply stays CLEANING (your 4-status enum is kept).
- DTOs are Java `record`s (no boilerplate). Entities are never returned by the API.
- Booking DELETE = cancel (status CANCELLED). Room/Housekeeper DELETE is rejected if they have related data.
- A room becomes READY -> DIRTY by `POST /api/rooms/{id}/dirty` or by booking checkout.
- Login is a simple demo check (users in application.properties). Not real security.




