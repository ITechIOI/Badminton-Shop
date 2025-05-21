from pydantic import BaseModel
from typing import List
from typing import List, Dict, Any

class ProductItem(BaseModel):
    id: int
    name: str
    # description: str
    # imageUrl: str

class PredictionResponse(BaseModel):
    predicted_label: str
    similar_items: List[ProductItem]

class MatchResult(BaseModel):
    id: str
    score: float
    metadata: Dict[str, Any]