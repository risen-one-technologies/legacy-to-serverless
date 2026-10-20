# USDA APS Serverless Modernization

This repository contains a serverless modernization demo for the USDA Application Processing System (APS). It includes Python Lambda handlers, AWS SAM infrastructure for the retained baseline stack, a separate Serverless Framework v3 preview deployment, and a static browser frontend.

> **Migration safeguard:** The existing SAM stack `aps-serverless-dev` is retained. Do not delete it until migration and cutover requirements have been confirmed and the responsible project owners have approved the change. The v3 preview shares the `aps-applications` DynamoDB table, so preview activity can read or write data used by the SAM deployment.

## Current deployment layout

| Component | Current configuration |
| --- | --- |
| Retained SAM stack | `aps-serverless-dev` (configured in `samconfig.toml`; AWS region `us-east-1`) |
| Serverless Framework preview stack | `aps-serverless-v3-preview`, stage `dev`, region `us-east-1` |
| Shared DynamoDB table | `aps-applications` |
| Preview API base URL | `https://6d5kt7gaoh.execute-api.us-east-1.amazonaws.com/dev` |
| Local frontend origin | `http://127.0.0.1:5500` |

The SAM template declares the `aps-applications` table and the SAM application resources. The Serverless Framework v3 configuration names the table in `APPLICATIONS_TABLE` and grants its functions table permissions; it does not declare a second DynamoDB table. Both stacks therefore depend on the same named table. The SAM configuration currently specifies Python 3.14 for its functions, while `serverless.yml` specifies Python 3.11 for the preview functions.

## Architecture

The static frontend in `frontend/` calls API Gateway at the preview API base URL. API Gateway routes requests to individual AWS Lambda handlers in `src/handlers/`. Application creation and retrieval use DynamoDB table `aps-applications`; the shared service module validates and constructs new application records. The list handler currently uses a DynamoDB `Scan`.

```text
Browser (http://127.0.0.1:5500)
           |
           v
API Gateway (preview, stage dev)
           |
           +-- GET  /health ----------------------> health Lambda
           +-- POST /applications ---------------> create Lambda --> DynamoDB
           +-- GET  /applications ---------------> list Lambda ----> DynamoDB
           +-- GET  /applications/{applicationId} -> get Lambda ----> DynamoDB
```

The API handlers set CORS headers for `http://127.0.0.1:5500`. Use that exact origin when serving the frontend locally; `localhost` or a different port is a different browser origin.

## API endpoints

Set the base URL to `https://6d5kt7gaoh.execute-api.us-east-1.amazonaws.com/dev` and append one of these paths:

| Method and path | Purpose |
| --- | --- |
| `GET /health` | Returns a basic APS health response. |
| `POST /applications` | Validates and stores a draft application; accepts JSON fields `applicantName`, `applicationType`, and `requestedAmount`. Returns the created record. |
| `GET /applications` | Returns application records from the table. |
| `GET /applications/{applicationId}` | Returns one record by its ID. |

New records are assigned a UUID application ID, `DRAFT` status, `NOT_VALIDATED` validation status, timestamps, and an initially empty AVS reference. Invalid input is returned as a client error; a missing record is returned as not found.

## Prerequisites

- AWS CLI configured with credentials for the intended AWS account and permission to inspect/deploy the relevant resources.
- AWS SAM CLI for working with the SAM template.
- Python matching the selected stack runtime and `pip` for Python tooling. The SAM template currently declares Python 3.14; the v3 preview declares Python 3.11.
- `pytest` and `boto3` for the Python test suite. Install Python dependencies as appropriate for your environment; `requirements.txt` currently lists `boto3` only.
- Node.js and npm for the Serverless Framework v3 commands. `package.json` declares `serverless` `^3.40.0` and the repository includes `package-lock.json`.
- A browser and a local static HTTP server for the frontend.

Before running any AWS command, verify the active AWS identity and region. Do not assume the currently selected credentials target the intended account.

## Run the frontend locally

From this repository, serve the `frontend` directory on port 5500 using your preferred static server (for example, VS Code Live Server configured for port 5500). Open:

```text
http://127.0.0.1:5500
```

The frontend's `API_BASE_URL` in `frontend/app.js` currently points to the deployed preview API. It loads the application list and retrieves selected details from that API; submitting the form creates a record in the shared DynamoDB table. Treat the displayed and entered data accordingly. The separate frontend README describes sample mode, but the current `app.js` has a non-empty API URL, so sample-only behavior requires changing that setting locally.

## Tests

Run the Python tests from the repository root:

```powershell
python -m pytest -q
```

Current reported result: **13 passed (2 subtests)**. This is the supplied current result; it has not been rerun while preparing this README.

## PowerShell API checks

These commands call the deployed preview API. The POST command writes a new record to the shared `aps-applications` table. Use only approved test data and an authorized AWS environment.

```powershell
$baseUrl = 'https://6d5kt7gaoh.execute-api.us-east-1.amazonaws.com/dev'

# Health check
Invoke-RestMethod -Method Get -Uri "$baseUrl/health"

# Create a test application (writes to DynamoDB)
$body = @{
    applicantName   = 'API Smoke Test'
    applicationType = 'Direct Loan'
    requestedAmount = '1000.00'
} | ConvertTo-Json
$created = Invoke-RestMethod -Method Post -Uri "$baseUrl/applications" `
    -ContentType 'application/json' -Body $body
$created

# List applications
Invoke-RestMethod -Method Get -Uri "$baseUrl/applications"

# Retrieve the record returned by the create request
Invoke-RestMethod -Method Get -Uri "$baseUrl/applications/$($created.applicationId)"
```

## Serverless Framework v3 preview commands

Run these from the repository root. The local dependency can be invoked with `npx` after dependencies are installed.

```powershell
# Render the resolved configuration; does not deploy
npx serverless print --stage dev

# Build the deployment package locally; does not deploy
npx serverless package --stage dev

# Deploy/update the preview stack in AWS
npx serverless deploy --stage dev
```

`serverless.yml` declares service/stack `aps-serverless-v3-preview`, stage `dev`, and region `us-east-1`. Review the resolved configuration and AWS account before any deploy. Deployment changes AWS resources. The preview uses the shared DynamoDB table, and its create endpoint writes to that table. Keep the SAM stack `aps-serverless-dev` intact until migration/cutover requirements are confirmed; do not run stack deletion or remove the table as part of preview work.

For the retained SAM stack, `samconfig.toml` sets the stack name to `aps-serverless-dev`, enables change-set confirmation, and sets region `us-east-1`. Always review the SAM template and proposed change set before applying it. A stack update or deletion can affect shared resources and application data.

## Repository map

- `src/handlers/` — Lambda entry points for health, create, list, and get operations.
- `src/services/application_service.py` — application validation and draft record construction.
- `tests/` — Python unit tests for service and handlers.
- `template.yaml` — AWS SAM infrastructure definition.
- `samconfig.toml` — SAM deployment defaults, including `aps-serverless-dev`.
- `serverless.yml` — Serverless Framework v3 preview service and function permissions/routes.
- `frontend/` — static APS dashboard.
- `events/` — sample Lambda event payloads.
