# Smart Campus Operations Hub — Module C
Incident Tickets + Attachments + Technician Updates

## Tech Stack
- **Backend**: Spring Boot 3, Java 17, Spring Data JPA, Spring Security, MySQL, Lombok
- **Frontend**: React 18, axios, react-router-dom, lucide-react, framer-motion, date-fns

## Features
- **Ticket Management**: Create, view, filter, and manage incident tickets.
- **File Attachments**: Upload up to 3 image attachments per ticket.
- **Technician Workflow**: Assign technicians, update status (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED), and add resolution notes.
- **Communication**: Add and view comments on each ticket.
- **Admin Control**: Reject tickets with reason, assign staff, and close tickets.

## Setup Instructions

### Backend
1. Ensure MySQL is running and create a database named `PAF`.
2. Update `backend/src/main/resources/application.properties` with your MySQL credentials.
3. Run the backend:
   ```bash
   cd backend
   ./gradlew bootRun
   ```
4. API will be available at `http://localhost:8080/api`.

### Frontend
1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Start the development server:
   ```bash
   npm start
   ```
3. App will be available at `http://localhost:3000`.

## API Endpoints
- `GET /api/tickets` - List all tickets (with filters)
- `POST /api/tickets` - Create a new ticket
- `GET /api/tickets/{id}` - Get ticket details
- `PATCH /api/tickets/{id}/status` - Update ticket status
- `PATCH /api/tickets/{id}/assign` - Assign technician
- `POST /api/tickets/{id}/attachments` - Upload attachment
- `POST /api/tickets/{id}/comments` - Add comment
- `POST /api/tickets/{id}/resolution-notes` - Add resolution note

## Project Status
Implemented according to IT3030 PAF Assignment 2026 specifications.