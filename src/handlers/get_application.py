
import json
import os

import boto3


CORS_HEADERS = {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "http://127.0.0.1:5500",
    "Access-Control-Allow-Headers": "Content-Type,Authorization",
    "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
}


def lambda_handler(event, context):
    table_name = os.environ.get("APPLICATIONS_TABLE")

    if not table_name:
        return error_response(
            500,
            "APPLICATIONS_TABLE environment variable is not configured.",
        )

    application_id = (
        event.get("pathParameters") or {}
    ).get("applicationId")

    if not application_id:
        return error_response(400, "Application ID is required.")

    try:
        table = boto3.resource("dynamodb").Table(table_name)
        response = table.get_item(
            Key={"applicationId": application_id}
        )

        application = response.get("Item")

        if not application:
            return error_response(404, "Application not found.")

        return {
            "statusCode": 200,
            "headers": CORS_HEADERS,
            "body": json.dumps(application),
        }

    except Exception:
        return error_response(500, "Unable to retrieve application.")


def error_response(status_code, message):
    return {
        "statusCode": status_code,
        "headers": CORS_HEADERS,
        "body": json.dumps({"error": message}),
    }
