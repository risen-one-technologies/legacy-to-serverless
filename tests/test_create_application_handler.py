
import json
import os
import unittest
from unittest.mock import patch, MagicMock

from src.handlers.create_application import lambda_handler


class CreateApplicationHandlerTests(unittest.TestCase):

    @patch.dict(os.environ, {"APPLICATIONS_TABLE": "aps-applications"})
    @patch("src.handlers.create_application.boto3.resource")
    def test_creates_application_from_json_body(self, mock_resource):
        mock_table = MagicMock()
        mock_resource.return_value.Table.return_value = mock_table

        event = {
            "body": json.dumps({
                "applicantName": "John Smith",
                "applicationType": "STANDARD",
                "requestedAmount": "25000.00",
            })
        }

        response = lambda_handler(event, None)

        self.assertEqual(response["statusCode"], 201)

        application = json.loads(response["body"])
        self.assertEqual(application["applicantName"], "John Smith")
        self.assertEqual(application["status"], "DRAFT")
        self.assertTrue(application["applicationId"])

        mock_table.put_item.assert_called_once()
        saved_item = mock_table.put_item.call_args.kwargs["Item"]
        self.assertEqual(saved_item["applicantName"], "John Smith")

    def test_rejects_invalid_json(self):
        response = lambda_handler({"body": "{invalid"}, None)
        self.assertEqual(response["statusCode"], 400)

    def test_rejects_missing_applicant_name(self):
        event = {
            "body": json.dumps({
                "applicationType": "STANDARD",
                "requestedAmount": "1000",
            })
        }

        response = lambda_handler(event, None)
        self.assertEqual(response["statusCode"], 400)


if __name__ == "__main__":
    unittest.main()
