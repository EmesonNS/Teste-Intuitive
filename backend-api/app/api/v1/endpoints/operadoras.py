from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from app.db.session import get_db
from app.schemas.operadora import OperadoraBase, OperadoraDetail, OperadoraResponse, DespesaBase, DespesaPagination
from app.repository.operadora_repo import OperadoraRepository

router = APIRouter()

@router.get("/", response_model=OperadoraResponse)
def read_operadoras(
    db: Session = Depends(get_db),
    page: int = Query(1, ge=1),
    limit: int = Query(10, ge=1, le=100),
    search: Optional[str] = None
):
    skip = (page - 1) * limit
    operadoras, total = OperadoraRepository.get_all(db, skip=skip, limit=limit, search=search)

    return {
        "data": operadoras,
        "total": total,
        "page": page,
        "limit" : limit
    }

@router.get("/{cnpj}", response_model=OperadoraBase)
def read_operadora_detail(cnpj: str, db: Session = Depends(get_db)):
    operadora = OperadoraRepository.get_by_cnpj(db, cnpj)
    if not operadora:
        raise HTTPException(status_code=404, detail="Operadora não encontrada")
    return operadora

@router.get("/{cnpj}/despesas", response_model=DespesaPagination)
def read_operadora_despesas(
    cnpj: str,
    db: Session = Depends(get_db),
    page: int = Query(1, ge=1),
    limit: int = Query(10, ge=1, le=100),
    search: Optional[str] = None
):
    skip = (page - 1) * limit
    despesas, total = OperadoraRepository.get_despesas_by_cnpj(db, cnpj, skip=skip, limit=limit, search=search)

    if despesas is None and total == 0:
        pass
    
    return {
        "data": despesas,
        "total": total,
        "page": page,
        "limit": limit
    }