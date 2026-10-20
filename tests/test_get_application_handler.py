
import json
import os
import unittest
from unittest.mock import MagicMock, patch

from src.handlers.get_application import lambda_handler


class GetApplicationHandlerTests(unittest.TestCase):

    @patch.dict(os.environ, {"APPLICATIONS_TABLE": "aps-applications"})
    @patch("src.handlers.get_application.boto3.resource")
    def test_returns_application_when_found(self, mock_resource):
        mock_table = MagicMock()
        mock_resource.return_value.Table.return_value = mock_table
        mock_table.get_item.return_value = {
            "Item": {
                "applicationId": "app-123",
                "applicantName": "John Smith",
                "status": "DRAFT",
            }
        }

        response = lambda_handler(
            {"pathParameters": {"applicationId": "app-123"}},
            None,
        )

        self.assertEqual(response["statusCode"], 200)
        body = json.loads(response["body"])
        self.assertEqual(body["applicationId"], "app-123")
        self.assertEqual(body["applicantName"], "John Smith")

    @patch.dict(os.environ, {"APPLICATIONS_TABLE": "aps-applications"})
    @patch("src.handlers.get_application.boto3.resource")
    def test_returns_404_when_application_not_found(self, mock_resource):
        mock_table = MagicMock()
        mock_resource.return_value.Table.return_value = mock_table
        mock_table.get_item.return_value = {}

        response = lambda_handler(
            {"pathParameters": {"applicationId": "missing-id"}},
            None,
        )

        self.assertEqual(response["statusCode"], 404)

    def test_returns_400_when_application_id_is_missing(self):
        with patch.dict(os.environ, {"APPLICATIONS_TABLE": "aps-applications"}):
            response = lambda_handler({}, None)

        self.assertEqual(response["statusCode"], 400)


if __name__ == "__main__":
    unittest.main()
