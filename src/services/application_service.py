
from datetime import datetime, timezone
from decimal import Decimal
from uuid import uuid4


def create_application(applicant_name, application_type, requested_amount):
    """Create a new APS application in DRAFT status."""

    if not applicant_name or not applicant_name.strip():
        raise ValueError("Applicant name is required.")

    if not application_type or not application_type.strip():
        raise ValueError("Application type is required.")

    try:
        amount = Decimal(str(requested_amount))
    except Exception as exc:
        raise ValueError("Requested amount must be a valid number.") from exc

    if not amount.is_finite() or amount <= 0:
        raise ValueError("Requested amount must be greater than zero.")

    now = datetime.now(timezone.utc).isoformat()

    return {
        "applicationId": str(uuid4()),
        "applicantName": applicant_name.strip(),
        "applicationType": application_type.strip(),
        "requestedAmount": str(amount),
        "status": "DRAFT",
        "validationStatus": "NOT_VALIDATED",
        "avsReference": None,
        "createdAt": now,
        "updatedAt": now,
    }
