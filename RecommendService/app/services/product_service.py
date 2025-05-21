import requests
from fastapi import HTTPException
from app.core.config import settings

def get_menu_items():
    graphql_endpoint = settings.NESTJS_MENU_ENDPOINT
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.get(graphql_endpoint, headers=headers)

        # 🔍 In lỗi nếu có
        print("Status code:", response.status_code)
        print("Response:", response.text)

        response.raise_for_status()
        print("Response JSON:", response.json())

        return response.json()

    except requests.RequestException as e:
        raise HTTPException(status_code=502, detail=f"Cannot connect to Spring Boot Product-Service: {str(e)}")


def filter_menu_by_ids(menu_data, ids):
    id_set = set(ids)  
    return [item for item in menu_data if str(item.get("id")) in id_set]