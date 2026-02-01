from pydantic import BaseModel, ConfigDict
from typing import List, Optional
from datetime import datetime

class DespesaBase(BaseModel):
    trimestre: int
    ano: int
    valor: float
    descricao: str
    data_carga: datetime

    model_config = ConfigDict(from_attributes=True)

class DespesaPagination(BaseModel):
    data: List[DespesaBase]
    total: int
    page: int
    limit: int

class OperadoraBase(BaseModel):
    registro_ans: str
    cnpj: str
    razao_social: str
    modalidade: str
    uf: str

    model_config = ConfigDict(from_atributes=True)

class OperadoraDetail(OperadoraBase):
    despesas: List[DespesaBase] = []

class OperadoraResponse(BaseModel):
    data: List[OperadoraBase]
    total: int
    page: int
    limit: int