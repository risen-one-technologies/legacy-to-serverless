
import json
import os

import boto3

from src.services.application_service import create_application


CORS_HEADERS = {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "http://127.0.0.1:5500",
    "Access-Control-Allow-Headers": "Content-Type,Authorization",
    "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
}


def lambda_handler(event, context):
    try:
        body = event.get("body")

        if isinstance(body, str):
            body = json.loads(body)
        elif body is None:
            body = {}

        if not isinstance(body, dict):
            raise ValueError("Request body must be a JSON object.")

        application = create_application(
            applicant_name=body.get("applicantName"),
            application_type=body.get("applicationType"),
            requested_amount=body.get("requestedAmount"),
        )

        table_name = os.environ.get("APPLICATIONS_TABLE")

        if not table_name:
            return error_response(
                500,
                "APPLICATIONS_TABLE environment variable is not configured.",
            )

        dynamodb = boto3.resource("dynamodb")
        table = dynamodb.Table(table_name)
        table.put_item(Item=application)

        return {
            "statusCode": 201,
            "headers": CORS_HEADERS,
            "body": json.dumps(application),
        }

    except json.JSONDecodeError:
        return error_response(400, "Request body must contain valid JSON.")

    except ValueError as exc:
        return error_response(400, str(exc))

    except Exception:
        return error_response(500, "Unable to save application.")


def error_response(status_code, message):
    return {
        "statusCode": status_code,
        "headers": CORS_HEADERS,
        "body": json.dumps({"error": message}),
    }
