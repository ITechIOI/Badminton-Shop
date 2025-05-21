from fastapi import APIRouter, File, UploadFile, HTTPException
from app.services.inference import image_to_embedding
from app.services.vector_search import query_pinecone
from app.services.product_service import get_menu_items, filter_menu_by_ids
from app.schema.response import PredictionResponse
from fastapi import Form
import numpy as np

router = APIRouter(
    prefix="/recommend",
    tags=['Recommend']
)

# tính toán điểm tin cậy cho model
def convert_matches(matches):
    converted = []
    for match in matches:
        converted.append({
            "id": match["id"],
            "score": float(match.get("score", 0.0)), 
           # "metadata": match.get("metadata", {})
        })
    return converted

# Trả về dữ liệu dự đoán có điểm tin cậy - score
@router.post("/predict/score")
async def predict_food(file: UploadFile = File(...), limit: int = Form(...)):
    if not file.filename.lower().endswith((".jpg", ".jpeg", ".png")):
        raise HTTPException(status_code=400, detail="Invalid file format")

    image_bytes = await file.read()
    try:
        embedding = image_to_embedding(image_bytes)
        embedding = np.array(embedding)
        print('Embedding: ', embedding)
        pinecone_result = query_pinecone(embedding, top_k=limit)
        matches = pinecone_result["matches"]
        return convert_matches(matches)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Chỉ trả về dữ liệu dự đoán
@router.post("/predict", response_model=PredictionResponse)
async def predict_food(file: UploadFile = File(...), limit: int = Form(...)):
    if not file.filename.lower().endswith((".jpg", ".jpeg", ".png")):
        raise HTTPException(status_code=400, detail="Invalid file format")

    image_bytes = await file.read()
    try:
        embedding = image_to_embedding(image_bytes)
        pinecone_result = query_pinecone(embedding, top_k=limit)
        matches = pinecone_result["matches"]
        
        matches = pinecone_result["matches"]
        if not matches:
            raise HTTPException(status_code=404, detail="Không tìm thấy sản phẩm phù hợp trong Pinecone.")

        ids = [match["id"] for match in pinecone_result["matches"]]
        predicted_label = pinecone_result["matches"][0]["metadata"].get("label", "Unknown")

        product_items = get_menu_items()
       # print("List menu", menu_items)
        filtered_items = filter_menu_by_ids(product_items, ids)

        return PredictionResponse(predicted_label=predicted_label, similar_items=filtered_items)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))