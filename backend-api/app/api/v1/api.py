from fastapi import APIRouter
from app.api.v1.endpoints import operadoras, estatisticas

api_router = APIRouter()

api_router.include_router(operadoras.router, prefix="/operadoras", tags=["operadoras"])
api_router.include_router(estatisticas.router, prefix="/estatisticas", tags=["estatisticas"])