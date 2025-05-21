import requests
import json
import numpy as np
from pinecone import Pinecone
from app.core.config import settings
from fastapi import HTTPException

# ✅ Khởi tạo Pinecone SDK mới
pc = Pinecone(api_key=settings.PINECONE_API_KEY)

# ✅ Truy cập index
index = pc.Index(settings.PINECONE_INDEX)

# ✅ Lấy index theo tên
index = pc.Index(settings.PINECONE_INDEX)

# ✅ Gọi GraphQL từ NestJS để lấy danh sách món ăn
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

# ✅ Gửi image URL tới Jina AI để lấy embedding vector
def encode_image_from_url(image_url: str):
    payload = {
        "model": "jina-clip-v2",
        "dimensions": 512,
        "input": [{"image": image_url}],
        "embedding_type": "float",
        "normalized": True
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {settings.JINA_API_TOKEN}",
    }

    response = requests.post("https://api.jina.ai/v1/embeddings", headers=headers, data=json.dumps(payload))

    if response.status_code == 200:
        result = response.json()
        embedding = result["data"][0].get("embedding")
        if embedding is None or len(embedding) == 0:
            raise ValueError("❌ Không có embedding trả về.")
        return np.array(embedding)
    else:
        print(f"❌ API trả về lỗi {response.status_code}")
        print(response.text)
        return None

# ✅ Tạo và upsert vector vào Pinecone
def seed():
    menu_items = get_menu_items()
    vectors = []

    for item in menu_items:
        try:
            image_url = item["imageUrl"].split(" ")[0]  # ⚠️ xử lý nếu imageUrl chứa nhiều URL
            vector = encode_image_from_url(image_url)
            if vector is not None:
                vectors.append({
                    "id": str(item["id"]),
                    "values": vector.tolist(),
                    "metadata": {
                        "label": item["name"]
                    }
                })
                print(f"✅ Đã thêm vector cho: {item['name']}")
            else:
                print(f"⚠️ Bỏ qua: {item['name']}")
        except Exception as e:
            print(f"❌ Lỗi khi xử lý {item['name']}: {e}")

    if vectors:
        index.upsert(vectors=vectors)
        print(f"\n🌟 Đã upsert {len(vectors)} vectors vào Pinecone thành công.")
    else:
        print("⚠️ Không có vector nào được chèn.")

if __name__ == "__main__":
    seed()
