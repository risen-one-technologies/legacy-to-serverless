
import json
import os
import unittest
from unittest.mock import MagicMock, patch

from src.handlers.list_applications import lambda_handler


class ListApplicationsHandlerTests(unittest.TestCase):

    @patch.dict(os.environ, {"APPLICATIONS_TABLE": "aps-applications"})
    @patch("src.handlers.list_applications.boto3.resource")
    def test_returns_applications(self, mock_resource):
        mock_table = MagicMock()
        mock_resource.return_value.Table.return_value = mock_table
        mock_table.scan.return_value = {
            "Items": [
                {
                    "applicationId": "app-123",
                    "applicantName": "John Smith",
                    "status": "DRAFT",
                },
                {
                    "applicationId": "app-456",
                    "applicantName": "Jane Smith",
                    "status": "VALIDATED",
                },
            ]
        }

        response = lambda_handler({}, None)

        self.assertEqual(response["statusCode"], 200)
        applications = json.loads(response["body"])
        self.assertEqual(len(applications), 2)
        mock_table.scan.assert_called_once()

    @patch.dict(os.environ, {"APPLICATIONS_TABLE": "aps-applications"})
    @patch("src.handlers.list_applications.boto3.resource")
    def test_returns_empty_list_when_no_applications(self, mock_resource):
        mock_table = MagicMock()
        mock_resource.return_value.Table.return_value = mock_table
        mock_table.scan.return_value = {"Items": []}

        response = lambda_handler({}, None)

        self.assertEqual(response["statusCode"], 200)
        self.assertEqual(json.loads(response["body"]), [])


if __name__ == "__main__":
    unittest.main()
