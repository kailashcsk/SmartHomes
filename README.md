# SmartHomes E-commerce Platform - Assignment 4

Github Link: [Your Github Link]

This README provides instructions for setting up and running the SmartHomes e-commerce platform, focusing on the new Customer Service feature implemented in Assignment 4.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven
- Apache Tomcat 9
- Node.js and npm
- MySQL Database
- OpenAI API Key

## Backend Setup

1. Clone the repository to your local machine.

2. Set up OpenAI API Key:
   ```bash
   export OPENAI_API_KEY=your_api_key_here
   ```

3. Navigate to the backend project directory.

4. Build the project using Maven:
   ```bash
   mvn clean package
   ```

5. Deploy the WAR file to Tomcat:
   ```bash
   rm ~/Desktop/tomcat/webapps/smarthomes-backend.war
   cp target/smarthomes-backend.war ~/Desktop/tomcat/webapps/
   ```

6. Create upload directory for images:
   ```bash
   mkdir -p $CATALINA_HOME/webapps/smarthomes-backend/customer_service_images
   chmod 755 $CATALINA_HOME/webapps/smarthomes-backend/customer_service_images
   ```

7. Start/Restart Tomcat:
   ```bash
   $CATALINA_HOME/bin/shutdown.sh
   $CATALINA_HOME/bin/startup.sh
   ```

## Frontend Setup

1. Navigate to the frontend project directory.

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

## Customer Service Feature Implementation

### New Features Added:
1. Customer Service Button in Navigation
2. Ticket Creation for Delivered Orders
3. Image Upload Capability
4. OpenAI Integration for Decision Making
5. Ticket Status Tracking
6. Comprehensive Ticket Management

### Implementation Details:

#### Backend Components:
- `CustomerServiceServlet.java`: Handles ticket creation and management
- `OpenAIService.java`: Integrates with OpenAI for image analysis
- `TicketDAO.java`: Manages ticket data operations
- `FileUploadUtil.java`: Handles image upload functionality

#### Frontend Components:
- `CustomerService.jsx`: Main component for customer service interface
- Image preview functionality
- AI processing animation
- Ticket status lookup
- Comprehensive ticket history

### Database Schema:
```sql
CREATE TABLE tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    user_id INT,
    order_id INT,
    description TEXT NOT NULL,
    image_path VARCHAR(255),
    status ENUM('PENDING', 'IN_REVIEW', 'RESOLVED') DEFAULT 'PENDING',
    decision ENUM('REFUND_ORDER', 'REPLACE_ORDER', 'ESCALATE_TO_HUMAN') NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

## Testing Requirements

The system includes 6 test cases demonstrating different decision scenarios:
1. REFUND_ORDER (2 cases)
   - Complete product damage
   - Severe shipping damage
2. REPLACE_ORDER (2 cases)
   - Minor cosmetic damage
   - Package damage with intact product
3. ESCALATE_TO_HUMAN (2 cases)
   - Unclear technical issues
   - Special circumstances

## Important Notes

- Only delivered orders are eligible for ticket creation
- Image uploads are limited to 10MB
- Supported image formats: JPG, JPEG, PNG
- OpenAI API key must be configured properly
- Image storage path must be accessible and writable

## Running the Application

1. Start the backend server using the Tomcat commands provided above.
2. Start the frontend development server using `npm run dev`.
3. Access the application through the browser at `http://localhost:5173`.

## Troubleshooting

- If image upload fails, check directory permissions
- For OpenAI integration issues, verify API key configuration
- If ticket creation fails, ensure the order is in 'DELIVERED' status
- For image display issues, verify the file paths in the web server

## Testing the Customer Service Feature

1. Login as a customer
2. Place and complete an order (status: DELIVERED)
3. Navigate to Customer Service
4. Create tickets using different test scenarios
5. Verify AI decisions
6. Check ticket status and history

## Additional Information

- Image analysis uses OpenAI's gpt-4o-mini model
- Ticket numbers follow format: CST-YYYYMMDD-XXXXX
- All customer service features require authentication
- The system maintains a complete history of all tickets

