# aerie
Standalone version of the Aerie Ecosystem

## API(s)

To view the swagger UI for the API(s) defined, see swagger-ui.html (ex. http://localhost:8080/swagger-ui.html 
if running the service locally)

## Database

To view the H2 database, navigate to /h2-console (ex. http://localhost:8080/h2-console).  The JDBC URL will be 
whatever is configured in the application.yaml file.  The Default username is `sa` with no password.

## Reports

Several reports are available:

### Membership Report

High level report with counts of the number of members for each membership type along with counts of about to expire 
members.  (ex. http://localhost:8080/reports/membership)

### Expiring Report

Listing of members, with contact information, whose membership is about to expire in the next 30 days.  
(ex. http://localhost:8080/reports/expiring)

### Expired Report

Listing of members, with contact information, whose membership is expired.  (ex. http://localhost:8080/reports/expired)
