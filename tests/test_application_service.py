
import unittest

from src.services.application_service import create_application


class CreateApplicationTests(unittest.TestCase):

    def test_creates_application_in_draft_status(self):
        application = create_application(
            "John Smith",
            "STANDARD",
            "25000.00",
        )

        self.assertTrue(application["applicationId"])
        self.assertEqual(application["applicantName"], "John Smith")
        self.assertEqual(application["applicationType"], "STANDARD")
        self.assertEqual(application["requestedAmount"], "25000.00")
        self.assertEqual(application["status"], "DRAFT")
        self.assertEqual(
            application["validationStatus"],
            "NOT_VALIDATED",
        )

    def test_rejects_blank_applicant_name(self):
        with self.assertRaisesRegex(ValueError, "Applicant name"):
            create_application(" ", "STANDARD", "1000")

    def test_rejects_blank_application_type(self):
        with self.assertRaisesRegex(ValueError, "Application type"):
            create_application("John Smith", " ", "1000")

    def test_rejects_zero_or_negative_amount(self):
        for amount in ("0", "-100"):
            with self.subTest(amount=amount):
                with self.assertRaisesRegex(
                    ValueError,
                    "greater than zero",
                ):
                    create_application("John Smith", "STANDARD", amount)

    def test_rejects_invalid_amount(self):
        with self.assertRaisesRegex(ValueError, "valid number"):
            create_application("John Smith", "STANDARD", "not-a-number")


if __name__ == "__main__":
    unittest.main()
