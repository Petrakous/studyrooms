# StudyRooms Docker Demo

The docker profile ships with a small demo dataset so you can explore the app quickly without touching the default H2 dev setup.

## One-command start
```bash
docker compose up --build
```

- Entry point: http://localhost (via Nginx proxy)
- API base: http://localhost/api
- Swagger UI: http://localhost/swagger-ui.html
- SPA: http://localhost/spa/

## Demo credentials
- **Staff:** `staff` / `staff123`
- **Student:** `student` / `student123`

## Quick tour
1. **Student UI**
   - Browse to http://localhost and login as `student` / `student123`.
   - View available study spaces and create a reservation.
   - Open "My Reservations" to confirm it appears.
2. **Staff UI**
   - Login as `staff` / `staff123`.
   - Manage spaces or close a space for a given day (bulk cancellation) and observe notifications.
   - Check occupancy stats for a space and date range.
3. **Swagger / API**
   - Open http://localhost/swagger-ui.html and authenticate via `/api/auth/login`.
   - Use the JWT to call protected endpoints such as `/api/spaces` or `/api/reservations`.
   - For a quick public call, try the weather lookup: `/api/weather?lat=37.9838&lon=23.7275` (Athens)
4. **SPA**
   - Visit http://localhost/spa/ and login as a student.
   - List spaces, create or cancel a reservation, and verify errors are displayed from the API.
   - Log in as staff to manage spaces, review all reservations, and load occupancy stats inside the SPA.
5. **Optional consumer service**
   - Enable with `docker compose --profile with-consumer up --build` to see the consumer call the API using JWT.

## Notes
- Demo seeding runs only when `demo.seed.enabled=true` (set by default in the docker profile). It is idempotent and safe to re-run.
- The default dev/H2 profile remains untouched; run `./mvnw spring-boot:run` to use it without demo seed data.
