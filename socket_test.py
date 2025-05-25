import requests
import json
from datetime import datetime, timedelta

# Configuration
BASE_URL = "http://localhost:8080/api/v1"
HEADERS = {"Content-Type": "application/json"}

def check_active_auctions():
    """Check for active auctions"""
    try:
        response = requests.get(f"{BASE_URL}/auctions/active", headers=HEADERS)
        if response.status_code == 200:
            auctions = response.json()
            if auctions:
                print("Active Auctions:")
                for auction in auctions:
                    print(f"ID: {auction.get('id')}, Item: {auction.get('name')}, "
                          f"Current Price: {auction.get('currentPrice')}")
                return True
        return False
    except Exception as e:
        print(f"Error checking active auctions: {e}")
        return False

def create_test_auction():
    """Create a test auction item"""
    test_item = {
        "name": "Test Item " + datetime.now().strftime("%H%M%S"),
        "description": "Test auction item created by script",
        "startingPrice": 10.0,
        "minIncrement": 1.0,
        "endTime": (datetime.utcnow() + timedelta(hours=1)).isoformat() + "Z"
    }

    try:
        response = requests.post(
            f"{BASE_URL}/auctions",
            headers=HEADERS,
            data=json.dumps(test_item)
        )

        if response.status_code in (200, 201):
            print(f"Created test auction: {response.json().get('name')}")
            return True
        else:
            print(f"Failed to create test auction: {response.text}")
            return False
    except Exception as e:
        print(f"Error creating test auction: {e}")
        return False

def main():
    print("Checking for active auctions...")
    if not check_active_auctions():
        print("No active auctions found. Creating a test auction...")
        if create_test_auction():
            print("Successfully created a test auction.")
        else:
            print("Failed to create test auction.")
    else:
        print("Active auctions found. No need to create a test auction.")

if __name__ == "__main__":
    main()