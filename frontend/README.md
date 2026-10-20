# APS Frontend

Responsive APS application dashboard with browser-only sample records. It has no package install, framework, or build step, and it does not call AWS in sample mode.

## Run locally

Open `index.html` in a browser, or open the `frontend` folder in VS Code and use the Live Server extension. The dashboard starts with illustrative sample records; draft applications created in sample mode stay in that browser's local storage.

To connect the create form later, set `API_BASE_URL` at the top of `app.js` to the API Gateway base URL. The backend endpoints currently provide `GET /applications`, `GET /applications/{applicationId}`, and `POST /applications`. The create body is `applicantName`, `applicationType`, and `requestedAmount`. Configure API Gateway CORS for the local frontend origin before connecting. When configured, the page loads the list and details from these read endpoints.

All displayed people, amounts, statuses, and reference numbers are illustrative and are not USDA records.

