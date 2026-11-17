# CommunityApp

A Java-based community commerce web application built with Spring MVC, MyBatis, and MySQL. The platform supports customer and merchant workflows, product management, Stripe test payments, order tracking, and a customer loyalty points system.

## Features

### Customer Features
- Customer registration and login
- Browse shops and products
- View product details
- Place orders and pay through Stripe Checkout in test mode
- View order history and payment results
- Track loyalty points and transaction history

### Merchant Features
- Merchant dashboard
- Shop and product management
- Product category management
- View and manage customer orders
- Update order fulfillment status

### Payments and Loyalty Points
- Stripe Checkout integration using test mode
- Webhook handling for payment confirmation
- Idempotent webhook processing to prevent duplicate payment handling
- Order status updates after successful payment
- Customer loyalty points and transaction ledger

## Technology Stack

| Layer | Technologies |
|---|---|
| Backend | Java, Spring MVC, Spring Framework |
| Persistence | MyBatis, MySQL |
| Frontend | HTML, CSS, JavaScript, Zepto.js |
| Build Tool | Maven |
| Server | Apache Tomcat 9 |
| Payment Integration | Stripe Checkout and Webhooks |
| Testing | JUnit, Spring Test, isolated MySQL test database |

## Database

The application uses MySQL with 15 tables covering accounts, shops, products, orders, payments, and loyalty points.

A structure-only database schema is provided at:

`database/schema.sql`

Import it into a newly created database. Use a dedicated database rather than an existing database containing important records.

## Local Setup

1. Install a compatible Java JDK, Maven, MySQL, and Apache Tomcat 9.
2. Create a MySQL database named `community_app`.
3. Import `database/schema.sql` into the new database.
4. Copy `src/main/resources/jdbc.example.properties` to `src/main/resources/jdbc.properties`.
5. Configure the local MySQL username and password.
6. Configure Stripe test credentials and webhook settings through the application's local environment configuration.
7. Build the Maven project and deploy the resulting web application to Tomcat.

## Testing

The project includes DAO and service tests using JUnit and Spring Test.

A separate `community_app_test` database can be used to isolate test data from application data. Spring-managed test transactions support automatic rollback.

Some legacy integration tests require additional fixtures and modernization; the complete test suite is not currently guaranteed to pass.

## Security Notes

- Database credentials and Stripe secrets are excluded from version control.
- The repository includes a sample JDBC configuration without real credentials.
- Stripe payment functionality is intended for development and demonstration using test mode.
- Do not use real payment credentials or production customer data when running the project locally.

## Project Purpose

CommunityApp is a portfolio project demonstrating Java web development, relational database integration, commerce workflows, third-party payment integration, and transactional backend processing.