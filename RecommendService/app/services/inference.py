from app.core.config import settings  
import base64
import json
import requests

def image_to_embedding(image_bytes: bytes):
    encoded_image = base64.b64encode(image_bytes).decode("utf-8")

    payload = {
        "model": "jina-clip-v2",
        "dimensions": 512,
        "input": [
            {
                "image": encoded_image
            },
        ],
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
        if not embedding:
            raise ValueError("❌ Không có embedding trả về.")
        return embedding
    else:
        print(f"❌ API trả về lỗi {response.status_code}")
        print(response.text)
        return None

