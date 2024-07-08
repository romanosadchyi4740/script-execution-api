# GraalJS Wrapper API

This project provides a REST API wrapper around the GraalVM JavaScript interpreter. It allows you to execute JavaScript scripts in blocking and non-blocking modes, query the status of executed scripts, and manage script execution.

## Prerequisites

- Java 11
- Maven

## Building the Project
To build the project, navigate to the project root directory and run:
mvn clean install

## Running the Application
To run the application, use the following command:
java -jar target/graal-wrapper-0.0.1-SNAPSHOT.jar

## API Endpoints
### Execute a Script
- URL: /api/scripts/execute
- Method: POST
- Request Body: JavaScript code as plain text
- Query Parameters: blocking (optional, default false)
- Response: Script ID (non-blocking) or script output (blocking)

### List Scripts
- URL: /api/scripts
- Method: GET
- Query Parameters: status (optional), orderBy (optional)
- Response: List of scripts with their statuses

### Get Script Details
- URL: /api/scripts/{id}
- Method: GET
- Response: Script details including stdout and stderr
### Stop a Script
- URL: /api/scripts/{id}
- Method: POST
- Response: No content

### Cleanup Scripts
- URL: /api/scripts/cleanup
- Method: DELETE
- Request Body: List of script IDs
- Response: No content

## Project Structure
graal-wrapper<br />
├── src<br />
│   ├── main<br />
│   │   ├── java<br />
│   │   │   ├── com.test-task.graal-wrapper<br />
│   │   │   │   ├── GraalWrapperApplication.java<br />
│   │   │   │   ├── controller<br />
│   │   │   │   │   └── ScriptController.java<br />
│   │   │   │   ├── model<br />
│   │   │   │   │   └── Script.java<br />
│   │   │   │   └── service<br />
│   │   │   │       └── ScriptService.java<br />
│   └── test<br />
│       └── java<br />
│           └── com.test-task.graal-wrapper<br />
│               ├── service<br />
│               │   └── ScriptServiceTest.java<br />
│               └── GraalWrapperApplicationTests.java<br />
├── pom.xml<br />
└── README.
