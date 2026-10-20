
import json


def lambda_handler(event, context):
    """Return a basic health status for the APS API."""
    return {
        "statusCode": 200,
        "headers": {
            "Content-Type": "application/json"
        },
        "body": json.dumps({
            "application": "APS",
            "status": "UP"
        })
    }
