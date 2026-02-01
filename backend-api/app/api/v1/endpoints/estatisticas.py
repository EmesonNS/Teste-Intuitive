from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.schemas.stats import StatsResponse
from app.repository.operadora_repo import OperadoraRepository

router = APIRouter()

@router.get("/", response_model=StatsResponse)
def read_statistics(db: Session = Depends(get_db)):
    return OperadoraRepository.get_statistics(db)