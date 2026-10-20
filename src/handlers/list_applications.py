
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

    try:
        table = boto3.resource("dynamodb").Table(table_name)
        response = table.scan()
        applications = response.get("Items", [])

        return {
            "statusCode": 200,
            "headers": CORS_HEADERS,
            "body": json.dumps(applications),
        }

    except Exception:
        return error_response(500, "Unable to list applications.")


def error_response(status_code, message):
    return {
        "statusCode": status_code,
        "headers": CORS_HEADERS,
        "body": json.dumps({"error": message}),
    }
