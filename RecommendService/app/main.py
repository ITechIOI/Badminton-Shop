from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.product import router as menu_router

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(menu_router)

@app.get("/recommend/search-similar")
def search_similar_food():
    return "search-similar endpoint is working"

